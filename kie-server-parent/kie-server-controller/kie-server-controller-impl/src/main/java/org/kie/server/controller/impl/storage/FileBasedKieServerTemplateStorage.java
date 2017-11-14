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

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.kie.server.controller.api.model.spec.ServerTemplate;
import org.kie.server.controller.api.model.spec.ServerTemplateKey;
import org.kie.server.controller.api.storage.KieServerTemplateStorage;
import org.kie.soup.commons.xstream.XStreamUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.StreamException;

public class FileBasedKieServerTemplateStorage implements KieServerTemplateStorage {
	private static FileBasedKieServerTemplateStorage INSTANCE; 
	
    public static final String STORAGE_FILE_WATCHER_ENABLED = "org.kie.server.controller.templatefile.watcher.enabled";
    public static final String SERVER_TEMPLATE_FILE_NAME_PROP = "org.kie.server.controller.templatefile";
    public static final String DEFAULT_SERVER_TEMPLATE_FILENAME = System.getProperty("java.io.tmpdir")+
    		System.getProperty("file.separator")
    		+"template_store.xml";
    
    private static Logger logger = LoggerFactory.getLogger(FileBasedKieServerTemplateStorage.class);
    private Map<String, ServerTemplate> templateMap = new ConcurrentHashMap<>();
    private Map<String, ServerTemplateKey> templateKeyMap = new ConcurrentHashMap<>();
    private String templatesLocation;
  
    private XStream xstream;
    
    private boolean configWatcherEnabled = Boolean.parseBoolean(System.getProperty(STORAGE_FILE_WATCHER_ENABLED, "false"));
    private ControllerStorageFileWatcher watcher;
    
    public static synchronized FileBasedKieServerTemplateStorage getInstance() {
    	if (INSTANCE == null) {
    		INSTANCE = new FileBasedKieServerTemplateStorage();
    	}
    	return INSTANCE;
    }
    
    public static synchronized FileBasedKieServerTemplateStorage getInstance(String templatesLocation) {
    	if (INSTANCE == null) {
    		INSTANCE = new FileBasedKieServerTemplateStorage(templatesLocation);
    	}
    	return INSTANCE;
    }

    /**
     * Default constructor
     */
    protected FileBasedKieServerTemplateStorage() {
    	init(null); 
    }
    
    /**
     * Constructor that takes a template location argument
     * @param templatesLocation A string value that indicates where the server templates should be stored. A null value
     * indicates that the value should be retrieved from the system properties.
     */
    protected FileBasedKieServerTemplateStorage(String templatesLocation) {
    	init(templatesLocation);
    }
    
    /**
     * Initializes the class instance
     * @param templatesLocation A string value that indicates where the server templates should be stored. A null value
     * indicates that the value should be retrieved from the system properties.
     */
    protected synchronized void init(String templatesLocation) {
    	this.templateMap = new ConcurrentHashMap<>();
    	this.templateKeyMap = new ConcurrentHashMap<>();
    	this.xstream = XStreamUtils.createTrustingXStream();
    	if (templatesLocation != null && !templatesLocation.trim().isEmpty()) {
    		this.templatesLocation = templatesLocation;
    	} else {
    		this.templatesLocation = System.getProperty(SERVER_TEMPLATE_FILE_NAME_PROP, DEFAULT_SERVER_TEMPLATE_FILENAME);
    	}
        loadTemplateMapsFromFile();
    	
    	// setup template file watcher to be updated when changes are discovered
        if (configWatcherEnabled ) {
            this.watcher = new ControllerStorageFileWatcher(this.templatesLocation, this);
            Thread watcherThread = new Thread(watcher, "Kie Controller File Storage Watch Thread");
            watcherThread.start();
        }
    }

    /**
     * Writes the map of server templates to the file pointed at by templatesLocation
     */
    private synchronized void writeTemplateMap() {
        try (FileWriter writer = new FileWriter(templatesLocation)) {
            this.xstream.toXML(new ArrayList<ServerTemplate>(templateMap.values()), writer);
        } catch (Throwable e) {
            logger.error("Unable to write template maps for standalone controller",e);
        }
    }

