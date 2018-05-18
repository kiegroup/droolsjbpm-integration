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
package org.kie.server.controller.impl.storage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kie.server.api.model.KieContainerStatus;
import org.kie.server.api.model.KieScannerStatus;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.controller.api.model.runtime.Container;
import org.kie.server.controller.api.model.spec.Capability;
import org.kie.server.controller.api.model.spec.ContainerConfig;
import org.kie.server.controller.api.model.spec.ContainerSpec;
import org.kie.server.controller.api.model.spec.ProcessConfig;
import org.kie.server.controller.api.model.spec.RuleConfig;
import org.kie.server.controller.api.model.spec.ServerTemplate;
import org.kie.server.controller.api.model.spec.ServerTemplateKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;

public class FileBasedKieServerTemplateStorageTest {
    private static final Logger logger = LoggerFactory.getLogger(FileBasedKieServerTemplateStorageTest.class);
    private static final File TEST_SERVER_TEMPLATE_DIRECTORY = new File(System.getProperty("java.io.tmpdir"));
    
    private static Map<ServerTemplateKey, ServerTemplate> templateMap;
    private File tmpTemplateStore;
    private FileBasedKieServerTemplateStorage storage;
    
    /**
     * Method that creates a server template instance and saves it in the specManageService
     * @param templateName The name of the template being created
     * @param templateCount The expected number of templates saved in the specManageService, including the one being created 
     * @return A new server template instance
     */
    private static ServerTemplate createServerTemplateWithContainer(String templateName, int templateCount) {
        ServerTemplate template = new ServerTemplate();

        template.setName(templateName);
        template.setId(UUID.randomUUID().toString());

        Map<Capability, ContainerConfig> configs = new HashMap<Capability, ContainerConfig>();
        // Add a rule configuration to the server template's configuration
        RuleConfig ruleConfig = new RuleConfig();
        ruleConfig.setPollInterval(1000l);
        ruleConfig.setScannerStatus(KieScannerStatus.STARTED);

        configs.put(Capability.RULE, ruleConfig);

        // Add a process configuration to the server template's configuration
        ProcessConfig processConfig = new ProcessConfig();
        processConfig.setKBase("defaultKieBase");
        processConfig.setKSession("defaultKieSession");
        processConfig.setMergeMode("MERGE_COLLECTION");
        processConfig.setRuntimeStrategy("PER_PROCESS_INSTANCE");

        configs.put(Capability.PROCESS, processConfig);

        // Add a container specification to the specManageService, 
        // associating it with the new server template instance
        ContainerSpec containerSpec = new ContainerSpec();
        containerSpec.setId("test container");
        containerSpec.setServerTemplateKey(new ServerTemplateKey(template.getId(), template.getName()));
        containerSpec.setReleasedId(new ReleaseId("org.kie", "kie-server-kjar", "1.0"));
        containerSpec.setStatus(KieContainerStatus.STOPPED);
        containerSpec.setConfigs(configs);
        
        containerSpec.setServerTemplateKey(new ServerTemplateKey(template.getId(), template.getName()));
        template.addContainerSpec(containerSpec);

        // Create a container with the server template
        Container container = new Container();
        container.setServerInstanceId(template.getId());
        container.setServerTemplateId(template.getId());
        container.setResolvedReleasedId(containerSpec.getReleasedId());
        container.setContainerName(containerSpec.getContainerName());
        container.setContainerSpecId(containerSpec.getId());
        container.setUrl("http://fake.server.net/kie-server");
        container.setStatus(containerSpec.getStatus());

        return template;
    }
    
    /**
     * Retrieves the first template from our static map of server templates,
     * using an Iterator over the value in the map. Asserts that we did,
     * in fact, retrieve a template.
     * @return The first server template
     */
    private ServerTemplate getFirstTemplateFromMap() {
        Iterator<ServerTemplate> iter = templateMap.values().iterator();
        ServerTemplate testTemplate = iter.hasNext() ? iter.next() : null;
        assertNotNull("Unable to find a test server template!",testTemplate);
        return testTemplate;
    }
    
    /**
     * Retrieves a template from the static instance of FileBasedKieServerTemplateStorage,
     * based on the id found in the passed in ServerTemplate. Asserts that we
     * did retrieve a template, and that the retrieved template is equal to 
     * the one that was used as a source for the id.
     * @param template The source instance of a ServerTemplate
     * @return The retrieved ServerTemplate instance
     */
    private ServerTemplate loadTemplateWithAssertEquals(ServerTemplate template) {
        ServerTemplate loadedTemplate = storage.load(template.getId());
        assertNotNull("Unable to load template from storage",loadedTemplate);
        assertEquals("Loaded template is not the one asked for",template,loadedTemplate);
        return loadedTemplate;
    }
    
    @BeforeClass
    public static void beforeClass() {
        templateMap = Maps.newConcurrentMap();
        for (int x = 0; x < 3; x++) {
            StringBuilder templateName = new StringBuilder("test server : ").append(x);
            ServerTemplate template = createServerTemplateWithContainer(templateName.toString(),x+1);
            ServerTemplateKey key = new ServerTemplateKey(template.getId(), template.getName());
            templateMap.put(key, template);
        }
    }
    
    @Before
    public void setup() throws IOException {
    	tmpTemplateStore = File.createTempFile("templates_", ".xml", TEST_SERVER_TEMPLATE_DIRECTORY);
        storage = new FileBasedKieServerTemplateStorage(tmpTemplateStore.getAbsolutePath());
        templateMap.keySet().forEach(key -> {
            storage.store(templateMap.get(key));
        });
        assertEquals("Mismatched number of server templates stored",templateMap.keySet().size(),storage.loadKeys().size());
    }
    
