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
package org.kie.integration.eap.maven;

import org.apache.maven.model.Model;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.kie.integration.eap.maven.builder.EAPModulesDependencyBuilder;
import org.kie.integration.eap.maven.builder.EAPModulesGraphBuilder;
import org.kie.integration.eap.maven.configuration.EAPConfigurationArtifact;
import org.kie.integration.eap.maven.configuration.EAPConfigurationModuleDependency;
import org.kie.integration.eap.maven.distribution.EAPLayerDistributionManager;
import org.kie.integration.eap.maven.distribution.EAPStaticLayerDistribution;
import org.kie.integration.eap.maven.eap.EAPContainer;
import org.kie.integration.eap.maven.exception.EAPModuleDefinitionException;
import org.kie.integration.eap.maven.exception.EAPModulesDefinitionException;
import org.kie.integration.eap.maven.exception.EAPModulesDependencyBuilderException;
import org.kie.integration.eap.maven.model.dependency.EAPModuleDependency;
import org.kie.integration.eap.maven.model.dependency.EAPModuleMissingDependency;
import org.kie.integration.eap.maven.model.dependency.EAPStaticDistributionModuleDependency;
import org.kie.integration.eap.maven.model.dependency.EAPStaticModuleDependency;
import org.kie.integration.eap.maven.model.graph.EAPModulesGraph;
import org.kie.integration.eap.maven.model.layer.EAPLayer;
import org.kie.integration.eap.maven.model.module.EAPModule;
import org.kie.integration.eap.maven.model.resource.EAPModuleResource;
import org.kie.integration.eap.maven.model.resource.EAPUnresolvableArtifactResource;
import org.kie.integration.eap.maven.model.resource.EAPVersionMismatchedArtifactResource;
import org.kie.integration.eap.maven.scanner.EAPBaseModulesScanner;
import org.kie.integration.eap.maven.scanner.EAPModulesScanner;
import org.kie.integration.eap.maven.scanner.EAPStaticModulesScanner;
import org.kie.integration.eap.maven.util.EAPArtifactUtils;
import org.kie.integration.eap.maven.util.EAPArtifactsHolder;
import org.kie.integration.eap.maven.util.EAPConstants;
import org.kie.integration.eap.maven.util.EAPFileUtils;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.collection.DependencyCollectionException;
import org.eclipse.aether.graph.DependencyNode;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.ArtifactResolutionException;
import org.eclipse.aether.resolution.DependencyResolutionException;

import java.io.IOException;
import java.util.*;

public abstract class EAPBaseMojo extends AbstractMojo {

    /**
     * The Maven project.
     *
     * @parameter property="project"
     * @required
     * @readonly
     */
    protected MavenProject project;

    /**
     * The entry point to Aether, i.e. the component doing all the work.
     *
     * @component
     */
    protected RepositorySystem repoSystem;

    /**
     * The current repository/network configuration of Maven.
     *
     * @parameter default-value="${repositorySystemSession}"
     * @readonly
     */
    protected RepositorySystemSession repoSession;

    /**
     * The project's remote repositories to use for the resolution of plugins and their dependencies.
     *
     * @parameter default-value="${project.remoteProjectRepositories}"
     * @readonly
     */
    protected List<RemoteRepository> remoteRepos;

    /**
     * The name of the JBoss EAP layer distrubtion to geneate.
     *
     * @parameter default-value="unnamed"
     */
    protected String distributionName;

   /**
     * The maven module that contains all base EAP/AS modules for a given version.
     *
     * @parameter default-value=""
     */
    protected EAPConfigurationArtifact baseModule;

    /**
     * The static dependencies for this distribution modules.
     *
     * @parameter default-value=""
     */
    protected List<EAPConfigurationModuleDependency> staticDependencies;
    
    /**
     * The file to print the generated distribution graph.
     *
     * @parameter default-value=""
     */
    protected String graphOutputFile;

    /**
     * The flag that indicates if the build must fail when a dependency to a module resource is not satisfied.
     *
     * @parameter
     */
    protected Boolean failOnMissingDependency;

    /**
     * The flag that indicates if the build must fail when a module resource cannot be resolved in current project dependency tree.
     *
     * @parameter
     */
    protected Boolean failOnUnresolvableResource;

