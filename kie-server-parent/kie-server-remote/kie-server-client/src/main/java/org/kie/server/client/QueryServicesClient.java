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

package org.kie.server.client;

import java.util.List;
import java.util.Map;

import org.kie.internal.process.CorrelationKey;
import org.kie.server.api.model.definition.ProcessDefinition;
import org.kie.server.api.model.definition.ProcessInstanceQueryFilterSpec;
import org.kie.server.api.model.definition.QueryDefinition;
import org.kie.server.api.model.definition.QueryFilterSpec;
import org.kie.server.api.model.definition.TaskQueryFilterSpec;
import org.kie.server.api.model.instance.NodeInstance;
import org.kie.server.api.model.instance.ProcessInstance;
import org.kie.server.api.model.instance.TaskInstance;
import org.kie.server.api.model.instance.VariableInstance;
import org.kie.server.client.jms.ResponseHandler;

public interface QueryServicesClient {

    public static final String QUERY_MAP_PI = "ProcessInstances";
    public static final String QUERY_MAP_PI_WITH_VARS = "ProcessInstancesWithVariables";
    public static final String QUERY_MAP_TASK = "UserTasks";
    public static final String QUERY_MAP_TASK_WITH_VARS = "UserTasksWithVariables";
    public static final String QUERY_MAP_RAW = "RawList";
    public static final String QUERY_MAP_TASK_SUMMARY = "TaskSummaries";
    public static final String QUERY_MAP_PI_WITH_CUSTOM_VARS = "ProcessInstancesWithCustomVariables";
    public static final String QUERY_MAP_TASK_WITH_CUSTOM_VARS = "UserTasksWithCustomVariables";
    public static final String QUERY_MAP_ERROR = "ExecutionErrors";
    public static final String QUERY_MAP_TASK_WITH_MODIF = "UserTasksWithModifications";
    public static final String QUERY_MAP_TASK_WITH_PO = "UserTasksWithPotOwners";
    
    public static final String SORT_BY_NAME = "ProcessName";
    public static final String SORT_BY_VERSION = "ProcessVersion";
    public static final String SORT_BY_PROJECT = "Project";

    // runtime data searches
    ProcessDefinition findProcessByContainerIdProcessId(String containerId, String processId);

    List<ProcessDefinition> findProcessesById(String processId);

    List<ProcessDefinition> findProcesses(Integer page, Integer pageSize);

    List<ProcessDefinition> findProcesses(String filter, Integer page, Integer pageSize);

    List<ProcessDefinition> findProcessesByContainerId(String containerId, Integer page, Integer pageSize);

    List<ProcessDefinition> findProcesses(Integer page, Integer pageSize, String sort, boolean sortOrder);

    List<ProcessDefinition> findProcesses(String filter, Integer page, Integer pageSize, String sort, boolean sortOrder);

    List<ProcessDefinition> findProcessesByContainerId(String containerId, Integer page, Integer pageSize, String sort, boolean sortOrder);

    List<ProcessInstance> findProcessInstances(Integer page, Integer pageSize);

    List<ProcessInstance> findProcessInstancesByCorrelationKey(CorrelationKey correlationKey, Integer page, Integer pageSize);

    List<ProcessInstance> findProcessInstancesByProcessId(String processId, List<Integer> status, Integer page, Integer pageSize);

    List<ProcessInstance> findProcessInstancesByProcessName(String processName, List<Integer> status, Integer page, Integer pageSize);

    List<ProcessInstance> findProcessInstancesByContainerId(String containerId, List<Integer> status, Integer page, Integer pageSize);

    List<ProcessInstance> findProcessInstancesByStatus(List<Integer> status, Integer page, Integer pageSize);

    List<ProcessInstance> findProcessInstancesByInitiator(String initiator, List<Integer> status, Integer page, Integer pageSize);

    List<ProcessInstance> findProcessInstancesByVariable(String variableName, List<Integer> status, Integer page, Integer pageSize);

    List<ProcessInstance> findProcessInstancesByVariableAndValue(String variableName, String variableValue, List<Integer> status, Integer page, Integer pageSize);

    List<ProcessInstance> findProcessInstances(Integer page, Integer pageSize, String sort, boolean sortOrder);

    List<ProcessInstance> findProcessInstancesByCorrelationKey(CorrelationKey correlationKey, Integer page, Integer pageSize, String sort, boolean sortOrder);

    List<ProcessInstance> findProcessInstancesByProcessId(String processId, List<Integer> status, Integer page, Integer pageSize, String sort, boolean sortOrder);

    List<ProcessInstance> findProcessInstancesByProcessName(String processName, List<Integer> status, Integer page, Integer pageSize, String sort, boolean sortOrder);

    List<ProcessInstance> findProcessInstancesByContainerId(String containerId, List<Integer> status, Integer page, Integer pageSize, String sort, boolean sortOrder);

    List<ProcessInstance> findProcessInstancesByStatus(List<Integer> status, Integer page, Integer pageSize, String sort, boolean sortOrder);

    List<ProcessInstance> findProcessInstancesByInitiator(String initiator, List<Integer> status, Integer page, Integer pageSize, String sort, boolean sortOrder);

    List<ProcessInstance> findProcessInstancesByVariable(String variableName, List<Integer> status, Integer page, Integer pageSize, String sort, boolean sortOrder);

    List<ProcessInstance> findProcessInstancesByVariableAndValue(String variableName, String variableValue, List<Integer> status, Integer page, Integer pageSize, String sort, boolean sortOrder);

    ProcessInstance findProcessInstanceById(Long processInstanceId);

    ProcessInstance findProcessInstanceById(Long processInstanceId, boolean withVars);

    ProcessInstance findProcessInstanceByCorrelationKey(CorrelationKey correlationKey);

    NodeInstance findNodeInstanceByWorkItemId(Long processInstanceId, Long workItemId);

    List<NodeInstance> findActiveNodeInstances(Long processInstanceId, Integer page, Integer pageSize);

    List<NodeInstance> findCompletedNodeInstances(Long processInstanceId, Integer page, Integer pageSize);

    List<NodeInstance> findNodeInstances(Long processInstanceId, Integer page, Integer pageSize);

    List<VariableInstance> findVariablesCurrentState(Long processInstanceId);

    List<VariableInstance> findVariableHistory(Long processInstanceId, String variableName, Integer page, Integer pageSize);

    // QueryDataService related
    void registerQuery(QueryDefinition queryDefinition);

    void replaceQuery(QueryDefinition queryDefinition);

    void unregisterQuery(String queryName);

    QueryDefinition getQuery(String queryName);

    List<QueryDefinition> getQueries(Integer page, Integer pageSize);

    <T> List<T> query(String queryName, String mapper, Integer page, Integer pageSize, Class<T> resultType);

    <T> List<T> query(String queryName, String mapper, String orderBy, Integer page, Integer pageSize, Class<T> resultType);

    <T> List<T> query(String queryName, String mapper, QueryFilterSpec filterSpec, Integer page, Integer pageSize, Class<T> resultType);

    <T> List<T> query(String queryName, String mapper, String builder, Map<String, Object> parameters, Integer page, Integer pageSize, Class<T> resultType);

    List<ProcessInstance> findProcessInstancesWithFilters(String queryName, ProcessInstanceQueryFilterSpec filterSpec, Integer page, Integer pageSize);
    
    List<TaskInstance> findHumanTasksWithFilters(String queryName, TaskQueryFilterSpec filterSpec, Integer page, Integer pageSize);
    
    void setResponseHandler(ResponseHandler responseHandler);
}
