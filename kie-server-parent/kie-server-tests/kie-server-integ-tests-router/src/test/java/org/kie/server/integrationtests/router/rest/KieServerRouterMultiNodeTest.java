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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.ServerSocket;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.kie.server.api.marshalling.MarshallingFormat;
import org.kie.server.api.model.instance.ProcessInstance;
import org.kie.server.client.KieServicesClient;
import org.kie.server.client.KieServicesConfiguration;
import org.kie.server.client.KieServicesFactory;
import org.kie.server.client.QueryServicesClient;
import org.kie.server.integrationtests.router.client.KieServerRouterClient;
import org.kie.server.router.Configuration;
import org.kie.server.router.KieServerRouter;
import org.kie.server.router.KieServerRouterConstants;

import static org.junit.Assert.assertNotNull;

public class KieServerRouterMultiNodeTest {

    private KieServerRouter router;
    private File repository;

    private static String serverUrl;

    private List<WireMockServer> servers;

    @Before
    public void startStandaloneRouter() throws Exception {
        // setup repository for config of router
        repository = new File("target/standalone-router-repo");
        repository.mkdirs();
        System.setProperty(KieServerRouterConstants.ROUTER_REPOSITORY_DIR, repository.getAbsolutePath());

        URI uriPath = Thread.currentThread().getContextClassLoader().getResource("process-instance.xml").toURI();
        String fileContent = new String(Files.readAllBytes(Paths.get(uriPath)));
        servers = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            Integer port = allocatePort();
            WireMockServer server = new WireMockServer(port);
            server.stubFor(WireMock.get(WireMock.urlEqualTo("/queries/processes/instances/1"))
                                   .willReturn(WireMock.aResponse().withHeader("Content-Type", "application/xml; aggregatable = false").withBody(fileContent)));

            server.start();
            servers.add(server);
        }

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
            connection.setRequestProperty("Content-Type", "application/xstream");
            connection.setRequestProperty("X-KIE-ContentType", "application/xstream");
            connection.setDoOutput(true);
            connection.setDoInput(true);
            try (OutputStream stream = connection.getOutputStream()) {
                stream.write(value.getBytes());
            }
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuffer content = new StringBuffer();
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
            in.close();
            System.out.println(connection.getResponseCode() + " " + content);
            connection.disconnect();
        }
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
        try (KieServerRouterClient routerClient = new KieServerRouterClient(serverUrl)) {
            Configuration configuration = routerClient.getRouterConfig();
            assertNotNull(configuration);
        }
        KieServicesConfiguration conf = KieServicesFactory.newRestConfiguration(serverUrl, null);
        conf.setMarshallingFormat(MarshallingFormat.XSTREAM);
        KieServicesClient client = KieServicesFactory.newKieServicesClient(conf);

        QueryServicesClient queries = client.getServicesClient(QueryServicesClient.class);
        ProcessInstance pi = queries.findProcessInstanceById(1L);
        assertNotNull(pi);
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