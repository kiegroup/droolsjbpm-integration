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

import java.util.function.Consumer;

import org.kie.server.controller.api.model.events.*;
import org.kie.server.controller.client.event.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebSocketEventHandler implements Consumer<KieServerControllerEvent> {

    private static final Logger LOGGER = LoggerFactory.getLogger(WebSocketEventHandler.class);

    private EventHandler handler;

    public WebSocketEventHandler(final EventHandler handler) {
        this.handler = handler;
    }

    @Override
    public void accept(final KieServerControllerEvent event) {
        LOGGER.debug("Handling Kie Server Controller event: {}", event);
        if (event instanceof ServerInstanceConnected) {
            handler.onServerInstanceConnected((ServerInstanceConnected)event);
        } else if(event instanceof ServerInstanceDeleted){
            handler.onServerInstanceDeleted((ServerInstanceDeleted)event);
        } else if(event instanceof ServerInstanceDisconnected){
            handler.onServerInstanceDisconnected((ServerInstanceDisconnected)event);
        } else if(event instanceof ServerInstanceUpdated){
            handler.onServerInstanceUpdated((ServerInstanceUpdated)event);
        } else if(event instanceof ServerTemplateDeleted){
            handler.onServerTemplateDeleted((ServerTemplateDeleted)event);
        } else if(event instanceof ServerTemplateUpdated){
            handler.onServerTemplateUpdated((ServerTemplateUpdated)event);
        } else if(event instanceof ContainerSpecUpdated){
            handler.onContainerSpecUpdated((ContainerSpecUpdated)event);
        } else {
            LOGGER.warn("Unable to handle Kie Server Controller event: {}", event);
        }
    }
}
