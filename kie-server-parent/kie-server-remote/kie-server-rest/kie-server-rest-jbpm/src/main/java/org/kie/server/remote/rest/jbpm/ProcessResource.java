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

import static org.kie.server.api.rest.RestURI.ABORT_PROCESS_INSTANCES_DEL_URI;
import static org.kie.server.api.rest.RestURI.ABORT_PROCESS_INST_DEL_URI;
import static org.kie.server.api.rest.RestURI.PROCESS_INSTANCES_BY_CONTAINER_GET_URI;
import static org.kie.server.api.rest.RestURI.PROCESS_INSTANCES_BY_PARENT_GET_URI;
import static org.kie.server.api.rest.RestURI.PROCESS_INSTANCES_NODE_INSTANCES_GET_URI;
import static org.kie.server.api.rest.RestURI.PROCESS_INSTANCE_GET_URI;
import static org.kie.server.api.rest.RestURI.PROCESS_INSTANCE_SIGNALS_GET_URI;
import static org.kie.server.api.rest.RestURI.PROCESS_INSTANCE_VARS_GET_URI;
import static org.kie.server.api.rest.RestURI.PROCESS_INSTANCE_VARS_POST_URI;
import static org.kie.server.api.rest.RestURI.PROCESS_INSTANCE_VAR_GET_URI;
import static org.kie.server.api.rest.RestURI.PROCESS_INSTANCE_VAR_INSTANCES_GET_URI;
import static org.kie.server.api.rest.RestURI.PROCESS_INSTANCE_VAR_INSTANCE_BY_VAR_NAME_GET_URI;
import static org.kie.server.api.rest.RestURI.PROCESS_INSTANCE_VAR_PUT_URI;
import static org.kie.server.api.rest.RestURI.PROCESS_INSTANCE_WORK_ITEMS_BY_PROC_INST_ID_GET_URI;
import static org.kie.server.api.rest.RestURI.PROCESS_INSTANCE_WORK_ITEM_ABORT_PUT_URI;
import static org.kie.server.api.rest.RestURI.PROCESS_INSTANCE_WORK_ITEM_BY_ID_GET_URI;
import static org.kie.server.api.rest.RestURI.PROCESS_INSTANCE_WORK_ITEM_COMPLETE_PUT_URI;
import static org.kie.server.api.rest.RestURI.PROCESS_URI;
import static org.kie.server.api.rest.RestURI.SIGNAL_PROCESS_INSTANCES_PORT_URI;
import static org.kie.server.api.rest.RestURI.SIGNAL_PROCESS_INST_POST_URI;
import static org.kie.server.api.rest.RestURI.START_PROCESS_POST_URI;
import static org.kie.server.api.rest.RestURI.START_PROCESS_WITH_CORRELATION_KEY_POST_URI;
import static org.kie.server.remote.rest.common.util.RestUtils.buildConversationIdHeader;
import static org.kie.server.remote.rest.common.util.RestUtils.createCorrectVariant;
import static org.kie.server.remote.rest.common.util.RestUtils.createResponse;
import static org.kie.server.remote.rest.common.util.RestUtils.getContentType;
import static org.kie.server.remote.rest.common.util.RestUtils.getVariant;
import static org.kie.server.remote.rest.common.util.RestUtils.internalServerError;
import static org.kie.server.remote.rest.common.util.RestUtils.noContent;
import static org.kie.server.remote.rest.common.util.RestUtils.notFound;
import static org.kie.server.remote.rest.jbpm.resources.Messages.CONTAINER_NOT_FOUND;
import static org.kie.server.remote.rest.jbpm.resources.Messages.CREATE_RESPONSE_ERROR;
import static org.kie.server.remote.rest.jbpm.resources.Messages.PROCESS_DEFINITION_NOT_FOUND;
import static org.kie.server.remote.rest.jbpm.resources.Messages.PROCESS_INSTANCE_NOT_FOUND;
import static org.kie.server.remote.rest.jbpm.resources.Messages.UNEXPECTED_ERROR;
import static org.kie.server.remote.rest.jbpm.resources.Messages.WORK_ITEM_NOT_FOUND;

import java.text.MessageFormat;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
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
import org.jbpm.services.api.ProcessDefinitionNotFoundException;
import org.jbpm.services.api.ProcessInstanceNotFoundException;
import org.jbpm.services.api.WorkItemNotFoundException;
import org.kie.server.api.model.definition.ProcessDefinitionList;
import org.kie.server.api.model.instance.NodeInstanceList;
import org.kie.server.api.model.instance.ProcessInstance;
import org.kie.server.api.model.instance.ProcessInstanceList;
import org.kie.server.api.model.instance.VariableInstanceList;
import org.kie.server.api.model.instance.WorkItemInstance;
import org.kie.server.api.model.instance.WorkItemInstanceList;
import org.kie.server.remote.rest.common.Header;
import org.kie.server.services.api.KieServerRegistry;
import org.kie.server.services.jbpm.ProcessServiceBase;
import org.kie.server.services.jbpm.RuntimeDataServiceBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Api(value="Process instances :: BPM")
@Path("server/" + PROCESS_URI)
public class ProcessResource  {

    public static final Logger logger = LoggerFactory.getLogger(ProcessResource.class);

    private ProcessServiceBase processServiceBase;
    private RuntimeDataServiceBase runtimeDataServiceBase;
    private KieServerRegistry context;

    public ProcessResource() {

    }

    public ProcessResource(ProcessServiceBase processServiceBase, RuntimeDataServiceBase runtimeDataServiceBase, KieServerRegistry context) {
        this.processServiceBase = processServiceBase;
        this.runtimeDataServiceBase = runtimeDataServiceBase;
        this.context = context;
    }

    protected static String getRelativePath(HttpServletRequest httpRequest) {
        String url =  httpRequest.getRequestURI();
        url = url.replaceAll( ".*/rest", "");
        return url;
    }

