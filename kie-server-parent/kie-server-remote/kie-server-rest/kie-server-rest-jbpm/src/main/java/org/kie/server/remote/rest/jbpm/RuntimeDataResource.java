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

import static org.kie.server.api.rest.RestURI.CONTAINER_ID;
import static org.kie.server.api.rest.RestURI.NODE_INSTANCES_BY_INSTANCE_ID_GET_URI;
import static org.kie.server.api.rest.RestURI.NODE_INSTANCES_BY_WORK_ITEM_ID_GET_URI;
import static org.kie.server.api.rest.RestURI.PROCESS_DEFINITIONS_BY_CONTAINER_ID_DEF_ID_GET_URI;
import static org.kie.server.api.rest.RestURI.PROCESS_DEFINITIONS_BY_CONTAINER_ID_GET_URI;
import static org.kie.server.api.rest.RestURI.PROCESS_DEFINITIONS_BY_ID_GET_URI;
import static org.kie.server.api.rest.RestURI.PROCESS_DEFINITIONS_GET_URI;
import static org.kie.server.api.rest.RestURI.PROCESS_ID;
import static org.kie.server.api.rest.RestURI.PROCESS_INSTANCES_BY_CONTAINER_ID_GET_URI;
import static org.kie.server.api.rest.RestURI.PROCESS_INSTANCES_BY_CORRELATION_KEY_GET_URI;
import static org.kie.server.api.rest.RestURI.PROCESS_INSTANCES_BY_PROCESS_ID_GET_URI;
import static org.kie.server.api.rest.RestURI.PROCESS_INSTANCES_GET_URI;
import static org.kie.server.api.rest.RestURI.PROCESS_INSTANCE_BY_CORRELATION_KEY_GET_URI;
import static org.kie.server.api.rest.RestURI.PROCESS_INSTANCE_BY_INSTANCE_ID_GET_URI;
import static org.kie.server.api.rest.RestURI.PROCESS_INSTANCE_BY_VAR_NAME_GET_URI;
import static org.kie.server.api.rest.RestURI.PROCESS_INST_ID;
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
import static org.kie.server.api.rest.RestURI.TASK_INSTANCE_ID;
import static org.kie.server.api.rest.RestURI.VAR_INSTANCES_BY_INSTANCE_ID_GET_URI;
import static org.kie.server.api.rest.RestURI.VAR_INSTANCES_BY_VAR_INSTANCE_ID_GET_URI;
import static org.kie.server.remote.rest.common.util.RestUtils.buildConversationIdHeader;
import static org.kie.server.remote.rest.common.util.RestUtils.createCorrectVariant;
import static org.kie.server.remote.rest.common.util.RestUtils.getVariant;
import static org.kie.server.remote.rest.common.util.RestUtils.internalServerError;
import static org.kie.server.remote.rest.common.util.RestUtils.notFound;
import static org.kie.server.remote.rest.jbpm.docs.ParameterSamples.GET_PROCESS_DEFS_RESPONSE_JSON;
import static org.kie.server.remote.rest.jbpm.docs.ParameterSamples.GET_PROCESS_DEF_RESPONSE_JSON;
import static org.kie.server.remote.rest.jbpm.docs.ParameterSamples.GET_PROCESS_INSTANCES_RESPONSE_JSON;
import static org.kie.server.remote.rest.jbpm.docs.ParameterSamples.GET_PROCESS_INSTANCE_NODES_RESPONSE_JSON;
import static org.kie.server.remote.rest.jbpm.docs.ParameterSamples.GET_PROCESS_INSTANCE_NODE_RESPONSE_JSON;
import static org.kie.server.remote.rest.jbpm.docs.ParameterSamples.GET_PROCESS_INSTANCE_RESPONSE_JSON;
import static org.kie.server.remote.rest.jbpm.docs.ParameterSamples.GET_PROCESS_INSTANCE_VARS_LOG_RESPONSE_JSON;
import static org.kie.server.remote.rest.jbpm.docs.ParameterSamples.GET_TASK_EVENTS_RESPONSE_JSON;
import static org.kie.server.remote.rest.jbpm.docs.ParameterSamples.GET_TASK_RESPONSE_JSON;
import static org.kie.server.remote.rest.jbpm.docs.ParameterSamples.GET_TASK_SUMMARY_RESPONSE_JSON;
import static org.kie.server.remote.rest.jbpm.docs.ParameterSamples.JSON;
import static org.kie.server.remote.rest.jbpm.resources.Messages.CONTAINER_NOT_FOUND;
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

