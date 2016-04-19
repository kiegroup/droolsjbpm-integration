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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.kie.api.KieServices;
import org.kie.api.task.model.Status;
import org.kie.internal.KieInternalServices;
import org.kie.internal.executor.api.STATUS;
import org.kie.internal.process.CorrelationKey;
import org.kie.internal.process.CorrelationKeyFactory;
import org.kie.server.api.model.KieContainerResource;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.instance.ProcessInstance;
import org.kie.server.api.model.instance.RequestInfoInstance;
import org.kie.server.api.model.instance.TaskInstance;
import org.kie.server.api.model.instance.TaskSummary;
import org.kie.server.api.model.instance.WorkItemInstance;
import org.kie.server.client.KieServicesException;
import org.kie.server.integrationtests.category.Smoke;
import org.kie.server.integrationtests.config.TestConfig;

import static org.junit.Assert.*;


public class ProcessServiceIntegrationTest extends JbpmKieServerBaseIntegrationTest {

    private static ReleaseId releaseId = new ReleaseId("org.kie.server.testing", "definition-project",
            "1.0.0.Final");

    private static final String CONTAINER_ID = "definition-project";
    private static final String PERSON_CLASS_NAME = "org.jbpm.data.Person";

    @BeforeClass
    public static void buildAndDeployArtifacts() {

        buildAndDeployCommonMavenParent();
        buildAndDeployMavenProject(ClassLoader.class.getResource("/kjars-sources/definition-project").getFile());

        kieContainer = KieServices.Factory.get().newKieContainer(releaseId);
    }

    @Override
    protected void addExtraCustomClasses(Map<String, Class<?>> extraClasses) throws Exception {
        extraClasses.put(PERSON_CLASS_NAME, Class.forName(PERSON_CLASS_NAME, true, kieContainer.getClassLoader()));
    }

    @Test
    public void testStartCheckVariablesAndAbortProcess() throws Exception {
        assertSuccess(client.createContainer("definition-project", new KieContainerResource("definition-project", releaseId)));
        Class<?> personClass = Class.forName("org.jbpm.data.Person", true, kieContainer.getClassLoader());

        Object person = createPersonInstance("john");

        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("test", "mary");
        parameters.put("number", new Integer(12345));

        List<Object> list = new ArrayList<Object>();
        list.add("item");

        parameters.put("list", list);
        parameters.put("person", person);
        Long processInstanceId = null;
        try {
            processInstanceId = processClient.startProcess("definition-project", "definition-project.evaluation", parameters);

            assertNotNull(processInstanceId);
            assertTrue(processInstanceId.longValue() > 0);

            Object personVariable = processClient.getProcessInstanceVariable("definition-project", processInstanceId, "person");
            assertNotNull(personVariable);
            assertTrue(personClass.isAssignableFrom(personVariable.getClass()));

            personVariable = processClient.getProcessInstanceVariable("definition-project", processInstanceId, "person");
            assertNotNull(personVariable);
            assertTrue(personClass.isAssignableFrom(personVariable.getClass()));

            Map<String, Object> variables = processClient.getProcessInstanceVariables("definition-project", processInstanceId);
            assertNotNull(variables);
            assertEquals(4, variables.size());
            assertTrue(variables.containsKey("test"));
            assertTrue(variables.containsKey("number"));
            assertTrue(variables.containsKey("list"));
            assertTrue(variables.containsKey("person"));

            assertNotNull(variables.get("test"));
            assertNotNull(variables.get("number"));
            assertNotNull(variables.get("list"));
            assertNotNull(variables.get("person"));

            assertTrue(String.class.isAssignableFrom(variables.get("test").getClass()));
            assertTrue(Integer.class.isAssignableFrom(variables.get("number").getClass()));
            assertTrue(List.class.isAssignableFrom(variables.get("list").getClass()));
            assertTrue(personClass.isAssignableFrom(variables.get("person").getClass()));

            assertEquals("mary", variables.get("test"));
            assertEquals(12345, variables.get("number"));
            assertEquals(1, ((List) variables.get("list")).size());
            assertEquals("item", ((List) variables.get("list")).get(0));
            assertEquals("john", valueOf(variables.get("person"), "name"));
        } finally {
            if (processInstanceId != null) {
                processClient.abortProcessInstance("definition-project", processInstanceId);
            }
        }



    }

