/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
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

package org.kie.server.controller.impl.service;

import java.util.Collection;

import org.kie.server.controller.api.KieServerControllerIllegalArgumentException;
import org.kie.server.controller.api.model.runtime.Container;
import org.kie.server.controller.api.model.runtime.ContainerList;
import org.kie.server.controller.api.model.runtime.ServerInstanceKey;
import org.kie.server.controller.api.model.runtime.ServerInstanceKeyList;
import org.kie.server.controller.api.model.spec.ContainerSpec;
import org.kie.server.controller.api.model.spec.ServerTemplate;
import org.kie.server.controller.api.service.RuntimeManagementService;
import org.kie.server.controller.api.storage.KieServerTemplateStorage;
import org.kie.server.controller.impl.KieServerInstanceManager;
import org.kie.server.controller.impl.storage.InMemoryKieServerTemplateStorage;

public class RuntimeManagementServiceImpl implements RuntimeManagementService {

    private KieServerTemplateStorage templateStorage = InMemoryKieServerTemplateStorage.getInstance();
    private KieServerInstanceManager kieServerInstanceManager = KieServerInstanceManager.getInstance();

    @Override
    public ServerInstanceKeyList getServerInstances(final String serverTemplateId) {
        ServerTemplate serverTemplate = getServerTemplate(serverTemplateId);
        return new ServerInstanceKeyList(serverTemplate.getServerInstanceKeys());
    }

    @Override
    public ContainerList getContainers(final ServerTemplate serverTemplate,
                                       final ContainerSpec containerSpec) {
        return getServerTemplateContainers(serverTemplate.getId(),
                             containerSpec.getId());
    }

    protected ContainerList getServerTemplateContainers(final String serverTemplateId,
                                                        final String containerSpecId) {
        final ServerTemplate serverTemplate = getServerTemplate(serverTemplateId);

        if (serverTemplate.hasContainerSpec(containerSpecId) == false) {
            throw new KieServerControllerIllegalArgumentException("Server template with id " + serverTemplateId + " has no container with id " + containerSpecId);
        }

        final ContainerSpec containerSpec = serverTemplate.getContainerSpec(containerSpecId);

        Collection<Container> containers = kieServerInstanceManager.getContainers(serverTemplate,
                                                                                  containerSpec);
        return new ContainerList(containers);
    }

    protected ServerTemplate getServerTemplate(final String serverTemplateId){
        final ServerTemplate serverTemplate = templateStorage.load(serverTemplateId);
        if (serverTemplate == null) {
            throw new KieServerControllerIllegalArgumentException("No server template found for id " + serverTemplateId);
        }
        return serverTemplate;
    }

    protected ContainerList getServerInstanceContainers(final String serverTemplateId,
                                                        final String serverInstanceId) {
        final ServerTemplate serverTemplate = getServerTemplate(serverTemplateId);
        if (serverTemplate.hasServerInstanceId(serverInstanceId) == false) {
            throw new KieServerControllerIllegalArgumentException("Server template with id " + serverTemplateId + " has no instance with id " + serverInstanceId);
        }

        final ServerInstanceKey serverInstanceKey = serverTemplate.getServerInstance(serverInstanceId);

        Collection<Container> containers = kieServerInstanceManager.getContainers(serverInstanceKey);
        return new ContainerList(containers);
    }

    @Override
    public ContainerList getContainers(ServerInstanceKey serverInstanceKey) {
        return getServerInstanceContainers(serverInstanceKey.getServerTemplateId(),
                                           serverInstanceKey.getServerInstanceId());
    }

    public KieServerTemplateStorage getTemplateStorage() {
        return templateStorage;
    }

    public void setTemplateStorage(KieServerTemplateStorage templateStorage) {
        this.templateStorage = templateStorage;
    }

    public KieServerInstanceManager getKieServerInstanceManager() {
        return kieServerInstanceManager;
    }

    public void setKieServerInstanceManager(KieServerInstanceManager kieServerInstanceManager) {
        this.kieServerInstanceManager = kieServerInstanceManager;
    }
}
