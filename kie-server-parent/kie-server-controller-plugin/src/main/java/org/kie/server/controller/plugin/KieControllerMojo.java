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

package org.kie.server.controller.plugin;

import javax.ws.rs.ProcessingException;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Parameter;
import org.kie.server.gateway.KieControllerGateway;

public abstract class KieControllerMojo extends AbstractMojo {

    @Parameter(property = "kie-ctrl.hostname", defaultValue = "localhost")
    private String hostname;

    @Parameter(property = "kie-ctrl.port", defaultValue = "8080")
    private Integer port;

    @Parameter(property = "kie-ctrl.protocol", defaultValue = "http")
    private String protocol;

    @Parameter(property = "kie-ctrl.context-path", defaultValue = "business-central")
    private String contextPath;

    @Parameter(property = "kie-ctrl.controller-path", defaultValue = "/rest/controller")
    private String controllerPath;

    @Parameter(property = "kie-ctrl.controller-username", required = true, readonly = true)
    private String controllerUsername;

    @Parameter(property = "kie-ctrl.controller-password", required = true, readonly = true)
    private String controllerPassword;

    @Parameter(property = "kie-ctrl.connection-timeout", defaultValue = "100")
    protected Integer connectionTimeout;

    @Parameter(property = "kie-ctrl.socket-timeout", defaultValue = "2")
    protected Integer socketTimeout;

    protected KieControllerGateway kieControllerGateway;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {

        getLog().info("Protocol: " + protocol);
        getLog().info("Host Name: " + hostname);
        getLog().info("Port: " + port);
        getLog().info("Controller Username: " + controllerUsername);
        getLog().debug("Controller Password: ###SECRET###");
        getLog().debug("Connection Timeout: " + connectionTimeout);
        getLog().debug("Socket Timeout: " + socketTimeout);
        getLog().debug("Context Path: " + contextPath);

        kieControllerGateway = new KieControllerGateway(protocol, hostname, port, controllerUsername, controllerPassword, connectionTimeout, socketTimeout, contextPath, controllerPath);

        try {
            executeCommand();
        } catch (ProcessingException pe) {
            throw new MojoFailureException(pe.getMessage(), pe);
        } finally {
            kieControllerGateway.close();
        }

    }

    public abstract void executeCommand() throws MojoExecutionException, MojoFailureException;

}
