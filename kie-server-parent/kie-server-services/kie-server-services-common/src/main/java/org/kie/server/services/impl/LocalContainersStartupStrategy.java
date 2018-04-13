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
 * Startup strategy that will always deploy containers configured in the server state file regardless of
 * the controller configuration although when controller endpoint is given it will connect to it.
 */
public class LocalContainersStartupStrategy implements StartupStrategy {

    private static final Logger logger = LoggerFactory.getLogger(LocalContainersStartupStrategy.class);
    
    @Override
    public void startup(KieServerImpl kieServer, ContainerManager containerManager, KieServerState currentState, AtomicBoolean kieServerActive) {
        Set<KieContainerResource> containers = prepareContainers(currentState.getContainers()); 
        containerManager.installContainersSync(kieServer, containers, currentState, new KieServerSetup());
                
        KieServerController kieController = kieServer.getController();
        // try to load container information from available controllers if any...
        KieServerInfo kieServerInfo = kieServer.getInfoInternal();
                
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
                                                                                        new ContainerManager(){

                                                                                            @Override
                                                                                            public void installContainers(KieServerImpl kieServer,
                                                                                                                          Set<KieContainerResource> containers,
                                                                                                                          KieServerState currentState,
                                                                                                                          KieServerSetup kieServerSetup) {
                                                                                                // no-op containers are already deployed but allow it to be connected to controller
                                                                                            }

                                                                                            @Override
                                                                                            public void installContainersSync(KieServerImpl kieServer,
                                                                                                                              Set<KieContainerResource> containers,
                                                                                                                              KieServerState currentState,
                                                                                                                              KieServerSetup kieServerSetup) {
                                                                                                //no-op containers are already deployed but allow it to be connected to controller
                                                                                            }
                
                                                                                        },
                                                                                        kieServer, 
                                                                                        this), "KieServer-ControllerConnect");
            connectToControllerThread.start();

        }       
        
    }
    
    @Override
    public String toString() {
        return "LocalContainersStartupStrategy - deploys only locally (in server state file) kie containers, ignores kie containers given by controller";
    }

}
