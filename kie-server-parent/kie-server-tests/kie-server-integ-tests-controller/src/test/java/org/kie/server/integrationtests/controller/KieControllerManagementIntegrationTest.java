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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.kie.server.api.model.KieContainerResource;
import org.kie.server.api.model.KieContainerResourceList;
import org.kie.server.api.model.KieContainerStatus;
import org.kie.server.api.model.KieScannerStatus;
import org.kie.server.api.model.KieServerInfo;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.controller.api.ModelFactory;
import org.kie.server.controller.api.model.KieServerInstance;
import org.kie.server.controller.api.model.KieServerInstanceInfo;
import org.kie.server.controller.api.model.KieServerInstanceList;
import org.kie.server.controller.api.model.KieServerStatus;
import org.kie.server.controller.api.model.runtime.ServerInstanceKey;
import org.kie.server.controller.api.model.spec.Capability;
import org.kie.server.controller.api.model.spec.ContainerConfig;
import org.kie.server.controller.api.model.spec.ContainerSpec;
import org.kie.server.controller.api.model.spec.ProcessConfig;
import org.kie.server.controller.api.model.spec.RuleConfig;
import org.kie.server.controller.api.model.spec.ServerTemplate;
import org.kie.server.controller.client.exception.UnexpectedResponseCodeException;
import org.kie.server.controller.impl.storage.InMemoryKieServerTemplateStorage;
import org.kie.server.integrationtests.category.Smoke;

import static org.hamcrest.core.Is.*;
import static org.junit.Assert.*;
import static org.junit.Assume.*;

public class KieControllerManagementIntegrationTest extends KieControllerManagementBaseTest {

    private static ReleaseId releaseId = new ReleaseId("org.kie.server.testing", "stateless-session-kjar", "1.0.0-SNAPSHOT");

    private static final String CONTAINER_ID = "kie-concurrent";
    private static final String CONTAINER_NAME = "containerName";

    private KieServerInfo kieServerInfo;

    @BeforeClass
    public static void initialize() throws Exception {
        createAndDeployKJar(releaseId);
    }

    @Before
    public void getKieServerInfo() {
        InMemoryKieServerTemplateStorage.getInstance().clear();
        // Getting info from currently started kie server.
        ServiceResponse<KieServerInfo> reply = client.getServerInfo();
        assumeThat(reply.getType(), is(ServiceResponse.ResponseType.SUCCESS));
        kieServerInfo = reply.getResult();
    }

    @Test
    @Category(Smoke.class)
    public void testCreateKieServerInstance() {
        ServerTemplate serverTemplate = createServerTemplate();

        ServerTemplate storedServerTemplate = controllerClient.getServerTemplate(serverTemplate.getId());
        assertNotNull(storedServerTemplate);
        assertEquals(serverTemplate.getId(), storedServerTemplate.getId());
        assertEquals(serverTemplate.getName(), storedServerTemplate.getName());

        Collection<ServerTemplate> serverTemplates = controllerClient.listServerTemplates();
        assertNotNull(serverTemplates);
        assertEquals(1, serverTemplates.size());

        storedServerTemplate = serverTemplates.iterator().next();
        assertNotNull(storedServerTemplate);
        assertEquals(serverTemplate.getId(), storedServerTemplate.getId());
        assertEquals(serverTemplate.getName(), storedServerTemplate.getName());

    }

    @Test
    public void testCreateDuplicitKieServerInstance() {
        // Create kie server instance in controller.
        ServerTemplate serverTemplate = createServerTemplate();


        try {
            // Try to create same kie server instance.
            controllerClient.saveServerTemplate(serverTemplate);
            fail("Should throw exception about kie server instance already created.");
        } catch (UnexpectedResponseCodeException e) {
            assertEquals(404, e.getResponseCode());
        }
    }

    @Test
    public void testDeleteKieServerInstance() {
        ServerTemplate serverTemplate = createServerTemplate();

        Collection<ServerTemplate> serverTemplates = controllerClient.listServerTemplates();
        assertNotNull(serverTemplates);
        assertEquals(1, serverTemplates.size());

        // Delete created kie server instance.
        controllerClient.deleteServerTemplate(serverTemplate.getId());

        // There are no kie server instances in controller now.
        serverTemplates = controllerClient.listServerTemplates();
        assertNullOrEmpty("Active kie server instance found!", serverTemplates);
    }

