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
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.kie.server.controller.api.model.spec.ServerTemplate;
import org.kie.server.controller.api.storage.KieServerTemplateStorage;
import org.kie.server.controller.api.model.spec.ServerTemplateKey;

public class InMemoryKieServerTemplateStorage implements KieServerTemplateStorage {

    private static InMemoryKieServerTemplateStorage INSTANCE = new InMemoryKieServerTemplateStorage();

    private Map<String, ServerTemplate> store = new ConcurrentHashMap<String, ServerTemplate>();
    private Map<String, org.kie.server.controller.api.model.spec.ServerTemplateKey> storeKeys = new ConcurrentHashMap<String, org.kie.server.controller.api.model.spec.ServerTemplateKey>();

    protected InMemoryKieServerTemplateStorage() {
    }

    public static InMemoryKieServerTemplateStorage getInstance() {
        return INSTANCE;
    }


    @Override
    public synchronized ServerTemplate store(ServerTemplate serverTemplate) {
        storeKeys.put(serverTemplate.getId(), new ServerTemplateKey(serverTemplate.getId(), serverTemplate.getName()));
        return store.put(serverTemplate.getId(), serverTemplate);
    }

    @Override
    public List<org.kie.server.controller.api.model.spec.ServerTemplateKey> loadKeys() {
        return new ArrayList<org.kie.server.controller.api.model.spec.ServerTemplateKey>(storeKeys.values());
    }

    @Override
    public List<ServerTemplate> load() {
        return new ArrayList<ServerTemplate>(store.values());
    }

    @Override
    public ServerTemplate load(String identifier) {
        return store.get(identifier);
    }

    @Override
    public boolean exists(String identifier) {
        return store.containsKey(identifier);
    }

    @Override
    public synchronized ServerTemplate update(ServerTemplate serverTemplate) {

        delete(serverTemplate.getId());
        store(serverTemplate);

        return serverTemplate;
    }

    @Override
    public synchronized ServerTemplate delete(String identifier) {
        storeKeys.remove(identifier);
        return store.remove(identifier);
    }

    public synchronized void clear() {
        this.store.clear();
        this.storeKeys.clear();
    }
}
