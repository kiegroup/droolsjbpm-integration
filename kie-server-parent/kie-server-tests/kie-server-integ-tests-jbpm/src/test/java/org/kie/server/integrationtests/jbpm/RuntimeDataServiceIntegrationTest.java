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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.kie.api.KieServices;
import org.kie.api.task.model.Status;
import org.kie.internal.KieInternalServices;
import org.kie.internal.process.CorrelationKey;
import org.kie.internal.process.CorrelationKeyFactory;
import org.kie.internal.task.api.model.TaskEvent;
import org.kie.server.api.model.KieContainerResource;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.definition.ProcessDefinition;
import org.kie.server.api.model.instance.NodeInstance;
import org.kie.server.api.model.instance.ProcessInstance;
import org.kie.server.api.model.instance.TaskEventInstance;
import org.kie.server.api.model.instance.TaskInstance;
import org.kie.server.api.model.instance.TaskSummary;
import org.kie.server.api.model.instance.VariableInstance;
import org.kie.server.api.model.instance.WorkItemInstance;
import org.kie.server.client.KieServicesException;
import org.kie.server.integrationtests.category.Smoke;

import static org.junit.Assert.*;



public class RuntimeDataServiceIntegrationTest extends JbpmKieServerBaseIntegrationTest {

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
    public void testGetProcessDefinitions() throws Exception {
        assertSuccess(client.createContainer("definition-project", new KieContainerResource("definition-project", releaseId)));

        List<ProcessDefinition> definitions = queryClient.findProcesses(0, 20);
        assertNotNull(definitions);

        assertEquals(7, definitions.size());
        List<String> processIds = collectDefinitions(definitions);
        assertTrue(processIds.contains("definition-project.call-evaluation"));
        assertTrue(processIds.contains("definition-project.evaluation"));
        assertTrue(processIds.contains("definition-project.grouptask"));
        assertTrue(processIds.contains("definition-project.signalprocess"));
        assertTrue(processIds.contains("definition-project.usertask"));
        assertTrue(processIds.contains("customtask"));
        assertTrue(processIds.contains("signal-start"));

        // test paging of the result
        definitions = queryClient.findProcesses(0, 3);

        assertNotNull(definitions);
        assertEquals(3, definitions.size());
        processIds = collectDefinitions(definitions);
        assertTrue(processIds.contains("definition-project.call-evaluation"));
        assertTrue(processIds.contains("definition-project.evaluation"));
        assertTrue(processIds.contains("signal-start"));

        definitions = queryClient.findProcesses(1, 3);
        assertNotNull(definitions);

        assertEquals(3, definitions.size());
        processIds = collectDefinitions(definitions);
        assertTrue(processIds.contains("definition-project.signalprocess"));
        assertTrue(processIds.contains("customtask"));
        assertTrue(processIds.contains("definition-project.grouptask"));

    }

    @Test
    public void testGetProcessDefinitionsWithFilter() throws Exception {
        assertSuccess(client.createContainer("definition-project", new KieContainerResource("definition-project", releaseId)));

        List<ProcessDefinition> definitions = queryClient.findProcesses("evaluation", 0, 20);
        assertNotNull(definitions);

        assertEquals(2, definitions.size());
        List<String> processIds = collectDefinitions(definitions);
        assertTrue(processIds.contains("definition-project.call-evaluation"));
        assertTrue(processIds.contains("definition-project.evaluation"));

        // test paging of the result
        definitions = queryClient.findProcesses("evaluation", 0, 1);

        assertNotNull(definitions);
        assertEquals(1, definitions.size());
        processIds = collectDefinitions(definitions);
        assertTrue(processIds.contains("definition-project.call-evaluation"));

        definitions = queryClient.findProcesses("evaluation", 1, 1);
        assertNotNull(definitions);

        assertEquals(1, definitions.size());
        processIds = collectDefinitions(definitions);
        assertTrue(processIds.contains("definition-project.evaluation"));

    }

    @Test
    public void testGetProcessDefinitionsByContainer() throws Exception {
        assertSuccess(client.createContainer("definition-project", new KieContainerResource("definition-project", releaseId)));

        List<ProcessDefinition> definitions = queryClient.findProcessesByContainerId("definition-project", 0, 20);
        assertNotNull(definitions);

        assertEquals(7, definitions.size());
        List<String> processIds = collectDefinitions(definitions);
        assertTrue(processIds.contains("definition-project.call-evaluation"));
        assertTrue(processIds.contains("definition-project.evaluation"));
        assertTrue(processIds.contains("definition-project.grouptask"));
        assertTrue(processIds.contains("definition-project.signalprocess"));
        assertTrue(processIds.contains("definition-project.usertask"));
        assertTrue(processIds.contains("customtask"));
        assertTrue(processIds.contains("signal-start"));

        // test paging of the result
        definitions = queryClient.findProcessesByContainerId("definition-project", 0, 3);

        assertNotNull(definitions);
        assertEquals(3, definitions.size());
        processIds = collectDefinitions(definitions);
        assertTrue(processIds.contains("definition-project.call-evaluation"));
        assertTrue(processIds.contains("definition-project.evaluation"));
        assertTrue(processIds.contains("signal-start"));

        definitions = queryClient.findProcessesByContainerId("definition-project", 1, 3);
        assertNotNull(definitions);

        assertEquals(3, definitions.size());
        processIds = collectDefinitions(definitions);
        assertTrue(processIds.contains("definition-project.signalprocess"));
        assertTrue(processIds.contains("customtask"));
        assertTrue(processIds.contains("definition-project.grouptask"));

        // last check if there are process def for not existing project
        definitions = queryClient.findProcessesByContainerId("not-existing-project", 0, 10);
        assertNotNull(definitions);

        assertEquals(0, definitions.size());

    }

