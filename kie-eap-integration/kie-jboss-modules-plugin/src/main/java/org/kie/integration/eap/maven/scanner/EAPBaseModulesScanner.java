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

import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.plugin.logging.Log;
import org.codehaus.plexus.component.annotations.Component;
import org.kie.integration.eap.maven.exception.EAPModuleDefinitionException;
import org.kie.integration.eap.maven.exception.EAPModuleResourceDuplicationException;
import org.kie.integration.eap.maven.model.dependency.EAPStaticDistributionModuleDependency;
import org.kie.integration.eap.maven.model.dependency.EAPStaticModuleDependency;
import org.kie.integration.eap.maven.model.layer.EAPLayer;
import org.kie.integration.eap.maven.model.module.EAPBaseModule;
import org.kie.integration.eap.maven.model.module.EAPModule;
import org.kie.integration.eap.maven.model.resource.EAPArtifactResource;
import org.kie.integration.eap.maven.util.EAPArtifactUtils;
import org.kie.integration.eap.maven.util.EAPConstants;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.resolution.ArtifactResolutionException;

import java.util.Collection;
import java.util.Properties;

@Component( role = EAPModulesScanner.class, hint="base" )
public class EAPBaseModulesScanner extends EAPStaticModulesScanner {

    protected EAPModule createModuleInstance(Artifact artifact, String moduleName, String moduleLocation, String moduleSlot) {
        EAPBaseModule m = new EAPBaseModule(moduleName, moduleSlot);
        m.setLayer(layer);
        m.setArtifact(artifact);
        return m;
    }

    public String getModuleTypeSupported() {
        return EAPConstants.MODULE_TYPE_BASE;
    }

    protected void checkModuleProperties(Properties moduleProperties, String moduleArtifactCoordinates) throws EAPModuleDefinitionException {
        String moduleName = (String) moduleProperties.get(EAPConstants.MODULE_NAME);
        String moduleType = (String) moduleProperties.get(EAPConstants.MODULE_TYPE);
        String moduleSlot = (String) moduleProperties.get(EAPConstants.MODULE_SLOT);
        String moduleDependenciesRaw = (String) moduleProperties.get(EAPConstants.MODULE_DEPENDENCIES);

        if (moduleName == null || moduleName.trim().length() == 0)
            throw new EAPModuleDefinitionException(moduleArtifactCoordinates, "The module name is not set.");
        if (moduleType == null || moduleType.trim().length() == 0)
            throw new EAPModuleDefinitionException(moduleArtifactCoordinates, "The module type is not set.");
        if (moduleSlot == null || moduleSlot.trim().length() == 0)
            throw new EAPModuleDefinitionException(moduleArtifactCoordinates, "The module slot is not set.");
        if (!getModuleTypeSupported().equals(moduleType)) throw new EAPModuleDefinitionException(moduleArtifactCoordinates, "The module scanned is not supported by this scanner implementation '" + getClass().getName() + "' as it only supports type " + getModuleTypeSupported());

    }

    protected void addStaticDependencies(EAPModule module, String moduleArtifactCoordinates, String moduleDependenciesRaw, Collection<Artifact> exclusions) throws EAPModuleDefinitionException {
        // Base module do not have static dependencies.
    }

    protected EAPArtifactResource addResource(EAPModule module, Model moduleModel, Dependency moduleDependency) throws ArtifactResolutionException, EAPModuleResourceDuplicationException {
        String depGroupId = EAPArtifactUtils.getPropertyValue(moduleModel, moduleDependency.getGroupId());
        String depArtifactId = EAPArtifactUtils.getPropertyValue(moduleModel, moduleDependency.getArtifactId());
        String depVersion = EAPArtifactUtils.getPropertyValue(moduleModel, moduleDependency.getVersion());
        String depType = EAPArtifactUtils.getPropertyValue(moduleModel, moduleDependency.getType());
        String depClassifier = EAPArtifactUtils.getPropertyValue(moduleModel, moduleDependency.getClassifier());

        Artifact moduleResourceArtifact = EAPArtifactUtils.createArtifact(depGroupId, depArtifactId, depVersion, depType, depClassifier);
        EAPArtifactResource result = EAPArtifactResource.create(moduleResourceArtifact);

        artifactsHolder.add(moduleResourceArtifact, module);
        return result;
    }

    public void setLogger(Log logger) {
        this.logger = logger;
    }

    @Override
    public void setBaseModulesLayer(EAPLayer baseModulesLayer) {
        throw new UnsupportedOperationException("Base modules scanner does not support the base layer setter");
    }

    public boolean isScanStaticDependencies() {
        return false;
    }

    public void setScanStaticDependencies(boolean scanStaticDependencies) {
        throw new UnsupportedOperationException("Base modules definitions does not support static dependencies.");
    }

    @Override
    public void setDistributionStaticDependencies(Collection<EAPStaticDistributionModuleDependency> dependencies) {
        throw new UnsupportedOperationException("Base modules definitions does not support static dependencies.");
    }
}
