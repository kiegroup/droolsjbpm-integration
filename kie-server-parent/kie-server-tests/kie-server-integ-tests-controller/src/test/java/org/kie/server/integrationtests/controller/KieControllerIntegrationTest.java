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

import static org.hamcrest.core.Is.*;
import static org.junit.Assert.*;
import static org.junit.Assume.*;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.kie.server.api.model.KieContainerResource;
import org.kie.server.api.model.KieContainerResourceList;
import org.kie.server.api.model.KieContainerStatus;
import org.kie.server.api.model.KieServerInfo;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.controller.api.model.KieServerInstance;
import org.kie.server.controller.api.model.KieServerInstanceInfo;
import org.kie.server.controller.api.model.KieServerInstanceList;
import org.kie.server.controller.api.model.KieServerStatus;
import org.kie.server.integrationtests.controller.client.exception.UnexpectedResponseCodeException;
import org.kie.server.integrationtests.category.Smoke;
import org.kie.server.integrationtests.shared.KieServerAssert;
import org.kie.server.integrationtests.shared.KieServerDeployer;
import org.kie.server.integrationtests.shared.KieServerSynchronization;

public class KieControllerIntegrationTest extends KieControllerBaseTest {

    private static ReleaseId releaseId = new ReleaseId("org.kie.server.testing", "stateless-session-kjar", "1.0.0-SNAPSHOT");

    private static final String CONTAINER_ID = "kie-concurrent";

    private KieServerInfo kieServerInfo;

    @BeforeClass
    public static void initialize() throws Exception {
        KieServerDeployer.buildAndDeployCommonMavenParent();
        KieServerDeployer.buildAndDeployMavenProject(ClassLoader.class.getResource("/kjars-sources/stateless-session-kjar").getFile());
    }

    @Before
    public void getKieServerInfo() {
        // Getting info from currently started kie server.
        ServiceResponse<KieServerInfo> reply = client.getServerInfo();
        assumeThat(reply.getType(), is(ServiceResponse.ResponseType.SUCCESS));
        kieServerInfo = reply.getResult();
    }

    @Test
    @Category(Smoke.class)
    public void testCreateKieServerInstance() {
        // Create kie server instance in controller.
        KieServerInstance serverInstance = controllerClient.createKieServerInstance(kieServerInfo);

        checkServerInstance(serverInstance);
        assertEquals(kieServerInfo.getVersion(), serverInstance.getVersion());

        assertNotNull("Kie server instance isn't managed!", serverInstance.getManagedInstances());
        assertEquals(1, serverInstance.getManagedInstances().size());

        KieServerInstanceInfo managedInstance = serverInstance.getManagedInstances().iterator().next();
        checkServerInstanceInfo(managedInstance);
    }

    @Test
    public void testCreateDuplicitKieServerInstance() {
        // Create kie server instance in controller.
        KieServerInstance serverInstance = controllerClient.createKieServerInstance(kieServerInfo);
        assertNotNull(serverInstance);

        try {
            // Try to create same kie server instance.
            controllerClient.createKieServerInstance(kieServerInfo);
            fail("Should throw exception about kie server instance already created.");
        } catch (UnexpectedResponseCodeException e) {
            assertEquals(400, e.getResponseCode());
        }
    }

    @Test
    public void testDeleteKieServerInstance() {
        // Create kie server instance in controller.
        controllerClient.createKieServerInstance(kieServerInfo);

        // Check that instance is created.
        KieServerInstanceList instanceList = controllerClient.listKieServerInstances();

        assertNotNull(instanceList);
        assertNotNull(instanceList.getKieServerInstances());
        assertEquals(1, instanceList.getKieServerInstances().length);

        // Delete created kie server instance.
        controllerClient.deleteKieServerInstance(kieServerInfo.getServerId());

        // There are no kie server instances in controller now.
        instanceList = controllerClient.listKieServerInstances();
        assertNotNull(instanceList);
        KieServerAssert.assertNullOrEmpty("Active kie server instance found!", instanceList.getKieServerInstances());
    }

    @Test
    public void testDeleteNotExistingKieServerInstance() {
        try {
            // Try to delete not existing kie server instance.
            controllerClient.deleteKieServerInstance(kieServerInfo.getServerId());
            fail("Should throw exception about kie server instance not existing.");
        } catch (UnexpectedResponseCodeException e) {
            assertEquals(400, e.getResponseCode());
        }
    }

