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

package org.kie.server.integrationtests.jbpm;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;
import org.kie.api.KieServices;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.instance.TaskSummary;
import org.kie.server.integrationtests.config.TestConfig;
import org.kie.server.integrationtests.shared.KieServerDeployer;

public class UserTaskServiceAdditionalIntegrationTest extends JbpmKieServerBaseIntegrationTest {

    private static ReleaseId releaseId = new ReleaseId("org.kie.server.testing", "usertask-project",
            "1.0.0.Final");

    @BeforeClass
    public static void buildAndDeployArtifacts() {

        KieServerDeployer.buildAndDeployCommonMavenParent();
        KieServerDeployer.buildAndDeployMavenProjectFromResource("/kjars-sources/usertask-project");

        kieContainer = KieServices.Factory.get().newKieContainer(releaseId);

        createContainer(CONTAINER_ID_USERTASK, releaseId);
    }

    @Test
    public void testNotAllowedUserTaskUpdateOutputVariable() throws Exception {
        Long processInstanceId = processClient.startProcess(CONTAINER_ID_USERTASK, PROCESS_ID_USERTASK_DIFF_POTUSERS);
        assertNotNull(processInstanceId);
        assertTrue(processInstanceId.longValue() > 0);

        try {
            changeUser(USER_JOHN);
            List<TaskSummary> taskList = taskClient.findTasksAssignedAsPotentialOwner(USER_JOHN, 0, 10);
            assertNotNull(taskList);

            assertEquals(1, taskList.size());
            TaskSummary taskSummary = taskList.get(0);
            
            taskClient.startTask(CONTAINER_ID_USERTASK, taskSummary.getId(), USER_JOHN);

            Map<String, Object> taskOutcomeComplete = new HashMap<>();
            taskClient.completeTask(CONTAINER_ID_USERTASK, taskSummary.getId(), USER_JOHN, taskOutcomeComplete);

            changeUser(USER_YODA);
            taskList = taskClient.findTasksAssignedAsPotentialOwner(USER_YODA, 0, 10);
            assertNotNull(taskList);
            assertEquals(1, taskList.size());
            TaskSummary task2 = taskList.get(0);
            
            taskClient.startTask(CONTAINER_ID_USERTASK, task2.getId(), USER_YODA);

            changeUser(USER_JOHN);
            assertClientException(
                    () -> taskClient.saveTaskContent(CONTAINER_ID_USERTASK, task2.getId(),  taskOutcomeComplete),
                    403,
                    "User '[UserImpl:'"+ USER_JOHN +"']' does not have permissions to execute operation 'Modify' on task id "+ task2.getId());

        } finally {
            changeUser(TestConfig.getUsername());
            processClient.abortProcessInstance(CONTAINER_ID_USERTASK, processInstanceId);
        }
    }
}
