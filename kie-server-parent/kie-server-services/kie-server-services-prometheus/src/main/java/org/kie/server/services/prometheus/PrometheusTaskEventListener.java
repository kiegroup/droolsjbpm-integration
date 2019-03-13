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

import java.util.Date;

import io.prometheus.client.Counter;
import io.prometheus.client.Summary;
import org.jbpm.services.task.events.DefaultTaskEventListener;
import org.kie.api.task.TaskEvent;
import org.kie.api.task.model.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.kie.server.services.prometheus.PrometheusMetrics.millisToSeconds;

public class PrometheusTaskEventListener extends DefaultTaskEventListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(PrometheusTaskEventListener.class);

    private static final String[] TASK_LABELS = {"deployment_id", "process_id", "task_name"};

    private static final Counter numberOfTasksAdded = Counter.build()
            .name("kie_server_task_added_total")
            .help("Kie Server Added Tasks")
            .labelNames(TASK_LABELS)
            .register();

    private static final Counter numberOfTasksSkipped = Counter.build()
            .name("kie_server_task_skipped_total")
            .help("Kie Server Skipped Tasks")
            .labelNames(TASK_LABELS)
            .register();

    private static final Counter numberOfTasksCompleted = Counter.build()
            .name("kie_server_task_completed_total")
            .help("Kie Server Completed Tasks")
            .labelNames(TASK_LABELS)
            .register();

    private static final Counter numberOfTasksFailed = Counter.build()
            .name("kie_server_task_failed_total")
            .help("Kie Server Failed Tasks")
            .labelNames(TASK_LABELS)
            .register();

    private static final Counter numberOfTasksExited = Counter.build()
            .name("kie_server_task_exited_total")
            .help("Kie Server Exited Tasks")
            .labelNames(TASK_LABELS)
            .register();

    private static final Summary taskDuration = Summary.build()
            .name("kie_server_task_duration_seconds")
            .help("Kie Server Task Duration")
            .labelNames(TASK_LABELS)
            .register();

    private void registerCounterEvent(Counter counter, TaskEvent event) {
        final Task task = event.getTask();
        counter.labels(task.getTaskData().getDeploymentId(), task.getTaskData().getProcessId(), task.getName()).inc();
    }

    private void registerDuration(TaskEvent event) {
        final Task task = event.getTask();
        final Date createdOn = task.getTaskData().getCreatedOn();
        if (createdOn != null) {
            final double duration = millisToSeconds(System.currentTimeMillis() - createdOn.getTime());
            taskDuration.labels(task.getTaskData().getDeploymentId(), task.getTaskData().getProcessId(), task.getName()).observe(duration);
        }
    }

    @Override
    public void afterTaskAddedEvent(TaskEvent event) {
        LOGGER.debug("After Task Added event: {}", event);
        registerCounterEvent(numberOfTasksAdded, event);
    }

    @Override
    public void afterTaskSkippedEvent(TaskEvent event) {
        LOGGER.debug("After Task Skipped event: {}", event);
        registerCounterEvent(numberOfTasksSkipped, event);
    }

    @Override
    public void afterTaskCompletedEvent(TaskEvent event) {
        LOGGER.debug("After Task Completed event: {}", event);
        registerCounterEvent(numberOfTasksCompleted, event);
        registerDuration(event);
    }

    @Override
    public void afterTaskFailedEvent(TaskEvent event) {
        LOGGER.debug("After Task Failed event: {}", event);
        registerCounterEvent(numberOfTasksFailed, event);
        registerDuration(event);
    }

    @Override
    public void afterTaskExitedEvent(TaskEvent event) {
        LOGGER.debug("After Task Exited event: {}", event);
        registerCounterEvent(numberOfTasksExited, event);
        registerDuration(event);
    }
}
