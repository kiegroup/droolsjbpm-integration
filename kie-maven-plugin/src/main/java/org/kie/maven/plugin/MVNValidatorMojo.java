package org.kie.maven.plugin;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

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
import org.drools.compiler.commons.jci.compilers.CompilationResult;
import org.drools.compiler.compiler.io.Folder;
import org.drools.compiler.compiler.io.memory.MemoryFile;
import org.drools.compiler.compiler.io.memory.MemoryFileSystem;
import org.drools.compiler.kie.builder.impl.InternalKieModule;
import org.drools.compiler.kie.builder.impl.KieBuilderImpl;
import org.drools.compiler.kie.builder.impl.KieModuleKieProject;
import org.drools.compiler.kie.builder.impl.MemoryKieModule;
import org.drools.compiler.kie.builder.impl.ResultsImpl;
import org.drools.compiler.kie.builder.impl.ZipKieModule;
import org.drools.compiler.kproject.ReleaseIdImpl;
import org.drools.compiler.kproject.models.KieModuleModelImpl;
import org.drools.modelcompiler.CanonicalKieModule;
import org.drools.modelcompiler.builder.CanonicalModelKieProject;
import org.drools.modelcompiler.builder.ModelBuilderImpl;
import org.drools.modelcompiler.builder.ModelWriter;
import org.drools.modelcompiler.builder.PackageModel;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.Message;
import org.kie.api.builder.ReleaseId;
import org.kie.api.builder.model.KieModuleModel;
import org.kie.internal.jci.CompilationProblem;

import static java.util.stream.Collectors.groupingBy;
import static org.drools.compiler.kie.builder.impl.KieBuilderImpl.setDefaultsforEmptyKieModule;
import static org.drools.modelcompiler.builder.JavaParserCompiler.getCompiler;

@Mojo(name = "mvnValidator",
        requiresDependencyResolution = ResolutionScope.NONE,
        requiresProject = true,
        defaultPhase = LifecyclePhase.COMPILE)
public class MVNValidatorMojo extends AbstractKieMojo {

    public static PathMatcher drlFileMatcher = FileSystems.getDefault().getPathMatcher("glob:**.drl");

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

    @Parameter(property = "generateModel", defaultValue = "no")
    private String generateModel;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        if (ExecModelMode.shouldGenerateModel(generateModel)) {
            generateModel();
        }
    }

    private void generateModel() throws MojoExecutionException {
        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();

        List<InternalKieModule> kmoduleDeps = new ArrayList<>();

        KieServices ks = KieServices.Factory.get();

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
                                                                        getClass().getClassLoader());

            Thread.currentThread().setContextClassLoader(projectClassLoader);
        } catch (DependencyResolutionRequiredException | MalformedURLException e) {
            throw new RuntimeException(e);
        }

        try {
            setSystemProperties(properties);

            final KieBuilderImpl kieBuilder = (KieBuilderImpl) ks.newKieBuilder(projectDir);
            kieBuilder.buildAll(ExecutableModelMavenProject.SUPPLIER, s -> {
                return !s.contains("src/test/java");
            });

        } finally {
            Thread.currentThread().setContextClassLoader(contextClassLoader);
        }

        getLog().info("DSL successfully generated");
    }

    private void deleteDrlFiles() throws MojoExecutionException {
        // Remove drl files
        try {
            final Stream<Path> drlFiles = Files.find(outputDirectory.toPath(), Integer.MAX_VALUE, (p, f) -> drlFileMatcher.matches(p));
            drlFiles.forEach(p -> {
                try {
                    Files.delete(p);
                } catch (IOException e) {
                    e.printStackTrace();
                    throw new RuntimeException("Unable to delete file " + p);
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
            throw new MojoExecutionException("Unable to find .drl files");
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

    public static class ExecutableModelMavenProject implements KieBuilder.ProjectType {

        public static final BiFunction<InternalKieModule, ClassLoader, KieModuleKieProject> SUPPLIER = ExecutableModelMavenPluginKieProject::new;

        public static class ExecutableModelMavenPluginKieProject extends CanonicalModelKieProject {

            public ExecutableModelMavenPluginKieProject(InternalKieModule kieModule, ClassLoader classLoader) {
                super(true, kieModule, classLoader);
            }

            @Override
            public void writeProjectOutput(MemoryFileSystem trgMfs, ResultsImpl messages) {

                MemoryFileSystem srcMfs = new MemoryFileSystem();
                ModelWriter modelWriter = new ModelWriter();
                List<String> modelFiles = new ArrayList<>();

                for (ModelBuilderImpl modelBuilder : modelBuilders) {
                    final ModelWriter.Result result = modelWriter.writeModel( srcMfs, modelBuilder.getPackageModels() );
                    modelFiles.addAll(result.getModelFiles());
                    final String[] sources = result.getSources();
                    if(sources.length != 0) {
                        CompilationResult res = getCompiler().compile(sources, srcMfs, trgMfs, getClassLoader());

                        for (PackageModel pm : modelBuilder.getPackageModels()) {
                            pm.validateConsequence(getClassLoader(), trgMfs, messages);
                        }

                        Stream.of(res.getErrors()).collect(groupingBy(CompilationProblem::getFileName))
                                .forEach( (name, errors) -> {
                                    errors.forEach( messages::addMessage );
                                    org.drools.compiler.compiler.io.File srcFile = srcMfs.getFile(name );
                                    if ( srcFile instanceof MemoryFile ) {
                                        String src = new String ( srcMfs.getFileContents( ( MemoryFile ) srcFile ) );
                                        messages.addMessage(Message.Level.ERROR, name, "Java source of " + name + " in error:\n" + src);
                                    }
                                } );

                        for (CompilationProblem problem : res.getWarnings()) {
                            messages.addMessage(problem);
                        }
                    }
                }

            }
        }
    }
}
