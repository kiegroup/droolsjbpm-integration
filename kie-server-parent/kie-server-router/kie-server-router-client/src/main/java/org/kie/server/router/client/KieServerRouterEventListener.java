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

import java.util.Arrays;
import java.util.List;

import org.kie.server.api.KieServerConstants;
import org.kie.server.api.model.KieContainerResource;
import org.kie.server.api.model.KieContainerResourceFilter;
import org.kie.server.api.model.KieContainerResourceList;
import org.kie.server.api.model.KieContainerStatusFilter;
import org.kie.server.api.model.ReleaseIdFilter;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.common.rest.KieServerHttpRequest;
import org.kie.server.common.rest.KieServerHttpResponse;
import org.kie.server.services.api.KieContainerInstance;
import org.kie.server.services.api.KieServer;
import org.kie.server.services.api.KieServerEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KieServerRouterEventListener implements KieServerEventListener {
    
    private static final Logger logger = LoggerFactory.getLogger(KieServerRouterEventListener.class);
    
    private String serverId = System.getProperty(KieServerConstants.KIE_SERVER_ID);
    private String serverURL = System.getProperty(KieServerConstants.KIE_SERVER_LOCATION);
    private String routerURL = System.getProperty(KieServerConstants.KIE_SERVER_ROUTER);
    
    private KieContainerResourceFilter activeOnly = new KieContainerResourceFilter(ReleaseIdFilter.ACCEPT_ALL, KieContainerStatusFilter.parseFromNullableString("STARTED"));

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
                boolean success = send(url + "/admin/remove", ci.getContainerId());
                if (success) {
                    logger.info("Removed '{}' as server location for container id '{}'", serverURL, ci.getContainerId());
                }
                String alias = getContainerAlias(ci);
                success = send(url + "/admin/remove", alias);
                if (success) {
                    logger.info("Removed '{}' as server location for container alias '{}'", serverURL, alias);
                }
            });
        });
    }

    @Override
    public void afterServerStopped(KieServer kieServer) {        
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
            boolean success = send(url + "/admin/add", containerInstance.getContainerId());
            if (success) {
                logger.info("Added '{}' as server location for container id '{}'", serverURL, containerInstance.getContainerId());
            }
            String alias = getContainerAlias(containerInstance.getResource());
            success = send(url + "/admin/add", alias);
            if (success) {
                logger.info("Added '{}' as server location for container alias '{}'", serverURL, alias);
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
            boolean success = send(url + "/admin/remove", containerInstance.getContainerId());
            if (success) {
                logger.info("Removed '{}' as server location for container id '{}'", serverURL, containerInstance.getContainerId());
            }
            String alias = getContainerAlias(containerInstance.getResource());
            success = send(url + "/admin/remove", alias);
            if (success) {
                logger.info("Removed '{}' as server location for container alias '{}'", serverURL, alias);
            }
        });
        
    }
    
    protected boolean send(String url, String containerId) {
        String jsonBody = "{"
                + "\"containerId\" : \""+ containerId + "\","
                + "\"serverUrl\" : \""+ serverURL + "\","
                + "\"serverId\" : \""+ serverId + "\","
                + "}";
        try {
            KieServerHttpRequest httpRequest = KieServerHttpRequest.newRequest(url)
                    .followRedirects(true)
                    .timeout(5000)
                    .body(jsonBody)
                    .post();
            KieServerHttpResponse response = httpRequest.response();
            logger.debug("Response for url {} is {}", httpRequest.getUrl(), response.code());
            
            return true;
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
    
    protected List<String> routers() {
        return Arrays.asList(routerURL.split(","));
    }
    
    protected Throwable findCause(Exception e) {
        Throwable found = e;
        
        while (found.getCause() != null) {
            found = found.getCause();
        }
        
        return found;
    }
}
