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

package org.kie.server.router.proxy;

import java.net.SocketException;
import java.net.URI;
import java.net.UnknownHostException;
import java.nio.channels.UnresolvedAddressException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.proxy.LoadBalancingProxyClient;
import io.undertow.server.handlers.proxy.ProxyCallback;
import io.undertow.server.handlers.proxy.ProxyClient;
import io.undertow.server.handlers.proxy.ProxyConnection;
import org.jboss.logging.Logger;
import org.kie.server.router.Configuration;
import org.kie.server.router.ConfigurationListener;
import org.kie.server.router.handlers.AdminHttpHandler;
import org.kie.server.router.spi.ContainerResolver;
import org.kie.server.router.spi.RestrictionPolicy;

public class KieServerProxyClient implements ProxyClient, ConfigurationListener {

    private static final Logger log = Logger.getLogger(KieServerProxyClient.class);

    private ServiceLoader<ContainerResolver> containerResolverServiceLoader = ServiceLoader.load(ContainerResolver.class);
    private ServiceLoader<RestrictionPolicy> restrictionPolicyServiceLoader = ServiceLoader.load(RestrictionPolicy.class);

    private ContainerResolver containerResolver = new DefaultContainerResolver();
    private RestrictionPolicy restrictionPolicy = new DefaultRestrictionPolicy();

    private Map<String, CaptureHostLoadBalancingProxyClient> containerClients = new ConcurrentHashMap<>();

    private Configuration configuration;
    private AdminHttpHandler adminHandler;

    public KieServerProxyClient(Configuration configuration, AdminHttpHandler adminHandler) {
        this.configuration = configuration;
        this.adminHandler = adminHandler;
        this.configuration.addListener(this);
        List<ContainerResolver> foundResolvers = new ArrayList<>();
        containerResolverServiceLoader.forEach(cr -> foundResolvers.add(cr));

        List<RestrictionPolicy> foundPolicies = new ArrayList<>();
        restrictionPolicyServiceLoader.forEach(p -> foundPolicies.add(p));

        if (foundPolicies.size() > 1 || foundResolvers.size() > 1) {
            throw new IllegalStateException("Found more than one RestrictionPolicy " + foundPolicies + " or ContainerResolver " + foundResolvers);
        }

        if (!foundResolvers.isEmpty()) {
            this.containerResolver = foundResolvers.get(0);
        }
        if (!foundPolicies.isEmpty()) {
            this.restrictionPolicy = foundPolicies.get(0);
        }
        log.infof("Using '%s' container resolver and restriction policy '%s'", containerResolver, restrictionPolicy);
    }

    public synchronized void addContainer(String containerId, URI serverURI) {

        CaptureHostLoadBalancingProxyClient client = containerClients.get(containerId);
        if (client == null) {
            client = new CaptureHostLoadBalancingProxyClient();
            containerClients.put(containerId, client);
        }
        client.addHost(serverURI);
    }

   public synchronized void removeContainer(String containerId, URI serverURI) {

        LoadBalancingProxyClient client = containerClients.get(containerId);
        if (client == null) {
            log.debugf("No backend server found for %s and server URI %s", containerId, serverURI);
            return;
        }
        client.removeHost(serverURI);
    }

    @Override
    public ProxyTarget findTarget(HttpServerExchange exchange) {

        String containerId = containerResolver.resolveContainerId(exchange, configuration.getContainerInfosPerContainer());
        if (restrictionPolicy.restrictedEndpoint(exchange, containerId)) {
            log.debugf("URL %s is restricted according to policy %s", exchange.getRelativePath(), restrictionPolicy.toString());
            return null;
        }
        LoadBalancingProxyClient client = containerClients.get(containerId);

        if (client == null) {
            return null;
        }

        return client.findTarget(exchange);
    }

    @Override
    public void getConnection(ProxyTarget target, HttpServerExchange exchange, final ProxyCallback<ProxyConnection> callback, long timeout, TimeUnit timeUnit) {
        String containerId = containerResolver.resolveContainerId(exchange, configuration.getContainerInfosPerContainer());
        CaptureHostLoadBalancingProxyClient client = containerClients.get(containerId);
        try {
            client.getConnection(target, exchange, new ProxyCallback<ProxyConnection>() {
                @Override
                public void completed(HttpServerExchange exchange, ProxyConnection result) {
                    callback.completed(exchange, result);
                }

                @Override
                public void failed(HttpServerExchange httpServerExchange) {
                    try {
                        adminHandler.removeUnavailableServer(client.getUri());
                    } finally {
                        callback.failed(exchange);
                        client.clear();
                    }
                }

                @Override
                public void couldNotResolveBackend(HttpServerExchange exchange) {
                    callback.couldNotResolveBackend(exchange);
                }

                @Override
                public void queuedRequestFailed(HttpServerExchange exchange) {
                    callback.queuedRequestFailed(exchange);
                }
            }, timeout, timeUnit);
        } catch (Exception e) {
            if (e instanceof SocketException
                    || e instanceof UnknownHostException
                    || e instanceof UnresolvedAddressException
                    // xnio throws IllegalArgumentException for unresolvable host
                    || e instanceof IllegalArgumentException) {
                adminHandler.removeUnavailableServer(client.getUri());
            }

            throw new RuntimeException(e);
        }
    }

    @Override
    public void onContainerAdded(String container, String serverUrl) {
        addContainer(container, URI.create(serverUrl));
    }

    @Override
    public void onContainerRemoved(String container, String serverUrl) {
        removeContainer(container, URI.create(serverUrl));
    }
}
