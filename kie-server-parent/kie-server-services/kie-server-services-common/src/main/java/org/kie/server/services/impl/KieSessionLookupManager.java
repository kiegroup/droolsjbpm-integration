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

package org.kie.server.services.impl;

import java.util.HashSet;
import java.util.Set;

import org.kie.api.runtime.CommandExecutor;
import org.kie.server.services.api.KieContainerInstance;
import org.kie.server.services.api.KieServerRegistry;
import org.kie.server.services.api.KieSessionLookupHandler;

public class KieSessionLookupManager {

    private Set<KieSessionLookupHandler> handlers = new HashSet<KieSessionLookupHandler>();

    public void addHandler(KieSessionLookupHandler handler) {
        this.handlers.add(handler);
    }

    public void removeHandler(KieSessionLookupHandler handler) {
        this.handlers.remove(handler);
    }

    public CommandExecutor lookup(String kieSessionId, KieContainerInstance containerInstance, KieServerRegistry registry) {
        CommandExecutor commandExecutor = null;
        for (KieSessionLookupHandler handler : handlers) {
            commandExecutor = handler.lookupKieSession(kieSessionId, containerInstance, registry);

            if (commandExecutor != null) {
                break;
            }
        }

        return commandExecutor;
    }
}
