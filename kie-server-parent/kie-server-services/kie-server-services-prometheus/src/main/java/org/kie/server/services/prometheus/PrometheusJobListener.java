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
import io.prometheus.client.Summary;
import org.jbpm.executor.AsynchronousJobEvent;
import org.jbpm.executor.AsynchronousJobListener;
import org.kie.api.executor.RequestInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.apache.commons.lang3.StringUtils.defaultString;
import static org.kie.server.services.prometheus.PrometheusMetrics.millisToSeconds;

public class PrometheusJobListener implements AsynchronousJobListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(PrometheusJobListener.class);

    protected static final Gauge numberOfRunningJobs = Gauge.build()
            .name("kie_server_job_running_total")
            .help("Kie Server Running Jobs")
            .labelNames("container_id", "command_name")
            .register();

    protected static final Counter numberOfJobsScheduled = Counter.build()
            .name("kie_server_job_scheduled_total")
            .help("Kie Server Started Jobs")
            .labelNames("container_id", "command_name")
            .register();

    protected static final Counter numberOfJobsExecuted = Counter.build()
            .name("kie_server_job_executed_total")
            .help("Kie Server Executed Jobs")
            .labelNames("container_id", "failed", "command_name")
            .register();

    protected static final Counter numberOfJobsCancelled = Counter.build()
            .name("kie_server_job_cancelled_total")
            .help("Kie Server Cancelled Jobs")
            .labelNames("container_id", "command_name")
            .register();

    protected static final Summary jobDuration = Summary.build()
            .name("kie_server_job_duration_seconds")
            .help("Kie Server Job Duration")
            .labelNames("container_id", "command_name")
            .register();

    @Override
    public void beforeJobScheduled(AsynchronousJobEvent event) {

    }

    @Override
    public void afterJobScheduled(AsynchronousJobEvent event) {
        LOGGER.debug("After job scheduled event: {}", event);
        final RequestInfo job = event.getJob();
        numberOfJobsScheduled.labels(defaultString(job.getDeploymentId()), job.getCommandName()).inc();
    }

    @Override
    public void beforeJobExecuted(AsynchronousJobEvent event) {
        LOGGER.debug("Before job executed event: {}", event);
        final RequestInfo job = event.getJob();
        numberOfRunningJobs.labels(defaultString(job.getDeploymentId()), job.getCommandName()).inc();
    }

    @Override
    public void afterJobExecuted(AsynchronousJobEvent event) {
        LOGGER.debug("After job executed event: {}", event);
        final RequestInfo job = event.getJob();
        numberOfJobsExecuted.labels(defaultString(job.getDeploymentId()), String.valueOf(event.failed()), job.getCommandName()).inc();
        numberOfRunningJobs.labels(defaultString(job.getDeploymentId()), job.getCommandName()).dec();
        if(job.getTime() != null) {
            final double duration = millisToSeconds(System.currentTimeMillis() - job.getTime().getTime());
            LOGGER.debug("Job duration: {}s", duration);
            jobDuration.labels(defaultString(job.getDeploymentId()), job.getCommandName()).observe(duration);
        }
    }

    @Override
    public void beforeJobCancelled(AsynchronousJobEvent event) {

    }

    @Override
    public void afterJobCancelled(AsynchronousJobEvent event) {
        LOGGER.debug("After job cancelled event: {}", event);
        final RequestInfo job = event.getJob();
        numberOfJobsCancelled.labels(defaultString(job.getDeploymentId()), job.getCommandName()).inc();
    }
}