    @Test
    @Category(Smoke.class)
    public void testGetKieServerInstance() {
        // Create kie server instance in controller.
        controllerClient.createKieServerInstance(kieServerInfo);

        // Get kie server instance.
        KieServerInstance serverInstance = controllerClient.getKieServerInstance(kieServerInfo.getServerId());

        checkServerInstance(serverInstance);

        assertNotNull("Kie server instance isn't managed!", serverInstance.getManagedInstances());
        assertEquals(1, serverInstance.getManagedInstances().size());

        KieServerInstanceInfo managedInstance = serverInstance.getManagedInstances().iterator().next();
        checkServerInstanceInfo(managedInstance);
    }

    @Test
    public void testGetNotExistingKieServerInstance() {
        try {
            // Try to get not existing kie server instance.
            controllerClient.getKieServerInstance(kieServerInfo.getServerId());
            fail("Should throw exception about kie server instance not existing.");
        } catch (UnexpectedResponseCodeException e) {
            assertEquals(400, e.getResponseCode());
        }
    }

    @Test
    public void testListKieServerInstances() {
        // Create kie server instance in controller.
        controllerClient.createKieServerInstance(kieServerInfo);

        // List kie server instances.
        KieServerInstanceList instanceList = controllerClient.listKieServerInstances();

        assertNotNull(instanceList);
        assertNotNull(instanceList.getKieServerInstances());
        assertEquals(1, instanceList.getKieServerInstances().length);

        KieServerInstance serverInstance = instanceList.getKieServerInstances()[0];
        checkServerInstance(serverInstance);

        assertNotNull("Kie server instance isn't managed!", serverInstance.getManagedInstances());
        assertEquals(1, serverInstance.getManagedInstances().size());

        KieServerInstanceInfo managedInstance = serverInstance.getManagedInstances().iterator().next();
        checkServerInstanceInfo(managedInstance);
    }

    @Test
    public void testEmptyListKieServerInstances() throws Exception {
        KieServerInstanceList instanceList = controllerClient.listKieServerInstances();
        assertNotNull(instanceList);
        assertEquals("Active kie server instance found!", 0, instanceList.getKieServerInstances().length);
    }

    @Test
    public void testContainerHandling() {
        // Create kie server instance connection in controller.
        controllerClient.createKieServerInstance(kieServerInfo);

        // Deploy container for kie server instance.
        KieContainerResource containerToDeploy = new KieContainerResource(CONTAINER_ID, releaseId);
        KieContainerResource deployedContainer = controllerClient.createContainer(kieServerInfo.getServerId(), CONTAINER_ID, containerToDeploy);

        assertNotNull(deployedContainer);
        assertEquals(CONTAINER_ID, deployedContainer.getContainerId());
        assertEquals(containerToDeploy.getReleaseId(), deployedContainer.getReleaseId());

        // Check that container is deployed.
        KieContainerResource containerResponseEntity = controllerClient.getContainerInfo(kieServerInfo.getServerId(), CONTAINER_ID);
        checkContainer(containerResponseEntity, KieContainerStatus.STOPPED);

        // Container is in stopped state, so there are no containers deployed in kie server.
        ServiceResponse<KieContainerResourceList> containersList = client.listContainers();
        assertEquals(ServiceResponse.ResponseType.SUCCESS, containersList.getType());
        KieServerAssert.assertNullOrEmpty("Active containers found!", containersList.getResult().getContainers());

        // Undeploy container for kie server instance.
        controllerClient.disposeContainer(kieServerInfo.getServerId(), CONTAINER_ID);

        // Check that container is disposed.
        try {
            controllerClient.getContainerInfo(kieServerInfo.getServerId(), CONTAINER_ID);
            fail("Should throw exception about container info not found.");
        } catch (UnexpectedResponseCodeException e) {
            assertEquals(400, e.getResponseCode());
        }
    }

    @Test
    public void testCreateContainerOnNotExistingKieServerInstance() {
        // Try to create container using kie controller without created kie server instance.
        KieContainerResource containerToDeploy = new KieContainerResource(CONTAINER_ID, releaseId);
        try {
            controllerClient.createContainer(kieServerInfo.getServerId(), CONTAINER_ID, containerToDeploy);
            fail("Should throw exception about kie server instance not found.");
        } catch (UnexpectedResponseCodeException e) {
            assertEquals(400, e.getResponseCode());
        }
    }

