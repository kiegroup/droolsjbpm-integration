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

import org.junit.Test;
import org.kie.server.api.KieServerConstants;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class KieServerStateOpenShiftRepositoryEnhancedTest extends KieServerStateOpenShiftRepositoryTest {

    @Test(expected = IllegalStateException.class)
    public void testLoadWithNoInitCMandMissMatchedServerId() {
        assertNotNull(client.configMaps().withName(TEST_KIE_SERVER_ID).delete());
        System.setProperty(KieServerConstants.KIE_SERVER_ID, TEST_KIE_SERVER_ID);
        assertEquals(TEST_KIE_SERVER_ID, repo.load(TEST_KIE_SERVER_ID)
                                             .getConfiguration().getConfigItemValue(KieServerConstants.KIE_SERVER_ID));

        System.setProperty(KieServerConstants.KIE_SERVER_ID, "wrongId");
        repo.load(TEST_KIE_SERVER_ID)
            .getConfiguration().getConfigItemValue(KieServerConstants.KIE_SERVER_ID);
    }
}