    /**
     * The flag that indicates if the build must fail when a module version for a resource is not resolvable in current project dependencies.
     *
     * @parameter
     */
    protected Boolean failOnVersionMismatchedResource;

    /**
     * The flag that indicates if the optional dependencies must be scanned in the current project dependency tree.
     *
     * @parameter
     */
    protected Boolean includeOptionalDependencies;

    /**
     * The scanner for static modules.
     * @component role-hint='static'
     */
    protected EAPModulesScanner staticModulesScanner;

    /**
     * The scanner for base modules.
     * @component role-hint='base'
     */
    protected EAPModulesScanner baseModulesScanner;

    /**
     * The scanner for base modules.
     * @component role-hint='default'
     */
    protected EAPModulesDependencyBuilder modulesDependencyBuilder;

    /**
     * The scanner for base modules.
     * @component role-hint='flat'
     */
    protected EAPModulesGraphBuilder modulesGraphBuilder;

    /**
     * The scanner for base modules.
     * @component role-hint='default'
     */
    protected EAPLayerDistributionManager distributionManager;

    /** Collection to store all resources and its modules. Usefull to check if a resource has been duplicated. **/
    protected transient EAPArtifactsHolder artifactsHolder;

    /**
     * The static module layer.
     */
    protected EAPLayer staticModulesLayer;

    /**
     * The base module layer.
     */
    protected EAPLayer baseModulesLayer;

    /**
     * The target container. 
     */
    protected EAPContainer container = null;

    /**
     * The modules distribution generated.
     */
    protected EAPStaticLayerDistribution distribution;

    protected EAPBaseMojo() {
        // Default values
        failOnMissingDependency = true;
        failOnUnresolvableResource = true;
        includeOptionalDependencies = false;
        failOnVersionMismatchedResource = false;
    }

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {


        // Check configuration parameters.
        checkConfiguration();

        // Init services.
        initServices();

        // Generate project's artifact.
        Artifact rootArtifact = EAPArtifactUtils.createProjectArtifact(project);

        DependencyNode dependencyNode = null;

        try {
            // Generate the dependency graph for the distribution.
            dependencyNode = EAPArtifactUtils.getDependencyGraph(rootArtifact, repoSystem, repoSession, remoteRepos, includeOptionalDependencies);
        } catch (DependencyCollectionException e) {
            throw new MojoExecutionException("Error generating dependency graph.", e);
        } catch (DependencyResolutionException e) {
            throw new MojoExecutionException("Error generating dependency graph.", e);
        }

        // Collect the artifacts contained in the dependency graph.
        artifactsHolder = collectArtifacts(dependencyNode);

        // Load the static module definitions
        try {

            // Load the base layer modules (EAP/AS modules)
            baseModulesLayer = loadBaseLayer();

            // Load the static layer modules.
            staticModulesLayer = loadStaticLayer();

        } catch (EAPModulesDefinitionException e) {
            throw new MojoExecutionException("Error obtaining modules.", e);
        } catch (EAPModuleDefinitionException e) {
            throw new MojoExecutionException("Error loading module.", e);
        }

        // Check resources.
        checkResources();

        try {
            // Generate the modules dependencies.
            generateModulesDependencies(dependencyNode);

            // If exist missing dependencies, check if the build must be failed.
            checkMissingDependencies();

        } catch (EAPModulesDependencyBuilderException e) {
            throw new MojoExecutionException("Error generating JBoss EAP modules dependencies.", e);
        }

        // Generates the graph.
        EAPModulesGraph graph = generateModulesGraph();

        // Generates the distribution.
        distribution = new EAPStaticLayerDistribution(distributionName, graph, container);
        distribution.setStaticLayer(staticModulesLayer);
        distribution.setBaseLayer(baseModulesLayer);
        distribution.setArtifactsHolder(artifactsHolder);
        distribution.setIncludedOptionalDependencies(includeOptionalDependencies);

        if (graphOutputFile != null && graphOutputFile.trim().length() > 0) {
            try {
                EAPFileUtils.writeToFile(distribution.print(), graphOutputFile);
            } catch (IOException e) {
                throw new MojoExecutionException("Error printing graph to file " + graphOutputFile, e);
            }
        }

    }