    @Test
    public void testCreateDuplicitContainer() {
        // Create kie server instance connection in controller.
        controllerClient.createKieServerInstance(kieServerInfo);

        // Create container using kie controller.
        KieContainerResource containerToDeploy = new KieContainerResource(CONTAINER_ID, releaseId);
        KieContainerResource deployedContainer = controllerClient.createContainer(kieServerInfo.getServerId(), CONTAINER_ID, containerToDeploy);

        assertNotNull(deployedContainer);
        assertEquals(CONTAINER_ID, deployedContainer.getContainerId());
        assertEquals(containerToDeploy.getReleaseId(), deployedContainer.getReleaseId());

        try {
            // Try to create same container.
            controllerClient.createContainer(kieServerInfo.getServerId(), CONTAINER_ID, containerToDeploy);
            fail("Should throw exception about container being created already.");
        } catch (UnexpectedResponseCodeException e) {
            assertEquals(400, e.getResponseCode());
        }
    }

    @Test
    public void testDeleteNotExistingContainer() {
        // Try to dispose not existing container using kie controller without created kie server instance.
        try {
            controllerClient.disposeContainer(kieServerInfo.getServerId(), CONTAINER_ID);
            fail("Should throw exception about kie server instance not exists.");
        } catch (UnexpectedResponseCodeException e) {
            assertEquals(400, e.getResponseCode());
        }

        controllerClient.createKieServerInstance(kieServerInfo);
        // Try to dispose not existing container using kie controller with created kie server instance.
        try {
            controllerClient.disposeContainer(kieServerInfo.getServerId(), CONTAINER_ID);
            fail("Should throw exception about container not exists.");
        } catch (UnexpectedResponseCodeException e) {
            assertEquals(400, e.getResponseCode());
        }
    }

    @Test
    public void testGetContainer() {
        // Create kie server instance connection in controller.
        controllerClient.createKieServerInstance(kieServerInfo);

        // Deploy container for kie server instance.
        KieContainerResource containerToDeploy = new KieContainerResource(CONTAINER_ID, releaseId);
        controllerClient.createContainer(kieServerInfo.getServerId(), CONTAINER_ID, containerToDeploy);

        // Get container using kie controller.
        KieContainerResource containerResponseEntity = controllerClient.getContainerInfo(kieServerInfo.getServerId(), CONTAINER_ID);
        checkContainer(containerResponseEntity, KieContainerStatus.STOPPED);
    }


    @Test
    public void testStartAndStopContainer() throws Exception {
        // Create kie server instance connection in controller.
        controllerClient.createKieServerInstance(kieServerInfo);

        // Deploy container for kie server instance.
        KieContainerResource containerToDeploy = new KieContainerResource(CONTAINER_ID, releaseId);
        controllerClient.createContainer(kieServerInfo.getServerId(), CONTAINER_ID, containerToDeploy);

        // Get container using kie controller.
        KieContainerResource containerResponseEntity = controllerClient.getContainerInfo(kieServerInfo.getServerId(), CONTAINER_ID);
        checkContainer(containerResponseEntity, KieContainerStatus.STOPPED);

        // Check that container is not deployed in kie server (as container is in STOPPED state).
        ServiceResponse<KieContainerResource> containerInfo = client.getContainerInfo(CONTAINER_ID);
        assertEquals(ServiceResponse.ResponseType.FAILURE, containerInfo.getType());
        KieServerAssert.assertResultContainsString(containerInfo.getMsg(), "Container " + CONTAINER_ID + " is not instantiated.");

        controllerClient.startContainer(kieServerInfo.getServerId(), CONTAINER_ID);

        containerResponseEntity = controllerClient.getContainerInfo(kieServerInfo.getServerId(), CONTAINER_ID);
        checkContainer(containerResponseEntity, KieContainerStatus.STARTED);

        // Check that container is deployed in kie server.
        KieServerSynchronization.waitForKieServerSynchronization(client, 1);
        containerInfo = client.getContainerInfo(CONTAINER_ID);
        assertEquals(ServiceResponse.ResponseType.SUCCESS, containerInfo.getType());
        assertEquals(CONTAINER_ID, containerInfo.getResult().getContainerId());
        assertEquals(KieContainerStatus.STARTED, containerInfo.getResult().getStatus());
        assertEquals(releaseId, containerInfo.getResult().getReleaseId());

        controllerClient.stopContainer(kieServerInfo.getServerId(), CONTAINER_ID);

        containerResponseEntity = controllerClient.getContainerInfo(kieServerInfo.getServerId(), CONTAINER_ID);
        checkContainer(containerResponseEntity, KieContainerStatus.STOPPED);

        // Check that container is not deployed in kie server (as container is in STOPPED state).
        KieServerSynchronization.waitForKieServerSynchronization(client, 0);
        containerInfo = client.getContainerInfo(CONTAINER_ID);
        assertEquals(ServiceResponse.ResponseType.FAILURE, containerInfo.getType());
        KieServerAssert.assertResultContainsString(containerInfo.getMsg(), "Container " + CONTAINER_ID + " is not instantiated.");
    }

