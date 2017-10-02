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

import static org.kie.server.api.rest.RestURI.NODE_INSTANCES_BY_INSTANCE_ID_GET_URI;
import static org.kie.server.api.rest.RestURI.NODE_INSTANCES_BY_WORK_ITEM_ID_GET_URI;
import static org.kie.server.api.rest.RestURI.PROCESS_DEFINITIONS_BY_CONTAINER_ID_DEF_ID_GET_URI;
import static org.kie.server.api.rest.RestURI.PROCESS_DEFINITIONS_BY_CONTAINER_ID_GET_URI;
import static org.kie.server.api.rest.RestURI.PROCESS_DEFINITIONS_BY_ID_GET_URI;
import static org.kie.server.api.rest.RestURI.PROCESS_DEFINITIONS_GET_URI;
import static org.kie.server.api.rest.RestURI.PROCESS_INSTANCES_BY_CONTAINER_ID_GET_URI;
import static org.kie.server.api.rest.RestURI.PROCESS_INSTANCES_BY_CORRELATION_KEY_GET_URI;
import static org.kie.server.api.rest.RestURI.PROCESS_INSTANCES_BY_PROCESS_ID_GET_URI;
import static org.kie.server.api.rest.RestURI.PROCESS_INSTANCES_GET_URI;
import static org.kie.server.api.rest.RestURI.PROCESS_INSTANCE_BY_CORRELATION_KEY_GET_URI;
import static org.kie.server.api.rest.RestURI.PROCESS_INSTANCE_BY_INSTANCE_ID_GET_URI;
import static org.kie.server.api.rest.RestURI.PROCESS_INSTANCE_BY_VAR_NAME_GET_URI;
import static org.kie.server.api.rest.RestURI.QUERY_URI;
import static org.kie.server.api.rest.RestURI.TASKS_ASSIGN_BUSINESS_ADMINS_GET_URI;
import static org.kie.server.api.rest.RestURI.TASKS_ASSIGN_POT_OWNERS_GET_URI;
import static org.kie.server.api.rest.RestURI.TASKS_BY_VAR_NAME_GET_URI;
import static org.kie.server.api.rest.RestURI.TASKS_EVENTS_GET_URI;
import static org.kie.server.api.rest.RestURI.TASKS_GET_URI;
import static org.kie.server.api.rest.RestURI.TASKS_OWNED_GET_URI;
import static org.kie.server.api.rest.RestURI.TASK_BY_PROCESS_INST_ID_GET_URI;
import static org.kie.server.api.rest.RestURI.TASK_BY_WORK_ITEM_ID_GET_URI;
import static org.kie.server.api.rest.RestURI.TASK_GET_URI;
import static org.kie.server.api.rest.RestURI.VAR_INSTANCES_BY_INSTANCE_ID_GET_URI;
import static org.kie.server.api.rest.RestURI.VAR_INSTANCES_BY_VAR_INSTANCE_ID_GET_URI;
import static org.kie.server.remote.rest.common.util.RestUtils.buildConversationIdHeader;
import static org.kie.server.remote.rest.common.util.RestUtils.createCorrectVariant;
import static org.kie.server.remote.rest.common.util.RestUtils.getVariant;
import static org.kie.server.remote.rest.common.util.RestUtils.internalServerError;
import static org.kie.server.remote.rest.common.util.RestUtils.notFound;
import static org.kie.server.remote.rest.jbpm.resources.Messages.NODE_INSTANCE_NOT_FOUND;
import static org.kie.server.remote.rest.jbpm.resources.Messages.PROCESS_DEFINITION_NOT_FOUND;
import static org.kie.server.remote.rest.jbpm.resources.Messages.PROCESS_INSTANCE_NOT_FOUND;
import static org.kie.server.remote.rest.jbpm.resources.Messages.TASK_INSTANCE_NOT_FOUND;
import static org.kie.server.remote.rest.jbpm.resources.Messages.TASK_INSTANCE_NOT_FOUND_FOR_WORKITEM;
import static org.kie.server.remote.rest.jbpm.resources.Messages.UNEXPECTED_ERROR;

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
import org.kie.server.api.model.definition.ProcessDefinition;
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

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Api(value="Queries - processes, nodes, variables and tasks :: BPM")
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


    @ApiOperation(value="Retrieves process instances filtered by status, initiator, processName - depending what query parameters are given. Applies pagination by default and allows to specify sorting",
            response=ProcessInstanceList.class, code=200)
    @ApiResponses(value = { @ApiResponse(code = 500, message = "Unexpected error") })
    @GET
    @Path(PROCESS_INSTANCES_GET_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getProcessInstances(@Context HttpHeaders headers,
            @ApiParam(value = "optional process instance status (active, completed, aborted) - defaults ot active (1) only", required = false, allowableValues="1,2,3") @QueryParam("status") List<Integer> status, 
            @ApiParam(value = "optional process instance initiator - user who started process instance to filter process instances", required = false) @QueryParam("initiator") String initiator, 
            @ApiParam(value = "optional process name to filter process instances", required = false) @QueryParam("processName") String processName,
            @ApiParam(value = "optional pagination - at which page to start, defaults to 0 (meaning first)", required = false) @QueryParam("page") @DefaultValue("0") Integer page, 
            @ApiParam(value = "optional pagination - size of the result, defaults to 10", required = false) @QueryParam("pageSize") @DefaultValue("10") Integer pageSize,
            @ApiParam(value = "optional sort column, no default", required = false) @QueryParam("sort") String sort, 
            @ApiParam(value = "optional sort direction (asc, desc) - defaults to asc", required = false) @QueryParam("sortOrder") @DefaultValue("true") boolean sortOrder) {
        // no container id available so only used to transfer conversation id if given by client
        Header conversationIdHeader = buildConversationIdHeader("", context, headers);
        ProcessInstanceList processInstanceList = runtimeDataServiceBase.getProcessInstances(status, initiator, processName, page, pageSize, sort, sortOrder);
        logger.debug("Returning result of process instance search: {}", processInstanceList);

        return createCorrectVariant(processInstanceList, headers, Response.Status.OK, conversationIdHeader);
    }


    @ApiOperation(value="Retrieves process instances filtered by process id. Applies pagination by default and allows to specify sorting",
            response=ProcessInstanceList.class, code=200)
    @ApiResponses(value = { @ApiResponse(code = 500, message = "Unexpected error") })
    @GET
    @Path(PROCESS_INSTANCES_BY_PROCESS_ID_GET_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getProcessInstancesByProcessId(@Context HttpHeaders headers, 
            @ApiParam(value = "process id to filter process instance", required = true) @PathParam("pId")String processId,
            @ApiParam(value = "optional process instance status (active, completed, aborted) - defaults ot active (1) only", required = false, allowableValues="1,2,3") @QueryParam("status") List<Integer> status, 
            @ApiParam(value = "optinal process instance initiator - user who started process instance to filtr process instances", required = false) @QueryParam("initiator") String initiator,
            @ApiParam(value = "optional pagination - at which page to start, defaults to 0 (meaning first)", required = false) @QueryParam("page") @DefaultValue("0") Integer page, 
            @ApiParam(value = "optional pagination - size of the result, defaults to 10", required = false) @QueryParam("pageSize") @DefaultValue("10") Integer pageSize,
            @ApiParam(value = "optional sort column, no default", required = false) @QueryParam("sort") String sort, 
            @ApiParam(value = "optional sort direction (asc, desc) - defaults to asc", required = false) @QueryParam("sortOrder") @DefaultValue("true") boolean sortOrder) {
        // no container id available so only used to transfer conversation id if given by client
        Header conversationIdHeader = buildConversationIdHeader("", context, headers);

        ProcessInstanceList processInstanceList = runtimeDataServiceBase.getProcessInstancesByProcessId(processId, status, initiator, page, pageSize, sort, sortOrder);
        logger.debug("Returning result of process instance search: {}", processInstanceList);

        return createCorrectVariant(processInstanceList, headers, Response.Status.OK, conversationIdHeader);
    }

    @ApiOperation(value="Retrieves process instances filtered by container. Applies pagination by default and allows to specify sorting",
            response=ProcessInstanceList.class, code=200)
    @ApiResponses(value = { @ApiResponse(code = 500, message = "Unexpected error") })
    @GET
    @Path(PROCESS_INSTANCES_BY_CONTAINER_ID_GET_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getProcessInstancesByDeploymentId(@Context HttpHeaders headers, 
            @ApiParam(value = "container id to filter process instance", required = true) @PathParam("id") String containerId, 
            @ApiParam(value = "optional process instance status (active, completed, aborted) - defaults ot active (1) only", required = false, allowableValues="1,2,3") @QueryParam("status")List<Integer> status,
            @ApiParam(value = "optional pagination - at which page to start, defaults to 0 (meaning first)", required = false) @QueryParam("page") @DefaultValue("0") Integer page, 
            @ApiParam(value = "optional pagination - size of the result, defaults to 10", required = false) @QueryParam("pageSize") @DefaultValue("10") Integer pageSize,
            @ApiParam(value = "optional sort column, no default", required = false) @QueryParam("sort") String sort, 
            @ApiParam(value = "optional sort direction (asc, desc) - defaults to asc", required = false) @QueryParam("sortOrder") @DefaultValue("true") boolean sortOrder) {

        // no container id available so only used to transfer conversation id if given by client
        Header conversationIdHeader = buildConversationIdHeader("", context, headers);

        ProcessInstanceList processInstanceList = runtimeDataServiceBase.getProcessInstancesByDeploymentId(containerId, status, page, pageSize, sort, sortOrder);
        logger.debug("Returning result of process instance search: {}", processInstanceList);

        return createCorrectVariant(processInstanceList, headers, Response.Status.OK, conversationIdHeader);
    }

    @ApiOperation(value="Retrieves process instance by exactly matched correlation key",
            response=ProcessInstance.class, code=200)
    @ApiResponses(value = { @ApiResponse(code = 500, message = "Unexpected error") })
    @GET
    @Path(PROCESS_INSTANCE_BY_CORRELATION_KEY_GET_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getProcessInstanceByCorrelationKey(@Context HttpHeaders headers, 
            @ApiParam(value = "correlation key associated with process instance", required = true) @PathParam("correlationKey") String correlationKey) {
        Variant v = getVariant(headers);
        // no container id available so only used to transfer conversation id if given by client
        Header conversationIdHeader = buildConversationIdHeader("", context, headers);

        ProcessInstance processInstance = runtimeDataServiceBase.getProcessInstanceByCorrelationKey(correlationKey);
        if (processInstance == null) {

            return notFound(MessageFormat.format(PROCESS_INSTANCE_NOT_FOUND, correlationKey), v, conversationIdHeader);
        }
        return createCorrectVariant(processInstance, headers, Response.Status.OK, conversationIdHeader);

    }

    @ApiOperation(value="Retrieves process instances filtered by correlation key, retrieves all process instances that match correlationkey*. Applies pagination by default and allows to specify sorting",
            response=ProcessInstanceList.class, code=200)
    @ApiResponses(value = { @ApiResponse(code = 500, message = "Unexpected error") })
    @GET
    @Path(PROCESS_INSTANCES_BY_CORRELATION_KEY_GET_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getProcessInstancesByCorrelationKey(@Context HttpHeaders headers, 
            @ApiParam(value = "correlation key to filter process instance, can be given as partial correlation key like in starts with approach", required = true) @PathParam("correlationKey") String correlationKey, 
            @ApiParam(value = "optional pagination - at which page to start, defaults to 0 (meaning first)", required = false) @QueryParam("page") @DefaultValue("0") Integer page, 
            @ApiParam(value = "optional pagination - size of the result, defaults to 10", required = false) @QueryParam("pageSize") @DefaultValue("10") Integer pageSize,
            @ApiParam(value = "optional sort column, no default", required = false) @QueryParam("sort") String sort, 
            @ApiParam(value = "optional sort direction (asc, desc) - defaults to asc", required = false) @QueryParam("sortOrder") @DefaultValue("true") boolean sortOrder) {

        // no container id available so only used to transfer conversation id if given by client
        Header conversationIdHeader = buildConversationIdHeader("", context, headers);

        ProcessInstanceList processInstanceList = runtimeDataServiceBase.getProcessInstancesByCorrelationKey(correlationKey, page, pageSize, sort, sortOrder);

        return createCorrectVariant(processInstanceList, headers, Response.Status.OK, conversationIdHeader);

    }

    @ApiOperation(value="Retrieves process instances filtered by by variable or by variable and its value. Applies pagination by default and allows to specify sorting",
            response=ProcessInstanceList.class, code=200)
    @ApiResponses(value = { @ApiResponse(code = 500, message = "Unexpected error") })
    @GET
    @Path(PROCESS_INSTANCE_BY_VAR_NAME_GET_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getProcessInstanceByVariables(@Context HttpHeaders headers, 
            @ApiParam(value = "variable name to filter process instance", required = true) @PathParam("varName") String variableName, 
            @ApiParam(value = "variable value to filter process instance, optional when filtering by name only required when filtering by name and value", required = false) @QueryParam("varValue") String variableValue,
            @ApiParam(value = "optional process instance status (active, completed, aborted) - defaults ot active (1) only", required = false, allowableValues="1,2,3") @QueryParam("status")List<Integer> status, 
            @ApiParam(value = "optional pagination - at which page to start, defaults to 0 (meaning first)", required = false) @QueryParam("page") @DefaultValue("0") Integer page, 
            @ApiParam(value = "optional pagination - size of the result, defaults to 10", required = false) @QueryParam("pageSize") @DefaultValue("10") Integer pageSize,
            @ApiParam(value = "optional sort column, no default", required = false) @QueryParam("sort") String sort, 
            @ApiParam(value = "optional sort direction (asc, desc) - defaults to asc", required = false) @QueryParam("sortOrder") @DefaultValue("true") boolean sortOrder) {

        // no container id available so only used to transfer conversation id if given by client
        Header conversationIdHeader = buildConversationIdHeader("", context, headers);

        ProcessInstanceList processInstanceList = runtimeDataServiceBase.getProcessInstanceByVariables(variableName, variableValue, status, page, pageSize, sort, sortOrder);
        logger.debug("Returning result of process instance search: {}", processInstanceList);

        return createCorrectVariant(processInstanceList, headers, Response.Status.OK, conversationIdHeader);
    }

    @ApiOperation(value="Retrieves process instance for given process instance id and optionally loads its variables",
            response=ProcessInstance.class, code=200)
    @ApiResponses(value = { @ApiResponse(code = 500, message = "Unexpected error"), @ApiResponse(code = 404, message = "Process instance id not found") })
    @GET
    @Path(PROCESS_INSTANCE_BY_INSTANCE_ID_GET_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getProcessInstanceById(@Context HttpHeaders headers, 
            @ApiParam(value = "process instance id to retrieve process instance", required = true) @PathParam("pInstanceId") long processInstanceId, 
            @ApiParam(value = "load process instance variables or not, defaults to false", required = false) @QueryParam("withVars") boolean withVars) {
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

    @ApiOperation(value="Retrieves node instance for given process instance id and work item id",
            response=NodeInstance.class, code=200)
    @ApiResponses(value = { @ApiResponse(code = 500, message = "Unexpected error"), @ApiResponse(code = 404, message = "Node instance id not found") })
    @GET
    @Path(NODE_INSTANCES_BY_WORK_ITEM_ID_GET_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getNodeInstanceForWorkItem(@Context HttpHeaders headers, 
            @ApiParam(value = "process instance id that work item belongs to", required = true) @PathParam("pInstanceId") long processInstanceId, 
            @ApiParam(value = "work item id to retrieve node instance for", required = true) @PathParam("workItemId") long workItemId) {
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

    @ApiOperation(value="Retrieves node instances for given process instance. Depending on provided query parameters (activeOnly or completedOnly) will return active and/or completes nodes",
            response=NodeInstanceList.class, code=200)
    @ApiResponses(value = { @ApiResponse(code = 500, message = "Unexpected error")})
    @GET
    @Path(NODE_INSTANCES_BY_INSTANCE_ID_GET_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getProcessInstanceHistory(@Context HttpHeaders headers, 
            @ApiParam(value = "process instance id to to retrive history for", required = true) @PathParam("pInstanceId") long processInstanceId,
            @ApiParam(value = "include active nodes only", required = false) @QueryParam("activeOnly") Boolean active, 
            @ApiParam(value = "include completed nodes only", required = false) @QueryParam("completedOnly")Boolean completed,
            @ApiParam(value = "optional pagination - at which page to start, defaults to 0 (meaning first)", required = false) @QueryParam("page") @DefaultValue("0") Integer page, 
            @ApiParam(value = "optional pagination - size of the result, defaults to 10", required = false) @QueryParam("pageSize") @DefaultValue("10") Integer pageSize) {

        // no container id available so only used to transfer conversation id if given by client
        Header conversationIdHeader = buildConversationIdHeader("", context, headers);

        NodeInstanceList nodeInstanceList = runtimeDataServiceBase.getProcessInstanceHistory(processInstanceId, active, completed, page, pageSize);
        logger.debug("Returning result of node instances search: {}", nodeInstanceList);
        return createCorrectVariant(nodeInstanceList, headers, Response.Status.OK, conversationIdHeader);
    }

    @ApiOperation(value="Retrieves variables last value (from audit logs) for given process instance",
            response=VariableInstanceList.class, code=200)
    @ApiResponses(value = { @ApiResponse(code = 500, message = "Unexpected error")})
    @GET
    @Path(VAR_INSTANCES_BY_INSTANCE_ID_GET_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getVariablesCurrentState(@Context HttpHeaders headers, 
            @ApiParam(value = "process instance id to load variables current state (latest value) for", required = true) @PathParam("pInstanceId") long processInstanceId) {

        // no container id available so only used to transfer conversation id if given by client
        Header conversationIdHeader = buildConversationIdHeader("", context, headers);

        VariableInstanceList variableInstanceList = runtimeDataServiceBase.getVariablesCurrentState(processInstanceId);
        logger.debug("Returning result of variables search: {}", variableInstanceList);

        return createCorrectVariant(variableInstanceList, headers, Response.Status.OK, conversationIdHeader);
    }

    @ApiOperation(value="Retrieves variable history (from audit logs) for given variable name that belongs to process instance",
            response=VariableInstanceList.class, code=200)
    @ApiResponses(value = { @ApiResponse(code = 500, message = "Unexpected error")})
    @GET
    @Path(VAR_INSTANCES_BY_VAR_INSTANCE_ID_GET_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getVariableHistory(@Context HttpHeaders headers, 
            @ApiParam(value = "process instance id to load variable history for", required = true) @PathParam("pInstanceId") long processInstanceId,
            @ApiParam(value = "variable name that history should be loaded for", required = true) @PathParam("varName") String variableName,
            @ApiParam(value = "optional pagination - at which page to start, defaults to 0 (meaning first)", required = false) @QueryParam("page") @DefaultValue("0") Integer page, 
            @ApiParam(value = "optional pagination - size of the result, defaults to 10", required = false) @QueryParam("pageSize") @DefaultValue("10") Integer pageSize) {

        // no container id available so only used to transfer conversation id if given by client
        Header conversationIdHeader = buildConversationIdHeader("", context, headers);

        VariableInstanceList variableInstanceList = runtimeDataServiceBase.getVariableHistory(processInstanceId, variableName, page, pageSize);
        logger.debug("Returning result of variable '{}; history search: {}", variableName, variableInstanceList);

        return createCorrectVariant(variableInstanceList, headers, Response.Status.OK, conversationIdHeader);
    }

    @ApiOperation(value="Retrieves process definitions that belong to given container",
            response=ProcessDefinitionList.class, code=200)
    @ApiResponses(value = { @ApiResponse(code = 500, message = "Unexpected error")})
    @GET
    @Path(PROCESS_DEFINITIONS_BY_CONTAINER_ID_GET_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getProcessesByDeploymentId(@Context HttpHeaders headers, 
            @ApiParam(value = "container id to filter process definitions", required = true) @PathParam("id") String containerId,
            @ApiParam(value = "optional pagination - at which page to start, defaults to 0 (meaning first)", required = false) @QueryParam("page") @DefaultValue("0") Integer page, 
            @ApiParam(value = "optional pagination - size of the result, defaults to 10", required = false) @QueryParam("pageSize") @DefaultValue("10") Integer pageSize,
            @ApiParam(value = "optional sort column, no default", required = false) @QueryParam("sort") String sort, 
            @ApiParam(value = "optional sort direction (asc, desc) - defaults to asc", required = false) @QueryParam("sortOrder") @DefaultValue("true") boolean sortOrder) {

        // no container id available so only used to transfer conversation id if given by client
        Header conversationIdHeader = buildConversationIdHeader("", context, headers);

        ProcessDefinitionList processDefinitionList = runtimeDataServiceBase.getProcessesByDeploymentId(containerId, page, pageSize, sort, sortOrder);
        logger.debug("Returning result of process definition search: {}", processDefinitionList);

        return createCorrectVariant(processDefinitionList, headers, Response.Status.OK, conversationIdHeader);

    }

    @ApiOperation(value="Retrieves process definitions filtered by process id or name",
            response=ProcessDefinitionList.class, code=200)
    @ApiResponses(value = { @ApiResponse(code = 500, message = "Unexpected error")})
    @GET
    @Path(PROCESS_DEFINITIONS_GET_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getProcessesByFilter(@Context HttpHeaders headers, 
            @ApiParam(value = "process id or name to filter process definitions", required = true) @QueryParam("filter") String filter,
            @ApiParam(value = "optional pagination - at which page to start, defaults to 0 (meaning first)", required = false) @QueryParam("page") @DefaultValue("0") Integer page, 
            @ApiParam(value = "optional pagination - size of the result, defaults to 10", required = false) @QueryParam("pageSize") @DefaultValue("10") Integer pageSize,
            @ApiParam(value = "optional sort column, no default", required = false) @QueryParam("sort") String sort, 
            @ApiParam(value = "optional sort direction (asc, desc) - defaults to asc", required = false) @QueryParam("sortOrder") @DefaultValue("true") boolean sortOrder) {

        // no container id available so only used to transfer conversation id if given by client
        Header conversationIdHeader = buildConversationIdHeader("", context, headers);

        ProcessDefinitionList processDefinitionList = runtimeDataServiceBase.getProcessesByFilter(filter, page, pageSize, sort, sortOrder);
        logger.debug("Returning result of process definition search: {}", processDefinitionList);

        return createCorrectVariant(processDefinitionList, headers, Response.Status.OK, conversationIdHeader);
    }

    @ApiOperation(value="Retrieves process definitions filtered by process id",
            response=ProcessDefinitionList.class, code=200)
    @ApiResponses(value = { @ApiResponse(code = 500, message = "Unexpected error")})
    @GET
    @Path(PROCESS_DEFINITIONS_BY_ID_GET_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getProcessesById(@Context HttpHeaders headers, 
            @ApiParam(value = "process id to load process definition", required = true) @PathParam("pId") String processId) {

        // no container id available so only used to transfer conversation id if given by client
        Header conversationIdHeader = buildConversationIdHeader("", context, headers);

        ProcessDefinitionList processDefinitionList = runtimeDataServiceBase.getProcessesById(processId);
        logger.debug("Returning result of process definition search: {}", processDefinitionList);

        return createCorrectVariant(processDefinitionList, headers, Response.Status.OK, conversationIdHeader);
    }

    @ApiOperation(value="Retrieves process definition that belong to given container and has matching process id",
            response=ProcessDefinition.class, code=200)
    @ApiResponses(value = { @ApiResponse(code = 500, message = "Unexpected error")})
    @GET
    @Path(PROCESS_DEFINITIONS_BY_CONTAINER_ID_DEF_ID_GET_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getProcessesByDeploymentIdProcessId(@Context HttpHeaders headers, 
            @ApiParam(value = "container id that process definition belongs to", required = true) @PathParam("id") String containerId, 
            @ApiParam(value = "process id to load process definition", required = true) @PathParam("pId") String processId) {
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

    @ApiOperation(value="Retrieves task by associated work item id",
            response=TaskInstance.class, code=200)
    @ApiResponses(value = { @ApiResponse(code = 500, message = "Unexpected error"), @ApiResponse(code = 404, message = "Task not found for given work item id")})
    @GET
    @Path(TASK_BY_WORK_ITEM_ID_GET_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getTaskByWorkItemId(@Context HttpHeaders headers, 
            @ApiParam(value = "work item id to load task associated with", required = true) @PathParam("workItemId") Long workItemId) {
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

    @ApiOperation(value="Retrieves task by task id",
            response=TaskInstance.class, code=200)
    @ApiResponses(value = { @ApiResponse(code = 500, message = "Unexpected error"), @ApiResponse(code = 404, message = "Task not found for given id")})
    @GET
    @Path(TASK_GET_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getTaskById(@Context HttpHeaders headers, 
            @ApiParam(value = "task id to load task instance", required = true) @PathParam("tInstanceId") Long taskId) {
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

    @ApiOperation(value="Retrieves tasks assigned as business administrator, optionally filters by status and applies pagination",
            response=TaskSummaryList.class, code=200)
    @ApiResponses(value = { @ApiResponse(code = 500, message = "Unexpected error")})
    @GET
    @Path(TASKS_ASSIGN_BUSINESS_ADMINS_GET_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getTasksAssignedAsBusinessAdministratorByStatus(@Context HttpHeaders headers, 
            @ApiParam(value = "optional task status (Created, Ready, Reserved, InProgress, Suspended, Completed, Failed, Error, Exited, Obsolete)", required = false, allowableValues="Created, Ready, Reserved,InProgress,Suspended,Completed,Failed,Error,Exited,Obsolete") @QueryParam("status") List<String> status,
            @ApiParam(value = "optional user id to be used instead of authenticated user - only when bypass authenticated user is enabled", required = false) @QueryParam("user") String userId, 
            @ApiParam(value = "optional pagination - at which page to start, defaults to 0 (meaning first)", required = false) @QueryParam("page") @DefaultValue("0") Integer page, 
            @ApiParam(value = "optional pagination - size of the result, defaults to 10", required = false) @QueryParam("pageSize") @DefaultValue("10") Integer pageSize,
            @ApiParam(value = "optional sort column, no default", required = false) @QueryParam("sort") String sort, 
            @ApiParam(value = "optional sort direction (asc, desc) - defaults to asc", required = false) @QueryParam("sortOrder") @DefaultValue("true") boolean sortOrder) {
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

    @ApiOperation(value="Retrieves tasks assigned as potential owner, optionally filters by status and given groups and applies pagination",
            response=TaskSummaryList.class, code=200)
    @ApiResponses(value = { @ApiResponse(code = 500, message = "Unexpected error")})
    @GET
    @Path(TASKS_ASSIGN_POT_OWNERS_GET_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getTasksAssignedAsPotentialOwner(@Context HttpHeaders headers,
            @ApiParam(value = "optional task status (Created, Ready, Reserved, InProgress, Suspended, Completed, Failed, Error, Exited, Obsolete)", required = false, allowableValues="Created, Ready, Reserved,InProgress,Suspended,Completed,Failed,Error,Exited,Obsolete") @QueryParam("status") List<String> status,  
            @ApiParam(value = "optional group names to include in the query", required = true, allowMultiple=true) @QueryParam("groups") List<String> groupIds,
            @ApiParam(value = "optional user id to be used instead of authenticated user - only when bypass authenticated user is enabled", required = false) @QueryParam("user") String userId,
            @ApiParam(value = "optional pagination - at which page to start, defaults to 0 (meaning first)", required = false) @QueryParam("page") @DefaultValue("0") Integer page, 
            @ApiParam(value = "optional pagination - size of the result, defaults to 10", required = false) @QueryParam("pageSize") @DefaultValue("10") Integer pageSize,
            @ApiParam(value = "optional sort column, no default", required = false) @QueryParam("sort") String sort, 
            @ApiParam(value = "optional sort direction (asc, desc) - defaults to asc", required = false) @QueryParam("sortOrder") @DefaultValue("true") boolean sortOrder,
            @ApiParam(value = "optional custom filter for task data", required = false) @QueryParam("filter") String filter) {

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

    @ApiOperation(value="Retrieves tasks owned, optionally filters by status and applies pagination",
            response=TaskSummaryList.class, code=200)
    @ApiResponses(value = { @ApiResponse(code = 500, message = "Unexpected error")})
    @GET
    @Path(TASKS_OWNED_GET_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getTasksOwnedByStatus(@Context HttpHeaders headers,
            @ApiParam(value = "optional task status (Created, Ready, Reserved, InProgress, Suspended, Completed, Failed, Error, Exited, Obsolete)", required = false, allowableValues="Created, Ready, Reserved,InProgress,Suspended,Completed,Failed,Error,Exited,Obsolete") @QueryParam("status") List<String> status, 
            @ApiParam(value = "optional user id to be used instead of authenticated user - only when bypass authenticated user is enabled", required = false) @QueryParam("user") String userId,
            @ApiParam(value = "optional pagination - at which page to start, defaults to 0 (meaning first)", required = false) @QueryParam("page") @DefaultValue("0") Integer page, 
            @ApiParam(value = "optional pagination - size of the result, defaults to 10", required = false) @QueryParam("pageSize") @DefaultValue("10") Integer pageSize,
            @ApiParam(value = "optional sort column, no default", required = false) @QueryParam("sort") String sort, 
            @ApiParam(value = "optional sort direction (asc, desc) - defaults to asc", required = false) @QueryParam("sortOrder") @DefaultValue("true") boolean sortOrder) {
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

    @ApiOperation(value="Retrieves tasks associated with given process instance, optionally filters by status and applies pagination",
            response=TaskSummaryList.class, code=200)
    @ApiResponses(value = { @ApiResponse(code = 500, message = "Unexpected error")})
    @GET
    @Path(TASK_BY_PROCESS_INST_ID_GET_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getTasksByStatusByProcessInstanceId(@Context HttpHeaders headers, 
            @ApiParam(value = "process instance id to filter task instances", required = true) @PathParam("pInstanceId") Long processInstanceId,
            @ApiParam(value = "optional task status (Created, Ready, Reserved, InProgress, Suspended, Completed, Failed, Error, Exited, Obsolete)", required = false, allowableValues="Created, Ready, Reserved,InProgress,Suspended,Completed,Failed,Error,Exited,Obsolete") @QueryParam("status") List<String> status,
            @ApiParam(value = "optional pagination - at which page to start, defaults to 0 (meaning first)", required = false) @QueryParam("page") @DefaultValue("0") Integer page, 
            @ApiParam(value = "optional pagination - size of the result, defaults to 10", required = false) @QueryParam("pageSize") @DefaultValue("10") Integer pageSize,
            @ApiParam(value = "optional sort column, no default", required = false) @QueryParam("sort") String sort, 
            @ApiParam(value = "optional sort direction (asc, desc) - defaults to asc", required = false) @QueryParam("sortOrder") @DefaultValue("true") boolean sortOrder) {
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

    @ApiOperation(value="Retrieves tasks, optionally filters by status and applies pagination",
            response=TaskSummaryList.class, code=200)
    @ApiResponses(value = { @ApiResponse(code = 500, message = "Unexpected error")})
    @GET
    @Path(TASKS_GET_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getAllAuditTask(@Context HttpHeaders headers, 
            @ApiParam(value = "optional user id to be used instead of authenticated user - only when bypass authenticated user is enabled", required = false) @QueryParam("user") String userId,
            @ApiParam(value = "optional pagination - at which page to start, defaults to 0 (meaning first)", required = false) @QueryParam("page") @DefaultValue("0") Integer page, 
            @ApiParam(value = "optional pagination - size of the result, defaults to 10", required = false) @QueryParam("pageSize") @DefaultValue("10") Integer pageSize,
            @ApiParam(value = "optional sort column, no default", required = false) @QueryParam("sort") String sort, 
            @ApiParam(value = "optional sort direction (asc, desc) - defaults to asc", required = false) @QueryParam("sortOrder") @DefaultValue("true") boolean sortOrder) {
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

    @ApiOperation(value="Retrieves task events for given task id and applies pagination",
            response=TaskEventInstanceList.class, code=200)
    @ApiResponses(value = { @ApiResponse(code = 500, message = "Unexpected error")})
    @GET
    @Path(TASKS_EVENTS_GET_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getTaskEvents(@Context HttpHeaders headers, 
            @ApiParam(value = "task id to load task events for", required = true) @PathParam("tInstanceId") Long taskId,
            @ApiParam(value = "optional pagination - at which page to start, defaults to 0 (meaning first)", required = false) @QueryParam("page") @DefaultValue("0") Integer page, 
            @ApiParam(value = "optional pagination - size of the result, defaults to 10", required = false) @QueryParam("pageSize") @DefaultValue("10") Integer pageSize,
            @ApiParam(value = "optional sort column, no default", required = false) @QueryParam("sort") String sort, 
            @ApiParam(value = "optional sort direction (asc, desc) - defaults to asc", required = false) @QueryParam("sortOrder") @DefaultValue("true") boolean sortOrder) {
        Variant v = getVariant(headers);
        // no container id available so only used to transfer conversation id if given by client
        Header conversationIdHeader = buildConversationIdHeader("", context, headers);
        try {
            TaskEventInstanceList result = runtimeDataServiceBase.getTaskEvents(taskId, page, pageSize, sort, sortOrder);
            return createCorrectVariant(result, headers, Response.Status.OK, conversationIdHeader);
        } catch (TaskNotFoundException e) {
            return notFound(MessageFormat.format(TASK_INSTANCE_NOT_FOUND, taskId), v, conversationIdHeader);
        } catch (Exception e) {
            logger.error("Unexpected error during processing {}", e.getMessage(), e);
            return internalServerError(MessageFormat.format(UNEXPECTED_ERROR, e.getMessage()), v, conversationIdHeader);
        }
    }

    @ApiOperation(value="Retrieves tasks by variable name and optionally by variable value, optionally filters by status and applies pagination",
            response=TaskSummaryList.class, code=200)
    @ApiResponses(value = { @ApiResponse(code = 500, message = "Unexpected error")})
    @GET
    @Path(TASKS_BY_VAR_NAME_GET_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getTasksByVariables(@Context HttpHeaders headers, 
            @ApiParam(value = "name of the variable used to fiter tasks", required = true) @PathParam("varName") String variableName, 
            @ApiParam(value = "value of the variable used to fiter tasks, optional when filtering only by name, required when filtering by both name and value", required = false) @QueryParam("varValue") String variableValue,
            @ApiParam(value = "optional task status (Created, Ready, Reserved, InProgress, Suspended, Completed, Failed, Error, Exited, Obsolete)", required = false, allowableValues="Created, Ready, Reserved,InProgress,Suspended,Completed,Failed,Error,Exited,Obsolete") @QueryParam("status")List<String> status, 
            @ApiParam(value = "optional user id to be used instead of authenticated user - only when bypass authenticated user is enabled", required = false) @QueryParam("user") String userId, 
            @ApiParam(value = "optional pagination - at which page to start, defaults to 0 (meaning first)", required = false) @QueryParam("page") @DefaultValue("0") Integer page, 
            @ApiParam(value = "optional pagination - size of the result, defaults to 10", required = false) @QueryParam("pageSize") @DefaultValue("10") Integer pageSize,
            @ApiParam(value = "optional sort column, no default", required = false) @QueryParam("sort") String sort,
            @ApiParam(value = "optional sort direction (asc, desc) - defaults to asc", required = false) @QueryParam("sortOrder") @DefaultValue("true") boolean sortOrder) {

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
