/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package org.kie.server.maven.plugin;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.kie.server.gateway.KieServerGateway;

public abstract class KieServerAbstactMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project}", required = false, readonly = true)
    protected MavenProject project;

    @Parameter(property = "deploy.container")
    protected String container;

    @Parameter(property = "deploy.hostname", defaultValue = "localhost")
    protected String hostname;

    @Parameter(property = "deploy.port", defaultValue = "8080")
    protected Integer port;

    @Parameter(property = "deploy.protocol", defaultValue = "http")
    protected String protocol;

    @Parameter(property = "deploy.context-path", defaultValue = "kie-server")
    protected String contextPath;

    @Parameter(property = "deploy.username", required = true, readonly = true)
    protected String username;

    @Parameter(property = "deploy.password", required = true, readonly = true)
    protected String password;

    @Parameter(property = "deploy.timeout", defaultValue = "30000")
    protected Integer timeout;

    protected KieServerGateway kieServerGateway;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        getLog().info(project.toString());
        getLog().info("Protocol: " + protocol);
        getLog().info("Host Name: " + hostname);
        getLog().info("Port: " + port);
        getLog().info("Username: " + username);
        getLog().info("Password: ###SECRET###");
        getLog().info("Timeout: " + timeout);
        getLog().info("Context Path: " + contextPath);

        // default container name is the maven GAV
        // it enables the unified execution
        if (container == null) {
            container = project.getGroupId() + ":" + project.getArtifactId() + ":" + project.getVersion();
        }

        kieServerGateway = new KieServerGateway(protocol, hostname, port, username, password, timeout, contextPath);
        kieServerGateway.init();

        excecuteCommand();

    }

    public abstract void excecuteCommand() throws MojoExecutionException, MojoFailureException;

}
