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

import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.kie.integration.eap.maven.configuration.EAPConfigurationArtifact;
import org.kie.integration.eap.maven.distribution.EAPLayerDistributionManager;
import org.kie.integration.eap.maven.distribution.EAPStaticLayerDistribution;
import org.kie.integration.eap.maven.exception.EAPModuleDefinitionException;
import org.kie.integration.eap.maven.exception.EAPModuleResourceDuplicationException;
import org.kie.integration.eap.maven.exception.EAPModulesDefinitionException;
import org.kie.integration.eap.maven.model.dependency.EAPModuleDependency;
import org.kie.integration.eap.maven.model.dependency.EAPStaticModuleDependency;
import org.kie.integration.eap.maven.model.graph.EAPModuleGraphNode;
import org.kie.integration.eap.maven.model.graph.EAPModuleGraphNodeDependency;
import org.kie.integration.eap.maven.model.graph.EAPModuleGraphNodeResource;
import org.kie.integration.eap.maven.model.graph.distribution.EAPModuleNodeGraphDependency;
import org.kie.integration.eap.maven.model.module.EAPDynamicModule;
import org.kie.integration.eap.maven.model.resource.EAPModuleResource;
import org.kie.integration.eap.maven.patch.EAPDynamicModulesPatch;
import org.kie.integration.eap.maven.patch.EAPPatch;
import org.kie.integration.eap.maven.patch.EAPPatchException;
import org.kie.integration.eap.maven.patch.EAPPatchManager;
import org.kie.integration.eap.maven.template.EAPTemplateBuilder;
import org.kie.integration.eap.maven.template.assembly.EAPAssemblyTemplate;
import org.kie.integration.eap.maven.template.assembly.EAPAssemblyTemplateFile;
import org.kie.integration.eap.maven.util.*;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.ArtifactResolutionException;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * This plugin mojo generates a dynamic module (webapp) definition and the assembly files to assemble it.
 *
 * @goal build-dynamic-modules
 * @requiresProject true
 */
public class EAPDynamicModulesBuilderMojo extends AbstractMojo {

    private static final String JBOSS_DEP_STRUCTURE_NAME = "jboss-deployment-structure";
    private static final String JBOSS_ALL_NAME = "jboss-all";
    private static final String EXTENSION_XML = ".xml";
    private static final String JBOSS_DEP_STRUCTURE_ZIP_ENTRY_NAME = "WEB-INF/jboss-deployment-structure.xml";
    private static final String ASSEMBLY_DESCRIPTOR_NAME = "-assembly.xml";
    private static final String EXCLUSIONS_PATH = "WEB-INF/lib/";
    private static final String JBOSS_ALL_DEPENDENCY_NAME_PREFFIX = "jboss-all-";
    private static final String DISTRO_XML_ENTRY_PATH = new StringBuilder(EAPConstants.DISTRIBUTION_PROPERTIES_PACKAGE.
            replaceAll("\\.","/")).append("/").append(EAPConstants.DISTRO_PACKAGE_FILE_NAME).toString();

    /** The path where modules will be deployed in EAP. Corresponds to modules/system/layers. **/
    private static final String ASSEMBLY_OUTPUT_PATH = new StringBuilder("modules").append(File.separator).
            append("system").append(File.separator).append("layers").toString();

    private static Pattern PATTERN_WAR_LIBRARIES = Pattern.compile("WEB-INF/lib/(.*).jar");
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
     * The name of the dynamic module distribution to generate.
     *
     * @parameter default-value=""
     */
    protected String distributionName;

    /**
     * The output path for the generated artifacts and assembly files.
     * The resulting assembly.xml file will be created inside this path.
     *
     * @parameter default-value=""
     */
    protected String outputPath;

    /**
     * The output formats for assembly descriptor. Use comma-separated values.
     *
     * @parameter default-value="dir,war"
     */
    protected String assemblyFormats;

    /**
     * The static layer artifact than contains all the modules and properties for the layer.
     *
     * @parameter default-value=""
     */
    protected EAPConfigurationArtifact staticLayerArtifact;