    @Test(expected = KieServicesException.class)
    public void testStartNotExistingProcess() {
        assertSuccess(client.createContainer("definition-project", new KieContainerResource("definition-project", releaseId)));
        processClient.startProcess("definition-project", "not-existing", (Map)null);
    }

    @Test()
    public void testAbortExistingProcess() {
        assertSuccess(client.createContainer(CONTAINER_ID, new KieContainerResource(CONTAINER_ID, releaseId)));

        Map<String, Object> parameters = new HashMap<String, Object>();

        Long processInstanceId = processClient.startProcess(CONTAINER_ID, PROCESS_ID_EVALUATION, parameters);
        try {
            assertNotNull(processInstanceId);
            assertTrue(processInstanceId.longValue() > 0);

            // Process instance is running and is active.
            ProcessInstance processInstance = processClient.getProcessInstance(CONTAINER_ID, processInstanceId);
            assertNotNull(processInstance);
            assertEquals(org.kie.api.runtime.process.ProcessInstance.STATE_ACTIVE, processInstance.getState().intValue());

            processClient.abortProcessInstance(CONTAINER_ID, processInstanceId);

            // Process instance is now aborted.
            processInstance = processClient.getProcessInstance(CONTAINER_ID, processInstanceId);
            assertNotNull(processInstance);
            assertEquals(org.kie.api.runtime.process.ProcessInstance.STATE_ABORTED, processInstance.getState().intValue());
        } catch (Exception e) {
            processClient.abortProcessInstance(CONTAINER_ID, processInstanceId);
            fail(e.getMessage());
        }
    }

    @Test(expected = KieServicesException.class)
    public void testAbortNonExistingProcess() {
        assertSuccess(client.createContainer(CONTAINER_ID, new KieContainerResource(CONTAINER_ID, releaseId)));
        processClient.abortProcessInstance(CONTAINER_ID, 9999l);
    }

    @Test(expected = KieServicesException.class)
    public void testStartCheckNonExistingVariables() throws Exception {
        assertSuccess(client.createContainer("definition-project", new KieContainerResource("definition-project", releaseId)));

        Map<String, Object> parameters = new HashMap<String, Object>();
        Long processInstanceId = processClient.startProcess("definition-project", "definition-project.evaluation", parameters);
        try {
            processClient.getProcessInstanceVariable("definition-project", processInstanceId, "person");
        } finally {
            processClient.abortProcessInstance("definition-project", processInstanceId);
        }

    }

    @Test
    public void testAbortMultipleProcessInstances() throws Exception {
        assertSuccess(client.createContainer("definition-project", new KieContainerResource("definition-project", releaseId)));


        Long processInstanceId1 = processClient.startProcess("definition-project", "definition-project.evaluation");
        Long processInstanceId2 = processClient.startProcess("definition-project", "definition-project.evaluation");
        Long processInstanceId3 = processClient.startProcess("definition-project", "definition-project.evaluation");
        Long processInstanceId4 = processClient.startProcess("definition-project", "definition-project.evaluation");

        List<Long> processInstances = new ArrayList<Long>();
        processInstances.add(processInstanceId1);
        processInstances.add(processInstanceId2);
        processInstances.add(processInstanceId3);
        processInstances.add(processInstanceId4);

        processClient.abortProcessInstances("definition-project", processInstances);
    }

