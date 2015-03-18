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
import org.apache.maven.model.Exclusion;
import org.apache.maven.model.Model;
import org.apache.maven.plugin.logging.Log;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.kie.integration.eap.maven.exception.EAPModuleDefinitionException;
import org.kie.integration.eap.maven.exception.EAPModuleResourceDuplicationException;
import org.kie.integration.eap.maven.exception.EAPModulesDefinitionException;
import org.kie.integration.eap.maven.model.dependency.EAPModuleDependency;
import org.kie.integration.eap.maven.model.dependency.EAPStaticDistributionModuleDependency;
import org.kie.integration.eap.maven.model.dependency.EAPStaticModuleDependency;
import org.kie.integration.eap.maven.model.layer.EAPLayer;
import org.kie.integration.eap.maven.model.layer.EAPLayerImpl;
import org.kie.integration.eap.maven.model.module.EAPModule;
import org.kie.integration.eap.maven.model.module.EAPStaticModule;
import org.kie.integration.eap.maven.model.resource.EAPArtifactOptionalResource;
import org.kie.integration.eap.maven.model.resource.EAPArtifactResource;
import org.kie.integration.eap.maven.model.resource.EAPUnresolvableArtifactResource;
import org.kie.integration.eap.maven.model.resource.EAPVersionMismatchedArtifactResource;
import org.kie.integration.eap.maven.util.EAPArtifactUtils;
import org.kie.integration.eap.maven.util.EAPArtifactsHolder;
import org.kie.integration.eap.maven.util.EAPConstants;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.resolution.ArtifactResolutionException;

import java.io.IOException;
import java.util.*;

@Component( role = EAPModulesScanner.class, hint="static" )
public class EAPStaticModulesScanner implements EAPModulesScanner {

    protected EAPArtifactsHolder artifactsHolder;
    protected EAPLayer layer;
    private EAPLayer baseModulesLayer;
    protected Log logger;
    Collection<String> exclusions;
    Collection<EAPStaticDistributionModuleDependency> staticDistributionDependencies;

    // Other custom flags.
    protected boolean scanResources = true;
    protected boolean scanStaticDependencies = true;
    protected boolean artifactTreeResolved = true;

    @Override
    public EAPLayer scan(String layerName, Collection<Artifact> moduleArtifacts, Collection<Artifact> exclusions, EAPArtifactsHolder artifactsHolder) throws EAPModulesDefinitionException, EAPModuleDefinitionException {
        this.artifactsHolder = artifactsHolder;
        this.exclusions = new ArrayList<String>();

        if (moduleArtifacts != null && !moduleArtifacts.isEmpty()) {
            layer = new EAPLayerImpl(layerName);
            for (Artifact moduleArtifact : moduleArtifacts) {

                // Check module not exluded.
                if (!isModuleExcluded(moduleArtifact, exclusions)) {
                    EAPModule module = createModule(moduleArtifact, exclusions);
                    layer.addModule(module);
                } else {
                    try {
                        this.exclusions.add(createExclusion(moduleArtifact));
                    } catch (Exception e) {
                        throw new EAPModuleDefinitionException(EAPArtifactUtils.getArtifactCoordinates(moduleArtifact), "Cannot obtain pom for exclusion", e);
                    }
                }

            }

            // Clean static dependencies for excluded modules.
            if (scanStaticDependencies) cleanStaticDependencies(this.exclusions);
        }

        return layer;
    }

    private void cleanStaticDependencies(Collection<String> exclusions) {
        for (EAPModule module : layer.getModules()) {
            Collection<EAPModuleDependency> dependencies = module.getDependencies();
            if (dependencies != null && !dependencies.isEmpty()) {
                Iterator<EAPModuleDependency> dependencyIterator = dependencies.iterator();
                while (dependencyIterator.hasNext()) {
                    EAPModuleDependency dependency = dependencyIterator.next();
                    String depName = EAPArtifactUtils.getUID(dependency.getName(), dependency.getSlot());
                    if (layer.getModule(depName) == null && (baseModulesLayer == null || baseModulesLayer.getModule(depName) == null)) {
                        dependencyIterator.remove();
                    }
                    else if (exclusions != null && exclusions.contains(depName)) dependencyIterator.remove();
                }
            }
        }
    }

    protected String createExclusion(Artifact moduleArtifact) throws IOException, XmlPullParserException {
        Model moduleModel = EAPArtifactUtils.generateModel(moduleArtifact);
        String moduleName = EAPArtifactUtils.getPropertyValue(moduleModel, (String) moduleModel.getProperties().get(EAPConstants.MODULE_NAME));
        String moduleSlot = EAPArtifactUtils.getPropertyValue(moduleModel, (String) moduleModel.getProperties().get(EAPConstants.MODULE_SLOT));

        return EAPArtifactUtils.getUID(moduleName, moduleSlot);
    }


