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

package org.kie.server.router;

import java.net.URI;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.jboss.logging.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.kie.server.router.handlers.AdminHttpHandler;
import org.kie.server.router.handlers.ContainersHttpHandler;
import org.kie.server.router.handlers.DocumentsHttpHandler;
import org.kie.server.router.handlers.JobsHttpHandler;
import org.kie.server.router.handlers.KieServerInfoHandler;
import org.kie.server.router.handlers.QueriesDataHttpHandler;
import org.kie.server.router.handlers.QueriesHttpHandler;
import org.kie.server.router.proxy.KieServerProxyClient;
import org.kie.server.router.repository.FileRepository;
import org.kie.server.router.spi.ConfigRepository;
import org.kie.server.router.utils.HttpUtils;

import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.BlockingHandler;
import io.undertow.server.handlers.PathHandler;
import io.undertow.server.handlers.ResponseCodeHandler;
import io.undertow.server.handlers.proxy.ProxyHandler;

public class KieServerRouter {
    private static final String HOST = System.getProperty(KieServerRouterConstants.ROUTER_HOST, "localhost");
    private static final String PORT = System.getProperty(KieServerRouterConstants.ROUTER_PORT, "9000");
    private int failedAttemptsInterval = Integer.parseInt(System.getProperty(KieServerRouterConstants.KIE_SERVER_CONTROLLER_ATTEMPT_INTERVAL, "10"));

    private static final String CONTROLLER = System.getProperty(KieServerRouterConstants.CONTROLLER);
    
    private static final Logger log = Logger.getLogger(KieServerRouter.class);

    private static final String SERVER_INFO_JSON = "{\n"+
            "      \"version\" : \"LATEST\",\n"+
            "      \"name\" : \"" + KieServerInfoHandler.getRouterName() + "\",\n"+
            "      \"location\" : \"" + KieServerInfoHandler.getLocationUrl() + "\",\n"+
            "      \"capabilities\" : [ \"KieServer\", \"BRM\", \"BPM\", \"CaseMgmt\", \"BPM-UI\", \"BRP\" ],\n"+
            "      \"id\" : \"" + KieServerInfoHandler.getRouterId() + "\"\n"+
            "}";

    private ServiceLoader<ConfigRepository> configRepositoryServiceLoader = ServiceLoader.load(ConfigRepository.class);
    
    private Undertow server;
    private ConfigRepository repository = new FileRepository();
    
    private ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture<?> controllerConnectionAttempts;

    public KieServerRouter() {
        configRepositoryServiceLoader.forEach( repo -> repository = repo);
        log.info("KIE Server router repository implementation is " + repository);
    }

