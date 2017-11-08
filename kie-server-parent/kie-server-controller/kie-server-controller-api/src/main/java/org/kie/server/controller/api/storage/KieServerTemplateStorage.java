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

package org.kie.server.controller.api.storage;

import java.util.List;

import org.kie.server.controller.api.model.spec.ServerTemplate;
import org.kie.server.controller.api.model.spec.ServerTemplateKey;

public interface KieServerTemplateStorage {
    /**
     * Store the ServerTemplate information
     * @param serverTemplate
     * @return
     */
    ServerTemplate store(ServerTemplate serverTemplate);

    /**
     * Load all known ServerTemplates
     * @return
     */
    List<ServerTemplateKey> loadKeys();

    /**
     * Load all known ServerTemplates
     * @return
     */
    List<ServerTemplate> load();

    /**
     * Load individual instance of the KieServer template
     * @param identifier
     * @return
     */
    ServerTemplate load(String identifier);

    /**
     * Checks if there is server template with given id stored
     * @param identifier
     * @return
     */
    boolean exists(String identifier);

    /**
     * Update individual ServerTemplate by overriding complete content of existing one
     * @param serverTemplate
     * @return
     */
    ServerTemplate update(ServerTemplate serverTemplate);

    /**
     * Removes given ServerTemplate from the storage
     * @param identifier
     * @return
     */
    ServerTemplate delete(String identifier);
    
    default void close() {
        
    };
}
