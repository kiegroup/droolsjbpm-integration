/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package org.kie.maven.plugin;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import javax.inject.Inject;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.artifact.resolver.filter.CumulativeScopeArtifactFilter;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.drools.compiler.compiler.BPMN2ProcessFactory;
import org.drools.compiler.compiler.DecisionTableFactory;
import org.drools.compiler.compiler.GuidedDecisionTableFactory;
import org.drools.compiler.compiler.GuidedRuleTemplateFactory;
import org.drools.compiler.compiler.GuidedScoreCardFactory;
import org.drools.compiler.compiler.PMMLCompilerFactory;
import org.drools.compiler.compiler.ProcessBuilderFactory;
import org.drools.compiler.kie.builder.impl.InternalKieModule;
import org.drools.compiler.kie.builder.impl.KieContainerImpl;
import org.drools.compiler.kie.builder.impl.KieMetaInfoBuilder;
import org.drools.compiler.kie.builder.impl.KieProject;
import org.drools.compiler.kie.builder.impl.ResultsImpl;
import org.drools.compiler.kie.builder.impl.ZipKieModule;
import org.drools.compiler.kproject.ReleaseIdImpl;
import org.drools.compiler.kproject.models.KieModuleModelImpl;
import org.drools.core.rule.KieModuleMetaInfo;
import org.kie.api.KieServices;
import org.kie.api.builder.KieRepository;
import org.kie.api.builder.Message;
import org.kie.api.builder.ReleaseId;
import org.kie.api.builder.model.KieModuleModel;

import static org.drools.compiler.kie.builder.impl.KieBuilderImpl.setDefaultsforEmptyKieModule;

/**
 * This goal builds the Drools files belonging to the kproject.
 */
@Mojo(name = "build",
        requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME,
        requiresProject = true,
        defaultPhase = LifecyclePhase.COMPILE)
public class BuildMojo extends AbstractKieMojo {

    /**
     * Directory containing the generated JAR.
     */
    @Parameter(required = true, defaultValue = "${project.build.outputDirectory}")
    private File outputDirectory;

    /**
     * Project resources folder.
     */
    @Parameter(required = true, defaultValue = "src/main/resources")
    private File sourceFolder;

    @Parameter(required = true, defaultValue = "${project}")
    private MavenProject project;

    @Parameter
    private Map<String, String> properties;

    /**
     * Param passed by the Maven Incremental compiler to identify the value used in the kieMap to identify the
     * KieModuleMetaInfo from the current complation
     */
    @Parameter(required = false, defaultValue = "${compilation.ID}")
    private String compilationID;

    /**
     * This container is the same accessed in the KieMavenCli in the kie-wb-common
     */
    @Inject
    private PlexusContainer container;

