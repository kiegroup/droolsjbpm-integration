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
import org.junit.experimental.categories.Category;
import org.kie.api.KieServices;
import org.kie.api.task.model.Status;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.instance.TaskEventInstance;
import org.kie.server.api.model.instance.TaskInstance;
import org.kie.server.api.model.instance.TaskSummary;
import org.kie.server.integrationtests.category.Smoke;
import org.kie.server.integrationtests.shared.KieServerDeployer;
import org.kie.server.integrationtests.shared.KieServerReflections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class ByPassUserTaskServiceIntegrationTest extends JbpmKieServerBaseIntegrationTest {

    private static ReleaseId releaseId = new ReleaseId("org.kie.server.testing", "definition-project",
            "1.0.0.Final");

    @BeforeClass
    public static void buildAndDeployArtifacts() {

        KieServerDeployer.buildAndDeployCommonMavenParent();
        KieServerDeployer.buildAndDeployMavenProjectFromResource("/kjars-sources/definition-project");


        kieContainer = KieServices.Factory.get().newKieContainer(releaseId);

        createContainer(CONTAINER_ID, releaseId);
    }

    @Override
    protected void addExtraCustomClasses(Map<String, Class<?>> extraClasses) throws Exception {
        extraClasses.put(PERSON_CLASS_NAME, Class.forName(PERSON_CLASS_NAME, true, kieContainer.getClassLoader()));
    }

    @Test
    @Category(Smoke.class)
    public void testProcessWithUserTasks() throws Exception {
        Long processInstanceId = processClient.startProcess(CONTAINER_ID, PROCESS_ID_USERTASK);
        assertNotNull(processInstanceId);
        assertTrue(processInstanceId.longValue() > 0);
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
            taskOutcome.put("person_", createPersonInstance(USER_MARY));

            Map<String, Object> content = new HashMap<>();
            content.put("name", "joda");
            taskClient.saveTaskContent(CONTAINER_ID, taskSummary.getId(), "admin", content);
            List<TaskEventInstance> events = taskClient.findTaskEvents(taskSummary.getId(), 0, 10);
            assertEquals("admin", events.get(events.size() - 1).getUserId());

            taskClient.saveTaskContent(CONTAINER_ID, taskSummary.getId(), "other", content);
            events = taskClient.findTaskEvents(taskSummary.getId(), 0, 10);
            assertEquals("other", events.get(events.size() - 1).getUserId());

            taskClient.completeTask(CONTAINER_ID, taskSummary.getId(), USER_YODA, taskOutcome);

            // check if task outcomes are properly set as process variables
            Object personVar = processClient.getProcessInstanceVariable(CONTAINER_ID, processInstanceId, "personData");
            assertNotNull(personVar);
            assertEquals(USER_MARY, KieServerReflections.valueOf(personVar, "name"));

            String stringVar = (String) processClient.getProcessInstanceVariable(CONTAINER_ID, processInstanceId, "stringData");
            assertNotNull(personVar);
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
    
    
    
    private void assertTaskEventInstance(TaskEventInstance expected, TaskEventInstance actual) {
        assertNotNull(actual);
        assertEquals(expected.getType(), actual.getType());
        assertEquals(expected.getProcessInstanceId(), actual.getProcessInstanceId());
        assertEquals(expected.getTaskId(), actual.getTaskId());
        assertEquals(expected.getUserId(), actual.getUserId());
        assertNotNull(actual.getId());
        assertNotNull(actual.getLogTime());
        assertNotNull(actual.getWorkItemId());
    }

    private void checkTaskNameAndStatus(TaskSummary taskSummary, String name, Status status) {
        assertNotNull(taskSummary);
        assertEquals(name, taskSummary.getName());
        assertEquals(status.toString(), taskSummary.getStatus());
    }

    private void checkTaskStatusAndOwners(String containerId, Long taskId, Status status, String actualOwner, String potentialOwner) {
        TaskInstance task = taskClient.getTaskInstance(containerId, taskId, false, false, true);
        checkTaskStatusAndActualOwner(containerId, taskId, status, actualOwner);
        assertEquals(1, task.getPotentialOwners().size());
        assertEquals(potentialOwner, task.getPotentialOwners().get(0));
    }

    private void checkTaskStatusAndActualOwner(String containerId, Long taskId, Status status, String actualOwner) {
        TaskInstance task = taskClient.getTaskInstance(containerId, taskId);
        assertEquals(taskId, task.getId());
        assertEquals(status.toString(), task.getStatus());
        assertEquals(actualOwner, task.getActualOwner());
    }
}
