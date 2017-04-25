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

package org.kie.server.remote.rest.jbpm.admin;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Variant;

import org.jbpm.services.api.DeploymentNotFoundException;
import org.jbpm.services.api.NodeInstanceNotFoundException;
import org.jbpm.services.api.NodeNotFoundException;
import org.jbpm.services.api.ProcessInstanceNotFoundException;
import org.jbpm.services.api.admin.ExecutionErrorNotFoundException;
import org.kie.server.api.model.admin.ExecutionErrorInstance;
import org.kie.server.api.model.admin.ExecutionErrorInstanceList;
import org.kie.server.api.model.admin.MigrationReportInstance;
import org.kie.server.api.model.admin.MigrationReportInstanceList;
import org.kie.server.api.model.admin.ProcessNodeList;
import org.kie.server.api.model.admin.TimerInstanceList;
import org.kie.server.api.model.instance.NodeInstanceList;
import org.kie.server.remote.rest.common.Header;
import org.kie.server.services.api.KieServerRegistry;
import org.kie.server.services.jbpm.admin.ProcessAdminServiceBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.kie.server.api.rest.RestURI.*;
import static org.kie.server.remote.rest.common.util.RestUtils.*;
import static org.kie.server.remote.rest.jbpm.resources.Messages.*;

@Path("server/" + ADMIN_PROCESS_URI)
public class ProcessAdminResource {

    private static final Logger logger = LoggerFactory.getLogger(ProcessAdminResource.class);

    private ProcessAdminServiceBase processAdminServiceBase;
    private KieServerRegistry context;


    public ProcessAdminResource() {

    }

    public ProcessAdminResource(ProcessAdminServiceBase processAdminServiceBase, KieServerRegistry context) {
        this.processAdminServiceBase = processAdminServiceBase;
        this.context = context;
    }

