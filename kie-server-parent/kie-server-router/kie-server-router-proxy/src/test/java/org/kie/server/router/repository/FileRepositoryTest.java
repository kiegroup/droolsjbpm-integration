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

package org.kie.server.router.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileReader;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.kie.server.router.Configuration;
import org.kie.server.router.ConfigurationListener;
import org.kie.server.router.ConfigurationManager;
import org.kie.server.router.ContainerInfo;
import org.kie.server.router.KieServerRouterConstants;
import org.kie.server.router.KieServerRouterEnvironment;

import io.undertow.util.FileUtils;

public class FileRepositoryTest {

    @Before
    public void init () {
        System.setProperty(KieServerRouterConstants.ROUTER_REPOSITORY_DIR, "target");
    }

    @After
    public void cleanup() {
        System.clearProperty(KieServerRouterConstants.CONFIG_FILE_WATCHER_ENABLED);
        System.clearProperty(KieServerRouterConstants.CONFIG_FILE_WATCHER_INTERVAL);
        System.clearProperty(KieServerRouterConstants.ROUTER_REPOSITORY_DIR);
    }
    
    @Test
    public void testStoreAndLoad() {
        
        Configuration config = new Configuration();
        
        config.addContainerHost("container1", "http://localhost:8080/server");
        config.addContainerHost("container2", "http://localhost:8180/server");
        
        config.addServerHost("server1", "http://localhost:8080/server");
        config.addServerHost("server2", "http://localhost:8180/server");

        ContainerInfo containerInfo = new ContainerInfo("test1.0", "test", "org.kie:test:1.0");
        config.addContainerInfo(containerInfo);

        FileRepository repo = new FileRepository(new KieServerRouterEnvironment());
        
        repo.persist(config);
        
        Configuration loaded = repo.load();
        
        assertNotNull(loaded);
        assertNotNull(loaded.getHostsPerContainer());
        assertNotNull(loaded.getHostsPerServer());
        assertEquals(2, loaded.getHostsPerContainer().size());
        assertEquals(2, loaded.getHostsPerServer().size());
        assertEquals(2, loaded.getContainerInfosPerContainer().size());
        
        assertEquals(1, loaded.getHostsPerContainer().get("container1").size());
        assertEquals(1, loaded.getHostsPerContainer().get("container2").size());
        
        assertEquals("http://localhost:8080/server", loaded.getHostsPerContainer().get("container1").iterator().next());
        assertEquals("http://localhost:8180/server", loaded.getHostsPerContainer().get("container2").iterator().next());
        
        assertEquals(1, loaded.getHostsPerServer().get("server1").size());
        assertEquals(1, loaded.getHostsPerServer().get("server2").size());
        
        assertEquals("http://localhost:8080/server", loaded.getHostsPerServer().get("server1").iterator().next());
        assertEquals("http://localhost:8180/server", loaded.getHostsPerServer().get("server2").iterator().next());

        assertEquals(1, loaded.getContainerInfosPerContainer().get("test").size());
        assertEquals(1, loaded.getContainerInfosPerContainer().get("test1.0").size());

        ContainerInfo loadedCI = loaded.getContainerInfosPerContainer().get("test").iterator().next();
        assertEquals(containerInfo, loadedCI);

        loadedCI = loaded.getContainerInfosPerContainer().get("test1.0").iterator().next();
        assertEquals(containerInfo, loadedCI);
        
        repo.clean();
    }
    
