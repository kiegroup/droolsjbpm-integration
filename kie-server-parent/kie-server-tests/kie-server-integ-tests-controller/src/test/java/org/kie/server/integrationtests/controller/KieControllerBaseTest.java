/*
 * Copyright 2015 JBoss Inc
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

package org.kie.server.integrationtests.controller;

import org.junit.After;
import org.junit.Before;
import org.kie.server.controller.client.KieServerControllerClient;
import org.kie.server.integrationtests.config.TestConfig;
import org.kie.server.integrationtests.shared.RestOnlyBaseIntegrationTest;

public abstract class KieControllerBaseTest extends RestOnlyBaseIntegrationTest {

    protected KieServerControllerClient controllerClient;

    @Before
    public void createControllerClient() {
        if (TestConfig.isLocalServer()) {
            controllerClient = new KieServerControllerClient(TestConfig.getControllerHttpUrl(), null, null);
        } else {
            controllerClient = new KieServerControllerClient(TestConfig.getControllerHttpUrl(), TestConfig.getUsername(), TestConfig.getPassword());
        }
        controllerClient.setMarshallingFormat(marshallingFormat);
    }

    @After
    public void closeControllerClient() {
        controllerClient.close();
    }
}