    @Test
    public void testDeleteNotExistingKieServerInstance() {
        try {
            // Try to delete not existing kie server instance.
            controllerClient.deleteServerTemplate("not existing");
            fail("Should throw exception about kie server instance not existing.");
        } catch (UnexpectedResponseCodeException e) {
            assertEquals(404, e.getResponseCode());
        }
    }

    @Test
    @Category(Smoke.class)
    public void testGetKieServerInstance() {
        // Create kie server instance in controller.
        ServerTemplate serverTemplate = createServerTemplate();

        ServerInstanceKey serverInstanceKey = serverTemplate.getServerInstanceKeys().iterator().next();

        // Get kie server instance.
        ServerTemplate serverInstance = controllerClient.getServerTemplate(serverTemplate.getId());

        assertNotNull(serverInstance);
        assertEquals(kieServerInfo.getServerId(), serverInstance.getId());
        assertEquals(kieServerInfo.getName(), serverInstance.getName());

        assertNotNull("Kie server instance isn't managed!", serverInstance.getServerInstanceKeys());
        assertEquals(1, serverInstance.getServerInstanceKeys().size());

        ServerInstanceKey managedInstance = serverInstance.getServerInstanceKeys().iterator().next();
        assertNotNull(managedInstance);
        assertEquals(kieServerInfo.getLocation(), managedInstance.getUrl());
        assertEquals(serverTemplate.getId(), managedInstance.getServerTemplateId());
        assertEquals(serverInstanceKey.getServerName(), managedInstance.getServerName());
    }

    @Test
    public void testGetNotExistingKieServerInstance() {
        try {
            // Try to get not existing kie server instance.
            controllerClient.getServerTemplate(kieServerInfo.getServerId());
            fail("Should throw exception about kie server instance not existing.");
        } catch (UnexpectedResponseCodeException e) {
            assertEquals(404, e.getResponseCode());
        }
    }

    @Test
    public void testListKieServerInstances() {
        // Create kie server instance in controller.
        createServerTemplate();

        // List kie server instances.
        Collection<ServerTemplate> instanceList = controllerClient.listServerTemplates();

        assertNotNull(instanceList);
        assertEquals(1, instanceList.size());

        ServerTemplate serverInstance = instanceList.iterator().next();
        assertEquals(kieServerInfo.getServerId(), serverInstance.getId());
        assertEquals(kieServerInfo.getName(), serverInstance.getName());

        assertNotNull("Kie server instance isn't managed!", serverInstance.getServerInstanceKeys());
        assertEquals(1, serverInstance.getServerInstanceKeys().size());

        ServerInstanceKey managedInstance = serverInstance.getServerInstanceKeys().iterator().next();
        assertNotNull(managedInstance);
        assertEquals(kieServerInfo.getLocation(), managedInstance.getUrl());

    }

    @Test
    public void testEmptyListKieServerInstances() throws Exception {
        Collection<ServerTemplate> instanceList = controllerClient.listServerTemplates();
        assertNullOrEmpty("Active kie server instance found!", instanceList);
    }

    @Test
    public void testContainerHandling() {
        // Create kie server instance connection in controller.
        ServerTemplate serverTemplate = createServerTemplate();

        // Deploy container for kie server instance.
        ContainerSpec containerToDeploy = new ContainerSpec(CONTAINER_ID, CONTAINER_NAME, serverTemplate, releaseId, KieContainerStatus.STOPPED, new HashMap());
        controllerClient.saveContainerSpec(serverTemplate.getId(), containerToDeploy);

        // Check that container is deployed.
        ContainerSpec containerResponseEntity = controllerClient.getContainerInfo(kieServerInfo.getServerId(), CONTAINER_ID);
        assertNotNull(containerResponseEntity);
        assertEquals(CONTAINER_ID, containerResponseEntity.getId());
        assertEquals(releaseId, containerResponseEntity.getReleasedId());
        assertEquals(KieContainerStatus.STOPPED, containerResponseEntity.getStatus());

        // Container is in stopped state, so there are no containers deployed in kie server.
        ServiceResponse<KieContainerResourceList> containersList = client.listContainers();
        assertEquals(ServiceResponse.ResponseType.SUCCESS, containersList.getType());
        assertNullOrEmpty("Active containers found!", containersList.getResult().getContainers());

        // Undeploy container for kie server instance.
        controllerClient.deleteContainerSpec(serverTemplate.getId(), CONTAINER_ID);

        // Check that container is disposed.
        try {
            controllerClient.getContainerInfo(serverTemplate.getId(), CONTAINER_ID);
            fail("Should throw exception about container info not found.");
        } catch (UnexpectedResponseCodeException e) {
            assertEquals(404, e.getResponseCode());
        }
    }

