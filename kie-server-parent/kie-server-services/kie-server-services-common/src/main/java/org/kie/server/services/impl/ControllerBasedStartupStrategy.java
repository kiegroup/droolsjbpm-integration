/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
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

package org.kie.server.services.impl;

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

/**
 * Default startup strategy that will favor controller configuration whenever is given and thus override any local configuration.
 * In case controller endpoint is not present it will fallback to what is known in the local server state file.
 */
public class ControllerBasedStartupStrategy implements StartupStrategy {

    private static final Logger logger = LoggerFactory.getLogger(ControllerBasedStartupStrategy.class);
    
    @Override
    public void startup(KieServerImpl kieServer, ContainerManager containerManager, KieServerState currentState, AtomicBoolean kieServerActive) {
        boolean readyToRun = false;
        KieServerController kieController = kieServer.getController();
        // try to load container information from available controllers if any...
        KieServerInfo kieServerInfo = kieServer.getInfoInternal();
        Set<KieContainerResource> containers = null;
        KieServerSetup kieServerSetup = null;
        try {
            kieServerSetup = kieController.connect(kieServerInfo);

            containers = kieServerSetup.getContainers();
            readyToRun = true;
        } catch (KieControllerNotDefinedException e) {
            // if no controllers use local storage
            containers = currentState.getContainers();
            kieServerSetup = new KieServerSetup();
            readyToRun = true;
        } catch (KieControllerNotConnectedException e) {
            // if controllers are defined but cannot be reached schedule connection and disable until it gets connection to one of them
            readyToRun = false;
            logger.warn("Unable to connect to any controllers, delaying container installation until connection can be established");
            Thread connectToControllerThread = new Thread(new ControllerConnectRunnable(kieServerActive,
                                                                                        kieController,
                                                                                        kieServerInfo,
                                                                                        currentState,
                                                                                        containerManager,
                                                                                        kieServer, 
                                                                                        this), "KieServer-ControllerConnect");
            connectToControllerThread.start();
            if (Boolean.parseBoolean(currentState.getConfiguration().getConfigItemValue(KieServerConstants.CFG_SYNC_DEPLOYMENT, "false"))) {
                logger.info("Containers were requested to be deployed synchronously, holding application start...");
                try {
                    connectToControllerThread.join();
                } catch (InterruptedException e1) {
                    logger.debug("Interrupt exception when waiting for deployments");
                }
            }
        }

        if (readyToRun) {
            kieServer.addServerStatusMessage(kieServerInfo);
            if (Boolean.parseBoolean(currentState.getConfiguration().getConfigItemValue(KieServerConstants.CFG_SYNC_DEPLOYMENT, "false"))) {
                containerManager.installContainersSync(kieServer, containers, currentState, kieServerSetup);
            } else {
                containerManager.installContainers(kieServer, containers, currentState, kieServerSetup);
            }
        }        
        
    }

    @Override
    public String toString() {
        return "ControllerBasedStartupStrategy - deploys kie containers given by controller ignoring locally defined";
    }

}
