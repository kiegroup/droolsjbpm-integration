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

import java.util.HashMap;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.kie.server.api.model.KieContainerStatus;
import org.kie.server.api.model.KieScannerStatus;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.controller.api.model.spec.Capability;
import org.kie.server.controller.api.model.spec.ContainerConfig;
import org.kie.server.controller.api.model.spec.ContainerSpec;
import org.kie.server.controller.api.model.spec.ProcessConfig;
import org.kie.server.controller.api.model.spec.RuleConfig;
import org.kie.server.controller.api.model.spec.ServerTemplateKey;

/**
 * Create new container on server template
 */
@Mojo( name = "create-container", defaultPhase = LifecyclePhase.DEPLOY, threadSafe = true, requiresProject = true)
public class CreateContainerMojo extends KieControllerAbstractMojo {

    @Parameter(property = "kie-ctrl.template-id", required = true)
    protected String templateId;

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

        if (container == null) {
            container = project.getGroupId() + ":" + project.getArtifactId() + ":" + project.getVersion();
        }
        getLog().info("Container: " + container);

        ContainerSpec containerSpec = buildContainerSpec();

        kieControllerGateway.createContainer(templateId, container, containerSpec);
        getLog().info("Container " + container + " created on server template " + templateId);

    }

    protected ContainerSpec buildContainerSpec() {

        HashMap<Capability, ContainerConfig> configs = new HashMap<>();

        if(runtimeStrategy != null || kbase != null || ksession != null || mergeMode != null) {
            processConfig(configs);
        } else {
            getLog().info("Process Config: Use Default Process Config");
        }

        if (pollInterval != null || scannerStatus != null) {
            ruleConfig(configs);
        } else {
            getLog().info("Rule Config: Use Default Rule Config");
        }

        ReleaseId releasedId = new ReleaseId(project.getGroupId(), project.getArtifactId(), project.getVersion());
        getLog().info("Release id: " + releasedId);

        return new ContainerSpec(
            container, container,
            new ServerTemplateKey(templateId, templateId),
            releasedId,
            KieContainerStatus.STOPPED,
            configs
        );

    }

    private void ruleConfig(HashMap<Capability, ContainerConfig> configs) {
        StringBuilder builder = new StringBuilder("Rule Config: ");

        RuleConfig ruleConfig = new RuleConfig();
        configs.put(Capability.RULE, ruleConfig);

        builder.append("Poll Interval: ");
        builder.append(pollInterval);
        ruleConfig.setPollInterval(pollInterval);

        builder.append("Kie Scanner Status: ");
        builder.append(scannerStatus);
        KieScannerStatus kieScannerStatus = KieScannerStatus.valueOf(scannerStatus);
        ruleConfig.setScannerStatus(kieScannerStatus);

        String ruleConfigInfo = builder.toString().replace("null", "Default");
        getLog().info(ruleConfigInfo);
    }

    private void processConfig(HashMap<Capability, ContainerConfig> configs) {
        ProcessConfig processConfig = new ProcessConfig();
        configs.put(Capability.PROCESS, processConfig);

        StringBuilder builder = new StringBuilder("Process Config: ");

        builder.append("Runtime Strategy: ");
        builder.append(runtimeStrategy);
        processConfig.setRuntimeStrategy(runtimeStrategy);

        builder.append(" - ");

        builder.append("Kie Base: ");
        builder.append(kbase);
        processConfig.setKBase(kbase);

        builder.append(" - ");

        builder.append("Kie Session: ");
        builder.append(ksession);
        processConfig.setKSession(ksession);

        builder.append(" - ");

        builder.append("Merge Mode: ");
        builder.append(mergeMode);
        processConfig.setMergeMode(mergeMode);

        String processConfigInfo = builder.toString().replace("null", "Default");
        getLog().info(processConfigInfo);
    }
}
