package org.kie.server.integrationtests.jbpm;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.BeforeClass;
import org.junit.Test;
import org.kie.api.KieServices;
import org.kie.api.runtime.KieContainer;
import org.kie.server.api.model.KieContainerResource;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.instance.TaskInstance;
import org.kie.server.api.model.instance.TaskSummary;
import org.kie.server.api.model.instance.TaskSummaryList;
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
        if (TestConfig.isLocalServer()) {
            KieServicesConfiguration localServerConfig =
                    KieServicesFactory.newRestConfiguration(TestConfig.getHttpUrl(), null, null).setMarshallingFormat(marshallingFormat);
            localServerConfig.setTimeout(30000);
            localServerConfig.addJaxbClasses(extraClasses);
            return KieServicesFactory.newKieServicesClient(localServerConfig, kieContainer.getClassLoader());
        } else {
            configuration.setMarshallingFormat(marshallingFormat);
            configuration.addJaxbClasses(extraClasses);
            return KieServicesFactory.newKieServicesClient(configuration, kieContainer.getClassLoader());
        }
    }

    @Test
    public void testProcessWithUserTasks() throws Exception {
        assertSuccess(client.createContainer("definition-project", new KieContainerResource("definition-project", releaseId)));
        Long processInstanceId = client.startProcess("definition-project", "definition-project.usertask");
        assertNotNull(processInstanceId);
        assertTrue(processInstanceId.longValue() > 0);
        try {
            TaskSummaryList taskList = client.getTasksAssignedAsPotentialOwner("definition-project", "yoda", 0, 10);
            assertNotNull(taskList);
            assertNotNull(taskList.getTasks());

            TaskSummary[] tasks = taskList.getTasks();
            assertEquals(1, tasks.length);
            TaskSummary taskSummary = tasks[0];
            assertEquals("First task", taskSummary.getName());

            // startTask and completeTask task
            client.startTask("definition-project", taskSummary.getId(), "yoda");

            Map<String, Object> taskOutcome = new HashMap<String, Object>();
            taskOutcome.put("string_", "my custom data");
            taskOutcome.put("person_", createPersonInstance("mary"));

            client.completeTask("definition-project", taskSummary.getId(), "yoda", taskOutcome);

            // check if task outcomes are properly set as process variables
            Object personVar = client.getProcessInstanceVariable("definition-project", processInstanceId, "personData");
            assertNotNull(personVar);
            assertEquals("mary", valueOf(personVar, "name"));

            String stringVar = (String) client.getProcessInstanceVariable("definition-project", processInstanceId, "stringData");
            assertNotNull(personVar);
            assertEquals("my custom data", stringVar);

            taskList = client.getTasksAssignedAsPotentialOwner("definition-project", "yoda", 0, 10);
            assertNotNull(taskList);
            assertNotNull(taskList.getTasks());

            tasks = taskList.getTasks();
            assertEquals(1, tasks.length);
            taskSummary = tasks[0];
            assertEquals("Second task", taskSummary.getName());

        } finally {
            client.abortProcessInstance("definition-project", processInstanceId);
        }
    }

    @Test
    public void testReleaseAndClaim() throws Exception {
        assertSuccess(client.createContainer("definition-project", new KieContainerResource("definition-project", releaseId)));
        Long processInstanceId = client.startProcess("definition-project", "definition-project.usertask");
        assertNotNull(processInstanceId);
        assertTrue(processInstanceId.longValue() > 0);
        try {
            TaskSummaryList taskList = client.getTasksAssignedAsPotentialOwner("definition-project", "yoda", 0, 10);
            assertNotNull(taskList);
            assertNotNull(taskList.getTasks());

            TaskSummary[] tasks = taskList.getTasks();
            assertEquals(1, tasks.length);
            TaskSummary taskSummary = tasks[0];
            assertEquals("First task", taskSummary.getName());
            assertEquals("Reserved", taskSummary.getStatus());

            // release task
            client.releaseTask("definition-project", taskSummary.getId(), "yoda");

            taskList = client.getTasksAssignedAsPotentialOwner("definition-project", "yoda", 0, 10);
            assertNotNull(taskList);
            assertNotNull(taskList.getTasks());

            tasks = taskList.getTasks();
            assertEquals(1, tasks.length);
            taskSummary = tasks[0];
            assertEquals("First task", taskSummary.getName());
            assertEquals("Ready", taskSummary.getStatus());

            client.claimTask("definition-project", taskSummary.getId(), "yoda");

            taskList = client.getTasksAssignedAsPotentialOwner("definition-project", "yoda", 0, 10);
            assertNotNull(taskList);
            assertNotNull(taskList.getTasks());

            tasks = taskList.getTasks();
            assertEquals(1, tasks.length);
            taskSummary = tasks[0];
            assertEquals("First task", taskSummary.getName());
            assertEquals("Reserved", taskSummary.getStatus());

        } finally {
            client.abortProcessInstance("definition-project", processInstanceId);
        }
    }

    @Test
    public void testStartAndStop() throws Exception {
        assertSuccess(client.createContainer("definition-project", new KieContainerResource("definition-project", releaseId)));
        Long processInstanceId = client.startProcess("definition-project", "definition-project.usertask");
        assertNotNull(processInstanceId);
        assertTrue(processInstanceId.longValue() > 0);
        try {
            TaskSummaryList taskList = client.getTasksAssignedAsPotentialOwner("definition-project", "yoda", 0, 10);
            assertNotNull(taskList);
            assertNotNull(taskList.getTasks());

            TaskSummary[] tasks = taskList.getTasks();
            assertEquals(1, tasks.length);
            TaskSummary taskSummary = tasks[0];
            assertEquals("First task", taskSummary.getName());
            assertEquals("Reserved", taskSummary.getStatus());

            // release task
            client.startTask("definition-project", taskSummary.getId(), "yoda");

            taskList = client.getTasksAssignedAsPotentialOwner("definition-project", "yoda", 0, 10);
            assertNotNull(taskList);
            assertNotNull(taskList.getTasks());

            tasks = taskList.getTasks();
            assertEquals(1, tasks.length);
            taskSummary = tasks[0];
            assertEquals("First task", taskSummary.getName());
            assertEquals("InProgress", taskSummary.getStatus());

            client.stopTask("definition-project", taskSummary.getId(), "yoda");

            taskList = client.getTasksAssignedAsPotentialOwner("definition-project", "yoda", 0, 10);
            assertNotNull(taskList);
            assertNotNull(taskList.getTasks());

            tasks = taskList.getTasks();
            assertEquals(1, tasks.length);
            taskSummary = tasks[0];
            assertEquals("First task", taskSummary.getName());
            assertEquals("Reserved", taskSummary.getStatus());

        } finally {
            client.abortProcessInstance("definition-project", processInstanceId);
        }
    }

    @Test
    public void testSuspendAndResume() throws Exception {
        assertSuccess(client.createContainer("definition-project", new KieContainerResource("definition-project", releaseId)));
        Long processInstanceId = client.startProcess("definition-project", "definition-project.usertask");
        assertNotNull(processInstanceId);
        assertTrue(processInstanceId.longValue() > 0);
        try {
            TaskSummaryList taskList = client.getTasksAssignedAsPotentialOwner("definition-project", "yoda", 0, 10);
            assertNotNull(taskList);
            assertNotNull(taskList.getTasks());

            TaskSummary[] tasks = taskList.getTasks();
            assertEquals(1, tasks.length);
            TaskSummary taskSummary = tasks[0];
            assertEquals("First task", taskSummary.getName());
            assertEquals("Reserved", taskSummary.getStatus());

            // release task
            client.suspendTask("definition-project", taskSummary.getId(), "yoda");

            taskList = client.getTasksAssignedAsPotentialOwner("definition-project", "yoda", 0, 10);
            assertNotNull(taskList);
            if (taskList.getTasks() != null && taskList.getTasks().length > 0) {
                fail("Should not be any tasks for yoda as potential owner");
            }

            client.resumeTask("definition-project", taskSummary.getId(), "yoda");

            taskList = client.getTasksAssignedAsPotentialOwner("definition-project", "yoda", 0, 10);
            assertNotNull(taskList);
            assertNotNull(taskList.getTasks());

            tasks = taskList.getTasks();
            assertEquals(1, tasks.length);
            taskSummary = tasks[0];
            assertEquals("First task", taskSummary.getName());
            assertEquals("Reserved", taskSummary.getStatus());

        } finally {
            client.abortProcessInstance("definition-project", processInstanceId);
        }
    }

    @Test
    public void testFailUserTask() throws Exception {
        assertSuccess(client.createContainer("definition-project", new KieContainerResource("definition-project", releaseId)));
        Long processInstanceId = client.startProcess("definition-project", "definition-project.usertask");
        assertNotNull(processInstanceId);
        assertTrue(processInstanceId.longValue() > 0);
        try {
            TaskSummaryList taskList = client.getTasksAssignedAsPotentialOwner("definition-project", "yoda", 0, 10);
            assertNotNull(taskList);
            assertNotNull(taskList.getTasks());

            TaskSummary[] tasks = taskList.getTasks();
            assertEquals(1, tasks.length);
            TaskSummary taskSummary = tasks[0];
            assertEquals("First task", taskSummary.getName());

            // startTask and completeTask task
            client.startTask("definition-project", taskSummary.getId(), "yoda");

            Map<String, Object> taskOutcome = new HashMap<String, Object>();
            taskOutcome.put("string_", "my custom data");
            taskOutcome.put("person_", createPersonInstance("mary"));

            client.failTask("definition-project", taskSummary.getId(), "yoda", taskOutcome);

            taskList = client.getTasksAssignedAsPotentialOwner("definition-project", "yoda", 0, 10);
            assertNotNull(taskList);
            assertNotNull(taskList.getTasks());

            tasks = taskList.getTasks();
            assertEquals(1, tasks.length);
            taskSummary = tasks[0];
            assertEquals("Second task", taskSummary.getName());

        } finally {
            client.abortProcessInstance("definition-project", processInstanceId);
        }
    }

    @Test
    public void testSkipUserTask() throws Exception {
        assertSuccess(client.createContainer("definition-project", new KieContainerResource("definition-project", releaseId)));
        Long processInstanceId = client.startProcess("definition-project", "definition-project.usertask");
        assertNotNull(processInstanceId);
        assertTrue(processInstanceId.longValue() > 0);
        try {
            TaskSummaryList taskList = client.getTasksAssignedAsPotentialOwner("definition-project", "yoda", 0, 10);
            assertNotNull(taskList);
            assertNotNull(taskList.getTasks());

            TaskSummary[] tasks = taskList.getTasks();
            assertEquals(1, tasks.length);
            TaskSummary taskSummary = tasks[0];
            assertEquals("First task", taskSummary.getName());

            client.skipTask("definition-project", taskSummary.getId(), "yoda");

            taskList = client.getTasksAssignedAsPotentialOwner("definition-project", "yoda", 0, 10);
            assertNotNull(taskList);
            assertNotNull(taskList.getTasks());

            tasks = taskList.getTasks();
            assertEquals(1, tasks.length);
            taskSummary = tasks[0];
            assertEquals("Second task", taskSummary.getName());

        } finally {
            client.abortProcessInstance("definition-project", processInstanceId);
        }
    }

    @Test
    public void testUserTaskContentOperations() throws Exception {
        assertSuccess(client.createContainer("definition-project", new KieContainerResource("definition-project", releaseId)));

        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("stringData", "john is working on it");
        parameters.put("personData", createPersonInstance("john"));

        Long processInstanceId = client.startProcess("definition-project", "definition-project.usertask", parameters);
        assertNotNull(processInstanceId);
        assertTrue(processInstanceId.longValue() > 0);
        try {
            TaskSummaryList taskList = client.getTasksAssignedAsPotentialOwner("definition-project", "yoda", 0, 10);
            assertNotNull(taskList);
            assertNotNull(taskList.getTasks());

            TaskSummary[] tasks = taskList.getTasks();
            assertEquals(1, tasks.length);
            TaskSummary taskSummary = tasks[0];
            assertEquals("First task", taskSummary.getName());

            // start task
            client.startTask("definition-project", taskSummary.getId(), "yoda");

            // first verify task input
            Map<String, Object> taskInput = client.getTaskInputContentByTaskId("definition-project", taskSummary.getId());
            assertNotNull(taskInput);
            assertEquals(5, taskInput.size());
            assertTrue(taskInput.containsKey("_string"));
            assertTrue(taskInput.containsKey("_person"));

            assertEquals("john is working on it", taskInput.get("_string"));
            assertEquals("john", valueOf(taskInput.get("_person"), "name"));

            Map<String, Object> taskOutcome = new HashMap<String, Object>();
            taskOutcome.put("string_", "my custom data");
            taskOutcome.put("person_", createPersonInstance("mary"));

            Long outputContentId = client.saveTaskContent("definition-project", taskSummary.getId(), taskOutcome);

            taskInput = client.getTaskOutputContentByTaskId("definition-project", taskSummary.getId());
            assertNotNull(taskInput);
            assertEquals(2, taskInput.size());
            assertTrue(taskInput.containsKey("string_"));
            assertTrue(taskInput.containsKey("person_"));

            assertEquals("my custom data", taskInput.get("string_"));
            assertEquals("mary", valueOf(taskInput.get("person_"), "name"));

            // let's delete the content as we won't need it
            client.deleteTaskContent("definition-project", taskSummary.getId(), outputContentId);

            taskInput = client.getTaskOutputContentByTaskId("definition-project", taskSummary.getId());
            assertNotNull(taskInput);
            assertEquals(0, taskInput.size());

            // complete task
            Map<String, Object> taskOutcomeComplete = new HashMap<String, Object>();
            taskOutcomeComplete.put("string_", "my custom data 2");
            taskOutcomeComplete.put("person_", createPersonInstance("peter"));

            client.completeTask("definition-project", taskSummary.getId(), "yoda", taskOutcomeComplete);

            // check if task outcomes are properly set as process variables
            Object personVar = client.getProcessInstanceVariable("definition-project", processInstanceId, "personData");
            assertNotNull(personVar);
            assertEquals("peter", valueOf(personVar, "name"));

            String stringVar = (String) client.getProcessInstanceVariable("definition-project", processInstanceId, "stringData");
            assertNotNull(personVar);
            assertEquals("my custom data 2", stringVar);

            taskList = client.getTasksAssignedAsPotentialOwner("definition-project", "yoda", 0, 10);
            assertNotNull(taskList);
            assertNotNull(taskList.getTasks());

            tasks = taskList.getTasks();
            assertEquals(1, tasks.length);
            taskSummary = tasks[0];
            assertEquals("Second task", taskSummary.getName());

        } finally {
            client.abortProcessInstance("definition-project", processInstanceId);
        }
    }

    @Test
    public void testUserTaskById() throws Exception {
        assertSuccess(client.createContainer("definition-project", new KieContainerResource("definition-project", releaseId)));
        Long processInstanceId = client.startProcess("definition-project", "definition-project.usertask");
        assertNotNull(processInstanceId);
        assertTrue(processInstanceId.longValue() > 0);
        try {
            TaskSummaryList taskList = client.getTasksAssignedAsPotentialOwner("definition-project", "yoda", 0, 10);
            assertNotNull(taskList);
            assertNotNull(taskList.getTasks());

            TaskSummary[] tasks = taskList.getTasks();
            assertEquals(1, tasks.length);
            TaskSummary taskSummary = tasks[0];
            assertEquals("First task", taskSummary.getName());

            TaskInstance taskInstance = client.getTaskInstance("definition-project", taskSummary.getId());
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
            assertNotNull(taskInstance.getInputData());
            assertEquals(0, taskInstance.getInputData().size());
            assertNotNull(taskInstance.getOutputData());
            assertEquals(0, taskInstance.getOutputData().size());
        } finally {
            client.abortProcessInstance("definition-project", processInstanceId);
        }
    }

    @Test
    public void testUserTaskByIdWithDetails() throws Exception {
        assertSuccess(client.createContainer("definition-project", new KieContainerResource("definition-project", releaseId)));

        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("stringData", "john is working on it");
        parameters.put("personData", createPersonInstance("john"));

        Long processInstanceId = client.startProcess("definition-project", "definition-project.usertask", parameters);
        assertNotNull(processInstanceId);
        assertTrue(processInstanceId.longValue() > 0);
        try {
            TaskSummaryList taskList = client.getTasksAssignedAsPotentialOwner("definition-project", "yoda", 0, 10);
            assertNotNull(taskList);
            assertNotNull(taskList.getTasks());

            TaskSummary[] tasks = taskList.getTasks();
            assertEquals(1, tasks.length);
            TaskSummary taskSummary = tasks[0];
            assertEquals("First task", taskSummary.getName());

            TaskInstance taskInstance = client.getTaskInstance("definition-project", taskSummary.getId(), true, true, true);
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
            client.abortProcessInstance("definition-project", processInstanceId);
        }
    }

}
