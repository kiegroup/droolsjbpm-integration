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

package org.kie.server.client;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.kie.server.common.rest.KieServerHttpRequestException;
import org.kie.server.api.model.KieServerInfo;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.client.balancer.BalancerStrategy;
import org.kie.server.client.balancer.LoadBalancer;
import org.kie.server.client.impl.AbstractKieServicesClientImpl;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.Assert.*;

public class LoadBalancerClientTest {

    private String mockServerBaseUri1;
    private String mockServerBaseUri2;
    private String mockServerBaseUri3;

    private WireMockServer wireMockServer1;
    private WireMockServer wireMockServer2;
    private WireMockServer wireMockServer3;

    private KieServicesConfiguration config;

    protected WireMockServer createMockServer(String version, int port) {
        WireMockServer wireMockServer = new WireMockServer(port);
        wireMockServer.stubFor(get(urlEqualTo("/"))
                .withHeader("Accept", equalTo("application/xml"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/xml")
                        .withBody("<response type=\"SUCCESS\" msg=\"Kie Server info\">\n" +
                                "  <kie-server-info>\n" +
                                "    <version>" + version + "</version>\n" +
                                "  </kie-server-info>\n" +
                                "</response>")));

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

        int port3 = BaseKieServicesClientTest.findFreePort();
        wireMockServer3 = createMockServer("3", port3);
        wireMockServer3.start();

        mockServerBaseUri1 = "http://localhost:" + port1;
        mockServerBaseUri2 = "http://localhost:" + port2;
        mockServerBaseUri3 = "http://localhost:" + port3;
        String mockServerBaseUri3Duplicated = "http://localhost:" + port3;

        config = KieServicesFactory.newRestConfiguration( mockServerBaseUri1+"|"+ mockServerBaseUri2 + "|" + mockServerBaseUri3 + "|" + mockServerBaseUri3Duplicated, null, null );
        // set capabilities upfront to avoid additional request to server info to make the tests more determinable
        config.setCapabilities(Arrays.asList("KieServer"));
    }

    @After
    public void stopServers() {
        wireMockServer1.stop();
        wireMockServer2.stop();
        wireMockServer3.stop();
    }

    @Test
    public void testCloneConfigurationWithLoadBalancer() {
        KieServicesConfiguration cloned = config.clone();
        assertNotNull(cloned);
        assertNull(cloned.getLoadBalancer());

        cloned.setLoadBalancer(LoadBalancer.getDefault("test url"));

        KieServicesConfiguration cloneOfCloned = cloned.clone();
        assertNotNull(cloned);
        assertNotNull(cloned.getLoadBalancer());

        assertEquals(cloned.getLoadBalancer(), cloneOfCloned.getLoadBalancer());
    }

    @Test
    public void testDefaultLoadBalancer() {

        KieServicesClient client = KieServicesFactory.newKieServicesClient(config);

        List<String> available = ((AbstractKieServicesClientImpl)client).getLoadBalancer().getAvailableEndpoints();
        assertNotNull(available);
        assertEquals(3, available.size());

        ServiceResponse<KieServerInfo> response = client.getServerInfo();
        assertSuccess(response);
        assertEquals("Server version", "1", response.getResult().getVersion());

        response = client.getServerInfo();
        assertSuccess(response);
        assertEquals("Server version", "2", response.getResult().getVersion());

        response = client.getServerInfo();
        assertSuccess(response);
        assertEquals("Server version", "3", response.getResult().getVersion());
    }

    @Test
    public void testRandomLoadBalancer() {
        config.setLoadBalancer(LoadBalancer.forStrategy(config.getServerUrl(), BalancerStrategy.Type.RANDOM_STRATEGY));
        KieServicesClient client = KieServicesFactory.newKieServicesClient(config);

        List<String> available = ((AbstractKieServicesClientImpl)client).getLoadBalancer().getAvailableEndpoints();
        assertNotNull(available);
        assertEquals(3, available.size());

        ServiceResponse<KieServerInfo> response = client.getServerInfo();
        assertSuccess(response);
//        assertEquals("Server version", "1", response.getResult().getVersion());

        response = client.getServerInfo();
        assertSuccess(response);
//        assertEquals("Server version", "2", response.getResult().getVersion());

        response = client.getServerInfo();
        assertSuccess(response);
//        assertEquals("Server version", "3", response.getResult().getVersion());
    }

    @Test
    public void testDefaultLoadBalancerUnavailableServer() throws Exception {

        wireMockServer1.stop();

        KieServicesClient client = KieServicesFactory.newKieServicesClient(config);

        ServiceResponse<KieServerInfo> response = client.getServerInfo();
        assertSuccess(response);
        assertEquals("Server version", "2", response.getResult().getVersion());

        response = client.getServerInfo();
        assertSuccess(response);
        assertEquals("Server version", "3", response.getResult().getVersion());

        response = client.getServerInfo();
        assertSuccess(response);
        assertEquals("Server version", "2", response.getResult().getVersion());

        response = client.getServerInfo();
        assertSuccess(response);
        assertEquals("Server version", "3", response.getResult().getVersion());

        List<String> available = ((AbstractKieServicesClientImpl)client).getLoadBalancer().getAvailableEndpoints();
        assertNotNull(available);
        assertEquals(2, available.size());

        // now let's put back online server 1
        wireMockServer1.start();

        Future waitForResult = ((AbstractKieServicesClientImpl)client).getLoadBalancer().checkFailedEndpoints();
        waitForResult.get(5, TimeUnit.SECONDS);

        available = ((AbstractKieServicesClientImpl)client).getLoadBalancer().getAvailableEndpoints();
        assertNotNull(available);
        assertEquals(3, available.size());

        response = client.getServerInfo();
        assertSuccess(response);
        assertEquals("Server version", "2", response.getResult().getVersion());

        response = client.getServerInfo();
        assertSuccess(response);
        assertEquals("Server version", "3", response.getResult().getVersion());
        // this is the most important as it was offline before (server 1)
        response = client.getServerInfo();
        assertSuccess(response);
        assertEquals("Server version", "1", response.getResult().getVersion());
    }

    @Test
    public void testDefaultLoadBalancerNoServersAvailable() throws Exception {

        KieServicesClient client = KieServicesFactory.newKieServicesClient(config);
        ServiceResponse<KieServerInfo> response = client.getServerInfo();
        assertSuccess(response);
        assertEquals("Server version", "1", response.getResult().getVersion());

        wireMockServer1.stop();
        wireMockServer2.stop();
        wireMockServer3.stop();

        try {
            client.getServerInfo();
            fail("No servers available as all of them were stopped");
        } catch (KieServerHttpRequestException e) {
            assertEquals("No available endpoints found", e.getMessage());
        }

        // now let's put back online server 1
        wireMockServer1.start();

        try {
            client.getServerInfo();
            fail("No servers available even though one was started as load balancer was not refreshed");
        } catch (KieServerHttpRequestException e) {
            assertEquals("No available endpoints found", e.getMessage());
        }
        // now let's refresh load balancer info
        Future waitForResult = ((AbstractKieServicesClientImpl)client).getLoadBalancer().checkFailedEndpoints();
        waitForResult.get(5, TimeUnit.SECONDS);

        response = client.getServerInfo();
        assertSuccess(response);
        assertEquals("Server version", "1", response.getResult().getVersion());
    }

    @Test
    public void testDefaultLoadBalancerNotValidHost() throws Exception {

        config = KieServicesFactory.newRestConfiguration( "http://not-existing-host.com:8080/server", null, null );
        // set capabilities upfront to avoid additional request to server info to make the tests more determinable
        config.setCapabilities(Arrays.asList("KieServer"));

        KieServicesClient client = KieServicesFactory.newKieServicesClient(config);
        try {
            client.getServerInfo();
            fail("There is no valid kie server url");
        } catch (KieServerHttpRequestException e) {
            // expected since no valid endpoint was found
        }

        List<String> failed = ((AbstractKieServicesClientImpl)client).getLoadBalancer().getFailedEndpoints();
        assertEquals(1, failed.size());

        ((AbstractKieServicesClientImpl)client).getLoadBalancer().activate(mockServerBaseUri1);

        ServiceResponse<KieServerInfo> response = client.getServerInfo();
        assertSuccess(response);
        assertEquals("Server version", "1", response.getResult().getVersion());
    }



    private void assertSuccess(ServiceResponse<?> response) {
        assertEquals("Response type", ServiceResponse.ResponseType.SUCCESS, response.getType());
    }
}