    @Test
    public void testSignalProcessInstance() throws Exception {
        assertSuccess(client.createContainer("definition-project", new KieContainerResource("definition-project", releaseId)));
        Long processInstanceId = processClient.startProcess("definition-project", "definition-project.signalprocess");
        assertNotNull(processInstanceId);
        assertTrue(processInstanceId.longValue() > 0);

        try {

            List<String> availableSignals = processClient.getAvailableSignals("definition-project", processInstanceId);
            assertNotNull(availableSignals);
            assertEquals(2, availableSignals.size());
            assertTrue(availableSignals.contains("Signal1"));
            assertTrue(availableSignals.contains("Signal2"));

            Object person = createPersonInstance("john");
            processClient.signalProcessInstance("definition-project", processInstanceId, "Signal1", person);

            processClient.signalProcessInstance("definition-project", processInstanceId, "Signal2", "My custom string event");
        } catch (Exception e){
            processClient.abortProcessInstance("definition-project", processInstanceId);
            e.printStackTrace();
            fail(e.getMessage());
        }

    }

    @Test
    public void testSignalProcessInstanceNullEvent() throws Exception {
        assertSuccess(client.createContainer("definition-project", new KieContainerResource("definition-project", releaseId)));
        Long processInstanceId = processClient.startProcess("definition-project", "definition-project.signalprocess");
        assertNotNull(processInstanceId);
        assertTrue(processInstanceId.longValue() > 0);

        try {

            List<String> availableSignals = processClient.getAvailableSignals("definition-project", processInstanceId);
            assertNotNull(availableSignals);
            assertEquals(2, availableSignals.size());
            assertTrue(availableSignals.contains("Signal1"));
            assertTrue(availableSignals.contains("Signal2"));

            processClient.signalProcessInstance("definition-project", processInstanceId, "Signal1", null);

            processClient.signalProcessInstance("definition-project", processInstanceId, "Signal2", null);
        } catch (Exception e){
            processClient.abortProcessInstance("definition-project", processInstanceId);
            e.printStackTrace();
            fail(e.getMessage());
        }

    }

    @Test
    public void testSignalProcessInstances() throws Exception {
        assertSuccess(client.createContainer("definition-project", new KieContainerResource("definition-project", releaseId)));
        Long processInstanceId = processClient.startProcess("definition-project", "definition-project.signalprocess");
        assertNotNull(processInstanceId);
        assertTrue(processInstanceId.longValue() > 0);

        Long processInstanceId2 = processClient.startProcess("definition-project", "definition-project.signalprocess");
        assertNotNull(processInstanceId2);
        assertTrue(processInstanceId2.longValue() > 0);

        List<Long> processInstanceIds = new ArrayList<Long>();
        processInstanceIds.add(processInstanceId);
        processInstanceIds.add(processInstanceId2);

        try {

            List<String> availableSignals = processClient.getAvailableSignals("definition-project", processInstanceId);
            assertNotNull(availableSignals);
            assertEquals(2, availableSignals.size());
            assertTrue(availableSignals.contains("Signal1"));
            assertTrue(availableSignals.contains("Signal2"));

            availableSignals = processClient.getAvailableSignals("definition-project", processInstanceId2);
            assertNotNull(availableSignals);
            assertEquals(2, availableSignals.size());
            assertTrue(availableSignals.contains("Signal1"));
            assertTrue(availableSignals.contains("Signal2"));

            Object person = createPersonInstance("john");
            processClient.signalProcessInstances("definition-project", processInstanceIds, "Signal1", person);

            processClient.signalProcessInstances("definition-project", processInstanceIds, "Signal2", "My custom string event");
        } catch (Exception e){
            processClient.abortProcessInstance("definition-project", processInstanceId);
            processClient.abortProcessInstance("definition-project", processInstanceId2);
            e.printStackTrace();
            fail(e.getMessage());
        }

    }

