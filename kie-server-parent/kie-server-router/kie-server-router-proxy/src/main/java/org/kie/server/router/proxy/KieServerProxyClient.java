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

package org.kie.server.router.proxy;

import java.net.URI;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.proxy.LoadBalancingProxyClient;
import io.undertow.server.handlers.proxy.ProxyCallback;
import io.undertow.server.handlers.proxy.ProxyClient;
import io.undertow.server.handlers.proxy.ProxyConnection;


public class KieServerProxyClient implements ProxyClient {
    
    private static final String NOT_FOUND = "NOT_FOUND";
    
    private Pattern p = Pattern.compile(".*/containers/([^/]+).*");
    private Pattern p2 = Pattern.compile(".*/containers/instances/([^/]+).*");
    
    private Map<String, LoadBalancingProxyClient> containerClients = new ConcurrentHashMap<>();
    
    public synchronized void addContainer(String containerId, URI serverURI) {
        
        LoadBalancingProxyClient client = containerClients.get(containerId);
        if (client == null) {
            client = new LoadBalancingProxyClient();
            containerClients.put(containerId, client);
        }
        client.addHost(serverURI);
    }
    
   public synchronized void removeContainer(String containerId, URI serverURI) {
        
        LoadBalancingProxyClient client = containerClients.get(containerId);
        if (client == null) {
            return;
        }
        client.removeHost(serverURI);
    }

    @Override
    public ProxyTarget findTarget(HttpServerExchange exchange) {
        
        String containerId = resolveContainerId(exchange);
        LoadBalancingProxyClient client = containerClients.get(containerId);
        
        if (client == null) {
            return null;
        }
        
        return client.findTarget(exchange);
    }

    @Override
    public void getConnection(ProxyTarget target, HttpServerExchange exchange, ProxyCallback<ProxyConnection> callback, long timeout, TimeUnit timeUnit) {
        String containerId = resolveContainerId(exchange);
        LoadBalancingProxyClient client = containerClients.get(containerId);

        client.getConnection(target, exchange, callback, timeout, timeUnit);
    }

    protected String resolveContainerId(HttpServerExchange exchange) {
        String relativePath = exchange.getRelativePath();                
        Matcher matcher = p.matcher(relativePath);
        
        if (matcher.find()) {            
            String containerId = matcher.group(1);
            if (containerClients.containsKey(containerId)) {
                return containerId;
            }
        }
        matcher = p2.matcher(relativePath);
        
        if (matcher.find()) {            
            return matcher.group(1);            
        }
        return NOT_FOUND;
    }
}
