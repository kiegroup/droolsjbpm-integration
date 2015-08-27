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

import static org.hamcrest.core.Is.*;
import static org.junit.Assert.*;
import static org.junit.Assume.*;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kie.server.api.model.KieContainerResource;
import org.kie.server.api.model.KieContainerStatus;
import org.kie.server.api.model.KieServerInfo;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.controller.api.model.KieServerInstance;
import org.kie.server.controller.api.model.KieServerInstanceInfo;
import org.kie.server.controller.api.model.KieServerInstanceList;
import org.kie.server.controller.api.model.KieServerStatus;

public class KieControllerIntegrationTest extends KieControllerBaseTest {

    private static ReleaseId releaseId = new ReleaseId("org.kie.server.testing", "stateless-session-kjar", "1.0.0-SNAPSHOT");

    private static final String CONTAINER_ID = "kie-concurrent";

    private KieServerInfo kieServerInfo;

    @BeforeClass
    public static void initialize() throws Exception {
        createAndDeployKJar(releaseId);
    }

    @Before
    public void getKieServerInfo() {
        // Getting info from currently started kie server.
        ServiceResponse<KieServerInfo> reply = client.getServerInfo();
        assumeThat(reply.getType(), is(ServiceResponse.ResponseType.SUCCESS));
        kieServerInfo = reply.getResult();
    }

    @Test
    public void testCreateKieServerInstance() {
        // Create kie server instance in controller.
        KieServerInstance serverInstance = controllerClient.createKieServerInstance(kieServerInfo);

        assertNotNull(serverInstance);
        assertEquals(kieServerInfo.getServerId(), serverInstance.getIdentifier());
        assertEquals(kieServerInfo.getName(), serverInstance.getName());
        assertEquals(KieServerStatus.UP, serverInstance.getStatus());
        assertEquals(kieServerInfo.getVersion(), serverInstance.getVersion());

        assertNotNull("Kie server instance isn't managed!", serverInstance.getManagedInstances());
        assertEquals(1, serverInstance.getManagedInstances().size());

        KieServerInstanceInfo managedInstance = serverInstance.getManagedInstances().iterator().next();
        assertNotNull(managedInstance);
        assertArrayEquals(kieServerInfo.getCapabilities().toArray(), managedInstance.getCapabilities().toArray());
        assertEquals(kieServerInfo.getLocation(), managedInstance.getLocation());
        assertEquals(KieServerStatus.UP, managedInstance.getStatus());
    }

    @Test
    public void testDeleteKieServerInstance() throws Exception {
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
        assertNullOrEmpty("Active kie server instance found!", instanceList.getKieServerInstances());
    }

    @Test
    public void testGetKieServerInstance() throws Exception {
        // Create kie server instance in controller.
        controllerClient.createKieServerInstance(kieServerInfo);

        // Get kie server instance.
        KieServerInstance serverInstance = controllerClient.getKieServerInstance(kieServerInfo.getServerId());

        assertNotNull(serverInstance);
        assertEquals(kieServerInfo.getServerId(), serverInstance.getIdentifier());
        assertEquals(kieServerInfo.getName(), serverInstance.getName());
        assertEquals(KieServerStatus.UP, serverInstance.getStatus());
        assertEquals(kieServerInfo.getVersion(), serverInstance.getVersion());

        assertNotNull("Kie server instance isn't managed!", serverInstance.getManagedInstances());
        assertEquals(1, serverInstance.getManagedInstances().size());

        KieServerInstanceInfo managedInstance = serverInstance.getManagedInstances().iterator().next();
        assertNotNull(managedInstance);
        assertArrayEquals(kieServerInfo.getCapabilities().toArray(), managedInstance.getCapabilities().toArray());
        assertEquals(kieServerInfo.getLocation(), managedInstance.getLocation());
        assertEquals(KieServerStatus.UP, managedInstance.getStatus());
    }

    @Test
    public void testListKieServerInstances() throws Exception {
        // Create kie server instance in controller.
        controllerClient.createKieServerInstance(kieServerInfo);

        // List kie server instances.
        KieServerInstanceList instanceList = controllerClient.listKieServerInstances();

        assertNotNull(instanceList);
        assertNotNull(instanceList.getKieServerInstances());
        assertEquals(1, instanceList.getKieServerInstances().length);

        KieServerInstance serverInstance = instanceList.getKieServerInstances()[0];
        assertEquals(kieServerInfo.getServerId(), serverInstance.getIdentifier());
        assertEquals(kieServerInfo.getName(), serverInstance.getName());
        assertEquals(KieServerStatus.UP, serverInstance.getStatus());
        assertEquals(kieServerInfo.getVersion(), serverInstance.getVersion());

        assertNotNull("Kie server instance isn't managed!", serverInstance.getManagedInstances());
        assertEquals(1, serverInstance.getManagedInstances().size());

        KieServerInstanceInfo managedInstance = serverInstance.getManagedInstances().iterator().next();
        assertNotNull(managedInstance);
        assertArrayEquals(kieServerInfo.getCapabilities().toArray(), managedInstance.getCapabilities().toArray());
        assertEquals(kieServerInfo.getLocation(), managedInstance.getLocation());
        assertEquals(KieServerStatus.UP, managedInstance.getStatus());
    }

