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

package org.kie.server.controller.websocket.common;

import org.junit.Test;
import org.kie.server.api.model.KieContainerStatus;
import org.kie.server.api.model.KieScannerStatus;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.controller.api.commands.KieServerControllerDescriptorCommand;
import org.kie.server.controller.api.model.spec.Capability;
import org.kie.server.controller.api.model.spec.ContainerConfig;
import org.kie.server.controller.api.model.spec.ContainerSpec;
import org.kie.server.controller.api.model.spec.ProcessConfig;
import org.kie.server.controller.api.model.spec.RuleConfig;
import org.kie.server.controller.api.service.SpecManagementService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.*;

public class WebSocketUtilsTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(WebSocketUtilsTest.class);

    @Test
    public void testContainerSpecSerialization() {
        final ContainerSpec spec = new ContainerSpec();
        spec.setId("id");
        spec.setContainerName("name");
        spec.setStatus(KieContainerStatus.STARTED);
        spec.setReleasedId(new ReleaseId("groupId",
                                         "artifactId",
                                         "1.0"));
        final ProcessConfig processConfig = new ProcessConfig("runtimeStrategy",
                                                              "kBase",
                                                              "kSession",
                                                              "mergeMode");
        spec.addConfig(Capability.PROCESS,
                       processConfig);
        final RuleConfig ruleConfig = new RuleConfig(1L,
                                                     KieScannerStatus.SCANNING);
        spec.addConfig(Capability.RULE,
                       ruleConfig);
        final String specContent = WebSocketUtils.marshal(spec);
        LOGGER.info("JSON content\n{}", specContent);
        final ContainerSpec specResult = WebSocketUtils.unmarshal(specContent,
                                                                  ContainerSpec.class);

        assertNotNull(specResult);
        assertEquals(spec,
                     specResult);
        assertEquals(spec.getId(),
                     specResult.getId());
        assertEquals(spec.getStatus(),
                     specResult.getStatus());
        assertEquals(spec.getContainerName(),
                     specResult.getContainerName());
        assertEquals(spec.getReleasedId(),
                     specResult.getReleasedId());
        assertNotNull(specResult.getConfigs());
        assertEquals(spec.getConfigs().size(),
                     specResult.getConfigs().size());
        final ContainerConfig processConfigResult = specResult.getConfigs().get(Capability.PROCESS);
        assertNotNull(processConfigResult);
        assertTrue(processConfigResult instanceof ProcessConfig);
        assertEquals(processConfig,
                     processConfigResult);
        final ContainerConfig ruleConfigResult = specResult.getConfigs().get(Capability.RULE);
        assertNotNull(ruleConfigResult);
        assertTrue(ruleConfigResult instanceof RuleConfig);
        assertEquals(ruleConfig,
                     ruleConfigResult);
    }

    @Test
    public void testKieServerCommandSerialization() {
        final String serverTemplateId = "serverTemplateId";
        final String containerSpecId = "containerSpecId";
        final Capability capability = Capability.PROCESS;
        final ProcessConfig processConfig = new ProcessConfig("runtimeStrategy", "kBase", "kSession", "mergeMode");
        final RuleConfig ruleConfig = new RuleConfig(1l, KieScannerStatus.SCANNING);
        KieServerControllerDescriptorCommand command = new KieServerControllerDescriptorCommand(SpecManagementService.class.getName(),
                                                                                                "updateContainerConfig",
                                                                                                null,
                                                                                                null,
                                                                                                serverTemplateId,
                                                                                                containerSpecId,
                                                                                                capability,
                                                                                                processConfig,
                                                                                                ruleConfig);
        final String content = WebSocketUtils.marshal(command);
        LOGGER.info("JSON content\n{}", content);
        final KieServerControllerDescriptorCommand commandResult = WebSocketUtils.unmarshal(content,
                                                                                            KieServerControllerDescriptorCommand.class);
        assertNotNull(commandResult);
        assertEquals(command.getService(),
                     commandResult.getService());
        assertEquals(command.getMethod(),
                     commandResult.getMethod());
        assertEquals(command.getArguments().size(),
                     commandResult.getArguments().size());
        assertTrue(commandResult.getArguments().contains(serverTemplateId));
        assertTrue(commandResult.getArguments().contains(containerSpecId));
        assertTrue(commandResult.getArguments().contains(capability));
        assertTrue(commandResult.getArguments().contains(processConfig));
        assertTrue(commandResult.getArguments().contains(ruleConfig));

    }
}
