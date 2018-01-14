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

package org.kie.server.controller.websocket.notification;

import java.util.ArrayList;
import javax.websocket.RemoteEndpoint;
import javax.websocket.Session;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kie.server.controller.api.model.events.*;
import org.kie.server.controller.api.model.notification.KieServerControllerNotification;
import org.kie.server.controller.api.model.runtime.Container;
import org.kie.server.controller.api.model.runtime.ServerInstance;
import org.kie.server.controller.api.model.spec.ContainerSpec;
import org.kie.server.controller.api.model.spec.ServerTemplate;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class WebSocketNotificationServiceTest {

    @Mock
    Session session;

    @Mock
    RemoteEndpoint.Async async;

    @InjectMocks
    WebSocketNotificationService notificationService;

    @Before
    public void setUp() {
        when(session.getId()).thenReturn("id");
        when(session.getAsyncRemote()).thenReturn(async);
        WebSocketNotificationSessionManager.getInstance().addSession(session);
    }

    @Test
    public void testServerInstanceConnected() {
        final ServerInstanceConnected event = new ServerInstanceConnected(new ServerInstance());
        notificationService.notify(event);

        verify(async).sendObject(eq(new KieServerControllerNotification(event)),
                                 any());
    }

    @Test
    public void testServerInstanceDisconnected() {
        final ServerInstanceDisconnected event = new ServerInstanceDisconnected("serverId");
        notificationService.notify(event);

        verify(async).sendObject(eq(new KieServerControllerNotification(event)),
                                 any());
    }

    @Test
    public void testServerInstanceDeleted() {
        final ServerInstanceDeleted event = new ServerInstanceDeleted("serverId");
        notificationService.notify(event);

        verify(async).sendObject(eq(new KieServerControllerNotification(event)),
                                 any());
    }

    @Test
    public void testServerInstanceUpdated() {
        final ServerInstanceUpdated event = new ServerInstanceUpdated(new ServerInstance());
        notificationService.notify(event);

        verify(async).sendObject(eq(new KieServerControllerNotification(event)),
                                 any());
    }

    @Test
    public void testServerTemplateUpdated() {
        final ServerTemplateUpdated event = new ServerTemplateUpdated(new ServerTemplate());
        notificationService.notify(event);

        verify(async).sendObject(eq(new KieServerControllerNotification(event)),
                                 any());
    }

    @Test
    public void testServerTemplateDeleted() {
        final ServerTemplateDeleted event = new ServerTemplateDeleted("serverTemplateId");
        notificationService.notify(event);

        verify(async).sendObject(eq(new KieServerControllerNotification(event)),
                                 any());
    }

    @Test
    public void testContainerSpecUpdated() {
        final ServerTemplate serverTemplate = new ServerTemplate();
        final ContainerSpec containerSpec = new ContainerSpec();
        final ArrayList<Container> containers = new ArrayList<>();

        final ContainerSpecUpdated event = new ContainerSpecUpdated(serverTemplate,
                                                                    containerSpec,
                                                                    containers);
        notificationService.notify(serverTemplate,
                                   containerSpec,
                                   containers);

        verify(async).sendObject(eq(new KieServerControllerNotification(event)),
                                 any());
    }
}
