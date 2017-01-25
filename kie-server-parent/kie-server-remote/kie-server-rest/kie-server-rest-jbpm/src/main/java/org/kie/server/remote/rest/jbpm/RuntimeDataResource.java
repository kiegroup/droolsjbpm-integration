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

package org.kie.server.remote.rest.jbpm;

import java.text.MessageFormat;
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

import org.jbpm.services.api.ProcessInstanceNotFoundException;
import org.jbpm.services.api.TaskNotFoundException;
import org.kie.server.api.model.definition.ProcessDefinitionList;
import org.kie.server.api.model.instance.NodeInstance;
import org.kie.server.api.model.instance.NodeInstanceList;
import org.kie.server.api.model.instance.ProcessInstance;
import org.kie.server.api.model.instance.ProcessInstanceList;
import org.kie.server.api.model.instance.TaskEventInstanceList;
import org.kie.server.api.model.instance.TaskInstance;
import org.kie.server.api.model.instance.TaskSummaryList;
import org.kie.server.api.model.instance.VariableInstanceList;
import org.kie.server.remote.rest.common.Header;
import org.kie.server.services.api.KieServerRegistry;
import org.kie.server.services.jbpm.RuntimeDataServiceBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.kie.server.api.rest.RestURI.*;
import static org.kie.server.remote.rest.common.util.RestUtils.*;
import static org.kie.server.remote.rest.jbpm.resources.Messages.*;

@Path("server/" + QUERY_URI)
public class RuntimeDataResource {

    public static final Logger logger = LoggerFactory.getLogger(RuntimeDataResource.class);

    private RuntimeDataServiceBase runtimeDataServiceBase;
    private KieServerRegistry context;

    public RuntimeDataResource() {

    }

    public RuntimeDataResource(RuntimeDataServiceBase delegate, KieServerRegistry context) {
        this.runtimeDataServiceBase = delegate;
        this.context = context;
    }


