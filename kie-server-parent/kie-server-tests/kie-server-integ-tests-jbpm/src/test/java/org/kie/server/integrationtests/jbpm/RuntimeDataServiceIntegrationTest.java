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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.assertj.core.api.Assertions;
import org.jbpm.workflow.core.WorkflowProcess;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.kie.api.KieServices;
import org.kie.api.task.model.Status;
import org.kie.internal.KieInternalServices;
import org.kie.internal.process.CorrelationKey;
import org.kie.internal.process.CorrelationKeyFactory;
import org.kie.internal.task.api.model.TaskEvent;
import org.kie.server.api.exception.KieServicesException;
import org.kie.server.api.exception.KieServicesHttpException;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.definition.ProcessDefinition;
import org.kie.server.api.model.definition.SearchQueryFilterSpec;
import org.kie.server.api.model.instance.NodeInstance;
import org.kie.server.api.model.instance.ProcessInstance;
import org.kie.server.api.model.instance.ProcessInstanceCustomVars;
import org.kie.server.api.model.instance.ProcessInstanceUserTaskWithVariables;
import org.kie.server.api.model.instance.TaskEventInstance;
import org.kie.server.api.model.instance.TaskInstance;
import org.kie.server.api.model.instance.TaskSummary;
import org.kie.server.api.model.instance.VariableInstance;
import org.kie.server.api.model.instance.WorkItemInstance;
import org.kie.server.client.QueryServicesClient;
import org.kie.server.integrationtests.category.Smoke;
import org.kie.server.integrationtests.config.TestConfig;
import org.kie.server.integrationtests.shared.KieServerAssert;
import org.kie.server.integrationtests.shared.KieServerDeployer;

import static org.hamcrest.CoreMatchers.is;
import static org.jbpm.services.api.AdvanceRuntimeDataService.PROCESS_ATTR_DEFINITION_ID;
import static org.jbpm.services.api.AdvanceRuntimeDataService.PROCESS_ATTR_DEPLOYMENT_ID;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeFalse;
import static org.kie.server.api.util.QueryParamFactory.equalsTo;
import static org.kie.server.api.util.QueryParamFactory.history;
import static org.kie.server.api.util.QueryParamFactory.list;
import static org.kie.server.api.util.QueryParamFactory.onlyActiveTasks;
import static org.kie.server.api.util.QueryParamFactory.onlyCompletedTasks;



public class RuntimeDataServiceIntegrationTest extends JbpmKieServerBaseIntegrationTest {

    private static ReleaseId releaseId = new ReleaseId("org.kie.server.testing", "definition-project",
            "1.0.0.Final");

    protected static final String SORT_BY_PROCESS_ID = "ProcessId";
    protected static final String SORT_BY_INSTANCE_PROCESS_ID = "Id";
    protected static final String SORT_BY_TASK_STATUS = "Status";
    protected static final String SORT_BY_TASK_EVENTS_TYPE = "Type";

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
    public void testGetProcessDefinitions() throws Exception {
        List<ProcessDefinition> definitions = queryClient.findProcesses(0, 20);
        assertNotNull(definitions);

        assertEquals(20, definitions.size());
        List<String> processIds = collectDefinitions(definitions);
        checkProcessDefinitions(processIds);

        // test paging of the result
        List<ProcessDefinition> definitionsFirstPage = queryClient.findProcesses(0, 5);
        List<String> processIdsFirstPage = collectDefinitions(definitionsFirstPage);
        Assertions.assertThat(processIdsFirstPage).hasSize(5);
        Assertions.assertThat(processIds).containsAll(processIdsFirstPage);

        List<ProcessDefinition> definitionsSecondPage = queryClient.findProcesses(1, 5);
        List<String> processIdsSecondPage = collectDefinitions(definitionsSecondPage);
        Assertions.assertThat(processIdsSecondPage).hasSize(5);
        Assertions.assertThat(processIds).containsAll(processIdsSecondPage);
        Assertions.assertThat(processIdsSecondPage).doesNotContainAnyElementsOf(processIdsFirstPage);
    }

    @Test
    public void testGetProcessDefinitionsSorted() throws Exception {
        List<ProcessDefinition> definitions = queryClient.findProcesses(0, 20, QueryServicesClient.SORT_BY_NAME, false);
        assertNotNull(definitions);

        assertEquals(20, definitions.size());
        List<String> processIds = collectDefinitions(definitions);
        checkProcessDefinitions(processIds);

        // test paging of the result
        List<ProcessDefinition> definitionsAscOrder = queryClient.findProcesses(0, 3, QueryServicesClient.SORT_BY_NAME, true);
        List<String> processIdsAscOrder = collectDefinitions(definitionsAscOrder);
        Assertions.assertThat(processIdsAscOrder).hasSize(3);
        Assertions.assertThat(processIdsAscOrder).contains(PROCESS_ID_BOUNDARY_SIGNAL_PROCESS, PROCESS_ID_BOUNDARY_SIGNAL_EXPRESSION_PROCESS, PROCESS_ID_CALL_EVALUATION);

        List<ProcessDefinition> definitionsDescOrder = queryClient.findProcesses(0, 3, QueryServicesClient.SORT_BY_NAME, false);
        List<String> processIdsDescOrder = collectDefinitions(definitionsDescOrder);
        Assertions.assertThat(processIdsDescOrder).hasSize(3);
        Assertions.assertThat(processIdsDescOrder).contains(PROCESS_ID_XYZ_TRANSLATIONS, PROCESS_ID_USERTASK, PROCESS_ID_USERTASK_WITH_SLA_ON_TASK);
    }

    @Test
    public void testGetProcessDefinitionsWithFilter() throws Exception {
        List<ProcessDefinition> definitions = queryClient.findProcesses("evaluation", 0, 20);
        assertNotNull(definitions);

        assertEquals(2, definitions.size());
        List<String> processIds = collectDefinitions(definitions);
        assertTrue(processIds.contains(PROCESS_ID_CALL_EVALUATION));
        assertTrue(processIds.contains(PROCESS_ID_EVALUATION));

        // test paging of the result
        definitions = queryClient.findProcesses("evaluation", 0, 1);

        assertNotNull(definitions);
        assertEquals(1, definitions.size());
        processIds = collectDefinitions(definitions);
        assertTrue(processIds.contains(PROCESS_ID_CALL_EVALUATION));

        definitions = queryClient.findProcesses("evaluation", 1, 1);
        assertNotNull(definitions);

        assertEquals(1, definitions.size());
        processIds = collectDefinitions(definitions);
        assertTrue(processIds.contains(PROCESS_ID_EVALUATION));

    }

    @Test
    public void testGetProcessDefinitionsWithFilterSorted() throws Exception {
        List<ProcessDefinition> definitions = queryClient.findProcesses("evaluation", 0, 20, QueryServicesClient.SORT_BY_NAME, true);
        assertNotNull(definitions);

        Assertions.assertThat(definitions).hasSize(2);
        List<String> processIds = collectDefinitions(definitions);
        assertTrue(processIds.get(0).equals(PROCESS_ID_CALL_EVALUATION));
        assertTrue(processIds.get(1).equals(PROCESS_ID_EVALUATION));

        // test paging of the result
        definitions = queryClient.findProcesses("evaluation", 0, 20, QueryServicesClient.SORT_BY_NAME, false);

        assertNotNull(definitions);
        assertEquals(2, definitions.size());
        processIds = collectDefinitions(definitions);
        assertTrue(processIds.get(0).equals(PROCESS_ID_EVALUATION));
        assertTrue(processIds.get(1).equals(PROCESS_ID_CALL_EVALUATION));

    }

    @Test
    public void testGetProcessDefinitionsByContainer() throws Exception {
        List<ProcessDefinition> definitions = queryClient.findProcessesByContainerId(CONTAINER_ID, 0, 20);
        assertNotNull(definitions);

        Assertions.assertThat(definitions).hasSize(20);
        List<String> processIds = collectDefinitions(definitions);
        checkProcessDefinitions(processIds);

        // test paging of the result
        List<ProcessDefinition> definitionsFirstPage = queryClient.findProcessesByContainerId(CONTAINER_ID, 0, 5);
        List<String> processIdsFirstPage = collectDefinitions(definitionsFirstPage);
        Assertions.assertThat(processIdsFirstPage).hasSize(5);
        Assertions.assertThat(processIds).containsAll(processIdsFirstPage);

        List<ProcessDefinition> definitionsSecondPage = queryClient.findProcessesByContainerId(CONTAINER_ID, 1, 5);
        List<String> processIdsSecondPage = collectDefinitions(definitionsSecondPage);
        Assertions.assertThat(processIdsSecondPage).hasSize(5);
        Assertions.assertThat(processIds).containsAll(processIdsSecondPage);
        Assertions.assertThat(processIdsSecondPage).doesNotContainAnyElementsOf(processIdsFirstPage);

        // last check if there are process def for not existing project
        List<ProcessDefinition> definitionsNotExistingProject = queryClient.findProcessesByContainerId("not-existing-project", 0, 10);
        Assertions.assertThat(definitionsNotExistingProject).hasSize(0);
    }

    @Test
    public void testGetProcessDefinitionsByContainerSorted() throws Exception {
        List<ProcessDefinition> definitions = queryClient.findProcessesByContainerId(CONTAINER_ID, 0, 20, QueryServicesClient.SORT_BY_NAME, true);
        assertNotNull(definitions);

        Assertions.assertThat(definitions).hasSize(20);
        List<String> processIds = collectDefinitions(definitions);
        checkProcessDefinitions(processIds);

        // test paging of the result
        List<ProcessDefinition> definitionsAscOrder = queryClient.findProcessesByContainerId(CONTAINER_ID, 0, 3, QueryServicesClient.SORT_BY_NAME, true);
        List<String> processIdsAscOrder = collectDefinitions(definitionsAscOrder);
        Assertions.assertThat(processIdsAscOrder).hasSize(3);
        Assertions.assertThat(processIdsAscOrder).contains(PROCESS_ID_BOUNDARY_SIGNAL_PROCESS, PROCESS_ID_BOUNDARY_SIGNAL_EXPRESSION_PROCESS, PROCESS_ID_CALL_EVALUATION);

        List<ProcessDefinition> definitionsDescOrder = queryClient.findProcessesByContainerId(CONTAINER_ID, 0, 3, QueryServicesClient.SORT_BY_NAME, false);
        List<String> processIdsDescOrder = collectDefinitions(definitionsDescOrder);
        Assertions.assertThat(processIdsDescOrder).hasSize(3);
        Assertions.assertThat(processIdsDescOrder).contains(PROCESS_ID_XYZ_TRANSLATIONS, PROCESS_ID_USERTASK, PROCESS_ID_USERTASK_WITH_SLA_ON_TASK);

    }

