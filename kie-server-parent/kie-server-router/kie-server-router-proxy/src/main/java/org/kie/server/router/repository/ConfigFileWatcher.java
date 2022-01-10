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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.util.concurrent.atomic.AtomicBoolean;

import org.jboss.logging.Logger;
import org.kie.server.router.ConfigurationManager;
import org.kie.server.router.KieServerRouterEnvironment;

public class ConfigFileWatcher implements Runnable {
    
    private static final Logger log = Logger.getLogger(ConfigFileWatcher.class);

    private Path toWatch;
    private AtomicBoolean active = new AtomicBoolean(true);

    private ConfigurationManager configurationManager;
    
    private long lastUpdate = -1;

    private KieServerRouterEnvironment env;
    
    public ConfigFileWatcher(KieServerRouterEnvironment env, ConfigurationManager configuration) {
        this.env = env;
        this.configurationManager = configuration;

        this.toWatch = Paths.get(env.getRepositoryDir());
        if (!Files.isDirectory(this.toWatch)) {
            this.toWatch = Paths.get(env.getRepositoryDir()).getParent();
        }

        this.toWatch = Paths.get(toWatch.toString(), "kie-server-router.json");
        try {
            if(toWatch.toFile().exists()) {
                lastUpdate = Files.getLastModifiedTime(toWatch).toMillis();
            } else {
                log.warnv("configuration file does not exist {0} , creating...", this.toWatch);
                configuration.persist();
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
                        this.configurationManager.syncPersistent();
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