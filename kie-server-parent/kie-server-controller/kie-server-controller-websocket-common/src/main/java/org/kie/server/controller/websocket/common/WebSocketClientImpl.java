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

package org.kie.server.controller.websocket.common;

import java.io.IOException;
import java.net.URI;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import javax.websocket.*;

import org.kie.server.controller.websocket.common.auth.WebSocketAuthConfigurator;
import org.kie.server.controller.websocket.common.config.WebSocketClientConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class WebSocketClientImpl<T extends MessageHandler> extends Endpoint implements WebSocketClient<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(WebSocketClientImpl.class);

    private WebSocketContainer container = null;

    private Session session = null;

    private ClientEndpointConfig config = null;

    protected T messageHandler;

    private AtomicBoolean closed = new AtomicBoolean(true);

    private Thread reconnectThread = null;

    private Consumer<WebSocketClient> onReconnect;

    public WebSocketClientImpl() {
        super();
    }

    public WebSocketClientImpl(final Consumer<WebSocketClient> onReconnect) {
        this();
        this.onReconnect = onReconnect;
    }

    @Override
    public void onClose(Session session, CloseReason reason) {
        if (!session.getId().equals(this.session.getId())) {
            LOGGER.info("Session closed does not match this session... ignoring");
            return;
        }
        LOGGER.info("Session {} is closed due to {}", session.getId(), reason);

        if (!closed.get()) {

            reconnectThread = new Thread(() -> {

                while (!session.isOpen()) {
                    try {
                        LOGGER.debug("Waiting 10 seconds before attempting to reconnect to controller {}", session.getRequestURI());
                        Thread.sleep(10000);

                        this.session = container.connectToServer(this, this.config, session.getRequestURI());
                        if (onReconnect != null) {
                            onReconnect.accept(this);
                        }
                        break;
                    } catch (InterruptedException e) {
                        break;
                    } catch (RuntimeException|DeploymentException|IOException e) {
                        LOGGER.warn("Unable to reconnect to controller over Web Socket {} due to {}", session.getRequestURI(), e.getMessage());
                    }
                }
            }, "Kie Server - Web Socket reconnect");
            reconnectThread.start();
        }
    }

    @Override
    public void init(final WebSocketClientConfiguration clientConfig) {
        try {
            if (container == null) {
                container = ContainerProvider.getWebSocketContainer();
                container.setDefaultMaxSessionIdleTimeout(clientConfig.getMaxSessionIdleTimeout());
                container.setAsyncSendTimeout(clientConfig.getAsyncSendTimeout());
            }
            this.config = ClientEndpointConfig.Builder.create()
                    .configurator(new WebSocketAuthConfigurator(clientConfig.getUserName(),
                                                                clientConfig.getPassword(),
                                                                clientConfig.getToken()))
                    .encoders(clientConfig.getEncoders())
                    .decoders(clientConfig.getDecoders())
                    .build();
            session = container.connectToServer(this, this.config, URI.create(clientConfig.getControllerUrl()));
            LOGGER.info("New Web Socket Session with id: {}, started", session.getId());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() {
        LOGGER.debug("Closing Web Socket connection to controller");
        this.closed.set(true);
        if (reconnectThread != null) {
            reconnectThread.interrupt();
        }
        try {
            this.messageHandler = null;
            session.close();
        } catch (IOException e) {
            LOGGER.warn("Unexpected error while closing Web Socket connection to controller", e);
        }
    }

    @Override
    public void sendTextWithHandler(final String content,
                                    final Consumer<T> handler) throws IOException {
        if (!session.isOpen()) {
            throw new RuntimeException("No connection to controller");
        }

        if(handler != null && this.messageHandler != null) {
            handler.accept(this.messageHandler);
        }

        LOGGER.debug("Sending text message using Web Socket Session with id: {}", session.getId());

        session.getBasicRemote().sendText(content);
    }

    @Override
    public boolean isActive() {
        return session != null && session.isOpen();
    }

    @Override
    public void onOpen(final Session session,
                       final EndpointConfig config) {
        LOGGER.info("Connection to Kie Controller over Web Socket is now open with session id: {}", session.getId());
        if(this.messageHandler != null) {
            session.addMessageHandler(this.messageHandler);
        }
        this.closed.set(false);
    }

    @Override
    public void onError(final Session session,
                        final Throwable thr) {
        LOGGER.error("Error received on session id: {}, message: {}", session.getId(), thr.getMessage(), thr);
    }
}
