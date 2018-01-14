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

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import javax.websocket.Session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebSocketNotificationSessionManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(WebSocketNotificationSessionManager.class);
    private static final WebSocketNotificationSessionManager INSTANCE = new WebSocketNotificationSessionManager();

    private final ConcurrentMap<String, Session> availableSessionsById = new ConcurrentHashMap<>();

    public static WebSocketNotificationSessionManager getInstance() {
        return INSTANCE;
    }

    public void addSession(Session session) {
        this.availableSessionsById.put(session.getId(),
                                       session);
        LOGGER.debug("Session '" + session.getId() + "' added to Web Socket Notification manager");
    }

    public void removeSession(Session session) {
        this.availableSessionsById.remove(session.getId());
        LOGGER.debug("Session '" + session.getId() + "' removed from Web Socket Notification manager");
    }

    public Collection<Session> getSessions(){
        return availableSessionsById.values();
    }
}