    protected void checkResources() throws MojoExecutionException {
        // If checks are not enabled, do nothing.
        if (!failOnUnresolvableResource && !failOnVersionMismatchedResource) return;
        
        Collection<Object[]> unresolvableResources = new LinkedHashSet<Object[]>();
        Collection<Object[]> versionMismatchResources = new LinkedHashSet<Object[]>();

        if (staticModulesLayer != null) {
            Collection<EAPModule>  modules = staticModulesLayer.getModules();
            if (modules != null && !modules.isEmpty()) {
                for (EAPModule module : modules) {
                    Collection<EAPModuleResource> resources = module.getResources();
                    if (resources != null && !resources.isEmpty()) {
                        for (EAPModuleResource resource : resources) {
                            
                            // Check if the resource if unresolvable.
                            try {
                                EAPUnresolvableArtifactResource missingResource = (EAPUnresolvableArtifactResource) resource;
                                Object [] missingRes = new Object[] {module, missingResource};
                                unresolvableResources.add(missingRes);
                            } catch (Exception e) {
                                // If not missing, continue.
                            }

                            // Check if the resource version mismatches.
                            try {
                                EAPVersionMismatchedArtifactResource missingResource = (EAPVersionMismatchedArtifactResource) resource;
                                Object [] missingRes = new Object[] {module, missingResource};
                                versionMismatchResources.add(missingRes);
                            } catch (Exception e) {
                                // If not version mismatching, continue.
                            }
                        }
                    }
                }

                if (!unresolvableResources.isEmpty()) {
                    getLog().info("********************* Unresolvable resources ******************************");
                    getLog().info("************* Module name <-> Unresolvable resource ***********************");
                    for (Object[] obj : unresolvableResources) {
                        EAPModule module = (EAPModule) obj[0];
                        EAPUnresolvableArtifactResource resource = (EAPUnresolvableArtifactResource) obj[1];
                        getLog().info(module.getUniqueId() + " <-> " + resource.getName());
                    }
                    getLog().info("*************************************************************************");

                    if (failOnUnresolvableResource) throw new MojoExecutionException("There are unresolved resources.");
                }

                if (!versionMismatchResources.isEmpty()) {
                    getLog().info("********************************* Version mismatch resources ******************************************");
                    getLog().info("************** Module name <-> Resource resolved <-> Version defined in module definition *************");
                    for (Object[] obj : versionMismatchResources) {
                        EAPModule module = (EAPModule) obj[0];
                        EAPVersionMismatchedArtifactResource resource = (EAPVersionMismatchedArtifactResource) obj[1];
                        getLog().info(module.getUniqueId() + " <-> " + resource.getName() + " <-> " + resource.getVersion());
                    }
                    getLog().info("********************************************************************************************************");

                    if (failOnVersionMismatchedResource) throw new MojoExecutionException("There are version mismatched resources.");
                }
            }
        }
    }

    protected void checkMissingDependencies() throws EAPModulesDependencyBuilderException {
        Collection<Object[]> missingDependencies = new LinkedHashSet<Object[]>();

        if (staticModulesLayer != null) {
            Collection<EAPModule>  modules = staticModulesLayer.getModules();
            if (modules != null && !modules.isEmpty()) {
                for (EAPModule module : modules) {
                    Collection<EAPModuleDependency> dependencies = module.getDependencies();
                    if (dependencies != null && !dependencies.isEmpty()) {
                        for (EAPModuleDependency dependency : dependencies) {
                            try {
                                EAPModuleMissingDependency missingDependency = (EAPModuleMissingDependency) dependency;
                                if (!missingDependency.isOptional()) {
                                    Object [] missingDep = new Object[] {module, dependency};
                                    missingDependencies.add(missingDep);
                                }
                            } catch (Exception e) {
                                // If not missing, continue.
                            }
                        }
                    }
                }

                if (!missingDependencies.isEmpty()) {
                    getLog().info("********************* Missing dependencies ******************************");
                    for (Object[] obj : missingDependencies) {
                        EAPModule module = (EAPModule) obj[0];
                        EAPModuleMissingDependency dependency = (EAPModuleMissingDependency) obj[1];
                        String dependencyCoords = EAPArtifactUtils.getArtifactCoordinates(dependency.getArtifact());
                        Collection<EAPModule> parents = dependency.getReferences();
                        StringBuilder parentsNames = new StringBuilder();
                        if (parents != null && !parents.isEmpty()) {
                            for (EAPModule parent : parents) {
                                parentsNames.append(parent.getUniqueId()).append(" ");
                            }
                        }
                        StringBuilder log = new StringBuilder();
                        log.append("Module/s [").append(parentsNames).append("] require [").append(dependencyCoords).append("]").append(" and this depedency is not present in any module. This artifact should be added or excluded");
                        if (!parentsNames.toString().trim().equalsIgnoreCase(module.getUniqueId())) {
                            log.append(" in module " + module.getUniqueId());
                        }

                        getLog().info(log.toString());
                    }
                    getLog().info("*************************************************************************");

                    if (failOnMissingDependency) throw new EAPModulesDependencyBuilderException("There are missing/unresolved dependencies.");
                }
            }
        }
    }