    /**
     * The scanner for static modules.
     * @component role-hint='velocity'
     */
    private EAPTemplateBuilder templateBuilder;

    /**
     * The scanner for static modules.
     * @component role-hint='default'
     */
    protected EAPLayerDistributionManager distributionManager;

    /**
     * The patch manager.
     * @component 
     */
    protected EAPPatchManager patchManager;

    // Class members.
    private Collection<Artifact> dynamicModuleArtifacts = null;
    private Collection<EAPDynamicModule> dynamicModules;
    private EAPStaticLayerDistribution staticLayerDistribution;
    private EAPArtifactsHolder artifactsHolder;
    private Map<Artifact, EAPModuleGraphNode> staticModulesGraphArtifacts;
    private String distroOutputPath = null;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {

        // Check configuration parameters.
        checkConfiguration();

        initServices();

        distroOutputPath = new StringBuilder(outputPath).append(File.separator).append("dynamic-modules").
                append(File.separator).append(distributionName).toString();

        try {
            // Obtain the dynamic module definitions dependencies present in project.
            dynamicModuleArtifacts = scanPomDependencies();
            if (dynamicModuleArtifacts == null || dynamicModuleArtifacts.isEmpty()) throw new EAPModulesDefinitionException("No dynamic modules found in project dependency artifacts.");
            getLog().info("Found " + dynamicModuleArtifacts.size() + " POM dependency artifacts.");
           
            // Obtain the static layer descriptor artifact.
            Artifact moduleResolvedArtifact = artifactsHolder.resolveArtifact(staticLayerArtifact.getArtifact());
            staticLayerDistribution = distributionManager.read(getStaticDistributionXMLAsString(moduleResolvedArtifact));
            
            // Load the dynamic modules.
            dynamicModules = new ArrayList<EAPDynamicModule>();

            // Create the model.
            for (Artifact dynamicModuleArtifact : dynamicModuleArtifacts) {
                EAPDynamicModule dynamicModule = scanDynamicModule(dynamicModuleArtifact);
                dynamicModules.add(dynamicModule);
            }

            // Fill the artifact-modulegraph holder Map.
            staticModulesGraphArtifacts = new LinkedHashMap<Artifact, EAPModuleGraphNode>(staticLayerDistribution.getGraph().getNodes().size());
            List<EAPModuleGraphNode> staticModules = staticLayerDistribution.getGraph().getNodes();
            for (EAPModuleGraphNode staticModule : staticModules) {
                fixDynamicModuleDependency(staticModule);
                staticModulesGraphArtifacts.put(staticModule.getArtifact(), staticModule);
            }

        } catch (ArtifactResolutionException e) {
            throw new MojoExecutionException("Cannot resolve the dependency. ", e);
        } catch (EAPModulesDefinitionException e) {
            throw new MojoExecutionException("Cannot resolve module definitions. ", e);
        } catch (EAPModuleDefinitionException e) {
            throw new MojoExecutionException("cannot resolve a module definition. ", e);
        } catch (EAPModuleResourceDuplicationException e) {
            throw new MojoExecutionException("Resource is duplicated. ", e);
        } catch (Exception e) {
            throw new MojoExecutionException("Cannot read static layer distribution.", e);
        }

        // Initialize patches.
        String patchOutputPath = new StringBuilder(distroOutputPath).append(File.separator).append(EAPConstants.PATCHES_PATH).toString();
        patchManager.initDynamic(staticLayerDistribution.getContainer(), patchOutputPath, artifactsHolder, staticLayerDistribution);

        // Execute patches.
        try {
            patchManager.executeDynamic();
        } catch (EAPPatchException e) {
            throw new MojoExecutionException("Problem executing a dynamic module patch.", e);
        }

        // Genrate the jboss-deployment-structure and assembly files for each dynamic module.
        for (EAPDynamicModule dynamicModule : dynamicModules) {

            // IF the current WAR contains a jboss deployment structure descriptor, read its dependencies.
            Collection<EAPModuleNodeGraphDependency> staticModuleDependencies = new LinkedList<EAPModuleNodeGraphDependency>();
            Collection<String> staticModuleResourceNames = new LinkedHashSet<String>();

            Collection<EAPModuleDependency> _dependencies = dynamicModule.getDependencies();
            if (_dependencies != null && !_dependencies.isEmpty()) {
                for (EAPModuleDependency dependency : _dependencies) {
                    EAPModuleNodeGraphDependency dep = new EAPModuleNodeGraphDependency(dependency.getName(), dependency.getSlot(), false);
                    staticModuleDependencies.add(dep);

                    EAPModuleGraphNode node = staticModulesGraphArtifacts.get(((EAPStaticModuleDependency)dependency).getArtifacts().iterator().next());
                    
                    List<EAPModuleGraphNodeResource> resources = node.getResources();
                    if (resources != null && !resources.isEmpty()) {
                        for (EAPModuleResource resource : resources) {
                            staticModuleResourceNames.add(EXCLUSIONS_PATH + resource.getFileName());
                        }
                    }
                }
            }

            // Obtain the war file and generate jboss-deployment-structure description.
            ZipFile war = null;
            EAPWarResources warResources = null;
            String jbossDeploymentStructure = null;
            String jbossAll = null;
            String warArtifactCoordinates = EAPArtifactUtils.getArtifactCoordinates(dynamicModule.getWarFile());
            try {
                war = getWarFile(dynamicModule);
                warResources = scanWarResources(war);
                jbossDeploymentStructure = generateJbossDeploymentStructure(staticModuleDependencies, war, warResources.getJbossDeploymentStructure());
            } catch (IOException e) {
                throw new MojoExecutionException("Cannot obtain WAR dependency file or cannot access its content.", e);
            } catch (ArtifactResolutionException e) {
                throw new MojoExecutionException("Cannot resolve WAR dependency.", e);
            }
            
            // Generate the jboss-all descriptor is the dynamic module depends on another one.
            if (dynamicModule.isAddJbossAll()) {
                String jbossAllPropertyName = new StringBuilder(JBOSS_ALL_DEPENDENCY_NAME_PREFFIX).append(dynamicModule.getName()).toString();
                jbossAll = templateBuilder.buildDynamicModuleDependency(jbossAllPropertyName);
            }

            // Generate the assembly file.
            try {
                // Generate the war assembly exclusions list.
                Collection<String> exclusions = generateWarExclusions(staticModuleResourceNames, warResources.getWarLibs());
                exclusions.add(JBOSS_DEP_STRUCTURE_ZIP_ENTRY_NAME);

                //Generate the assembly descriptor content.
                String assembly = generateAssemblyDescriptor(dynamicModule, warArtifactCoordinates,jbossDeploymentStructure, jbossAll, exclusions);

                // Write the generated assembly descriptor.
                File out = EAPFileUtils.writeFile(new File(distroOutputPath),dynamicModule.getArtifact().getArtifactId() + ASSEMBLY_DESCRIPTOR_NAME, assembly);

                getLog().info("Assembly file generated into " + out.getAbsolutePath());
                getLog().info("Dynamic distribution generated sucessfully.");
            } catch (IOException e) {
                throw new MojoExecutionException("Exception generating the assembly file. ", e);
            }
        }

    }
    
