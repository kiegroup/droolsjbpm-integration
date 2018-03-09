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

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.stubbing.Scenario;
import org.assertj.core.api.Assertions;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.kie.server.api.model.KieServerInfo;
import org.kie.server.api.model.KieServerStateInfo;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.client.balancer.BalancerStrategy;
import org.kie.server.client.balancer.LoadBalancer;
import org.kie.server.client.balancer.impl.RoundRobinBalancerStrategy;
import org.kie.server.client.impl.AbstractKieServicesClientImpl;
import org.kie.server.common.rest.KieServerHttpRequestException;
import org.kie.server.common.rest.NoEndpointFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

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

    private static final Logger logger = LoggerFactory.getLogger(LoadBalancerClientTest.class);

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
        wireMockServer.stubFor(get(urlEqualTo("/state"))
                .withHeader("Accept", equalTo("application/xml"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/xml")
                        .withBody("<response type=\"SUCCESS\" msg=\"Kie Server state\">\n" +
                                "  <kie-server-state-info>\n" +
                                "    <version>" + version + "</version>\n" +
                                "  </kie-server-state-info>\n" +
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
        final List<Future<?>> checkFailedEndpointsJob = new ArrayList<>();
        config.setLoadBalancer(new LoadBalancer(new RoundRobinBalancerStrategy(Arrays.asList(config.getServerUrl().split("\\|")))) {

            @Override
            public Future<?> checkFailedEndpoints() {
                Future<?> future = super.checkFailedEndpoints();
                checkFailedEndpointsJob.add(future);
                return future;
            }
            
        });
        
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
        
        assertEquals(2, checkFailedEndpointsJob.size());
        Future<?> waitingForJobsToComplete = checkFailedEndpointsJob.get(1);
        waitingForJobsToComplete.get(5, TimeUnit.SECONDS);
        
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

    @Test
    public void testMultipleConcurrentFailRequestsForLoadBalancerWithSingleServer() throws Exception {

        class SendKieRequestThread extends Thread {
            CountDownLatch startLatch;
            CountDownLatch stopLatch;
            int threadNo;
            KieServicesClient kieClient;

            SendKieRequestThread(int threadNo, CountDownLatch startLatch, CountDownLatch stopLatch, KieServicesClient client) {
                this.startLatch = startLatch;
                this.stopLatch = stopLatch;
                this.threadNo = threadNo;
                this.kieClient = client;
            }

            @Override
            public void run() {
                try {
                    startLatch.await();
                    logger.debug("Th#" + threadNo + " Calling Kie Server ");

                    // Stagger execution of threads by 20ms
                    Thread.sleep(1 * threadNo);
                    // Call KieServer...
                    try {
                        ServiceResponse<KieServerStateInfo> response = this.kieClient.getServerState();
                        fail("Unexpected successful request");
                    } catch (NoEndpointFoundException e) {
                        // expected failed configured in "Timeout Fail followed by Success" scenario
                    }

                } catch (Exception e) {

                    logger.debug("Exception while calling kie Server: "+e);
                } finally {
                    logger.debug("Th#" + threadNo +" Done.");
                    stopLatch.countDown();
                }
            }
        }

        // Setup a single server
        config = KieServicesFactory.newRestConfiguration( mockServerBaseUri1, null, null );
        config.setCapabilities(Arrays.asList("KieServer"));

        KieServicesClient client = KieServicesFactory.newKieServicesClient(config);

        // Issue successful request
        ServiceResponse<KieServerStateInfo> response = client.getServerState();
        assertSuccess(response);
        Assertions.assertThat(response.getResult().getContainers()).isEmpty();

        // Setup 2 concurrent requests both failing with a timeout representing a temporary failure in server
        wireMockServer1.stubFor(get(urlEqualTo("/state"))
                .withHeader("Accept", equalTo("application/xml"))
                .inScenario("Timeout Fails followed by Scan Success")
                .whenScenarioStateIs(Scenario.STARTED)
                .willReturn(aResponse()
                        .withFixedDelay(5100)
                        .withStatus(200)
                        .withHeader("Content-Type", "application/xml")
                        .withBody("<response type=\"SUCCESS\" msg=\"Kie Server state\">\n" +
                                "  <kie-server-state-info>\n" +
                                "    <version>1a</version>\n" +
                                "  </kie-server-state-info>\n" +
                                "</response>"))
                .willSetStateTo("Req Timeout 1"));

        wireMockServer1.stubFor(get(urlEqualTo("/state"))
                .withHeader("Accept", equalTo("application/xml"))
                .inScenario("Timeout Fails followed by Scan Success")
                .whenScenarioStateIs("Req Timeout 1")
                .willReturn(aResponse()
                        .withFixedDelay(5100)
                        .withStatus(200)
                        .withHeader("Content-Type", "application/xml")
                        .withBody("<response type=\"SUCCESS\" msg=\"Kie Server state\">\n" +
                                "  <kie-server-state-info>\n" +
                                "    <version>1b</version>\n" +
                                "  </kie-server-state-info>\n" +
                                "</response>"))
                .willSetStateTo("Req Timeout 2"));

        // Add a delay to background thread scanning to ensure availableEndpoints list
        // is kept empty long enough to demonstrate failing situation.
        wireMockServer1.stubFor(get(urlEqualTo("/"))
                .inScenario("Timeout Fails followed by Scan Success")
                .whenScenarioStateIs("Req Timeout 2")
                .willReturn(aResponse()
                        .withFixedDelay(500)
                        .withStatus(200)
                        .withHeader("Content-Type", "application/xml")
                        .withBody("<response type=\"SUCCESS\" msg=\"Kie Server info\">\n" +
                                "  <kie-server-info>\n" +
                                "    <version>background scan</version>\n" +
                                "  </kie-server-info>\n" +
                                "</response>"))
                .willSetStateTo("Good Req 1"));

        // Request to indicate server is back online
        wireMockServer1.stubFor(get(urlEqualTo("/state"))
                .withHeader("Accept", equalTo("application/xml"))
                .inScenario("Timeout Fails followed by Scan Success")
                .whenScenarioStateIs("Good Req 1")
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/xml")
                        .withBody("<response type=\"SUCCESS\" msg=\"Kie Server state\">\n" +
                                "  <kie-server-state-info>\n" +
                                "    <version>1c</version>\n" +
                                "  </kie-server-state-info>\n" +
                                "</response>"))
                .willSetStateTo("Good Req 2"));

        // Kickoff first scenario
        int threadCount=2;
        logger.debug("Starting 2 Threads");
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch stopLatch = new CountDownLatch(threadCount);
        for (int i=1; i<= threadCount; i++) {
            new SendKieRequestThread(i, startLatch, stopLatch, client).start();
        }
        // Threads will be waiting to proceed, so let them off.
        startLatch.countDown();

        // We expect the threads to complete within 7 seconds
        stopLatch.await(7, TimeUnit.SECONDS);
        logger.debug("\nEnd of Threads - ");

        // Need to sleep to ensure background thread completes.
        logger.debug("\nSleeping for 3 seconds - ");
        Thread.sleep(3000);

        // Expect to now have server:/state incorrectly retained in failedEndpoints
        List<String> availableList = ((AbstractKieServicesClientImpl)client).getLoadBalancer().getAvailableEndpoints();
        availableList.forEach(item -> logger.debug("Available Endpoint : [" + item + "]"));
        List<String> failedList = ((AbstractKieServicesClientImpl)client).getLoadBalancer().getFailedEndpoints();
        failedList.forEach(item -> logger.debug("Failed Endpoint : [" + item + "]"));

        // Now set up a subsequent request failure due to server temporarily not responding.");
        // Could have many successful requests up to this point after first failing scenario but as
        // soon as we have another timeout failure like below, server:/state gets moved to availableEndpoints.
        wireMockServer1.stubFor(get(urlEqualTo("/state"))
                .withHeader("Accept", equalTo("application/xml"))
                .inScenario("Brief Timeout Fails followed by Scan Success")
                .willReturn(aResponse()
                        .withFixedDelay(5100)
                        .withStatus(200)
                        .withHeader("Content-Type", "application/xml")
                        .withBody("<response type=\"SUCCESS\" msg=\"Kie Server state\">\n" +
                                "  <kie-server-state-info>\n" +
                                "    <version>1d</version>\n" +
                                "  </kie-server-state-info>\n" +
                                "</response>"))
                .willSetStateTo("After Failed Req"));

        wireMockServer1.stubFor(get(urlEqualTo("/state"))
                .inScenario("Brief Timeout Fails followed by Scan Success")
                .whenScenarioStateIs("After Failed Req")
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/xml")
                        .withBody("<response type=\"SUCCESS\" msg=\"Kie Server state\">\n" +
                                "  <kie-server-state-info>\n" +
                                "    <version>1e</version>\n" +
                                "  </kie-server-state-info>\n" +
                                "</response>"))
                .willSetStateTo("After success 1"));

        wireMockServer1.stubFor(get(urlEqualTo("/"))
                .inScenario("Brief Timeout Fails followed by Scan Success")
                .whenScenarioStateIs("After success 1")
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/xml")
                        .withBody("<response type=\"SUCCESS\" msg=\"Kie Server info\">\n" +
                                "  <kie-server-info>\n" +
                                "    <version>background scan</version>\n" +
                                "  </kie-server-info>\n" +
                                "</response>"))
                .willSetStateTo("After success 2"));
        logger.debug(" Current wireMockServer1 stub count =" + wireMockServer1.listAllStubMappings().getMappings().size());

        // Make call to failing request
        try {
            response = client.getServerState();
            fail("Unexpected successful request");
        } catch (NoEndpointFoundException e) {
            // expect failure
        }

        // Expect to now have server:/state incorrectly retained in availableEndpoints transferred from failedEndpoints
        availableList = ((AbstractKieServicesClientImpl)client).getLoadBalancer().getAvailableEndpoints();
        availableList.forEach(item -> logger.debug("Available Endpoint : [" + item + "]"));
        failedList = ((AbstractKieServicesClientImpl)client).getLoadBalancer().getFailedEndpoints();
        if (failedList.isEmpty()) {
            logger.debug("Failed Endpoint : []");
        }
        else {
            failedList.forEach(item -> logger.debug("Failed Endpoint : [" + item + "]"));
        }

        // Need delay here to give background thread a chance to complete scanning to see if server is online
        Thread.sleep(1000);

        // Set up what should be a successful request
        wireMockServer1.stubFor(get(urlEqualTo("/state"))
                .withHeader("Accept", equalTo("application/xml"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/xml")
                        .withBody("<response type=\"SUCCESS\" msg=\"Kie Server state\">\n" +
                                "  <kie-server-state-info>\n" +
                                "    <version>1b</version>\n" +
                                "  </kie-server-state-info>\n" +
                                "</response>")));

        // Run request and expect success request but instead a 404 HTTP status results
        // due to final request URL generated as "http://localhost:<port>/state/state"
        // based on presence of server:/state in availableEndpoints.
        response = client.getServerState();
        assertSuccess(response);
        Assertions.assertThat(response.getResult().getContainers()).isEmpty();
    }


    @Test
    public void testDefaultLoadBalancerFirstServerNotAvailable() throws Exception {
        KieServicesClient client = KieServicesFactory.newKieServicesClient(config);

        wireMockServer1.stop();

        ServiceResponse<KieServerStateInfo> response = client.getServerState();
        assertSuccess(response);
        Assertions.assertThat(response.getResult().getContainers()).isEmpty();
    }

    private void assertSuccess(ServiceResponse<?> response) {
        assertEquals("Response type", ServiceResponse.ResponseType.SUCCESS, response.getType());
    }
}