    protected void checkConfiguration() throws MojoFailureException {
        if (distributionName == null || distributionName.trim().length() == 0) throw new MojoFailureException("Distribution name configuration parameter cannot be null or empty.");
        if (baseModule == null) throw new MojoFailureException("Base module configuration parameter is not set.");
    }

    protected void initServices() {

        // Configure the module scanner for static modules.
        ((EAPStaticModulesScanner)staticModulesScanner).setLogger(getLog());

        // Configure the module scanner for base modules.
        ((EAPBaseModulesScanner)baseModulesScanner).setLogger(getLog());

    }

    /**
     * Retuns the modules excluded from the maven base layer module definition..
     * This artifact is defined in plugin configuration.
     *
     * @return The exclusions.
     */
    protected Collection<Artifact> getBaseLayerExclusions() {
        return baseModule.getExclusionArtifacts();
    }

    /**
     * Returns the artifact instance that contains all base modules definitions for a EAP/AS specific version.
     * This artifact is defined in plugin configuration.
     *
     * @return The base modules artifact.
     */
    protected Artifact getBaseModulesArtifact() {
        return baseModule.getArtifact();
    }
    
    protected Collection<EAPStaticDistributionModuleDependency> getStaticDistributionDependencies() throws EAPModuleDefinitionException {
        Collection<EAPStaticDistributionModuleDependency> result = null;
        if (staticDependencies != null && !staticDependencies.isEmpty()) {
            result = new LinkedList<EAPStaticDistributionModuleDependency>();
            for (EAPConfigurationModuleDependency dependency : staticDependencies) {
                String moduleDependenciesRaw = dependency.getDependencies();
                Collection<EAPStaticModuleDependency> _moduleStaticDependencies = EAPArtifactUtils.getStaticDependencies(null, project.getModel(), moduleDependenciesRaw);
                for (EAPStaticModuleDependency dep : _moduleStaticDependencies) {
                    EAPStaticDistributionModuleDependency newDep = new EAPStaticDistributionModuleDependency(EAPArtifactUtils.getUID(dependency.getName(), dependency.getSlot()),dep);
                    result.add(newDep);
                }
            }
        }
        
        return result;
    }

    protected EAPArtifactsHolder collectArtifacts(DependencyNode rootNode) {
        // Fill the project resolved artifacts cache.
        EAPArtifactsHolder holder = new EAPArtifactsHolder(repoSystem, repoSession, remoteRepos);
        EAPArtifactUtils.toArtifacts(holder, rootNode.getChildren(), null);
        return holder;
    }

