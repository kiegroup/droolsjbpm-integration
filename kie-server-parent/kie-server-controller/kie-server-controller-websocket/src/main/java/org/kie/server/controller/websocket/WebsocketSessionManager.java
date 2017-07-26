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

package org.kie.server.controller.websocket;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

import javax.websocket.CloseReason;
import javax.websocket.CloseReason.CloseCodes;
import javax.websocket.Session;

import org.kie.server.controller.websocket.common.handlers.KieServerMessageHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebsocketSessionManager {

    private static final Logger logger = LoggerFactory.getLogger(WebsocketSessionManager.class);
    
    private ConcurrentMap<String, Session> availableSessionsById = new ConcurrentHashMap<>();
    private ConcurrentMap<String, List<Session>> availableSessionsByUrl = new ConcurrentHashMap<>();
    private ConcurrentMap<String, String> sessionToUrl = new ConcurrentHashMap<>();
    private ConcurrentMap<String, KieServerMessageHandler> handlersPerSession = new ConcurrentHashMap<>();
        
    private static WebsocketSessionManager INSTANCE = new WebsocketSessionManager();

    public static WebsocketSessionManager getInstance() {
        return INSTANCE;
    }
    
    public void addSession(Session session) {
        this.availableSessionsById.put(session.getId(), session);
        this.handlersPerSession.put(session.getId(), new KieServerMessageHandler(session));
        logger.debug("Session '" + session.getId() + "' added to websocket manager");
    }
    
    public void addSession(String url, Session session) {
        List<Session> newSessions =  new ArrayList<>();
        List<Session> sessions = this.availableSessionsByUrl.putIfAbsent(url, newSessions);
        if (sessions == null) {
            sessions = newSessions;
        }
        sessions.add(session);
        this.sessionToUrl.put(session.getId(), url);        
        logger.debug("Session '" + session.getId() + "' associated with url: " + url);
    }
    
    public String removeSession(Session session) {
        this.availableSessionsById.remove(session.getId());
        String url = sessionToUrl.remove(session.getId());
        
        List<Session> sessions = availableSessionsByUrl.get(url);
        Iterator<Session> it = sessions.iterator();
        
        while (it.hasNext()) {
            Session s = it.next();
            if (s.getId().equals(session.getId())) {
                it.remove();
                break;
            }
        }
        
        this.handlersPerSession.remove(session.getId());
        logger.debug("Session '" + session.getId() + "' removed to websocket manager");
        
        if (availableSessionsByUrl.get(url).isEmpty()) {
            return url;
        }
        
        return null;
    }
    
    public List<Session> getByUrl(String url) {
        List<Session> sessions = availableSessionsByUrl.get(url);
        
        if (sessions == null) {
            return Collections.emptyList();
        }
        
        return sessions.stream().filter(s -> s.isOpen()).collect(Collectors.toList());
    }
    
    public KieServerMessageHandler getHandler(String sessionId) {
        return this.handlersPerSession.get(sessionId);
    }
    
    public void close() {
        availableSessionsById.values().forEach(session -> {
            if (session.isOpen()) {
                try {
                    session.close(new CloseReason(CloseCodes.GOING_AWAY, "Server is going down"));
                } catch (Exception e) {
                   logger.warn("Unexpected error while shutting down websocket session", e);
                }
            }
        });
    }
}
