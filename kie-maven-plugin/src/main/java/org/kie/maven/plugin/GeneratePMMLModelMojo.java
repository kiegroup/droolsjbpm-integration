/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
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
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.artifact.resolver.filter.CumulativeScopeArtifactFilter;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.drools.compiler.compiler.io.Folder;
import org.drools.compiler.compiler.io.memory.MemoryFile;
import org.drools.compiler.compiler.io.memory.MemoryFileSystem;
import org.drools.compiler.kie.builder.impl.InternalKieModule;
import org.drools.compiler.kie.builder.impl.KieBuilderImpl;
import org.drools.compiler.kie.builder.impl.KieModuleKieProject;
import org.drools.compiler.kie.builder.impl.MemoryKieModule;
import org.drools.compiler.kie.builder.impl.ResultsImpl;
import org.drools.core.definitions.InternalKnowledgePackage;
import org.drools.modelcompiler.CanonicalKieModule;
import org.drools.modelcompiler.builder.CanonicalModelKieProject;
import org.drools.modelcompiler.builder.ModelBuilderImpl;
import org.drools.modelcompiler.builder.ModelWriter;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.internal.io.ResourceTypePackage;
import org.kie.api.io.ResourceType;
import org.kie.pmml.commons.model.HasSourcesMap;
import org.kie.pmml.commons.model.KiePMMLFactoryModel;
import org.kie.pmml.commons.model.KiePMMLModel;

import static org.kie.maven.plugin.ExecModelMode.isModelCompilerInClassPath;
import static org.kie.pmml.commons.utils.KiePMMLModelUtils.getSanitizedPackageName;

@Mojo(name = "generatePMMLModel",
        requiresDependencyResolution = ResolutionScope.NONE,
        requiresProject = true,
        defaultPhase = LifecyclePhase.COMPILE)
public class GeneratePMMLModelMojo extends AbstractKieMojo {

    private static final String KMODULE_XML_TEMPLATE = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" +
            "<kmodule xmlns=\"http://jboss.org/kie/6.0.0/kmodule\">\r\n" +
            "\r\n%s\r\n" +
            "</kmodule>";
    private static final String KBASE_TAG_TEMPLATE = "\t<kbase name=\"%s\" packages=\"%s\">\r\n" + // defaulting that all pmml files have to be put inside PMMLResources folder
            "\t\t<ksession name=\"%sSession\" type=\"stateless\" />\r\n" +
            "\t</kbase>\r\n";
    private static final String CLASSES = "classes";
    private static final String PMMLRESOURCES = "PMMLResources";
    private static final String PMML = "pmml";
    public static PathMatcher drlFileMatcher = FileSystems.getDefault().getPathMatcher("glob:**.drl");
    @Parameter(defaultValue = "${session}", required = true, readonly = true)
    private MavenSession mavenSession;

    @Parameter(required = true, defaultValue = "${project.build.directory}")
    private File targetDirectory;

    @Parameter(required = true, defaultValue = "${project.basedir}")
    private File projectDir;

    @Parameter(required = true, defaultValue = "${project.build.testSourceDirectory}")
    private File testDir;

    @Parameter
    private Map<String, String> properties;

    @Parameter(required = true, defaultValue = "${project}")
    private MavenProject project;

    @Parameter(required = true, defaultValue = "${project.build.outputDirectory}")
    private File outputDirectory;

    @Parameter(property = "generatePMMLModel", defaultValue = "no")
    private String generatePMMLModel;

