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
import static org.junit.Assert.assertNull;

import java.io.File;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Test;
import org.kie.server.router.Configuration;
import org.kie.server.router.ConfigurationListener;
import org.kie.server.router.ContainerInfo;
import org.kie.server.router.KieServerRouterConstants;

public class FileRepositoryTest {

    @After
    public void cleanup() {
        System.clearProperty(KieServerRouterConstants.CONFIG_FILE_WATCHER_ENABLED);
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
        
        FileRepository repo = new FileRepository(new File("target"));
        
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
    public void testWathServiceOnConfigFile() throws Exception {
        Configuration config = new Configuration();
        
        config.addContainerHost("container1", "http://localhost:8080/server");
        config.addContainerHost("container2", "http://localhost:8180/server");
        
        config.addServerHost("server1", "http://localhost:8080/server");
        config.addServerHost("server2", "http://localhost:8180/server");

        ContainerInfo containerInfo = new ContainerInfo("test1.0", "test", "org.kie:test:1.0");
        config.addContainerInfo(containerInfo);
        
        File repositoryDirectory = new File("target" + File.separator + UUID.randomUUID().toString());
        repositoryDirectory.mkdirs();
        
        FileRepository repo = new FileRepository(repositoryDirectory);
        
        repo.persist(config);
        
        System.setProperty(KieServerRouterConstants.CONFIG_FILE_WATCHER_ENABLED, "true");
        
        FileRepository repoWithWatcher = new FileRepository(repositoryDirectory);
        Configuration loaded = repoWithWatcher.load();
        
        CountDownLatch latch = new CountDownLatch(1);
        loaded.addListener(new ConfigurationListener() {

            @Override
            public void onConfigurationReloaded() {
                latch.countDown();
            }
            
        });
        
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
        repoWithWatcher.close();
        repoWithWatcher.clean();
    }
}
