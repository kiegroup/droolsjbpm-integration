/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
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

import java.util.Calendar;
import java.util.concurrent.TimeoutException;

import org.junit.After;
import org.junit.Before;
import org.kie.server.api.model.KieContainerResource;
import org.kie.server.api.model.KieContainerResourceList;
import org.kie.server.api.model.KieContainerStatus;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.controller.client.KieServerControllerClient;
import org.kie.server.controller.client.KieServerMgmtControllerClient;
import org.kie.server.integrationtests.config.TestConfig;
import org.kie.server.integrationtests.shared.RestOnlyBaseIntegrationTest;

public abstract class KieControllerManagementBaseTest extends RestOnlyBaseIntegrationTest {

    private static final long SYNCHRONIZATION_TIMEOUT = 30000;
    private static final long TIMEOUT_BETWEEN_CALLS = 200;

    protected KieServerMgmtControllerClient controllerClient;

    @Before
    public void createControllerClient() {
        if (TestConfig.isLocalServer()) {
            controllerClient = new KieServerMgmtControllerClient(TestConfig.getControllerHttpUrl(), null, null);
        } else {
            controllerClient = new KieServerMgmtControllerClient(TestConfig.getControllerHttpUrl(), TestConfig.getUsername(), TestConfig.getPassword());
        }
        controllerClient.setMarshallingFormat(marshallingFormat);
    }

    @After
    public void closeControllerClient() {
        controllerClient.close();
    }

    protected void waitForKieServerSynchronization(int numberOfExpectedContainers) throws Exception {
        long timeoutTime = Calendar.getInstance().getTimeInMillis() + SYNCHRONIZATION_TIMEOUT;
        while(Calendar.getInstance().getTimeInMillis() < timeoutTime) {
            ServiceResponse<KieContainerResourceList> containersList = client.listContainers();

            // If synchronization finished (number of containers same as expected) then return.
            if (containersList.getResult().getContainers() == null) {
                if(numberOfExpectedContainers == 0) {
                    return;
                }
            } else if (numberOfExpectedContainers == containersList.getResult().getContainers().size()) {
                // Check that all containers are created or disposed.
                boolean containersInitializing = false;
                for (KieContainerResource container : containersList.getResult().getContainers()) {
                    if (KieContainerStatus.CREATING.equals(container.getStatus()) ||
                            KieContainerStatus.DISPOSING.equals(container.getStatus())) {
                        containersInitializing = true;
                    }
                }
                if (!containersInitializing) {
                    return;
                }
            }

            Thread.sleep(TIMEOUT_BETWEEN_CALLS);
        }
        throw new TimeoutException("Timeout while waiting for kie server synchronization.");
    }
}