    @Test
    public void testGetProcessDefinitionsById() throws Exception {
        assertSuccess(client.createContainer("definition-project", new KieContainerResource("definition-project", releaseId)));

        List<ProcessDefinition> definitions = queryClient.findProcessesById("definition-project.usertask");
        assertNotNull(definitions);

        assertEquals(1, definitions.size());
        List<String> processIds = collectDefinitions(definitions);
        assertTrue(processIds.contains("definition-project.usertask"));


         // last check if there are process def for not existing project
        definitions = queryClient.findProcessesById("not-existing-project");
        assertNotNull(definitions);

        assertEquals(0, definitions.size());

    }

    @Test
    public void testGetProcessDefinitionByContainerAndId() throws Exception {
        assertSuccess(client.createContainer("definition-project", new KieContainerResource("definition-project", releaseId)));

        ProcessDefinition definition = queryClient.findProcessByContainerIdProcessId("definition-project", "definition-project.usertask");
        assertNotNull(definition);
        assertEquals("definition-project.usertask", definition.getId());
        assertEquals("usertask", definition.getName());
        assertEquals("1.0", definition.getVersion());
        assertEquals("org.jbpm", definition.getPackageName());
        assertEquals("definition-project", definition.getContainerId());

    }

    @Test
    public void testGetProcessDefinitionByContainerAndNonExistingId() throws Exception {
        assertSuccess(client.createContainer("definition-project", new KieContainerResource("definition-project", releaseId)));

        try {
            queryClient.findProcessByContainerIdProcessId("definition-project", "non-existing");
            fail("KieServicesException should be thrown complaining about process definition not found.");

        } catch (KieServicesException e) {
            assertResultContainsString(e.getMessage(), "Could not find process definition \"non-existing\" in container \"definition-project\"");
        }
    }

    @Test
    public void testGetProcessInstances() throws Exception {
        assertSuccess(client.createContainer("definition-project", new KieContainerResource("definition-project", releaseId)));

        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("stringData", "waiting for signal");
        parameters.put("personData", createPersonInstance("john"));

        List<Long> processInstanceIds = createProcessInstances(parameters);

        try {
            List<ProcessInstance> instances = queryClient.findProcessInstances(0, 10);
            assertNotNull(instances);
            assertEquals(5, instances.size());

            List<Long> found = collectInstances(instances);
            assertEquals(processInstanceIds, found);

            instances = queryClient.findProcessInstances(0, 3);
            assertNotNull(instances);
            assertEquals(3, instances.size());

            instances = queryClient.findProcessInstances(1, 3);
            assertNotNull(instances);
            assertEquals(2, instances.size());

        } finally {
            abortProcessInstances(processInstanceIds);
        }

    }

    @Test
    public void testGetProcessInstancesByContainer() throws Exception {
        assertSuccess(client.createContainer("definition-project", new KieContainerResource("definition-project", releaseId)));

        int offset = queryClient.findProcessInstancesByContainerId("definition-project", Collections.singletonList(2), 0, 10).size();

        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("stringData", "waiting for signal");
        parameters.put("personData", createPersonInstance("john"));

        List<Long> processInstanceIds = createProcessInstances(parameters);

        try {
            List<ProcessInstance> instances = queryClient.findProcessInstancesByContainerId("definition-project", null, 0, 10);
            assertNotNull(instances);
            assertEquals(5, instances.size());

            List<Long> found = collectInstances(instances);
            assertEquals(processInstanceIds, found);

            instances = queryClient.findProcessInstancesByContainerId("definition-project", null, 0, 3);
            assertNotNull(instances);
            assertEquals(3, instances.size());

            instances = queryClient.findProcessInstancesByContainerId("definition-project", null, 1, 3);
            assertNotNull(instances);
            assertEquals(2, instances.size());

            // search for completed only
            instances = queryClient.findProcessInstancesByContainerId("definition-project", Collections.singletonList(2), 0, 10);
            assertNotNull(instances);
            assertEquals(0 + offset, instances.size());

        } finally {
            abortProcessInstances(processInstanceIds);
        }

    }

    @Test
    public void testGetProcessInstancesByProcessId() throws Exception {
        assertSuccess(client.createContainer("definition-project", new KieContainerResource("definition-project", releaseId)));

        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("stringData", "waiting for signal");
        parameters.put("personData", createPersonInstance("john"));

        List<Long> processInstanceIds = createProcessInstances(parameters);

        try {
            List<ProcessInstance> instances = queryClient.findProcessInstancesByProcessId("definition-project.usertask", null, 0, 10);
            assertNotNull(instances);
            assertEquals(2, instances.size());
            assertEquals("definition-project.usertask", instances.get(0).getProcessId());
            assertEquals("definition-project.usertask", instances.get(1).getProcessId());

            instances = queryClient.findProcessInstancesByProcessId("definition-project.usertask", null, 0, 1);
            assertNotNull(instances);
            assertEquals(1, instances.size());
            assertEquals("definition-project.usertask", instances.get(0).getProcessId());

            instances = queryClient.findProcessInstancesByProcessId("definition-project.usertask", null, 1, 1);
            assertNotNull(instances);
            assertEquals(1, instances.size());
            assertEquals("definition-project.usertask", instances.get(0).getProcessId());

            // search for completed only
            instances = queryClient.findProcessInstancesByProcessId("definition-project.usertask", Collections.singletonList(2), 0, 10);
            assertNotNull(instances);
            assertEquals(0, instances.size());

        } finally {
            abortProcessInstances(processInstanceIds);
        }

    }

    @Test
    public void testGetProcessInstancesByProcessName() throws Exception {
        assertSuccess(client.createContainer("definition-project", new KieContainerResource("definition-project", releaseId)));

        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("stringData", "waiting for signal");
        parameters.put("personData", createPersonInstance("john"));

        List<Long> processInstanceIds = createProcessInstances(parameters);

        try {
            List<ProcessInstance> instances = queryClient.findProcessInstancesByProcessName("usertask", null, 0, 10);
            assertNotNull(instances);
            assertEquals(2, instances.size());
            assertEquals("definition-project.usertask", instances.get(0).getProcessId());
            assertEquals("definition-project.usertask", instances.get(1).getProcessId());

            instances = queryClient.findProcessInstancesByProcessName("usertask", null, 0, 1);
            assertNotNull(instances);
            assertEquals(1, instances.size());
            assertEquals("definition-project.usertask", instances.get(0).getProcessId());

            instances = queryClient.findProcessInstancesByProcessName("usertask", null, 1, 1);
            assertNotNull(instances);
            assertEquals(1, instances.size());
            assertEquals("definition-project.usertask", instances.get(0).getProcessId());

            // search for completed only
            instances = queryClient.findProcessInstancesByProcessName("usertask", Collections.singletonList(2), 0, 10);
            assertNotNull(instances);
            assertEquals(0, instances.size());

        } finally {
            abortProcessInstances(processInstanceIds);
        }

    }

