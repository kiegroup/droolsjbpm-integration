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

package org.kie.server.client.balancer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.kie.server.api.KieServerConstants;
import org.kie.server.client.KieServicesConfiguration;
import org.kie.server.client.balancer.impl.RandomBalancerStrategy;
import org.kie.server.client.balancer.impl.RoundRobinBalancerStrategy;
import org.kie.server.common.rest.KieServerHttpRequest;
import org.kie.server.common.rest.KieServerHttpRequestException;
import org.kie.server.common.rest.NoEndpointFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoadBalancer {

    public interface EndpointListener {
        default void markAsActive(String endpoint) {
            // do nothing
        };

        default void markAsFailed(String endpoint) {
            // do nothing
        };
    };

    public static final Long FAILED_ENDPOINT_INTERVAL_CHECK = Long.getLong(KieServerConstants.KIE_JBPM_SERVER_CLIENT_FAILED_ENDPOINT_INTERVAL_CHECK, 5000);
    private static final Logger logger = LoggerFactory.getLogger(LoadBalancer.class);

    private static final String URL_SEP = "\\|";

    private ScheduledExecutorService executorService = Executors.newScheduledThreadPool(4);

    private final BalancerStrategy balancerStrategy;
    private CopyOnWriteArraySet<String> failedEndpoints = new CopyOnWriteArraySet<>();
    private final List<EndpointListener> endpointListeners = new ArrayList<>();

    private String userName;
    private String password;

    private volatile boolean backgroundCheck = false;

    private boolean checkFailedEndpoint;

    public void addListener(EndpointListener listener) {
        synchronized (endpointListeners) {
            this.endpointListeners.add(listener);
        }
    }

    public LoadBalancer(BalancerStrategy balancerStrategy) {
        this.checkFailedEndpoint = true;
        this.balancerStrategy = balancerStrategy;
    }

    public String getUrl() throws KieServerHttpRequestException {
        try {
            String selectedUrl = balancerStrategy.next();
            logger.debug("Load balancer {} selected url '{}'", balancerStrategy, selectedUrl);
            return selectedUrl;
        } catch (NoEndpointFoundException e) {
            checkFailedEndpoints();
            throw e;
        }
    }

    public String markAsFailed(String url) {
        
        String baseUrl = balancerStrategy.markAsOffline(url);
        failedEndpoints.add(baseUrl);
        logger.debug("Url '{}' is marked as failed and will be considered offline by {}", url, balancerStrategy);
        List<EndpointListener> threadSafeListeners = new ArrayList<>();
        synchronized (endpointListeners) {
            threadSafeListeners.addAll(endpointListeners);
        }
        threadSafeListeners.forEach(e -> e.markAsFailed(baseUrl));
        return baseUrl;
    }

    public void activate(String url) {
        
        String baseUrl = balancerStrategy.markAsOnline(url);
        failedEndpoints.remove(baseUrl);
        logger.debug("Url '{}' is marked as activated and will be considered online by {}", url, balancerStrategy);
        List<EndpointListener> threadSafeListeners = new ArrayList<>();
        synchronized (endpointListeners) {
            threadSafeListeners.addAll(endpointListeners);
        }
        threadSafeListeners.forEach(e -> e.markAsActive(baseUrl));
    }

    public void close() {
        try {
            executorService.shutdownNow();
        } catch (Exception e) {
            logger.debug("Error when shutting down load balancer executor service");
        }
    }

    public List<String> getAvailableEndpoints() {
        return this.balancerStrategy.getAvailableEndpoints();
    }

    public List<String> getFailedEndpoints() {
        return new ArrayList<String>(failedEndpoints);
    }

    /*
     *  background operations for checking failed endpoints
     */
    public synchronized Future<?> checkFailedEndpoints() {
        if (!checkFailedEndpoint) {
            return null;
        }

        CountDownLatch latch = new CountDownLatch (1);
        EndpointListener lst = new EndpointListener() {
            @Override
            public void markAsActive(String endpoint) {
                synchronized (endpointListeners) {
                    endpointListeners.remove(this);
                }
                latch.countDown();
            }
        };
        endpointListeners.add(lst);
        // create a bridge between the latch and the latch
        Future<Object> futureBridge = new Future<Object> () {

            @Override
            public boolean cancel(boolean mayInterruptIfRunning) {
                return false;
            }

            @Override
            public boolean isCancelled() {
                return false;
            }

            @Override
            public boolean isDone() {
                return latch.getCount() == 0;
            }

            @Override
            public Object get() throws InterruptedException, ExecutionException {
                latch.await();
                return null;
            }

            @Override
            public Object get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
                if(!latch.await(timeout, unit)) {
                    logger.debug("Load balancer await for termination expired");
                }
                return null;
            }
            
        };

        if(backgroundCheck) {
            return futureBridge;
        }

        executorService.scheduleWithFixedDelay(new CheckFailedEndpoints(), 0, FAILED_ENDPOINT_INTERVAL_CHECK, TimeUnit.MILLISECONDS);
        backgroundCheck = true;
        return futureBridge;
    }

    /*
     * factory methods
     */

    public static LoadBalancer getDefault(String urls) {
        String[] endpoints = new String[0];
        if (urls != null) {
            endpoints = urls.split(URL_SEP);
        }
        return getDefault(Arrays.asList(endpoints));
    }

    public static LoadBalancer getDefault(List<String> urls) {
        RoundRobinBalancerStrategy strategy = new RoundRobinBalancerStrategy(urls);
        return new LoadBalancer(strategy);
    }

    public static LoadBalancer forStrategy(String urls, BalancerStrategy.Type type) {
        String[] endpoints = urls.split(URL_SEP);
        return forStrategy(Arrays.asList(endpoints), type);
    }

    public static LoadBalancer forStrategy(List<String> urls, BalancerStrategy.Type type) {
        BalancerStrategy strategy = null;

        switch (type) {
            case RANDOM_STRATEGY:
                strategy = new RandomBalancerStrategy(urls);
                break;
            case ROUND_ROBIN_STRATEGY:
                strategy = new RoundRobinBalancerStrategy(urls);
                break;
        }
        if (strategy == null) {
            throw new IllegalArgumentException("Unknown strategy type " + type);
        }
        return new LoadBalancer(strategy);
    }

    /*
     * Runnable for checks on failed endpoints
     */
    private class CheckFailedEndpoints implements Runnable {

        @Override
        public void run() {
            if (failedEndpoints == null || failedEndpoints.isEmpty()) {
                return;
            }

            List<String> endpoints = new ArrayList<>(failedEndpoints);
            logger.debug("Starting to scan if any of the failed endpoints is back online. Endpoints about to check: {}", endpoints);
            Iterator<String> iterator = endpoints.iterator();

            while(iterator.hasNext()) {
                String failedEndpoint = iterator.next();
                try {
                    KieServerHttpRequest httpRequest = KieServerHttpRequest.newRequest(failedEndpoint, userName, password).followRedirects(true).timeout(1000);
                    httpRequest.get();

                    logger.debug("Url '{}' is back online, adding it to load balancer", failedEndpoint);

                    // then activate to avoid concurrent modifications on the failedEndpoints
                    activate(failedEndpoint);
                } catch (Exception e) {
                    logger.debug("Url '{}' is still offline due to {}", failedEndpoint, (e.getCause() == null ? e.getMessage() : e.getCause().getMessage()));
                }
            }

            logger.debug("Ending scan if any of the failed endpoints is back online");
        }
    }

    public void setCheckFailedEndpoint(boolean checkFailedEndpoint) {
        this.checkFailedEndpoint = checkFailedEndpoint;
    }

    public boolean isCheckFailedEndpoint() {
        return checkFailedEndpoint;
    }
}