    protected EAPModule createModule(Artifact moduleArtifact, Collection<Artifact> exclusions) throws EAPModuleDefinitionException {
        EAPModule result = null;

        String moduleArtifactCoordinates = EAPArtifactUtils.getArtifactCoordinates(moduleArtifact);

        try {
            // Obtain module properties.
            Model moduleModel = EAPArtifactUtils.generateModel(moduleArtifact);
            String moduleName = EAPArtifactUtils.getPropertyValue(moduleModel, (String) moduleModel.getProperties().get(EAPConstants.MODULE_NAME));
            String moduleLocation = EAPArtifactUtils.getPropertyValue(moduleModel, (String) moduleModel.getProperties().get(EAPConstants.MODULE_LOCATION));
            String moduleType = EAPArtifactUtils.getPropertyValue(moduleModel, (String) moduleModel.getProperties().get(EAPConstants.MODULE_TYPE));
            String moduleSlot = EAPArtifactUtils.getPropertyValue(moduleModel, (String) moduleModel.getProperties().get(EAPConstants.MODULE_SLOT));
            String moduleDependenciesRaw = EAPArtifactUtils.getPropertyValue(moduleModel, (String) moduleModel.getProperties().get(EAPConstants.MODULE_DEPENDENCIES));
            
            // Check same module does not exist in base layer.
            if (moduleName != null && moduleSlot != null) {
                String moduleUID = EAPArtifactUtils.getUID(moduleName, moduleSlot);
                if (baseModulesLayer != null && baseModulesLayer.getModule(moduleUID) != null) throw new EAPModuleDefinitionException(moduleArtifactCoordinates, "The already module exist in JBoss EAP/AS base layer.");
            }

            // Check module properties.
            Properties moduleProperties = new Properties();
            if (moduleName != null) moduleProperties.put(EAPConstants.MODULE_NAME, moduleName);
            if (moduleLocation != null) moduleProperties.put(EAPConstants.MODULE_LOCATION, moduleLocation);
            if (moduleType != null) moduleProperties.put(EAPConstants.MODULE_TYPE, moduleType);
            if (moduleSlot != null) moduleProperties.put(EAPConstants.MODULE_SLOT, moduleSlot);
            if (moduleDependenciesRaw != null) moduleProperties.put(EAPConstants.MODULE_DEPENDENCIES, moduleDependenciesRaw);
            checkModuleProperties(moduleProperties, moduleArtifactCoordinates);
            
            // Find additional module properties.
            Properties moduleModelProperties = moduleModel.getProperties();
            moduleProperties = new Properties();
            Iterator<String> pNames = (Iterator<String>) moduleModelProperties.propertyNames();
            while (pNames.hasNext()) {
                String pName = pNames.next();
                if (pName != null && pName.startsWith(EAPConstants.MODULE_PROPERTY_PREFFIX)
                        && !pName.equalsIgnoreCase(EAPConstants.MODULE_NAME)
                        && !pName.equalsIgnoreCase(EAPConstants.MODULE_TYPE)
                        && !pName.equalsIgnoreCase(EAPConstants.MODULE_LOCATION)
                        && !pName.equalsIgnoreCase(EAPConstants.MODULE_SLOT)) {
                    String pValue = (String) moduleModelProperties.get(pName);
                    moduleProperties.put(pName, pValue);
                }
            }
            
            result = createModuleInstance(moduleArtifact, moduleName, moduleLocation, moduleSlot, moduleProperties);

            // Add the static module dependencies.
            if (scanStaticDependencies) {
                addStaticDependencies(result, moduleModel, moduleDependenciesRaw, exclusions);
                addStaticDistributionDependencies(result);
            }


            // Obtain module resources.
            if (scanResources) {
                List<Dependency> moduleDependencies = moduleModel.getDependencies();
                if (moduleDependencies != null && !moduleDependencies.isEmpty()) {
                    for (org.apache.maven.model.Dependency moduleDependency : moduleDependencies) {

                        // Resolve the module dependency in current project.
                        EAPArtifactResource resource = addResource(result, moduleModel, moduleDependency);
                        result.addResource(resource);
                    }
                }
            }

        } catch (ArtifactResolutionException e) {
            throw new EAPModuleDefinitionException(moduleArtifactCoordinates, "The artifact cannot be resolved.", e);
        } catch (XmlPullParserException e) {
            throw new EAPModuleDefinitionException(moduleArtifactCoordinates, "The artifact's pom cannot be pared.", e);
        } catch (IOException e) {
            throw new EAPModuleDefinitionException(moduleArtifactCoordinates, "The artifact's pom cannot be read.", e);
        } catch (EAPModuleResourceDuplicationException e) {
            throw new EAPModuleDefinitionException(moduleArtifactCoordinates, "Resource already present in other module.", e);
        }

        return result;
    }

