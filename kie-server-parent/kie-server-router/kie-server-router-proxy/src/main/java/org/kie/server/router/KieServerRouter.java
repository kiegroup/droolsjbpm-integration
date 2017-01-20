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

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Base64;
import java.util.Date;

import io.undertow.util.Headers;
import org.jboss.logging.Logger;
import org.kie.server.router.handlers.AdminHttpHandler;
import org.kie.server.router.handlers.ContainersHttpHandler;
import org.kie.server.router.handlers.DocumentsHttpHandler;
import org.kie.server.router.handlers.JobsHttpHandler;
import org.kie.server.router.handlers.KieServerInfoHandler;
import org.kie.server.router.handlers.QueriesDataHttpHandler;
import org.kie.server.router.handlers.QueriesHttpHandler;
import org.kie.server.router.proxy.KieServerProxyClient;

import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.PathHandler;
import io.undertow.server.handlers.ResponseCodeHandler;
import io.undertow.server.handlers.proxy.ProxyHandler;
import org.kie.server.router.repository.FileRepository;

public class KieServerRouter {
    private static final String HOST = System.getProperty(KieServerRouterConstants.ROUTER_HOST, "localhost");
    private static final String PORT = System.getProperty(KieServerRouterConstants.ROUTER_PORT, "9000");

    private static final String CONTROLLER = System.getProperty(KieServerRouterConstants.CONTROLLER);
    private static final String USER_NAME = System.getProperty(KieServerRouterConstants.KIE_CONTROLLER_USER, "kieserver");
    private static final String PASSWORD = System.getProperty(KieServerRouterConstants.KIE_CONTROLLER_PASSWORD, "kieserver1!");
    private static final String TOKEN = System.getProperty(KieServerRouterConstants.KIE_CONTROLLER_TOKEN);
    
    private static final Logger log = Logger.getLogger(KieServerRouter.class);

    private static final String JSON_RESPONSE = "{\n"+
            "      \"version\" : \"LATEST\",\n"+
            "      \"name\" : \"KIE Server Router\",\n"+
            "      \"location\" : \"" + KieServerInfoHandler.getLocationUrl() + "\",\n"+
            "      \"capabilities\" : [ \"KieServer\", \"BRM\", \"BPM\", \"CaseMgmt\", \"BPM-UI\", \"BRP\" ],\n"+
            "      \"id\" : \"kie-server-router\"\n"+
            "}";
    
    private Undertow server;
    private FileRepository repository = new FileRepository();

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
        final KieServerProxyClient proxyClient = new KieServerProxyClient();
        
        HttpHandler notFoundHandler = ResponseCodeHandler.HANDLE_404;        
        AdminHttpHandler adminHandler = new AdminHttpHandler(proxyClient, repository);

        PathHandler pathHandler = Handlers.path(new ProxyHandler(proxyClient, notFoundHandler));
        pathHandler.addPrefixPath("/queries/definitions", new QueriesDataHttpHandler(notFoundHandler, adminHandler));
        pathHandler.addPrefixPath("/queries", new QueriesHttpHandler(notFoundHandler, adminHandler));
        pathHandler.addPrefixPath("/jobs", new JobsHttpHandler(notFoundHandler, adminHandler));
        pathHandler.addPrefixPath("/documents", new DocumentsHttpHandler(notFoundHandler, adminHandler));
        pathHandler.addExactPath("/containers", new ContainersHttpHandler(notFoundHandler, adminHandler));
        pathHandler.addPrefixPath("/admin", adminHandler);
        pathHandler.addExactPath("/", new KieServerInfoHandler());
   
        // main server configuration
        server = Undertow.builder()
                .addHttpListener(port, host)                
                .setHandler(pathHandler)
                .build();
        server.start();
        log.infof("KieServerRouter started on %s:%s at %s", host, port, new Date());
        connectToController();

    }

    public void stop() {
        stop(false);
    }
    
    public void stop(boolean clean) {
        disconnectToController();
        if (server != null) {
            server.stop();
            if (clean) {
                repository.clean();
            }
            log.infof("KieServerRouter stopped on %s:%s at %s", System.getProperty(KieServerRouterConstants.ROUTER_HOST), System.getProperty(KieServerRouterConstants.ROUTER_PORT), new Date());
        } else {
            log.error("KieServerRouter was not started");
        }
    }

    protected void connectToController() {
        if (CONTROLLER == null) {
            return;
        }

        try {
            URL controllerURL = new URL(CONTROLLER + "/server/kie-server-router");
            HttpURLConnection con = (HttpURLConnection) controllerURL.openConnection();
            con.setRequestMethod("PUT");

            con.setRequestProperty(Headers.ACCEPT_STRING, "application/json");
            con.setRequestProperty(Headers.CONTENT_TYPE_STRING, "application/json");
            con.setRequestProperty(Headers.AUTHORIZATION_STRING, getAuthorization());

            con.setDoOutput(true);
            con.getOutputStream().write(JSON_RESPONSE.getBytes("UTF-8"));

            log.debugf("Sending 'POST' request to URL : %s", controllerURL);
            int responseCode = con.getResponseCode();
            log.debugf("Response Code : %s", responseCode);

            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);

            }
            in.close();

            response.toString();
            log.infof("KieServerRouter connected to controller at " + CONTROLLER);
        } catch (Exception e) {
            log.error("Error when connecting to controller at " + CONTROLLER, e);
        }
    }

    protected void disconnectToController() {
        if (CONTROLLER == null) {
            return;
        }

        try {
            URL controllerURL = new URL(CONTROLLER + "/server/kie-server-router/?location="+ URLEncoder.encode(KieServerInfoHandler.getLocationUrl(), "UTF-8"));
            HttpURLConnection con = (HttpURLConnection) controllerURL.openConnection();
            con.setRequestMethod("DELETE");

            con.setRequestProperty(Headers.ACCEPT_STRING, "application/json");
            con.setRequestProperty(Headers.CONTENT_TYPE_STRING, "application/json");
            con.setRequestProperty(Headers.AUTHORIZATION_STRING, getAuthorization());

            con.setDoOutput(true);

            log.debugf("Sending 'POST' request to URL : %s", controllerURL);
            int responseCode = con.getResponseCode();
            log.debugf("Response Code : %s", responseCode);
            log.infof("KieServerRouter disconnected from controller at " + CONTROLLER);
        } catch (Exception e) {
            log.error("Error when disconnecting from controller at " + CONTROLLER, e);
        }
    }

    protected String getAuthorization() throws Exception{
        if (TOKEN != null) {
            return "Bearer " + TOKEN;
        } else {
            return "Basic " + Base64.getEncoder().encodeToString((USER_NAME + ":" + PASSWORD).getBytes("UTF-8"));
        }
    }
}