    public static void main(String[] args) {
        KieServerRouter router = new KieServerRouter();        
        router.start(HOST, Integer.parseInt(PORT));

        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                router.stop();
            }
        });
    }
    
    public void start(String host, Integer port) {
        System.setProperty(KieServerRouterConstants.ROUTER_HOST, host);
        System.setProperty(KieServerRouterConstants.ROUTER_PORT, port.toString());

        Configuration configuration = repository.load();
        
        AdminHttpHandler adminHandler = new AdminHttpHandler(configuration, repository);
        final KieServerProxyClient proxyClient = new KieServerProxyClient(configuration, adminHandler);
        Map<String, List<String>> perContainer = configuration.getHostsPerContainer();

        for (Map.Entry<String, List<String>> entry : perContainer.entrySet()) {
            Set<String> uniqueUrls = new LinkedHashSet<>(entry.getValue());
            uniqueUrls.forEach(url -> {
                proxyClient.addContainer(entry.getKey(), URI.create(url));
            });
        }
        
        HttpHandler notFoundHandler = ResponseCodeHandler.HANDLE_404;        


        ProxyHandler proxyHandler = new ProxyHandler(proxyClient, notFoundHandler);
        PathHandler pathHandler = Handlers.path(proxyHandler);
        pathHandler.addPrefixPath("/queries/definitions", new QueriesDataHttpHandler(notFoundHandler, adminHandler));
        pathHandler.addPrefixPath("/queries", new QueriesHttpHandler(notFoundHandler, adminHandler));
        pathHandler.addPrefixPath("/jobs", new JobsHttpHandler(proxyHandler, adminHandler));
        pathHandler.addPrefixPath("/documents", new DocumentsHttpHandler(notFoundHandler, adminHandler));
        pathHandler.addExactPath("/containers", new ContainersHttpHandler(notFoundHandler, adminHandler));        
        pathHandler.addPrefixPath("/mgmt", adminHandler);
        pathHandler.addExactPath("/", new KieServerInfoHandler());

        HttpHandler blockingHandler = new BlockingHandler(pathHandler);

        // main server configuration
        server = Undertow.builder()
                .addHttpListener(port, host)                
                .setHandler(blockingHandler)
                .build();
        server.start();
        log.infof("KieServerRouter started on %s:%s at %s", host, port, new Date());
        connectToController(adminHandler);

    }

    public void stop() {
        stop(false);
    }
    
    public void stop(boolean clean) {
        executorService.shutdownNow();
        disconnectToController();
        if (server != null) {
            server.stop();
            repository.close();
            if (clean) {
                repository.clean();
            }
            log.infof("KieServerRouter stopped on %s:%s at %s", System.getProperty(KieServerRouterConstants.ROUTER_HOST), System.getProperty(KieServerRouterConstants.ROUTER_PORT), new Date());
        } else {
            log.error("KieServerRouter was not started");
        }
    }

    protected void connectToController(AdminHttpHandler adminHandler) {
        if (CONTROLLER == null) {
            return;
        }
        try {
            String jsonResponse = HttpUtils.putHttpCall(CONTROLLER + "/server/" + KieServerInfoHandler.getRouterId(), SERVER_INFO_JSON);
            log.debugf("Controller response :: ", jsonResponse);
            boostrapFromControllerResponse(jsonResponse, adminHandler);

            log.infof("KieServerRouter connected to controller at " + CONTROLLER);
        } catch (Exception e) {
            log.error("Error when connecting to controller at " + CONTROLLER + " due to " + e.getMessage());
            log.debug(e);
            
            controllerConnectionAttempts = executorService.scheduleAtFixedRate(() -> {
                
                try {
                    String jsonResponse = HttpUtils.putHttpCall(CONTROLLER + "/server/" + KieServerInfoHandler.getRouterId(), SERVER_INFO_JSON);
                    log.debugf("Controller response :: ", jsonResponse);
                    boostrapFromControllerResponse(jsonResponse, adminHandler);
                    
                    controllerConnectionAttempts.cancel(false);
                    log.infof("KieServerRouter connected to controller at " + CONTROLLER);
                } catch (Exception ex) {
                    log.error("Error when connecting to controller at " + CONTROLLER + 
                            " next attempt in " + failedAttemptsInterval + " " + TimeUnit.SECONDS.toString());
                    log.debug(ex);
                }
            },
            failedAttemptsInterval, failedAttemptsInterval, TimeUnit.SECONDS);
        }
    }

    protected void disconnectToController() {
        if (CONTROLLER == null) {
            return;
        }
        try {
            HttpUtils.deleteHttpCall(CONTROLLER + "/server/" + KieServerInfoHandler.getRouterId() + "/?location="+ URLEncoder.encode(KieServerInfoHandler.getLocationUrl(), "UTF-8"));
            log.infof("KieServerRouter disconnected from controller at " + CONTROLLER);
        } catch (Exception e) {
            log.error("Error when disconnecting from controller at " + CONTROLLER, e);
        }
    }
    
    protected void boostrapFromControllerResponse(String jsonResponse, AdminHttpHandler adminHandler) throws JSONException {
        List<String> containers = new ArrayList<>();

        JSONObject serverConfig = new JSONObject(jsonResponse);
        try {
            JSONArray sourceList = serverConfig.getJSONArray("containers");

            for (int i = 0; i < sourceList.length(); i++) {
                JSONObject container = sourceList.getJSONObject(i);
                containers.add(container.getString("container-id"));
            }
        } catch (JSONException e) {
            // if the server template did not exist the containers can be null, meaning not JSONArray
            log.debug("Error when getting list of containers:: " + e.getMessage(), e);
        }

        adminHandler.addControllerContainers(containers);
    }

}