    @Test
    public void testGetProcessInstancesByStatus() throws Exception {
        assertSuccess(client.createContainer("definition-project", new KieContainerResource("definition-project", releaseId)));

        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("stringData", "waiting for signal");
        parameters.put("personData", createPersonInstance("john"));

        List<Long> processInstanceIds = createProcessInstances(parameters);

        try {
            List<ProcessInstance> instances = queryClient.findProcessInstancesByStatus(Collections.singletonList(1), 0, 10);
            assertNotNull(instances);
            assertEquals(5, instances.size());

            instances = queryClient.findProcessInstancesByStatus(Collections.singletonList(1), 0, 3);
            assertNotNull(instances);
            assertEquals(3, instances.size());


            instances = queryClient.findProcessInstancesByStatus(Collections.singletonList(1), 1, 3);
            assertNotNull(instances);
            assertEquals(2, instances.size());

            // search for completed only
            instances = queryClient.findProcessInstancesByProcessId("definition-project.usertask", Collections.singletonList(2), 0, 10);
            assertNotNull(instances);
            assertEquals(0, instances.size());

        } finally {
            abortProcessInstances(processInstanceIds);
        }

    }

    @Test
    public void testGetProcessInstancesByInitiator() throws Exception {
        assertSuccess(client.createContainer("definition-project", new KieContainerResource("definition-project", releaseId)));

        int offset = queryClient.findProcessInstancesByInitiator("yoda", Collections.singletonList(2), 0, 10).size();

        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("stringData", "waiting for signal");
        parameters.put("personData", createPersonInstance("john"));

        List<Long> processInstanceIds = createProcessInstances(parameters);

        try {
            List<ProcessInstance> instances = queryClient.findProcessInstancesByInitiator("yoda", null, 0, 10);
            assertNotNull(instances);
            assertEquals(5, instances.size());

            instances = queryClient.findProcessInstancesByInitiator("yoda", null, 0, 3);
            assertNotNull(instances);
            assertEquals(3, instances.size());


            instances = queryClient.findProcessInstancesByInitiator("yoda", null, 1, 3);
            assertNotNull(instances);
            assertEquals(2, instances.size());

            // search for completed only
            instances = queryClient.findProcessInstancesByInitiator("yoda", Collections.singletonList(2), 0, 10);
            assertNotNull(instances);
            assertEquals(0 + offset, instances.size());

        } finally {
            abortProcessInstances(processInstanceIds);
        }

    }

    @Test
    @Category(Smoke.class)
    public void testGetProcessInstanceById() throws Exception {
        assertSuccess(client.createContainer("definition-project", new KieContainerResource("definition-project", releaseId)));

        Map<String, Object> parameters = new HashMap<String, Object>();
        Long processInstanceId = processClient.startProcess("definition-project", "definition-project.evaluation", parameters);
        try {
            ProcessInstance instance = queryClient.findProcessInstanceById(processInstanceId);
            assertNotNull(instance);
            assertEquals(processInstanceId, instance.getId());
            assertEquals("definition-project.evaluation", instance.getProcessId());
            assertEquals("evaluation", instance.getProcessName());
            assertEquals("1.0", instance.getProcessVersion());
            assertEquals("yoda", instance.getInitiator());
            assertEquals("definition-project", instance.getContainerId());
            assertNullOrEmpty(instance.getCorrelationKey());
            assertEquals("evaluation", instance.getProcessInstanceDescription());
            assertEquals(-1, instance.getParentId().longValue());
        } finally {
            processClient.abortProcessInstance("definition-project", processInstanceId);
        }
    }

    @Test
    public void testGetProcessInstanceByNonExistingId() throws Exception {
        assertSuccess(client.createContainer("definition-project", new KieContainerResource("definition-project", releaseId)));

        try {
            queryClient.findProcessInstanceById(-9999l);
            fail("KieServicesException should be thrown complaining about process instance not found.");

        } catch (KieServicesException e) {
            assertResultContainsString(e.getMessage(), "Could not find process instance with id");
        }
    }

    @Test
    public void testGetProcessInstanceByCorrelationKey() throws Exception {
        assertSuccess(client.createContainer(CONTAINER_ID, new KieContainerResource(CONTAINER_ID, releaseId)));

        CorrelationKeyFactory correlationKeyFactory = KieInternalServices.Factory.get().newCorrelationKeyFactory();

        String businessKey = "simple-key";
        CorrelationKey key = correlationKeyFactory.newCorrelationKey(businessKey);

        Map<String, Object> parameters = new HashMap<String, Object>();
        Long processInstanceId = processClient.startProcess(CONTAINER_ID, PROCESS_ID_EVALUATION, key, parameters);
        try {
            List<ProcessInstance> returnedProcessInstances = new ArrayList<ProcessInstance>();

            ProcessInstance instance = queryClient.findProcessInstanceById(processInstanceId);
            returnedProcessInstances.add(instance);

            instance = queryClient.findProcessInstanceByCorrelationKey(key);
            returnedProcessInstances.add(instance);

            List<ProcessInstance> processInstances = queryClient.findProcessInstancesByCorrelationKey(key, 0, 100);
            assertNotNull(processInstances);
            // Separate active instances as response contains also instances already closed or aborted.
            List<ProcessInstance> activeInstances = new ArrayList<ProcessInstance>();
            for (ProcessInstance processInstance : processInstances) {
                if (org.kie.api.runtime.process.ProcessInstance.STATE_ACTIVE == processInstance.getState().intValue()) {
                    activeInstances.add(processInstance);
                }
            }
            assertEquals(1, activeInstances.size());
            returnedProcessInstances.addAll(activeInstances);

            // All returned instances should contain all values
            for (ProcessInstance returnedProcessInstance : returnedProcessInstances) {
                assertNotNull(returnedProcessInstance);
                assertEquals(processInstanceId, returnedProcessInstance.getId());
                assertEquals(PROCESS_ID_EVALUATION, returnedProcessInstance.getProcessId());
                assertEquals("evaluation", returnedProcessInstance.getProcessName());
                assertEquals("1.0", returnedProcessInstance.getProcessVersion());
                assertEquals(USER_YODA, returnedProcessInstance.getInitiator());
                assertEquals(CONTAINER_ID, returnedProcessInstance.getContainerId());
                assertEquals(businessKey, returnedProcessInstance.getCorrelationKey());
                assertEquals("evaluation", returnedProcessInstance.getProcessInstanceDescription());
                assertEquals(-1, returnedProcessInstance.getParentId().longValue());
                assertEquals(org.kie.api.runtime.process.ProcessInstance.STATE_ACTIVE, returnedProcessInstance.getState().intValue());
            }
        } finally {
            processClient.abortProcessInstance(CONTAINER_ID, processInstanceId);
        }
    }

