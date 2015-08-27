/*
 * Copyright 2015 JBoss Inc
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
import java.util.List;
import java.util.Set;

import org.kie.server.api.model.KieContainerResource;
import org.kie.server.api.model.KieContainerStatus;
import org.kie.server.api.model.KieServerInfo;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.controller.api.KieServerControllerAdmin;
import org.kie.server.controller.api.KieServerControllerException;
import org.kie.server.controller.api.model.KieServerInstance;
import org.kie.server.controller.api.model.KieServerInstanceInfo;
import org.kie.server.controller.api.model.KieServerSetup;
import org.kie.server.controller.api.model.KieServerStatus;
import org.kie.server.controller.api.storage.KieServerControllerStorage;
import org.kie.server.controller.api.storage.KieServerStorageAware;
import org.kie.server.controller.impl.storage.InMemoryKieServerControllerStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class KieServerControllerAdminImpl implements KieServerControllerAdmin, KieServerStorageAware {

    private static final Logger logger = LoggerFactory.getLogger(KieServerControllerAdminImpl.class);

    private KieServerControllerStorage storage = InMemoryKieServerControllerStorage.getInstance();

    @Override
    public KieServerInstance addKieServerInstance(KieServerInfo kieServerInfo) throws KieServerControllerException {
        KieServerInstance kieServerInstance = this.storage.load(kieServerInfo.getServerId());
        if (kieServerInstance != null) {
            throw new KieServerControllerException("KieServerInstance is already registered with id: " + kieServerInfo.getServerId());
        }
        kieServerInstance = new KieServerInstance();
        kieServerInstance.setIdentifier(kieServerInfo.getServerId());
        kieServerInstance.setVersion(kieServerInfo.getVersion());
        kieServerInstance.setName(kieServerInfo.getName());
        kieServerInstance.setKieServerSetup(new KieServerSetup());
        kieServerInstance.setStatus(KieServerStatus.UP);
        kieServerInstance.setManagedInstances(new HashSet<KieServerInstanceInfo>());

        if (kieServerInfo.getLocation() != null && !kieServerInfo.getLocation().isEmpty()) {
            KieServerInstanceInfo instanceInfo = new KieServerInstanceInfo(kieServerInfo.getLocation(), KieServerStatus.UP, kieServerInfo.getCapabilities());
            kieServerInstance.getManagedInstances().add(instanceInfo);
        }
        storage.store(kieServerInstance);
        return kieServerInstance;
    }

    @Override
    public KieServerInstance removeKieServerInstance(String identifier) throws KieServerControllerException {
        KieServerInstance kieServerInstance = this.storage.load(identifier);
        if (kieServerInstance == null) {
            throw new KieServerControllerException("KieServerInstance not found with id: " + identifier);
        }
        return storage.delete(identifier);
    }

    @Override
    public List<KieServerInstance> listKieServerInstances() {
        return storage.load();
    }

    @Override
    public KieServerInstance getKieServerInstance(String identifier) throws KieServerControllerException {
        KieServerInstance kieServerInstance = this.storage.load(identifier);
        if (kieServerInstance == null) {
            throw new KieServerControllerException("KieServerInstance not found with id: " + identifier);
        }
        return kieServerInstance;
    }

    @Override
    public KieContainerResource createContainer(String id, String containerId, KieContainerResource container) {

        KieServerInstance kieServerInstance = this.storage.load(id);
        if (kieServerInstance == null) {
            throw new KieServerControllerException("KieServerInstance not found with id: " + id);
        }

        Set<KieContainerResource> containers = kieServerInstance.getKieServerSetup().getContainers();
        if (containers == null) {
            containers = new HashSet<KieContainerResource>();
            kieServerInstance.getKieServerSetup().setContainers(containers);
        }
        containers.add(container);
        container.setStatus(KieContainerStatus.STOPPED);

        notifyKieServersOnCreateContainer(kieServerInstance, container);

        return container;
    }

    @Override
    public void startContainer(String id, String containerId) {

        KieServerInstance kieServerInstance = this.storage.load(id);
        if (kieServerInstance == null) {
            throw new KieServerControllerException("KieServerInstance not found with id: " + id);
        }

        KieContainerResource kieContainerResource = findKieContainer(kieServerInstance, containerId);
        if (kieContainerResource != null) {
            kieContainerResource.setStatus(KieContainerStatus.STARTED);

            this.storage.update(kieServerInstance);

            notifyKieServersOnDeleteContainer(kieServerInstance, containerId);
        }
    }

    @Override
    public void stopContainer(String id, String containerId) {
        KieServerInstance kieServerInstance = this.storage.load(id);
        if (kieServerInstance == null) {
            throw new KieServerControllerException("KieServerInstance not found with id: " + id);
        }

        KieContainerResource kieContainerResource = findKieContainer(kieServerInstance, containerId);
        if (kieContainerResource != null) {
            kieContainerResource.setStatus(KieContainerStatus.STOPPED);

            this.storage.update(kieServerInstance);

            notifyKieServersOnDeleteContainer(kieServerInstance, containerId);
        }

    }

    @Override
    public void deleteContainer(String id, String containerId) {
        KieServerInstance kieServerInstance = this.storage.load(id);
        if (kieServerInstance == null) {
            throw new KieServerControllerException("KieServerInstance not found with id: " + id);
        }

        Set<KieContainerResource> containers = kieServerInstance.getKieServerSetup().getContainers();
        if (containers != null) {
            KieContainerResource containerResourceToDel = null;
            for (KieContainerResource containerResource : containers) {
                 if (containerResource.getContainerId().equals(containerId)) {
                     containerResourceToDel = containerResource;
                     break;
                 }
            }

            // delete if found
            if (containerResourceToDel != null) {
                containers.remove(containerResourceToDel);
            }
        }
        notifyKieServersOnDeleteContainer(kieServerInstance, containerId);
    }

    @Override
    public KieContainerResource getContainer(String id, String containerId) {
        KieServerInstance kieServerInstance = this.storage.load(id);
        if (kieServerInstance == null) {
            throw new KieServerControllerException("KieServerInstance not found with id: " + id);
        }

        Set<KieContainerResource> containers = kieServerInstance.getKieServerSetup().getContainers();
        if (containers != null) {

            for (KieContainerResource containerResource : containers) {
                if (containerResource.getContainerId().equals(containerId)) {
                    return containerResource;
                }
            }
        }

        throw new KieServerControllerException("Container not found with id: " + containerId + " within kie server with id " + id);
    }

    @Override
    public void setStorage(KieServerControllerStorage storage) {
        this.storage = storage;
    }

    @Override
    public KieServerControllerStorage getStorage() {
        return this.storage;
    }

    public abstract void notifyKieServersOnCreateContainer(KieServerInstance kieServerInstance, KieContainerResource container);

    public abstract void notifyKieServersOnDeleteContainer(KieServerInstance kieServerInstance, String containerId);

    protected KieContainerResource findKieContainer(KieServerInstance kieServerInstance, String containerId) {


        Set<KieContainerResource> containers = kieServerInstance.getKieServerSetup().getContainers();
        if (containers != null) {

            for (KieContainerResource containerResource : containers) {
                if (containerResource.getContainerId().equals(containerId)) {

                    return containerResource;
                }
            }
        }

        return null;
    }
}
