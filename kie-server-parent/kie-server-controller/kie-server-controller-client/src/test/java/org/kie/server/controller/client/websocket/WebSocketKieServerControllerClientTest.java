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

package org.kie.server.controller.client.websocket;

import java.lang.reflect.Method;

import org.apache.commons.lang3.reflect.MethodUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kie.server.api.commands.DescriptorCommand;
import org.kie.server.controller.api.service.RuleCapabilitiesService;
import org.kie.server.controller.api.service.RuntimeManagementService;
import org.kie.server.controller.api.service.SpecManagementService;
import org.kie.server.controller.websocket.common.KieServerMessageHandlerWebSocketClient;
import org.kie.server.controller.websocket.common.WebSocketUtils;
import org.kie.server.controller.websocket.common.handlers.InternalMessageHandler;
import org.kie.server.controller.websocket.common.handlers.WebSocketServiceResponse;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class WebSocketKieServerControllerClientTest {

    @Mock
    private KieServerMessageHandlerWebSocketClient client;

    @InjectMocks
    @Spy
    private WebSocketKieServerControllerClient controllerClient;

    @Before
    public void setup() throws Exception {
        when(controllerClient.getMessageHandler()).thenReturn(mock(WebSocketServiceResponse.class));
    }

    /**
     * Verifies that all methods declared in the management interfaces do serialize the correct service and method names.
     * @throws Exception
     */
    @Test
    public void testServiceMethods() throws Exception {
        verifyServiceMethods(SpecManagementService.class);
        verifyServiceMethods(RuntimeManagementService.class);
        verifyServiceMethods(RuleCapabilitiesService.class);
    }

    private void verifyServiceMethods(final Class service) throws Exception {
        final String name = service.getName();
        final Method[] methods = service.getMethods();

        for (int i = 0; i < methods.length; i++) {
            final Method m = methods[i];

            MethodUtils.invokeMethod(controllerClient,
                                     m.getName(),
                                     new Object[m.getParameterCount()]);

            ArgumentCaptor<String> contentCaptor = ArgumentCaptor.forClass(String.class);
            verify(client).sendTextWithInternalHandler(contentCaptor.capture(),
                                                       any(InternalMessageHandler.class));

            final DescriptorCommand command = WebSocketUtils.unmarshal(contentCaptor.getValue(),
                                                                 DescriptorCommand.class);
            assertNotNull(command);
            assertEquals(name,
                         command.getService());
            assertEquals(m.getName(),
                         command.getMethod());

            reset(client);
        }
    }
}
