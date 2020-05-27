/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
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

package org.kie.server.integrationtests.taskassigning;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import org.junit.BeforeClass;
import org.junit.Test;
import org.kie.server.api.model.instance.ProcessInstance;
import org.kie.server.api.model.instance.TaskSummary;
import org.kie.server.api.marshalling.MarshallingFormat;
import org.kie.server.api.model.KieContainerResource;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.client.KieServicesClient;
import org.kie.server.client.KieServicesConfiguration;
import org.kie.server.client.KieServicesFactory;
import org.kie.server.client.ProcessServicesClient;
import org.kie.server.client.UserTaskServicesClient;
import org.kie.server.integrationtests.config.TestConfig;
import org.kie.server.integrationtests.shared.KieServerAssert;
import org.kie.server.integrationtests.shared.KieServerDeployer;
import org.kie.server.integrationtests.shared.basetests.KieServerBaseIntegrationTest;

import static org.assertj.core.api.Assertions.assertThat;

public class TaskAssigningIntegrationTest extends KieServerBaseIntegrationTest {

    private static final ReleaseId TEST_KJAR = new ReleaseId("org.kie.server.testing", "task-assigning", "1.0.0.Final");
    private static final String CONTAINER_ID = "task-assigning";
    private static final String PROCESS_ID = "org.kie.server.CreditDispute";
    private static final int ASSIGNMENT_TIMEOUT_SECONDS = 600;

    private final KieServicesClient kieServicesClient = createDefaultClient();
    private final ProcessServicesClient processClient = kieServicesClient.getServicesClient(ProcessServicesClient.class);
    private final UserTaskServicesClient taskClient = kieServicesClient.getServicesClient(UserTaskServicesClient.class);

    @BeforeClass
    public static void deployArtifacts() {
        KieServerDeployer.buildAndDeployCommonMavenParent();
        KieServerDeployer.buildAndDeployMavenProjectFromResource("/kjars-sources/task-assigning");

        KieServicesClient staticKieServicesClient = createDefaultStaticClient();
        ServiceResponse<KieContainerResource> reply = staticKieServicesClient
                .createContainer(CONTAINER_ID, new KieContainerResource(CONTAINER_ID, TEST_KJAR));
        KieServerAssert.assertSuccess(reply);
    }

    public TaskAssigningIntegrationTest() throws Exception {
        // nothing to do here
    }

    @Test
    public void task_assigning_with_skills() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("cardType", "VISA");
        parameters.put("language", "ZH");
        Long processInstanceId = processClient.startProcess(CONTAINER_ID, PROCESS_ID, parameters);

        // maciek is in the CreditAnalyst group and has a VISA skill => should be assigned to the ResolveDispute task.
        doNextTaskByUser("maciek");

        // mary is in the ClientRelations group and has a ZH (Chinese) skill => should be assigned to the NotifyCustomer task.
        doNextTaskByUser("mary");

        ProcessInstance processInstance = processClient.getProcessInstance(CONTAINER_ID, processInstanceId);
        assertThat(processInstance).isNotNull();
        assertThat(processInstance.getState().intValue())
                .isEqualTo(org.kie.api.runtime.process.ProcessInstance.STATE_COMPLETED);
    }

    private void doNextTaskByUser(String userId) {
        List<TaskSummary> tasks = waitForTasks(() -> taskClient.findTasks(userId, 0, 1), ASSIGNMENT_TIMEOUT_SECONDS);
        assertThat(tasks).hasSize(1);
        final long resolveDisputeTaskId = tasks.get(0).getId();
        taskClient.startTask(CONTAINER_ID, resolveDisputeTaskId, userId);
        taskClient.completeTask(CONTAINER_ID, resolveDisputeTaskId, userId, Collections.emptyMap());
    }

    private List<TaskSummary> waitForTasks(Supplier<List<TaskSummary>> taskProducer, int timeoutSeconds) {
        if (timeoutSeconds < 1) {
            throw new IllegalArgumentException("TimeoutSeconds must be a positive integer.");
        }
        final long TIME_STEP_MILLIS = 500L;
        long timeSpentMillis = 0L;
        List<TaskSummary> tasks;
        while ((tasks = taskProducer.get()).isEmpty()) {
            if (timeSpentMillis > timeoutSeconds * 1000) {
                throw new RuntimeException("No task retrieved in " + timeoutSeconds + " seconds.");
            }
            try {
                Thread.sleep(TIME_STEP_MILLIS);
                timeSpentMillis += TIME_STEP_MILLIS;
            } catch (InterruptedException e) {
                throw new RuntimeException("Interrupted waiting.", e);
            }
        }

        return tasks;
    }

    @Override
    protected KieServicesClient createDefaultClient() throws Exception {
        addExtraCustomClasses(extraClasses);
        KieServicesConfiguration configuration = TestConfig.isLocalServer() ?
             KieServicesFactory.newRestConfiguration(TestConfig.getKieServerHttpUrl(), null, null)
                : KieServicesFactory.newRestConfiguration(TestConfig.getKieServerHttpUrl(), TestConfig.getUsername(),
                TestConfig.getPassword());
        // the marshalling format does not matter for this test
        return createDefaultClient(configuration, MarshallingFormat.JSON);
    }
}
