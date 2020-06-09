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

import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import org.kie.server.api.KieServerConstants;
import org.kie.server.api.model.KieContainerResource;
import org.kie.server.api.model.KieServerInfo;
import org.kie.server.controller.api.model.KieServerSetup;
import org.kie.server.services.api.StartupStrategy;
import org.kie.server.services.impl.storage.KieServerState;


public class InmutableContainerStartupStrategy implements StartupStrategy {

    @Override
    public void startup(KieServerImpl kieServer, ContainerManager containerManager, KieServerState currentState, AtomicBoolean kieServerActive) {
        KieServerInfo kieServerInfo = kieServer.getInfoInternal();
        KieServerSetup kieServerSetup = kieServer.getController().connect(kieServerInfo);

        Set<KieContainerResource> containers = kieServerSetup.getContainers();

        kieServer.addServerStatusMessage(kieServerInfo);
        if (Boolean.parseBoolean(currentState.getConfiguration().getConfigItemValue(KieServerConstants.CFG_SYNC_DEPLOYMENT, "false"))) {
            containerManager.installContainersSync(kieServer, containers, currentState, kieServerSetup);
        } else {
            containerManager.installContainers(kieServer, containers, currentState, kieServerSetup);
        }
    }

    @Override
    public String toString() {
        return "InmutableContainerStartupStrategy - deploys once during startup the containers selected by the controller";
    }
}