/*
 * Copyright 2022 Red Hat, Inc. and/or its affiliates.
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

package org.kie.server.client;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.http.Fault;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.kie.server.api.model.KieServerInfo;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.client.balancer.LoadBalancer;
import org.kie.server.client.impl.AbstractKieServicesClientImpl;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class LoadBalancerClientAuthTest {

    private String mockServerBaseUri1;
    private String mockServerBaseUri2;
    private String loadBalanceUri;

    private WireMockServer wireMockServer1;
    private WireMockServer wireMockServer2;

    private static final String USER = "user";
    private static final String PASS = "pass";
    private static final String AUTH_VALUE = "Basic " + new String(Base64.getEncoder().encode((USER + ":" + PASS).getBytes(StandardCharsets.UTF_8)));

    protected WireMockServer createMockServer(String version, int port) {
        WireMockServer wireMockServer = new WireMockServer(port);
        wireMockServer.stubFor(get(urlEqualTo("/")).withHeader("Authorization", equalTo(AUTH_VALUE))
                                                   .atPriority(1)
                                                   .willReturn(aResponse().withStatus(200)
                                                                          .withBody("<response type=\"SUCCESS\" msg=\"Kie Server info\">\n" +
                                                                                    "  <kie-server-info>\n" +
                                                                                    "    <version>" + version + "</version>\n" +
                                                                                    "  </kie-server-info>\n" +
                                                                                    "</response>")));
        wireMockServer.stubFor(get(urlEqualTo("/")).atPriority(2)
                                                   .willReturn(aResponse().withStatus(401).withFault(Fault.EMPTY_RESPONSE))); // simulate an invalid response DROOLS-6837
        return wireMockServer;
    }

    @Before
    public void startServers() {
        int port1 = BaseKieServicesClientTest.findFreePort();
        wireMockServer1 = createMockServer("1", port1);
        wireMockServer1.start();

        int port2 = BaseKieServicesClientTest.findFreePort();
        wireMockServer2 = createMockServer("2", port2);
        wireMockServer2.start();

        mockServerBaseUri1 = "http://localhost:" + port1;
        mockServerBaseUri2 = "http://localhost:" + port2;
        loadBalanceUri = mockServerBaseUri1 + "|" + mockServerBaseUri2;
    }

    @After
    public void stopServers() {
        wireMockServer1.stop();
        wireMockServer2.stop();
    }

    @Test
    public void testDefaultLoadBalancer() {
        KieServicesConfiguration config = KieServicesFactory.newRestConfiguration(loadBalanceUri, USER, PASS);
        config.setCapabilities(Arrays.asList("KieServer"));

        KieServicesClient client = KieServicesFactory.newKieServicesClient(config);

        List<String> available = ((AbstractKieServicesClientImpl) client).getLoadBalancer().getAvailableEndpoints();
        assertNotNull(available);
        assertEquals(2, available.size());

        ServiceResponse<KieServerInfo> response = client.getServerInfo();
        assertSuccess(response);
        assertEquals("Server version", "1", response.getResult().getVersion());

        response = client.getServerInfo();
        assertSuccess(response);
        assertEquals("Server version", "2", response.getResult().getVersion());
    }

    private void assertSuccess(ServiceResponse<?> response) {
        assertEquals("Response type", ServiceResponse.ResponseType.SUCCESS, response.getType());
    }

    @Test
    public void testCheckFailedEndpointsDefaultConfig() throws Exception {
        KieServicesConfiguration config = KieServicesFactory.newRestConfiguration(loadBalanceUri, USER, PASS);
        config.setCapabilities(Arrays.asList("KieServer"));

        testCheckFailedEndpoints(config);
    }

    @Test
    public void testCheckFailedEndpointsCreateLoadBalancer() throws Exception {
        KieServicesConfiguration config = KieServicesFactory.newRestConfiguration(loadBalanceUri, USER, PASS);
        config.setCapabilities(Arrays.asList("KieServer"));

        LoadBalancer loadBalancer = LoadBalancer.getDefault(loadBalanceUri);

        assertNull(loadBalancer.getUserName());
        assertNull(loadBalancer.getPassword());

        loadBalancer.setUserName(USER);
        loadBalancer.setPassword(PASS);
        config.setLoadBalancer(loadBalancer);

        testCheckFailedEndpoints(config);
    }

    private void testCheckFailedEndpoints(KieServicesConfiguration config) throws Exception {
        // server 1 is down
        wireMockServer1.stop();

        KieServicesClient client = KieServicesFactory.newKieServicesClient(config);

        ServiceResponse<KieServerInfo> response = client.getServerInfo();
        assertSuccess(response);
        assertEquals("Server version", "2", response.getResult().getVersion());

        response = client.getServerInfo();
        assertSuccess(response);
        assertEquals("Server version", "2", response.getResult().getVersion());

        List<String> available = ((AbstractKieServicesClientImpl) client).getLoadBalancer().getAvailableEndpoints();
        assertNotNull(available);
        assertEquals(1, available.size());

        // now let's put back online server 1
        wireMockServer1.start();

        Future waitForResult = ((AbstractKieServicesClientImpl) client).getLoadBalancer().checkFailedEndpoints();
        waitForResult.get(5, TimeUnit.SECONDS);

        available = ((AbstractKieServicesClientImpl) client).getLoadBalancer().getAvailableEndpoints();
        assertNotNull(available);
        assertEquals(2, available.size());

        response = client.getServerInfo();
        assertSuccess(response);
        assertEquals("Server version", "2", response.getResult().getVersion());

        // this is the most important as it was offline before (server 1)
        response = client.getServerInfo();
        assertSuccess(response);
        assertEquals("Server version", "1", response.getResult().getVersion());
    }
}