    public void execute() throws MojoExecutionException, MojoFailureException {

        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();

        List<InternalKieModule> kmoduleDeps = new ArrayList<InternalKieModule>();

        try {
            Set<URL> urls = new HashSet<URL>();
            for (String element : project.getCompileClasspathElements()) {
                urls.add(new File(element).toURI().toURL());
            }

            project.setArtifactFilter(new CumulativeScopeArtifactFilter(Arrays.asList("compile",
                                                                                      "runtime")));
            for (Artifact artifact : project.getArtifacts()) {
                File file = artifact.getFile();
                if (file != null) {
                    urls.add(file.toURI().toURL());
                    KieModuleModel depModel = getDependencyKieModel(file);
                    if (depModel != null) {
                        ReleaseId releaseId = new ReleaseIdImpl(artifact.getGroupId(),
                                                                artifact.getArtifactId(),
                                                                artifact.getVersion());
                        kmoduleDeps.add(new ZipKieModule(releaseId,
                                                         depModel,
                                                         file));
                    }
                }
            }
            urls.add(outputDirectory.toURI().toURL());

            ClassLoader projectClassLoader = URLClassLoader.newInstance(urls.toArray(new URL[0]),
                                                                        Thread.currentThread().getContextClassLoader());

            Thread.currentThread().setContextClassLoader(projectClassLoader);

            BPMN2ProcessFactory.loadProvider(projectClassLoader);
            DecisionTableFactory.loadProvider(projectClassLoader);
            ProcessBuilderFactory.loadProvider(projectClassLoader);
            PMMLCompilerFactory.loadProvider(projectClassLoader);
            GuidedDecisionTableFactory.loadProvider(projectClassLoader);
            GuidedRuleTemplateFactory.loadProvider(projectClassLoader);
            GuidedScoreCardFactory.loadProvider(projectClassLoader);
        } catch (DependencyResolutionRequiredException e) {
            throw new RuntimeException(e);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }

        KieServices ks = KieServices.Factory.get();

        try {
            setSystemProperties(properties);
            KieRepository kr = ks.getRepository();
            InternalKieModule kModule = (InternalKieModule) kr.addKieModule(ks.getResources().newFileSystemResource(sourceFolder));
            for (InternalKieModule kmoduleDep : kmoduleDeps) {
                kModule.addKieDependency(kmoduleDep);
            }

            KieContainerImpl kContainer = (KieContainerImpl) ks.newKieContainer(kModule.getReleaseId());

            KieProject kieProject = kContainer.getKieProject();
            ResultsImpl messages = kieProject.verify();

            List<Message> errors = messages.filterMessages(Message.Level.ERROR);
            if (!errors.isEmpty()) {
                for (Message error : errors) {
                    getLog().error(error.toString());
                }
                throw new MojoFailureException("Build failed!");
            } else {
                if (container != null && compilationID != null) {
                    Optional<Map<String, Object>> optionalKieMap = getKieMap();
                    if (optionalKieMap.isPresent()) {
                        KieMetaInfoBuilder builder = new KieMetaInfoBuilder(kModule);
                        KieModuleMetaInfo modelMetaInfo = builder.getKieModuleMetaInfo();

                        /*Standard for the kieMap keys -> compilationID + dot + classtype with first lowercase*/
                        StringBuilder sbModelMetaInfo = new StringBuilder(compilationID).append(".").append(modelMetaInfo.getClass().getName());
                        StringBuilder sbkModule = new StringBuilder(compilationID).append(".").append(kModule.getClass().getName());
                        if(modelMetaInfo != null) {
                            optionalKieMap.get().put(sbModelMetaInfo.toString(),
                                                     modelMetaInfo);
                            getLog().info("KieModelMetaInfo available in the map shared with the Maven Embedded");
                        }
                        /* @TODO if(kModule != null) {
                            optionalKieMap.get().put(sbkModule.toString(),
                                                     getRaw(kModule));
                            getLog().info("KieModule available in the map shared with the Maven Embedded");
                        }*/
                    }
                } else {
                    new KieMetaInfoBuilder(kModule).writeKieModuleMetaInfo(new DiskResourceStore(outputDirectory));
                }
            }
        } finally {
            Thread.currentThread().setContextClassLoader(contextClassLoader);
        }
        getLog().info("KieModule successfully built!");
    }

    private byte[] getRaw(Object o){
        ObjectOutput out = null;
        ByteArrayOutputStream bos = null;

        try {
            bos = new ByteArrayOutputStream();
            out = new ObjectOutputStream(bos);
            out.writeObject(o);
            out.flush();
            byte[] objBytes = bos.toByteArray();
            return  objBytes;
        }catch (IOException ioe){
            getLog().error(ioe.getMessage());
        } finally {
            try {
                bos.close();
            } catch (IOException ex) {
                getLog().error(ex.getMessage());
            }
        }
        return new byte[]{};
    }

    private Optional<Map<String, Object>> getKieMap() {
        Map<String, Object> kieMap = null;

        try {
            /**
             * Retrieve the map passed into the Plexus container by the MavenEmbedder from the MavenIncrementalCompiler in the kie-wb-common
             */
            kieMap = (Map) container.lookup(Map.class,
                                            "java.util.HashMap",
                                            "kieMap");
            return Optional.of(kieMap);
        } catch (ComponentLookupException cle) {
            getLog().info("kieMap not present with compilationID and container present");
            return Optional.empty();
        }
    }

    private KieModuleModel getDependencyKieModel(File jar) {
        ZipFile zipFile = null;
        try {
            zipFile = new ZipFile(jar);
            ZipEntry zipEntry = zipFile.getEntry(KieModuleModelImpl.KMODULE_JAR_PATH);
            if (zipEntry != null) {
                KieModuleModel kieModuleModel = KieModuleModelImpl.fromXML(zipFile.getInputStream(zipEntry));
                setDefaultsforEmptyKieModule(kieModuleModel);
                return kieModuleModel;
            }
        } catch (Exception e) {
        } finally {
            if (zipFile != null) {
                try {
                    zipFile.close();
                } catch (IOException e) {
                }
            }
        }
        return null;
    }
}