    private String getStaticDistributionXMLAsString(Artifact moduleResolvedArtifact) throws MojoExecutionException {
        String result = null;
        try {
            ZipFile zipFile = new ZipFile(moduleResolvedArtifact.getFile(), ZipFile.OPEN_READ);
            for (Enumeration e = zipFile.entries(); e.hasMoreElements();) {
                ZipEntry entry = (ZipEntry) e.nextElement();

                if (entry.getName().equalsIgnoreCase(DISTRO_XML_ENTRY_PATH)) {
                    InputStream in = zipFile.getInputStream(entry);
                    result = EAPFileUtils.getStringFromInputStream(in);
                }
            }
        } catch (Exception e) {
            throw new MojoExecutionException("Cannot read the distribution descriptor file.", e);
        }
        
        return result;
    } 

    private Collection<String> generateWarExclusions(Collection<String> staticModuleResourceNames, Collection<String> warLibs) {
        if (warLibs == null || warLibs.isEmpty()) return null;
        if (staticModuleResourceNames == null || staticModuleResourceNames.isEmpty()) return Collections.emptyList();

        Map<String, String> staticResourceCoords = new HashMap<String, String>(staticModuleResourceNames.size());
        Map<String, String> warResourceArtId_fileName = new HashMap<String, String>(warLibs.size());

        for (String resourceName : staticModuleResourceNames) {
            // Lenght of "WEb-INF/lib/" is 12.
            String[] coords = EAPArtifactUtils.parseFileName(resourceName.substring(12, resourceName.length()));
            staticResourceCoords.put(coords[0], coords[1]);
        }

        Map<String, String> warResourceCoords = new HashMap<String, String>(warLibs.size());
        for (String resourceName : warLibs) {
            // Lenght of "WEb-INF/lib/" is 12.
            String[] coords = EAPArtifactUtils.parseFileName(resourceName.substring(12, resourceName.length()));
            warResourceCoords.put(coords[0], coords[1]);
            warResourceArtId_fileName.put(coords[0], resourceName);
        }

        Collection<String> exclusions = new LinkedList<String>();
        for (Map.Entry<String, String> warResourceCoordsEntry : warResourceCoords.entrySet()) {
            String warResourceArtId = warResourceCoordsEntry.getKey();
            String warResourceVersion = warResourceCoordsEntry.getValue();

            String staticResourceVersion = staticResourceCoords.get(warResourceArtId);
            if (staticResourceVersion != null) {

                if (warResourceVersion != null && !warResourceVersion.equalsIgnoreCase(staticResourceVersion)) {
                    getLog().warn("Excluded " + warResourceArtId + ":" + warResourceVersion + " from war but the version defined in static module is " + staticResourceVersion);
                }
                exclusions.add(warResourceArtId_fileName.get(warResourceArtId));
            }
        }
        return exclusions;
    }

