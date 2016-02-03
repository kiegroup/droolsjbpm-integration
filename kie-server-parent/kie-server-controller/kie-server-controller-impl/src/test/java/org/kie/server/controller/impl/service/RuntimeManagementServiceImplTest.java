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
import org.kie.server.controller.api.model.runtime.Container;
import org.kie.server.controller.api.model.runtime.ServerInstanceKey;
import org.kie.server.controller.impl.KieServerInstanceManager;
import org.kie.server.controller.impl.storage.InMemoryKieServerTemplateStorage;
import org.mockito.Mockito;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class RuntimeManagementServiceImplTest extends AbstractServiceImplTest {

    @Before
    public void setup() {
        runtimeManagementService = new RuntimeManagementServiceImpl();
        specManagementService = new SpecManagementServiceImpl();
        kieServerInstanceManager = Mockito.mock(KieServerInstanceManager.class);

        ((RuntimeManagementServiceImpl)runtimeManagementService).setKieServerInstanceManager(kieServerInstanceManager);

        createServerTemplateWithContainer();
    }

    @After
    public void cleanup() {
        InMemoryKieServerTemplateStorage.getInstance().clear();
    }

    @Test
    public void testGetServerInstances() {

        Collection<org.kie.server.controller.api.model.runtime.ServerInstanceKey> found = runtimeManagementService.getServerInstances(serverTemplate.getId());
        assertNotNull(found);

        assertEquals(0, found.size());

        serverTemplate.addServerInstance(new ServerInstanceKey(serverTemplate.getId(), "test server","instanceId" , "http://fake.url.org"));
        specManagementService.saveServerTemplate(serverTemplate);

        found = runtimeManagementService.getServerInstances(serverTemplate.getId());
        assertNotNull(found);

        assertEquals(1, found.size());

        org.kie.server.controller.api.model.runtime.ServerInstanceKey server = found.iterator().next();
        assertNotNull(server);

        assertEquals(serverTemplate.getId(), server.getServerTemplateId());
        assertEquals("instanceId", server.getServerInstanceId());
        assertEquals("test server", server.getServerName());
        assertEquals("http://fake.url.org", server.getUrl());
    }

    @Test
    public void testGetContainers() {

        List<Container> fakeResult = new ArrayList<Container>();
        fakeResult.add(container);
        when(kieServerInstanceManager.getContainers(any(ServerInstanceKey.class))).thenReturn(fakeResult);

        org.kie.server.controller.api.model.runtime.ServerInstanceKey instanceKey = new ServerInstanceKey("instanceId", "test server", serverTemplate.getId(), "http://fake.url.org");
        serverTemplate.addServerInstance(instanceKey);
        specManagementService.saveServerTemplate(serverTemplate);

        Collection<Container> containers = runtimeManagementService.getContainers(instanceKey);
        assertNotNull(containers);

        assertEquals(1, containers.size());
        verify(kieServerInstanceManager, times(1)).getContainers(any(ServerInstanceKey.class));
    }

}
