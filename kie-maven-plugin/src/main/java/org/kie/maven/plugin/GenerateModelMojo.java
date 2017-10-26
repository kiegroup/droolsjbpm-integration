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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
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
import org.drools.compiler.compiler.io.memory.MemoryFile;
import org.drools.compiler.compiler.io.memory.MemoryFileSystem;
import org.drools.compiler.kie.builder.impl.InternalKieModule;
import org.drools.compiler.kie.builder.impl.KieBuilderImpl;
import org.drools.compiler.kie.builder.impl.MemoryKieModule;
import org.drools.compiler.kie.builder.impl.ZipKieModule;
import org.drools.compiler.kproject.ReleaseIdImpl;
import org.drools.compiler.kproject.models.KieModuleModelImpl;
import org.drools.modelcompiler.builder.CanonicalModelMavenPluginKieProject;
import org.kie.api.KieServices;
import org.kie.api.builder.ReleaseId;
import org.kie.api.builder.model.KieModuleModel;

import static org.drools.compiler.kie.builder.impl.KieBuilderImpl.setDefaultsforEmptyKieModule;

@Mojo(name = "generateModel",
        requiresDependencyResolution = ResolutionScope.NONE,
        requiresProject = true,
        defaultPhase = LifecyclePhase.COMPILE)
public class GenerateModelMojo extends AbstractKieMojo {

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

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {

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
            kieBuilder.buildAll(CanonicalModelMavenPluginKieProject::new);

            InternalKieModule kieModule = (InternalKieModule) kieBuilder.getKieModule();
            List<String> generatedFiles = kieModule.getFileNames()
                    .stream()
                    .filter(f -> f.endsWith("java"))
                    .collect(Collectors.toList());

            getLog().info(String.format("Found %d generated files in Canonical Model", generatedFiles.size()));

            MemoryFileSystem mfs = ((MemoryKieModule) kieModule).getMemoryFileSystem();

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
                    Files.createDirectories(newFile);
                    Files.copy(f.getContents(), newFile, StandardCopyOption.REPLACE_EXISTING);

                    getLog().info("Generating " + newFile);
                } catch (IOException e) {
                    e.printStackTrace();
                    throw new MojoExecutionException("Unable to write file", e);
                }
            }


            // copy the META-INF packages file
            final MemoryFile packagesMemoryFile = (MemoryFile) mfs.getFile("META-INF/packages");
            final Path packagesDestinationPath = Paths.get(targetDirectory.getPath(), "classes", "META-INF", packagesMemoryFile.getName());

            try {
                Files.copy(packagesMemoryFile.getContents(), packagesDestinationPath, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                e.printStackTrace();
                throw new MojoExecutionException("Unable to write file", e);
            }


        } finally {
            Thread.currentThread().setContextClassLoader(contextClassLoader);
        }

        getLog().info("DSL successfully generated");
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
