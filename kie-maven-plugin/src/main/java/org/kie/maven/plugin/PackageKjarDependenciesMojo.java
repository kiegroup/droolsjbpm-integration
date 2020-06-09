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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.io.Files;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.handler.ArtifactHandler;
import org.apache.maven.artifact.handler.manager.ArtifactHandlerManager;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.ArtifactRepositoryPolicy;
import org.apache.maven.artifact.repository.MavenArtifactRepository;
import org.apache.maven.artifact.repository.layout.ArtifactRepositoryLayout;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.DefaultProjectBuildingRequest;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuildingRequest;
import org.apache.maven.repository.RepositorySystem;
import org.apache.maven.settings.Settings;
import org.apache.maven.shared.artifact.ArtifactCoordinate;
import org.apache.maven.shared.artifact.DefaultArtifactCoordinate;
import org.apache.maven.shared.artifact.resolve.ArtifactResolver;
import org.apache.maven.shared.artifact.resolve.ArtifactResolverException;
import org.apache.maven.shared.artifact.resolve.ArtifactResult;
import org.apache.maven.shared.dependencies.DefaultDependableCoordinate;
import org.apache.maven.shared.dependencies.resolve.DependencyResolver;
import org.apache.maven.shared.dependencies.resolve.DependencyResolverException;
import org.apache.maven.shared.utils.StringUtils;

@Mojo(name = "package-dependencies-kjar",
      defaultPhase = LifecyclePhase.PREPARE_PACKAGE,
      threadSafe = true,
      requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME)
public class PackageKjarDependenciesMojo extends AbstractKieMojo {

    private static final Pattern ALT_REPO_SYNTAX_PATTERN = Pattern.compile("(.+)::(.*)::(.+)");

    @Parameter(defaultValue = "${project.build.outputDirectory}", required = true, readonly = true)
    private String outputDirectory;

    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    private MavenProject project;

    @Parameter
    private String classifier;

    @Parameter(defaultValue = "${session}", readonly = true, required = true)
    private MavenSession session;

    @Component
    private ArtifactResolver artifactResolver;

    @Component
    private DependencyResolver dependencyResolver;

    @Parameter(defaultValue = "${project.remoteArtifactRepositories}", readonly = true, required = true)
    private List<ArtifactRepository> pomRemoteRepositories;

    @Parameter(property = "remoteRepositories")
    private String remoteRepositories;

    @Component
    private RepositorySystem repositorySystem;

    @Component
    private ArtifactHandlerManager artifactHandlerManager;

    @Component(role = ArtifactRepositoryLayout.class)
    private Map<String, ArtifactRepositoryLayout> repositoryLayouts;

    @Parameter
    private List<ArtifactItem> artifactItems;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            if (artifactItems == null || artifactItems.isEmpty()) {
                getLog().info("Skipping plugin execution");
                return;
            }

            ArtifactRepositoryPolicy always =
                    new ArtifactRepositoryPolicy(true, ArtifactRepositoryPolicy.UPDATE_POLICY_ALWAYS,
                                                 ArtifactRepositoryPolicy.CHECKSUM_POLICY_WARN);

            List<ArtifactRepository> repoList = new ArrayList<>();

            if (pomRemoteRepositories != null) {
                repoList.addAll(pomRemoteRepositories);
            }

            if (remoteRepositories != null) {
                // Use the same format as in the deploy plugin id::layout::url
                String[] repos = StringUtils.split(remoteRepositories, ",");
                for (String repo : repos) {
                    repoList.add(parseRepository(repo, always));
                }
            }

            File outputFolder = new File(outputDirectory + "/KIE-INF/lib");
            if (!outputFolder.exists()) {
                outputFolder.mkdirs();
            }
            getLog().info("Create directory: " + outputFolder);

            for (ArtifactItem artifactItem : artifactItems) {
                DefaultDependableCoordinate coordinate = new DefaultDependableCoordinate();
                coordinate.setArtifactId(artifactItem.getArtifactId());
                coordinate.setGroupId(artifactItem.getGroupId());
                coordinate.setVersion(artifactItem.getVersion());
                coordinate.setClassifier(artifactItem.getClassifier());
                coordinate.setType(artifactItem.getType());

                getLog().info("Resolving " + coordinate + " with transitive dependencies");

                ProjectBuildingRequest buildingRequest = new DefaultProjectBuildingRequest(session.getProjectBuildingRequest());
                Settings settings = session.getSettings();
                repositorySystem.injectMirror(repoList, settings.getMirrors());
                repositorySystem.injectProxy(repoList, settings.getProxies());
                repositorySystem.injectAuthentication(repoList, settings.getServers());
                buildingRequest.setRemoteRepositories(repoList);

                Iterable<ArtifactResult> results = dependencyResolver.resolveDependencies(buildingRequest, coordinate, null);
                for (ArtifactResult artifact : results) {
                    getLog().info("Copying dependencies: " + artifact.getArtifact());
                    ArtifactResult artifactResolverResult = artifactResolver.resolveArtifact(buildingRequest, toArtifactCoordinate(artifact.getArtifact()));
                    Artifact artifactResolved = artifactResolverResult.getArtifact();
                    File local = artifactResolved.getFile();
                    Files.copy(local, new File(outputFolder, local.getName()));
                }
 
            }
        } catch (IOException | ArtifactResolverException | DependencyResolverException e) {
            throw new MojoExecutionException("Couldn't download artifact: " + e.getMessage(), e);
        }

    }

    private ArtifactCoordinate toArtifactCoordinate(Artifact dependableCoordinate) {
        ArtifactHandler artifactHandler = artifactHandlerManager.getArtifactHandler(dependableCoordinate.getType());
        DefaultArtifactCoordinate artifactCoordinate = new DefaultArtifactCoordinate();
        artifactCoordinate.setGroupId(dependableCoordinate.getGroupId());
        artifactCoordinate.setArtifactId(dependableCoordinate.getArtifactId());
        artifactCoordinate.setVersion(dependableCoordinate.getVersion());
        artifactCoordinate.setClassifier(dependableCoordinate.getClassifier());
        artifactCoordinate.setExtension(artifactHandler.getExtension());
        return artifactCoordinate;
    }

    private ArtifactRepository parseRepository(String repo, ArtifactRepositoryPolicy policy) throws MojoFailureException {
        // if it's a simple url
        String id = "temp";
        ArtifactRepositoryLayout layout = getLayout("default");
        String url = repo;

        // if it's an extended repo URL of the form id::layout::url
        if (repo.contains("::")) {
            Matcher matcher = ALT_REPO_SYNTAX_PATTERN.matcher(repo);
            if (!matcher.matches()) {
                throw new MojoFailureException(repo, "Invalid syntax for repository: " + repo,
                                               "Invalid syntax for repository. Use \"id::layout::url\" or \"URL\".");
            }

            id = matcher.group(1).trim();
            if (!StringUtils.isEmpty(matcher.group(2))) {
                layout = getLayout(matcher.group(2).trim());
            }
            url = matcher.group(3).trim();
        }
        return new MavenArtifactRepository(id, url, layout, policy, policy);
    }

    private ArtifactRepositoryLayout getLayout(String id) throws MojoFailureException {
        ArtifactRepositoryLayout layout = repositoryLayouts.get(id);

        if (layout == null) {
            throw new MojoFailureException(id, "Invalid repository layout", "Invalid repository layout: " + id);
        }

        return layout;
    }


}