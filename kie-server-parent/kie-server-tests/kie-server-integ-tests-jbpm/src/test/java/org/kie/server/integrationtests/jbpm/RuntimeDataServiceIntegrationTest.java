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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.BeforeClass;
import org.junit.Test;
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
import org.kie.server.client.KieServicesClient;
import org.kie.server.client.KieServicesConfiguration;
import org.kie.server.client.KieServicesException;
import org.kie.server.client.KieServicesFactory;
import org.kie.server.integrationtests.config.TestConfig;

import static org.junit.Assert.*;

public class RuntimeDataServiceIntegrationTest extends JbpmKieServerBaseIntegrationTest {

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

            localServerConfig.addJaxbClasses(extraClasses);
            return KieServicesFactory.newKieServicesClient(localServerConfig, kieContainer.getClassLoader());
        } else {
            configuration.setMarshallingFormat(marshallingFormat);
            configuration.addJaxbClasses(extraClasses);
            return KieServicesFactory.newKieServicesClient(configuration, kieContainer.getClassLoader());
        }
    }

    @Test
    public void testGetProcessDefinitions() throws Exception {
        assertSuccess(client.createContainer("definition-project", new KieContainerResource("definition-project", releaseId)));

        List<ProcessDefinition> definitions = client.findProcesses(0, 20);
        assertNotNull(definitions);

        assertEquals(4, definitions.size());
        List<String> processIds = collectDefinitions(definitions);
        assertTrue(processIds.contains("definition-project.call-evaluation"));
        assertTrue(processIds.contains("definition-project.evaluation"));
        assertTrue(processIds.contains("definition-project.signalprocess"));
        assertTrue(processIds.contains("definition-project.usertask"));

        // test paging of the result
        definitions = client.findProcesses(0, 3);

        assertNotNull(definitions);
        assertEquals(3, definitions.size());
        processIds = collectDefinitions(definitions);
        assertTrue(processIds.contains("definition-project.call-evaluation"));
        assertTrue(processIds.contains("definition-project.evaluation"));
        assertTrue(processIds.contains("definition-project.signalprocess"));

        definitions = client.findProcesses(1, 3);
        assertNotNull(definitions);

        assertEquals(1, definitions.size());
        processIds = collectDefinitions(definitions);
        assertTrue(processIds.contains("definition-project.usertask"));

    }

    @Test
    public void testGetProcessDefinitionsWithFilter() throws Exception {
        assertSuccess(client.createContainer("definition-project", new KieContainerResource("definition-project", releaseId)));

        List<ProcessDefinition> definitions = client.findProcesses("evaluation", 0, 20);
        assertNotNull(definitions);

        assertEquals(2, definitions.size());
        List<String> processIds = collectDefinitions(definitions);
        assertTrue(processIds.contains("definition-project.call-evaluation"));
        assertTrue(processIds.contains("definition-project.evaluation"));

        // test paging of the result
        definitions = client.findProcesses("evaluation", 0, 1);

        assertNotNull(definitions);
        assertEquals(1, definitions.size());
        processIds = collectDefinitions(definitions);
        assertTrue(processIds.contains("definition-project.call-evaluation"));

        definitions = client.findProcesses("evaluation", 1, 1);
        assertNotNull(definitions);

        assertEquals(1, definitions.size());
        processIds = collectDefinitions(definitions);
        assertTrue(processIds.contains("definition-project.evaluation"));

    }

    @Test
    public void testGetProcessDefinitionsByContainer() throws Exception {
        assertSuccess(client.createContainer("definition-project", new KieContainerResource("definition-project", releaseId)));

        List<ProcessDefinition> definitions = client.findProcessesByContainerId("definition-project", 0, 20);
        assertNotNull(definitions);

        assertEquals(4, definitions.size());
        List<String> processIds = collectDefinitions(definitions);
        assertTrue(processIds.contains("definition-project.call-evaluation"));
        assertTrue(processIds.contains("definition-project.evaluation"));
        assertTrue(processIds.contains("definition-project.signalprocess"));
        assertTrue(processIds.contains("definition-project.usertask"));

        // test paging of the result
        definitions = client.findProcessesByContainerId("definition-project", 0, 3);

        assertNotNull(definitions);
        assertEquals(3, definitions.size());
        processIds = collectDefinitions(definitions);
        assertTrue(processIds.contains("definition-project.call-evaluation"));
        assertTrue(processIds.contains("definition-project.evaluation"));
        assertTrue(processIds.contains("definition-project.signalprocess"));

        definitions = client.findProcessesByContainerId("definition-project", 1, 3);
        assertNotNull(definitions);

        assertEquals(1, definitions.size());
        processIds = collectDefinitions(definitions);
        assertTrue(processIds.contains("definition-project.usertask"));

        // last check if there are process def for not existing project
        definitions = client.findProcessesByContainerId("not-existing-project", 0, 10);
        assertNotNull(definitions);

        assertEquals(0, definitions.size());

    }

    @Test
    public void testGetProcessDefinitionsById() throws Exception {
        assertSuccess(client.createContainer("definition-project", new KieContainerResource("definition-project", releaseId)));

        List<ProcessDefinition> definitions = client.findProcessesById("definition-project.usertask");
        assertNotNull(definitions);

        assertEquals(1, definitions.size());
        List<String> processIds = collectDefinitions(definitions);
        assertTrue(processIds.contains("definition-project.usertask"));


         // last check if there are process def for not existing project
        definitions = client.findProcessesById("not-existing-project");
        assertNotNull(definitions);

        assertEquals(0, definitions.size());

    }

    @Test
    public void testGetProcessDefinitionByContainerAndId() throws Exception {
        assertSuccess(client.createContainer("definition-project", new KieContainerResource("definition-project", releaseId)));

        ProcessDefinition definition = client.findProcessByContainerIdProcessId("definition-project", "definition-project.usertask");
        assertNotNull(definition);
        assertEquals("definition-project.usertask", definition.getId());
        assertEquals("usertask", definition.getName());
        assertEquals("1.0", definition.getVersion());
        assertEquals("org.jbpm", definition.getPackageName());
        assertEquals("definition-project", definition.getContainerId());

    }

    @Test(expected = KieServicesException.class)
    public void testGetProcessDefinitionByContainerAndNonExistingId() throws Exception {
        assertSuccess(client.createContainer("definition-project", new KieContainerResource("definition-project", releaseId)));

        client.findProcessByContainerIdProcessId("definition-project", "non-existing");

    }

    @Test
    public void testGetProcessInstances() throws Exception {
        assertSuccess(client.createContainer("definition-project", new KieContainerResource("definition-project", releaseId)));

        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("stringData", "waiting for signal");
        parameters.put("personData", createPersonInstance("john"));

        List<Long> processInstanceIds = createProcessInstances(parameters);

        try {
            List<ProcessInstance> instances = client.findProcessInstances(0, 10);
            assertNotNull(instances);
            assertEquals(5, instances.size());

            List<Long> found = collectInstances(instances);
            assertEquals(processInstanceIds, found);

            instances = client.findProcessInstances(0, 3);
            assertNotNull(instances);
            assertEquals(3, instances.size());

            instances = client.findProcessInstances(1, 3);
            assertNotNull(instances);
            assertEquals(2, instances.size());

        } finally {
            abortProcessInstances(processInstanceIds);
        }

    }

    @Test
    public void testGetProcessInstancesByContainer() throws Exception {
        assertSuccess(client.createContainer("definition-project", new KieContainerResource("definition-project", releaseId)));

        int offset = client.findProcessInstancesByContainerId("definition-project", Collections.singletonList(2), 0, 10).size();

        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("stringData", "waiting for signal");
        parameters.put("personData", createPersonInstance("john"));

        List<Long> processInstanceIds = createProcessInstances(parameters);

        try {
            List<ProcessInstance> instances = client.findProcessInstancesByContainerId("definition-project", null, 0, 10);
            assertNotNull(instances);
            assertEquals(5, instances.size());

            List<Long> found = collectInstances(instances);
            assertEquals(processInstanceIds, found);

            instances = client.findProcessInstancesByContainerId("definition-project", null, 0, 3);
            assertNotNull(instances);
            assertEquals(3, instances.size());

            instances = client.findProcessInstancesByContainerId("definition-project", null, 1, 3);
            assertNotNull(instances);
            assertEquals(2, instances.size());

            // search for completed only
            instances = client.findProcessInstancesByContainerId("definition-project", Collections.singletonList(2), 0, 10);
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
            List<ProcessInstance> instances = client.findProcessInstancesByProcessId("definition-project.usertask", null, 0, 10);
            assertNotNull(instances);
            assertEquals(2, instances.size());
            assertEquals("definition-project.usertask", instances.get(0).getProcessId());
            assertEquals("definition-project.usertask", instances.get(1).getProcessId());

            instances = client.findProcessInstancesByProcessId("definition-project.usertask", null, 0, 1);
            assertNotNull(instances);
            assertEquals(1, instances.size());
            assertEquals("definition-project.usertask", instances.get(0).getProcessId());

            instances = client.findProcessInstancesByProcessId("definition-project.usertask", null, 1, 1);
            assertNotNull(instances);
            assertEquals(1, instances.size());
            assertEquals("definition-project.usertask", instances.get(0).getProcessId());

            // search for completed only
            instances = client.findProcessInstancesByProcessId("definition-project.usertask", Collections.singletonList(2), 0, 10);
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
            List<ProcessInstance> instances = client.findProcessInstancesByProcessName("usertask", null, 0, 10);
            assertNotNull(instances);
            assertEquals(2, instances.size());
            assertEquals("definition-project.usertask", instances.get(0).getProcessId());
            assertEquals("definition-project.usertask", instances.get(1).getProcessId());

            instances = client.findProcessInstancesByProcessName("usertask", null, 0, 1);
            assertNotNull(instances);
            assertEquals(1, instances.size());
            assertEquals("definition-project.usertask", instances.get(0).getProcessId());

            instances = client.findProcessInstancesByProcessName("usertask", null, 1, 1);
            assertNotNull(instances);
            assertEquals(1, instances.size());
            assertEquals("definition-project.usertask", instances.get(0).getProcessId());

            // search for completed only
            instances = client.findProcessInstancesByProcessName("usertask", Collections.singletonList(2), 0, 10);
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
            List<ProcessInstance> instances = client.findProcessInstancesByStatus(Collections.singletonList(1), 0, 10);
            assertNotNull(instances);
            assertEquals(5, instances.size());

            instances = client.findProcessInstancesByStatus(Collections.singletonList(1), 0, 3);
            assertNotNull(instances);
            assertEquals(3, instances.size());


            instances = client.findProcessInstancesByStatus(Collections.singletonList(1), 1, 3);
            assertNotNull(instances);
            assertEquals(2, instances.size());

            // search for completed only
            instances = client.findProcessInstancesByProcessId("definition-project.usertask", Collections.singletonList(2), 0, 10);
            assertNotNull(instances);
            assertEquals(0, instances.size());

        } finally {
            abortProcessInstances(processInstanceIds);
        }

    }

    @Test
    public void testGetProcessInstancesByInitiator() throws Exception {
        assertSuccess(client.createContainer("definition-project", new KieContainerResource("definition-project", releaseId)));

        int offset = client.findProcessInstancesByInitiator("yoda", Collections.singletonList(2), 0, 10).size();

        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("stringData", "waiting for signal");
        parameters.put("personData", createPersonInstance("john"));

        List<Long> processInstanceIds = createProcessInstances(parameters);

        try {
            List<ProcessInstance> instances = client.findProcessInstancesByInitiator("yoda", null, 0, 10);
            assertNotNull(instances);
            assertEquals(5, instances.size());

            instances = client.findProcessInstancesByInitiator("yoda", null, 0, 3);
            assertNotNull(instances);
            assertEquals(3, instances.size());


            instances = client.findProcessInstancesByInitiator("yoda", null, 1, 3);
            assertNotNull(instances);
            assertEquals(2, instances.size());

            // search for completed only
            instances = client.findProcessInstancesByInitiator("yoda", Collections.singletonList(2), 0, 10);
            assertNotNull(instances);
            assertEquals(0 + offset, instances.size());

        } finally {
            abortProcessInstances(processInstanceIds);
        }

    }

    @Test
    public void testGetProcessInstanceById() throws Exception {
        assertSuccess(client.createContainer("definition-project", new KieContainerResource("definition-project", releaseId)));

        Map<String, Object> parameters = new HashMap<String, Object>();
        Long processInstanceId = client.startProcess("definition-project", "definition-project.evaluation", parameters);
        try {
            ProcessInstance instance = client.findProcessInstanceById(processInstanceId);
            assertNotNull(instance);
            assertEquals(processInstanceId, instance.getId());
            assertEquals("definition-project.evaluation", instance.getProcessId());
            assertEquals("evaluation", instance.getProcessName());
            assertEquals("1.0", instance.getProcessVersion());
            assertEquals("yoda", instance.getInitiator());
            assertEquals("definition-project", instance.getContainerId());
            assertEquals("", instance.getCorrelationKey());
            assertEquals("evaluation", instance.getProcessInstanceDescription());
            assertEquals(-1, instance.getParentId().longValue());
        } finally {
            client.abortProcessInstance("definition-project", processInstanceId);
        }
    }

    @Test(expected = KieServicesException.class)
    public void testGetProcessInstanceByNonExistingId() throws Exception {
        assertSuccess(client.createContainer("definition-project", new KieContainerResource("definition-project", releaseId)));

        client.findProcessInstanceById(-9999l);

    }

    @Test
    public void testGetProcessInstanceByCorrelationKey() throws Exception {
        assertSuccess(client.createContainer("definition-project", new KieContainerResource("definition-project", releaseId)));

        CorrelationKeyFactory correlationKeyFactory = KieInternalServices.Factory.get().newCorrelationKeyFactory();

        CorrelationKey key = correlationKeyFactory.newCorrelationKey("simple-key");

        Map<String, Object> parameters = new HashMap<String, Object>();
        Long processInstanceId = client.startProcess("definition-project", "definition-project.evaluation", key, parameters);
        try {
            ProcessInstance instance = client.findProcessInstanceById(processInstanceId);
            assertNotNull(instance);
            assertEquals(processInstanceId, instance.getId());
            assertEquals("definition-project.evaluation", instance.getProcessId());
            assertEquals("evaluation", instance.getProcessName());
            assertEquals("1.0", instance.getProcessVersion());
            assertEquals("yoda", instance.getInitiator());
            assertEquals("definition-project", instance.getContainerId());
            assertEquals("simple-key", instance.getCorrelationKey());
            assertEquals("evaluation", instance.getProcessInstanceDescription());
            assertEquals(-1, instance.getParentId().longValue());

            instance = client.findProcessInstanceByCorrelationKey(key);
            assertNotNull(instance);
            assertEquals(processInstanceId, instance.getId());
            assertEquals("definition-project.evaluation", instance.getProcessId());
            assertEquals("evaluation", instance.getProcessName());
            assertEquals("1.0", instance.getProcessVersion());
            assertEquals("yoda", instance.getInitiator());
            assertEquals("definition-project", instance.getContainerId());
            assertEquals("simple-key", instance.getCorrelationKey());
            assertEquals("evaluation", instance.getProcessInstanceDescription());
            assertEquals(-1, instance.getParentId().longValue());
        } finally {
            client.abortProcessInstance("definition-project", processInstanceId);
        }
    }

    @Test
    public void testGetNodeInstances() throws Exception {
        assertSuccess(client.createContainer("definition-project", new KieContainerResource("definition-project", releaseId)));

        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("stringData", "waiting for signal");
        parameters.put("personData", createPersonInstance("john"));

        Long processInstanceId = client.startProcess("definition-project", "definition-project.usertask", parameters);

        try {
            List<NodeInstance> instances = client.findActiveNodeInstances(processInstanceId, 0, 10);
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

            nodeInstance = client.findNodeInstanceByWorkItemId(processInstanceId, nodeInstance.getWorkItemId());
            assertNotNull(nodeInstance);
            assertEquals("First task", nodeInstance.getName());
            assertEquals("definition-project", nodeInstance.getContainerId());
            assertEquals("HumanTaskNode", nodeInstance.getNodeType());
            assertEquals(false, nodeInstance.getCompleted());
            assertEquals(processInstanceId, nodeInstance.getProcessInstanceId());
            assertNotNull(nodeInstance.getWorkItemId());
            assertNotNull(nodeInstance.getDate());


            instances = client.findCompletedNodeInstances(processInstanceId, 0, 10);
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

            instances = client.findNodeInstances(processInstanceId, 0, 10);
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
            client.abortProcessInstance("definition-project", processInstanceId);
        }

    }

    @Test
    public void testGetVariableInstance() throws Exception {
        assertSuccess(client.createContainer("definition-project", new KieContainerResource("definition-project", releaseId)));

        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("stringData", "waiting for signal");
        parameters.put("personData", createPersonInstance("john"));

        Long processInstanceId = client.startProcess("definition-project", "definition-project.usertask", parameters);

        try {

            List<VariableInstance> currentState = client.findVariablesCurrentState(processInstanceId);
            assertNotNull(currentState);
            assertEquals(2, currentState.size());

            VariableInstance variableInstance = currentState.get(0);
            assertNotNull(variableInstance);
            assertEquals(processInstanceId, variableInstance.getProcessInstanceId());
            assertEquals("", variableInstance.getOldValue());
            assertEquals("Person{name='john'}", variableInstance.getValue());
            assertEquals("personData", variableInstance.getVariableName());

            variableInstance = currentState.get(1);
            assertNotNull(variableInstance);
            assertEquals(processInstanceId, variableInstance.getProcessInstanceId());
            assertEquals("", variableInstance.getOldValue());
            assertEquals("waiting for signal", variableInstance.getValue());
            assertEquals("stringData", variableInstance.getVariableName());

            List<VariableInstance> varHistory = client.findVariableHistory(processInstanceId, "stringData", 0, 10);
            assertNotNull(varHistory);
            assertEquals(1, varHistory.size());

            variableInstance = varHistory.get(0);
            assertNotNull(variableInstance);
            assertEquals(processInstanceId, variableInstance.getProcessInstanceId());
            assertEquals("", variableInstance.getOldValue());
            assertEquals("waiting for signal", variableInstance.getValue());
            assertEquals("stringData", variableInstance.getVariableName());

            client.setProcessVariable("definition-project", processInstanceId, "stringData", "updated value");

            currentState = client.findVariablesCurrentState(processInstanceId);
            assertNotNull(currentState);
            assertEquals(2, currentState.size());

            variableInstance = currentState.get(0);
            assertNotNull(variableInstance);
            assertEquals(processInstanceId, variableInstance.getProcessInstanceId());
            assertEquals("", variableInstance.getOldValue());
            assertEquals("Person{name='john'}", variableInstance.getValue());
            assertEquals("personData", variableInstance.getVariableName());

            variableInstance = currentState.get(1);
            assertNotNull(variableInstance);
            assertEquals(processInstanceId, variableInstance.getProcessInstanceId());
            assertEquals("waiting for signal", variableInstance.getOldValue());
            assertEquals("updated value", variableInstance.getValue());
            assertEquals("stringData", variableInstance.getVariableName());


            varHistory = client.findVariableHistory(processInstanceId, "stringData", 0, 10);
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
            assertEquals("", variableInstance.getOldValue());
            assertEquals("waiting for signal", variableInstance.getValue());
            assertEquals("stringData", variableInstance.getVariableName());

        } finally {
            client.abortProcessInstance("definition-project", processInstanceId);
        }

    }

    @Test
    public void testFindTasks() throws Exception {
        assertSuccess(client.createContainer("definition-project", new KieContainerResource("definition-project", releaseId)));

        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("stringData", "waiting for signal");
        parameters.put("personData", createPersonInstance("john"));

        Long processInstanceId = client.startProcess("definition-project", "definition-project.usertask", parameters);

        try {

            List<TaskSummary> tasks = client.findTasks("yoda", 0, 10);
            assertNotNull(tasks);

            TaskSummary taskInstance = tasks.get(0);
            assertNotNull(taskInstance);
            assertEquals("First task", taskInstance.getName());
            assertEquals("", taskInstance.getDescription());
            assertEquals("Reserved", taskInstance.getStatus());
            assertEquals(0, taskInstance.getPriority().intValue());
            assertEquals("yoda", taskInstance.getActualOwner());
            assertEquals("yoda", taskInstance.getCreatedBy());
            assertEquals("definition-project.usertask", taskInstance.getProcessId());
            assertEquals("definition-project", taskInstance.getContainerId());
            assertEquals(-1, taskInstance.getParentId().longValue());
            assertEquals(processInstanceId, taskInstance.getProcessInstanceId());

            TaskInstance taskById = client.findTaskById(taskInstance.getId());
            assertNotNull(taskById);
            assertEquals("First task", taskById.getName());
            assertEquals("", taskById.getDescription());
            assertEquals("Reserved", taskById.getStatus());
            assertEquals(0, taskById.getPriority().intValue());
            assertEquals("yoda", taskById.getActualOwner());
            assertEquals("yoda", taskById.getCreatedBy());
            assertEquals("definition-project.usertask", taskById.getProcessId());
            assertEquals("definition-project", taskById.getContainerId());
            assertEquals(processInstanceId, taskById.getProcessInstanceId());

            List<WorkItemInstance> workItems = client.getWorkItemByProcessInstance("definition-project", processInstanceId);
            assertNotNull(workItems);
            assertEquals(1, workItems.size());

            taskById = client.findTaskByWorkItemId(workItems.get(0).getId());
            assertNotNull(taskById);
            assertEquals("First task", taskById.getName());
            assertEquals("", taskById.getDescription());
            assertEquals("Reserved", taskById.getStatus());
            assertEquals(0, taskById.getPriority().intValue());
            assertEquals("yoda", taskById.getActualOwner());
            assertEquals("yoda", taskById.getCreatedBy());
            assertEquals("definition-project.usertask", taskById.getProcessId());
            assertEquals("definition-project", taskById.getContainerId());
            assertEquals(processInstanceId, taskById.getProcessInstanceId());


        } finally {
            client.abortProcessInstance("definition-project", processInstanceId);
        }
    }

    @Test
    public void testFindTaskEvents() throws Exception {
        assertSuccess(client.createContainer("definition-project", new KieContainerResource("definition-project", releaseId)));

        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("stringData", "waiting for signal");
        parameters.put("personData", createPersonInstance("john"));

        Long processInstanceId = client.startProcess("definition-project", "definition-project.usertask", parameters);

        try {

            List<TaskSummary> tasks = client.findTasks("yoda", 0, 10);
            assertNotNull(tasks);

            TaskSummary taskInstance = tasks.get(0);

            List<TaskEventInstance> events = client.findTaskEvents(taskInstance.getId(), 0, 10);
            assertNotNull(events);
            assertEquals(1, events.size());

            TaskEventInstance event = events.get(0);
            assertNotNull(event);
            assertEquals(TaskEvent.TaskEventType.ADDED.toString(), event.getType());
            assertEquals(processInstanceId, event.getProcessInstanceId());
            assertEquals(taskInstance.getId(), event.getTaskId());
            assertEquals("definition-project.usertask", event.getUserId());   // is this really correct to set process id as user for added task

            // now let's start it
            client.startTask("definition-project", taskInstance.getId(), "yoda");
            events = client.findTaskEvents(taskInstance.getId(), 0, 10);
            assertNotNull(events);
            assertEquals(2, events.size());

            event = events.get(0);
            assertNotNull(event);
            assertEquals(TaskEvent.TaskEventType.ADDED.toString(), event.getType());
            assertEquals(processInstanceId, event.getProcessInstanceId());
            assertEquals(taskInstance.getId(), event.getTaskId());
            assertEquals("definition-project.usertask", event.getUserId());    // is this really correct to set process id as user for added task

            event = events.get(1);
            assertNotNull(event);
            assertEquals(TaskEvent.TaskEventType.STARTED.toString(), event.getType());
            assertEquals(processInstanceId, event.getProcessInstanceId());
            assertEquals(taskInstance.getId(), event.getTaskId());
            assertEquals("yoda", event.getUserId());

            // now let's stop it
            client.stopTask("definition-project", taskInstance.getId(), "yoda");

            events = client.findTaskEvents(taskInstance.getId(), 0, 10);
            assertNotNull(events);
            assertEquals(3, events.size());

            event = events.get(0);
            assertNotNull(event);
            assertEquals(TaskEvent.TaskEventType.ADDED.toString(), event.getType());
            assertEquals(processInstanceId, event.getProcessInstanceId());
            assertEquals(taskInstance.getId(), event.getTaskId());
            assertEquals("definition-project.usertask", event.getUserId());    // is this really correct to set process id as user for added task

            event = events.get(1);
            assertNotNull(event);
            assertEquals(TaskEvent.TaskEventType.STARTED.toString(), event.getType());
            assertEquals(processInstanceId, event.getProcessInstanceId());
            assertEquals(taskInstance.getId(), event.getTaskId());
            assertEquals("yoda", event.getUserId());

            event = events.get(2);
            assertNotNull(event);
            assertEquals(TaskEvent.TaskEventType.STOPPED.toString(), event.getType());
            assertEquals(processInstanceId, event.getProcessInstanceId());
            assertEquals(taskInstance.getId(), event.getTaskId());
            assertEquals("yoda", event.getUserId());

        } finally {
            client.abortProcessInstance("definition-project", processInstanceId);
        }
    }

    @Test
    public void testFindTasksOwned() throws Exception {
        assertSuccess(client.createContainer("definition-project", new KieContainerResource("definition-project", releaseId)));

        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("stringData", "waiting for signal");
        parameters.put("personData", createPersonInstance("john"));

        Long processInstanceId = client.startProcess("definition-project", "definition-project.usertask", parameters);

        try {

            List<TaskSummary> tasks = client.findTasksOwned("yoda", 0, 10);
            assertNotNull(tasks);
            assertEquals(1, tasks.size());

            TaskSummary taskInstance = tasks.get(0);
            assertNotNull(taskInstance);
            assertEquals("First task", taskInstance.getName());
            assertEquals("", taskInstance.getDescription());
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

            tasks = client.findTasksOwned("yoda", status, 0, 10);
            assertNotNull(tasks);
            assertEquals(0, tasks.size());

            client.startTask("definition-project", taskInstance.getId(), "yoda");
            tasks = client.findTasksOwned("yoda", status, 0, 10);
            assertNotNull(tasks);
            assertEquals(1, tasks.size());

            taskInstance = tasks.get(0);
            assertNotNull(taskInstance);
            assertEquals("First task", taskInstance.getName());
            assertEquals("", taskInstance.getDescription());
            assertEquals("InProgress", taskInstance.getStatus());
            assertEquals(0, taskInstance.getPriority().intValue());
            assertEquals("yoda", taskInstance.getActualOwner());
            assertEquals("yoda", taskInstance.getCreatedBy());
            assertEquals("definition-project.usertask", taskInstance.getProcessId());
            assertEquals("definition-project", taskInstance.getContainerId());
            assertEquals(-1, taskInstance.getParentId().longValue());
            assertEquals(processInstanceId, taskInstance.getProcessInstanceId());

        } finally {
            client.abortProcessInstance("definition-project", processInstanceId);
        }
    }

    @Test
    public void testFindTasksAssignedAsPotentialOwner() throws Exception {
        assertSuccess(client.createContainer("definition-project", new KieContainerResource("definition-project", releaseId)));

        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("stringData", "waiting for signal");
        parameters.put("personData", createPersonInstance("john"));

        Long processInstanceId = client.startProcess("definition-project", "definition-project.usertask", parameters);

        try {

            List<TaskSummary> tasks = client.findTasksAssignedAsPotentialOwner("yoda", 0, 10);
            assertNotNull(tasks);
            assertEquals(1, tasks.size());

            TaskSummary taskInstance = tasks.get(0);
            assertNotNull(taskInstance);
            assertEquals("First task", taskInstance.getName());
            assertEquals("", taskInstance.getDescription());
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

            tasks = client.findTasksAssignedAsPotentialOwner("yoda", status, 0, 10);
            assertNotNull(tasks);
            assertEquals(0, tasks.size());

            client.startTask("definition-project", taskInstance.getId(), "yoda");
            tasks = client.findTasksAssignedAsPotentialOwner("yoda", status, 0, 10);
            assertNotNull(tasks);
            assertEquals(1, tasks.size());

            taskInstance = tasks.get(0);
            assertNotNull(taskInstance);
            assertEquals("First task", taskInstance.getName());
            assertEquals("", taskInstance.getDescription());
            assertEquals("InProgress", taskInstance.getStatus());
            assertEquals(0, taskInstance.getPriority().intValue());
            assertEquals("yoda", taskInstance.getActualOwner());
            assertEquals("yoda", taskInstance.getCreatedBy());
            assertEquals("definition-project.usertask", taskInstance.getProcessId());
            assertEquals("definition-project", taskInstance.getContainerId());
            assertEquals(-1, taskInstance.getParentId().longValue());
            assertEquals(processInstanceId, taskInstance.getProcessInstanceId());

        } finally {
            client.abortProcessInstance("definition-project", processInstanceId);
        }
    }

    @Test
    public void testFindTasksByStatusByProcessInstanceId() throws Exception {
        assertSuccess(client.createContainer("definition-project", new KieContainerResource("definition-project", releaseId)));

        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("stringData", "waiting for signal");
        parameters.put("personData", createPersonInstance("john"));

        Long processInstanceId = client.startProcess("definition-project", "definition-project.usertask", parameters);

        try {
            List<String> status = new ArrayList<String>();
            status.add(Status.Reserved.toString());
            status.add(Status.InProgress.toString());

            List<TaskSummary> tasks = client.findTasksByStatusByProcessInstanceId(processInstanceId, status, 0, 10);
            assertNotNull(tasks);
            assertEquals(1, tasks.size());

            TaskSummary taskInstance = tasks.get(0);
            assertNotNull(taskInstance);
            assertEquals("First task", taskInstance.getName());
            assertEquals("", taskInstance.getDescription());
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

            tasks = client.findTasksByStatusByProcessInstanceId(processInstanceId, status, 0, 10);
            assertNotNull(tasks);
            assertEquals(0, tasks.size());

            client.startTask("definition-project", taskInstance.getId(), "yoda");
            tasks = client.findTasksByStatusByProcessInstanceId(processInstanceId, status, 0, 10);
            assertNotNull(tasks);
            assertEquals(1, tasks.size());

            taskInstance = tasks.get(0);
            assertNotNull(taskInstance);
            assertEquals("First task", taskInstance.getName());
            assertEquals("", taskInstance.getDescription());
            assertEquals("InProgress", taskInstance.getStatus());
            assertEquals(0, taskInstance.getPriority().intValue());
            assertEquals("yoda", taskInstance.getActualOwner());
            assertEquals("yoda", taskInstance.getCreatedBy());
            assertEquals("definition-project.usertask", taskInstance.getProcessId());
            assertEquals("definition-project", taskInstance.getContainerId());
            assertEquals(-1, taskInstance.getParentId().longValue());
            assertEquals(processInstanceId, taskInstance.getProcessInstanceId());

        } finally {
            client.abortProcessInstance("definition-project", processInstanceId);
        }
    }

    protected List<Long> createProcessInstances(Map<String, Object> parameters) {
        List<Long> processInstanceIds = new ArrayList<Long>();

        processInstanceIds.add(client.startProcess("definition-project", "definition-project.signalprocess", parameters));
        processInstanceIds.add(client.startProcess("definition-project", "definition-project.usertask", parameters));
        processInstanceIds.add(client.startProcess("definition-project", "definition-project.signalprocess", parameters));
        processInstanceIds.add(client.startProcess("definition-project", "definition-project.usertask", parameters));
        processInstanceIds.add(client.startProcess("definition-project", "definition-project.signalprocess", parameters));

        return processInstanceIds;
    }

    protected void abortProcessInstances(List<Long> processInstanceIds) {
        for (Long piId : processInstanceIds) {
            client.abortProcessInstance("definition-project", piId);
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
