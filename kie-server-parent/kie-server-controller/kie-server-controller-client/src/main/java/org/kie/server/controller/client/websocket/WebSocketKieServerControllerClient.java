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

import java.io.IOException;

import org.kie.server.api.model.KieServiceResponse.ResponseType;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.controller.api.commands.KieServerControllerDescriptorCommand;
import org.kie.server.controller.api.model.KieServerControllerServiceResponse;
import org.kie.server.controller.api.model.runtime.ContainerList;
import org.kie.server.controller.api.model.runtime.ServerInstanceKey;
import org.kie.server.controller.api.model.runtime.ServerInstanceKeyList;
import org.kie.server.controller.api.model.spec.*;
import org.kie.server.controller.api.service.RuleCapabilitiesService;
import org.kie.server.controller.api.service.RuntimeManagementService;
import org.kie.server.controller.api.service.SpecManagementService;
import org.kie.server.controller.client.KieServerControllerClient;
import org.kie.server.controller.client.event.EventHandler;
import org.kie.server.controller.client.exception.KieServerControllerClientException;
import org.kie.server.controller.websocket.common.KieServerControllerNotificationWebSocketClient;
import org.kie.server.controller.websocket.common.KieServerMessageHandlerWebSocketClient;
import org.kie.server.controller.websocket.common.WebSocketUtils;
import org.kie.server.controller.websocket.common.config.WebSocketClientConfiguration;
import org.kie.server.controller.websocket.common.decoder.KieServerControllerNotificationDecoder;
import org.kie.server.controller.websocket.common.handlers.WebSocketServiceResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebSocketKieServerControllerClient implements KieServerControllerClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(WebSocketKieServerControllerClient.class);

    private KieServerMessageHandlerWebSocketClient client;
    private KieServerControllerNotificationWebSocketClient notificationClient;

    public WebSocketKieServerControllerClient(final String controllerUrl,
                                              final String userName,
                                              final String password,
                                              final String token,
                                              final EventHandler handler) {
        this(controllerUrl,
             userName,
             password,
             token,
             new KieServerMessageHandlerWebSocketClient(),
             handler == null ? null : new KieServerControllerNotificationWebSocketClient(new WebSocketEventHandler(handler)));
    }

    public WebSocketKieServerControllerClient(final String controllerUrl,
                                              final String userName,
                                              final String password,
                                              final String token,
                                              final KieServerMessageHandlerWebSocketClient client,
                                              final KieServerControllerNotificationWebSocketClient notificationClient) {
        this.client = client;
        this.client.init(WebSocketClientConfiguration.builder()
                                 .controllerUrl(controllerUrl + "/management")
                                 .userName(userName)
                                 .password(password)
                                 .token(token)
                                 .build());
        if (notificationClient != null) {
            this.notificationClient = notificationClient;
            this.notificationClient.init(WebSocketClientConfiguration.builder()
                                                 .controllerUrl(controllerUrl + "/notification")
                                                 .userName(userName)
                                                 .password(password)
                                                 .token(token)
                                                 .decoders(KieServerControllerNotificationDecoder.class)
                                                 .build());
        }
    }

    @Override
    public void close() throws IOException {
        client.close();
        if (notificationClient != null) {
            notificationClient.close();
        }
    }

    private <T> T sendCommand(final String service,
                              final String method,
                              final Object... arguments) {

        final KieServerControllerDescriptorCommand command = new KieServerControllerDescriptorCommand(service,
                                                                                                      method,
                                                                                                      arguments);

        LOGGER.debug("About to send descriptor command to kie server controller: {}",
                     command);

        final String content = WebSocketUtils.marshal(command);
        LOGGER.debug("Content to be sent over Web Socket '{}'",
                     content);
        try {
            final WebSocketServiceResponse response = getMessageHandler();
            client.sendTextWithInternalHandler(content,
                                               response);
            LOGGER.debug("Message successfully sent to kie server controller");
            if (response.getType() == ResponseType.FAILURE) {
                throw new KieServerControllerClientException(response.getMsg());
            } else {
                return (T) response.getResult();
            }
        } catch (KieServerControllerClientException e) {
            LOGGER.warn("Received Web Socket service error with message: {}",
                        e.getMessage());
            throw e;
        } catch (Exception e) {
            LOGGER.error("Error trying to send message to kie server controller",
                         e);
            throw new KieServerControllerClientException(e);
        }
    }

    protected WebSocketServiceResponse getMessageHandler() {
        return new WebSocketServiceResponse(true,
                                            (message) -> WebSocketUtils.unmarshal(message,
                                                                                  KieServerControllerServiceResponse.class));
    }

    @Override
    public ServerTemplateList listServerTemplates() {
        return sendCommand(SpecManagementService.class.getName(),
                           "listServerTemplates");
    }

    @Override
    public void saveContainerSpec(final String serverTemplateId,
                                  final ContainerSpec containerSpec) {
        sendCommand(SpecManagementService.class.getName(),
                    "saveContainerSpec",
                    serverTemplateId,
                    containerSpec);
    }

    @Override
    public void updateContainerSpec(final String serverTemplateId,
                                    final ContainerSpec containerSpec) {
        sendCommand(SpecManagementService.class.getName(),
                    "updateContainerSpec",
                    serverTemplateId,
                    containerSpec);
    }

    @Override
    public void updateContainerSpec(final String serverTemplateId,
                                    final String containerId,
                                    final ContainerSpec containerSpec) {
        sendCommand(SpecManagementService.class.getName(),
                    "updateContainerSpec",
                    serverTemplateId,
                    containerId,
                    containerSpec);
    }

    @Override
    public void saveServerTemplate(final ServerTemplate serverTemplate) {
        sendCommand(SpecManagementService.class.getName(),
                    "saveServerTemplate",
                    serverTemplate);
    }

    @Override
    public ServerTemplate getServerTemplate(final String serverTemplateId) {
        return sendCommand(SpecManagementService.class.getName(),
                           "getServerTemplate",
                           serverTemplateId);
    }

    @Override
    public ServerTemplateKeyList listServerTemplateKeys() {
        return sendCommand(SpecManagementService.class.getName(),
                           "listServerTemplateKeys");
    }

    @Override
    public ContainerSpecList listContainerSpec(final String serverTemplateId) {
        return sendCommand(SpecManagementService.class.getName(),
                           "listContainerSpec",
                           serverTemplateId);
    }

    @Override
    public ContainerSpec getContainerInfo(final String serverTemplateId,
                                          final String containerId) {
        return sendCommand(SpecManagementService.class.getName(),
                           "getContainerInfo",
                           serverTemplateId,
                           containerId);
    }

    @Override
    public void deleteContainerSpec(final String serverTemplateId,
                                    final String containerSpecId) {
        sendCommand(SpecManagementService.class.getName(),
                    "deleteContainerSpec",
                    serverTemplateId,
                    containerSpecId);
    }

    @Override
    public void deleteServerTemplate(final String serverTemplateId) {
        sendCommand(SpecManagementService.class.getName(),
                    "deleteServerTemplate",
                    serverTemplateId);
    }

    @Override
    public void copyServerTemplate(final String serverTemplateId,
                                   final String newServerTemplateId,
                                   final String newServerTemplateName) {
        sendCommand(SpecManagementService.class.getName(),
                    "copyServerTemplate",
                    serverTemplateId,
                    newServerTemplateId,
                    newServerTemplateName);
    }

    @Override
    public void updateContainerConfig(final String serverTemplateId,
                                      final String containerSpecId,
                                      final Capability capability,
                                      final ContainerConfig containerConfig) {
        sendCommand(SpecManagementService.class.getName(),
                    "updateContainerConfig",
                    serverTemplateId,
                    containerSpecId,
                    capability,
                    containerConfig);
    }

    @Override
    public void updateServerTemplateConfig(final String serverTemplateId,
                                           final Capability capability,
                                           final ServerConfig serverConfig) {
        sendCommand(SpecManagementService.class.getName(),
                    "updateServerTemplateConfig",
                    serverTemplateId,
                    capability,
                    serverConfig);
    }

    @Override
    public void startContainer(final ContainerSpecKey containerSpecKey) {
        sendCommand(SpecManagementService.class.getName(),
                    "startContainer",
                    containerSpecKey);
    }

    @Override
    public void stopContainer(final ContainerSpecKey containerSpecKey) {
        sendCommand(SpecManagementService.class.getName(),
                    "stopContainer",
                    containerSpecKey);
    }
    
    @Override
    public void activateContainer(final ContainerSpecKey containerSpecKey) {
        sendCommand(SpecManagementService.class.getName(),
                    "activateContainer",
                    containerSpecKey);
    }

    @Override
    public void deactivateContainer(final ContainerSpecKey containerSpecKey) {
        sendCommand(SpecManagementService.class.getName(),
                    "deactivateContainer",
                    containerSpecKey);
    }

    @Override
    public void scanNow(final ContainerSpecKey containerSpecKey) {
        sendCommand(RuleCapabilitiesService.class.getName(),
                    "scanNow",
                    containerSpecKey);
    }

    @Override
    public void startScanner(final ContainerSpecKey containerSpecKey,
                             final Long interval) {
        sendCommand(RuleCapabilitiesService.class.getName(),
                    "startScanner",
                    containerSpecKey,
                    interval);
    }

    @Override
    public void stopScanner(final ContainerSpecKey containerSpecKey) {
        sendCommand(RuleCapabilitiesService.class.getName(),
                    "stopScanner",
                    containerSpecKey);
    }

    @Override
    public void upgradeContainer(final ContainerSpecKey containerSpecKey,
                                 final ReleaseId releaseId) {
        sendCommand(RuleCapabilitiesService.class.getName(),
                    "upgradeContainer",
                    containerSpecKey,
                    releaseId);
    }

    @Override
    public ContainerList getContainers(final ServerInstanceKey serverInstanceKey) {
        return sendCommand(RuntimeManagementService.class.getName(),
                           "getContainers",
                           serverInstanceKey);
    }

    @Override
    public ContainerList getContainers(final ServerTemplate serverTemplate,
                                       final ContainerSpec containerSpec) {
        return sendCommand(RuntimeManagementService.class.getName(),
                           "getContainers",
                           serverTemplate,
                           containerSpec);
    }

    @Override
    public ServerInstanceKeyList getServerInstances(final String serverTemplateId) {
        return sendCommand(RuntimeManagementService.class.getName(),
                           "getServerInstances",
                           serverTemplateId);
    }

    @Override
    public void deleteServerInstance(final ServerInstanceKey serverInstanceKey) {
        sendCommand(SpecManagementService.class.getName(),
                    "deleteServerInstance",
                    serverInstanceKey);
    }
}
