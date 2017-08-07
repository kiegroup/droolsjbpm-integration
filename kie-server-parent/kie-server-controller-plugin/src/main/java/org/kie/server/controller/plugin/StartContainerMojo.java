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

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

/**
 * Start container in server template
 */
@Mojo( name = "start-container", defaultPhase = LifecyclePhase.DEPLOY, threadSafe = true, requiresProject = true)
public class StartContainerMojo extends KieControllerAbstractMojo {

    @Parameter(property = "kie-ctrl.template-id", required = true)
    protected String templateId;

    @Parameter(property = "kie-ctrl.container")
    protected String container;

    @Parameter(defaultValue = "${project}", readonly = true)
    protected MavenProject project;

    @Override
    public void executeCommand() throws MojoExecutionException, MojoFailureException {

        if (container == null) {
            container = project.getGroupId() + ":" + project.getArtifactId() + ":" + project.getVersion();
        }
        getLog().info("Container: " + container);

        kieControllerGateway.startContainer(templateId, container);
        getLog().info("Container " + container + " started on server template " + templateId);

    }

}
