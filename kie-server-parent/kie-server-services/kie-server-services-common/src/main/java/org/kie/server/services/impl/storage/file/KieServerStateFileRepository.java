/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
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

package org.kie.server.services.impl.storage.file;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.reflection.PureJavaReflectionProvider;
import org.kie.server.api.KieServerConstants;
import org.kie.server.api.model.KieContainerResource;
import org.kie.server.api.model.KieServerConfig;
import org.kie.server.api.model.KieServerConfigItem;
import org.kie.server.services.impl.storage.KieServerState;
import org.kie.server.services.impl.storage.KieServerStateRepository;

public class KieServerStateFileRepository implements KieServerStateRepository {

    private final File repositoryDir;

    private XStream xs = new XStream(new PureJavaReflectionProvider());

    private Map<String, KieServerState> knownStates = new ConcurrentHashMap<String, KieServerState>();

    public KieServerStateFileRepository(File repositoryDir) {
        this.repositoryDir = repositoryDir;
        xs.alias("kie-server-state", KieServerState.class);
        xs.alias("container", KieContainerResource.class);
        xs.alias("config-item", KieServerConfigItem.class);
    }

    public KieServerStateFileRepository() {
        this(new File(System.getProperty(KieServerConstants.KIE_SERVER_STATE_REPO, ".")));
    }

    public synchronized void store(String serverId, KieServerState kieServerState) {
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(new File(repositoryDir, serverId + ".xml"));

            xs.toXML(kieServerState, fos);

        } catch (IOException ex) {
//            logger.warn("Error when persisting known session id", ex);
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                }
            }
        }

        knownStates.put(serverId, kieServerState);
    }

    public KieServerState load(String serverId) {
        if (knownStates.containsKey(serverId)) {
            return knownStates.get(serverId);
        }

        synchronized (knownStates) {
            File serverStateFile = new File(repositoryDir, serverId + ".xml");
            KieServerState kieServerState = new KieServerState();

            if (serverStateFile.exists()) {
                kieServerState = (KieServerState) xs.fromXML(serverStateFile);
                // override controllers if given as system property
                String defaultController = System.getProperty(KieServerConstants.KIE_SERVER_CONTROLLER);
                if (defaultController != null) {
                    String[] controllerList = defaultController.split(",");
                    Set<String> controllers = new HashSet<String>();
                    for (String controller : controllerList) {
                        controllers.add(controller.trim());
                    }
                    kieServerState.setControllers(controllers);
                }

                populateWithSystemProperties(kieServerState.getConfiguration());
            } else {
                KieServerConfig config = new KieServerConfig();
                populateWithSystemProperties(config);
                kieServerState.setConfiguration(config);
            }
            knownStates.put(serverId, kieServerState);

            return kieServerState;
        }
    }

    protected void populateWithSystemProperties(KieServerConfig config) {
        // populate the config state with system properties that are valid to kie server
        Properties systemProperties = System.getProperties();
        for (String property : systemProperties.stringPropertyNames()) {

            if (property.startsWith("org.kie.server") || property.startsWith("org.kie.executor")) {
                KieServerConfigItem configItem = new KieServerConfigItem(property, systemProperties.getProperty(property), String.class.getName());
                config.addConfigItem(configItem);
            }
        }
    }

    public void clearCache() {
        this.knownStates.clear();
    }
}
