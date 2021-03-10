/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
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

package org.kie.server.services.impl;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import org.kie.server.api.KieServerConstants;
import org.kie.server.api.model.KieContainerResource;
import org.kie.server.api.model.KieServerInfo;
import org.kie.server.controller.api.KieServerController;
import org.kie.server.controller.api.model.KieServerSetup;
import org.kie.server.services.api.KieControllerNotConnectedException;
import org.kie.server.services.api.KieControllerNotDefinedException;
import org.kie.server.services.api.StartupStrategy;
import org.kie.server.services.impl.controller.ControllerConnectRunnable;
import org.kie.server.services.impl.storage.KieServerState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ImmutableContainerStartupStrategy implements StartupStrategy {

    private static final Logger logger = LoggerFactory.getLogger(ImmutableContainerStartupStrategy.class);

    private Set<KieContainerResource> kieContainers;

    public ImmutableContainerStartupStrategy(List<KieContainerResource> kieContainer) {
        this.kieContainers = new HashSet<>(kieContainer);
    }

    @Override
    public void startup(KieServerImpl kieServer, ContainerManager containerManager, KieServerState currentState, AtomicBoolean kieServerActive) {
        KieServerInfo kieServerInfo = kieServer.getInfoInternal();
        KieServerSetup kieServerSetup = new KieServerSetup();
        kieServerSetup.setContainers(kieContainers);

        kieServer.addServerStatusMessage(kieServerInfo);
        if (Boolean.parseBoolean(currentState.getConfiguration().getConfigItemValue(KieServerConstants.CFG_SYNC_DEPLOYMENT, "false"))) {
            containerManager.installContainersSync(kieServer, kieContainers, currentState, kieServerSetup);
        } else {
            containerManager.installContainers(kieServer, kieContainers, currentState, kieServerSetup);
        }

        KieServerController kieController = kieServer.getController();
        try {
            kieController.connect(kieServerInfo);            
        } catch (KieControllerNotDefinedException e) {
            // no-op
        } catch (KieControllerNotConnectedException e) {            
            logger.warn("Unable to connect to any controllers, delaying container installation until connection can be established");
            Thread connectToControllerThread = new Thread(new ControllerConnectRunnable(kieServerActive,
                                                                                        kieController,
                                                                                        kieServerInfo,
                                                                                        currentState,
                                                                                        new DummyContainerManager(),
                                                                                        kieServer, 
                                                                                        this), "KieServer-ControllerConnect");
            connectToControllerThread.start();

        }
    }

    @Override
    public String toString() {
        return "InmutableContainerStartupStrategy - deploys once during startup the containers selected by the controller";
    }
}