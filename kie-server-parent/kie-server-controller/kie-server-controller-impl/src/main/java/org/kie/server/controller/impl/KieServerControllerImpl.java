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

package org.kie.server.controller.impl;

import java.util.HashSet;
import java.util.Set;

import org.kie.server.api.model.KieServerInfo;
import org.kie.server.controller.api.KieServerController;
import org.kie.server.controller.api.model.KieServerInstance;
import org.kie.server.controller.api.model.KieServerInstanceInfo;
import org.kie.server.controller.api.model.KieServerSetup;
import org.kie.server.controller.api.model.KieServerStatus;
import org.kie.server.controller.api.storage.KieServerControllerStorage;
import org.kie.server.controller.api.storage.KieServerStorageAware;
import org.kie.server.controller.impl.storage.InMemoryKieServerControllerStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public abstract class KieServerControllerImpl implements KieServerController, KieServerStorageAware {

    private static final Logger logger = LoggerFactory.getLogger(KieServerControllerImpl.class);

    private KieServerControllerStorage storage = InMemoryKieServerControllerStorage.getInstance();

    @Override
    public KieServerSetup connect(KieServerInfo serverInfo) {

        KieServerInstance kieServerInstance = storage.load(serverInfo.getServerId());

        if (kieServerInstance != null) {
            logger.debug("Server id {} know to the controller, checking if given server exists", serverInfo.getServerId());

            KieServerInstanceInfo instanceInfo = findByServerInfo(serverInfo, kieServerInstance);

            if (instanceInfo == null) {
                logger.debug("Server instance '{}' not yet registered", serverInfo.getLocation());
                instanceInfo = new KieServerInstanceInfo(serverInfo.getLocation(), KieServerStatus.UP, serverInfo.getCapabilities());
                kieServerInstance.getManagedInstances().add(instanceInfo);
            } else {
                logger.debug("Server instance {} already registered, changing its status to {}", serverInfo.getLocation(), KieServerStatus.UP);
                instanceInfo.setStatus(KieServerStatus.UP);
            }
            logger.debug("KieServerInstance updated after connect from server {}", serverInfo.getLocation());
            storage.update(kieServerInstance);


        } else {
            logger.debug("Server id {} unknown to this controller, registering...", serverInfo.getServerId());
            kieServerInstance = new KieServerInstance();
            kieServerInstance.setIdentifier(serverInfo.getServerId());
            kieServerInstance.setName(serverInfo.getName());
            kieServerInstance.setVersion(serverInfo.getVersion());
            kieServerInstance.setKieServerSetup(new KieServerSetup());
            kieServerInstance.setStatus(KieServerStatus.UP);
            kieServerInstance.setManagedInstances(new HashSet<KieServerInstanceInfo>());

            // add newly connected server instance
            KieServerInstanceInfo instanceInfo = new KieServerInstanceInfo(serverInfo.getLocation(), KieServerStatus.UP, serverInfo.getCapabilities());
            kieServerInstance.getManagedInstances().add(instanceInfo);

            logger.debug("KieServerInstance stored after connect (register) from server {}", serverInfo.getLocation());
            storage.store(kieServerInstance);
        }

        logger.info("Server {} connected to controller", serverInfo.getLocation());
        return kieServerInstance.getKieServerSetup();
    }

    @Override
    public void disconnect(KieServerInfo serverInfo) {
        KieServerInstance kieServerInstance = storage.load(serverInfo.getServerId());
        if (kieServerInstance != null) {
            logger.debug("Server id {} know to the controller, checking if given server exists", serverInfo.getServerId());

            KieServerInstanceInfo instanceInfo = findByServerInfo(serverInfo, kieServerInstance);

            if (instanceInfo != null) {
                logger.debug("Server instance {} already registered, changing its status to {}", serverInfo.getLocation(), KieServerStatus.DOWN);
                instanceInfo.setStatus(KieServerStatus.DOWN);

                logger.debug("KieServerInstance updated after disconnect from server {}", serverInfo.getLocation());
                storage.update(kieServerInstance);

                logger.info("Server {} disconnected from controller", serverInfo.getLocation());
            }
        }
    }

    @Override
    public void setStorage(KieServerControllerStorage storage) {
        this.storage = storage;
    }

    @Override
    public KieServerControllerStorage getStorage() {
        return this.storage;
    }

    // helper methods
    protected KieServerInstanceInfo findByServerInfo(KieServerInfo serverInfo, KieServerInstance kieServerInstance) {

        Set<KieServerInstanceInfo> managedInstances = kieServerInstance.getManagedInstances();
        if (managedInstances != null) {
            for (KieServerInstanceInfo managedInstance : managedInstances) {
                if (serverInfo.getLocation().equals(managedInstance.getLocation())) {
                    return managedInstance;
                }
            }
        }

        return null;
    }
}
