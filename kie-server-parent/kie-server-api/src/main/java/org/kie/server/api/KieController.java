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

package org.kie.server.api;

import java.util.Set;

import org.kie.server.api.model.KieContainerResource;

public interface KieController {

    /**
     * Returns list of <code>KieContainerResource</code> retrieved from one of the given controllers.
     * Possible outcomes:
     * <ul>
     *  <li>Non empty list in case sync was performed successfully and there are containers for given server id</li>
     *  <li>Empty list in case sync was performed successfully but there are no containers for given server id</li>
     *  <li>Null in case there is no controllers or not possible to connect to them</li>
     * </ul>
     * @param controllers
     * @param serverId
     * @return
     */
    Set<KieContainerResource> getContainers(Set<String> controllers, String serverId);
}
