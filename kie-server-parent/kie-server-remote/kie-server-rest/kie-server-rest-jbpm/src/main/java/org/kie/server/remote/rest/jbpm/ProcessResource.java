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
import org.jbpm.services.api.ProcessInstanceNotFoundException;
import org.jbpm.services.api.WorkItemNotFoundException;
import org.kie.internal.KieInternalServices;
import org.kie.internal.process.CorrelationKeyFactory;
import org.kie.server.api.model.definition.ProcessDefinitionList;
import org.kie.server.api.model.instance.NodeInstanceList;
import org.kie.server.api.model.instance.ProcessInstanceList;
import org.kie.server.api.model.instance.VariableInstanceList;
import org.kie.server.remote.rest.common.Header;
import org.kie.server.services.api.KieServerRegistry;
import org.kie.server.services.jbpm.ProcessServiceBase;
import org.kie.server.services.jbpm.RuntimeDataServiceBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.kie.server.api.rest.RestURI.*;
import static org.kie.server.remote.rest.common.util.RestUtils.*;
import static org.kie.server.remote.rest.jbpm.resources.Messages.*;

@Path("server/" + PROCESS_URI)
public class ProcessResource  {

    public static final Logger logger = LoggerFactory.getLogger(ProcessResource.class);

    private ProcessServiceBase processServiceBase;
    private RuntimeDataServiceBase runtimeDataServiceBase;
    private KieServerRegistry context;

    private CorrelationKeyFactory correlationKeyFactory = KieInternalServices.Factory.get().newCorrelationKeyFactory();

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

    @POST
    @Path(START_PROCESS_POST_URI)
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response startProcess(@javax.ws.rs.core.Context HttpHeaders headers, @PathParam("id") String containerId, @PathParam("pId") String processId, @DefaultValue("") String payload) {
        Variant v = getVariant(headers);
        String type = getContentType(headers);

        try {
            String response = processServiceBase.startProcess(containerId, processId, payload, type);

            logger.debug("Returning CREATED response with content '{}'", response);
            Header conversationIdHeader = buildConversationIdHeader(containerId, context, headers);
            return createResponse(response, v, Response.Status.CREATED, conversationIdHeader);
        } catch (Exception e) {
            logger.error("Unexpected error during processing {}", e.getMessage(), e);
            return internalServerError(
                    MessageFormat.format(CREATE_RESPONSE_ERROR, e.getMessage()), v);
        }
    }

    @POST
    @Path(START_PROCESS_WITH_CORRELATION_KEY_POST_URI)
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response startProcessWithCorrelation(@javax.ws.rs.core.Context HttpHeaders headers, @PathParam("id") String containerId, @PathParam("pId") String processId,
            @PathParam("correlationKey") String correlationKey, @DefaultValue("") String payload) {
        Variant v = getVariant(headers);
        String type = getContentType(headers);

        try {
            String response = processServiceBase.startProcessWithCorrelation(containerId, processId, correlationKey, payload, type);

            logger.debug("Returning CREATED response with content '{}'", response);
            Header conversationIdHeader = buildConversationIdHeader(containerId, context, headers);
            return createResponse(response, v, Response.Status.CREATED, conversationIdHeader);
        } catch (Exception e) {
            logger.error("Unexpected error during processing {}", e.getMessage(), e);
            return internalServerError(
                    MessageFormat.format(CREATE_RESPONSE_ERROR, e.getMessage()), v);
        }
    }


