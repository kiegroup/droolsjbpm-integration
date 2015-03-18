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
package org.kie.integration.eap.maven.model.layer;

import org.kie.integration.eap.maven.exception.EAPModulesDefinitionException;
import org.kie.integration.eap.maven.model.module.EAPModule;
import org.eclipse.aether.artifact.Artifact;

import java.util.Collection;
import java.util.Properties;

public interface EAPLayer {

    /**
     * The layer name.
     *
     * @return The layer name.
     */
    String getName();

    /**
     * The layer properties.
     *
     * @return The layer properties.
     */
    Properties getProperties();

    /**
     * The layer modules.
     * @return Return the modules collection.
     */
    Collection<EAPModule> getModules();

    /**
     * Add a module into the layer.
     *
     * @return Module added into the layer.
     * @throws org.kie.integration.eap.maven.exception.EAPModulesDefinitionException Error if module is already added in the layer.
     */
    EAPModule addModule(EAPModule module) throws EAPModulesDefinitionException;


    /**
     * Returns the layer's module.
     *
     * @param ModuleUID The module UID.
     * @return The module instance or null if not found.
     */
    EAPModule getModule(String ModuleUID);

    /**
     * Returns the layer's module.
     *
     * @param artifact The artifact that contains the module definition.
     * @return The module instance or null if not found.
     */
    EAPModule getModule(Artifact artifact);

}