    @Test
    public void testGetProcessInstanceByCorrelationKeyPaging() throws Exception {
        assertSuccess(client.createContainer(CONTAINER_ID, new KieContainerResource(CONTAINER_ID, releaseId)));

        CorrelationKeyFactory correlationKeyFactory = KieInternalServices.Factory.get().newCorrelationKeyFactory();

        String businessKey = "simple-key";
        CorrelationKey key = correlationKeyFactory.newCorrelationKey(businessKey);

        // Start and abort 2 processes to be sure that there are processes to be returned.
        Map<String, Object> parameters = new HashMap<String, Object>();
        Long processInstanceId1 = processClient.startProcess(CONTAINER_ID, PROCESS_ID_EVALUATION, key, parameters);
        processClient.abortProcessInstance(CONTAINER_ID, processInstanceId1);
        Long processInstanceId2 = processClient.startProcess(CONTAINER_ID, PROCESS_ID_EVALUATION, key, parameters);
        processClient.abortProcessInstance(CONTAINER_ID, processInstanceId2);

        List<ProcessInstance> processInstancesPage0 = queryClient.findProcessInstancesByCorrelationKey(key, 0, 1);
        List<ProcessInstance> processInstancesPage1 = queryClient.findProcessInstancesByCorrelationKey(key, 1, 1);
        assertEquals(1, processInstancesPage0.size());
        assertEquals(1, processInstancesPage1.size());
        assertNotEquals("Process instances are same! Paging doesn't work.", processInstancesPage0.get(0).getId(), processInstancesPage1.get(0).getId());
    }

    @Test
    public void testGetProcessInstancesByVariableName() throws Exception {
        assertSuccess(client.createContainer("definition-project", new KieContainerResource("definition-project", releaseId)));

        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("stringData", "waiting for signal");
        parameters.put("personData", createPersonInstance("john"));

        List<Long> processInstanceIds = createProcessInstances(parameters);

        try {
            List<ProcessInstance> instances = queryClient.findProcessInstancesByVariable("stringData", null, 0, 10);
            assertNotNull(instances);
            assertEquals(5, instances.size());

            List<Long> found = collectInstances(instances);
            assertEquals(processInstanceIds, found);

            instances = queryClient.findProcessInstancesByVariable("stringData", null, 0, 3);
            assertNotNull(instances);
            assertEquals(3, instances.size());

            instances = queryClient.findProcessInstancesByVariable("stringData", null, 1, 3);
            assertNotNull(instances);
            assertEquals(2, instances.size());

        } finally {
            abortProcessInstances(processInstanceIds);
        }

    }

    @Test
    public void testGetProcessInstancesByVariableNameAndValue() throws Exception {
        assertSuccess(client.createContainer("definition-project", new KieContainerResource("definition-project", releaseId)));

        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("personData", createPersonInstance("john"));

        List<Long> processInstanceIds = createProcessInstances(parameters);

        for (Long processInstanceId : processInstanceIds) {
            processClient.setProcessVariable("definition-project", processInstanceId, "stringData", "waiting for signal");
        }

        try {
            List<ProcessInstance> instances = queryClient.findProcessInstancesByVariableAndValue("stringData", "waiting%", null, 0, 10);
            assertNotNull(instances);
            assertEquals(5, instances.size());

            List<Long> found = collectInstances(instances);
            assertEquals(processInstanceIds, found);

            instances = queryClient.findProcessInstancesByVariableAndValue("stringData", "waiting%", null, 0, 3);
            assertNotNull(instances);
            assertEquals(3, instances.size());

            instances = queryClient.findProcessInstancesByVariableAndValue("stringData", "waiting%", null, 1, 3);
            assertNotNull(instances);
            assertEquals(2, instances.size());

            processClient.setProcessVariable("definition-project", processInstanceIds.get(0), "stringData", "updated value");

            instances = queryClient.findProcessInstancesByVariableAndValue("stringData", "waiting%", null, 0, 10);
            assertNotNull(instances);
            assertEquals(4, instances.size());

            instances = queryClient.findProcessInstancesByVariableAndValue("stringData", "updated value", null, 0, 10);
            assertNotNull(instances);
            assertEquals(1, instances.size());

        } finally {
            abortProcessInstances(processInstanceIds);
        }

    }

