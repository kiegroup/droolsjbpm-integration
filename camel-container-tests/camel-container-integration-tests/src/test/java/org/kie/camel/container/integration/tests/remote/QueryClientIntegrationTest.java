/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.kie.camel.container.api.ExecutionServerCommand;
import org.kie.server.api.model.definition.ProcessDefinition;
import org.kie.server.api.model.definition.QueryDefinition;
import org.kie.server.api.model.instance.ProcessInstance;

public class QueryClientIntegrationTest extends AbstractRemoteIntegrationTest {

    @Test
    public void testFindProcessesById() {
        final Map<String, Object> parameters = new HashMap<>();
        parameters.put("processId", PROCESS_ID);
        final ExecutionServerCommand executionServerCommand = new ExecutionServerCommand();
        executionServerCommand.setClient("query");
        executionServerCommand.setOperation("findProcessesById");
        executionServerCommand.setParameters(parameters);
        final Object response = runOnExecutionServer(executionServerCommand);
        Assertions.assertThat(response).isNotNull();
        Assertions.assertThat(response).isInstanceOf(List.class);
        final List<ProcessDefinition> processDefinitions = (List<ProcessDefinition>) response;
        Assertions.assertThat(processDefinitions).isNotEmpty();
        final List<String> processIds = processDefinitions.stream().map(p -> p.getId()).collect(Collectors.toList());
        Assertions.assertThat(processIds).contains(PROCESS_ID);
    }

    @Test
    public void testFindProcesses() {
        final Map<String, Object> parameters = new HashMap<>();
        parameters.put("page", "0");
        parameters.put("pageSize", "10");
        final ExecutionServerCommand executionServerCommand = new ExecutionServerCommand();
        executionServerCommand.setClient("query");
        executionServerCommand.setOperation("findProcesses");
        executionServerCommand.setParameters(parameters);
        final Object response = runOnExecutionServer(executionServerCommand);
        Assertions.assertThat(response).isNotNull();
        Assertions.assertThat(response).isInstanceOf(List.class);
        final List<ProcessDefinition> processDefinitions = (List<ProcessDefinition>) response;
        Assertions.assertThat(processDefinitions).isNotEmpty();
        final List<String> processIds = processDefinitions.stream().map(p -> p.getId()).collect(Collectors.toList());
        Assertions.assertThat(processIds).contains(PROCESS_ID);
    }

    @Test
    public void testFindProcessesByContainerId() {
        final Map<String, Object> parameters = new HashMap<>();
        parameters.put("containerId", CONTAINER_ID);
        parameters.put("page", "0");
        parameters.put("pageSize", "10");
        final ExecutionServerCommand executionServerCommand = new ExecutionServerCommand();
        executionServerCommand.setClient("query");
        executionServerCommand.setOperation("findProcessesByContainerId");
        executionServerCommand.setParameters(parameters);
        final Object response = runOnExecutionServer(executionServerCommand);
        Assertions.assertThat(response).isNotNull();
        Assertions.assertThat(response).isInstanceOf(List.class);
        final List<ProcessDefinition> processDefinitions = (List<ProcessDefinition>) response;
        Assertions.assertThat(processDefinitions).isNotEmpty();
        final List<String> processIds = processDefinitions.stream().map(p -> p.getId()).collect(Collectors.toList());
        Assertions.assertThat(processIds).contains(PROCESS_ID);
    }

    @Test
    public void testFindProcessesByContainerIdWrongId() {
        final Map<String, Object> parameters = new HashMap<>();
        parameters.put("containerId", "wrong-container-id");
        parameters.put("page", "0");
        parameters.put("pageSize", "10");
        final ExecutionServerCommand executionServerCommand = new ExecutionServerCommand();
        executionServerCommand.setClient("query");
        executionServerCommand.setOperation("findProcessesByContainerId");
        executionServerCommand.setParameters(parameters);
        final Object response = runOnExecutionServer(executionServerCommand);
        Assertions.assertThat(response).isNotNull();
        Assertions.assertThat(response).isInstanceOf(List.class);
        final List<ProcessDefinition> processDefinitions = (List<ProcessDefinition>) response;
        Assertions.assertThat(processDefinitions).isEmpty();
    }

