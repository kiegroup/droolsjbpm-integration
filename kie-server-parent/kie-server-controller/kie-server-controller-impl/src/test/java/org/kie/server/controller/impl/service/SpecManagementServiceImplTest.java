/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
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

package org.kie.server.controller.impl.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.kie.server.api.model.KieContainerStatus;
import org.kie.server.api.model.KieScannerStatus;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.controller.api.KieServerControllerIllegalArgumentException;
import org.kie.server.controller.api.model.events.ServerTemplateUpdated;
import org.kie.server.controller.api.model.runtime.Container;
import org.kie.server.controller.api.model.runtime.ServerInstanceKey;
import org.kie.server.controller.api.model.spec.Capability;
import org.kie.server.controller.api.model.spec.ContainerConfig;
import org.kie.server.controller.api.model.spec.ContainerSpec;
import org.kie.server.controller.api.model.spec.ContainerSpecList;
import org.kie.server.controller.api.model.spec.ProcessConfig;
import org.kie.server.controller.api.model.spec.RuleConfig;
import org.kie.server.controller.api.model.spec.ServerTemplate;
import org.kie.server.controller.api.model.spec.ServerTemplateKey;
import org.kie.server.controller.api.model.spec.ServerTemplateKeyList;
import org.kie.server.controller.api.model.spec.ServerTemplateList;
import org.kie.server.controller.api.service.NotificationService;
import org.kie.server.controller.api.storage.KieServerTemplateStorage;
import org.kie.server.controller.impl.KieServerInstanceManager;
import org.kie.server.controller.impl.storage.InMemoryKieServerTemplateStorage;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SpecManagementServiceImplTest extends AbstractServiceImplTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Mock
    private KieServerTemplateStorage templateStorage;

    @Mock
    private KieServerInstanceManager kieServerInstanceManager;

    @Mock
    private NotificationService notificationService;

    @Before
    public void setup() {
        specManagementService = new SpecManagementServiceImpl();

        final SpecManagementServiceImpl specManagementService = (SpecManagementServiceImpl) this.specManagementService;

        specManagementService.setKieServerInstanceManager(kieServerInstanceManager);
    }

    @After
    public void cleanup() {
        InMemoryKieServerTemplateStorage.getInstance().clear();
    }

    @Test
    public void testCreateServerTemplate() {

        ServerTemplate serverTemplate = new ServerTemplate();

        serverTemplate.setName("test server");
        serverTemplate.setId(UUID.randomUUID().toString());

        specManagementService.saveServerTemplate(serverTemplate);

        ServerTemplateKeyList existing = specManagementService.listServerTemplateKeys();
        assertNotNull(existing);
        assertEquals(1, existing.getServerTemplates().length);

        org.kie.server.controller.api.model.spec.ServerTemplateKey saved = existing.getServerTemplates()[0];

        assertEquals(serverTemplate.getName(), saved.getName());
        assertEquals(serverTemplate.getId(), saved.getId());
    }
    
    @Test
    public void notabletoCreateContainerWithEmptyid() {

        ServerTemplate serverTemplate = new ServerTemplate();

        serverTemplate.setName("test server");
        serverTemplate.setId(UUID.randomUUID().toString());

        specManagementService.saveServerTemplate(serverTemplate);

        ServerTemplateKeyList existing = specManagementService.listServerTemplateKeys();
        assertNotNull(existing);
        assertEquals(1, existing.getServerTemplates().length);
        
        Map<Capability, ContainerConfig> configs = new HashMap<Capability, ContainerConfig>();
        RuleConfig ruleConfig = new RuleConfig();
        ruleConfig.setPollInterval(1000l);
        ruleConfig.setScannerStatus(KieScannerStatus.STARTED);

        configs.put(Capability.RULE, ruleConfig);

        ProcessConfig processConfig = new ProcessConfig();
        processConfig.setKBase("defaultKieBase");
        processConfig.setKSession("defaultKieSession");
        processConfig.setMergeMode("MERGE_COLLECTION");
        processConfig.setRuntimeStrategy("PER_PROCESS_INSTANCE");

        configs.put(Capability.PROCESS, processConfig);

        ContainerSpec containerSpec = new ContainerSpec();
        containerSpec.setId("");
        containerSpec.setServerTemplateKey(new ServerTemplateKey(serverTemplate.getId(), serverTemplate.getName()));
        containerSpec.setReleasedId(new ReleaseId("org.kie", "kie-server-kjar", "1.0"));
        containerSpec.setStatus(KieContainerStatus.STOPPED);
        containerSpec.setConfigs(configs);

        try {
            specManagementService.saveContainerSpec(serverTemplate.getId(), containerSpec);
            fail("Cannot create container with empty container id.");
        } catch (Exception ex) {
            assertEquals("Cannot create container with empty container id.",
                         ex.getMessage());
        }


        org.kie.server.controller.api.model.spec.ServerTemplate createdServerTemplate = specManagementService.getServerTemplate(serverTemplate.getId());
        assertEquals(0, createdServerTemplate.getContainersSpec().size());

    }

    @Test
    public void testCreateServerTemplateAndContainer() {

        ServerTemplate serverTemplate = new ServerTemplate();

        serverTemplate.setName("test server");
        serverTemplate.setId(UUID.randomUUID().toString());

        specManagementService.saveServerTemplate(serverTemplate);

        ServerTemplateKeyList existing = specManagementService.listServerTemplateKeys();
        assertNotNull(existing);
        assertEquals(1, existing.getServerTemplates().length);

        Map<Capability, ContainerConfig> configs = new HashMap<Capability, ContainerConfig>();
        RuleConfig ruleConfig = new RuleConfig();
        ruleConfig.setPollInterval(1000l);
        ruleConfig.setScannerStatus(KieScannerStatus.STARTED);

        configs.put(Capability.RULE, ruleConfig);

        ProcessConfig processConfig = new ProcessConfig();
        processConfig.setKBase("defaultKieBase");
        processConfig.setKSession("defaultKieSession");
        processConfig.setMergeMode("MERGE_COLLECTION");
        processConfig.setRuntimeStrategy("PER_PROCESS_INSTANCE");

        configs.put(Capability.PROCESS, processConfig);

        ContainerSpec containerSpec = new ContainerSpec();
        containerSpec.setId("test container");
        containerSpec.setServerTemplateKey(new ServerTemplateKey(serverTemplate.getId(), serverTemplate.getName()));
        containerSpec.setReleasedId(new ReleaseId("org.kie", "kie-server-kjar", "1.0"));
        containerSpec.setStatus(KieContainerStatus.STOPPED);
        containerSpec.setConfigs(configs);

        specManagementService.saveContainerSpec(serverTemplate.getId(), containerSpec);

        org.kie.server.controller.api.model.spec.ServerTemplate createdServerTemplate = specManagementService.getServerTemplate(serverTemplate.getId());
        assertNotNull(createdServerTemplate);
        assertNotNull(createdServerTemplate.getContainersSpec());
        assertEquals(1, createdServerTemplate.getContainersSpec().size());

        org.kie.server.controller.api.model.spec.ContainerSpec container = createdServerTemplate.getContainersSpec().iterator().next();
        assertNotNull(container);

        assertEquals(containerSpec.getId(), container.getId());
        assertEquals(containerSpec.getStatus(), container.getStatus());
        assertEquals(containerSpec.getServerTemplateKey(), container.getServerTemplateKey());
        assertEquals(containerSpec.getReleasedId(), container.getReleasedId());

        assertNotNull(container.getConfigs());
        assertEquals(containerSpec.getConfigs().size(), container.getConfigs().size());

        ContainerSpecList specs = specManagementService.listContainerSpec(serverTemplate.getId());
        assertNotNull(specs);
        assertEquals(1, specs.getContainerSpecs().length);

        container = specs.getContainerSpecs()[0];
        assertNotNull(container);

        assertEquals(containerSpec.getId(), container.getId());
        assertEquals(containerSpec.getStatus(), container.getStatus());
        assertEquals(containerSpec.getServerTemplateKey(), container.getServerTemplateKey());
        assertEquals(containerSpec.getReleasedId(), container.getReleasedId());

        assertNotNull(container.getConfigs());
        assertEquals(containerSpec.getConfigs().size(), container.getConfigs().size());
    }

    @Test
    public void testListServerTemplates() {

        int limit = getRandomInt(5, 10);
        for (int x = 0; x < limit; x++) {
            ServerTemplate serverTemplate = new ServerTemplate();

            serverTemplate.setName("test server " + x);
            serverTemplate.setId(UUID.randomUUID().toString());

            specManagementService.saveServerTemplate(serverTemplate);
        }
        ServerTemplateKeyList existing = specManagementService.listServerTemplateKeys();
        assertNotNull(existing);
        assertEquals(limit, existing.getServerTemplates().length);

        ServerTemplateList allTemplates = specManagementService.listServerTemplates();
        assertNotNull(allTemplates);
        assertEquals(limit, allTemplates.getServerTemplates().length);
    }

    @Test
    public void testCreateAndDeleteServerTemplate() {

        ServerTemplate serverTemplate = new ServerTemplate();

        serverTemplate.setName("test server");
        serverTemplate.setId(UUID.randomUUID().toString());

        specManagementService.saveServerTemplate(serverTemplate);

        ServerTemplateKeyList existing = specManagementService.listServerTemplateKeys();
        assertNotNull(existing);
        assertEquals(1, existing.getServerTemplates().length);

        org.kie.server.controller.api.model.spec.ServerTemplateKey saved = existing.getServerTemplates()[0];

        assertEquals(serverTemplate.getName(), saved.getName());
        assertEquals(serverTemplate.getId(), saved.getId());

        specManagementService.deleteServerTemplate(serverTemplate.getId());
        existing = specManagementService.listServerTemplateKeys();
        assertNotNull(existing);
        assertEquals(0, existing.getServerTemplates().length);
    }

    @Test
    public void testCreateServerTemplateAndAddRemoveContainer() {

        ServerTemplate serverTemplate = new ServerTemplate();

        serverTemplate.setName("test server");
        serverTemplate.setId(UUID.randomUUID().toString());

        specManagementService.saveServerTemplate(serverTemplate);

        ServerTemplateKeyList existing = specManagementService.listServerTemplateKeys();
        assertNotNull(existing);
        assertEquals(1, existing.getServerTemplates().length);

        int limit = getRandomInt(3, 6);
        for (int x = 0; x < limit; x++) {

            Map<Capability, ContainerConfig> configs = new HashMap<Capability, ContainerConfig>();
            RuleConfig ruleConfig = new RuleConfig();
            ruleConfig.setPollInterval(1000l);
            ruleConfig.setScannerStatus(KieScannerStatus.STARTED);

            ProcessConfig processConfig = new ProcessConfig();
            processConfig.setKBase("defaultKieBase");
            processConfig.setKSession("defaultKieSession");
            processConfig.setMergeMode("MERGE_COLLECTION");
            processConfig.setRuntimeStrategy("PER_PROCESS_INSTANCE");

            ContainerSpec containerSpec = new ContainerSpec();
            containerSpec.setId("test container " + x);
            containerSpec.setServerTemplateKey(new ServerTemplateKey(serverTemplate.getId(), serverTemplate.getName()));
            containerSpec.setReleasedId(new ReleaseId("org.kie", "kie-server-kjar", x + ".0"));
            containerSpec.setStatus(KieContainerStatus.STOPPED);
            containerSpec.setConfigs(configs);

            specManagementService.saveContainerSpec(serverTemplate.getId(), containerSpec);
        }

        org.kie.server.controller.api.model.spec.ServerTemplate createdServerTemplate = specManagementService.getServerTemplate(serverTemplate.getId());
        assertNotNull(createdServerTemplate);
        assertNotNull(createdServerTemplate.getContainersSpec());
        assertEquals(limit, createdServerTemplate.getContainersSpec().size());

        // remove first container with suffix 0
        specManagementService.deleteContainerSpec(serverTemplate.getId(), "test container " + 0);

        createdServerTemplate = specManagementService.getServerTemplate(serverTemplate.getId());
        assertNotNull(createdServerTemplate);
        assertNotNull(createdServerTemplate.getContainersSpec());
        assertEquals(limit - 1, createdServerTemplate.getContainersSpec().size());
    }

    @Test
    public void testCreateServerTemplateAndCreateThenCopyContainer() {

        ServerTemplate serverTemplate = new ServerTemplate();

        serverTemplate.setName("test server");
        serverTemplate.setId(UUID.randomUUID().toString());

        specManagementService.saveServerTemplate(serverTemplate);

        ServerTemplateKeyList existing = specManagementService.listServerTemplateKeys();
        assertNotNull(existing);
        assertEquals(1, existing.getServerTemplates().length);

        Map<Capability, ContainerConfig> configs = new HashMap<Capability, ContainerConfig>();
        RuleConfig ruleConfig = new RuleConfig();
        ruleConfig.setPollInterval(1000l);
        ruleConfig.setScannerStatus(KieScannerStatus.STARTED);

        ProcessConfig processConfig = new ProcessConfig();
        processConfig.setKBase("defaultKieBase");
        processConfig.setKSession("defaultKieSession");
        processConfig.setMergeMode("MERGE_COLLECTION");
        processConfig.setRuntimeStrategy("PER_PROCESS_INSTANCE");

        ContainerSpec containerSpec = new ContainerSpec();
        containerSpec.setId("test container");
        containerSpec.setServerTemplateKey(new ServerTemplateKey(serverTemplate.getId(), serverTemplate.getName()));
        containerSpec.setReleasedId(new ReleaseId("org.kie", "kie-server-kjar", "1.0"));
        containerSpec.setStatus(KieContainerStatus.STOPPED);
        containerSpec.setConfigs(configs);

        specManagementService.saveContainerSpec(serverTemplate.getId(), containerSpec);

        org.kie.server.controller.api.model.spec.ServerTemplate createdServerTemplate = specManagementService.getServerTemplate(serverTemplate.getId());
        assertNotNull(createdServerTemplate);
        assertNotNull(createdServerTemplate.getContainersSpec());
        assertEquals(1, createdServerTemplate.getContainersSpec().size());

        org.kie.server.controller.api.model.spec.ContainerSpec container = createdServerTemplate.getContainersSpec().iterator().next();
        assertNotNull(container);

        assertEquals(containerSpec.getId(), container.getId());
        assertEquals(containerSpec.getStatus(), container.getStatus());
        assertEquals(containerSpec.getServerTemplateKey(), container.getServerTemplateKey());
        assertEquals(containerSpec.getReleasedId(), container.getReleasedId());

        assertNotNull(container.getConfigs());
        assertEquals(containerSpec.getConfigs().size(), container.getConfigs().size());

        String newServerTemplateId = "Copied server id";
        String newServerTemplateName = "Copied server name";

        specManagementService.copyServerTemplate(serverTemplate.getId(), newServerTemplateId, newServerTemplateName);

        existing = specManagementService.listServerTemplateKeys();
        assertNotNull(existing);
        assertEquals(2, existing.getServerTemplates().length);

        createdServerTemplate = specManagementService.getServerTemplate(newServerTemplateId);
        assertNotNull(createdServerTemplate);
        assertEquals(newServerTemplateName, createdServerTemplate.getName());
        assertEquals(newServerTemplateId, createdServerTemplate.getId());
        assertNotNull(createdServerTemplate.getContainersSpec());
        assertEquals(1, createdServerTemplate.getContainersSpec().size());

        container = createdServerTemplate.getContainersSpec().iterator().next();
        assertNotNull(container);

        assertEquals(containerSpec.getId(), container.getId());
        assertEquals(containerSpec.getStatus(), container.getStatus());
        assertEquals(newServerTemplateId, container.getServerTemplateKey().getId());
        assertEquals(newServerTemplateName, container.getServerTemplateKey().getName());
        assertEquals(containerSpec.getReleasedId(), container.getReleasedId());

        assertNotNull(container.getConfigs());
        assertEquals(containerSpec.getConfigs().size(), container.getConfigs().size());
    }

      
    @Test
    public void testCreateServerTemplateAndUpdateContainerConfig() {

        ServerTemplate serverTemplate = new ServerTemplate();

        serverTemplate.setName("test server");
        serverTemplate.setId(UUID.randomUUID().toString());

        specManagementService.saveServerTemplate(serverTemplate);

        ServerTemplateKeyList existing = specManagementService.listServerTemplateKeys();
        assertNotNull(existing);
        assertEquals(1, existing.getServerTemplates().length);

        Map<Capability, ContainerConfig> configs = new HashMap<Capability, ContainerConfig>();
        RuleConfig ruleConfig = new RuleConfig();
        ruleConfig.setPollInterval(1000l);
        ruleConfig.setScannerStatus(KieScannerStatus.STARTED);

        configs.put(Capability.RULE, ruleConfig);

        ProcessConfig processConfig = new ProcessConfig();
        processConfig.setKBase("defaultKieBase");
        processConfig.setKSession("defaultKieSession");
        processConfig.setMergeMode("MERGE_COLLECTION");
        processConfig.setRuntimeStrategy("PER_PROCESS_INSTANCE");

        configs.put(Capability.PROCESS, processConfig);

        ContainerSpec containerSpec = new ContainerSpec();
        containerSpec.setId("test container");
        containerSpec.setServerTemplateKey(new ServerTemplateKey(serverTemplate.getId(), serverTemplate.getName()));
        containerSpec.setReleasedId(new ReleaseId("org.kie", "kie-server-kjar", "1.0"));
        containerSpec.setStatus(KieContainerStatus.STOPPED);
        containerSpec.setConfigs(configs);

        specManagementService.saveContainerSpec(serverTemplate.getId(), containerSpec);

        org.kie.server.controller.api.model.spec.ServerTemplate createdServerTemplate = specManagementService.getServerTemplate(serverTemplate.getId());
        assertNotNull(createdServerTemplate);
        assertNotNull(createdServerTemplate.getContainersSpec());
        assertEquals(1, createdServerTemplate.getContainersSpec().size());

        org.kie.server.controller.api.model.spec.ContainerSpec container = createdServerTemplate.getContainersSpec().iterator().next();
        assertNotNull(container);

        assertEquals(containerSpec.getId(), container.getId());
        assertEquals(containerSpec.getStatus(), container.getStatus());
        assertEquals(containerSpec.getServerTemplateKey(), container.getServerTemplateKey());
        assertEquals(containerSpec.getReleasedId(), container.getReleasedId());

        assertNotNull(container.getConfigs());
        assertEquals(containerSpec.getConfigs().size(), container.getConfigs().size());

        ContainerConfig ruleConfigCurrent = containerSpec.getConfigs().get(Capability.RULE);
        assertNotNull(ruleConfigCurrent);
        assertTrue(ruleConfigCurrent instanceof org.kie.server.controller.api.model.spec.RuleConfig);
        assertEquals(ruleConfig.getPollInterval(), ((org.kie.server.controller.api.model.spec.RuleConfig) ruleConfigCurrent).getPollInterval());
        assertEquals(ruleConfig.getScannerStatus(), ((org.kie.server.controller.api.model.spec.RuleConfig) ruleConfigCurrent).getScannerStatus());

        ContainerConfig containerConfig = new RuleConfig();
        ((RuleConfig) containerConfig).setScannerStatus(KieScannerStatus.SCANNING);
        ((RuleConfig) containerConfig).setPollInterval(10l);

        specManagementService.updateContainerConfig(serverTemplate.getId(), containerSpec.getId(), Capability.RULE, containerConfig);

        ContainerSpecList specs = specManagementService.listContainerSpec(serverTemplate.getId());
        assertNotNull(specs);
        assertEquals(1, specs.getContainerSpecs().length);

        container = specs.getContainerSpecs()[0];
        assertNotNull(container);

        assertEquals(containerSpec.getId(), container.getId());
        assertEquals(containerSpec.getStatus(), container.getStatus());
        assertEquals(containerSpec.getServerTemplateKey(), container.getServerTemplateKey());
        assertEquals(containerSpec.getReleasedId(), container.getReleasedId());

        assertNotNull(container.getConfigs());
        assertEquals(containerSpec.getConfigs().size(), container.getConfigs().size());

        ContainerConfig ruleConfigCurrent2 = container.getConfigs().get(Capability.RULE);
        assertNotNull(ruleConfigCurrent2);
        assertTrue(ruleConfigCurrent2 instanceof org.kie.server.controller.api.model.spec.RuleConfig);
        assertEquals(((org.kie.server.controller.api.model.spec.RuleConfig) containerConfig).getPollInterval(), ((org.kie.server.controller.api.model.spec.RuleConfig) ruleConfigCurrent2).getPollInterval());
        assertEquals(((org.kie.server.controller.api.model.spec.RuleConfig) containerConfig).getScannerStatus(), ((org.kie.server.controller.api.model.spec.RuleConfig) ruleConfigCurrent2).getScannerStatus());
    }

    @Test
    public void testStartContainer() {
        createServerTemplateWithContainer();
        List<Container> fakeResult = new ArrayList<Container>();
        fakeResult.add(container);
        when(kieServerInstanceManager.startContainer(any(ServerTemplate.class), any(ContainerSpec.class), any())).thenReturn(fakeResult);

        specManagementService.startContainer(containerSpec);

        verify(kieServerInstanceManager, times(1)).startContainer(any(ServerTemplate.class), any(ContainerSpec.class), any());

        ServerTemplate updated = specManagementService.getServerTemplate(serverTemplate.getId());
        assertNotNull(updated);

        ContainerSpec updatedContainer = updated.getContainerSpec(containerSpec.getId());
        assertNotNull(updatedContainer);

        assertEquals(KieContainerStatus.STARTED, updatedContainer.getStatus());
    }

    @Test
    public void testStopContainer() {
        createServerTemplateWithContainer();
        List<Container> fakeResult = new ArrayList<Container>();
        fakeResult.add(container);

        specManagementService.stopContainer(containerSpec);

        verify(kieServerInstanceManager, times(1)).stopContainer(any(ServerTemplate.class), any(ContainerSpec.class), any());

        ServerTemplate updated = specManagementService.getServerTemplate(serverTemplate.getId());
        assertNotNull(updated);

        ContainerSpec updatedContainer = updated.getContainerSpec(containerSpec.getId());
        assertNotNull(updatedContainer);

        assertEquals(KieContainerStatus.STOPPED, updatedContainer.getStatus());
    }

    @Test
    public void testUpdateContainerConfigWhenContainerConfigIsARuleConfig() {

        final SpecManagementServiceImpl specManagementService = spy((SpecManagementServiceImpl) this.specManagementService);
        final Capability capability = Capability.RULE;
        final RuleConfig ruleConfig = mock(RuleConfig.class);
        final ServerTemplate serverTemplate = mock(ServerTemplate.class);
        final ContainerSpec containerSpec = mock(ContainerSpec.class);

        final List<?> expectedContainers = mock(List.class);

        doReturn(expectedContainers).when(specManagementService).updateContainerRuleConfig(ruleConfig,
                                                                                           serverTemplate,
                                                                                           containerSpec);

        final List<Container> actualContainers = specManagementService.updateContainerConfig(capability,
                                                                                             ruleConfig,
                                                                                             serverTemplate,
                                                                                             containerSpec);
        assertEquals(expectedContainers, actualContainers);
    }

    @Test
    public void testUpdateContainerConfigWhenContainerConfigIsAProcessConfig() {

        final SpecManagementServiceImpl specManagementService = spy((SpecManagementServiceImpl) this.specManagementService);
        final Capability capability = Capability.PROCESS;
        final ProcessConfig processConfig = mock(ProcessConfig.class);
        final ServerTemplate serverTemplate = mock(ServerTemplate.class);
        final ContainerSpec containerSpec = mock(ContainerSpec.class);

        final List<?> expectedContainers = mock(List.class);

        doReturn(expectedContainers).when(specManagementService).updateContainerProcessConfig(processConfig,
                                                                                              capability,
                                                                                              serverTemplate,
                                                                                              containerSpec);

        final List<Container> actualContainers = specManagementService.updateContainerConfig(capability,
                                                                                             processConfig,
                                                                                             serverTemplate,
                                                                                             containerSpec);
        assertEquals(expectedContainers, actualContainers);
    }

    @Test
    public void testUpdateContainerConfigWhenServerTemplateIsNull() {

        final SpecManagementServiceImpl specManagementService = (SpecManagementServiceImpl) this.specManagementService;
        final String serverTemplateId = "serverTemplateId";
        final String containerSpecId = "containerSpecId";
        final Capability capability = Capability.PROCESS;
        final ContainerConfig containerConfig = mock(ContainerConfig.class);

        specManagementService.setTemplateStorage(templateStorage);

        doReturn(null).when(templateStorage).load(serverTemplateId);

        expectedException.expect(KieServerControllerIllegalArgumentException.class);
        expectedException.expectMessage("No server template found for id serverTemplateId");

        specManagementService.updateContainerConfig(serverTemplateId, containerSpecId, capability, containerConfig);
    }

    @Test
    public void testUpdateContainerConfigWhenContainerSpecIsNull() {

        final SpecManagementServiceImpl specManagementService = (SpecManagementServiceImpl) this.specManagementService;
        final String serverTemplateId = "serverTemplateId";
        final String containerSpecId = "containerSpecId";
        final Capability capability = Capability.PROCESS;
        final ContainerConfig containerConfig = mock(ContainerConfig.class);
        final ServerTemplate serverTemplate = mock(ServerTemplate.class);

        specManagementService.setTemplateStorage(templateStorage);

        doReturn(serverTemplate).when(templateStorage).load(serverTemplateId);

        expectedException.expect(KieServerControllerIllegalArgumentException.class);
        expectedException.expectMessage("No container spec found for id containerSpecId within server template with id serverTemplateId");

        specManagementService.updateContainerConfig(serverTemplateId, containerSpecId, capability, containerConfig);
    }

    @Test
    public void testUpdateContainerConfigWhenAffectedContainersIsEmpty() {

        final SpecManagementServiceImpl specManagementService = spy((SpecManagementServiceImpl) this.specManagementService);
        final String serverTemplateId = "serverTemplateId";
        final String containerSpecId = "containerSpecId";
        final Capability capability = Capability.PROCESS;
        final ContainerConfig containerConfig = mock(ContainerConfig.class);
        final ServerTemplate serverTemplate = mock(ServerTemplate.class);
        final ContainerSpec containerSpec = mock(ContainerSpec.class);
        final Map<Capability, ContainerConfig> configs = spy(new HashMap<>());
        final List<?> expectedContainers = new ArrayList<>();

        specManagementService.setTemplateStorage(templateStorage);
        specManagementService.setNotificationService(notificationService);

        doReturn(serverTemplate).when(templateStorage).load(serverTemplateId);
        doReturn(containerSpec).when(serverTemplate).getContainerSpec(containerSpecId);
        doReturn(expectedContainers).when(specManagementService).updateContainerConfig(capability, containerConfig, serverTemplate, containerSpec);
        doReturn(configs).when(containerSpec).getConfigs();

        specManagementService.updateContainerConfig(serverTemplateId, containerSpecId, capability, containerConfig);

        verify(specManagementService).logInfo("Update of container configuration resulted in no changes to containers running on kie-servers");
        verify(specManagementService, never()).logDebug(any(), any());
        verify(configs).put(capability, containerConfig);
        verify(templateStorage).update(serverTemplate);
        verify(notificationService).notify(any(ServerTemplateUpdated.class));
    }

    @Test
    public void testUpdateContainerConfigWhenAffectedContainersIsNotEmpty() {

        final SpecManagementServiceImpl specManagementService = spy((SpecManagementServiceImpl) this.specManagementService);
        final String serverTemplateId = "serverTemplateId";
        final String containerSpecId = "containerSpecId";
        final Capability capability = Capability.PROCESS;
        final ContainerConfig containerConfig = mock(ContainerConfig.class);
        final ServerTemplate serverTemplate = mock(ServerTemplate.class);
        final ContainerSpec containerSpec = mock(ContainerSpec.class);
        final Map<Capability, ContainerConfig> configs = spy(new HashMap<>());
        final Container container1 = makeContainer("1");
        final Container container2 = makeContainer("2");
        final List<Container> expectedContainers = new ArrayList<Container>() {{
            add(container1);
            add(container2);
        }};

        specManagementService.setTemplateStorage(templateStorage);
        specManagementService.setNotificationService(notificationService);

        doReturn(serverTemplate).when(templateStorage).load(serverTemplateId);
        doReturn(containerSpec).when(serverTemplate).getContainerSpec(containerSpecId);
        doReturn(expectedContainers).when(specManagementService).updateContainerConfig(capability, containerConfig, serverTemplate, containerSpec);
        doReturn(configs).when(containerSpec).getConfigs();

        specManagementService.updateContainerConfig(serverTemplateId, containerSpecId, capability, containerConfig);

        verify(specManagementService).logDebug("Container {} on server {} was affected by a change in the scanner",
                                               container1.getContainerSpecId(),
                                               container1.getServerInstanceKey());
        verify(specManagementService).logDebug("Container {} on server {} was affected by a change in the scanner",
                                               container2.getContainerSpecId(),
                                               container2.getServerInstanceKey());
        verify(specManagementService, never()).logInfo(any());
        verify(configs).put(capability, containerConfig);
        verify(templateStorage).update(serverTemplate);
        verify(notificationService).notify(any(ServerTemplateUpdated.class));
    }

    private Container makeContainer(final String seed) {

        final Container container = mock(Container.class);

        doReturn(seed).when(container).getContainerSpecId();
        doReturn(mock(ServerInstanceKey.class)).when(container).getServerInstanceKey();

        return container;
    }

    @Test
    public void testUpdateContainerConfigWhenContainerConfigIsNotAProcessConfigNeitherARuleConfig() {

        final SpecManagementServiceImpl specManagementService = spy((SpecManagementServiceImpl) this.specManagementService);
        final Capability capability = Capability.PROCESS;
        final ContainerConfig containerConfig = mock(ContainerConfig.class);
        final ServerTemplate serverTemplate = mock(ServerTemplate.class);
        final ContainerSpec containerSpec = mock(ContainerSpec.class);
        final List<?> expectedContainers = new ArrayList<>();

        final List<Container> actualContainers = specManagementService.updateContainerConfig(capability,
                                                                                             containerConfig,
                                                                                             serverTemplate,
                                                                                             containerSpec);
        assertEquals(expectedContainers, actualContainers);
    }

    @Test
    public void testUpdateContainerProcessConfig() {

        final SpecManagementServiceImpl specManagementService = (SpecManagementServiceImpl) this.specManagementService;
        final ProcessConfig processConfig = mock(ProcessConfig.class);
        final Capability capability = Capability.PROCESS;
        final ServerTemplate serverTemplate = mock(ServerTemplate.class);
        final ContainerSpec containerSpec = mock(ContainerSpec.class);
        final Map<Capability, ProcessConfig> configs = spy(new HashMap<>());
        final List<?> expectedContainers = mock(List.class);

        doReturn(configs).when(containerSpec).getConfigs();
        doReturn(expectedContainers).when(kieServerInstanceManager).upgradeContainer(serverTemplate, containerSpec);

        final List<Container> actualContainers = specManagementService.updateContainerProcessConfig(processConfig,
                                                                                                    capability,
                                                                                                    serverTemplate,
                                                                                                    containerSpec);

        assertEquals(expectedContainers, actualContainers);
    }

    @Test
    public void testUpdateAndStartContainerSpecDefault() {
        testUpdateAndStartContainerSpec(false, true);
    }

    @Test
    public void testUpdateContainerSpecDefault() {
        testUpdateAndStartContainerSpec(false, false);
    }

    @Test
    public void testUpdateAndStartContainerSpecResetBeforeUpdate() {
        testUpdateAndStartContainerSpec(true, true);
    }

    @Test
    public void testUpdateContainerSpecResetBeforeUpdate() {
        testUpdateAndStartContainerSpec(true, false);
    }

    private void testUpdateAndStartContainerSpec(boolean resetBeforeUpdate, boolean started) {
        final String serverTemplateId = "serverTemplateId";
        final String serverTemplateName = "serverTemplateName";
        final String containerSpecId = "containerSpecId";

        ServerTemplate template = new ServerTemplate(serverTemplateId, serverTemplateName);

        ContainerSpec containerSpec = new ContainerSpec();
        containerSpec.setId(containerSpecId);
        containerSpec.setServerTemplateKey(new ServerTemplateKey(template.getId(), template.getName()));
        containerSpec.setReleasedId(new ReleaseId("org.kie", "kie-server-kjar", "1.0"));
        containerSpec.setStatus(started ? KieContainerStatus.STARTED : KieContainerStatus.STOPPED);

        template.addContainerSpec(containerSpec);

        when(templateStorage.load(eq(serverTemplateId))).thenReturn(template);

        final SpecManagementServiceImpl specManagementService = (SpecManagementServiceImpl) this.specManagementService;

        specManagementService.setTemplateStorage(templateStorage);
        specManagementService.setNotificationService(notificationService);

        specManagementService.updateContainerSpec(serverTemplateId, containerSpecId, containerSpec, resetBeforeUpdate);

        verify(templateStorage).update(eq(template));

        ArgumentCaptor<ServerTemplateUpdated> captor = ArgumentCaptor.forClass(ServerTemplateUpdated.class);

        verify(notificationService).notify(captor.capture());

        ServerTemplateUpdated serverTemplateUpdated = captor.getValue();

        assertNotNull(serverTemplateUpdated);
        assertEquals(template, serverTemplateUpdated.getServerTemplate());

        if(!resetBeforeUpdate) {
            assertFalse(serverTemplateUpdated.isResetBeforeUpdate());
        } else {
            assertTrue(serverTemplateUpdated.isResetBeforeUpdate());
        }

        verify(kieServerInstanceManager, started ? times(1) : never()).upgradeAndStartContainer(eq(template), eq(containerSpec), eq(resetBeforeUpdate), any());

    }


	@Test
	public void notabletoUpdateContainerWithEmptyid() {
		final String serverTemplateId = "serverTemplateId";
		final String serverTemplateName = "serverTemplateName";
		final String containerSpecId = "containerSpecId";

		ServerTemplate template = new ServerTemplate(serverTemplateId, serverTemplateName);

		ContainerSpec containerSpec = new ContainerSpec();
		containerSpec.setId("");
		containerSpec.setServerTemplateKey(new ServerTemplateKey(template.getId(), template.getName()));
		containerSpec.setReleasedId(new ReleaseId("org.kie", "kie-server-kjar", "1.0"));
		containerSpec.setStatus(KieContainerStatus.STARTED);

		template.addContainerSpec(containerSpec);

		when(templateStorage.load(eq(serverTemplateId))).thenReturn(template);

		final SpecManagementServiceImpl specManagementService = (SpecManagementServiceImpl) this.specManagementService;

		specManagementService.setTemplateStorage(templateStorage);
		specManagementService.setNotificationService(notificationService);

		try {
			specManagementService.updateContainerSpec(serverTemplateId, containerSpecId, containerSpec, true);
			fail("Cannot update container with empty container id");
		} catch (Exception ex) {
			assertEquals("Cannot update container with empty container id.", ex.getMessage());
		}

	}   

    @Test
    public void testUpdateContainerRuleConfigWhenKieScannerStatusIsStarted() {

        final SpecManagementServiceImpl specManagementService = (SpecManagementServiceImpl) this.specManagementService;
        final RuleConfig ruleConfig = mock(RuleConfig.class);
        final ServerTemplate serverTemplate = mock(ServerTemplate.class);
        final ContainerSpec containerSpec = mock(ContainerSpec.class);
        final Long interval = 1L;
        final List<?> expectedContainers = mock(List.class);

        doReturn(interval).when(ruleConfig).getPollInterval();
        doReturn(KieScannerStatus.STARTED).when(ruleConfig).getScannerStatus();
        doReturn(expectedContainers).when(kieServerInstanceManager).startScanner(serverTemplate, containerSpec, interval);

        final List<Container> actualContainers = specManagementService.updateContainerRuleConfig(ruleConfig,
                                                                                                 serverTemplate,
                                                                                                 containerSpec);

        assertEquals(expectedContainers, actualContainers);
    }

    @Test
    public void testUpdateContainerRuleConfigWhenKieScannerStatusIsStopped() {

        final SpecManagementServiceImpl specManagementService = (SpecManagementServiceImpl) this.specManagementService;
        final RuleConfig ruleConfig = mock(RuleConfig.class);
        final ServerTemplate serverTemplate = mock(ServerTemplate.class);
        final ContainerSpec containerSpec = mock(ContainerSpec.class);
        final List<?> expectedContainers = mock(List.class);

        doReturn(KieScannerStatus.STOPPED).when(ruleConfig).getScannerStatus();
        doReturn(expectedContainers).when(kieServerInstanceManager).stopScanner(serverTemplate, containerSpec);

        final List<Container> actualContainers = specManagementService.updateContainerRuleConfig(ruleConfig, serverTemplate, containerSpec);

        assertEquals(expectedContainers, actualContainers);
    }

    @Test
    public void testUpdateContainerRuleConfigWhenKieScannerStatusIsNotStartedNeitherStopped() {

        final SpecManagementServiceImpl specManagementService = (SpecManagementServiceImpl) this.specManagementService;
        final RuleConfig ruleConfig = mock(RuleConfig.class);
        final ServerTemplate serverTemplate = mock(ServerTemplate.class);
        final ContainerSpec containerSpec = mock(ContainerSpec.class);
        final List<?> expectedContainers = new ArrayList<>();

        doReturn(KieScannerStatus.UNKNOWN).when(ruleConfig).getScannerStatus();

        final List<Container> actualContainers = specManagementService.updateContainerRuleConfig(ruleConfig, serverTemplate, containerSpec);

        assertEquals(expectedContainers, actualContainers);
    }

    protected int getRandomInt(int min, int max) {
        return (int) Math.floor(Math.random() * (max - min + 1)) + min;
    }

    @Test
    public void testDeleteServerInstance() {
        final ServerTemplate serverTemplate = new ServerTemplate("serverTemplateId",
                                                                 "serverTemplateName");
        final ServerInstanceKey serverInstanceKey = new ServerInstanceKey(serverTemplate.getId(),
                                                                          "serverName",
                                                                          "serverInstanceId",
                                                                          "url");

        serverTemplate.addServerInstance(serverInstanceKey);
        specManagementService.saveServerTemplate(serverTemplate);
        when(kieServerInstanceManager.isAlive(serverInstanceKey)).thenReturn(false);

        specManagementService.deleteServerInstance(serverInstanceKey);

        final ServerTemplate updatedServerTemplate = specManagementService.getServerTemplate(serverTemplate.getId());

        assertEquals(0,
                     updatedServerTemplate.getServerInstanceKeys().size());
    }

    @Test
    public void testDeleteServerInstanceAlive() {
        final ServerTemplate serverTemplate = new ServerTemplate("serverTemplateId",
                                                                 "serverTemplateName");
        final ServerInstanceKey serverInstanceKey = new ServerInstanceKey(serverTemplate.getId(),
                                                                          "serverName",
                                                                          "serverInstanceId",
                                                                          "url");

        serverTemplate.addServerInstance(serverInstanceKey);
        specManagementService.saveServerTemplate(serverTemplate);
        when(kieServerInstanceManager.isAlive(serverInstanceKey)).thenReturn(true);

        try {
            specManagementService.deleteServerInstance(serverInstanceKey);
            fail("Deleting a live server instance should fail");
        } catch (Exception ex) {
            assertEquals("Can't delete live instance.",
                         ex.getMessage());
        }

        final ServerTemplate updatedServerTemplate = specManagementService.getServerTemplate(serverTemplate.getId());

        assertEquals(1,
                     updatedServerTemplate.getServerInstanceKeys().size());
    }
}
