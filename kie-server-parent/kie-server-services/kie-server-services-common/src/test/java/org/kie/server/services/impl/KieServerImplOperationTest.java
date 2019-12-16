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

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.drools.compiler.kie.builder.impl.InternalKieScanner;
import org.drools.core.impl.InternalKieContainer;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.kie.api.KieServices;
import org.kie.api.builder.KieRepository;
import org.kie.api.builder.KieScanner;
import org.kie.server.api.KieServerConstants;
import org.kie.server.api.KieServerEnvironment;
import org.kie.server.api.model.KieContainerResource;
import org.kie.server.api.model.KieContainerStatus;
import org.kie.server.api.model.Message;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.Severity;
import org.kie.server.services.impl.storage.KieServerState;
import org.kie.server.services.impl.storage.KieServerStateRepository;
import org.kie.server.services.impl.storage.file.KieServerStateFileRepository;
import org.kie.server.services.impl.util.DummyKieServerExtension;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class KieServerImplOperationTest {

    private static final File REPOSITORY_DIR = new File("target/repository-dir");
    private static final String KIE_SERVER_ID = "kie-server-impl-test";
 
    private KieServerImpl kieServer;    
    private String origServerId = null;
    private KieServerStateRepository repository;

    private DummyKieServerExtension errorKieServerExtension;

    @Mock
    private KieServices services;

    @Before
    public void setupKieServerImpl() throws Exception {
        MockitoAnnotations.initMocks(this);
        origServerId = KieServerEnvironment.getServerId();
        System.setProperty("org.kie.server.id", KIE_SERVER_ID);
        KieServerEnvironment.setServerId(KIE_SERVER_ID);

        FileUtils.deleteDirectory(REPOSITORY_DIR);
        FileUtils.forceMkdir(REPOSITORY_DIR);
        repository = new KieServerStateFileRepository(REPOSITORY_DIR);
        errorKieServerExtension = new DummyKieServerExtension();

        kieServer = new KieServerImpl(repository, services) {

            @Override
            protected Map<String, Object> getContainerParameters(org.kie.api.builder.ReleaseId releaseId, List<Message> messages) {
                Map<String, Object> parameters = new HashMap<String, Object>();
                parameters.put(KieServerConstants.KIE_SERVER_PARAM_MESSAGES, messages);
                return parameters;
            }

        };

        kieServer.init();
        kieServer.getServerRegistry().registerServerExtension(errorKieServerExtension);
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

    @Test
    public void testDeletionContainer() {
        String containerId = "test-container";
        ReleaseId releaseId = new ReleaseId("g", "a", "v");
        KieContainerResource container = new KieContainerResource("id", releaseId);
        container.setMessages(Collections.singletonList(new Message(Severity.ERROR, "Compilation failure")));

        InternalKieContainer mockedKieContainer = Mockito.mock(InternalKieContainer.class);
        KieRepository mockedKieRepository = Mockito.mock(KieRepository.class);
        Mockito.when(mockedKieContainer.getReleaseId()).thenReturn(new ReleaseId("g", "a", "v"));
        Mockito.when(mockedKieContainer.getContainerReleaseId()).thenReturn(new ReleaseId("g", "a", "v"));
        Mockito.when(services.getRepository()).thenReturn(mockedKieRepository);

        MutableBoolean fail = new MutableBoolean(false);
        Mockito.when(services.newKieContainer(containerId, releaseId)).then(e -> {
            if (fail.isTrue()) {
                throw new IllegalStateException("already exists in container");
            } else {
                return mockedKieContainer;
            }
        });
        // the first time it throws an error 
        errorKieServerExtension.addMessage(new Message(Severity.ERROR, "compilation failure"));
        kieServer.createContainer(containerId, container);
        Assert.assertTrue(kieServer.getContainerInfo(containerId).getResult().getStatus().equals(KieContainerStatus.FAILED));
        errorKieServerExtension.clear();

        // now it throws an already exists
        fail.setTrue();
        kieServer.createContainer(containerId, container);
        Assert.assertTrue(kieServer.getContainerInfo(containerId).getResult().getStatus().equals(KieContainerStatus.FAILED));

        // delete
        kieServer.disposeContainer(containerId);
        Assert.assertNull(kieServer.getContainerInfo(containerId).getResult());
        Mockito.verify(mockedKieContainer, Mockito.times(1)).dispose();

    }
}