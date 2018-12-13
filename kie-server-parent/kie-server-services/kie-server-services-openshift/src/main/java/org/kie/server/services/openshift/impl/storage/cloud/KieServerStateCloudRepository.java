/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package org.kie.server.services.openshift.impl.storage.cloud;

import java.util.function.Function;

import javax.validation.constraints.NotNull;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.reflection.PureJavaReflectionProvider;
import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.kie.server.api.KieServerConstants;
import org.kie.server.api.model.KieContainerResource;
import org.kie.server.api.model.KieServerConfigItem;
import org.kie.server.services.impl.storage.KieServerState;
import org.kie.server.services.impl.storage.KieServerStateRepository;
import org.kie.server.services.openshift.api.KieServerReadinessProbe;
import org.kie.soup.commons.xstream.XStreamUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KieServerStateCloudRepository implements KieServerStateRepository,
                                           KieServerReadinessProbe, CloudClientFactory {

    public static final String ROLLOUT_REQUIRED = "services.server.kie.org/openshift-startup-strategy.rolloutRequired";
    public static final String CFG_MAP_DATA_KEY = "kie-server-state";
    public static final String CFG_MAP_LABEL_NAME = "services.server.kie.org/kie-server-state";
    public static final String CFG_MAP_LABEL_VALUE = "USED";
    
    protected static final String STATE_CHANGE_TIMESTAMP = "services.server.kie.org/kie-server-state.changeTimestamp";
    private static final Logger logger = LoggerFactory.getLogger(KieServerStateCloudRepository.class);

    protected final XStream xs;
    
    public static XStream initializeXStream() {
        XStream xs = XStreamUtils.createTrustingXStream(new PureJavaReflectionProvider());
        String[] voidDeny = {"void.class", "Void.class"};
        xs.denyTypes(voidDeny);
        xs.alias(CFG_MAP_DATA_KEY, KieServerState.class);
        xs.alias("container", KieContainerResource.class);
        xs.alias("config-item", KieServerConfigItem.class);

        return xs;
    }

    public KieServerStateCloudRepository() {
        xs = initializeXStream();
    }

    @Override
    @NotNull
    public KieServerState load(@NotNull String serverId) {
        KieServerState kieServerState = processKieServerState(client -> {
            ConfigMap cm = client.configMaps().withName(serverId).get();
            if (cm == null) {
                throw new IllegalStateException(("KieServerId: [" + serverId + "], not found. Please create an associated ConfigMap with configuration first."));
            }
            return (KieServerState) xs.fromXML(cm.getData().get(CFG_MAP_DATA_KEY));
        });

        if (kieServerState == null || !retrieveKieServerId(kieServerState).equals(serverId)) {
            throw new IllegalStateException("Invalid KieServerId: " + serverId + ", or inconsistent state data.");
        }

        return kieServerState;
    }

    @Override
    public void store(String serverId, KieServerState kieServerState) {
        /**
         * To be implemented for supporting pure Kubernetes cluster deployment.
         */
        throw new UnsupportedOperationException();
    }

    protected String retrieveKieServerId(KieServerState kieServerState) {
        String kssServerId = null;
        try {
            kssServerId = kieServerState.getConfiguration().getConfigItemValue(KieServerConstants.KIE_SERVER_ID);
        } catch (Exception e) {
            logger.error("Failed to retrieve server id from KieServerState", e);
        }
        if (kssServerId == null || kssServerId.length() == 0) {
            throw new IllegalArgumentException("Invalid KieServerId: Can not be null or empty.");
        }
        return kssServerId;
    }

    private <R> R processKieServerState(Function<KubernetesClient, R> func) {
        R result = null;
        try (KubernetesClient client = createKubernetesClient()) {
            result = func.apply(client);
        } catch (IllegalStateException ise) {
            logger.error("Processing KieServerState failed.", ise);
            throw ise;
        } catch (Exception e) {
            logger.error("Processing KieServerState failed.", e);
        }
        return result;
    }
}