    protected EAPLayer loadStaticLayer() throws EAPModulesDefinitionException, EAPModuleDefinitionException {
        Collection<Artifact> modules = null;
        Set<org.apache.maven.artifact.Artifact> artifacts = project.getDependencyArtifacts();

        if (artifacts != null) {
            modules = new LinkedList<Artifact>();
            for (org.apache.maven.artifact.Artifact artifact : artifacts) {
                if (EAPConstants.POM.equalsIgnoreCase(artifact.getType())) {
                    Artifact resolved = null;
                    try {
                        resolved = EAPArtifactUtils.resolveArtifact(artifact.getGroupId(), artifact.getArtifactId(), artifact.getVersion(), artifact.getType(), artifact.getClassifier(), repoSystem, repoSession, remoteRepos);
                    } catch (ArtifactResolutionException e) {
                        throw new EAPModulesDefinitionException("Artifact for module cannot be resolved.", e);
                    }
                    modules.add(resolved);
                }
            }
        }
        if (modules == null || modules.isEmpty()) throw new EAPModulesDefinitionException("No static module definitions found in project dependencies.");

        ((EAPStaticModulesScanner)staticModulesScanner).setBaseModulesLayer(baseModulesLayer);

        // Static distribution dependencies.
        Collection<EAPStaticDistributionModuleDependency> staticDistroDeps = getStaticDistributionDependencies();
        staticModulesScanner.setDistributionStaticDependencies(staticDistroDeps);

        return staticModulesScanner.scan(distributionName, modules, null, artifactsHolder);
    }

    protected EAPLayer loadBaseLayer() throws EAPModulesDefinitionException, EAPModuleDefinitionException {
        Artifact baseModulesArtifact = getBaseModulesArtifact();
        EAPLayer result = null;
        Collection<Artifact> modules = null;
        // Obtain the maven module that contains all static module definitions.
        String modulesArtifactCoordinates = EAPArtifactUtils.getArtifactCoordinates(baseModulesArtifact);

        getLog().info("Loading module definitions from " + modulesArtifactCoordinates);
        String globalModuleName = null;
        try {
            Artifact modulesArtifact = artifactsHolder.resolveArtifact(baseModulesArtifact);

            Model modulesArtifactModel = EAPArtifactUtils.generateModel(modulesArtifact);
            globalModuleName = EAPArtifactUtils.getPropertyValue(modulesArtifactModel, (String) modulesArtifactModel.getProperties().get(EAPConstants.MODULE_NAME));
            if (globalModuleName == null || globalModuleName.trim().length() == 0) throw new EAPModulesDefinitionException("No module name property found for " + modulesArtifactCoordinates);

            container = new EAPContainer(globalModuleName);
            
            List<String> modulesList = modulesArtifactModel.getModules();

            if (modulesList == null || modulesList.isEmpty()) throw new EAPModulesDefinitionException("No modules found for " + modulesArtifactCoordinates);
            getLog().info("Found " + modulesList.size() + " module definitions to load");

            modules = new LinkedList<Artifact>();

            for (String moduleName : modulesList) {

                // Resolve the module definition artifact.
                Artifact moduleArtifact = artifactsHolder.resolveArtifact(modulesArtifact.getGroupId(), moduleName,
                        modulesArtifact.getVersion(), EAPConstants.POM);

                modules.add(moduleArtifact);
            }

        } catch (ArtifactResolutionException e) {
            throw new EAPModulesDefinitionException("Modules artifact cannot be resolved." , e);
        } catch (IOException e) {
            throw new EAPModulesDefinitionException("Modules artifact pom cannot be read.." , e);
        } catch (XmlPullParserException e) {
            throw new EAPModulesDefinitionException("Modules artifact pom cannot be parsed." , e);
        }

        if (modules != null) {
            result = baseModulesScanner.scan(globalModuleName, modules, getBaseLayerExclusions(), artifactsHolder);
        }

        getLog().info("Module definitions loaded successfully");
        return result;
    }

    /**
     * Given the root dependency node for the maven artifacts,
     * runs the modules dependencies resolution.
     *
     */
    protected void generateModulesDependencies(DependencyNode rootNode) throws EAPModulesDependencyBuilderException {
        modulesDependencyBuilder.build(staticModulesLayer, rootNode, artifactsHolder);
    }

    protected EAPModulesGraph generateModulesGraph() {
        return modulesGraphBuilder.build(distributionName, staticModulesLayer);
    }

    protected Collection<EAPLayer> getAllLayers() {
        Collection<EAPLayer> allLayers = new ArrayList<EAPLayer>(2);
        allLayers.add(staticModulesLayer);
        allLayers.add(baseModulesLayer);
        return allLayers;
    }

}
