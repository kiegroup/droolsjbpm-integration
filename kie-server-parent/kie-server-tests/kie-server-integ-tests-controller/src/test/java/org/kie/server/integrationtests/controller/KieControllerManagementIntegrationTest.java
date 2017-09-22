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
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.kie.server.api.model.KieContainerResource;
import org.kie.server.api.model.KieContainerResourceList;
import org.kie.server.api.model.KieContainerStatus;
import org.kie.server.api.model.KieScannerResource;
import org.kie.server.api.model.KieScannerStatus;
import org.kie.server.api.model.KieServerConfigItem;
import org.kie.server.api.model.KieServerInfo;
import org.kie.server.api.model.KieServerStateInfo;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.controller.api.ModelFactory;
import org.kie.server.controller.api.model.runtime.ServerInstanceKey;
import org.kie.server.controller.api.model.spec.Capability;
import org.kie.server.controller.api.model.spec.ContainerConfig;
import org.kie.server.controller.api.model.spec.ContainerSpec;
import org.kie.server.controller.api.model.spec.ProcessConfig;
import org.kie.server.controller.api.model.spec.RuleConfig;
import org.kie.server.controller.api.model.spec.ServerTemplate;
import org.kie.server.controller.impl.storage.InMemoryKieServerTemplateStorage;
import org.kie.server.integrationtests.category.Smoke;
import org.kie.server.integrationtests.controller.client.exception.UnexpectedResponseCodeException;
import org.kie.server.integrationtests.shared.KieServerAssert;
import org.kie.server.integrationtests.shared.KieServerDeployer;
import org.kie.server.integrationtests.shared.KieServerSynchronization;

import static org.junit.Assert.*;

public class KieControllerManagementIntegrationTest extends KieControllerManagementBaseTest {

    private static final String CONTAINER_ID = "kie-concurrent";
    private static final String CONTAINER_NAME = "containerName";
    private static ReleaseId releaseId = new ReleaseId("org.kie.server.testing", "stateless-session-kjar", "1.0.0-SNAPSHOT");
    private KieServerInfo kieServerInfo;

    @BeforeClass
    public static void initialize() throws Exception {
        KieServerDeployer.createAndDeployKJar(releaseId);
    }

    @Before
    public void getKieServerInfo() {
        InMemoryKieServerTemplateStorage.getInstance().clear();
        // Getting info from currently started kie server.
        ServiceResponse<KieServerInfo> reply = client.getServerInfo();
        KieServerAssert.assertSuccess(reply);
        kieServerInfo = reply.getResult();
    }

    @Test
    @Category(Smoke.class)
    public void testCreateKieServerInstance() {
        ServerTemplate serverTemplate = createServerTemplate();

        ServerTemplate storedServerTemplate = mgmtControllerClient.getServerTemplate(serverTemplate.getId());
        checkServerTemplate(storedServerTemplate);

        Collection<ServerTemplate> serverTemplates = mgmtControllerClient.listServerTemplates();
        assertNotNull(serverTemplates);
        assertEquals(1, serverTemplates.size());

        storedServerTemplate = serverTemplates.iterator().next();
        checkServerTemplate(storedServerTemplate);
    }

    @Test
    public void testCreateDuplicitKieServerInstance() {
        // Create kie server instance in controller.
        ServerTemplate serverTemplate = createServerTemplate();

        try {
            // Try to create same kie server instance.
            mgmtControllerClient.saveServerTemplate(serverTemplate);
            fail("Should throw exception about kie server instance already created.");
        } catch (UnexpectedResponseCodeException e) {
            assertEquals(404, e.getResponseCode());
        }
    }

    @Test
    public void testDeleteKieServerInstance() {
        ServerTemplate serverTemplate = createServerTemplate();

        Collection<ServerTemplate> serverTemplates = mgmtControllerClient.listServerTemplates();
        assertNotNull(serverTemplates);
        assertEquals(1, serverTemplates.size());

        // Delete created kie server instance.
        mgmtControllerClient.deleteServerTemplate(serverTemplate.getId());

        // There are no kie server instances in controller now.
        serverTemplates = mgmtControllerClient.listServerTemplates();
        KieServerAssert.assertNullOrEmpty("Active kie server instance found!", serverTemplates);
    }

