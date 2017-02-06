/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
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

package org.kie.server.services.jbpm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jbpm.services.api.model.NodeInstanceDesc;
import org.jbpm.services.api.model.ProcessDefinition;
import org.jbpm.services.api.model.ProcessInstanceDesc;
import org.jbpm.services.api.model.ProcessInstanceWithVarsDesc;
import org.jbpm.services.api.model.UserTaskInstanceDesc;
import org.jbpm.services.api.model.UserTaskInstanceWithVarsDesc;
import org.jbpm.services.api.model.VariableDesc;
import org.kie.api.runtime.query.QueryContext;
import org.kie.api.task.model.Status;
import org.kie.api.task.model.TaskSummary;
import org.kie.internal.query.QueryFilter;
import org.kie.server.api.model.definition.ProcessDefinitionList;
import org.kie.server.api.model.definition.QueryDefinition;
import org.kie.server.api.model.definition.QueryDefinitionList;
import org.kie.server.api.model.instance.NodeInstance;
import org.kie.server.api.model.instance.NodeInstanceList;
import org.kie.server.api.model.instance.ProcessInstance;
import org.kie.server.api.model.instance.ProcessInstanceList;
import org.kie.server.api.model.instance.TaskInstance;
import org.kie.server.api.model.instance.TaskInstanceList;
import org.kie.server.api.model.instance.TaskSummaryList;
import org.kie.server.api.model.instance.VariableInstance;
import org.kie.server.api.model.instance.VariableInstanceList;

public class ConvertUtils {

    public static ProcessInstanceList convertToProcessInstanceList(Collection<ProcessInstanceDesc> instances) {
        if (instances == null) {
            return new ProcessInstanceList(new org.kie.server.api.model.instance.ProcessInstance[0]);
        }

        List<ProcessInstance> processInstances = new ArrayList<ProcessInstance>(instances.size());
        for (ProcessInstanceDesc pi : instances) {
            org.kie.server.api.model.instance.ProcessInstance instance = convertToProcessInstance(pi);

            processInstances.add(instance);
        }

        return new ProcessInstanceList(processInstances);
    }

    public static ProcessInstanceList convertToProcessInstanceWithVarsList(Collection<ProcessInstanceWithVarsDesc> instances) {
        if (instances == null) {
            return new ProcessInstanceList(new org.kie.server.api.model.instance.ProcessInstance[0]);
        }

        List<ProcessInstance> processInstances = new ArrayList<ProcessInstance>(instances.size());
        for (ProcessInstanceWithVarsDesc pi : instances) {
            org.kie.server.api.model.instance.ProcessInstance instance = convertToProcessInstance(pi);

            instance.setVariables(pi.getVariables());

            processInstances.add(instance);
        }

        return new ProcessInstanceList(processInstances);
    }

    public static org.kie.server.api.model.instance.ProcessInstance convertToProcessInstance(ProcessInstanceDesc pi) {
        if (pi == null) {
            return null;
        }

        org.kie.server.api.model.instance.ProcessInstance instance = org.kie.server.api.model.instance.ProcessInstance.builder()
                .id(pi.getId())
                .processId(pi.getProcessId())
                .processName(pi.getProcessName())
                .processVersion(pi.getProcessVersion())
                .containerId(pi.getDeploymentId())
                .processInstanceDescription(pi.getProcessInstanceDescription())
                .correlationKey(pi.getCorrelationKey())
                .parentInstanceId(pi.getParentId())
                .date(pi.getDataTimeStamp())
                .initiator(pi.getInitiator())
                .state(pi.getState())
                .build();

        if (pi.getActiveTasks() != null && !pi.getActiveTasks().isEmpty()) {
            org.kie.server.api.model.instance.TaskSummary[] tasks = new org.kie.server.api.model.instance.TaskSummary[pi.getActiveTasks().size()];
            int counter = 0;
            for (UserTaskInstanceDesc taskSummary : pi.getActiveTasks()) {
                org.kie.server.api.model.instance.TaskSummary task = org.kie.server.api.model.instance.TaskSummary.builder()
                        .id(taskSummary.getTaskId())
                        .name(taskSummary.getName())
                        .description(taskSummary.getDescription())
                        .activationTime(taskSummary.getActivationTime())
                        .actualOwner(taskSummary.getActualOwner())
                        .containerId(taskSummary.getDeploymentId())
                        .createdBy(taskSummary.getCreatedBy())
                        .createdOn(taskSummary.getCreatedOn())
                        .priority(taskSummary.getPriority())
                        .processId(taskSummary.getProcessId())
                        .processInstanceId(taskSummary.getProcessInstanceId())
                        .status(taskSummary.getStatus())
                        .build();
                tasks[counter] = task;
                counter++;
            }
            instance.setActiveUserTasks(new TaskSummaryList(tasks));
        }

        return instance;
    }

