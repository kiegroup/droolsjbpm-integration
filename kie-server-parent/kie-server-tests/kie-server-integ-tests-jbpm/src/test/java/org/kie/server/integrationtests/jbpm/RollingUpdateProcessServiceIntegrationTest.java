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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.After;
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
import org.kie.server.integrationtests.shared.KieServerDeployer;
import org.kie.server.integrationtests.shared.KieServerSynchronization;

import static org.junit.Assert.*;

public class RollingUpdateProcessServiceIntegrationTest extends JbpmKieServerBaseIntegrationTest {

    private static ReleaseId releaseId = new ReleaseId("org.kie.server.testing", "definition-project",
            "1.0.0.Final");
    private static final ReleaseId releaseId101 = new ReleaseId("org.kie.server.testing", "definition-project",
            "1.0.1.Final");


    protected static final String CONTAINER_ALIAS = "project";
    protected static final String CONTAINER_ID_101 = "definition-project-101";

    @BeforeClass
    public static void buildAndDeployArtifacts() {

        KieServerDeployer.buildAndDeployCommonMavenParent();
        KieServerDeployer.buildAndDeployMavenProject(ClassLoader.class.getResource("/kjars-sources/definition-project").getFile());
        KieServerDeployer.buildAndDeployMavenProject(ClassLoader.class.getResource("/kjars-sources/definition-project-101").getFile());

        kieContainer = KieServices.Factory.get().newKieContainer(releaseId);

        createContainer(CONTAINER_ID, releaseId, CONTAINER_ALIAS);
    }

    @After
    public void removeExtraContainer() {
        abortAllProcesses();
        client.disposeContainer(CONTAINER_ID_101);
    }

    protected void createExtraContainer() {
        KieContainerResource containerResource = new KieContainerResource(CONTAINER_ID_101, releaseId);
        containerResource.setContainerAlias(CONTAINER_ALIAS);
        client.createContainer(CONTAINER_ID_101, containerResource);
    }

    @Override
    protected void addExtraCustomClasses(Map<String, Class<?>> extraClasses) throws Exception {
        extraClasses.put(PERSON_CLASS_NAME, Class.forName(PERSON_CLASS_NAME, true, kieContainer.getClassLoader()));
    }

    @Test
    public void testStartCheckVariablesAndAbortProcess() throws Exception {
        Class<?> personClass = Class.forName(PERSON_CLASS_NAME, true, kieContainer.getClassLoader());

        Object person = createPersonInstance(USER_JOHN);

        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("test", USER_MARY);
        parameters.put("number", new Integer(12345));

        List<Object> list = new ArrayList<Object>();
        list.add("item");

        parameters.put("list", list);
        parameters.put("person", person);
        Long processInstanceId = processClient.startProcess(CONTAINER_ALIAS, PROCESS_ID_EVALUATION, parameters);

        assertNotNull(processInstanceId);
        assertTrue(processInstanceId.longValue() > 0);

        ProcessInstance processInstance = processClient.getProcessInstance(CONTAINER_ALIAS, processInstanceId);
        assertNotNull(processInstance);
        assertEquals(CONTAINER_ID, processInstance.getContainerId());

        Object personVariable = processClient.getProcessInstanceVariable(CONTAINER_ALIAS, processInstanceId, "person");
        assertNotNull(personVariable);
        assertTrue(personClass.isAssignableFrom(personVariable.getClass()));

        personVariable = processClient.getProcessInstanceVariable(CONTAINER_ALIAS, processInstanceId, "person");
        assertNotNull(personVariable);
        assertTrue(personClass.isAssignableFrom(personVariable.getClass()));

        Map<String, Object> variables = processClient.getProcessInstanceVariables(CONTAINER_ALIAS, processInstanceId);
        assertNotNull(variables);
        assertEquals(5, variables.size());
        assertTrue(variables.containsKey("test"));
        assertTrue(variables.containsKey("number"));
        assertTrue(variables.containsKey("list"));
        assertTrue(variables.containsKey("person"));
        assertTrue(variables.containsKey("initiator"));

        assertNotNull(variables.get("test"));
        assertNotNull(variables.get("number"));
        assertNotNull(variables.get("list"));
        assertNotNull(variables.get("person"));
        assertNotNull(variables.get("initiator"));

        assertTrue(String.class.isAssignableFrom(variables.get("test").getClass()));
        assertTrue(Integer.class.isAssignableFrom(variables.get("number").getClass()));
        assertTrue(List.class.isAssignableFrom(variables.get("list").getClass()));
        assertTrue(personClass.isAssignableFrom(variables.get("person").getClass()));
        assertTrue(String.class.isAssignableFrom(variables.get("initiator").getClass()));

        assertEquals(USER_MARY, variables.get("test"));
        assertEquals(12345, variables.get("number"));
        assertEquals(1, ((List) variables.get("list")).size());
        assertEquals("item", ((List) variables.get("list")).get(0));
        assertEquals(USER_JOHN, valueOf(variables.get("person"), "name"));
        assertEquals(TestConfig.getUsername(), variables.get("initiator"));

    }

