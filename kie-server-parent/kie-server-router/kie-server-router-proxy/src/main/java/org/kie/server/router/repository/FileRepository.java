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

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.PrintWriter;

import org.kie.server.router.Configuration;
import org.kie.server.router.KieServerRouterEnvironment;
import org.kie.server.router.spi.ConfigRepository;

public class FileRepository implements ConfigRepository {
    
    private final File repositoryDir; 
    private ConfigurationMarshaller marshaller = new ConfigurationMarshaller();
    
    private Configuration configuration;
    
    private ConfigFileWatcher watcher;
    private KieServerRouterEnvironment env;

    public FileRepository(KieServerRouterEnvironment env) {
        this.repositoryDir = new File(env.getRepositoryDir());
        this.env = env;
    }

    private KieServerRouterEnvironment environment() {
        return env;
    }

    @Override
    public void persist(Configuration configuration) {

        File configFile = new File(repositoryDir, "kie-server-router.json");
        try (FileOutputStream fos = new FileOutputStream(configFile)) {
            
            String config = marshaller.marshall(configuration);

            try (PrintWriter writer = new PrintWriter(fos)) {
                writer.write(config);
            }

            configFile.setLastModified(System.currentTimeMillis());

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public Configuration load() {
        this.configuration = new Configuration();
        File serverStateFile = new File(repositoryDir, "kie-server-router" + ".json");
        if (serverStateFile.exists()) {
            try (FileReader reader = new FileReader(serverStateFile)){
                
                this.configuration = marshaller.unmarshall(reader);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        // setup config file watcher to be updated when changes are discovered
        if (environment().isConfigFileWatcherEnabled() ) {
            this.watcher = new ConfigFileWatcher(environment(), serverStateFile.getParentFile().getAbsolutePath(), marshaller, configuration);
            Thread watcherThread = new Thread(watcher, "Kie Router Config Watch Thread");
            watcherThread.start();
        }
        return this.configuration;
    }

    @Override
    public void clean() {
        persist(new Configuration());
    }
    
    @Override
    public void close() {
        if (watcher != null) {
            watcher.stop();
        }
    }
}
