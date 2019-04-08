/**
 * Copyright (C) 2019 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.server.services.openshift.impl.storage.cloud;

import java.util.UUID;

import io.fabric8.kubernetes.api.model.ConfigMap;
import org.junit.Test;
import org.kie.server.services.impl.storage.KieServerState;

import static org.junit.Assert.assertTrue;
import static org.kie.server.api.KieServerConstants.KIE_SERVER_LOCATION;
import static org.kie.server.services.openshift.api.KieServerOpenShiftConstants.CFG_MAP_LABEL_SERVER_STATE_VALUE_DETACHED;

public class KieServerStateOpenShiftRepositoryDetachedCMBCTest extends KieServerStateOpenShiftRepositoryTest {

    @Test
    public void testDetachedKieServerCMatBCEnv() {
        createDummyDCandRC("myapp2-kieserver-6", "myapp2-kieserver-detached-bc", UUID.randomUUID().toString());
        ConfigMap cfm = client.configMaps()
                .load(KieServerStateOpenShiftRepositoryTest.class
                .getResourceAsStream("/test-kieserver-state-config-map-detached-bc.yml")).get();
    
        repo.createOrReplaceCM(client, cfm);
        // When BC/WB loads 'DETACHED' KieServerCM, should remove KIE server location config item. 
        KieServerState state = repo.load("myapp2-kieserver-detached-bc");
        ConfigMap updatedCfm = client.configMaps().inNamespace(testNamespace)
                                     .withName("myapp2-kieserver-6").get();
        
        assertTrue(state.getConfiguration().getConfigItem(KIE_SERVER_LOCATION) == null);
        assertTrue(updatedCfm.getMetadata().getLabels().containsValue(CFG_MAP_LABEL_SERVER_STATE_VALUE_DETACHED));
    }
}
