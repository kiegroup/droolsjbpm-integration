/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.Response;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.kie.api.KieServices;
import org.kie.api.task.model.Status;
import org.kie.internal.task.api.model.TaskEvent;
import org.kie.server.api.exception.KieServicesException;
import org.kie.server.api.exception.KieServicesHttpException;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.instance.TaskAttachment;
import org.kie.server.api.model.instance.TaskComment;
import org.kie.server.api.model.instance.TaskEventInstance;
import org.kie.server.api.model.instance.TaskInstance;
import org.kie.server.api.model.instance.TaskSummary;
import org.kie.server.client.impl.AbstractKieServicesClientImpl;
import org.kie.server.integrationtests.category.Smoke;
import org.kie.server.integrationtests.config.TestConfig;
import org.kie.server.integrationtests.shared.KieServerAssert;
import org.kie.server.integrationtests.shared.KieServerDeployer;
import org.kie.server.integrationtests.shared.KieServerReflections;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeFalse;
import static org.kie.server.integrationtests.jbpm.RuntimeDataServiceIntegrationTest.SORT_BY_TASK_EVENTS_TYPE;

public class UserTaskServiceIntegrationTest extends JbpmKieServerBaseIntegrationTest {

    private static ReleaseId releaseId = new ReleaseId("org.kie.server.testing", "definition-project",
            "1.0.0.Final");

    private static final String USER_WITHOUT_PERMISSIONS = "noPermissionsUser";
    
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

    @Test
    public void testCompleteAlreadyCompletedUserTask() throws Exception {
        Map<String, Object> variables = new HashMap<String, Object>();
        variables.put("car", "BMW");

        Long processInstanceId = processClient.startProcess(CONTAINER_ID, "test1.process1", variables);
        assertNotNull(processInstanceId);
        assertTrue(processInstanceId.longValue() > 0);

        List<TaskSummary> taskList = taskClient.findTasksAssignedAsPotentialOwner(USER_YODA, 0, 10);
        assertNotNull(taskList);
        assertEquals(1, taskList.size());

        Long taskId = taskList.get(0).getId();

        // startTask and completeTask task
        taskClient.startTask(CONTAINER_ID, taskId, USER_YODA);

        Map<String, Object> taskOutcome = new HashMap<String, Object>();
        taskOutcome.put("carOutput", "Skoda");

        taskClient.completeTask(CONTAINER_ID, taskId, USER_YODA, taskOutcome);
        
        try {
            taskClient.completeTask(CONTAINER_ID, taskId, USER_YODA, taskOutcome);
           fail("Completing of already completed task should throw an exception.");
        } catch(KieServicesHttpException e) {
           assertEquals(Response.Status.FORBIDDEN.getStatusCode(), e.getHttpCode().intValue());
           // expected for REST
        } catch(KieServicesException e) {
           // expected for JMS
        }
    }