    private List<Path> createdPaths = new ArrayList<>();
    private List<Path> movedPmmlFiles = new ArrayList<>();

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        // GeneratePMMLModelMojo is executed when BuildMojo and GenerateModelMojo aren't and vice-versa
        boolean modelParameterEnabled = PMMLModelMode.modelParameterEnabled(generatePMMLModel);
        boolean modelCompilerInClassPath = isModelCompilerInClassPath(project.getDependencies());
        if (modelParameterEnabled && modelCompilerInClassPath) {
            generateModel();
        } else if (modelParameterEnabled) { // !modelCompilerInClassPath
            getLog().warn("You're trying to build rule assets in a project from an executable rule model, but you did not provide the required dependency on the project classpath.\n" +
                                  "To enable executable rule models for your project, add the `drools-model-compiler` dependency in the `pom.xml` file of your project.\n");
        }
    }

    private void generateModel() throws MojoExecutionException {
        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            Set<URL> urls = new HashSet<>();
            for (String element : project.getCompileClasspathElements()) {
                urls.add(new File(element).toURI().toURL());
            }

            project.setArtifactFilter(new CumulativeScopeArtifactFilter(Arrays.asList("compile",
                                                                                      "runtime")));
            for (Artifact artifact : project.getArtifacts()) {
                File file = artifact.getFile();
                if (file != null) {
                    urls.add(file.toURI().toURL());
                }
            }
            urls.add(outputDirectory.toURI().toURL());

            ClassLoader projectClassLoader = URLClassLoader.newInstance(urls.toArray(new URL[0]),
                                                                        getClass().getClassLoader());
            Thread.currentThread().setContextClassLoader(projectClassLoader);
        } catch (DependencyResolutionRequiredException | MalformedURLException e) {
            throw new RuntimeException(e);
        }

        KieServices ks = KieServices.Factory.get();
        try {
            // FLAG TO KNOW THAT BUILD HAS BEEN LAUNCHED BY MAVEN
            properties.put("kie-maven-plugin-launcher", "true");
            setSystemProperties(properties);

            final KieBuilderImpl kieBuilder = (KieBuilderImpl) ks.newKieBuilder(projectDir);
            kieBuilder.setPomModel(new ProjectPomModel(mavenSession));
            String testPath = "src" + File.separator + "test" + File.separator + "java";
            String pmmlResourcePath = "src" + File.separator + "main" + File.separator + "resources" + File.separator + PMMLRESOURCES;
            kieBuilder.buildAll(ExecutableModelMavenProject.SUPPLIER,
                                s -> !s.contains(testPath) && !s.contains(pmmlResourcePath));
            InternalKieModule kieModule = (InternalKieModule) kieBuilder.getKieModule();
            List<String> generatedFiles = kieModule.getFileNames()
                    .stream()
                    .filter(f -> f.endsWith("java"))
                    .collect(Collectors.toList());

            Set<String> drlFiles = kieModule.getFileNames()
                    .stream()
                    .filter(f -> f.endsWith("drl"))
                    .collect(Collectors.toSet());

            getLog().info(String.format("Found %d generated files in Canonical Model", generatedFiles.size()));

            MemoryFileSystem mfs = kieModule instanceof CanonicalKieModule ?
                    ((MemoryKieModule) ((CanonicalKieModule) kieModule).getInternalKieModule()).getMemoryFileSystem() :
                    ((MemoryKieModule) kieModule).getMemoryFileSystem();

            final String droolsModelCompilerPath = "/generated-sources/drools-model-compiler/main/java";
            final String newCompileSourceRoot = targetDirectory.getPath() + droolsModelCompilerPath;
            project.addCompileSourceRoot(newCompileSourceRoot);

            for (String generatedFile : generatedFiles) {
                final MemoryFile f = (MemoryFile) mfs.getFile(generatedFile);
                final Path newFile = Paths.get(targetDirectory.getPath(),
                                               droolsModelCompilerPath,
                                               f.getPath().toPortableString());

                try {
                    Files.deleteIfExists(newFile);
                    Files.createDirectories(newFile.getParent());
                    Files.copy(f.getContents(), newFile, StandardCopyOption.REPLACE_EXISTING);

                    getLog().info("Generating " + newFile);
                } catch (IOException e) {
                    e.printStackTrace();
                    throw new MojoExecutionException("Unable to write file", e);
                }
            }

            // copy the META-INF packages file
            final String path = CanonicalKieModule.getModelFileWithGAV(kieModule.getReleaseId());
            final MemoryFile packagesMemoryFile = (MemoryFile) mfs.getFile(path);
            final String packagesMemoryFilePath = packagesMemoryFile.getFolder().getPath().toPortableString();
            final Path packagesDestinationPath = Paths.get(targetDirectory.getPath(), CLASSES, packagesMemoryFilePath, packagesMemoryFile.getName());

            try {
                if (!Files.exists(packagesDestinationPath)) {
                    Files.createDirectories(packagesDestinationPath.getParent());
                }
                Files.copy(packagesMemoryFile.getContents(), packagesDestinationPath, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                e.printStackTrace();
                throw new MojoExecutionException("Unable to write file", e);
            }

            if (ExecModelMode.shouldDeleteFile(generatePMMLModel)) {
                deleteDrlFiles(drlFiles);
            }

        } finally {
            Thread.currentThread().setContextClassLoader(contextClassLoader);
        }
        // REMOVING FLAG TO KNOW THAT BUILD HAS BEEN LAUNCHED BY MAVEN
        properties.remove("kie-maven-plugin-launcher");
        getLog().info("PMML model successfully generated");
    }

    private void deleteDrlFiles(Set<String> actualDrlFiles) throws MojoExecutionException {
        // Remove drl files
        try (final Stream<Path> drlFilesToDeleted = Files.find(outputDirectory.toPath(), Integer.MAX_VALUE, (p, f) -> drlFileMatcher.matches(p))) {
            Set<String> deletedFiles = new HashSet<>();
            drlFilesToDeleted.forEach(p -> {
                try {
                    Files.delete(p);
                    deletedFiles.add(p.toString());
                } catch (IOException e) {
                    e.printStackTrace();
                    throw new RuntimeException("Unable to delete file " + p);
                }
            });
            actualDrlFiles.retainAll(deletedFiles);
            if (!actualDrlFiles.isEmpty()) {
                String actualDrlFiles1 = String.join(",", actualDrlFiles);
                getLog().warn("Base directory: " + projectDir);
                getLog().warn("Files not deleted: " + actualDrlFiles1);
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new MojoExecutionException("Unable to find .drl files");
        }
    }

    public static class ExecutableModelMavenProject implements KieBuilder.ProjectType {

        public static final BiFunction<InternalKieModule, ClassLoader, KieModuleKieProject> SUPPLIER = ExecutableModelMavenPluginKieProject::new;

        public static class ExecutableModelMavenPluginKieProject extends CanonicalModelKieProject {

            public ExecutableModelMavenPluginKieProject(InternalKieModule kieModule, ClassLoader classLoader) {
                super(true, kieModule, classLoader);
            }

            @Override
            public void writeProjectOutput(MemoryFileSystem trgMfs, ResultsImpl messages) {
                MemoryFileSystem srcMfs = new MemoryFileSystem();
                List<String> modelFiles = new ArrayList<>();
                ModelWriter modelWriter = new ModelWriter();
                StringBuilder kbasesTagsBuilder = new StringBuilder();
                for (ModelBuilderImpl modelBuilder : modelBuilders) {
                    ModelWriter.Result result = modelWriter.writeModel(srcMfs, modelBuilder.getPackageSources());
                    modelFiles.addAll(result.getModelFiles());
                    final InternalKnowledgePackage[] packages = modelBuilder.getPackages();
                    for (InternalKnowledgePackage internalKnowledgePackage : packages) {
                        try {
                            final ResourceTypePackage<?> pmmlPackage = internalKnowledgePackage.getResourceTypePackages().get(ResourceType.PMML);
                            if (pmmlPackage != null) {
                                final Iterator<?> iterator = pmmlPackage.iterator();
                                while (iterator.hasNext()) {
                                    Object retrieved = iterator.next();
                                    // Declaring kbase tag inside generated kmodule; one kbase for each actual model
                                    if (retrieved instanceof KiePMMLModel && !(retrieved instanceof KiePMMLFactoryModel)) {
                                        KiePMMLModel kiePMMLModel = (KiePMMLModel) retrieved;
                                        String kbaseName = kiePMMLModel.getName();
                                        String kpackageName = kiePMMLModel.getKModulePackageName();
                                        kbasesTagsBuilder.append(String.format(KBASE_TAG_TEMPLATE, kbaseName, kpackageName, kbaseName));
                                    }
                                    // adding sources to source folder
                                    if (retrieved instanceof HasSourcesMap) {
                                        final Map<String, String> sourcesMap = ((HasSourcesMap) retrieved).getSourcesMap();
                                        sourcesMap.forEach((fileName, fileSource) -> {
                                            String actualFilePath = fileName.replace(".", "/");
                                            String filePath = "src/main/java/" + actualFilePath + ".java";
                                            srcMfs.write(filePath, fileSource.getBytes());
                                        });
                                    }
                                }
                            }
                        } catch (Exception e) {
                            throw new RuntimeException(e.getMessage(), e);
                        }
                    }
                    final Folder mainFolder = srcMfs.getFolder("src/main");
                    final Folder targetFolder = trgMfs.getFolder(".");
                    srcMfs.copyFolder(mainFolder, trgMfs, targetFolder);
                }
                modelWriter.writeModelFile(modelFiles, trgMfs, getInternalKieModule().getReleaseId());
            }
        }
    }
}
