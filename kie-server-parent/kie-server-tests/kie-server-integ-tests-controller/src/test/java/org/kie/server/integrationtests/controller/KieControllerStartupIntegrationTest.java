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

import static org.junit.Assert.*;

import java.util.Collection;
import java.util.HashMap;
import java.util.concurrent.TimeoutException;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kie.server.api.model.KieContainerResource;
import org.kie.server.api.model.KieContainerResourceList;
import org.kie.server.api.model.KieContainerStatus;
import org.kie.server.api.model.KieServerInfo;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.client.KieServicesClient;
import org.kie.server.client.KieServicesConfiguration;
import org.kie.server.client.KieServicesFactory;
import org.kie.server.controller.api.model.spec.Capability;
import org.kie.server.controller.api.model.spec.ContainerConfig;
import org.kie.server.controller.api.model.spec.ContainerSpec;
import org.kie.server.controller.api.model.spec.ServerTemplate;
import org.kie.server.integrationtests.config.TestConfig;

public class KieControllerStartupIntegrationTest extends KieControllerManagementBaseTest {

    private static ReleaseId releaseId = new ReleaseId("org.kie.server.testing", "stateless-session-kjar", "1.0.0-SNAPSHOT");

    private static final String CONTAINER_ID = "kie-concurrent";

    @Override
    protected KieServicesClient createDefaultClient() {
        // For these tests we use embedded kie server as we need to control turning server off and on.
        KieServicesConfiguration config = KieServicesFactory.newRestConfiguration(TestConfig.getEmbeddedKieServerHttpUrl(), null, null);
        config.setMarshallingFormat(marshallingFormat);
        return KieServicesFactory.newKieServicesClient(config);
    }

    @BeforeClass
    public static void initialize() throws Exception {
        buildAndDeployCommonMavenParent();
        buildAndDeployMavenProject(ClassLoader.class.getResource("/kjars-sources/stateless-session-kjar").getFile());
    }

    @Before
    @Override
    public void setup() throws Exception {
        // Start embedded kie server to be correctly initialized and cleaned before tests.
        if (!TestConfig.isLocalServer()) {
            startKieServer();
        }
        super.setup();
    }

    @After
    public void cleanupEmbeddedKieServer() {
        // Turn off embedded kie server if running in container, turn on if running local tests.
        try {
            if (TestConfig.isLocalServer()) {
                startKieServer();
            } else {
                stopKieServer();
            }
        } catch (Exception e) {
            // Exception thrown if there is already kie server started or stopped respectively.
            // Don't need to do anything in such case.
        }
    }

    @Test
    public void testRegisterKieServerAfterStartup() {
        // Turn off embedded kie server.
        stopKieServer();

        // Check that there are no kie servers deployed in controller.
        Collection<ServerTemplate> instanceList = controllerClient.listServerTemplates();
        assertNotNull(instanceList);
        assertNullOrEmpty("Active kie server instance found!", instanceList);

        // Turn on new kie server.
        startKieServer();

        // Check that kie server is registered in controller.
        instanceList = controllerClient.listServerTemplates();
        assertNotNull(instanceList);
        assertEquals(1, instanceList.size());

        // Getting info from currently started kie server.
        ServiceResponse<KieServerInfo> reply = client.getServerInfo();
        assertNotNull(reply);
        assertEquals(ServiceResponse.ResponseType.SUCCESS, reply.getType());
        assertNotNull(reply.getResult());

        ServerTemplate deployedServerInstance = instanceList.iterator().next();
        assertNotNull(deployedServerInstance);
        assertEquals(reply.getResult().getServerId(), deployedServerInstance.getId());
    }

    @Test
    public void testTurnOffKieServerAfterShutdown() {
        // Register kie server in controller.
        ServiceResponse<KieServerInfo> kieServerInfo = client.getServerInfo();
        assertEquals(ServiceResponse.ResponseType.SUCCESS, kieServerInfo.getType());
        assertNotNull(kieServerInfo.getResult());

        ServerTemplate serverTemplate = new ServerTemplate(kieServerInfo.getResult().getServerId(), kieServerInfo.getResult().getName());
        controllerClient.saveServerTemplate(serverTemplate);

        // Check that kie server is registered.
        Collection<ServerTemplate> instanceList = controllerClient.listServerTemplates();
        assertEquals(1, instanceList.size());
        assertEquals(kieServerInfo.getResult().getServerId(), instanceList.iterator().next().getId()); //maybe change to avoid next -> null

        // Turn off embedded kie server.
        stopKieServer();

        // Check that kie server is down in controller.
        instanceList = controllerClient.listServerTemplates();
        assertEquals(1, instanceList.size());
        assertEquals(kieServerInfo.getResult().getServerId(), instanceList.iterator().next().getId()); //maybe change to avoid next -> null
    }