    @Test
    public void testManipulateProcessVariable() throws Exception {
        assertSuccess(client.createContainer("definition-project", new KieContainerResource("definition-project", releaseId)));
        Long processInstanceId = processClient.startProcess("definition-project", "definition-project.signalprocess");
        assertNotNull(processInstanceId);
        assertTrue(processInstanceId.longValue() > 0);

        try {
            Object personVar = null;
            try {
                personVar = processClient.getProcessInstanceVariable("definition-project", processInstanceId, "personData");
                fail("Should fail as there is no process variable personData set yet");
            } catch (KieServicesException e) {
                // expected
            }
            assertNull(personVar);

            personVar = createPersonInstance("john");
            processClient.setProcessVariable("definition-project", processInstanceId, "personData", personVar);

            personVar = processClient.getProcessInstanceVariable("definition-project", processInstanceId, "personData");
            assertNotNull(personVar);
            assertEquals("john", valueOf(personVar, "name"));


            processClient.setProcessVariable("definition-project", processInstanceId, "stringData", "custom value");

            String stringVar = (String) processClient.getProcessInstanceVariable("definition-project", processInstanceId, "stringData");
            assertNotNull(personVar);
            assertEquals("custom value", stringVar);

        } finally {
            processClient.abortProcessInstance("definition-project", processInstanceId);
        }

    }

    @Test
    public void testManipulateProcessVariables() throws Exception {
        assertSuccess(client.createContainer("definition-project", new KieContainerResource("definition-project", releaseId)));
        Long processInstanceId = processClient.startProcess("definition-project", "definition-project.signalprocess");
        assertNotNull(processInstanceId);
        assertTrue(processInstanceId.longValue() > 0);

        try {
            Object personVar = null;
            String stringVar = null;
            try {
                personVar = processClient.getProcessInstanceVariable("definition-project", processInstanceId, "personData");
                fail("Should fail as there is no process variable personData set yet");
            } catch (KieServicesException e) {
                // expected
            }
            assertNull(personVar);

            try {
                stringVar = (String) processClient.getProcessInstanceVariable("definition-project", processInstanceId, "stringData");
                fail("Should fail as there is no process variable personData set yet");
            } catch (KieServicesException e) {
                // expected
            }
            assertNull(personVar);
            assertNull(stringVar);

            personVar = createPersonInstance("john");
            stringVar = "string variable test";

            Map<String, Object> variables = new HashMap<String, Object>();
            variables.put("personData", personVar);
            variables.put("stringData", stringVar);

            processClient.setProcessVariables("definition-project", processInstanceId, variables);

            personVar = processClient.getProcessInstanceVariable("definition-project", processInstanceId, "personData");
            assertNotNull(personVar);
            assertEquals("john", valueOf(personVar, "name"));

            stringVar = (String) processClient.getProcessInstanceVariable("definition-project", processInstanceId, "stringData");
            assertNotNull(personVar);
            assertEquals("string variable test", stringVar);

        } finally {
            processClient.abortProcessInstance("definition-project", processInstanceId);
        }

    }

    @Test
    @Category(Smoke.class)
    public void testGetProcessInstance() throws Exception {
        assertSuccess(client.createContainer("definition-project", new KieContainerResource("definition-project", releaseId)));
        Long processInstanceId = processClient.startProcess("definition-project", "definition-project.signalprocess");
        assertNotNull(processInstanceId);
        assertTrue(processInstanceId.longValue() > 0);

        try {
            ProcessInstance processInstance = processClient.getProcessInstance("definition-project", processInstanceId);
            assertNotNull(processInstance);
            assertEquals(processInstanceId, processInstance.getId());
            assertEquals(org.kie.api.runtime.process.ProcessInstance.STATE_ACTIVE, processInstance.getState().intValue());
            assertEquals("definition-project.signalprocess", processInstance.getProcessId());
            assertEquals("signalprocess", processInstance.getProcessName());
            assertEquals("1.0", processInstance.getProcessVersion());
            assertEquals("definition-project", processInstance.getContainerId());
            assertEquals("signalprocess", processInstance.getProcessInstanceDescription());
            assertEquals(TestConfig.getUsername(), processInstance.getInitiator());
            assertEquals(-1l, processInstance.getParentId().longValue());
            assertNotNull(processInstance.getCorrelationKey());
            assertNotNull(processInstance.getDate());

            Map<String, Object> variables = processInstance.getVariables();
            assertNull(variables);
        } finally {
            processClient.abortProcessInstance("definition-project", processInstanceId);
        }

    }

