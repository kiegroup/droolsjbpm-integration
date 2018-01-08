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

import java.io.IOException;
import javax.annotation.PostConstruct;
import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;

import org.kie.server.controller.api.commands.KieServerControllerDescriptorCommand;
import org.kie.server.controller.api.model.KieServerControllerServiceResponse;
import org.kie.server.controller.websocket.common.decoder.KieServerControllerDescriptorCommandDecoder;
import org.kie.server.controller.websocket.common.encoder.KieServerControllerServiceResponseEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ServerEndpoint(value = "/websocket/controller/management",
        encoders = KieServerControllerServiceResponseEncoder.class,
        decoders = KieServerControllerDescriptorCommandDecoder.class)
public class WebSocketKieServerMgmtControllerImpl {

    private static final Logger LOGGER = LoggerFactory.getLogger(WebSocketKieServerMgmtControllerImpl.class);

    private KieServerMgmtCommandService commandService = KieServerMgmtCommandServiceImpl.getInstance();

    @PostConstruct
    public void configure() {
        LOGGER.info("Kie Server Controller Management WebSocket service initialized");
    }

    @OnOpen
    public void onManagementClientConnect(final Session session,
                                          final EndpointConfig config) {
        LOGGER.debug("New Web Socket Management Client session: {}",
                     session.getId());
    }

    @OnMessage
    public void onMessage(final KieServerControllerDescriptorCommand command,
                          final Session session) {
        LOGGER.debug("Message received on session: {}",
                     session.getId());

        final KieServerControllerServiceResponse response = commandService.executeCommand(command);
        try {
            session.getBasicRemote().sendObject(response);
        } catch (IOException | EncodeException ex) {
            LOGGER.error("Error trying to send Web Socket response: {}",
                         ex.getMessage(),
                         ex);
            throw new RuntimeException(ex);
        }
    }

    @OnClose
    public void onManagementClientDisconnect(final Session session,
                                             final CloseReason closeReason) {
        LOGGER.debug("Web Socket Management Client session: {}, disconnected, reason: {}",
                     session.getId(),
                     closeReason);
    }

    @OnError
    public void onManagementClientError(final Session session,
                                        final Throwable e) {
        LOGGER.error("Web Socket Management Client session: {}, unexpected error",
                     session.getId(),
                     e);
    }
}