    @Test
    public void testGetProcessDefinitionsById() throws Exception {
        List<ProcessDefinition> definitions = queryClient.findProcessesById(PROCESS_ID_USERTASK);
        assertNotNull(definitions);

        Assertions.assertThat(definitions).hasSize(1);
        List<String> processIds = collectDefinitions(definitions);
        assertTrue(processIds.contains(PROCESS_ID_USERTASK));


         // last check if there are process def for not existing project
        definitions = queryClient.findProcessesById("not-existing-project");
        assertNotNull(definitions);

        assertEquals(0, definitions.size());

    }

    @Test
    public void testGetProcessDefinitionByContainerAndId() throws Exception {
        ProcessDefinition definition = queryClient.findProcessByContainerIdProcessId(CONTAINER_ID, PROCESS_ID_USERTASK);
        assertNotNull(definition);
        assertEquals(PROCESS_ID_USERTASK, definition.getId());
        assertEquals("usertask", definition.getName());
        assertEquals("1.0", definition.getVersion());
        assertEquals("org.jbpm", definition.getPackageName());
        assertEquals(CONTAINER_ID, definition.getContainerId());

    }

    @Test
    public void testGetProcessDefinitionByContainerAndNonExistingId() throws Exception {
        try {
            queryClient.findProcessByContainerIdProcessId(CONTAINER_ID, "non-existing");
            fail("KieServicesException should be thrown complaining about process definition not found.");

        } catch (KieServicesException e) {
            KieServerAssert.assertResultContainsString(e.getMessage(), "Could not find process definition \"non-existing\" in container \"definition-project\"");
        }
    }

