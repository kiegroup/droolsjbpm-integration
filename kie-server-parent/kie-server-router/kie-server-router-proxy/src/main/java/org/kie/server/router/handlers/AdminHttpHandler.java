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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.jboss.logging.Logger;
import org.json.JSONObject;
import org.kie.server.router.Configuration;
import org.kie.server.router.ContainerInfo;
import org.kie.server.router.KieServerRouterConstants;
import org.kie.server.router.proxy.aggragate.JSONResponseAggregator;
import org.kie.server.router.proxy.aggragate.JaxbXMLResponseAggregator;
import org.kie.server.router.proxy.aggragate.ResponseAggregator;
import org.kie.server.router.proxy.aggragate.XstreamXMLResponseAggregator;
import org.kie.server.router.repository.ConfigurationMarshaller;
import org.kie.server.router.spi.ConfigRepository;
import org.kie.server.router.utils.FailedHostInfo;
import org.kie.server.router.utils.HttpUtils;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.ResponseCodeHandler;
import io.undertow.util.Headers;

public class AdminHttpHandler implements HttpHandler {
    
    private static final Logger log = Logger.getLogger(AdminHttpHandler.class);

    private String CONTROLLER = System.getProperty(KieServerRouterConstants.CONTROLLER);
    private int interval = Integer.parseInt(System.getProperty(KieServerRouterConstants.KIE_SERVER_CONTROLLER_ATTEMPT_INTERVAL, "5"));
    private int attemptsLimit = Integer.parseInt(System.getProperty(KieServerRouterConstants.KIE_SERVER_RECOVERY_ATTEMPT_LIMIT, "100"));
    
//    private KieServerProxyClient proxyClient;
    private Configuration configuration = new Configuration();
    private List<ResponseAggregator> aggregators = new ArrayList<>();
    
    private ConfigRepository repository = null;
    private ConfigurationMarshaller marshaller = new ConfigurationMarshaller();

    private Set<String> controllerContainers = new HashSet<>();

    private ScheduledExecutorService executorService;
    
    private ScheduledFuture<?> failedHostsReconnects;
    private ScheduledFuture<?> addToControllerAttempts;
    private ScheduledFuture<?> removeFromControllerAttempts;
    
    private CopyOnWriteArrayList<FailedHostInfo> failedHosts = new CopyOnWriteArrayList<>();
    private CopyOnWriteArrayList<ContainerInfo> containersToAddToController = new CopyOnWriteArrayList<>();
    private CopyOnWriteArrayList<String> containersToRemoveFromController = new CopyOnWriteArrayList<>();

    private static final String CONTAINER_SPEC_JSON = "{\n" +
            "    \"container-id\" : \"#1@\",\n" +
            "    \"container-name\" : \"#2@\",\n" +
            "    \"server-template-key\" : {\n" +
            "      \"server-id\" : \"kie-server-router\",\n" +
            "      \"server-name\" : \"KIE Server Router\"\n" +
            "    },\n" +
            "    \"release-id\" : {\n" +
            "      \"group-id\" : \"#3@\",\n" +
            "      \"artifact-id\" : \"#4@\",\n" +
            "      \"version\" : \"#5@\"\n" +
            "    },\n" +
            "    \"configuration\" : { },\n" +
            "    \"status\" : \"STARTED\"\n" +
            "  }";
        
