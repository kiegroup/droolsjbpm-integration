/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kie.server.controller.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

import org.kie.server.api.model.KieServerInfo;
import org.kie.server.controller.api.ModelFactory;
import org.kie.server.controller.api.model.runtime.ServerInstanceKey;
import org.kie.server.controller.api.model.spec.ServerTemplate;
import org.kie.server.controller.api.model.spec.ServerTemplateKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KieServerHealthCheckControllerImpl extends KieServerControllerImpl {

    private static final String PING_ALIVE_TIMEOUT = "org.kie.controller.ping.alive.timeout";

    private static final String PING_ALIVE_DISABLED = "org.kie.controller.ping.alive.disable";

    private static long PING_INTERVAL;

    private static boolean PING_DISABLED;

    private static final Logger logger = LoggerFactory.getLogger(KieServerHealthCheckControllerImpl.class);

    private Ping ping;

    private ExecutorService executorService;

    static {
        try {
            PING_INTERVAL = Long.parseLong(System.getProperty(PING_ALIVE_TIMEOUT, "5000"));
        } catch (NumberFormatException e) {
            logger.warn("The property " + PING_ALIVE_TIMEOUT + " is not a number; Fallback to 5000 ms ping");
            PING_INTERVAL = 5000L;
        }
        try {
            PING_DISABLED = Boolean.parseBoolean(System.getProperty(PING_ALIVE_DISABLED, "false"));
        } catch (NumberFormatException e) {
            logger.warn("The property " + PING_ALIVE_DISABLED + " is not true/false value; Fallback to false");
            PING_DISABLED = false;
        }
    }

    public void setExecutorService(ExecutorService executorService) {
        this.executorService = executorService;
    }

    class Ping implements Runnable {

        private AtomicBoolean stop = new AtomicBoolean(false);

        @Override
        public void run() {
            try {
                while (!stop.get()) {
                    for (KieServerInfo kieServerInfo : getServerInfoList()) {
                        ServerInstanceKey serverInstanceKey = ModelFactory.newServerInstanceKey(kieServerInfo.getServerId(), kieServerInfo.getLocation());
                        if (!KieServerInstanceManager.getInstance().isAlive(serverInstanceKey)) {
                            logger.debug("ping isAlive " + kieServerInfo.getLocation() + ": KO. disconnected.");
                            disconnect(kieServerInfo);
                        } else {
                            logger.debug("ping isAlive " + kieServerInfo.getLocation() + ": OK");
                        }
                    }
                    Thread.sleep(PING_INTERVAL);
                }
            } catch (InterruptedException e) {
                logger.warn("Rest Kie health check was interrupted");
            }

        }

        public void stop() {
            stop.set(true);
        }

    }

    public synchronized void start() {
        if (PING_DISABLED) {
            return;
        }
        logger.info("Starting is alive ping");
        ping = new Ping();
        executorService.execute(ping);

    }

    public synchronized void stop() {
        if (PING_DISABLED) {
            return;
        }
        logger.info("Stopping is alive ping");
        ping.stop();
        ping = null;
    }

    private List<KieServerInfo> getServerInfoList() {
        List<KieServerInfo> serversInfo = new ArrayList<>();
        List<ServerTemplateKey> templateKeys = getTemplateStorage().loadKeys();
        for (ServerTemplateKey templateKey : templateKeys) {
            ServerTemplate template = getTemplateStorage().load(templateKey.getId());
            for (ServerInstanceKey serverInstanceKey : template.getServerInstanceKeys()) {
                KieServerInfo serverInfo = new KieServerInfo(template.getId(), template.getName());
                serverInfo.setLocation(serverInstanceKey.getUrl());
                serversInfo.add(serverInfo);
            }
        }
        return serversInfo;
    }



}
