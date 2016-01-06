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

package org.kie.server.services.jbpm;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jbpm.services.api.ProcessInstanceNotFoundException;
import org.jbpm.services.api.RuntimeDataService;
import org.jbpm.services.api.model.NodeInstanceDesc;
import org.jbpm.services.api.model.ProcessDefinition;
import org.jbpm.services.api.model.ProcessInstanceDesc;
import org.jbpm.services.api.model.UserTaskInstanceDesc;
import org.jbpm.services.api.model.VariableDesc;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.api.task.model.Status;
import org.kie.api.task.model.TaskSummary;
import org.kie.internal.KieInternalServices;
import org.kie.internal.identity.IdentityProvider;
import org.kie.internal.process.CorrelationKey;
import org.kie.internal.process.CorrelationKeyFactory;
import org.kie.api.runtime.query.QueryContext;
import org.kie.internal.query.QueryFilter;
import org.kie.internal.task.api.AuditTask;
import org.kie.internal.task.api.model.TaskEvent;
import org.kie.server.api.KieServerConstants;
import org.kie.server.api.model.definition.ProcessDefinitionList;
import org.kie.server.api.model.instance.NodeInstance;
import org.kie.server.api.model.instance.NodeInstanceList;
import org.kie.server.api.model.instance.ProcessInstanceList;
import org.kie.server.api.model.instance.TaskEventInstance;
import org.kie.server.api.model.instance.TaskEventInstanceList;
import org.kie.server.api.model.instance.TaskInstance;
import org.kie.server.api.model.instance.TaskSummaryList;
import org.kie.server.api.model.instance.VariableInstance;
import org.kie.server.api.model.instance.VariableInstanceList;
import org.kie.server.services.api.KieServerRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RuntimeDataServiceBase {

    public static final Logger logger = LoggerFactory.getLogger(RuntimeDataServiceBase.class);

    private RuntimeDataService runtimeDataService;
    private IdentityProvider identityProvider;

    private boolean bypassAuthUser;

    private CorrelationKeyFactory correlationKeyFactory = KieInternalServices.Factory.get().newCorrelationKeyFactory();

    public RuntimeDataServiceBase(RuntimeDataService delegate, KieServerRegistry context) {
        this.runtimeDataService = delegate;
        this.identityProvider = context.getIdentityProvider();

        this.bypassAuthUser = Boolean.parseBoolean(context.getConfig().getConfigItemValue(KieServerConstants.CFG_BYPASS_AUTH_USER, "false"));
    }

    protected String getUser(String queryParamUser) {
        if (bypassAuthUser) {
            return queryParamUser;
        }

        return identityProvider.getName();
    }

    public ProcessInstanceList getProcessInstances(List<Integer> status, String initiator, String processName, Integer page, Integer pageSize) {

        if (status == null || status.isEmpty()) {
            status = new ArrayList<Integer>();
            status.add(ProcessInstance.STATE_ACTIVE);
        }
        Collection<ProcessInstanceDesc> instances = null;

        if (processName != null && !processName.isEmpty()) {
            logger.debug("About to search for process instances with process name '{}' with page {} and page size {}", processName, page, pageSize);

            instances = runtimeDataService.getProcessInstancesByProcessName(status, processName, nullEmpty(initiator), buildQueryContext(page, pageSize, "ProcessInstanceId", true));
            logger.debug("Found {} process instances for process name '{}', statuses '{}'", instances.size(), processName, status);
        } else {
            logger.debug("About to search for process instances with page {} and page size {}", page, pageSize);
            instances = runtimeDataService.getProcessInstances(status, nullEmpty(initiator), buildQueryContext(page, pageSize, "ProcessInstanceId", true));

            logger.debug("Found {} process instances , statuses '{}'", instances.size(), status);
        }
        ProcessInstanceList processInstanceList = convertToProcessInstanceList(instances);
        logger.debug("Returning result of process instance search: {}", processInstanceList);

        return processInstanceList;
    }

    public ProcessInstanceList getProcessInstancesByProcessId(String processId, List<Integer> status, String initiator, Integer page, Integer pageSize) {

        if (status == null || status.isEmpty()) {
            status = new ArrayList<Integer>();
            status.add(ProcessInstance.STATE_ACTIVE);
        }
        logger.debug("About to search for process instances with process id '{}' with page {} and page size {}", processId, page, pageSize);

        Collection<ProcessInstanceDesc> instances = runtimeDataService.getProcessInstancesByProcessId(status, processId, nullEmpty(initiator), buildQueryContext(page, pageSize, "ProcessInstanceId", true));
        logger.debug("Found {} process instance for process id '{}', statuses '{}'", instances.size(), processId, status);

        ProcessInstanceList processInstanceList = convertToProcessInstanceList(instances);
        logger.debug("Returning result of process instance search: {}", processInstanceList);

        return processInstanceList;
    }


    public ProcessInstanceList getProcessInstancesByDeploymentId(String containerId, List<Integer> status, Integer page, Integer pageSize) {

        if (status == null || status.isEmpty()) {
            status = new ArrayList<Integer>();
            status.add(ProcessInstance.STATE_ACTIVE);
        }
        logger.debug("About to search for process instance belonging to container '{}' with page {} and page size {}", containerId, page, pageSize);

        Collection<ProcessInstanceDesc> instances = runtimeDataService.getProcessInstancesByDeploymentId(containerId, status, buildQueryContext(page, pageSize, "ProcessInstanceId", true));
        logger.debug("Found {} process instance for container '{}', statuses '{}'", instances.size(), containerId, status);

        ProcessInstanceList processInstanceList = convertToProcessInstanceList(instances);
        logger.debug("Returning result of process instance search: {}", processInstanceList);

        return processInstanceList;
    }


    public ProcessInstanceList getProcessInstancesByCorrelationKey(String correlationKey, Integer page, Integer pageSize) {

        String[] correlationProperties = correlationKey.split(":");

        CorrelationKey actualCorrelationKey = correlationKeyFactory.newCorrelationKey(Arrays.asList(correlationProperties));

        Collection<ProcessInstanceDesc> instances = runtimeDataService.getProcessInstancesByCorrelationKey(actualCorrelationKey, buildQueryContext(page, pageSize, "ProcessInstanceId", true));

        ProcessInstanceList processInstanceList = convertToProcessInstanceList(instances);
        logger.debug("Returning result of process instance search: {}", processInstanceList);

        return processInstanceList;
    }

    public org.kie.server.api.model.instance.ProcessInstance getProcessInstanceByCorrelationKey(String correlationKey) {

        String[] correlationProperties = correlationKey.split(":");

        CorrelationKey actualCorrelationKey = correlationKeyFactory.newCorrelationKey(Arrays.asList(correlationProperties));

        ProcessInstanceDesc instance = runtimeDataService.getProcessInstanceByCorrelationKey(actualCorrelationKey);

        org.kie.server.api.model.instance.ProcessInstance processInstanceList = convertToProcessInstance(instance);
        logger.debug("Returning result of process instance search: {}", instance);

        return processInstanceList;
    }


    public ProcessInstanceList getProcessInstanceByVariables(String variableName, String variableValue, List<Integer> status, Integer page, Integer pageSize) {

        Collection<ProcessInstanceDesc> instances = null;
        if (variableValue != null && !variableValue.isEmpty()) {
            logger.debug("About to search for process instance that has variable '{}' with value '{}' with page {} and page size {}", variableName, variableValue, page, pageSize);

            instances = runtimeDataService.getProcessInstancesByVariableAndValue(variableName, variableValue, status, buildQueryContext(page, pageSize, "ProcessInstanceId", true));
            logger.debug("Found {} process instance with variable {} and variable value {}", instances.size(), variableName, variableValue);
        } else {
            logger.debug("About to search for process instance that has variable '{}' with page {} and page size {}", variableName, page, pageSize);

            instances = runtimeDataService.getProcessInstancesByVariable(variableName, status, buildQueryContext(page, pageSize, "ProcessInstanceId", true));
            logger.debug("Found {} process instance with variable {} ", instances.size(), variableName);
        }

        ProcessInstanceList processInstanceList = convertToProcessInstanceList(instances);
        logger.debug("Returning result of process instance search: {}", processInstanceList);

        return processInstanceList;
    }

    public org.kie.server.api.model.instance.ProcessInstance getProcessInstanceById(long processInstanceId, boolean withVars) {

        ProcessInstanceDesc processInstanceDesc = runtimeDataService.getProcessInstanceById(processInstanceId);
        if (processInstanceDesc == null) {
            throw new ProcessInstanceNotFoundException("Could not find process instance with id " + processInstanceId);
        }
        
        org.kie.server.api.model.instance.ProcessInstance processInstance = convertToProcessInstance(processInstanceDesc);
        
        if (Boolean.TRUE.equals(withVars)) {
            Collection<VariableDesc> variableDescs = runtimeDataService.getVariablesCurrentState(processInstanceId);
            Map<String, Object> vars = new HashMap<String, Object>();
            for (VariableDesc var : variableDescs) {
                vars.put(var.getVariableId(), var.getNewValue());
            }
            processInstance.setVariables(vars);
        }
        
        return processInstance;
    }


    public NodeInstance getNodeInstanceForWorkItem(long processInstanceId, long workItemId) {

        NodeInstanceDesc nodeInstanceDesc = runtimeDataService.getNodeInstanceForWorkItem(workItemId);
        if (nodeInstanceDesc == null) {
            throw new IllegalArgumentException("Could not find node instance with id \""+workItemId+"\" within process instance with id \""+processInstanceId +"\"");
        }
        return convertToNodeInstance(nodeInstanceDesc);
    }


    public NodeInstanceList getProcessInstanceHistory(long processInstanceId, Boolean active, Boolean completed, Integer page, Integer pageSize) {

        logger.debug("About to search for node instances with page {} and page size {}", page, pageSize);
        Collection<NodeInstanceDesc> result = null;

        if ((Boolean.TRUE.equals(active) && Boolean.TRUE.equals(completed)) || (active == null && completed == null)) {
            logger.debug("Searching for active and completed node instances for process instance with id {}", processInstanceId);
            result = runtimeDataService.getProcessInstanceFullHistory(processInstanceId, buildQueryContext(page, pageSize));
        } else if (Boolean.TRUE.equals(active)) {
            logger.debug("Searching for active node instances for process instance with id {}", processInstanceId);
            result = runtimeDataService.getProcessInstanceHistoryActive(processInstanceId, buildQueryContext(page, pageSize));
        } else if (Boolean.TRUE.equals(completed)) {
            logger.debug("Searching for completed node instances for process instance with id {}", processInstanceId);
            result = runtimeDataService.getProcessInstanceHistoryCompleted(processInstanceId, buildQueryContext(page, pageSize));
        }

        NodeInstanceList nodeInstanceList = convertToNodeInstanceList(result);
        logger.debug("Returning result of node instances search: {}", nodeInstanceList);
        return nodeInstanceList;
    }

    public VariableInstanceList getVariablesCurrentState(long processInstanceId) {
        logger.debug("About to search for variables within process instance  '{}'", processInstanceId);

        Collection<VariableDesc> variableDescs = runtimeDataService.getVariablesCurrentState(processInstanceId);
        logger.debug("Found {} variables within process instance '{}'", variableDescs.size(), processInstanceId);

        VariableInstanceList variableInstanceList = convertToVariablesList(variableDescs);
        logger.debug("Returning result of variables search: {}", variableInstanceList);

        return variableInstanceList;
    }


    public VariableInstanceList getVariableHistory(long processInstanceId,  String variableName, Integer page, Integer pageSize) {
        logger.debug("About to search for variable '{}; history within process instance '{}' with page {} and page size {}", variableName, processInstanceId, page, pageSize);

        Collection<VariableDesc> variableDescs = runtimeDataService.getVariableHistory(processInstanceId, variableName, buildQueryContext(page, pageSize));
        logger.debug("Found {} variable {} history entries within process instance '{}'", variableDescs.size(), variableName, processInstanceId);

        VariableInstanceList variableInstanceList = convertToVariablesList(variableDescs);
        logger.debug("Returning result of variable '{}; history search: {}", variableName, variableInstanceList);

        return variableInstanceList;
    }


    public ProcessDefinitionList getProcessesByDeploymentId(String containerId, Integer page, Integer pageSize) {

        logger.debug("About to search for process definitions within container '{}' with page {} and page size {}", containerId, page, pageSize);

        Collection<ProcessDefinition> definitions = runtimeDataService.getProcessesByDeploymentId(containerId, buildQueryContext(page, pageSize, "ProcessName", true));
        logger.debug("Found {} process definitions within container '{}'", definitions.size(), containerId);

        ProcessDefinitionList processDefinitionList = convertToProcessList(definitions);
        logger.debug("Returning result of process definition search: {}", processDefinitionList);

        return processDefinitionList;

    }

    public ProcessDefinitionList getProcessesByFilter(String filter, Integer page, Integer pageSize) {

        Collection<ProcessDefinition> definitions;

        if (filter != null && !filter.isEmpty()) {
            logger.debug("About to search for process definitions with filter '{}' with page {} and page size {}", filter, page, pageSize);

            definitions = runtimeDataService.getProcessesByFilter(filter, buildQueryContext(page, pageSize, "ProcessName", true));
            logger.debug("Found {} process definitions with filter '{}'", definitions.size(), filter);
        } else {
            logger.debug("About to search for process definitions with page {} and page size {}", page, pageSize);

            definitions = runtimeDataService.getProcesses(buildQueryContext(page, pageSize, "ProcessName", true));
            logger.debug("Found {} process definitions", definitions.size(), filter);
        }

        ProcessDefinitionList processDefinitionList = convertToProcessList(definitions);
        logger.debug("Returning result of process definition search: {}", processDefinitionList);

        return processDefinitionList;
    }

    public ProcessDefinitionList getProcessesById(String processId) {

        Collection<ProcessDefinition> definitions = runtimeDataService.getProcessesById(processId);

        ProcessDefinitionList processDefinitionList = convertToProcessList(definitions);
        logger.debug("Returning result of process definition search: {}", processDefinitionList);

        return processDefinitionList;
    }

    public org.kie.server.api.model.definition.ProcessDefinition getProcessesByDeploymentIdProcessId(String containerId, String processId) {
        ProcessDefinition processDesc = runtimeDataService.getProcessesByDeploymentIdProcessId(containerId, processId);
        if (processDesc == null) {
            throw new IllegalArgumentException("Could not find process definition \""+processId+"\" in container \""+containerId +"\"");
        }
        return convertToProcess(processDesc);
    }

    public TaskInstance getTaskByWorkItemId(long workItemId) {

        UserTaskInstanceDesc userTaskDesc = runtimeDataService.getTaskByWorkItemId(workItemId);

        return convertToTask(userTaskDesc);
    }

    public TaskInstance getTaskById(long taskId) {

        UserTaskInstanceDesc userTaskDesc = runtimeDataService.getTaskById(taskId);

        return convertToTask(userTaskDesc);
    }



    public TaskSummaryList getTasksAssignedAsBusinessAdministratorByStatus(List<String> status, String userId, Integer page, Integer pageSize) {

        userId = getUser(userId);
        logger.debug("About to search for task assigned as business admin for user '{}'", userId);
        List<TaskSummary> tasks;
        if (status == null || status.isEmpty()) {
            tasks = runtimeDataService.getTasksAssignedAsBusinessAdministrator(userId, buildQueryFilter(page, pageSize));
        } else {
            List<Status> taskStatuses = buildTaskStatuses(status);

            tasks = runtimeDataService.getTasksAssignedAsBusinessAdministratorByStatus(userId, taskStatuses, buildQueryFilter(page, pageSize));
        }

        logger.debug("Found {} tasks for user '{}' assigned as business admin", tasks.size(), userId);
        TaskSummaryList result = convertToTaskSummaryList(tasks);

        return result;

    }

    public TaskSummaryList getTasksAssignedAsPotentialOwner(List<String> status,  List<String> groupIds, String userId, Integer page, Integer pageSize ) {

        List<Status> taskStatuses = buildTaskStatuses(status);

        userId = getUser(userId);
        logger.debug("About to search for task assigned as potential owner for user '{}'", userId);
        List<TaskSummary> tasks;

        if (groupIds != null && !groupIds.isEmpty()) {

            if (taskStatuses == null) {
                tasks = runtimeDataService.getTasksAssignedAsPotentialOwner(userId, groupIds, buildQueryFilter(page, pageSize));
            } else {
                tasks = runtimeDataService.getTasksAssignedAsPotentialOwner(userId, groupIds, taskStatuses, buildQueryFilter(page, pageSize));
            }
        } else if (taskStatuses != null) {
            tasks = runtimeDataService.getTasksAssignedAsPotentialOwnerByStatus(userId, taskStatuses, buildQueryFilter(page, pageSize));
        } else {

            tasks = runtimeDataService.getTasksAssignedAsPotentialOwner(userId, buildQueryFilter(page, pageSize));
        }

        logger.debug("Found {} tasks for user '{}' assigned as potential owner", tasks.size(), userId);
        TaskSummaryList result = convertToTaskSummaryList(tasks);

        return result;


    }

    public TaskSummaryList getTasksOwnedByStatus(List<String> status, String userId, Integer page, Integer pageSize) {

        List<Status> taskStatuses = buildTaskStatuses(status);

        userId = getUser(userId);
        logger.debug("About to search for task owned user '{}'", userId);
        List<TaskSummary> tasks;

         if (taskStatuses != null) {
            tasks = runtimeDataService.getTasksOwnedByStatus(userId, taskStatuses, buildQueryFilter(page, pageSize));
        } else {

            tasks = runtimeDataService.getTasksOwned(userId, buildQueryFilter(page, pageSize));
        }

        logger.debug("Found {} tasks owned by user '{}'", tasks.size(), userId);
        TaskSummaryList result = convertToTaskSummaryList(tasks);

        return result;
    }

    public TaskSummaryList getTasksByStatusByProcessInstanceId(Number processInstanceId, List<String> status, Integer page, Integer pageSize) {

        List<Status> taskStatuses = buildTaskStatuses(status);
        if (taskStatuses == null) {
            taskStatuses = new ArrayList<Status>();
            taskStatuses.add(Status.Ready);
            taskStatuses.add(Status.Reserved);
            taskStatuses.add(Status.InProgress);
        }

        logger.debug("About to search for tasks attached to process instance with id '{}'", processInstanceId);
        List<TaskSummary> tasks = runtimeDataService.getTasksByStatusByProcessInstanceId(processInstanceId.longValue(), taskStatuses, buildQueryFilter(page, pageSize));


        logger.debug("Found {} tasks attached to process instance with id '{}'", tasks.size(), processInstanceId);
        TaskSummaryList result = convertToTaskSummaryList(tasks);

        return result;
    }

    public TaskSummaryList getAllAuditTask(String userId, Integer page, Integer pageSize) {

        userId = getUser(userId);
        logger.debug("About to search for tasks available for user '{}'", userId);
        List<AuditTask> tasks = runtimeDataService.getAllAuditTask(userId, buildQueryFilter(page, pageSize));


        logger.debug("Found {} tasks available for user '{}'", tasks.size(), userId);
        TaskSummaryList result = null;
        if (tasks == null) {
            result = new TaskSummaryList(new org.kie.server.api.model.instance.TaskSummary[0]);
        } else {
            org.kie.server.api.model.instance.TaskSummary[] instances = new org.kie.server.api.model.instance.TaskSummary[tasks.size()];
            int counter = 0;
            for (AuditTask taskSummary : tasks) {

                org.kie.server.api.model.instance.TaskSummary task = org.kie.server.api.model.instance.TaskSummary.builder()
                        .id(taskSummary.getTaskId())
                        .name(taskSummary.getName())
                        .description(taskSummary.getDescription())
                        .taskParentId(taskSummary.getParentId())
                        .activationTime(taskSummary.getActivationTime())
                        .actualOwner(taskSummary.getActualOwner())
                        .containerId(taskSummary.getDeploymentId())
                        .createdBy(taskSummary.getCreatedBy())
                        .createdOn(taskSummary.getCreatedOn())
                        .expirationTime(taskSummary.getDueDate())
                        .priority(taskSummary.getPriority())
                        .processId(taskSummary.getProcessId())
                        .processInstanceId(taskSummary.getProcessInstanceId())
                        .status(taskSummary.getStatus())
                        .build();
                instances[counter] = task;
                counter++;
            }
            result = new TaskSummaryList(instances);
        }


        return result;
    }


    public TaskEventInstanceList getTaskEvents(long taskId, Integer page, Integer pageSize) {

        logger.debug("About to search for task {} events", taskId);
        List<TaskEvent> tasks = runtimeDataService.getTaskEvents(taskId, buildQueryFilter(page, pageSize, "Id", true));


        logger.debug("Found {} task events available for task '{}'", tasks.size(), taskId);
        TaskEventInstanceList result = null;
        if (tasks == null) {
            result = new TaskEventInstanceList(new TaskEventInstance[0]);
        } else {
            TaskEventInstance[] instances = new TaskEventInstance[tasks.size()];
            int counter = 0;
            for (TaskEvent taskSummary : tasks) {

                TaskEventInstance task = TaskEventInstance.builder()
                        .id(taskSummary.getTaskId())
                        .taskId(taskSummary.getTaskId())
                        .date(taskSummary.getLogTime())
                        .processInstanceId(taskSummary.getProcessInstanceId())
                        .type(taskSummary.getType().toString())
                        .user(taskSummary.getUserId())
                        .workItemId(taskSummary.getWorkItemId())
                        .build();
                instances[counter] = task;
                counter++;
            }
            result = new TaskEventInstanceList(instances);
        }


        return result;

    }

    public TaskSummaryList getTasksByVariables(String userId, String variableName, String variableValue, List<String> status, Integer page, Integer pageSize) {

        userId = getUser(userId);
        List<Status> taskStatuses = buildTaskStatuses(status);
        if (taskStatuses == null) {
            taskStatuses = new ArrayList<Status>();
            taskStatuses.add(Status.Ready);
            taskStatuses.add(Status.Reserved);
            taskStatuses.add(Status.InProgress);
        }

        List<TaskSummary> instances = null;
        if (variableValue != null && !variableValue.isEmpty()) {
            logger.debug("About to search for tasks that has variable '{}' with value '{}' with page {} and page size {}", variableName, variableValue, page, pageSize);

            instances = runtimeDataService.getTasksByVariableAndValue(userId, variableName, variableValue, taskStatuses, buildQueryContext(page, pageSize));
            logger.debug("Found {} tasks with variable {} and variable value {}", instances.size(), variableName, variableValue);
        } else {
            logger.debug("About to search for tasks that has variable '{}' with page {} and page size {}", variableName, page, pageSize);

            instances = runtimeDataService.getTasksByVariable(userId, variableName, taskStatuses, buildQueryContext(page, pageSize));
            logger.debug("Found {} tasks with variable {} ", instances.size(), variableName);
        }

        TaskSummaryList taskSummaryList = convertToTaskSummaryList(instances);
        logger.debug("Returning result of task by variable search: {}", taskSummaryList);

        return taskSummaryList;
    }

    protected ProcessInstanceList convertToProcessInstanceList(Collection<ProcessInstanceDesc> instances) {
        if (instances == null) {
            return new ProcessInstanceList(new org.kie.server.api.model.instance.ProcessInstance[0]);
        }

        List<org.kie.server.api.model.instance.ProcessInstance> processInstances = new ArrayList<org.kie.server.api.model.instance.ProcessInstance>(instances.size());
        for (ProcessInstanceDesc pi : instances) {
            org.kie.server.api.model.instance.ProcessInstance instance = convertToProcessInstance(pi);

            processInstances.add(instance);
        }

        return new ProcessInstanceList(processInstances);
    }

    protected org.kie.server.api.model.instance.ProcessInstance convertToProcessInstance(ProcessInstanceDesc pi) {
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
                        .build();
                tasks[counter] = task;
                counter++;
            }
            instance.setActiveUserTasks(new TaskSummaryList(tasks));
        }

        return instance;
    }

    protected ProcessDefinitionList convertToProcessList(Collection<ProcessDefinition> definitions) {
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

    protected org.kie.server.api.model.definition.ProcessDefinition convertToProcess(ProcessDefinition processDesc) {
        if (processDesc == null) {
            return null;
        }

        org.kie.server.api.model.definition.ProcessDefinition processDefinition = org.kie.server.api.model.definition.ProcessDefinition.builder()
                .id(processDesc.getId())
                .name(processDesc.getName())
                .packageName(processDesc.getPackageName())
                .version(processDesc.getVersion())
                .containerId(processDesc.getDeploymentId())
                .build();

        return processDefinition;
    }

    protected QueryContext buildQueryContext(Integer page, Integer pageSize) {
        return new QueryContext(page * pageSize, pageSize);
    }

    protected QueryContext buildQueryContext(Integer page, Integer pageSize, String orderBy, boolean asc) {
        return new QueryContext(page * pageSize, pageSize, orderBy, asc);
    }

    protected QueryFilter buildQueryFilter(Integer page, Integer pageSize) {
        return new QueryFilter(page * pageSize, pageSize);
    }

    protected QueryFilter buildQueryFilter(Integer page, Integer pageSize, String orderBy, boolean asc) {
        return new QueryFilter(page * pageSize, pageSize, orderBy, asc);
    }

    protected List<Status> buildTaskStatuses(List<String> status) {
        if (status == null || status.isEmpty()) {
            return null;
        }

        List<Status> taskStatuses = new ArrayList<Status>();

        for (String s : status) {
            taskStatuses.add(Status.valueOf(s));
        }

        return taskStatuses;
    }


    protected NodeInstance convertToNodeInstance(NodeInstanceDesc nodeInstanceDesc) {

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

    protected NodeInstanceList convertToNodeInstanceList(Collection<NodeInstanceDesc> definitions) {
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

    protected VariableInstance convertToVariable(VariableDesc variableDesc) {
        VariableInstance instance = VariableInstance.builder()
                .name(variableDesc.getVariableId())
                .processInstanceId(variableDesc.getProcessInstanceId())
                .value(variableDesc.getNewValue())
                .oldValue(variableDesc.getOldValue())
                .date(variableDesc.getDataTimeStamp())
                .build();

        return instance;
    }

    protected VariableInstanceList convertToVariablesList(Collection<VariableDesc> variables) {
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

    protected TaskInstance convertToTask(UserTaskInstanceDesc userTask) {

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

    protected TaskSummaryList convertToTaskSummaryList(Collection<TaskSummary> tasks) {
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

    protected org.kie.server.api.model.instance.TaskSummary convertToTaskSummary(TaskSummary taskSummary) {
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

    protected String nullEmpty(String value) {
        if (value != null && value.isEmpty()) {
            return null;
        }

        return value;
    }
}
