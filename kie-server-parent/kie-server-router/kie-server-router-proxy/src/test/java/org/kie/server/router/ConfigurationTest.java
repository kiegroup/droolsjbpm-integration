/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
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

package org.kie.server.router;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class ConfigurationTest {


    @Test
    public void testRemoveServerWhenUnavailable() {

        Configuration config = new Configuration();

        config.addContainerHost("container1", "http://localhost:8080/server");
        config.addContainerHost("container2", "http://localhost:8180/server");

        config.addServerHost("server1", "http://localhost:8080/server");
        config.addServerHost("server2", "http://localhost:8180/server");

        ContainerInfo containerInfo = new ContainerInfo("test1.0", "test", "org.kie:test:1.0");
        config.addContainerInfo(containerInfo);

        assertEquals(2, config.getHostsPerContainer().size());
        assertEquals(2, config.getHostsPerServer().size());

        assertEquals(1, config.getHostsPerContainer().get("container1").size());
        assertEquals(1, config.getHostsPerContainer().get("container2").size());
        assertEquals(1, config.getHostsPerServer().get("server1").size());
        assertEquals(1, config.getHostsPerServer().get("server2").size());

        config.removeUnavailableServer("http://localhost:8180/server");

        assertEquals(2, config.getHostsPerContainer().size());
        assertEquals(2, config.getHostsPerServer().size());

        assertEquals(1, config.getHostsPerContainer().get("container1").size());
        assertEquals(0, config.getHostsPerContainer().get("container2").size());
        assertEquals(1, config.getHostsPerServer().get("server1").size());
        assertEquals(0, config.getHostsPerServer().get("server2").size());
    }

    @Test
    public void testRemoveServerWhenUnavailableRequestURL() {

        Configuration config = new Configuration();

        config.addContainerHost("container1", "http://localhost:8080/server");
        config.addContainerHost("container2", "http://localhost:8180/server");

        config.addServerHost("server1", "http://localhost:8080/server");
        config.addServerHost("server2", "http://localhost:8180/server");

        ContainerInfo containerInfo = new ContainerInfo("test1.0", "test", "org.kie:test:1.0");
        config.addContainerInfo(containerInfo);

        assertEquals(2, config.getHostsPerContainer().size());
        assertEquals(2, config.getHostsPerServer().size());

        assertEquals(1, config.getHostsPerContainer().get("container1").size());
        assertEquals(1, config.getHostsPerContainer().get("container2").size());
        assertEquals(1, config.getHostsPerServer().get("server1").size());
        assertEquals(1, config.getHostsPerServer().get("server2").size());

        config.removeUnavailableServer("http://localhost:8180/server/containers/instances/1");

        assertEquals(2, config.getHostsPerContainer().size());
        assertEquals(2, config.getHostsPerServer().size());

        assertEquals(1, config.getHostsPerContainer().get("container1").size());
        assertEquals(0, config.getHostsPerContainer().get("container2").size());
        assertEquals(1, config.getHostsPerServer().get("server1").size());
        assertEquals(0, config.getHostsPerServer().get("server2").size());
    }
    
    @Test
    public void testMultipleServersWithSameUrl() {

        Configuration config = new Configuration();

        // add two server with same host url for container
        config.addContainerHost("container1", "http://localhost:8080/server");
        config.addContainerHost("container1", "http://localhost:8080/server");
        // add two server with same host url for alias
        config.addContainerHost("container", "http://localhost:8080/server");
        config.addContainerHost("container", "http://localhost:8080/server");
        // add two server with same host url
        config.addServerHost("server1", "http://localhost:8080/server");
        config.addServerHost("server1", "http://localhost:8080/server");
        // add two containers info each for every server
        ContainerInfo containerInfo = new ContainerInfo("test1.0", "test", "org.kie:test:1.0");
        config.addContainerInfo(containerInfo);
        ContainerInfo containerInfo2 = new ContainerInfo("test1.0", "test", "org.kie:test:1.0");
        config.addContainerInfo(containerInfo2);

        assertEquals(2, config.getHostsPerContainer().size());
        assertEquals(1, config.getHostsPerServer().size());

        assertEquals(2, config.getHostsPerContainer().get("container1").size());        
        assertEquals(2, config.getHostsPerServer().get("server1").size());

        config.removeContainerHost("container1", "http://localhost:8080/server");
        config.removeContainerHost("container", "http://localhost:8080/server");
        config.removeServerHost("server1", "http://localhost:8080/server");

        config.removeContainerInfo(containerInfo);

        assertEquals(2, config.getHostsPerContainer().size());
        assertEquals(1, config.getHostsPerServer().size());

        assertEquals(1, config.getHostsPerContainer().get("container1").size());       
        assertEquals(1, config.getHostsPerServer().get("server1").size());
    }
    
    @Test
    public void testReloadFromConfigurationAddedServersAndContainers() {
        
        Configuration config = new Configuration();
        config.addContainerHost("container1", "http://localhost:8080/server");
        config.addServerHost("server1", "http://localhost:8080/server");
        ContainerInfo containerInfo = new ContainerInfo("test1.0", "test", "org.kie:test:1.0");
        config.addContainerInfo(containerInfo);

        assertEquals(1, config.getHostsPerContainer().size());
        assertEquals(1, config.getHostsPerServer().size());

        assertEquals(1, config.getHostsPerContainer().get("container1").size());        
        assertEquals(1, config.getHostsPerServer().get("server1").size());
        
        Configuration updated = new Configuration();
        updated.addContainerHost("container1", "http://localhost:8080/server");
        updated.addServerHost("server1", "http://localhost:8080/server");
        updated.addContainerHost("container2", "http://localhost:8081/server");
        updated.addServerHost("server2", "http://localhost:8081/server");
        ContainerInfo updatedContainerInfo = new ContainerInfo("test1.0", "test", "org.kie:test:1.0");
        updated.addContainerInfo(updatedContainerInfo);
        
        config.reloadFrom(updated);
        
        assertEquals(2, config.getHostsPerContainer().size());
        assertEquals(2, config.getHostsPerServer().size());

        assertEquals(1, config.getHostsPerContainer().get("container1").size());        
        assertEquals(1, config.getHostsPerServer().get("server1").size());
        assertEquals(1, config.getHostsPerContainer().get("container2").size());        
        assertEquals(1, config.getHostsPerServer().get("server2").size());
    }
    
    @Test
    public void testReloadFromConfigurationReplacedServersAndContainers() {
        
        Configuration config = new Configuration();
        config.addContainerHost("container1", "http://localhost:8080/server");
        config.addServerHost("server1", "http://localhost:8080/server");
        ContainerInfo containerInfo = new ContainerInfo("test1.0", "test", "org.kie:test:1.0");
        config.addContainerInfo(containerInfo);

        assertEquals(1, config.getHostsPerContainer().size());
        assertEquals(1, config.getHostsPerServer().size());

        assertEquals(1, config.getHostsPerContainer().get("container1").size());        
        assertEquals(1, config.getHostsPerServer().get("server1").size());
        
        Configuration updated = new Configuration();
        updated.addContainerHost("container2", "http://localhost:8081/server");
        updated.addServerHost("server2", "http://localhost:8081/server");
        ContainerInfo updatedContainerInfo = new ContainerInfo("test1.0", "test", "org.kie:test:1.0");
        updated.addContainerInfo(updatedContainerInfo);
        
        config.reloadFrom(updated);
        
        assertEquals(1, config.getHostsPerContainer().size());
        assertEquals(1, config.getHostsPerServer().size());

        assertNull(config.getHostsPerContainer().get("container1"));        
        assertNull(config.getHostsPerServer().get("server1"));
        assertEquals(1, config.getHostsPerContainer().get("container2").size());        
        assertEquals(1, config.getHostsPerServer().get("server2").size());
    }
    
    @Test
    public void testReloadFromConfigurationUpdatedUrls() {
        
        Configuration config = new Configuration();
        config.addContainerHost("container1", "http://localhost:8080/server");
        config.addServerHost("server1", "http://localhost:8080/server");
        ContainerInfo containerInfo = new ContainerInfo("test1.0", "test", "org.kie:test:1.0");
        config.addContainerInfo(containerInfo);

        assertEquals(1, config.getHostsPerContainer().size());
        assertEquals(1, config.getHostsPerServer().size());

        assertEquals(1, config.getHostsPerContainer().get("container1").size());        
        assertEquals(1, config.getHostsPerServer().get("server1").size());
        
        assertEquals("http://localhost:8080/server", config.getHostsPerContainer().get("container1").get(0));        
        assertEquals("http://localhost:8080/server", config.getHostsPerServer().get("server1").get(0));
               
        Configuration updated = new Configuration();
        updated.addContainerHost("container1", "http://localhost:8081/server");
        updated.addServerHost("server1", "http://localhost:8081/server");
        ContainerInfo updatedContainerInfo = new ContainerInfo("test1.0", "test", "org.kie:test:1.0");
        updated.addContainerInfo(updatedContainerInfo);
        
        config.reloadFrom(updated);
        
        assertEquals(1, config.getHostsPerContainer().size());
        assertEquals(1, config.getHostsPerServer().size());

        assertEquals(1, config.getHostsPerContainer().get("container1").size());        
        assertEquals(1, config.getHostsPerServer().get("server1").size());

        assertEquals("http://localhost:8081/server", config.getHostsPerContainer().get("container1").get(0));        
        assertEquals("http://localhost:8081/server", config.getHostsPerServer().get("server1").get(0));
    }
    
    @Test
    public void testReloadFromConfigurationUpdatedAndAddedUrls() {
        
        Configuration config = new Configuration();
        config.addContainerHost("container1", "http://localhost:8080/server");
        config.addServerHost("server1", "http://localhost:8080/server");
        ContainerInfo containerInfo = new ContainerInfo("test1.0", "test", "org.kie:test:1.0");
        config.addContainerInfo(containerInfo);

        assertEquals(1, config.getHostsPerContainer().size());
        assertEquals(1, config.getHostsPerServer().size());

        assertEquals(1, config.getHostsPerContainer().get("container1").size());        
        assertEquals(1, config.getHostsPerServer().get("server1").size());
        
        assertEquals("http://localhost:8080/server", config.getHostsPerContainer().get("container1").get(0));        
        assertEquals("http://localhost:8080/server", config.getHostsPerServer().get("server1").get(0));
               
        Configuration updated = new Configuration();
        updated.addContainerHost("container1", "http://localhost:8081/server");
        updated.addServerHost("server1", "http://localhost:8081/server");
        updated.addContainerHost("container1", "http://localhost:8080/server");
        updated.addServerHost("server1", "http://localhost:8080/server");
        ContainerInfo updatedContainerInfo = new ContainerInfo("test1.0", "test", "org.kie:test:1.0");
        updated.addContainerInfo(updatedContainerInfo);
        
        config.reloadFrom(updated);
        
        assertEquals(1, config.getHostsPerContainer().size());
        assertEquals(1, config.getHostsPerServer().size());

        assertEquals(2, config.getHostsPerContainer().get("container1").size());        
        assertEquals(2, config.getHostsPerServer().get("server1").size());

        assertEquals("http://localhost:8080/server", config.getHostsPerContainer().get("container1").get(0));        
        assertEquals("http://localhost:8080/server", config.getHostsPerServer().get("server1").get(0));
        assertEquals("http://localhost:8081/server", config.getHostsPerContainer().get("container1").get(1));        
        assertEquals("http://localhost:8081/server", config.getHostsPerServer().get("server1").get(1));
    }

    @Test
    public void testRemoveNotExistingContainerInfo() {

        Configuration config = new Configuration();

        ContainerInfo containerInfo = new ContainerInfo("test1.0", "test", "org.kie:test:1.0");
        config.addContainerInfo(containerInfo);

        assertEquals(2, config.getContainerInfosPerContainer().size());

        ContainerInfo notExistingContainerInfo = new ContainerInfo("not-existing-test1.0", "not-existing-test", "org.kie:test:1.0");
        config.removeContainerInfo(notExistingContainerInfo);

        assertEquals(2, config.getContainerInfosPerContainer().size());
    }
}
