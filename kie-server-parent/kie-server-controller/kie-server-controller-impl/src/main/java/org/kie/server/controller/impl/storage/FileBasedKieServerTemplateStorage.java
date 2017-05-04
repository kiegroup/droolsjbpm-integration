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

import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.kie.server.api.marshalling.Marshaller;
import org.kie.server.api.marshalling.MarshallerFactory;
import org.kie.server.api.marshalling.MarshallingFormat;
import org.kie.server.api.marshalling.xstream.XStreamMarshaller;
import org.kie.server.controller.api.model.spec.ServerTemplate;
import org.kie.server.controller.api.model.spec.ServerTemplateKey;
import org.kie.server.controller.api.storage.KieServerTemplateStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileBasedKieServerTemplateStorage implements KieServerTemplateStorage {
    public static final String SERVER_TEMPLATE_FILE_NAME_PROP = "org.kie.server.controller.templatefile";
    public static final String DEFAULT_SERVER_TEMPLATE_FILENAME = System.getProperty("java.io.tmpdir")+
    		System.getProperty("file.separator")
    		+"template_store.xml";
    private static Logger logger = LoggerFactory.getLogger(FileBasedKieServerTemplateStorage.class);
    private Map<String, ServerTemplate> templateMap = new ConcurrentHashMap<>();
    private Map<String, ServerTemplateKey> templateKeyMap = new ConcurrentHashMap<>();
    private String templatesLocation = System.getProperty(SERVER_TEMPLATE_FILE_NAME_PROP, DEFAULT_SERVER_TEMPLATE_FILENAME);
    private Marshaller templateMarshaller = MarshallerFactory.getMarshaller(MarshallingFormat.XSTREAM,ServerTemplate.class.getClassLoader());


    /**
     * Writes the map of server templates to the file pointed at by templatesLocation
     */
    private synchronized void writeTemplateMap() {
        try (FileWriter writer = new FileWriter(templatesLocation)) {
            ((XStreamMarshaller)templateMarshaller).getXstream()
            	.toXML(new ArrayList<ServerTemplate>(templateMap.values()), writer);
        } catch (Throwable e) {
            logger.error("Unable to write template maps for standalone controller",e);
        }
    }

    /**
     * Loads the map of server templates from the file pointed at by the templatesLocation
     */
    @SuppressWarnings("unchecked")
    private synchronized void loadTemplateMapsFromFile() {
        ArrayList<ServerTemplate> templates = null;
        try (FileReader reader = new FileReader(templatesLocation)) {
            templates = (ArrayList<ServerTemplate>)((XStreamMarshaller)templateMarshaller).getXstream().fromXML(reader);
        } catch (Throwable e) {
            logger.error("Unable to read server template maps from file",e);
        }
        if (templates != null && !templates.isEmpty()) {
            templates.forEach(template -> {
                templateKeyMap.put(template.getId(),new ServerTemplateKey(template.getId(),template.getName()));
                templateMap.put(template.getId(),template);
            });
        }
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
        if (templateKeyMap.isEmpty()) {
            loadTemplateMapsFromFile();
        }
        return new ArrayList<ServerTemplateKey>(templateKeyMap.values());
    }

    @Override
    public List<ServerTemplate> load() {
        if (templateKeyMap.isEmpty()) {
            loadTemplateMapsFromFile();
        }
        return new ArrayList<ServerTemplate>(templateMap.values());
    }

    @Override
    public ServerTemplate load(String identifier) {
        if (templateKeyMap.isEmpty()) {
            loadTemplateMapsFromFile();
        }
        return templateMap.get(identifier);
    }

    @Override
    public boolean exists(String identifier) {
        if (templateKeyMap.isEmpty()) {
            loadTemplateMapsFromFile();
        }
        return templateMap.containsKey(identifier);
    }

    @Override
    public ServerTemplate update(ServerTemplate serverTemplate) {
        ServerTemplate updated = null;
        synchronized (templateMap) {
            if (templateKeyMap.isEmpty()) {
                loadTemplateMapsFromFile();
            }
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
    
    public void reloadTemplateMaps() {
        this.clearTemplateMaps();
        loadTemplateMapsFromFile();
    }

    public void setTemplatesLocation(String templatesLocation) {
        this.templatesLocation = templatesLocation;
    }

    public String getTemplatesLocation() {
        return templatesLocation;
    }
}