    private void fixDynamicModuleDependency(EAPModuleGraphNode module) {
        for (EAPDynamicModule dynModule : dynamicModules) {
            Collection<EAPModuleDependency>  dependencies = dynModule.getDependencies();
            if (dependencies != null && !dependencies.isEmpty()) {
                for (EAPModuleDependency dependency : dependencies) {
                    EAPStaticModuleDependency staticModuleDependency = (EAPStaticModuleDependency) dependency;
                    if (!staticModuleDependency.getArtifacts().isEmpty()) {
                        Artifact moduleArtifact = staticModuleDependency.getArtifacts().iterator().next();
                        if (EAPArtifactUtils.equals(module.getArtifact(), moduleArtifact)) {
                            staticModuleDependency.setName(module.getName());
                            staticModuleDependency.setSlot(module.getSlot());
                        }
                    }
                }
            }
        }
    }

    private EAPDynamicModule scanDynamicModule(Artifact artifact) throws EAPModuleDefinitionException{
        if (artifact == null) return  null;

        String moduleArtifactCoordinates = EAPArtifactUtils.getArtifactCoordinates(artifact);

        try {
            Model moduleModel = EAPArtifactUtils.generateModel(artifact);
            String moduleName = EAPArtifactUtils.getPropertyValue(moduleModel, (String) moduleModel.getProperties().get(EAPConstants.MODULE_NAME));
            String moduleType = EAPArtifactUtils.getPropertyValue(moduleModel, (String) moduleModel.getProperties().get(EAPConstants.MODULE_TYPE));
            String moduleDependenciesRaw = EAPArtifactUtils.getPropertyValue(moduleModel, (String) moduleModel.getProperties().get(EAPConstants.MODULE_DEPENDENCIES));
            String module_addJbossAll = EAPArtifactUtils.getPropertyValue(moduleModel, (String) moduleModel.getProperties().get(EAPConstants.MODULE_ADD_JBOSS_ALL));

            // Obtain module properties.
            if (moduleName == null || moduleName.trim().length() == 0)
                throw new EAPModuleDefinitionException(moduleArtifactCoordinates, "The module name is not set.");
            if (moduleType == null || moduleType.trim().length() == 0)
                throw new EAPModuleDefinitionException(moduleArtifactCoordinates, "The module type is not set.");
            if (!moduleType.equalsIgnoreCase(EAPConstants.MODULE_TYPE_DYNAMIC))
                throw new EAPModuleDefinitionException(moduleArtifactCoordinates, "The module is not dynamic.");

            EAPDynamicModule result = new EAPDynamicModule(moduleName);
            result.setArtifact(artifact);
            if (module_addJbossAll != null && module_addJbossAll.trim().length() > 0) {
                // For this dynamic module, add the jboss-all.xml descriptor.
                // Check if the property is available in current project.
                String jbossAllPropertyName = new StringBuilder(JBOSS_ALL_DEPENDENCY_NAME_PREFFIX).append(moduleName).toString();
                String jbossAllPropertyValue = (String) project.getProperties().get(jbossAllPropertyName);
                if (jbossAllPropertyValue == null || jbossAllPropertyValue.trim().length() == 0) throw new EAPModuleDefinitionException(moduleName, "This module contains the jboss-all.xml descriptor " +
                        "but no project property named '" + jbossAllPropertyName+ "' is found. Please, set this property in current project using the name of the dependant webapp file as the value.");
                
                result.setAddJbossAll(Boolean.valueOf(module_addJbossAll));
            }
            
            // Obtain module resources.
            List<Dependency> moduleDependencies = moduleModel.getDependencies();
            if (moduleDependencies != null && !moduleDependencies.isEmpty()) {
                for (org.apache.maven.model.Dependency moduleDependency : moduleDependencies) {

                    String artifactId = EAPArtifactUtils.getPropertyValue(moduleModel, moduleDependency.getArtifactId());;
                    String groupId = EAPArtifactUtils.getPropertyValue(moduleModel, moduleDependency.getGroupId());
                    String version = EAPArtifactUtils.getPropertyValue(moduleModel, moduleDependency.getVersion());
                    String type = EAPArtifactUtils.getPropertyValue(moduleModel, moduleDependency.getType());
                    String classifier = EAPArtifactUtils.getPropertyValue(moduleModel, moduleDependency.getClassifier());

                    Artifact artifact1 = EAPArtifactUtils.createArtifact(groupId, artifactId, version, type, classifier);
                    if (moduleDependency.getType().equalsIgnoreCase(EAPConstants.WAR)) result.setWarFile(artifact1);
                }
            }
            
            // Module dependencies.
            Collection<EAPStaticModuleDependency> moduleStaticDependencies = EAPArtifactUtils.getStaticDependencies(artifact, moduleModel, moduleDependenciesRaw);
            if (moduleStaticDependencies != null) {
                // If module pom descriptor file contains dependencies, add these ones.
                for (EAPStaticModuleDependency dep : moduleStaticDependencies) {
                    String moduleUID = EAPArtifactUtils.getUID(dep.getName(), dep.getSlot());
                    EAPModuleGraphNode node = staticLayerDistribution.getGraph().getNode(moduleUID);
                    if (node == null) throw new EAPModuleDefinitionException(moduleArtifactCoordinates, "The module contains a dependency to the module " + moduleUID + " which is missing in the static layer");
                    dep.addArtifact(node.getArtifact());
                    result.addDependency(dep);
                }
            } else {
                // If module pom descriptor file does not contain dependencies, add all modules included in the staticLayer as module dependencies.
                List<EAPModuleGraphNode> modules = staticLayerDistribution.getGraph().getNodes();
                if (modules != null && !modules.isEmpty()) {
                    for (EAPModuleGraphNode module : modules) {
                        Artifact moduleArtifact = module.getArtifact();
                        EAPStaticModuleDependency dep = new EAPStaticModuleDependency(moduleArtifact.getArtifactId());
                        dep.setArtifacts(Arrays.asList(new Artifact[] {moduleArtifact}));
                        result.addDependency(dep);
                    }
                }
            }

            if (result.getWarFile() == null) throw new EAPModuleDefinitionException(moduleArtifactCoordinates, "The module has not the required WAR dependency.");

            return result;

        } catch (XmlPullParserException e) {
            throw new EAPModuleDefinitionException(moduleArtifactCoordinates, "The artifact's pom cannot be pared.", e);
        } catch (IOException e) {
            throw new EAPModuleDefinitionException(moduleArtifactCoordinates, "The artifact's pom cannot be read.", e);
        } 
    }

