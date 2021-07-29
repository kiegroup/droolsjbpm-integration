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

package org.kie.server.router.repository;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.util.concurrent.atomic.AtomicBoolean;

import org.jboss.logging.Logger;
import org.kie.server.router.Configuration;
import org.kie.server.router.KieServerRouterEnvironment;

public class ConfigFileWatcher implements Runnable {
    
    private static final Logger log = Logger.getLogger(ConfigFileWatcher.class);

    private Path toWatch;
    private AtomicBoolean active = new AtomicBoolean(true);
    
    private ConfigurationMarshaller marshaller;    
    private Configuration configuration;
    
    private long lastUpdate = -1;

    private KieServerRouterEnvironment env;
    
    public ConfigFileWatcher(KieServerRouterEnvironment env, String configFilePath, ConfigurationMarshaller marshaller, Configuration configuration) {
        this.env = env;
        this.marshaller = marshaller;
        this.configuration = configuration;
        this.toWatch = Paths.get(configFilePath);
        if (!Files.isDirectory(this.toWatch)) {
            this.toWatch = Paths.get(configFilePath).getParent();
        }

        this.toWatch = Paths.get(toWatch.toString(), "kie-server-router.json");
        try {
            if(toWatch.toFile().exists()) {
                lastUpdate = Files.getLastModifiedTime(toWatch).toMillis();
            } else {
                log.warnv("configuration file does not exist {0} , creating...", this.toWatch);
                String cfg = marshaller.marshall(configuration);
                File file = new File(this.toWatch.toString());
                if(file.createNewFile()){
                    try (FileOutputStream fos = new FileOutputStream(file); PrintWriter writer = new PrintWriter(fos)) {
                        writer.write(cfg);
                    }
                }
                FileTime lastModified = Files.getLastModifiedTime(toWatch);
                lastUpdate =  lastModified.toMillis();
            }
        } catch (IOException e) {
            log.error("Unable to read last modified date of routers config file", e);
        } catch (final Exception e) {
            log.error("Unable to writer config file", e);
        }
    }

    private KieServerRouterEnvironment environment() {
        return env;
    }

    public void stop() {
        this.active.set(false);
    }
    
    @Override
    public void run() {
        try{
            while(active.get()) {
                try {
                    if(!toWatch.toFile().exists()) {
                       log.warnv("configuration file does not exist {0} ", this.toWatch);
                       Thread.sleep(environment().getConfigFileWatcherInterval());
                       continue;
                    }
                    FileTime lastModified = Files.getLastModifiedTime(toWatch);
                    log.debug("Config file " + toWatch + " last modified " + lastModified);
                    if (lastModified.toMillis() > lastUpdate) {
                   
                        log.debug("Config file updated, reloading...");
                        try (FileReader reader = new FileReader(toWatch.toFile())){                                
                            Configuration updated = marshaller.unmarshall(reader);
                            this.configuration.reloadFrom(updated);
                        } catch (Exception e) {
                            log.error("Unexpected exception while reading updated configuration file :: " + e.getMessage(), e);
                        }
                        lastUpdate = lastModified.toMillis();
                    }
                } catch(IOException ioe) {
                    log.warn("Unexpected exception while watching config file, maybe file does not exist? ", ioe);
                }
                Thread.sleep(environment().getConfigFileWatcherInterval());
            }
        } catch (InterruptedException e) {
            log.debug("Interrupted exception received...");
        }
    }
}