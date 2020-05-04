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
import org.kie.server.api.model.KieContainerResource;
import org.kie.server.api.model.KieServerConfigItem;
import org.kie.server.services.impl.storage.KieServerState;
import org.kie.server.services.impl.storage.KieServerStateRepository;
import org.kie.soup.xstream.XStreamUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.kie.server.api.KieServerConstants.KIE_SERVER_ID;
import static org.kie.server.services.openshift.api.KieServerOpenShiftConstants.CFG_MAP_DATA_KEY;

public abstract class KieServerStateCloudRepository implements KieServerStateRepository, CloudClientFactory {

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
            kssServerId = kieServerState.getConfiguration().getConfigItemValue(KIE_SERVER_ID);
        } catch (Exception e) {
            logger.error("Failed to retrieve server id from KieServerState", e);
        }
        if (kssServerId == null || kssServerId.length() == 0) {
            throw new IllegalArgumentException("Invalid KieServerId: Can not be null or empty.");
        }
        return kssServerId;
    }
}