    @Test
    public void testGetNodeInstances() throws Exception {
        assertSuccess(client.createContainer("definition-project", new KieContainerResource("definition-project", releaseId)));

        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("stringData", "waiting for signal");
        parameters.put("personData", createPersonInstance("john"));

        Long processInstanceId = processClient.startProcess("definition-project", "definition-project.usertask", parameters);

        try {
            List<NodeInstance> instances = queryClient.findActiveNodeInstances(processInstanceId, 0, 10);
            assertNotNull(instances);
            assertEquals(1, instances.size());

            NodeInstance nodeInstance = instances.get(0);
            assertNotNull(nodeInstance);
            assertEquals("First task", nodeInstance.getName());
            assertEquals("definition-project", nodeInstance.getContainerId());
            assertEquals("HumanTaskNode", nodeInstance.getNodeType());
            assertEquals(false, nodeInstance.getCompleted());
            assertEquals(processInstanceId, nodeInstance.getProcessInstanceId());
            assertNotNull(nodeInstance.getWorkItemId());
            assertNotNull(nodeInstance.getDate());

            nodeInstance = queryClient.findNodeInstanceByWorkItemId(processInstanceId, nodeInstance.getWorkItemId());
            assertNotNull(nodeInstance);
            assertEquals("First task", nodeInstance.getName());
            assertEquals("definition-project", nodeInstance.getContainerId());
            assertEquals("HumanTaskNode", nodeInstance.getNodeType());
            assertEquals(false, nodeInstance.getCompleted());
            assertEquals(processInstanceId, nodeInstance.getProcessInstanceId());
            assertNotNull(nodeInstance.getWorkItemId());
            assertNotNull(nodeInstance.getDate());


            instances = queryClient.findCompletedNodeInstances(processInstanceId, 0, 10);
            assertNotNull(instances);
            assertEquals(1, instances.size());

            nodeInstance = instances.get(0);
            assertNotNull(nodeInstance);
            assertEquals("start", nodeInstance.getName());
            assertEquals("definition-project", nodeInstance.getContainerId());
            assertEquals("StartNode", nodeInstance.getNodeType());
            assertEquals(true, nodeInstance.getCompleted());
            assertEquals(processInstanceId, nodeInstance.getProcessInstanceId());
            assertNull(nodeInstance.getWorkItemId());
            assertNotNull(nodeInstance.getDate());

            instances = queryClient.findNodeInstances(processInstanceId, 0, 10);
            assertNotNull(instances);
            assertEquals(3, instances.size());

            nodeInstance = instances.get(0);
            assertNotNull(nodeInstance);
            assertEquals("start", nodeInstance.getName());
            assertEquals("definition-project", nodeInstance.getContainerId());
            assertEquals("StartNode", nodeInstance.getNodeType());
            assertEquals(true, nodeInstance.getCompleted());
            assertEquals(processInstanceId, nodeInstance.getProcessInstanceId());
            assertNull(nodeInstance.getWorkItemId());
            assertNotNull(nodeInstance.getDate());

            nodeInstance = instances.get(1);
            assertNotNull(nodeInstance);
            assertEquals("First task", nodeInstance.getName());
            assertEquals("definition-project", nodeInstance.getContainerId());
            assertEquals("HumanTaskNode", nodeInstance.getNodeType());
            assertEquals(false, nodeInstance.getCompleted());
            assertEquals(processInstanceId, nodeInstance.getProcessInstanceId());
            assertNotNull(nodeInstance.getWorkItemId());
            assertNotNull(nodeInstance.getDate());

            nodeInstance = instances.get(2);
            assertNotNull(nodeInstance);
            assertEquals("start", nodeInstance.getName());
            assertEquals("definition-project", nodeInstance.getContainerId());
            assertEquals("StartNode", nodeInstance.getNodeType());
            assertEquals(false, nodeInstance.getCompleted());
            assertEquals(processInstanceId, nodeInstance.getProcessInstanceId());
            assertNull(nodeInstance.getWorkItemId());
            assertNotNull(nodeInstance.getDate());



        } finally {
            processClient.abortProcessInstance("definition-project", processInstanceId);
        }

    }

    @Test
    public void testGetVariableInstance() throws Exception {
        assertSuccess(client.createContainer("definition-project", new KieContainerResource("definition-project", releaseId)));

        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("stringData", "waiting for signal");
        parameters.put("personData", createPersonInstance("john"));

        Long processInstanceId = processClient.startProcess("definition-project", "definition-project.usertask", parameters);

        try {

            List<VariableInstance> currentState = queryClient.findVariablesCurrentState(processInstanceId);
            assertNotNull(currentState);
            assertEquals(2, currentState.size());

            for (VariableInstance variableInstance : currentState) {

                if ("personData".equals(variableInstance.getVariableName())) {
                    assertNotNull(variableInstance);
                    assertEquals(processInstanceId, variableInstance.getProcessInstanceId());
                    assertNullOrEmpty(variableInstance.getOldValue());
                    assertEquals("Person{name='john'}", variableInstance.getValue());
                    assertEquals("personData", variableInstance.getVariableName());
                } else if ("stringData".equals(variableInstance.getVariableName())) {

                    assertNotNull(variableInstance);
                    assertEquals(processInstanceId, variableInstance.getProcessInstanceId());
                    assertNullOrEmpty(variableInstance.getOldValue());
                    assertEquals("waiting for signal", variableInstance.getValue());
                    assertEquals("stringData", variableInstance.getVariableName());
                } else {
                    fail("Got unexpected variable " + variableInstance.getVariableName());
                }
            }

            List<VariableInstance> varHistory = queryClient.findVariableHistory(processInstanceId, "stringData", 0, 10);
            assertNotNull(varHistory);
            assertEquals(1, varHistory.size());

            VariableInstance variableInstance = varHistory.get(0);
            assertNotNull(variableInstance);
            assertEquals(processInstanceId, variableInstance.getProcessInstanceId());
            assertNullOrEmpty(variableInstance.getOldValue());
            assertEquals("waiting for signal", variableInstance.getValue());
            assertEquals("stringData", variableInstance.getVariableName());

            processClient.setProcessVariable("definition-project", processInstanceId, "stringData", "updated value");

            currentState = queryClient.findVariablesCurrentState(processInstanceId);
            assertNotNull(currentState);
            assertEquals(2, currentState.size());

            for (VariableInstance variable : currentState) {
                if ("personData".equals(variable.getVariableName())) {
                    assertNotNull(variable);
                    assertEquals(processInstanceId, variable.getProcessInstanceId());
                    assertNullOrEmpty(variable.getOldValue());
                    assertEquals("Person{name='john'}", variable.getValue());
                    assertEquals("personData", variable.getVariableName());
                } else if ("stringData".equals(variable.getVariableName())) {
                    assertNotNull(variable);
                    assertEquals(processInstanceId, variable.getProcessInstanceId());
                    assertEquals("waiting for signal", variable.getOldValue());
                    assertEquals("updated value", variable.getValue());
                    assertEquals("stringData", variable.getVariableName());
                } else {
                    fail("Got unexpected variable " + variable.getVariableName());
                }
            }

            varHistory = queryClient.findVariableHistory(processInstanceId, "stringData", 0, 10);
            assertNotNull(varHistory);
            assertEquals(2, varHistory.size());

            variableInstance = varHistory.get(0);
            assertNotNull(variableInstance);
            assertEquals(processInstanceId, variableInstance.getProcessInstanceId());
            assertEquals("waiting for signal", variableInstance.getOldValue());
            assertEquals("updated value", variableInstance.getValue());
            assertEquals("stringData", variableInstance.getVariableName());

            variableInstance = varHistory.get(1);
            assertNotNull(variableInstance);
            assertEquals(processInstanceId, variableInstance.getProcessInstanceId());
            assertNullOrEmpty(variableInstance.getOldValue());
            assertEquals("waiting for signal", variableInstance.getValue());
            assertEquals("stringData", variableInstance.getVariableName());

        } finally {
            processClient.abortProcessInstance("definition-project", processInstanceId);
        }

    }

