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
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.kie.server.api.model.KieContainerStatus;
import org.kie.server.api.model.KieScannerStatus;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.controller.api.model.runtime.Container;
import org.kie.server.controller.api.model.spec.Capability;
import org.kie.server.controller.api.model.spec.ContainerConfig;
import org.kie.server.controller.api.model.spec.ContainerSpec;
import org.kie.server.controller.api.model.spec.ProcessConfig;
import org.kie.server.controller.api.model.spec.RuleConfig;
import org.kie.server.controller.api.model.spec.ServerTemplate;
import org.kie.server.controller.api.model.spec.ServerTemplateKey;
import org.kie.server.controller.impl.KieServerInstanceManager;
import org.kie.server.controller.impl.storage.InMemoryKieServerTemplateStorage;
import org.mockito.Mockito;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class SpecManagementServiceImplTest extends AbstractServiceImplTest {

    @Before
    public void setup() {
        specManagementService = new SpecManagementServiceImpl();
        kieServerInstanceManager = Mockito.mock(KieServerInstanceManager.class);

        ((SpecManagementServiceImpl) specManagementService).setKieServerInstanceManager(kieServerInstanceManager);
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

        Collection<org.kie.server.controller.api.model.spec.ServerTemplateKey> existing = specManagementService.listServerTemplateKeys();
        assertNotNull(existing);
        assertEquals(1,
                     existing.size());

        org.kie.server.controller.api.model.spec.ServerTemplateKey saved = existing.iterator().next();

        assertEquals(serverTemplate.getName(),
                     saved.getName());
        assertEquals(serverTemplate.getId(),
                     saved.getId());
    }

    @Test
    public void testCreateServerTemplateAndContainer() {

        ServerTemplate serverTemplate = new ServerTemplate();

        serverTemplate.setName("test server");
        serverTemplate.setId(UUID.randomUUID().toString());

        specManagementService.saveServerTemplate(serverTemplate);

        Collection<org.kie.server.controller.api.model.spec.ServerTemplateKey> existing = specManagementService.listServerTemplateKeys();
        assertNotNull(existing);
        assertEquals(1,
                     existing.size());

        Map<Capability, ContainerConfig> configs = new HashMap<Capability, ContainerConfig>();
        RuleConfig ruleConfig = new RuleConfig();
        ruleConfig.setPollInterval(1000l);
        ruleConfig.setScannerStatus(KieScannerStatus.STARTED);

        configs.put(Capability.RULE,
                    ruleConfig);

        ProcessConfig processConfig = new ProcessConfig();
        processConfig.setKBase("defaultKieBase");
        processConfig.setKSession("defaultKieSession");
        processConfig.setMergeMode("MERGE_COLLECTION");
        processConfig.setRuntimeStrategy("PER_PROCESS_INSTANCE");

        configs.put(Capability.PROCESS,
                    processConfig);

        ContainerSpec containerSpec = new ContainerSpec();
        containerSpec.setId("test container");
        containerSpec.setServerTemplateKey(new ServerTemplateKey(serverTemplate.getId(),
                                                                 serverTemplate.getName()));
        containerSpec.setReleasedId(new ReleaseId("org.kie",
                                                  "kie-server-kjar",
                                                  "1.0"));
        containerSpec.setStatus(KieContainerStatus.STOPPED);
        containerSpec.setConfigs(configs);

        specManagementService.saveContainerSpec(serverTemplate.getId(),
                                                containerSpec);

        org.kie.server.controller.api.model.spec.ServerTemplate createdServerTemplate = specManagementService.getServerTemplate(serverTemplate.getId());
        assertNotNull(createdServerTemplate);
        assertNotNull(createdServerTemplate.getContainersSpec());
        assertEquals(1,
                     createdServerTemplate.getContainersSpec().size());

        org.kie.server.controller.api.model.spec.ContainerSpec container = createdServerTemplate.getContainersSpec().iterator().next();
        assertNotNull(container);

        assertEquals(containerSpec.getId(),
                     container.getId());
        assertEquals(containerSpec.getStatus(),
                     container.getStatus());
        assertEquals(containerSpec.getServerTemplateKey(),
                     container.getServerTemplateKey());
        assertEquals(containerSpec.getReleasedId(),
                     container.getReleasedId());

        assertNotNull(container.getConfigs());
        assertEquals(containerSpec.getConfigs().size(),
                     container.getConfigs().size());

        Collection<org.kie.server.controller.api.model.spec.ContainerSpec> specs = specManagementService.listContainerSpec(serverTemplate.getId());
        assertNotNull(specs);
        assertEquals(1,
                     specs.size());

        container = specs.iterator().next();
        assertNotNull(container);

        assertEquals(containerSpec.getId(),
                     container.getId());
        assertEquals(containerSpec.getStatus(),
                     container.getStatus());
        assertEquals(containerSpec.getServerTemplateKey(),
                     container.getServerTemplateKey());
        assertEquals(containerSpec.getReleasedId(),
                     container.getReleasedId());

        assertNotNull(container.getConfigs());
        assertEquals(containerSpec.getConfigs().size(),
                     container.getConfigs().size());
    }

    @Test
    public void testListServerTemplates() {

        int limit = getRandomInt(5,
                                 10);
        for (int x = 0; x < limit; x++) {
            ServerTemplate serverTemplate = new ServerTemplate();

            serverTemplate.setName("test server " + x);
            serverTemplate.setId(UUID.randomUUID().toString());

            specManagementService.saveServerTemplate(serverTemplate);
        }
        Collection<org.kie.server.controller.api.model.spec.ServerTemplateKey> existing = specManagementService.listServerTemplateKeys();
        assertNotNull(existing);
        assertEquals(limit,
                     existing.size());

        Collection<org.kie.server.controller.api.model.spec.ServerTemplate> allTemplates = specManagementService.listServerTemplates();
        assertNotNull(allTemplates);
        assertEquals(limit,
                     allTemplates.size());
    }

    @Test
    public void testCreateAndDeleteServerTemplate() {

        ServerTemplate serverTemplate = new ServerTemplate();

        serverTemplate.setName("test server");
        serverTemplate.setId(UUID.randomUUID().toString());

        specManagementService.saveServerTemplate(serverTemplate);

        Collection<org.kie.server.controller.api.model.spec.ServerTemplateKey> existing = specManagementService.listServerTemplateKeys();
        assertNotNull(existing);
        assertEquals(1,
                     existing.size());

        org.kie.server.controller.api.model.spec.ServerTemplateKey saved = existing.iterator().next();

        assertEquals(serverTemplate.getName(),
                     saved.getName());
        assertEquals(serverTemplate.getId(),
                     saved.getId());

        specManagementService.deleteServerTemplate(serverTemplate.getId());
        existing = specManagementService.listServerTemplateKeys();
        assertNotNull(existing);
        assertEquals(0,
                     existing.size());
    }

    @Test
    public void testCreateServerTemplateAndAddRemoveContainer() {

        ServerTemplate serverTemplate = new ServerTemplate();

        serverTemplate.setName("test server");
        serverTemplate.setId(UUID.randomUUID().toString());

        specManagementService.saveServerTemplate(serverTemplate);

        Collection<org.kie.server.controller.api.model.spec.ServerTemplateKey> existing = specManagementService.listServerTemplateKeys();
        assertNotNull(existing);
        assertEquals(1,
                     existing.size());

        int limit = getRandomInt(3,
                                 6);
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
            containerSpec.setServerTemplateKey(new ServerTemplateKey(serverTemplate.getId(),
                                                                     serverTemplate.getName()));
            containerSpec.setReleasedId(new ReleaseId("org.kie",
                                                      "kie-server-kjar",
                                                      x + ".0"));
            containerSpec.setStatus(KieContainerStatus.STOPPED);
            containerSpec.setConfigs(configs);

            specManagementService.saveContainerSpec(serverTemplate.getId(),
                                                    containerSpec);
        }

        org.kie.server.controller.api.model.spec.ServerTemplate createdServerTemplate = specManagementService.getServerTemplate(serverTemplate.getId());
        assertNotNull(createdServerTemplate);
        assertNotNull(createdServerTemplate.getContainersSpec());
        assertEquals(limit,
                     createdServerTemplate.getContainersSpec().size());

        // remove first container with suffix 0
        specManagementService.deleteContainerSpec(serverTemplate.getId(),
                                                  "test container " + 0);

        createdServerTemplate = specManagementService.getServerTemplate(serverTemplate.getId());
        assertNotNull(createdServerTemplate);
        assertNotNull(createdServerTemplate.getContainersSpec());
        assertEquals(limit - 1,
                     createdServerTemplate.getContainersSpec().size());
    }

    @Test
    public void testCreateServerTemplateAndCreateThenCopyContainer() {

        ServerTemplate serverTemplate = new ServerTemplate();

        serverTemplate.setName("test server");
        serverTemplate.setId(UUID.randomUUID().toString());

        specManagementService.saveServerTemplate(serverTemplate);

        Collection<org.kie.server.controller.api.model.spec.ServerTemplateKey> existing = specManagementService.listServerTemplateKeys();
        assertNotNull(existing);
        assertEquals(1,
                     existing.size());

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
        containerSpec.setServerTemplateKey(new ServerTemplateKey(serverTemplate.getId(),
                                                                 serverTemplate.getName()));
        containerSpec.setReleasedId(new ReleaseId("org.kie",
                                                  "kie-server-kjar",
                                                  "1.0"));
        containerSpec.setStatus(KieContainerStatus.STOPPED);
        containerSpec.setConfigs(configs);

        specManagementService.saveContainerSpec(serverTemplate.getId(),
                                                containerSpec);

        org.kie.server.controller.api.model.spec.ServerTemplate createdServerTemplate = specManagementService.getServerTemplate(serverTemplate.getId());
        assertNotNull(createdServerTemplate);
        assertNotNull(createdServerTemplate.getContainersSpec());
        assertEquals(1,
                     createdServerTemplate.getContainersSpec().size());

        org.kie.server.controller.api.model.spec.ContainerSpec container = createdServerTemplate.getContainersSpec().iterator().next();
        assertNotNull(container);

        assertEquals(containerSpec.getId(),
                     container.getId());
        assertEquals(containerSpec.getStatus(),
                     container.getStatus());
        assertEquals(containerSpec.getServerTemplateKey(),
                     container.getServerTemplateKey());
        assertEquals(containerSpec.getReleasedId(),
                     container.getReleasedId());

        assertNotNull(container.getConfigs());
        assertEquals(containerSpec.getConfigs().size(),
                     container.getConfigs().size());

        String newServerTemplateId = "Copied server id";
        String newServerTemplateName = "Copied server name";

        specManagementService.copyServerTemplate(serverTemplate.getId(),
                                                 newServerTemplateId,
                                                 newServerTemplateName);

        existing = specManagementService.listServerTemplateKeys();
        assertNotNull(existing);
        assertEquals(2,
                     existing.size());

        createdServerTemplate = specManagementService.getServerTemplate(newServerTemplateId);
        assertNotNull(createdServerTemplate);
        assertEquals(newServerTemplateName,
                     createdServerTemplate.getName());
        assertEquals(newServerTemplateId,
                     createdServerTemplate.getId());
        assertNotNull(createdServerTemplate.getContainersSpec());
        assertEquals(1,
                     createdServerTemplate.getContainersSpec().size());

        container = createdServerTemplate.getContainersSpec().iterator().next();
        assertNotNull(container);

        assertEquals(containerSpec.getId(),
                     container.getId());
        assertEquals(containerSpec.getStatus(),
                     container.getStatus());
        assertEquals(newServerTemplateId,
                     container.getServerTemplateKey().getId());
        assertEquals(newServerTemplateName,
                     container.getServerTemplateKey().getName());
        assertEquals(containerSpec.getReleasedId(),
                     container.getReleasedId());

        assertNotNull(container.getConfigs());
        assertEquals(containerSpec.getConfigs().size(),
                     container.getConfigs().size());
    }

    @Test
    public void testCreateServerTemplateAndUpdateContainerConfig() {

        ServerTemplate serverTemplate = new ServerTemplate();

        serverTemplate.setName("test server");
        serverTemplate.setId(UUID.randomUUID().toString());

        specManagementService.saveServerTemplate(serverTemplate);

        Collection<org.kie.server.controller.api.model.spec.ServerTemplateKey> existing = specManagementService.listServerTemplateKeys();
        assertNotNull(existing);
        assertEquals(1,
                     existing.size());

        Map<Capability, ContainerConfig> configs = new HashMap<Capability, ContainerConfig>();
        RuleConfig ruleConfig = new RuleConfig();
        ruleConfig.setPollInterval(1000l);
        ruleConfig.setScannerStatus(KieScannerStatus.STARTED);

        configs.put(Capability.RULE,
                    ruleConfig);

        ProcessConfig processConfig = new ProcessConfig();
        processConfig.setKBase("defaultKieBase");
        processConfig.setKSession("defaultKieSession");
        processConfig.setMergeMode("MERGE_COLLECTION");
        processConfig.setRuntimeStrategy("PER_PROCESS_INSTANCE");

        configs.put(Capability.PROCESS,
                    processConfig);

        ContainerSpec containerSpec = new ContainerSpec();
        containerSpec.setId("test container");
        containerSpec.setServerTemplateKey(new ServerTemplateKey(serverTemplate.getId(),
                                                                 serverTemplate.getName()));
        containerSpec.setReleasedId(new ReleaseId("org.kie",
                                                  "kie-server-kjar",
                                                  "1.0"));
        containerSpec.setStatus(KieContainerStatus.STOPPED);
        containerSpec.setConfigs(configs);

        specManagementService.saveContainerSpec(serverTemplate.getId(),
                                                containerSpec);

        org.kie.server.controller.api.model.spec.ServerTemplate createdServerTemplate = specManagementService.getServerTemplate(serverTemplate.getId());
        assertNotNull(createdServerTemplate);
        assertNotNull(createdServerTemplate.getContainersSpec());
        assertEquals(1,
                     createdServerTemplate.getContainersSpec().size());

        org.kie.server.controller.api.model.spec.ContainerSpec container = createdServerTemplate.getContainersSpec().iterator().next();
        assertNotNull(container);

        assertEquals(containerSpec.getId(),
                     container.getId());
        assertEquals(containerSpec.getStatus(),
                     container.getStatus());
        assertEquals(containerSpec.getServerTemplateKey(),
                     container.getServerTemplateKey());
        assertEquals(containerSpec.getReleasedId(),
                     container.getReleasedId());

        assertNotNull(container.getConfigs());
        assertEquals(containerSpec.getConfigs().size(),
                     container.getConfigs().size());

        ContainerConfig ruleConfigCurrent = containerSpec.getConfigs().get(Capability.RULE);
        assertNotNull(ruleConfigCurrent);
        assertTrue(ruleConfigCurrent instanceof org.kie.server.controller.api.model.spec.RuleConfig);
        assertEquals(ruleConfig.getPollInterval(),
                     ((org.kie.server.controller.api.model.spec.RuleConfig) ruleConfigCurrent).getPollInterval());
        assertEquals(ruleConfig.getScannerStatus(),
                     ((org.kie.server.controller.api.model.spec.RuleConfig) ruleConfigCurrent).getScannerStatus());

        ContainerConfig containerConfig = new RuleConfig();
        ((RuleConfig) containerConfig).setScannerStatus(KieScannerStatus.SCANNING);
        ((RuleConfig) containerConfig).setPollInterval(10l);

        specManagementService.updateContainerConfig(serverTemplate.getId(),
                                                    containerSpec.getId(),
                                                    Capability.RULE,
                                                    containerConfig);

        Collection<org.kie.server.controller.api.model.spec.ContainerSpec> specs = specManagementService.listContainerSpec(serverTemplate.getId());
        assertNotNull(specs);
        assertEquals(1,
                     specs.size());

        container = specs.iterator().next();
        assertNotNull(container);

        assertEquals(containerSpec.getId(),
                     container.getId());
        assertEquals(containerSpec.getStatus(),
                     container.getStatus());
        assertEquals(containerSpec.getServerTemplateKey(),
                     container.getServerTemplateKey());
        assertEquals(containerSpec.getReleasedId(),
                     container.getReleasedId());

        assertNotNull(container.getConfigs());
        assertEquals(containerSpec.getConfigs().size(),
                     container.getConfigs().size());

        ContainerConfig ruleConfigCurrent2 = containerSpec.getConfigs().get(Capability.RULE);
        assertNotNull(ruleConfigCurrent2);
        assertTrue(ruleConfigCurrent2 instanceof org.kie.server.controller.api.model.spec.RuleConfig);
        assertEquals(((org.kie.server.controller.api.model.spec.RuleConfig) containerConfig).getPollInterval(),
                     ((org.kie.server.controller.api.model.spec.RuleConfig) ruleConfigCurrent2).getPollInterval());
        assertEquals(((org.kie.server.controller.api.model.spec.RuleConfig) containerConfig).getScannerStatus(),
                     ((org.kie.server.controller.api.model.spec.RuleConfig) ruleConfigCurrent2).getScannerStatus());
    }

    @Test
    public void testStartContainer() {
        createServerTemplateWithContainer();
        List<Container> fakeResult = new ArrayList<Container>();
        fakeResult.add(container);
        when(kieServerInstanceManager.startContainer(any(ServerTemplate.class),
                                                     any(ContainerSpec.class))).thenReturn(fakeResult);

        specManagementService.startContainer(containerSpec);

        verify(kieServerInstanceManager,
               times(1)).startContainer(any(ServerTemplate.class),
                                        any(ContainerSpec.class));

        ServerTemplate updated = specManagementService.getServerTemplate(serverTemplate.getId());
        assertNotNull(updated);

        ContainerSpec updatedContainer = updated.getContainerSpec(containerSpec.getId());
        assertNotNull(updatedContainer);

        assertEquals(KieContainerStatus.STARTED,
                     updatedContainer.getStatus());
    }

    @Test
    public void testStopContainer() {
        createServerTemplateWithContainer();

        final SpecManagementServiceImpl spy = spy((SpecManagementServiceImpl) specManagementService);
        final List<Container> fakeResult = new ArrayList<Container>() {{
            add(container);
        }};
        final Runnable successCallback = () -> {
        };
        final Runnable errorCallback = () -> {
        };

        doReturn(successCallback).when(spy).updateContainerAsStopped(any(),
                                                                     any());
        doReturn(errorCallback).when(spy).updateContainerAsStarted(any(),
                                                                   any());
        when(kieServerInstanceManager.stopContainer(any(ServerTemplate.class),
                                                    any(ContainerSpec.class),
                                                    eq(successCallback),
                                                    eq(errorCallback))).thenReturn(fakeResult);

        spy.stopContainer(containerSpec);

        verify(kieServerInstanceManager).stopContainer(any(ServerTemplate.class),
                                                       any(ContainerSpec.class),
                                                       eq(successCallback),
                                                       eq(errorCallback));

        final ServerTemplate updatedServerTemplate = spy.getServerTemplate(serverTemplate.getId());
        final ContainerSpec updatedContainer = updatedServerTemplate.getContainerSpec(containerSpec.getId());

        assertNotNull(updatedServerTemplate);
        assertNotNull(updatedContainer);
        assertEquals(KieContainerStatus.STOPPED,
                     updatedContainer.getStatus());
    }

    protected int getRandomInt(int min,
                               int max) {
        return (int) Math.floor(Math.random() * (max - min + 1)) + min;
    }
}
