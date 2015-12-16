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

package org.kie.server.services.impl;

import java.util.List;

public interface KieContainersRegistry {

    /**
     * Adds a KieContainerInstance to the registry if one does not exists yet.
     * @param containerId the id of the container
     * @param ci the container instance
     * @return null if it was added or the previous instance if one exists
     */
    KieContainerInstanceImpl addIfDoesntExist(String containerId, KieContainerInstanceImpl ci);

    /**
     * Returns the container instance for the given container id
     * @param containerId
     * @return
     */
    KieContainerInstanceImpl getContainer(String containerId);

    /**
     * Removes container instance from the registry
     * @param containerId
     * @return the instance or null if it did not exist
     */
    KieContainerInstanceImpl removeContainer(String containerId);

    /**
     * Returns a list of all instantiated containers
     * @return
     */
    List<KieContainerInstanceImpl> getContainers();


}
