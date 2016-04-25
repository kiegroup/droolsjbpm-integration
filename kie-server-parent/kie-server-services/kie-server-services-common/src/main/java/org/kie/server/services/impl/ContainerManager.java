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

package org.kie.server.services.impl;

import java.util.Set;

import org.kie.server.api.KieServerEnvironment;
import org.kie.server.api.model.KieContainerResource;
import org.kie.server.api.model.KieContainerStatus;
import org.kie.server.controller.api.model.KieServerSetup;
import org.kie.server.services.impl.storage.KieServerState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ContainerManager {

    private static final Logger logger = LoggerFactory.getLogger(ContainerManager.class);

    public void installContainers(KieServerImpl kieServer, Set<KieContainerResource> containers, KieServerState currentState, KieServerSetup kieServerSetup) {
        installContainersSync(kieServer, containers, currentState, kieServerSetup);
    }

    public void installContainersSync(KieServerImpl kieServer, Set<KieContainerResource> containers, KieServerState currentState, KieServerSetup kieServerSetup) {
        logger.info("About to install containers '{}' on kie server '{}'", containers, kieServer);
        if (containers == null) {
            return;
        }
        for (KieContainerResource containerResource : containers) {
            if (KieContainerStatus.STARTED.equals(containerResource.getStatus())) {
                kieServer.createContainer(containerResource.getContainerId(), containerResource);
            }
        }
        currentState.setContainers(containers);
        if (kieServerSetup.getServerConfig() != null) {
            currentState.setConfiguration(kieServerSetup.getServerConfig());
        }
        kieServer.getServerRegistry().getStateRepository().store(KieServerEnvironment.getServerId(), currentState);
    }
}
