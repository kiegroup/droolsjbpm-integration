/*
 * Copyright 2014 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kie.integration.eap.maven.model.graph;

import org.kie.integration.eap.maven.model.common.PathFilter;
import org.kie.integration.eap.maven.model.resource.EAPModuleResource;

public interface EAPModuleGraphNodeResource extends EAPModuleResource, Comparable  {

    /**
     * If the resource should be added in module definition.
     * @return If the resource should be added in module definition.
     */
    boolean isAddAsResource();

    /**
     * The resource filter.
     *
     * @return The resource filter.
     */
    PathFilter getFilter();

}