/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
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

package org.kie.server.services.api;

import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import org.kie.server.api.model.KieContainerResource;
import org.kie.server.services.impl.ContainerManager;
import org.kie.server.services.impl.KieServerImpl;
import org.kie.server.services.impl.storage.KieServerState;

public interface StartupStrategy {

    void startup(KieServerImpl kieServer, ContainerManager containerManager, KieServerState currentState, AtomicBoolean kieServerActive);
    
    default Set<KieContainerResource> prepareContainers(Set<KieContainerResource> containers) {
        // be default do nothing
        return containers;
    }
}
