/*
 * Copyright 2014 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kie.integration.eap.maven;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectDependenciesResolver;
import org.kie.integration.eap.maven.eap.EAPFileSystemBaseModulesScanner;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.repository.RemoteRepository;

import java.util.List;

/**
 * This plugin mojo generates all the module descriptors for a given JBoss EAP/AS version.
 *
 * @goal generate-eap-base-module-descriptors
 * @requiresProject true
 */
public class EAPBaseModulesDescriptorGenerationMojo extends AbstractMojo {

    /**
     * The Maven project.
     *
     * @parameter property="project"
     * @required
     * @readonly
     */
    private MavenProject project;

    /**
     * The entry point to Aether, i.e. the component doing all the work.
     *
     * @component
     */
    private RepositorySystem repoSystem;

    /**
     * @component
     */
    protected ProjectDependenciesResolver projectDependenciesResolver;

    /**
     * The current repository/network configuration of Maven.
     *
     * @parameter default-value="${repositorySystemSession}"
     * @readonly
     */
    private RepositorySystemSession repoSession;

    /**
     * The project's remote repositories to use for the resolution of plugins and their dependencies.
     *
     * @parameter default-value="${project.remoteProjectRepositories}"
     * @readonly
     */
    private List<RemoteRepository> remoteRepos;

    /**
     * The root path of a JBoss EAP installation, to scan modules and resources..
     *
     * @parameter default-value=""
     */
    private String eapRootPath;

    /**
     * The full path for the generated output resources file..
     *
     * @parameter default-value=""
     */
    private String outputFilePath;

    /**
     * The full path for the temporary files. If not set, the java.io.tmpdir will be used as default..
     *
     * @parameter default-value=""
     */
    private String tempPath;

    /**
     * The name of the JBoss application server.
     *
     * @parameter default-value=""
     */
    private String eapName;

    /**
     * The version for the maven modules to generate.
     *
     * @parameter default-value=""
     */
    private String mavenModulesVersion;


    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        if (eapRootPath == null || eapRootPath.trim().length() == 0) throw new MojoFailureException("EAP root path is not set.");
        if (outputFilePath == null || outputFilePath.trim().length() == 0) throw new MojoFailureException("Output file path is not set.");
        if (eapName == null || eapName.trim().length() == 0) throw new MojoFailureException("The resulting distributio JBoss EAP/AS name is not set.");

        if (mavenModulesVersion == null || mavenModulesVersion.trim().length() == 0) {
            getLog().warn("No mavenModulesVersion goal property set. By default the current project version will be used.");
            mavenModulesVersion = project.getVersion();
        }
        
        getLog().info("Starting the generation of base EAP static modules present in path " + eapRootPath);
        EAPFileSystemBaseModulesScanner generator = new EAPFileSystemBaseModulesScanner(eapRootPath, eapName, mavenModulesVersion, outputFilePath);
        generator.setLogger(getLog());
        generator.setTempPath(tempPath);
        try {
            generator.generate();
        } catch (Exception e) {
            throw new MojoExecutionException("Error generating EAP base module descriptors.");
        }
        getLog().info("Base EAP static module descriptor sucessfully generated at " + outputFilePath);
    }
}
