package org.kie.maven.plugin;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.drools.compiler.compiler.io.memory.MemoryFile;
import org.drools.compiler.compiler.io.memory.MemoryFileSystem;
import org.drools.compiler.kie.builder.impl.InternalKieModule;
import org.drools.compiler.kie.builder.impl.KieBuilderImpl;
import org.drools.compiler.kie.builder.impl.MemoryKieModule;
import org.drools.modelcompiler.builder.CanonicalModelKieProject;
import org.kie.api.KieServices;

@Mojo(name = "generateModel",
        requiresDependencyResolution = ResolutionScope.NONE,
        requiresProject = true,
        defaultPhase = LifecyclePhase.GENERATE_SOURCES)
public class GenerateModelMojo extends AbstractKieMojo {

    @Parameter(required = true, defaultValue = "${project.build.directory}")
    private File targetDirectory;

    @Parameter(required = true, defaultValue = "${project.basedir}")
    private File projectDir;

    @Parameter
    private Map<String, String> properties;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {

        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();

        KieServices ks = KieServices.Factory.get();

        try {
            setSystemProperties(properties);

            final KieBuilderImpl kieBuilder = (KieBuilderImpl) ks.newKieBuilder(projectDir);
            kieBuilder.buildAll(CanonicalModelKieProject::new);

            InternalKieModule kieModule = (InternalKieModule) kieBuilder.getKieModule();
            List<String> generatedFiles = kieModule.getFileNames()
                    .stream()
                    .filter(f -> f.endsWith("java"))
                    .collect(Collectors.toList());

            MemoryFileSystem mfs = ((MemoryKieModule) kieModule).getMemoryFileSystem();

            for (String generatedFile : generatedFiles) {
                MemoryFile f = (MemoryFile) mfs.getFile(generatedFile);
                final Path newFile = Paths.get(targetDirectory.getPath(), f.getPath().toPortableString());

                try {
                    Files.createDirectories(newFile);
                    Files.copy(f.getContents(), newFile, StandardCopyOption.REPLACE_EXISTING);
                    getLog().info("Generating " + newFile);
                } catch (IOException e) {
                    throw new MojoExecutionException("Unable to write file", e);
                }
            }
        } finally {
            Thread.currentThread().setContextClassLoader(contextClassLoader);
        }

        getLog().info("DSL successfully generated");
    }
}
