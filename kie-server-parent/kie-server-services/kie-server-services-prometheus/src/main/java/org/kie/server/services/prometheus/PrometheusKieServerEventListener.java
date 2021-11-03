/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.server.services.prometheus;

import io.prometheus.client.Counter;
import io.prometheus.client.Gauge;
import org.kie.server.api.model.KieServerInfo;
import org.kie.server.services.api.KieContainerInstance;
import org.kie.server.services.api.KieServer;
import org.kie.server.services.api.KieServerEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PrometheusKieServerEventListener implements KieServerEventListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(PrometheusKieServerEventListener.class);

    protected static final Counter numberOfContainersStartedSinceBoot = Counter.build()
            .name("kie_server_container_started_since_boot")
            .help("Kie Server Started Containers since startup")
            .register();

    protected static final Gauge numberOfContainersStartedByContainerId = Gauge.build()
            .name("kie_server_container_started")
            .help("Kie Server Started Containers by ID")
            .labelNames("container_id")
            .register();

    protected static final Gauge numberOfContainersStartedTotal = Gauge.build()
            .name("kie_server_container_started_total")
            .help("Total of Kie Server Started Containers")
            .register();

    protected static final Gauge runningContainersByContainerId = Gauge.build()
            .name("kie_server_container_running")
            .help("Total of Running Kie Containers by Container ID")
            .labelNames("container_id")
            .register();

    protected static final Gauge runningContainersTotal = Gauge.build()
            .name("kie_server_container_running_total")
            .help("Total of Running Kie Containers")
            .register();

    protected static final Gauge kieServerStartTime = Gauge.build()
            .name("kie_server_start_time")
            .help("Kie Server Start Time")
            .labelNames("name", "server_id", "location", "version")
            .register();

    @Override
    public void beforeServerStarted(KieServer kieServer) {
    }

    @Override
    public void afterServerStarted(KieServer kieServer) {
        LOGGER.debug("After Kie Server started: {}", kieServer);
        final KieServerInfo info = kieServer.getInfo().getResult();
        kieServerStartTime.labels(info.getName(),
                                  info.getServerId(),
                                  info.getLocation(),
                                  info.getVersion()).set(System.currentTimeMillis());
    }

    @Override
    public void beforeServerStopped(KieServer kieServer) {
    }

    @Override
    public void afterServerStopped(KieServer kieServer) {
    }

    @Override
    public void beforeContainerStarted(KieServer kieServer, KieContainerInstance containerInstance) {
    }

    @Override
    public void afterContainerStarted(KieServer kieServer, KieContainerInstance containerInstance) {
        LOGGER.debug("After container started: {}", containerInstance);
        numberOfContainersStartedByContainerId.labels(containerInstance.getContainerId()).inc();
        numberOfContainersStartedTotal.inc();

        runningContainersByContainerId.labels(containerInstance.getContainerId()).inc();
        runningContainersTotal.inc();

        numberOfContainersStartedSinceBoot.inc();
    }

    @Override
    public void beforeContainerStopped(KieServer kieServer, KieContainerInstance containerInstance) {


    }

    @Override
    public void afterContainerStopped(KieServer kieServer, KieContainerInstance containerInstance) {
        LOGGER.debug("After container stopped: {}", containerInstance);
        numberOfContainersStartedByContainerId.labels(containerInstance.getContainerId()).dec();
        numberOfContainersStartedTotal.dec();

        runningContainersByContainerId.labels(containerInstance.getContainerId()).dec();
        runningContainersTotal.dec();
    }

    @Override
    public void beforeContainerActivated(KieServer kieServer, KieContainerInstance containerInstance) {
    }

    @Override
    public void afterContainerActivated(KieServer kieServer, KieContainerInstance containerInstance) {
        LOGGER.debug("After container activated: {}", containerInstance);
        runningContainersByContainerId.labels(containerInstance.getContainerId()).inc();
        runningContainersTotal.inc();
    }

    @Override
    public void beforeContainerDeactivated(KieServer kieServer, KieContainerInstance containerInstance) {
        LOGGER.debug("After container deactivated: {}", containerInstance);
        runningContainersByContainerId.labels(containerInstance.getContainerId()).dec();
        runningContainersTotal.dec();
    }

    @Override
    public void afterContainerDeactivated(KieServer kieServer, KieContainerInstance containerInstance) {
    }
}
