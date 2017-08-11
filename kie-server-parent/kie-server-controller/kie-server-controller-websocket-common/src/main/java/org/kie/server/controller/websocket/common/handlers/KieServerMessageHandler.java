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

package org.kie.server.controller.websocket.common.handlers;

import java.io.IOException;
import java.util.ArrayDeque;

import javax.websocket.MessageHandler;
import javax.websocket.Session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KieServerMessageHandler implements MessageHandler.Whole<String> {

    private static final Logger logger = LoggerFactory.getLogger(KieServerMessageHandler.class);
    private ArrayDeque<InternalMessageHandler> internalHandlers = new ArrayDeque<>();
    
    private Session session;    
    
    public KieServerMessageHandler(Session session) {
        this.session = session;
        this.session.addMessageHandler(this);
    }
    
    @Override
    public void onMessage(String message) {
        logger.debug("Message received with content '{}'", message);
        InternalMessageHandler handler = internalHandlers.poll();
        logger.debug("About to handle message with handler {}", handler);
        
        String response = handler.onMessage(message);
        logger.debug("Response to be send (if not null) is '{}'", response);
        // add handler if the current one has next one, this needs to be before sending response
        addHandler(handler.getNextHandler());                     
        if (response != null) {
            try {
                session.getBasicRemote().sendText(response);
                logger.debug("Response successfully sent");
                
                handler.afterResponseSent();
            } catch (IOException e) {
                logger.error("Error when sending response", e);
            }
        }
        
    }

    public void addHandler(InternalMessageHandler handler) {
        if (handler != null) {
            this.internalHandlers.add(handler);
        }
    }
}