    @DELETE
    @Path(ABORT_PROCESS_INST_DEL_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response abortProcessInstance(@javax.ws.rs.core.Context HttpHeaders headers, @PathParam("id") String containerId, @PathParam("pInstanceId") Long processInstanceId) {
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


    @DELETE
    @Path(ABORT_PROCESS_INSTANCES_DEL_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response abortProcessInstances(@javax.ws.rs.core.Context HttpHeaders headers, @PathParam("id") String containerId, @QueryParam("instanceId") List<Long> processInstanceIds) {
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

    @POST
    @Path(SIGNAL_PROCESS_INST_POST_URI)
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response signalProcessInstance(@javax.ws.rs.core.Context HttpHeaders headers, @PathParam("id") String containerId,
            @PathParam("pInstanceId") Long processInstanceId, @PathParam("sName") String signalName, String eventPayload) {

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


    @POST
    @Path(SIGNAL_PROCESS_INSTANCES_PORT_URI)
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response signalProcessInstances(@javax.ws.rs.core.Context HttpHeaders headers, @PathParam("id") String containerId,
            @QueryParam("instanceId") List<Long> processInstanceIds, @PathParam("sName") String signalName, String eventPayload) {
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

    @GET
    @Path(PROCESS_INSTANCE_GET_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getProcessInstance(@javax.ws.rs.core.Context HttpHeaders headers, @PathParam("id") String containerId,
            @PathParam("pInstanceId") Long processInstanceId, @QueryParam("withVars") boolean withVars) {
        Variant v = getVariant(headers);
        String type = getContentType(headers);
        try {

            String response = processServiceBase.getProcessInstance(containerId, processInstanceId, withVars, type);

            logger.debug("Returning OK response with content '{}'", response);
            Header conversationIdHeader = buildConversationIdHeader(containerId, context, headers);
            return createResponse(response, v, Response.Status.OK, conversationIdHeader);

        } catch (Exception e) {
            logger.error("Unexpected error during processing {}", e.getMessage(), e);
            return internalServerError(MessageFormat.format(UNEXPECTED_ERROR, e.getMessage()), v);
        }
    }


    @PUT
    @Path(PROCESS_INSTANCE_VAR_PUT_URI)
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response setProcessVariable(@javax.ws.rs.core.Context HttpHeaders headers, @PathParam("id") String containerId,
            @PathParam("pInstanceId") Long processInstanceId, @PathParam("varName") String varName, String variablePayload) {

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

    @POST
    @Path(PROCESS_INSTANCE_VARS_POST_URI)
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response setProcessVariables(@javax.ws.rs.core.Context HttpHeaders headers, @PathParam("id") String containerId,
            @PathParam("pInstanceId") Long processInstanceId, String variablePayload) {
        Variant v = getVariant(headers);
        String type = getContentType(headers);
        Header conversationIdHeader = buildConversationIdHeader(containerId, context, headers);
        try {

            processServiceBase.setProcessVariables(containerId, processInstanceId, variablePayload, type);
            return createResponse("", v, Response.Status.OK, conversationIdHeader);

        } catch (ProcessInstanceNotFoundException e) {
            return notFound(MessageFormat.format(PROCESS_INSTANCE_NOT_FOUND, processInstanceId), v, conversationIdHeader);
        } catch (DeploymentNotFoundException e) {
            return notFound(MessageFormat.format(CONTAINER_NOT_FOUND, containerId), v, conversationIdHeader);
        } catch (Exception e) {
            logger.error("Unexpected error during processing {}", e.getMessage(), e);
            return internalServerError(MessageFormat.format(UNEXPECTED_ERROR, e.getMessage()), v, conversationIdHeader);
        }
    }

    @GET
    @Path(PROCESS_INSTANCE_VAR_GET_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Object getProcessInstanceVariable(@javax.ws.rs.core.Context HttpHeaders headers, @PathParam("id") String containerId,
                                             @PathParam("pInstanceId") Long processInstanceId, @PathParam("varName") String varName) {
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

    @GET
    @Path(PROCESS_INSTANCE_VARS_GET_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getProcessInstanceVariables(@javax.ws.rs.core.Context HttpHeaders headers, @PathParam("id") String containerId,
            @PathParam("pInstanceId") Long processInstanceId) {
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


    @GET
    @Path(PROCESS_INSTANCE_SIGNALS_GET_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getAvailableSignals(@javax.ws.rs.core.Context HttpHeaders headers, @PathParam("id") String containerId,
            @PathParam("pInstanceId") Long processInstanceId) {
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


    @PUT
    @Path(PROCESS_INSTANCE_WORK_ITEM_COMPLETE_PUT_URI)
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response completeWorkItem(@javax.ws.rs.core.Context HttpHeaders headers, @PathParam("id") String containerId,
            @PathParam("pInstanceId") Long processInstanceId, @PathParam("workItemId") Long workItemId, String resultPayload) {

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


    @PUT
    @Path(PROCESS_INSTANCE_WORK_ITEM_ABORT_PUT_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response abortWorkItem(@javax.ws.rs.core.Context HttpHeaders headers, @PathParam("id") String containerId,
            @PathParam("pInstanceId") Long processInstanceId, @PathParam("workItemId") Long workItemId) {
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


    @GET
    @Path(PROCESS_INSTANCE_WORK_ITEM_BY_ID_GET_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getWorkItem(@javax.ws.rs.core.Context HttpHeaders headers, @PathParam("id") String containerId,
            @PathParam("pInstanceId") Long processInstanceId, @PathParam("workItemId") Long workItemId) {
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


    @GET
    @Path(PROCESS_INSTANCE_WORK_ITEMS_BY_PROC_INST_ID_GET_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getWorkItemByProcessInstance(@javax.ws.rs.core.Context HttpHeaders headers, @PathParam("id") String containerId,
            @PathParam("pInstanceId") Long processInstanceId) {
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

    @GET
    @Path(PROCESS_INSTANCES_BY_CONTAINER_GET_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getProcessInstancesByDeploymentId(@Context HttpHeaders headers, @PathParam("id") String containerId, @QueryParam("status")List<Integer> status,
            @QueryParam("page") @DefaultValue("0") Integer page, @QueryParam("pageSize") @DefaultValue("10") Integer pageSize,
            @QueryParam("sort") String sort, @QueryParam("sortOrder") @DefaultValue("true") boolean sortOrder) {

        Header conversationIdHeader = buildConversationIdHeader(containerId, context, headers);

        ProcessInstanceList processInstanceList = runtimeDataServiceBase.getProcessInstancesByDeploymentId(containerId, status, page, pageSize, sort, sortOrder);
        logger.debug("Returning result of process instance search: {}", processInstanceList);

        return createCorrectVariant(processInstanceList, headers, Response.Status.OK, conversationIdHeader);
    }

    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getProcessesByDeploymentId(@Context HttpHeaders headers, @PathParam("id") String containerId,
            @QueryParam("page") @DefaultValue("0") Integer page, @QueryParam("pageSize") @DefaultValue("10") Integer pageSize,
            @QueryParam("sort") String sort, @QueryParam("sortOrder") @DefaultValue("true") boolean sortOrder) {

        Header conversationIdHeader = buildConversationIdHeader(containerId, context, headers);

        ProcessDefinitionList processDefinitionList = runtimeDataServiceBase.getProcessesByDeploymentId(containerId, page, pageSize, sort, sortOrder);
        logger.debug("Returning result of process definition search: {}", processDefinitionList);

        return createCorrectVariant(processDefinitionList, headers, Response.Status.OK, conversationIdHeader);

    }

    @GET
    @Path(NODE_INSTANCES_BY_INSTANCE_ID_GET_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getProcessInstanceHistory(@Context HttpHeaders headers, @PathParam("id") String containerId, @PathParam("pInstanceId") long processInstanceId,
            @QueryParam("activeOnly")Boolean active, @QueryParam("completedOnly")Boolean completed,
            @QueryParam("page") @DefaultValue("0") Integer page, @QueryParam("pageSize") @DefaultValue("10") Integer pageSize) {

        Header conversationIdHeader = buildConversationIdHeader(containerId, context, headers);

        NodeInstanceList nodeInstanceList = runtimeDataServiceBase.getProcessInstanceHistory(processInstanceId, active, completed, page, pageSize);
        logger.debug("Returning result of node instances search: {}", nodeInstanceList);
        return createCorrectVariant(nodeInstanceList, headers, Response.Status.OK, conversationIdHeader);
    }

    @GET
    @Path(VAR_INSTANCES_BY_INSTANCE_ID_GET_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getVariablesCurrentState(@Context HttpHeaders headers, @PathParam("id") String containerId, @PathParam("pInstanceId") long processInstanceId) {

        Header conversationIdHeader = buildConversationIdHeader(containerId, context, headers);

        VariableInstanceList variableInstanceList = runtimeDataServiceBase.getVariablesCurrentState(processInstanceId);
        logger.debug("Returning result of variables search: {}", variableInstanceList);

        return createCorrectVariant(variableInstanceList, headers, Response.Status.OK, conversationIdHeader);
    }

    @GET
    @Path(VAR_INSTANCES_BY_VAR_INSTANCE_ID_GET_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getVariableHistory(@Context HttpHeaders headers,  @PathParam("id") String containerId, @PathParam("pInstanceId") long processInstanceId,
            @PathParam("varName") String variableName,
            @QueryParam("page") @DefaultValue("0") Integer page, @QueryParam("pageSize") @DefaultValue("10") Integer pageSize) {

        Header conversationIdHeader = buildConversationIdHeader(containerId, context, headers);

        VariableInstanceList variableInstanceList = runtimeDataServiceBase.getVariableHistory(processInstanceId, variableName, page, pageSize);
        logger.debug("Returning result of variable '{}; history search: {}", variableName, variableInstanceList);

        return createCorrectVariant(variableInstanceList, headers, Response.Status.OK, conversationIdHeader);
    }

    @GET
    @Path(PROCESS_INSTANCES_BY_PARENT_GET_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getProcessInstances(@Context HttpHeaders headers, @PathParam("id") String containerId, @PathParam("pInstanceId") long parentProcessInstanceId,
            @QueryParam("status") List<Integer> status,
            @QueryParam("page") @DefaultValue("0") Integer page, @QueryParam("pageSize") @DefaultValue("10") Integer pageSize,
            @QueryParam("sort") String sort, @QueryParam("sortOrder") @DefaultValue("true") boolean sortOrder) {

        Header conversationIdHeader = buildConversationIdHeader(containerId, context, headers);
        ProcessInstanceList processInstanceList = processServiceBase.getProcessInstancesByParent(parentProcessInstanceId, status, page, pageSize, sort, sortOrder);
        logger.debug("Returning result of process instance search: {}", processInstanceList);

        return createCorrectVariant(processInstanceList, headers, Response.Status.OK, conversationIdHeader);
    }

}
