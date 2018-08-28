/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
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

package org.kie.server.controller.websocket.client;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.kie.server.api.KieServerConstants;
import org.kie.server.api.KieServerEnvironment;
import org.kie.server.api.marshalling.Marshaller;
import org.kie.server.api.marshalling.MarshallerFactory;
import org.kie.server.api.marshalling.MarshallingException;
import org.kie.server.api.marshalling.MarshallingFormat;
import org.kie.server.api.model.KieServerConfig;
import org.kie.server.api.model.KieServerInfo;
import org.kie.server.common.KeyStoreHelperUtil;
import org.kie.server.controller.api.KieServerController;
import org.kie.server.controller.api.model.KieServerSetup;
import org.kie.server.controller.websocket.client.handlers.KieServerSetupMessageHandler;
import org.kie.server.controller.websocket.common.KieServerMessageHandlerWebSocketClient;
import org.kie.server.controller.websocket.common.WebSocketClient;
import org.kie.server.controller.websocket.common.config.WebSocketClientConfiguration;
import org.kie.server.services.api.KieControllerNotConnectedException;
import org.kie.server.services.api.KieControllerNotDefinedException;
import org.kie.server.services.api.KieServerRegistry;
import org.kie.server.services.api.KieServerRegistryAware;
import org.kie.server.services.impl.controller.DefaultRestControllerImpl;
import org.kie.server.services.impl.storage.KieServerState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebSocketKieServerControllerImpl implements KieServerController, KieServerRegistryAware {
    
    private static final Logger logger = LoggerFactory.getLogger(WebSocketKieServerControllerImpl.class);

    private KieServerRegistry context;
    private final KieServerMessageHandlerWebSocketClient client;
    private final Marshaller marshaller;
    
    private KieServerInfo serverInfo;
    
    private DefaultRestControllerImpl restController;

    public WebSocketKieServerControllerImpl() {
        this.marshaller = MarshallerFactory.getMarshaller(MarshallingFormat.JSON, this.getClass().getClassLoader());
        
        this.client = new KieServerMessageHandlerWebSocketClient((WebSocketClient client) -> {
            try {
                ((KieServerMessageHandlerWebSocketClient) client).sendTextWithInternalHandler(serialize(serverInfo),
                                                                                              message -> {
                                                                                                  logger.info("Successfully reconnected");
                                                                                                  return null;
                                                                                              });
            } catch (IOException e) {
                logger.warn("Error when trying to reconnect to Web Socket server - {}", e.getMessage());
            }
        });
    }

    @Override
    public KieServerSetup connect(KieServerInfo serverInfo) {
        KieServerState currentState = context.getStateRepository().load(KieServerEnvironment.getServerId());
        Set<String> controllers = currentState.getControllers();
        this.serverInfo = serverInfo;
        

        KieServerConfig config = currentState.getConfiguration();
        if (controllers != null && !controllers.isEmpty()) {
            for (String controllerUrl : controllers) {

                if (controllerUrl != null && !controllerUrl.isEmpty()) {
                    if (controllerUrl.toLowerCase().startsWith("ws")) {
                        
                        String connectAndSyncUrl = controllerUrl + "/" + KieServerEnvironment.getServerId();
    
                        final KieServerSetup kieServerSetup = new KieServerSetup();
                        try {
                            this.client.init(WebSocketClientConfiguration.builder()
                                                     .controllerUrl(connectAndSyncUrl)
                                                     .userName(config.getConfigItemValue(KieServerConstants.CFG_KIE_CONTROLLER_USER,
                                                                                         "kieserver"))
                                                     .password(KeyStoreHelperUtil.loadControllerPassword(config))
                                                     .token(config.getConfigItemValue(KieServerConstants.CFG_KIE_CONTROLLER_TOKEN))
                                                     .build());
                            CountDownLatch waitLatch = new CountDownLatch(1);
                            
                            client.sendTextWithInternalHandler(serialize(serverInfo), new KieServerSetupMessageHandler(context, waitLatch, kieServerSetup));
    
                            boolean received = waitLatch.await(10, TimeUnit.SECONDS);
                            if (received && kieServerSetup.getContainers() != null) {
                                // once there is non null list let's return it
                                return kieServerSetup;
    
                            }
    
                            break;
                        } catch (Exception e) {
                            // let's check all other controllers in case of running in cluster of controllers
                            logger.warn("Exception encountered while syncing with controller at {} error {}", connectAndSyncUrl, e.getCause() == null ? e.getMessage() : e.getCause().getMessage());
                            logger.debug("Exception encountered while syncing with controller at {} error {}", connectAndSyncUrl, e.getMessage(), e);
                        }
                    } else {
                        logger.info("Kie Server points to non Web Socket controller '{}', using default REST mechanism", controllerUrl);
                        
                        KieServerSetup kieServerSetup = restController.connectToSingleController(serverInfo, config, controllerUrl);
                        if (kieServerSetup != null) {
                            return kieServerSetup;
                        }
                    }

                }
            }

            throw new KieControllerNotConnectedException("Unable to connect to any controller");
        } else {
            throw new KieControllerNotDefinedException("Unable to connect to any controller");
        }
    }

    @Override
    public void disconnect(KieServerInfo serverInfo) {
        if (this.client.isActive()) {
            this.client.close();
        } else {            
            
            KieServerState currentState = context.getStateRepository().load(KieServerEnvironment.getServerId());
            Set<String> controllers = currentState.getControllers();
            KieServerConfig config = currentState.getConfiguration();
            if (controllers != null && !controllers.isEmpty()) {
                for (String controllerUrl : controllers) {

                    if (controllerUrl != null && !controllerUrl.isEmpty() && !controllerUrl.toLowerCase().startsWith("ws")) {
                        
                        logger.info("Kie Server points to non Web Socket controller '{}', using default REST mechanism", controllerUrl);
                        
                        boolean disconnected = restController.disconnectFromSingleController(serverInfo, config, controllerUrl);
                        if (disconnected) {
                            break;
                        }
                    }
                }
            }
        }
        
    }

    protected String serialize(Object object) {
        if (object == null) {
            return "";
        }

        try {
            return marshaller.marshall(object);
        } catch ( MarshallingException e ) {
            throw new IllegalStateException( "Error while serializing request data!", e );
        }
    }

    @Override
    public void setRegistry(KieServerRegistry registry) {
        this.context = registry;
        
        this.restController = new DefaultRestControllerImpl(this.context);
    }

    @Override
    public KieServerRegistry getRegistry() {
        return this.context;
    }

}