    @Test
    public void testCreateContainerOnNotExistingKieServerInstance() {
        // Try to create container using kie controller without created kie server instance.
        ContainerSpec containerToDeploy = new ContainerSpec(CONTAINER_ID, CONTAINER_NAME, new ServerTemplate(), releaseId, KieContainerStatus.STOPPED, new HashMap());
        try {

            controllerClient.saveContainerSpec(kieServerInfo.getServerId(), containerToDeploy);
            fail("Should throw exception about kie server instance not found.");
        } catch (UnexpectedResponseCodeException e) {
            assertEquals(404, e.getResponseCode());
        }
    }

    @Test
    public void testCreateDuplicitContainer() {
        // Create kie server instance connection in controller.
        ServerTemplate serverTemplate = createServerTemplate();

        // Deploy container for kie server instance.
        ContainerSpec containerToDeploy = new ContainerSpec(CONTAINER_ID, CONTAINER_NAME, serverTemplate, releaseId, KieContainerStatus.STOPPED, new HashMap());
        controllerClient.saveContainerSpec(serverTemplate.getId(), containerToDeploy);

        try {
            // Try to create same container.
            controllerClient.saveContainerSpec(serverTemplate.getId(), containerToDeploy);
            fail("Should throw exception about container being created already.");
        } catch (UnexpectedResponseCodeException e) {
            assertEquals(400, e.getResponseCode());
        }
    }

    @Test
    public void testDeleteNotExistingContainer() {
        // Try to dispose not existing container using kie controller without created kie server instance.
        try {
            controllerClient.deleteContainerSpec(kieServerInfo.getServerId(), CONTAINER_ID);
            fail("Should throw exception about kie server instance not exists.");
        } catch (UnexpectedResponseCodeException e) {
            assertEquals(404, e.getResponseCode());
        }

        createServerTemplate();
        // Try to dispose not existing container using kie controller with created kie server instance.
        try {
            controllerClient.deleteContainerSpec(kieServerInfo.getServerId(), CONTAINER_ID);
            fail("Should throw exception about container not exists.");
        } catch (UnexpectedResponseCodeException e) {
            assertEquals(404, e.getResponseCode());
        }
    }

    @Test
    public void testGetContainer() {
        // Create kie server instance connection in controller.
        ServerTemplate serverTemplate = createServerTemplate();

        // Deploy container for kie server instance.
        ContainerSpec containerToDeploy = new ContainerSpec(CONTAINER_ID, CONTAINER_NAME, serverTemplate, releaseId, KieContainerStatus.STOPPED, new HashMap());
        controllerClient.saveContainerSpec(serverTemplate.getId(), containerToDeploy);

        // Get container using kie controller.
        ContainerSpec containerResponseEntity = controllerClient.getContainerInfo(kieServerInfo.getServerId(), CONTAINER_ID);
        assertNotNull(containerResponseEntity);
        assertEquals(CONTAINER_ID, containerResponseEntity.getId());
        assertEquals(CONTAINER_NAME, containerResponseEntity.getContainerName());
        assertEquals(releaseId, containerResponseEntity.getReleasedId());
        assertEquals(KieContainerStatus.STOPPED, containerResponseEntity.getStatus());
    }