    /**
     * Loads the map of server templates from the file pointed at by the templatesLocation
     */
    @SuppressWarnings("unchecked")
    public synchronized void loadTemplateMapsFromFile() {
        ArrayList<ServerTemplate> templates = null;
        Map<String, ServerTemplate> newTemplateMap = new ConcurrentHashMap<>();
        Map<String, ServerTemplateKey> newTemplateKeyMap = new ConcurrentHashMap<>();

        try (FileReader reader = new FileReader(templatesLocation)) {
            templates = (ArrayList<ServerTemplate>)this.xstream.fromXML(reader);
        } catch (StreamException | FileNotFoundException e) {
            logger.warn("Unable to read server template maps from file {}. File does not exist.", templatesLocation);
            writeTemplateMap();
        } catch (Throwable e) {
            logger.error("Unable to read server template maps from file",e);
        }
        if (templates != null && !templates.isEmpty()) {
            templates.forEach(template -> {
                newTemplateKeyMap.put(template.getId(),new ServerTemplateKey(template.getId(),template.getName()));
                newTemplateMap.put(template.getId(),template);
            });
        }
        templateKeyMap = newTemplateKeyMap;
        templateMap = newTemplateMap;
    }


    @Override
    public ServerTemplate store(ServerTemplate serverTemplate) {
        ServerTemplate template = null;
        synchronized (templateMap) {
            templateKeyMap.put(serverTemplate.getId(),
                               new ServerTemplateKey(serverTemplate.getId(),
                                                     serverTemplate.getName()));
            template = templateMap.put(serverTemplate.getId(),
                                   serverTemplate);
            writeTemplateMap();
        }
        return template;
    }

    @Override
    public List<ServerTemplateKey> loadKeys() {
        return new ArrayList<ServerTemplateKey>(templateKeyMap.values());
    }
    
    /**
     * Returns the ServerTemplateKey object associated with a server template's id
     * @param id The server template id that points to the ServerTemplateKey
     * @return The ServerTemplateKey that is associated with the id, or null if the
     * id does exist in the templateKeyMap
     */
    public ServerTemplateKey getTemplateKey(String id) {
    	return templateKeyMap.get(id);
    }

    @Override
    public List<ServerTemplate> load() {
        return new ArrayList<ServerTemplate>(templateMap.values());
    }

    @Override
    public ServerTemplate load(String identifier) {
        return templateMap.get(identifier);
    }

    @Override
    public boolean exists(String identifier) {
        return templateMap.containsKey(identifier);
    }

    @Override
    public ServerTemplate update(ServerTemplate serverTemplate) {
        ServerTemplate updated = null;
        synchronized (templateMap) {
            templateKeyMap.put(serverTemplate.getId(),
                               new ServerTemplateKey(serverTemplate.getId(),serverTemplate.getName()));
            updated = templateMap.put(serverTemplate.getId(),serverTemplate);
            writeTemplateMap();
        }
        return updated;
    }

    @Override
    public ServerTemplate delete(String identifier) {
        ServerTemplate removed = null;
        synchronized (templateMap) {
            templateKeyMap.remove(identifier);
            removed = templateMap.remove(identifier);
            writeTemplateMap();
        }
        return removed;
    }
    
    public void clearTemplateMaps() {
        synchronized (templateMap) {
            templateKeyMap.entrySet().clear();
            templateMap.entrySet().clear();
        }
    }

    public void setTemplatesLocation(String templatesLocation) {
        this.templatesLocation = templatesLocation;
    }

    public String getTemplatesLocation() {
        return templatesLocation;
    }

    @Override
    public String toString() {
        return "FileBasedKieServerTemplateStorage: { templatesLocation = " + templatesLocation + "}";
    }
    
    @Override
    public void close() {
        if (watcher != null) {
            watcher.stop();
        }
    }
}
