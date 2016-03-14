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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.Before;
import org.junit.Test;
import org.kie.remote.common.rest.KieRemoteHttpRequestException;
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

    @Before
    public void startServers() {
        int port1 = BaseKieServicesClientTest.findFreePort();
        wireMockServer1 = new WireMockServer(port1);
        wireMockServer1.stubFor(get(urlEqualTo("/"))
                .withHeader("Accept", equalTo("application/xml"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/xml")
                        .withBody("<response type=\"SUCCESS\" msg=\"Kie Server info\">\n" +
                                "  <kie-server-info>\n" +
                                "    <version>1</version>\n" +
                                "  </kie-server-info>\n" +
                                "</response>")));
        wireMockServer1.start();

        int port2 = BaseKieServicesClientTest.findFreePort();
        wireMockServer2 = new WireMockServer(port2);

        wireMockServer2.stubFor(get(urlEqualTo("/"))
                .withHeader("Accept", equalTo("application/xml"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/xml")
                        .withBody("<response type=\"SUCCESS\" msg=\"Kie Server info\">\n" +
                                "  <kie-server-info>\n" +
                                "    <version>2</version>\n" +
                                "  </kie-server-info>\n" +
                                "</response>")));
        wireMockServer2.start();

        int port3 = BaseKieServicesClientTest.findFreePort();
        wireMockServer3 = new WireMockServer(port3);
        wireMockServer3.stubFor(get(urlEqualTo("/"))
                .withHeader("Accept", equalTo("application/xml"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/xml")
                        .withBody("<response type=\"SUCCESS\" msg=\"Kie Server info\">\n" +
                                "  <kie-server-info>\n" +
                                "    <version>3</version>\n" +
                                "  </kie-server-info>\n" +
                                "</response>")));
        wireMockServer3.start();

        mockServerBaseUri1 = "http://localhost:" + port1;
        mockServerBaseUri2 = "http://localhost:" + port2;
        mockServerBaseUri3 = "http://localhost:" + port3;

        config = KieServicesFactory.newRestConfiguration( mockServerBaseUri1+"|"+ mockServerBaseUri2 + "|" + mockServerBaseUri3, null, null );
        // set capabilities upfront to avoid additional request to server info to make the tests more determinable
        config.setCapabilities(Arrays.asList("KieServer"));
    }

    public void stopServers() {
        wireMockServer1.stop();
        wireMockServer2.stop();
        wireMockServer3.stop();
    }

    @Test
    public void testDefaultLoadBalancer() {

        KieServicesClient client = KieServicesFactory.newKieServicesClient(config);
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

        // now let's put back online server 1
        wireMockServer1.start();

        Future waitForResult = ((AbstractKieServicesClientImpl)client).getLoadBalancer().checkFailedEndpoints();
        waitForResult.get(5, TimeUnit.SECONDS);

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
        } catch (KieRemoteHttpRequestException e) {
            assertEquals("No available endpoints found", e.getMessage());
        }

        // now let's put back online server 1
        wireMockServer1.start();

        try {
            client.getServerInfo();
            fail("No servers available even though one was started as load balancer was not refreshed");
        } catch (KieRemoteHttpRequestException e) {
            assertEquals("No available endpoints found", e.getMessage());
        }
        // now let's refresh load balancer info
        Future waitForResult = ((AbstractKieServicesClientImpl)client).getLoadBalancer().checkFailedEndpoints();
        waitForResult.get(5, TimeUnit.SECONDS);

        response = client.getServerInfo();
        assertSuccess(response);
        assertEquals("Server version", "1", response.getResult().getVersion());
    }

    private void assertSuccess(ServiceResponse<?> response) {
        assertEquals("Response type", ServiceResponse.ResponseType.SUCCESS, response.getType());
    }
}
