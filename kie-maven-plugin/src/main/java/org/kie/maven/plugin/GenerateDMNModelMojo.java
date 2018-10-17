package org.kie.maven.plugin;

import java.io.File;
import java.nio.file.FileSystems;
import java.nio.file.PathMatcher;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.drools.compiler.kie.builder.impl.InternalKieModule;
import org.drools.compiler.kie.builder.impl.KieBuilderImpl;
import org.kie.api.KieServices;
import org.kie.api.io.Resource;
import org.kie.api.io.ResourceConfiguration;
import org.kie.api.io.ResourceType;
import org.kie.api.io.ResourceWithConfiguration;
import org.kie.dmn.api.core.AfterGeneratingSourcesListener;
import org.kie.dmn.core.api.DMNFactory;
import org.kie.dmn.core.assembler.DMNAssemblerService;
import org.kie.dmn.core.compiler.DMNCompilerConfigurationImpl;
import org.kie.dmn.core.compiler.ExecModelCompilerOption;
import org.kie.internal.builder.KnowledgeBuilder;
import org.kie.internal.builder.KnowledgeBuilderFactory;
import org.kie.internal.io.ResourceWithConfigurationImpl;

@Mojo(name = "generateDMNModel",
        requiresDependencyResolution = ResolutionScope.NONE,
        requiresProject = true,
        defaultPhase = LifecyclePhase.COMPILE)
public class GenerateDMNModelMojo extends AbstractKieMojo {

    public static PathMatcher drlFileMatcher = FileSystems.getDefault().getPathMatcher("glob:**.drl");

    @Parameter(required = true, defaultValue = "${project.build.directory}")
    private File targetDirectory;

    @Parameter(required = true, defaultValue = "${project.basedir}")
    private File projectDir;

    @Parameter
    private Map<String, String> properties;

    @Parameter(required = true, defaultValue = "${project}")
    private MavenProject project;

    @Parameter(required = true, defaultValue = "${project.build.outputDirectory}")
    private File outputDirectory;

    @Parameter(property = "generateModel", defaultValue = "no")
    private String generateModel;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        generateDMNModel();
    }

    private void generateDMNModel() throws MojoExecutionException {
        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();

        KieServices ks = KieServices.Factory.get();

        try {
            setSystemProperties(properties);

            final KieBuilderImpl kieBuilder = (KieBuilderImpl) ks.newKieBuilder(projectDir);

            DMNCompilerConfigurationImpl dmnCompilerConfiguration = (DMNCompilerConfigurationImpl) DMNFactory.newCompilerConfiguration();

            dmnCompilerConfiguration.setProperty(ExecModelCompilerOption.PROPERTY_NAME, Boolean.TRUE.toString());
            dmnCompilerConfiguration.addListener(new AfterGeneratingSourcesListener() {
                @Override
                public void accept(List<AfterGeneratingSourcesListener.GeneratedSource> generatedSource) {
                    getLog().info("generatedSource = " + generatedSource);
                }
            });

            InternalKieModule kieModule = (InternalKieModule) kieBuilder.getKieModuleIgnoringErrors();
            List<String> dmnFiles = kieModule.getFileNames()
                    .stream()
                    .filter(f -> f.endsWith("dmn"))
                    .collect(Collectors.toList());

            getLog().info("dmnFiles = " + dmnFiles);

            DMNAssemblerService assemblerService = new DMNAssemblerService(dmnCompilerConfiguration);
            KnowledgeBuilder knowledgeBuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();

            for (String dmnFile : dmnFiles) {

                Resource resource = kieModule.getResource(dmnFile);
                ResourceConfiguration resourceConfiguration = kieModule.getResourceConfiguration(dmnFile);

                ResourceWithConfiguration resourceWithConfiguration =
                        new ResourceWithConfigurationImpl(resource, resourceConfiguration, a -> {
                        }, b -> {
                        });

                assemblerService.addResources(knowledgeBuilder, Collections.singletonList(resourceWithConfiguration), ResourceType.DMN);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            Thread.currentThread().setContextClassLoader(contextClassLoader);
        }

        getLog().info("DMN Model successfully generated");
    }
}