    protected void checkConfiguration() throws MojoFailureException {
        if (distributionName == null || distributionName.trim().length() == 0) throw new MojoFailureException("Distribution name configuration parameter cannot be null or empty.");
        if (outputPath == null || outputPath.trim().length() == 0) throw new MojoFailureException("Output path configuration parameter cannot be null or empty.");
        if (staticLayerArtifact == null) throw new MojoFailureException("Static layer artifact is not set.");
    }

    protected void initServices() {
        // Configure the artifacts holder.
        artifactsHolder = new EAPArtifactsHolder(repoSystem, repoSession, remoteRepos);
    }

    private Collection<Artifact> scanPomDependencies() throws ArtifactResolutionException {
        Collection<Artifact> result = null;
        Set<org.apache.maven.artifact.Artifact> artifacts = project.getDependencyArtifacts();

        if (artifacts != null) {
            result = new LinkedList<Artifact>();
            for (org.apache.maven.artifact.Artifact artifact : artifacts) {
                if (EAPConstants.POM.equalsIgnoreCase(artifact.getType())) {
                    Artifact resolved = EAPArtifactUtils.resolveArtifact(artifact.getGroupId(), artifact.getArtifactId(), artifact.getVersion(), artifact.getType(), artifact.getClassifier(), repoSystem, repoSession, remoteRepos);
                    result.add(resolved);
                }
            }
        }

        return result;
    }

