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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;
import org.kie.api.KieServices;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.instance.TaskEventInstance;
import org.kie.server.api.model.instance.TaskSummary;
import org.kie.server.integrationtests.shared.KieServerDeployer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class ByPassUserTaskServiceIntegrationTest extends JbpmKieServerBaseIntegrationTest {

    private static ReleaseId releaseId = new ReleaseId("org.kie.server.testing", "bypasscredentials-project", "1.0.0.Final");

    @BeforeClass
    public static void buildAndDeployArtifacts() {

        KieServerDeployer.buildAndDeployCommonMavenParent();
        KieServerDeployer.buildAndDeployMavenProjectFromResource("/kjars-sources/bypasscredentials-project");


        kieContainer = KieServices.Factory.get().newKieContainer(releaseId);

        createContainer(CONTAINER_ID, releaseId);
    }

    @Test
    public void testProcessWithUserTasks() {
        Long processInstanceId = processClient.startProcess(CONTAINER_ID, PROCESS_ID_USERTASK2);
        assertNotNull(processInstanceId);
        assertTrue(processInstanceId > 0);
        try {
            List<TaskSummary> taskList = taskClient.findTasksAssignedAsPotentialOwner(USER_YODA, 0, 10);
            assertNotNull(taskList);

            assertEquals(1, taskList.size());
            TaskSummary taskSummary = taskList.get(0);
            assertEquals("First task", taskSummary.getName());

            // startTask and completeTask task
            taskClient.startTask(CONTAINER_ID, taskSummary.getId(), USER_YODA);

            Map<String, Object> taskOutcome = new HashMap<String, Object>();
            taskOutcome.put("string_", "my custom data");

            Map<String, Object> content = new HashMap<>();
            content.put("name", "joda");
            taskClient.saveTaskContent(CONTAINER_ID, taskSummary.getId(), "admin", content);
            List<TaskEventInstance> events = taskClient.findTaskEvents(taskSummary.getId(), 0, 10);
            assertEquals("admin", events.get(events.size() - 1).getUserId());

            taskClient.saveTaskContent(CONTAINER_ID, taskSummary.getId(), "other", content);
            events = taskClient.findTaskEvents(taskSummary.getId(), 0, 10);
            assertEquals("other", events.get(events.size() - 1).getUserId());

            taskClient.completeTask(CONTAINER_ID, taskSummary.getId(), USER_YODA, taskOutcome);

            String stringVar = (String) processClient.getProcessInstanceVariable(CONTAINER_ID, processInstanceId, "stringData");
            assertNotNull(stringVar);
            assertEquals("my custom data", stringVar);

            taskList = taskClient.findTasksAssignedAsPotentialOwner(USER_YODA, 0, 10);
            assertNotNull(taskList);

            assertEquals(1, taskList.size());
            taskSummary = taskList.get(0);
            assertEquals("Second task", taskSummary.getName());

        } finally {
            processClient.abortProcessInstance(CONTAINER_ID, processInstanceId);
        }
    }

}
