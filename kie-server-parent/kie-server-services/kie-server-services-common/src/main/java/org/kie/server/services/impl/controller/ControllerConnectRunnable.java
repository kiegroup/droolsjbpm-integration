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

package org.kie.server.services.impl.controller;

import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import org.kie.server.api.KieServerConstants;
import org.kie.server.api.model.KieContainerResource;
import org.kie.server.api.model.KieServerInfo;
import org.kie.server.controller.api.KieServerController;
import org.kie.server.controller.api.model.KieServerSetup;
import org.kie.server.services.api.KieControllerNotConnectedException;
import org.kie.server.services.impl.ContainerManager;
import org.kie.server.services.impl.KieServerImpl;
import org.kie.server.services.impl.storage.KieServerState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ControllerConnectRunnable implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(ControllerConnectRunnable.class);
    private volatile AtomicBoolean kieServerActive;
    private KieServerController kieController;
    private KieServerInfo kieServerInfo;
    private KieServerState currentState;
    private ContainerManager containerManager;
    private KieServerImpl kieServer;

    public ControllerConnectRunnable(AtomicBoolean kieServerActive,
                                     KieServerController kieController,
                                     KieServerInfo kieServerInfo,
                                     KieServerState currentState,
                                     ContainerManager containerManager,
                                     KieServerImpl kieServer) {
        this.kieServerActive = kieServerActive;
        this.kieController = kieController;
        this.kieServerInfo = kieServerInfo;
        this.currentState = currentState;
        this.containerManager = containerManager;
        this.kieServer = kieServer;
    }

    @Override
    public void run() {

        while (kieServerActive.get()) {
            try {
                logger.debug("Attempting to connect to one of the controllers...");
                KieServerSetup kieServerSetup = kieController.connect(kieServerInfo);
                logger.debug("Connected to controller and retrieved setup details {}", kieServerSetup);
                Set<KieContainerResource> containers = kieServerSetup.getContainers();
                // add status message when connected
                kieServer.addServerStatusMessage(kieServerInfo);

                containerManager.installContainers(kieServer, containers, currentState, kieServerSetup);

                break;

            } catch (KieControllerNotConnectedException e) {
                long waitTime = Long.parseLong(System.getProperty(KieServerConstants.CFG_KIE_SERVER_CONTROLLER_CONNECT_INTERVAL, "10000"));
                logger.debug("Still cannot connect to any controllers, waiting for {} before next attempt", waitTime);
                try {
                    Thread.sleep(waitTime);
                } catch (InterruptedException e1) {
                    logger.warn("Controller connect thread got interrupted");
                }
            }
        }
        logger.info("Connected to controller, quiting connector thread");
    }
}
