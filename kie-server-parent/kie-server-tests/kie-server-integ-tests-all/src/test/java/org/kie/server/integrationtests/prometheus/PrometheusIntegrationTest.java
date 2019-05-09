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
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import org.junit.experimental.categories.Category;
import org.kie.api.task.model.Status;
import org.kie.internal.executor.api.STATUS;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.cases.CaseFile;
import org.kie.server.api.model.instance.JobRequestInstance;
import org.kie.server.api.model.instance.ProcessInstance;
import org.kie.server.api.model.instance.TaskSummary;
import org.kie.server.integrationtests.category.JEEOnly;
import org.kie.server.integrationtests.config.TestConfig;
import org.kie.server.integrationtests.jbpm.JbpmKieServerBaseIntegrationTest;
import org.kie.server.integrationtests.shared.KieServerDeployer;
import org.kie.server.integrationtests.shared.KieServerSynchronization;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.kie.api.runtime.process.ProcessInstance.STATE_ABORTED;
import static org.kie.api.runtime.process.ProcessInstance.STATE_COMPLETED;

public class PrometheusIntegrationTest extends JbpmKieServerBaseIntegrationTest {

    private static final String CONTAINER_ID = "prometheus";
    private static final String CONTAINER_ID_CASE = "prometheus-case";
    private static final String PROCESS_ID = "per-process-instance-project.usertask";
    private static final String USER_ID = "yoda";

    private static final String CASE_DEF_ID = "UserTaskCase";
    private static final String CASE_OWNER_ROLE = "owner";
    private static final String CASE_CONTACT_ROLE = "contact";

    protected static final String BUSINESS_KEY = "test key";
    protected static final String PRINT_OUT_COMMAND = "org.jbpm.executor.commands.PrintOutCommand";

    private static ReleaseId releaseId = new ReleaseId("org.kie.server.testing", "per-process-instance-project", "1.0.0.Final");
    private static ReleaseId caseReleaseId = new ReleaseId("org.kie.server.testing", "case-insurance", "1.0.0.Final");
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
        KieServerDeployer.buildAndDeployMavenProjectFromResource("/kjars-sources/case-insurance");

        createContainer(CONTAINER_ID, releaseId);
        createContainer(CONTAINER_ID_CASE, caseReleaseId);
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

    @Test
    public void testPrometheusCaseMetrics() {
        String caseId = startUserTaskCase(USER_YODA, USER_JOHN);
        assertThat(caseId).isNotEmpty();

        assertThat(getMetrics()).contains(
              "kie_server_case_started_total{case_definition_id=\"" + CASE_DEF_ID + "\",",
              "kie_server_case_running_total{case_definition_id=\"" + CASE_DEF_ID + "\","
        );

        caseClient.cancelCaseInstance(CONTAINER_ID_CASE, caseId);

        assertThat(getMetrics()).contains(
              "kie_server_case_started_total{case_definition_id=\"" + CASE_DEF_ID + "\",",
              "kie_server_case_running_total{case_definition_id=\"" + CASE_DEF_ID + "\",",
              "kie_server_case_duration_seconds_count{case_definition_id=\"" + CASE_DEF_ID + "\",",
              "kie_server_case_duration_seconds_sum{case_definition_id=\"" + CASE_DEF_ID + "\","
        );
    }

    @Test
    @Category(JEEOnly.class) // Executor in kie-server-integ-tests-all is using JMS for execution. Skipping test for non JEE containers as they don't have JMS.
    public void testPrometheusJobMetrics() throws Exception {
        int currentNumberOfCancelled = jobServicesClient.getRequestsByStatus(Collections.singletonList(STATUS.CANCELLED.toString()), 0, 1000).size();
        int currentNumberOfDone = jobServicesClient.getRequestsByStatus(Collections.singletonList(STATUS.DONE.toString()), 0, 1000).size();

        Instant tomorrow = Instant.now().plus(1, ChronoUnit.DAYS);

        JobRequestInstance jobRequestInstanceTomorrow = createJobRequestInstance();
        jobRequestInstanceTomorrow.setScheduledDate(Date.from(tomorrow));
        Long jobIdTomorrow = jobServicesClient.scheduleRequest(jobRequestInstanceTomorrow);
        jobServicesClient.cancelRequest(jobIdTomorrow);

        JobRequestInstance jobRequestInstanceNow = createJobRequestInstance();
        Long jobIdNow = jobServicesClient.scheduleRequest(jobRequestInstanceNow);
        KieServerSynchronization.waitForJobToFinish(jobServicesClient, jobIdNow);

        assertThat(getMetrics()).contains(
              "kie_server_job_scheduled_total{container_id=\"\",command_name=\"" + PRINT_OUT_COMMAND + "\",}",
              "kie_server_job_cancelled_total{container_id=\"\",command_name=\"" + PRINT_OUT_COMMAND + "\",} " + (currentNumberOfCancelled + 1)
              // Uncomment when JBPM-8452 is resolved.
//              "kie_server_job_executed_total{container_id=\"\",failed=\"false\",command_name=\"" + PRINT_OUT_COMMAND + "\",} " + (currentNumberOfDone + 1),
//              "kie_server_job_running_total{container_id=\"\",command_name=\"" + PRINT_OUT_COMMAND + "\",}",
//              "kie_server_job_duration_seconds_count{container_id=\"\",command_name=\"" + PRINT_OUT_COMMAND + "\",}",
//              "kie_server_job_duration_seconds_sum{container_id=\"\",command_name=\"" + PRINT_OUT_COMMAND + "\",}"
          );
    }

    private String startUserTaskCase(String owner, String contact) {
        Map<String, Object> data = new HashMap<>();
        data.put("s", "first case started");
        CaseFile caseFile = CaseFile.builder()
                .addUserAssignments(CASE_OWNER_ROLE, owner)
                .addUserAssignments(CASE_CONTACT_ROLE, contact)
                .data(data)
                .build();

        String caseId = caseClient.startCase(CONTAINER_ID_CASE, CASE_DEF_ID, caseFile);
        assertNotNull(caseId);
        return caseId;
    }

    private JobRequestInstance createJobRequestInstance() {
        Map<String, Object> data = new HashMap<>();
        data.put("businessKey", BUSINESS_KEY);

        JobRequestInstance jobRequestInstance = new JobRequestInstance();
        jobRequestInstance.setCommand(PRINT_OUT_COMMAND);
        jobRequestInstance.setData(data);
        return jobRequestInstance;
    }
}