    @Test
    public void testGetProcessInstanceWithVariables() throws Exception {
        assertSuccess(client.createContainer("definition-project", new KieContainerResource("definition-project", releaseId)));

        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("stringData", "waiting for signal");
        parameters.put("personData", createPersonInstance("john"));

        Long processInstanceId = processClient.startProcess("definition-project", "definition-project.signalprocess", parameters);
        assertNotNull(processInstanceId);
        assertTrue(processInstanceId.longValue() > 0);

        try {
            ProcessInstance processInstance = processClient.getProcessInstance("definition-project", processInstanceId, true);
            assertNotNull(processInstance);
            assertEquals(processInstanceId, processInstance.getId());
            assertEquals(org.kie.api.runtime.process.ProcessInstance.STATE_ACTIVE, processInstance.getState().intValue());
            assertEquals("definition-project.signalprocess", processInstance.getProcessId());
            assertEquals("signalprocess", processInstance.getProcessName());
            assertEquals("1.0", processInstance.getProcessVersion());
            assertEquals("definition-project", processInstance.getContainerId());
            assertEquals("signalprocess", processInstance.getProcessInstanceDescription());
            assertEquals(TestConfig.getUsername(), processInstance.getInitiator());
            assertEquals(-1l, processInstance.getParentId().longValue());
            assertNotNull(processInstance.getCorrelationKey());
            assertNotNull(processInstance.getDate());

            Map<String, Object> variables = processInstance.getVariables();
            assertNotNull(variables);
            assertEquals(2, variables.size());

            assertTrue(variables.containsKey("stringData"));
            assertTrue(variables.containsKey("personData"));

            String stringVar = (String) variables.get("stringData");
            Object personVar = variables.get("personData");

            assertNotNull(personVar);
            assertEquals("john", valueOf(personVar, "name"));

            assertNotNull(personVar);
            assertEquals("waiting for signal", stringVar);

        } finally {
            processClient.abortProcessInstance("definition-project", processInstanceId);
        }

    }

    @Test(expected = KieServicesException.class)
    public void testGetNonExistingProcessInstance() {
        assertSuccess(client.createContainer("definition-project", new KieContainerResource("definition-project", releaseId)));
        processClient.getProcessInstance("definition-project", 9999l);
    }

    @Test
    public void testWorkItemOperations() throws Exception {

        changeUser("john");

        assertSuccess(client.createContainer("definition-project", new KieContainerResource("definition-project", releaseId)));

        Map<String, Object> parameters = new HashMap<String, Object>();

        parameters.put("person", createPersonInstance("john"));

        Long processInstanceId = processClient.startProcess("definition-project", "definition-project.evaluation", parameters);
        try {
            assertNotNull(processInstanceId);
            assertTrue(processInstanceId.longValue() > 0);

            // Completing human task so we can move in process flow to work item.
            // User task shouldn't be handled as work item because in such case it doesn't behave consistently:
            // i.e. leaving open tasks after finishing process instance.
            List<String> status = Arrays.asList(Status.Ready.toString());
            List<TaskSummary> taskList = taskClient.findTasksByStatusByProcessInstanceId(processInstanceId, status, 0, 10);

            assertEquals(1, taskList.size());
            TaskSummary taskSummary = taskList.get(0);
            taskClient.startTask("definition-project", taskSummary.getId(), "john");
            taskClient.completeTask("definition-project", taskSummary.getId(), "john", null);

            TaskInstance userTask = taskClient.findTaskById(taskSummary.getId());
            assertNotNull(userTask);
            assertEquals("Evaluate items", userTask.getName());
            assertEquals(Status.Completed.toString(), userTask.getStatus());

            List<WorkItemInstance> workItems = processClient.getWorkItemByProcessInstance("definition-project", processInstanceId);
            assertNotNull(workItems);
            assertEquals(1, workItems.size());

            WorkItemInstance workItemInstance = workItems.get(0);
            assertNotNull(workItemInstance);
            assertEquals(processInstanceId, workItemInstance.getProcessInstanceId());
            assertEquals("Email", workItemInstance.getName());
            assertEquals("definition-project", workItemInstance.getContainerId());
            assertEquals(0, workItemInstance.getState().intValue());
            assertEquals(5, workItemInstance.getParameters().size());

            assertNotNull(workItemInstance.getId());
            assertNotNull(workItemInstance.getNodeId());
            assertNotNull(workItemInstance.getProcessInstanceId());


            workItemInstance = processClient.getWorkItem("definition-project", processInstanceId, workItemInstance.getId());
            assertNotNull(workItemInstance);
            assertEquals(processInstanceId, workItemInstance.getProcessInstanceId());
            assertEquals("Email", workItemInstance.getName());
            assertEquals("definition-project", workItemInstance.getContainerId());
            assertEquals(0, workItemInstance.getState().intValue());
            assertEquals(5, workItemInstance.getParameters().size());

            assertNotNull(workItemInstance.getId());
            assertNotNull(workItemInstance.getNodeId());
            assertNotNull(workItemInstance.getProcessInstanceId());

            processClient.abortWorkItem("definition-project", processInstanceId, workItemInstance.getId());

            workItems = processClient.getWorkItemByProcessInstance("definition-project", processInstanceId);
            assertNotNull(workItems);
            assertEquals(0, workItems.size());

        } catch (Exception e){
            processClient.abortProcessInstance("definition-project", processInstanceId);
            throw e;
        } finally {
            changeUser(TestConfig.getUsername());
        }
    }

