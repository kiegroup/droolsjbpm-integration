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
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;

import org.kie.server.common.rest.KieServerHttpRequest;
import org.kie.server.common.rest.KieServerHttpRequestException;
import org.kie.server.client.balancer.impl.RandomBalancerStrategy;
import org.kie.server.client.balancer.impl.RoundRobinBalancerStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoadBalancer {

    private static final Logger logger = LoggerFactory.getLogger(LoadBalancer.class);

    private static final String URL_SEP = "\\|";

    private ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

    private final BalancerStrategy balancerStrategy;
    private CopyOnWriteArraySet<String> failedEndpoints = new CopyOnWriteArraySet<String>();

    protected LoadBalancer(BalancerStrategy balancerStrategy) {
        this.balancerStrategy = balancerStrategy;
    }

    public String getUrl() throws KieServerHttpRequestException {
        String selectedUrl = balancerStrategy.next();
        logger.debug("Load balancer {} selected url '{}'", balancerStrategy, selectedUrl);
        return selectedUrl;
    }

    public void markAsFailed(String url) {
        failedEndpoints.add(url);
        balancerStrategy.markAsOffline(url);
        logger.debug("Url '{}' is marked as failed and will be considered offline by {}", url, balancerStrategy);
    }

    public void activate(String url) {
        failedEndpoints.remove(url);
        balancerStrategy.markAsOnline(url);
        logger.debug("Url '{}' is marked as activated and will be considered online by {}", url, balancerStrategy);
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
    public Future<?> checkFailedEndpoints() {
        return executorService.submit(new CheckFailedEndpoints());
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
            logger.debug("Starting to scan if any of the failed endpoints is back online");
            Iterator<String> iterator = failedEndpoints.iterator();

            while(iterator.hasNext()) {
                String failedEndpoint = iterator.next();
                try {
                    KieServerHttpRequest httpRequest =
                            KieServerHttpRequest.newRequest(failedEndpoint).followRedirects(true).timeout(1000);
                    httpRequest.get();

                    logger.debug("Url '{}' is back online, adding it to load balancer", failedEndpoint);
                    // first remove
//                    iterator.remove();
                    // then activate to avoid concurrent modifications on the failedEndpoints
                    activate(failedEndpoint);
                } catch (Exception e) {
                    logger.debug("Url '{}' is still offline due to {}", failedEndpoint, (e.getCause() == null ? e.getMessage() : e.getCause().getMessage()));
                }
            }
        }
    }
}
