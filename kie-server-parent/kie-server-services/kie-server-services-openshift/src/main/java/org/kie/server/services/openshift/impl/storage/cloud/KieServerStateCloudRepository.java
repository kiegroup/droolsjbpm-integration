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

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.reflection.PureJavaReflectionProvider;
import org.kie.server.api.KieServerConstants;
import org.kie.server.api.model.KieContainerResource;
import org.kie.server.api.model.KieServerConfigItem;
import org.kie.server.services.impl.storage.KieServerState;
import org.kie.server.services.impl.storage.KieServerStateRepository;
import org.kie.server.services.openshift.api.KieServerReadinessProbe;
import org.kie.soup.commons.xstream.XStreamUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class KieServerStateCloudRepository implements KieServerStateRepository,
                                                    KieServerReadinessProbe, CloudClientFactory {

    public static final String ROLLOUT_REQUIRED = "services.server.kie.org/openshift-startup-strategy.rolloutRequired";
    public static final String CFG_MAP_DATA_KEY = "kie-server-state";
    public static final String CFG_MAP_LABEL_NAME = "services.server.kie.org/kie-server-state";
    public static final String CFG_MAP_LABEL_VALUE_USED = "USED";
    public static final String CFG_MAP_LABEL_VALUE_IMMUTABLE = "IMMUTABLE";

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
}
