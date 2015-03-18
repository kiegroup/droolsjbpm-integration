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
package org.kie.integration.eap.maven.model.resource;

import org.eclipse.aether.graph.Exclusion;

import java.util.Collection;

public interface EAPModuleResource<T> {

    /**
     * The resource element.
     *
     * @return The resource element.
     */
    T getResource();

    /**
     * The resource name.
     * By default, if not set, the value used is the resource name.
     *
     * @return The resource name.
     */
    String getName();

    /**
     * The resource filename.
     * @return The resource filename.
     */
    String getFileName();

    /**
     * The exclusion for this resource module.
     * @return
     */
    Collection<Exclusion> getExclusions();

}
