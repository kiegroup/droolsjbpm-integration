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
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.artifact.resolver.filter.CumulativeScopeArtifactFilter;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.drools.ancompiler.CompiledNetworkSource;
import org.drools.ancompiler.ObjectTypeNodeCompiler;
import org.drools.compiler.kproject.ReleaseIdImpl;
import org.drools.core.impl.InternalKnowledgeBase;
import org.drools.modelcompiler.CanonicalKieModule;
import org.kie.api.KieServices;
import org.kie.api.runtime.KieContainer;

import static org.kie.maven.plugin.ExecModelMode.ancEnabled;
import static org.kie.maven.plugin.ExecModelMode.isModelCompilerInClassPath;

@Mojo(name = "generateANC",
        requiresDependencyResolution = ResolutionScope.NONE,
        defaultPhase = LifecyclePhase.COMPILE)
public class GenerateANCMojo extends AbstractDMNValidationAwareMojo {

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

    private static final String ALPHA_NETWORK_COMPILER_PATH = "/generated-sources/alpha-network-compiler/main/java";

    @Override
    public void execute() throws MojoExecutionException {
        // GenerateModelMojo is executed when BuildMojo isn't and vice-versa
        boolean ancParameterEnabled = ancEnabled(getGenerateModelOption());
        boolean modelCompilerInClassPath = isModelCompilerInClassPath(project.getDependencies());
        if (ancParameterEnabled && modelCompilerInClassPath) {
            generateANC();
        } else if (ancParameterEnabled) { // !modelCompilerInClassPath
            getLog().warn("You're trying to build rule assets in a project from an executable rule model, but you did not provide the required dependency on the project classpath.\n" +
                                  "To enable executable rule models for your project, add the `drools-model-compiler` dependency in the `pom.xml` file of your project.\n");
        }
    }

    private void generateANC() throws MojoExecutionException {
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

        try {
            setSystemProperties(properties);

            KieServices ks = KieServices.Factory.get();

            KieContainer kieContainer = ks.newKieContainer(new ReleaseIdImpl(project.getGroupId(),
                                                                             project.getArtifactId(),
                                                                             project.getVersion()));

            for (String kbase : kieContainer.getKieBaseNames()) {
                InternalKnowledgeBase kieBase = (InternalKnowledgeBase) kieContainer.getKieBase(kbase);

                List<CompiledNetworkSource> ancSourceFiles = ObjectTypeNodeCompiler.compiledNetworkSources(kieBase.getRete());

                getLog().info(String.format("Found %d generated files in Knowledge Base %s", ancSourceFiles.size(), kbase));

                final String alphaNetworkCompilerPath = ALPHA_NETWORK_COMPILER_PATH;
                final String newCompileSourceRoot = targetDirectory.getPath() + alphaNetworkCompilerPath;
                project.addCompileSourceRoot(newCompileSourceRoot);

                for (CompiledNetworkSource generatedFile : ancSourceFiles) {
                    final Path newFile = Paths.get(targetDirectory.getPath(),
                                                   alphaNetworkCompilerPath,
                                                   generatedFile.getSourceName());

                    try {
                        Files.deleteIfExists(newFile);
                        Files.createDirectories(newFile.getParent());
                        byte[] bytes = generatedFile.getSource().getBytes(StandardCharsets.UTF_8);
                        Files.write(newFile,
                                    bytes,
                                    StandardOpenOption.CREATE,
                                    StandardOpenOption.TRUNCATE_EXISTING);

                        getLog().info("Written Compiled Alpha Network: " + newFile);
                    } catch (IOException e) {
                        e.printStackTrace();
                        throw new MojoExecutionException("Unable to write file", e);
                    }
                }
            }

            // generate the ANC file
            String ancFile = CanonicalKieModule.getANCFile(new ReleaseIdImpl(
                    project.getGroupId(),
                    project.getArtifactId(),
                    project.getVersion()
            ));
            final Path ancFilePath = Paths.get(targetDirectory.getPath(),
                                               "classes",
                                               ancFile);

            try {
                Files.deleteIfExists(ancFilePath);
                Files.createDirectories(ancFilePath.getParent());
                Files.createFile(ancFilePath);
                getLog().info("Written ANC File: " + ancFilePath.toAbsolutePath());
            } catch (IOException e) {
                e.printStackTrace();
                throw new MojoExecutionException("Unable to write file: ", e);
            }
        } finally {
            Thread.currentThread().setContextClassLoader(contextClassLoader);
        }

        getLog().info("Compiled Alpha Network successfully generated");
    }
}