    protected  void addStaticDistributionDependencies(EAPModule result) {
        if (staticDistributionDependencies != null && !staticDistributionDependencies.isEmpty()) {
            for (EAPStaticDistributionModuleDependency dependency : staticDistributionDependencies) {
                String moduleUID = dependency.getModuleUID();
                
                // Handle the special ALL case. ALL = Apply to all modules.
                boolean isForAllModules = EAPStaticDistributionModuleDependency.DISTRO_DEPEPDENCY_FOR_ALL_MODULES.equalsIgnoreCase(moduleUID) ? true : false;
                if ((isForAllModules || result.getUniqueId().equalsIgnoreCase(moduleUID)) && 
                        result.getDependency(EAPArtifactUtils.getUID(dependency.getName(), dependency.getSlot())) == null) {
                    result.addDependency(dependency);
                }
            }
        }
    }

    protected void checkModuleProperties(Properties moduleProperties, String moduleArtifactCoordinates) throws EAPModuleDefinitionException {
        String moduleName = (String) moduleProperties.get(EAPConstants.MODULE_NAME);
        String moduleLocation = (String) moduleProperties.get(EAPConstants.MODULE_LOCATION);
        String moduleType = (String) moduleProperties.get(EAPConstants.MODULE_TYPE);
        String moduleSlot = (String) moduleProperties.get(EAPConstants.MODULE_SLOT);
        String moduleDependenciesRaw = (String) moduleProperties.get(EAPConstants.MODULE_DEPENDENCIES);

        if (moduleName == null || moduleName.trim().length() == 0)
            throw new EAPModuleDefinitionException(moduleArtifactCoordinates, "The module name is not set.");
        if (moduleType == null || moduleType.trim().length() == 0)
            throw new EAPModuleDefinitionException(moduleArtifactCoordinates, "The module type is not set.");
        if (moduleSlot == null || moduleSlot.trim().length() == 0)
            throw new EAPModuleDefinitionException(moduleArtifactCoordinates, "The module slot is not set.");
        if (getModuleTypeSupported().equalsIgnoreCase(moduleType)) {
            // For static modules the location property is required.
            if (moduleLocation == null || moduleLocation.trim().length() == 0)
                throw new EAPModuleDefinitionException(moduleArtifactCoordinates, "The module location is not set.");
        } else {
            throw new EAPModuleDefinitionException(moduleArtifactCoordinates, "The module scanned is not supported by this scanner implementation '" + getClass().getName() + "' as it only supports type " + getModuleTypeSupported());
        }
    }

    public String getModuleTypeSupported() {
        return EAPConstants.MODULE_TYPE_STATIC;
    }

    protected EAPModule createModuleInstance(Artifact artifact, String moduleName, String moduleLocation, String moduleSlot, Properties moduleProperties) {
        EAPStaticModule m = new EAPStaticModule(moduleName, moduleLocation, moduleSlot, moduleProperties);
        m.setLayer(layer);
        m.setArtifact(artifact);
        return m;
    }

    protected boolean isModuleExcluded(Artifact module, Collection<Artifact> exclusions) {
        if (module == null) return true;

        if (exclusions != null && !exclusions.isEmpty()) {
            for (Artifact exclusion : exclusions) {
                if (EAPArtifactUtils.equals(exclusion, module)) return true;
            }
        }

        return false;
    }

    protected void addStaticDependencies(EAPModule module, Model moduleModel, String moduleDependenciesRaw, Collection<Artifact> exclusions) throws EAPModuleDefinitionException {
        Collection<EAPStaticModuleDependency> moduleStaticDependencies = EAPArtifactUtils.getStaticDependencies(module.getArtifact(), moduleModel, moduleDependenciesRaw);
        if (moduleStaticDependencies != null) {
            for (EAPStaticModuleDependency dep : moduleStaticDependencies) {
                module.addDependency(dep);
            }
        }
    }

