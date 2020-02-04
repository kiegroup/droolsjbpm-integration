/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.server.controller.api.model.spec;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.kie.server.api.model.KieContainerStatus;
import org.kie.server.api.model.KieScannerStatus;
import org.kie.server.api.model.KieServerMode;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.controller.api.model.runtime.ServerInstanceKey;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class ServerTemplateTest {

    @Test
    public void testClone() {
        ServerTemplate src = new ServerTemplate();
        src.setId("test1");
        src.setName("test1");
        src.setMode(KieServerMode.DEVELOPMENT);
        List<String> capabilities = new ArrayList<String>();
        capabilities.add(Capability.RULE.toString());
        src.setCapabilities(capabilities);
        Map<Capability, ContainerConfig> containerSpecRule = new HashMap<>();
        containerSpecRule.put(Capability.RULE, new RuleConfig(1L, KieScannerStatus.STARTED));
        src.addContainerSpec(new ContainerSpec("test", "test1", new ServerTemplateKey("test", "test"),
                                                new ReleaseId("test", "test", "1"), KieContainerStatus.STARTED, containerSpecRule));

        Map<Capability, ContainerConfig> containerSpecProcess = new HashMap<>();
        containerSpecProcess.put(Capability.PROCESS, new ProcessConfig("test1", "test1", "test1", "test1"));
        src.addContainerSpec(new ContainerSpec("test1", "test1", new ServerTemplateKey("test1", "test1"),
                                               new ReleaseId("test1", "test1", "2"), KieContainerStatus.STARTED, containerSpecProcess));

        Map<Capability, ServerConfig> cloneServerConfig = new HashMap<Capability, ServerConfig>();
        cloneServerConfig.put(Capability.RULE,new ServerConfig());
        src.setConfigs(cloneServerConfig);
        src.addServerInstance(new ServerInstanceKey("test1", "test1", "test1", "test1"));

        ServerTemplate dest = src.cloneServerTemplate();

        assertEquals(src, dest);
        assertFalse(src == dest);

    }
}