    protected ZipFile getWarFile(EAPDynamicModule module) throws IOException, ArtifactResolutionException {
        Artifact warArtifact = artifactsHolder.resolveArtifact(module.getWarFile());
        return new ZipFile(warArtifact.getFile(), ZipFile.OPEN_READ);
    }

    protected EAPWarResources scanWarResources(ZipFile war) throws MojoExecutionException, IOException {
        Document currentJbossDepStructureDoc = null;
        Collection<String> warDependencies = new LinkedList<String>();

        String warName = EAPFileUtils.extractFileName(war.getName());

        try {
            for (Enumeration e = war.entries(); e.hasMoreElements();) {
                ZipEntry entry = (ZipEntry) e.nextElement();

                if (entry.getName().equalsIgnoreCase(JBOSS_DEP_STRUCTURE_ZIP_ENTRY_NAME)) {
                    InputStream in = war.getInputStream(entry);
                    EAPXMLUtils xmlUtils = new EAPXMLUtils(in);
                    currentJbossDepStructureDoc = xmlUtils.getDocument();
                }
                if (isWarLibrary(entry.getName())) {
                    warDependencies.add(entry.getName());
                }


            }
        } catch (IOException e) {
            throw new MojoExecutionException("Cannot read the jboss-deployment-structure descriptor from WAR dependency: " + warName, e);
        } catch (Exception e) {
            throw new MojoExecutionException("Cannot read the jboss-deployment-structure descriptor from WAR dependency: " + warName, e);
        }
        return new EAPWarResources(currentJbossDepStructureDoc, warDependencies);
    }

    protected boolean isWarLibrary(String fileName) {
        return PATTERN_WAR_LIBRARIES.matcher(fileName).matches();
    }

