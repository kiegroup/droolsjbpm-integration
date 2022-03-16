/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
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

package org.kie.server.router.handlers;

import org.jboss.logging.Logger;
import org.json.JSONObject;
import org.kie.server.router.ConfigurationManager;
import org.kie.server.router.KieServerRouterEnvironment;
import org.kie.server.router.utils.HttpUtils;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.ResponseCodeHandler;
import io.undertow.util.Headers;

public class AdminHttpHandler implements HttpHandler {

    private static final Logger log = Logger.getLogger(AdminHttpHandler.class);

    private ConfigurationManager configurationManager;

    public AdminHttpHandler(ConfigurationManager configurationManager) {
        this.configurationManager = configurationManager;
    }

    @Override
    public void handleRequest(final HttpServerExchange exchange) throws Exception {
        final String path = exchange.getRelativePath();
        
        if (path.startsWith("/list")) {
            
            String jsonConfig = configurationManager.toJsonConfig();
            exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
            exchange.getResponseHeaders().put(Headers.CONTENT_LENGTH, jsonConfig.getBytes("UTF-8").length);
            exchange.getResponseSender().send(jsonConfig);
            
            return;
        } 
        
        exchange.getRequestReceiver().receiveFullString((ex, data) -> {
            try {
                JSONObject jsonData = new JSONObject(data);

                String containerId = jsonData.getString("containerId");
                String alias = jsonData.getString("alias");
                String serverId = jsonData.getString("serverId");
                String serverUrl = jsonData.getString("serverUrl");
                String releaseId = jsonData.getString("releaseId");
                if (path.startsWith("/add")) {
                    log.infof("Added %s as server location for container %s ", serverUrl, containerId);
                    configurationManager.add(containerId, alias, serverId, serverUrl, releaseId);
                    ResponseCodeHandler.HANDLE_200.handleRequest(exchange);
                } else if (path.startsWith("/remove")) {                    
                    log.infof("Removed %s as server location for container %s ", serverUrl, containerId);
                    configurationManager.remove(containerId, alias, serverId, serverUrl, releaseId);
                    ResponseCodeHandler.HANDLE_200.handleRequest(exchange);
                } else {
                    exchange.getResponseHeaders().put(Headers.STATUS, "");
                    ResponseCodeHandler.HANDLE_404.handleRequest(exchange);
                }
            } catch (Exception e) {
                log.error("Error while performing admin operation", e);
                
            }
        });
    }

}