    @Test
    public void testWatchServiceOnConfigFile() throws Exception {
        ScheduledExecutorService executorService = Executors.newScheduledThreadPool(8);

        Configuration config = new Configuration();
        
        config.addContainerHost("container1", "http://localhost:8080/server");
        config.addContainerHost("container2", "http://localhost:8180/server");
        
        config.addServerHost("server1", "http://localhost:8080/server");
        config.addServerHost("server2", "http://localhost:8180/server");

        ContainerInfo containerInfo = new ContainerInfo("test1.0", "test", "org.kie:test:1.0");
        config.addContainerInfo(containerInfo);

        File repositoryDirectory = new File("target" + File.separator + UUID.randomUUID().toString());
        repositoryDirectory.mkdirs();

        System.setProperty(KieServerRouterConstants.CONFIG_FILE_WATCHER_ENABLED, "true");
        System.setProperty(KieServerRouterConstants.CONFIG_FILE_WATCHER_INTERVAL, "1000");

        KieServerRouterEnvironment kieServerRouterEnvironment = new KieServerRouterEnvironment();

        FileRepository repo = new FileRepository(kieServerRouterEnvironment);
        repo.persist(config);

        ConfigurationManager manager = new ConfigurationManager(kieServerRouterEnvironment, repo, executorService);

        Configuration loaded = manager.getConfiguration();

        CountDownLatch latch = new CountDownLatch(1);
        loaded.addListener(new ConfigurationListener() {

            @Override
            public void onConfigurationReloaded() {
                latch.countDown();
            }
            
        });

        manager.startWatcher();
        assertNotNull(loaded);
        assertNotNull(loaded.getHostsPerContainer());
        assertNotNull(loaded.getHostsPerServer());
        assertEquals(2, loaded.getHostsPerContainer().size());
        assertEquals(2, loaded.getHostsPerServer().size());
        assertEquals(2, loaded.getContainerInfosPerContainer().size());
        
        assertEquals(1, loaded.getHostsPerContainer().get("container1").size());
        assertEquals(1, loaded.getHostsPerContainer().get("container2").size());
        
        config.removeContainerHost("container2", "http://localhost:8180/server");
        config.removeServerHost("server2", "http://localhost:8180/server");
        // delay it a bit from the creation of the file
        Thread.sleep(3000);
        
        repo.persist(config);
        
        boolean reloaded = latch.await(20, TimeUnit.SECONDS);
        
        if (reloaded) {
            assertNotNull(loaded);
            assertNotNull(loaded.getHostsPerContainer());
            assertNotNull(loaded.getHostsPerServer());
            assertEquals(2, loaded.getHostsPerContainer().size());
            assertEquals(2, loaded.getHostsPerServer().size());
            assertEquals(2, loaded.getContainerInfosPerContainer().size());
            
            assertEquals(1, loaded.getHostsPerContainer().get("container1").size());
            assertEquals(0, loaded.getHostsPerContainer().get("container2").size());
        }

        manager.close();
        executorService.shutdownNow();
    }

    @Test
    public void testWatchServiceOnLatelyCreatedConfigFile() throws Exception {
        ScheduledExecutorService executorService = Executors.newScheduledThreadPool(8);
        // Start watcher service with not existing config file
        String fileDir = "target" + File.separator + UUID.randomUUID().toString();
        System.setProperty(KieServerRouterConstants.ROUTER_REPOSITORY_DIR, fileDir);
        File repositoryDirectory = new File(fileDir);
        repositoryDirectory.mkdirs();

        System.setProperty(KieServerRouterConstants.CONFIG_FILE_WATCHER_ENABLED, "true");
        System.setProperty(KieServerRouterConstants.CONFIG_FILE_WATCHER_INTERVAL, "1000");

        KieServerRouterEnvironment environment = new KieServerRouterEnvironment();

        FileRepository repoWithWatcher = new FileRepository(environment);
        ConfigurationManager configurationManager = new ConfigurationManager(environment, repoWithWatcher, executorService);
        Configuration loaded = configurationManager.getConfiguration();

        CountDownLatch latch = new CountDownLatch(1);
        loaded.addListener(new ConfigurationListener() {

            @Override
            public void onConfigurationReloaded() {
                latch.countDown();
            }
        });

        configurationManager.startWatcher();

        Configuration config = new Configuration();

        config.addContainerHost("container1", "http://localhost:8080/server");
        config.addContainerHost("container2", "http://localhost:8180/server");

        config.addServerHost("server1", "http://localhost:8080/server");
        config.addServerHost("server2", "http://localhost:8180/server");

        ContainerInfo containerInfo = new ContainerInfo("test1.0", "test", "org.kie:test:1.0");
        config.addContainerInfo(containerInfo);

        repoWithWatcher.persist(config);

        // delay it a bit for the watcher to be triggered
        latch.await(5, TimeUnit.SECONDS);

        assertNotNull(loaded);
        assertNotNull(loaded.getHostsPerContainer());
        assertNotNull(loaded.getHostsPerServer());
        assertEquals(2, loaded.getHostsPerContainer().size());
        assertEquals(2, loaded.getHostsPerServer().size());
        assertEquals(2, loaded.getContainerInfosPerContainer().size());
        assertEquals(1, loaded.getHostsPerContainer().get("container1").size());
        assertEquals(1, loaded.getHostsPerContainer().get("container2").size());

        configurationManager.close();
        executorService.shutdownNow();
        repoWithWatcher.close();
        repoWithWatcher.clean();
    }