    protected String generateJbossDeploymentStructure(Collection<EAPModuleNodeGraphDependency> staticModuleDependencies, ZipFile war, Document currentJbossDepStructureDoc) throws MojoExecutionException, IOException {
        String warName = EAPFileUtils.extractFileName(war.getName());

        Collection<? extends EAPModuleGraphNodeDependency> dependencies = new LinkedList<EAPModuleGraphNodeDependency>(staticModuleDependencies);

        if (currentJbossDepStructureDoc != null) {
            getLog().info("Jboss deployment descritpor file found in WAR " + warName + ". Reading its dependencies.");

            // Obtain the current dependencies present in current deployment structure descriptor.
            Collection<EAPModuleNodeGraphDependency> actualDependencies = readCurrentJBossDepStructurDependencies(currentJbossDepStructureDoc);

            // Merge current jboss-deployment-structure (if present) with the ones from static module definition.
            dependencies = mergeDependencies(staticModuleDependencies, actualDependencies, war);
        }

        return templateBuilder.buildJbossDeploymentStructure(dependencies);
    }

    private String generateAssemblyDescriptor(final EAPDynamicModule module, final String inputWarCoordinates, String jbossDeploymentStructureContent, String jbossAllContent, final Collection<String> exclusions) throws MojoExecutionException, IOException {
        final Collection<EAPAssemblyTemplateFile> assemblyFiles = new LinkedList<EAPAssemblyTemplateFile>();
        final String layerId = module.getName();
        
        // Write the jboss-deployment-structure content into a temp path.
        String jbossDepStuctureName = new StringBuilder(layerId).append("-").append(JBOSS_DEP_STRUCTURE_NAME).append(EXTENSION_XML).toString();
        final File out = EAPFileUtils.writeFile(new File(distroOutputPath), jbossDepStuctureName, jbossDeploymentStructureContent);

        final EAPAssemblyTemplateFile jbossDepStructureFile = new EAPAssemblyTemplateFile() {

            @Override
            public String getSource() {
                return out.getAbsolutePath();
            }

            @Override
            public String getOutputDirectory() {
                return EAPConstants.WEB_INF;
            }

            @Override
            public String getFinalName() {
                return new StringBuilder(JBOSS_DEP_STRUCTURE_NAME).append(EXTENSION_XML).toString();
            }

            @Override
            public boolean isFiltered() {
                return false;
            }
        };

        assemblyFiles.add(jbossDepStructureFile);
        
        if (jbossAllContent != null) {
            String jbossAllName = new StringBuilder(layerId).append("-").append(JBOSS_ALL_NAME).append(EXTENSION_XML).toString();
            final File out2 = EAPFileUtils.writeFile(new File(distroOutputPath), jbossAllName, jbossAllContent);
            
            final EAPAssemblyTemplateFile jbossAllFile = new EAPAssemblyTemplateFile() {

                @Override
                public String getSource() {
                    return out2.getAbsolutePath();
                }

                @Override
                public String getOutputDirectory() {
                    return EAPConstants.META_INF;
                }

                @Override
                public String getFinalName() {
                    return new StringBuilder(JBOSS_ALL_NAME).append(EXTENSION_XML).toString();
                }

                @Override
                public boolean isFiltered() {
                    return true;
                }
            };

            assemblyFiles.add(jbossAllFile);
        }

        // The assembly tempalate model instance.
        final EAPAssemblyTemplate assemblyTemplate = new EAPAssemblyTemplate() {
            @Override
            public String getId() {
                return layerId;
            }

            @Override
            public String[] getFormats() {
                return assemblyFormats.split(",");
            }

            @Override
            public Collection<String> getInclusions() {
                return Arrays.asList(new String[] {inputWarCoordinates});
            }

            @Override
            public Collection<String> getExclusions() {
                return exclusions;
            }

            @Override
            public Collection<EAPAssemblyTemplateFile> getFiles() {
                return assemblyFiles;
            }
        };

        // Patch lifecycle method.
        try {
            patchManager.iterateDynamic(new EAPPatchManager.EAPPatchRunnable() {
                @Override
                public void execute(EAPPatch patch) throws EAPPatchException {
                    EAPDynamicModulesPatch dynamicModulesPatch = (EAPDynamicModulesPatch) patch;
                    dynamicModulesPatch.patchAssembly(module, assemblyTemplate);
                }
            });
        } catch (EAPPatchException e) {
            throw new MojoExecutionException("Error executing dynamic patch in lifecycle method 'patchAssembly'.", e);
        }

        // Build the content.
        return templateBuilder.buildDynamicModuleAssembly(assemblyTemplate);
    }