    @Test
    public void testStartAndStopContainer() throws Exception {
        // Create kie server instance connection in controller.
        ServerTemplate serverTemplate = createServerTemplate();

        // Deploy container for kie server instance.
        ContainerSpec containerToDeploy = new ContainerSpec(CONTAINER_ID, CONTAINER_NAME, serverTemplate, releaseId, KieContainerStatus.STOPPED, new HashMap());
        controllerClient.saveContainerSpec(serverTemplate.getId(), containerToDeploy);

        // Get container using kie controller.
        ContainerSpec containerResponseEntity = controllerClient.getContainerInfo(kieServerInfo.getServerId(), CONTAINER_ID);
        assertNotNull(containerResponseEntity);
        assertEquals(CONTAINER_ID, containerResponseEntity.getId());
        assertEquals(releaseId, containerResponseEntity.getReleasedId());
        assertEquals(KieContainerStatus.STOPPED, containerResponseEntity.getStatus());

        // Check that container is not deployed in kie server (as container is in STOPPED state).
        ServiceResponse<KieContainerResource> containerInfo = client.getContainerInfo(CONTAINER_ID);
        assertEquals(ServiceResponse.ResponseType.FAILURE, containerInfo.getType());
        assertResultContainsString(containerInfo.getMsg(), "Container " + CONTAINER_ID + " is not instantiated.");

        controllerClient.startContainer(kieServerInfo.getServerId(), CONTAINER_ID);

        containerResponseEntity = controllerClient.getContainerInfo(kieServerInfo.getServerId(), CONTAINER_ID);
        assertNotNull(containerResponseEntity);
        assertEquals(CONTAINER_ID, containerResponseEntity.getId());
        assertEquals(releaseId, containerResponseEntity.getReleasedId());
        assertEquals(KieContainerStatus.STARTED, containerResponseEntity.getStatus());

        // Check that container is deployed in kie server.
        waitForKieServerSynchronization(1);
        containerInfo = client.getContainerInfo(CONTAINER_ID);
        assertEquals(ServiceResponse.ResponseType.SUCCESS, containerInfo.getType());
        assertEquals(CONTAINER_ID, containerInfo.getResult().getContainerId());
        assertEquals(KieContainerStatus.STARTED, containerInfo.getResult().getStatus());
        assertEquals(releaseId, containerInfo.getResult().getReleaseId());

        controllerClient.stopContainer(kieServerInfo.getServerId(), CONTAINER_ID);

        containerResponseEntity = controllerClient.getContainerInfo(kieServerInfo.getServerId(), CONTAINER_ID);
        assertNotNull(containerResponseEntity);
        assertEquals(CONTAINER_ID, containerResponseEntity.getId());
        assertEquals(releaseId, containerResponseEntity.getReleasedId());
        assertEquals(KieContainerStatus.STOPPED, containerResponseEntity.getStatus());

        // Check that container is not deployed in kie server (as container is in STOPPED state).
        waitForKieServerSynchronization(0);
        containerInfo = client.getContainerInfo(CONTAINER_ID);
        assertEquals(ServiceResponse.ResponseType.FAILURE, containerInfo.getType());
        assertResultContainsString(containerInfo.getMsg(), "Container " + CONTAINER_ID + " is not instantiated.");
    }

    @Test
    public void testStartNotExistingContainer() throws Exception {
        // Try to start not existing container using kie controller without created kie server instance.
        try {
            controllerClient.startContainer(kieServerInfo.getServerId(), CONTAINER_ID);
            fail("Should throw exception about container not found.");
        } catch (UnexpectedResponseCodeException e) {
            assertEquals(404, e.getResponseCode());
        }

        createServerTemplate();
        // Try to start not existing container using kie controller with created kie server instance.
        try {
            controllerClient.startContainer(kieServerInfo.getServerId(), CONTAINER_ID);
            fail("Should throw exception about container not found.");
        } catch (UnexpectedResponseCodeException e) {
            assertEquals(404, e.getResponseCode());
        }
    }

    @Test
    public void testStopNotExistingContainer() throws Exception {
        // Try to stop not existing container using kie controller without created kie server instance.
        try {
            controllerClient.stopContainer(kieServerInfo.getServerId(), CONTAINER_ID);
            fail("Should throw exception about container not found.");
        } catch (UnexpectedResponseCodeException e) {
            assertEquals(404, e.getResponseCode());
        }

        createServerTemplate();
        // Try to stop not existing container using kie controller with created kie server instance.
        try {
            controllerClient.stopContainer(kieServerInfo.getServerId(), CONTAINER_ID);
            fail("Should throw exception about container not found.");
        } catch (UnexpectedResponseCodeException e) {
            assertEquals(404, e.getResponseCode());
        }
    }

    @Test
    public void testGetNotExistingContainer() {
        // Try to get not existing container using kie controller without created kie server instance.
        try {
            controllerClient.getContainerInfo(kieServerInfo.getServerId(), CONTAINER_ID);
            fail("Should throw exception about container info not found.");
        } catch (UnexpectedResponseCodeException e) {
            assertEquals(404, e.getResponseCode());
        }

        createServerTemplate();
        // Try to get not existing container using kie controller with created kie server instance.
        try {
            controllerClient.getContainerInfo(kieServerInfo.getServerId(), CONTAINER_ID);
            fail("Should throw exception about container info not found.");
        } catch (UnexpectedResponseCodeException e) {
            assertEquals(404, e.getResponseCode());
        }
    }

