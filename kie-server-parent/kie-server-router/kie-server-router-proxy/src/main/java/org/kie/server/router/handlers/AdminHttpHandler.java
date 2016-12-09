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

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.jboss.logging.Logger;
import org.json.JSONObject;
import org.kie.server.router.Configuration;
import org.kie.server.router.proxy.KieServerProxyClient;
import org.kie.server.router.proxy.aggragate.JSONResponseAggregator;
import org.kie.server.router.proxy.aggragate.JaxbXMLResponseAggregator;
import org.kie.server.router.proxy.aggragate.ResponseAggregator;
import org.kie.server.router.proxy.aggragate.XstreamXMLResponseAggregator;
import org.kie.server.router.repository.ConfigurationMarshaller;
import org.kie.server.router.repository.FileRepository;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.ResponseCodeHandler;
import io.undertow.util.Headers;

public class AdminHttpHandler implements HttpHandler {
    
    private static final Logger log = Logger.getLogger(AdminHttpHandler.class);
    
    private KieServerProxyClient proxyClient;
    private Configuration configuration = new Configuration();
    private List<ResponseAggregator> aggregators = new ArrayList<>();
    
    private FileRepository repository = null;
    private ConfigurationMarshaller marshaller = new ConfigurationMarshaller();
        
    public AdminHttpHandler(KieServerProxyClient proxyClient, FileRepository repository) {
        this.proxyClient = proxyClient;
        this.repository = repository;
        
        Configuration loaded = repository.load();
        if (loaded != null) {
            this.configuration = loaded;
            
            Map<String, Set<String>> perContainer = this.configuration.getHostsPerContainer();
            
            for (Entry<String, Set<String>> entry : perContainer.entrySet()) {
                
                String containerId = entry.getKey();
                
                entry.getValue().forEach(url -> {
                    proxyClient.addContainer(containerId, URI.create(url));
                });
            }
        }
        
        this.aggregators.add(new JSONResponseAggregator());
        this.aggregators.add(new XstreamXMLResponseAggregator());
        this.aggregators.add(new JaxbXMLResponseAggregator());
    }

    @Override
    public void handleRequest(final HttpServerExchange exchange) throws Exception {
        final String path = exchange.getRelativePath();
        
        if (path.startsWith("/list")) {
            
            String jsonConfig = marshaller.marshall(configuration);
            exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
            exchange.getResponseHeaders().put(Headers.CONTENT_LENGTH, jsonConfig.getBytes("UTF-8").length);
            exchange.getResponseSender().send(jsonConfig);
            
            return;
        } 
        
        exchange.getRequestReceiver().receiveFullString((ex, data) -> {
            try {
            
            
            JSONObject jsonData = new JSONObject(data);
            
            String containerId = jsonData.getString("containerId");
            String serverId = jsonData.getString("serverId");
            String serverUrl = jsonData.getString("serverUrl");
            
            if (path.startsWith("/add")) {
                proxyClient.addContainer(containerId, URI.create(serverUrl));
                log.infof("Added %s as server location for container %s ", serverUrl, containerId);
                
                synchronized (configuration) {                
                    configuration.addContainerHost(containerId, serverUrl);
                    configuration.addServerHost(serverId, serverUrl);
                    
                    repository.persist(configuration);
                }
                ResponseCodeHandler.HANDLE_200.handleRequest(exchange);
            
            } else if (path.startsWith("/remove")) {
            
                proxyClient.removeContainer(containerId, URI.create(serverUrl));
                log.infof("Removed %s as server location for container %s ", serverUrl, containerId);
                synchronized (configuration) {
                    configuration.removeContainerHost(containerId, serverUrl);
                    configuration.removeServerHost(serverId, serverUrl);
                    
                    repository.persist(configuration);
                }
                
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
    
    public Map<String, Set<String>> getHostsPerServer() {
        return configuration.getHostsPerServer();
    }

    
    public List<ResponseAggregator> getAggregators() {
        return Collections.unmodifiableList(aggregators);
    }
}
