/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.server.integrationtests.controller;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Before;
import org.junit.Test;
import org.junit.runners.Parameterized;
import org.kie.server.api.marshalling.MarshallingFormat;
import org.kie.server.client.KieServicesConfiguration;
import org.kie.server.controller.api.model.runtime.ServerInstanceKey;
import org.kie.server.controller.api.model.runtime.ServerInstanceKeyList;
import org.kie.server.controller.api.model.spec.ServerTemplate;
import org.kie.server.controller.client.KieServerControllerClientFactory;
import org.kie.server.controller.client.exception.KieServerControllerClientException;
import org.kie.server.integrationtests.config.TestConfig;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

public class WebSocketKieControllerManagementIntegrationTest extends KieControllerManagementIntegrationTest<KieServerControllerClientException> {

    @Override
    protected void assertNotFoundException(KieServerControllerClientException e) {
        assertNotNull(e.getMessage());
    }

    @Override
    protected void assertBadRequestException(KieServerControllerClientException e) {
        assertNotNull(e.getMessage());
    }

    @Parameterized.Parameters(name = "{index}: {0} {1}")
    public static Collection<Object[]> data() {
        KieServicesConfiguration restConfiguration = createKieServicesRestConfiguration();

        return Arrays.asList(new Object[][]{{MarshallingFormat.JSON, restConfiguration}});
    }

    @Before
    @Override
    public void createControllerClient() {
        if (TestConfig.isLocalServer()) {
            controllerClient = KieServerControllerClientFactory.newWebSocketClient(TestConfig.getControllerWebSocketManagementUrl(),
                                                                                   null,
                                                                                   (String)null);
        } else {
            controllerClient = KieServerControllerClientFactory.newWebSocketClient(TestConfig.getControllerWebSocketManagementUrl(),
                                                                                   TestConfig.getUsername(),
                                                                                   TestConfig.getPassword());
        }
    }

    @Test
    public void testDeleteServerInstance() {
        // Create kie server instance in controller.
        ServerTemplate serverTemplate = createServerTemplate();

        assertEquals(1, serverTemplate.getServerInstanceKeys().size());

        ServerInstanceKey serverInstanceKey = new ServerInstanceKey(serverTemplate.getId(),
                                                                          "serverName",
                                                                          "serverInstanceId",
                                                                          "url");

        serverTemplate.addServerInstance(serverInstanceKey);
        assertEquals(2, serverTemplate.getServerInstanceKeys().size());

        controllerClient.saveServerTemplate(serverTemplate);

        ServerInstanceKeyList serverInstanceKeyList = controllerClient.getServerInstances(serverTemplate.getId());
        assertEquals(2, serverInstanceKeyList.getServerInstanceKeys().length);

        controllerClient.deleteServerInstance(serverInstanceKey);

        serverInstanceKeyList = controllerClient.getServerInstances(serverTemplate.getId());
        assertEquals(1, serverInstanceKeyList.getServerInstanceKeys().length);
    }

    @Test
    public void testDeleteLiveServerInstance() {
        // Create kie server instance in controller.
        ServerTemplate serverTemplate = createServerTemplate();

        ServerInstanceKey serverInstanceKey = serverTemplate.getServerInstanceKeys().iterator().next();
        assertEquals(1, serverTemplate.getServerInstanceKeys().size());

        try {
            controllerClient.deleteServerInstance(serverInstanceKey);
            fail("Deleting a live server instance should fail");
        } catch (KieServerControllerClientException ex){
            assertEquals("Can't delete live instance.",
                         ex.getMessage());
        }

        ServerInstanceKeyList serverInstanceKeyList = controllerClient.getServerInstances(serverTemplate.getId());
        assertEquals(1, serverInstanceKeyList.getServerInstanceKeys().length);
    }

}
