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

package org.kie.server.controller.websocket.management;

import org.junit.Test;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.controller.api.commands.KieServerControllerDescriptorCommand;
import org.kie.server.controller.api.model.KieServerControllerServiceResponse;
import org.kie.server.controller.api.model.spec.*;
import org.kie.server.controller.api.service.RuleCapabilitiesService;
import org.kie.server.controller.api.service.SpecManagementService;
import org.kie.server.controller.websocket.common.WebSocketUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.*;
import static org.kie.server.api.model.KieServiceResponse.ResponseType.FAILURE;
import static org.kie.server.api.model.KieServiceResponse.ResponseType.SUCCESS;

public class KieServerMgmtCommandServiceImplTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(KieServerMgmtCommandServiceImplTest.class);
    private static final String SERVER_TEMPLATE_ID = "templateId";
    private static final String SERVER_TEMPLATE_NAME = "templateName";
    private static final String CONTAINER_SPEC_ID = "specId";
    private static final String CONTAINER_SPEC_NAME = "specName";

    private KieServerMgmtCommandService service = KieServerMgmtCommandServiceImpl.getInstance();

    @Test
    public void testNullCommandScript() {
        final KieServerControllerServiceResponse response = service.executeCommand(null);

        assertNotNull(response);
        assertEquals(FAILURE,
                     response.getType());
    }

    @Test
    public void testInvalidService() {
        final KieServerControllerServiceResponse response = service.executeCommand(new KieServerControllerDescriptorCommand("service",
                                                                                                                            "method"));

        assertNotNull(response);
        assertEquals(FAILURE,
                     response.getType());
    }

    @Test
    public void testServiceWithWrongNumberOfParameter() {
        final KieServerControllerServiceResponse response = service.executeCommand(new KieServerControllerDescriptorCommand(SpecManagementService.class.getName(),
                                                                                                                            "listServerTemplates",
                                                                                                                            1));

        assertNotNull(response);
        assertEquals(FAILURE,
                     response.getType());
    }

    @Test
    public void testValidServiceWithoutParameters() {
        final KieServerControllerServiceResponse response = service.executeCommand(new KieServerControllerDescriptorCommand(SpecManagementService.class.getName(),
                                                                                                                            "listServerTemplates"));

        assertNotNull(response);
        assertEquals(SUCCESS,
                     response.getType());
    }

    @Test
    public void testCommandSerialization() {
        final ServerTemplate serverTemplate = new ServerTemplate();
        serverTemplate.setId(SERVER_TEMPLATE_ID);
        serverTemplate.setName(SERVER_TEMPLATE_NAME);
        KieServerControllerDescriptorCommand command = new KieServerControllerDescriptorCommand(SpecManagementService.class.getName(),
                                                                                                "saveServerTemplate",
                                                                                                serverTemplate);

        final String content = WebSocketUtils.marshal(command);

        LOGGER.info("JSON content\n{}", content);
        KieServerControllerServiceResponse response = service.executeCommand(WebSocketUtils.unmarshal(content,
                                                                                                      KieServerControllerDescriptorCommand.class));

        assertNotNull(response);
        assertEquals(response.getMsg(),
                     SUCCESS,
                     response.getType());
        assertNull(response.getResult());

        command = new KieServerControllerDescriptorCommand(SpecManagementService.class.getName(),
                                                           "getServerTemplate",
                                                           (Object) "templateId");

        response = service.executeCommand(command);

        assertNotNull(response);
        assertEquals(SUCCESS,
                     response.getType());
        assertNotNull(response.getResult());
        assertEquals(serverTemplate,
                     response.getResult());

        String responseContent = WebSocketUtils.marshal(response);
        response = WebSocketUtils.unmarshal(responseContent,
                                            KieServerControllerServiceResponse.class);
        assertNotNull(response);
        assertEquals(SUCCESS,
                     response.getType());
        assertNotNull(response.getResult());
        assertEquals(serverTemplate,
                     response.getResult());
    }

    @Test
    public void testCommandWithDomainArguments() {
        final ContainerSpec containerSpec = new ContainerSpec();
        containerSpec.setId(CONTAINER_SPEC_ID);
        containerSpec.setContainerName(CONTAINER_SPEC_NAME);
        KieServerControllerDescriptorCommand command = new KieServerControllerDescriptorCommand(SpecManagementService.class.getName(),
                                                                                                "saveContainerSpec",
                                                                                                null,
                                                                                                null,
                                                                                                "templateId",
                                                                                                containerSpec);
        final String content = WebSocketUtils.marshal(command);
        LOGGER.info("JSON content\n{}", content);
        KieServerControllerServiceResponse response = service.executeCommand(WebSocketUtils.unmarshal(content,
                                                                                                      KieServerControllerDescriptorCommand.class));

        assertNotNull(response);
        assertEquals(response.getMsg(),
                     SUCCESS,
                     response.getType());
        assertNull(response.getResult());
    }

    @Test
    public void testCommandUpgradeContainer() {
        final ContainerSpecKey serverTemplate = new ContainerSpecKey("id",
                                                                     "name",
                                                                     new ServerTemplateKey("stid",
                                                                                           "stname"));
        final ReleaseId releaseId = new ReleaseId("group",
                                                  "artifact",
                                                  "version");
        KieServerControllerDescriptorCommand command = new KieServerControllerDescriptorCommand(RuleCapabilitiesService.class.getName(),
                                                                                                "upgradeContainer",
                                                                                                serverTemplate,
                                                                                                releaseId);
        final String content = WebSocketUtils.marshal(command);
        LOGGER.info("JSON content\n{}", content);
        KieServerControllerServiceResponse response = service.executeCommand(WebSocketUtils.unmarshal(content,
                                                                                                      KieServerControllerDescriptorCommand.class));

        assertNotNull(response);
        assertEquals(FAILURE,
                     response.getType());
        assertEquals("No server template found for id stid", response.getMsg());
        assertNull(response.getResult());
    }

    @Test
    public void testUpdateContainerConfig() {
        KieServerControllerDescriptorCommand command = new KieServerControllerDescriptorCommand(SpecManagementService.class.getName(),
                                                                                                "updateContainerConfig",
                                                                                                null,
                                                                                                null,
                                                                                                "serverTemplateId",
                                                                                                "containerSpecId",
                                                                                                Capability.PROCESS,
                                                                                                new ProcessConfig());
        final String content = WebSocketUtils.marshal(command);
        LOGGER.info("JSON content\n{}", content);
        KieServerControllerServiceResponse response = service.executeCommand(WebSocketUtils.unmarshal(content,
                                                                                                      KieServerControllerDescriptorCommand.class));

        assertNotNull(response);
        assertEquals(FAILURE,
                     response.getType());
        assertEquals("No server template found for id serverTemplateId", response.getMsg());
        assertNull(response.getResult());
    }
}