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
import java.io.IOException;
import java.io.PrintWriter;

import org.kie.server.router.Configuration;
import org.kie.server.router.KieServerRouterConstants;

public class FileRepository {
    
    private final File repositoryDir; 
    private ConfigurationMarshaller marshaller = new ConfigurationMarshaller();
    
    public FileRepository() {
        this(new File(System.getProperty(KieServerRouterConstants.ROUTER_REPOSITORY_DIR, ".")));
    }
    
    public FileRepository(File repositoryDir) {
        this.repositoryDir = repositoryDir;   
    }

    public void persist(Configuration configuration) {
                
        
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(new File(repositoryDir, "kie-server-router" + ".json"));
            
            String config = marshaller.marshall(configuration);
            
            PrintWriter writer = new PrintWriter(fos);
            writer.write(config);
            writer.close();

        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                }
            }
        }
    }
    
    public Configuration load() {
        Configuration configuration = new Configuration();
        File serverStateFile = new File(repositoryDir, "kie-server-router" + ".json");
        if (serverStateFile.exists()) {
            try (FileReader reader = new FileReader(serverStateFile)){
                
                configuration = marshaller.unmarshall(reader);
                return configuration;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        return null;
    }

    public void clean() {
        persist(new Configuration());
    }
}
