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

package org.kie.server.router.proxy.aggregate;

import io.undertow.protocols.ssl.UndertowXnioSsl;
import io.undertow.server.handlers.proxy.LoadBalancingProxyClient;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.kie.server.router.Configuration;
import org.kie.server.router.handlers.AdminHttpHandler;
import org.kie.server.router.proxy.CaptureHostLoadBalancingProxyClient;
import org.kie.server.router.proxy.KieServerProxyClient;
import org.kie.server.router.repository.FileRepository;
import org.kie.server.router.spi.ConfigRepository;
import org.kie.server.router.utils.SSLContextBuilder;
import org.xnio.ssl.XnioSsl;

import java.lang.reflect.Field;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class KieServerProxyClientTest {

    private static final String TRUSTSTORE_PATH = SSLContextBuilder.class.getClassLoader().getResource("keystores/router.truststore").getFile();
    private static final String TRUSTSTORE_PASSWORD = "mykeystorepass";

    private ConfigRepository repository = new FileRepository();
    private ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

    @After
    public void cleanProps() {
        System.clearProperty("javax.net.ssl.trustStore");
        System.clearProperty("javax.net.ssl.trustStorePassword");
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testAddHost() throws URISyntaxException, NoSuchFieldException, IllegalAccessException {

        Configuration configuration = repository.load();
        AdminHttpHandler adminHandler = new AdminHttpHandler(configuration, repository, executorService);
        KieServerProxyClient proxyClient = new KieServerProxyClient(configuration, adminHandler);

        // clean ssl properties
        String containerId = "my-container";
        URI uri = new URI("http://localhost:8080");

        Field containerClientsField = proxyClient.getClass().getDeclaredField("containerClients");
        containerClientsField.setAccessible(true);
        Map<String, CaptureHostLoadBalancingProxyClient> containerClients = (Map<String, CaptureHostLoadBalancingProxyClient>) containerClientsField.get(proxyClient);

        containerClients.put(containerId, new CaptureHostLoadBalancingProxyClientStub());
        proxyClient.addContainer(containerId, uri);

        // Check if the containerClients has the expected values
        Assert.assertEquals(1, containerClients.size());
        Assert.assertEquals(true, containerClients.containsKey(containerId));
        Assert.assertNotNull(containerClients.get(containerId));

        // Check if the LoadBalancingProxyClient.Host contains the added host
        CaptureHostLoadBalancingProxyClientStub client = (CaptureHostLoadBalancingProxyClientStub) containerClients.get(containerId);
        Assert.assertEquals(uri, client.getHost());
        Assert.assertEquals(null, client.getSsl());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testAddHostWithSSL() throws URISyntaxException, NoSuchFieldException, IllegalAccessException {
        System.setProperty("javax.net.ssl.trustStore", TRUSTSTORE_PATH);
        System.setProperty("javax.net.ssl.trustStorePassword", TRUSTSTORE_PASSWORD);

        Configuration configuration = repository.load();
        AdminHttpHandler adminHandler = new AdminHttpHandler(configuration, repository, executorService);
        KieServerProxyClient proxyClient = new KieServerProxyClient(configuration, adminHandler);

        String containerId = "my-container-ssl";
        URI uri = new URI("https://localhost:9443");

        Field containerClientsField = proxyClient.getClass().getDeclaredField("containerClients");
        containerClientsField.setAccessible(true);
        Map<String, CaptureHostLoadBalancingProxyClient> containerClients = (Map<String, CaptureHostLoadBalancingProxyClient>) containerClientsField.get(proxyClient);

        containerClients.put(containerId, new CaptureHostLoadBalancingProxyClientStub());
        proxyClient.addContainer(containerId, uri);

        // Check if the containerClients has the expected values
        Assert.assertEquals(1, containerClients.size());
        Assert.assertEquals(true, containerClients.containsKey(containerId));
        Assert.assertNotNull(containerClients.get(containerId));

        // Check if the LoadBalancingProxyClient.Host contains the added host
        CaptureHostLoadBalancingProxyClientStub client = (CaptureHostLoadBalancingProxyClientStub) containerClients.get(containerId);
        Assert.assertEquals(uri, client.getHost());
        Assert.assertSame(UndertowXnioSsl.class, client.getSsl().getClass());

    }

    private class CaptureHostLoadBalancingProxyClientStub extends CaptureHostLoadBalancingProxyClient {

        URI host;
        XnioSsl ssl;

        @Override
        public synchronized LoadBalancingProxyClient addHost(URI host, XnioSsl ssl) {
            this.host = host;
            this.ssl = ssl;
            return this;
        }

        @Override
        public synchronized LoadBalancingProxyClient addHost(URI host) {
            this.host = host;
            return this;
        }

        public URI getHost() {
            return host;
        }

        public XnioSsl getSsl() {
            return ssl;
        }
    }
}