    @Test
    public void testWorkItemOperationComplete() throws Exception {

        changeUser("john");

        assertSuccess(client.createContainer("definition-project", new KieContainerResource("definition-project", releaseId)));

        Map<String, Object> parameters = new HashMap<String, Object>();

        Long processInstanceId = processClient.startProcess("definition-project", "definition-project.evaluation", parameters);
        try {
            assertNotNull(processInstanceId);
            assertTrue(processInstanceId.longValue() > 0);

            // Completing human task so we can move in process flow to work item.
            // User task shouldn't be handled as work item because in such case it doesn't behave consistently:
            // i.e. leaving open tasks after finishing process instance.
            List<String> status = Arrays.asList(Status.Ready.toString());
            List<TaskSummary> taskList = taskClient.findTasksByStatusByProcessInstanceId(processInstanceId, status, 0, 10);

            assertEquals(1, taskList.size());
            TaskSummary taskSummary = taskList.get(0);
            taskClient.startTask("definition-project", taskSummary.getId(), "john");
            taskClient.completeTask("definition-project", taskSummary.getId(), "john", null);

            TaskInstance userTask = taskClient.findTaskById(taskSummary.getId());
            assertNotNull(userTask);
            assertEquals("Evaluate items", userTask.getName());
            assertEquals(Status.Completed.toString(), userTask.getStatus());

            List<WorkItemInstance> workItems = processClient.getWorkItemByProcessInstance("definition-project", processInstanceId);
            assertNotNull(workItems);
            assertEquals(1, workItems.size());

            WorkItemInstance workItemInstance = workItems.get(0);
            assertNotNull(workItemInstance);

            processClient.completeWorkItem("definition-project", processInstanceId, workItemInstance.getId(), parameters);

            workItems = processClient.getWorkItemByProcessInstance("definition-project", processInstanceId);
            assertNotNull(workItems);
            assertEquals(0, workItems.size());

        } catch (Exception e){
            processClient.abortProcessInstance("definition-project", processInstanceId);
            throw e;
        } finally {
            changeUser(TestConfig.getUsername());
        }
    }

