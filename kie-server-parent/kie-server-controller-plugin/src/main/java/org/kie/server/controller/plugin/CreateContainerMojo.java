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
import org.kie.server.controller.api.model.spec.ContainerSpec;
import org.kie.server.controller.builder.ContainerBuilder;
import org.kie.server.controller.log.LogHelper;

/**
 * Create new container on server template
 */
@Mojo( name = "create-container", defaultPhase = LifecyclePhase.DEPLOY, threadSafe = true, requiresProject = true)
public class CreateContainerMojo extends KieControllerMojo {

    @Parameter(property = "kie-ctrl.template-id", required = true)
    protected String templateId;

    @Parameter(property = "kie-ctrl.template-name")
    private String templateName;

    @Parameter(property = "kie-ctrl.container")
    protected String container;

    @Parameter(defaultValue = "${project}", readonly = true)
    protected MavenProject project;

    @Parameter(property = "kie-ctrl.runtime-strategy")
    private String runtimeStrategy;

    @Parameter(property = "kie-ctrl.kbase")
    private String kbase;

    @Parameter(property = "kie-ctrl.ksession")
    private String ksession;

    @Parameter(property = "kie-ctrl.mergeMode")
    private String mergeMode;

    @Parameter(property = "kie-ctrl.pollInterval")
    private Long pollInterval;

    @Parameter(property = "kie-ctrl.scannerStatus")
    private String scannerStatus;

    @Override
    public void executeCommand() throws MojoExecutionException, MojoFailureException {

        if (templateName == null || templateName.isEmpty()) {
            templateName = templateId;
        }

        getLog().info("Server Template Id: " + templateId);
        getLog().info("Server Template Name: " + templateName);

        ContainerBuilder containerBuilder =
            ContainerBuilder.create(project.getGroupId(), project.getArtifactId(), project.getVersion())
                .id(container)
                .runtimeStrategy(runtimeStrategy)
                .kbase(kbase)
                .ksession(ksession)
                .mergeMode(mergeMode)
                .pollInterval(pollInterval)
                .scannerStatus(scannerStatus);

        ContainerSpec containerSpec = containerBuilder.build(templateId, templateName);

        LogHelper.logContainer(getLog(), containerSpec);
        container = containerBuilder.getId();

        kieControllerGateway.createContainer(templateId, containerSpec);
        getLog().info("Container " + container + " CREATED on server template " + templateId);

    }
}
