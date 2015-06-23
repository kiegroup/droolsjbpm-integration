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

package org.kie.server.remote.rest.jbpm;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Variant;

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
import org.kie.internal.query.QueryContext;
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
import org.kie.server.remote.rest.common.exception.ExecutionServerRestOperationException;
import org.kie.server.services.api.KieServerRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.kie.server.api.rest.RestURI.*;
import static org.kie.server.remote.rest.common.util.RestUtils.*;
import static org.kie.server.remote.rest.jbpm.resources.Messages.*;

@Path("/server")
public class RuntimeDataResource {

    public static final Logger logger = LoggerFactory.getLogger(RuntimeDataResource.class);

    private RuntimeDataService runtimeDataService;
    private IdentityProvider identityProvider;

    private boolean bypassAuthUser;

    private CorrelationKeyFactory correlationKeyFactory = KieInternalServices.Factory.get().newCorrelationKeyFactory();

    public RuntimeDataResource(RuntimeDataService delegate, KieServerRegistry context) {
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

    @GET
    @Path(PROCESS_INSTANCES_GET_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getProcessInstances(@Context HttpHeaders headers,
            @QueryParam("status") List<Integer> status, @QueryParam("initiator") String initiator, @QueryParam("processName") String processName,
            @QueryParam("page") @DefaultValue("0") Integer page, @QueryParam("pageSize") @DefaultValue("10") Integer pageSize) {

        if (status == null || status.isEmpty()) {
            status = new ArrayList<Integer>();
            status.add(ProcessInstance.STATE_ACTIVE);
        }
        Collection<ProcessInstanceDesc> instances = null;

        if (processName != null && !processName.isEmpty()) {
            logger.debug("About to search for process instances with process name '{}' with page {} and page size {}", processName, page, pageSize);

            instances = runtimeDataService.getProcessInstancesByProcessName(status, processName, initiator, buildQueryContext(page, pageSize));
            logger.debug("Found {} process instances for process name '{}', statuses '{}'", instances.size(), processName, status);
        } else {
            logger.debug("About to search for process instances with page {} and page size {}", page, pageSize);
            instances = runtimeDataService.getProcessInstances(status, initiator, buildQueryContext(page, pageSize));

            logger.debug("Found {} process instances '{}', statuses '{}'", instances.size(), status);
        }
        ProcessInstanceList processInstanceList = convertToProcessInstanceList(instances);
        logger.debug("Returning result of process instance search: {}", processInstanceList);

        return createCorrectVariant(processInstanceList, headers, Response.Status.OK);
    }



    @GET
    @Path(PROCESS_INSTANCES_BY_PROCESS_ID_GET_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getProcessInstancesByProcessId(@Context HttpHeaders headers, @PathParam("pId")String processId,
            @QueryParam("status") List<Integer> status, @QueryParam("initiator") String initiator,
            @QueryParam("page") @DefaultValue("0") Integer page, @QueryParam("pageSize") @DefaultValue("10") Integer pageSize) {

        if (status == null || status.isEmpty()) {
            status = new ArrayList<Integer>();
            status.add(ProcessInstance.STATE_ACTIVE);
        }
        logger.debug("About to search for process instances with process id '{}' with page {} and page size {}", processId, page, pageSize);

        Collection<ProcessInstanceDesc> instances = runtimeDataService.getProcessInstancesByProcessId(status, processId, initiator, buildQueryContext(page, pageSize));
        logger.debug("Found {} process instance for process id '{}', statuses '{}'", instances.size(), processId, status);

        ProcessInstanceList processInstanceList = convertToProcessInstanceList(instances);
        logger.debug("Returning result of process instance search: {}", processInstanceList);

        return createCorrectVariant(processInstanceList, headers, Response.Status.OK);
    }


    @GET
    @Path(PROCESS_INSTANCES_BY_CONTAINER_ID_GET_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getProcessInstancesByDeploymentId(@Context HttpHeaders headers, @PathParam("id") String containerId, @QueryParam("status")List<Integer> status,
            @QueryParam("page") @DefaultValue("0") Integer page, @QueryParam("pageSize") @DefaultValue("10") Integer pageSize) {

        if (status == null || status.isEmpty()) {
            status = new ArrayList<Integer>();
            status.add(ProcessInstance.STATE_ACTIVE);
        }
        logger.debug("About to search for process instance belonging to container '{}' with page {} and page size {}", containerId, page, pageSize);

        Collection<ProcessInstanceDesc> instances = runtimeDataService.getProcessInstancesByDeploymentId(containerId, status , buildQueryContext(page, pageSize));
        logger.debug("Found {} process instance for container '{}', statuses '{}'", instances.size(), containerId, status);

        ProcessInstanceList processInstanceList = convertToProcessInstanceList(instances);
        logger.debug("Returning result of process instance search: {}", processInstanceList);

        return createCorrectVariant(processInstanceList, headers, Response.Status.OK);
    }

    @GET
    @Path(PROCESS_INSTANCE_BY_CORRELATION_KEY_GET_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getProcessInstanceByCorrelationKey(@Context HttpHeaders headers, @PathParam("correlationKey") String correlationKey) {

        Variant v = getVariant(headers);

        String[] correlationProperties = correlationKey.split(":");

        CorrelationKey actualCorrelationKey = correlationKeyFactory.newCorrelationKey(Arrays.asList(correlationProperties));

        ProcessInstanceDesc processInstanceDesc = runtimeDataService.getProcessInstanceByCorrelationKey(actualCorrelationKey);
        if (processInstanceDesc == null) {

            throw ExecutionServerRestOperationException.notFound(MessageFormat.format(PROCESS_INSTANCE_NOT_FOUND, correlationKey), v);
        }
        return createCorrectVariant(convertToProcessInstance(processInstanceDesc), headers, Response.Status.OK);
    }

    @GET
    @Path(PROCESS_INSTANCE_BY_INSTANCE_ID_GET_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getProcessInstanceById(@Context HttpHeaders headers, @PathParam("pInstanceId") long processInstanceId) {
        Variant v = getVariant(headers);
        ProcessInstanceDesc processInstanceDesc = runtimeDataService.getProcessInstanceById(processInstanceId);
        if (processInstanceDesc == null) {

            throw ExecutionServerRestOperationException.notFound(MessageFormat.format(PROCESS_INSTANCE_NOT_FOUND, processInstanceId), v);
        }
        return createCorrectVariant(convertToProcessInstance(processInstanceDesc), headers, Response.Status.OK);
    }

    @GET
    @Path(NODE_INSTANCES_BY_WORK_ITEM_ID_GET_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getNodeInstanceForWorkItem(@Context HttpHeaders headers, @PathParam("pInstanceId") long processInstanceId, @PathParam("workItemId") Long workItemId) {
        Variant v = getVariant(headers);
        NodeInstanceDesc nodeInstanceDesc = runtimeDataService.getNodeInstanceForWorkItem(workItemId);
        if (nodeInstanceDesc == null) {

            throw ExecutionServerRestOperationException.notFound(MessageFormat.format(NODE_INSTANCE_NOT_FOUND, workItemId, processInstanceId), v);
        }
        return createCorrectVariant(convertToNodeInstance(nodeInstanceDesc), headers, Response.Status.OK);
    }

    @GET
    @Path(NODE_INSTANCES_BY_INSTANCE_ID_GET_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getProcessInstanceHistory(@Context HttpHeaders headers, @PathParam("pInstanceId") long processInstanceId,
            @QueryParam("activeOnly")Boolean active, @QueryParam("completedOnly")Boolean completed,
            @QueryParam("page") @DefaultValue("0") Integer page, @QueryParam("pageSize") @DefaultValue("10") Integer pageSize) {

        logger.debug("About to search for node instances with page {} and page size {}", page, pageSize);
        Collection<NodeInstanceDesc> result;

        if (Boolean.TRUE.equals(active)) {
            logger.debug("Searching for active node instances for process instance with id {}", processInstanceId);
            result = runtimeDataService.getProcessInstanceHistoryActive(processInstanceId, buildQueryContext(page, pageSize));
        } else if (Boolean.TRUE.equals(completed)) {
            logger.debug("Searching for completed node instances for process instance with id {}", processInstanceId);
            result = runtimeDataService.getProcessInstanceHistoryCompleted(processInstanceId, buildQueryContext(page, pageSize));
        } else {
            logger.debug("Searching for active and completed node instances for process instance with id {}", processInstanceId);
            result = runtimeDataService.getProcessInstanceFullHistory(processInstanceId, buildQueryContext(page, pageSize));
        }

        NodeInstanceList nodeInstanceList = convertToNodeInstanceList(result);
        logger.debug("Returning result of node instances search: {}", nodeInstanceList);
        return createCorrectVariant(nodeInstanceList, headers, Response.Status.OK);
    }

    @GET
    @Path(VAR_INSTANCES_BY_INSTANCE_ID_GET_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getVariablesCurrentState(@Context HttpHeaders headers, @PathParam("pInstanceId") long processInstanceId) {
        logger.debug("About to search for variables within process instance  '{}'", processInstanceId);

        Collection<VariableDesc> variableDescs = runtimeDataService.getVariablesCurrentState(processInstanceId);
        logger.debug("Found {} variables within process instance '{}'", variableDescs.size(), processInstanceId);

        VariableInstanceList variableInstanceList = convertToVariablesList(variableDescs);
        logger.debug("Returning result of variables search: {}", variableInstanceList);

        return createCorrectVariant(variableInstanceList, headers, Response.Status.OK);
    }

    @GET
    @Path(VAR_INSTANCES_BY_VAR_INSTANCE_ID_GET_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getVariableHistory(@Context HttpHeaders headers, @PathParam("pInstanceId") long processInstanceId,
            @PathParam("varName") String variableName,
            @QueryParam("page") @DefaultValue("0") Integer page, @QueryParam("pageSize") @DefaultValue("10") Integer pageSize) {
        logger.debug("About to search for variable '{}; history within process instance '{}' with page {} and page size {}", variableName, processInstanceId, page, pageSize);

        Collection<VariableDesc> variableDescs = runtimeDataService.getVariableHistory(processInstanceId, variableName, buildQueryContext(page, pageSize));
        logger.debug("Found {} variable {} history entries within process instance '{}'", variableDescs.size(), variableName, processInstanceId);

        VariableInstanceList variableInstanceList = convertToVariablesList(variableDescs);
        logger.debug("Returning result of variable '{}; history search: {}", variableName, variableInstanceList);

        return createCorrectVariant(variableInstanceList, headers, Response.Status.OK);
    }


    @GET
    @Path(PROCESS_DEFINITIONS_BY_CONTAINER_ID_GET_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getProcessesByDeploymentId(@Context HttpHeaders headers, @PathParam("id") String containerId,
            @QueryParam("page") @DefaultValue("0") Integer page, @QueryParam("pageSize") @DefaultValue("10") Integer pageSize) {

        logger.debug("About to search for process definitions within container '{}' with page {} and page size {}", containerId, page, pageSize);

        Collection<ProcessDefinition> definitions = runtimeDataService.getProcessesByDeploymentId(containerId, buildQueryContext(page, pageSize, "ProcessName", true));
        logger.debug("Found {} process definitions within container '{}'", definitions.size(), containerId);

        ProcessDefinitionList processDefinitionList = convertToProcessList(definitions);
        logger.debug("Returning result of process definition search: {}", processDefinitionList);

        return createCorrectVariant(processDefinitionList, headers, Response.Status.OK);

    }

    @GET
    @Path(PROCESS_DEFINITIONS_GET_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getProcessesByFilter(@Context HttpHeaders headers, @QueryParam("filter") String filter,
            @QueryParam("page") @DefaultValue("0") Integer page, @QueryParam("pageSize") @DefaultValue("10") Integer pageSize) {

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

        return createCorrectVariant(processDefinitionList, headers, Response.Status.OK);
    }

    @GET
    @Path(PROCESS_DEFINITIONS_BY_ID_GET_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getProcessesById(@Context HttpHeaders headers, @PathParam("pId") String processId) {

        Collection<ProcessDefinition> definitions = runtimeDataService.getProcessesById(processId);

        ProcessDefinitionList processDefinitionList = convertToProcessList(definitions);
        logger.debug("Returning result of process definition search: {}", processDefinitionList);

        return createCorrectVariant(processDefinitionList, headers, Response.Status.OK);
    }

    @GET
    @Path(PROCESS_DEFINITIONS_BY_CONTAINER_ID_DEF_ID_GET_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getProcessesByDeploymentIdProcessId(@Context HttpHeaders headers, @PathParam("id") String containerId, @PathParam("pId") String processId) {
        Variant v = getVariant(headers);
        ProcessDefinition processDesc = runtimeDataService.getProcessesByDeploymentIdProcessId(containerId, processId);
        if (processDesc == null) {

            throw ExecutionServerRestOperationException.notFound(MessageFormat.format(PROCESS_DEFINITION_NOT_FOUND, processId, containerId), v);
        }
        return createCorrectVariant(convertToProcess(processDesc), headers, Response.Status.OK);
    }

    @GET
    @Path(TASK_BY_WORK_ITEM_ID_GET_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getTaskByWorkItemId(@Context HttpHeaders headers, @PathParam("workItemId") Long workItemId) {
        Variant v = getVariant(headers);
        UserTaskInstanceDesc userTaskDesc = runtimeDataService.getTaskByWorkItemId(workItemId);
        if (userTaskDesc == null) {

            throw ExecutionServerRestOperationException.notFound(MessageFormat.format(TASK_INSTANCE_NOT_FOUND_FOR_WORKITEM, workItemId), v);
        }
        return createCorrectVariant(convertToTask(userTaskDesc), headers, Response.Status.OK);
    }

    @GET
    @Path(TASK_GET_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getTaskById(@Context HttpHeaders headers, @PathParam("tInstanceId") Long taskId) {
        Variant v = getVariant(headers);
        UserTaskInstanceDesc userTaskDesc = runtimeDataService.getTaskById(taskId);
        if (userTaskDesc == null) {

            throw ExecutionServerRestOperationException.notFound(MessageFormat.format(TASK_INSTANCE_NOT_FOUND, taskId), v);
        }
        return createCorrectVariant(convertToTask(userTaskDesc), headers, Response.Status.OK);
    }


    @GET
    @Path(TASKS_ASSIGN_BUSINESS_ADMINS_GET_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getTasksAssignedAsBusinessAdministratorByStatus(@Context HttpHeaders headers, @QueryParam("status") List<String> status,
            @QueryParam("user") String userId, @QueryParam("page") @DefaultValue("0") Integer page, @QueryParam("pageSize") @DefaultValue("10") Integer pageSize) {
        Variant v = getVariant(headers);

        try {

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

            return createCorrectVariant(result, headers, Response.Status.OK);

        } catch (Exception e) {
            logger.error("Unexpected error during processing {}", e.getMessage(), e);
            throw ExecutionServerRestOperationException.internalServerError(MessageFormat.format(UNEXPECTED_ERROR, e.getMessage()), v);
        }
    }

    @GET
    @Path(TASKS_ASSIGN_POT_OWNERS_GET_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getTasksAssignedAsPotentialOwner(@Context HttpHeaders headers,
            @QueryParam("status") List<String> status,  @QueryParam("groups") List<String> groupIds,
            @QueryParam("user") String userId,
            @QueryParam("page") @DefaultValue("0") Integer page, @QueryParam("pageSize") @DefaultValue("10") Integer pageSize ) {

        Variant v = getVariant(headers);

        try {
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

            return createCorrectVariant(result, headers, Response.Status.OK);

        } catch (Exception e) {
            logger.error("Unexpected error during processing {}", e.getMessage(), e);
            throw ExecutionServerRestOperationException.internalServerError(MessageFormat.format(UNEXPECTED_ERROR, e.getMessage()), v);
        }

    }

    @GET
    @Path(TASKS_OWNED_GET_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getTasksOwnedByStatus(@Context HttpHeaders headers,
            @QueryParam("status") List<String> status, @QueryParam("user") String userId,
            @QueryParam("page") @DefaultValue("0") Integer page, @QueryParam("pageSize") @DefaultValue("10") Integer pageSize) {
        Variant v = getVariant(headers);

        try {
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

            return createCorrectVariant(result, headers, Response.Status.OK);

        } catch (Exception e) {
            logger.error("Unexpected error during processing {}", e.getMessage(), e);
            throw ExecutionServerRestOperationException.internalServerError(MessageFormat.format(UNEXPECTED_ERROR, e.getMessage()), v);
        }
    }

    @GET
    @Path(TASK_BY_PROCESS_INST_ID_GET_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getTasksByStatusByProcessInstanceId(@Context HttpHeaders headers, @PathParam("pInstanceId") Long processInstanceId,
            @QueryParam("status") List<String> status,
            @QueryParam("page") @DefaultValue("0") Integer page, @QueryParam("pageSize") @DefaultValue("10") Integer pageSize) {
        Variant v = getVariant(headers);

        try {
            List<Status> taskStatuses = buildTaskStatuses(status);
            if (taskStatuses == null) {
                taskStatuses = new ArrayList<Status>();
                taskStatuses.add(Status.Ready);
                taskStatuses.add(Status.Reserved);
                taskStatuses.add(Status.InProgress);
            }

            logger.debug("About to search for tasks attached to process instance with id '{}'", processInstanceId);
            List<TaskSummary> tasks = runtimeDataService.getTasksByStatusByProcessInstanceId(processInstanceId, taskStatuses, buildQueryFilter(page, pageSize));


            logger.debug("Found {} tasks attached to process instance with id '{}'", tasks.size(), processInstanceId);
            TaskSummaryList result = convertToTaskSummaryList(tasks);

            return createCorrectVariant(result, headers, Response.Status.OK);

        } catch (Exception e) {
            logger.error("Unexpected error during processing {}", e.getMessage(), e);
            throw ExecutionServerRestOperationException.internalServerError(MessageFormat.format(UNEXPECTED_ERROR, e.getMessage()), v);
        }
    }

    @GET
    @Path(TASKS_GET_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getAllAuditTask(@Context HttpHeaders headers, @QueryParam("user") String userId,
            @QueryParam("page") @DefaultValue("0") Integer page, @QueryParam("pageSize") @DefaultValue("10") Integer pageSize) {
        Variant v = getVariant(headers);

        try {
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


            return createCorrectVariant(result, headers, Response.Status.OK);

        } catch (Exception e) {
            logger.error("Unexpected error during processing {}", e.getMessage(), e);
            throw ExecutionServerRestOperationException.internalServerError(MessageFormat.format(UNEXPECTED_ERROR, e.getMessage()), v);
        }
    }

    @GET
    @Path(TASKS_EVENTS_GET_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getTaskEvents(@Context HttpHeaders headers, @PathParam("tInstanceId") Long taskId,
            @QueryParam("page") @DefaultValue("0") Integer page, @QueryParam("pageSize") @DefaultValue("10") Integer pageSize) {
        Variant v = getVariant(headers);

        try {
            logger.debug("About to search for task {} events", taskId);
            List<TaskEvent> tasks = runtimeDataService.getTaskEvents(taskId, buildQueryFilter(page, pageSize));


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


            return createCorrectVariant(result, headers, Response.Status.OK);

        } catch (Exception e) {
            logger.error("Unexpected error during processing {}", e.getMessage(), e);
            throw ExecutionServerRestOperationException.internalServerError(MessageFormat.format(UNEXPECTED_ERROR, e.getMessage()), v);
        }
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
}
