/*
 * Copyright 2015 JBoss Inc
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

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.BeforeClass;
import org.junit.Test;
import org.kie.api.KieServices;
import org.kie.api.task.model.Status;
import org.kie.server.api.model.KieContainerResource;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.instance.TaskInstance;
import org.kie.server.api.model.instance.TaskSummary;
import org.kie.server.client.KieServicesClient;
import org.kie.server.client.KieServicesConfiguration;
import org.kie.server.client.KieServicesFactory;
import org.kie.server.integrationtests.config.TestConfig;

import static org.junit.Assert.*;

public class UserTaskServiceIntegrationTest extends JbpmKieServerBaseIntegrationTest {

    private static ReleaseId releaseId = new ReleaseId("org.kie.server.testing", "definition-project",
            "1.0.0.Final");


    @BeforeClass
    public static void buildAndDeployArtifacts() {

        buildAndDeployCommonMavenParent();
        buildAndDeployMavenProject(ClassLoader.class.getResource("/kjars-sources/definition-project").getFile());

        kieContainer = KieServices.Factory.get().newKieContainer(releaseId);
    }

    protected KieServicesClient createDefaultClient() {
        Set<Class<?>> extraClasses = new HashSet<Class<?>>();
        try {
            extraClasses.add(Class.forName("org.jbpm.data.Person", true, kieContainer.getClassLoader()));
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        KieServicesClient kieServicesClient = null;

        if (TestConfig.isLocalServer()) {
            KieServicesConfiguration localServerConfig =
                    KieServicesFactory.newRestConfiguration(TestConfig.getHttpUrl(), null, null).setMarshallingFormat(marshallingFormat);
            localServerConfig.setTimeout(30000);
            localServerConfig.addJaxbClasses(extraClasses);
            kieServicesClient =  KieServicesFactory.newKieServicesClient(localServerConfig, kieContainer.getClassLoader());
        } else {
            configuration.setMarshallingFormat(marshallingFormat);
            configuration.addJaxbClasses(extraClasses);
            kieServicesClient =  KieServicesFactory.newKieServicesClient(configuration, kieContainer.getClassLoader());
        }

        setupClients(kieServicesClient);

        return kieServicesClient;
    }

    @Test
    public void testProcessWithUserTasks() throws Exception {
        assertSuccess(client.createContainer("definition-project", new KieContainerResource("definition-project", releaseId)));
        Long processInstanceId = processClient.startProcess("definition-project", "definition-project.usertask");
        assertNotNull(processInstanceId);
        assertTrue(processInstanceId.longValue() > 0);
        try {
            List<TaskSummary> taskList = taskClient.findTasksAssignedAsPotentialOwner("yoda", 0, 10);
            assertNotNull(taskList);

            assertEquals(1, taskList.size());
            TaskSummary taskSummary = taskList.get(0);
            assertEquals("First task", taskSummary.getName());

            // startTask and completeTask task
            taskClient.startTask("definition-project", taskSummary.getId(), "yoda");

            Map<String, Object> taskOutcome = new HashMap<String, Object>();
            taskOutcome.put("string_", "my custom data");
            taskOutcome.put("person_", createPersonInstance("mary"));

            taskClient.completeTask("definition-project", taskSummary.getId(), "yoda", taskOutcome);

            // check if task outcomes are properly set as process variables
            Object personVar = processClient.getProcessInstanceVariable("definition-project", processInstanceId, "personData");
            assertNotNull(personVar);
            assertEquals("mary", valueOf(personVar, "name"));

            String stringVar = (String) processClient.getProcessInstanceVariable("definition-project", processInstanceId, "stringData");
            assertNotNull(personVar);
            assertEquals("my custom data", stringVar);

            taskList = taskClient.findTasksAssignedAsPotentialOwner("yoda", 0, 10);
            assertNotNull(taskList);

            assertEquals(1, taskList.size());
            taskSummary = taskList.get(0);
            assertEquals("Second task", taskSummary.getName());

        } finally {
            processClient.abortProcessInstance("definition-project", processInstanceId);
        }
    }

    @Test
    public void testReleaseAndClaim() throws Exception {
        assertSuccess(client.createContainer("definition-project", new KieContainerResource("definition-project", releaseId)));
        Long processInstanceId = processClient.startProcess("definition-project", "definition-project.usertask");
        assertNotNull(processInstanceId);
        assertTrue(processInstanceId.longValue() > 0);
        try {
            List<TaskSummary> taskList = taskClient.findTasksAssignedAsPotentialOwner("yoda", 0, 10);
            assertNotNull(taskList);

            assertEquals(1, taskList.size());
            TaskSummary taskSummary = taskList.get(0);
            assertEquals("First task", taskSummary.getName());
            assertEquals("Reserved", taskSummary.getStatus());

            // release task
            taskClient.releaseTask("definition-project", taskSummary.getId(), "yoda");

            taskList = taskClient.findTasksAssignedAsPotentialOwner("yoda", 0, 10);
            assertNotNull(taskList);

            assertEquals(1, taskList.size());
            taskSummary = taskList.get(0);
            assertEquals("First task", taskSummary.getName());
            assertEquals("Ready", taskSummary.getStatus());

            taskClient.claimTask("definition-project", taskSummary.getId(), "yoda");

            taskList = taskClient.findTasksAssignedAsPotentialOwner("yoda", 0, 10);
            assertNotNull(taskList);

            assertEquals(1, taskList.size());
            taskSummary = taskList.get(0);
            assertEquals("First task", taskSummary.getName());
            assertEquals("Reserved", taskSummary.getStatus());

        } finally {
            processClient.abortProcessInstance("definition-project", processInstanceId);
        }
    }

    @Test
    public void testStartAndStop() throws Exception {
        assertSuccess(client.createContainer("definition-project", new KieContainerResource("definition-project", releaseId)));
        Long processInstanceId = processClient.startProcess("definition-project", "definition-project.usertask");
        assertNotNull(processInstanceId);
        assertTrue(processInstanceId.longValue() > 0);
        try {
            List<TaskSummary> taskList = taskClient.findTasksAssignedAsPotentialOwner("yoda", 0, 10);
            assertNotNull(taskList);

            assertEquals(1, taskList.size());
            TaskSummary taskSummary = taskList.get(0);
            assertEquals("First task", taskSummary.getName());
            assertEquals("Reserved", taskSummary.getStatus());

            // release task
            taskClient.startTask("definition-project", taskSummary.getId(), "yoda");

            taskList = taskClient.findTasksAssignedAsPotentialOwner("yoda", 0, 10);
            assertNotNull(taskList);

            assertEquals(1, taskList.size());
            taskSummary = taskList.get(0);
            assertEquals("First task", taskSummary.getName());
            assertEquals("InProgress", taskSummary.getStatus());

            taskClient.stopTask("definition-project", taskSummary.getId(), "yoda");

            taskList = taskClient.findTasksAssignedAsPotentialOwner("yoda", 0, 10);
            assertNotNull(taskList);

            assertEquals(1, taskList.size());
            taskSummary = taskList.get(0);
            assertEquals("First task", taskSummary.getName());
            assertEquals("Reserved", taskSummary.getStatus());

        } finally {
            processClient.abortProcessInstance("definition-project", processInstanceId);
        }
    }

    @Test
    public void testSuspendAndResume() throws Exception {
        assertSuccess(client.createContainer("definition-project", new KieContainerResource("definition-project", releaseId)));
        Long processInstanceId = processClient.startProcess("definition-project", "definition-project.usertask");
        assertNotNull(processInstanceId);
        assertTrue(processInstanceId.longValue() > 0);
        try {
            List<TaskSummary> taskList = taskClient.findTasksAssignedAsPotentialOwner("yoda", 0, 10);
            assertNotNull(taskList);

            assertEquals(1, taskList.size());
            TaskSummary taskSummary = taskList.get(0);
            assertEquals("First task", taskSummary.getName());
            assertEquals("Reserved", taskSummary.getStatus());

            // release task
            taskClient.suspendTask("definition-project", taskSummary.getId(), "yoda");

            taskList = taskClient.findTasksAssignedAsPotentialOwner("yoda", 0, 10);
            assertNotNull(taskList);
            if (taskList.size() > 0) {
                fail("Should not be any tasks for yoda as potential owner");
            }

            taskClient.resumeTask("definition-project", taskSummary.getId(), "yoda");

            taskList = taskClient.findTasksAssignedAsPotentialOwner("yoda", 0, 10);
            assertNotNull(taskList);

            assertEquals(1, taskList.size());
            taskSummary = taskList.get(0);
            assertEquals("First task", taskSummary.getName());
            assertEquals("Reserved", taskSummary.getStatus());

        } finally {
            processClient.abortProcessInstance("definition-project", processInstanceId);
        }
    }

    @Test
    public void testFailUserTask() throws Exception {
        assertSuccess(client.createContainer("definition-project", new KieContainerResource("definition-project", releaseId)));
        Long processInstanceId = processClient.startProcess("definition-project", "definition-project.usertask");
        assertNotNull(processInstanceId);
        assertTrue(processInstanceId.longValue() > 0);
        try {
            List<TaskSummary> taskList = taskClient.findTasksAssignedAsPotentialOwner("yoda", 0, 10);
            assertNotNull(taskList);

            assertEquals(1, taskList.size());
            TaskSummary taskSummary = taskList.get(0);
            assertEquals("First task", taskSummary.getName());

            // startTask and completeTask task
            taskClient.startTask("definition-project", taskSummary.getId(), "yoda");

            Map<String, Object> taskOutcome = new HashMap<String, Object>();
            taskOutcome.put("string_", "my custom data");
            taskOutcome.put("person_", createPersonInstance("mary"));

            taskClient.failTask("definition-project", taskSummary.getId(), "yoda", taskOutcome);

            taskList = taskClient.findTasksAssignedAsPotentialOwner("yoda", 0, 10);
            assertNotNull(taskList);

            assertEquals(1, taskList.size());
            taskSummary = taskList.get(0);
            assertEquals("Second task", taskSummary.getName());

        } finally {
            processClient.abortProcessInstance("definition-project", processInstanceId);
        }
    }

    @Test
    public void testSkipUserTask() throws Exception {
        assertSuccess(client.createContainer("definition-project", new KieContainerResource("definition-project", releaseId)));
        Long processInstanceId = processClient.startProcess("definition-project", "definition-project.usertask");
        assertNotNull(processInstanceId);
        assertTrue(processInstanceId.longValue() > 0);
        try {
            List<TaskSummary> taskList = taskClient.findTasksAssignedAsPotentialOwner("yoda", 0, 10);
            assertNotNull(taskList);

            assertEquals(1, taskList.size());
            TaskSummary taskSummary = taskList.get(0);
            assertEquals("First task", taskSummary.getName());
            assertTrue(taskSummary.getSkipable().booleanValue());

            taskClient.skipTask("definition-project", taskSummary.getId(), "yoda");

            taskList = taskClient.findTasksAssignedAsPotentialOwner("yoda", 0, 10);
            assertNotNull(taskList);

            assertEquals(1, taskList.size());
            taskSummary = taskList.get(0);
            assertEquals("Second task", taskSummary.getName());

        } finally {
            processClient.abortProcessInstance("definition-project", processInstanceId);
        }
    }

    @Test
    public void testUserTaskContentOperations() throws Exception {
        assertSuccess(client.createContainer("definition-project", new KieContainerResource("definition-project", releaseId)));

        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("stringData", "john is working on it");
        parameters.put("personData", createPersonInstance("john"));

        Long processInstanceId = processClient.startProcess("definition-project", "definition-project.usertask", parameters);
        assertNotNull(processInstanceId);
        assertTrue(processInstanceId.longValue() > 0);
        try {
            List<TaskSummary> taskList = taskClient.findTasksAssignedAsPotentialOwner("yoda", 0, 10);
            assertNotNull(taskList);
         ;
            assertEquals(1, taskList.size());
            TaskSummary taskSummary = taskList.get(0);
            assertEquals("First task", taskSummary.getName());

            // start task
            taskClient.startTask("definition-project", taskSummary.getId(), "yoda");

            // first verify task input
            Map<String, Object> taskInput = taskClient.getTaskInputContentByTaskId("definition-project", taskSummary.getId());
            assertNotNull(taskInput);
            assertEquals(5, taskInput.size());
            assertTrue(taskInput.containsKey("_string"));
            assertTrue(taskInput.containsKey("_person"));

            assertEquals("john is working on it", taskInput.get("_string"));
            assertEquals("john", valueOf(taskInput.get("_person"), "name"));

            Map<String, Object> taskOutcome = new HashMap<String, Object>();
            taskOutcome.put("string_", "my custom data");
            taskOutcome.put("person_", createPersonInstance("mary"));

            Long outputContentId = taskClient.saveTaskContent("definition-project", taskSummary.getId(), taskOutcome);

            taskInput = taskClient.getTaskOutputContentByTaskId("definition-project", taskSummary.getId());
            assertNotNull(taskInput);
            assertEquals(2, taskInput.size());
            assertTrue(taskInput.containsKey("string_"));
            assertTrue(taskInput.containsKey("person_"));

            assertEquals("my custom data", taskInput.get("string_"));
            assertEquals("mary", valueOf(taskInput.get("person_"), "name"));

            // let's delete the content as we won't need it
            taskClient.deleteTaskContent("definition-project", taskSummary.getId(), outputContentId);

            taskInput = taskClient.getTaskOutputContentByTaskId("definition-project", taskSummary.getId());
            assertNotNull(taskInput);
            assertEquals(0, taskInput.size());

            // complete task
            Map<String, Object> taskOutcomeComplete = new HashMap<String, Object>();
            taskOutcomeComplete.put("string_", "my custom data 2");
            taskOutcomeComplete.put("person_", createPersonInstance("peter"));

            taskClient.completeTask("definition-project", taskSummary.getId(), "yoda", taskOutcomeComplete);

            // check if task outcomes are properly set as process variables
            Object personVar = processClient.getProcessInstanceVariable("definition-project", processInstanceId, "personData");
            assertNotNull(personVar);
            assertEquals("peter", valueOf(personVar, "name"));

            String stringVar = (String) processClient.getProcessInstanceVariable("definition-project", processInstanceId, "stringData");
            assertNotNull(personVar);
            assertEquals("my custom data 2", stringVar);

            taskList = taskClient.findTasksAssignedAsPotentialOwner("yoda", 0, 10);
            assertNotNull(taskList);

            assertEquals(1, taskList.size());
            taskSummary = taskList.get(0);
            assertEquals("Second task", taskSummary.getName());

        } finally {
            processClient.abortProcessInstance("definition-project", processInstanceId);
        }
    }

    @Test
    public void testUserTaskById() throws Exception {
        assertSuccess(client.createContainer("definition-project", new KieContainerResource("definition-project", releaseId)));
        Long processInstanceId = processClient.startProcess("definition-project", "definition-project.usertask");
        assertNotNull(processInstanceId);
        assertTrue(processInstanceId.longValue() > 0);
        try {
            List<TaskSummary> taskList = taskClient.findTasksAssignedAsPotentialOwner("yoda", 0, 10);
            assertNotNull(taskList);

            assertEquals(1, taskList.size());
            TaskSummary taskSummary = taskList.get(0);
            assertEquals("First task", taskSummary.getName());

            TaskInstance taskInstance = taskClient.getTaskInstance("definition-project", taskSummary.getId());
            assertNotNull(taskInstance);
            assertEquals("First task", taskInstance.getName());
            assertEquals("", taskInstance.getDescription());
            assertEquals("", taskInstance.getSubject());
            assertEquals("Reserved", taskInstance.getStatus());
            assertEquals(0, taskInstance.getPriority().intValue());
            assertEquals("yoda", taskInstance.getActualOwner());
            assertEquals("yoda", taskInstance.getCreatedBy());
            assertEquals("definition-project.usertask", taskInstance.getProcessId());
            assertEquals("definition-project", taskInstance.getContainerId());
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
            processClient.abortProcessInstance("definition-project", processInstanceId);
        }
    }

    @Test
    public void testUserTaskByIdWithDetails() throws Exception {
        assertSuccess(client.createContainer("definition-project", new KieContainerResource("definition-project", releaseId)));

        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("stringData", "john is working on it");
        parameters.put("personData", createPersonInstance("john"));

        Long processInstanceId = processClient.startProcess("definition-project", "definition-project.usertask", parameters);
        assertNotNull(processInstanceId);
        assertTrue(processInstanceId.longValue() > 0);
        try {
            List<TaskSummary> taskList = taskClient.findTasksAssignedAsPotentialOwner("yoda", 0, 10);
            assertNotNull(taskList);

            assertEquals(1, taskList.size());
            TaskSummary taskSummary = taskList.get(0);
            assertEquals("First task", taskSummary.getName());

            TaskInstance taskInstance = taskClient.getTaskInstance("definition-project", taskSummary.getId(), true, true, true);
            assertNotNull(taskInstance);
            assertEquals("First task", taskInstance.getName());
            assertEquals("", taskInstance.getDescription());
            assertEquals("", taskInstance.getSubject());
            assertEquals("Reserved", taskInstance.getStatus());
            assertEquals(0, taskInstance.getPriority().intValue());
            assertEquals("yoda", taskInstance.getActualOwner());
            assertEquals("yoda", taskInstance.getCreatedBy());
            assertEquals("definition-project.usertask", taskInstance.getProcessId());
            assertEquals("definition-project", taskInstance.getContainerId());
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
            assertTrue(taskInstance.getPotentialOwners().contains("yoda"));

            assertNotNull(taskInstance.getBusinessAdmins());
            assertEquals(2, taskInstance.getBusinessAdmins().size());
            assertTrue(taskInstance.getBusinessAdmins().contains("Administrator"));
            assertTrue(taskInstance.getBusinessAdmins().contains("Administrators"));

            assertNotNull(taskInstance.getInputData());
            assertEquals(5, taskInstance.getInputData().size());

            Map<String, Object> inputs = taskInstance.getInputData();
            assertTrue(inputs.containsKey("ActorId"));
            assertTrue(inputs.containsKey("_string"));
            assertTrue(inputs.containsKey("Skippable"));
            assertTrue(inputs.containsKey("_person"));
            assertTrue(inputs.containsKey("NodeName"));

            assertEquals("yoda", inputs.get("ActorId"));
            assertEquals("john is working on it", inputs.get("_string"));
            assertEquals("true", inputs.get("Skippable"));
            assertEquals("First task", inputs.get("NodeName"));
            assertEquals("john", valueOf(inputs.get("_person"), "name"));

            assertNotNull(taskInstance.getOutputData());
            assertEquals(0, taskInstance.getOutputData().size());

        } finally {
            processClient.abortProcessInstance("definition-project", processInstanceId);
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
        assertSuccess(client.createContainer("definition-project", new KieContainerResource("definition-project", releaseId)));
        Long processInstanceId = processClient.startProcess("definition-project", "definition-project.usertask");
        assertNotNull(processInstanceId);
        assertTrue(processInstanceId.longValue() > 0);
        try {
            List<TaskSummary> taskList = taskClient.findTasksAssignedAsPotentialOwner("yoda", 0, 10);
            assertNotNull(taskList);

            assertEquals(1, taskList.size());
            TaskSummary taskSummary = taskList.get(0);
            assertEquals("First task", taskSummary.getName());

            checkTaskStatusAndActualOwner("definition-project", taskSummary.getId(), Status.Reserved, "yoda");

            // forwarding Reserved task to john (forwarding Reserved -> Ready)
            taskClient.forwardTask("definition-project", taskSummary.getId(), "yoda", "john");

            // user yoda has empty task list now
            taskList = taskClient.findTasksAssignedAsPotentialOwner("yoda", 0, 10);
            assertNotNull(taskList);
            assertEquals(0, taskList.size());

            // forwarded task is set to Ready state and is assigned to john as potential owner
            changeUser("john");
            taskList = taskClient.findTasksAssignedAsPotentialOwner("john", 0, 10);
            assertNotNull(taskList);
            assertEquals(1, taskList.size());
            assertEquals(taskSummary.getId(), taskList.get(0).getId());

            checkTaskStatusAndOwners("definition-project", taskSummary.getId(), Status.Ready, "", "john");

            // forwarding task in Ready state back to yoda (forwarding Ready -> Ready)
            taskClient.forwardTask("definition-project", taskSummary.getId(), "john", "yoda");

            // forwarded task stays in Ready state and is assigned to yoda as potential owner
            changeUser("yoda");
            checkTaskStatusAndOwners("definition-project", taskSummary.getId(), Status.Ready, "", "yoda");

            // starting task to change its status to In progress
            taskClient.startTask("definition-project", taskSummary.getId(), "yoda");
            checkTaskStatusAndActualOwner("definition-project", taskSummary.getId(), Status.InProgress, "yoda");

            // forwarding task In progress back to john (forwarding In progress -> Ready)
            taskClient.forwardTask("definition-project", taskSummary.getId(), "yoda", "john");

            // forwarded task change state to Ready and is assigned to john as potential owner
            changeUser("john");
            checkTaskStatusAndOwners("definition-project", taskSummary.getId(), Status.Ready, "", "john");
        } finally {
            processClient.abortProcessInstance("definition-project", processInstanceId);
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
        assertSuccess(client.createContainer("definition-project", new KieContainerResource("definition-project", releaseId)));
        Long processInstanceId = processClient.startProcess("definition-project", "definition-project.usertask");
        assertNotNull(processInstanceId);
        assertTrue(processInstanceId.longValue() > 0);
        try {
            List<TaskSummary> taskList = taskClient.findTasksAssignedAsPotentialOwner("yoda", 0, 10);
            assertNotNull(taskList);

            assertEquals(1, taskList.size());
            TaskSummary taskSummary = taskList.get(0);
            assertEquals("First task", taskSummary.getName());

            checkTaskStatusAndActualOwner("definition-project", taskSummary.getId(), Status.Reserved, "yoda");

            // delegating Reserved task to john (delegating Reserved -> Reserved)
            taskClient.delegateTask("definition-project", taskSummary.getId(), "yoda", "john");

            // user yoda has empty task list now
            taskList = taskClient.findTasksAssignedAsPotentialOwner("yoda", 0, 10);
            assertNotNull(taskList);
            assertEquals(0, taskList.size());

            // delegated task stays in Reserved state and is assigned to john as actual owner
            changeUser("john");
            taskList = taskClient.findTasksAssignedAsPotentialOwner("john", 0, 10);
            assertNotNull(taskList);
            assertEquals(1, taskList.size());
            assertEquals(taskSummary.getId(), taskList.get(0).getId());

            checkTaskStatusAndActualOwner("definition-project", taskSummary.getId(), Status.Reserved, "john");

            // releasing task to change its state to Ready
            taskClient.releaseTask("definition-project", taskSummary.getId(), "john");
            checkTaskStatusAndActualOwner("definition-project", taskSummary.getId(), Status.Ready, "");

            // delegating task in Ready state back to yoda (delegating Ready -> Reserved)
            taskClient.delegateTask("definition-project", taskSummary.getId(), "john", "yoda");

            // delegated task change its state to Reserved and is assigned to yoda as actual owner
            changeUser("yoda");
            checkTaskStatusAndActualOwner("definition-project", taskSummary.getId(), Status.Reserved, "yoda");

            // starting task to change its status to In progress
            taskClient.startTask("definition-project", taskSummary.getId(), "yoda");
            checkTaskStatusAndActualOwner("definition-project", taskSummary.getId(), Status.InProgress, "yoda");

            // delegating task In progress back to john (delegating In progress -> Reserved)
            taskClient.delegateTask("definition-project", taskSummary.getId(), "yoda", "john");

            // delegated task change state to Reserved and is assigned to john as actual owner
            changeUser("john");
            checkTaskStatusAndActualOwner("definition-project", taskSummary.getId(), Status.Reserved, "john");
        } finally {
            processClient.abortProcessInstance("definition-project", processInstanceId);
            changeUser(TestConfig.getUsername());
        }
    }

    @Test
    public void testUserTaskSetTaskProperties() throws Exception {
        assertSuccess(client.createContainer("definition-project", new KieContainerResource("definition-project", releaseId)));

        Long processInstanceId = processClient.startProcess("definition-project", "definition-project.usertask");
        assertNotNull(processInstanceId);
        assertTrue(processInstanceId.longValue() > 0);
        try {
            List<TaskSummary> taskList = taskClient.findTasksAssignedAsPotentialOwner("yoda", 0, 10);
            assertNotNull(taskList);

            assertEquals(1, taskList.size());
            TaskSummary taskSummary = taskList.get(0);

            // verify current task properties
            assertEquals(0, taskSummary.getPriority().intValue());
            assertNull(taskSummary.getExpirationTime());
            assertTrue(taskSummary.getSkipable().booleanValue());
            assertEquals("First task", taskSummary.getName());
            assertTrue(taskSummary.getDescription().isEmpty());

            // set task properties
            Calendar currentTime = Calendar.getInstance();
            currentTime.add(Calendar.DAY_OF_YEAR, 1);
            Date expirationDate = currentTime.getTime();
            taskClient.setTaskDescription("definition-project", taskSummary.getId(), "Simple user task.");
            taskClient.setTaskExpirationDate("definition-project", taskSummary.getId(), expirationDate);
            taskClient.setTaskName("definition-project", taskSummary.getId(), "Modified name");
            taskClient.setTaskPriority("definition-project", taskSummary.getId(), 10);
            taskClient.setTaskSkipable("definition-project", taskSummary.getId(), false);

            // start task
            taskClient.startTask("definition-project", taskSummary.getId(), "yoda");

            // retrieve started task
            TaskInstance taskInstance = taskClient.getTaskInstance("definition-project", taskSummary.getId());

            // verify modified task properties
            assertEquals(10, taskInstance.getPriority().intValue());
            assertEquals(expirationDate.getTime(), taskInstance.getExpirationTime().getTime());
            assertFalse(taskInstance.getSkipable().booleanValue());
            assertEquals("Modified name", taskInstance.getName());
            assertEquals("Simple user task.", taskInstance.getDescription());
        } finally {
            processClient.abortProcessInstance("definition-project", processInstanceId);
        }
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