    @Test
    public void testStartNotExistingContainer() throws Exception {
        // Try to start not existing container using kie controller without created kie server instance.
        try {
            controllerClient.startContainer(kieServerInfo.getServerId(), CONTAINER_ID);
            fail("Should throw exception about container not found.");
        } catch (UnexpectedResponseCodeException e) {
            assertEquals(400, e.getResponseCode());
        }

        controllerClient.createKieServerInstance(kieServerInfo);
        // Try to start not existing container using kie controller with created kie server instance.
        try {
            controllerClient.startContainer(kieServerInfo.getServerId(), CONTAINER_ID);
            fail("Should throw exception about container not found.");
        } catch (UnexpectedResponseCodeException e) {
            assertEquals(400, e.getResponseCode());
        }
    }

    @Test
    public void testStopNotExistingContainer() throws Exception {
        // Try to stop not existing container using kie controller without created kie server instance.
        try {
            controllerClient.stopContainer(kieServerInfo.getServerId(), CONTAINER_ID);
            fail("Should throw exception about container not found.");
        } catch (UnexpectedResponseCodeException e) {
            assertEquals(400, e.getResponseCode());
        }

        controllerClient.createKieServerInstance(kieServerInfo);
        // Try to stop not existing container using kie controller with created kie server instance.
        try {
            controllerClient.stopContainer(kieServerInfo.getServerId(), CONTAINER_ID);
            fail("Should throw exception about container not found.");
        } catch (UnexpectedResponseCodeException e) {
            assertEquals(400, e.getResponseCode());
        }
    }

    @Test
    public void testGetNotExistingContainer() {
        // Try to get not existing container using kie controller without created kie server instance.
        try {
            controllerClient.getContainerInfo(kieServerInfo.getServerId(), CONTAINER_ID);
            fail("Should throw exception about container info not found.");
        } catch (UnexpectedResponseCodeException e) {
            assertEquals(400, e.getResponseCode());
        }

        controllerClient.createKieServerInstance(kieServerInfo);
        // Try to get not existing container using kie controller with created kie server instance.
        try {
            controllerClient.getContainerInfo(kieServerInfo.getServerId(), CONTAINER_ID);
            fail("Should throw exception about container info not found.");
        } catch (UnexpectedResponseCodeException e) {
            assertEquals(400, e.getResponseCode());
        }
    }

    protected void checkContainer(KieContainerResource container, KieContainerStatus status) {
        assertNotNull(container);
        assertEquals(CONTAINER_ID, container.getContainerId());
        assertEquals(releaseId, container.getReleaseId());
        assertEquals(status, container.getStatus());
    }

    protected void checkServerInstance(KieServerInstance serverInstance) {
        assertNotNull(serverInstance);
        assertEquals(kieServerInfo.getServerId(), serverInstance.getIdentifier());
        assertEquals(kieServerInfo.getName(), serverInstance.getName());
        assertEquals(KieServerStatus.UP, serverInstance.getStatus());
    }

    protected void checkServerInstanceInfo(KieServerInstanceInfo serverInstanceInfo) {
        assertNotNull(serverInstanceInfo);
        assertArrayEquals(kieServerInfo.getCapabilities().toArray(), serverInstanceInfo.getCapabilities().toArray());
        assertEquals(kieServerInfo.getLocation(), serverInstanceInfo.getLocation());
        assertEquals(KieServerStatus.UP, serverInstanceInfo.getStatus());
    }

}