    @Test
    public void testListContainers() {
        // Create kie server instance connection in controller.
        ServerTemplate serverTemplate = createServerTemplate();

        // Deploy container for kie server instance.
        ContainerSpec containerToDeploy = new ContainerSpec(CONTAINER_ID, CONTAINER_NAME, serverTemplate, releaseId, KieContainerStatus.STOPPED, new HashMap());
        controllerClient.saveContainerSpec(serverTemplate.getId(), containerToDeploy);

        Collection<ContainerSpec> containerList = controllerClient.listContainerSpec(kieServerInfo.getServerId());

        assertNotNull(containerList);
        assertEquals(1, containerList.size());

        ContainerSpec containerResponseEntity = containerList.iterator().next();
        assertNotNull(containerResponseEntity);
        assertEquals(CONTAINER_ID, containerResponseEntity.getId());
        assertEquals(CONTAINER_NAME, containerResponseEntity.getContainerName());
        assertEquals(releaseId, containerResponseEntity.getReleasedId());
        assertEquals(KieContainerStatus.STOPPED, containerResponseEntity.getStatus());
    }

    @Test
    public void testEmptyListContainers() {
        try {
            Collection<ContainerSpec> emptyList = controllerClient.listContainerSpec(kieServerInfo.getServerId());
            fail("Should throw exception about kie server instance not existing.");
        } catch (UnexpectedResponseCodeException e) {
            assertEquals(404, e.getResponseCode());
        }

        // Create kie server instance connection in controller.
        createServerTemplate();

        Collection<ContainerSpec> emptyList = controllerClient.listContainerSpec(kieServerInfo.getServerId());
        assertNullOrEmpty("Active containers found!", emptyList);
    }

    @Test
    public void testUpdateContainerConfig() {
        // Create kie server instance connection in controller.
        ServerTemplate serverTemplate = createServerTemplate();

        Map<Capability, ContainerConfig> containerConfigMap = new HashMap();

        ProcessConfig processConfig = new ProcessConfig("PER_PROCESS_INSTANCE", "kieBase", "kieSession", "MERGE_COLLECTION");
        containerConfigMap.put(Capability.PROCESS, processConfig);

        RuleConfig ruleConfig = new RuleConfig(500l, KieScannerStatus.SCANNING);
        containerConfigMap.put(Capability.RULE, ruleConfig);

        ContainerSpec containerSpec = new ContainerSpec(CONTAINER_ID, CONTAINER_NAME, serverTemplate, releaseId, KieContainerStatus.STOPPED, containerConfigMap);
        controllerClient.saveContainerSpec(serverTemplate.getId(), containerSpec);

        ContainerSpec containerResponseEntity = controllerClient.getContainerInfo(kieServerInfo.getServerId(), CONTAINER_ID);
        assertNotNull(containerResponseEntity);
        assertEquals(CONTAINER_ID, containerResponseEntity.getId());
        assertEquals(releaseId, containerResponseEntity.getReleasedId());
        assertEquals(KieContainerStatus.STOPPED, containerResponseEntity.getStatus());

        // Check process config and rule config
        checkContainerConfig(kieServerInfo.getServerId(), CONTAINER_ID, processConfig, ruleConfig);

        processConfig = new ProcessConfig("SINGLETON", "defaultKieBase", "defaultKieSession", "OVERRIDE_ALL");
        controllerClient.updateContainerConfig(kieServerInfo.getServerId(), CONTAINER_ID, Capability.PROCESS, processConfig);

        // Check process config and rule config
        checkContainerConfig(kieServerInfo.getServerId(), CONTAINER_ID, processConfig, ruleConfig);

        ruleConfig = new RuleConfig(1000l, KieScannerStatus.STOPPED);
        controllerClient.updateContainerConfig(kieServerInfo.getServerId(), CONTAINER_ID, Capability.RULE, ruleConfig);

        // Check process config and rule config
        checkContainerConfig(kieServerInfo.getServerId(), CONTAINER_ID, processConfig, ruleConfig);
    }

