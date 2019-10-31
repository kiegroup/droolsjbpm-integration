/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
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

import java.io.IOException;
import java.util.Collections;
import java.util.concurrent.Executors;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.kie.server.api.model.KieContainerStatus;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.client.KieServicesClient;
import org.kie.server.controller.api.model.spec.ContainerSpec;
import org.kie.server.controller.api.model.spec.ServerTemplate;
import org.kie.server.controller.api.model.spec.ServerTemplateList;
import org.kie.server.controller.client.KieServerControllerClient;
import org.kie.server.controller.client.KieServerControllerClientFactory;
import org.kie.server.controller.client.event.EventHandler;
import org.kie.server.integrationtests.config.TestConfig;
import org.kie.server.integrationtests.shared.KieServerAssert;
import org.kie.server.integrationtests.shared.KieServerDeployer;
import org.kie.server.integrationtests.shared.KieServerExecutor;
import org.kie.server.integrationtests.shared.basetests.KieServerBaseIntegrationTest;
import org.mockito.InOrder;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class WebSocketKieControllerNotificationIntegrationTest extends KieServerBaseIntegrationTest {

    private static final ReleaseId releaseId = new ReleaseId("org.kie.server.testing",
                                                             "stateless-session-kjar",
                                                             "1.0.0");

    private static final String CONTAINER_ID = "kie-concurrent";
    private static final String CONTAINER_NAME = "containerName";

    KieServerControllerClient controllerClient;

    EventHandler eventHandler;

    @BeforeClass
    public static void initialize() throws Exception {
        KieServerDeployer.buildAndDeployCommonMavenParent();
        KieServerDeployer.buildAndDeployMavenProjectFromResource("/kjars-sources/stateless-session-kjar");
    }

    @Override
    protected KieServicesClient createDefaultClient() {
        return null;
    }

    @Before
    @Override
    public void setup() throws Exception {
        super.setup();
        disposeAllContainers();
        disposeAllServerInstances();
        eventHandler = mock(EventHandler.class);
        if (TestConfig.isLocalServer()) {
            controllerClient = KieServerControllerClientFactory.newWebSocketClient(TestConfig.getControllerWebSocketManagementUrl(),
                                                                                   null,
                                                                                   null,
                                                                                   eventHandler);
        } else {
            controllerClient = KieServerControllerClientFactory.newWebSocketClient(TestConfig.getControllerWebSocketManagementUrl(),
                                                                                   TestConfig.getUsername(),
                                                                                   TestConfig.getPassword(),
                                                                                   eventHandler);
        }
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

    @Test(timeout = 30 * 1000)
    public void testServerTemplateEvents() throws Exception {
        runAsync(() -> {
            // Check that there are no kie servers deployed in controller.
            ServerTemplateList instanceList = controllerClient.listServerTemplates();
            assertNotNull(instanceList);
            KieServerAssert.assertNullOrEmpty("Active kie server instance found!",
                                              instanceList.getServerTemplates());

            // Create new server template
            ServerTemplate template = new ServerTemplate("notification-int-test",
                                                         "Notification Test Server");
            controllerClient.saveServerTemplate(template);

            // Check that kie server is registered in controller.
            instanceList = controllerClient.listServerTemplates();
            assertNotNull(instanceList);
            assertEquals(1,
                         instanceList.getServerTemplates().length);

            // Delete server template
            controllerClient.deleteServerTemplate(template.getId());
        });

        verify(eventHandler, timeout(2000L)).onServerTemplateUpdated(any());
        verify(eventHandler, timeout(2000L)).onServerTemplateDeleted(any());

        verifyNoMoreInteractions(eventHandler);
    }

    @Test(timeout = 60 * 1000)
    @Ignore("Skipped due to race condition between embedded Kie server initialization and Kie server healthcheck.")
    // Test is unstable due to race condition between embedded Kie server initialization and Kie server healthcheck triggered by controller.
    // If healthcheck is triggered between KieServerImpl initialization and registering of Kie server REST endpoint then Kie server instance is removed from controller, causing additional events.
    // Template events are tested in the other integration tests.
    public void testKieServerEvents() throws Exception {
        // Check that there are no kie servers deployed in controller.
        ServerTemplateList instanceList = controllerClient.listServerTemplates();
        assertNotNull(instanceList);
        KieServerAssert.assertNullOrEmpty("Active kie server instance found!",
                                          instanceList.getServerTemplates());

        // Turn on new kie server.
        server = new KieServerExecutor();
        server.startKieServer();

        try {
            // Check that kie server is registered in controller.
            instanceList = controllerClient.listServerTemplates();
            assertNotNull(instanceList);
            assertEquals(1,
                         instanceList.getServerTemplates().length);

            // Verify instance creation events
            verify(eventHandler, timeout(2000L)).onServerTemplateUpdated(any());
            verify(eventHandler, timeout(2000L)).onServerInstanceUpdated(any());
            verify(eventHandler, timeout(2000L)).onServerInstanceConnected(any());

            // Deploy container to Kie Server
            ServerTemplate template = instanceList.getServerTemplates()[0];
            ContainerSpec containerSpec = new ContainerSpec(CONTAINER_ID,
                                                            CONTAINER_NAME,
                                                            template,
                                                            releaseId,
                                                            KieContainerStatus.STOPPED,
                                                            Collections.emptyMap());
            controllerClient.saveContainerSpec(template.getId(),
                                               containerSpec);

            // Verify create container event
            verify(eventHandler, timeout(2000L).times(2)).onServerTemplateUpdated(any());

            controllerClient.startContainer(containerSpec);

            // Verify start container event
            verify(eventHandler, timeout(2000L)).onContainerSpecUpdated(any());

            controllerClient.stopContainer(containerSpec);

            // Verify stop container event
            verify(eventHandler, timeout(2000L).times(2)).onContainerSpecUpdated(any());

            controllerClient.deleteContainerSpec(template.getId(),
                                                 containerSpec.getId());

            // Verify delete container
            verify(eventHandler, timeout(2000L).times(3)).onServerTemplateUpdated(any());

        } finally {
            server.stopKieServer();
        }

        // Verify disconnect
        verify(eventHandler, timeout(2000L)).onServerInstanceDeleted(any());
        verify(eventHandler, timeout(2000L).times(4)).onServerTemplateUpdated(any());
        verify(eventHandler, timeout(2000L)).onServerInstanceDisconnected(any());

        verifyNoMoreInteractions(eventHandler);
    }

    protected void runAsync(final Runnable runnable) throws Exception {
        Executors.newSingleThreadExecutor().submit(runnable).get();
    }
}