    @Test
    public void testContainerCreatedAfterStartup() throws Exception {
        // Getting info from currently started kie server.
        ServiceResponse<KieServerInfo> kieServerInfo = client.getServerInfo();
        assertEquals(ServiceResponse.ResponseType.SUCCESS, kieServerInfo.getType());
        assertNotNull(kieServerInfo.getResult());

        // Check that there are no containers in kie server.
        ServiceResponse<KieContainerResourceList> containersList = client.listContainers();
        assertEquals(ServiceResponse.ResponseType.SUCCESS, containersList.getType());
        assertNullOrEmpty("Active containers found!", containersList.getResult().getContainers());

        // Check that there are no kie servers deployed in controller.
        Collection<ServerTemplate> instanceList = controllerClient.listServerTemplates();
        assertNullOrEmpty("Active kie server instance found!", instanceList);

        // Turn kie server off, add embedded kie server to controller, create container and start kie server again.
        stopKieServer();

        ServerTemplate serverTemplate = new ServerTemplate(kieServerInfo.getResult().getServerId(), kieServerInfo.getResult().getName());
        controllerClient.saveServerTemplate(serverTemplate);

        ContainerSpec containerSpec = new ContainerSpec(CONTAINER_ID, CONTAINER_ID, serverTemplate, releaseId, KieContainerStatus.STOPPED, new HashMap<Capability, ContainerConfig>());
        controllerClient.saveContainerSpec(serverTemplate.getId(), containerSpec);
        ContainerSpec deployedContainer = controllerClient.getContainerInfo(kieServerInfo.getResult().getServerId(), CONTAINER_ID);

        assertNotNull(deployedContainer);
        assertEquals(CONTAINER_ID, deployedContainer.getId());
        assertEquals(releaseId, deployedContainer.getReleasedId());
        assertEquals(KieContainerStatus.STOPPED, deployedContainer.getStatus());

        controllerClient.startContainer(kieServerInfo.getResult().getServerId(), CONTAINER_ID);

        startKieServer();

        // Check that container is deployed on kie server.
        ServiceResponse<KieContainerResource> containerInfo = client.getContainerInfo(CONTAINER_ID);
        assertEquals(ServiceResponse.ResponseType.SUCCESS, containerInfo.getType());
        assertNotNull(containerInfo.getResult());
        assertEquals(CONTAINER_ID, containerInfo.getResult().getContainerId());
        assertEquals(KieContainerStatus.STARTED, containerInfo.getResult().getStatus());
    }

    @Test
    public void testContainerDisposedAfterStartup() throws Exception {
        // Getting info from currently started kie server.
        ServiceResponse<KieServerInfo> kieServerInfo = client.getServerInfo();
        assertEquals(ServiceResponse.ResponseType.SUCCESS, kieServerInfo.getType());
        assertNotNull(kieServerInfo.getResult());

        // Create container.
        ServerTemplate serverTemplate = new ServerTemplate(kieServerInfo.getResult().getServerId(), kieServerInfo.getResult().getName());
        controllerClient.saveServerTemplate(serverTemplate);
        ContainerSpec containerSpec = new ContainerSpec(CONTAINER_ID, CONTAINER_ID, serverTemplate, releaseId, KieContainerStatus.STOPPED, new HashMap<Capability, ContainerConfig>());
        controllerClient.saveContainerSpec(kieServerInfo.getResult().getServerId(), containerSpec);
        controllerClient.startContainer(kieServerInfo.getResult().getServerId(), CONTAINER_ID);

        // Check that there is one container deployed.
        try {
            waitForKieServerSynchronization(1);
        } catch (TimeoutException e) {
            // Sometimes creating container fails in embedded server (unknown Socket timeout error, tends to happen here).
            // Retrigger container creation. These tests should be refactored to use more reliable container instead of embedded TJWSEmbeddedJaxrsServer.
            controllerClient.startContainer(kieServerInfo.getResult().getServerId(), CONTAINER_ID);
            waitForKieServerSynchronization(1);
        }
        ServiceResponse<KieContainerResourceList> containersList = client.listContainers();
        assertEquals(ServiceResponse.ResponseType.SUCCESS, containersList.getType());
        assertNotNull(containersList.getResult().getContainers());
        assertEquals(1, containersList.getResult().getContainers().size());

        Collection<ServerTemplate> instanceList = controllerClient.listServerTemplates();
        assertEquals(1, instanceList.size());

        // Turn kie server off, dispose container and start kie server again.
        stopKieServer();

        controllerClient.stopContainer(kieServerInfo.getResult().getServerId(), CONTAINER_ID);
        controllerClient.deleteContainerSpec(serverTemplate.getId(), CONTAINER_ID);

        startKieServer();

        // Check that no container is deployed on kie server.
        containersList = client.listContainers();
        assertEquals(ServiceResponse.ResponseType.SUCCESS, containersList.getType());
        assertNullOrEmpty("Active containers found!", containersList.getResult().getContainers());
    }
}