    @Test
    public void testFileCreation() throws Exception {
        ScheduledExecutorService executorService = Executors.newScheduledThreadPool(8);

        ConfigurationMarshaller marshaller = new ConfigurationMarshaller();
        Configuration configuration = new Configuration();
        
        String fileDir = "target" + File.separator + UUID.randomUUID().toString();
        System.setProperty(KieServerRouterConstants.ROUTER_REPOSITORY_DIR, fileDir);
        File repositoryDirectory = new File(fileDir);
        repositoryDirectory.mkdirs();

        System.setProperty(KieServerRouterConstants.CONFIG_FILE_WATCHER_ENABLED, "true");
        System.setProperty(KieServerRouterConstants.CONFIG_FILE_WATCHER_INTERVAL, "1000");

        KieServerRouterEnvironment environment = new KieServerRouterEnvironment();

        FileRepository repoWithWatcher = new FileRepository(environment);
        ConfigurationManager configurationManager = new ConfigurationManager(environment, repoWithWatcher, executorService);
        Configuration loaded = configurationManager.getConfiguration();


        CountDownLatch latch = new CountDownLatch(1);
        loaded.addListener(new ConfigurationListener() {

            @Override
            public void onConfigurationReloaded() {
                latch.countDown();
            }
        });

        configurationManager.startWatcher();

        // delay it a bit for the watcher to be triggered
        latch.await(20, TimeUnit.SECONDS);

        configurationManager.stopWatcher();
        assertEquals(loaded.toString().trim(), configuration.toString().trim());

        // second part of the test

        System.setProperty(KieServerRouterConstants.CONFIG_FILE_WATCHER_ENABLED, "false");
        // Create configuration file
        Configuration config = new Configuration();
        config.addContainerHost("container1", "http://localhost:8080/server");
        config.addContainerHost("container2", "http://localhost:8180/server");

        config.addServerHost("server1", "http://localhost:8080/server");
        config.addServerHost("server2", "http://localhost:8180/server");

        ContainerInfo containerInfo = new ContainerInfo("test1.0", "test", "org.kie:test:1.0");
        config.addContainerInfo(containerInfo);

        repoWithWatcher.persist(config);

        File serverStateFile = new File(repositoryDirectory, "kie-server-router" + ".json");

        assertTrue(serverStateFile.exists());

        if (serverStateFile.exists()) {
            try (FileReader reader = new FileReader(serverStateFile)){
                configuration = marshaller.unmarshall(reader);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        assertEquals(config.toString().trim(), configuration.toString().trim());

        configurationManager.close();
        executorService.shutdownNow();
        repoWithWatcher.close();
        repoWithWatcher.clean();
    }

    @Test
    public void testConfigurationFileStartStop() throws Exception {

        String fileDir = "target" + File.separator + UUID.randomUUID().toString();
        File repositoryDirectory = new File(fileDir);
        repositoryDirectory.mkdirs();

        System.setProperty(KieServerRouterConstants.ROUTER_REPOSITORY_DIR, fileDir);
        System.setProperty(KieServerRouterConstants.CONFIG_FILE_WATCHER_ENABLED, "true");
        System.setProperty(KieServerRouterConstants.CONFIG_FILE_WATCHER_INTERVAL, "1000");

        KieServerRouterEnvironment env = new KieServerRouterEnvironment();
        FileRepository repoWithWatcher = new FileRepository(env);
        Configuration loaded = repoWithWatcher.load();


        String containerId = "container1";
        String serverId = "server1";
        String releaseId = "1.0";
        String serverUrl = "http://localhost:8080/server";
        ContainerInfo containerInfo = new ContainerInfo(containerId, containerId, releaseId);
        loaded.addContainerHost(containerId, serverUrl);
        loaded.addServerHost(serverId, serverUrl);
        loaded.addContainerInfo(containerInfo);

        containerId = "container2";
        containerInfo = new ContainerInfo(containerId, containerId, releaseId);
        loaded.addContainerHost(containerId, serverUrl);
        loaded.addServerHost(serverId, serverUrl);
        loaded.addContainerInfo(containerInfo);

        containerId = "container1";
        serverId = "server2";
        serverUrl = "http://localhost:8083/server";
        containerInfo = new ContainerInfo(containerId, containerId, releaseId);
        loaded.addContainerHost(containerId, serverUrl);
        loaded.addServerHost(serverId, serverUrl);
        loaded.addContainerInfo(containerInfo);

        containerId = "container2";
        containerInfo = new ContainerInfo(containerId, containerId, releaseId);
        loaded.addContainerHost(containerId, serverUrl);
        loaded.addServerHost(serverId, serverUrl);
        loaded.addContainerInfo(containerInfo);

        repoWithWatcher.persist(loaded);

        loaded.removeContainerHost(containerId, serverUrl);
        loaded.removeServerHost(serverId, serverUrl);
        loaded.removeContainerInfo(containerInfo);

        containerId = "container1";
        loaded.removeContainerHost(containerId, serverUrl);
        loaded.removeServerHost(serverId, serverUrl);
        loaded.removeContainerInfo(containerInfo);
        repoWithWatcher.persist(loaded);

        Assert.assertEquals(1, loaded.getHostsPerContainer().get("container1").size());
        Assert.assertEquals(0, loaded.getHostsPerServer().get("server2").size());
        repoWithWatcher.close();
        repoWithWatcher.clean();
        FileUtils.deleteRecursive(repositoryDirectory.toPath());
    }

}