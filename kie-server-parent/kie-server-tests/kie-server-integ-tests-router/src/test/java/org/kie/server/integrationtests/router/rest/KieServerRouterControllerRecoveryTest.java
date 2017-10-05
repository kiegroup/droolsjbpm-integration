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

package org.kie.server.integrationtests.router.rest;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.delete;
import static com.github.tomakehurst.wiremock.client.WireMock.deleteRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.putRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.kie.server.integrationtests.config.TestConfig;
import org.kie.server.integrationtests.shared.filter.Authenticator;
import org.kie.server.router.KieServerRouter;
import org.kie.server.router.KieServerRouterConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.RequestListener;

public class KieServerRouterControllerRecoveryTest {

    private static final Logger logger = LoggerFactory.getLogger(KieServerRouterControllerRecoveryTest.class);

    private static KieServerRouter router;
    private static File repository;

    private static String serverUrl;
    
    private WireMockServer wireMockServer;
    private int mockKieServerPort;
    
    private String CONTAINER_JSON =
            "{\"containerId\" : \"test\","
            + "\"alias\" : \"test-alias\","
            + "\"serverUrl\" : \"http://localhost\","
            + "\"serverId\" : \"test-server\","
            + "\"releaseId\" : \"org.kie.server:test:1.0\"}";

    @Before
    public void startStandaloneRouter(){
        mockKieServerPort = allocatePort();
        System.setProperty(KieServerRouterConstants.KIE_SERVER_CONTROLLER_ATTEMPT_INTERVAL, "1");
        System.setProperty(KieServerRouterConstants.CONTROLLER, "http://localhost:" + mockKieServerPort);
        
        configureMockServer();
                
        // setup repository for config of router
        repository = new File("target/controller-router-repo");
        repository.mkdirs();
        System.setProperty(KieServerRouterConstants.ROUTER_REPOSITORY_DIR, repository.getAbsolutePath());
     

        // setup and start router
        Integer port = allocatePort();
        router = new KieServerRouter();
        router.start("localhost", port);

        serverUrl = "http://localhost:" + port;
    }

    @After
    public void stopStandaloneRouter(){
        // stop router and remove its config
        System.clearProperty(KieServerRouterConstants.ROUTER_REPOSITORY_DIR);
        System.clearProperty(KieServerRouterConstants.KIE_SERVER_CONTROLLER_ATTEMPT_INTERVAL);
        System.clearProperty(KieServerRouterConstants.CONTROLLER);
        router.stop(true);
        router = null;

        Stream.of(repository.listFiles()).forEach(f-> f.delete());
        repository.delete();
        if (wireMockServer != null && wireMockServer.isRunning()) {
            wireMockServer.stop();
        }
    }

    @Test
    public void testRecoveryOfCreatedContainerOnController() throws Exception {

        Response response = null;
        try {
            WebTarget clientRequest = newRequest(serverUrl + "/mgmt/add");
            logger.debug("[POST] " + clientRequest.getUri());

            response = clientRequest.request(MediaType.APPLICATION_JSON_TYPE).put(Entity.json(CONTAINER_JSON));
            Assert.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
            response.close();
            
            CountDownLatch latch = new CountDownLatch(1);
            CountDownLatch deleteLatch = new CountDownLatch(1);
            wireMockServer.addMockServiceRequestListener(new RequestListener() {                
                @Override
                public void requestReceived(Request request, com.github.tomakehurst.wiremock.http.Response response) {
                    if (request.getUrl().equals("/management/servers/kie-server-router/containers/test") && request.getMethod().name().equalsIgnoreCase("PUT")) {
                        latch.countDown();
                    }
                    if (request.getUrl().equals("/management/servers/kie-server-router/containers/test") && request.getMethod().name().equalsIgnoreCase("DELETE")) {
                        deleteLatch.countDown();
                    } 
                }
            });
            wireMockServer.start();
            
            latch.await(5, TimeUnit.SECONDS);
            wireMockServer.verify(1, putRequestedFor(urlEqualTo("/management/servers/kie-server-router/containers/test")));
            
            // let's not stop and simulate delete of container
            wireMockServer.stop();
            
            clientRequest = newRequest(serverUrl + "/mgmt/remove");
            logger.debug("[POST] " + clientRequest.getUri());

            response = clientRequest.request(MediaType.APPLICATION_JSON_TYPE).put(Entity.json(CONTAINER_JSON));
            Assert.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
            response.close();
            
            wireMockServer.start();
            deleteLatch.await(5, TimeUnit.SECONDS);
            wireMockServer.verify(1, deleteRequestedFor(urlEqualTo("/management/servers/kie-server-router/containers/test")));
            
        } finally {
            if(response != null) {
                response.close();
            }
        }
    }


    private static int allocatePort() {
        try {
            ServerSocket server = new ServerSocket(0);
            int port = server.getLocalPort();
            server.close();
            return port;
        } catch (IOException e) {
            // failed to dynamically allocate port, try to use hard coded one
            return 9783;
        }
    }
    private static Client httpClient;

    protected WebTarget newRequest(String uriString) {

        if(httpClient == null) {
            httpClient = new ResteasyClientBuilder()
                    .establishConnectionTimeout(10, TimeUnit.SECONDS)
                    .socketTimeout(10, TimeUnit.SECONDS)
                    .register(new Authenticator(TestConfig.getUsername(), TestConfig.getPassword()))
                    .build();
        }
        return httpClient.target(uriString);
    }
    
    private void configureMockServer() {
        wireMockServer = new WireMockServer(mockKieServerPort);
        wireMockServer.stubFor(put(urlEqualTo("/server/kie-server-router"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody("{}")));
        wireMockServer.stubFor(put(urlEqualTo("/management/servers/kie-server-router/containers/test"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody("")));
        wireMockServer.stubFor(delete(urlEqualTo("/management/servers/kie-server-router/containers/test"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody("")));     
        wireMockServer.stubFor(delete(urlPathEqualTo("/server/kie-server-router"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody(""))); 
    }
}
