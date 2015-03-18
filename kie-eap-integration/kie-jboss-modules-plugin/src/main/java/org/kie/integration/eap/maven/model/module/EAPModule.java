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
package org.kie.integration.eap.maven.model.module;

import org.kie.integration.eap.maven.model.dependency.EAPModuleDependency;
import org.kie.integration.eap.maven.model.layer.EAPLayer;
import org.kie.integration.eap.maven.model.resource.EAPModuleResource;
import org.eclipse.aether.artifact.Artifact;

import java.util.Collection;
import java.util.Properties;

public interface EAPModule {

    /**
     * The module layer.
     *
     * @return The module layer.
     */
    EAPLayer getLayer();

    /**
     * The module properties.
     * @return The module properties.
     */
    Properties getProperties();

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
     * Returns the unique identifier for the module.
     * The UID is the concatenation of:
     * <code>#layer + #module_name + #module_slot</code>
     *
     * @return The UID for the module.
     */
    String getUniqueId();

    /**
     * The resources for the module.
     *
     * @return The resources for the module.
     */
    Collection<EAPModuleResource> getResources();

    /**
     * Adds a resource for the module.
     *
     * @param resource The resoruce to add.
     *
     * @return If the resource has been added.
     */
    boolean addResource(EAPModuleResource resource);

    /**
     * The module dependencies.
     *
     * @return The module dependencies.
     */
    Collection<EAPModuleDependency> getDependencies();

    /**
     * Adds a dependency for the module.
     *
     * @param dependency The dependency to add.
     *
     * @return If the dependency has been added.
     */
    boolean addDependency(EAPModuleDependency dependency);

    /**
     * Returns a dependency for a given name.
     *
     * @param uid The module UID of the dependency.
     * @return The dependency object.
     */
    EAPModuleDependency getDependency(String uid);

    /**
     * Returns a dependency instance for this kind of module.
     *
     * @return The new dependency object.
     */
    EAPModuleDependency createDependency();

    /**
     * Return the artifact that contains this module definition.
     *
     * @return The artifact that contains this module definition.
     */
    Artifact getArtifact();

}