    @After
    public void clean() {
        System.clearProperty(FileBasedKieServerTemplateStorage.STORAGE_FILE_WATCHER_ENABLED);
        try {
            Files.deleteIfExists(tmpTemplateStore.toPath());
        } catch (IOException e) {
            logger.warn("Exception while deleting test server template storage",e);
            e.printStackTrace();
        }
    }
    
    @Test
    public void testStore() {
        /*
         * Just need to make sure that if we load the keys
         * back in from the file, we get the same size set
         * More in depth testing of the actual elements retrieve
         * happens in later tests
        */
        storage.loadTemplateMapsFromFile();
        assertEquals("Mismatched number of server templates",templateMap.keySet().size(),storage.loadKeys().size());
    }
    
    @Test
    public void testLoadKeys() {
        /*
         * Using the clearTemplateMaps method insures
         * that the code that checks for loading from
         * files is called
         */
        storage.loadTemplateMapsFromFile();
        List<ServerTemplateKey> keys = storage.loadKeys();
        /*
         * Now we check that both the number of keys retrieved is correct
         * and that for each key we think we should have, it is in our 
         * reloaded keys
         */
        assertEquals("Mismatched number of server template keys",templateMap.keySet().size(),keys.size());
        templateMap.keySet().forEach(key -> {
            assertTrue("Key for server template not found",keys.contains(key));
        });
    }
    
    @Test
    public void testLoadList() {
        storage.loadTemplateMapsFromFile();
        List<ServerTemplate> templates = storage.load();
        assertEquals("Mismatched number of server templates",templateMap.values().size(),templates.size());
        templateMap.values().forEach(value -> {
            assertTrue("Server template not found",templates.contains(value));
        });
    }
 
    @Test
    public void testLoadSingle() {
        storage.loadTemplateMapsFromFile();
        ServerTemplate toSearchFor = getFirstTemplateFromMap();
        loadTemplateWithAssertEquals(toSearchFor);
    }

    @Test
    public void testLoadNotExisting() {
        storage.loadTemplateMapsFromFile();
        String notExists = "not-exists";
        ServerTemplate loadedTemplate = storage.load(notExists);
        assertNull(loadedTemplate);
    }

    @Test
    public void testExists() {
        storage.loadTemplateMapsFromFile();
        ServerTemplate toSearchFor = getFirstTemplateFromMap();
        assertTrue("Exists fails",storage.exists(toSearchFor.getId()));
    }

    @Test
    public void testNotExists() {
        storage.loadTemplateMapsFromFile();
        String notExists = "not-exists";
        assertFalse("Exists return true for not existing id: " + notExists, storage.exists(notExists));
    }

    @Test
    public void testUpdate() {
        final String testName = "Updated template Name";
        storage.loadTemplateMapsFromFile();
        ServerTemplate toUpdateTemplate = getFirstTemplateFromMap();
        toUpdateTemplate.setName(testName);
        storage.update(toUpdateTemplate);
        storage.loadTemplateMapsFromFile();
        loadTemplateWithAssertEquals(toUpdateTemplate);
    }
    
    @Test
    public void testDelete() {
        storage.clearTemplateMaps();
        ServerTemplate toDeleteTemplate = getFirstTemplateFromMap();
        storage.delete(toDeleteTemplate.getId());
        storage.clearTemplateMaps();
        assertTrue("Delete template failed",!storage.exists(toDeleteTemplate.getId()));
    }

    @Test
    public void testDeleteNotExistingTemplate() {
        storage.loadTemplateMapsFromFile();
        List<ServerTemplate> templates = storage.load();
        assertEquals("Mismatched number of server templates", templateMap.values().size(), templates.size());
        storage.delete("not-exists");
        storage.loadTemplateMapsFromFile();
        templates = storage.load();
        assertEquals("Mismatched number of server templates", templateMap.values().size(), templates.size());
    }

    @Test
    public void testLoadNotExistingTemplate() {
        List<ServerTemplate> templates = storage.load();
        assertEquals(3, templates.size());

        // Delete template file
        tmpTemplateStore.delete();

        storage.loadTemplateMapsFromFile();

        // Storage should still contain previously loaded templates
        templates = storage.load();
        assertEquals(3, templates.size());
    }

    @Test
    public void testGetStorageLocation() {
        String location = storage.getTemplatesLocation();
        assertEquals(tmpTemplateStore.getAbsolutePath(), location);
    }
    
    @Test(timeout=30000)
    public void testUpdatedStorageFromWatcher() throws Exception {
        FileBasedKieServerTemplateStorage secondStorage = new FileBasedKieServerTemplateStorage(tmpTemplateStore.getAbsolutePath());
        
        System.setProperty(FileBasedKieServerTemplateStorage.STORAGE_FILE_WATCHER_ENABLED, "true");
        System.setProperty(ControllerStorageFileWatcher.STORAGE_FILE_WATCHER_INTERVAL, "1000");
        CountDownLatch waitForReload = new CountDownLatch(2);
        storage = new FileBasedKieServerTemplateStorage(tmpTemplateStore.getAbsolutePath()) {

            @Override
            public void loadTemplateMapsFromFile() {
                super.loadTemplateMapsFromFile();
                waitForReload.countDown();
            }
            
        };
        List<ServerTemplate> templates = storage.load();
        assertEquals(3, templates.size());
        
        // delay it a bit from the creation of the file
        Thread.sleep(3000);        
        
        ServerTemplate serverTemplate = new ServerTemplate();

        serverTemplate.setName("UpdateFromOtherController");
        serverTemplate.setId(UUID.randomUUID().toString());
        secondStorage.store(serverTemplate);
        
        waitForReload.await();
        
        templates = storage.load();
        assertEquals(4, templates.size());
    }
}
