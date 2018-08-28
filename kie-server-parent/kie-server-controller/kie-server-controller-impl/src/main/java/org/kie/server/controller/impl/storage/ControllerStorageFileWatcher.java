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
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ControllerStorageFileWatcher implements Runnable {
    
    private static final Logger log = LoggerFactory.getLogger(ControllerStorageFileWatcher.class);
    public static final String STORAGE_FILE_WATCHER_INTERVAL = "org.kie.server.controller.templatefile.watcher.interval";
    
    private long sleepTime = Long.parseLong(System.getProperty(STORAGE_FILE_WATCHER_INTERVAL, "30000"));

    private Path toWatch;
    private AtomicBoolean active = new AtomicBoolean(true);
    private String templateFileName;
    
    private long lastUpdate = -1;
    
    private FileBasedKieServerTemplateStorage storage;
   
    public ControllerStorageFileWatcher(String configFilePath, FileBasedKieServerTemplateStorage storage) {
        this.toWatch = Paths.get(configFilePath);
        this.templateFileName = toWatch.toFile().getName();
        this.storage = storage;
        if (!Files.isDirectory(this.toWatch)) {
            this.toWatch = Paths.get(configFilePath).getParent();
        }
        this.toWatch = Paths.get(toWatch.toString(), templateFileName);
        try {
            lastUpdate = Files.getLastModifiedTime(toWatch).toMillis();
        } catch (IOException e) {
            log.error("Unable to read last modified date of controller template file", e);
        }
    }

    public void stop() {
        this.active.set(false);
    }
    
    @Override
    public void run() {
        try{
            while(active.get()) {
                
                FileTime lastModified = Files.getLastModifiedTime(toWatch);
                log.debug("Config file " + toWatch + " last modified " + lastModified);
                if (lastModified.toMillis() > lastUpdate) {
               
                    log.debug("Template file updated, reloading...");
                    try (FileReader reader = new FileReader(toWatch.toFile())){                                                                
                        this.storage.loadTemplateMapsFromFile();
                        log.info("Successfully reloaded server templates from file");
                    } catch (Exception e) {
                        log.error("Unexpected exception while reading updated template file :: " + e.getMessage(), e);
                    }
                    lastUpdate = lastModified.toMillis();
                }
                
                Thread.sleep(sleepTime);             
            }
        } catch (InterruptedException e) {
            log.debug("Interrupted exception received...");
        } catch (IOException e1) {
            log.warn("Unexpected exception while watching template file", e1);
        }
    }
}
