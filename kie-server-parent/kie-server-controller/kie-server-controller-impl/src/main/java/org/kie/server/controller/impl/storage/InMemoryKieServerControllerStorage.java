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

package org.kie.server.controller.impl.storage;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.kie.server.controller.api.model.KieServerInstance;
import org.kie.server.controller.api.storage.KieServerControllerStorage;

public class InMemoryKieServerControllerStorage implements KieServerControllerStorage {

    private static InMemoryKieServerControllerStorage INSTANCE = new InMemoryKieServerControllerStorage();

    private Map<String, KieServerInstance> store = new ConcurrentHashMap<String, KieServerInstance>();

    private InMemoryKieServerControllerStorage() {
    }

    public static InMemoryKieServerControllerStorage getInstance() {
        return INSTANCE;
    }

    @Override
    public KieServerInstance store(KieServerInstance kieServerInstance) {
        this.store.put(kieServerInstance.getIdentifier(), kieServerInstance);

        return kieServerInstance;
    }

    @Override
    public List<KieServerInstance> load() {
        return new ArrayList<KieServerInstance>(this.store.values());
    }

    @Override
    public KieServerInstance load(String identifier) {
        return this.store.get(identifier);
    }

    @Override
    public KieServerInstance update(KieServerInstance kieServerInstance) {
        this.store.put(kieServerInstance.getIdentifier(), kieServerInstance);

        return kieServerInstance;
    }

    @Override
    public KieServerInstance delete(String identifier) {
        return this.store.remove(identifier);
    }
}
