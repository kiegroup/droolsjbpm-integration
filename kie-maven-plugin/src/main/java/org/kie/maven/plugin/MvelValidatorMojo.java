package org.kie.maven.plugin;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.drools.compiler.compiler.io.memory.MemoryFileSystem;
import org.drools.compiler.kie.builder.impl.InternalKieModule;
import org.drools.compiler.kie.builder.impl.KieBuilderImpl;
import org.drools.compiler.kie.builder.impl.KieModuleKieProject;
import org.drools.compiler.kie.builder.impl.ResultsImpl;
import org.drools.modelcompiler.builder.CanonicalModelKieProject;
import org.drools.modelcompiler.builder.ModelBuilderImpl;
import org.drools.modelcompiler.builder.ModelWriter;
import org.drools.modelcompiler.builder.PackageModel;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.drools.modelcompiler.builder.JavaParserCompiler.getCompiler;

@Mojo(name = "mvelValidator",
        requiresDependencyResolution = ResolutionScope.NONE,
        requiresProject = true,
        defaultPhase = LifecyclePhase.COMPILE)
public class MvelValidatorMojo extends AbstractKieMojo {

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
        if (ExecModelMode.shouldValidateMVEL(generateModel)) {
            getLog().info("Starting MVEL Validation " + 4);
            validateMVEL();
        }
    }

    private void validateMVEL() throws MojoExecutionException {
        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();

        KieServices ks = KieServices.Factory.get();

        try {
            setSystemProperties(properties);

            final KieBuilderImpl kieBuilder = (KieBuilderImpl) ks.newKieBuilder(projectDir);
            kieBuilder.buildAll((k, c) -> new ExecutableModelMavenPluginKieProject(outputDirectory, k, c), s -> {
                return !s.contains("src/test/java");
            });
        } finally {
            Thread.currentThread().setContextClassLoader(contextClassLoader);
        }
    }

    public static class ExecutableModelMavenPluginKieProject extends CanonicalModelKieProject {

        private File outputDirectory;

        public ExecutableModelMavenPluginKieProject(File outputDirectory, InternalKieModule kieModule, ClassLoader classLoader) {
            super(true, kieModule, classLoader);
            this.outputDirectory = outputDirectory;
        }

        Logger logger = LoggerFactory.getLogger(ExecutableModelMavenPluginKieProject.class);

        @Override
        public void writeProjectOutput(MemoryFileSystem trgMfs, ResultsImpl messages) {
            MemoryFileSystem srcMfs = new MemoryFileSystem();
            ModelWriter modelWriter = new ModelWriter();

            modelBuilders.forEach(m -> logger.info(m.toString()));

            for (ModelBuilderImpl modelBuilder : modelBuilders) {
                final ModelWriter.Result result = modelWriter.writeModel(srcMfs, modelBuilder.getPackageModels());
                final String[] sources = result.getSources();
                if (sources.length != 0) {
                    getCompiler().compile(sources, srcMfs, trgMfs, getClassLoader());

                    for (PackageModel pm : modelBuilder.getPackageModels()) {
                        pm.validateConsequence(getClassLoader(), trgMfs, messages);
                    }

                    List<Message> errorMessages = messages.getMessages(Message.Level.ERROR);
                    if (!errorMessages.isEmpty()) {
                        String errorMessagesString = errorMessages.stream()
                                .map(Object::toString)
                                .collect(Collectors.joining("\n"));
                        throw new RuntimeException(new MojoExecutionException(errorMessagesString));
                    }
                }
            }
        }
    }
}
