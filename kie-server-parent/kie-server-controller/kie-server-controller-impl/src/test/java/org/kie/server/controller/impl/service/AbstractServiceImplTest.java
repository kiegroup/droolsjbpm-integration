/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package org.kie.server.controller.impl.service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.kie.server.api.model.KieContainerStatus;
import org.kie.server.api.model.KieScannerStatus;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.controller.api.model.spec.*;
import org.kie.server.controller.api.service.RuleCapabilitiesService;
import org.kie.server.controller.api.service.RuntimeManagementService;
import org.kie.server.controller.api.service.SpecManagementService;
import org.kie.server.controller.impl.KieServerInstanceManager;
import org.kie.server.controller.api.model.runtime.Container;

import static org.junit.Assert.*;

public abstract class AbstractServiceImplTest {

    protected SpecManagementService specManagementService;
    protected RuleCapabilitiesService ruleCapabilitiesService;
    protected RuntimeManagementService runtimeManagementService;
    protected KieServerInstanceManager kieServerInstanceManager;

    protected ServerTemplate serverTemplate;
    protected Container container;
    protected ContainerSpec containerSpec;

    protected void createServerTemplateWithContainer() {
        serverTemplate = new ServerTemplate();

        serverTemplate.setName("test server");
        serverTemplate.setId(UUID.randomUUID().toString());

        specManagementService.saveServerTemplate(serverTemplate);

        ServerTemplateKeyList existing = specManagementService.listServerTemplateKeys();
        assertNotNull(existing);
        assertEquals(1, existing.getServerTemplates().length);

        Map<Capability, ContainerConfig> configs = new HashMap<Capability, ContainerConfig>();
        RuleConfig ruleConfig = new RuleConfig();
        ruleConfig.setPollInterval(1000l);
        ruleConfig.setScannerStatus(KieScannerStatus.STARTED);

        configs.put(Capability.RULE, ruleConfig);

        ProcessConfig processConfig = new ProcessConfig();
        processConfig.setKBase("defaultKieBase");
        processConfig.setKSession("defaultKieSession");
        processConfig.setMergeMode("MERGE_COLLECTION");
        processConfig.setRuntimeStrategy("PER_PROCESS_INSTANCE");

        configs.put(Capability.PROCESS, processConfig);

        containerSpec = new ContainerSpec();
        containerSpec.setId("test container");
        containerSpec.setServerTemplateKey(new ServerTemplateKey(serverTemplate.getId(), serverTemplate.getName()));
        containerSpec.setReleasedId(new ReleaseId("org.kie", "kie-server-kjar", "1.0"));
        containerSpec.setStatus(KieContainerStatus.STOPPED);
        containerSpec.setConfigs(configs);

        specManagementService.saveContainerSpec(serverTemplate.getId(), containerSpec);

        container = new Container();
        container.setServerInstanceId(serverTemplate.getId());
        container.setServerTemplateId(serverTemplate.getId());
        container.setResolvedReleasedId(containerSpec.getReleasedId());
        container.setContainerName(containerSpec.getContainerName());
        container.setContainerSpecId(containerSpec.getId());
        container.setUrl("http://fake.server.net/kie-server");
        container.setStatus(containerSpec.getStatus());

    }
}
