/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
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
package org.kie.server.ext.prometheus;

import io.prometheus.client.Gauge;
import org.jbpm.executor.AsynchronousJobEvent;
import org.jbpm.executor.AsynchronousJobListener;

public class JobsCustomPrometheusMetricListener implements AsynchronousJobListener {

    private static final Gauge randomGauge = Gauge.build()
                                                  .name("random_gauge_jobs_scheduled_count")
                                                  .help("Random gauge as an example of custom KIE Prometheus metric")
                                                  .register();

    @Override
    public void beforeJobScheduled(AsynchronousJobEvent event) {
        randomGauge.inc();
    }

    @Override
    public void afterJobScheduled(AsynchronousJobEvent event) {
        // Not measured
    }

    @Override
    public void beforeJobExecuted(AsynchronousJobEvent event) {
        // Not measured
    }

    @Override
    public void afterJobExecuted(AsynchronousJobEvent event) {
        // Not measured
    }

    @Override
    public void beforeJobCancelled(AsynchronousJobEvent event) {
        // Not measured
    }

    @Override
    public void afterJobCancelled(AsynchronousJobEvent event) {
        // Not measured
    }
}