    @Test
    public void testFindTasks() throws Exception {
        assertSuccess(client.createContainer("definition-project", new KieContainerResource("definition-project", releaseId)));

        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("stringData", "waiting for signal");
        parameters.put("personData", createPersonInstance("john"));

        Long processInstanceId = processClient.startProcess("definition-project", "definition-project.usertask", parameters);

        try {

            List<TaskSummary> tasks = taskClient.findTasks("yoda", 0, 50);
            assertNotNull(tasks);

            TaskSummary taskInstance = null;
            for (TaskSummary t : tasks) {
                if (t.getProcessInstanceId().equals(processInstanceId)) {
                    taskInstance = t;
                    break;
                }
            }

            assertNotNull(taskInstance);
            assertEquals("First task", taskInstance.getName());
            assertNullOrEmpty(taskInstance.getDescription());
            assertEquals("Reserved", taskInstance.getStatus());
            assertEquals(0, taskInstance.getPriority().intValue());
            assertEquals("yoda", taskInstance.getActualOwner());
            assertEquals("yoda", taskInstance.getCreatedBy());
            assertEquals("definition-project.usertask", taskInstance.getProcessId());
            assertEquals("definition-project", taskInstance.getContainerId());
            assertEquals(-1, taskInstance.getParentId().longValue());
            assertEquals(processInstanceId, taskInstance.getProcessInstanceId());

            TaskInstance taskById = taskClient.findTaskById(taskInstance.getId());
            assertNotNull(taskById);
            assertEquals("First task", taskById.getName());
            assertNullOrEmpty(taskById.getDescription());
            assertEquals("Reserved", taskById.getStatus());
            assertEquals(0, taskById.getPriority().intValue());
            assertEquals("yoda", taskById.getActualOwner());
            assertEquals("yoda", taskById.getCreatedBy());
            assertEquals("definition-project.usertask", taskById.getProcessId());
            assertEquals("definition-project", taskById.getContainerId());
            assertEquals(processInstanceId, taskById.getProcessInstanceId());

            List<WorkItemInstance> workItems = processClient.getWorkItemByProcessInstance("definition-project", processInstanceId);
            assertNotNull(workItems);
            assertEquals(1, workItems.size());

            taskById = taskClient.findTaskByWorkItemId(workItems.get(0).getId());
            assertNotNull(taskById);
            assertEquals("First task", taskById.getName());
            assertNullOrEmpty(taskById.getDescription());
            assertEquals("Reserved", taskById.getStatus());
            assertEquals(0, taskById.getPriority().intValue());
            assertEquals("yoda", taskById.getActualOwner());
            assertEquals("yoda", taskById.getCreatedBy());
            assertEquals("definition-project.usertask", taskById.getProcessId());
            assertEquals("definition-project", taskById.getContainerId());
            assertEquals(processInstanceId, taskById.getProcessInstanceId());


        } finally {
            processClient.abortProcessInstance("definition-project", processInstanceId);
        }
    }

