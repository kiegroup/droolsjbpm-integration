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
}