    @Test
    public void testDeleteNotExistingKieServerInstance() {
        try {
            // Try to delete not existing kie server instance.
            mgmtControllerClient.deleteServerTemplate("not existing");
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
        ServerTemplate serverInstance = mgmtControllerClient.getServerTemplate(serverTemplate.getId());

        checkServerTemplate(serverInstance);

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
            mgmtControllerClient.getServerTemplate(kieServerInfo.getServerId());
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
        Collection<ServerTemplate> instanceList = mgmtControllerClient.listServerTemplates();

        assertNotNull(instanceList);
        assertEquals(1, instanceList.size());

        ServerTemplate serverInstance = instanceList.iterator().next();
        checkServerTemplate(serverInstance);

        assertNotNull("Kie server instance isn't managed!", serverInstance.getServerInstanceKeys());
        assertEquals(1, serverInstance.getServerInstanceKeys().size());

        ServerInstanceKey managedInstance = serverInstance.getServerInstanceKeys().iterator().next();
        assertNotNull(managedInstance);
        assertEquals(kieServerInfo.getLocation(), managedInstance.getUrl());
    }

    @Test
    public void testEmptyListKieServerInstances() throws Exception {
        Collection<ServerTemplate> instanceList = mgmtControllerClient.listServerTemplates();
        KieServerAssert.assertNullOrEmpty("Active kie server instance found!", instanceList);
    }

    @Test
    public void testContainerHandling() {
        // Create kie server instance connection in controller.
        ServerTemplate serverTemplate = createServerTemplate();

        // Deploy container for kie server instance.
        ContainerSpec containerToDeploy = new ContainerSpec(CONTAINER_ID, CONTAINER_NAME, serverTemplate, releaseId, KieContainerStatus.STOPPED, new HashMap());
        mgmtControllerClient.saveContainerSpec(serverTemplate.getId(), containerToDeploy);

        // Check that container is deployed.
        ContainerSpec containerResponseEntity = mgmtControllerClient.getContainerInfo(kieServerInfo.getServerId(), CONTAINER_ID);
        checkContainer(containerResponseEntity, KieContainerStatus.STOPPED);

        // Container is in stopped state, so there are no containers deployed in kie server.
        ServiceResponse<KieContainerResourceList> containersList = client.listContainers();
        assertEquals(ServiceResponse.ResponseType.SUCCESS, containersList.getType());
        KieServerAssert.assertNullOrEmpty("Active containers found!", containersList.getResult().getContainers());

        // Undeploy container for kie server instance.
        mgmtControllerClient.deleteContainerSpec(serverTemplate.getId(), CONTAINER_ID);

        // Check that container is disposed.
        try {
            mgmtControllerClient.getContainerInfo(serverTemplate.getId(), CONTAINER_ID);
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

            mgmtControllerClient.saveContainerSpec(kieServerInfo.getServerId(), containerToDeploy);
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
        mgmtControllerClient.saveContainerSpec(serverTemplate.getId(), containerToDeploy);

        try {
            // Try to create same container.
            mgmtControllerClient.saveContainerSpec(serverTemplate.getId(), containerToDeploy);
            fail("Should throw exception about container being created already.");
        } catch (UnexpectedResponseCodeException e) {
            assertEquals(400, e.getResponseCode());
        }
    }

    @Test
    public void testDeleteNotExistingContainer() {
        // Try to dispose not existing container using kie controller without created kie server instance.
        try {
            mgmtControllerClient.deleteContainerSpec(kieServerInfo.getServerId(), CONTAINER_ID);
            fail("Should throw exception about kie server instance not exists.");
        } catch (UnexpectedResponseCodeException e) {
            assertEquals(404, e.getResponseCode());
        }

        createServerTemplate();
        // Try to dispose not existing container using kie controller with created kie server instance.
        try {
            mgmtControllerClient.deleteContainerSpec(kieServerInfo.getServerId(), CONTAINER_ID);
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
        mgmtControllerClient.saveContainerSpec(serverTemplate.getId(), containerToDeploy);

        // Get container using kie controller.
        ContainerSpec containerResponseEntity = mgmtControllerClient.getContainerInfo(kieServerInfo.getServerId(), CONTAINER_ID);
        checkContainer(containerResponseEntity, KieContainerStatus.STOPPED);
        assertEquals(CONTAINER_NAME, containerResponseEntity.getContainerName());
    }

    @Test
    public void testStartAndStopContainer() throws Exception {
        // Create kie server instance connection in controller.
        ServerTemplate serverTemplate = createServerTemplate();

        // Deploy container for kie server instance.
        ContainerSpec containerToDeploy = new ContainerSpec(CONTAINER_ID, CONTAINER_NAME, serverTemplate, releaseId, KieContainerStatus.STOPPED, new HashMap());
        mgmtControllerClient.saveContainerSpec(serverTemplate.getId(), containerToDeploy);

        // Get container using kie controller.
        ContainerSpec containerResponseEntity = mgmtControllerClient.getContainerInfo(kieServerInfo.getServerId(), CONTAINER_ID);
        checkContainer(containerResponseEntity, KieContainerStatus.STOPPED);

        // Check that container is not deployed in kie server (as container is in STOPPED state).
        ServiceResponse<KieContainerResource> containerInfo = client.getContainerInfo(CONTAINER_ID);
        assertEquals(ServiceResponse.ResponseType.FAILURE, containerInfo.getType());
        KieServerAssert.assertResultContainsString(containerInfo.getMsg(), "Container " + CONTAINER_ID + " is not instantiated.");

        mgmtControllerClient.startContainer(kieServerInfo.getServerId(), CONTAINER_ID);

        containerResponseEntity = mgmtControllerClient.getContainerInfo(kieServerInfo.getServerId(), CONTAINER_ID);
        checkContainer(containerResponseEntity, KieContainerStatus.STARTED);

        // Check that container is deployed in kie server.
        KieServerSynchronization.waitForKieServerSynchronization(client, 1);
        containerInfo = client.getContainerInfo(CONTAINER_ID);
        assertEquals(ServiceResponse.ResponseType.SUCCESS, containerInfo.getType());
        assertEquals(CONTAINER_ID, containerInfo.getResult().getContainerId());
        assertEquals(KieContainerStatus.STARTED, containerInfo.getResult().getStatus());
        assertEquals(releaseId, containerInfo.getResult().getReleaseId());

        mgmtControllerClient.stopContainer(kieServerInfo.getServerId(), CONTAINER_ID);

        containerResponseEntity = mgmtControllerClient.getContainerInfo(kieServerInfo.getServerId(), CONTAINER_ID);
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
            mgmtControllerClient.startContainer(kieServerInfo.getServerId(), CONTAINER_ID);
            fail("Should throw exception about container not found.");
        } catch (UnexpectedResponseCodeException e) {
            assertEquals(404, e.getResponseCode());
        }

        createServerTemplate();
        // Try to start not existing container using kie controller with created kie server instance.
        try {
            mgmtControllerClient.startContainer(kieServerInfo.getServerId(), CONTAINER_ID);
            fail("Should throw exception about container not found.");
        } catch (UnexpectedResponseCodeException e) {
            assertEquals(404, e.getResponseCode());
        }
    }

    @Test
    public void testStopNotExistingContainer() throws Exception {
        // Try to stop not existing container using kie controller without created kie server instance.
        try {
            mgmtControllerClient.stopContainer(kieServerInfo.getServerId(), CONTAINER_ID);
            fail("Should throw exception about container not found.");
        } catch (UnexpectedResponseCodeException e) {
            assertEquals(404, e.getResponseCode());
        }

        createServerTemplate();
        // Try to stop not existing container using kie controller with created kie server instance.
        try {
            mgmtControllerClient.stopContainer(kieServerInfo.getServerId(), CONTAINER_ID);
            fail("Should throw exception about container not found.");
        } catch (UnexpectedResponseCodeException e) {
            assertEquals(404, e.getResponseCode());
        }
    }

    @Test
    public void testGetNotExistingContainer() {
        // Try to get not existing container using kie controller without created kie server instance.
        try {
            mgmtControllerClient.getContainerInfo(kieServerInfo.getServerId(), CONTAINER_ID);
            fail("Should throw exception about container info not found.");
        } catch (UnexpectedResponseCodeException e) {
            assertEquals(404, e.getResponseCode());
        }

        createServerTemplate();
        // Try to get not existing container using kie controller with created kie server instance.
        try {
            mgmtControllerClient.getContainerInfo(kieServerInfo.getServerId(), CONTAINER_ID);
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
        mgmtControllerClient.saveContainerSpec(serverTemplate.getId(), containerToDeploy);

        Collection<ContainerSpec> containerList = mgmtControllerClient.listContainerSpec(kieServerInfo.getServerId());

        assertNotNull(containerList);
        assertEquals(1, containerList.size());

        ContainerSpec containerResponseEntity = containerList.iterator().next();
        checkContainer(containerResponseEntity, KieContainerStatus.STOPPED);
        assertEquals(CONTAINER_NAME, containerResponseEntity.getContainerName());
    }

    @Test
    public void testEmptyListContainers() {
        try {
            Collection<ContainerSpec> emptyList = mgmtControllerClient.listContainerSpec(kieServerInfo.getServerId());
            fail("Should throw exception about kie server instance not existing.");
        } catch (UnexpectedResponseCodeException e) {
            assertEquals(404, e.getResponseCode());
        }

        // Create kie server instance connection in controller.
        createServerTemplate();

        Collection<ContainerSpec> emptyList = mgmtControllerClient.listContainerSpec(kieServerInfo.getServerId());
        KieServerAssert.assertNullOrEmpty("Active containers found!", emptyList);
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
        mgmtControllerClient.saveContainerSpec(serverTemplate.getId(), containerSpec);

        ContainerSpec containerResponseEntity = mgmtControllerClient.getContainerInfo(kieServerInfo.getServerId(), CONTAINER_ID);
        checkContainer(containerResponseEntity, KieContainerStatus.STOPPED);

        // Check process config and rule config
        checkContainerConfig(kieServerInfo.getServerId(), CONTAINER_ID, processConfig, ruleConfig);

        processConfig = new ProcessConfig("SINGLETON", "defaultKieBase", "defaultKieSession", "OVERRIDE_ALL");
        mgmtControllerClient.updateContainerConfig(kieServerInfo.getServerId(), CONTAINER_ID, Capability.PROCESS, processConfig);

        // Check process config and rule config
        checkContainerConfig(kieServerInfo.getServerId(), CONTAINER_ID, processConfig, ruleConfig);

        ruleConfig = new RuleConfig(1000l, KieScannerStatus.STOPPED);
        mgmtControllerClient.updateContainerConfig(kieServerInfo.getServerId(), CONTAINER_ID, Capability.RULE, ruleConfig);

        // Check process config and rule config
        checkContainerConfig(kieServerInfo.getServerId(), CONTAINER_ID, processConfig, ruleConfig);
    }

    @Test
    public void testUpdateNotExistingContainerConfig() {
        // Create kie server instance connection in controller.
        ServerTemplate serverTemplate = createServerTemplate();

        ContainerSpec containerSpec = new ContainerSpec(CONTAINER_ID, CONTAINER_NAME, serverTemplate, releaseId, KieContainerStatus.STOPPED, new HashMap());
        mgmtControllerClient.saveContainerSpec(serverTemplate.getId(), containerSpec);

        ContainerSpec containerResponseEntity = mgmtControllerClient.getContainerInfo(kieServerInfo.getServerId(), CONTAINER_ID);
        checkContainer(containerResponseEntity, KieContainerStatus.STOPPED);
        KieServerAssert.assertNullOrEmpty("Config is not empty.", containerResponseEntity.getConfigs().values());

        // Try update not existing ProcessConfig
        ProcessConfig processConfig = new ProcessConfig("PER_PROCESS_INSTANCE", "kieBase", "kieSession", "MERGE_COLLECTION");
        mgmtControllerClient.updateContainerConfig(kieServerInfo.getServerId(), CONTAINER_ID, Capability.PROCESS, processConfig);

        // Try update not existing RuleConfig
        RuleConfig ruleConfig = new RuleConfig(500l, KieScannerStatus.SCANNING);
        mgmtControllerClient.updateContainerConfig(kieServerInfo.getServerId(), CONTAINER_ID, Capability.RULE, ruleConfig);
    }

    @Test
    public void testUpdateContainerConfigOnNotExistingContainer() {
        ProcessConfig config = new ProcessConfig("PER_PROCESS_INSTANCE", "kieBase", "kieSession", "MERGE_COLLECTION");
        try {
            mgmtControllerClient.updateContainerConfig(kieServerInfo.getServerId(), CONTAINER_ID, Capability.PROCESS, config);
            fail("Should throw exception about kie server instance not existing.");
        } catch (UnexpectedResponseCodeException e) {
            assertEquals(404, e.getResponseCode());
        }

        // Create kie server instance connection in controller.
        createServerTemplate();

        try {
            mgmtControllerClient.updateContainerConfig(kieServerInfo.getServerId(), CONTAINER_ID, Capability.PROCESS, config);
            fail("Should throw exception about container info not found.");
        } catch (UnexpectedResponseCodeException e) {
            assertEquals(404, e.getResponseCode());
        }
    }

    protected ServerTemplate createServerTemplate() {
        ServerTemplate serverTemplate = new ServerTemplate();
        serverTemplate.setId(kieServerInfo.getServerId());
        serverTemplate.setName(kieServerInfo.getName());

        serverTemplate.addServerInstance(ModelFactory.newServerInstanceKey(serverTemplate.getId(), kieServerInfo.getLocation()));
        mgmtControllerClient.saveServerTemplate(serverTemplate);

        return serverTemplate;
    }

    protected void checkContainer(ContainerSpec container, KieContainerStatus status) {
        assertNotNull(container);
        assertEquals(CONTAINER_ID, container.getId());
        assertEquals(releaseId, container.getReleasedId());
        assertEquals(status, container.getStatus());
    }

    protected void checkContainerConfig(String serverTemplateId, String containerId, ContainerConfig... configs) {
        Map<Capability, ContainerConfig> configMap = mgmtControllerClient.getContainerInfo(serverTemplateId, containerId).getConfigs();
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

    protected void checkServerTemplate(ServerTemplate actual) {
        assertNotNull(actual);
        assertEquals(kieServerInfo.getServerId(), actual.getId());
        assertEquals(kieServerInfo.getName(), actual.getName());
    }

    @Test
    public void testUpdateContainerConfigSent() throws Exception {
        // The usual setup of the kie-server along with a container spec
        ServerTemplate serverTemplate = createServerTemplate();
        Map<Capability, ContainerConfig> containerConfigMap = new HashMap<>();
        ProcessConfig processConfig = new ProcessConfig("PER_PROCESS_INSTANCE", "kieBase", "kieSession", "MERGE_COLLECTION");
        containerConfigMap.put(Capability.PROCESS, processConfig);
        RuleConfig ruleConfig = new RuleConfig(500L, KieScannerStatus.STARTED);
        containerConfigMap.put(Capability.RULE, ruleConfig);
        ContainerSpec containerSpec = new ContainerSpec(CONTAINER_ID, CONTAINER_NAME, serverTemplate, releaseId, KieContainerStatus.STARTED, containerConfigMap);

        // Tell the controller to save the spec for the given template, which since the
        // container status is STARTED should also cause it to be deployed to the kie-server
        mgmtControllerClient.saveContainerSpec(serverTemplate.getId(), containerSpec);
        KieServerSynchronization.waitForKieServerSynchronization(client, 1);
        checkContainerConfigAgainstServer(processConfig, ruleConfig);

        // Update the rule configuration, turning off the scanner
        ruleConfig.setScannerStatus(KieScannerStatus.STOPPED);
        mgmtControllerClient.updateContainerConfig(kieServerInfo.getServerId(), CONTAINER_ID, Capability.RULE, ruleConfig);

        // Check the rule configuration
        KieServerSynchronization.waitForKieServerScannerStatus(client, CONTAINER_ID, KieScannerStatus.STOPPED);
        checkContainerConfigAgainstServer(ruleConfig);

        // Update the configuration
        processConfig = new ProcessConfig("SINGLETON", "defaultKieBase", "defaultKieSession", "OVERRIDE_ALL");
        mgmtControllerClient.updateContainerConfig(kieServerInfo.getServerId(), CONTAINER_ID, Capability.PROCESS, processConfig);

        // Reset the container, since the update process should not do that by itself
        mgmtControllerClient.stopContainer(kieServerInfo.getServerId(), CONTAINER_ID);
        mgmtControllerClient.startContainer(kieServerInfo.getServerId(), CONTAINER_ID);

        // Update the process configuration
        KieServerSynchronization.waitForKieServerConfig(client, CONTAINER_ID, "MergeMode", "OVERRIDE_ALL");
        checkContainerConfigAgainstServer(processConfig);

        // Restart the scanner with the new interval
        ruleConfig.setScannerStatus(KieScannerStatus.STARTED);
        ruleConfig.setPollInterval(1000L);
        mgmtControllerClient.updateContainerConfig(kieServerInfo.getServerId(), CONTAINER_ID, Capability.RULE, ruleConfig);
        KieServerSynchronization.waitForKieServerScannerStatus(client, CONTAINER_ID, KieScannerStatus.STARTED, 1000L);
        checkContainerConfigAgainstServer(ruleConfig, processConfig);
    }

    @Test
    public void testDeleteContainerStopsContainer() throws Exception {
        ServerTemplate serverTemplate = createServerTemplate();
        Map<Capability, ContainerConfig> containerConfigMap = new HashMap<>();

        ContainerSpec containerSpec = new ContainerSpec(CONTAINER_ID, CONTAINER_NAME, serverTemplate, releaseId, KieContainerStatus.STARTED, containerConfigMap);
        mgmtControllerClient.saveContainerSpec(serverTemplate.getId(), containerSpec);
        KieServerSynchronization.waitForKieServerSynchronization(client, 1);

        ServiceResponse<KieServerStateInfo> response = client.getServerState();
        KieServerAssert.assertSuccess(response);

        KieServerStateInfo serverState = response.getResult();
        assertNotNull(serverState);
        assertTrue("Expected to find containers, but none were found", serverState.getContainers() != null && serverState.getContainers().size() > 0);

        mgmtControllerClient.deleteContainerSpec(serverTemplate.getId(), CONTAINER_ID);
        KieServerSynchronization.waitForKieServerSynchronization(client, 0);

        response = client.getServerState();
        serverState = response.getResult();
        assertNotNull(serverState);
        assertFalse("Did not expect to find containers", serverState.getContainers() != null && serverState.getContainers().size() > 0);
    }

    protected void checkContainerConfigAgainstServer(ContainerConfig... configs) {
        ServiceResponse<KieContainerResource> containerResource = client.getContainerInfo(CONTAINER_ID);
        KieServerAssert.assertSuccess(containerResource);

        KieContainerResource kcr = containerResource.getResult();
        assertNotNull(kcr);
        for (ContainerConfig config : configs) {
            if (config instanceof ProcessConfig) {
                ProcessConfig pc = (ProcessConfig) config;
                Map<String, String> configMap = new HashMap<>();
                configMap.put("KBase", pc.getKBase());
                configMap.put("KSession", pc.getKSession());
                configMap.put("MergeMode", pc.getMergeMode());
                configMap.put("RuntimeStrategy", pc.getRuntimeStrategy());

                assertNotNull("No configuration items found for checking process configuration", kcr.getConfigItems());
                List<KieServerConfigItem> kci = kcr.getConfigItems();
                for (KieServerConfigItem item : kci) {
                    String name = item.getName();
                    String value = item.getValue();
                    assertEquals(configMap.get(name), value);
                }
            } else if (config instanceof RuleConfig) {
                RuleConfig rc = (RuleConfig) config;
                KieScannerResource scanner = kcr.getScanner();
                assertNotNull("No scanner resource found", scanner);
                assertEquals(rc.getScannerStatus(), scanner.getStatus());
                // Only test the polling interval when starting the scanner
                // since it could be wrong at any other time
                if (rc.getScannerStatus() == KieScannerStatus.STARTED) {
                    assertEquals(rc.getPollInterval(), scanner.getPollInterval());
                }
            }
        }
    }
}
