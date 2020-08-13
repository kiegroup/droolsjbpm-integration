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
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.kie.server.api.KieServerConstants;
import org.kie.server.api.KieServerEnvironment;
import org.kie.server.api.model.KieContainerResource;
import org.kie.server.api.model.KieContainerStatus;
import org.kie.server.api.model.KieServerInfo;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.controller.api.KieServerController;
import org.kie.server.controller.api.model.KieServerSetup;
import org.kie.server.services.api.StartupStrategy;
import org.kie.server.services.impl.storage.KieServerState;
import org.kie.server.services.impl.storage.file.KieServerStateFileRepository;

public class KieServerImplInitTest {

    private static final File REPOSITORY_DIR = new File("target/repository-dir");
    private static final String KIE_SERVER_ID = "kie-server-impl-test";

    private KieServerImpl kieServer;
    private String origServerId = null;

    @Before
    public void setupKieServerImpl() throws Exception {
        origServerId = KieServerEnvironment.getServerId();
        System.setProperty("org.kie.server.id", KIE_SERVER_ID);
        KieServerEnvironment.setServerId(KIE_SERVER_ID);

        FileUtils.deleteDirectory(REPOSITORY_DIR);
        FileUtils.forceMkdir(REPOSITORY_DIR);                
    }

    @After
    public void cleanUp() {
        if (kieServer != null) {
            kieServer.destroy();
        }
        KieServerEnvironment.setServerId(origServerId);
        System.clearProperty(KieServerConstants.KIE_SERVER_STARTUP_STRATEGY);
        StartupStrategyProvider.clear();
    }
    
    @Test
    public void testStartupStrategyProviderDefault() {
        StartupStrategy strategy = StartupStrategyProvider.get().getStrategy();
        assertTrue(strategy instanceof ControllerBasedStartupStrategy);
    }
    
    @Test
    public void testStartupStrategyProviderLocalContainers() {        
        System.setProperty(KieServerConstants.KIE_SERVER_STARTUP_STRATEGY, LocalContainersStartupStrategy.class.getSimpleName());
        StartupStrategyProvider.clear();
        
        StartupStrategy strategy = StartupStrategyProvider.get().getStrategy();
        assertTrue(strategy instanceof LocalContainersStartupStrategy);
    }
    
    @Test(timeout=10000)
    public void testDefaultStartupStrategy() throws Exception { 
        final TestContainerManager testContainerManager = new TestContainerManager();
        
        kieServer = new KieServerImpl(new KieServerStateFileRepository(REPOSITORY_DIR)) {

            @Override
            protected ContainerManager getContainerManager() {
                return testContainerManager;
            }
            
        };

        kieServer.init();
               
        assertTrue(testContainerManager.isInstalled());
        assertEquals(0, testContainerManager.getInstalledContainers().size());
    }
    
    @Test(timeout=10000)
    public void testDefaultStartupStrategyFromController() throws Exception { 
        final TestContainerManager testContainerManager = new TestContainerManager();
        
        kieServer = new KieServerImpl(new KieServerStateFileRepository(REPOSITORY_DIR)) {

            @Override
            protected ContainerManager getContainerManager() {
                return testContainerManager;
            }

            @Override
            protected KieServerController getController() {
                return new KieServerController() {
                    
                    
                    @Override
                    public void disconnect(KieServerInfo serverInfo) {                        
                    }
                    
                    @Override
                    public KieServerSetup connect(KieServerInfo serverInfo) {
                        KieServerSetup serverSetup = new KieServerSetup();
                        KieContainerResource container = new KieContainerResource("test", new ReleaseId("", "", ""), KieContainerStatus.STARTED);
                        Set<KieContainerResource> containers = new HashSet<>();
                        containers.add(container);
                        serverSetup.setContainers(containers);
                        return serverSetup;
                    }
                };
            }
            
        };

        kieServer.init();
               
        assertTrue(testContainerManager.isInstalled());
        assertEquals(1, testContainerManager.getInstalledContainers().size());
    }
    
    @Test(timeout=10000)
    public void testLOcalContainersStartupStrategyFromController() throws Exception { 
        System.setProperty(KieServerConstants.KIE_SERVER_STARTUP_STRATEGY, LocalContainersStartupStrategy.class.getSimpleName());
        StartupStrategyProvider.clear();
        final TestContainerManager testContainerManager = new TestContainerManager();
        
        kieServer = new KieServerImpl(new KieServerStateFileRepository(REPOSITORY_DIR)) {

            @Override
            protected ContainerManager getContainerManager() {
                return testContainerManager;
            }

            @Override
            protected KieServerController getController() {
                return new KieServerController() {
                    
                    
                    @Override
                    public void disconnect(KieServerInfo serverInfo) {                        
                    }
                    
                    @Override
                    public KieServerSetup connect(KieServerInfo serverInfo) {
                        KieServerSetup serverSetup = new KieServerSetup();
                        KieContainerResource container = new KieContainerResource("test", new ReleaseId("", "", ""), KieContainerStatus.STARTED);
                        Set<KieContainerResource> containers = new HashSet<>();
                        containers.add(container);
                        serverSetup.setContainers(containers);
                        return serverSetup;
                    }
                };
            }
            
        };

        kieServer.init();
               
        assertTrue(testContainerManager.isInstalled());
        // even though controller returns containers they should be ignored when LocalContainersStartupStrategy is used
        assertEquals(0, testContainerManager.getInstalledContainers().size());
    }
    

    private class TestContainerManager extends ContainerManager {

        private boolean installed = false; 
        private Set<KieContainerResource> installedContainers;
        
        @Override
        public void installContainers(KieServerImpl kieServer, Set<KieContainerResource> containers, KieServerState currentState, KieServerSetup kieServerSetup) {
            this.installed = true;
            this.installedContainers = containers;
        }

        @Override
        public void installContainersSync(KieServerImpl kieServer, Set<KieContainerResource> containers, KieServerState currentState, KieServerSetup kieServerSetup) {
            this.installed = true;
            this.installedContainers = containers;
        }
        
        public boolean isInstalled() {
            return this.installed;
        }
        
        public Set<KieContainerResource> getInstalledContainers() {
            return installedContainers;
        }
    }
}