    @Test
    public void testStartCheckProcessWithCorrelationKey() throws Exception {
        assertSuccess(client.createContainer(CONTAINER_ID, new KieContainerResource(CONTAINER_ID, releaseId)));

        String firstSimpleKey = "first-simple-key";
        String secondSimpleKey = "second-simple-key";
        String stringVarName = "stringData";
        String stringVarValue = "string variable test";

        CorrelationKeyFactory correlationKeyFactory = KieInternalServices.Factory.get().newCorrelationKeyFactory();

        CorrelationKey firstKey = correlationKeyFactory.newCorrelationKey(firstSimpleKey);
        CorrelationKey secondKey = correlationKeyFactory.newCorrelationKey(secondSimpleKey);

        Long firstProcessInstanceId = processClient.startProcess(CONTAINER_ID, PROCESS_ID_EVALUATION, firstKey);
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put(stringVarName, stringVarValue);
        Long secondProcessInstanceId = processClient.startProcess(CONTAINER_ID, PROCESS_ID_EVALUATION, secondKey, parameters);
        try {
            ProcessInstance instance = processClient.getProcessInstance(CONTAINER_ID, firstProcessInstanceId);
            assertNotNull(instance);
            assertEquals(firstProcessInstanceId, instance.getId());
            assertEquals(PROCESS_ID_EVALUATION, instance.getProcessId());
            assertEquals("evaluation", instance.getProcessName());
            assertEquals("1.0", instance.getProcessVersion());
            assertEquals(USER_YODA, instance.getInitiator());
            assertEquals(CONTAINER_ID, instance.getContainerId());
            assertEquals(firstSimpleKey, instance.getCorrelationKey());
            assertEquals("evaluation", instance.getProcessInstanceDescription());
            assertEquals(-1, instance.getParentId().longValue());

            instance = processClient.getProcessInstance(CONTAINER_ID, secondProcessInstanceId, true);
            assertNotNull(instance);
            assertEquals(secondProcessInstanceId, instance.getId());
            assertEquals(PROCESS_ID_EVALUATION, instance.getProcessId());
            assertEquals("evaluation", instance.getProcessName());
            assertEquals("1.0", instance.getProcessVersion());
            assertEquals(USER_YODA, instance.getInitiator());
            assertEquals(CONTAINER_ID, instance.getContainerId());
            assertEquals(secondSimpleKey, instance.getCorrelationKey());
            assertEquals("evaluation", instance.getProcessInstanceDescription());
            assertEquals(-1, instance.getParentId().longValue());
            assertTrue(instance.getVariables().containsKey(stringVarName));
            assertEquals(stringVarValue, instance.getVariables().get(stringVarName));
        } finally {
            processClient.abortProcessInstance(CONTAINER_ID, firstProcessInstanceId);
            processClient.abortProcessInstance(CONTAINER_ID, secondProcessInstanceId);
        }
    }

    @Test
    public void testStartProcessWithCustomTask() throws Exception {
        assertSuccess(client.createContainer("definition-project", new KieContainerResource("definition-project", releaseId)));
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("id", "custom id");
        Long processInstanceId = null;
        try {
            processInstanceId = processClient.startProcess("definition-project", "customtask");

            assertNotNull(processInstanceId);
            assertTrue(processInstanceId.longValue() > 0);

            ProcessInstance pi = queryClient.findProcessInstanceById(processInstanceId);
            assertNotNull(pi);
            assertEquals(org.kie.api.runtime.process.ProcessInstance.STATE_COMPLETED, pi.getState().intValue());

        } catch(Exception e) {
            if (processInstanceId != null) {
                processClient.abortProcessInstance("definition-project", processInstanceId);
            }
            fail("Exception " + e.getMessage());
        }
    }

    @Test
    public void testSignalContainer() throws Exception {
        assertSuccess(client.createContainer("definition-project", new KieContainerResource("definition-project", releaseId)));
        Long processInstanceId = processClient.startProcess("definition-project", "definition-project.signalprocess");
        assertNotNull(processInstanceId);
        assertTrue(processInstanceId.longValue() > 0);

        try {

            List<String> availableSignals = processClient.getAvailableSignals("definition-project", processInstanceId);
            assertNotNull(availableSignals);
            assertEquals(2, availableSignals.size());
            assertTrue(availableSignals.contains("Signal1"));
            assertTrue(availableSignals.contains("Signal2"));

            Object person = createPersonInstance("john");
            processClient.signal("definition-project", "Signal1", person);

            processClient.signal("definition-project", "Signal2", "My custom string event");

            ProcessInstance pi = processClient.getProcessInstance("definition-project", processInstanceId);
            assertNotNull(pi);
            assertEquals(org.kie.api.runtime.process.ProcessInstance.STATE_COMPLETED, pi.getState().intValue());
        } catch (Exception e){
            processClient.abortProcessInstance("definition-project", processInstanceId);
            e.printStackTrace();
            fail(e.getMessage());
        }

    }

