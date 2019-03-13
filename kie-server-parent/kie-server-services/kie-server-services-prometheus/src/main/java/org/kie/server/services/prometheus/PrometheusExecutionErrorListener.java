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
import org.kie.internal.runtime.error.ExecutionError;
import org.kie.internal.runtime.error.ExecutionErrorListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.apache.commons.lang3.StringUtils.defaultString;

public class PrometheusExecutionErrorListener implements ExecutionErrorListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(PrometheusExecutionErrorListener.class);

    private static final Counter numberOfErrors = Counter.build()
            .name("kie_server_execution_error_total")
            .help("Kie Server Execution Errors")
            .labelNames("deployment_id", "error_type", "activity_name")
            .register();

    @Override
    public void onExecutionError(final ExecutionError error) {
        LOGGER.debug("On Execution Error: {}", error);
        numberOfErrors.labels((error.getDeploymentId()),
                              defaultString(error.getType()),
                              defaultString(error.getActivityName())).inc();
    }
}
