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

package org.kie.server.integrationtests.prometheus;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Stream;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.StringUtils;
import org.jboss.resteasy.client.jaxrs.BasicAuthentication;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kie.api.task.model.Status;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.instance.ProcessInstance;
import org.kie.server.api.model.instance.TaskSummary;
import org.kie.server.integrationtests.config.TestConfig;
import org.kie.server.integrationtests.jbpm.JbpmKieServerBaseIntegrationTest;
import org.kie.server.integrationtests.shared.KieServerDeployer;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.kie.api.runtime.process.ProcessInstance.STATE_ABORTED;
import static org.kie.api.runtime.process.ProcessInstance.STATE_COMPLETED;

public class PrometheusIntegrationTest extends JbpmKieServerBaseIntegrationTest {

    private static final String CONTAINER_ID = "prometheus";
    private static final String PROCESS_ID = "per-process-instance-project.usertask";
    private static final String USER_ID = "yoda";
    private static ReleaseId releaseId = new ReleaseId("org.kie.server.testing", "per-process-instance-project", "1.0.0.Final");
    private static Client httpClient;

    @AfterClass
    public static void closeHttpClient() {
        if (httpClient != null) {
            httpClient.close();
            httpClient = null;
        }
    }

    @BeforeClass
    public static void buildAndDeployArtifacts() {
        KieServerDeployer.buildAndDeployCommonMavenParent();
        KieServerDeployer.buildAndDeployMavenProjectFromResource("/kjars-sources/per-process-instance-project");

        createContainer(CONTAINER_ID, releaseId);
    }

    protected String getMetrics() {
        if (httpClient == null) {
            httpClient = new ResteasyClientBuilder().readTimeout(10, TimeUnit.SECONDS).build();
        }

        WebTarget webTarget = httpClient.target(URI.create(TestConfig.getKieServerHttpUrl()).resolve("../rest/metrics"));
        webTarget.register(new BasicAuthentication(TestConfig.getUsername(), TestConfig.getPassword()));

        try (Response response = webTarget.request(MediaType.TEXT_PLAIN).get()) {
            assertEquals(200, response.getStatus());
            return response.readEntity(String.class);
        }
    }

    @Test
    public void testKieServerStartAndContainersMetrics() {
        final String serverName = client.getServerInfo().getResult().getName();
        assertThat(getMetrics()).contains(
                "kie_server_start_time{name=\"" + serverName + "\",",
                "kie_server_deployments_active_total{deployment_id=\"prometheus\",} 1.0",
                "kie_server_container_started_total{container_id=\"prometheus\",} 1.0",
                "kie_server_container_running_total{container_id=\"prometheus\",} 1.0"
        );
    }

    @Test
    public void testPrometheusProcessAndTaskMetrics() {
        processClient.startProcess(CONTAINER_ID, PROCESS_ID);
        Long instanceId = processClient.startProcess(CONTAINER_ID, PROCESS_ID);
        processClient.abortProcessInstance(CONTAINER_ID, instanceId);

        taskClient.findTasksAssignedAsPotentialOwner(USER_ID, 0, 100).forEach(task -> {
            taskClient.startTask(CONTAINER_ID, task.getId(), USER_ID);
            taskClient.completeTask(CONTAINER_ID, task.getId(), USER_ID, null);
        });

        List<ProcessInstance> instances = queryClient.findProcessInstancesByProcessId(PROCESS_ID, Arrays.asList(STATE_COMPLETED, STATE_ABORTED), 0, 100);

        int totalInstances = instances.size();
        long completedInstances = instances.stream().filter(pi -> pi.getState() == STATE_COMPLETED).count();
        long abortedInstances = instances.stream().filter(pi -> pi.getState() == STATE_ABORTED).count();

        final List<TaskSummary> tasks = taskClient.findTasks(USER_ID, 0, 1000);
        final Supplier<Stream<TaskSummary>> taskSummaryStream = () -> tasks.stream().filter(t -> "First task".equals(t.getName()) && CONTAINER_ID.equals(t.getContainerId()) && StringUtils.equalsAny(t.getStatus(), Status.Completed.toString(), Status.Exited.toString()));
        long totalTasks = taskSummaryStream.get().count();
        long completedTasks = taskSummaryStream.get().filter(t -> Status.Completed.toString().equals(t.getStatus())).count();
        long exitedTasks = taskSummaryStream.get().filter(t -> Status.Exited.toString().equals(t.getStatus())).count();

        assertThat(getMetrics()).contains(
                format("kie_server_process_instance_started_total{container_id=\"prometheus\",process_id=\"per-process-instance-project.usertask\",} %d.0", totalInstances),
                format("kie_server_process_instance_completed_total{container_id=\"prometheus\",process_id=\"per-process-instance-project.usertask\",status=\"2\",} %d.0", completedInstances),
                format("kie_server_process_instance_completed_total{container_id=\"prometheus\",process_id=\"per-process-instance-project.usertask\",status=\"3\",} %d.0", abortedInstances),
                format("kie_server_process_instance_duration_seconds_count{container_id=\"prometheus\",process_id=\"per-process-instance-project.usertask\",} %d.0", totalInstances),
                "kie_server_process_instance_duration_seconds_sum{container_id=\"prometheus\",process_id=\"per-process-instance-project.usertask\",}",
                "kie_server_process_instance_running_total{container_id=\"prometheus\",process_id=\"per-process-instance-project.usertask\",} 0.0",
                "kie_server_work_item_duration_seconds_count{name=\"Human Task\",}",
                "kie_server_work_item_duration_seconds_sum{name=\"Human Task\",}",
                format("kie_server_task_added_total{deployment_id=\"prometheus\",process_id=\"per-process-instance-project.usertask\",task_name=\"First task\",} %d.0", totalTasks),
                format("kie_server_task_completed_total{deployment_id=\"prometheus\",process_id=\"per-process-instance-project.usertask\",task_name=\"First task\",} %d.0", completedTasks),
                format("kie_server_task_duration_seconds_count{deployment_id=\"prometheus\",process_id=\"per-process-instance-project.usertask\",task_name=\"First task\",} %d.0", totalTasks),
                "kie_server_task_duration_seconds_sum{deployment_id=\"prometheus\",process_id=\"per-process-instance-project.usertask\",task_name=\"First task\",}",
                format("kie_server_task_exited_total{deployment_id=\"prometheus\",process_id=\"per-process-instance-project.usertask\",task_name=\"First task\",} %d.0", exitedTasks)
        );
    }
}
