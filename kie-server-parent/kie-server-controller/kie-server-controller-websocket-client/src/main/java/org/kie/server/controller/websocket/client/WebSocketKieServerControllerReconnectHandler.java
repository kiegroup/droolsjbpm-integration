/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
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

import org.kie.server.controller.websocket.client.handlers.CommandScriptMessageHandler;
import org.kie.server.controller.websocket.common.handlers.InternalMessageHandler;
import org.kie.server.services.api.KieServerRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebSocketKieServerControllerReconnectHandler implements InternalMessageHandler {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketKieServerControllerReconnectHandler.class);

    private KieServerRegistry context;

    public WebSocketKieServerControllerReconnectHandler(KieServerRegistry context) {
        this.context = context;
    }

    @Override
    public String onMessage(String message) {
        logger.info("Successfully reconnected");
        return null;
    }

    public InternalMessageHandler getNextHandler() {
        return new CommandScriptMessageHandler(context);
    };
}
