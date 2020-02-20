/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
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

package org.kie.server.controller.impl;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.After;
import org.junit.Test;
import org.kie.server.api.KieServerConstants;
import org.kie.server.api.model.KieServerInfo;
import org.kie.server.api.model.Severity;
import org.kie.server.controller.api.model.KieServerSetup;
import org.kie.server.controller.api.model.spec.Capability;
import org.kie.server.controller.api.model.spec.ServerTemplate;
import org.kie.server.controller.impl.storage.InMemoryKieServerTemplateStorage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class KieServerControllerImplTest {

    private static final String DEFAULT_SERVER_TEMPLATE_ID = "templateId";
    private static final String DEFAULT_SERVER_TEMPLATE_NAME = "templateName";
    private static final String DEFAULT_KIE_SERVER_ID = DEFAULT_SERVER_TEMPLATE_ID;
    private static final String DEFAULT_KIE_SERVER_LOCATION = "http://some-random-location.com";

    private KieServerControllerImpl controller = new KieServerControllerImpl(){};

    @After
    public void cleanup() {
        InMemoryKieServerTemplateStorage.getInstance().clear();
    }

    @Test
    public void testConnectWithAllCapabilities() {
        storeDefaultServerTemplate(Capability.RULE, Capability.PROCESS, Capability.PLANNING);

        KieServerInfo kieServerInfo = createDefaultKieServerInfo(KieServerConstants.CAPABILITY_BRM, KieServerConstants.CAPABILITY_BPM, KieServerConstants.CAPABILITY_BRP);

        KieServerSetup kieServerSetup = controller.connect(kieServerInfo);
        assertTrue(kieServerSetup.hasNoErrors());

        assertTrue(controller.getTemplateStorage().load(DEFAULT_KIE_SERVER_ID).hasServerInstance(DEFAULT_KIE_SERVER_LOCATION));
    }

    @Test
    public void testConnectServerTemplateMissingProcessCapability() {
        storeDefaultServerTemplate(Capability.RULE, Capability.PLANNING);

        KieServerInfo kieServerInfo = createDefaultKieServerInfo(KieServerConstants.CAPABILITY_BRM, KieServerConstants.CAPABILITY_BPM, KieServerConstants.CAPABILITY_BRP);

        KieServerSetup kieServerSetup = controller.connect(kieServerInfo);
        assertTrue(kieServerSetup.hasNoErrors());

        assertTrue(controller.getTemplateStorage().load(DEFAULT_KIE_SERVER_ID).hasServerInstance(DEFAULT_KIE_SERVER_LOCATION));
    }

    @Test
    public void testConnectKieServerMissingProcessCapability() {
        storeDefaultServerTemplate(Capability.RULE, Capability.PROCESS, Capability.PLANNING);

        KieServerInfo kieServerInfo = createDefaultKieServerInfo(KieServerConstants.CAPABILITY_BRM, KieServerConstants.CAPABILITY_BRP);

        KieServerSetup kieServerSetup = controller.connect(kieServerInfo);
        assertFalse(kieServerSetup.hasNoErrors());
        assertEquals(1, kieServerSetup.getMessages().size());
        assertEquals(Severity.ERROR, kieServerSetup.getMessages().iterator().next().getSeverity());
        assertEquals("Expected capabilities were [RULE, PROCESS, PLANNING]", kieServerSetup.getMessages().iterator().next().getMessages().iterator().next());

        assertFalse(controller.getTemplateStorage().load(DEFAULT_KIE_SERVER_ID).hasServerInstance(DEFAULT_KIE_SERVER_LOCATION));
    }

    private void storeDefaultServerTemplate(Capability... capabilities) {
        List<String> capabilitiesAsString = Arrays.asList(capabilities).stream().map(Capability::toString).collect(Collectors.toList());

        ServerTemplate serverTemplate = new ServerTemplate(DEFAULT_SERVER_TEMPLATE_ID, DEFAULT_SERVER_TEMPLATE_NAME);
        serverTemplate.setCapabilities(capabilitiesAsString);
        controller.getTemplateStorage().store(serverTemplate);
    }

    private KieServerInfo createDefaultKieServerInfo(String... capabilities) {
        KieServerInfo kieServerInfo = new KieServerInfo();
        kieServerInfo.setServerId(DEFAULT_KIE_SERVER_ID);
        kieServerInfo.setLocation(DEFAULT_KIE_SERVER_LOCATION);
        kieServerInfo.setCapabilities(Arrays.asList(capabilities));
        return kieServerInfo;
    }
}