    public static ProcessDefinitionList convertToProcessList(Collection<ProcessDefinition> definitions) {
        if (definitions == null) {
            return new ProcessDefinitionList(new org.kie.server.api.model.definition.ProcessDefinition[0]);
        }

        List<org.kie.server.api.model.definition.ProcessDefinition> processes = new ArrayList<org.kie.server.api.model.definition.ProcessDefinition>(definitions.size());
        for (ProcessDefinition pd : definitions) {
            org.kie.server.api.model.definition.ProcessDefinition definition = convertToProcess(pd);

            processes.add(definition);
        }

        return new ProcessDefinitionList(processes);
    }

    public static org.kie.server.api.model.definition.ProcessDefinition convertToProcess(ProcessDefinition processDesc) {
        if (processDesc == null) {
            return null;
        }

        org.kie.server.api.model.definition.ProcessDefinition processDefinition = org.kie.server.api.model.definition.ProcessDefinition.builder()
                .id(processDesc.getId())
                .name(processDesc.getName())
                .packageName(processDesc.getPackageName())
                .version(processDesc.getVersion())
                .containerId(processDesc.getDeploymentId())
                .dynamic(processDesc.isDynamic())
                .build();

        return processDefinition;
    }

    public static QueryContext buildQueryContext(Integer page, Integer pageSize) {
        return new QueryContext(page * pageSize, pageSize);
    }

    public static QueryContext buildQueryContext(Integer page, Integer pageSize, String orderBy, boolean asc) {
        if (orderBy != null && !orderBy.isEmpty()) {
            return new QueryContext(page * pageSize, pageSize, orderBy, asc);
        }

        return new QueryContext(page * pageSize, pageSize);
    }

    public static QueryFilter buildQueryFilter(Integer page, Integer pageSize) {
        return new QueryFilter(page * pageSize, pageSize);
    }

    public static QueryFilter buildQueryFilter(Integer page, Integer pageSize, String orderBy, boolean asc) {
        QueryFilter queryFilter = null;
        if (orderBy != null && !orderBy.isEmpty()) {
            queryFilter = new QueryFilter(page * pageSize, pageSize, orderBy, asc);
        } else {
            queryFilter = new QueryFilter(page * pageSize, pageSize);
        }

        return queryFilter;
    }

    public static QueryFilter buildTaskByNameQueryFilter(Integer page, Integer pageSize, String orderBy, boolean asc, String filter) {
        QueryFilter queryFilter = null;
        if (orderBy != null && !orderBy.isEmpty()) {
            queryFilter = new QueryFilter(page * pageSize, pageSize, orderBy, asc);
        } else {
            queryFilter = new QueryFilter(page * pageSize, pageSize);
        }

        if (filter != null && !filter.isEmpty()) {
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("taskName", filter);

            queryFilter.setFilterParams("t.name like :taskName");
            queryFilter.setParams(params);
        }

        return queryFilter;
    }

    public static List<Status> buildTaskStatuses(List<String> status) {
        if (status == null || status.isEmpty()) {
            return null;
        }

        List<Status> taskStatuses = new ArrayList<Status>();

        for (String s : status) {
            taskStatuses.add(Status.valueOf(s));
        }

        return taskStatuses;
    }


    public static NodeInstance convertToNodeInstance(NodeInstanceDesc nodeInstanceDesc) {

        NodeInstance nodeInstance = NodeInstance.builder()
                .id(nodeInstanceDesc.getId())
                .name(nodeInstanceDesc.getName())
                .nodeId(nodeInstanceDesc.getNodeId())
                .nodeType(nodeInstanceDesc.getNodeType())
                .processInstanceId(nodeInstanceDesc.getProcessInstanceId())
                .containerId(nodeInstanceDesc.getDeploymentId())
                .workItemId(nodeInstanceDesc.getWorkItemId())
                .completed(nodeInstanceDesc.isCompleted())
                .connection(nodeInstanceDesc.getConnection())
                .date(nodeInstanceDesc.getDataTimeStamp())
                .build();

        return nodeInstance;

    }

    public static NodeInstanceList convertToNodeInstanceList(Collection<NodeInstanceDesc> definitions) {
        if (definitions == null) {
            return new NodeInstanceList(new NodeInstance[0]);
        }

        List<NodeInstance> processes = new ArrayList<NodeInstance>(definitions.size());
        for (NodeInstanceDesc ni : definitions) {
            NodeInstance nodeInstance = convertToNodeInstance(ni);

            processes.add(nodeInstance);
        }

        return new NodeInstanceList(processes);
    }

    public static VariableInstance convertToVariable(VariableDesc variableDesc) {
        VariableInstance instance = VariableInstance.builder()
                .name(variableDesc.getVariableId())
                .processInstanceId(variableDesc.getProcessInstanceId())
                .value(variableDesc.getNewValue())
                .oldValue(variableDesc.getOldValue())
                .date(variableDesc.getDataTimeStamp())
                .build();

        return instance;
    }