    @Test
    public void testAutoProgressComplete() throws Exception {
        Long processInstanceId = processClient.startProcess(CONTAINER_ID, PROCESS_ID_USERTASK);
        assertNotNull(processInstanceId);
        assertTrue(processInstanceId.longValue() > 0);
        try {
            List<TaskSummary> taskList = taskClient.findTasksAssignedAsPotentialOwner(USER_YODA, 0, 10);
            assertNotNull(taskList);

            assertEquals(1, taskList.size());
            TaskSummary taskSummary = taskList.get(0);
            assertEquals("First task", taskSummary.getName());

            // release task
            taskClient.releaseTask(CONTAINER_ID, taskSummary.getId(), USER_YODA);

            Map<String, Object> taskOutcome = new HashMap<String, Object>();
            taskOutcome.put("string_", "my custom data");
            taskOutcome.put("person_", createPersonInstance(USER_MARY));

            // do all operations, claim -> start -> complete, at once
            taskClient.completeAutoProgress(CONTAINER_ID, taskSummary.getId(), USER_YODA, taskOutcome);

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

    @Test
    public void testReleaseAndClaim() throws Exception {
        Long processInstanceId = processClient.startProcess(CONTAINER_ID, PROCESS_ID_USERTASK);
        assertNotNull(processInstanceId);
        assertTrue(processInstanceId.longValue() > 0);
        try {
            List<TaskSummary> taskList = taskClient.findTasksAssignedAsPotentialOwner(USER_YODA, 0, 10);
            assertNotNull(taskList);

            assertEquals(1, taskList.size());
            TaskSummary taskSummary = taskList.get(0);
            checkTaskNameAndStatus(taskSummary, "First task", Status.Reserved);

            // release task
            taskClient.releaseTask(CONTAINER_ID, taskSummary.getId(), USER_YODA);

            taskList = taskClient.findTasksAssignedAsPotentialOwner(USER_YODA, 0, 10);
            assertNotNull(taskList);

            assertEquals(1, taskList.size());
            taskSummary = taskList.get(0);
            checkTaskNameAndStatus(taskSummary, "First task", Status.Ready);

            taskClient.claimTask(CONTAINER_ID, taskSummary.getId(), USER_YODA);

            taskList = taskClient.findTasksAssignedAsPotentialOwner(USER_YODA, 0, 10);
            assertNotNull(taskList);

            assertEquals(1, taskList.size());
            taskSummary = taskList.get(0);
            checkTaskNameAndStatus(taskSummary, "First task", Status.Reserved);

            //Try to claim task which is already reserved by different user (Code 403)
            changeUser(USER_JOHN);
            Long taskId = taskList.get(0).getId();
            assertClientException(
                                  () -> taskClient.claimTask(CONTAINER_ID, taskId, USER_JOHN),
                                  403,
                                  "User '[UserImpl:'"+ USER_JOHN +"']' was unable to execute operation 'Claim' on task id "+ taskId +" due to a no 'current status' match");

        } finally {
            changeUser(TestConfig.getUsername());
            processClient.abortProcessInstance(CONTAINER_ID, processInstanceId);
        }
    }
    
    
    @Test
    public void testReleaseAndBulkClaim() throws Exception {
        Long processInstanceId = processClient.startProcess(CONTAINER_ID, PROCESS_ID_USERTASK);
        assertNotNull(processInstanceId);
        assertTrue(processInstanceId.longValue() > 0);
        try {
            List<TaskSummary> taskList = taskClient.findTasksAssignedAsPotentialOwner(USER_YODA, 0, 10);
            assertNotNull(taskList);

            assertEquals(1, taskList.size());
            TaskSummary taskSummary = taskList.get(0);
            checkTaskNameAndStatus(taskSummary, "First task", Status.Reserved);

            // release task
            taskClient.releaseTask(CONTAINER_ID, taskSummary.getId(), USER_YODA);

            taskList = taskClient.findTasksAssignedAsPotentialOwner(USER_YODA, 0, 10);
            assertNotNull(taskList);

            assertEquals(1, taskList.size());
            taskSummary = taskList.get(0);
            checkTaskNameAndStatus(taskSummary, "First task", Status.Ready);

            taskClient.claimTasks(CONTAINER_ID, Collections.singletonList(taskSummary.getId()), USER_YODA);

            taskList = taskClient.findTasksAssignedAsPotentialOwner(USER_YODA, 0, 10);
            assertNotNull(taskList);

            assertEquals(1, taskList.size());
            taskSummary = taskList.get(0);
            checkTaskNameAndStatus(taskSummary, "First task", Status.Reserved);

            //Try to claim task which is already reserved by different user (Code 403)
            changeUser(USER_JOHN);
            Long taskId = taskList.get(0).getId();
            assertClientException(
                                  () -> taskClient.claimTask(CONTAINER_ID, taskId, USER_JOHN),
                                  403,
                                  "User '[UserImpl:'"+ USER_JOHN +"']' was unable to execute operation 'Claim' on task id "+ taskId +" due to a no 'current status' match");

        } finally {
            changeUser(TestConfig.getUsername());
            processClient.abortProcessInstance(CONTAINER_ID, processInstanceId);
        }
    }

    @Test
    public void testStartAndStop() throws Exception {
        Long processInstanceId = processClient.startProcess(CONTAINER_ID, PROCESS_ID_USERTASK);
        assertNotNull(processInstanceId);
        assertTrue(processInstanceId.longValue() > 0);
        try {
            List<TaskSummary> taskList = taskClient.findTasksAssignedAsPotentialOwner(USER_YODA, 0, 10);
            assertNotNull(taskList);

            assertEquals(1, taskList.size());
            TaskSummary taskSummary = taskList.get(0);
            checkTaskNameAndStatus(taskSummary, "First task", Status.Reserved);

            // release task
            taskClient.startTask(CONTAINER_ID, taskSummary.getId(), USER_YODA);

            taskList = taskClient.findTasksAssignedAsPotentialOwner(USER_YODA, 0, 10);
            assertNotNull(taskList);

            assertEquals(1, taskList.size());
            taskSummary = taskList.get(0);
            checkTaskNameAndStatus(taskSummary, "First task", Status.InProgress);

            taskClient.stopTask(CONTAINER_ID, taskSummary.getId(), USER_YODA);

            taskList = taskClient.findTasksAssignedAsPotentialOwner(USER_YODA, 0, 10);
            assertNotNull(taskList);

            assertEquals(1, taskList.size());
            taskSummary = taskList.get(0);
            checkTaskNameAndStatus(taskSummary, "First task", Status.Reserved);

        } finally {
            processClient.abortProcessInstance(CONTAINER_ID, processInstanceId);
        }
    }

    @Test
    public void testSuspendAndResume() throws Exception {
        Long processInstanceId = processClient.startProcess(CONTAINER_ID, PROCESS_ID_USERTASK);
        assertNotNull(processInstanceId);
        assertTrue(processInstanceId.longValue() > 0);
        try {
            List<TaskSummary> taskList = taskClient.findTasksAssignedAsPotentialOwner(USER_YODA, 0, 10);
            assertNotNull(taskList);

            assertEquals(1, taskList.size());
            TaskSummary taskSummary = taskList.get(0);
            checkTaskNameAndStatus(taskSummary, "First task", Status.Reserved);

            // release task
            taskClient.suspendTask(CONTAINER_ID, taskSummary.getId(), USER_YODA);

            taskList = taskClient.findTasksAssignedAsPotentialOwner(USER_YODA, 0, 10);
            assertNotNull(taskList);
            assertEquals(1, taskList.size());
            taskSummary = taskList.get(0);
            checkTaskNameAndStatus(taskSummary, "First task", Status.Suspended);


            taskClient.resumeTask(CONTAINER_ID, taskSummary.getId(), USER_YODA);

            taskList = taskClient.findTasksAssignedAsPotentialOwner(USER_YODA, 0, 10);
            assertNotNull(taskList);

            assertEquals(1, taskList.size());
            taskSummary = taskList.get(0);
            checkTaskNameAndStatus(taskSummary, "First task", Status.Reserved);

        } finally {
            processClient.abortProcessInstance(CONTAINER_ID, processInstanceId);
        }
    }

    @Test
    public void testFailUserTask() throws Exception {
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

            taskClient.failTask(CONTAINER_ID, taskSummary.getId(), USER_YODA, taskOutcome);

            taskList = taskClient.findTasksAssignedAsPotentialOwner(USER_YODA, 0, 10);
            assertNotNull(taskList);

            assertEquals(1, taskList.size());
            taskSummary = taskList.get(0);
            assertEquals("Second task", taskSummary.getName());

        } finally {
            processClient.abortProcessInstance(CONTAINER_ID, processInstanceId);
        }
    }

    @Test
    public void testSkipUserTask() throws Exception {
        Long processInstanceId = processClient.startProcess(CONTAINER_ID, PROCESS_ID_USERTASK);
        assertNotNull(processInstanceId);
        assertTrue(processInstanceId.longValue() > 0);
        try {
            List<TaskSummary> taskList = taskClient.findTasksAssignedAsPotentialOwner(USER_YODA, 0, 10);
            assertNotNull(taskList);

            assertEquals(1, taskList.size());
            TaskSummary taskSummary = taskList.get(0);
            assertEquals("First task", taskSummary.getName());
            assertTrue(taskSummary.getSkipable().booleanValue());

            taskClient.skipTask(CONTAINER_ID, taskSummary.getId(), USER_YODA);

            // find all tasks with Obsolete status - should be only one
            taskList = taskClient.findTasksByStatusByProcessInstanceId(processInstanceId, Arrays.asList("Obsolete"), 0, 10);
            assertNotNull(taskList);

            assertEquals(1, taskList.size());
            taskSummary = taskList.get(0);
            checkTaskNameAndStatus(taskSummary, "First task", Status.Obsolete);

            // Verify we did skip the task and process moved on
            taskList = taskClient.findTasksAssignedAsPotentialOwner(USER_YODA, 0, 10);
            assertNotNull(taskList);

            assertEquals(1, taskList.size());
            taskSummary = taskList.get(0);
            checkTaskNameAndStatus(taskSummary, "Second task", Status.Reserved);

        } finally {
            processClient.abortProcessInstance(CONTAINER_ID, processInstanceId);
        }
    }

    @Test
    public void testUserTaskContentOperations() throws Exception {
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("stringData", "john is working on it");
        parameters.put("personData", createPersonInstance(USER_JOHN));

        Long processInstanceId = processClient.startProcess(CONTAINER_ID, PROCESS_ID_USERTASK, parameters);
        assertNotNull(processInstanceId);
        assertTrue(processInstanceId.longValue() > 0);
        try {
            List<TaskSummary> taskList = taskClient.findTasksAssignedAsPotentialOwner(USER_YODA, 0, 10);
            assertNotNull(taskList);

            assertEquals(1, taskList.size());
            TaskSummary taskSummary = taskList.get(0);
            assertEquals("First task", taskSummary.getName());

            // start task
            taskClient.startTask(CONTAINER_ID, taskSummary.getId(), USER_YODA);

            // first verify task input
            Map<String, Object> taskInput = taskClient.getTaskInputContentByTaskId(CONTAINER_ID, taskSummary.getId());
            assertNotNull(taskInput);
            assertEquals(6, taskInput.size());
            assertTrue(taskInput.containsKey("_string"));
            assertTrue(taskInput.containsKey("_person"));

            assertEquals("john is working on it", taskInput.get("_string"));
            assertEquals(USER_JOHN, KieServerReflections.valueOf(taskInput.get("_person"), "name"));

            Map<String, Object> taskOutcome = new HashMap<String, Object>();
            taskOutcome.put("string_", "my custom data");
            taskOutcome.put("person_", createPersonInstance(USER_MARY));

            Long outputContentId = taskClient.saveTaskContent(CONTAINER_ID, taskSummary.getId(), taskOutcome);

            taskInput = taskClient.getTaskOutputContentByTaskId(CONTAINER_ID, taskSummary.getId());
            assertNotNull(taskInput);
            assertEquals(2, taskInput.size());
            assertTrue(taskInput.containsKey("string_"));
            assertTrue(taskInput.containsKey("person_"));

            assertEquals("my custom data", taskInput.get("string_"));
            assertEquals(USER_MARY, KieServerReflections.valueOf(taskInput.get("person_"), "name"));

            // let's delete the content as we won't need it
            taskClient.deleteTaskContent(CONTAINER_ID, taskSummary.getId(), outputContentId);

            taskInput = taskClient.getTaskOutputContentByTaskId(CONTAINER_ID, taskSummary.getId());
            assertNotNull(taskInput);
            assertEquals(0, taskInput.size());

            // complete task
            Map<String, Object> taskOutcomeComplete = new HashMap<String, Object>();
            taskOutcomeComplete.put("string_", "my custom data 2");
            taskOutcomeComplete.put("person_", createPersonInstance("peter"));

            taskClient.completeTask(CONTAINER_ID, taskSummary.getId(), USER_YODA, taskOutcomeComplete);

            // check if task outcomes are properly set as process variables
            Object personVar = processClient.getProcessInstanceVariable(CONTAINER_ID, processInstanceId, "personData");
            assertNotNull(personVar);
            assertEquals("peter", KieServerReflections.valueOf(personVar, "name"));

            String stringVar = (String) processClient.getProcessInstanceVariable(CONTAINER_ID, processInstanceId, "stringData");
            assertNotNull(personVar);
            assertEquals("my custom data 2", stringVar);

            taskList = taskClient.findTasksAssignedAsPotentialOwner(USER_YODA, 0, 10);
            assertNotNull(taskList);

            assertEquals(1, taskList.size());
            taskSummary = taskList.get(0);
            assertEquals("Second task", taskSummary.getName());

        } finally {
            processClient.abortProcessInstance(CONTAINER_ID, processInstanceId);
        }
    }

    @Test
    public void testUserTaskById() throws Exception {
        Long processInstanceId = processClient.startProcess(CONTAINER_ID, PROCESS_ID_USERTASK);
        assertNotNull(processInstanceId);
        assertTrue(processInstanceId.longValue() > 0);
        try {
            List<TaskSummary> taskList = taskClient.findTasksAssignedAsPotentialOwner(USER_YODA, 0, 10);
            assertNotNull(taskList);

            assertEquals(1, taskList.size());
            TaskSummary taskSummary = taskList.get(0);
            assertEquals("First task", taskSummary.getName());

            TaskInstance taskInstance = taskClient.getTaskInstance(CONTAINER_ID, taskSummary.getId());
            assertNotNull(taskInstance);
            assertEquals("First task", taskInstance.getName());
            KieServerAssert.assertNullOrEmpty(taskInstance.getDescription());
            KieServerAssert.assertNullOrEmpty(taskInstance.getSubject());
            assertEquals("Reserved", taskInstance.getStatus());
            assertEquals(0, taskInstance.getPriority().intValue());
            assertEquals(USER_YODA, taskInstance.getActualOwner());
            assertEquals(USER_YODA, taskInstance.getCreatedBy());
            assertEquals(PROCESS_ID_USERTASK, taskInstance.getProcessId());
            assertEquals(CONTAINER_ID, taskInstance.getContainerId());
            assertEquals(taskSummary.getId(), taskInstance.getId());
            assertEquals(-1, taskInstance.getParentId().longValue());
            assertEquals(true, taskInstance.getSkipable());
            assertEquals(processInstanceId, taskInstance.getProcessInstanceId());

            assertNotNull(taskInstance.getWorkItemId());
            assertTrue(taskInstance.getWorkItemId().longValue() > 0);

            assertNull(taskInstance.getExcludedOwners());
            assertNull(taskInstance.getPotentialOwners());
            assertNull(taskInstance.getBusinessAdmins());
            assertNull(taskInstance.getInputData());
            assertNull(taskInstance.getOutputData());

        } finally {
            processClient.abortProcessInstance(CONTAINER_ID, processInstanceId);
        }
    }

    @Test (expected = KieServicesException.class)
    public void testNotExistingUserTaskById() throws Exception {
        taskClient.getTaskInstance(CONTAINER_ID, -9999l);
    }

    @Test
    public void testUserTaskByIdWithDetails() throws Exception {
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("stringData", "john is working on it");
        parameters.put("personData", createPersonInstance(USER_JOHN));

        Long processInstanceId = processClient.startProcess(CONTAINER_ID, PROCESS_ID_USERTASK, parameters);
        assertNotNull(processInstanceId);
        assertTrue(processInstanceId.longValue() > 0);
        try {
            List<TaskSummary> taskList = taskClient.findTasksAssignedAsPotentialOwner(USER_YODA, 0, 10);
            assertNotNull(taskList);

            assertEquals(1, taskList.size());
            TaskSummary taskSummary = taskList.get(0);
            assertEquals("First task", taskSummary.getName());

            TaskInstance taskInstance = taskClient.getTaskInstance(CONTAINER_ID, taskSummary.getId(), true, true, true);
            assertNotNull(taskInstance);
            assertEquals("First task", taskInstance.getName());
            KieServerAssert.assertNullOrEmpty(taskInstance.getDescription());
            KieServerAssert.assertNullOrEmpty(taskInstance.getSubject());
            assertEquals("Reserved", taskInstance.getStatus());
            assertEquals(0, taskInstance.getPriority().intValue());
            assertEquals(USER_YODA, taskInstance.getActualOwner());
            assertEquals(USER_YODA, taskInstance.getCreatedBy());
            assertEquals(PROCESS_ID_USERTASK, taskInstance.getProcessId());
            assertEquals(CONTAINER_ID, taskInstance.getContainerId());
            assertEquals(taskSummary.getId(), taskInstance.getId());
            assertEquals(-1, taskInstance.getParentId().longValue());
            assertEquals(true, taskInstance.getSkipable());
            assertEquals(processInstanceId, taskInstance.getProcessInstanceId());

            assertNotNull(taskInstance.getWorkItemId());
            assertTrue(taskInstance.getWorkItemId().longValue() > 0);

            assertNotNull(taskInstance.getExcludedOwners());
            assertEquals(0, taskInstance.getExcludedOwners().size());
            assertNotNull(taskInstance.getPotentialOwners());
            assertEquals(1, taskInstance.getPotentialOwners().size());
            assertTrue(taskInstance.getPotentialOwners().contains(USER_YODA));

            assertNotNull(taskInstance.getBusinessAdmins());
            assertEquals(2, taskInstance.getBusinessAdmins().size());
            assertTrue(taskInstance.getBusinessAdmins().contains(USER_ADMINISTRATOR));
            assertTrue(taskInstance.getBusinessAdmins().contains("Administrators"));

            assertNotNull(taskInstance.getInputData());
            assertEquals(6, taskInstance.getInputData().size());

            Map<String, Object> inputs = taskInstance.getInputData();
            assertTrue(inputs.containsKey("ActorId"));
            assertTrue(inputs.containsKey("_string"));
            assertTrue(inputs.containsKey("Skippable"));
            assertTrue(inputs.containsKey("_person"));
            assertTrue(inputs.containsKey("NodeName"));

            assertEquals(USER_YODA, inputs.get("ActorId"));
            assertEquals("john is working on it", inputs.get("_string"));
            assertEquals("true", inputs.get("Skippable"));
            assertEquals("First task", inputs.get("NodeName"));
            assertEquals(USER_JOHN, KieServerReflections.valueOf(inputs.get("_person"), "name"));

            assertNotNull(taskInstance.getOutputData());
            assertEquals(0, taskInstance.getOutputData().size());

        } finally {
            processClient.abortProcessInstance(CONTAINER_ID, processInstanceId);
        }
    }

    /**
     * Test verifying forwardTask method and its functionality.
     * Also check task status changes when forwarding task.
     *
     * @throws Exception
     */
    @Test
    public void testForwardUserTask() throws Exception {
        Long processInstanceId = processClient.startProcess(CONTAINER_ID, PROCESS_ID_USERTASK);
        assertNotNull(processInstanceId);
        assertTrue(processInstanceId.longValue() > 0);
        try {
            List<TaskSummary> taskList = taskClient.findTasksAssignedAsPotentialOwner(USER_YODA, 0, 10);
            assertNotNull(taskList);

            assertEquals(1, taskList.size());
            TaskSummary taskSummary = taskList.get(0);
            assertEquals("First task", taskSummary.getName());

            checkTaskStatusAndActualOwner(CONTAINER_ID, taskSummary.getId(), Status.Reserved, USER_YODA);

            // forwarding Reserved task to john (forwarding Reserved -> Ready)
            taskClient.forwardTask(CONTAINER_ID, taskSummary.getId(), USER_YODA, USER_JOHN);

            // user yoda has empty task list now
            taskList = taskClient.findTasksAssignedAsPotentialOwner(USER_YODA, 0, 10);
            assertNotNull(taskList);
            assertEquals(0, taskList.size());

            // forwarded task is set to Ready state and is assigned to john as potential owner
            changeUser(USER_JOHN);
            taskList = taskClient.findTasksAssignedAsPotentialOwner(USER_JOHN, 0, 10);
            assertNotNull(taskList);
            assertEquals(1, taskList.size());
            assertEquals(taskSummary.getId(), taskList.get(0).getId());

            checkTaskStatusAndOwners(CONTAINER_ID, taskSummary.getId(), Status.Ready, "", USER_JOHN);

            // forwarding task in Ready state back to yoda (forwarding Ready -> Ready)
            taskClient.forwardTask(CONTAINER_ID, taskSummary.getId(), USER_JOHN, USER_YODA);

            // forwarded task stays in Ready state and is assigned to yoda as potential owner
            changeUser(USER_YODA);
            checkTaskStatusAndOwners(CONTAINER_ID, taskSummary.getId(), Status.Ready, "", USER_YODA);

            // starting task to change its status to In progress
            taskClient.startTask(CONTAINER_ID, taskSummary.getId(), USER_YODA);
            checkTaskStatusAndActualOwner(CONTAINER_ID, taskSummary.getId(), Status.InProgress, USER_YODA);

            // forwarding task In progress back to john (forwarding In progress -> Ready)
            taskClient.forwardTask(CONTAINER_ID, taskSummary.getId(), USER_YODA, USER_JOHN);

            // forwarded task change state to Ready and is assigned to john as potential owner
            changeUser(USER_JOHN);
            checkTaskStatusAndOwners(CONTAINER_ID, taskSummary.getId(), Status.Ready, "", USER_JOHN);

            // forwarding task in Ready state by a user who isn't potential owner (Code 403)
            changeUser(USER_YODA);
            assertClientException(
                                  () -> taskClient.forwardTask(CONTAINER_ID, taskSummary.getId(), USER_YODA, USER_WITHOUT_PERMISSIONS),
                                  403,
                                  "User '[UserImpl:'"+ USER_YODA +"']' does not have permissions to execute operation 'Forward' on task id "+ taskSummary.getId());
            
        } finally {
            processClient.abortProcessInstance(CONTAINER_ID, processInstanceId);
            changeUser(TestConfig.getUsername());
        }
    }

    /**
     * Test verifying delegateTask method and its functionality.
     * Also check task status changes when delegating task.
     *
     * @throws Exception
     */
    @Test
    public void testDelegateUserTask() throws Exception {
        Long processInstanceId = processClient.startProcess(CONTAINER_ID, PROCESS_ID_USERTASK);
        assertNotNull(processInstanceId);
        assertTrue(processInstanceId.longValue() > 0);
        try {
            List<TaskSummary> taskList = taskClient.findTasksAssignedAsPotentialOwner(USER_YODA, 0, 10);
            assertNotNull(taskList);

            assertEquals(1, taskList.size());
            TaskSummary taskSummary = taskList.get(0);
            assertEquals("First task", taskSummary.getName());

            checkTaskStatusAndActualOwner(CONTAINER_ID, taskSummary.getId(), Status.Reserved, USER_YODA);

            // delegating Reserved task to john (delegating Reserved -> Reserved)
            taskClient.delegateTask(CONTAINER_ID, taskSummary.getId(), USER_YODA, USER_JOHN);

            // user yoda has empty task list now
            taskList = taskClient.findTasksAssignedAsPotentialOwner(USER_YODA, 0, 10);
            assertNotNull(taskList);
            assertEquals(0, taskList.size());

            // delegated task stays in Reserved state and is assigned to john as actual owner
            changeUser(USER_JOHN);
            taskList = taskClient.findTasksAssignedAsPotentialOwner(USER_JOHN, 0, 10);
            assertNotNull(taskList);
            assertEquals(1, taskList.size());
            assertEquals(taskSummary.getId(), taskList.get(0).getId());

            checkTaskStatusAndActualOwner(CONTAINER_ID, taskSummary.getId(), Status.Reserved, USER_JOHN);

            // releasing task to change its state to Ready
            taskClient.releaseTask(CONTAINER_ID, taskSummary.getId(), USER_JOHN);
            checkTaskStatusAndActualOwner(CONTAINER_ID, taskSummary.getId(), Status.Ready, "");

            // delegating task in Ready state back to yoda (delegating Ready -> Reserved)
            taskClient.delegateTask(CONTAINER_ID, taskSummary.getId(), USER_JOHN, USER_YODA);

            // delegated task change its state to Reserved and is assigned to yoda as actual owner
            changeUser(USER_YODA);
            checkTaskStatusAndActualOwner(CONTAINER_ID, taskSummary.getId(), Status.Reserved, USER_YODA);

            // starting task to change its status to In progress
            taskClient.startTask(CONTAINER_ID, taskSummary.getId(), USER_YODA);
            checkTaskStatusAndActualOwner(CONTAINER_ID, taskSummary.getId(), Status.InProgress, USER_YODA);

            // delegating task In progress back to john (delegating In progress -> Reserved)
            taskClient.delegateTask(CONTAINER_ID, taskSummary.getId(), USER_YODA, USER_JOHN);

            // delegated task change state to Reserved and is assigned to john as actual owner
            changeUser(USER_JOHN);
            checkTaskStatusAndActualOwner(CONTAINER_ID, taskSummary.getId(), Status.Reserved, USER_JOHN);

            // delegating task in Reserved state by a user who isn't potential owner (Code 403)
            changeUser(USER_YODA);
            assertClientException(
                                  () -> taskClient.delegateTask(CONTAINER_ID, taskSummary.getId(), USER_YODA, USER_WITHOUT_PERMISSIONS),
                                  403,
                                  "User '[UserImpl:'"+ USER_YODA +"']' does not have permissions to execute operation 'Delegate' on task id "+ taskSummary.getId());
            
        } finally {
            processClient.abortProcessInstance(CONTAINER_ID, processInstanceId);
            changeUser(TestConfig.getUsername());
        }
    }

    @Test
    public void testNominateUserTask() throws Exception {
        Long processInstanceId = processClient.startProcess(CONTAINER_ID, PROCESS_ID_USERTASK);
        assertNotNull(processInstanceId);
        assertTrue(processInstanceId.longValue() > 0);
        try {
            List<TaskSummary> taskList = taskClient.findTasksAssignedAsPotentialOwner(USER_YODA, 0, 10);
            assertNotNull(taskList);

            assertEquals(1, taskList.size());
            TaskSummary taskSummary = taskList.get(0);
            assertEquals("First task", taskSummary.getName());

            checkTaskStatusAndActualOwner(CONTAINER_ID, taskSummary.getId(), Status.Reserved, USER_YODA);

            // Nominated task with status different to Created by a user who isn't potential owner (403)
            changeUser(USER_JOHN);
            List<String> listPotOwners = new ArrayList<String>();
            listPotOwners.add(USER_YODA);
            listPotOwners.add(USER_JOHN);
            assertClientException(
                                  () -> taskClient.nominateTask(CONTAINER_ID, taskSummary.getId(), USER_JOHN, listPotOwners),
                                  403,
                                  "User '[UserImpl:'"+ USER_JOHN +"']' was unable to execute operation 'Nominate' on task id "+ taskSummary.getId() +" due to a no 'current status' match");

        } finally {
            processClient.abortProcessInstance(CONTAINER_ID, processInstanceId);
            changeUser(TestConfig.getUsername());
        }
    }
    
    @Test
    public void testUserTaskSetTaskProperties() throws Exception {
        Long processInstanceId = processClient.startProcess(CONTAINER_ID, PROCESS_ID_USERTASK);
        assertNotNull(processInstanceId);
        assertTrue(processInstanceId.longValue() > 0);
        try {
            List<TaskSummary> taskList = taskClient.findTasksAssignedAsPotentialOwner(USER_YODA, 0, 10);
            assertNotNull(taskList);

            assertEquals(1, taskList.size());
            TaskSummary taskSummary = taskList.get(0);

            // verify current task properties
            assertEquals(0, taskSummary.getPriority().intValue());
            assertNull(taskSummary.getExpirationTime());
            assertTrue(taskSummary.getSkipable().booleanValue());
            assertEquals("First task", taskSummary.getName());
            KieServerAssert.assertNullOrEmpty(taskSummary.getDescription());

            // set task properties
            Calendar currentTime = Calendar.getInstance();
            currentTime.add(Calendar.DAY_OF_YEAR, 1);
            Date expirationDate = currentTime.getTime();
            taskClient.setTaskDescription(CONTAINER_ID, taskSummary.getId(), "Simple user task.");
            taskClient.setTaskExpirationDate(CONTAINER_ID, taskSummary.getId(), expirationDate);
            taskClient.setTaskName(CONTAINER_ID, taskSummary.getId(), "Modified name");
            taskClient.setTaskPriority(CONTAINER_ID, taskSummary.getId(), 10);
            taskClient.setTaskSkipable(CONTAINER_ID, taskSummary.getId(), false);

            // start task
            taskClient.startTask(CONTAINER_ID, taskSummary.getId(), USER_YODA);

            // retrieve started task
            TaskInstance taskInstance = taskClient.getTaskInstance(CONTAINER_ID, taskSummary.getId());

            // verify modified task properties
            assertEquals(10, taskInstance.getPriority().intValue());
            assertNotNull(taskInstance.getExpirationDate());
            assertFalse(taskInstance.getSkipable().booleanValue());
            assertEquals("Modified name", taskInstance.getName());
            assertEquals("Simple user task.", taskInstance.getDescription());
        } finally {
            processClient.abortProcessInstance(CONTAINER_ID, processInstanceId);
        }
    }

    @Test
    public void testUserTaskComments() throws Exception {
        Long processInstanceId = processClient.startProcess(CONTAINER_ID, PROCESS_ID_USERTASK);
        assertNotNull(processInstanceId);
        assertTrue(processInstanceId.longValue() > 0);
        try {
            List<TaskSummary> taskList = taskClient.findTasksAssignedAsPotentialOwner(USER_YODA, 0, 10);
            assertNotNull(taskList);
            assertEquals(1, taskList.size());
            TaskSummary taskSummary = taskList.get(0);

            // Adding comment to user task as yoda.
            String firstComment = "First comment.";
            Calendar firstCommentTime = Calendar.getInstance();
            Long firstCommentId = taskClient.addTaskComment(CONTAINER_ID, taskSummary.getId(), firstComment, USER_YODA, firstCommentTime.getTime());

            // Adding second comment to user task as john.
            String secondComment = "Second comment.";
            Calendar secondCommentTime = Calendar.getInstance();
            secondCommentTime.add(Calendar.MINUTE, 5);
            Long secondCommentId = taskClient.addTaskComment(CONTAINER_ID, taskSummary.getId(), secondComment, USER_JOHN, secondCommentTime.getTime());

            // start task
            taskClient.startTask(CONTAINER_ID, taskSummary.getId(), USER_YODA);

            // Verifying first comment returned by getTaskCommentById().
            TaskComment firstTaskComment = taskClient.getTaskCommentById(CONTAINER_ID, taskSummary.getId(), firstCommentId);
            assertNotNull(firstTaskComment.getAddedAt());
            assertEquals(USER_YODA, firstTaskComment.getAddedBy());
            assertEquals(firstCommentId, firstTaskComment.getId());
            assertEquals(firstComment, firstTaskComment.getText());

            // Verifying second comment returned by getTaskCommentsByTaskId().
            List<TaskComment> taskComments = taskClient.getTaskCommentsByTaskId(CONTAINER_ID, taskSummary.getId());
            assertEquals(2, taskComments.size());

            TaskComment secondTaskComment = null;
            if (secondCommentId.equals(taskComments.get(0).getId())) {
                secondTaskComment = taskComments.get(0);
            } else {
                secondTaskComment = taskComments.get(1);
            }
            assertNotNull(secondTaskComment.getAddedAt());
            assertEquals(USER_JOHN, secondTaskComment.getAddedBy());
            assertEquals(secondCommentId, secondTaskComment.getId());
            assertEquals(secondComment, secondTaskComment.getText());

            // Delete task comment.
            taskClient.deleteTaskComment(CONTAINER_ID, taskSummary.getId(), secondCommentId);

            // Now there is just one comment left.
            taskComments = taskClient.getTaskCommentsByTaskId(CONTAINER_ID, taskSummary.getId());
            assertEquals(1, taskComments.size());
            assertEquals(firstCommentId, taskComments.get(0).getId());
        } finally {
            processClient.abortProcessInstance(CONTAINER_ID, processInstanceId);
        }
    }

    @Test
    public void testUserTaskAttachments() throws Exception {
        Long processInstanceId = processClient.startProcess(CONTAINER_ID, PROCESS_ID_USERTASK);
        assertNotNull(processInstanceId);
        assertTrue(processInstanceId.longValue() > 0);
        try {
            List<TaskSummary> taskList = taskClient.findTasksAssignedAsPotentialOwner(USER_YODA, 0, 10);
            assertNotNull(taskList);
            assertEquals(1, taskList.size());
            TaskSummary taskSummary = taskList.get(0);

            String attachment1Name = "First attachment";
            String attachment2Name = "Second attachment";

            // Adding attachments to user task.
            Object firstAttachmentContent = createPersonInstance(USER_MARY);
            Long firstAttachmentId = taskClient.addTaskAttachment(CONTAINER_ID, taskSummary.getId(), USER_YODA, attachment1Name, firstAttachmentContent);

            changeUser(USER_JOHN);
            String secondAttachmentContent = "This is second attachment.";
            Long secondAttachmentId = taskClient.addTaskAttachment(CONTAINER_ID, taskSummary.getId(), USER_JOHN, attachment2Name, secondAttachmentContent);

            changeUser(USER_YODA);
            // start task
            taskClient.startTask(CONTAINER_ID, taskSummary.getId(), USER_YODA);

            // Verifying first attachment returned by getTaskAttachmentById().
            TaskAttachment firstTaskAttachment = taskClient.getTaskAttachmentById(CONTAINER_ID, taskSummary.getId(), firstAttachmentId);
            assertNotNull(firstTaskAttachment.getAddedAt());
            assertEquals(USER_YODA, firstTaskAttachment.getAddedBy());
            assertNotNull(firstTaskAttachment.getAttachmentContentId());
            assertEquals(firstAttachmentContent.getClass().getName(), firstTaskAttachment.getContentType());
            assertEquals(firstAttachmentId, firstTaskAttachment.getId());
            assertNotNull(firstTaskAttachment.getName());
            assertEquals(attachment1Name, firstTaskAttachment.getName());
            assertNotNull(firstTaskAttachment.getSize());

            // Verifying second attachment returned by getTaskAttachmentsByTaskId().
            List<TaskAttachment> taskAttachments = taskClient.getTaskAttachmentsByTaskId(CONTAINER_ID, taskSummary.getId());
            assertEquals(2, taskAttachments.size());

            TaskAttachment secondTaskAttachment = null;
            if (secondAttachmentId.equals(taskAttachments.get(0).getId())) {
                secondTaskAttachment = taskAttachments.get(0);
            } else {
                secondTaskAttachment = taskAttachments.get(1);
            }
            assertNotNull(secondTaskAttachment.getAddedAt());
            assertEquals(USER_JOHN, secondTaskAttachment.getAddedBy());
            assertNotNull(secondTaskAttachment.getAttachmentContentId());
            assertEquals(String.class.getName(), secondTaskAttachment.getContentType());
            assertEquals(secondAttachmentId, secondTaskAttachment.getId());
            assertNotNull(secondTaskAttachment.getName());
            assertEquals(attachment2Name, secondTaskAttachment.getName());
            assertNotNull(secondTaskAttachment.getSize());

            // Verifying second attachment content returned by getTaskAttachmentContentById().
            Object taskAttachmentContent = taskClient.getTaskAttachmentContentById(CONTAINER_ID, taskSummary.getId(), secondAttachmentId);
            assertEquals(secondAttachmentContent, taskAttachmentContent);

            // Delete task attachment.
            taskClient.deleteTaskAttachment(CONTAINER_ID, taskSummary.getId(), firstAttachmentId);

            // Now there is just one attachment left.
            taskAttachments = taskClient.getTaskAttachmentsByTaskId(CONTAINER_ID, taskSummary.getId());
            assertEquals(1, taskAttachments.size());
            assertEquals(secondAttachmentId, taskAttachments.get(0).getId());
        } finally {
            processClient.abortProcessInstance(CONTAINER_ID, processInstanceId);
        }
    }

    @Test
    public void testUserTaskAttachmentsAsByteArray() throws Exception {
        Long processInstanceId = processClient.startProcess(CONTAINER_ID, PROCESS_ID_USERTASK);
        assertNotNull(processInstanceId);
        assertTrue(processInstanceId.longValue() > 0);
        try {
            List<TaskSummary> taskList = taskClient.findTasksAssignedAsPotentialOwner(USER_YODA, 0, 10);
            assertNotNull(taskList);
            assertEquals(1, taskList.size());
            TaskSummary taskSummary = taskList.get(0);

            // Adding attachments to user task.
            byte[] attachmentContent = new String("This is first attachment.").getBytes();
            Long attachmentId = taskClient.addTaskAttachment(CONTAINER_ID, taskSummary.getId(), USER_YODA, "my attachment", attachmentContent);

            // start task
            taskClient.startTask(CONTAINER_ID, taskSummary.getId(), USER_YODA);

            // Verifying attachment returned by getTaskAttachmentById().
            Object taskAttachmentContent = taskClient.getTaskAttachmentContentById(CONTAINER_ID, taskSummary.getId(), attachmentId);
            assertArrayEquals(attachmentContent, (byte[])taskAttachmentContent);
        } finally {
            processClient.abortProcessInstance(CONTAINER_ID, processInstanceId);
        }
    }

    @Test
    public void testExitUserTask() throws Exception {
        Long processInstanceId = processClient.startProcess(CONTAINER_ID, PROCESS_ID_USERTASK);
        assertNotNull(processInstanceId);
        assertTrue(processInstanceId.longValue() > 0);
        try {
            List<TaskSummary> taskList = taskClient.findTasksAssignedAsPotentialOwner(USER_YODA, 0, 10);
            assertNotNull(taskList);

            assertEquals(1, taskList.size());
            TaskSummary taskSummary = taskList.get(0);
            assertEquals(Status.Reserved.toString(), taskSummary.getStatus());

            // exit task
            changeUser(USER_ADMINISTRATOR);
            taskClient.exitTask(CONTAINER_ID, taskSummary.getId(), USER_ADMINISTRATOR);
            changeUser(USER_YODA);

            TaskInstance task = taskClient.findTaskById(taskSummary.getId());
            assertNotNull(task);
            assertEquals(taskSummary.getId(), task.getId());
            assertEquals(Status.Exited.toString(), task.getStatus());
        } finally {
            processClient.abortProcessInstance(CONTAINER_ID, processInstanceId);
            changeUser(TestConfig.getUsername());
        }
    }

    @Test
    public void testGroupUserTask() throws Exception {
        // don't run the test on local server as it does not properly support authentication
        assumeFalse(TestConfig.isLocalServer());
        String taskName = "Group task";

        Long processInstanceId = processClient.startProcess(CONTAINER_ID, PROCESS_ID_GROUPTASK);
        assertNotNull(processInstanceId);
        assertTrue(processInstanceId.longValue() > 0);
        try {
            List<TaskSummary> taskList = taskClient.findTasksByStatusByProcessInstanceId(processInstanceId, null, 0, 10);
            assertNotNull(taskList);

            assertEquals(1, taskList.size());
            TaskSummary taskSummary = taskList.get(0);
            assertEquals(taskName, taskSummary.getName());
            assertEquals(Status.Ready.toString(), taskSummary.getStatus());

            // User yoda isn't in group "engineering", can't claim task.
            Long taskId = taskList.get(0).getId();
            assertClientException(
                                  () -> taskClient.claimTask(CONTAINER_ID, taskId, USER_YODA),
                                  403,
                                  "User '[UserImpl:'"+ USER_YODA +"']' does not have permissions to execute operation 'Claim' on task id "+ taskId);
            
            // User john is in group "engineering", can work with task.
            changeUser(USER_JOHN);
            taskClient.claimTask(CONTAINER_ID, taskSummary.getId(), USER_JOHN);
            taskClient.startTask(CONTAINER_ID, taskSummary.getId(), USER_JOHN);

            taskList = taskClient.findTasksOwned(USER_JOHN, 0, 10);
            assertNotNull(taskList);

            assertEquals(1, taskList.size());
            taskSummary = taskList.get(0);
            assertEquals(taskName, taskSummary.getName());
            assertEquals(Status.InProgress.toString(), taskSummary.getStatus());

            // complete task
            Map<String, Object> taskOutcomeComplete = new HashMap<String, Object>();
            taskClient.completeTask(CONTAINER_ID, taskSummary.getId(), USER_JOHN, taskOutcomeComplete);

            taskList = taskClient.findTasksAssignedAsPotentialOwner(USER_JOHN, 0, 10);
            KieServerAssert.assertNullOrEmpty("Found task to be processed!", taskList);

        } catch (Exception e){
            processClient.abortProcessInstance(CONTAINER_ID, processInstanceId);
            throw e;
        } finally {
            changeUser(TestConfig.getUsername());
        }
    }

    @Test
    public void testFindTasksByVariable() throws Exception {
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("stringData", "john is working on it");
        parameters.put("personData", createPersonInstance(USER_JOHN));

        Long processInstanceId = processClient.startProcess(CONTAINER_ID, PROCESS_ID_USERTASK, parameters);
        assertNotNull(processInstanceId);
        assertTrue(processInstanceId.longValue() > 0);
        try {
            List<TaskSummary> taskList = taskClient.findTasksAssignedAsPotentialOwner(USER_YODA, 0, 10);
            assertNotNull(taskList);

            assertEquals(1, taskList.size());
            TaskSummary taskSummary = taskList.get(0);
            assertEquals("First task", taskSummary.getName());

            taskList = taskClient.findTasksByVariable(USER_YODA, "_string", null, 0, 10);
            assertEquals(1, taskList.size());
            taskSummary = taskList.get(0);
            assertEquals("First task", taskSummary.getName());

            // start task
            taskClient.startTask(CONTAINER_ID, taskSummary.getId(), USER_YODA);

            // complete task
            Map<String, Object> taskOutcomeComplete = new HashMap<String, Object>();
            taskOutcomeComplete.put("string_", "my custom data 2");
            taskOutcomeComplete.put("person_", createPersonInstance("peter"));

            taskClient.saveTaskContent(CONTAINER_ID, taskSummary.getId(), taskOutcomeComplete);

            List<String> inprogressTasks = new ArrayList<String>();
            inprogressTasks.add(Status.InProgress.toString());

            taskList = taskClient.findTasksByVariable(USER_YODA, "string_", inprogressTasks, 0, 10);
            assertEquals(1, taskList.size());
            taskSummary = taskList.get(0);
            assertEquals("First task", taskSummary.getName());

            taskClient.completeTask(CONTAINER_ID, taskSummary.getId(), USER_YODA, taskOutcomeComplete);

            taskList = taskClient.findTasksAssignedAsPotentialOwner(USER_YODA, 0, 10);
            assertNotNull(taskList);

            assertEquals(1, taskList.size());
            taskSummary = taskList.get(0);
            assertEquals("Second task", taskSummary.getName());

        } finally {
            processClient.abortProcessInstance(CONTAINER_ID, processInstanceId);
        }
    }

    @Test
    public void testFindTasksByVariableAndValue() throws Exception {
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("stringData", "john is working on it");
        parameters.put("personData", createPersonInstance(USER_JOHN));

        Long processInstanceId = processClient.startProcess(CONTAINER_ID, PROCESS_ID_USERTASK, parameters);
        assertNotNull(processInstanceId);
        assertTrue(processInstanceId.longValue() > 0);
        try {
            List<TaskSummary> taskList = taskClient.findTasksAssignedAsPotentialOwner(USER_YODA, 0, 10);
            assertNotNull(taskList);

            assertEquals(1, taskList.size());
            TaskSummary taskSummary = taskList.get(0);
            assertEquals("First task", taskSummary.getName());

            taskList = taskClient.findTasksByVariableAndValue(USER_YODA, "_string", "john is working on it", null, 0, 10);
            assertEquals(1, taskList.size());
            taskSummary = taskList.get(0);
            assertEquals("First task", taskSummary.getName());

            // start task
            taskClient.startTask(CONTAINER_ID, taskSummary.getId(), USER_YODA);

            // complete task
            Map<String, Object> taskOutcomeComplete = new HashMap<String, Object>();
            taskOutcomeComplete.put("string_", "my custom data 2");
            taskOutcomeComplete.put("person_", createPersonInstance("peter"));

            taskClient.saveTaskContent(CONTAINER_ID, taskSummary.getId(), taskOutcomeComplete);

            List<String> inprogressTasks = new ArrayList<String>();
            inprogressTasks.add(Status.InProgress.toString());

            taskList = taskClient.findTasksByVariableAndValue(USER_YODA, "string_", "my*", inprogressTasks, 0, 10);
            assertEquals(1, taskList.size());
            taskSummary = taskList.get(0);
            assertEquals("First task", taskSummary.getName());

            taskClient.completeTask(CONTAINER_ID, taskSummary.getId(), USER_YODA, taskOutcomeComplete);

            taskList = taskClient.findTasksAssignedAsPotentialOwner(USER_YODA, 0, 10);
            assertNotNull(taskList);

            assertEquals(1, taskList.size());
            taskSummary = taskList.get(0);
            assertEquals("Second task", taskSummary.getName());

        } finally {
            processClient.abortProcessInstance(CONTAINER_ID, processInstanceId);
        }
    }

    @Test
    public void testFindTasksByVariableAndValueSortedByProcessInstanceId() throws Exception {
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("stringData", "john is working on it");
        parameters.put("personData", createPersonInstance(USER_JOHN));

        Long processInstanceId = processClient.startProcess(CONTAINER_ID, PROCESS_ID_USERTASK, parameters);
        Long processInstanceId2 = processClient.startProcess(CONTAINER_ID, PROCESS_ID_USERTASK, parameters);

        try {
            List<TaskSummary> taskList = taskClient.findTasksAssignedAsPotentialOwner(USER_YODA, 0, 10);
            assertNotNull(taskList);
            assertEquals(2, taskList.size());

            List<String> status = new ArrayList<String>();
            status.add(Status.Reserved.toString());

            taskList = taskClient.findTasksByVariableAndValue(USER_YODA, "_string", "john is working on it", status, 0, 10, "processInstanceId", true);
            assertEquals(2, taskList.size());
            if (processInstanceId < processInstanceId2) {
                assertEquals(processInstanceId, taskList.get(0).getProcessInstanceId());
                assertEquals(processInstanceId2, taskList.get(1).getProcessInstanceId());
            } else {
                assertEquals(processInstanceId2, taskList.get(0).getProcessInstanceId());
                assertEquals(processInstanceId, taskList.get(1).getProcessInstanceId());
            }

            taskList = taskClient.findTasksByVariableAndValue(USER_YODA, "_string", "john is working on it", status, 0, 10, "processInstanceId", false);
            assertEquals(2, taskList.size());
            if (processInstanceId < processInstanceId2) {
                assertEquals(processInstanceId2, taskList.get(0).getProcessInstanceId());
                assertEquals(processInstanceId, taskList.get(1).getProcessInstanceId());
            } else {
                assertEquals(processInstanceId, taskList.get(0).getProcessInstanceId());
                assertEquals(processInstanceId2, taskList.get(1).getProcessInstanceId());
            }
        } finally {
            processClient.abortProcessInstance(CONTAINER_ID, processInstanceId);
            processClient.abortProcessInstance(CONTAINER_ID, processInstanceId2);
        }
    }

    @Test
    public void testProcessWithUserTasksWithConversationId() throws Exception {
        // Used to load conversation Id.
        client.getContainerInfo(CONTAINER_ID);
        String conversationId = client.getConversationId();
        assertNotNull(conversationId);

        Long processInstanceId = processClient.startProcess(CONTAINER_ID, PROCESS_ID_USERTASK);
        assertNotNull(processInstanceId);
        assertTrue(processInstanceId.longValue() > 0);
        String afterNextCallConversationId = ((AbstractKieServicesClientImpl)processClient).getConversationId();
        assertEquals(conversationId, afterNextCallConversationId);

        // complete conversation to start with new one
        client.completeConversation();

        try {
            List<TaskSummary> taskList = taskClient.findTasksAssignedAsPotentialOwner(USER_YODA, 0, 10);
            assertNotNull(taskList);

            assertEquals(1, taskList.size());
            TaskSummary taskSummary = taskList.get(0);
            assertEquals("First task", taskSummary.getName());

            afterNextCallConversationId = ((AbstractKieServicesClientImpl)taskClient).getConversationId();
            // since there is no container id in the query endpoint no conversation is set
            assertNull(afterNextCallConversationId);

            // startTask and completeTask task
            taskClient.startTask(CONTAINER_ID, taskSummary.getId(), USER_YODA);

            afterNextCallConversationId = ((AbstractKieServicesClientImpl)taskClient).getConversationId();
            assertNotEquals(conversationId, afterNextCallConversationId);
            conversationId = afterNextCallConversationId;

            Map<String, Object> taskOutcome = new HashMap<String, Object>();
            taskOutcome.put("string_", "my custom data");
            taskOutcome.put("person_", createPersonInstance(USER_MARY));

            taskClient.completeTask(CONTAINER_ID, taskSummary.getId(), USER_YODA, taskOutcome);

            afterNextCallConversationId = ((AbstractKieServicesClientImpl)taskClient).getConversationId();
            assertEquals(conversationId, afterNextCallConversationId);

            // complete conversation to start with new one
            client.completeConversation();

            // check if task outcomes are properly set as process variables
            Object personVar = processClient.getProcessInstanceVariable(CONTAINER_ID, processInstanceId, "personData");
            assertNotNull(personVar);
            assertEquals(USER_MARY, KieServerReflections.valueOf(personVar, "name"));

            afterNextCallConversationId = ((AbstractKieServicesClientImpl)processClient).getConversationId();
            assertNotEquals(conversationId, afterNextCallConversationId);
            conversationId = ((AbstractKieServicesClientImpl)processClient).getConversationId();

            String stringVar = (String) processClient.getProcessInstanceVariable(CONTAINER_ID, processInstanceId, "stringData");
            assertNotNull(personVar);
            assertEquals("my custom data", stringVar);

            afterNextCallConversationId = ((AbstractKieServicesClientImpl)processClient).getConversationId();
            assertEquals(conversationId, afterNextCallConversationId);

            taskList = taskClient.findTasksAssignedAsPotentialOwner(USER_YODA, 0, 10);
            assertNotNull(taskList);

            assertEquals(1, taskList.size());
            taskSummary = taskList.get(0);
            assertEquals("Second task", taskSummary.getName());

            afterNextCallConversationId = ((AbstractKieServicesClientImpl)taskClient).getConversationId();
            assertEquals(conversationId, afterNextCallConversationId);

        } finally {
            processClient.abortProcessInstance(CONTAINER_ID, processInstanceId);
        }
    }

    @Test
    public void testFindTaskEvents() throws Exception {
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("stringData", "waiting for signal");
        parameters.put("personData", createPersonInstance(USER_JOHN));

        Long processInstanceId = processClient.startProcess(CONTAINER_ID, PROCESS_ID_USERTASK, parameters);

        try {

            List<TaskSummary> tasks = taskClient.findTasksByStatusByProcessInstanceId(processInstanceId, null, 0, 10);
            assertNotNull(tasks);
            assertEquals(1, tasks.size());

            TaskSummary taskInstance = tasks.get(0);

            TaskEventInstance expectedTaskEventInstance = TaskEventInstance
                    .builder()
                    .type(TaskEvent.TaskEventType.ADDED.toString())
                    .processInstanceId(processInstanceId)
                    .taskId(taskInstance.getId())
                    .user(PROCESS_ID_USERTASK)
                    .build();

            List<TaskEventInstance> events = taskClient.findTaskEvents(CONTAINER_ID, taskInstance.getId(), 0, 10);
            assertNotNull(events);
            assertEquals(2, events.size());
            assertTaskEventInstance(expectedTaskEventInstance, events.get(0));

            // now let's start it
            taskClient.startTask(CONTAINER_ID, taskInstance.getId(), USER_YODA);
            events = taskClient.findTaskEvents(CONTAINER_ID, taskInstance.getId(), 0, 10);
            assertNotNull(events);
            assertEquals(3, events.size());
            assertTaskEventInstance(expectedTaskEventInstance, events.get(0));
            expectedTaskEventInstance.setType(TaskEvent.TaskEventType.STARTED.toString());
            expectedTaskEventInstance.setUserId(USER_YODA);
            assertTaskEventInstance(expectedTaskEventInstance, events.get(2));

        } finally {
            processClient.abortProcessInstance(CONTAINER_ID, processInstanceId);
        }
    }

    @Test
    public void testFindTaskEventsForNotExistingTask() {
        String invalidId = "-999";
        try {
            taskClient.findTaskEvents(CONTAINER_ID, -999L, 0, 10);
            fail("KieServicesException should be thrown when task not found");
        } catch (KieServicesException e) {
            if(configuration.isRest()) {
                KieServerAssert.assertResultContainsString(e.getMessage(),  "Could not find task instance with id \"" + invalidId + "\"");
                KieServicesHttpException httpEx = (KieServicesHttpException) e;
                assertEquals(Integer.valueOf(404), httpEx.getHttpCode());
            } else {
                assertTrue(e.getMessage().contains("No task found with id " + invalidId));
            }
        }
    }

    @Test
    public void testFindTaskEventsSortedBy() throws Exception {
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("stringData", "waiting for signal");
        parameters.put("personData", createPersonInstance(USER_JOHN));

        Long processInstanceId = processClient.startProcess(CONTAINER_ID, PROCESS_ID_USERTASK, parameters);

        try {

            List<TaskSummary> tasks = taskClient.findTasksByStatusByProcessInstanceId(processInstanceId, null, 0, 10);
            assertNotNull(tasks);
            assertEquals(1, tasks.size());

            TaskSummary taskInstance = tasks.get(0);

            taskClient.startTask(CONTAINER_ID, taskInstance.getId(), USER_YODA);
            taskClient.stopTask(CONTAINER_ID, taskInstance.getId(), USER_YODA);

            List<TaskEventInstance> events = taskClient.findTaskEvents(CONTAINER_ID, taskInstance.getId(), 0, 4, SORT_BY_TASK_EVENTS_TYPE, true);
            assertEquals(TaskEvent.TaskEventType.ACTIVATED.toString(), events.get(0).getType());
            assertEquals(TaskEvent.TaskEventType.ADDED.toString(), events.get(1).getType());
            assertEquals(TaskEvent.TaskEventType.STARTED.toString(), events.get(2).getType());
            assertEquals(TaskEvent.TaskEventType.STOPPED.toString(), events.get(3).getType());

            try {
                events = taskClient.findTaskEvents(CONTAINER_ID, taskInstance.getId(), 2, 3, SORT_BY_TASK_EVENTS_TYPE, true);
                KieServerAssert.assertNullOrEmpty("Task events list is not empty.", events);
            } catch (KieServicesException ee) {
                if(configuration.isRest()) {
                    KieServerAssert.assertResultContainsString(ee.getMessage(), "Could not find task instance with id " + taskInstance.getId());
                    KieServicesHttpException httpEx = (KieServicesHttpException) ee;
                    assertEquals(Integer.valueOf(404), httpEx.getHttpCode());
                } else {
                    assertTrue(ee.getMessage().contains( "No task found with id " + taskInstance.getId() ));
                }
            }

            events = taskClient.findTaskEvents(CONTAINER_ID, taskInstance.getId(), 0, 4, SORT_BY_TASK_EVENTS_TYPE, false);
            assertEquals(TaskEvent.TaskEventType.STOPPED.toString(), events.get(0).getType());
            assertEquals(TaskEvent.TaskEventType.STARTED.toString(), events.get(1).getType());
            assertEquals(TaskEvent.TaskEventType.ADDED.toString(), events.get(2).getType());
            assertEquals(TaskEvent.TaskEventType.ACTIVATED.toString(), events.get(3).getType());
        } finally {
            processClient.abortProcessInstance(CONTAINER_ID, processInstanceId);
        }
    }

    @Test
    public void testUserTaskUpdate() throws Exception {
        Long processInstanceId = processClient.startProcess(CONTAINER_ID, PROCESS_ID_USERTASK);
        assertNotNull(processInstanceId);
        assertTrue(processInstanceId.longValue() > 0);
        try {
            List<TaskSummary> taskList = taskClient.findTasksAssignedAsPotentialOwner(USER_YODA, 0, 10);
            assertNotNull(taskList);

            assertEquals(1, taskList.size());
            TaskSummary taskSummary = taskList.get(0);

            // verify current task properties
            assertEquals(0, taskSummary.getPriority().intValue());
            assertNull(taskSummary.getExpirationTime());
            assertEquals("First task", taskSummary.getName());
            KieServerAssert.assertNullOrEmpty(taskSummary.getDescription());

            // set task properties
            Calendar currentTime = Calendar.getInstance();
            currentTime.add(Calendar.DAY_OF_YEAR, 1);
            Date expirationDate = currentTime.getTime();

            TaskInstance task = TaskInstance.builder()
                    .name("Modified name")
                    .description("Simple user task.")
                    .priority(10)
                    .expirationTime(expirationDate)
                    .build();

            taskClient.updateTask(CONTAINER_ID, taskSummary.getId(), USER_YODA, task);

            // retrieve started task
            TaskInstance taskInstance = taskClient.getTaskInstance(CONTAINER_ID, taskSummary.getId());

            // verify modified task properties
            assertEquals(10, taskInstance.getPriority().intValue());
            assertNotNull(taskInstance.getExpirationDate());
            assertEquals("Modified name", taskInstance.getName());
            assertEquals("Simple user task.", taskInstance.getDescription());
        } finally {
            processClient.abortProcessInstance(CONTAINER_ID, processInstanceId);
        }
    }

    @Test
    public void testUserTaskUpdateWithData() throws Exception {
        Long processInstanceId = processClient.startProcess(CONTAINER_ID, PROCESS_ID_USERTASK);
        assertNotNull(processInstanceId);
        assertTrue(processInstanceId.longValue() > 0);
        try {
            List<TaskSummary> taskList = taskClient.findTasksAssignedAsPotentialOwner(USER_YODA, 0, 10);
            assertNotNull(taskList);

            assertEquals(1, taskList.size());
            TaskSummary taskSummary = taskList.get(0);

            // verify current task properties
            assertEquals(0, taskSummary.getPriority().intValue());
            assertNull(taskSummary.getExpirationTime());
            assertEquals("First task", taskSummary.getName());
            KieServerAssert.assertNullOrEmpty(taskSummary.getDescription());

            // set task properties
            Calendar currentTime = Calendar.getInstance();
            currentTime.add(Calendar.DAY_OF_YEAR, 1);
            Date expirationDate = currentTime.getTime();

            Map<String, Object> inputData = new HashMap<>();
            inputData.put("added input", "test");

            Map<String, Object> outputData = new HashMap<>();
            outputData.put("string_", "my custom data");
            outputData.put("person_", createPersonInstance(USER_MARY));

            TaskInstance task = TaskInstance.builder()
                    .name("Modified name")
                    .description("Simple user task.")
                    .priority(10)
                    .expirationTime(expirationDate)
                    .inputData(inputData)
                    .outputData(outputData)
                    .build();

            taskClient.updateTask(CONTAINER_ID, taskSummary.getId(), USER_YODA, task);

            // retrieve started task
            TaskInstance taskInstance = taskClient.getTaskInstance(CONTAINER_ID, taskSummary.getId(), true, true, false);

            // verify modified task properties
            assertEquals(10, taskInstance.getPriority().intValue());
            assertNotNull(taskInstance.getExpirationDate());
            assertEquals("Modified name", taskInstance.getName());
            assertEquals("Simple user task.", taskInstance.getDescription());

            String inputVar = (String) taskInstance.getInputData().get("added input");
            assertNotNull(inputVar);
            assertEquals("test", inputVar);

            Object personVar = taskInstance.getOutputData().get("person_");
            assertNotNull(personVar);
            assertEquals(USER_MARY, KieServerReflections.valueOf(personVar, "name"));

            String stringVar = (String) taskInstance.getOutputData().get("string_");
            assertNotNull(personVar);
            assertEquals("my custom data", stringVar);
        } finally {
            processClient.abortProcessInstance(CONTAINER_ID, processInstanceId);
        }
    }
    
    @Test
    public void testAllowedUserTaskUpdateOutputVariable(){
        Long processInstanceId = processClient.startProcess(CONTAINER_ID, PROCESS_ID_USERTASK);
        assertNotNull(processInstanceId);
        assertTrue(processInstanceId.longValue() > 0);
        try {
            List<TaskSummary> taskList = taskClient.findTasksAssignedAsPotentialOwner(USER_YODA, 0, 10);
            assertNotNull(taskList);

            assertEquals(1, taskList.size());
            TaskSummary taskSummary = taskList.get(0);
            
            taskClient.startTask(CONTAINER_ID, taskSummary.getId(), USER_YODA);

            Map<String, Object> outputData = new HashMap<>();
            outputData.put("string_", "my custom data");
            outputData.put("person_", createPersonInstance(USER_MARY));
            
            taskClient.saveTaskContent(CONTAINER_ID, taskSummary.getId(), outputData);
            
            // retrieve started task
            TaskInstance taskInstance = taskClient.getTaskInstance(CONTAINER_ID, taskSummary.getId(), true, true, false);

            Object personVar = taskInstance.getOutputData().get("person_");
            assertNotNull(personVar);
            assertEquals(USER_MARY, KieServerReflections.valueOf(personVar, "name"));

            String stringVar = (String) taskInstance.getOutputData().get("string_");
            assertNotNull(personVar);
            assertEquals("my custom data", stringVar);
        } finally {
            processClient.abortProcessInstance(CONTAINER_ID, processInstanceId);
        }
    }
    
    @Test
    public void testStartAndStopWithWrongContainerId() throws Exception {
        Long processInstanceId = processClient.startProcess(CONTAINER_ID, PROCESS_ID_USERTASK);
        assertNotNull(processInstanceId);
        assertTrue(processInstanceId.longValue() > 0);
        try {
            List<TaskSummary> taskList = taskClient.findTasksAssignedAsPotentialOwner(USER_YODA, 0, 10);
            assertNotNull(taskList);

            assertEquals(1, taskList.size());
            TaskSummary taskSummary = taskList.get(0);
            checkTaskNameAndStatus(taskSummary, "First task", Status.Reserved);

            // release task
            taskClient.startTask(CONTAINER_ID, taskSummary.getId(), USER_YODA);

            taskList = taskClient.findTasksAssignedAsPotentialOwner(USER_YODA, 0, 10);
            assertNotNull(taskList);

            assertEquals(1, taskList.size());
            taskSummary = taskList.get(0);
            checkTaskNameAndStatus(taskSummary, "First task", Status.InProgress);
            
            Long taskId = taskSummary.getId();
            // create another container with different id to make sure it cannot be used to stop task
            createContainer(CONTAINER_ID_V2, releaseId, "custom-alias");
            
            assertClientException(
                                  () -> taskClient.stopTask(CONTAINER_ID_V2, taskId, USER_YODA),
                                  404,
                                  "Could not find task instance",
                                  "Task with id " + taskId + " is not associated with " + CONTAINER_ID_V2);

            // task should be still in progress
            taskList = taskClient.findTasksAssignedAsPotentialOwner(USER_YODA, 0, 10);
            assertNotNull(taskList);

            assertEquals(1, taskList.size());
            taskSummary = taskList.get(0);
            checkTaskNameAndStatus(taskSummary, "First task", Status.InProgress);

        } finally {
            processClient.abortProcessInstance(CONTAINER_ID, processInstanceId);
            KieServerAssert.assertSuccess(client.disposeContainer(CONTAINER_ID_V2));
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
