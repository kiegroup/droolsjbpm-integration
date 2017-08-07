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
import org.kie.server.api.model.KieContainerResource;
import org.kie.server.api.model.KieContainerStatus;
import org.kie.server.controller.api.model.runtime.ServerInstanceKey;
import org.kie.server.controller.api.model.spec.ContainerSpec;
import org.kie.server.controller.api.model.spec.ServerTemplate;
import org.kie.server.gateway.KieServerGateway;
import org.kie.server.log.LogHelper;

/**
 * Deploy the k-jar artifact on managed kie-server template
 */
@Mojo( name = "deploy", defaultPhase = LifecyclePhase.DEPLOY, threadSafe = true, requiresProject = true)
public class DeployMojo extends CreateContainerMojo {

    @Parameter(property = "kie-ctrl.username", required = true, readonly = true)
    private String username;

    @Parameter(property = "kie-ctrl.password", required = true, readonly = true)
    private String password;

    @Parameter(property = "kie-ctrl.verify-server-wait-time", defaultValue = "1000")
    private Integer verifyServerWaitTime;

    @Override
    public void executeCommand() throws MojoExecutionException, MojoFailureException {

        if (container == null) {
            container = project.getGroupId() + ":" + project.getArtifactId() + ":" + project.getVersion();
        }
        ContainerSpec containerSpec = buildContainerSpec();

        getLog().info("Container: " + container);

        kieControllerGateway.createContainer(templateId, container, containerSpec);
        getLog().info("Container " + container + " created on server template " + templateId);

        kieControllerGateway.startContainer(templateId, container);
        getLog().info("Container " + container + " started on server template " + templateId);

        ServerTemplate serverTemplate = kieControllerGateway.getServerTemplate(templateId);
        LogHelper.logServerTemplate(getLog(), serverTemplate);

        getLog().info("Verify Server - Wait Time: " + verifyServerWaitTime);

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

        while (true) {

            try {
                Thread.sleep(verifyServerWaitTime);
            } catch (InterruptedException e) {
                throw new MojoExecutionException(e.getMessage(), e);
            }

            KieContainerResource container = kieServerGateway.getContainer(server.getUrl(), this.container);
            getLog().debug("Server Response: " + container);

            if (KieContainerStatus.CREATING.equals(container.getStatus())) {
                continue;
            }

            if (KieContainerStatus.STARTED.equals(container.getStatus())) {
                getLog().info("Server " + server.getUrl() + " started with messages " + LogHelper.read(container.getMessages()));
                break;
            }

            throw new MojoExecutionException(LogHelper.read(container.getMessages()));

        }

    }

}
