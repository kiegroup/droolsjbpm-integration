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
import org.kie.server.api.model.KieContainerResource;
import org.kie.server.api.model.KieContainerStatus;
import org.kie.server.controller.api.model.runtime.ServerInstanceKey;
import org.kie.server.controller.api.model.spec.ContainerSpec;
import org.kie.server.controller.api.model.spec.ServerTemplate;
import org.kie.server.controller.builder.ContainerBuilder;
import org.kie.server.controller.log.LogHelper;
import org.kie.server.gateway.KieServerGateway;

/**
 * Deploy the k-jar artifact on managed kie-server template
 */
@Mojo( name = "deploy-container", defaultPhase = LifecyclePhase.DEPLOY, threadSafe = true, requiresProject = true)
public class DeployContainerMojo extends KieControllerMojo {

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

    @Parameter(property = "kie-ctrl.username", required = true, readonly = true)
    private String username;

    @Parameter(property = "kie-ctrl.password", required = true, readonly = true)
    private String password;

    @Parameter(property = "kie-ctrl.verify-server-wait-time", defaultValue = "1000")
    private Integer verifyServerWaitTime;

    @Parameter(property = "kie-ctrl.verify-server-max-time", defaultValue = "300000")
    private Integer verifyServerMaxTime;

    @Override
    public void executeCommand() throws MojoExecutionException, MojoFailureException {

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

        kieControllerGateway.createContainer(templateId, container, containerSpec);
        getLog().info("Container " + container + " CREATED on server template " + templateId);

        kieControllerGateway.startContainer(templateId, container);
        getLog().info("Container " + container + " STARTED on server template " + templateId);

        ServerTemplate serverTemplate = kieControllerGateway.getServerTemplate(templateId);
        LogHelper.logServerTemplate(getLog(), serverTemplate);

        getLog().info("Verify Server - Wait Time: " + verifyServerWaitTime);
        getLog().info("Verify Server - Max Time: " + verifyServerMaxTime);

        KieServerGateway kieServerGateway = new KieServerGateway(username, password, connectionTimeout, socketTimeout);

        try {

            for (ServerInstanceKey server : serverTemplate.getServerInstanceKeys()) {
                verifyServerInstance(kieServerGateway, server);
            }

        } finally {
            kieServerGateway.close();
        }

    }

    private void verifyServerInstance(KieServerGateway kieServerGateway, ServerInstanceKey server) throws MojoExecutionException {

        getLog().info("Verifying Server: " + server.getUrl());
        long timeLimit = System.currentTimeMillis() + verifyServerMaxTime;

        while (System.currentTimeMillis() < timeLimit) {

            try {
                Thread.sleep(verifyServerWaitTime);
            } catch (InterruptedException e) {
                throw new MojoExecutionException(e.getMessage(), e);
            }

            KieContainerResource container = kieServerGateway.getContainer(server.getUrl(), this.container);
            getLog().debug("Server Response: " + container);

            // Container is not instantiated
            if (container == null) {
                getLog().debug("Container " + this.container + " is not jet instantiated.");
                continue;
            }

            // Container is instantiated but the state is not jet STARTED
            if (KieContainerStatus.CREATING.equals(container.getStatus())) {
                getLog().debug("Server " + server.getUrl() + " is creating with messages " + LogHelper.read(container.getMessages()));
                continue;
            }

            // Container is instantiated but the state is not jet STARTED
            if (KieContainerStatus.STARTED.equals(container.getStatus())) {
                getLog().info("Server " + server.getUrl() + " started with messages " + LogHelper.read(container.getMessages()));
                return;
            }

            // the only admitted states are CREATING or STARTED
            throw new MojoExecutionException(LogHelper.read(container.getMessages()));

        }

        // exceeds max time verify limit
        getLog().warn("It is NOT possible to VERIFY if the server " + server.getUrl() + " is STARTED: verify-server-max-time exceeded.");

    }

}