    private Collection<EAPModuleGraphNodeDependency> mergeDependencies(Collection<EAPModuleNodeGraphDependency> staticModuleDependencies, Collection<EAPModuleNodeGraphDependency> actualDependencies, ZipFile war) {
        Collection<EAPModuleGraphNodeDependency> dependencies = new LinkedList<EAPModuleGraphNodeDependency>();

        // Add the ones from static modules.
        dependencies.addAll(staticModuleDependencies);

        // Check the current jboss-dep-structure.xml file from WAR dependency.
        if (actualDependencies != null && !actualDependencies.isEmpty()) {
            for (EAPModuleNodeGraphDependency dependency : actualDependencies) {
                String depName = dependency.getName();
                String depSlot = dependency.getSlot() != null ? dependency.getSlot() : "main";
                String depCoords = new StringBuilder(depName).append(":").append(depSlot).toString();
                if (!staticModuleDependencies.contains(dependency)) {
                    getLog().warn("Dependency " + depCoords + " is present in actual jboss-deployment-structure file from WAR '" + EAPFileUtils.extractFileName(war.getName()) + "'. It will be added in the new generated jboss-deployment-structure descriptor, if not present.");
                    dependencies.add(dependency);
                } else {
                    getLog().warn("Dependency " + depCoords + " will be overriden by the one from static module definition.");
                }
            }
        }

        return dependencies;
    }

    protected Collection<EAPModuleNodeGraphDependency> readCurrentJBossDepStructurDependencies(Document document) {
        Collection<EAPModuleNodeGraphDependency> result = null;
        if (document != null) {
            NodeList moduleNodes = document.getElementsByTagName("module");
            if (moduleNodes != null && moduleNodes.getLength() > 0) {
                result = new LinkedList<EAPModuleNodeGraphDependency>();
                for (int i = 0; i < moduleNodes.getLength(); i++) {
                    Node node = moduleNodes.item(i);
                    NamedNodeMap attrs = node.getAttributes();

                    String nameNodeValue = null;
                    String slotNodeValue = null;
                    String exportNodeValue = null;

                    Node nameNode = attrs.getNamedItem("name");
                    if (nameNode != null) nameNodeValue = nameNode.getNodeValue();

                    Node slotNode = attrs.getNamedItem("slot");
                    if (slotNode != null) slotNodeValue = nameNode.getNodeValue();

                    Node exportNode = attrs.getNamedItem("export");
                    if (exportNode != null) exportNodeValue = nameNode.getNodeValue();

                    EAPModuleNodeGraphDependency dep = new EAPModuleNodeGraphDependency(nameNodeValue, slotNodeValue, exportNodeValue != null ? Boolean.valueOf(exportNodeValue) : false);
                    result.add(dep);
                }
            }
        }
        return result;
    }

    private static class EAPWarResources {
        private Document jbossDeploymentStructure;
        private Collection<String> warLibs = new LinkedList<String>();

        private EAPWarResources(Document jbossDeploymentStructure, Collection<String> warLibs) {
            this.jbossDeploymentStructure = jbossDeploymentStructure;
            this.warLibs = warLibs;
        }

        private Document getJbossDeploymentStructure() {
            return jbossDeploymentStructure;
        }

        private Collection<String> getWarLibs() {
            return warLibs;
        }
    }

}