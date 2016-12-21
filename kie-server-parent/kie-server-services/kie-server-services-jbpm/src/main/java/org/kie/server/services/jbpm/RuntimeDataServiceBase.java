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
import org.kie.server.api.model.instance.VariableInstanceList;
import org.kie.server.services.api.KieServerRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.kie.server.services.jbpm.ConvertUtils.*;

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

    public ProcessInstanceList getProcessInstances(List<Integer> status, String initiator, String processName, Integer page, Integer pageSize, String sort, boolean sortOrder) {
        if (sort == null || sort.isEmpty()) {
            sort = "ProcessInstanceId";
        }
        if (status == null || status.isEmpty()) {
            status = new ArrayList<Integer>();
            status.add(ProcessInstance.STATE_ACTIVE);
        }
        Collection<ProcessInstanceDesc> instances = null;

        if (processName != null && !processName.isEmpty()) {
            logger.debug("About to search for process instances with process name '{}' with page {} and page size {}", processName, page, pageSize);

            instances = runtimeDataService.getProcessInstancesByProcessName(status, processName, nullEmpty(initiator), buildQueryContext(page, pageSize, sort, sortOrder));
            logger.debug("Found {} process instances for process name '{}', statuses '{}'", instances.size(), processName, status);
        } else {
            logger.debug("About to search for process instances with page {} and page size {}", page, pageSize);
            instances = runtimeDataService.getProcessInstances(status, nullEmpty(initiator), buildQueryContext(page, pageSize, sort, sortOrder));

            logger.debug("Found {} process instances , statuses '{}'", instances.size(), status);
        }
        ProcessInstanceList processInstanceList = convertToProcessInstanceList(instances);
        logger.debug("Returning result of process instance search: {}", processInstanceList);

        return processInstanceList;
    }

    public ProcessInstanceList getProcessInstancesByProcessId(String processId, List<Integer> status, String initiator, Integer page, Integer pageSize, String sort, boolean sortOrder) {
        if (sort == null || sort.isEmpty()) {
            sort = "ProcessInstanceId";
        }
        if (status == null || status.isEmpty()) {
            status = new ArrayList<Integer>();
            status.add(ProcessInstance.STATE_ACTIVE);
        }
        logger.debug("About to search for process instances with process id '{}' with page {} and page size {}", processId, page, pageSize);

        Collection<ProcessInstanceDesc> instances = runtimeDataService.getProcessInstancesByProcessId(status, processId, nullEmpty(initiator), buildQueryContext(page, pageSize, sort, sortOrder));
        logger.debug("Found {} process instance for process id '{}', statuses '{}'", instances.size(), processId, status);

        ProcessInstanceList processInstanceList = convertToProcessInstanceList(instances);
        logger.debug("Returning result of process instance search: {}", processInstanceList);

        return processInstanceList;
    }


    public ProcessInstanceList getProcessInstancesByDeploymentId(String containerId, List<Integer> status, Integer page, Integer pageSize, String sort, boolean sortOrder) {
        if (sort == null || sort.isEmpty()) {
            sort = "ProcessInstanceId";
        }
        if (status == null || status.isEmpty()) {
            status = new ArrayList<Integer>();
            status.add(ProcessInstance.STATE_ACTIVE);
        }
        logger.debug("About to search for process instance belonging to container '{}' with page {} and page size {}", containerId, page, pageSize);

        Collection<ProcessInstanceDesc> instances = runtimeDataService.getProcessInstancesByDeploymentId(containerId, status, buildQueryContext(page, pageSize, sort, sortOrder));
        logger.debug("Found {} process instance for container '{}', statuses '{}'", instances.size(), containerId, status);

        ProcessInstanceList processInstanceList = convertToProcessInstanceList(instances);
        logger.debug("Returning result of process instance search: {}", processInstanceList);

        return processInstanceList;
    }


    public ProcessInstanceList getProcessInstancesByCorrelationKey(String correlationKey, Integer page, Integer pageSize, String sort, boolean sortOrder) {
        if (sort == null || sort.isEmpty()) {
            sort = "ProcessInstanceId";
        }
        String[] correlationProperties = correlationKey.split(":");

        CorrelationKey actualCorrelationKey = correlationKeyFactory.newCorrelationKey(Arrays.asList(correlationProperties));

        Collection<ProcessInstanceDesc> instances = runtimeDataService.getProcessInstancesByCorrelationKey(actualCorrelationKey, buildQueryContext(page, pageSize, sort, sortOrder));

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


    public ProcessInstanceList getProcessInstanceByVariables(String variableName, String variableValue, List<Integer> status, Integer page, Integer pageSize, String sort, boolean sortOrder) {
        if (sort == null || sort.isEmpty()) {
            sort = "ProcessInstanceId";
        }
        Collection<ProcessInstanceDesc> instances = null;
        if (variableValue != null && !variableValue.isEmpty()) {
            logger.debug("About to search for process instance that has variable '{}' with value '{}' with page {} and page size {}", variableName, variableValue, page, pageSize);

            instances = runtimeDataService.getProcessInstancesByVariableAndValue(variableName, variableValue, status, buildQueryContext(page, pageSize, sort, sortOrder));
            logger.debug("Found {} process instance with variable {} and variable value {}", instances.size(), variableName, variableValue);
        } else {
            logger.debug("About to search for process instance that has variable '{}' with page {} and page size {}", variableName, page, pageSize);

            instances = runtimeDataService.getProcessInstancesByVariable(variableName, status, buildQueryContext(page, pageSize, sort, sortOrder));
            logger.debug("Found {} process instance with variable {} ", instances.size(), variableName);
        }

        ProcessInstanceList processInstanceList = convertToProcessInstanceList(instances);
        logger.debug("Returning result of process instance search: {}", processInstanceList);

        return processInstanceList;
    }

    public org.kie.server.api.model.instance.ProcessInstance getProcessInstanceById(long processInstanceId) {
        return getProcessInstanceById(processInstanceId, false);
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


    public ProcessDefinitionList getProcessesByDeploymentId(String containerId, Integer page, Integer pageSize, String sort, boolean sortOrder) {

        logger.debug("About to search for process definitions within container '{}' with page {} and page size {}", containerId, page, pageSize);
        if (sort == null || sort.isEmpty()) {
            sort = "ProcessName";
        }
        Collection<ProcessDefinition> definitions = runtimeDataService.getProcessesByDeploymentId(containerId, buildQueryContext(page, pageSize, sort, sortOrder));
        logger.debug("Found {} process definitions within container '{}'", definitions.size(), containerId);

        ProcessDefinitionList processDefinitionList = convertToProcessList(definitions);
        logger.debug("Returning result of process definition search: {}", processDefinitionList);

        return processDefinitionList;

    }

    public ProcessDefinitionList getProcessesByFilter(String filter, Integer page, Integer pageSize, String sort, boolean sortOrder) {

        Collection<ProcessDefinition> definitions;
        if (sort == null || sort.isEmpty()) {
            sort = "ProcessName";
        }
        if (filter != null && !filter.isEmpty()) {
            logger.debug("About to search for process definitions with filter '{}' with page {} and page size {}", filter, page, pageSize);

            definitions = runtimeDataService.getProcessesByFilter(filter, buildQueryContext(page, pageSize, sort, sortOrder));
            logger.debug("Found {} process definitions with filter '{}'", definitions.size(), filter);
        } else {
            logger.debug("About to search for process definitions with page {} and page size {}", page, pageSize);

            definitions = runtimeDataService.getProcesses(buildQueryContext(page, pageSize, sort, sortOrder));
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



    public TaskSummaryList getTasksAssignedAsBusinessAdministratorByStatus(List<String> status, String userId, Integer page, Integer pageSize, String sort, boolean sortOrder) {

        userId = getUser(userId);
        logger.debug("About to search for task assigned as business admin for user '{}'", userId);
        List<TaskSummary> tasks;
        if (status == null || status.isEmpty()) {
            tasks = runtimeDataService.getTasksAssignedAsBusinessAdministrator(userId, buildQueryFilter(page, pageSize, sort, sortOrder));
        } else {
            List<Status> taskStatuses = buildTaskStatuses(status);

            tasks = runtimeDataService.getTasksAssignedAsBusinessAdministratorByStatus(userId, taskStatuses, buildQueryFilter(page, pageSize, sort, sortOrder));
        }

        logger.debug("Found {} tasks for user '{}' assigned as business admin", tasks.size(), userId);
        TaskSummaryList result = convertToTaskSummaryList(tasks);

        return result;

    }

    public TaskSummaryList getTasksAssignedAsPotentialOwner(List<String> status,  List<String> groupIds, String userId, Integer page, Integer pageSize, String sort, boolean sortOrder) {
        return getTasksAssignedAsPotentialOwner(status, groupIds, userId, page, pageSize, sort, sortOrder, null);
    }

    public TaskSummaryList getTasksAssignedAsPotentialOwner(List<String> status,  List<String> groupIds, String userId, Integer page, Integer pageSize, String sort, boolean sortOrder, String filter) {

        List<Status> taskStatuses = buildTaskStatuses(status);

        userId = getUser(userId);
        logger.debug("About to search for task assigned as potential owner for user '{}'", userId);
        List<TaskSummary> tasks;
        QueryFilter queryFilter = buildTaskByNameQueryFilter(page, pageSize, sort, sortOrder, filter);
        if (groupIds != null && !groupIds.isEmpty()) {

            if (taskStatuses == null) {
                tasks = runtimeDataService.getTasksAssignedAsPotentialOwner(userId, groupIds, queryFilter);
            } else {
                tasks = runtimeDataService.getTasksAssignedAsPotentialOwner(userId, groupIds, taskStatuses, queryFilter);
            }
        } else if (taskStatuses != null) {
            tasks = runtimeDataService.getTasksAssignedAsPotentialOwnerByStatus(userId, taskStatuses, queryFilter);
        } else {

            tasks = runtimeDataService.getTasksAssignedAsPotentialOwner(userId, queryFilter);
        }

        logger.debug("Found {} tasks for user '{}' assigned as potential owner", tasks.size(), userId);
        TaskSummaryList result = convertToTaskSummaryList(tasks);

        return result;


    }

    public TaskSummaryList getTasksOwnedByStatus(List<String> status, String userId, Integer page, Integer pageSize, String sort, boolean sortOrder) {

        List<Status> taskStatuses = buildTaskStatuses(status);

        userId = getUser(userId);
        logger.debug("About to search for task owned user '{}'", userId);
        List<TaskSummary> tasks;

         if (taskStatuses != null) {
            tasks = runtimeDataService.getTasksOwnedByStatus(userId, taskStatuses, buildQueryFilter(page, pageSize, sort, sortOrder));
        } else {

            tasks = runtimeDataService.getTasksOwned(userId, buildQueryFilter(page, pageSize, sort, sortOrder));
        }

        logger.debug("Found {} tasks owned by user '{}'", tasks.size(), userId);
        TaskSummaryList result = convertToTaskSummaryList(tasks);

        return result;
    }

    public TaskSummaryList getTasksByStatusByProcessInstanceId(Number processInstanceId, List<String> status, Integer page, Integer pageSize, String sort, boolean sortOrder) {

        List<Status> taskStatuses = buildTaskStatuses(status);
        if (taskStatuses == null) {
            taskStatuses = new ArrayList<Status>();
            taskStatuses.add(Status.Ready);
            taskStatuses.add(Status.Reserved);
            taskStatuses.add(Status.InProgress);
        }

        logger.debug("About to search for tasks attached to process instance with id '{}'", processInstanceId);
        List<TaskSummary> tasks = runtimeDataService.getTasksByStatusByProcessInstanceId(processInstanceId.longValue(), taskStatuses, buildQueryFilter(page, pageSize, sort, sortOrder));


        logger.debug("Found {} tasks attached to process instance with id '{}'", tasks.size(), processInstanceId);
        TaskSummaryList result = convertToTaskSummaryList(tasks);

        return result;
    }

    public TaskSummaryList getAllAuditTask(String userId, Integer page, Integer pageSize, String sort, boolean sortOrder) {

        userId = getUser(userId);
        logger.debug("About to search for tasks available for user '{}'", userId);
        List<AuditTask> tasks = runtimeDataService.getAllAuditTask(userId, buildQueryFilter(page, pageSize, sort, sortOrder));


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


    public TaskEventInstanceList getTaskEvents(long taskId, Integer page, Integer pageSize, String sort, boolean sortOrder) {

        if (sort == null || sort.isEmpty()) {
            sort = "Id";
        }

        logger.debug("About to search for task {} events", taskId);
        List<TaskEvent> tasks = runtimeDataService.getTaskEvents(taskId, buildQueryFilter(page, pageSize, sort, sortOrder));


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
                        .message(taskSummary.getMessage())
                        .build();
                instances[counter] = task;
                counter++;
            }
            result = new TaskEventInstanceList(instances);
        }


        return result;

    }

    public TaskSummaryList getTasksByVariables(String userId, String variableName, String variableValue, List<String> status, Integer page, Integer pageSize, String sort, boolean sortOrder) {

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

            instances = runtimeDataService.getTasksByVariableAndValue(userId, variableName, variableValue, taskStatuses, buildQueryContext(page, pageSize, sort, sortOrder));
            logger.debug("Found {} tasks with variable {} and variable value {}", instances.size(), variableName, variableValue);
        } else {
            logger.debug("About to search for tasks that has variable '{}' with page {} and page size {}", variableName, page, pageSize);

            instances = runtimeDataService.getTasksByVariable(userId, variableName, taskStatuses, buildQueryContext(page, pageSize, sort, sortOrder));
            logger.debug("Found {} tasks with variable {} ", instances.size(), variableName);
        }

        TaskSummaryList taskSummaryList = convertToTaskSummaryList(instances);
        logger.debug("Returning result of task by variable search: {}", taskSummaryList);

        return taskSummaryList;
    }


}
