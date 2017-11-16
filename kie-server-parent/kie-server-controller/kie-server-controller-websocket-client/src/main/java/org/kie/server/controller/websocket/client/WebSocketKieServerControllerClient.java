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
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import javax.websocket.ClientEndpointConfig;
import javax.websocket.CloseReason;
import javax.websocket.ContainerProvider;
import javax.websocket.Decoder;
import javax.websocket.Encoder;
import javax.websocket.Endpoint;
import javax.websocket.EndpointConfig;
import javax.websocket.Extension;
import javax.websocket.OnClose;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;

import org.kie.server.api.KieServerConstants;
import org.kie.server.api.model.KieServerConfig;
import org.kie.server.common.KeyStoreHelperUtil;
import org.kie.server.controller.websocket.common.handlers.InternalMessageHandler;
import org.kie.server.controller.websocket.common.handlers.KieServerMessageHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebSocketKieServerControllerClient extends Endpoint {
    
    private static final Logger logger = LoggerFactory.getLogger(WebSocketKieServerControllerClient.class);
    
    public static final String AUTHORIZATION = "Authorization";

    private WebSocketContainer container = null;
    private Session session = null;
    
    private String controllerUrl = null;
    private KieServerConfig config = null;
    
    private KieServerMessageHandler messageHandler;    

    private AtomicBoolean closed = new AtomicBoolean(true); 
    
    private Thread reconnectThread = null;
    
    private Consumer<WebSocketKieServerControllerClient> onReconnect;
    
    
    public WebSocketKieServerControllerClient(Consumer<WebSocketKieServerControllerClient> onReconnect) {
        this.onReconnect = onReconnect;
    }
    
    @OnClose
    public void onClose(Session session, CloseReason reason) {
        if (!session.getId().equals(this.session.getId())) {
            logger.info("Session closed does not match this session... ignoring");
            return;
        }
        logger.info("Session {} is closed due to {}", session.getId(), reason);
        
        if (!closed.get()) {
            
            reconnectThread = new Thread(() -> {
                
                while (!session.isOpen()) {
                    try {
                        
                        logger.debug("Waiting 10 seconds before attempting to reconnect to controller {}", controllerUrl);
                        Thread.sleep(10000);
                        
                        init(controllerUrl, config);
                        onReconnect.accept(this);
                        break;
                    } catch (InterruptedException e) {
                        break;
                    } catch (RuntimeException e) {
                        logger.warn("Unable to reconnect to controller over Web Socket {} due to {}", controllerUrl, e.getMessage());
                    }
                }
            }, "Kie Server - Web Socket reconnect");
            reconnectThread.start();
        }
    }
    
    public void init(final String controllerUrl, final KieServerConfig config) {
        this.config = config;
        this.controllerUrl = controllerUrl;
        try {
            if (container == null) {
                container = ContainerProvider.getWebSocketContainer();
            }
            session = container.connectToServer(this, new ClientEndpointConfig() {
                
                
                @Override
                public Map<String, Object> getUserProperties() {
                    return Collections.emptyMap();
                }
                
                @Override
                public List<Class<? extends Encoder>> getEncoders() {
                    return Collections.emptyList();
                }
                
                @Override
                public List<Class<? extends Decoder>> getDecoders() {
                    return Collections.emptyList();
                }
                
                @Override
                public List<String> getPreferredSubprotocols() {
                    return Collections.emptyList();
                }
                
                @Override
                public List<Extension> getExtensions() {
                    return Collections.emptyList();
                }
                
                @Override
                public Configurator getConfigurator() {
                   
                    return new Configurator(){

                        @Override
                        public void beforeRequest(Map<String, List<String>> headers) {                            
                            super.beforeRequest(headers);
                            
                            String userName = config.getConfigItemValue(KieServerConstants.CFG_KIE_CONTROLLER_USER, "kieserver");
                            String password = KeyStoreHelperUtil.loadControllerPassword(config);
                            String token = config.getConfigItemValue(KieServerConstants.CFG_KIE_CONTROLLER_TOKEN);
                            
                            if (token != null && !token.isEmpty()) {
                                headers.put(AUTHORIZATION, Arrays.asList("Bearer " + token));
                            } else {
                                try {
                                    headers.put(AUTHORIZATION, Arrays.asList("Basic " + Base64.getEncoder().encodeToString((userName + ':' + password).getBytes("UTF-8"))));
                                } catch (UnsupportedEncodingException e) {
                                    logger.warn(e.getMessage());
                                }
                            }
                        }
                        
                    };
                }
            }, URI.create(controllerUrl));
            
            this.messageHandler = new KieServerMessageHandler(session);            
            this.closed.set(false);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    public void close() {
        this.closed.set(true);
        if (reconnectThread != null) {
            reconnectThread.interrupt();
        }
        try {
            this.messageHandler = null;
            session.close();
        } catch (IOException e) {
            logger.warn("Unexpected error while closing Web Socket connection to controller", e);
        }
    }
    
    public void sendWithHandler(String content, InternalMessageHandler handler) throws IOException {
        if (!session.isOpen()) {
            throw new RuntimeException("No connection to controller");
        }
        
        messageHandler.addHandler(handler);
        session.getBasicRemote().sendText(content);
    }
    
    public boolean isActive() {
        if (session != null && session.isOpen()) {
            return true;
        }
        
        return false;
    }

    @Override
    public void onOpen(Session session, EndpointConfig config) {
        logger.info("Connection to Kie Controller over Web Socket is now open with session id " + session.getId());
        
    }

    @Override
    public void onError(Session session, Throwable thr) {
        logger.error("Error received {} on session {}", thr.getMessage(), session.getId(), thr);
    }
}