    @GET
    @Path(PROCESS_INSTANCES_GET_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getProcessInstances(@Context HttpHeaders headers,
            @QueryParam("status") List<Integer> status, @QueryParam("initiator") String initiator, @QueryParam("processName") String processName,
            @QueryParam("page") @DefaultValue("0") Integer page, @QueryParam("pageSize") @DefaultValue("10") Integer pageSize,
            @QueryParam("sort") String sort, @QueryParam("sortOrder") @DefaultValue("true") boolean sortOrder) {
        // no container id available so only used to transfer conversation id if given by client
        Header conversationIdHeader = buildConversationIdHeader("", context, headers);
        ProcessInstanceList processInstanceList = runtimeDataServiceBase.getProcessInstances(status, initiator, processName, page, pageSize, sort, sortOrder);
        logger.debug("Returning result of process instance search: {}", processInstanceList);

        return createCorrectVariant(processInstanceList, headers, Response.Status.OK, conversationIdHeader);
    }



    @GET
    @Path(PROCESS_INSTANCES_BY_PROCESS_ID_GET_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getProcessInstancesByProcessId(@Context HttpHeaders headers, @PathParam("pId")String processId,
            @QueryParam("status") List<Integer> status, @QueryParam("initiator") String initiator,
            @QueryParam("page") @DefaultValue("0") Integer page, @QueryParam("pageSize") @DefaultValue("10") Integer pageSize,
            @QueryParam("sort") String sort, @QueryParam("sortOrder") @DefaultValue("true") boolean sortOrder) {
        // no container id available so only used to transfer conversation id if given by client
        Header conversationIdHeader = buildConversationIdHeader("", context, headers);

        ProcessInstanceList processInstanceList = runtimeDataServiceBase.getProcessInstancesByProcessId(processId, status, initiator, page, pageSize, sort, sortOrder);
        logger.debug("Returning result of process instance search: {}", processInstanceList);

        return createCorrectVariant(processInstanceList, headers, Response.Status.OK, conversationIdHeader);
    }


    @GET
    @Path(PROCESS_INSTANCES_BY_CONTAINER_ID_GET_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getProcessInstancesByDeploymentId(@Context HttpHeaders headers, @PathParam("id") String containerId, @QueryParam("status")List<Integer> status,
            @QueryParam("page") @DefaultValue("0") Integer page, @QueryParam("pageSize") @DefaultValue("10") Integer pageSize,
            @QueryParam("sort") String sort, @QueryParam("sortOrder") @DefaultValue("true") boolean sortOrder) {

        // no container id available so only used to transfer conversation id if given by client
        Header conversationIdHeader = buildConversationIdHeader("", context, headers);

        ProcessInstanceList processInstanceList = runtimeDataServiceBase.getProcessInstancesByDeploymentId(containerId, status, page, pageSize, sort, sortOrder);
        logger.debug("Returning result of process instance search: {}", processInstanceList);

        return createCorrectVariant(processInstanceList, headers, Response.Status.OK, conversationIdHeader);
    }

    @GET
    @Path(PROCESS_INSTANCE_BY_CORRELATION_KEY_GET_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getProcessInstanceByCorrelationKey(@Context HttpHeaders headers, @PathParam("correlationKey") String correlationKey) {
        Variant v = getVariant(headers);
        // no container id available so only used to transfer conversation id if given by client
        Header conversationIdHeader = buildConversationIdHeader("", context, headers);

        ProcessInstance processInstance = runtimeDataServiceBase.getProcessInstanceByCorrelationKey(correlationKey);
        if (processInstance == null) {

            return notFound(MessageFormat.format(PROCESS_INSTANCE_NOT_FOUND, correlationKey), v, conversationIdHeader);
        }
        return createCorrectVariant(processInstance, headers, Response.Status.OK, conversationIdHeader);

    }

    @GET
    @Path(PROCESS_INSTANCES_BY_CORRELATION_KEY_GET_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getProcessInstancesByCorrelationKey(@Context HttpHeaders headers, @PathParam("correlationKey") String correlationKey
            , @QueryParam("page") @DefaultValue("0") Integer page, @QueryParam("pageSize") @DefaultValue("10") Integer pageSize,
            @QueryParam("sort") String sort, @QueryParam("sortOrder") @DefaultValue("true") boolean sortOrder) {

        // no container id available so only used to transfer conversation id if given by client
        Header conversationIdHeader = buildConversationIdHeader("", context, headers);

        ProcessInstanceList processInstanceList = runtimeDataServiceBase.getProcessInstancesByCorrelationKey(correlationKey, page, pageSize, sort, sortOrder);

        return createCorrectVariant(processInstanceList, headers, Response.Status.OK, conversationIdHeader);

    }

    @GET
    @Path(PROCESS_INSTANCE_BY_VAR_NAME_GET_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getProcessInstanceByVariables(@Context HttpHeaders headers, @PathParam("varName") String variableName, @QueryParam("varValue") String variableValue,
            @QueryParam("status")List<Integer> status, @QueryParam("page") @DefaultValue("0") Integer page, @QueryParam("pageSize") @DefaultValue("10") Integer pageSize,
            @QueryParam("sort") String sort, @QueryParam("sortOrder") @DefaultValue("true") boolean sortOrder) {

        // no container id available so only used to transfer conversation id if given by client
        Header conversationIdHeader = buildConversationIdHeader("", context, headers);

        ProcessInstanceList processInstanceList = runtimeDataServiceBase.getProcessInstanceByVariables(variableName, variableValue, status, page, pageSize, sort, sortOrder);
        logger.debug("Returning result of process instance search: {}", processInstanceList);

        return createCorrectVariant(processInstanceList, headers, Response.Status.OK, conversationIdHeader);
    }

    @GET
    @Path(PROCESS_INSTANCE_BY_INSTANCE_ID_GET_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getProcessInstanceById(@Context HttpHeaders headers, @PathParam("pInstanceId") long processInstanceId, @QueryParam("withVars") boolean withVars) {
        Variant v = getVariant(headers);
        // no container id available so only used to transfer conversation id if given by client
        Header conversationIdHeader = buildConversationIdHeader("", context, headers);
        org.kie.server.api.model.instance.ProcessInstance processInstanceDesc = null;
        try{
            processInstanceDesc = runtimeDataServiceBase.getProcessInstanceById(processInstanceId, withVars);
        } catch(ProcessInstanceNotFoundException e) {

            return notFound(MessageFormat.format(PROCESS_INSTANCE_NOT_FOUND, processInstanceId), v, conversationIdHeader);
        }
        return createCorrectVariant(processInstanceDesc, headers, Response.Status.OK, conversationIdHeader);
    }

    @GET
    @Path(NODE_INSTANCES_BY_WORK_ITEM_ID_GET_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getNodeInstanceForWorkItem(@Context HttpHeaders headers, @PathParam("pInstanceId") long processInstanceId, @PathParam("workItemId") long workItemId) {
        Variant v = getVariant(headers);
        // no container id available so only used to transfer conversation id if given by client
        Header conversationIdHeader = buildConversationIdHeader("", context, headers);
        NodeInstance nodeInstanceDesc = null;
        try {
            nodeInstanceDesc = runtimeDataServiceBase.getNodeInstanceForWorkItem(processInstanceId, workItemId);
        } catch (IllegalArgumentException e) {

            return notFound(MessageFormat.format(NODE_INSTANCE_NOT_FOUND, workItemId, processInstanceId), v, conversationIdHeader);
        }
        return createCorrectVariant(nodeInstanceDesc, headers, Response.Status.OK, conversationIdHeader);
    }

    @GET
    @Path(NODE_INSTANCES_BY_INSTANCE_ID_GET_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getProcessInstanceHistory(@Context HttpHeaders headers, @PathParam("pInstanceId") long processInstanceId,
            @QueryParam("activeOnly")Boolean active, @QueryParam("completedOnly")Boolean completed,
            @QueryParam("page") @DefaultValue("0") Integer page, @QueryParam("pageSize") @DefaultValue("10") Integer pageSize) {

        // no container id available so only used to transfer conversation id if given by client
        Header conversationIdHeader = buildConversationIdHeader("", context, headers);

        NodeInstanceList nodeInstanceList = runtimeDataServiceBase.getProcessInstanceHistory(processInstanceId, active, completed, page, pageSize);
        logger.debug("Returning result of node instances search: {}", nodeInstanceList);
        return createCorrectVariant(nodeInstanceList, headers, Response.Status.OK, conversationIdHeader);
    }

    @GET
    @Path(VAR_INSTANCES_BY_INSTANCE_ID_GET_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getVariablesCurrentState(@Context HttpHeaders headers, @PathParam("pInstanceId") long processInstanceId) {

        // no container id available so only used to transfer conversation id if given by client
        Header conversationIdHeader = buildConversationIdHeader("", context, headers);

        VariableInstanceList variableInstanceList = runtimeDataServiceBase.getVariablesCurrentState(processInstanceId);
        logger.debug("Returning result of variables search: {}", variableInstanceList);

        return createCorrectVariant(variableInstanceList, headers, Response.Status.OK, conversationIdHeader);
    }

    @GET
    @Path(VAR_INSTANCES_BY_VAR_INSTANCE_ID_GET_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getVariableHistory(@Context HttpHeaders headers, @PathParam("pInstanceId") long processInstanceId,
            @PathParam("varName") String variableName,
            @QueryParam("page") @DefaultValue("0") Integer page, @QueryParam("pageSize") @DefaultValue("10") Integer pageSize) {

        // no container id available so only used to transfer conversation id if given by client
        Header conversationIdHeader = buildConversationIdHeader("", context, headers);

        VariableInstanceList variableInstanceList = runtimeDataServiceBase.getVariableHistory(processInstanceId, variableName, page, pageSize);
        logger.debug("Returning result of variable '{}; history search: {}", variableName, variableInstanceList);

        return createCorrectVariant(variableInstanceList, headers, Response.Status.OK, conversationIdHeader);
    }


    @GET
    @Path(PROCESS_DEFINITIONS_BY_CONTAINER_ID_GET_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getProcessesByDeploymentId(@Context HttpHeaders headers, @PathParam("id") String containerId,
            @QueryParam("page") @DefaultValue("0") Integer page, @QueryParam("pageSize") @DefaultValue("10") Integer pageSize,
            @QueryParam("sort") String sort, @QueryParam("sortOrder") @DefaultValue("true") boolean sortOrder) {

        // no container id available so only used to transfer conversation id if given by client
        Header conversationIdHeader = buildConversationIdHeader("", context, headers);

        ProcessDefinitionList processDefinitionList = runtimeDataServiceBase.getProcessesByDeploymentId(containerId, page, pageSize, sort, sortOrder);
        logger.debug("Returning result of process definition search: {}", processDefinitionList);

        return createCorrectVariant(processDefinitionList, headers, Response.Status.OK, conversationIdHeader);

    }

    @GET
    @Path(PROCESS_DEFINITIONS_GET_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getProcessesByFilter(@Context HttpHeaders headers, @QueryParam("filter") String filter,
            @QueryParam("page") @DefaultValue("0") Integer page, @QueryParam("pageSize") @DefaultValue("10") Integer pageSize,
            @QueryParam("sort") String sort, @QueryParam("sortOrder") @DefaultValue("true") boolean sortOrder) {

        // no container id available so only used to transfer conversation id if given by client
        Header conversationIdHeader = buildConversationIdHeader("", context, headers);

        ProcessDefinitionList processDefinitionList = runtimeDataServiceBase.getProcessesByFilter(filter, page, pageSize, sort, sortOrder);
        logger.debug("Returning result of process definition search: {}", processDefinitionList);

        return createCorrectVariant(processDefinitionList, headers, Response.Status.OK, conversationIdHeader);
    }

    @GET
    @Path(PROCESS_DEFINITIONS_BY_ID_GET_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getProcessesById(@Context HttpHeaders headers, @PathParam("pId") String processId) {

        // no container id available so only used to transfer conversation id if given by client
        Header conversationIdHeader = buildConversationIdHeader("", context, headers);

        ProcessDefinitionList processDefinitionList = runtimeDataServiceBase.getProcessesById(processId);
        logger.debug("Returning result of process definition search: {}", processDefinitionList);

        return createCorrectVariant(processDefinitionList, headers, Response.Status.OK, conversationIdHeader);
    }

    @GET
    @Path(PROCESS_DEFINITIONS_BY_CONTAINER_ID_DEF_ID_GET_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getProcessesByDeploymentIdProcessId(@Context HttpHeaders headers, @PathParam("id") String containerId, @PathParam("pId") String processId) {
        Variant v = getVariant(headers);
        // no container id available so only used to transfer conversation id if given by client
        Header conversationIdHeader = buildConversationIdHeader(containerId, context, headers);
        org.kie.server.api.model.definition.ProcessDefinition processDesc = null;
        try {

            processDesc = runtimeDataServiceBase.getProcessesByDeploymentIdProcessId(containerId, processId);
        } catch (IllegalArgumentException e) {
            return notFound(MessageFormat.format(PROCESS_DEFINITION_NOT_FOUND, processId, containerId), v, conversationIdHeader);
        }
        return createCorrectVariant(processDesc, headers, Response.Status.OK, conversationIdHeader);
    }

    @GET
    @Path(TASK_BY_WORK_ITEM_ID_GET_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getTaskByWorkItemId(@Context HttpHeaders headers, @PathParam("workItemId") Long workItemId) {
        Variant v = getVariant(headers);
        // no container id available so only used to transfer conversation id if given by client
        Header conversationIdHeader = buildConversationIdHeader("", context, headers);
        TaskInstance userTaskDesc = null;
        try {
            userTaskDesc = runtimeDataServiceBase.getTaskByWorkItemId(workItemId);
            return createCorrectVariant(userTaskDesc, headers, Response.Status.OK, conversationIdHeader);

        } catch (TaskNotFoundException e) {
            return notFound(MessageFormat.format(TASK_INSTANCE_NOT_FOUND_FOR_WORKITEM, workItemId), v, conversationIdHeader);
        } catch (Exception e) {
            logger.error("Unexpected error during processing {}", e.getMessage(), e);
            return internalServerError(MessageFormat.format(UNEXPECTED_ERROR, e.getMessage()), v, conversationIdHeader);
        }
    }

    @GET
    @Path(TASK_GET_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getTaskById(@Context HttpHeaders headers, @PathParam("tInstanceId") Long taskId) {
        Variant v = getVariant(headers);
        // no container id available so only used to transfer conversation id if given by client
        Header conversationIdHeader = buildConversationIdHeader("", context, headers);

        TaskInstance userTaskDesc = null;
        try {
            userTaskDesc = runtimeDataServiceBase.getTaskById(taskId);
            return createCorrectVariant(userTaskDesc, headers, Response.Status.OK, conversationIdHeader);

        } catch (TaskNotFoundException e) {
            return notFound(MessageFormat.format(TASK_INSTANCE_NOT_FOUND, taskId), v, conversationIdHeader);
        } catch (Exception e) {
            logger.error("Unexpected error during processing {}", e.getMessage(), e);
            return internalServerError(MessageFormat.format(UNEXPECTED_ERROR, e.getMessage()), v, conversationIdHeader);
        }
    }


    @GET
    @Path(TASKS_ASSIGN_BUSINESS_ADMINS_GET_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getTasksAssignedAsBusinessAdministratorByStatus(@Context HttpHeaders headers, @QueryParam("status") List<String> status,
            @QueryParam("user") String userId, @QueryParam("page") @DefaultValue("0") Integer page, @QueryParam("pageSize") @DefaultValue("10") Integer pageSize,
            @QueryParam("sort") String sort, @QueryParam("sortOrder") @DefaultValue("true") boolean sortOrder) {
        Variant v = getVariant(headers);
        // no container id available so only used to transfer conversation id if given by client
        Header conversationIdHeader = buildConversationIdHeader("", context, headers);
        try {

            TaskSummaryList result = runtimeDataServiceBase.getTasksAssignedAsBusinessAdministratorByStatus(status, userId, page, pageSize, sort, sortOrder);

            return createCorrectVariant(result, headers, Response.Status.OK, conversationIdHeader);

        } catch (Exception e) {
            logger.error("Unexpected error during processing {}", e.getMessage(), e);
            return internalServerError(MessageFormat.format(UNEXPECTED_ERROR, e.getMessage()), v, conversationIdHeader);
        }
    }

    @GET
    @Path(TASKS_ASSIGN_POT_OWNERS_GET_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getTasksAssignedAsPotentialOwner(@Context HttpHeaders headers,
            @QueryParam("status") List<String> status,  @QueryParam("groups") List<String> groupIds,
            @QueryParam("user") String userId,
            @QueryParam("page") @DefaultValue("0") Integer page, @QueryParam("pageSize") @DefaultValue("10") Integer pageSize,
            @QueryParam("sort") String sort, @QueryParam("sortOrder") @DefaultValue("true") boolean sortOrder,
            @QueryParam("filter") String filter) {

        Variant v = getVariant(headers);
        // no container id available so only used to transfer conversation id if given by client
        Header conversationIdHeader = buildConversationIdHeader("", context, headers);

        try {

            TaskSummaryList result = runtimeDataServiceBase.getTasksAssignedAsPotentialOwner(status, groupIds, userId, page, pageSize, sort, sortOrder, filter);

            return createCorrectVariant(result, headers, Response.Status.OK, conversationIdHeader);

        } catch (Exception e) {
            logger.error("Unexpected error during processing {}", e.getMessage(), e);
            return internalServerError(MessageFormat.format(UNEXPECTED_ERROR, e.getMessage()), v, conversationIdHeader);
        }

    }

    @GET
    @Path(TASKS_OWNED_GET_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getTasksOwnedByStatus(@Context HttpHeaders headers,
            @QueryParam("status") List<String> status, @QueryParam("user") String userId,
            @QueryParam("page") @DefaultValue("0") Integer page, @QueryParam("pageSize") @DefaultValue("10") Integer pageSize,
            @QueryParam("sort") String sort, @QueryParam("sortOrder") @DefaultValue("true") boolean sortOrder) {
        Variant v = getVariant(headers);
        // no container id available so only used to transfer conversation id if given by client
        Header conversationIdHeader = buildConversationIdHeader("", context, headers);

        try {

            TaskSummaryList result = runtimeDataServiceBase.getTasksOwnedByStatus(status, userId, page, pageSize, sort, sortOrder);

            return createCorrectVariant(result, headers, Response.Status.OK, conversationIdHeader);

        } catch (Exception e) {
            logger.error("Unexpected error during processing {}", e.getMessage(), e);
            return internalServerError(MessageFormat.format(UNEXPECTED_ERROR, e.getMessage()), v, conversationIdHeader);
        }
    }

    @GET
    @Path(TASK_BY_PROCESS_INST_ID_GET_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getTasksByStatusByProcessInstanceId(@Context HttpHeaders headers, @PathParam("pInstanceId") Long processInstanceId,
            @QueryParam("status") List<String> status,
            @QueryParam("page") @DefaultValue("0") Integer page, @QueryParam("pageSize") @DefaultValue("10") Integer pageSize,
            @QueryParam("sort") String sort, @QueryParam("sortOrder") @DefaultValue("true") boolean sortOrder) {
        Variant v = getVariant(headers);
        // no container id available so only used to transfer conversation id if given by client
        Header conversationIdHeader = buildConversationIdHeader("", context, headers);
        try {

            TaskSummaryList result = runtimeDataServiceBase.getTasksByStatusByProcessInstanceId(processInstanceId, status, page, pageSize, sort, sortOrder);

            return createCorrectVariant(result, headers, Response.Status.OK, conversationIdHeader);

        } catch (Exception e) {
            logger.error("Unexpected error during processing {}", e.getMessage(), e);
            return internalServerError(MessageFormat.format(UNEXPECTED_ERROR, e.getMessage()), v, conversationIdHeader);
        }
    }

    @GET
    @Path(TASKS_GET_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getAllAuditTask(@Context HttpHeaders headers, @QueryParam("user") String userId,
            @QueryParam("page") @DefaultValue("0") Integer page, @QueryParam("pageSize") @DefaultValue("10") Integer pageSize,
            @QueryParam("sort") String sort, @QueryParam("sortOrder") @DefaultValue("true") boolean sortOrder) {
        Variant v = getVariant(headers);
        // no container id available so only used to transfer conversation id if given by client
        Header conversationIdHeader = buildConversationIdHeader("", context, headers);

        try {

            TaskSummaryList result = runtimeDataServiceBase.getAllAuditTask(userId, page, pageSize, sort, sortOrder);

            return createCorrectVariant(result, headers, Response.Status.OK, conversationIdHeader);

        } catch (Exception e) {
            logger.error("Unexpected error during processing {}", e.getMessage(), e);
            return internalServerError(MessageFormat.format(UNEXPECTED_ERROR, e.getMessage()), v, conversationIdHeader);
        }
    }

    @GET
    @Path(TASKS_EVENTS_GET_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getTaskEvents(@Context HttpHeaders headers, @PathParam("tInstanceId") Long taskId,
            @QueryParam("page") @DefaultValue("0") Integer page, @QueryParam("pageSize") @DefaultValue("10") Integer pageSize,
            @QueryParam("sort") String sort, @QueryParam("sortOrder") @DefaultValue("true") boolean sortOrder) {
        Variant v = getVariant(headers);
        // no container id available so only used to transfer conversation id if given by client
        Header conversationIdHeader = buildConversationIdHeader("", context, headers);

        try {

            TaskEventInstanceList result = runtimeDataServiceBase.getTaskEvents(taskId, page, pageSize, sort, sortOrder);

            return createCorrectVariant(result, headers, Response.Status.OK, conversationIdHeader);

        } catch (Exception e) {
            logger.error("Unexpected error during processing {}", e.getMessage(), e);
            return internalServerError(MessageFormat.format(UNEXPECTED_ERROR, e.getMessage()), v, conversationIdHeader);
        }
    }

    @GET
    @Path(TASKS_BY_VAR_NAME_GET_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getTasksByVariables(@Context HttpHeaders headers, @PathParam("varName") String variableName, @QueryParam("varValue") String variableValue,
            @QueryParam("status")List<String> status, @QueryParam("user") String userId, @QueryParam("page") @DefaultValue("0") Integer page, @QueryParam("pageSize") @DefaultValue("10") Integer pageSize,
            @QueryParam("sort") String sort, @QueryParam("sortOrder") @DefaultValue("true") boolean sortOrder) {

        Variant v = getVariant(headers);
        // no container id available so only used to transfer conversation id if given by client
        Header conversationIdHeader = buildConversationIdHeader("", context, headers);

        try {

            TaskSummaryList result = runtimeDataServiceBase.getTasksByVariables(userId, variableName, variableValue, status, page, pageSize, sort, sortOrder);

            return createCorrectVariant(result, headers, Response.Status.OK, conversationIdHeader);

        } catch (Exception e) {
            logger.error("Unexpected error during processing {}", e.getMessage(), e);
            return internalServerError(MessageFormat.format(UNEXPECTED_ERROR, e.getMessage()), v, conversationIdHeader);
        }
    }
}
