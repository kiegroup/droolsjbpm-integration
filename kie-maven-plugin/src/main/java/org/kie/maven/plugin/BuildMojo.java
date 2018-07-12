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

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
import org.drools.compiler.compiler.io.memory.MemoryFile;
import org.drools.compiler.compiler.io.memory.MemoryFileSystem;
import org.drools.compiler.kie.builder.impl.FileKieModule;
import org.drools.compiler.kie.builder.impl.InternalKieModule;
import org.drools.compiler.kie.builder.impl.KieBuilderImpl;
import org.drools.compiler.kie.builder.impl.KieMetaInfoBuilder;
import org.drools.compiler.kie.builder.impl.MemoryKieModule;
import org.drools.compiler.kie.builder.impl.ResultsImpl;
import org.drools.compiler.kie.builder.impl.ZipKieModule;
import org.drools.compiler.kproject.ReleaseIdImpl;
import org.drools.compiler.kproject.models.KieModuleModelImpl;
import org.drools.core.common.ProjectClassLoader;
import org.drools.core.rule.KieModuleMetaInfo;
import org.drools.core.rule.TypeMetaInfo;
import org.kie.api.KieServices;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.Message;
import org.kie.api.builder.ReleaseId;
import org.kie.api.builder.model.KieModuleModel;
import org.kie.internal.io.ResourceFactory;

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

    @Parameter(required = false, defaultValue = "no")
    private String usesPMML;



    /**
     * This container is the same accessed in the KieMavenCli in the kie-wb-common
     */
    @Inject
    private PlexusContainer container;


    @Parameter(property = "generateModel", defaultValue = "no")
    private String generateModel;

    public void execute() throws MojoExecutionException, MojoFailureException {
        if(!ExecModelMode.shouldGenerateModel(generateModel)) {
            buildDrl();
        }
    }

    private void buildDrl() throws MojoFailureException {
        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();

        List<InternalKieModule> kmoduleDeps = new ArrayList<>();

        try {
            Set<URL> urls = new HashSet<URL>();
            for (String element : project.getCompileClasspathElements()) {
                urls.add(new File(element).toURI().toURL());
            }

            project.setArtifactFilter(new CumulativeScopeArtifactFilter(Arrays.asList("compile", "runtime")));
            for (Artifact artifact : project.getArtifacts()) {
                File file = artifact.getFile();
                if (file != null) {
                    urls.add(file.toURI().toURL());
                    KieModuleModel depModel = getDependencyKieModel(file);
                    if (depModel != null) {
                        ReleaseId releaseId = new ReleaseIdImpl(artifact.getGroupId(), artifact.getArtifactId(), artifact.getVersion());
                        kmoduleDeps.add(new ZipKieModule(releaseId, depModel, file));
                    }
                }
            }
            urls.add(outputDirectory.toURI().toURL());

            ClassLoader projectClassLoader = URLClassLoader.newInstance(urls.toArray(new URL[0]), getClass().getClassLoader());

            Thread.currentThread().setContextClassLoader(projectClassLoader);
        } catch (DependencyResolutionRequiredException e) {
            throw new RuntimeException(e);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }

        KieServices ks = KieServices.Factory.get();

        try {
            setSystemProperties(properties);

            KieFileSystem kfs = ks.newKieFileSystem();
            for (File file : getResourceFiles(sourceFolder)) {
                if (!file.getPath().contains("META-INF")) {
                    kfs.write( ResourceFactory.newFileResource(file) );
                }
            }

            KieBuilderImpl kieBuilder = new KieBuilderImpl(kfs);
            InternalKieModule kModule = (InternalKieModule)kieBuilder.getKieModule();
            for (InternalKieModule kmoduleDep : kmoduleDeps) {
                kModule.addKieDependency(kmoduleDep);
            }

            kieBuilder.buildAll();
            ResultsImpl messages = (ResultsImpl)kieBuilder.getResults();

            List<Message> errors = messages != null ? messages.filterMessages( Message.Level.ERROR): Collections.emptyList();

            if (container != null) {
                Optional<Map<String, Object>> optionalKieMap = getKieMap();
                if (optionalKieMap.isPresent()) {
                    Map<String, Object> kieMap = optionalKieMap.get();
                    String compilationID = getCompilationID(kieMap);
                    shareKieObjectsWithMap(kModule, compilationID, kieMap);
                    shareStoreWithMap(kModule.getModuleClassLoader(), compilationID, kieMap);
                    shareTypesMetaInfoWithMap(kModule, compilationID, kieMap);
                }else{
                    getLog().info("Kie Map not present");
                }
            } else {
                new KieMetaInfoBuilder(kModule).writeKieModuleMetaInfo(new DiskResourceStore(outputDirectory));
            }

            if (!errors.isEmpty()) {
                for (Message error : errors) {
                    getLog().error(error.toString());
                }
                throw new MojoFailureException("Build failed!");
            } else {
                writeClassFiles( kModule );
            }
        } finally {
            Thread.currentThread().setContextClassLoader(contextClassLoader);
        }
        getLog().info("KieModule successfully built!");
    }

    private List<File> getResourceFiles(File parent) {
        List<File> files = new ArrayList<>();
        if (parent.isDirectory()) {
            File children[] = parent.listFiles();
            for (File child: children) {
                if (child.isDirectory()) {
                    List<File> childsFiles = getResourceFiles(child);
                    if (childsFiles != null && !childsFiles.isEmpty()) {
                        files.addAll(childsFiles);
                    }
                } else {
                    files.add(child);
                }
            }
        }
        return files;
    }

    private void writeClassFiles( InternalKieModule kModule ) throws MojoFailureException {
        MemoryFileSystem mfs = ((MemoryKieModule )kModule).getMemoryFileSystem();
        kModule.getFileNames()
                .stream()
                .filter(name -> name.endsWith(".class"))
                .forEach( fileName -> {
                    try {
                        saveFile( mfs, fileName );
                    } catch (MojoFailureException e) {
                        throw new RuntimeException( e );
                    }
                } );
    }

    private void saveFile(MemoryFileSystem mfs, String fileName) throws MojoFailureException {
        MemoryFile memFile = (MemoryFile)mfs.getFile(fileName);
        final Path path = Paths.get(outputDirectory.getPath(), memFile.getPath().toPortableString());

        try {
            Files.deleteIfExists(path);
            Files.createDirectories(path);
            Files.copy(memFile.getContents(), path, StandardCopyOption.REPLACE_EXISTING);
        } catch(IOException iox) {
            iox.printStackTrace();
            throw new MojoFailureException("Unable to write file", iox);
        }
    }

    private String getCompilationID(Map<String, Object> optionalKieMap) {
        Object compilationIDObj = optionalKieMap.get("compilation.ID");
        if(compilationIDObj != null){
            return compilationIDObj.toString();
        }else{
            getLog().error("compilation.ID key not present in the shared map using thread name:"
                                   + Thread.currentThread().getName());
            return Thread.currentThread().getName();
        }
    }

    private void shareKieObjectsWithMap(InternalKieModule kModule, String compilationID, Map<String, Object> kieMap) {
        KieMetaInfoBuilder builder = new KieMetaInfoBuilder(kModule);
        KieModuleMetaInfo modelMetaInfo = builder.getKieModuleMetaInfo();

        /*Standard for the kieMap keys -> compilationID + dot + class name */
        StringBuilder sbModelMetaInfo = new StringBuilder(compilationID).append(".").append(KieModuleMetaInfo.class.getName());
        StringBuilder sbkModule = new StringBuilder(compilationID).append(".").append(FileKieModule.class.getName());

        if (modelMetaInfo != null) {
            kieMap.put(sbModelMetaInfo.toString(), modelMetaInfo);
            getLog().info("KieModelMetaInfo available in the map shared with the Maven Embedder with key:" +sbModelMetaInfo.toString());
        }
        if (kModule != null) {
            kieMap.put(sbkModule.toString(), kModule);
            getLog().info("KieModule available in the map shared with the Maven Embedder with key:"+sbkModule.toString());
        }
    }

    private void shareStoreWithMap(ClassLoader classLoader, String compilationID, Map<String, Object> kieMap) {
        if (classLoader instanceof ProjectClassLoader) {
            ProjectClassLoader projectClassloder = (ProjectClassLoader) classLoader;
            Map<String, byte[]> types = projectClassloder.getStore();
            if (projectClassloder.getStore() != null) {
                StringBuilder sbTypes = new StringBuilder(compilationID).append(".").append("ProjectClassloaderStore");
                kieMap.put(sbTypes.toString(), types);
                getLog().info("ProjectClassloader Store available in the map shared with the Maven Embedder");
            }
        }
    }

    private void shareTypesMetaInfoWithMap(InternalKieModule kModule, String compilationID, Map<String, Object> kieMap) {
        KieMetaInfoBuilder kb = new KieMetaInfoBuilder(kModule);
        KieModuleMetaInfo info = kb.generateKieModuleMetaInfo(null);
        Map<String, TypeMetaInfo> typesMetaInfos = info.getTypeMetaInfos();

        if (typesMetaInfos != null) {
            StringBuilder sbTypes = new StringBuilder(compilationID).append(".").append(TypeMetaInfo.class.getName());
            Set<String> eventClasses = new HashSet<>();
            for (Map.Entry<String, TypeMetaInfo> item : typesMetaInfos.entrySet()) {
                if (item.getValue().isEvent()) {
                    eventClasses.add(item.getKey());
                }
            }
            kieMap.put(sbTypes.toString(), eventClasses);
            getLog().info("TypesMetaInfo keys available in the map shared with the Maven Embedder");
        }
    }

    private Optional<Map<String, Object>> getKieMap() {
        try {
            /**
             * Retrieve the map passed into the Plexus container by the MavenEmbedder from the MavenIncrementalCompiler in the kie-wb-common
             */
            Map<String, Object> kieMap = (Map) container.lookup(Map.class,
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