    @Test
    public void testFindProcessByContainerIdProcessId() {
        final Map<String, Object> parameters = new HashMap<>();
        parameters.put("containerId", CONTAINER_ID);
        parameters.put("processId", PROCESS_ID);
        final ExecutionServerCommand executionServerCommand = new ExecutionServerCommand();
        executionServerCommand.setClient("query");
        executionServerCommand.setOperation("findProcessByContainerIdProcessId");
        executionServerCommand.setParameters(parameters);
        final Object response = runOnExecutionServer(executionServerCommand);
        Assertions.assertThat(response).isNotNull();
        Assertions.assertThat(response).isInstanceOf(ProcessDefinition.class);
        final ProcessDefinition processDefinition = (ProcessDefinition) response;
        Assertions.assertThat(processDefinition.getContainerId()).isEqualTo(CONTAINER_ID);
        Assertions.assertThat(processDefinition.getId()).isEqualTo(PROCESS_ID);
    }

    @Test
    public void testFindProcessInstances() {
        final Long processInstanceId = startProcess(CONTAINER_ID, PROCESS_WITH_SIGNAL_ID);

        final Map<String, Object> parameters = new HashMap<>();
        parameters.put("page", "0");
        parameters.put("pageSize", "10");
        final ExecutionServerCommand executionServerCommand = new ExecutionServerCommand();
        executionServerCommand.setClient("query");
        executionServerCommand.setOperation("findProcessInstances");
        executionServerCommand.setParameters(parameters);
        final Object response = runOnExecutionServer(executionServerCommand);
        Assertions.assertThat(response).isNotNull();
        Assertions.assertThat(response).isInstanceOf(List.class);
        final List<ProcessInstance> processInstances = (List<ProcessInstance>) response;
        Assertions.assertThat(processInstances).isNotEmpty();
        final List<Long> processInstancesIds =
                processInstances.stream().map(p -> p.getId()).collect(Collectors.toList());
        Assertions.assertThat(processInstancesIds).contains(processInstanceId);

        sendSignalToProcessInstance(CONTAINER_ID, processInstanceId, SIGNAL_NAME);
    }

    @Test
    public void testFindProcessInstancesByContainerId() {
        final Long processInstanceId = startProcess(CONTAINER_ID, PROCESS_WITH_SIGNAL_ID);

        final List<Integer> statuses = Arrays.asList(org.kie.api.runtime.process.ProcessInstance.STATE_ACTIVE);
        final Map<String, Object> parameters = new HashMap<>();
        parameters.put("containerId", CONTAINER_ID);
        parameters.put("page", "0");
        parameters.put("pageSize", "10");
        parameters.put("status", statuses);
        final ExecutionServerCommand executionServerCommand = new ExecutionServerCommand();
        executionServerCommand.setClient("query");
        executionServerCommand.setOperation("findProcessInstancesByContainerId");
        executionServerCommand.setParameters(parameters);

        final Object response = runOnExecutionServer(executionServerCommand);
        Assertions.assertThat(response).isNotNull();
        Assertions.assertThat(response).isInstanceOf(List.class);
        final List<ProcessInstance> processInstances = (List<ProcessInstance>) response;
        Assertions.assertThat(processInstances).isNotEmpty();
        final List<Long> processInstancesIds =
                processInstances.stream().map(p -> p.getId()).collect(Collectors.toList());
        Assertions.assertThat(processInstancesIds).contains(processInstanceId);

        sendSignalToProcessInstance(CONTAINER_ID, processInstanceId, SIGNAL_NAME);
    }

    @Test
    public void testFindProcessInstancesByStatus() {
        final Long processInstanceId = startProcess(CONTAINER_ID, PROCESS_WITH_SIGNAL_ID);

        final List<Integer> statuses = Arrays.asList(org.kie.api.runtime.process.ProcessInstance.STATE_ACTIVE);
        final Map<String, Object> parameters = new HashMap<>();
        parameters.put("status", statuses);
        parameters.put("page", "0");
        parameters.put("pageSize", "10");
        final ExecutionServerCommand executionServerCommand = new ExecutionServerCommand();
        executionServerCommand.setClient("query");
        executionServerCommand.setOperation("findProcessInstancesByStatus");
        executionServerCommand.setParameters(parameters);

        final Object response = runOnExecutionServer(executionServerCommand);
        Assertions.assertThat(response).isNotNull();
        Assertions.assertThat(response).isInstanceOf(List.class);
        final List<ProcessInstance> processInstances = (List<ProcessInstance>) response;
        Assertions.assertThat(processInstances).isNotEmpty();
        final List<Long> processInstancesIds =
                processInstances.stream().map(p -> p.getId()).collect(Collectors.toList());
        Assertions.assertThat(processInstancesIds).contains(processInstanceId);

        sendSignalToProcessInstance(CONTAINER_ID, processInstanceId, SIGNAL_NAME);
    }