    @Test
    public void testFindTaskEvents() throws Exception {
        assertSuccess(client.createContainer("definition-project", new KieContainerResource("definition-project", releaseId)));

        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("stringData", "waiting for signal");
        parameters.put("personData", createPersonInstance("john"));

        Long processInstanceId = processClient.startProcess("definition-project", "definition-project.usertask", parameters);

        try {

            List<TaskSummary> tasks = taskClient.findTasksByStatusByProcessInstanceId(processInstanceId, null, 0, 10);
            assertNotNull(tasks);
            assertEquals(1, tasks.size());

            TaskSummary taskInstance = tasks.get(0);

            List<TaskEventInstance> events = taskClient.findTaskEvents(taskInstance.getId(), 0, 10);
            assertNotNull(events);
            assertEquals(1, events.size());

            TaskEventInstance event = events.get(0);
            assertNotNull(event);
            assertEquals(TaskEvent.TaskEventType.ADDED.toString(), event.getType());
            assertEquals(processInstanceId, event.getProcessInstanceId());
            assertEquals(taskInstance.getId(), event.getTaskId());
            assertEquals("definition-project.usertask", event.getUserId());   // is this really correct to set process id as user for added task

            // now let's start it
            taskClient.startTask("definition-project", taskInstance.getId(), "yoda");
            events = taskClient.findTaskEvents(taskInstance.getId(), 0, 10);
            assertNotNull(events);
            assertEquals(2, events.size());

            event = getTaskEventInstanceFromListByType(events, TaskEvent.TaskEventType.ADDED.toString());
            assertNotNull(event);
            assertEquals(TaskEvent.TaskEventType.ADDED.toString(), event.getType());
            assertEquals(processInstanceId, event.getProcessInstanceId());
            assertEquals(taskInstance.getId(), event.getTaskId());
            assertEquals("definition-project.usertask", event.getUserId());    // is this really correct to set process id as user for added task

            event = getTaskEventInstanceFromListByType(events, TaskEvent.TaskEventType.STARTED.toString());
            assertNotNull(event);
            assertEquals(TaskEvent.TaskEventType.STARTED.toString(), event.getType());
            assertEquals(processInstanceId, event.getProcessInstanceId());
            assertEquals(taskInstance.getId(), event.getTaskId());
            assertEquals("yoda", event.getUserId());

            // now let's stop it
            taskClient.stopTask("definition-project", taskInstance.getId(), "yoda");

            events = taskClient.findTaskEvents(taskInstance.getId(), 0, 10);
            assertNotNull(events);
            assertEquals(3, events.size());

            event = getTaskEventInstanceFromListByType(events, TaskEvent.TaskEventType.ADDED.toString());
            assertNotNull(event);
            assertEquals(TaskEvent.TaskEventType.ADDED.toString(), event.getType());
            assertEquals(processInstanceId, event.getProcessInstanceId());
            assertEquals(taskInstance.getId(), event.getTaskId());
            assertEquals("definition-project.usertask", event.getUserId());    // is this really correct to set process id as user for added task

            event = getTaskEventInstanceFromListByType(events, TaskEvent.TaskEventType.STARTED.toString());
            assertNotNull(event);
            assertEquals(TaskEvent.TaskEventType.STARTED.toString(), event.getType());
            assertEquals(processInstanceId, event.getProcessInstanceId());
            assertEquals(taskInstance.getId(), event.getTaskId());
            assertEquals("yoda", event.getUserId());

            event = getTaskEventInstanceFromListByType(events, TaskEvent.TaskEventType.STOPPED.toString());
            assertNotNull(event);
            assertEquals(TaskEvent.TaskEventType.STOPPED.toString(), event.getType());
            assertEquals(processInstanceId, event.getProcessInstanceId());
            assertEquals(taskInstance.getId(), event.getTaskId());
            assertEquals("yoda", event.getUserId());

        } finally {
            processClient.abortProcessInstance("definition-project", processInstanceId);
        }
    }

    private TaskEventInstance getTaskEventInstanceFromListByType(List<TaskEventInstance> events, String type) {
        for (TaskEventInstance t : events) {
            if (t.getType().equals(type)) {
                return t;
            }
        }
        return null;
    }

    @Test
    public void testFindTasksOwned() throws Exception {
        assertSuccess(client.createContainer("definition-project", new KieContainerResource("definition-project", releaseId)));

        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("stringData", "waiting for signal");
        parameters.put("personData", createPersonInstance("john"));

        Long processInstanceId = processClient.startProcess("definition-project", "definition-project.usertask", parameters);

        try {

            List<TaskSummary> tasks = taskClient.findTasksOwned("yoda", 0, 10);
            assertNotNull(tasks);
            assertEquals(1, tasks.size());

            TaskSummary taskInstance = tasks.get(0);
            assertNotNull(taskInstance);
            assertEquals("First task", taskInstance.getName());
            assertNullOrEmpty(taskInstance.getDescription());
            assertEquals("Reserved", taskInstance.getStatus());
            assertEquals(0, taskInstance.getPriority().intValue());
            assertEquals("yoda", taskInstance.getActualOwner());
            assertEquals("yoda", taskInstance.getCreatedBy());
            assertEquals("definition-project.usertask", taskInstance.getProcessId());
            assertEquals("definition-project", taskInstance.getContainerId());
            assertEquals(-1, taskInstance.getParentId().longValue());
            assertEquals(processInstanceId, taskInstance.getProcessInstanceId());

            List<String> status = new ArrayList<String>();
            status.add(Status.InProgress.toString());

            tasks = taskClient.findTasksOwned("yoda", status, 0, 10);
            assertNotNull(tasks);
            assertEquals(0, tasks.size());

            taskClient.startTask("definition-project", taskInstance.getId(), "yoda");
            tasks = taskClient.findTasksOwned("yoda", status, 0, 10);
            assertNotNull(tasks);
            assertEquals(1, tasks.size());

            taskInstance = tasks.get(0);
            assertNotNull(taskInstance);
            assertEquals("First task", taskInstance.getName());
            assertNullOrEmpty(taskInstance.getDescription());
            assertEquals("InProgress", taskInstance.getStatus());
            assertEquals(0, taskInstance.getPriority().intValue());
            assertEquals("yoda", taskInstance.getActualOwner());
            assertEquals("yoda", taskInstance.getCreatedBy());
            assertEquals("definition-project.usertask", taskInstance.getProcessId());
            assertEquals("definition-project", taskInstance.getContainerId());
            assertEquals(-1, taskInstance.getParentId().longValue());
            assertEquals(processInstanceId, taskInstance.getProcessInstanceId());

        } finally {
            processClient.abortProcessInstance("definition-project", processInstanceId);
        }
    }

