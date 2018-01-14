/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
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

import java.util.ArrayList;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.kie.server.controller.api.model.events.*;
import org.kie.server.controller.api.model.runtime.ServerInstance;
import org.kie.server.controller.api.model.spec.ContainerSpec;
import org.kie.server.controller.api.model.spec.ServerTemplate;
import org.kie.server.controller.client.event.EventHandler;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class WebSocketEventHandlerTest {

    @Mock
    EventHandler eventHandler;

    @InjectMocks
    WebSocketEventHandler handler;

    @Test
    public void testInvalidEvent() {
        handler.accept(mock(KieServerControllerEvent.class));

        verifyZeroInteractions(eventHandler);
    }

    @Test
    public void testServerInstanceConnected() {
        handler.accept(new ServerInstanceConnected(new ServerInstance()));

        verify(eventHandler).onServerInstanceConnected(any());
        verifyNoMoreInteractions(eventHandler);
    }

    @Test
    public void testServerInstanceDisconnected() {
        handler.accept(new ServerInstanceDisconnected("serverId"));

        verify(eventHandler).onServerInstanceDisconnected(any());
        verifyNoMoreInteractions(eventHandler);
    }

    @Test
    public void testServerInstanceDeleted() {
        handler.accept(new ServerInstanceDeleted("serverId"));

        verify(eventHandler).onServerInstanceDeleted(any());
        verifyNoMoreInteractions(eventHandler);
    }

    @Test
    public void testServerInstanceUpdated() {
        handler.accept(new ServerInstanceUpdated(new ServerInstance()));

        verify(eventHandler).onServerInstanceUpdated(any());
        verifyNoMoreInteractions(eventHandler);
    }

    @Test
    public void testServerTemplateUpdated() {
        handler.accept(new ServerTemplateUpdated(new ServerTemplate()));

        verify(eventHandler).onServerTemplateUpdated(any());
        verifyNoMoreInteractions(eventHandler);
    }

    @Test
    public void testServerTemplateDeleted() {
        handler.accept(new ServerTemplateDeleted("serverTemplateId"));

        verify(eventHandler).onServerTemplateDeleted(any());
        verifyNoMoreInteractions(eventHandler);
    }

    @Test
    public void testContainerSpecUpdated() {
        handler.accept(new ContainerSpecUpdated(new ServerTemplate(),
                                                new ContainerSpec(),
                                                new ArrayList<>()));

        verify(eventHandler).onContainerSpecUpdated(any());
        verifyNoMoreInteractions(eventHandler);
    }
}
