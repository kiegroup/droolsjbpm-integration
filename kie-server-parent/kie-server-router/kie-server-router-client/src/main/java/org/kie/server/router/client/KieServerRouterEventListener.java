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

package org.kie.server.router.client;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.kie.server.api.KieServerConstants;
import org.kie.server.api.model.KieContainerResource;
import org.kie.server.api.model.KieContainerResourceFilter;
import org.kie.server.api.model.KieContainerResourceList;
import org.kie.server.api.model.KieContainerStatusFilter;
import org.kie.server.api.model.ReleaseIdFilter;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.common.rest.KieServerHttpRequest;
import org.kie.server.common.rest.KieServerHttpRequestException;
import org.kie.server.common.rest.KieServerHttpResponse;
import org.kie.server.services.api.KieContainerInstance;
import org.kie.server.services.api.KieServer;
import org.kie.server.services.api.KieServerEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KieServerRouterEventListener implements KieServerEventListener {
    
    private static final Logger logger = LoggerFactory.getLogger(KieServerRouterEventListener.class);

    private static final String ROUTER_ADD_URL = "/mgmt/add";
    private static final String ROUTER_REMOVE_URL = "/mgmt/remove";
    
    private String serverId = System.getProperty(KieServerConstants.KIE_SERVER_ID);

    private String serverURL = System.getProperty(KieServerConstants.KIE_SERVER_LOCATION);
    private String routerURL = System.getProperty(KieServerConstants.KIE_SERVER_ROUTER);
    private String username = System.getProperty(KieServerConstants.KIE_ROUTER_MANAGEMENT_USERNAME, serverId);
    private String password = System.getProperty(KieServerConstants.KIE_ROUTER_MANAGEMENT_PASSWORD);

    private int failedAttemptsInterval = Integer.parseInt(System.getProperty(KieServerConstants.KIE_SERVER_ROUTER_ATTEMPT_INTERVAL, "10"));
    
    private KieContainerResourceFilter activeOnly = new KieContainerResourceFilter(ReleaseIdFilter.ACCEPT_ALL, KieContainerStatusFilter.parseFromNullableString("STARTED"));

    private ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

    private RouterConnectionObserver observer = new RouterConnectionObserver();

    private String CONTAINER_JSON =
            "\"containerId\" : \"{0}\","
            + "\"alias\" : \"{1}\","
            + "\"serverUrl\" : \"{2}\","
            + "\"serverId\" : \"{3}\","
            + "\"releaseId\" : \"{4}\"";


    public KieServerRouterEventListener() {
    }

    public KieServerRouterEventListener(String serverId, String serverURL, String routerURL, int failedAttemptsInterval) {
        this.serverId = serverId;
        this.serverURL = serverURL;
        this.routerURL = routerURL;
        this.failedAttemptsInterval = failedAttemptsInterval;
    }

    @Override
    public void beforeServerStarted(KieServer kieServer) {       
    }

    @Override
    public void afterServerStarted(KieServer kieServer) {        
    }

    @Override
    public void beforeServerStopped(KieServer kieServer) {   
        if (routerURL == null) {
            logger.debug("KieServer router url not given, skipping");
            return;
        }
        ServiceResponse<KieContainerResourceList> containers = kieServer.listContainers(activeOnly);
        containers.getResult().getContainers().forEach(ci -> {
            
            routers().forEach(url -> {
                String alias = getContainerAlias(ci);
                String containerIdPayload = "{" + MessageFormat.format(CONTAINER_JSON, ci.getContainerId(), alias, serverURL, serverId, ci.getReleaseId().toExternalForm()) + "}";
                boolean success = send(url + ROUTER_REMOVE_URL, ci.getContainerId(), containerIdPayload, false, false);
                if (success) {
                    logger.info("Removed '{}' as server location for container id '{}'", serverURL, ci.getContainerId());
                }
            });
        });
    }

    @Override
    public void afterServerStopped(KieServer kieServer) {
        close();
    }

    @Override
    public void beforeContainerStarted(KieServer kieServer, KieContainerInstance containerInstance) {        
    }

    @Override
    public void afterContainerStarted(KieServer kieServer, KieContainerInstance containerInstance) {
        if (routerURL == null) {
            logger.debug("KieServer router url not given, skipping");
            return;
        }
        
        routers().forEach(url -> {
            String alias = getContainerAlias(containerInstance.getResource());
            String containerIdPayload = "{" + MessageFormat.format(CONTAINER_JSON, containerInstance.getContainerId(), alias, serverURL, serverId, containerInstance.getResource().getReleaseId().toExternalForm()) + "}";
            boolean success = send(url + ROUTER_ADD_URL, containerInstance.getContainerId(), containerIdPayload, true, true);
            if (success) {
                logger.info("Added '{}' as server location for container id '{}'", serverURL, containerInstance.getContainerId());
            }
        });
    }

    @Override
    public void beforeContainerStopped(KieServer kieServer, KieContainerInstance containerInstance) {        
    }

    @Override
    public void afterContainerStopped(KieServer kieServer, KieContainerInstance containerInstance) {
        if (routerURL == null) {
            logger.debug("KieServer router url not given, skipping");
            return;
        }
        routers().forEach(url -> {
            String alias = getContainerAlias(containerInstance.getResource());
            String containerIdPayload = "{" + MessageFormat.format(CONTAINER_JSON, containerInstance.getContainerId(), alias, serverURL, serverId, containerInstance.getResource().getReleaseId().toExternalForm()) + "}";
            boolean success = send(url + ROUTER_REMOVE_URL, containerInstance.getContainerId(), containerIdPayload, false, true);
            if (success) {
                logger.info("Removed '{}' as server location for container id '{}'", serverURL, containerInstance.getContainerId());
            }
        });
        
    }
    
    @Override
    public void beforeContainerActivated(KieServer kieServer, KieContainerInstance containerInstance) {
    }

    @Override
    public void afterContainerActivated(KieServer kieServer, KieContainerInstance containerInstance) {
    }

    @Override
    public void beforeContainerDeactivated(KieServer kieServer, KieContainerInstance containerInstance) {
    }

    @Override
    public void afterContainerDeactivated(KieServer kieServer, KieContainerInstance containerInstance) {  
    }

    protected boolean send(String url, String containerId, String payload, boolean add, boolean retry) {

        try {
            KieServerHttpRequest httpRequest = null;
            // auth will be used if password is provided. user will be the server id
            if (password != null && !password.isEmpty()) {
                httpRequest = KieServerHttpRequest.newRequest(url, username, password);
            } else {
                httpRequest = KieServerHttpRequest.newRequest(url);
            }

            httpRequest.followRedirects(true)
                    .contentType("application/json")
                    .accept("application/json")
                    .timeout(5000)
                    .body(payload)
                    .post();
            KieServerHttpResponse response = httpRequest.response();
            int responseCode = response.code();
            logger.debug("Response for url {} is {}", httpRequest.getUrl(), responseCode);
            if (responseCode > 201) {
                throw new KieServerHttpRequestException("Connection error " + responseCode);
            }
            observer.onSuccess(url);

            return true;
        } catch (KieServerHttpRequestException ioe) {
            logger.debug("Send to router failed", ioe);
            if (retry) {
                executorService.schedule(() -> {
                            boolean success = send(url, containerId, payload, add, true);
                            if (success) {
                                if (add) {
                                    logger.info("Added '{}' as server location for container '{}'", serverURL, containerId);
                                } else {
                                    logger.info("Removed '{}' as server location for container '{}'", serverURL, containerId);
                                }
                            }
                        },
                        failedAttemptsInterval, TimeUnit.SECONDS);
                logger.warn("Failed at sending request to router at {} due to {}. Next attempt is scheduled to fire in {} seconds", url, findCause(ioe).getMessage(), failedAttemptsInterval);
            } else {
                logger.warn("Failed at sending request to router at {} due to {}.", url, findCause(ioe).getMessage());
            }
            observer.onFailure(url);
            return false;
        } catch (Exception e) {
            logger.warn("Failed at sending request to router at {} due to {}", url, findCause(e).getMessage());
            logger.debug("Send to router failed", e);
            return false;
        }
    }

    protected String getContainerAlias(KieContainerResource containerInstance) {
        String alias = containerInstance.getContainerAlias();
        
        if (alias == null || alias.isEmpty()) {
            alias = containerInstance.getReleaseId().getArtifactId();
        }
        
        return alias;
    }

    public String getUsername() {
        return username;
    }
    protected List<String> routers() {
        ArrayList<String> list = new ArrayList<>();
        String[] routerUrls = routerURL.split(",");

        for (String routerUrl : routerUrls) {
            routerUrl = routerUrl.trim();
            if (routerUrl.endsWith("/")) {
                routerUrl = routerUrl.substring(0, routerUrl.length()-1);
            }
            list.add(routerUrl);
        }
        return list;
    }
    
    protected Throwable findCause(Exception e) {
        Throwable found = e;
        
        while (found.getCause() != null) {
            found = found.getCause();
        }
        
        return found;
    }

    public RouterConnectionObserver getObserver() {
        return observer;
    }

    public void setObserver(RouterConnectionObserver observer) {
        this.observer = observer;
    }

    public static class RouterConnectionObserver {
        public void onSuccess(String url) {

        }

        public void onFailure(String url) {

        }
    }

    public void close() {
        logger.debug("About to shutdown internal executor service to handle failed attempts when connecting to kie server router...");
        this.executorService.shutdownNow();
        logger.debug("Internal executor service to handle failed attempts when connecting to kie server router stopped successfully");
    }
}