    public AdminHttpHandler(Configuration configuration, ConfigRepository repository, ScheduledExecutorService executorService) {
        this.configuration = configuration;
        this.repository = repository;
        this.executorService = executorService;

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
                String alias = jsonData.getString("alias");
                String serverId = jsonData.getString("serverId");
                String serverUrl = jsonData.getString("serverUrl");
                String releaseId = jsonData.getString("releaseId");
    
                ContainerInfo containerInfo = new ContainerInfo(containerId, alias, releaseId);
                
                if (path.startsWith("/add")) {
                    log.infof("Added %s as server location for container %s ", serverUrl, containerId);
                    
                    synchronized (configuration) {                
                        configuration.addContainerHost(containerId, serverUrl);
                        configuration.addContainerHost(alias, serverUrl);
                        configuration.addServerHost(serverId, serverUrl);
    
                        configuration.addContainerInfo(containerInfo);
                        
                        repository.persist(configuration);
                    }
    
                    updateControllerOnAdd(containerId, releaseId, alias, containerInfo);
    
                    ResponseCodeHandler.HANDLE_200.handleRequest(exchange);
                
                } else if (path.startsWith("/remove")) {
    
                    log.infof("Removed %s as server location for container %s ", serverUrl, containerId);
                    synchronized (configuration) {
                        configuration.removeContainerHost(containerId, serverUrl);
                        configuration.removeContainerHost(alias, serverUrl);
                        configuration.removeServerHost(serverId, serverUrl);
    
                        configuration.removeContainerInfo(containerInfo);
                        
                        repository.persist(configuration);
                    }
                    
                    updateControllerOnRemove(containerId);
                    
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
    
    public Map<String, List<String>> getHostsPerServer() {
        return configuration.getHostsPerServer();
    }

    
    public List<ResponseAggregator> getAggregators() {
        return Collections.unmodifiableList(aggregators);
    }

    public void addControllerContainers(List<String> containers) {
        this.controllerContainers.addAll(containers);
    }

    public Configuration getConfiguration() {
        return configuration;
    }
    public void removeUnavailableServer(String url) {
        synchronized (configuration) {
            FailedHostInfo failedHost = configuration.removeUnavailableServer(url);
            repository.persist(configuration);
            
            failedHosts.add(failedHost);
            log.debug("Scheduling host checks...");
            failedHostsReconnects = executorService.scheduleAtFixedRate(() -> {
                if (failedHosts.isEmpty()) {
                    return;
                }
                Iterator<FailedHostInfo> it = failedHosts.iterator();
                while (it.hasNext()) {
                    FailedHostInfo fHost = (FailedHostInfo) it.next();
                    if (attemptsLimit == fHost.getAttempts()) {
                        it.remove();
                        log.info("Host " + fHost.getServerUrl() + " has reached reconnect attempts limit " + attemptsLimit + " quiting");
                        continue;
                    }
                    try {
                        HttpUtils.getHttpCall(fHost.getServerUrl());
                        log.info("Server at " + fHost.getServerUrl() + " is back online");
                        failedHostsReconnects.cancel(false);
                        
                        synchronized (configuration) {
                            for (String containerId : fHost.getContainers()) {
                                configuration.addContainerHost(containerId, fHost.getServerUrl());
                            }
                            configuration.addServerHost(fHost.getServerId(), fHost.getServerUrl());

                            repository.persist(configuration);
                        }                        
                    } catch (Exception e) {
                        log.debug("Host " + fHost.getServerUrl() + " is still not available, attempting to reconnect in " + interval + " seconds, error " + e.getMessage());
                    } finally {
                        fHost.attempted();
                    }
                }
            },
            interval, interval, TimeUnit.SECONDS);
        }
    }
    
    protected void pushToController(String releaseId, String containerId, String alias) throws Exception {
        String[] gav = releaseId.split(":");
        String jsonPayload = CONTAINER_SPEC_JSON
                .replaceFirst("#1@", containerId)
                .replaceFirst("#2@", alias)
                .replaceFirst("#3@", gav[0])
                .replaceFirst("#4@", gav[1])
                .replaceFirst("#5@", gav[2]);
        HttpUtils.putHttpCall(CONTROLLER + "/management/servers/" + KieServerInfoHandler.getRouterId() + "/containers/" + containerId, jsonPayload);        
        log.infof("Added %s container into controller at %s ", containerId, CONTROLLER);        
    }
    
    protected void dropFromController(String containerId) throws Exception {
        HttpUtils.deleteHttpCall(CONTROLLER + "/management/servers/" + KieServerInfoHandler.getRouterId() + "/containers/" + containerId);
        
        log.infof("Removed %s container from controller at %s ", containerId, CONTROLLER);
    }
    
    protected void updateControllerOnRemove(String containerId) {
        if (CONTROLLER != null && controllerContainers.contains(containerId)) {
            List<String> hostsPerContainer = configuration.getHostsPerContainer().getOrDefault(containerId, Collections.emptyList());
            if (hostsPerContainer.isEmpty()) {
                
                controllerContainers.remove(containerId);               
                containersToRemoveFromController.add(containerId);
                if (removeFromControllerAttempts == null) {
                    removeFromControllerAttempts = executorService.scheduleAtFixedRate(() -> {
                        
                        try {
                            List<String> sent = new ArrayList<>();
                            for (String container : containersToRemoveFromController) {

                                if (controllerContainers.contains(container)) {
                                    // skip given container if it's back in controllers containers                                            
                                    sent.add(container);
                                    continue;
                                }
                                dropFromController(container);
                                sent.add(container);
                            }
                            containersToRemoveFromController.removeAll(sent);
                            if (containersToRemoveFromController.isEmpty()) {
                                removeFromControllerAttempts.cancel(false);
                                removeFromControllerAttempts = null;
                            }
                        } catch (Exception e) {
                            log.warn("Exception when notifying controller about deleted containers " + e.getMessage() + " next attempt in " + interval + " seconds");
                            log.debug(e);
                        }
                    },
                    interval, interval, TimeUnit.SECONDS);
                }
            }
            
        }
    }
    
    protected void updateControllerOnAdd(String containerId, String releaseId, String alias, ContainerInfo containerInfo) {
        if (CONTROLLER != null && releaseId != null && !controllerContainers.contains(containerId)) {
            
            controllerContainers.add(containerId);
            containersToAddToController.add(containerInfo);
            if (addToControllerAttempts == null) {
                addToControllerAttempts = executorService.scheduleAtFixedRate(() -> {
                    
                    try {
                        List<ContainerInfo> sent = new ArrayList<>();
                        for (ContainerInfo container : containersToAddToController) {
                            if (!controllerContainers.contains(container.getContainerId())) {
                                
                                sent.add(container);
                                continue;
                            }
                            
                            pushToController(container.getReleaseId(), container.getContainerId(), container.getAlias());
                            sent.add(container);                            
                        }
                        
                        containersToAddToController.removeAll(sent);
                        if (containersToAddToController.isEmpty()) {
                            addToControllerAttempts.cancel(false);
                            addToControllerAttempts = null;
                        }
                    } catch (Exception e) {
                        log.warn("Exception when notifying controller about deleted containers " + e.getMessage() + " next attempt in " + interval + " seconds");
                        log.debug("Stacktrace", e);
                    }
                },
                interval, interval, TimeUnit.SECONDS);
            }
            
        }
    }
}
