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

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import javax.ws.rs.core.Configuration;

import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.junit.After;
import org.junit.Before;
import org.kie.server.controller.client.KieServerControllerClient;
import org.kie.server.controller.client.KieServerControllerClientFactory;
import org.kie.server.integrationtests.config.TestConfig;
import org.kie.server.integrationtests.shared.basetests.RestOnlyBaseIntegrationTest;

public abstract class KieControllerManagementBaseTest extends RestOnlyBaseIntegrationTest {

    protected KieServerControllerClient controllerClient;

    @Before
    public void createControllerClient() {
        final Configuration configuration =
                new ResteasyClientBuilder()
                        .establishConnectionTimeout(10,
                                                    TimeUnit.SECONDS)
                        .socketTimeout(60,
                                       TimeUnit.SECONDS)
                        .getConfiguration();
        if (TestConfig.isLocalServer()) {
            controllerClient = KieServerControllerClientFactory.newRestClient(TestConfig.getControllerHttpUrl(),
                                                                              null,
                                                                              null,
                                                                              marshallingFormat,
                                                                              configuration);
        } else {
            controllerClient = KieServerControllerClientFactory.newRestClient(TestConfig.getControllerHttpUrl(),
                                                                              TestConfig.getUsername(),
                                                                              TestConfig.getPassword(),
                                                                              marshallingFormat,
                                                                              configuration);
        }
    }

    @Before
    @Override
    public void setup() throws Exception {
        super.setup();
        disposeAllContainers();
        disposeAllServerInstances();
    }

    @After
    public void closeControllerClient() {
        if (controllerClient != null) {
            try {
                logger.info("Closing Kie Server Management Controller client");
                controllerClient.close();
            } catch (IOException e) {
                logger.error("Error trying to close Kie Server Management Controller Client: {}",
                             e.getMessage(),
                             e);
            }
        }
    }
}
