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

package org.kie.server.services.api;

import java.util.List;

/**
 * ContainerLocator allows to provide various strategies to find right container within kie server
 * to enable various extensions on top of containers, e.g.:
 * <ul>
 *     <li>Use latest container</li>
 *     <li>Container for given process instance</li>
 *     <li>Container for given task instance</li>
 * </ul>
 */
public interface ContainerLocator {

    /**
     * Locates container id for given alias based on strategy implemented concrete
     * representation of ContainerLocator interface
     * @param alias container alias
     * @return returns container id or null if not found
     */
    String locateContainer(String alias, List<? extends KieContainerInstance> containerInstances);
}
