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
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Variant;

import org.kie.server.api.model.instance.RequestInfoInstanceList;
import org.kie.server.remote.rest.common.Header;
import org.kie.server.services.api.KieServerRegistry;
import org.kie.server.services.jbpm.ExecutorServiceBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.kie.server.api.rest.RestURI.*;
import static org.kie.server.remote.rest.common.util.RestUtils.*;
import static org.kie.server.remote.rest.jbpm.resources.Messages.*;

@Path("server/" + JOB_URI)
public class ExecutorResource {

    public static final Logger logger = LoggerFactory.getLogger(ExecutorResource.class);

    private ExecutorServiceBase executorServiceBase;
    private KieServerRegistry context;

    public ExecutorResource() {

    }

    public ExecutorResource(ExecutorServiceBase executorServiceBase, KieServerRegistry context) {
        this.executorServiceBase = executorServiceBase;
        this.context = context;
    }

    // operations
    @POST
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response scheduleRequest(@javax.ws.rs.core.Context HttpHeaders headers, @QueryParam("containerId") String containerId, String payload) {
        Variant v = getVariant(headers);
        String type = getContentType(headers);
        // no container id available so only used to transfer conversation id if given by client
        Header conversationIdHeader = buildConversationIdHeader("", context, headers);

        try {

            String response = executorServiceBase.scheduleRequest(containerId, payload, type);

            logger.debug("Returning CREATED response with content '{}'", response);
            return createResponse(response, v, Response.Status.CREATED, conversationIdHeader);

        } catch (IllegalArgumentException e) {
            logger.error("Invalid Command type ", e.getMessage(), e);
            return internalServerError( e.getMessage(), v, conversationIdHeader);
        } catch (Exception e) {
            logger.error("Unexpected error during processing {}", e.getMessage(), e);
            return internalServerError(MessageFormat.format(UNEXPECTED_ERROR, e.getMessage()), v, conversationIdHeader);
        }


    }

    @DELETE
    @Path(CANCEL_JOB_DEL_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response cancelRequest(@javax.ws.rs.core.Context HttpHeaders headers, @PathParam("jobId") long requestId) {
        Variant v = getVariant(headers);
        // no container id available so only used to transfer conversation id if given by client
        Header conversationIdHeader = buildConversationIdHeader("", context, headers);
        try {
            executorServiceBase.cancelRequest(requestId);
            // produce 204 NO_CONTENT response code
            return noContent(v, conversationIdHeader);
        } catch (Exception e) {
            logger.error("Unexpected error during processing {}", e.getMessage(), e);
            return internalServerError(MessageFormat.format(UNEXPECTED_ERROR, e.getMessage()), v, conversationIdHeader);
        }
    }

    @PUT
    @Path(REQUEUE_JOB_PUT_URI)
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response requeueRequest(@javax.ws.rs.core.Context HttpHeaders headers, @PathParam("jobId") long requestId){
        Variant v = getVariant(headers);
        // no container id available so only used to transfer conversation id if given by client
        Header conversationIdHeader = buildConversationIdHeader("", context, headers);
        try {
            executorServiceBase.requeueRequest(requestId);

            return createResponse("", v, Response.Status.CREATED, conversationIdHeader);
        } catch (Exception e) {
            logger.error("Unexpected error during processing {}", e.getMessage(), e);
            return internalServerError(MessageFormat.format(UNEXPECTED_ERROR, e.getMessage()), v, conversationIdHeader);
        }
    }

    // queries
    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getRequestsByStatus(@javax.ws.rs.core.Context HttpHeaders headers, @QueryParam("status") List<String> statuses,
            @QueryParam("page") @DefaultValue("0") Integer page, @QueryParam("pageSize") @DefaultValue("10") Integer pageSize) {
        Variant v = getVariant(headers);
        // no container id available so only used to transfer conversation id if given by client
        Header conversationIdHeader = buildConversationIdHeader("", context, headers);
        try {
            RequestInfoInstanceList result = executorServiceBase.getRequestsByStatus(statuses, page, pageSize);

            return createCorrectVariant(result, headers, Response.Status.OK, conversationIdHeader);
        }  catch (Exception e) {
            logger.error("Unexpected error during processing {}", e.getMessage(), e);
            return internalServerError(MessageFormat.format(UNEXPECTED_ERROR, e.getMessage()), v, conversationIdHeader);
        }

    }

    @GET
    @Path(JOB_INSTANCES_BY_KEY_GET_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getRequestsByBusinessKey(@javax.ws.rs.core.Context HttpHeaders headers, @PathParam("key") String businessKey,
            @QueryParam("page") @DefaultValue("0") Integer page, @QueryParam("pageSize") @DefaultValue("10") Integer pageSize) {

        Variant v = getVariant(headers);
        // no container id available so only used to transfer conversation id if given by client
        Header conversationIdHeader = buildConversationIdHeader("", context, headers);
        try {
            RequestInfoInstanceList result = executorServiceBase.getRequestsByBusinessKey(businessKey, page, pageSize);

            return createCorrectVariant(result, headers, Response.Status.OK, conversationIdHeader);
        }  catch (Exception e) {
            logger.error("Unexpected error during processing {}", e.getMessage(), e);
            return internalServerError(MessageFormat.format(UNEXPECTED_ERROR, e.getMessage()), v, conversationIdHeader);
        }
    }

    @GET
    @Path(JOB_INSTANCES_BY_CMD_GET_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getRequestsByCommand(@javax.ws.rs.core.Context HttpHeaders headers, @PathParam("cmd") String command,
            @QueryParam("page") @DefaultValue("0") Integer page, @QueryParam("pageSize") @DefaultValue("10") Integer pageSize) {
        Variant v = getVariant(headers);
        // no container id available so only used to transfer conversation id if given by client
        Header conversationIdHeader = buildConversationIdHeader("", context, headers);
        try {
            RequestInfoInstanceList result = executorServiceBase.getRequestsByCommand(command, page, pageSize);

            return createCorrectVariant(result, headers, Response.Status.OK, conversationIdHeader);
        }  catch (Exception e) {
            logger.error("Unexpected error during processing {}", e.getMessage(), e);
            return internalServerError(MessageFormat.format(UNEXPECTED_ERROR, e.getMessage()), v, conversationIdHeader);
        }
    }

    // instance details
    @GET
    @Path(JOB_INSTANCE_GET_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getRequestById(@javax.ws.rs.core.Context HttpHeaders headers, @PathParam("jobId") Long requestId,
            @QueryParam("withErrors") boolean withErrors, @QueryParam("withData") boolean withData) {

        Variant v = getVariant(headers);
        String type = getContentType(headers);
        // no container id available so only used to transfer conversation id if given by client
        Header conversationIdHeader = buildConversationIdHeader("", context, headers);
        try {

            String response = executorServiceBase.getRequestById(requestId, withErrors, withData, type);

            return createResponse(response, v, Response.Status.OK, conversationIdHeader);
        } catch (Exception e) {
            logger.error("Unexpected error during processing {}", e.getMessage(), e);
            return internalServerError(MessageFormat.format(UNEXPECTED_ERROR, e.getMessage()), v, conversationIdHeader);
        }
    }
}
