/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
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

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.ServerSocket;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.MappingBuilder;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.junit.WireMockRule;

import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.kie.server.api.marshalling.MarshallingFormat;
import org.kie.server.api.model.definition.ProcessDefinition;
import org.kie.server.api.model.instance.ProcessInstance;
import org.kie.server.client.KieServicesClient;
import org.kie.server.client.KieServicesConfiguration;
import org.kie.server.client.KieServicesFactory;
import org.kie.server.client.QueryServicesClient;
import org.kie.server.integrationtests.router.client.KieServerRouterClient;
import org.kie.server.router.Configuration;
import org.kie.server.router.KieServerRouter;
import org.kie.server.router.KieServerRouterConstants;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;


public class KieServerRouterMultiNodeTest {

    private static final String CONTENT_TYPE = "Content-Type";
    private static final String MEDIA_TYPE_AGGREGATABLE_FALSE = "application/xml; aggregatable = false";
    private static final String MEDIA_TYPE_AGGREGATABLE_TRUE = "application/xml; aggregatable = true";
    private static final String MEDIA_TYPE_NO_PARAM = "application/xml";
    private static final String PROCESSES_DEFINITIONS_PATH = "/queries/processes/definitions?sort=&sortOrder=true&page=0&pageSize=10";
    
    @Rule
    public WireMockRule server1 = new WireMockRule(allocatePort());
    
    @Rule
    public WireMockRule server2 = new WireMockRule(allocatePort());
    
    private KieServerRouter router;
    private File repository;

    private static String serverUrl;

    private List<WireMockServer> servers;
    
    private String bodyProcessInstance;
    private String bodyProcessDefinition1;
    private String bodyProcessDefinition2;
    
    @Before
    public void startStandaloneRouter() throws Exception {
        // setup repository for config of router
        repository = new File("target/standalone-router-repo");
        repository.mkdirs();
        System.setProperty(KieServerRouterConstants.ROUTER_REPOSITORY_DIR, repository.getAbsolutePath());

        bodyProcessInstance = getFileContent("process-instance.xml");
        bodyProcessDefinition1 = getFileContent("process-definition-1.xml");
        bodyProcessDefinition2 = getFileContent("process-definition-2.xml");
         
        servers = new ArrayList<>();
        
        servers.add(server1);
        servers.add(server2);

        // setup and start router
        Integer port = allocatePort();
        router = new KieServerRouter(false, "mock");
        router.start("localhost", port);

        serverUrl = "http://localhost:" + port;

        for (WireMockServer server : servers) {
            JSONObject jsonData = new JSONObject();

            jsonData.put("containerId", "some-container");
            jsonData.put("alias", "some-alias");
            jsonData.put("serverId", "some-id-" + server.port());
            jsonData.put("serverUrl", "http://localhost:" + server.port());
            jsonData.put("releaseId", "1.0.0.Final");
            String value = jsonData.toString();
            HttpURLConnection connection = (HttpURLConnection) URI.create(serverUrl + "/mgmt/add").toURL().openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty(CONTENT_TYPE, "application/xstream");
            connection.setRequestProperty("X-KIE-ContentType", "application/xstream");
            connection.setDoOutput(true);
            try (OutputStream stream = connection.getOutputStream()) {
                stream.write(value.getBytes());
            }
            connection.getResponseCode();
            connection.disconnect();
        }
    }

    private String getFileContent(String filename) throws URISyntaxException, IOException {
        URI uriPath = Thread.currentThread().getContextClassLoader().getResource(filename).toURI();
        return new String(Files.readAllBytes(Paths.get(uriPath)));
    }

    @After
    public void stopStandaloneRouter() {
        // stop router and remove its config
        System.clearProperty(KieServerRouterConstants.ROUTER_REPOSITORY_DIR);
        router.stop(true);
        router = null;

        servers.forEach(e -> e.stop());
        servers.clear();
        Stream.of(repository.listFiles()).forEach(f -> f.delete());
        repository.delete();
    }

    @Test
    public void testNonAggregatableMultipleResponseRouter() throws Exception {
        MappingBuilder arrangedResponse = 
                 get(urlEqualTo("/queries/processes/instances/1"))
                .willReturn(aResponse()
                .withHeader(CONTENT_TYPE, MEDIA_TYPE_AGGREGATABLE_FALSE)
                .withBody(bodyProcessInstance));
        
        server1.stubFor(arrangedResponse);
        server2.stubFor(arrangedResponse);
        
        QueryServicesClient queries = initRouterClient();
        ProcessInstance pi = queries.findProcessInstanceById(1L);
        assertNotNull(pi);
    }
    
