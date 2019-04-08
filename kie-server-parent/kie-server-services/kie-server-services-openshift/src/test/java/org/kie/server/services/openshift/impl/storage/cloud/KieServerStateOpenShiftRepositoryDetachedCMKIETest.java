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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.kie.server.api.KieServerConstants.KIE_SERVER_ID;
import static org.kie.server.api.KieServerConstants.KIE_SERVER_LOCATION;
import static org.kie.server.services.openshift.api.KieServerOpenShiftConstants.CFG_MAP_LABEL_SERVER_STATE_VALUE_USED;

public class KieServerStateOpenShiftRepositoryDetachedCMKIETest extends KieServerStateOpenShiftRepositoryTest {

    @Test
    public void testDetachedKieServerCMatKieServerEnv() {
        // By setting org.kie.server.id property, simulates a KIE server runtime. (isKieServerRuntime -> true)
        System.setProperty(KIE_SERVER_ID, "myapp2-kieserver-detached-kie");
        System.setProperty(KIE_SERVER_LOCATION, "http://myapp2-kieserver-5-myproject.127.0.0.1.nip.io:80/services/rest/server");
        createDummyDCandRC("myapp2-kieserver-5", "myapp2-kieserver-detached-kie", UUID.randomUUID().toString());
        ConfigMap cfm = client.configMaps()
                .load(KieServerStateOpenShiftRepositoryTest.class
                .getResourceAsStream("/test-kieserver-state-config-map-detached-kie.yml")).get();
    
        repo.createOrReplaceCM(client, cfm);
        // When KIE server loads 'DETACHED' KieServerCM, should change it to 'USED' and adding server location. 
        KieServerState state = repo.load("myapp2-kieserver-detached-kie");
        ConfigMap updatedCfm = client.configMaps().inNamespace(testNamespace)
                                     .withName("myapp2-kieserver-5").get();
        
        assertNotNull(state.getConfiguration().getConfigItem(KIE_SERVER_LOCATION));
        assertTrue(updatedCfm.getMetadata().getLabels().containsValue(CFG_MAP_LABEL_SERVER_STATE_VALUE_USED));
    }
}