    @Test
    public void testStartProcessInDifferentDeploymentWithAlias() throws Exception {

        Object person = createPersonInstance(USER_JOHN);

        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("test", USER_MARY);
        parameters.put("number", new Integer(12345));

        List<Object> list = new ArrayList<Object>();
        list.add("item");

        parameters.put("list", list);
        parameters.put("person", person);


        Long processInstanceIdV1 = processClient.startProcess(CONTAINER_ALIAS, PROCESS_ID_EVALUATION, parameters);

        assertNotNull(processInstanceIdV1);
        assertTrue(processInstanceIdV1.longValue() > 0);

        ProcessInstance processInstance = processClient.getProcessInstance(CONTAINER_ALIAS, processInstanceIdV1);
        assertNotNull(processInstance);
        assertEquals(CONTAINER_ID, processInstance.getContainerId());

        List<TaskSummary> tasks = taskClient.findTasksAssignedAsPotentialOwner(USER_YODA, 0, 10);
        assertEquals(1, tasks.size());
        TaskSummary task = tasks.get(0);
        assertEquals(CONTAINER_ID, task.getContainerId());

        taskClient.completeAutoProgress(CONTAINER_ALIAS, task.getId(), USER_YODA, null);

        createExtraContainer();

        Long processInstanceIdV2 = processClient.startProcess(CONTAINER_ALIAS, PROCESS_ID_EVALUATION, parameters);

        assertNotNull(processInstanceIdV2);
        assertTrue(processInstanceIdV2.longValue() > 0);

        processInstance = processClient.getProcessInstance(CONTAINER_ALIAS, processInstanceIdV2);
        assertNotNull(processInstance);
        assertEquals(CONTAINER_ID_101, processInstance.getContainerId());

        tasks = taskClient.findTasksAssignedAsPotentialOwner(USER_YODA, 0, 10);
        assertEquals(1, tasks.size());
        task = tasks.get(0);
        assertEquals(CONTAINER_ID_101, task.getContainerId());

        taskClient.completeAutoProgress(CONTAINER_ALIAS, task.getId(), USER_YODA, null);

    }

    @Test
    public void testStartProcessInDifferentDeploymentWithContainerId() throws Exception {

        Object person = createPersonInstance(USER_JOHN);

        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("test", USER_MARY);
        parameters.put("number", new Integer(12345));

        List<Object> list = new ArrayList<Object>();
        list.add("item");

        parameters.put("list", list);
        parameters.put("person", person);

        Long processInstanceIdV1 = processClient.startProcess(CONTAINER_ID, PROCESS_ID_EVALUATION, parameters);

        assertNotNull(processInstanceIdV1);
        assertTrue(processInstanceIdV1.longValue() > 0);

        ProcessInstance processInstance = processClient.getProcessInstance(CONTAINER_ALIAS, processInstanceIdV1);
        assertNotNull(processInstance);
        assertEquals(CONTAINER_ID, processInstance.getContainerId());

        List<TaskSummary> tasks = taskClient.findTasksAssignedAsPotentialOwner(USER_YODA, 0, 10);
        assertEquals(1, tasks.size());
        TaskSummary task = tasks.get(0);
        assertEquals(CONTAINER_ID, task.getContainerId());

        taskClient.completeAutoProgress(CONTAINER_ALIAS, task.getId(), USER_YODA, null);

        createExtraContainer();

        Long processInstanceIdV2 = processClient.startProcess(CONTAINER_ID_101, PROCESS_ID_EVALUATION, parameters);

        assertNotNull(processInstanceIdV2);
        assertTrue(processInstanceIdV2.longValue() > 0);

        processInstance = processClient.getProcessInstance(CONTAINER_ALIAS, processInstanceIdV2);
        assertNotNull(processInstance);
        assertEquals(CONTAINER_ID_101, processInstance.getContainerId());

        tasks = taskClient.findTasksAssignedAsPotentialOwner(USER_YODA, 0, 10);
        assertEquals(1, tasks.size());
        task = tasks.get(0);
        assertEquals(CONTAINER_ID_101, task.getContainerId());

        taskClient.completeAutoProgress(CONTAINER_ALIAS, task.getId(), USER_YODA, null);

        // let's start another instance of the old version of container
        Long processInstanceIdV3 = processClient.startProcess(CONTAINER_ID, PROCESS_ID_EVALUATION, parameters);

        assertNotNull(processInstanceIdV3);
        assertTrue(processInstanceIdV3.longValue() > 0);

        processInstance = processClient.getProcessInstance(CONTAINER_ALIAS, processInstanceIdV3);
        assertNotNull(processInstance);
        assertEquals(CONTAINER_ID, processInstance.getContainerId());

        tasks = taskClient.findTasksAssignedAsPotentialOwner(USER_YODA, 0, 10);
        assertEquals(1, tasks.size());
        task = tasks.get(0);
        assertEquals(CONTAINER_ID, task.getContainerId());

        taskClient.completeAutoProgress(CONTAINER_ALIAS, task.getId(), USER_YODA, null);

    }
}
