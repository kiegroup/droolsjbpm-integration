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

import java.util.List;

import org.kie.server.controller.api.model.events.*;
import org.kie.server.controller.api.model.notification.KieServerControllerNotification;
import org.kie.server.controller.api.model.runtime.Container;
import org.kie.server.controller.api.model.spec.ContainerSpec;
import org.kie.server.controller.api.model.spec.ServerTemplate;
import org.kie.server.controller.api.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebSocketNotificationService implements NotificationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(WebSocketNotificationService.class);

    private static WebSocketNotificationService INSTANCE = new WebSocketNotificationService();

    private final WebSocketNotificationSessionManager manager = WebSocketNotificationSessionManager.getInstance();

    protected WebSocketNotificationService() {
        LOGGER.info("WebSocket Notification Service started.");
    }

    public static WebSocketNotificationService getInstance() {
        return INSTANCE;
    }

    private void notifySessions(final KieServerControllerEvent event) {
        final KieServerControllerNotification notification = new KieServerControllerNotification(event);
        LOGGER.debug("Sending notification to all Web Socket sessions");
        manager.broadcastObject(notification);
    }

    @Override
    public void notify(final ServerTemplate serverTemplate,
                       final ContainerSpec containerSpec,
                       final List<Container> containers) {
        LOGGER.info("WebSocket notification about change requested on server {} with container spec {} with following result {}",
                    serverTemplate,
                    containerSpec,
                    containers);
        notifySessions(new ContainerSpecUpdated(serverTemplate,
                                                containerSpec,
                                                containers));
    }

    @Override
    public void notify(final ServerTemplateUpdated serverTemplateUpdated) {
        LOGGER.info("WebSocket notify on updated :: {}",
                    serverTemplateUpdated);
        notifySessions(serverTemplateUpdated);
    }

    @Override
    public void notify(final ServerTemplateDeleted serverTemplateDeleted) {
        LOGGER.info("WebSocket notify on deleted :: {}",
                    serverTemplateDeleted);
        notifySessions(serverTemplateDeleted);
    }

    @Override
    public void notify(final ServerInstanceUpdated serverInstanceUpdated) {
        LOGGER.info("WebSocket notify on instance updated :: {}",
                    serverInstanceUpdated);
        notifySessions(serverInstanceUpdated);
    }

    @Override
    public void notify(final ServerInstanceDeleted serverInstanceDeleted) {
        LOGGER.info("WebSocket notify on instance deleted :: {}",
                    serverInstanceDeleted);
        notifySessions(serverInstanceDeleted);
    }

    @Override
    public void notify(final ServerInstanceConnected serverInstanceConnected) {
        LOGGER.info("WebSocket notify on instance connected :: {}",
                    serverInstanceConnected);
        notifySessions(serverInstanceConnected);
    }

    @Override
    public void notify(final ServerInstanceDisconnected serverInstanceDisconnected) {
        LOGGER.info("WebSocket notify on instance disconnected :: {}",
                    serverInstanceDisconnected);
        notifySessions(serverInstanceDisconnected);
    }
}