    @ApiOperation(value="Starts new process instance of given process definition within given container with optional variables",
            response=Long.class, code=201)
    @ApiResponses(value = { @ApiResponse(code = 500, message = "Unexpected error"),
            @ApiResponse(code = 404, message = "Process ID or Container Id not found") })
    @POST
    @Path(START_PROCESS_POST_URI)
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response startProcess(@javax.ws.rs.core.Context HttpHeaders headers, 
            @ApiParam(value = "container id where the process definition resides", required = true) @PathParam("id") String containerId, 
            @ApiParam(value = "process id that new instance should be created from", required = true) @PathParam("pId") String processId, 
            @ApiParam(value = "optional map of process variables", required = false) @DefaultValue("") String payload) {
        Variant v = getVariant(headers);
        String type = getContentType(headers);

        try {
            String response = processServiceBase.startProcess(containerId, processId, payload, type);

            logger.debug("Returning CREATED response with content '{}'", response);
            Header conversationIdHeader = buildConversationIdHeader(containerId, context, headers);
            return createResponse(response, v, Response.Status.CREATED, conversationIdHeader);
        } catch (DeploymentNotFoundException e) {
            return notFound(
                    MessageFormat.format(CONTAINER_NOT_FOUND, containerId), v);
        } catch (ProcessDefinitionNotFoundException e) {
            return notFound(
                    MessageFormat.format(PROCESS_DEFINITION_NOT_FOUND, processId, containerId), v);
        } catch (Exception e) {
            logger.error("Unexpected error during processing {}", e.getMessage(), e);
            return internalServerError(
                    MessageFormat.format(CREATE_RESPONSE_ERROR, e.getMessage()), v);
        }
    }

    @ApiOperation(value="Starts new process instance with correlation key of given process definition within given container with optional variables",
            response=Long.class, code=201)
    @ApiResponses(value = { @ApiResponse(code = 500, message = "Unexpected error"),
            @ApiResponse(code = 404, message = "Process ID or Container Id not found") })
    @POST
    @Path(START_PROCESS_WITH_CORRELATION_KEY_POST_URI)
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response startProcessWithCorrelation(@javax.ws.rs.core.Context HttpHeaders headers, 
            @ApiParam(value = "container id where the process definition resides", required = true) @PathParam("id") String containerId, 
            @ApiParam(value = "process id that new instance should be created from", required = true) @PathParam("pId") String processId,
            @ApiParam(value = "correlation key to be assigned to process instance", required = true) @PathParam("correlationKey") String correlationKey, 
            @ApiParam(value = "optional map of process variables", required = false) @DefaultValue("") String payload) {
        Variant v = getVariant(headers);
        String type = getContentType(headers);

        try {
            String response = processServiceBase.startProcessWithCorrelation(containerId, processId, correlationKey, payload, type);

            logger.debug("Returning CREATED response with content '{}'", response);
            Header conversationIdHeader = buildConversationIdHeader(containerId, context, headers);
            return createResponse(response, v, Response.Status.CREATED, conversationIdHeader);
        } catch (DeploymentNotFoundException e) {
            return notFound(
                    MessageFormat.format(CONTAINER_NOT_FOUND, containerId), v);
        } catch (ProcessDefinitionNotFoundException e) {
            return notFound(
                    MessageFormat.format(PROCESS_DEFINITION_NOT_FOUND, processId, containerId), v);
        } catch (Exception e) {
            logger.error("Unexpected error during processing {}", e.getMessage(), e);
            return internalServerError(
                    MessageFormat.format(CREATE_RESPONSE_ERROR, e.getMessage()), v);
        }
    }

