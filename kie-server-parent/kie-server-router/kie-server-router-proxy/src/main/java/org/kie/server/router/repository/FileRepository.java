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

import org.jboss.logging.Logger;
import org.kie.server.router.Configuration;
import org.kie.server.router.KieServerRouterEnvironment;
import org.kie.server.router.spi.ConfigRepository;

public class FileRepository implements ConfigRepository {

    private static final Logger log = Logger.getLogger(FileRepository.class);

    private final File repositoryDir; 
    private ConfigurationMarshaller marshaller = new ConfigurationMarshaller();


    public FileRepository(KieServerRouterEnvironment env) {
        this.repositoryDir = new File(env.getRepositoryDir());
    }

    @Override
    public synchronized void persist(Configuration configuration) {

        File configFile = new File(repositoryDir, "kie-server-router.json");
        try (FileOutputStream fos = new FileOutputStream(configFile)) {
            
            String config = marshaller.marshall(configuration);

            try (PrintWriter writer = new PrintWriter(fos)) {
                writer.write(config);
            }

            configFile.setLastModified(System.currentTimeMillis());

        } catch (Exception ex) {
            log.error("Could not persist configuration {0}", configFile, ex);
        }
    }

    @Override
    public Configuration load() {
        Configuration configuration = new Configuration();
        File serverStateFile = new File(repositoryDir, "kie-server-router.json");
        if (serverStateFile.exists()) {
            try (FileReader reader = new FileReader(serverStateFile)){
                configuration = marshaller.unmarshall(reader);
            } catch (Exception e) {
                log.error("configuration file could not be read {0}", serverStateFile, e);
            }
        }
        return configuration;
    }

    @Override
    public void clean() {
        persist(new Configuration());
    }

}