    @Test
    public void testGetProcessInstances() throws Exception {
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("stringData", "waiting for signal");
        parameters.put("personData", createPersonInstance(USER_JOHN));

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
    public void testGetProcessInstancesSortedByName() throws Exception {
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("stringData", "waiting for signal");
        parameters.put("personData", createPersonInstance(USER_JOHN));

        List<Long> processInstanceIds = createProcessInstances(parameters);

        try {
            List<ProcessInstance> instances = queryClient.findProcessInstances(0, 3, SORT_BY_PROCESS_ID, true);
            assertNotNull(instances);
            assertEquals(3, instances.size());
            for (ProcessInstance instance : instances) {
                assertTrue(processInstanceIds.contains(instance.getId()));
                assertEquals(PROCESS_ID_SIGNAL_PROCESS, instance.getProcessId());
            }

            instances = queryClient.findProcessInstances(1, 3, SORT_BY_PROCESS_ID, true);
            assertNotNull(instances);
            assertEquals(2, instances.size());
            for (ProcessInstance instance : instances) {
                assertTrue(processInstanceIds.contains(instance.getId()));
                assertEquals(PROCESS_ID_USERTASK, instance.getProcessId());
            }

            instances = queryClient.findProcessInstances(0, 10, SORT_BY_PROCESS_ID, false);
            assertNotNull(instances);
            assertEquals(5, instances.size());
            for (int i = 0; i < instances.size(); i++) {
                if (i < 2) {
                    assertEquals(PROCESS_ID_USERTASK, instances.get(i).getProcessId());
                } else {
                    assertEquals(PROCESS_ID_SIGNAL_PROCESS, instances.get(i).getProcessId());
                }
            }
        } finally {
            abortProcessInstances(processInstanceIds);
        }
    }

    @Test
    public void testGetProcessInstancesByContainer() throws Exception {
        int offset = queryClient.findProcessInstancesByContainerId(CONTAINER_ID, Collections.singletonList(2), 0, 10).size();

        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("stringData", "waiting for signal");
        parameters.put("personData", createPersonInstance(USER_JOHN));

        List<Long> processInstanceIds = createProcessInstances(parameters);

        try {
            List<ProcessInstance> instances = queryClient.findProcessInstancesByContainerId(CONTAINER_ID, null, 0, 10);
            assertNotNull(instances);
            assertEquals(5, instances.size());

            List<Long> found = collectInstances(instances);
            assertEquals(processInstanceIds, found);

            instances = queryClient.findProcessInstancesByContainerId(CONTAINER_ID, null, 0, 3);
            assertNotNull(instances);
            assertEquals(3, instances.size());

            instances = queryClient.findProcessInstancesByContainerId(CONTAINER_ID, null, 1, 3);
            assertNotNull(instances);
            assertEquals(2, instances.size());

            // search for completed only
            instances = queryClient.findProcessInstancesByContainerId(CONTAINER_ID, Collections.singletonList(2), 0, 10);
            assertNotNull(instances);
            assertEquals(0 + offset, instances.size());

        } finally {
            abortProcessInstances(processInstanceIds);
        }

    }

    @Test
    public void testGetProcessInstancesByContainerSortedByName() throws Exception {
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("stringData", "waiting for signal");
        parameters.put("personData", createPersonInstance(USER_JOHN));

        List<Long> processInstanceIds = createProcessInstances(parameters);

        try {
            List<ProcessInstance> instances = queryClient.findProcessInstancesByContainerId(CONTAINER_ID, null, 0, 3, SORT_BY_PROCESS_ID, true);
            assertNotNull(instances);
            assertEquals(3, instances.size());
            for (ProcessInstance instance : instances) {
                assertTrue(processInstanceIds.contains(instance.getId()));
                assertEquals(PROCESS_ID_SIGNAL_PROCESS, instance.getProcessId());
            }

            instances = queryClient.findProcessInstancesByContainerId(CONTAINER_ID, null, 1, 3, SORT_BY_PROCESS_ID, true);
            assertNotNull(instances);
            assertEquals(2, instances.size());
            for (ProcessInstance instance : instances) {
                assertTrue(processInstanceIds.contains(instance.getId()));
                assertEquals(PROCESS_ID_USERTASK, instance.getProcessId());
            }

            instances = queryClient.findProcessInstancesByContainerId(CONTAINER_ID, null, 0, 10, SORT_BY_PROCESS_ID, false);
            assertNotNull(instances);
            assertEquals(5, instances.size());
            for (int i = 0; i < instances.size(); i++) {
                if (i < 2) {
                    assertEquals(PROCESS_ID_USERTASK, instances.get(i).getProcessId());
                } else {
                    assertEquals(PROCESS_ID_SIGNAL_PROCESS, instances.get(i).getProcessId());
                }
            }
        } finally {
            abortProcessInstances(processInstanceIds);
        }
    }

    @Test
    public void testGetProcessInstancesByProcessId() throws Exception {
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("stringData", "waiting for signal");
        parameters.put("personData", createPersonInstance(USER_JOHN));

        List<Long> processInstanceIds = createProcessInstances(parameters);

        try {
            List<ProcessInstance> instances = queryClient.findProcessInstancesByProcessId(PROCESS_ID_USERTASK, null, 0, 10);
            assertNotNull(instances);
            assertEquals(2, instances.size());
            assertEquals(PROCESS_ID_USERTASK, instances.get(0).getProcessId());
            assertEquals(PROCESS_ID_USERTASK, instances.get(1).getProcessId());

            instances = queryClient.findProcessInstancesByProcessId(PROCESS_ID_USERTASK, null, 0, 1);
            assertNotNull(instances);
            assertEquals(1, instances.size());
            assertEquals(PROCESS_ID_USERTASK, instances.get(0).getProcessId());

            instances = queryClient.findProcessInstancesByProcessId(PROCESS_ID_USERTASK, null, 1, 1);
            assertNotNull(instances);
            assertEquals(1, instances.size());
            assertEquals(PROCESS_ID_USERTASK, instances.get(0).getProcessId());
        } finally {
            abortProcessInstances(processInstanceIds);
        }

    }

    @Test
    public void testGetProcessInstancesByProcessIdAndStatus() throws Exception {
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("stringData", "waiting for signal");
        parameters.put("personData", createPersonInstance(USER_JOHN));

        List<Integer> abortedStatus = Collections.singletonList(org.kie.api.runtime.process.ProcessInstance.STATE_ABORTED);
        List<Integer> activeStatus = Collections.singletonList(org.kie.api.runtime.process.ProcessInstance.STATE_ACTIVE);

        List<ProcessInstance> instances = queryClient.findProcessInstancesByProcessId(PROCESS_ID_USERTASK, abortedStatus, 0, 10000);
        assertNotNull(instances);
        int originalAborted = instances.size();

        instances = queryClient.findProcessInstancesByProcessId(PROCESS_ID_USERTASK, activeStatus, 0, 10000);
        assertNotNull(instances);
        assertEquals(0, instances.size());

        Long processInstanceId = processClient.startProcess(CONTAINER_ID, PROCESS_ID_USERTASK, parameters);

        instances = queryClient.findProcessInstancesByProcessId(PROCESS_ID_USERTASK, activeStatus, 0, 10000);
        assertNotNull(instances);
        assertEquals(1, instances.size());
        assertEquals(PROCESS_ID_USERTASK, instances.get(0).getProcessId());

        processClient.abortProcessInstance(CONTAINER_ID, processInstanceId);

        instances = queryClient.findProcessInstancesByProcessId(PROCESS_ID_USERTASK, abortedStatus, 0, 10000);
        assertNotNull(instances);
        assertEquals(originalAborted + 1, instances.size());
    }
    
    @Test
    public void testGetProcessInstancesByProcessIdAndStatusAndInitiator() throws Exception {
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("stringData", "waiting for signal");
        parameters.put("personData", createPersonInstance(USER_JOHN));

        List<Integer> abortedStatus = Collections.singletonList(org.kie.api.runtime.process.ProcessInstance.STATE_ABORTED);
        List<Integer> activeStatus = Collections.singletonList(org.kie.api.runtime.process.ProcessInstance.STATE_ACTIVE);

        List<ProcessInstance> instances = queryClient.findProcessInstancesByProcessIdAndInitiator(PROCESS_ID_USERTASK, USER_YODA, abortedStatus, 0, 10000);
        assertNotNull(instances);
        int originalAborted = instances.size();

        instances = queryClient.findProcessInstancesByProcessIdAndInitiator(PROCESS_ID_USERTASK, USER_YODA, activeStatus, 0, 10000);
        assertNotNull(instances);
        assertEquals(0, instances.size());

        Long processInstanceId = processClient.startProcess(CONTAINER_ID, PROCESS_ID_USERTASK, parameters);

        instances = queryClient.findProcessInstancesByProcessIdAndInitiator(PROCESS_ID_USERTASK, USER_YODA, activeStatus, 0, 10000);
        assertNotNull(instances);
        assertEquals(1, instances.size());
        assertEquals(PROCESS_ID_USERTASK, instances.get(0).getProcessId());

        processClient.abortProcessInstance(CONTAINER_ID, processInstanceId);

        instances = queryClient.findProcessInstancesByProcessIdAndInitiator(PROCESS_ID_USERTASK, USER_YODA, abortedStatus, 0, 10000);
        assertNotNull(instances);
        assertEquals(originalAborted + 1, instances.size());
        
        instances = queryClient.findProcessInstancesByProcessIdAndInitiator(PROCESS_ID_USERTASK, USER_JOHN, abortedStatus, 0, 10000);
        assertNotNull(instances);
        assertEquals(0, instances.size());
    }

    @Test
    public void testGetProcessInstancesByProcessIdSortedByInstanceId() throws Exception {
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("stringData", "waiting for signal");
        parameters.put("personData", createPersonInstance(USER_JOHN));

        List<Long> processInstanceIds = createProcessInstances(parameters);

        try {
            List<ProcessInstance> instances = queryClient.findProcessInstancesByProcessId(PROCESS_ID_USERTASK, null, 0, 10, SORT_BY_INSTANCE_PROCESS_ID, false);
            assertNotNull(instances);
            assertEquals(2, instances.size());
            assertEquals(PROCESS_ID_USERTASK, instances.get(0).getProcessId());
            assertEquals(PROCESS_ID_USERTASK, instances.get(1).getProcessId());
            assertTrue(instances.get(0).getId() > instances.get(1).getId());

        } finally {
            abortProcessInstances(processInstanceIds);
        }
    }

    @Test
    public void testGetProcessInstancesByProcessName() throws Exception {
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("stringData", "waiting for signal");
        parameters.put("personData", createPersonInstance(USER_JOHN));

        List<Long> processInstanceIds = createProcessInstances(parameters);

        try {
            List<ProcessInstance> instances = queryClient.findProcessInstancesByProcessName("usertask", null, 0, 10);
            assertNotNull(instances);
            assertEquals(2, instances.size());
            assertEquals(PROCESS_ID_USERTASK, instances.get(0).getProcessId());
            assertEquals(PROCESS_ID_USERTASK, instances.get(1).getProcessId());

            instances = queryClient.findProcessInstancesByProcessName("usertask", null, 0, 1);
            assertNotNull(instances);
            assertEquals(1, instances.size());
            assertEquals(PROCESS_ID_USERTASK, instances.get(0).getProcessId());

            instances = queryClient.findProcessInstancesByProcessName("usertask", null, 1, 1);
            assertNotNull(instances);
            assertEquals(1, instances.size());
            assertEquals(PROCESS_ID_USERTASK, instances.get(0).getProcessId());

        } finally {
            abortProcessInstances(processInstanceIds);
        }
    }

    @Test
    public void testGetProcessInstancesByProcessNameAndStatus() throws Exception {
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("stringData", "waiting for signal");
        parameters.put("personData", createPersonInstance(USER_JOHN));

        List<Integer> abortedStatus = Collections.singletonList(org.kie.api.runtime.process.ProcessInstance.STATE_ABORTED);
        List<Integer> activeStatus = Collections.singletonList(org.kie.api.runtime.process.ProcessInstance.STATE_ACTIVE);

        List<ProcessInstance> instances = queryClient.findProcessInstancesByProcessName("usertask", abortedStatus, 0, 10000);
        assertNotNull(instances);
        int originalAborted = instances.size();

        instances = queryClient.findProcessInstancesByProcessName("usertask", activeStatus, 0, 10000);
        assertNotNull(instances);
        assertEquals(0, instances.size());

        Long processInstanceId = processClient.startProcess(CONTAINER_ID, PROCESS_ID_USERTASK, parameters);

        instances = queryClient.findProcessInstancesByProcessName("usertask", activeStatus, 0, 10000);
        assertNotNull(instances);
        assertEquals(1, instances.size());
        assertEquals(PROCESS_ID_USERTASK, instances.get(0).getProcessId());

        processClient.abortProcessInstance(CONTAINER_ID, processInstanceId);

        instances = queryClient.findProcessInstancesByProcessName("usertask", abortedStatus, 0, 10000);
        assertNotNull(instances);
        assertEquals(originalAborted + 1, instances.size());
    }

    @Test
    public void testGetProcessInstancesByProcessNameSortedByInstanceId() throws Exception {
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("stringData", "waiting for signal");
        parameters.put("personData", createPersonInstance(USER_JOHN));

        List<Long> processInstanceIds = createProcessInstances(parameters);

        try {
            List<ProcessInstance> instances = queryClient.findProcessInstancesByProcessName("usertask", null, 0, 10, SORT_BY_INSTANCE_PROCESS_ID, false);
            assertNotNull(instances);
            assertEquals(2, instances.size());
            assertEquals(PROCESS_ID_USERTASK, instances.get(0).getProcessId());
            assertEquals(PROCESS_ID_USERTASK, instances.get(1).getProcessId());
            assertTrue(instances.get(0).getId() > instances.get(1).getId());

        } finally {
            abortProcessInstances(processInstanceIds);
        }
    }

    @Test
    public void testGetProcessInstancesByStatus() throws Exception {
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("stringData", "waiting for signal");
        parameters.put("personData", createPersonInstance(USER_JOHN));

        List<Integer> abortedStatus = Collections.singletonList(org.kie.api.runtime.process.ProcessInstance.STATE_ABORTED);
        List<Integer> activeStatus = Collections.singletonList(org.kie.api.runtime.process.ProcessInstance.STATE_ACTIVE);

        List<ProcessInstance> instances = queryClient.findProcessInstancesByStatus(abortedStatus, 0, 10000);
        assertNotNull(instances);
        int originalAborted = instances.size();

        instances = queryClient.findProcessInstancesByStatus(activeStatus, 0, 10000);
        assertNotNull(instances);
        assertEquals(0, instances.size());

        Long processInstanceId = processClient.startProcess(CONTAINER_ID, PROCESS_ID_USERTASK, parameters);

        instances = queryClient.findProcessInstancesByStatus(activeStatus, 0, 10000);
        assertNotNull(instances);
        assertEquals(1, instances.size());

        processClient.abortProcessInstance(CONTAINER_ID, processInstanceId);

        instances = queryClient.findProcessInstancesByStatus(abortedStatus, 0, 10000);
        assertNotNull(instances);
        assertEquals(originalAborted + 1, instances.size());
    }

    @Test
    public void testGetProcessInstancesByStatusPaging() throws Exception {
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("stringData", "waiting for signal");
        parameters.put("personData", createPersonInstance(USER_JOHN));

        List<Long> processInstanceIds = createProcessInstances(parameters);

        try {
            List<Integer> activeStatus = Collections.singletonList(org.kie.api.runtime.process.ProcessInstance.STATE_ACTIVE);

            List<ProcessInstance> instances = queryClient.findProcessInstancesByStatus(activeStatus, 0, 10);
            assertNotNull(instances);
            assertEquals(5, instances.size());

            instances = queryClient.findProcessInstancesByStatus(activeStatus, 0, 3);
            assertNotNull(instances);
            assertEquals(3, instances.size());

            instances = queryClient.findProcessInstancesByStatus(activeStatus, 1, 3);
            assertNotNull(instances);
            assertEquals(2, instances.size());

        } finally {
            abortProcessInstances(processInstanceIds);
        }

    }

    @Test
    public void testGetProcessInstancesByStatusSortedByName() throws Exception {
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("stringData", "waiting for signal");
        parameters.put("personData", createPersonInstance(USER_JOHN));

        List<Long> processInstanceIds = createProcessInstances(parameters);

        try {
            List<ProcessInstance> instances = queryClient.findProcessInstancesByStatus(Collections.singletonList(1), 0, 3, SORT_BY_PROCESS_ID, true);
            assertNotNull(instances);
            assertEquals(3, instances.size());
            for (ProcessInstance instance : instances) {
                assertTrue(processInstanceIds.contains(instance.getId()));
                assertEquals(PROCESS_ID_SIGNAL_PROCESS, instance.getProcessId());
            }

            instances = queryClient.findProcessInstancesByStatus(Collections.singletonList(1), 1, 3, SORT_BY_PROCESS_ID, true);
            assertNotNull(instances);
            assertEquals(2, instances.size());
            for (ProcessInstance instance : instances) {
                assertTrue(processInstanceIds.contains(instance.getId()));
                assertEquals(PROCESS_ID_USERTASK, instance.getProcessId());
            }

            instances = queryClient.findProcessInstancesByStatus(Collections.singletonList(1), 0, 10, SORT_BY_PROCESS_ID, false);
            assertNotNull(instances);
            assertEquals(5, instances.size());
            for (int i = 0; i < instances.size(); i++) {
                if (i < 2) {
                    assertEquals(PROCESS_ID_USERTASK, instances.get(i).getProcessId());
                } else {
                    assertEquals(PROCESS_ID_SIGNAL_PROCESS, instances.get(i).getProcessId());
                }
            }
        } finally {
            abortProcessInstances(processInstanceIds);
        }
    }

    @Test
    public void testGetProcessInstancesByInitiator() throws Exception {
        int offset = queryClient.findProcessInstancesByInitiator(USER_YODA, Collections.singletonList(2), 0, 10).size();

        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("stringData", "waiting for signal");
        parameters.put("personData", createPersonInstance(USER_JOHN));

        List<Long> processInstanceIds = createProcessInstances(parameters);

        try {
            List<ProcessInstance> instances = queryClient.findProcessInstancesByInitiator(USER_YODA, null, 0, 10);
            assertNotNull(instances);
            assertEquals(5, instances.size());

            instances = queryClient.findProcessInstancesByInitiator(USER_YODA, null, 0, 3);
            assertNotNull(instances);
            assertEquals(3, instances.size());


            instances = queryClient.findProcessInstancesByInitiator(USER_YODA, null, 1, 3);
            assertNotNull(instances);
            assertEquals(2, instances.size());

            // search for completed only
            instances = queryClient.findProcessInstancesByInitiator(USER_YODA, Collections.singletonList(2), 0, 10);
            assertNotNull(instances);
            assertEquals(0 + offset, instances.size());

        } finally {
            abortProcessInstances(processInstanceIds);
        }

    }

    @Test
    public void testGetProcessInstancesByInitiatorSortedByName() throws Exception {
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("stringData", "waiting for signal");
        parameters.put("personData", createPersonInstance(USER_JOHN));

        List<Long> processInstanceIds = createProcessInstances(parameters);

        try {
            List<ProcessInstance> instances = queryClient.findProcessInstancesByInitiator(USER_YODA, null, 0, 3, SORT_BY_PROCESS_ID, true);
            assertNotNull(instances);
            assertEquals(3, instances.size());
            for (ProcessInstance instance : instances) {
                assertTrue(processInstanceIds.contains(instance.getId()));
                assertEquals(PROCESS_ID_SIGNAL_PROCESS, instance.getProcessId());
            }

            instances = queryClient.findProcessInstancesByInitiator(USER_YODA, null, 1, 3, SORT_BY_PROCESS_ID, true);
            assertNotNull(instances);
            assertEquals(2, instances.size());
            for (ProcessInstance instance : instances) {
                assertTrue(processInstanceIds.contains(instance.getId()));
                assertEquals(PROCESS_ID_USERTASK, instance.getProcessId());
            }

            instances = queryClient.findProcessInstancesByInitiator(USER_YODA, null, 0, 10, SORT_BY_PROCESS_ID, false);
            assertNotNull(instances);
            assertEquals(5, instances.size());
            for (int i = 0; i < instances.size(); i++) {
                if (i < 2) {
                    assertEquals(PROCESS_ID_USERTASK, instances.get(i).getProcessId());
                } else {
                    assertEquals(PROCESS_ID_SIGNAL_PROCESS, instances.get(i).getProcessId());
                }
            }
        } finally {
            abortProcessInstances(processInstanceIds);
        }
    }

    @Test
    @Category(Smoke.class)
    public void testGetProcessInstanceById() throws Exception {
        Map<String, Object> parameters = new HashMap<String, Object>();
        Long processInstanceId = processClient.startProcess(CONTAINER_ID, PROCESS_ID_EVALUATION, parameters);
        try {
            ProcessInstance instance = queryClient.findProcessInstanceById(processInstanceId);
            assertNotNull(instance);
            assertEquals(processInstanceId, instance.getId());
            assertEquals(PROCESS_ID_EVALUATION, instance.getProcessId());
            assertEquals("evaluation", instance.getProcessName());
            assertEquals("1.0", instance.getProcessVersion());
            assertEquals(USER_YODA, instance.getInitiator());
            assertEquals(CONTAINER_ID, instance.getContainerId());
            assertEquals(processInstanceId.toString(), instance.getCorrelationKey());
            assertEquals("evaluation", instance.getProcessInstanceDescription());
            assertEquals(-1, instance.getParentId().longValue());
        } finally {
            processClient.abortProcessInstance(CONTAINER_ID, processInstanceId);
        }
    }

    @Test
    public void testGetProcessInstanceWithVariables() throws Exception {
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("stringData", "waiting for signal");
        Object person = createPersonInstance(USER_JOHN);
        parameters.put("personData", person);

        Long processInstanceId = processClient.startProcess(CONTAINER_ID, PROCESS_ID_SIGNAL_PROCESS, parameters);
        assertNotNull(processInstanceId);
        assertTrue(processInstanceId.longValue() > 0);

        try {
            ProcessInstance processInstance = queryClient.findProcessInstanceById(processInstanceId, true);
            assertNotNull(processInstance);
            assertEquals(processInstanceId, processInstance.getId());
            assertEquals(org.kie.api.runtime.process.ProcessInstance.STATE_ACTIVE, processInstance.getState().intValue());
            assertEquals(PROCESS_ID_SIGNAL_PROCESS, processInstance.getProcessId());
            assertEquals("signalprocess", processInstance.getProcessName());
            assertEquals("1.0", processInstance.getProcessVersion());
            assertEquals(CONTAINER_ID, processInstance.getContainerId());
            assertEquals("signalprocess", processInstance.getProcessInstanceDescription());
            assertEquals(TestConfig.getUsername(), processInstance.getInitiator());
            assertEquals(-1l, processInstance.getParentId().longValue());
            assertNotNull(processInstance.getCorrelationKey());
            assertNotNull(processInstance.getDate());

            Map<String, Object> variables = processInstance.getVariables();
            assertNotNull(variables);
            assertEquals(4, variables.size());

            assertTrue(variables.containsKey("stringData"));
            assertTrue(variables.containsKey("personData"));
            assertTrue(variables.containsKey("initiator"));
            assertTrue(variables.containsKey("nullAccepted"));

            String stringVar = (String) variables.get("stringData");
            Object personVar = variables.get("personData");
            String initiator = (String) variables.get("initiator");

            assertNotNull(personVar);
            assertEquals(person.toString(), personVar);

            assertNotNull(stringVar);
            assertEquals("waiting for signal", stringVar);

            assertNotNull(initiator);
            assertEquals(TestConfig.getUsername(), initiator);
        } finally {
            processClient.abortProcessInstance(CONTAINER_ID, processInstanceId);
        }
    }

    @Test
    public void testGetProcessInstanceByNonExistingId() throws Exception {
        try {
            queryClient.findProcessInstanceById(-9999l);
            fail("KieServicesException should be thrown complaining about process instance not found.");

        } catch (KieServicesException e) {
            KieServerAssert.assertResultContainsString(e.getMessage(), "Could not find process instance with id");
        }
    }

    @Test
    public void testGetProcessInstanceByCorrelationKey() throws Exception {
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
    public void testGetProcessInstancesByCorrelationKeySortedById() throws Exception {
        CorrelationKeyFactory correlationKeyFactory = KieInternalServices.Factory.get().newCorrelationKeyFactory();

        String firstBusinessKey = "my-simple-key-first";
        String secondBusinessKey = "my-simple-key-second";
        CorrelationKey firstKey = correlationKeyFactory.newCorrelationKey(firstBusinessKey);
        CorrelationKey secondKey = correlationKeyFactory.newCorrelationKey(secondBusinessKey);
        CorrelationKey partKey = correlationKeyFactory.newCorrelationKey("my-simple%");

        Long processInstanceEvalutionId = processClient.startProcess(CONTAINER_ID, PROCESS_ID_EVALUATION, firstKey);
        Long processInstanceSignalId = processClient.startProcess(CONTAINER_ID, PROCESS_ID_SIGNAL_PROCESS, secondKey);
        try {
            List<ProcessInstance> returnedProcessInstances = queryClient.findProcessInstancesByCorrelationKey(partKey, 0, 100, SORT_BY_INSTANCE_PROCESS_ID, false);
            assertNotNull(returnedProcessInstances);
            assertProcessInstancesOrderById(returnedProcessInstances, false);

            ProcessInstance returnedSignalProcess = findProcessInstance(returnedProcessInstances, processInstanceSignalId);
            assertEquals(PROCESS_ID_SIGNAL_PROCESS, returnedSignalProcess.getProcessId());
            assertEquals(processInstanceSignalId, returnedSignalProcess.getId());
            assertEquals(secondBusinessKey, returnedSignalProcess.getCorrelationKey());
            ProcessInstance returnedEvaluation = findProcessInstance(returnedProcessInstances, processInstanceEvalutionId);
            assertEquals(PROCESS_ID_EVALUATION, returnedEvaluation.getProcessId());
            assertEquals(processInstanceEvalutionId, returnedEvaluation.getId());
            assertEquals(firstBusinessKey, returnedEvaluation.getCorrelationKey());
        } finally {
            processClient.abortProcessInstance(CONTAINER_ID, processInstanceEvalutionId);
            processClient.abortProcessInstance(CONTAINER_ID, processInstanceSignalId);
        }
    }

    @Test
    public void testGetProcessInstanceByCorrelationKeyPaging() throws Exception {
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
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("stringData", "waiting for signal");
        parameters.put("personData", createPersonInstance(USER_JOHN));

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
    public void testGetProcessInstancesByVariableNameSortedByName() throws Exception {
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("stringData", "waiting for signal");
        parameters.put("personData", createPersonInstance(USER_JOHN));

        List<Long> processInstanceIds = createProcessInstances(parameters);

        try {
            List<ProcessInstance> instances = queryClient.findProcessInstancesByVariable("stringData", null, 0, 3, SORT_BY_PROCESS_ID, true);
            assertNotNull(instances);
            assertEquals(3, instances.size());
            for (ProcessInstance instance : instances) {
                assertTrue(processInstanceIds.contains(instance.getId()));
                assertEquals(PROCESS_ID_SIGNAL_PROCESS, instance.getProcessId());
            }

            instances = queryClient.findProcessInstancesByVariable("stringData", null, 1, 3, SORT_BY_PROCESS_ID, true);
            assertNotNull(instances);
            assertEquals(2, instances.size());
            for (ProcessInstance instance : instances) {
                assertTrue(processInstanceIds.contains(instance.getId()));
                assertEquals(PROCESS_ID_USERTASK, instance.getProcessId());
            }

            instances = queryClient.findProcessInstancesByVariable("stringData", null, 0, 10, SORT_BY_PROCESS_ID, false);
            assertNotNull(instances);
            assertEquals(5, instances.size());
            for (int i = 0; i < instances.size(); i++) {
                if (i < 2) {
                    assertEquals(PROCESS_ID_USERTASK, instances.get(i).getProcessId());
                } else {
                    assertEquals(PROCESS_ID_SIGNAL_PROCESS, instances.get(i).getProcessId());
                }
            }
        } finally {
            abortProcessInstances(processInstanceIds);
        }
    }

    @Test
    public void testGetProcessInstancesByVariableNameAndValue() throws Exception {
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("personData", createPersonInstance(USER_JOHN));

        List<Long> processInstanceIds = createProcessInstances(parameters);

        for (Long processInstanceId : processInstanceIds) {
            processClient.setProcessVariable(CONTAINER_ID, processInstanceId, "stringData", "waiting for signal");
        }

        try {
            List<ProcessInstance> instances = queryClient.findProcessInstancesByVariableAndValue("stringData", "waiting%", null, 0, 50);
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

            processClient.setProcessVariable(CONTAINER_ID, processInstanceIds.get(0), "stringData", "updated value");

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
    public void testGetProcessInstancesByVariableNameAndValueSortedByName() throws Exception {
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("personData", createPersonInstance(USER_JOHN));

        List<Long> processInstanceIds = createProcessInstances(parameters);

        for (Long processInstanceId : processInstanceIds) {
            processClient.setProcessVariable(CONTAINER_ID, processInstanceId, "stringData", "waiting for signal");
        }

        try {
            List status = Arrays.asList(org.kie.api.runtime.process.ProcessInstance.STATE_ACTIVE);
            List<ProcessInstance> instances = queryClient.findProcessInstancesByVariableAndValue("stringData", "waiting%", status, 0, 3, SORT_BY_PROCESS_ID, true);
            assertNotNull(instances);
            assertEquals(3, instances.size());
            for (ProcessInstance instance : instances) {
                assertTrue(processInstanceIds.contains(instance.getId()));
                assertEquals(PROCESS_ID_SIGNAL_PROCESS, instance.getProcessId());
            }

            instances = queryClient.findProcessInstancesByVariableAndValue("stringData", "waiting%", status, 1, 3, SORT_BY_PROCESS_ID, true);
            assertNotNull(instances);
            assertEquals(2, instances.size());
            for (ProcessInstance instance : instances) {
                assertTrue(processInstanceIds.contains(instance.getId()));
                assertEquals(PROCESS_ID_USERTASK, instance.getProcessId());
            }

            instances = queryClient.findProcessInstancesByVariableAndValue("stringData", "waiting%", status, 0, 10, SORT_BY_PROCESS_ID, false);
            assertNotNull(instances);
            assertEquals(5, instances.size());
            for (int i = 0; i < instances.size(); i++) {
                if (i < 2) {
                    assertEquals(PROCESS_ID_USERTASK, instances.get(i).getProcessId());
                } else {
                    assertEquals(PROCESS_ID_SIGNAL_PROCESS, instances.get(i).getProcessId());
                }
            }
        } finally {
            abortProcessInstances(processInstanceIds);
        }
    }

    @Test
    public void testGetNodeInstances() throws Exception {
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("stringData", "waiting for signal");
        parameters.put("personData", createPersonInstance(USER_JOHN));

        Long processInstanceId = processClient.startProcess(CONTAINER_ID, PROCESS_ID_USERTASK, parameters);

        try {
            List<NodeInstance> instances = queryClient.findActiveNodeInstances(processInstanceId, 0, 10);
            assertNotNull(instances);
            assertEquals(1, instances.size());

            NodeInstance expectedFirstTask = NodeInstance
                    .builder()
                    .name("First task")
                    .containerId(CONTAINER_ID)
                    .nodeType("HumanTaskNode")
                    .completed(false)
                    .processInstanceId(processInstanceId)
                    .build();

            NodeInstance nodeInstance = instances.get(0);
            assertNodeInstance(expectedFirstTask, nodeInstance);
            assertNotNull(nodeInstance.getWorkItemId());
            assertNotNull(nodeInstance.getDate());

            nodeInstance = queryClient.findNodeInstanceByWorkItemId(processInstanceId, nodeInstance.getWorkItemId());
            assertNodeInstance(expectedFirstTask, nodeInstance);
            assertNotNull(nodeInstance.getWorkItemId());
            assertNotNull(nodeInstance.getDate());

            instances = queryClient.findCompletedNodeInstances(processInstanceId, 0, 10);
            assertNotNull(instances);
            assertEquals(1, instances.size());

            NodeInstance expectedStart = NodeInstance
                    .builder()
                    .name("start")
                    .containerId(CONTAINER_ID)
                    .nodeType("StartNode")
                    .completed(true)
                    .processInstanceId(processInstanceId)
                    .build();

            nodeInstance = instances.get(0);
            assertNodeInstance(expectedStart, nodeInstance);
            assertNull(nodeInstance.getWorkItemId());
            assertNotNull(nodeInstance.getDate());

            instances = queryClient.findNodeInstances(processInstanceId, 0, 10);
            assertNotNull(instances);
            assertEquals(3, instances.size());

            nodeInstance = instances.get(0);
            assertNodeInstance(expectedFirstTask, nodeInstance);
            assertNotNull(nodeInstance.getWorkItemId());
            assertNotNull(nodeInstance.getDate());

            nodeInstance = instances.get(1);
            assertNodeInstance(expectedStart, nodeInstance);
            assertNull(nodeInstance.getWorkItemId());
            assertNotNull(nodeInstance.getDate());

            nodeInstance = instances.get(2);
            expectedStart.setCompleted(false);
            assertNodeInstance(expectedStart, nodeInstance);
            assertNull(nodeInstance.getWorkItemId());
            assertNotNull(nodeInstance.getDate());
        } finally {
            processClient.abortProcessInstance(CONTAINER_ID, processInstanceId);
        }
    }

    @Test
    public void testGetVariableInstance() throws Exception {
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("stringData", "waiting for signal");
        parameters.put("personData", createPersonInstance(USER_JOHN));

        Long processInstanceId = processClient.startProcess(CONTAINER_ID, PROCESS_ID_USERTASK, parameters);

        try {

            List<VariableInstance> currentState = queryClient.findVariablesCurrentState(processInstanceId);
            assertNotNull(currentState);
            assertEquals(3, currentState.size());

            for (VariableInstance variableInstance : currentState) {

                if ("personData".equals(variableInstance.getVariableName())) {
                    assertNotNull(variableInstance);
                    assertEquals(processInstanceId, variableInstance.getProcessInstanceId());
                    KieServerAssert.assertNullOrEmpty(variableInstance.getOldValue());
                    assertEquals("Person{name='john'}", variableInstance.getValue());
                    assertEquals("personData", variableInstance.getVariableName());
                } else if ("stringData".equals(variableInstance.getVariableName())) {
                    assertNotNull(variableInstance);
                    assertEquals(processInstanceId, variableInstance.getProcessInstanceId());
                    KieServerAssert.assertNullOrEmpty(variableInstance.getOldValue());
                    assertEquals("waiting for signal", variableInstance.getValue());
                    assertEquals("stringData", variableInstance.getVariableName());
                } else if("initiator".equals(variableInstance.getVariableName())){
                    assertNotNull(variableInstance);
                    assertEquals(processInstanceId, variableInstance.getProcessInstanceId());
                    assertEquals(TestConfig.getUsername(), variableInstance.getValue());
                    KieServerAssert.assertNullOrEmpty(variableInstance.getOldValue());
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
            KieServerAssert.assertNullOrEmpty(variableInstance.getOldValue());
            assertEquals("waiting for signal", variableInstance.getValue());
            assertEquals("stringData", variableInstance.getVariableName());

            processClient.setProcessVariable(CONTAINER_ID, processInstanceId, "stringData", "updated value");

            currentState = queryClient.findVariablesCurrentState(processInstanceId);
            assertNotNull(currentState);
            assertEquals(3, currentState.size());

            for (VariableInstance variable : currentState) {
                if ("personData".equals(variable.getVariableName())) {
                    assertNotNull(variable);
                    assertEquals(processInstanceId, variable.getProcessInstanceId());
                    KieServerAssert.assertNullOrEmpty(variable.getOldValue());
                    assertEquals("Person{name='john'}", variable.getValue());
                    assertEquals("personData", variable.getVariableName());
                } else if ("stringData".equals(variable.getVariableName())) {
                    assertNotNull(variable);
                    assertEquals(processInstanceId, variable.getProcessInstanceId());
                    assertEquals("waiting for signal", variable.getOldValue());
                    assertEquals("updated value", variable.getValue());
                    assertEquals("stringData", variable.getVariableName());
                } else if("initiator".equals(variable.getVariableName())){
                    assertNotNull(variable);
                    assertEquals(processInstanceId, variable.getProcessInstanceId());
                    assertEquals(TestConfig.getUsername(), variable.getValue());
                    KieServerAssert.assertNullOrEmpty(variable.getOldValue());
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
            KieServerAssert.assertNullOrEmpty(variableInstance.getOldValue());
            assertEquals("waiting for signal", variableInstance.getValue());
            assertEquals("stringData", variableInstance.getVariableName());

        } finally {
            processClient.abortProcessInstance(CONTAINER_ID, processInstanceId);
        }

    }

    @Test (expected = KieServicesException.class)
    public void testNotExistingUserTaskFindByWorkItemId() throws Exception {
        taskClient.findTaskByWorkItemId(-9999l);
    }

    @Test (expected = KieServicesException.class)
    public void testNotExistingUserTaskFindById() throws Exception {
        taskClient.findTaskById(-9999l);
    }

    @Test
    public void testFindTasks() throws Exception {
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("stringData", "waiting for signal");
        parameters.put("personData", createPersonInstance(USER_JOHN));

        Long processInstanceId = processClient.startProcess(CONTAINER_ID, PROCESS_ID_USERTASK, parameters);
        ProcessInstance desc = processClient.getProcessInstance(CONTAINER_ID, processInstanceId);

        try {

            List<TaskSummary> tasks = taskClient.findTasks(USER_YODA, 0, 50);
            assertNotNull(tasks);

            TaskSummary taskSummary = null;
            for (TaskSummary t : tasks) {
                if (t.getProcessInstanceId().equals(processInstanceId)) {
                    taskSummary = t;
                    break;
                }
            }

            TaskSummary expectedTaskSummary = createDefaultTaskSummary(processInstanceId);

            assertTaskSummary(expectedTaskSummary, taskSummary);

            List<WorkItemInstance> workItems = processClient.getWorkItemByProcessInstance(CONTAINER_ID, processInstanceId);
            assertNotNull(workItems);
            assertEquals(1, workItems.size());
            Long workItemId = workItems.get(0).getId();

            TaskInstance expectedTaskInstance = TaskInstance
                    .builder()
                    .name("First task")
                    .status(Status.Reserved.toString())
                    .priority(0)
                    .actualOwner(USER_YODA)
                    .createdBy(USER_YODA)
                    .processId(PROCESS_ID_USERTASK)
                    .containerId(CONTAINER_ID)
                    .processInstanceId(processInstanceId)
                    .processType(WorkflowProcess.PROCESS_TYPE)
                    .correlationKey(desc.getCorrelationKey())
                    .build();

            TaskInstance taskById = taskClient.findTaskById(taskSummary.getId());
            assertTaskInstance(expectedTaskInstance, taskById);
            assertEquals(workItemId, taskById.getWorkItemId());
            assertNull(taskById.getSlaCompliance());

            taskById = taskClient.findTaskById(taskSummary.getId(), true);
            assertTaskInstance(expectedTaskInstance, taskById);
            assertTrue(taskById.getSlaCompliance()==0);
            assertEquals(workItemId, taskById.getWorkItemId());


            taskById = taskClient.findTaskByWorkItemId(workItems.get(0).getId());
            assertTaskInstance(expectedTaskInstance, taskById);
        } finally {
            processClient.abortProcessInstance(CONTAINER_ID, processInstanceId);
        }
    }

    @Test
    public void testFindTasksSortedByProcessInstanceId() throws Exception {
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("stringData", "waiting for signal");
        parameters.put("personData", createPersonInstance(USER_JOHN));

        Long processInstanceId = processClient.startProcess(CONTAINER_ID, PROCESS_ID_USERTASK, parameters);
        Long processInstanceId2 = processClient.startProcess(CONTAINER_ID, PROCESS_ID_USERTASK, parameters);

        try {
            List<TaskSummary> tasks = taskClient.findTasks(USER_YODA, 0, 50, "processInstanceId", false);
            assertNotNull(tasks);

            List<TaskSummary> reservedTasks = tasks.stream()
                                                    .filter(n -> Status.Reserved.toString().equals(n.getStatus()))
                                                    .collect(Collectors.toList());

            TaskSummary firstTask = reservedTasks.get(0);
            TaskSummary secondTask = reservedTasks.get(1);
            Assertions.assertThat(firstTask.getProcessInstanceId())
                .as("First reserved task should have bigger process instance id as results should be sorted in descending order.")
                .isGreaterThan(secondTask.getProcessInstanceId());

            //latest task is from second process
            TaskSummary expectedTaskSummary;
            if (processInstanceId2 > processInstanceId) {
                expectedTaskSummary = createDefaultTaskSummary(processInstanceId2);
            } else {
                expectedTaskSummary = createDefaultTaskSummary(processInstanceId);
            }
            assertTaskSummary(expectedTaskSummary, firstTask);

            expectedTaskSummary = null;
            if (processInstanceId2 > processInstanceId) {
                expectedTaskSummary = createDefaultTaskSummary(processInstanceId);
            } else {
                expectedTaskSummary = createDefaultTaskSummary(processInstanceId2);
            }
            assertTaskSummary(expectedTaskSummary, secondTask);
        } finally {
            processClient.abortProcessInstance(CONTAINER_ID, processInstanceId);
            processClient.abortProcessInstance(CONTAINER_ID, processInstanceId2);
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

            List<TaskEventInstance> events = taskClient.findTaskEvents(taskInstance.getId(), 0, 10);
            assertNotNull(events);
            assertEquals(2, events.size());

            TaskEventInstance expectedTaskEventInstance = TaskEventInstance
                    .builder()
                    .type(TaskEvent.TaskEventType.ADDED.toString())
                    .processInstanceId(processInstanceId)
                    .taskId(taskInstance.getId())
                    .user(PROCESS_ID_USERTASK)      // is this really correct to set process id as user for added task
                    .build();

            TaskEventInstance event = events.get(0);
            assertTaskEventInstance(expectedTaskEventInstance, event);
            //assertEquals(PROCESS_ID_USERTASK, event.getUserId());   // is this really correct to set process id as user for added task

            // now let's start it
            taskClient.startTask(CONTAINER_ID, taskInstance.getId(), USER_YODA);
            events = taskClient.findTaskEvents(taskInstance.getId(), 0, 10);
            assertNotNull(events);
            assertEquals(3, events.size());

            event = getTaskEventInstanceFromListByType(events, TaskEvent.TaskEventType.ADDED.toString());
            assertTaskEventInstance(expectedTaskEventInstance, event);

            event = getTaskEventInstanceFromListByType(events, TaskEvent.TaskEventType.STARTED.toString());
            expectedTaskEventInstance.setType(TaskEvent.TaskEventType.STARTED.toString());
            expectedTaskEventInstance.setUserId(USER_YODA);
            assertTaskEventInstance(expectedTaskEventInstance, event);

            // now let's stop it
            taskClient.stopTask(CONTAINER_ID, taskInstance.getId(), USER_YODA);

            events = taskClient.findTaskEvents(taskInstance.getId(), 0, 10);
            assertNotNull(events);
            assertEquals(4, events.size());

            event = getTaskEventInstanceFromListByType(events, TaskEvent.TaskEventType.ADDED.toString());
            expectedTaskEventInstance.setType(TaskEvent.TaskEventType.ADDED.toString());
            expectedTaskEventInstance.setUserId(PROCESS_ID_USERTASK);  // is this really correct to set process id as user for added task
            assertTaskEventInstance(expectedTaskEventInstance, event);

            event = getTaskEventInstanceFromListByType(events, TaskEvent.TaskEventType.STARTED.toString());
            expectedTaskEventInstance.setType(TaskEvent.TaskEventType.STARTED.toString());
            expectedTaskEventInstance.setUserId(USER_YODA);
            assertTaskEventInstance(expectedTaskEventInstance, event);

            event = getTaskEventInstanceFromListByType(events, TaskEvent.TaskEventType.STOPPED.toString());
            expectedTaskEventInstance.setType(TaskEvent.TaskEventType.STOPPED.toString());
            assertTaskEventInstance(expectedTaskEventInstance, event);
        } finally {
            processClient.abortProcessInstance(CONTAINER_ID, processInstanceId);
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
    public void testFindTaskEventsSortedByType() throws Exception {
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("stringData", "waiting for signal");
        parameters.put("personData", createPersonInstance(USER_JOHN));

        Long processInstanceId = processClient.startProcess(CONTAINER_ID, PROCESS_ID_USERTASK, parameters);

        try {

            List<TaskSummary> tasks = taskClient.findTasksByStatusByProcessInstanceId(processInstanceId, null, 0, 10);
            assertNotNull(tasks);
            assertEquals(1, tasks.size());

            TaskSummary taskInstance = tasks.get(0);

            // now let's start it
            taskClient.startTask(CONTAINER_ID, taskInstance.getId(), USER_YODA);

            // now let's stop it
            taskClient.stopTask(CONTAINER_ID, taskInstance.getId(), USER_YODA);

            // test paging of the result
            List<TaskEventInstance> events = taskClient.findTaskEvents(taskInstance.getId(), 0, 4, SORT_BY_TASK_EVENTS_TYPE, true);

            assertNotNull(events);
            assertEquals(4, events.size());

            TaskEventInstance event = events.get(0);
            assertNotNull(event);
            assertEquals(taskInstance.getId(), event.getTaskId());
            assertEquals(TaskEvent.TaskEventType.ACTIVATED.toString(), event.getType());

            event = events.get(1);
            assertNotNull(event);
            assertEquals(taskInstance.getId(), event.getTaskId());
            assertEquals(TaskEvent.TaskEventType.ADDED.toString(), event.getType());

            event = events.get(2);
            assertNotNull(event);
            assertEquals(taskInstance.getId(), event.getTaskId());
            assertEquals(TaskEvent.TaskEventType.STARTED.toString(), event.getType());

            event = events.get(3);
            assertNotNull(event);
            assertEquals(taskInstance.getId(), event.getTaskId());
            assertEquals(TaskEvent.TaskEventType.STOPPED.toString(), event.getType());

            try {
                events = taskClient.findTaskEvents(taskInstance.getId(), 2, 3, SORT_BY_TASK_EVENTS_TYPE, true);
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

            events = taskClient.findTaskEvents(taskInstance.getId(), 0, 10, SORT_BY_TASK_EVENTS_TYPE, false);
            assertNotNull(events);
            assertEquals(4, events.size());

            event = events.get(0);
            assertNotNull(event);
            assertEquals(taskInstance.getId(), event.getTaskId());
            assertEquals(TaskEvent.TaskEventType.STOPPED.toString(), event.getType());

            event = events.get(1);
            assertNotNull(event);
            assertEquals(taskInstance.getId(), event.getTaskId());
            assertEquals(TaskEvent.TaskEventType.STARTED.toString(), event.getType());

        } finally {
            processClient.abortProcessInstance(CONTAINER_ID, processInstanceId);
        }
    }

    @Test
    public void testFindTasksOwned() throws Exception {
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("stringData", "waiting for signal");
        parameters.put("personData", createPersonInstance(USER_JOHN));

        Long processInstanceId = processClient.startProcess(CONTAINER_ID, PROCESS_ID_USERTASK, parameters);

        try {

            List<TaskSummary> tasks = taskClient.findTasksOwned(USER_YODA, 0, 10);
            assertNotNull(tasks);
            assertEquals(1, tasks.size());

            TaskSummary expectedTaskSummary = createDefaultTaskSummary(processInstanceId);

            TaskSummary taskInstance = tasks.get(0);
            assertTaskSummary(expectedTaskSummary, taskInstance);

            List<String> status = new ArrayList<String>();
            status.add(Status.InProgress.toString());

            tasks = taskClient.findTasksOwned(USER_YODA, status, 0, 10);
            assertNotNull(tasks);
            assertEquals(0, tasks.size());

            taskClient.startTask(CONTAINER_ID, taskInstance.getId(), USER_YODA);
            tasks = taskClient.findTasksOwned(USER_YODA, status, 0, 10);
            assertNotNull(tasks);
            assertEquals(1, tasks.size());

            taskInstance = tasks.get(0);
            expectedTaskSummary.setStatus(Status.InProgress.toString());
            assertTaskSummary(expectedTaskSummary, taskInstance);
        } finally {
            processClient.abortProcessInstance(CONTAINER_ID, processInstanceId);
        }
    }

    @Test
    public void testFindTasksOwnedSortedByStatus() throws Exception {
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("stringData", "waiting for signal");
        parameters.put("personData", createPersonInstance(USER_JOHN));

        Long processInstanceId = processClient.startProcess(CONTAINER_ID, PROCESS_ID_USERTASK, parameters);
        Long processInstanceId2 = processClient.startProcess(CONTAINER_ID, PROCESS_ID_USERTASK, parameters);

        try {
            List<TaskSummary> tasks = taskClient.findTasksOwned(USER_YODA, 0, 10, SORT_BY_TASK_STATUS, true);
            assertNotNull(tasks);
            assertEquals(2, tasks.size());
            Long someTaskId = tasks.get(0).getId();

            taskClient.startTask(CONTAINER_ID, someTaskId, USER_YODA);
            tasks = taskClient.findTasksOwned(USER_YODA, 0, 10, SORT_BY_TASK_STATUS, true);
            assertNotNull(tasks);
            assertEquals(2, tasks.size());
            assertEquals(Status.InProgress.toString(), tasks.get(0).getStatus());
            assertEquals(Status.Reserved.toString(), tasks.get(1).getStatus());

            tasks = taskClient.findTasksOwned(USER_YODA, 0, 10, SORT_BY_TASK_STATUS, false);
            assertNotNull(tasks);
            assertEquals(2, tasks.size());
            assertEquals(Status.Reserved.toString(), tasks.get(0).getStatus());
            assertEquals(Status.InProgress.toString(), tasks.get(1).getStatus());

        } finally {
            processClient.abortProcessInstance(CONTAINER_ID, processInstanceId);
            processClient.abortProcessInstance(CONTAINER_ID, processInstanceId2);
        }
    }

    @Test
    public void testFindTasksAssignedAsPotentialOwner() throws Exception {
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("stringData", "waiting for signal");
        parameters.put("personData", createPersonInstance(USER_JOHN));

        Long processInstanceId = processClient.startProcess(CONTAINER_ID, PROCESS_ID_USERTASK, parameters);

        try {

            List<TaskSummary> tasks = taskClient.findTasksAssignedAsPotentialOwner(USER_YODA, 0, 10);
            assertNotNull(tasks);
            assertEquals(1, tasks.size());

            TaskSummary expectedTaskSummary = createDefaultTaskSummary(processInstanceId);

            TaskSummary taskInstance = tasks.get(0);
            assertTaskSummary(expectedTaskSummary, taskInstance);

            List<String> status = new ArrayList<String>();
            status.add(Status.InProgress.toString());

            tasks = taskClient.findTasksAssignedAsPotentialOwner(USER_YODA, status, 0, 10);
            assertNotNull(tasks);
            assertEquals(0, tasks.size());

            taskClient.startTask(CONTAINER_ID, taskInstance.getId(), USER_YODA);
            tasks = taskClient.findTasksAssignedAsPotentialOwner(USER_YODA, status, 0, 10);
            assertNotNull(tasks);
            assertEquals(1, tasks.size());

            taskInstance = tasks.get(0);
            expectedTaskSummary.setStatus(Status.InProgress.toString());
            assertTaskSummary(expectedTaskSummary, taskInstance);

        } finally {
            processClient.abortProcessInstance(CONTAINER_ID, processInstanceId);
        }
    }

    @Test
    public void testFindTasksAssignedAsPotentialOwnerSortedByStatus() throws Exception {
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("stringData", "waiting for signal");
        parameters.put("personData", createPersonInstance(USER_JOHN));

        Long processInstanceId = processClient.startProcess(CONTAINER_ID, PROCESS_ID_USERTASK, parameters);
        Long processInstanceId2 = processClient.startProcess(CONTAINER_ID, PROCESS_ID_USERTASK, parameters);

        try {
            List<TaskSummary> tasks = taskClient.findTasksAssignedAsPotentialOwner(USER_YODA, 0, 10, SORT_BY_TASK_STATUS, true);
            assertNotNull(tasks);
            assertEquals(2, tasks.size());
            Long someTaskId = tasks.get(0).getId();

            taskClient.startTask(CONTAINER_ID, someTaskId, USER_YODA);

            tasks = taskClient.findTasksAssignedAsPotentialOwner(USER_YODA, 0, 10, SORT_BY_TASK_STATUS, true);
            assertNotNull(tasks);
            assertEquals(2, tasks.size());
            assertEquals(Status.InProgress.toString(), tasks.get(0).getStatus());
            assertEquals(Status.Reserved.toString(), tasks.get(1).getStatus());

            tasks = taskClient.findTasksAssignedAsPotentialOwner(USER_YODA, 0, 10, SORT_BY_TASK_STATUS, false);
            assertNotNull(tasks);
            assertEquals(2, tasks.size());
            assertEquals(Status.Reserved.toString(), tasks.get(0).getStatus());
            assertEquals(Status.InProgress.toString(), tasks.get(1).getStatus());
        } finally {
            processClient.abortProcessInstance(CONTAINER_ID, processInstanceId);
            processClient.abortProcessInstance(CONTAINER_ID, processInstanceId2);
        }
    }

    @Test
    public void testFindTasksByStatusByProcessInstanceId() throws Exception {
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("stringData", "waiting for signal");
        parameters.put("personData", createPersonInstance(USER_JOHN));

        Long processInstanceId = processClient.startProcess(CONTAINER_ID, PROCESS_ID_USERTASK, parameters);

        try {
            List<String> status = new ArrayList<String>();
            status.add(Status.Reserved.toString());
            status.add(Status.InProgress.toString());

            List<TaskSummary> tasks = taskClient.findTasksByStatusByProcessInstanceId(processInstanceId, status, 0, 10);
            assertNotNull(tasks);
            assertEquals(1, tasks.size());

            TaskSummary expectedTaskSummary = createDefaultTaskSummary(processInstanceId);

            TaskSummary taskInstance = tasks.get(0);
            assertTaskSummary(expectedTaskSummary, taskInstance);

            status = new ArrayList<String>();
            status.add(Status.InProgress.toString());

            tasks = taskClient.findTasksByStatusByProcessInstanceId(processInstanceId, status, 0, 10);
            assertNotNull(tasks);
            assertEquals(0, tasks.size());

            taskClient.startTask(CONTAINER_ID, taskInstance.getId(), USER_YODA);
            tasks = taskClient.findTasksByStatusByProcessInstanceId(processInstanceId, status, 0, 10);
            assertNotNull(tasks);
            assertEquals(1, tasks.size());

            taskInstance = tasks.get(0);
            expectedTaskSummary.setStatus(Status.InProgress.toString());
            assertTaskSummary(expectedTaskSummary, taskInstance);

        } finally {
            processClient.abortProcessInstance(CONTAINER_ID, processInstanceId);
        }
    }

    @Test
    public void testFindTasksByStatusByProcessInstanceIdMissingParam() throws Exception {
        // only applicable for REST
        assumeFalse(configuration.isJms());
        try {
            taskClient.findTasksByStatusByProcessInstanceId(null, Arrays.asList("Ready"), 0, 10);
            fail("Method should throw missing InstanceId exception");
        } catch (IllegalArgumentException e) {
            assertEquals("Missing value for processInstanceId", e.getMessage());
        }

    }


    @Test
    public void testFindTasksWithNameFilter() {
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("stringData", "waiting for signal");
        parameters.put("personData", createPersonInstance(USER_JOHN));


        Long processInstanceId = processClient.startProcess(CONTAINER_ID, PROCESS_ID_USERTASK, parameters);

        try {

            List<TaskSummary> tasks = taskClient.findTasksByStatusByProcessInstanceId(processInstanceId, null, 0, 10);
            assertNotNull(tasks);
            assertEquals(1, tasks.size());

            tasks = taskClient.findTasksAssignedAsPotentialOwner(USER_YODA, "First%", null, 0, 10);
            assertNotNull(tasks);
            assertEquals(1, tasks.size());

            tasks = taskClient.findTasksAssignedAsPotentialOwner(USER_YODA, "First%", null, 0, 10, "Status", false);
            assertNotNull(tasks);
            assertEquals(1, tasks.size());

        } finally {
            processClient.abortProcessInstance(CONTAINER_ID, processInstanceId);
        }
    }

    @Test
    public void testFindTaskEventsForNotExistingTask() {
        String invalidId = "-9,999";
        try {
            taskClient.findTaskEvents(-9999l, 0, 10);
            fail("KieServicesException should be thrown when task not found");
        } catch (KieServicesException e) {
            if (configuration.isRest()) {
                KieServerAssert.assertResultContainsString(e.getMessage(), "Could not find task instance with id \"" + invalidId + "\"");
                KieServicesHttpException httpEx = (KieServicesHttpException) e;
                assertEquals(Integer.valueOf(404), httpEx.getHttpCode());
            } else {
                assertTrue(e.getMessage().contains("No task found with id " + invalidId));
            }
        }
    }

    @Test
    public void testQueryProcessesByVariables() throws Exception {
        Assume.assumeTrue(configuration.isRest());
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("stringData", "waiting for signal");
        parameters.put("personData", createPersonInstance(USER_JOHN));

        Long processInstanceId = processClient.startProcess(CONTAINER_ID, PROCESS_ID_USERTASK, parameters);
        
        List<TaskSummary> tasks = taskClient.findTasksByStatusByProcessInstanceId(processInstanceId, null, 0, 10);
        Assertions.assertThat(tasks).hasSize(1);

        SearchQueryFilterSpec spec = new SearchQueryFilterSpec();
        spec.setAttributesQueryParams(list(equalsTo(PROCESS_ATTR_DEPLOYMENT_ID, CONTAINER_ID)));
        List<ProcessInstanceCustomVars> listProcesses = queryClient.queryProcessesByVariables(spec, 0, 2);
        Assertions.assertThat(listProcesses).extracting(ProcessInstanceCustomVars::getContainerId).containsOnly(CONTAINER_ID);

        SearchQueryFilterSpec specHistory = new SearchQueryFilterSpec();
        specHistory.setAttributesQueryParams(list(history(), equalsTo(PROCESS_ATTR_DEPLOYMENT_ID, CONTAINER_ID)));
        List<ProcessInstanceCustomVars> listHistoryProcesses = queryClient.queryProcessesByVariables(specHistory, 0, 2, "processInstanceId" , false);
        assertNotNull(listHistoryProcesses);
        listHistoryProcesses.stream().forEach(e -> assertEquals(CONTAINER_ID, e.getContainerId()));
        
        spec = new SearchQueryFilterSpec();
        spec.setAttributesQueryParams(list(onlyActiveTasks(), equalsTo(PROCESS_ATTR_DEFINITION_ID, PROCESS_ID_USERTASK)));
        List<ProcessInstanceUserTaskWithVariables> listTasks = queryClient.queryUserTaskByVariables(spec, 0, 2, "processInstanceId", true);
        Assertions.assertThat(listTasks).hasSize(1).extracting(ProcessInstanceUserTaskWithVariables::getProcessDefinitionId).containsOnly(PROCESS_ID_USERTASK);

        this.taskClient.startTask(CONTAINER_ID, listTasks.get(0).getId(), "yoda");
        this.taskClient.completeTask(CONTAINER_ID, listTasks.get(0).getId(), "yoda", Collections.emptyMap());

        spec = new SearchQueryFilterSpec();
        spec.setAttributesQueryParams(list(onlyCompletedTasks(), equalsTo(PROCESS_ATTR_DEFINITION_ID, PROCESS_ID_USERTASK)));

        listTasks = queryClient.queryUserTaskByVariables(spec, 0, 2, "task.id", false);
        Assertions.assertThat(listTasks).hasSize(1);

        spec = new SearchQueryFilterSpec();
        spec.setAttributesQueryParams(list(onlyActiveTasks(), equalsTo(PROCESS_ATTR_DEFINITION_ID, PROCESS_ID_USERTASK)));

        listTasks = queryClient.queryUserTaskByVariables(spec, 0, 2, "task.id", true);
        Assertions.assertThat(listTasks).hasSize(1);

        this.taskClient.startTask(CONTAINER_ID, listTasks.get(0).getId(), "yoda");
        this.taskClient.completeTask(CONTAINER_ID, listTasks.get(0).getId(), "yoda", Collections.emptyMap());

        spec = new SearchQueryFilterSpec();
        spec.setAttributesQueryParams(list(onlyActiveTasks(), equalsTo(PROCESS_ATTR_DEFINITION_ID, PROCESS_ID_USERTASK)));

        listTasks = queryClient.queryUserTaskByVariables(spec, 0, 2);
        Assertions.assertThat(listTasks).isEmpty();

        spec = new SearchQueryFilterSpec();
        spec.setAttributesQueryParams(list(onlyCompletedTasks(), equalsTo(PROCESS_ATTR_DEFINITION_ID, PROCESS_ID_USERTASK)));
        listTasks = queryClient.queryUserTaskByVariables(spec, 0, 2);
        Assertions.assertThat(listTasks).isEmpty();
    }


    private void checkProcessDefinitions(List<String> processIds) {
        assertTrue(processIds.contains(PROCESS_ID_EVALUATION));
        assertTrue(processIds.contains(PROCESS_ID_GROUPTASK));
        assertTrue(processIds.contains(PROCESS_ID_SIGNAL_PROCESS));
        assertTrue(processIds.contains(PROCESS_ID_CUSTOM_TASK));
        assertTrue(processIds.contains(PROCESS_ID_SIGNAL_START));
        assertTrue(processIds.contains(PROCESS_ID_ASYNC_SCRIPT));
        assertTrue(processIds.contains(PROCESS_ID_TIMER));
    }

    private void assertNodeInstance(NodeInstance expected, NodeInstance actual) {
        assertNotNull(actual);
        assertEquals(expected.getName(), actual.getName());
        assertEquals(expected.getContainerId(), actual.getContainerId());
        assertEquals(expected.getNodeType(), actual.getNodeType());
        assertEquals(expected.getCompleted(), actual.getCompleted());
        assertEquals(expected.getProcessInstanceId(), actual.getProcessInstanceId());
    }

    private void assertTaskSummary(TaskSummary expected, TaskSummary actual) {
        assertNotNull(actual);
        assertEquals(expected.getName(), actual.getName());
        assertEquals(expected.getProcessId(), actual.getProcessId());
        KieServerAssert.assertNullOrEmpty(actual.getDescription());
        assertEquals(expected.getStatus(), actual.getStatus());
        assertEquals(expected.getPriority(), actual.getPriority());
        assertEquals(expected.getActualOwner(), actual.getActualOwner());
        assertEquals(expected.getCreatedBy(), actual.getCreatedBy());
        assertEquals(expected.getContainerId(), actual.getContainerId());
        assertEquals(expected.getParentId(), actual.getParentId());
        assertEquals(expected.getProcessInstanceId(), actual.getProcessInstanceId());
    }

    private void assertTaskInstance(TaskInstance expected, TaskInstance actual) {
        assertNotNull(actual);
        assertEquals(expected.getName(), actual.getName());
        KieServerAssert.assertNullOrEmpty(actual.getDescription());
        assertEquals(expected.getStatus(), actual.getStatus());
        assertEquals(expected.getPriority(), actual.getPriority());
        assertEquals(expected.getActualOwner(), actual.getActualOwner());
        assertEquals(expected.getCreatedBy(), actual.getCreatedBy());
        assertEquals(expected.getProcessId(), actual.getProcessId());
        assertEquals(expected.getContainerId(), actual.getContainerId());
        assertEquals(expected.getProcessInstanceId(), actual.getProcessInstanceId());
        assertEquals(expected.getCorrelationKey(), actual.getCorrelationKey());
        assertEquals(expected.getProcessType(), actual.getProcessType());
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

    private void assertProcessInstancesOrderById(List<ProcessInstance> processInstances, boolean ascending) {
        List<Long> pids = collectInstances(processInstances);
        List<Long> sortedPids = new ArrayList<Long>(pids);

        if(ascending) {
            Collections.sort(sortedPids);
        } else {
            sortedPids.sort(Collections.reverseOrder());
        }

        assertThat("Processes are returned in wrong order!", pids, is(sortedPids));
    }

    private TaskSummary createDefaultTaskSummary(long processInstanceId) {
        return TaskSummary
                .builder()
                .name("First task")
                .status(Status.Reserved.toString())
                .priority(0)
                .actualOwner(USER_YODA)
                .createdBy(USER_YODA)
                .processId(PROCESS_ID_USERTASK)
                .containerId(CONTAINER_ID)
                .taskParentId(-1l)
                .processInstanceId(processInstanceId)
                .build();
    }


    protected List<Long> createProcessInstances(Map<String, Object> parameters) {
        List<Long> processInstanceIds = new ArrayList<Long>();

        processInstanceIds.add(processClient.startProcess(CONTAINER_ID, PROCESS_ID_SIGNAL_PROCESS, parameters));
        processInstanceIds.add(processClient.startProcess(CONTAINER_ID, PROCESS_ID_USERTASK, parameters));
        processInstanceIds.add(processClient.startProcess(CONTAINER_ID, PROCESS_ID_SIGNAL_PROCESS, parameters));
        processInstanceIds.add(processClient.startProcess(CONTAINER_ID, PROCESS_ID_USERTASK, parameters));
        processInstanceIds.add(processClient.startProcess(CONTAINER_ID, PROCESS_ID_SIGNAL_PROCESS, parameters));

        Collections.sort(processInstanceIds);
        return processInstanceIds;
    }

    protected void abortProcessInstances(List<Long> processInstanceIds) {
        for (Long piId : processInstanceIds) {
            processClient.abortProcessInstance(CONTAINER_ID, piId);
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

    private ProcessInstance findProcessInstance(List<ProcessInstance> processInstances, Long pid) {
        return processInstances.stream().filter(n -> n.getId().equals(pid)).findFirst().orElseThrow(() -> new RuntimeException("Process instance with id " + pid + " not found."));
    }
}
