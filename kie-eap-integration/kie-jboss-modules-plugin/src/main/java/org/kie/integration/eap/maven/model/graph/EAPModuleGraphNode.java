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
import org.eclipse.aether.artifact.Artifact;

import java.util.Collection;
import java.util.List;
import java.util.Properties;

public interface EAPModuleGraphNode extends Comparable {

    /**
     * The module name.
     *
     * @return The module name.
     */
    String getName();

    /**
     * The module location in JBoss EAP modules root folder.
     * @return The module location.
     */
    String getLocation();

    /**
     * The module slot in JBoss EAP modules root folder.
     * @return The module slot.
     */
    String getSlot();

    /**
     * The module properties.
     * @return The module properties.
     */
    Properties getProperties();

    /**
     * The module definition pom artifact.
     * @return module definition pom artifact.
     */
    Artifact getArtifact();

    /**
     * Obtain a unique ID for the module graph represented.
     * @return The unique ID.
     */
    String getUniqueId();

    /**
     * The module resources.
     * @return The module resources.
     */
    List<EAPModuleGraphNodeResource> getResources();

    /**
     * The module dependencies.
     * @return The module dependencies.
     */
    List<EAPModuleGraphNodeDependency> getDependencies();

    /**
     * The module export filters.
     *
     * @return The module export filters.
     */
    Collection<PathFilter> getExports();

    /**
     * Prints the module graph model as text output.
     * @return The printed module graph model.
     */
    String print();

}
