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

package org.kie.server.controller.api.storage;

import java.util.List;

import org.kie.server.controller.api.model.KieServerInstance;

/**
 * Represents permanent storage for KieServerController so it can survive server restarts and still
 * maintain consistent information about managed KieServerInstance
 */
public interface KieServerControllerStorage {

    /**
     * Store the KieServerInstance information
     * @param kieServerInstance
     * @return
     */
    KieServerInstance store(KieServerInstance kieServerInstance);

    /**
     * Load all known KieServerInstances
     * @return
     */
    List<KieServerInstance> load();

    /**
     * Load individual instance of the KieServer
     * @param identifier
     * @return
     */
    KieServerInstance load(String identifier);

    /**
     * Update individual KieServiceInstance by overriding complete content of existing one
     * @param kieServerInstance
     * @return
     */
    KieServerInstance update(KieServerInstance kieServerInstance);

    /**
     * Removes given KieServerInstance from the storage
     * @param identifier
     * @return
     */
    KieServerInstance delete(String identifier);
}
