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

import io.prometheus.client.Gauge;
import org.jbpm.services.api.DeploymentEvent;
import org.jbpm.services.api.DeploymentEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PrometheusDeploymentEventListener implements DeploymentEventListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(PrometheusDeploymentEventListener.class);

    private static final Gauge runningDeployments = Gauge.build()
            .name("kie_server_deployments_active_total")
            .help("Kie Server Active Deployments")
            .labelNames("deployment_id")
            .register();

    @Override
    public void onDeploy(DeploymentEvent event) {
        LOGGER.debug("OnDeploy: {}", event);
        runningDeployments.labels(event.getDeploymentId()).inc();
    }

    @Override
    public void onUnDeploy(DeploymentEvent event) {
        LOGGER.debug("OnUnDeploy: {}", event);
        runningDeployments.labels(event.getDeploymentId()).dec();
    }

    @Override
    public void onActivate(DeploymentEvent event) {
    }

    @Override
    public void onDeactivate(DeploymentEvent event) {
    }
}