    @ApiOperation(value="Aborts active process instance identified by given id",
            response=Void.class, code=204)
    @ApiResponses(value = { @ApiResponse(code = 500, message = "Unexpected error"),
            @ApiResponse(code = 404, message = "Process instance or Container Id not found") })
    @DELETE
    @Path(ABORT_PROCESS_INST_DEL_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response abortProcessInstance(@javax.ws.rs.core.Context HttpHeaders headers, 
            @ApiParam(value = "container id that process instance belongs to", required = true) @PathParam("id") String containerId, 
            @ApiParam(value = "identifier of the process instance to be aborted", required = true) @PathParam("pInstanceId") Long processInstanceId) {
        Variant v = getVariant(headers);
        Header conversationIdHeader = buildConversationIdHeader(containerId, context, headers);
        try {
            processServiceBase.abortProcessInstance(containerId, processInstanceId);
            // produce 204 NO_CONTENT response code
            return noContent(v, conversationIdHeader);
        } catch (ProcessInstanceNotFoundException e) {
            return notFound(
                    MessageFormat.format(PROCESS_INSTANCE_NOT_FOUND, processInstanceId), v, conversationIdHeader);
        } catch (DeploymentNotFoundException e) {
            return notFound(
                    MessageFormat.format(CONTAINER_NOT_FOUND, containerId), v, conversationIdHeader);
        } catch (Exception e) {
            logger.error("Unexpected error during processing {}", e.getMessage(), e);
            return internalServerError(MessageFormat.format(UNEXPECTED_ERROR, e.getMessage()), v, conversationIdHeader);
        }
    }


    @ApiOperation(value="Aborts active process instances identified by given list of identifiers",
            response=Void.class, code=204)
    @ApiResponses(value = { @ApiResponse(code = 500, message = "Unexpected error"),
            @ApiResponse(code = 404, message = "Process instance or Container Id not found") })
    @DELETE
    @Path(ABORT_PROCESS_INSTANCES_DEL_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response abortProcessInstances(@javax.ws.rs.core.Context HttpHeaders headers, 
            @ApiParam(value = "container id that process instance belongs to", required = true) @PathParam("id") String containerId, 
            @ApiParam(value = "list of identifiers of the process instances to be aborted", required = true) @QueryParam("instanceId") List<Long> processInstanceIds) {
        Variant v = getVariant(headers);
        Header conversationIdHeader = buildConversationIdHeader(containerId, context, headers);
        try {
            processServiceBase.abortProcessInstances(containerId, processInstanceIds);
            // produce 204 NO_CONTENT response code
            return noContent(v, conversationIdHeader);
        } catch (ProcessInstanceNotFoundException e) {
            return notFound(
                    MessageFormat.format(PROCESS_INSTANCE_NOT_FOUND, processInstanceIds), v, conversationIdHeader);
        } catch (DeploymentNotFoundException e) {
            return notFound(
                    MessageFormat.format(CONTAINER_NOT_FOUND, containerId), v, conversationIdHeader);
        } catch (Exception e) {
            logger.error("Unexpected error during processing {}", e.getMessage(), e);
            return internalServerError(MessageFormat.format(UNEXPECTED_ERROR, e.getMessage()), v, conversationIdHeader);
        }
    }

    @ApiOperation(value="Signals active process instance identified by given id with singal name and optional event data",
            response=Void.class, code=200)
    @ApiResponses(value = { @ApiResponse(code = 500, message = "Unexpected error"),
            @ApiResponse(code = 404, message = "Process instance or Container Id not found") })
    @POST
    @Path(SIGNAL_PROCESS_INST_POST_URI)
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response signalProcessInstance(@javax.ws.rs.core.Context HttpHeaders headers, 
            @ApiParam(value = "container id that process instance belongs to", required = true) @PathParam("id") String containerId,
            @ApiParam(value = "identifier of the process instance to be signaled", required = true) @PathParam("pInstanceId") Long processInstanceId, 
            @ApiParam(value = "signal name to be send to process instance", required = true) @PathParam("sName") String signalName, 
            @ApiParam(value = "optional event data - any type can be provided", required = false) String eventPayload) {

        Variant v = getVariant(headers);
        String type = getContentType(headers);
        Header conversationIdHeader = buildConversationIdHeader(containerId, context, headers);
        try {

            processServiceBase.signalProcessInstance(containerId, processInstanceId, signalName, eventPayload, type);

            return createResponse(null, v, Response.Status.OK, conversationIdHeader);

        } catch (ProcessInstanceNotFoundException e) {
            return notFound(MessageFormat.format(PROCESS_INSTANCE_NOT_FOUND, processInstanceId), v, conversationIdHeader);
        } catch (DeploymentNotFoundException e) {
            return notFound(MessageFormat.format(CONTAINER_NOT_FOUND, containerId), v, conversationIdHeader);
        } catch (Exception e) {
            logger.error("Unexpected error during processing {}", e.getMessage(), e);
            return internalServerError(MessageFormat.format(UNEXPECTED_ERROR, e.getMessage()), v, conversationIdHeader);
        }
    }

    @ApiOperation(value="Signals active process instances identified by given ids with singal name and optional event data",
            response=Void.class, code=200)
    @ApiResponses(value = { @ApiResponse(code = 500, message = "Unexpected error"),
            @ApiResponse(code = 404, message = "Process instance or Container Id not found") })
    @POST
    @Path(SIGNAL_PROCESS_INSTANCES_PORT_URI)
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response signalProcessInstances(@javax.ws.rs.core.Context HttpHeaders headers, 
            @ApiParam(value = "container id that process instance belongs to", required = true) @PathParam("id") String containerId,
            @ApiParam(value = "list of identifiers of the process instances to be signaled", required = true) @QueryParam("instanceId") List<Long> processInstanceIds, 
            @ApiParam(value = "signal name to be send to process instance", required = true) @PathParam("sName") String signalName, 
            @ApiParam(value = "optional event data - any type can be provided", required = false) String eventPayload) {
        Variant v = getVariant(headers);
        String type = getContentType(headers);
        Header conversationIdHeader = buildConversationIdHeader(containerId, context, headers);
        try {
            if (processInstanceIds != null && !processInstanceIds.isEmpty()) {
                logger.debug("Signaling given process instances - {}", processInstanceIds);
                processServiceBase.signalProcessInstances(containerId, processInstanceIds, signalName, eventPayload, type);
            } else {
                logger.debug("No process instances given, signal container..");
                processServiceBase.signal(containerId, signalName, eventPayload, type);
            }

            return createResponse("", v, Response.Status.OK, conversationIdHeader);

        } catch (ProcessInstanceNotFoundException e) {
            return notFound(MessageFormat.format(PROCESS_INSTANCE_NOT_FOUND, processInstanceIds), v, conversationIdHeader);
        } catch (DeploymentNotFoundException e) {
            return notFound(MessageFormat.format(CONTAINER_NOT_FOUND, containerId), v, conversationIdHeader);
        } catch (Exception e) {
            logger.error("Unexpected error during processing {}", e.getMessage(), e);
            return internalServerError(MessageFormat.format(UNEXPECTED_ERROR, e.getMessage()), v, conversationIdHeader);
        }
    }

    @ApiOperation(value="Retrieves process instance identified by given id optionally with variables (variables will be loaded only for active process instance)",
            response=ProcessInstance.class, code=200)
    @ApiResponses(value = { @ApiResponse(code = 500, message = "Unexpected error"),
            @ApiResponse(code = 404, message = "Process instance or Container Id not found") })
    @GET
    @Path(PROCESS_INSTANCE_GET_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getProcessInstance(@javax.ws.rs.core.Context HttpHeaders headers, 
            @ApiParam(value = "container id that process instance belongs to", required = true) @PathParam("id") String containerId,
            @ApiParam(value = "identifier of the process instance to be fetched", required = true) @PathParam("pInstanceId") Long processInstanceId, 
            @ApiParam(value = "indicates if process instance variables should be loaded or not", required = false) @QueryParam("withVars") boolean withVars) {
        Variant v = getVariant(headers);
        String type = getContentType(headers);
        Header conversationIdHeader = buildConversationIdHeader(containerId, context, headers);
        try {

            String response = processServiceBase.getProcessInstance(containerId, processInstanceId, withVars, type);

            logger.debug("Returning OK response with content '{}'", response);            
            return createResponse(response, v, Response.Status.OK, conversationIdHeader);

        } catch (ProcessInstanceNotFoundException e) {
            return notFound(MessageFormat.format(PROCESS_INSTANCE_NOT_FOUND, processInstanceId), v, conversationIdHeader);
        } catch (DeploymentNotFoundException e) {
            return notFound(MessageFormat.format(CONTAINER_NOT_FOUND, containerId), v, conversationIdHeader);
        } catch (Exception e) {
            logger.error("Unexpected error during processing {}", e.getMessage(), e);
            return internalServerError(MessageFormat.format(UNEXPECTED_ERROR, e.getMessage()), v);
        }
    }

    @ApiOperation(value="Updates active process instance's (identified by given id) variable with given name",
            response=Void.class, code=201)
    @ApiResponses(value = { @ApiResponse(code = 500, message = "Unexpected error"),
            @ApiResponse(code = 404, message = "Process instance or Container Id not found") })
    @PUT
    @Path(PROCESS_INSTANCE_VAR_PUT_URI)
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response setProcessVariable(@javax.ws.rs.core.Context HttpHeaders headers, 
            @ApiParam(value = "container id that process instance belongs to", required = true) @PathParam("id") String containerId,
            @ApiParam(value = "identifier of the process instance to be updated", required = true) @PathParam("pInstanceId") Long processInstanceId, 
            @ApiParam(value = "name of the variable to be set/updated", required = true) @PathParam("varName") String varName, 
            @ApiParam(value = "variable data - any type can be provided", required = true) String variablePayload) {

        Variant v = getVariant(headers);
        String type = getContentType(headers);
        Header conversationIdHeader = buildConversationIdHeader(containerId, context, headers);
        try {

            processServiceBase.setProcessVariable(containerId, processInstanceId, varName, variablePayload, type);
            return createResponse("", v, Response.Status.CREATED, conversationIdHeader);

        } catch (ProcessInstanceNotFoundException e) {
            return notFound(MessageFormat.format(PROCESS_INSTANCE_NOT_FOUND, processInstanceId), v, conversationIdHeader);
        } catch (DeploymentNotFoundException e) {
            return notFound(MessageFormat.format(CONTAINER_NOT_FOUND, containerId), v, conversationIdHeader);
        } catch (Exception e) {
            logger.error("Unexpected error during processing {}", e.getMessage(), e);
            return internalServerError(MessageFormat.format(UNEXPECTED_ERROR, e.getMessage()), v, conversationIdHeader);
        }
    }

    @ApiOperation(value="Updates active process instance's (identified by given id) variables given as map in the body",
            response=Void.class, code=201)
    @ApiResponses(value = { @ApiResponse(code = 500, message = "Unexpected error"),
            @ApiResponse(code = 404, message = "Process instance or Container Id not found") })
    @POST
    @Path(PROCESS_INSTANCE_VARS_POST_URI)
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response setProcessVariables(@javax.ws.rs.core.Context HttpHeaders headers, 
            @ApiParam(value = "container id that process instance belongs to", required = true) @PathParam("id") String containerId,
            @ApiParam(value = "identifier of the process instance to be updated", required = true) @PathParam("pInstanceId") Long processInstanceId, 
            @ApiParam(value = "variable data give as map", required = true) String variablePayload) {
        Variant v = getVariant(headers);
        String type = getContentType(headers);
        Header conversationIdHeader = buildConversationIdHeader(containerId, context, headers);
        try {

            processServiceBase.setProcessVariables(containerId, processInstanceId, variablePayload, type);
            return createResponse("", v, Response.Status.CREATED, conversationIdHeader);

        } catch (ProcessInstanceNotFoundException e) {
            return notFound(MessageFormat.format(PROCESS_INSTANCE_NOT_FOUND, processInstanceId), v, conversationIdHeader);
        } catch (DeploymentNotFoundException e) {
            return notFound(MessageFormat.format(CONTAINER_NOT_FOUND, containerId), v, conversationIdHeader);
        } catch (Exception e) {
            logger.error("Unexpected error during processing {}", e.getMessage(), e);
            return internalServerError(MessageFormat.format(UNEXPECTED_ERROR, e.getMessage()), v, conversationIdHeader);
        }
    }

    @ApiOperation(value="Retrieves active process instance's (identified by given id) variable given as variable name",
            response=Object.class, code=200)
    @ApiResponses(value = { @ApiResponse(code = 500, message = "Unexpected error"),
            @ApiResponse(code = 404, message = "Process instance or Container Id not found") })
    @GET
    @Path(PROCESS_INSTANCE_VAR_GET_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Object getProcessInstanceVariable(@javax.ws.rs.core.Context HttpHeaders headers, 
            @ApiParam(value = "container id that process instance belongs to", required = true) @PathParam("id") String containerId,
            @ApiParam(value = "identifier of the process instance that variable should be retrieved from", required = true) @PathParam("pInstanceId") Long processInstanceId, 
            @ApiParam(value = "variable name to be retrieved", required = true) @PathParam("varName") String varName) {
        Variant v = getVariant(headers);
        String type = getContentType(headers);
        Header conversationIdHeader = buildConversationIdHeader(containerId, context, headers);
        try {

            String response = processServiceBase.getProcessInstanceVariable(containerId, processInstanceId, varName, type);

            logger.debug("Returning OK response with content '{}'", response);
            return createResponse(response, v, Response.Status.OK, conversationIdHeader);

        } catch (ProcessInstanceNotFoundException e) {
            return notFound(
                    MessageFormat.format(PROCESS_INSTANCE_NOT_FOUND, processInstanceId), v, conversationIdHeader);
        } catch (DeploymentNotFoundException e) {
            return notFound(
                    MessageFormat.format(CONTAINER_NOT_FOUND, containerId), v, conversationIdHeader);
        } catch (Exception e) {
            logger.error("Unexpected error during processing {}", e.getMessage(), e);
            return internalServerError(MessageFormat.format(UNEXPECTED_ERROR, e.getMessage()), v, conversationIdHeader);
        }
    }

    @ApiOperation(value="Retrieves active process instance's (identified by given id) variables, variables are returned as map where key is the variable name (string) and value is variable value (any type)",
            response=Map.class, code=200)
    @ApiResponses(value = { @ApiResponse(code = 500, message = "Unexpected error"),
            @ApiResponse(code = 404, message = "Process instance or Container Id not found") })
    @GET
    @Path(PROCESS_INSTANCE_VARS_GET_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getProcessInstanceVariables(@javax.ws.rs.core.Context HttpHeaders headers, 
            @ApiParam(value = "container id that process instance belongs to", required = true) @PathParam("id") String containerId,
            @ApiParam(value = "identifier of the process instance that variables should be retrieved from", required = true) @PathParam("pInstanceId") Long processInstanceId) {
        Variant v = getVariant(headers);
        String type = getContentType(headers);
        Header conversationIdHeader = buildConversationIdHeader(containerId, context, headers);
        try {

            String response = processServiceBase.getProcessInstanceVariables(containerId, processInstanceId, type);

            logger.debug("Returning OK response with content '{}'", response);
            return createResponse(response, v, Response.Status.OK, conversationIdHeader);

        } catch (ProcessInstanceNotFoundException e) {
            return notFound(
                    MessageFormat.format(PROCESS_INSTANCE_NOT_FOUND, processInstanceId), v, conversationIdHeader);
        } catch (DeploymentNotFoundException e) {
            return notFound(
                    MessageFormat.format(CONTAINER_NOT_FOUND, containerId), v, conversationIdHeader);
        } catch (Exception e) {
            logger.error("Unexpected error during processing {}", e.getMessage(), e);
            return internalServerError(MessageFormat.format(UNEXPECTED_ERROR, e.getMessage()), v, conversationIdHeader);
        }

    }

    @ApiOperation(value="Retrieves active process instance's (identified by given id) active signals",
            response=String.class, responseContainer="List", code=200)
    @ApiResponses(value = { @ApiResponse(code = 500, message = "Unexpected error"),
            @ApiResponse(code = 404, message = "Process instance or Container Id not found") })
    @GET
    @Path(PROCESS_INSTANCE_SIGNALS_GET_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getAvailableSignals(@javax.ws.rs.core.Context HttpHeaders headers, 
            @ApiParam(value = "container id that process instance belongs to", required = true) @PathParam("id") String containerId,
            @ApiParam(value = "identifier of the process instance that signals should be collected for", required = true) @PathParam("pInstanceId") Long processInstanceId) {
        Variant v = getVariant(headers);
        String type = getContentType(headers);
        Header conversationIdHeader = buildConversationIdHeader(containerId, context, headers);
        try {

            String response = processServiceBase.getAvailableSignals(containerId, processInstanceId, type);

            logger.debug("Returning OK response with content '{}'", response);

            return createResponse(response, v, Response.Status.OK, conversationIdHeader);
        }  catch (ProcessInstanceNotFoundException e) {
            return notFound(MessageFormat.format(PROCESS_INSTANCE_NOT_FOUND, processInstanceId), v, conversationIdHeader);
        } catch (DeploymentNotFoundException e) {
            return notFound(MessageFormat.format(CONTAINER_NOT_FOUND, containerId), v, conversationIdHeader);
        } catch (Exception e) {
            logger.error("Unexpected error during processing {}", e.getMessage(), e);
            return internalServerError(MessageFormat.format(UNEXPECTED_ERROR, e.getMessage()), v, conversationIdHeader);
        }

    }

    @ApiOperation(value="Completes work item identified by workItemId within process instance and container. Optionally completion can provide outcome data - as map",
            response=Void.class, code=201)
    @ApiResponses(value = { @ApiResponse(code = 500, message = "Unexpected error"),
            @ApiResponse(code = 404, message = "Process instance, Work Item or Container Id not found") })
    @PUT
    @Path(PROCESS_INSTANCE_WORK_ITEM_COMPLETE_PUT_URI)
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response completeWorkItem(@javax.ws.rs.core.Context HttpHeaders headers, 
            @ApiParam(value = "container id that process instance belongs to", required = true) @PathParam("id") String containerId,
            @ApiParam(value = "identifier of the process instance that work item belongs to", required = true) @PathParam("pInstanceId") Long processInstanceId, 
            @ApiParam(value = "identifier of the work item to complete", required = true) @PathParam("workItemId") Long workItemId, 
            @ApiParam(value = "optional outcome data give as map", required = false) String resultPayload) {

        Variant v = getVariant(headers);
        String type = getContentType(headers);
        Header conversationIdHeader = buildConversationIdHeader(containerId, context, headers);
        try {

            processServiceBase.completeWorkItem(containerId, processInstanceId, workItemId, resultPayload, type);

            return createResponse("", v, Response.Status.CREATED, conversationIdHeader);

        } catch (ProcessInstanceNotFoundException e) {
            return notFound(MessageFormat.format(PROCESS_INSTANCE_NOT_FOUND, processInstanceId), v, conversationIdHeader);
        } catch (DeploymentNotFoundException e) {
            return notFound(MessageFormat.format(CONTAINER_NOT_FOUND, containerId), v, conversationIdHeader);
        } catch (Exception e) {
            logger.error("Unexpected error during processing {}", e.getMessage(), e);
            return internalServerError(MessageFormat.format(UNEXPECTED_ERROR, e.getMessage()), v, conversationIdHeader);
        }
    }


    @ApiOperation(value="Aborts work item identified by workItemId within process instance and container",
            response=Void.class, code=201)
    @ApiResponses(value = { @ApiResponse(code = 500, message = "Unexpected error"),
            @ApiResponse(code = 404, message = "Process instance, Work Item or Container Id not found") })
    @PUT
    @Path(PROCESS_INSTANCE_WORK_ITEM_ABORT_PUT_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response abortWorkItem(@javax.ws.rs.core.Context HttpHeaders headers, 
            @ApiParam(value = "container id that process instance belongs to", required = true) @PathParam("id") String containerId,
            @ApiParam(value = "identifier of the process instance that work item belongs to", required = true) @PathParam("pInstanceId") Long processInstanceId, 
            @ApiParam(value = "identifier of the work item to abort", required = true) @PathParam("workItemId") Long workItemId) {
        Variant v = getVariant(headers);
        Header conversationIdHeader = buildConversationIdHeader(containerId, context, headers);
        try {

            processServiceBase.abortWorkItem(containerId, processInstanceId, workItemId);

            return createResponse("", v, Response.Status.CREATED, conversationIdHeader);

        } catch (ProcessInstanceNotFoundException e) {
            return notFound(MessageFormat.format(PROCESS_INSTANCE_NOT_FOUND, processInstanceId), v, conversationIdHeader);
        } catch (DeploymentNotFoundException e) {
            return notFound(MessageFormat.format(CONTAINER_NOT_FOUND, containerId), v, conversationIdHeader);
        } catch (Exception e) {
            logger.error("Unexpected error during processing {}", e.getMessage(), e);
            return internalServerError(MessageFormat.format(UNEXPECTED_ERROR, e.getMessage()), v, conversationIdHeader);
        }
    }

    @ApiOperation(value="Retrieves work item identified by workItemId within process instance and container",
            response=WorkItemInstance.class, code=200)
    @ApiResponses(value = { @ApiResponse(code = 500, message = "Unexpected error"),
            @ApiResponse(code = 404, message = "Process instance, Work Item or Container Id not found") })
    @GET
    @Path(PROCESS_INSTANCE_WORK_ITEM_BY_ID_GET_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getWorkItem(@javax.ws.rs.core.Context HttpHeaders headers, 
            @ApiParam(value = "container id that process instance belongs to", required = true) @PathParam("id") String containerId,
            @ApiParam(value = "identifier of the process instance that work item belongs to", required = true) @PathParam("pInstanceId") Long processInstanceId, 
            @ApiParam(value = "identifier of the work item to retrieve", required = true) @PathParam("workItemId") Long workItemId) {
        Variant v = getVariant(headers);
        String type = getContentType(headers);
        Header conversationIdHeader = buildConversationIdHeader(containerId, context, headers);
        try {

            String response = processServiceBase.getWorkItem(containerId, processInstanceId, workItemId, type);

            logger.debug("Returning OK response with content '{}'", response);
            return createResponse(response, v, Response.Status.OK, conversationIdHeader);
        }  catch (ProcessInstanceNotFoundException e) {
            return notFound(MessageFormat.format(PROCESS_INSTANCE_NOT_FOUND, processInstanceId), v, conversationIdHeader);
        } catch (DeploymentNotFoundException e) {
            return notFound(MessageFormat.format(CONTAINER_NOT_FOUND, containerId), v, conversationIdHeader);
        }  catch (WorkItemNotFoundException e) {
            return notFound(MessageFormat.format(WORK_ITEM_NOT_FOUND, workItemId), v, conversationIdHeader);
        } catch (Exception e) {
            logger.error("Unexpected error during processing {}", e.getMessage(), e);
            return internalServerError(MessageFormat.format(UNEXPECTED_ERROR, e.getMessage()), v, conversationIdHeader);
        }

    }

    @ApiOperation(value="Retrieves work items within process instance and container",
            response=WorkItemInstanceList.class, code=200)
    @ApiResponses(value = { @ApiResponse(code = 500, message = "Unexpected error"),
            @ApiResponse(code = 404, message = "Process instance, Work Item or Container Id not found") })
    @GET
    @Path(PROCESS_INSTANCE_WORK_ITEMS_BY_PROC_INST_ID_GET_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getWorkItemByProcessInstance(@javax.ws.rs.core.Context HttpHeaders headers, 
            @ApiParam(value = "container id that process instance belongs to", required = true) @PathParam("id") String containerId,
            @ApiParam(value = "identifier of the process instance that work items belong to", required = true) @PathParam("pInstanceId") Long processInstanceId) {
        Variant v = getVariant(headers);
        String type = getContentType(headers);
        Header conversationIdHeader = buildConversationIdHeader(containerId, context, headers);
        try {

            String response = processServiceBase.getWorkItemByProcessInstance(containerId, processInstanceId, type);

            logger.debug("Returning OK response with content '{}'", response);
            return createResponse(response, v, Response.Status.OK, conversationIdHeader);
        }  catch (ProcessInstanceNotFoundException e) {
            return notFound(MessageFormat.format(PROCESS_INSTANCE_NOT_FOUND, processInstanceId), v, conversationIdHeader);
        } catch (DeploymentNotFoundException e) {
            return notFound(MessageFormat.format(CONTAINER_NOT_FOUND, containerId), v, conversationIdHeader);
        } catch (Exception e) {
            logger.error("Unexpected error during processing {}", e.getMessage(), e);
            return internalServerError(MessageFormat.format(UNEXPECTED_ERROR, e.getMessage()), v, conversationIdHeader);
        }
    }

    @ApiOperation(value="Retrieves process instances that belong to given container",
            response=ProcessInstanceList.class, code=200)
    @ApiResponses(value = { @ApiResponse(code = 500, message = "Unexpected error"),
                            @ApiResponse(code = 404, message = "Container Id not found")})
    @GET
    @Path(PROCESS_INSTANCES_BY_CONTAINER_GET_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getProcessInstancesByDeploymentId(@Context HttpHeaders headers, 
            @ApiParam(value = "container id that process instance belongs to", required = true) @PathParam("id") String containerId, 
            @ApiParam(value = "optional process instance status (active, completed, aborted) - defaults ot active (1) only", required = false, allowableValues="1,2,3") @QueryParam("status") List<Integer> status,
            @ApiParam(value = "optional pagination - at which page to start, defaults to 0 (meaning first)", required = false) @QueryParam("page") @DefaultValue("0") Integer page, 
            @ApiParam(value = "optional pagination - size of the result, defaults to 10", required = false) @QueryParam("pageSize") @DefaultValue("10") Integer pageSize,
            @ApiParam(value = "optional sort column, no default", required = false) @QueryParam("sort") String sort, 
            @ApiParam(value = "optional sort direction (asc, desc) - defaults to asc", required = false) @QueryParam("sortOrder") @DefaultValue("true") boolean sortOrder) {

        Variant v = getVariant(headers);
        Header conversationIdHeader = buildConversationIdHeader(containerId, context, headers);
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

    @ApiOperation(value="Retrieves process definitions that belong to given container",
            response=ProcessDefinitionList.class, code=200)
    @ApiResponses(value = { @ApiResponse(code = 500, message = "Unexpected error"),
                            @ApiResponse(code = 404, message = "Container Id not found")})
    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getProcessesByDeploymentId(@Context HttpHeaders headers, 
            @ApiParam(value = "container id that process instance belongs to", required = true) @PathParam("id") String containerId,
            @ApiParam(value = "optional pagination - at which page to start, defaults to 0 (meaning first)", required = false) @QueryParam("page") @DefaultValue("0") Integer page, 
            @ApiParam(value = "optional pagination - size of the result, defaults to 10", required = false) @QueryParam("pageSize") @DefaultValue("10") Integer pageSize,
            @ApiParam(value = "optional sort column, no default", required = false) @QueryParam("sort") String sort, 
            @ApiParam(value = "optional sort direction (asc, desc) - defaults to asc", required = false) @QueryParam("sortOrder") @DefaultValue("true") boolean sortOrder) {

        Variant v = getVariant(headers);
        Header conversationIdHeader = buildConversationIdHeader(containerId, context, headers);
        try {
            ProcessDefinitionList processDefinitionList = runtimeDataServiceBase.getProcessesByDeploymentId(containerId, page, pageSize, sort, sortOrder);
            logger.debug("Returning result of process definition search: {}", processDefinitionList);
    
            return createCorrectVariant(processDefinitionList, headers, Response.Status.OK, conversationIdHeader);
            
        } catch (DeploymentNotFoundException e) {
            return notFound(MessageFormat.format(CONTAINER_NOT_FOUND, containerId), v, conversationIdHeader);
        } catch (Exception e) {
            logger.error("Unexpected error during processing {}", e.getMessage(), e);
            return internalServerError(MessageFormat.format(UNEXPECTED_ERROR, e.getMessage()), v, conversationIdHeader);
        }
    }

    @ApiOperation(value="Retrieves node instances for given process instance. Depending on provided query parameters (activeOnly or completedOnly) will return active and/or completes nodes",
            response=NodeInstanceList.class, code=200)
    @ApiResponses(value = { @ApiResponse(code = 500, message = "Unexpected error"),
                            @ApiResponse(code = 404, message = "Process Instance or Container Id not found")})
    @GET
    @Path(PROCESS_INSTANCES_NODE_INSTANCES_GET_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getProcessInstanceHistory(@Context HttpHeaders headers, @PathParam("id") 
            @ApiParam(value = "container id that process instance belongs to", required = true) String containerId, 
            @ApiParam(value = "identifier of the process instance that history should be collected for", required = true) @PathParam("pInstanceId") long processInstanceId,
            @ApiParam(value = "instructs if active nodes only should be collected, defaults to false", required = false) @QueryParam("activeOnly")Boolean active, 
            @ApiParam(value = "instructs if completed nodes only should be collected, defaults to false", required = false) @QueryParam("completedOnly")Boolean completed,
            @ApiParam(value = "optional pagination - at which page to start, defaults to 0 (meaning first)", required = false) @QueryParam("page") @DefaultValue("0") Integer page, 
            @ApiParam(value = "optional pagination - size of the result, defaults to 10", required = false) @QueryParam("pageSize") @DefaultValue("10") Integer pageSize) {
        
        Variant v = getVariant(headers);
        Header conversationIdHeader = buildConversationIdHeader(containerId, context, headers);
        try {
            NodeInstanceList nodeInstanceList = runtimeDataServiceBase.getProcessInstanceHistory(processInstanceId, active, completed, page, pageSize);
            logger.debug("Returning result of node instances search: {}", nodeInstanceList);
            return createCorrectVariant(nodeInstanceList, headers, Response.Status.OK, conversationIdHeader);
        }  catch (ProcessInstanceNotFoundException e) {
            return notFound(MessageFormat.format(PROCESS_INSTANCE_NOT_FOUND, processInstanceId), v, conversationIdHeader);
        } catch (DeploymentNotFoundException e) {
            return notFound(MessageFormat.format(CONTAINER_NOT_FOUND, containerId), v, conversationIdHeader);
        } catch (Exception e) {
            logger.error("Unexpected error during processing {}", e.getMessage(), e);
            return internalServerError(MessageFormat.format(UNEXPECTED_ERROR, e.getMessage()), v, conversationIdHeader);
        }
    }

    @ApiOperation(value="Retrieves variables last value (from audit logs) for given process instance",
            response=VariableInstanceList.class, code=200)
    @ApiResponses(value = { @ApiResponse(code = 500, message = "Unexpected error"),
                            @ApiResponse(code = 404, message = "Process Instance or Container Id not found")})
    @GET
    @Path(PROCESS_INSTANCE_VAR_INSTANCES_GET_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getVariablesCurrentState(@Context HttpHeaders headers, 
            @ApiParam(value = "container id that process instance belongs to", required = true) @PathParam("id") String containerId, 
            @ApiParam(value = "identifier of the process instance that variables state should be collected for", required = true) @PathParam("pInstanceId") long processInstanceId) {

        Variant v = getVariant(headers);
        Header conversationIdHeader = buildConversationIdHeader(containerId, context, headers);
        try {
            VariableInstanceList variableInstanceList = runtimeDataServiceBase.getVariablesCurrentState(processInstanceId);
            logger.debug("Returning result of variables search: {}", variableInstanceList);
    
            return createCorrectVariant(variableInstanceList, headers, Response.Status.OK, conversationIdHeader);
        }  catch (ProcessInstanceNotFoundException e) {
            return notFound(MessageFormat.format(PROCESS_INSTANCE_NOT_FOUND, processInstanceId), v, conversationIdHeader);
        } catch (DeploymentNotFoundException e) {
            return notFound(MessageFormat.format(CONTAINER_NOT_FOUND, containerId), v, conversationIdHeader);
        } catch (Exception e) {
            logger.error("Unexpected error during processing {}", e.getMessage(), e);
            return internalServerError(MessageFormat.format(UNEXPECTED_ERROR, e.getMessage()), v, conversationIdHeader);
        }
    }

    @ApiOperation(value="Retrieves variable history (from audit logs) for given variable name that belongs to process instance",
            response=VariableInstanceList.class, code=200)
    @ApiResponses(value = { @ApiResponse(code = 500, message = "Unexpected error"),
                            @ApiResponse(code = 404, message = "Process Instance or Container Id not found")})
    @GET
    @Path(PROCESS_INSTANCE_VAR_INSTANCE_BY_VAR_NAME_GET_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getVariableHistory(@Context HttpHeaders headers,  
            @ApiParam(value = "container id that process instance belongs to", required = true) @PathParam("id") String containerId, 
            @ApiParam(value = "identifier of the process instance that variable history should be collected for", required = true) @PathParam("pInstanceId") long processInstanceId,
            @ApiParam(value = "name of the variables that history should be collected for", required = true) @PathParam("varName") String variableName,
            @ApiParam(value = "optional pagination - at which page to start, defaults to 0 (meaning first)", required = false) @QueryParam("page") @DefaultValue("0") Integer page, 
            @ApiParam(value = "optional pagination - size of the result, defaults to 10", required = false) @QueryParam("pageSize") @DefaultValue("10") Integer pageSize) {

        Header conversationIdHeader = buildConversationIdHeader(containerId, context, headers);
        Variant v = getVariant(headers);
        try {
            VariableInstanceList variableInstanceList = runtimeDataServiceBase.getVariableHistory(processInstanceId, variableName, page, pageSize);
            logger.debug("Returning result of variable '{}; history search: {}", variableName, variableInstanceList);
    
            return createCorrectVariant(variableInstanceList, headers, Response.Status.OK, conversationIdHeader);
        }  catch (ProcessInstanceNotFoundException e) {
            return notFound(MessageFormat.format(PROCESS_INSTANCE_NOT_FOUND, processInstanceId), v, conversationIdHeader);
        } catch (DeploymentNotFoundException e) {
            return notFound(MessageFormat.format(CONTAINER_NOT_FOUND, containerId), v, conversationIdHeader);
        } catch (Exception e) {
            logger.error("Unexpected error during processing {}", e.getMessage(), e);
            return internalServerError(MessageFormat.format(UNEXPECTED_ERROR, e.getMessage()), v, conversationIdHeader);
        }
    }

    @ApiOperation(value="Retrieves process instances that belong to given container and have given parent process instance, optionally allow to filter by process instance state.",
            response=ProcessInstanceList.class, code=200)
    @ApiResponses(value = { @ApiResponse(code = 500, message = "Unexpected error")})
    @GET
    @Path(PROCESS_INSTANCES_BY_PARENT_GET_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getProcessInstances(@Context HttpHeaders headers, 
            @ApiParam(value = "container id that process instance belongs to", required = true) @PathParam("id") String containerId, 
            @ApiParam(value = "identifier of the parent process instance that process instances should be collected for", required = true) @PathParam("pInstanceId") long parentProcessInstanceId,
            @ApiParam(value = "optional process instance status (active, completed, aborted) - defaults ot active (1) only", required = false, allowableValues="1,2,3") @QueryParam("status") List<Integer> status,
            @ApiParam(value = "optional pagination - at which page to start, defaults to 0 (meaning first)", required = false) @QueryParam("page") @DefaultValue("0") Integer page, 
            @ApiParam(value = "optional pagination - size of the result, defaults to 10", required = false) @QueryParam("pageSize") @DefaultValue("10") Integer pageSize,
            @ApiParam(value = "optional sort column, no default", required = false) @QueryParam("sort") String sort, 
            @ApiParam(value = "optional sort direction (asc, desc) - defaults to asc", required = false) @QueryParam("sortOrder") @DefaultValue("true") boolean sortOrder) {

        Header conversationIdHeader = buildConversationIdHeader(containerId, context, headers);
        ProcessInstanceList processInstanceList = processServiceBase.getProcessInstancesByParent(parentProcessInstanceId, status, page, pageSize, sort, sortOrder);
        logger.debug("Returning result of process instance search: {}", processInstanceList);

        return createCorrectVariant(processInstanceList, headers, Response.Status.OK, conversationIdHeader);
    }

}