import org.jbpm.services.api.DeploymentNotFoundException;
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
import io.swagger.annotations.Example;
import io.swagger.annotations.ExampleProperty;

@Api(value="Process queries")
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


    @ApiOperation(value="Returns all process instances filtered by optional parameters.",
            response=ProcessInstanceList.class, code=200)
    @ApiResponses(value = { @ApiResponse(code = 500, message = "Unexpected error"), 
            @ApiResponse(code = 200, message = "Successfull response", examples=@Example(value= {
                    @ExampleProperty(mediaType=JSON, value=GET_PROCESS_INSTANCES_RESPONSE_JSON)})) })
    @GET
    @Path(PROCESS_INSTANCES_GET_URI)
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
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


    @ApiOperation(value="Returns all process instances for a specified process.",
            response=ProcessInstanceList.class, code=200)
    @ApiResponses(value = { @ApiResponse(code = 500, message = "Unexpected error"), 
            @ApiResponse(code = 200, message = "Successfull response", examples=@Example(value= {
                    @ExampleProperty(mediaType=JSON, value=GET_PROCESS_INSTANCES_RESPONSE_JSON)})) })
    @GET
    @Path(PROCESS_INSTANCES_BY_PROCESS_ID_GET_URI)
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getProcessInstancesByProcessId(@Context HttpHeaders headers, 
            @ApiParam(value = "process id to filter process instance", required = true) @PathParam(PROCESS_ID)String processId,
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

    @ApiOperation(value="Returns all process instances for a specified KIE container.",
            response=ProcessInstanceList.class, code=200)
    @ApiResponses(value = { @ApiResponse(code = 500, message = "Unexpected error"),
                            @ApiResponse(code = 404, message = "Container Id not found"), 
                            @ApiResponse(code = 200, message = "Successfull response", examples=@Example(value= {
                                    @ExampleProperty(mediaType=JSON, value=GET_PROCESS_INSTANCES_RESPONSE_JSON)}))})
    @GET
    @Path(PROCESS_INSTANCES_BY_CONTAINER_ID_GET_URI)
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getProcessInstancesByDeploymentId(@Context HttpHeaders headers, 
            @ApiParam(value = "container id to filter process instance", required = true) @PathParam(CONTAINER_ID) String containerId, 
            @ApiParam(value = "optional process instance status (active, completed, aborted) - defaults ot active (1) only", required = false, allowableValues="1,2,3") @QueryParam("status")List<Integer> status,
            @ApiParam(value = "optional pagination - at which page to start, defaults to 0 (meaning first)", required = false) @QueryParam("page") @DefaultValue("0") Integer page, 
            @ApiParam(value = "optional pagination - size of the result, defaults to 10", required = false) @QueryParam("pageSize") @DefaultValue("10") Integer pageSize,
            @ApiParam(value = "optional sort column, no default", required = false) @QueryParam("sort") String sort, 
            @ApiParam(value = "optional sort direction (asc, desc) - defaults to asc", required = false) @QueryParam("sortOrder") @DefaultValue("true") boolean sortOrder) {

        // no container id available so only used to transfer conversation id if given by client
        Header conversationIdHeader = buildConversationIdHeader("", context, headers);
        Variant v = getVariant(headers);
        try {
            ProcessInstanceList processInstanceList = runtimeDataServiceBase.getProcessInstancesByDeploymentId(containerId, status, page, pageSize, sort, sortOrder);
            logger.debug("Returning result of process instance search: {}", processInstanceList);

            return createCorrectVariant(processInstanceList, headers, Response.Status.OK, conversationIdHeader);
        
        } catch (DeploymentNotFoundException e) {
            return notFound(MessageFormat.format(CONTAINER_NOT_FOUND, containerId), v, conversationIdHeader);
        } catch (Exception e) {
            logger.error("Unexpected error during processing {}", e.getMessage(), e);
            return internalServerError(MessageFormat.format(UNEXPECTED_ERROR, e.getMessage()), v, conversationIdHeader);
        }
    }

    @ApiOperation(value="Returns information about a single process instance with a specified correlation key.",
            response=ProcessInstance.class, code=200)
    @ApiResponses(value = { @ApiResponse(code = 500, message = "Unexpected error"), 
            @ApiResponse(code = 200, message = "Successfull response", examples=@Example(value= {
                    @ExampleProperty(mediaType=JSON, value=GET_PROCESS_INSTANCE_RESPONSE_JSON)})) })
    @GET
    @Path(PROCESS_INSTANCE_BY_CORRELATION_KEY_GET_URI)
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
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

    @ApiOperation(value="Returns process instances with a specified correlation key.",
            response=ProcessInstanceList.class, code=200)
    @ApiResponses(value = { @ApiResponse(code = 500, message = "Unexpected error"), 
            @ApiResponse(code = 200, message = "Successfull response", examples=@Example(value= {
                    @ExampleProperty(mediaType=JSON, value=GET_PROCESS_INSTANCES_RESPONSE_JSON)})) })
    @GET
    @Path(PROCESS_INSTANCES_BY_CORRELATION_KEY_GET_URI)
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
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

    @ApiOperation(value="Returns process instances with a specified variable.",
            response=ProcessInstanceList.class, code=200)
    @ApiResponses(value = { @ApiResponse(code = 500, message = "Unexpected error"), 
            @ApiResponse(code = 200, message = "Successfull response", examples=@Example(value= {
                    @ExampleProperty(mediaType=JSON, value=GET_PROCESS_INSTANCES_RESPONSE_JSON)})) })
    @GET
    @Path(PROCESS_INSTANCE_BY_VAR_NAME_GET_URI)
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
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

    @ApiOperation(value="Returns information about a specified process instance.",
            response=ProcessInstance.class, code=200)
    @ApiResponses(value = { @ApiResponse(code = 500, message = "Unexpected error"), @ApiResponse(code = 404, message = "Process instance id not found"), 
            @ApiResponse(code = 200, message = "Successfull response", examples=@Example(value= {
                    @ExampleProperty(mediaType=JSON, value=GET_PROCESS_INSTANCE_RESPONSE_JSON)})) })
    @GET
    @Path(PROCESS_INSTANCE_BY_INSTANCE_ID_GET_URI)
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getProcessInstanceById(@Context HttpHeaders headers, 
            @ApiParam(value = "process instance id to retrieve process instance", required = true) @PathParam(PROCESS_INST_ID) long processInstanceId, 
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

    @ApiOperation(value="Returns node instances for a specified work item in a specified process instance.",
            response=NodeInstance.class, code=200)
    @ApiResponses(value = { @ApiResponse(code = 500, message = "Unexpected error"), @ApiResponse(code = 404, message = "Node instance id not found"), 
            @ApiResponse(code = 200, message = "Successfull response", examples=@Example(value= {
                    @ExampleProperty(mediaType=JSON, value=GET_PROCESS_INSTANCE_NODE_RESPONSE_JSON)})) })
    @GET
    @Path(NODE_INSTANCES_BY_WORK_ITEM_ID_GET_URI)
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getNodeInstanceForWorkItem(@Context HttpHeaders headers, 
            @ApiParam(value = "process instance id that work item belongs to", required = true) @PathParam(PROCESS_INST_ID) long processInstanceId, 
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

    @ApiOperation(value="Returns node instances for a specified process instance.",
            response=NodeInstanceList.class, code=200)
    @ApiResponses(value = { @ApiResponse(code = 500, message = "Unexpected error"), 
            @ApiResponse(code = 200, message = "Successfull response", examples=@Example(value= {
                    @ExampleProperty(mediaType=JSON, value=GET_PROCESS_INSTANCE_NODES_RESPONSE_JSON)}))})
    @GET
    @Path(NODE_INSTANCES_BY_INSTANCE_ID_GET_URI)
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getProcessInstanceHistory(@Context HttpHeaders headers, 
            @ApiParam(value = "process instance id to to retrive history for", required = true) @PathParam(PROCESS_INST_ID) long processInstanceId,
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

    @ApiOperation(value="Returns current variable values of a specified process instance.",
            response=VariableInstanceList.class, code=200)
    @ApiResponses(value = { @ApiResponse(code = 500, message = "Unexpected error"), 
            @ApiResponse(code = 200, message = "Successfull response", examples=@Example(value= {
                    @ExampleProperty(mediaType=JSON, value=GET_PROCESS_INSTANCE_VARS_LOG_RESPONSE_JSON)}))})
    @GET
    @Path(VAR_INSTANCES_BY_INSTANCE_ID_GET_URI)
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getVariablesCurrentState(@Context HttpHeaders headers, 
            @ApiParam(value = "process instance id to load variables current state (latest value) for", required = true) @PathParam(PROCESS_INST_ID) long processInstanceId) {

        // no container id available so only used to transfer conversation id if given by client
        Header conversationIdHeader = buildConversationIdHeader("", context, headers);

        VariableInstanceList variableInstanceList = runtimeDataServiceBase.getVariablesCurrentState(processInstanceId);
        logger.debug("Returning result of variables search: {}", variableInstanceList);

        return createCorrectVariant(variableInstanceList, headers, Response.Status.OK, conversationIdHeader);
    }

    @ApiOperation(value="Returns the history of a specified variable in a specified process instance.",
            response=VariableInstanceList.class, code=200)
    @ApiResponses(value = { @ApiResponse(code = 500, message = "Unexpected error"), 
            @ApiResponse(code = 200, message = "Successfull response", examples=@Example(value= {
                    @ExampleProperty(mediaType=JSON, value=GET_PROCESS_INSTANCE_VARS_LOG_RESPONSE_JSON)}))})
    @GET
    @Path(VAR_INSTANCES_BY_VAR_INSTANCE_ID_GET_URI)
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getVariableHistory(@Context HttpHeaders headers, 
            @ApiParam(value = "process instance id to load variable history for", required = true) @PathParam(PROCESS_INST_ID) long processInstanceId,
            @ApiParam(value = "variable name that history should be loaded for", required = true) @PathParam("varName") String variableName,
            @ApiParam(value = "optional pagination - at which page to start, defaults to 0 (meaning first)", required = false) @QueryParam("page") @DefaultValue("0") Integer page, 
            @ApiParam(value = "optional pagination - size of the result, defaults to 10", required = false) @QueryParam("pageSize") @DefaultValue("10") Integer pageSize) {

        // no container id available so only used to transfer conversation id if given by client
        Header conversationIdHeader = buildConversationIdHeader("", context, headers);

        VariableInstanceList variableInstanceList = runtimeDataServiceBase.getVariableHistory(processInstanceId, variableName, page, pageSize);
        logger.debug("Returning result of variable '{}; history search: {}", variableName, variableInstanceList);

        return createCorrectVariant(variableInstanceList, headers, Response.Status.OK, conversationIdHeader);
    }

    @ApiOperation(value="Returns all process definitions in a specified KIE container.",
            response=ProcessDefinitionList.class, code=200)
    @ApiResponses(value = { @ApiResponse(code = 500, message = "Unexpected error"), 
            @ApiResponse(code = 200, message = "Successfull response", examples=@Example(value= {
                    @ExampleProperty(mediaType=JSON, value=GET_PROCESS_DEFS_RESPONSE_JSON)}))})
    @GET
    @Path(PROCESS_DEFINITIONS_BY_CONTAINER_ID_GET_URI)
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getProcessesByDeploymentId(@Context HttpHeaders headers, 
            @ApiParam(value = "container id to filter process definitions", required = true) @PathParam(CONTAINER_ID) String containerId,
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

    @ApiOperation(value="Returns all process definitions.",
            response=ProcessDefinitionList.class, code=200)
    @ApiResponses(value = { @ApiResponse(code = 500, message = "Unexpected error"), 
            @ApiResponse(code = 200, message = "Successfull response", examples=@Example(value= {
                    @ExampleProperty(mediaType=JSON, value=GET_PROCESS_DEFS_RESPONSE_JSON)}))})
    @GET
    @Path(PROCESS_DEFINITIONS_GET_URI)
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getProcessesByFilter(@Context HttpHeaders headers, 
            @ApiParam(value = "process id or name to filter process definitions", required = false) @QueryParam("filter") String filter,
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

    @ApiOperation(value="Returns all process definitions for a specified process.",
            response=ProcessDefinitionList.class, code=200)
    @ApiResponses(value = { @ApiResponse(code = 500, message = "Unexpected error"), 
            @ApiResponse(code = 200, message = "Successfull response", examples=@Example(value= {
                    @ExampleProperty(mediaType=JSON, value=GET_PROCESS_DEFS_RESPONSE_JSON)}))})
    @GET
    @Path(PROCESS_DEFINITIONS_BY_ID_GET_URI)
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getProcessesById(@Context HttpHeaders headers, 
            @ApiParam(value = "process id to load process definition", required = true) @PathParam(PROCESS_ID) String processId) {

        // no container id available so only used to transfer conversation id if given by client
        Header conversationIdHeader = buildConversationIdHeader("", context, headers);

        ProcessDefinitionList processDefinitionList = runtimeDataServiceBase.getProcessesById(processId);
        logger.debug("Returning result of process definition search: {}", processDefinitionList);

        return createCorrectVariant(processDefinitionList, headers, Response.Status.OK, conversationIdHeader);
    }

    @ApiOperation(value="Returns information about a specified process definition in a specified KIE container.",
            response=ProcessDefinition.class, code=200)
    @ApiResponses(value = { @ApiResponse(code = 500, message = "Unexpected error"), 
            @ApiResponse(code = 200, message = "Successfull response", examples=@Example(value= {
                    @ExampleProperty(mediaType=JSON, value=GET_PROCESS_DEF_RESPONSE_JSON)}))})
    @GET
    @Path(PROCESS_DEFINITIONS_BY_CONTAINER_ID_DEF_ID_GET_URI)
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getProcessesByDeploymentIdProcessId(@Context HttpHeaders headers, 
            @ApiParam(value = "container id that process definition belongs to", required = true) @PathParam(CONTAINER_ID) String containerId, 
            @ApiParam(value = "process id to load process definition", required = true) @PathParam(PROCESS_ID) String processId) {
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

    @ApiOperation(value="Returns task instances with a specified work item.",
            response=TaskInstance.class, code=200)
    @ApiResponses(value = { @ApiResponse(code = 500, message = "Unexpected error"), @ApiResponse(code = 404, message = "Task not found for given work item id"), 
            @ApiResponse(code = 200, message = "Successfull response", examples=@Example(value= {
                    @ExampleProperty(mediaType=JSON, value=GET_TASK_RESPONSE_JSON)}))})
    @GET
    @Path(TASK_BY_WORK_ITEM_ID_GET_URI)
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
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

    @ApiOperation(value="Returns information about a specified task instance.",
            response=TaskInstance.class, code=200)
    @ApiResponses(value = { @ApiResponse(code = 500, message = "Unexpected error"), @ApiResponse(code = 404, message = "Task not found for given id"), 
            @ApiResponse(code = 200, message = "Successfull response", examples=@Example(value= {
                    @ExampleProperty(mediaType=JSON, value=GET_TASK_RESPONSE_JSON)}))})
    @GET
    @Path(TASK_GET_URI)
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getTaskById(@Context HttpHeaders headers, 
            @ApiParam(value = "task id to load task instance", required = true) @PathParam(TASK_INSTANCE_ID) Long taskId) {
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

    @ApiOperation(value="Returns task instances assigned to business administrators.",
            response=TaskSummaryList.class, code=200)
    @ApiResponses(value = { @ApiResponse(code = 500, message = "Unexpected error"), 
            @ApiResponse(code = 200, message = "Successfull response", examples=@Example(value= {
                    @ExampleProperty(mediaType=JSON, value=GET_TASK_SUMMARY_RESPONSE_JSON)}))})
    @GET
    @Path(TASKS_ASSIGN_BUSINESS_ADMINS_GET_URI)
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
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

    @ApiOperation(value="Returns tasks with a user defined as a potential owner.",
            response=TaskSummaryList.class, code=200)
    @ApiResponses(value = { @ApiResponse(code = 500, message = "Unexpected error"), 
            @ApiResponse(code = 200, message = "Successfull response", examples=@Example(value= {
                    @ExampleProperty(mediaType=JSON, value=GET_TASK_SUMMARY_RESPONSE_JSON)}))})
    @GET
    @Path(TASKS_ASSIGN_POT_OWNERS_GET_URI)
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getTasksAssignedAsPotentialOwner(@Context HttpHeaders headers,
            @ApiParam(value = "optional task status (Created, Ready, Reserved, InProgress, Suspended, Completed, Failed, Error, Exited, Obsolete)", required = false, allowableValues="Created, Ready, Reserved,InProgress,Suspended,Completed,Failed,Error,Exited,Obsolete") @QueryParam("status") List<String> status,  
            @ApiParam(value = "optional group names to include in the query", required = false, allowMultiple=true) @QueryParam("groups") List<String> groupIds,
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

    @ApiOperation(value="Returns task instances that the querying user owns.",
            response=TaskSummaryList.class, code=200)
    @ApiResponses(value = { @ApiResponse(code = 500, message = "Unexpected error"), 
            @ApiResponse(code = 200, message = "Successfull response", examples=@Example(value= {
                    @ExampleProperty(mediaType=JSON, value=GET_TASK_SUMMARY_RESPONSE_JSON)}))})
    @GET
    @Path(TASKS_OWNED_GET_URI)
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
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

    @ApiOperation(value="Returns task instances associated with a specified process instance.",
            response=TaskSummaryList.class, code=200)
    @ApiResponses(value = { @ApiResponse(code = 500, message = "Unexpected error"), 
            @ApiResponse(code = 200, message = "Successfull response", examples=@Example(value= {
                    @ExampleProperty(mediaType=JSON, value=GET_TASK_SUMMARY_RESPONSE_JSON)}))})
    @GET
    @Path(TASK_BY_PROCESS_INST_ID_GET_URI)
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getTasksByStatusByProcessInstanceId(@Context HttpHeaders headers, 
            @ApiParam(value = "process instance id to filter task instances", required = true) @PathParam(PROCESS_INST_ID) Long processInstanceId,
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

    @ApiOperation(value="Returns all task instances.",
            response=TaskSummaryList.class, code=200)
    @ApiResponses(value = { @ApiResponse(code = 500, message = "Unexpected error"), 
            @ApiResponse(code = 200, message = "Successfull response", examples=@Example(value= {
                    @ExampleProperty(mediaType=JSON, value=GET_TASK_SUMMARY_RESPONSE_JSON)}))})
    @GET
    @Path(TASKS_GET_URI)
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
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

    @ApiOperation(value="Returns events for a specified task instance.",
            response=TaskEventInstanceList.class, code=200)
    @ApiResponses(value = { @ApiResponse(code = 500, message = "Unexpected error"), 
            @ApiResponse(code = 200, message = "Successfull response", examples=@Example(value= {
                    @ExampleProperty(mediaType=JSON, value=GET_TASK_EVENTS_RESPONSE_JSON)}))})
    @GET
    @Path(TASKS_EVENTS_GET_URI)
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getTaskEvents(@Context HttpHeaders headers, 
            @ApiParam(value = "task id to load task events for", required = true) @PathParam(TASK_INSTANCE_ID) Long taskId,
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

    @ApiOperation(value="Returns task instances with a specified variable.",
            response=TaskSummaryList.class, code=200)
    @ApiResponses(value = { @ApiResponse(code = 500, message = "Unexpected error"), 
            @ApiResponse(code = 200, message = "Successfull response", examples=@Example(value= {
                    @ExampleProperty(mediaType=JSON, value=GET_TASK_SUMMARY_RESPONSE_JSON)}))})
    @GET
    @Path(TASKS_BY_VAR_NAME_GET_URI)
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
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
