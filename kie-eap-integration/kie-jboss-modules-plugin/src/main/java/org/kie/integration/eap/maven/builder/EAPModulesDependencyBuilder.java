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
package org.kie.integration.eap.maven.builder;

import org.codehaus.plexus.component.annotations.Component;
import org.kie.integration.eap.maven.exception.EAPModulesDependencyBuilderException;
import org.kie.integration.eap.maven.model.layer.EAPLayer;
import org.kie.integration.eap.maven.util.EAPArtifactsHolder;
import org.eclipse.aether.graph.DependencyNode;

@Component( role = EAPModulesDependencyBuilder.class )
public interface EAPModulesDependencyBuilder {

    /**
     * Builds the dependencies between all modules in each layer.
     * Adds the module reference for each module artifact resource in the <code>artifactsHolder</code> instance.
     *
     * @param layer The module layer to perform dependency resolution. The module instances dependencies will be added.
     * @param dependencies The maven aether dependency graph resolved.
     * @param artifactsHolder The holder artifacts instance.
     *
     * @throws EAPModulesDependencyBuilderException Error during dependency resoultion.
     */
    void build(EAPLayer layer, DependencyNode dependencies, EAPArtifactsHolder artifactsHolder) throws EAPModulesDependencyBuilderException;

}
