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

import java.util.Collections;
import java.util.ServiceLoader;

import javax.annotation.PostConstruct;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.websocket.CloseReason;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

import org.kie.server.api.marshalling.MarshallingFormat;
import org.kie.server.api.model.KieServerInfo;
import org.kie.server.controller.api.model.KieServerSetup;
import org.kie.server.controller.api.service.NotificationService;
import org.kie.server.controller.api.service.PersistingServerTemplateStorageService;
import org.kie.server.controller.api.storage.KieServerTemplateStorage;
import org.kie.server.controller.impl.KieServerControllerImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ServerEndpoint("/websocket/controller/{server-id}")
public class WebsocketKieServerControllerImpl extends KieServerControllerImpl {
    
    private static final Logger logger = LoggerFactory.getLogger(WebsocketKieServerControllerImpl.class);
    
    private WebsocketSessionManager manager = WebsocketSessionManager.getInstance();
    
    @Inject
    private Instance<KieServerTemplateStorage> templateStorage;
    
    @Inject
    private Instance<NotificationService> notificationService;    
    
    public WebsocketKieServerControllerImpl() {
        ServiceLoader<PersistingServerTemplateStorageService> storageServices = ServiceLoader.load(PersistingServerTemplateStorageService.class);
        if (storageServices != null && storageServices.iterator().hasNext()) {
            PersistingServerTemplateStorageService storageService = storageServices.iterator().next();
            setTemplateStorage(storageService.getTemplateStorage());
            logger.debug("Server template storage for standalone kie server controller is {}", storageService.getTemplateStorage().toString());
        } else {
            logger.debug("No server template storage defined. Default storage: InMemoryKieServerTemplateStorage will be used");
        }
    }
    
    @PostConstruct
    public void configure() {
        try {
            setTemplateStorage(templateStorage.get());
        } catch (RuntimeException e) {
            logger.warn("Unable to find template storage implementation, using in memory");
        }
        try {
            setNotificationService(notificationService.get());
        } catch (RuntimeException e) {
            logger.warn("Unable to find notification service implementation, using logging only");
        }
    }

    
    @OnOpen
    public void onKieServerConnect(@PathParam("server-id") String serverId, Session session) {
        manager.addSession(session);
        
        manager.getHandler(session.getId()).addHandler((String message) -> {

            String contentType = MarshallingFormat.JSON.getType();
            
            KieServerInfo serverInfo = WebsocketUtils.unmarshal(message, contentType, KieServerInfo.class);
            manager.addSession(serverInfo.getLocation(), session);
            
            logger.debug("Server info {}", serverInfo);
            KieServerSetup serverSetup = connect(serverInfo);

            logger.info("Server with id '{}' connected", serverId);
            String response = WebsocketUtils.marshal(contentType, serverSetup);
            
            return response;

        });
    }
    
    @OnClose
    public void onKieServerDisconnect(@PathParam("server-id") String serverId, Session session, CloseReason closeReason) {
        String url = manager.removeSession(session);
        if (url != null) {
            KieServerInfo serverInfo = new KieServerInfo(serverId, "", "", Collections.<String>emptyList(), url);
            disconnect(serverInfo);
            logger.info("Server with id '{}' disconnected", serverId);
        }
        
    }
    
    @OnError
    public void onKieServerError(Session session, Throwable e) {
//        manager.removeSession(session);
        logger.error("Unexpected error", e);
    }
}
