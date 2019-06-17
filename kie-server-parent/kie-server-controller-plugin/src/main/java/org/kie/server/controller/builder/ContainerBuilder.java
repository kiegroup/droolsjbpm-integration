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

package org.kie.server.controller.builder;

import java.util.HashMap;

import org.hibernate.validator.constraints.NotEmpty;
import org.kie.server.api.model.KieContainerStatus;
import org.kie.server.api.model.KieScannerStatus;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.controller.api.model.spec.Capability;
import org.kie.server.controller.api.model.spec.ContainerConfig;
import org.kie.server.controller.api.model.spec.ContainerSpec;
import org.kie.server.controller.api.model.spec.ProcessConfig;
import org.kie.server.controller.api.model.spec.RuleConfig;
import org.kie.server.controller.api.model.spec.ServerTemplateKey;

public class ContainerBuilder {

    private String id;

    @NotEmpty
    private String groupId;

    @NotEmpty
    private String artifactId;

    @NotEmpty
    private String version;

    // config parameters
    private String runtimeStrategy;
    private String kbase;
    private String ksession;
    private String mergeMode;
    private Long pollInterval;
    private String scannerStatus;

    public ContainerBuilder() {
    }

    // if container name is not defined by default we use maven GAV
    public String getId() {
        return (id != null) ? id : getGAV();
    }

    public ContainerSpec build(String templateId, String templateName) {

        HashMap<Capability, ContainerConfig> configs = new HashMap<>();

        if (runtimeStrategy != null || kbase != null || ksession != null || mergeMode != null) {
            ProcessConfig processConfig = new ProcessConfig();
            configs.put(Capability.PROCESS, processConfig);

            processConfig.setRuntimeStrategy(runtimeStrategy == null ? "PER_PROCESS_INSTANCE" : runtimeStrategy);
            processConfig.setKBase(kbase == null ? "" : kbase);
            processConfig.setKSession(ksession == null ? "" : ksession);
            processConfig.setMergeMode(mergeMode == null ? "MERGE_COLLECTIONS" : mergeMode);
        }

        if (pollInterval != null || scannerStatus != null) {
            RuleConfig ruleConfig = new RuleConfig();
            configs.put(Capability.RULE, ruleConfig);
            ruleConfig.setPollInterval(pollInterval);

            KieScannerStatus kieScannerStatus = KieScannerStatus.valueOf(scannerStatus);
            ruleConfig.setScannerStatus(kieScannerStatus);
        }

        ReleaseId releasedId = new ReleaseId(groupId, artifactId, version);

        return new ContainerSpec(
            getId(), getId(),
            new ServerTemplateKey(templateId, templateName),
            releasedId, KieContainerStatus.STOPPED, configs
        );

    }

    private String getGAV() {
        return groupId + ":" + artifactId + ":" + version;
    }

    public static ContainerBuilder create(String groupId, String artifactId, String version) {

        ContainerBuilder containerBuilder = new ContainerBuilder();
        containerBuilder.setGroupId(groupId);
        containerBuilder.setArtifactId(artifactId);
        containerBuilder.setVersion(version);
        return containerBuilder;

    }

    public ContainerBuilder id(String id) {
        this.id = id;
        return this;
    }

    public ContainerBuilder runtimeStrategy(String runtimeStrategy) {
        this.runtimeStrategy = runtimeStrategy;
        return this;
    }

    public ContainerBuilder kbase(String kbase) {
        this.kbase = kbase;
        return this;
    }

    public ContainerBuilder ksession(String ksession) {
        this.ksession = ksession;
        return this;
    }

    public ContainerBuilder mergeMode(String mergeMode) {
        this.mergeMode = mergeMode;
        return this;
    }

    public ContainerBuilder pollInterval(Long pollInterval) {
        this.pollInterval = pollInterval;
        return this;
    }

    public ContainerBuilder scannerStatus(String scannerStatus) {
        this.scannerStatus = scannerStatus;
        return this;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public void setArtifactId(String artifactId) {
        this.artifactId = artifactId;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public void setRuntimeStrategy(String runtimeStrategy) {
        this.runtimeStrategy = runtimeStrategy;
    }

    public void setKbase(String kbase) {
        this.kbase = kbase;
    }

    public void setKsession(String ksession) {
        this.ksession = ksession;
    }

    public void setMergeMode(String mergeMode) {
        this.mergeMode = mergeMode;
    }

    public void setPollInterval(Long pollInterval) {
        this.pollInterval = pollInterval;
    }

    public void setScannerStatus(String scannerStatus) {
        this.scannerStatus = scannerStatus;
    }

}