    @Test
    public void testFindTasksAssignedAsPotentialOwner() throws Exception {
        assertSuccess(client.createContainer("definition-project", new KieContainerResource("definition-project", releaseId)));

        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("stringData", "waiting for signal");
        parameters.put("personData", createPersonInstance("john"));

        Long processInstanceId = processClient.startProcess("definition-project", "definition-project.usertask", parameters);

        try {

            List<TaskSummary> tasks = taskClient.findTasksAssignedAsPotentialOwner("yoda", 0, 10);
            assertNotNull(tasks);
            assertEquals(1, tasks.size());

            TaskSummary taskInstance = tasks.get(0);
            assertNotNull(taskInstance);
            assertEquals("First task", taskInstance.getName());
            assertNullOrEmpty(taskInstance.getDescription());
            assertEquals("Reserved", taskInstance.getStatus());
            assertEquals(0, taskInstance.getPriority().intValue());
            assertEquals("yoda", taskInstance.getActualOwner());
            assertEquals("yoda", taskInstance.getCreatedBy());
            assertEquals("definition-project.usertask", taskInstance.getProcessId());
            assertEquals("definition-project", taskInstance.getContainerId());
            assertEquals(-1, taskInstance.getParentId().longValue());
            assertEquals(processInstanceId, taskInstance.getProcessInstanceId());

            List<String> status = new ArrayList<String>();
            status.add(Status.InProgress.toString());

            tasks = taskClient.findTasksAssignedAsPotentialOwner("yoda", status, 0, 10);
            assertNotNull(tasks);
            assertEquals(0, tasks.size());

            taskClient.startTask("definition-project", taskInstance.getId(), "yoda");
            tasks = taskClient.findTasksAssignedAsPotentialOwner("yoda", status, 0, 10);
            assertNotNull(tasks);
            assertEquals(1, tasks.size());

            taskInstance = tasks.get(0);
            assertNotNull(taskInstance);
            assertEquals("First task", taskInstance.getName());
            assertNullOrEmpty(taskInstance.getDescription());
            assertEquals("InProgress", taskInstance.getStatus());
            assertEquals(0, taskInstance.getPriority().intValue());
            assertEquals("yoda", taskInstance.getActualOwner());
            assertEquals("yoda", taskInstance.getCreatedBy());
            assertEquals("definition-project.usertask", taskInstance.getProcessId());
            assertEquals("definition-project", taskInstance.getContainerId());
            assertEquals(-1, taskInstance.getParentId().longValue());
            assertEquals(processInstanceId, taskInstance.getProcessInstanceId());

        } finally {
            processClient.abortProcessInstance("definition-project", processInstanceId);
        }
    }

    @Test
    public void testFindTasksByStatusByProcessInstanceId() throws Exception {
        assertSuccess(client.createContainer("definition-project", new KieContainerResource("definition-project", releaseId)));

        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("stringData", "waiting for signal");
        parameters.put("personData", createPersonInstance("john"));

        Long processInstanceId = processClient.startProcess("definition-project", "definition-project.usertask", parameters);

        try {
            List<String> status = new ArrayList<String>();
            status.add(Status.Reserved.toString());
            status.add(Status.InProgress.toString());

            List<TaskSummary> tasks = taskClient.findTasksByStatusByProcessInstanceId(processInstanceId, status, 0, 10);
            assertNotNull(tasks);
            assertEquals(1, tasks.size());

            TaskSummary taskInstance = tasks.get(0);
            assertNotNull(taskInstance);
            assertEquals("First task", taskInstance.getName());
            assertNullOrEmpty(taskInstance.getDescription());
            assertEquals("Reserved", taskInstance.getStatus());
            assertEquals(0, taskInstance.getPriority().intValue());
            assertEquals("yoda", taskInstance.getActualOwner());
            assertEquals("yoda", taskInstance.getCreatedBy());
            assertEquals("definition-project.usertask", taskInstance.getProcessId());
            assertEquals("definition-project", taskInstance.getContainerId());
            assertEquals(-1, taskInstance.getParentId().longValue());
            assertEquals(processInstanceId, taskInstance.getProcessInstanceId());

            status = new ArrayList<String>();
            status.add(Status.InProgress.toString());

            tasks = taskClient.findTasksByStatusByProcessInstanceId(processInstanceId, status, 0, 10);
            assertNotNull(tasks);
            assertEquals(0, tasks.size());

            taskClient.startTask("definition-project", taskInstance.getId(), "yoda");
            tasks = taskClient.findTasksByStatusByProcessInstanceId(processInstanceId, status, 0, 10);
            assertNotNull(tasks);
            assertEquals(1, tasks.size());

            taskInstance = tasks.get(0);
            assertNotNull(taskInstance);
            assertEquals("First task", taskInstance.getName());
            assertNullOrEmpty(taskInstance.getDescription());
            assertEquals("InProgress", taskInstance.getStatus());
            assertEquals(0, taskInstance.getPriority().intValue());
            assertEquals("yoda", taskInstance.getActualOwner());
            assertEquals("yoda", taskInstance.getCreatedBy());
            assertEquals("definition-project.usertask", taskInstance.getProcessId());
            assertEquals("definition-project", taskInstance.getContainerId());
            assertEquals(-1, taskInstance.getParentId().longValue());
            assertEquals(processInstanceId, taskInstance.getProcessInstanceId());

        } finally {
            processClient.abortProcessInstance("definition-project", processInstanceId);
        }
    }

    protected List<Long> createProcessInstances(Map<String, Object> parameters) {
        List<Long> processInstanceIds = new ArrayList<Long>();

        processInstanceIds.add(processClient.startProcess("definition-project", "definition-project.signalprocess", parameters));
        processInstanceIds.add(processClient.startProcess("definition-project", "definition-project.usertask", parameters));
        processInstanceIds.add(processClient.startProcess("definition-project", "definition-project.signalprocess", parameters));
        processInstanceIds.add(processClient.startProcess("definition-project", "definition-project.usertask", parameters));
        processInstanceIds.add(processClient.startProcess("definition-project", "definition-project.signalprocess", parameters));

        Collections.sort(processInstanceIds);
        return processInstanceIds;
    }

    protected void abortProcessInstances(List<Long> processInstanceIds) {
        for (Long piId : processInstanceIds) {
            processClient.abortProcessInstance("definition-project", piId);
        }
    }

    protected List<String> collectDefinitions(List<ProcessDefinition> definitions) {
        List<String> ids = new ArrayList<String>();

        for (ProcessDefinition definition : definitions) {
            ids.add(definition.getId());
        }
        return ids;
    }

    protected List<Long> collectInstances(List<ProcessInstance> instances) {
        List<Long> ids = new ArrayList<Long>();

        for (ProcessInstance instance : instances) {
            ids.add(instance.getId());
        }
        return ids;
    }
}
