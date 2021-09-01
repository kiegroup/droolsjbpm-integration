/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
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

package org.kie.camel.container.integration.tests.remote;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.kie.camel.container.api.ExecutionServerCommand;
import org.kie.server.api.model.instance.TaskInstance;
import org.kie.server.api.model.instance.TaskSummary;

public class UserTaskClientIntegrationTest extends AbstractRemoteIntegrationTest {

    private static final String INITIAL_PROCESS_VARIABLE_VALUE = "initial-value";
    private static final String NEW_PROCESS_VARIABLE_VALUE = "new-value";

    @Test
    public void testFindTaskById() {
        final Map<String, Object> initialProcessVariables = new HashMap<>();
        initialProcessVariables.put(PROCESS_VARIABLE_NAME, "initialValue");
        final long processInstanceId = startProcess(CONTAINER_ID, PROCESS_WITH_HUMAN_TASK, initialProcessVariables);
        Assertions.assertThat(processInstanceId).isPositive();

        final List<TaskSummary> allTasks = listTaskInstancesAsPotentialOwner();
        Assertions.assertThat(allTasks).isNotEmpty();

        final Map<String, Object> parameters = new HashMap<>();
        parameters.put("taskId", allTasks.get(0).getId());
        final ExecutionServerCommand executionServerCommand = new ExecutionServerCommand();
        executionServerCommand.setClient("userTask");
        executionServerCommand.setOperation("findTaskById");
        executionServerCommand.setParameters(parameters);
        final Object response = runOnExecutionServer(executionServerCommand);
        Assertions.assertThat(response).isNotNull();
        Assertions.assertThat(response).isInstanceOf(TaskInstance.class);

        final TaskInstance taskInstance = (TaskInstance) response;
        Assertions.assertThat(taskInstance.getId()).isEqualTo(allTasks.get(0).getId());
    }

    @Test
    public void testTaskWorkflow() {
        final Map<String, Object> initialProcessVariables = new HashMap<>();
        initialProcessVariables.put(PROCESS_VARIABLE_NAME, INITIAL_PROCESS_VARIABLE_VALUE);
        final long processInstanceId = startProcess(CONTAINER_ID, PROCESS_WITH_HUMAN_TASK, initialProcessVariables);
        Assertions.assertThat(processInstanceId).isPositive();
        Assertions.assertThat(getProcessVariables(CONTAINER_ID, processInstanceId).get(PROCESS_VARIABLE_NAME))
                .isEqualTo(INITIAL_PROCESS_VARIABLE_VALUE);

        final List<TaskSummary> taskInstances = listTaskInstancesAsPotentialOwner();
        Assertions.assertThat(taskInstances).hasSize(1);
        final long taskId = taskInstances.get(0).getId();

        final Map<String, Object> parameters = new HashMap<>();
        parameters.put("containerId", CONTAINER_ID);
        parameters.put("taskId", taskId);
        parameters.put("userId", DEFAULT_USER);
        final ExecutionServerCommand claimTaskCommand = new ExecutionServerCommand();
        claimTaskCommand.setClient("userTask");
        claimTaskCommand.setOperation("claimTask");
        claimTaskCommand.setParameters(parameters);
        runOnExecutionServer(claimTaskCommand);

        final ExecutionServerCommand startTaskCommand = new ExecutionServerCommand();
        startTaskCommand.setClient("userTask");
        startTaskCommand.setOperation("startTask");
        startTaskCommand.setParameters(parameters);
        runOnExecutionServer(startTaskCommand);

        final Map<String, Object> taskParameters = new HashMap<>();
        taskParameters.put("var1", NEW_PROCESS_VARIABLE_VALUE);
        parameters.put("params", taskParameters);
        final ExecutionServerCommand completeTaskCommand = new ExecutionServerCommand();
        completeTaskCommand.setClient("userTask");
        completeTaskCommand.setOperation("completeTask");
        completeTaskCommand.setParameters(parameters);
        runOnExecutionServer(completeTaskCommand);

        Assertions.assertThat(getProcessVariables(CONTAINER_ID, processInstanceId).get(PROCESS_VARIABLE_NAME))
                .isEqualTo(NEW_PROCESS_VARIABLE_VALUE);
        sendSignalToProcessInstance(CONTAINER_ID, processInstanceId, SIGNAL_NAME);
    }

    @Test
    public void testCompleteAutoProgress() {
        final Map<String, Object> initialProcessVariables = new HashMap<>();
        initialProcessVariables.put(PROCESS_VARIABLE_NAME, INITIAL_PROCESS_VARIABLE_VALUE);
        final long processInstanceId = startProcess(CONTAINER_ID, PROCESS_WITH_HUMAN_TASK, initialProcessVariables);
        Assertions.assertThat(processInstanceId).isPositive();
        Assertions.assertThat(getProcessVariables(CONTAINER_ID, processInstanceId).get(PROCESS_VARIABLE_NAME))
                .isEqualTo(INITIAL_PROCESS_VARIABLE_VALUE);

        final List<TaskSummary> taskInstances = listTaskInstancesAsPotentialOwner();
        Assertions.assertThat(taskInstances).hasSize(1);
        final long taskId = taskInstances.get(0).getId();

        final Map<String, Object> taskParameters = new HashMap<>();
        taskParameters.put("var1", NEW_PROCESS_VARIABLE_VALUE);
        final Map<String, Object> parameters = new HashMap<>();
        parameters.put("containerId", CONTAINER_ID);
        parameters.put("taskId", taskId);
        parameters.put("userId", DEFAULT_USER);
        parameters.put("params", taskParameters);
        final ExecutionServerCommand executionServerCommand = new ExecutionServerCommand();
        executionServerCommand.setClient("userTask");
        executionServerCommand.setOperation("completeAutoProgress");
        executionServerCommand.setParameters(parameters);
        runOnExecutionServer(executionServerCommand);

        Assertions.assertThat(getProcessVariables(CONTAINER_ID, processInstanceId).get(PROCESS_VARIABLE_NAME))
                .isEqualTo(NEW_PROCESS_VARIABLE_VALUE);
        sendSignalToProcessInstance(CONTAINER_ID, processInstanceId, SIGNAL_NAME);
    }

    private List<TaskSummary> listTaskInstancesAsPotentialOwner() {
        final Map<String, Object> parameters = new HashMap<>();
        parameters.put("userId", "yoda");
        parameters.put("page", "0");
        parameters.put("pageSize", "10");
        final ExecutionServerCommand executionServerCommand = new ExecutionServerCommand();
        executionServerCommand.setClient("userTask");
        executionServerCommand.setOperation("findTasksAssignedAsPotentialOwner");
        executionServerCommand.setParameters(parameters);
        final Object response = runOnExecutionServer(executionServerCommand);
        Assertions.assertThat(response).isNotNull();
        Assertions.assertThat(response).isInstanceOf(List.class);

        final List<TaskSummary> taskInstances = (List<TaskSummary>) response;
        return taskInstances;
    }
}