    @Test
    public void testSignalStartProcess() throws Exception {
        assertSuccess(client.createContainer("definition-project", new KieContainerResource("definition-project", releaseId)));
        try {

            List<Integer> status = new ArrayList<Integer>();
            status.add(org.kie.api.runtime.process.ProcessInstance.STATE_ACTIVE);
            status.add(org.kie.api.runtime.process.ProcessInstance.STATE_ABORTED);
            status.add(org.kie.api.runtime.process.ProcessInstance.STATE_COMPLETED);

            List<ProcessInstance> processInstances = queryClient.findProcessInstancesByProcessId("signal-start", status, 0, 10);
            int initial = processInstances.size();

            Object person = createPersonInstance("john");
            processClient.signal("definition-project", "start-process", person);

            processInstances = queryClient.findProcessInstancesByProcessId("signal-start", status, 0, 10);
            assertNotNull(processInstances);
            assertEquals(initial + 1, processInstances.size());

        } catch (Exception e){
            e.printStackTrace();
            fail(e.getMessage());
        }

    }

    @Test(timeout = 60 * 1000)
    public void testStartProcessInstanceWithAsyncNodes() throws Exception {
        assertSuccess(client.createContainer(CONTAINER_ID, new KieContainerResource(CONTAINER_ID, releaseId)));

        List<String> status = new ArrayList<String>();
        status.add(STATUS.QUEUED.toString());
        status.add(STATUS.RUNNING.toString());
        status.add(STATUS.DONE.toString());
        int originalJobCount = jobServicesClient.getRequestsByStatus(status, 0, 1000).size();

        Long processInstanceId = processClient.startProcess(CONTAINER_ID, PROCESS_ID_ASYNC_SCRIPT);
        assertNotNull(processInstanceId);
        assertTrue(processInstanceId.longValue() > 0);

        try {

            // async node is executed as a job
            List<RequestInfoInstance> jobs = jobServicesClient.getRequestsByStatus(status, 0, 1000);
            assertNotNull(jobs);
            assertEquals(originalJobCount + 1, jobs.size());

            // wait for process instance to be completed
            waitForProcessInstanceToFinish(CONTAINER_ID, processInstanceId);

            ProcessInstance pi = processClient.getProcessInstance(CONTAINER_ID, processInstanceId);
            assertNotNull(pi);
            assertEquals(org.kie.api.runtime.process.ProcessInstance.STATE_COMPLETED, pi.getState().intValue());

        } catch (Exception e){
            processClient.abortProcessInstance(CONTAINER_ID, processInstanceId);
            fail(e.getMessage());
        }
    }

    @Test(timeout = 60 * 1000)
    public void testProcessInstanceWithTimer() throws Exception {
        assertSuccess(client.createContainer(CONTAINER_ID, new KieContainerResource(CONTAINER_ID, releaseId)));

        Long processInstanceId = processClient.startProcess(CONTAINER_ID, PROCESS_ID_TIMER);
        assertNotNull(processInstanceId);
        assertTrue(processInstanceId.longValue() > 0);

        try {
            waitForProcessInstanceToFinish(CONTAINER_ID, processInstanceId);

            ProcessInstance pi = processClient.getProcessInstance(CONTAINER_ID, processInstanceId);
            assertNotNull(pi);
            assertEquals(org.kie.api.runtime.process.ProcessInstance.STATE_COMPLETED, pi.getState().intValue());

        } catch (Exception e){
            processClient.abortProcessInstance(CONTAINER_ID, processInstanceId);
            fail(e.getMessage());
        }
    }
}
