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
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.ReleaseId;
import org.kie.api.builder.model.KieModuleModel;
import org.kie.dmn.core.compiler.*;

import static org.drools.compiler.kie.builder.impl.KieBuilderImpl.setDefaultsforEmptyKieModule;

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
        if (ExecModelMode.shouldGenerateModel(generateModel)) {
            generateDMNModel();
        }
    }

    private void generateDMNModel() throws MojoExecutionException {
        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();

        KieServices ks = KieServices.Factory.get();

        try {
            setSystemProperties(properties);

            System.setProperty(ExecModelCompilerOption.PROPERTY_NAME, Boolean.TRUE.toString());

            final KieBuilderImpl kieBuilder = (KieBuilderImpl) ks.newKieBuilder(projectDir);
            kieBuilder.buildAll(ExecutableModelDMNMavenProject.class);

//            InternalKieModule kieModule = (InternalKieModule) kieBuilder.getKieModule();
//            List<String> generatedFiles = kieModule.getFileNames()
//                    .stream()
//                    .filter(f -> f.endsWith("java"))
//                    .collect(Collectors.toList());
//
//            getLog().info(String.format("Found %d generated files in Canonical Model", generatedFiles.size()));
//
//            MemoryFileSystem mfs = kieModule instanceof CanonicalKieModule ?
//                    ((MemoryKieModule) ((CanonicalKieModule) kieModule).getInternalKieModule()).getMemoryFileSystem() :
//                    ((MemoryKieModule) kieModule).getMemoryFileSystem();
//
//            final String droolsModelCompilerPath = "/generated-sources/drools-model-compiler/main/java";
//            final String newCompileSourceRoot = targetDirectory.getPath() + droolsModelCompilerPath;
//            project.addCompileSourceRoot(newCompileSourceRoot);
//
//            for (String generatedFile : generatedFiles) {
//                final MemoryFile f = (MemoryFile) mfs.getFile(generatedFile);
//                final Path newFile = Paths.get(targetDirectory.getPath(),
//                                               droolsModelCompilerPath,
//                                               f.getPath().toPortableString());
//
//                try {
//                    Files.deleteIfExists(newFile);
//                    Files.createDirectories(newFile);
//                    Files.copy(f.getContents(), newFile, StandardCopyOption.REPLACE_EXISTING);
//
//                    getLog().info("Generating " + newFile);
//                } catch (IOException e) {
//                    e.printStackTrace();
//                    throw new MojoExecutionException("Unable to write file", e);
//                }
//            }
//
//            // copy the META-INF packages file
//            final MemoryFile packagesMemoryFile = (MemoryFile) mfs.getFile(CanonicalKieModule.MODEL_FILE);
//            final String packagesMemoryFilePath = packagesMemoryFile.getFolder().getPath().toPortableString();
//            final Path packagesDestinationPath = Paths.get(targetDirectory.getPath(), "classes", packagesMemoryFilePath, packagesMemoryFile.getName());
//
//            try {
//                if (!Files.exists(packagesDestinationPath)) {
//                    Files.createDirectories(packagesDestinationPath);
//                }
//                Files.copy(packagesMemoryFile.getContents(), packagesDestinationPath, StandardCopyOption.REPLACE_EXISTING);
//            } catch (IOException e) {
//                e.printStackTrace();
//                throw new MojoExecutionException("Unable to write file", e);
//            }
//
//            if (ExecModelMode.shouldDeleteFile(generateModel)) {
//                deleteDrlFiles();
//            }
        } finally {
            Thread.currentThread().setContextClassLoader(contextClassLoader);
        }

        getLog().info("DMN Model successfully generated");
    }

    public static class ExecutableModelDMNMavenProject implements KieBuilder.ProjectType {

        public static final BiFunction<InternalKieModule, ClassLoader, KieModuleKieProject> SUPPLIER = ExecutableModelDMNMavenPluginKieProject::new;

        public static class ExecutableModelDMNMavenPluginKieProject extends CanonicalModelKieProject {

            public ExecutableModelDMNMavenPluginKieProject(InternalKieModule kieModule, ClassLoader classLoader) {
                super(true, kieModule, classLoader);
            }

            @Override
            public void writeProjectOutput(MemoryFileSystem trgMfs, ResultsImpl messages) {
                MemoryFileSystem srcMfs = new MemoryFileSystem();
                List<String> modelFiles = new ArrayList<>();
                ModelWriter modelWriter = new ModelWriter();

                System.out.println("++++ modelBuilders = " + modelBuilders);

//                for (ModelBuilderImpl modelBuilder : modelBuilders) {
//                    ModelWriter.Result result = modelWriter.writeModel(srcMfs, modelBuilder.getPackageModels());
//                    modelFiles.addAll(result.getModelFiles());
//                    final Folder sourceFolder = srcMfs.getFolder("src/main/java");
//                    final Folder targetFolder = trgMfs.getFolder(".");
//                    srcMfs.copyFolder(sourceFolder, trgMfs, targetFolder);
//                }
//                modelWriter.writeModelFile(modelFiles, trgMfs);
            }
        }
    }
}
