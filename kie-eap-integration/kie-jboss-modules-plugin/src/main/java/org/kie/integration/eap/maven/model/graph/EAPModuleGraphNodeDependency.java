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
import org.kie.integration.eap.maven.model.dependency.EAPModuleDependency;

import java.util.Collection;

public interface EAPModuleGraphNodeDependency extends EAPModuleDependency, Comparable {

    /**
     * Is the dependency not resolved.
     * @return Is the dependency not resolved.
     */
    Boolean isMissing();

    /**
     * Dependency services behavior (import,export,none)
     * @return The services for this dependency.
     */
    String getServices();

    /**
     * Dependency meta-inf behavior (import,export,none)
     * @return The meta-inf for this dependency.
     */
    String getMetaInf();

    /**
     * The export paths for this dependency.
     * @return The export paths for this dependency.
     */
    Collection<PathFilter> getExports();

    /**
     * The import paths for this dependency.
     * @return The import paths for this dependency.
     */
    Collection<PathFilter> getImports();

}