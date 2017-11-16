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

import org.kie.server.controller.api.model.runtime.Container;
import org.kie.server.controller.api.model.runtime.ServerInstanceKey;
import org.kie.server.controller.api.model.spec.ServerTemplate;
import org.kie.server.controller.api.service.RuntimeManagementService;
import org.kie.server.controller.api.storage.KieServerTemplateStorage;
import org.kie.server.controller.impl.KieServerInstanceManager;
import org.kie.server.controller.impl.storage.InMemoryKieServerTemplateStorage;

public class RuntimeManagementServiceImpl implements RuntimeManagementService {

    private KieServerTemplateStorage templateStorage = InMemoryKieServerTemplateStorage.getInstance();
    private KieServerInstanceManager kieServerInstanceManager = KieServerInstanceManager.getInstance();

    @Override
    public Collection<ServerInstanceKey> getServerInstances(String serverTemplateId) {
        ServerTemplate serverTemplate = templateStorage.load(serverTemplateId);
        if (serverTemplate == null) {
            throw new RuntimeException("No server template found for id " + serverTemplateId);
        }

        return serverTemplate.getServerInstanceKeys();
    }

    @Override
    public Collection<Container> getContainers(ServerInstanceKey serverInstanceKey) {

        Collection<Container> containers = kieServerInstanceManager.getContainers(serverInstanceKey);

        return containers;
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
