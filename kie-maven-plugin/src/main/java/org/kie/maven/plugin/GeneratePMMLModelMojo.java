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

import java.io.ByteArrayInputStream;
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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
import org.drools.compiler.builder.impl.KnowledgeBuilderImpl;
import org.drools.compiler.kie.builder.impl.KieBuilderImpl;
import org.drools.core.impl.InternalKnowledgeBase;
import org.drools.core.impl.KnowledgeBaseImpl;
import org.drools.core.io.impl.FileSystemResource;
import org.drools.modelcompiler.builder.GeneratedFile;
import org.kie.api.KieServices;
import org.kie.api.io.Resource;
import org.kie.pmml.commons.model.HasNestedModels;
import org.kie.pmml.commons.model.HasSourcesMap;
import org.kie.pmml.commons.model.KiePMMLModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.kie.api.pmml.PMMLConstants.KIE_PMML_IMPLEMENTATION;
import static org.kie.api.pmml.PMMLConstants.LEGACY;
import static org.kie.api.pmml.PMMLConstants.NEW;
import static org.kie.maven.plugin.ExecModelMode.isModelCompilerInClassPath;
import static org.kie.pmml.evaluator.assembler.service.PMMLCompilerService.getKiePMMLModelsFromResourceWithSources;

@Mojo(name = "generatePMMLModel",
        requiresDependencyResolution = ResolutionScope.NONE,
        requiresProject = true,
        defaultPhase = LifecyclePhase.COMPILE)
public class GeneratePMMLModelMojo extends AbstractKieMojo {

    private static final Logger logger = LoggerFactory.getLogger(GeneratePMMLModelMojo.class);

