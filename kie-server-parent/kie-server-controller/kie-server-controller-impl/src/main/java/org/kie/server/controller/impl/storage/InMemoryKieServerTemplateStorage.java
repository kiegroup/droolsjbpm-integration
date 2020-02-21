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

package org.kie.server.controller.impl.storage;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.kie.server.controller.api.model.runtime.ServerInstanceKey;
import org.kie.server.controller.api.model.spec.Capability;
import org.kie.server.controller.api.model.spec.ContainerSpec;
import org.kie.server.controller.api.model.spec.ServerTemplate;
import org.kie.server.controller.api.model.spec.ServerTemplateKey;
import org.kie.server.controller.api.storage.KieServerTemplateStorage;

public class InMemoryKieServerTemplateStorage implements KieServerTemplateStorage {

    private static InMemoryKieServerTemplateStorage INSTANCE = new InMemoryKieServerTemplateStorage();

    private Map<String, ServerTemplate> store = new HashMap<String, ServerTemplate>();
    private Map<String, org.kie.server.controller.api.model.spec.ServerTemplateKey> storeKeys = new ConcurrentHashMap<String, org.kie.server.controller.api.model.spec.ServerTemplateKey>();

    protected InMemoryKieServerTemplateStorage() {
    }

    public static InMemoryKieServerTemplateStorage getInstance() {
        return INSTANCE;
    }


    @Override
    public ServerTemplate store(ServerTemplate serverTemplate) {
        synchronized (store) {
            storeKeys.put(serverTemplate.getId(), new ServerTemplateKey(serverTemplate.getId(), serverTemplate.getName()));
            return store.put(serverTemplate.getId(), serverTemplate);
        }
    }

    @Override
    public List<org.kie.server.controller.api.model.spec.ServerTemplateKey> loadKeys() {
        return new ArrayList<org.kie.server.controller.api.model.spec.ServerTemplateKey>(storeKeys.values());
    }

    @Override
    public List<ServerTemplate> load() {
        synchronized (store) {
            return store.values().stream().map(this::cloneServerTemplate).collect(Collectors.toList());
        }
    }

    @Override
    public ServerTemplate load(String identifier) {
        synchronized (store) {
            return cloneServerTemplate(store.get(identifier));
        }
    }

    @Override
    public boolean exists(String identifier) {
        synchronized (store) {
            return store.containsKey(identifier);
        }
    }

    private ServerTemplate cloneServerTemplate(ServerTemplate current) {

        if (current == null) {
            return null;
        }

        List<ContainerSpec> specs = current.getContainersSpec().stream().map(ContainerSpec::new).collect(Collectors.toList());

        ServerTemplate serverTemplate = new ServerTemplate(current.getId(),
                                                           current.getName(),
                                                           new ArrayList<>(current.getCapabilities()),
                                                           current.getConfigs().isEmpty() ? new EnumMap<>(Capability.class) : new EnumMap<>(current.getConfigs()),
                                                           specs,
                                                           new ArrayList<ServerInstanceKey>(current.getServerInstanceKeys()));

        serverTemplate.setMode(current.getMode());

        return serverTemplate;

    }

    @Override
    public ServerTemplate update(ServerTemplate serverTemplate) {
        synchronized (store) {
            delete(serverTemplate.getId());
            store(serverTemplate);
        }
        return serverTemplate;
    }

    @Override
    public  ServerTemplate delete(String identifier) {
        synchronized (store) {
            storeKeys.remove(identifier);
            return store.remove(identifier);
        }
    }

    public  void clear() {
        synchronized (store) {
            this.store.clear();
            this.storeKeys.clear();
        }
    }
}
