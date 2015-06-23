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

import java.util.ArrayList;
import java.util.Arrays;
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
import org.kie.server.api.model.instance.ProcessInstance;
import org.kie.server.api.model.instance.TaskInstance;
import org.kie.server.api.model.instance.TaskSummary;
import org.kie.server.api.model.instance.WorkItemInstance;
import org.kie.server.client.KieServicesClient;
import org.kie.server.client.KieServicesConfiguration;
import org.kie.server.client.KieServicesException;
import org.kie.server.client.KieServicesFactory;
import org.kie.server.integrationtests.config.TestConfig;

import static org.junit.Assert.*;

public class ProcessServiceIntegrationTest extends JbpmKieServerBaseIntegrationTest {

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
            localServerConfig.setTimeout(100000);
            localServerConfig.addJaxbClasses(extraClasses);
            return KieServicesFactory.newKieServicesClient(localServerConfig, kieContainer.getClassLoader());
        } else {
            configuration.setMarshallingFormat(marshallingFormat);
            configuration.addJaxbClasses(extraClasses);
            return KieServicesFactory.newKieServicesClient(configuration, kieContainer.getClassLoader());
        }
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

        Long processInstanceId = client.startProcess("definition-project", "definition-project.evaluation", parameters);

        assertNotNull(processInstanceId);
        assertTrue(processInstanceId.longValue() > 0);

        Object personVariable = client.getProcessInstanceVariable("definition-project", processInstanceId, "person");
        assertNotNull(personVariable);
        assertTrue(personClass.isAssignableFrom(personVariable.getClass()));

        personVariable = client.getProcessInstanceVariable("definition-project", processInstanceId, "person");
        assertNotNull(personVariable);
        assertTrue(personClass.isAssignableFrom(personVariable.getClass()));

        Map<String, Object> variables = client.getProcessInstanceVariables("definition-project", processInstanceId);
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
        assertEquals(1, ((List)variables.get("list")).size());
        assertEquals("item", ((List)variables.get("list")).get(0));
        assertEquals("john", valueOf(variables.get("person"), "name"));

        client.abortProcessInstance("definition-project", processInstanceId);

    }

    @Test(expected = KieServicesException.class)
    public void testStartNotExistingProcess() {
        assertSuccess(client.createContainer("definition-project", new KieContainerResource("definition-project", releaseId)));
        client.startProcess("definition-project", "not-existing", (Map)null);
    }


    @Test(expected = KieServicesException.class)
    public void testAbortExistingProcess() {
        assertSuccess(client.createContainer("definition-project", new KieContainerResource("definition-project", releaseId)));
        client.abortProcessInstance("definition-project", 9999l);
    }

    @Test(expected = KieServicesException.class)
    public void testStartCheckNonExistingVariables() throws Exception {
        assertSuccess(client.createContainer("definition-project", new KieContainerResource("definition-project", releaseId)));

        Map<String, Object> parameters = new HashMap<String, Object>();
        Long processInstanceId = client.startProcess("definition-project", "definition-project.evaluation", parameters);
        try {
            client.getProcessInstanceVariable("definition-project", processInstanceId, "person");
        } finally {
            client.abortProcessInstance("definition-project", processInstanceId);
        }

    }

    @Test
    public void testAbortMultipleProcessInstances() throws Exception {
        assertSuccess(client.createContainer("definition-project", new KieContainerResource("definition-project", releaseId)));


        Long processInstanceId1 = client.startProcess("definition-project", "definition-project.evaluation");
        Long processInstanceId2 = client.startProcess("definition-project", "definition-project.evaluation");
        Long processInstanceId3 = client.startProcess("definition-project", "definition-project.evaluation");
        Long processInstanceId4 = client.startProcess("definition-project", "definition-project.evaluation");

        List<Long> processInstances = new ArrayList<Long>();
        processInstances.add(processInstanceId1);
        processInstances.add(processInstanceId2);
        processInstances.add(processInstanceId3);
        processInstances.add(processInstanceId4);

        client.abortProcessInstances("definition-project", processInstances);
    }

    @Test
    public void testSignalProcessInstance() throws Exception {
        assertSuccess(client.createContainer("definition-project", new KieContainerResource("definition-project", releaseId)));
        Long processInstanceId = client.startProcess("definition-project", "definition-project.signalprocess");
        assertNotNull(processInstanceId);
        assertTrue(processInstanceId.longValue() > 0);

        try {

            List<String> availableSignals = client.getAvailableSignals("definition-project", processInstanceId);
            assertNotNull(availableSignals);
            assertEquals(2, availableSignals.size());
            assertTrue(availableSignals.contains("Signal1"));
            assertTrue(availableSignals.contains("Signal2"));

            Object person = createPersonInstance("john");
            client.signalProcessInstance("definition-project", processInstanceId, "Signal1", person);

            client.signalProcessInstance("definition-project", processInstanceId, "Signal2", "My custom string event");
        } catch (Exception e){
            client.abortProcessInstance("definition-project", processInstanceId);
            e.printStackTrace();
            fail(e.getMessage());
        }

    }

    @Test
    public void testSignalProcessInstanceNullEvent() throws Exception {
        assertSuccess(client.createContainer("definition-project", new KieContainerResource("definition-project", releaseId)));
        Long processInstanceId = client.startProcess("definition-project", "definition-project.signalprocess");
        assertNotNull(processInstanceId);
        assertTrue(processInstanceId.longValue() > 0);

        try {

            List<String> availableSignals = client.getAvailableSignals("definition-project", processInstanceId);
            assertNotNull(availableSignals);
            assertEquals(2, availableSignals.size());
            assertTrue(availableSignals.contains("Signal1"));
            assertTrue(availableSignals.contains("Signal2"));

            client.signalProcessInstance("definition-project", processInstanceId, "Signal1", null);

            client.signalProcessInstance("definition-project", processInstanceId, "Signal2", null);
        } catch (Exception e){
            client.abortProcessInstance("definition-project", processInstanceId);
            e.printStackTrace();
            fail(e.getMessage());
        }

    }

    @Test
    public void testSignalProcessInstances() throws Exception {
        assertSuccess(client.createContainer("definition-project", new KieContainerResource("definition-project", releaseId)));
        Long processInstanceId = client.startProcess("definition-project", "definition-project.signalprocess");
        assertNotNull(processInstanceId);
        assertTrue(processInstanceId.longValue() > 0);

        Long processInstanceId2 = client.startProcess("definition-project", "definition-project.signalprocess");
        assertNotNull(processInstanceId2);
        assertTrue(processInstanceId2.longValue() > 0);

        List<Long> processInstanceIds = new ArrayList<Long>();
        processInstanceIds.add(processInstanceId);
        processInstanceIds.add(processInstanceId2);

        try {

            List<String> availableSignals = client.getAvailableSignals("definition-project", processInstanceId);
            assertNotNull(availableSignals);
            assertEquals(2, availableSignals.size());
            assertTrue(availableSignals.contains("Signal1"));
            assertTrue(availableSignals.contains("Signal2"));

            availableSignals = client.getAvailableSignals("definition-project", processInstanceId2);
            assertNotNull(availableSignals);
            assertEquals(2, availableSignals.size());
            assertTrue(availableSignals.contains("Signal1"));
            assertTrue(availableSignals.contains("Signal2"));

            Object person = createPersonInstance("john");
            client.signalProcessInstances("definition-project", processInstanceIds, "Signal1", person);

            client.signalProcessInstances("definition-project", processInstanceIds, "Signal2", "My custom string event");
        } catch (Exception e){
            client.abortProcessInstance("definition-project", processInstanceId);
            client.abortProcessInstance("definition-project", processInstanceId2);
            e.printStackTrace();
            fail(e.getMessage());
        }

    }

    @Test
    public void testManipulateProcessVariable() throws Exception {
        assertSuccess(client.createContainer("definition-project", new KieContainerResource("definition-project", releaseId)));
        Long processInstanceId = client.startProcess("definition-project", "definition-project.signalprocess");
        assertNotNull(processInstanceId);
        assertTrue(processInstanceId.longValue() > 0);

        try {
            Object personVar = null;
            try {
                personVar = client.getProcessInstanceVariable("definition-project", processInstanceId, "personData");
                fail("Should fail as there is no process variable personData set yet");
            } catch (KieServicesException e) {
                // expected
            }
            assertNull(personVar);

            personVar = createPersonInstance("john");
            client.setProcessVariable("definition-project", processInstanceId, "personData", personVar);

            personVar = client.getProcessInstanceVariable("definition-project", processInstanceId, "personData");
            assertNotNull(personVar);
            assertEquals("john", valueOf(personVar, "name"));


            client.setProcessVariable("definition-project", processInstanceId, "stringData", "custom value");

            String stringVar = (String) client.getProcessInstanceVariable("definition-project", processInstanceId, "stringData");
            assertNotNull(personVar);
            assertEquals("custom value", stringVar);

        } finally {
            client.abortProcessInstance("definition-project", processInstanceId);
        }

    }

    @Test
    public void testManipulateProcessVariables() throws Exception {
        assertSuccess(client.createContainer("definition-project", new KieContainerResource("definition-project", releaseId)));
        Long processInstanceId = client.startProcess("definition-project", "definition-project.signalprocess");
        assertNotNull(processInstanceId);
        assertTrue(processInstanceId.longValue() > 0);

        try {
            Object personVar = null;
            String stringVar = null;
            try {
                personVar = client.getProcessInstanceVariable("definition-project", processInstanceId, "personData");
                fail("Should fail as there is no process variable personData set yet");
            } catch (KieServicesException e) {
                // expected
            }
            assertNull(personVar);

            try {
                stringVar = (String) client.getProcessInstanceVariable("definition-project", processInstanceId, "stringData");
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

            client.setProcessVariables("definition-project", processInstanceId, variables);

            personVar = client.getProcessInstanceVariable("definition-project", processInstanceId, "personData");
            assertNotNull(personVar);
            assertEquals("john", valueOf(personVar, "name"));

            stringVar = (String) client.getProcessInstanceVariable("definition-project", processInstanceId, "stringData");
            assertNotNull(personVar);
            assertEquals("string variable test", stringVar);

        } finally {
            client.abortProcessInstance("definition-project", processInstanceId);
        }

    }

    @Test
    public void testGetProcessInstance() throws Exception {
        assertSuccess(client.createContainer("definition-project", new KieContainerResource("definition-project", releaseId)));
        Long processInstanceId = client.startProcess("definition-project", "definition-project.signalprocess");
        assertNotNull(processInstanceId);
        assertTrue(processInstanceId.longValue() > 0);

        try {
            ProcessInstance processInstance = client.getProcessInstance("definition-project", processInstanceId);
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
            assertNull(processInstance.getCorrelationKey());
            assertNotNull(processInstance.getDate());

            Map<String, Object> variables = processInstance.getVariables();
            assertNull(variables);
        } finally {
            client.abortProcessInstance("definition-project", processInstanceId);
        }

    }

    @Test
    public void testGetProcessInstanceWithVariables() throws Exception {
        assertSuccess(client.createContainer("definition-project", new KieContainerResource("definition-project", releaseId)));

        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("stringData", "waiting for signal");
        parameters.put("personData", createPersonInstance("john"));

        Long processInstanceId = client.startProcess("definition-project", "definition-project.signalprocess", parameters);
        assertNotNull(processInstanceId);
        assertTrue(processInstanceId.longValue() > 0);

        try {
            ProcessInstance processInstance = client.getProcessInstance("definition-project", processInstanceId, true);
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
            assertNull(processInstance.getCorrelationKey());
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
            client.abortProcessInstance("definition-project", processInstanceId);
        }

    }

    @Test(expected = KieServicesException.class)
    public void testGetNonExistingProcessInstance() {
        assertSuccess(client.createContainer("definition-project", new KieContainerResource("definition-project", releaseId)));
        client.getProcessInstance("definition-project", 9999l);
    }

    @Test
    public void testWorkItemOperations() throws Exception {

        changeUser("john");

        assertSuccess(client.createContainer("definition-project", new KieContainerResource("definition-project", releaseId)));

        Map<String, Object> parameters = new HashMap<String, Object>();

        parameters.put("person", createPersonInstance("john"));

        Long processInstanceId = client.startProcess("definition-project", "definition-project.evaluation", parameters);
        try {
            assertNotNull(processInstanceId);
            assertTrue(processInstanceId.longValue() > 0);

            // Completing human task so we can move in process flow to work item.
            // User task shouldn't be handled as work item because in such case it doesn't behave consistently:
            // i.e. leaving open tasks after finishing process instance.
            List<String> status = Arrays.asList(Status.Ready.toString());
            List<TaskSummary> taskList = client.findTasksByStatusByProcessInstanceId(processInstanceId, status, 0, 10);

            assertEquals(1, taskList.size());
            TaskSummary taskSummary = taskList.get(0);
            client.startTask("definition-project", taskSummary.getId(), "john");
            client.completeTask("definition-project", taskSummary.getId(), "john", null);

            TaskInstance userTask = client.findTaskById(taskSummary.getId());
            assertNotNull(userTask);
            assertEquals("Evaluate items", userTask.getName());
            assertEquals(Status.Completed.toString(), userTask.getStatus());

            List<WorkItemInstance> workItems = client.getWorkItemByProcessInstance("definition-project", processInstanceId);
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


            workItemInstance = client.getWorkItem("definition-project", processInstanceId, workItemInstance.getId());
            assertNotNull(workItemInstance);
            assertEquals(processInstanceId, workItemInstance.getProcessInstanceId());
            assertEquals("Email", workItemInstance.getName());
            assertEquals("definition-project", workItemInstance.getContainerId());
            assertEquals(0, workItemInstance.getState().intValue());
            assertEquals(5, workItemInstance.getParameters().size());

            assertNotNull(workItemInstance.getId());
            assertNotNull(workItemInstance.getNodeId());
            assertNotNull(workItemInstance.getProcessInstanceId());

            client.abortWorkItem("definition-project", processInstanceId, workItemInstance.getId());

            workItems = client.getWorkItemByProcessInstance("definition-project", processInstanceId);
            assertNotNull(workItems);
            assertEquals(0, workItems.size());

        } catch (Exception e){
            client.abortProcessInstance("definition-project", processInstanceId);
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

        Long processInstanceId = client.startProcess("definition-project", "definition-project.evaluation", parameters);
        try {
            assertNotNull(processInstanceId);
            assertTrue(processInstanceId.longValue() > 0);

            // Completing human task so we can move in process flow to work item.
            // User task shouldn't be handled as work item because in such case it doesn't behave consistently:
            // i.e. leaving open tasks after finishing process instance.
            List<String> status = Arrays.asList(Status.Ready.toString());
            List<TaskSummary> taskList = client.findTasksByStatusByProcessInstanceId(processInstanceId, status, 0, 10);

            assertEquals(1, taskList.size());
            TaskSummary taskSummary = taskList.get(0);
            client.startTask("definition-project", taskSummary.getId(), "john");
            client.completeTask("definition-project", taskSummary.getId(), "john", null);

            TaskInstance userTask = client.findTaskById(taskSummary.getId());
            assertNotNull(userTask);
            assertEquals("Evaluate items", userTask.getName());
            assertEquals(Status.Completed.toString(), userTask.getStatus());

            List<WorkItemInstance> workItems = client.getWorkItemByProcessInstance("definition-project", processInstanceId);
            assertNotNull(workItems);
            assertEquals(1, workItems.size());

            WorkItemInstance workItemInstance = workItems.get(0);
            assertNotNull(workItemInstance);

            client.completeWorkItem("definition-project", processInstanceId, workItemInstance.getId(), parameters);

            workItems = client.getWorkItemByProcessInstance("definition-project", processInstanceId);
            assertNotNull(workItems);
            assertEquals(0, workItems.size());

        } catch (Exception e){
            client.abortProcessInstance("definition-project", processInstanceId);
            throw e;
        } finally {
            changeUser(TestConfig.getUsername());
        }
    }
}