    public static VariableInstanceList convertToVariablesList(Collection<VariableDesc> variables) {
        if (variables == null) {
            return new VariableInstanceList(new VariableInstance[0]);
        }

        List<VariableInstance> processes = new ArrayList<VariableInstance>(variables.size());
        for (VariableDesc vi : variables) {
            VariableInstance nodeInstance = convertToVariable(vi);

            processes.add(nodeInstance);
        }

        return new VariableInstanceList(processes);
    }

    public static TaskInstanceList convertToTaskInstanceWithVarsList(Collection<UserTaskInstanceWithVarsDesc> instances) {
        if (instances == null) {
            return new TaskInstanceList(new org.kie.server.api.model.instance.TaskInstance[0]);
        }

        List<TaskInstance> taskInstances = new ArrayList<TaskInstance>(instances.size());
        for (UserTaskInstanceWithVarsDesc task : instances) {
            org.kie.server.api.model.instance.TaskInstance instance = convertToTask(task);

            instance.setInputData(task.getVariables());

            taskInstances.add(instance);
        }

        return new TaskInstanceList(taskInstances);
    }

    public static TaskInstanceList convertToTaskInstanceList(Collection<UserTaskInstanceDesc> instances) {
        if (instances == null) {
            return new TaskInstanceList(new org.kie.server.api.model.instance.TaskInstance[0]);
        }

        List<TaskInstance> taskInstances = new ArrayList<TaskInstance>(instances.size());
        for (UserTaskInstanceDesc task : instances) {
            org.kie.server.api.model.instance.TaskInstance instance = convertToTask(task);
            taskInstances.add(instance);
        }

        return new TaskInstanceList(taskInstances);
    }

    public static TaskInstance convertToTask(UserTaskInstanceDesc userTask) {

        TaskInstance instance = TaskInstance.builder()
                .id(userTask.getTaskId())
                .name(userTask.getName())
                .processInstanceId(userTask.getProcessInstanceId())
                .processId(userTask.getProcessId())
                .activationTime(userTask.getActivationTime())
                .actualOwner(userTask.getActualOwner())
                .containerId(userTask.getDeploymentId())
                .createdBy(userTask.getCreatedBy())
                .createdOn(userTask.getCreatedOn())
                .description(userTask.getDescription())
                .expirationTime(userTask.getDueDate())
                .status(userTask.getStatus())
                .priority(userTask.getPriority())
                .build();

        return instance;
    }

    public static TaskSummaryList convertToTaskSummaryList(Collection<TaskSummary> tasks) {
        if (tasks == null) {
            return new TaskSummaryList(new org.kie.server.api.model.instance.TaskSummary[0]);
        }
        org.kie.server.api.model.instance.TaskSummary[] instances = new org.kie.server.api.model.instance.TaskSummary[tasks.size()];
        int counter = 0;
        for (TaskSummary taskSummary : tasks) {

            instances[counter] = convertToTaskSummary(taskSummary);
            counter++;
        }

        return new TaskSummaryList(instances);
    }

    public static org.kie.server.api.model.instance.TaskSummary convertToTaskSummary(TaskSummary taskSummary) {
        org.kie.server.api.model.instance.TaskSummary task = org.kie.server.api.model.instance.TaskSummary.builder()
                .id(taskSummary.getId())
                .name(taskSummary.getName())
                .description(taskSummary.getDescription())
                .subject(taskSummary.getSubject())
                .taskParentId(taskSummary.getParentId())
                .activationTime(taskSummary.getActivationTime())
                .actualOwner(taskSummary.getActualOwnerId())
                .containerId(taskSummary.getDeploymentId())
                .createdBy(taskSummary.getCreatedById())
                .createdOn(taskSummary.getCreatedOn())
                .expirationTime(taskSummary.getExpirationTime())
                .priority(taskSummary.getPriority())
                .processId(taskSummary.getProcessId())
                .processInstanceId(taskSummary.getProcessInstanceId())
                .status(taskSummary.getStatusId())
                .skipable(taskSummary.isSkipable())
                .build();
        return task;
    }

    public static QueryDefinition convertQueryDefinition(org.jbpm.services.api.query.model.QueryDefinition queryDefinition) {
        if (queryDefinition == null) {
            return null;
        }

        QueryDefinition query = QueryDefinition.builder()
                                .name(queryDefinition.getName())
                                .expression(queryDefinition.getExpression())
                                .source(queryDefinition.getSource())
                                .target(queryDefinition.getTarget().toString())
                                .build();
        return query;
    }

    public static QueryDefinitionList convertToQueryDefinitionList(Collection<org.jbpm.services.api.query.model.QueryDefinition> definitions) {
        if (definitions == null) {
            return new QueryDefinitionList(new QueryDefinition[0]);
        }
        QueryDefinition[] instances = new QueryDefinition[definitions.size()];
        int counter = 0;
        for (org.jbpm.services.api.query.model.QueryDefinition queryDefinition : definitions) {

            instances[counter] = convertQueryDefinition(queryDefinition);
            counter++;
        }

        return new QueryDefinitionList(instances);
    }

    public static String nullEmpty(String value) {
        if (value != null && value.isEmpty()) {
            return null;
        }

        return value;
    }
}
