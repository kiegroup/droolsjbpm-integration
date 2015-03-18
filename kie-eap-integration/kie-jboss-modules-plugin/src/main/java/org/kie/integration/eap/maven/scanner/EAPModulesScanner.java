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
package org.kie.integration.eap.maven.scanner;

import org.codehaus.plexus.component.annotations.Component;
import org.kie.integration.eap.maven.exception.EAPModuleDefinitionException;
import org.kie.integration.eap.maven.exception.EAPModulesDefinitionException;
import org.kie.integration.eap.maven.model.dependency.EAPStaticDistributionModuleDependency;
import org.kie.integration.eap.maven.model.dependency.EAPStaticModuleDependency;
import org.kie.integration.eap.maven.model.layer.EAPLayer;
import org.kie.integration.eap.maven.util.EAPArtifactsHolder;
import org.eclipse.aether.artifact.Artifact;

import java.util.Collection;

@Component( role = EAPModulesScanner.class )
public interface EAPModulesScanner {

    /**
     * Scan the module descriptors and generate model module definitions for a single layer.
     *
     * @param layerName The name for the layer to scan.
     * @param moduleArtifacts The collection of pom module artifacts.
     * @param artifactsHolder The resolved artifacts for this project.
     *
     * @return The layer scanned.
     *
     * @throws org.kie.integration.eap.maven.exception.EAPModulesDefinitionException Exception in a single module loading.
     * @throws org.kie.integration.eap.maven.exception.EAPModuleDefinitionException Exception loading module definitions.
     */
    EAPLayer scan(String layerName, Collection<Artifact> moduleArtifacts, Collection<Artifact> exclusions, EAPArtifactsHolder artifactsHolder) throws EAPModulesDefinitionException, EAPModuleDefinitionException;

    /**
     * Returns the module type that supports this scanner implementation.
     *
     * @return The module type that supports this scanner implementation.
     */
    String getModuleTypeSupported();

    /**
     * Set it to scan module resources too.
     *
     */
    void setScanResources(boolean scanResources);

    /**
     * Set if static dependency properties must be scanned.
     *
     * @param scanStaticDependencies if static dependency properties must be scanned.
     */
    void setScanStaticDependencies(boolean scanStaticDependencies);

    /**
     * Set the static dependencies at distribution level.
     * @param dependencies The static dependencies for this distribution.
     */
    void setDistributionStaticDependencies(Collection<EAPStaticDistributionModuleDependency> dependencies);
}
