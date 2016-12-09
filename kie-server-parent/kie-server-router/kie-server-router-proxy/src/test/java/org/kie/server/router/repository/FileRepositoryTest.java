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

import java.io.File;

import org.junit.Test;
import org.kie.server.router.Configuration;

public class FileRepositoryTest {

    @Test
    public void testStoreAndLoad() {
        
        Configuration config = new Configuration();
        
        config.addContainerHost("container1", "http://localhost:8080/server");
        config.addContainerHost("container2", "http://localhost:8180/server");
        
        config.addServerHost("server1", "http://localhost:8080/server");
        config.addServerHost("server2", "http://localhost:8180/server");
        
        FileRepository repo = new FileRepository(new File("target"));
        
        repo.persist(config);
        
        Configuration loaded = repo.load();
        
        assertNotNull(loaded);
        assertNotNull(loaded.getHostsPerContainer());
        assertNotNull(loaded.getHostsPerServer());
        assertEquals(2, loaded.getHostsPerContainer().size());
        assertEquals(2, loaded.getHostsPerServer().size());
        
        assertEquals(1, loaded.getHostsPerContainer().get("container1").size());
        assertEquals(1, loaded.getHostsPerContainer().get("container2").size());
        
        assertEquals("http://localhost:8080/server", loaded.getHostsPerContainer().get("container1").iterator().next());
        assertEquals("http://localhost:8180/server", loaded.getHostsPerContainer().get("container2").iterator().next());
        
        assertEquals(1, loaded.getHostsPerServer().get("server1").size());
        assertEquals(1, loaded.getHostsPerServer().get("server2").size());
        
        assertEquals("http://localhost:8080/server", loaded.getHostsPerServer().get("server1").iterator().next());
        assertEquals("http://localhost:8180/server", loaded.getHostsPerServer().get("server2").iterator().next());
    }
}