    private static final String PMML = "pmml";
    private static final String generatedSourcesPath = "/generated-sources/";
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
    @Parameter(defaultValue = "${project.resources}", required = true, readonly = true)
    private List<org.apache.maven.model.Resource> resourcesDirectories;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        if (!isNewInvoked()) {
            getLog().warn("Skipping `generatePMMLModel` because " + KIE_PMML_IMPLEMENTATION.getName() +
                                  " is not " + NEW.getName());
            return;
        }
        boolean modelCompilerInClassPath = isModelCompilerInClassPath(project.getDependencies());
        if (!modelCompilerInClassPath) {
            getLog().warn("Skipping `generatePMMLModel` because you did" +
                                  " not provide the required dependency on the project classpath.\n" +
                                  "To enable it for your project, add the `drools-model-compiler`" +
                                  " dependency in the `pom.xml` file of your project.\n");
        } else {
            generateModel();
        }
    }

    static boolean isNewInvoked() {
        final String property = System.getProperty(KIE_PMML_IMPLEMENTATION.getName(), LEGACY.getName());
        return property.equals(NEW.getName());
    }

    private void generateModel() throws MojoExecutionException {
        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        URLClassLoader projectClassLoader = null;
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
            projectClassLoader = URLClassLoader.newInstance(urls.toArray(new URL[0]), getClass().getClassLoader());
            Thread.currentThread().setContextClassLoader(projectClassLoader);
            generateFiles();
        } catch (DependencyResolutionRequiredException | MalformedURLException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        } finally {
            Thread.currentThread().setContextClassLoader(contextClassLoader);
            if (projectClassLoader != null) {
                try {
                    projectClassLoader.close();
                } catch (IOException e) {
                    getLog().warn(e);
                }
            }
        }
        getLog().info("PMML model successfully generated");
    }

    private void generateFiles() throws MojoExecutionException {
        final List<GeneratedFile> generatedFiles = getGeneratedFiles();
        KieServices ks = KieServices.Factory.get();
        final KieBuilderImpl kieBuilder = (KieBuilderImpl) ks.newKieBuilder(projectDir);
        kieBuilder.setPomModel(new ProjectPomModel(mavenSession));
        final String newCompileSourceRoot = targetDirectory.getPath() + generatedSourcesPath;
        project.addCompileSourceRoot(newCompileSourceRoot);
        for (GeneratedFile generatedFile : generatedFiles) {
            writeFile(generatedFile);
        }
    }

    void writeFile(final GeneratedFile generatedFile) throws MojoExecutionException {
        final Path newFile = Paths.get(targetDirectory.getPath(),
                                       generatedSourcesPath,
                                       generatedFile.getPath());
        try {
            Files.deleteIfExists(newFile);
            Files.createDirectories(newFile.getParent());
            Files.copy(new ByteArrayInputStream(generatedFile.getData()), newFile,
                       StandardCopyOption.REPLACE_EXISTING);
            getLog().info("Generating " + newFile);
        } catch (IOException e) {
            getLog().error(e);
            throw new MojoExecutionException("Unable to write file", e);
        }
    }

    private List<GeneratedFile> getGeneratedFiles() throws MojoExecutionException {
        List<GeneratedFile> toReturn = new ArrayList<>();
        for (org.apache.maven.model.Resource resourceDirectory : resourcesDirectories) {
            File directoryFile = new File(resourceDirectory.getDirectory());
            getLog().info("Looking for PMML models in " + directoryFile.getPath());
            String errorMessageTemplate = null;
            if (!directoryFile.exists()) {
                errorMessageTemplate = "Resource path %s does not exists";
            } else if (!directoryFile.canRead()) {
                errorMessageTemplate = "Resource path %s is not readable";
            } else if (!directoryFile.isDirectory()) {
                errorMessageTemplate = "Resource path %s is not a directory";
            }
            if (errorMessageTemplate != null) {
                throw new MojoExecutionException(String.format(errorMessageTemplate, resourceDirectory));
            }
            toReturn.addAll(getGeneratedFiles(directoryFile));
        }
        if (toReturn.isEmpty()) {
            getLog().info("No PMML Models found.");
        } else {
            getLog().info(String.format("Found %s PMML models", toReturn.size()));
        }
        return toReturn;
    }

    private List<GeneratedFile> getGeneratedFiles(File resourceDirectory) throws MojoExecutionException {
        final List<GeneratedFile> toReturn = new ArrayList<>();
        try (Stream<Path> stream = Files
                .walk(resourceDirectory.toPath(), Integer.MAX_VALUE)
                .filter(path -> path.toString().endsWith(PMML))) {
            return stream
                    .map(Path::toFile)
                    .map(FileSystemResource::new)
                    .map(this::parseResource)
                    .map(this::getGenerateFiles)
                    .reduce(toReturn, (previous, toAdd) -> {
                        previous.addAll(toAdd);
                        return previous;
                    });
        } catch (Exception e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }
    }

    private List<GeneratedFile> getGenerateFiles(final PMMLResource pmmlResources) {
        final List<GeneratedFile> toReturn = new ArrayList<>();
        List<KiePMMLModel> kiepmmlModels = pmmlResources.getKiePmmlModels();
        addModels(kiepmmlModels, pmmlResources, toReturn);
        return toReturn;
    }

    private void addModels(final List<KiePMMLModel> kiepmmlModels,
                           final PMMLResource resource,
                           final List<GeneratedFile> generatedFiles) {
        for (KiePMMLModel model : kiepmmlModels) {
            if (model.getName() == null || model.getName().isEmpty()) {
                String errorMessage = String.format("Model name should not be empty inside %s",
                                                    resource.getModelPath());
                throw new RuntimeException(errorMessage);
            }
            if (!(model instanceof HasSourcesMap)) {
                String errorMessage = String.format("Expecting HasSourcesMap instance, retrieved %s inside %s",
                                                    model.getClass().getName(),
                                                    resource.getModelPath());
                throw new RuntimeException(errorMessage);
            }
            Map<String, String> sourceMap = ((HasSourcesMap) model).getSourcesMap();
            for (Map.Entry<String, String> sourceMapEntry : sourceMap.entrySet()) {
                String path = sourceMapEntry.getKey().replace('.', File.separatorChar) + ".java";
                generatedFiles.add(new GeneratedFile(GeneratedFile.Type.PMML, path, sourceMapEntry.getValue()));
            }
            Map<String, String> rulesSourceMap = ((HasSourcesMap) model).getRulesSourcesMap();
            if (rulesSourceMap != null) {
                for (Map.Entry<String, String> rulesSourceMapEntry : rulesSourceMap.entrySet()) {
                    String path = rulesSourceMapEntry.getKey().replace('.', File.separatorChar) + ".java";
                    generatedFiles.add(new GeneratedFile(GeneratedFile.Type.RULE, path, rulesSourceMapEntry.getValue()));
                }
            }
            if (model instanceof HasNestedModels) {
                addModels(((HasNestedModels) model).getNestedModels(), resource, generatedFiles);
            }
        }
    }

    private PMMLResource parseResource(Resource resource) {
        final InternalKnowledgeBase knowledgeBase = new KnowledgeBaseImpl("PMML", null);
        KnowledgeBuilderImpl kbuilderImpl = new KnowledgeBuilderImpl(knowledgeBase);
        List<KiePMMLModel> kiePMMLModels = getKiePMMLModelsFromResourceWithSources(kbuilderImpl, resource);
        String modelPath = resource.getSourcePath();
        return new PMMLResource(kiePMMLModels, new File(resource.getSourcePath()).toPath(), modelPath);
    }
}