    protected EAPArtifactResource addResource(EAPModule module, Model moduleModel, org.apache.maven.model.Dependency moduleDependency) throws ArtifactResolutionException, EAPModuleResourceDuplicationException {
        EAPArtifactResource result = null;
        String depGroupId = EAPArtifactUtils.getPropertyValue(moduleModel, moduleDependency.getGroupId());
        String depArtifactId = EAPArtifactUtils.getPropertyValue(moduleModel, moduleDependency.getArtifactId());
        String depVersion = EAPArtifactUtils.getPropertyValue(moduleModel, moduleDependency.getVersion());
        String depType = EAPArtifactUtils.getPropertyValue(moduleModel, moduleDependency.getType());
        String depClassifier = EAPArtifactUtils.getPropertyValue(moduleModel, moduleDependency.getClassifier());

        Artifact moduleResourceArtifact = EAPArtifactUtils.createArtifact(depGroupId, depArtifactId, depVersion, depType, depClassifier);

        if (artifactTreeResolved) {
            // Artifacts are already resolved in the holder.
            Artifact resolvedArtifact = artifactsHolder.getArtifact(moduleResourceArtifact);

            if (resolvedArtifact != null) {
                // This artifact is resolvable in current tree.
                Artifact resolved = artifactsHolder.resolveArtifact(resolvedArtifact);
                result = moduleDependency.isOptional() ? EAPArtifactOptionalResource.create(resolved) : EAPArtifactResource.create(resolved);
                artifactsHolder.setModule(resolvedArtifact, module);
            } else {
                // Same artifact coordinates have not been resolved in current project dependency tree.
                resolvedArtifact = artifactsHolder.contains(depGroupId, depArtifactId, depType);
                
                boolean isResourceVersionMissing = false;
                // Check if the dependency for module have no version defined.
                if (depVersion == null || depVersion.trim().length() == 0) isResourceVersionMissing = true;
                
                // Check if another version for artifact has been resolved.
                if (resolvedArtifact != null) {
                    // There exist another version resolved for this artifact. Resolve it in current project.
                    resolvedArtifact = artifactsHolder.resolveArtifact(resolvedArtifact);
                    
                    if (isResourceVersionMissing) {
                        result = EAPArtifactResource.create(resolvedArtifact);
                    } else {
                        if (logger != null)
                            logger.warn("The artifact " + moduleResourceArtifact.toString() + " is resolvable in current project but using another version: '" + resolvedArtifact.getVersion() + "'.");
                        result = EAPVersionMismatchedArtifactResource.create(resolvedArtifact, depVersion);
                    }
                    
                    // Add the resolved artifact with different version as module resource.
                    moduleResourceArtifact = resolvedArtifact;
                } else {
                    // Artifact is not resolvable in current project.
                    if (logger != null)
                        logger.warn("Artifact " + moduleResourceArtifact.toString() + " is not resolvable in current project. Will be not added as module resource.");
                    result = EAPUnresolvableArtifactResource.create(moduleResourceArtifact);
                }

                // The artifact is not resolvable in current artifacts holder because it's not resolvable in current project dependencies.
                // Add the dymmy created artifact instance.
                artifactsHolder.add(moduleResourceArtifact, module);
            }
        } else {
            // Artifacts are not yet resolved in the holder.
            Artifact resolvedArtifact = artifactsHolder.resolveArtifact(moduleResourceArtifact);
            result = moduleDependency.isOptional() ? EAPArtifactOptionalResource.create(resolvedArtifact) : EAPArtifactResource.create(resolvedArtifact);
            artifactsHolder.add(moduleResourceArtifact, module);
        }

        // Handle module resource exclusions.
        if (result != null) {
            List<Exclusion> exclusions = moduleDependency.getExclusions();
            if (exclusions != null && !exclusions.isEmpty()) {
                for (Exclusion exclusion : exclusions) {
                    if (logger != null) logger.info("Excluding [" + exclusion.getGroupId() + EAPConstants.ARTIFACT_SEPARATOR + exclusion.getArtifactId() + "] from module " + module.getUniqueId());
                    // Work with aether model.
                    org.eclipse.aether.graph.Exclusion e = new org.eclipse.aether.graph.Exclusion(exclusion.getGroupId(), exclusion.getArtifactId(), null, null);
                    result.addExclusion(e);
                }
            }
        }

        return result;
    }

    public void setLogger(Log logger) {
        this.logger = logger;
    }

    public boolean isScanResources() {
        return scanResources;
    }

    public void setScanResources(boolean scanResources) {
        this.scanResources = scanResources;
    }

    public boolean isArtifactTreeResolved() {
        return artifactTreeResolved;
    }

    public void setArtifactTreeResolved(boolean artifactTreeResolved) {
        this.artifactTreeResolved = artifactTreeResolved;
    }

    public void setBaseModulesLayer(EAPLayer baseModulesLayer) {
        this.baseModulesLayer = baseModulesLayer;
    }

    public boolean isScanStaticDependencies() {
        return scanStaticDependencies;
    }

    public void setScanStaticDependencies(boolean scanStaticDependencies) {
        this.scanStaticDependencies = scanStaticDependencies;
    }

    @Override
    public void setDistributionStaticDependencies(Collection<EAPStaticDistributionModuleDependency> dependencies) {
        this.staticDistributionDependencies = dependencies;
    }
}