    @Test
    public void testEmptyListKieServerInstances() throws Exception {
        KieServerInstanceList instanceList = controllerClient.listKieServerInstances();
        assertNotNull(instanceList);
        assertNullOrEmpty("Active kie server instance found!", instanceList.getKieServerInstances());
    }

    @Test
    public void testContainerHandling() throws Exception {
        // Create kie server instance connection in controller.
        controllerClient.createKieServerInstance(kieServerInfo);

        // Deploy container for kie server instance.
        KieContainerResource containerToDeploy = new KieContainerResource(CONTAINER_ID, releaseId);
        KieContainerResource deployedContainer = controllerClient.createContainer(kieServerInfo.getServerId(), CONTAINER_ID, containerToDeploy);

        assertNotNull(deployedContainer);
        assertEquals(CONTAINER_ID, deployedContainer.getContainerId());
        assertEquals(containerToDeploy.getReleaseId(), deployedContainer.getReleaseId());

        // Check that container is deployed.
        ServiceResponse<KieContainerResource> containerInfo = client.getContainerInfo(CONTAINER_ID);
        assertEquals(ServiceResponse.ResponseType.SUCCESS, containerInfo.getType());
        assertEquals(CONTAINER_ID, containerInfo.getResult().getContainerId());
        assertEquals(KieContainerStatus.STARTED, containerInfo.getResult().getStatus());
        assertEquals(releaseId, containerInfo.getResult().getReleaseId());

        // Undeploy container for kie server instance.
        controllerClient.disposeContainer(kieServerInfo.getServerId(), CONTAINER_ID);

        // Check that container is not deployed.
        containerInfo = client.getContainerInfo(CONTAINER_ID);
        assertEquals(ServiceResponse.ResponseType.FAILURE, containerInfo.getType());
        assertResultContainsString(containerInfo.getMsg(), "Container " + CONTAINER_ID + " is not instantiated.");
    }

    @Test
    public void testGetContainer() throws Exception {
        // Create kie server instance connection in controller.
        controllerClient.createKieServerInstance(kieServerInfo);

        // Deploy container for kie server instance.
        KieContainerResource containerToDeploy = new KieContainerResource(CONTAINER_ID, releaseId);
        controllerClient.createContainer(kieServerInfo.getServerId(), CONTAINER_ID, containerToDeploy);

        // Get container using kie controller.
        KieContainerResource containerResponseEntity = controllerClient.getContainerInfo(kieServerInfo.getServerId(), CONTAINER_ID);
        assertNotNull(containerResponseEntity);
        assertEquals(CONTAINER_ID, containerResponseEntity.getContainerId());
        assertEquals(releaseId, containerResponseEntity.getReleaseId());
        assertEquals(KieContainerStatus.STOPPED, containerResponseEntity.getStatus());
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
        assertNotNull(containerResponseEntity);
        assertEquals(CONTAINER_ID, containerResponseEntity.getContainerId());
        assertEquals(releaseId, containerResponseEntity.getReleaseId());
        assertEquals(KieContainerStatus.STOPPED, containerResponseEntity.getStatus());

        controllerClient.startContainer(kieServerInfo.getServerId(), CONTAINER_ID);

        containerResponseEntity = controllerClient.getContainerInfo(kieServerInfo.getServerId(), CONTAINER_ID);
        assertNotNull(containerResponseEntity);
        assertEquals(CONTAINER_ID, containerResponseEntity.getContainerId());
        assertEquals(releaseId, containerResponseEntity.getReleaseId());
        assertEquals(KieContainerStatus.STARTED, containerResponseEntity.getStatus());

        controllerClient.stopContainer(kieServerInfo.getServerId(), CONTAINER_ID);

        containerResponseEntity = controllerClient.getContainerInfo(kieServerInfo.getServerId(), CONTAINER_ID);
        assertNotNull(containerResponseEntity);
        assertEquals(CONTAINER_ID, containerResponseEntity.getContainerId());
        assertEquals(releaseId, containerResponseEntity.getReleaseId());
        assertEquals(KieContainerStatus.STOPPED, containerResponseEntity.getStatus());
    }
}
