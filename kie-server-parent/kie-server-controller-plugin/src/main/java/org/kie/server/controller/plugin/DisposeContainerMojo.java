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

import javax.ws.rs.NotFoundException;
import javax.ws.rs.client.ResponseProcessingException;
import javax.ws.rs.core.Response;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.kie.server.api.model.KieContainerStatus;
import org.kie.server.controller.api.model.spec.ContainerSpec;

@Mojo( name = "dispose-container", defaultPhase = LifecyclePhase.DEPLOY, threadSafe = true, requiresProject = false)
public class DisposeContainerMojo extends KieControllerMojo {

    @Parameter(property = "kie-ctrl.template-id", required = true)
    protected String templateId;

    @Parameter(property = "kie-ctrl.container")
    protected String container;

    @Parameter(defaultValue = "${project}", readonly = true)
    protected MavenProject project;

    @Override
    public void executeCommand() throws MojoExecutionException, MojoFailureException {

        getLog().info("Server Template Id: " + templateId);

        if (container == null && project == null) {
            throw new MojoFailureException("Unable to find container name");
        } else if (container == null) {
            container = project.getGroupId() + ":" + project.getArtifactId() + ":" + project.getVersion();
        }

        ContainerSpec containerSpec;

        try {
            containerSpec = kieControllerGateway.getContainer(templateId, container);
        } catch (NotFoundException containerNotFoundException) {
            getLog().info("Container " + container + " not found on server template " + templateId + ". Skip that task");
            return;
        } catch (ResponseProcessingException exception) {
            if (exception.getResponse().getStatus() == Response.Status.NOT_FOUND.getStatusCode()) {
                getLog().info("Container " + container + " not found on server template " + templateId + ". Skip that task");
                return;
            } else {
                throw new RuntimeException(exception.getMessage(), exception);
            }
        }

        if (KieContainerStatus.STARTED.equals(containerSpec.getStatus())) {
            kieControllerGateway.stopContainer(templateId, container);
            getLog().info("Container " + container + " STOPPED on server template " + templateId);
        }

        kieControllerGateway.disposeContainer(templateId, container);
        getLog().info("Container " + container + " DISPOSED on server template " + templateId);

    }

}