    @Test
    public void testFindProcessInstanceByInitiator() {
        final Long processInstanceId = startProcess(CONTAINER_ID, PROCESS_WITH_SIGNAL_ID);

        final List<Integer> statuses = Arrays.asList(org.kie.api.runtime.process.ProcessInstance.STATE_ACTIVE);
        final Map<String, Object> parameters = new HashMap<>();
        parameters.put("initiator", INITIATOR);
        parameters.put("status", statuses);
        parameters.put("page", "0");
        parameters.put("pageSize", "10");
        final ExecutionServerCommand executionServerCommand = new ExecutionServerCommand();
        executionServerCommand.setClient("query");
        executionServerCommand.setOperation("findProcessInstancesByInitiator");
        executionServerCommand.setParameters(parameters);
        final Object response = runOnExecutionServer(executionServerCommand);
        Assertions.assertThat(response).isNotNull();
        Assertions.assertThat(response).isInstanceOf(List.class);
        final List<ProcessInstance> processInstances = (List<ProcessInstance>) response;
        Assertions.assertThat(processInstances).isNotEmpty();
        final List<Long> processInstancesIds =
                processInstances.stream().map(p -> p.getId()).collect(Collectors.toList());
        Assertions.assertThat(processInstancesIds).contains(processInstanceId);

        sendSignalToProcessInstance(CONTAINER_ID, processInstanceId, SIGNAL_NAME);
    }

    @Test
    public void testListQueries() {
        registerQuery(getSimpleQueryDefinition());

        final Map<String, Object> parameters = new HashMap<>();
        parameters.put("page", 0);
        parameters.put("pageSize", 10);
        final ExecutionServerCommand executionServerCommand = new ExecutionServerCommand();
        executionServerCommand.setClient("query");
        executionServerCommand.setOperation("getQueries");
        executionServerCommand.setParameters(parameters);

        Object response = runOnExecutionServer(executionServerCommand);
        Assertions.assertThat(response).isNotNull();
        Assertions.assertThat(response).isInstanceOf(List.class);
        List<QueryDefinition> queryDefinitionList = (List<QueryDefinition>) response;
        Assertions.assertThat(queryDefinitionList).isNotEmpty();
        List<String> queryNames = queryDefinitionList.stream().map(q -> q.getName()).collect(Collectors.toList());
        Assertions.assertThat(queryNames).contains(SIMPLE_QUERY_NAME);

        unregisterQuery(SIMPLE_QUERY_NAME);
    }

    private static final QueryDefinition getSimpleQueryDefinition() {
        final QueryDefinition simpleQueryDefinition = new QueryDefinition();
        simpleQueryDefinition.setName(SIMPLE_QUERY_NAME);
        simpleQueryDefinition.setExpression(SIMPLE_QUERY_EXPRESSION);
        simpleQueryDefinition.setTarget(SIMPLE_QUERY_TARGET);
        simpleQueryDefinition.setSource(SIMPLE_QUERY_DATASOURCE);

        return simpleQueryDefinition;
    }

    private QueryDefinition registerQuery(final QueryDefinition queryDefinition) {
        final Map<String, Object> parameters = new HashMap<>();
        parameters.put("queryDefinition", queryDefinition);
        final ExecutionServerCommand executionServerCommand = new ExecutionServerCommand();
        executionServerCommand.setClient("query");
        executionServerCommand.setOperation("registerQuery");
        executionServerCommand.setParameters(parameters);

        Object object = runOnExecutionServer(executionServerCommand);
        Assertions.assertThat(object).isNotNull();
        Assertions.assertThat(object).isInstanceOf(QueryDefinition.class);
        QueryDefinition response = (QueryDefinition) object;

        return response;
    }

    private void unregisterQuery(final String queryName) {
        final Map<String, Object> parameters = new HashMap<>();
        parameters.put("queryName", queryName);
        final ExecutionServerCommand executionServerCommand = new ExecutionServerCommand();
        executionServerCommand.setClient("query");
        executionServerCommand.setOperation("unregisterQuery");
        executionServerCommand.setParameters(parameters);

        runOnExecutionServer(executionServerCommand);
    }
}