    @PUT
    @Path(MIGRATE_PROCESS_INST_PUT_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response migrateProcessInstance(@javax.ws.rs.core.Context HttpHeaders headers, @PathParam("id") String containerId, @PathParam("pInstanceId") Long processInstanceId,
            @QueryParam("targetContainerId") String targetContainerId, @QueryParam("targetProcessId") String targetProcessId, String payload) {
        Variant v = getVariant(headers);
        String type = getContentType(headers);
        Header conversationIdHeader = buildConversationIdHeader(containerId, context, headers);
        try {

            MigrationReportInstance reportInstance = processAdminServiceBase.migrateProcessInstance(containerId, processInstanceId, targetContainerId, targetProcessId, payload, type);

            return createCorrectVariant(reportInstance, headers, Response.Status.CREATED, conversationIdHeader);
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


    @PUT
    @Path(MIGRATE_PROCESS_INSTANCES_PUT_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response migrateProcessInstances(@javax.ws.rs.core.Context HttpHeaders headers, @PathParam("id") String containerId, @QueryParam("pInstanceId") List<Long> processInstanceIds,
            @QueryParam("targetContainerId") String targetContainerId, @QueryParam("targetProcessId") String targetProcessId, String payload) {
        Variant v = getVariant(headers);
        String type = getContentType(headers);
        Header conversationIdHeader = buildConversationIdHeader(containerId, context, headers);
        try {
            MigrationReportInstanceList reportInstances = processAdminServiceBase.migrateProcessInstances(containerId, processInstanceIds, targetContainerId, targetProcessId, payload, type);

            return createCorrectVariant(reportInstances, headers, Response.Status.CREATED, conversationIdHeader);
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


    @DELETE
    @Path(CANCEL_NODE_INST_PROCESS_INST_DELETE_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response cancelNodeInstance(@javax.ws.rs.core.Context HttpHeaders headers, @PathParam("id") String containerId, @PathParam("pInstanceId") Long processInstanceId,
            @PathParam("nodeInstanceId") Long nodeInstanceId) {
        Variant v = getVariant(headers);
        Header conversationIdHeader = buildConversationIdHeader(containerId, context, headers);
        try {

            processAdminServiceBase.cancelNodeInstance(containerId, processInstanceId, nodeInstanceId);

            return noContent(v, conversationIdHeader);
        } catch (NodeInstanceNotFoundException e) {
            return notFound(
                    MessageFormat.format(NODE_INSTANCE_NOT_FOUND, nodeInstanceId, processInstanceId), v, conversationIdHeader);
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

    @PUT
    @Path(RETRIGGER_NODE_INST_PROCESS_INST_PUT_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response retriggerNodeInstance(@javax.ws.rs.core.Context HttpHeaders headers, @PathParam("id") String containerId, @PathParam("pInstanceId") Long processInstanceId,
            @PathParam("nodeInstanceId") Long nodeInstanceId) {
        Variant v = getVariant(headers);
        Header conversationIdHeader = buildConversationIdHeader(containerId, context, headers);
        try {

            processAdminServiceBase.retriggerNodeInstance(containerId, processInstanceId, nodeInstanceId);

            return createResponse("", v, Response.Status.CREATED, conversationIdHeader);
        } catch (NodeInstanceNotFoundException e) {
            return notFound(
                    MessageFormat.format(NODE_INSTANCE_NOT_FOUND, nodeInstanceId, processInstanceId), v, conversationIdHeader);
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

    @PUT
    @Path(UPDATE_TIMER_PROCESS_INST_PUT_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response updateTimer(@javax.ws.rs.core.Context HttpHeaders headers, @PathParam("id") String containerId, @PathParam("pInstanceId") Long processInstanceId,
            @PathParam("timerId") Long timerId, @QueryParam("relative") @DefaultValue("true") boolean relative, String payload) {
        Variant v = getVariant(headers);
        String type = getContentType(headers);
        Header conversationIdHeader = buildConversationIdHeader(containerId, context, headers);
        try {

            processAdminServiceBase.updateTimer(containerId, processInstanceId, timerId, relative, payload, type);

            return createResponse("", v, Response.Status.CREATED, conversationIdHeader);
        } catch (NodeInstanceNotFoundException e) {
            return notFound(
                    MessageFormat.format(TIMER_INSTANCE_NOT_FOUND, timerId, processInstanceId), v, conversationIdHeader);
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

    @POST
    @Path(TRIGGER_NODE_PROCESS_INST_POST_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response triggerNode(@javax.ws.rs.core.Context HttpHeaders headers, @PathParam("id") String containerId, @PathParam("pInstanceId") Long processInstanceId,
            @PathParam("nodeId") Long nodeId) {
        Variant v = getVariant(headers);
        String type = getContentType(headers);
        Header conversationIdHeader = buildConversationIdHeader(containerId, context, headers);
        try {

            processAdminServiceBase.triggerNode(containerId, processInstanceId, nodeId);

            return createResponse("", v, Response.Status.CREATED, conversationIdHeader);
        } catch (NodeNotFoundException e) {
            return notFound(
                    MessageFormat.format(NODE_NOT_FOUND, nodeId, processInstanceId), v, conversationIdHeader);
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
    @Path(TIMERS_PROCESS_INST_GET_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getTimerInstances(@javax.ws.rs.core.Context HttpHeaders headers, @PathParam("id") String containerId, @PathParam("pInstanceId") Long processInstanceId) {
        Variant v = getVariant(headers);
        Header conversationIdHeader = buildConversationIdHeader(containerId, context, headers);
        try {
            TimerInstanceList timerInstanceList = processAdminServiceBase.getTimerInstances(containerId, processInstanceId);

            return createCorrectVariant(timerInstanceList, headers, Response.Status.OK, conversationIdHeader);
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
    @Path(NODE_INSTANCES_PROCESS_INST_GET_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getActiveNodeInstances(@javax.ws.rs.core.Context HttpHeaders headers, @PathParam("id") String containerId, @PathParam("pInstanceId") Long processInstanceId) {
        Variant v = getVariant(headers);
        Header conversationIdHeader = buildConversationIdHeader(containerId, context, headers);
        try {
            NodeInstanceList nodeInstanceList = processAdminServiceBase.getActiveNodeInstances(containerId, processInstanceId);

            return createCorrectVariant(nodeInstanceList, headers, Response.Status.OK, conversationIdHeader);
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
    @Path(NODES_PROCESS_INST_GET_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getNodes(@javax.ws.rs.core.Context HttpHeaders headers, @PathParam("id") String containerId, @PathParam("pInstanceId") Long processInstanceId) {
        Variant v = getVariant(headers);
        Header conversationIdHeader = buildConversationIdHeader(containerId, context, headers);
        try {
            ProcessNodeList processNodeList = processAdminServiceBase.getProcessNodes(containerId, processInstanceId);

            return createCorrectVariant(processNodeList, headers, Response.Status.OK, conversationIdHeader);
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

    @PUT
    @Path(ACK_ERROR_PUT_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response acknowledgeError(@javax.ws.rs.core.Context HttpHeaders headers, @PathParam("id") String containerId, @PathParam("errorId") String errorId) {
        Variant v = getVariant(headers);
        Header conversationIdHeader = buildConversationIdHeader(containerId, context, headers);
        try {

            processAdminServiceBase.acknowledgeError(containerId, Arrays.asList(errorId));
            return createCorrectVariant("", headers, Response.Status.CREATED, conversationIdHeader);
        } catch (ExecutionErrorNotFoundException e) {
            return notFound(e.getMessage(), v, conversationIdHeader);
        } catch (DeploymentNotFoundException e) {
            return notFound(MessageFormat.format(CONTAINER_NOT_FOUND, containerId), v, conversationIdHeader);
        } catch (Exception e) {
            logger.error("Unexpected error during processing {}", e.getMessage(), e);
            return internalServerError(MessageFormat.format(UNEXPECTED_ERROR, e.getMessage()), v, conversationIdHeader);
        }
    }

    @PUT
    @Path(ACK_ERRORS_PUT_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response acknowledgeErrors(@javax.ws.rs.core.Context HttpHeaders headers, @PathParam("id") String containerId, @QueryParam("errorId") List<String> errorIds) {
        Variant v = getVariant(headers);
        Header conversationIdHeader = buildConversationIdHeader(containerId, context, headers);
        try {

            processAdminServiceBase.acknowledgeError(containerId, errorIds);
            return createCorrectVariant("", headers, Response.Status.CREATED, conversationIdHeader);
        } catch (ExecutionErrorNotFoundException e) {
            return notFound(e.getMessage(), v, conversationIdHeader);
        } catch (DeploymentNotFoundException e) {
            return notFound(MessageFormat.format(CONTAINER_NOT_FOUND, containerId), v, conversationIdHeader);
        } catch (Exception e) {
            logger.error("Unexpected error during processing {}", e.getMessage(), e);
            return internalServerError(MessageFormat.format(UNEXPECTED_ERROR, e.getMessage()), v, conversationIdHeader);
        }
    }

    @GET
    @Path(ERROR_GET_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getExecutionErrorById(@javax.ws.rs.core.Context HttpHeaders headers, @PathParam("id") String containerId, @PathParam("errorId") String errorId) {
        Variant v = getVariant(headers);
        Header conversationIdHeader = buildConversationIdHeader(containerId, context, headers);
        try {
            ExecutionErrorInstance executionErrorInstance = processAdminServiceBase.getError(containerId, errorId);

            return createCorrectVariant(executionErrorInstance, headers, Response.Status.OK, conversationIdHeader);
        } catch (ExecutionErrorNotFoundException e) {
            return notFound(e.getMessage(), v, conversationIdHeader);
        } catch (DeploymentNotFoundException e) {
            return notFound(
                    MessageFormat.format(CONTAINER_NOT_FOUND, containerId), v, conversationIdHeader);
        } catch (Exception e) {
            logger.error("Unexpected error during processing {}", e.getMessage(), e);
            return internalServerError(MessageFormat.format(UNEXPECTED_ERROR, e.getMessage()), v, conversationIdHeader);
        }
    }

    @GET
    @Path(ERRORS_BY_PROCESS_INST_GET_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getExecutionErrorsByProcessInstance(@javax.ws.rs.core.Context HttpHeaders headers, @PathParam("id") String containerId, @PathParam("pInstanceId") Long processInstanceId,
            @QueryParam("includeAck") @DefaultValue("false") boolean includeAcknowledged, @QueryParam("node") String nodeName,
            @QueryParam("page") @DefaultValue("0") Integer page, @QueryParam("pageSize") @DefaultValue("10") Integer pageSize,
            @QueryParam("sort") String sort, @QueryParam("sortOrder") @DefaultValue("true") boolean sortOrder) {
        Variant v = getVariant(headers);
        Header conversationIdHeader = buildConversationIdHeader(containerId, context, headers);
        try {
            ExecutionErrorInstanceList executionErrorInstanceList = processAdminServiceBase.getExecutionErrorsByProcessInstance(containerId, processInstanceId, nodeName,
                    includeAcknowledged, page, pageSize, sort, sortOrder);

            return createCorrectVariant(executionErrorInstanceList, headers, Response.Status.OK, conversationIdHeader);
        } catch (ExecutionErrorNotFoundException e) {
            return notFound(e.getMessage(), v, conversationIdHeader);
        } catch (DeploymentNotFoundException e) {
            return notFound(
                    MessageFormat.format(CONTAINER_NOT_FOUND, containerId), v, conversationIdHeader);
        } catch (Exception e) {
            logger.error("Unexpected error during processing {}", e.getMessage(), e);
            return internalServerError(MessageFormat.format(UNEXPECTED_ERROR, e.getMessage()), v, conversationIdHeader);
        }
    }

    @GET
    @Path(ERRORS_GET_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getExecutionErrors(@javax.ws.rs.core.Context HttpHeaders headers, @PathParam("id") String containerId,
            @QueryParam("includeAck") @DefaultValue("false") boolean includeAcknowledged,
            @QueryParam("page") @DefaultValue("0") Integer page, @QueryParam("pageSize") @DefaultValue("10") Integer pageSize,
            @QueryParam("sort") String sort, @QueryParam("sortOrder") @DefaultValue("true") boolean sortOrder) {
        Variant v = getVariant(headers);
        Header conversationIdHeader = buildConversationIdHeader(containerId, context, headers);
        try {
            ExecutionErrorInstanceList executionErrorInstanceList = processAdminServiceBase.getExecutionErrors(containerId, includeAcknowledged, page, pageSize, sort, sortOrder);

            return createCorrectVariant(executionErrorInstanceList, headers, Response.Status.OK, conversationIdHeader);
        } catch (ExecutionErrorNotFoundException e) {
            return notFound(e.getMessage(), v, conversationIdHeader);
        } catch (DeploymentNotFoundException e) {
            return notFound(
                    MessageFormat.format(CONTAINER_NOT_FOUND, containerId), v, conversationIdHeader);
        } catch (Exception e) {
            logger.error("Unexpected error during processing {}", e.getMessage(), e);
            return internalServerError(MessageFormat.format(UNEXPECTED_ERROR, e.getMessage()), v, conversationIdHeader);
        }
    }
}
