/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
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

package org.kie.server.services.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.drools.compiler.kie.builder.impl.InternalKieScanner;
import org.drools.core.impl.InternalKieContainer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.kie.api.builder.KieScanner;
import org.kie.server.api.KieServerEnvironment;
import org.kie.server.api.model.KieContainerResource;
import org.kie.server.api.model.KieContainerStatus;
import org.kie.server.api.model.Message;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.services.impl.storage.KieServerState;
import org.kie.server.services.impl.storage.KieServerStateRepository;
import org.kie.server.services.impl.storage.file.KieServerStateFileRepository;
import org.mockito.Mockito;

public class KieServerImplOperationTest {

    private static final File REPOSITORY_DIR = new File("target/repository-dir");
    private static final String KIE_SERVER_ID = "kie-server-impl-test";
 
    private KieServerImpl kieServer;    
    private String origServerId = null;
    private KieServerStateRepository repository;

    @Before
    public void setupKieServerImpl() throws Exception {
        origServerId = KieServerEnvironment.getServerId();
        System.setProperty("org.kie.server.id", KIE_SERVER_ID);
        KieServerEnvironment.setServerId(KIE_SERVER_ID);

        FileUtils.deleteDirectory(REPOSITORY_DIR);
        FileUtils.forceMkdir(REPOSITORY_DIR);
        repository = new KieServerStateFileRepository(REPOSITORY_DIR);
        kieServer = new KieServerImpl(repository) {

            @Override
            protected Map<String, Object> getContainerParameters(org.kie.api.builder.ReleaseId releaseId, List<Message> messages) {
                
                return Collections.emptyMap();
            }
            
        };
        kieServer.init();
    }

    @After
    public void cleanUp() {
        if (kieServer != null) {
            kieServer.destroy();
        }
        KieServerEnvironment.setServerId(origServerId);
    }
    
    @Test
    public void testDisposeContainerWithScanner() {
        
        InternalKieScanner mockedScanner = Mockito.mock(InternalKieScanner.class);
        Mockito.when(mockedScanner.getStatus()).thenReturn(KieScanner.Status.RUNNING);
        
        InternalKieContainer mockedKieContainer = Mockito.mock(InternalKieContainer.class);
        Mockito.when(mockedKieContainer.getReleaseId()).thenReturn(new ReleaseId("g", "a", "v"));        
        
        KieContainerInstanceImpl kieContainerInstance = Mockito.mock(KieContainerInstanceImpl.class);
        Mockito.when(kieContainerInstance.getContainerId()).thenReturn("id");
        Mockito.when(kieContainerInstance.getStatus()).thenReturn(KieContainerStatus.STARTED);
        Mockito.when(kieContainerInstance.getKieContainer()).thenReturn(mockedKieContainer);
        Mockito.when(kieContainerInstance.getResource()).thenReturn(new KieContainerResource("id", new ReleaseId("g", "a", "v")));
        Mockito.when(kieContainerInstance.getScanner()).thenReturn(mockedScanner);
        
        kieServer.getServerRegistry().registerContainer("id", kieContainerInstance);
        
        kieServer.disposeContainer("id");
        
        Mockito.verify(kieContainerInstance, Mockito.times(1)).stopScanner();
        
    }
    
    @Test
    public void testDisposeContainerWithNoScanner() {
        
        InternalKieContainer mockedKieContainer = Mockito.mock(InternalKieContainer.class);
        Mockito.when(mockedKieContainer.getReleaseId()).thenReturn(new ReleaseId("g", "a", "v"));        
        
        KieContainerInstanceImpl kieContainerInstance = Mockito.mock(KieContainerInstanceImpl.class);
        Mockito.when(kieContainerInstance.getContainerId()).thenReturn("id");
        Mockito.when(kieContainerInstance.getStatus()).thenReturn(KieContainerStatus.STARTED);
        Mockito.when(kieContainerInstance.getKieContainer()).thenReturn(mockedKieContainer);
        Mockito.when(kieContainerInstance.getResource()).thenReturn(new KieContainerResource("id", new ReleaseId("g", "a", "v")));        
        
        kieServer.getServerRegistry().registerContainer("id", kieContainerInstance);
        
        kieServer.disposeContainer("id");
        
        Mockito.verify(kieContainerInstance, Mockito.times(0)).stopScanner();
        
    }
    
    @Test
    public void testActivateAndDeactivateContainer() {
        
        InternalKieContainer mockedKieContainer = Mockito.mock(InternalKieContainer.class);
        Mockito.when(mockedKieContainer.getReleaseId()).thenReturn(new ReleaseId("g", "a", "v"));        
        
        KieContainerResource container = new KieContainerResource("id", new ReleaseId("g", "a", "v"));
        KieContainerInstanceImpl kieContainerInstance = Mockito.mock(KieContainerInstanceImpl.class);
        Mockito.when(kieContainerInstance.getContainerId()).thenReturn("id");
        Mockito.when(kieContainerInstance.getStatus()).thenReturn(KieContainerStatus.STARTED);
        Mockito.when(kieContainerInstance.getKieContainer()).thenReturn(mockedKieContainer);
        Mockito.when(kieContainerInstance.getResource()).thenReturn(container);        
        
        kieServer.getServerRegistry().registerContainer("id", kieContainerInstance);
        KieServerState currentState = repository.load(KIE_SERVER_ID);
        currentState.getContainers().add(container);
        repository.store(KIE_SERVER_ID, currentState);
        
        kieServer.deactivateContainer("id");
        
        currentState = repository.load(KIE_SERVER_ID);
        assertNotNull(currentState);
        assertNotNull(currentState.getContainers());
        assertEquals(1, currentState.getContainers().size());
        
        container = currentState.getContainers().iterator().next();
        assertNotNull(container);
        assertEquals(KieContainerStatus.DEACTIVATED, container.getStatus());
        
        
        Mockito.when(kieContainerInstance.getStatus()).thenReturn(KieContainerStatus.DEACTIVATED);
        kieServer.activateContainer("id");
        
        currentState = repository.load(KIE_SERVER_ID);
        assertNotNull(currentState);
        assertNotNull(currentState.getContainers());
        assertEquals(1, currentState.getContainers().size());
        
        container = currentState.getContainers().iterator().next();
        assertNotNull(container);
        assertEquals(KieContainerStatus.STARTED, container.getStatus());
    }
    
}
