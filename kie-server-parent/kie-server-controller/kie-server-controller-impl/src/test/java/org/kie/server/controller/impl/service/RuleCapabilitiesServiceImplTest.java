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
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.kie.server.api.model.KieScannerStatus;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.controller.api.model.runtime.Container;
import org.kie.server.controller.api.model.spec.Capability;
import org.kie.server.controller.api.model.spec.ContainerConfig;
import org.kie.server.controller.api.model.spec.ContainerSpec;
import org.kie.server.controller.api.model.spec.RuleConfig;
import org.kie.server.controller.api.model.spec.ServerTemplate;
import org.kie.server.controller.impl.KieServerInstanceManager;
import org.kie.server.controller.impl.storage.InMemoryKieServerTemplateStorage;
import org.mockito.Mockito;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class RuleCapabilitiesServiceImplTest extends AbstractServiceImplTest {

    @Before
    public void setup() {
        ruleCapabilitiesService = new RuleCapabilitiesServiceImpl();
        specManagementService = new SpecManagementServiceImpl();
        kieServerInstanceManager = Mockito.mock(KieServerInstanceManager.class);

        ((RuleCapabilitiesServiceImpl)ruleCapabilitiesService).setKieServerInstanceManager(kieServerInstanceManager);

        createServerTemplateWithContainer();
    }

    @After
    public void cleanup() {
        InMemoryKieServerTemplateStorage.getInstance().clear();
    }

    @Test
    public void testScanNow() {

        List<Container> fakeResult = new ArrayList<Container>();
        fakeResult.add(container);
        when(kieServerInstanceManager.scanNow(any(ServerTemplate.class), any(ContainerSpec.class))).thenReturn(fakeResult);

        ruleCapabilitiesService.scanNow(containerSpec);

        verify(kieServerInstanceManager, times(1)).scanNow(any(ServerTemplate.class), any(ContainerSpec.class));
    }

    @Test
    public void testStartScanner() {

        List<Container> fakeResult = new ArrayList<Container>();
        fakeResult.add(container);
        when(kieServerInstanceManager.startScanner(any(ServerTemplate.class), any(ContainerSpec.class), anyLong())).thenReturn(fakeResult);

        ruleCapabilitiesService.startScanner(containerSpec, 100l);

        verify(kieServerInstanceManager, times(1)).startScanner(any(ServerTemplate.class), any(ContainerSpec.class), anyLong());

        ServerTemplate updated = specManagementService.getServerTemplate(serverTemplate.getId());

        Collection<ContainerSpec> containerSpecs = updated.getContainersSpec();
        assertNotNull(containerSpecs);
        assertEquals(1, containerSpecs.size());

        ContainerSpec updatedContainer = containerSpecs.iterator().next();
        assertNotNull(updatedContainer);

        ContainerConfig ruleConfig = updatedContainer.getConfigs().get(Capability.RULE);
        assertNotNull(ruleConfig);
        assertTrue(ruleConfig instanceof RuleConfig);

        RuleConfig ruleCg = (RuleConfig) ruleConfig;

        assertEquals(KieScannerStatus.STARTED, ruleCg.getScannerStatus());
        assertEquals(100l, ruleCg.getPollInterval().longValue());
    }

    @Test
    public void testStopScanner() {
        List<Container> fakeResult = new ArrayList<Container>();
        fakeResult.add(container);
        when(kieServerInstanceManager.stopScanner(any(ServerTemplate.class), any(ContainerSpec.class))).thenReturn(fakeResult);

        ruleCapabilitiesService.stopScanner(containerSpec);

        verify(kieServerInstanceManager, times(1)).stopScanner(any(ServerTemplate.class), any(ContainerSpec.class));

        ServerTemplate updated = specManagementService.getServerTemplate(serverTemplate.getId());

        Collection<ContainerSpec> containerSpecs = updated.getContainersSpec();
        assertNotNull(containerSpecs);
        assertEquals(1, containerSpecs.size());

        ContainerSpec updatedContainer = containerSpecs.iterator().next();
        assertNotNull(updatedContainer);

        ContainerConfig ruleConfig = updatedContainer.getConfigs().get(Capability.RULE);
        assertNotNull(ruleConfig);
        assertTrue(ruleConfig instanceof RuleConfig);

        RuleConfig ruleCg = (RuleConfig) ruleConfig;

        assertEquals(KieScannerStatus.STOPPED, ruleCg.getScannerStatus());
        assertNull(ruleCg.getPollInterval());
    }

    @Test
    public void testUpgradeContainer() {

        List<Container> fakeResult = new ArrayList<Container>();
        fakeResult.add(container);
        when(kieServerInstanceManager.upgradeContainer(any(ServerTemplate.class), any(ContainerSpec.class))).thenReturn(fakeResult);

        ReleaseId initial = containerSpec.getReleasedId();
        ReleaseId upgradeTo = new ReleaseId("org.kie", "kie-server-kjar", "2.0");

        ruleCapabilitiesService.upgradeContainer(containerSpec, upgradeTo);

        verify(kieServerInstanceManager, times(1)).upgradeContainer(any(ServerTemplate.class), any(ContainerSpec.class));

        ServerTemplate updated = specManagementService.getServerTemplate(serverTemplate.getId());

        Collection<ContainerSpec> containerSpecs = updated.getContainersSpec();
        assertNotNull(containerSpecs);
        assertEquals(1, containerSpecs.size());

        ContainerSpec updatedContainer = containerSpecs.iterator().next();
        assertNotNull(updatedContainer);

        assertNotNull(updatedContainer.getReleasedId());
        assertNotEquals(initial, updatedContainer.getReleasedId());
        assertEquals(upgradeTo, updatedContainer.getReleasedId());
    }

}