    @Test
    public void testUpdateNotExistingContainerConfig() {
        // Create kie server instance connection in controller.
        ServerTemplate serverTemplate = createServerTemplate();

        ContainerSpec containerSpec = new ContainerSpec(CONTAINER_ID, CONTAINER_NAME, serverTemplate, releaseId, KieContainerStatus.STOPPED, new HashMap());
        controllerClient.saveContainerSpec(serverTemplate.getId(), containerSpec);

        ContainerSpec containerResponseEntity = controllerClient.getContainerInfo(kieServerInfo.getServerId(), CONTAINER_ID);
        assertNotNull(containerResponseEntity);
        assertEquals(CONTAINER_ID, containerResponseEntity.getId());
        assertEquals(releaseId, containerResponseEntity.getReleasedId());
        assertEquals(KieContainerStatus.STOPPED, containerResponseEntity.getStatus());
        assertNullOrEmpty("Config is not empty.", containerResponseEntity.getConfigs().values());

        // Try update not existing ProcessConfig
        try {
            ProcessConfig processConfig = new ProcessConfig("PER_PROCESS_INSTANCE", "kieBase", "kieSession", "MERGE_COLLECTION");
            controllerClient.updateContainerConfig(kieServerInfo.getServerId(), CONTAINER_ID, Capability.PROCESS, processConfig);
            fail("Should throw exception about process config not existing.");
        } catch (UnexpectedResponseCodeException e) {
            assertEquals(400, e.getResponseCode());
        }

        // Try update not existing RuleConfig
        try {
            RuleConfig ruleConfig = new RuleConfig(500l, KieScannerStatus.SCANNING);
            controllerClient.updateContainerConfig(kieServerInfo.getServerId(), CONTAINER_ID, Capability.RULE, ruleConfig);
            fail("Should throw exception about rule config not existing.");
        } catch (UnexpectedResponseCodeException e) {
            assertEquals(400, e.getResponseCode());
        }
    }

    @Test
    public void testUpdateContainerConfigOnNotExistingContainer() {
        ProcessConfig config = new ProcessConfig("PER_PROCESS_INSTANCE", "kieBase", "kieSession", "MERGE_COLLECTION");
        try {
            controllerClient.updateContainerConfig(kieServerInfo.getServerId(), CONTAINER_ID, Capability.PROCESS, config);
            fail("Should throw exception about kie server instance not existing.");
        } catch (UnexpectedResponseCodeException e) {
            assertEquals(400, e.getResponseCode());
        }

        // Create kie server instance connection in controller.
        createServerTemplate();

        try {
            controllerClient.updateContainerConfig(kieServerInfo.getServerId(), CONTAINER_ID, Capability.PROCESS, config);
            fail("Should throw exception about container info not found.");
        } catch (UnexpectedResponseCodeException e) {
            assertEquals(400, e.getResponseCode());
        }
    }

    protected ServerTemplate createServerTemplate() {
        ServerTemplate serverTemplate = new ServerTemplate();
        serverTemplate.setId(kieServerInfo.getServerId());
        serverTemplate.setName(kieServerInfo.getName());

        serverTemplate.addServerInstance(ModelFactory.newServerInstanceKey(serverTemplate.getId(), kieServerInfo.getLocation()));
        controllerClient.saveServerTemplate(serverTemplate);

        return serverTemplate;
    }

    protected void checkContainerConfig(String serverTemplateId, String containerId, ContainerConfig... configs) {
        Map<Capability, ContainerConfig> configMap = controllerClient.getContainerInfo(serverTemplateId, containerId).getConfigs();
        assertNotNull(configMap);

        for (ContainerConfig config : configs) {
            if (config instanceof ProcessConfig) {
                ProcessConfig pc = (ProcessConfig) config;
                ProcessConfig processConfig = (ProcessConfig) configMap.get(Capability.PROCESS);
                assertNotNull(processConfig);
                assertEquals(pc.getKBase(), processConfig.getKBase());
                assertEquals(pc.getKSession(), processConfig.getKSession());
                assertEquals(pc.getMergeMode(), processConfig.getMergeMode());
                assertEquals(pc.getRuntimeStrategy(), processConfig.getRuntimeStrategy());
            } else if (config instanceof RuleConfig) {
                RuleConfig rc = (RuleConfig) config;
                RuleConfig ruleConfig = (RuleConfig) configMap.get(Capability.RULE);
                assertNotNull(ruleConfig);
                assertEquals(rc.getPollInterval(), ruleConfig.getPollInterval());
                assertEquals(rc.getScannerStatus(), ruleConfig.getScannerStatus());
            }
        }
    }
}