    @Test
    public void testAggregatableMultipleResponseRouter() throws Exception {
        ResponseDefinitionBuilder resp1 = aResponse()
                .withHeader(CONTENT_TYPE, MEDIA_TYPE_AGGREGATABLE_TRUE)
                .withBody(bodyProcessDefinition1);
        
        ResponseDefinitionBuilder resp2 = aResponse()
                .withHeader(CONTENT_TYPE, MEDIA_TYPE_AGGREGATABLE_TRUE)
                .withBody(bodyProcessDefinition2);
        
        server1.stubFor(get(urlEqualTo(PROCESSES_DEFINITIONS_PATH)).willReturn(resp1));
        server2.stubFor(get(urlEqualTo(PROCESSES_DEFINITIONS_PATH)).willReturn(resp2));
        
        testAggregates();
    }

    @Test
    public void testFalseAggregatableMultipleResponseRouter() throws Exception {
        ResponseDefinitionBuilder resp1 = aResponse()
                .withHeader(CONTENT_TYPE, MEDIA_TYPE_AGGREGATABLE_FALSE)
                .withBody(bodyProcessDefinition1);
        
        ResponseDefinitionBuilder resp2 = aResponse()
                .withHeader(CONTENT_TYPE, MEDIA_TYPE_AGGREGATABLE_FALSE)
                .withBody(bodyProcessDefinition2);
        
        server1.stubFor(get(urlEqualTo(PROCESSES_DEFINITIONS_PATH)).willReturn(resp1));
        server2.stubFor(get(urlEqualTo(PROCESSES_DEFINITIONS_PATH)).willReturn(resp2));
        
        QueryServicesClient queries = initRouterClient();
        List<ProcessDefinition> lpi = queries.findProcesses(0, 10);
        assertNotNull(lpi);
        assertThat(lpi).hasSize(1);
    }

    @Test
    public void testNoAggregatableMultipleResponseRouter() throws Exception {
        ResponseDefinitionBuilder resp1 = aResponse()
                .withHeader(CONTENT_TYPE, MEDIA_TYPE_NO_PARAM)
                .withBody(bodyProcessDefinition1);
        
        ResponseDefinitionBuilder resp2 = aResponse()
                .withHeader(CONTENT_TYPE, MEDIA_TYPE_NO_PARAM)
                .withBody(bodyProcessDefinition2);
        
        server1.stubFor(get(urlEqualTo(PROCESSES_DEFINITIONS_PATH)).willReturn(resp1));
        server2.stubFor(get(urlEqualTo(PROCESSES_DEFINITIONS_PATH)).willReturn(resp2));
        
        testAggregates();
    }
    
    @Test
    public void testNoMediaTypeMultipleResponseRouter() throws Exception {
        ResponseDefinitionBuilder resp1 = aResponse()
                .withBody(bodyProcessDefinition1);
        
        ResponseDefinitionBuilder resp2 = aResponse()
                .withBody(bodyProcessDefinition2);
        
        server1.stubFor(get(urlEqualTo(PROCESSES_DEFINITIONS_PATH)).willReturn(resp1));
        server2.stubFor(get(urlEqualTo(PROCESSES_DEFINITIONS_PATH)).willReturn(resp2));
        
        testAggregates();
    }
    
    private void testAggregates() {
        QueryServicesClient queries = initRouterClient();
        List<ProcessDefinition> lpi = queries.findProcesses(0, 10);
        assertNotNull(lpi);
        assertThat(lpi).hasSize(2);
        List<String> names = lpi.stream().map(ni -> ni.getName()).collect(Collectors.toList());
        assertThat(names).contains("Evaluation updated", "Other evaluation process");
    }
    
    private QueryServicesClient initRouterClient() {
        try (KieServerRouterClient routerClient = new KieServerRouterClient(serverUrl)) {
            Configuration configuration = routerClient.getRouterConfig();
            assertNotNull(configuration);
        }
        KieServicesConfiguration conf = KieServicesFactory.newRestConfiguration(serverUrl, null);
        conf.setMarshallingFormat(MarshallingFormat.XSTREAM);
        KieServicesClient client = KieServicesFactory.newKieServicesClient(conf);

        return client.getServicesClient(QueryServicesClient.class);
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
}