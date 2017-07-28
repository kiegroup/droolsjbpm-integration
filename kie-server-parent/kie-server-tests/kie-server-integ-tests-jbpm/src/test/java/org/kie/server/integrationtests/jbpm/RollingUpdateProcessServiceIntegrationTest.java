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

import static org.assertj.core.api.Assertions.assertThat;
import static org.kie.server.remote.rest.jbpm.resources.Messages.NO_PROCESS_AVAILABLE_WITH_ID;
import static org.kie.server.remote.rest.jbpm.resources.Messages.PROCESS_DEFINITION_NOT_FOUND;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kie.api.KieServices;
import org.kie.api.runtime.process.WorkItem;
import org.kie.api.task.model.Status;
import org.kie.server.api.model.KieContainerResource;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.instance.ProcessInstance;
import org.kie.server.api.model.instance.TaskInstance;
import org.kie.server.api.model.instance.TaskSummary;
import org.kie.server.api.model.instance.WorkItemInstance;
import org.kie.server.integrationtests.config.TestConfig;
import org.kie.server.integrationtests.shared.KieServerDeployer;
import org.kie.server.integrationtests.shared.KieServerReflections;

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
        KieServerDeployer.buildAndDeployMavenProject(ClassLoader.class.getResource("/kjars-sources/different-gav-definition-project").getFile());

        kieContainer = KieServices.Factory.get().newKieContainer(releaseId);

        createContainer(CONTAINER_ID, releaseId, CONTAINER_ALIAS);
    }

    @After
    public void removeExtraContainer() {
        abortAllProcesses();
        client.disposeContainer(CONTAINER_ID_101);
    }

    protected void createExtraContainer() {
        KieContainerResource containerResource = new KieContainerResource(CONTAINER_ID_101, releaseId101);
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

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("test", USER_MARY);
        parameters.put("number", 12345);

        List<Object> list = new ArrayList<>();
        list.add("item");

        parameters.put("list", list);
        parameters.put("person", person);
        Long processInstanceId = processClient.startProcess(CONTAINER_ALIAS, PROCESS_ID_EVALUATION, parameters);

        assertThat(processInstanceId).isNotNull().isGreaterThan(0);

        ProcessInstance processInstance = processClient.getProcessInstance(CONTAINER_ALIAS, processInstanceId);
        assertThat(processInstance).isNotNull();
        assertThat(processInstance.getContainerId()).isEqualTo(CONTAINER_ID);

        Object personVariable = processClient.getProcessInstanceVariable(CONTAINER_ALIAS, processInstanceId, "person");
        assertThat(personVariable).isNotNull();
        assertThat(personClass).isAssignableFrom(personVariable.getClass());

        Map<String, Object> variables = processClient.getProcessInstanceVariables(CONTAINER_ALIAS, processInstanceId);
        assertThat(variables).isNotNull();
        assertThat(variables).hasSize(5);
        assertThat(variables).containsKey("test");
        assertThat(variables).containsKey("number");
        assertThat(variables).containsKey("list");
        assertThat(variables).containsKey("person");
        assertThat(variables).containsKey("initiator");

        assertThat(variables.get("test")).isNotNull();
        assertThat(variables.get("number")).isNotNull();
        assertThat(variables.get("list")).isNotNull();
        assertThat(variables.get("person")).isNotNull();
        assertThat(variables.get("initiator")).isNotNull();

        assertThat(String.class).isAssignableFrom(variables.get("test").getClass());
        assertThat(Integer.class).isAssignableFrom(variables.get("number").getClass());
        assertThat(List.class).isAssignableFrom(variables.get("list").getClass());
        assertThat(personClass).isAssignableFrom(variables.get("person").getClass());
        assertThat(String.class).isAssignableFrom(variables.get("initiator").getClass());

        assertThat(variables.get("test")).isEqualTo(USER_MARY);
        assertThat(variables.get("number")).isEqualTo(12345);
        assertThat((List) variables.get("list")).hasSize(1);
        assertThat(((List) variables.get("list")).get(0)).isEqualTo("item");
        assertThat(KieServerReflections.valueOf(variables.get("person"), "name")).isEqualTo(USER_JOHN);
        assertThat(variables.get("initiator")).isEqualTo(TestConfig.getUsername());

        processClient.setProcessVariable(CONTAINER_ALIAS, processInstanceId, "test", USER_JOHN);
        variables = processClient.getProcessInstanceVariables(CONTAINER_ALIAS, processInstanceId);
        assertThat(variables.get("test")).isEqualTo(USER_JOHN);

        Map<String, Object> newVariables = new HashMap<>();
        newVariables.put("test", USER_MARY);
        newVariables.put("number", 6789);

        processClient.setProcessVariables(CONTAINER_ALIAS, processInstanceId, newVariables);
        variables = processClient.getProcessInstanceVariables(CONTAINER_ALIAS, processInstanceId);
        assertThat(variables.get("test")).isEqualTo(USER_MARY);
        assertThat(variables.get("number")).isEqualTo(6789);

    }

    @Test
    public void testStartProcessInDifferentDeploymentWithAlias() throws Exception {

        Object person = createPersonInstance(USER_JOHN);

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("test", USER_MARY);
        parameters.put("number", 12345);

        List<Object> list = new ArrayList<>();
        list.add("item");

        parameters.put("list", list);
        parameters.put("person", person);


        Long processInstanceIdV1 = processClient.startProcess(CONTAINER_ALIAS, PROCESS_ID_EVALUATION, parameters);

        assertThat(processInstanceIdV1).isNotNull().isGreaterThan(0);

        ProcessInstance processInstance = processClient.getProcessInstance(CONTAINER_ALIAS, processInstanceIdV1);
        assertThat(processInstance).isNotNull();
        assertThat(processInstance.getContainerId()).isEqualTo(CONTAINER_ID);

        List<TaskSummary> tasks = taskClient.findTasksAssignedAsPotentialOwner(USER_YODA, 0, 10);
        assertThat(tasks).hasSize(1);
        TaskSummary task = tasks.get(0);
        assertThat(task.getContainerId()).isEqualTo(CONTAINER_ID);

        taskClient.completeAutoProgress(CONTAINER_ALIAS, task.getId(), USER_YODA, null);

        createExtraContainer();

        Long processInstanceIdV2 = processClient.startProcess(CONTAINER_ALIAS, PROCESS_ID_EVALUATION_2, parameters);

        assertThat(processInstanceIdV2).isNotNull().isGreaterThan(0);

        processInstance = processClient.getProcessInstance(CONTAINER_ALIAS, processInstanceIdV2);
        assertThat(processInstance).isNotNull();
        assertThat(processInstance.getContainerId()).isEqualTo(CONTAINER_ID_101);

        tasks = taskClient.findTasksAssignedAsPotentialOwner(USER_YODA, 0, 10);
        assertThat(tasks).hasSize(1);
        task = tasks.get(0);
        assertThat(task.getContainerId()).isEqualTo(CONTAINER_ID_101);

        taskClient.completeAutoProgress(CONTAINER_ALIAS, task.getId(), USER_YODA, null);

    }

    @Test
    public void testStartProcessInDifferentDeploymentWithContainerId() throws Exception {

        Object person = createPersonInstance(USER_JOHN);

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("test", USER_MARY);
        parameters.put("number", 12345);

        List<Object> list = new ArrayList<>();
        list.add("item");

        parameters.put("list", list);
        parameters.put("person", person);

        Long processInstanceIdV1 = processClient.startProcess(CONTAINER_ID, PROCESS_ID_EVALUATION, parameters);

        assertThat(processInstanceIdV1).isNotNull().isGreaterThan(0);

        ProcessInstance processInstance = processClient.getProcessInstance(CONTAINER_ALIAS, processInstanceIdV1);
        assertThat(processInstance).isNotNull();
        assertThat(processInstance.getContainerId()).isEqualTo(CONTAINER_ID);

        List<TaskSummary> tasks = taskClient.findTasksAssignedAsPotentialOwner(USER_YODA, 0, 10);
        assertThat(tasks).hasSize(1);
        TaskSummary task = tasks.get(0);
        assertThat(task.getContainerId()).isEqualTo(CONTAINER_ID);

        taskClient.completeAutoProgress(CONTAINER_ALIAS, task.getId(), USER_YODA, null);

        createExtraContainer();

        Long processInstanceIdV2 = processClient.startProcess(CONTAINER_ID_101, PROCESS_ID_EVALUATION_2, parameters);

        assertThat(processInstanceIdV2).isNotNull().isGreaterThan(0);

        processInstance = processClient.getProcessInstance(CONTAINER_ALIAS, processInstanceIdV2);
        assertThat(processInstance).isNotNull();
        assertThat(processInstance.getContainerId()).isEqualTo(CONTAINER_ID_101);

        tasks = taskClient.findTasksAssignedAsPotentialOwner(USER_YODA, 0, 10);
        assertThat(tasks).hasSize(1);
        task = tasks.get(0);
        assertThat(task.getContainerId()).isEqualTo(CONTAINER_ID_101);

        taskClient.completeAutoProgress(CONTAINER_ALIAS, task.getId(), USER_YODA, null);

        // let's start another instance of the old version of container
        Long processInstanceIdV3 = processClient.startProcess(CONTAINER_ID, PROCESS_ID_EVALUATION, parameters);

        assertThat(processInstanceIdV3).isNotNull().isGreaterThan(0);

        processInstance = processClient.getProcessInstance(CONTAINER_ALIAS, processInstanceIdV3);
        assertThat(processInstance).isNotNull();
        assertThat(processInstance.getContainerId()).isEqualTo(CONTAINER_ID);

        tasks = taskClient.findTasksAssignedAsPotentialOwner(USER_YODA, 0, 10);
        assertThat(tasks).hasSize(2);
        task = tasks.get(0);
        assertThat(task.getContainerId()).isEqualTo(CONTAINER_ID);

        taskClient.completeAutoProgress(CONTAINER_ALIAS, task.getId(), USER_YODA, null);

    }

    @Test
    public void testDifferentGAVsWithAlias() throws Exception {
        Long oldPid = processClient.startProcess(CONTAINER_ALIAS, PROCESS_ID_EVALUATION);
        assertThat(oldPid).isNotNull().isGreaterThan(0);

        ProcessInstance processInstance = processClient.getProcessInstance(CONTAINER_ALIAS, oldPid);
        assertThat(processInstance).isNotNull();
        assertThat(processInstance.getContainerId()).isEqualTo(CONTAINER_ID);

        // Create a container with completely different GAV
        ReleaseId differentReleaseId101 = new ReleaseId("org.kie.server.different", "different-gav-definition-project",
                "1.0.1.Final");
        KieContainerResource containerResource = new KieContainerResource(CONTAINER_ID_101, differentReleaseId101);
        containerResource.setContainerAlias(CONTAINER_ALIAS);
        client.createContainer(CONTAINER_ID_101, containerResource);

        Long pid = processClient.startProcess(CONTAINER_ALIAS, PROCESS_ID_EVALUATION_2);
        assertThat(pid).isNotNull().isGreaterThan(0);

        processInstance = processClient.getProcessInstance(CONTAINER_ALIAS, pid);
        assertThat(processInstance).isNotNull();
        assertThat(processInstance.getContainerId()).isEqualTo(CONTAINER_ID_101);

    }

    @Test
    public void testContainerIdAsAlias() throws Exception {
        Long oldPid = processClient.startProcess(CONTAINER_ALIAS, PROCESS_ID_EVALUATION);
        assertThat(oldPid).isNotNull().isGreaterThan(0);

        ProcessInstance processInstance = processClient.getProcessInstance(CONTAINER_ALIAS, oldPid);
        assertThat(processInstance).isNotNull();
        assertThat(processInstance.getContainerId()).isEqualTo(CONTAINER_ID);

        // Create the second container with alias equal to the first containerId
        KieContainerResource containerResource = new KieContainerResource(CONTAINER_ID_101, releaseId101);
        containerResource.setContainerAlias(CONTAINER_ID);
        client.createContainer(CONTAINER_ID_101, containerResource);

        assertClientException(() -> processClient.startProcess(CONTAINER_ALIAS, PROCESS_ID_EVALUATION_2),
                404,
                MessageFormat.format(PROCESS_DEFINITION_NOT_FOUND, PROCESS_ID_EVALUATION_2, CONTAINER_ALIAS),
                MessageFormat.format(NO_PROCESS_AVAILABLE_WITH_ID, PROCESS_ID_EVALUATION_2));

        // Instead the old one should be chosen, since it looks for containerId first
        oldPid = processClient.startProcess(CONTAINER_ID, PROCESS_ID_EVALUATION);
        assertThat(oldPid).isNotNull().isGreaterThan(0);

        processInstance = processClient.getProcessInstance(CONTAINER_ALIAS, oldPid);
        assertThat(processInstance).isNotNull();
        assertThat(processInstance.getContainerId()).isEqualTo(CONTAINER_ID);

    }

    @Test
    public void testSignalNewProcessWithAlias() throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("nullAccepted", false);

        Long oldPid = processClient.startProcess(CONTAINER_ALIAS, PROCESS_ID_SIGNAL_PROCESS, params);
        assertThat(oldPid).isNotNull().isGreaterThan(0);

        createExtraContainer();

        Long pid = processClient.startProcess(CONTAINER_ALIAS, PROCESS_ID_SIGNAL_PROCESS_2, params);
        assertThat(pid).isNotNull().isGreaterThan(0);

        List<String> availableSignals = processClient.getAvailableSignals(CONTAINER_ALIAS, pid);
        assertThat(availableSignals).isNotNull().hasSize(2);
        assertThat(availableSignals).contains("Signal1");
        assertThat(availableSignals).contains("Signal2");

        Object person = createPersonInstance(USER_JOHN);
        processClient.signalProcessInstance(CONTAINER_ALIAS, pid, "Signal1", person);

        processClient.signalProcessInstances(CONTAINER_ALIAS, Collections.singletonList(pid), "Signal2", "My custom string event");

        ProcessInstance pi = processClient.getProcessInstance(CONTAINER_ALIAS, pid);
        assertThat(pi).isNotNull();
        assertThat(pi.getState()).isEqualTo(org.kie.api.runtime.process.ProcessInstance.STATE_COMPLETED);

        pi = processClient.getProcessInstance(CONTAINER_ALIAS, oldPid);
        assertThat(pi).isNotNull();
        assertThat(pi.getState()).isEqualTo(org.kie.api.runtime.process.ProcessInstance.STATE_ACTIVE);

    }

    @Test
    public void testCompleteWorkItemInNewProcessWithAlias() throws Exception {
        Long oldPid = processClient.startProcess(CONTAINER_ALIAS, PROCESS_ID_EVALUATION);
        assertThat(oldPid).isNotNull().isGreaterThan(0);

        createExtraContainer();

        Long pid = processClient.startProcess(CONTAINER_ALIAS, PROCESS_ID_EVALUATION_2);
        assertThat(pid).isNotNull().isGreaterThan(0);

        List<TaskSummary> taskList = taskClient.findTasksAssignedAsPotentialOwner(USER_YODA, 0, 10);
        assertThat(taskList).hasSize(2);

        // Complete task from the first (old) process
        taskList = taskClient.findTasksByStatusByProcessInstanceId(oldPid, null, 0, 10);
        TaskSummary taskSummary = taskList.get(0);
        taskClient.startTask(CONTAINER_ALIAS, taskSummary.getId(), USER_YODA);
        taskClient.completeTask(CONTAINER_ALIAS, taskSummary.getId(), USER_YODA, null);

        TaskInstance userTask = taskClient.getTaskInstance(CONTAINER_ALIAS, taskSummary.getId());
        assertThat(userTask).isNotNull();
        assertThat(userTask.getContainerId()).isEqualTo(CONTAINER_ID);
        assertThat(userTask.getName()).isEqualTo("Evaluate items?");
        assertThat(userTask.getStatus()).isEqualTo(Status.Completed.toString());

        // Complete tasks from the second (new) process - there are two tasks instead of one
        taskList = taskClient.findTasksByStatusByProcessInstanceId(pid, null, 0, 10);
        assertThat(taskList).hasSize(1);

        // First one - Evaluate items
        taskSummary = taskList.get(0);
        taskClient.startTask(CONTAINER_ALIAS, taskSummary.getId(), USER_YODA);
        taskClient.completeTask(CONTAINER_ALIAS, taskSummary.getId(), USER_YODA, null);

        userTask = taskClient.getTaskInstance(CONTAINER_ALIAS, taskSummary.getId());
        assertThat(userTask).isNotNull();
        assertThat(userTask.getContainerId()).isEqualTo(CONTAINER_ID_101);
        assertThat(userTask.getName()).isEqualTo("Evaluate items");
        assertThat(userTask.getStatus()).isEqualTo(Status.Completed.toString());

        //Second one - Approve
        taskList = taskClient.findTasksByStatusByProcessInstanceId(pid, null, 0, 10);
        assertThat(taskList).hasSize(1);

        taskSummary = taskList.get(0);
        taskClient.startTask(CONTAINER_ALIAS, taskSummary.getId(), USER_YODA);
        taskClient.completeTask(CONTAINER_ALIAS, taskSummary.getId(), USER_YODA, null);

        userTask = taskClient.getTaskInstance(CONTAINER_ALIAS, taskSummary.getId());
        assertThat(userTask).isNotNull();
        assertThat(userTask.getContainerId()).isEqualTo(CONTAINER_ID_101);
        assertThat(userTask.getName()).isEqualTo("Approve");
        assertThat(userTask.getStatus()).isEqualTo(Status.Completed.toString());

        List<WorkItemInstance> workItems = processClient.getWorkItemByProcessInstance(CONTAINER_ALIAS, pid);
        assertThat(workItems).isNotNull().hasSize(1);

        WorkItemInstance workItemInstance = workItems.get(0);
        assertThat(workItemInstance).isNotNull();
        assertThat(workItemInstance.getContainerId()).isEqualTo(CONTAINER_ID_101);
        assertThat(workItemInstance.getName()).isEqualTo("Email");
        assertThat(workItemInstance.getState()).isEqualTo(WorkItem.PENDING);

        WorkItemInstance workItemInstance2 = processClient.getWorkItem(CONTAINER_ALIAS, pid, workItemInstance.getId());

        assertThat(workItemInstance2.getId()).isEqualTo(workItemInstance.getId());

        processClient.completeWorkItem(CONTAINER_ALIAS, pid, workItemInstance.getId(), null);

        ProcessInstance pi = processClient.getProcessInstance(CONTAINER_ALIAS, pid);
        assertThat(pi).isNotNull();
        assertThat(pi.getState()).isEqualTo(org.kie.api.runtime.process.ProcessInstance.STATE_COMPLETED);

        pi = processClient.getProcessInstance(CONTAINER_ALIAS, oldPid);
        assertThat(pi).isNotNull();
        assertThat(pi.getState()).isEqualTo(org.kie.api.runtime.process.ProcessInstance.STATE_ACTIVE);

    }
}
