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
import java.util.Map;

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

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Example;
import io.swagger.annotations.ExampleProperty;
import org.jbpm.services.api.TaskNotFoundException;
import org.jbpm.services.task.exception.PermissionDeniedException;
import org.kie.server.api.model.instance.TaskAttachment;
import org.kie.server.api.model.instance.TaskAttachmentList;
import org.kie.server.api.model.instance.TaskComment;
import org.kie.server.api.model.instance.TaskCommentList;
import org.kie.server.api.model.instance.TaskEventInstanceList;
import org.kie.server.api.model.instance.TaskInstance;
import org.kie.server.remote.rest.common.Header;
import org.kie.server.services.api.KieServerRegistry;
import org.kie.server.services.jbpm.RuntimeDataServiceBase;
import org.kie.server.services.jbpm.UserTaskServiceBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.kie.server.api.rest.RestURI.CONTAINER_ID;
import static org.kie.server.api.rest.RestURI.TASK_INSTANCE_ACTIVATE_PUT_URI;
import static org.kie.server.api.rest.RestURI.TASK_INSTANCE_ATTACHMENTS_GET_URI;
import static org.kie.server.api.rest.RestURI.TASK_INSTANCE_ATTACHMENT_ADD_POST_URI;
import static org.kie.server.api.rest.RestURI.TASK_INSTANCE_ATTACHMENT_CONTENT_GET_URI;
import static org.kie.server.api.rest.RestURI.TASK_INSTANCE_ATTACHMENT_DELETE_URI;
import static org.kie.server.api.rest.RestURI.TASK_INSTANCE_ATTACHMENT_GET_URI;
import static org.kie.server.api.rest.RestURI.TASK_INSTANCE_CLAIM_PUT_URI;
import static org.kie.server.api.rest.RestURI.TASK_INSTANCE_COMMENTS_GET_URI;
import static org.kie.server.api.rest.RestURI.TASK_INSTANCE_COMMENT_ADD_POST_URI;
import static org.kie.server.api.rest.RestURI.TASK_INSTANCE_COMMENT_DELETE_URI;
import static org.kie.server.api.rest.RestURI.TASK_INSTANCE_COMMENT_GET_URI;
import static org.kie.server.api.rest.RestURI.TASK_INSTANCE_COMPLETE_PUT_URI;
import static org.kie.server.api.rest.RestURI.TASK_INSTANCE_CONTENT_DATA_DELETE_URI;
import static org.kie.server.api.rest.RestURI.TASK_INSTANCE_DELEGATE_PUT_URI;
import static org.kie.server.api.rest.RestURI.TASK_INSTANCE_DESCRIPTION_PUT_URI;
import static org.kie.server.api.rest.RestURI.TASK_INSTANCE_EVENTS_GET_URI;
import static org.kie.server.api.rest.RestURI.TASK_INSTANCE_EXIT_PUT_URI;
import static org.kie.server.api.rest.RestURI.TASK_INSTANCE_EXPIRATION_DATE_PUT_URI;
import static org.kie.server.api.rest.RestURI.TASK_INSTANCE_FAIL_PUT_URI;
import static org.kie.server.api.rest.RestURI.TASK_INSTANCE_FORWARD_PUT_URI;
import static org.kie.server.api.rest.RestURI.TASK_INSTANCE_GET_URI;
import static org.kie.server.api.rest.RestURI.TASK_INSTANCE_ID;
import static org.kie.server.api.rest.RestURI.TASK_INSTANCE_INPUT_DATA_GET_URI;
import static org.kie.server.api.rest.RestURI.TASK_INSTANCE_NAME_PUT_URI;
import static org.kie.server.api.rest.RestURI.TASK_INSTANCE_NOMINATE_PUT_URI;
import static org.kie.server.api.rest.RestURI.TASK_INSTANCE_OUTPUT_DATA_GET_URI;
import static org.kie.server.api.rest.RestURI.TASK_INSTANCE_OUTPUT_DATA_PUT_URI;
import static org.kie.server.api.rest.RestURI.TASK_INSTANCE_PRIORITY_PUT_URI;
import static org.kie.server.api.rest.RestURI.TASK_INSTANCE_PUT_URI;
import static org.kie.server.api.rest.RestURI.TASK_INSTANCE_RELEASE_PUT_URI;
import static org.kie.server.api.rest.RestURI.TASK_INSTANCE_RESUME_PUT_URI;
import static org.kie.server.api.rest.RestURI.TASK_INSTANCE_SKIPABLE_PUT_URI;
import static org.kie.server.api.rest.RestURI.TASK_INSTANCE_SKIP_PUT_URI;
import static org.kie.server.api.rest.RestURI.TASK_INSTANCE_START_PUT_URI;
import static org.kie.server.api.rest.RestURI.TASK_INSTANCE_STOP_PUT_URI;
import static org.kie.server.api.rest.RestURI.TASK_INSTANCE_SUSPEND_PUT_URI;
import static org.kie.server.api.rest.RestURI.TASK_URI;
import static org.kie.server.remote.rest.common.util.RestUtils.buildConversationIdHeader;
import static org.kie.server.remote.rest.common.util.RestUtils.createCorrectVariant;
import static org.kie.server.remote.rest.common.util.RestUtils.createResponse;
import static org.kie.server.remote.rest.common.util.RestUtils.errorMessage;
import static org.kie.server.remote.rest.common.util.RestUtils.forbidden;
import static org.kie.server.remote.rest.common.util.RestUtils.getContentType;
import static org.kie.server.remote.rest.common.util.RestUtils.getVariant;
import static org.kie.server.remote.rest.common.util.RestUtils.internalServerError;
import static org.kie.server.remote.rest.common.util.RestUtils.noContent;
import static org.kie.server.remote.rest.common.util.RestUtils.notFound;
import static org.kie.server.remote.rest.jbpm.docs.ParameterSamples.BOOLEAN_JSON;
import static org.kie.server.remote.rest.jbpm.docs.ParameterSamples.BOOLEAN_XML;
import static org.kie.server.remote.rest.jbpm.docs.ParameterSamples.DATE_JSON;
import static org.kie.server.remote.rest.jbpm.docs.ParameterSamples.DATE_XML;
import static org.kie.server.remote.rest.jbpm.docs.ParameterSamples.GET_PROCESS_INSTANCE_VARS_RESPONSE_JSON;
import static org.kie.server.remote.rest.jbpm.docs.ParameterSamples.GET_TASK_ATTACHMENTS_RESPONSE_JSON;
import static org.kie.server.remote.rest.jbpm.docs.ParameterSamples.GET_TASK_COMMENTS_RESPONSE_JSON;
import static org.kie.server.remote.rest.jbpm.docs.ParameterSamples.GET_TASK_EVENTS_RESPONSE_JSON;
import static org.kie.server.remote.rest.jbpm.docs.ParameterSamples.GET_TASK_RESPONSE_JSON;
import static org.kie.server.remote.rest.jbpm.docs.ParameterSamples.INTEGER_JSON;
import static org.kie.server.remote.rest.jbpm.docs.ParameterSamples.INTEGER_XML;
import static org.kie.server.remote.rest.jbpm.docs.ParameterSamples.JSON;
import static org.kie.server.remote.rest.jbpm.docs.ParameterSamples.STRING_JSON;
import static org.kie.server.remote.rest.jbpm.docs.ParameterSamples.STRING_XML;
import static org.kie.server.remote.rest.jbpm.docs.ParameterSamples.TASK_ATTACHMENT_JSON;
import static org.kie.server.remote.rest.jbpm.docs.ParameterSamples.TASK_ATTACHMENT_XML;
import static org.kie.server.remote.rest.jbpm.docs.ParameterSamples.TASK_COMMENT_JSON;
import static org.kie.server.remote.rest.jbpm.docs.ParameterSamples.TASK_COMMENT_XML;
import static org.kie.server.remote.rest.jbpm.docs.ParameterSamples.TASK_JSON;
import static org.kie.server.remote.rest.jbpm.docs.ParameterSamples.TASK_XML;
import static org.kie.server.remote.rest.jbpm.docs.ParameterSamples.VAR_JSON;
import static org.kie.server.remote.rest.jbpm.docs.ParameterSamples.VAR_MAP_JSON;
import static org.kie.server.remote.rest.jbpm.docs.ParameterSamples.VAR_MAP_XML;
import static org.kie.server.remote.rest.jbpm.docs.ParameterSamples.XML;
import static org.kie.server.remote.rest.jbpm.resources.Messages.TASK_INSTANCE_NOT_FOUND;

@Api(value="Task instances")
@Path("server/" + TASK_URI)
public class UserTaskResource {

    public static final Logger logger = LoggerFactory.getLogger(UserTaskResource.class);

    private UserTaskServiceBase userTaskServiceBase;
    private RuntimeDataServiceBase runtimeDataServiceBase;
    private KieServerRegistry context;

    public UserTaskResource() {

    }

    public UserTaskResource(UserTaskServiceBase userTaskServiceBase, RuntimeDataServiceBase runtimeDataServiceBase, KieServerRegistry context) {
        this.userTaskServiceBase = userTaskServiceBase;
        this.runtimeDataServiceBase = runtimeDataServiceBase;
        this.context = context;
    }


    @ApiOperation(value="Activates a specified task instance to be progressed.",
            response=Void.class, code=201)
    @ApiResponses(value = { @ApiResponse(code = 500, message = "Unexpected error"), @ApiResponse(code = 404, message = "Task with given id not found") })
    @PUT
    @Path(TASK_INSTANCE_ACTIVATE_PUT_URI)
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response activate(@Context HttpHeaders headers, 
            @ApiParam(value = "container id that task instance belongs to", required = true, example = "evaluation_1.0.0-SNAPSHOT") @PathParam(CONTAINER_ID) String containerId,
            @ApiParam(value = "identifier of the task instance that should be activated", required = true, example = "123") @PathParam(TASK_INSTANCE_ID) Long taskId, 
            @ApiParam(value = "optional user id to be used instead of authenticated user - only when bypass authenticated user is enabled", required = false) @QueryParam("user") String userId) {

        Variant v = getVariant(headers);
        Header conversationIdHeader = buildConversationIdHeader(containerId, context, headers);
        try {

            userTaskServiceBase.activate(containerId, taskId, userId);

            return createResponse("", v, Response.Status.CREATED, conversationIdHeader);
        } catch (TaskNotFoundException e){
            return notFound(errorMessage(e, MessageFormat.format(TASK_INSTANCE_NOT_FOUND, taskId)), v, conversationIdHeader);
        } catch (Exception e) {
            logger.error("Unexpected error during processing {}", e.getMessage(), e);
            return internalServerError(errorMessage(e), v, conversationIdHeader);
        }

    }

    @ApiOperation(value="Claims (reserves) a specified task instance for the user sending the request.",
            response=Void.class, code=201)
    @ApiResponses(value = { @ApiResponse(code = 500, message = "Unexpected error"), 
                            @ApiResponse(code = 404, message = "Task with given id not found"), 
                            @ApiResponse(code = 403, message = "User was unable to execute current operation on task with given id due to a no 'current status' match or insufficient permissions")})
    @PUT
    @Path(TASK_INSTANCE_CLAIM_PUT_URI)
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response claim(@Context HttpHeaders headers, 
            @ApiParam(value = "container id that task instance belongs to", required = true, example = "evaluation_1.0.0-SNAPSHOT") @PathParam(CONTAINER_ID) String containerId,
            @ApiParam(value = "identifier of the task instance that should be claimed", required = true, example = "123") @PathParam(TASK_INSTANCE_ID) Long taskId, 
            @ApiParam(value = "optional user id to be used instead of authenticated user - only when bypass authenticated user is enabled", required = false) @QueryParam("user")  String userId) {
        Variant v = getVariant(headers);
        Header conversationIdHeader = buildConversationIdHeader(containerId, context, headers);
        try {

            userTaskServiceBase.claim(containerId, taskId, userId);

            return createResponse("", v, Response.Status.CREATED, conversationIdHeader);
        } catch (TaskNotFoundException e){
            return notFound(errorMessage(e, MessageFormat.format(TASK_INSTANCE_NOT_FOUND, taskId)), v, conversationIdHeader);
        } catch (PermissionDeniedException e){
            return forbidden(errorMessage(e, e.getMessage()), v, conversationIdHeader);
        } catch (Exception e) {
            logger.error("Unexpected error during processing {}", e.getMessage(), e);
            return internalServerError(errorMessage(e), v, conversationIdHeader);
        }
    }

    @ApiOperation(value="Completes a specified task instance.",
            response=Void.class, code=201)
    @ApiResponses(value = { @ApiResponse(code = 500, message = "Unexpected error"), 
                            @ApiResponse(code = 404, message = "Task with given id not found"), 
                            @ApiResponse(code = 403, message = "User was unable to execute current operation on task with given id due to a no 'current status' match or insufficient permissions")})
    @PUT
    @Path(TASK_INSTANCE_COMPLETE_PUT_URI)
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response complete(@Context HttpHeaders headers, 
            @ApiParam(value = "container id that task instance belongs to", required = true, example = "evaluation_1.0.0-SNAPSHOT") @PathParam(CONTAINER_ID) String containerId,
            @ApiParam(value = "identifier of the task instance that should be completed", required = true, example = "123") @PathParam(TASK_INSTANCE_ID) Long taskId, 
            @ApiParam(value = "optional user id to be used instead of authenticated user - only when bypass authenticated user is enabled", required = false) @QueryParam("user") String userId, 
            @ApiParam(value = "optional flag that allows to directlu claim and start task (if needed) before completion", required = false) @QueryParam("auto-progress") boolean autoProgress, 
            @ApiParam(value = "optional map of output variables", required = false, examples=@Example(value= {
                    @ExampleProperty(mediaType=JSON, value=VAR_MAP_JSON),
                    @ExampleProperty(mediaType=XML, value=VAR_MAP_XML)})) String payload) {

        Variant v = getVariant(headers);
        String type = getContentType(headers);
        Header conversationIdHeader = buildConversationIdHeader(containerId, context, headers);
        try {
            if (autoProgress) {
                userTaskServiceBase.completeAutoProgress(containerId, taskId, userId, payload, type);
            } else {
                userTaskServiceBase.complete(containerId, taskId, userId, payload, type);
            }
            return createResponse("", v, Response.Status.CREATED, conversationIdHeader);
        } catch (TaskNotFoundException e){
            return notFound(errorMessage(e, MessageFormat.format(TASK_INSTANCE_NOT_FOUND, taskId)), v, conversationIdHeader);
        } catch (PermissionDeniedException e){
            return forbidden(errorMessage(e, e.getMessage()), v, conversationIdHeader);
        } catch (Exception e) {
            logger.error("Unexpected error during processing {}", e.getMessage(), e);
            return internalServerError(errorMessage(e), v, conversationIdHeader);
        }
    }

    @ApiOperation(value="Delegates a specified task instance to a specified target user as the new task owner.",
            response=Void.class, code=201)
    @ApiResponses(value = { @ApiResponse(code = 500, message = "Unexpected error"), 
                            @ApiResponse(code = 404, message = "Task with given id not found"), 
                            @ApiResponse(code = 403, message = "User was unable to execute current operation on task with given id due to a no 'current status' match or insufficient permissions")})
    @PUT
    @Path(TASK_INSTANCE_DELEGATE_PUT_URI)
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response delegate(@Context HttpHeaders headers, 
            @ApiParam(value = "container id that task instance belongs to", required = true, example = "evaluation_1.0.0-SNAPSHOT") @PathParam(CONTAINER_ID) String containerId,
            @ApiParam(value = "identifier of the task instance that should be delegated", required = true, example = "123") @PathParam(TASK_INSTANCE_ID) Long taskId, 
            @ApiParam(value = "optional user id to be used instead of authenticated user - only when bypass authenticated user is enabled", required = false) @QueryParam("user") String userId, 
            @ApiParam(value = "user that task should be dalegated to", required = true) @QueryParam("targetUser") String targetUserId) {

        Variant v = getVariant(headers);
        Header conversationIdHeader = buildConversationIdHeader(containerId, context, headers);
        try {

            userTaskServiceBase.delegate(containerId, taskId, userId, targetUserId);

            return createResponse("", v, Response.Status.CREATED, conversationIdHeader);
        } catch (TaskNotFoundException e){
            return notFound(errorMessage(e, MessageFormat.format(TASK_INSTANCE_NOT_FOUND, taskId)), v, conversationIdHeader);
        } catch (PermissionDeniedException e){
            return forbidden(errorMessage(e, e.getMessage()), v, conversationIdHeader);
        } catch (Exception e) {
            logger.error("Unexpected error during processing {}", e.getMessage(), e);
            return internalServerError(errorMessage(e), v, conversationIdHeader);
        }

    }

    @ApiOperation(value="Exits a specified task instance.",
            response=Void.class, code=201)
    @ApiResponses(value = { @ApiResponse(code = 500, message = "Unexpected error"), @ApiResponse(code = 404, message = "Task with given id not found") })
    @PUT
    @Path(TASK_INSTANCE_EXIT_PUT_URI)
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response exit(@Context HttpHeaders headers, 
            @ApiParam(value = "container id that task instance belongs to", required = true, example = "evaluation_1.0.0-SNAPSHOT") @PathParam(CONTAINER_ID) String containerId,
            @ApiParam(value = "identifier of the task instance that should be exited", required = true, example = "123") @PathParam(TASK_INSTANCE_ID) Long taskId, 
            @ApiParam(value = "optional user id to be used instead of authenticated user - only when bypass authenticated user is enabled", required = false) @QueryParam("user") String userId) {

        Variant v = getVariant(headers);
        Header conversationIdHeader = buildConversationIdHeader(containerId, context, headers);
        try {

            userTaskServiceBase.exit(containerId, taskId, userId);

            return createResponse("", v, Response.Status.CREATED, conversationIdHeader);
        } catch (TaskNotFoundException e){
            return notFound(errorMessage(e, MessageFormat.format(TASK_INSTANCE_NOT_FOUND, taskId)), v, conversationIdHeader);
        } catch (Exception e) {
            logger.error("Unexpected error during processing {}", e.getMessage(), e);
            return internalServerError(errorMessage(e), v, conversationIdHeader);
        }
    }

    @ApiOperation(value="Fails a specified task instance.",
            response=Void.class, code=201)
    @ApiResponses(value = { @ApiResponse(code = 500, message = "Unexpected error"), @ApiResponse(code = 404, message = "Task with given id not found") })
    @PUT
    @Path(TASK_INSTANCE_FAIL_PUT_URI)
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response fail(@Context HttpHeaders headers, 
            @ApiParam(value = "container id that task instance belongs to", required = true, example = "evaluation_1.0.0-SNAPSHOT") @PathParam(CONTAINER_ID) String containerId,
            @ApiParam(value = "identifier of the task instance that should be failed", required = true, example = "123") @PathParam(TASK_INSTANCE_ID) Long taskId, 
            @ApiParam(value = "optional user id to be used instead of authenticated user - only when bypass authenticated user is enabled", required = false) @QueryParam("user") String userId, 
            @ApiParam(value = "optional map of output variables", required = false, examples=@Example(value= {
                    @ExampleProperty(mediaType=JSON, value=VAR_MAP_JSON),
                    @ExampleProperty(mediaType=XML, value=VAR_MAP_XML)})) String payload) {

        Variant v = getVariant(headers);
        String type = getContentType(headers);
        Header conversationIdHeader = buildConversationIdHeader(containerId, context, headers);
        try {
            userTaskServiceBase.fail(containerId, taskId, userId, payload, type);

            return createResponse("", v, Response.Status.CREATED, conversationIdHeader);
        } catch (TaskNotFoundException e){
            return notFound(errorMessage(e, MessageFormat.format(TASK_INSTANCE_NOT_FOUND, taskId)), v, conversationIdHeader);
        } catch (Exception e) {
            logger.error("Unexpected error during processing {}", e.getMessage(), e);
            return internalServerError(errorMessage(e), v, conversationIdHeader);
        }
    }

    @ApiOperation(value="Forwards a specified task instance to a specified target user for review or for suggested delegation.",
            response=Void.class, code=201)
    @ApiResponses(value = { @ApiResponse(code = 500, message = "Unexpected error"), 
                            @ApiResponse(code = 404, message = "Task with given id not found"), 
                            @ApiResponse(code = 403, message = "User was unable to execute current operation on task with given id due to a no 'current status' match or insufficient permissions")})
    @PUT
    @Path(TASK_INSTANCE_FORWARD_PUT_URI)
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response forward(@Context HttpHeaders headers, 
            @ApiParam(value = "container id that task instance belongs to", required = true, example = "evaluation_1.0.0-SNAPSHOT") @PathParam(CONTAINER_ID) String containerId,
            @ApiParam(value = "identifier of the task instance that should be forwarded", required = true, example = "123") @PathParam(TASK_INSTANCE_ID) Long taskId, 
            @ApiParam(value = "optional user id to be used instead of authenticated user - only when bypass authenticated user is enabled", required = false) @QueryParam("user") String userId, 
            @ApiParam(value = "user that the task should be forwarded to", required = true) @QueryParam("targetUser") String targetUserId) {

        Variant v = getVariant(headers);
        Header conversationIdHeader = buildConversationIdHeader(containerId, context, headers);
        try {

            userTaskServiceBase.forward(containerId, taskId, userId, targetUserId);

            return createResponse("", v, Response.Status.CREATED, conversationIdHeader);
        } catch (TaskNotFoundException e){
            return notFound(errorMessage(e, MessageFormat.format(TASK_INSTANCE_NOT_FOUND, taskId)), v, conversationIdHeader);
        } catch (PermissionDeniedException e){
            return forbidden(errorMessage(e, e.getMessage()), v, conversationIdHeader);
        } catch (Exception e) {
            logger.error("Unexpected error during processing {}", e.getMessage(), e);
            return internalServerError(errorMessage(e), v, conversationIdHeader);
        }
    }

    @ApiOperation(value="Releases a specified task instance from being claimed by the task owner.",
            response=Void.class, code=201)
    @ApiResponses(value = { @ApiResponse(code = 500, message = "Unexpected error"), 
                            @ApiResponse(code = 404, message = "Task with given id not found"), 
                            @ApiResponse(code = 403, message = "User was unable to execute current operation on task with given id due to a no 'current status' match or insufficient permissions")})
    @PUT
    @Path(TASK_INSTANCE_RELEASE_PUT_URI)
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response release(@Context HttpHeaders headers, 
            @ApiParam(value = "container id that task instance belongs to", required = true, example = "evaluation_1.0.0-SNAPSHOT") @PathParam(CONTAINER_ID) String containerId,
            @ApiParam(value = "identifier of the task instance that should be released", required = true, example = "123") @PathParam(TASK_INSTANCE_ID) Long taskId, 
            @ApiParam(value = "optional user id to be used instead of authenticated user - only when bypass authenticated user is enabled", required = false) @QueryParam("user") String userId) {
        Variant v = getVariant(headers);
        Header conversationIdHeader = buildConversationIdHeader(containerId, context, headers);
        try {
            userTaskServiceBase.release(containerId, taskId, userId);

            return createResponse("", v, Response.Status.CREATED, conversationIdHeader);
        } catch (TaskNotFoundException e){
            return notFound(errorMessage(e, MessageFormat.format(TASK_INSTANCE_NOT_FOUND, taskId)), v, conversationIdHeader);
        } catch (PermissionDeniedException e){
            return forbidden(errorMessage(e, e.getMessage()), v, conversationIdHeader);
        } catch (Exception e) {
            logger.error("Unexpected error during processing {}", e.getMessage(), e);
            return internalServerError(errorMessage(e), v, conversationIdHeader);
        }
    }

    @ApiOperation(value="Resumes a specified task instance.",
            response=Void.class, code=201)
    @ApiResponses(value = { @ApiResponse(code = 500, message = "Unexpected error"), @ApiResponse(code = 404, message = "Task with given id not found") })
    @PUT
    @Path(TASK_INSTANCE_RESUME_PUT_URI)
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response resume(@Context HttpHeaders headers, 
            @ApiParam(value = "container id that task instance belongs to", required = true, example = "evaluation_1.0.0-SNAPSHOT") @PathParam(CONTAINER_ID) String containerId,
            @ApiParam(value = "identifier of the task instance that should be resumed", required = true, example = "123") @PathParam(TASK_INSTANCE_ID) Long taskId, 
            @ApiParam(value = "optional user id to be used instead of authenticated user - only when bypass authenticated user is enabled", required = false) @QueryParam("user") String userId) {
        Variant v = getVariant(headers);
        Header conversationIdHeader = buildConversationIdHeader(containerId, context, headers);
        try {

            userTaskServiceBase.resume(containerId, taskId, userId);

            return createResponse("", v, Response.Status.CREATED, conversationIdHeader);
        } catch (TaskNotFoundException e){
            return notFound(errorMessage(e, MessageFormat.format(TASK_INSTANCE_NOT_FOUND, taskId)), v, conversationIdHeader);
        } catch (Exception e) {
            logger.error("Unexpected error during processing {}", e.getMessage(), e);
            return internalServerError(errorMessage(e), v, conversationIdHeader);
        }
    }

    @ApiOperation(value="Skips a specified task instance within the sequence of tasks in the process instance",
            response=Void.class, code=201)
    @ApiResponses(value = { @ApiResponse(code = 500, message = "Unexpected error"), @ApiResponse(code = 404, message = "Task with given id not found") })
    @PUT
    @Path(TASK_INSTANCE_SKIP_PUT_URI)
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response skip(@Context HttpHeaders headers, 
            @ApiParam(value = "container id that task instance belongs to", required = true, example = "evaluation_1.0.0-SNAPSHOT") @PathParam(CONTAINER_ID) String containerId,
            @ApiParam(value = "identifier of the task instance that should be skipped", required = true, example = "123") @PathParam(TASK_INSTANCE_ID) Long taskId, 
            @ApiParam(value = "optional user id to be used instead of authenticated user - only when bypass authenticated user is enabled", required = false) @QueryParam("user") String userId) {
        Variant v = getVariant(headers);
        Header conversationIdHeader = buildConversationIdHeader(containerId, context, headers);
        try {

            userTaskServiceBase.skip(containerId, taskId, userId);

            return createResponse("", v, Response.Status.CREATED, conversationIdHeader);
        } catch (TaskNotFoundException e){
            return notFound(errorMessage(e, MessageFormat.format(TASK_INSTANCE_NOT_FOUND, taskId)), v, conversationIdHeader);
        } catch (Exception e) {
            logger.error("Unexpected error during processing {}", e.getMessage(), e);
            return internalServerError(errorMessage(e), v, conversationIdHeader);
        }
    }
    
    @ApiOperation(value="Starts a specified task instance.",
            response=Void.class, code=201)
    @ApiResponses(value = { @ApiResponse(code = 500, message = "Unexpected error"), @ApiResponse(code = 404, message = "Task with given id not found") })
    @PUT
    @Path(TASK_INSTANCE_START_PUT_URI)
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response start(@Context HttpHeaders headers, 
            @ApiParam(value = "container id that task instance belongs to", required = true, example = "evaluation_1.0.0-SNAPSHOT") @PathParam(CONTAINER_ID) String containerId,
            @ApiParam(value = "identifier of the task instance that should be started", required = true, example = "123") @PathParam(TASK_INSTANCE_ID) Long taskId, 
            @ApiParam(value = "optional user id to be used instead of authenticated user - only when bypass authenticated user is enabled", required = false) @QueryParam("user") String userId) {

        Variant v = getVariant(headers);
        Header conversationIdHeader = buildConversationIdHeader(containerId, context, headers);
        try {
            userTaskServiceBase.start(containerId, taskId, userId);

            return createResponse("", v, Response.Status.CREATED, conversationIdHeader);
        } catch (TaskNotFoundException e){
            return notFound(errorMessage(e, MessageFormat.format(TASK_INSTANCE_NOT_FOUND, taskId)), v, conversationIdHeader);
        } catch (Exception e) {
            logger.error("Unexpected error during processing {}", e.getMessage(), e);
            return internalServerError(errorMessage(e), v, conversationIdHeader);
        }
    }

    @ApiOperation(value="Stops a specified task instance.",
            response=Void.class, code=201)
    @ApiResponses(value = { @ApiResponse(code = 500, message = "Unexpected error"), @ApiResponse(code = 404, message = "Task with given id not found") })
    @PUT
    @Path(TASK_INSTANCE_STOP_PUT_URI)
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response stop(@Context HttpHeaders headers, 
            @ApiParam(value = "container id that task instance belongs to", required = true, example = "evaluation_1.0.0-SNAPSHOT") @PathParam(CONTAINER_ID) String containerId,
            @ApiParam(value = "identifier of the task instance that should be stopped", required = true, example = "123") @PathParam(TASK_INSTANCE_ID) Long taskId, 
            @ApiParam(value = "optional user id to be used instead of authenticated user - only when bypass authenticated user is enabled", required = false) @QueryParam("user") String userId) {

        Variant v = getVariant(headers);
        Header conversationIdHeader = buildConversationIdHeader(containerId, context, headers);
        try {

            userTaskServiceBase.stop(containerId, taskId, userId);

            return createResponse("", v, Response.Status.CREATED, conversationIdHeader);
        } catch (TaskNotFoundException e){
            return notFound(errorMessage(e, MessageFormat.format(TASK_INSTANCE_NOT_FOUND, taskId)), v, conversationIdHeader);
        } catch (Exception e) {
            logger.error("Unexpected error during processing {}", e.getMessage(), e);
            return internalServerError(errorMessage(e), v, conversationIdHeader);
        }
    }

    @ApiOperation(value="Suspends a specified task instance.",
            response=Void.class, code=201)
    @ApiResponses(value = { @ApiResponse(code = 500, message = "Unexpected error"), @ApiResponse(code = 404, message = "Task with given id not found") })
    @PUT
    @Path(TASK_INSTANCE_SUSPEND_PUT_URI)
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response suspend(@Context HttpHeaders headers, 
            @ApiParam(value = "container id that task instance belongs to", required = true, example = "evaluation_1.0.0-SNAPSHOT") @PathParam(CONTAINER_ID) String containerId,
            @ApiParam(value = "identifier of the task instance that should be suspended", required = true, example = "123") @PathParam(TASK_INSTANCE_ID) Long taskId, 
            @ApiParam(value = "optional user id to be used instead of authenticated user - only when bypass authenticated user is enabled", required = false) @QueryParam("user") String userId) {
        Variant v = getVariant(headers);
        Header conversationIdHeader = buildConversationIdHeader(containerId, context, headers);
        try {
            userTaskServiceBase.suspend(containerId, taskId, userId);

            return createResponse("", v, Response.Status.CREATED, conversationIdHeader);
        } catch (TaskNotFoundException e){
            return notFound(errorMessage(e, MessageFormat.format(TASK_INSTANCE_NOT_FOUND, taskId)), v, conversationIdHeader);
        } catch (Exception e) {
            logger.error("Unexpected error during processing {}", e.getMessage(), e);
            return internalServerError(errorMessage(e), v, conversationIdHeader);
        }
    }

    @ApiOperation(value="Nominates one or more potential owners to whom the task instance should be assigned.",
            response=Void.class, code=201)
    @ApiResponses(value = { @ApiResponse(code = 500, message = "Unexpected error"), 
                            @ApiResponse(code = 404, message = "Task with given id not found"), 
                            @ApiResponse(code = 403, message = "User was unable to execute current operation on task with given id due to a no 'current status' match or insufficient permissions")})
    @PUT
    @Path(TASK_INSTANCE_NOMINATE_PUT_URI)
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response nominate(@Context HttpHeaders headers, 
            @ApiParam(value = "container id that task instance belongs to", required = true, example = "evaluation_1.0.0-SNAPSHOT") @PathParam(CONTAINER_ID) String containerId,
            @ApiParam(value = "identifier of the task instance that should be nominated", required = true, example = "123") @PathParam(TASK_INSTANCE_ID) Long taskId, 
            @ApiParam(value = "optional user id to be used instead of authenticated user - only when bypass authenticated user is enabled", required = false) @QueryParam("user") String userId, 
            @ApiParam(value = "list of users that the task should be nominated to", required = true) @QueryParam("potOwner") List<String> potentialOwners) {

        Variant v = getVariant(headers);
        Header conversationIdHeader = buildConversationIdHeader(containerId, context, headers);
        try {

            userTaskServiceBase.nominate(containerId, taskId, userId, potentialOwners);

            return createResponse("", v, Response.Status.CREATED, conversationIdHeader);
        } catch (TaskNotFoundException e){
            return notFound(errorMessage(e, MessageFormat.format(TASK_INSTANCE_NOT_FOUND, taskId)), v, conversationIdHeader);
        } catch (PermissionDeniedException e){
            return forbidden(errorMessage(e, e.getMessage()), v, conversationIdHeader);
        } catch (Exception e) {
            logger.error("Unexpected error during processing {}", e.getMessage(), e);
            return internalServerError(errorMessage(e), v, conversationIdHeader);
        }
    }

    @ApiOperation(value="Updates the priority of a specified task instance.",
            response=Void.class, code=201)
    @ApiResponses(value = { @ApiResponse(code = 500, message = "Unexpected error"), @ApiResponse(code = 404, message = "Task with given id not found") })
    @PUT
    @Path(TASK_INSTANCE_PRIORITY_PUT_URI)
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response setPriority(@Context HttpHeaders headers, 
            @ApiParam(value = "container id that task instance belongs to", required = true, example = "evaluation_1.0.0-SNAPSHOT") @PathParam(CONTAINER_ID) String containerId,
            @ApiParam(value = "identifier of the task instance where priority should be updated", required = true, example = "123") @PathParam(TASK_INSTANCE_ID) Long taskId, 
            @ApiParam(value = "priority as Integer", required = true, examples=@Example(value= {
                    @ExampleProperty(mediaType=JSON, value=INTEGER_JSON),
                    @ExampleProperty(mediaType=XML, value=INTEGER_XML)})) String priorityPayload) {

        Variant v = getVariant(headers);
        String type = getContentType(headers);
        Header conversationIdHeader = buildConversationIdHeader(containerId, context, headers);
        try {

            userTaskServiceBase.setPriority(containerId, taskId, priorityPayload, type);

            return createResponse("", v, Response.Status.CREATED, conversationIdHeader);
        } catch (TaskNotFoundException e){
            return notFound(errorMessage(e, MessageFormat.format(TASK_INSTANCE_NOT_FOUND, taskId)), v, conversationIdHeader);
        } catch (Exception e) {
            logger.error("Unexpected error during processing {}", e.getMessage(), e);
            return internalServerError(errorMessage(e), v, conversationIdHeader);
        }
    }

    @ApiOperation(value="Updates the expiration date for a specified task instance.",
            response=Void.class, code=201)
    @ApiResponses(value = { @ApiResponse(code = 500, message = "Unexpected error"), @ApiResponse(code = 404, message = "Task with given id not found") })
    @PUT
    @Path(TASK_INSTANCE_EXPIRATION_DATE_PUT_URI)
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response setExpirationDate(@Context HttpHeaders headers, 
            @ApiParam(value = "container id that task instance belongs to", required = true, example = "evaluation_1.0.0-SNAPSHOT") @PathParam(CONTAINER_ID) String containerId,
            @ApiParam(value = "identifier of the task instance where expiration date should be updated", required = true, example = "123") @PathParam(TASK_INSTANCE_ID) Long taskId, 
            @ApiParam(value = "expiration date as Date", required = true, examples=@Example(value= {
                    @ExampleProperty(mediaType=JSON, value=DATE_JSON),
                    @ExampleProperty(mediaType=XML, value=DATE_XML)})) String datePayload) {

        Variant v = getVariant(headers);
        String type = getContentType(headers);
        Header conversationIdHeader = buildConversationIdHeader(containerId, context, headers);
        try {

            userTaskServiceBase.setExpirationDate(containerId, taskId, datePayload, type);

            return createResponse("", v, Response.Status.CREATED, conversationIdHeader);
        } catch (TaskNotFoundException e){
            return notFound(errorMessage(e, MessageFormat.format(TASK_INSTANCE_NOT_FOUND, taskId)), v, conversationIdHeader);
        } catch (Exception e) {
            logger.error("Unexpected error during processing {}", e.getMessage(), e);
            return internalServerError(errorMessage(e), v, conversationIdHeader);
        }
    }

    @ApiOperation(value="Marks a specified task instance that can be skipped in a sequence of tasks.",
            response=Void.class, code=201)
    @ApiResponses(value = { @ApiResponse(code = 500, message = "Unexpected error"), @ApiResponse(code = 404, message = "Task with given id not found") })
    @PUT
    @Path(TASK_INSTANCE_SKIPABLE_PUT_URI)
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response setSkipable(@Context HttpHeaders headers, 
            @ApiParam(value = "container id that task instance belongs to", required = true, example = "evaluation_1.0.0-SNAPSHOT") @PathParam(CONTAINER_ID) String containerId,
            @ApiParam(value = "identifier of the task instance where skipable flag should be updated", required = true, example = "123") @PathParam(TASK_INSTANCE_ID) Long taskId, 
            @ApiParam(value = "skipable flag as Boolean", required = true, examples=@Example(value= {
                    @ExampleProperty(mediaType=JSON, value=BOOLEAN_JSON),
                    @ExampleProperty(mediaType=XML, value=BOOLEAN_XML)})) String skipablePayload) {

        Variant v = getVariant(headers);
        String type = getContentType(headers);
        Header conversationIdHeader = buildConversationIdHeader(containerId, context, headers);
        try {

            userTaskServiceBase.setSkipable(containerId, taskId, skipablePayload, type);

            return createResponse("", v, Response.Status.CREATED, conversationIdHeader);
        } catch (TaskNotFoundException e){
            return notFound(errorMessage(e, MessageFormat.format(TASK_INSTANCE_NOT_FOUND, taskId)), v, conversationIdHeader);
        } catch (Exception e) {
            logger.error("Unexpected error during processing {}", e.getMessage(), e);
            return internalServerError(errorMessage(e), v, conversationIdHeader);
        }
    }

    @ApiOperation(value="Updates the name of a specified task instance.",
            response=Void.class, code=201)
    @ApiResponses(value = { @ApiResponse(code = 500, message = "Unexpected error"), @ApiResponse(code = 404, message = "Task with given id not found") })
    @PUT
    @Path(TASK_INSTANCE_NAME_PUT_URI)
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response setName(@Context HttpHeaders headers, 
            @ApiParam(value = "container id that task instance belongs to", required = true, example = "evaluation_1.0.0-SNAPSHOT") @PathParam(CONTAINER_ID) String containerId,
            @ApiParam(value = "identifier of the task instance where name should be updated", required = true, example = "123") @PathParam(TASK_INSTANCE_ID) Long taskId, 
            @ApiParam(value = "name as String", required = true, examples=@Example(value= {
                    @ExampleProperty(mediaType=JSON, value=STRING_JSON),
                    @ExampleProperty(mediaType=XML, value=STRING_XML)})) String namePayload) {
        Variant v = getVariant(headers);
        String type = getContentType(headers);
        Header conversationIdHeader = buildConversationIdHeader(containerId, context, headers);
        try {
            userTaskServiceBase.setName(containerId, taskId, namePayload, type);

            return createResponse("", v, Response.Status.CREATED, conversationIdHeader);
        } catch (TaskNotFoundException e){
            return notFound(errorMessage(e, MessageFormat.format(TASK_INSTANCE_NOT_FOUND, taskId)), v, conversationIdHeader);
        } catch (Exception e) {
            logger.error("Unexpected error during processing {}", e.getMessage(), e);
            return internalServerError(errorMessage(e), v, conversationIdHeader);
        }
    }

    @ApiOperation(value="Updates the description of a specified task instance.",
            response=Void.class, code=201)
    @ApiResponses(value = { @ApiResponse(code = 500, message = "Unexpected error"), @ApiResponse(code = 404, message = "Task with given id not found") })
    @PUT
    @Path(TASK_INSTANCE_DESCRIPTION_PUT_URI)
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response setDescription(@Context HttpHeaders headers, 
            @ApiParam(value = "container id that task instance belongs to", required = true, example = "evaluation_1.0.0-SNAPSHOT") @PathParam(CONTAINER_ID) String containerId,
            @ApiParam(value = "identifier of the task instance where description should be updated", required = true, example = "123") @PathParam(TASK_INSTANCE_ID) Long taskId, 
            @ApiParam(value = "description as String", required = true, examples=@Example(value= {
                    @ExampleProperty(mediaType=JSON, value=STRING_JSON),
                    @ExampleProperty(mediaType=XML, value=STRING_XML)})) String descriptionPayload) {
        Variant v = getVariant(headers);
        String type = getContentType(headers);
        Header conversationIdHeader = buildConversationIdHeader(containerId, context, headers);
        try {

            userTaskServiceBase.setDescription(containerId, taskId, descriptionPayload, type);

            return createResponse("", v, Response.Status.CREATED, conversationIdHeader);
        } catch (TaskNotFoundException e){
            return notFound(errorMessage(e, MessageFormat.format(TASK_INSTANCE_NOT_FOUND, taskId)), v, conversationIdHeader);
        } catch (Exception e) {
            logger.error("Unexpected error during processing {}", e.getMessage(), e);
            return internalServerError(errorMessage(e), v, conversationIdHeader);
        }
    }

    @ApiOperation(value = "Adds output data to a specified task instance and returns the ID of the new output content.", response = Void.class, code = 201)
    @ApiResponses(value = {@ApiResponse(code = 500, message = "Unexpected error"),
                           @ApiResponse(code = 403, message = "User is not allowed to perform the operation"),
                           @ApiResponse(code = 404, message = "Task with given id not found")})
    @PUT
    @Path(TASK_INSTANCE_OUTPUT_DATA_PUT_URI)
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response saveContent(@Context HttpHeaders headers, 
            @ApiParam(value = "container id that task instance belongs to", required = true, example = "evaluation_1.0.0-SNAPSHOT") @PathParam(CONTAINER_ID) String containerId,
            @ApiParam(value = "identifier of the task instance that data should be saved into", required = true, example = "123") @PathParam(TASK_INSTANCE_ID) Long taskId, 
                                @ApiParam(value = "optional user id to be used instead of authenticated user - only when bypass authenticated user is enabled", required = false) @QueryParam("user") String userId,
            @ApiParam(value = "output data to be saved as Map ", required = true, examples=@Example(value= {
                    @ExampleProperty(mediaType=JSON, value=VAR_MAP_JSON),
                    @ExampleProperty(mediaType=XML, value=VAR_MAP_XML)})) String payload) {
        Variant v = getVariant(headers);
        String type = getContentType(headers);
        Header conversationIdHeader = buildConversationIdHeader(containerId, context, headers);
        try {

            String response = userTaskServiceBase.saveContent(containerId, userId, taskId, payload, type);

            logger.debug("Returning CREATED response with content '{}'", response);
            return createResponse(response, v, Response.Status.CREATED, conversationIdHeader);
        } catch (TaskNotFoundException e){
            return notFound(errorMessage(e, MessageFormat.format(TASK_INSTANCE_NOT_FOUND, taskId)), v, conversationIdHeader);
        } catch (PermissionDeniedException e) {
            return forbidden(errorMessage(e, e.getMessage()), v, conversationIdHeader);
        } catch (Exception e) {
            logger.error("Unexpected error during processing {}", e.getMessage(), e);
            return internalServerError(errorMessage(e), v, conversationIdHeader);
        }
    }

    @ApiOperation(value="Returns output data for a specified task instance.",
            response=Map.class, code=200)
    @ApiResponses(value = { @ApiResponse(code = 500, message = "Unexpected error"), @ApiResponse(code = 404, message = "Task with given id not found"), 
            @ApiResponse(code = 200, message = "Successfull response", examples=@Example(value= {
                    @ExampleProperty(mediaType=JSON, value=GET_PROCESS_INSTANCE_VARS_RESPONSE_JSON)})) })
    @GET
    @Path(TASK_INSTANCE_OUTPUT_DATA_GET_URI)
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getTaskOutputContentByTaskId(@Context HttpHeaders headers, 
            @ApiParam(value = "container id that task instance belongs to", required = true, example = "evaluation_1.0.0-SNAPSHOT") @PathParam(CONTAINER_ID) String containerId,
            @ApiParam(value = "identifier of the task instance that output data should be loaded from", required = true, example = "123") @PathParam(TASK_INSTANCE_ID) Long taskId) {
        Variant v = getVariant(headers);
        String type = getContentType(headers);
        Header conversationIdHeader = buildConversationIdHeader(containerId, context, headers);
        try {
            String response = userTaskServiceBase.getTaskOutputContentByTaskId(containerId, taskId, type);

            logger.debug("Returning OK response with content '{}'", response);
            return createResponse(response, v, Response.Status.OK, conversationIdHeader);

        } catch (TaskNotFoundException e){
            return notFound(errorMessage(e, MessageFormat.format(TASK_INSTANCE_NOT_FOUND, taskId)), v, conversationIdHeader);
        } catch (Exception e) {
            logger.error("Unexpected error during processing {}", e.getMessage(), e);
            return internalServerError(errorMessage(e), v, conversationIdHeader);
        }
    }

    @ApiOperation(value="Returns input data for a specified task instance.",
            response=Map.class, code=200)
    @ApiResponses(value = { @ApiResponse(code = 500, message = "Unexpected error"), @ApiResponse(code = 404, message = "Task with given id not found"), 
            @ApiResponse(code = 200, message = "Successfull response", examples=@Example(value= {
                    @ExampleProperty(mediaType=JSON, value=GET_PROCESS_INSTANCE_VARS_RESPONSE_JSON)})) })
    @GET
    @Path(TASK_INSTANCE_INPUT_DATA_GET_URI)
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getTaskInputContentByTaskId(@Context HttpHeaders headers, 
            @ApiParam(value = "container id that task instance belongs to", required = true, example = "evaluation_1.0.0-SNAPSHOT") @PathParam(CONTAINER_ID) String containerId,
            @ApiParam(value = "identifier of the task instance that input data should be loaded from", required = true, example = "123") @PathParam(TASK_INSTANCE_ID) Long taskId) {
        Variant v = getVariant(headers);
        String type = getContentType(headers);
        Header conversationIdHeader = buildConversationIdHeader(containerId, context, headers);
        try {
            String response = userTaskServiceBase.getTaskInputContentByTaskId(containerId, taskId, type);

            logger.debug("Returning OK response with content '{}'", response);
            return createResponse(response, v, Response.Status.OK, conversationIdHeader);

        } catch (TaskNotFoundException e){
            return notFound(errorMessage(e, MessageFormat.format(TASK_INSTANCE_NOT_FOUND, taskId)), v, conversationIdHeader);
        } catch (Exception e) {
            logger.error("Unexpected error during processing {}", e.getMessage(), e);
            return internalServerError(errorMessage(e), v, conversationIdHeader);
        }
    }

    @ApiOperation(value="Deletes output data by content ID from a specified task instance.",
            response=Void.class, code=204)
    @ApiResponses(value = { @ApiResponse(code = 500, message = "Unexpected error"), @ApiResponse(code = 404, message = "Task with given id not found") })
    @DELETE
    @Path(TASK_INSTANCE_CONTENT_DATA_DELETE_URI)
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response deleteContent(@Context HttpHeaders headers, 
            @ApiParam(value = "container id that task instance belongs to", required = true, example = "evaluation_1.0.0-SNAPSHOT") @PathParam(CONTAINER_ID) String containerId,
            @ApiParam(value = "identifier of the task instance that content belongs to", required = true, example = "123") @PathParam(TASK_INSTANCE_ID) Long taskId, 
            @ApiParam(value = "identifier of the content to be deleted", required = true, example = "567") @PathParam("contentId") Long contentId) {

        Variant v = getVariant(headers);
        Header conversationIdHeader = buildConversationIdHeader(containerId, context, headers);
        try {

            userTaskServiceBase.deleteContent(containerId, taskId, contentId);
            // produce 204 NO_CONTENT response code
            return noContent(v, conversationIdHeader);
        } catch (TaskNotFoundException e){
            return notFound(errorMessage(e, MessageFormat.format(TASK_INSTANCE_NOT_FOUND, taskId)), v, conversationIdHeader);
        } catch (Exception e) {
            logger.error("Unexpected error during processing {}", e.getMessage(), e);
            return internalServerError(errorMessage(e), v, conversationIdHeader);
        }
    }

    @ApiOperation(value="Adds a comment to a specified task instance and returns the ID of the new comment.",
            response=Long.class, code=201)
    @ApiResponses(value = { @ApiResponse(code = 500, message = "Unexpected error"), @ApiResponse(code = 404, message = "Task with given id not found"), 
            @ApiResponse(code = 200, message = "Successfull response", examples=@Example(value= {
                    @ExampleProperty(mediaType=JSON, value=INTEGER_JSON)})) })
    @POST
    @Path(TASK_INSTANCE_COMMENT_ADD_POST_URI)
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response addComment(@Context HttpHeaders headers, 
            @ApiParam(value = "container id that task instance belongs to", required = true, example = "evaluation_1.0.0-SNAPSHOT") @PathParam(CONTAINER_ID) String containerId,
            @ApiParam(value = "identifier of the task instance that comment should be added to", required = true, example = "123") @PathParam(TASK_INSTANCE_ID) Long taskId, 
            @ApiParam(value = "comment data as TaskComment", required = true, examples=@Example(value= {
                    @ExampleProperty(mediaType=JSON, value=TASK_COMMENT_JSON),
                    @ExampleProperty(mediaType=XML, value=TASK_COMMENT_XML)})) String payload) {
        Variant v = getVariant(headers);
        String type = getContentType(headers);
        Header conversationIdHeader = buildConversationIdHeader(containerId, context, headers);
        try {
            String response = userTaskServiceBase.addComment(containerId, taskId, payload, type);

            logger.debug("Returning CREATED response with content '{}'", response);
            return createResponse(response, v, Response.Status.CREATED, conversationIdHeader);
        } catch (TaskNotFoundException e){
            return notFound(errorMessage(e, MessageFormat.format(TASK_INSTANCE_NOT_FOUND, taskId)), v, conversationIdHeader);
        } catch (Exception e) {
            logger.error("Unexpected error during processing {}", e.getMessage(), e);
            return internalServerError(errorMessage(e), v, conversationIdHeader);
        }
    }

    @ApiOperation(value="Deletes a specified comment from a specified task instance.",
            response=Void.class, code=204)
    @ApiResponses(value = { @ApiResponse(code = 500, message = "Unexpected error"), @ApiResponse(code = 404, message = "Task with given id not found") })
    @DELETE
    @Path(TASK_INSTANCE_COMMENT_DELETE_URI)
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response deleteComment(@Context HttpHeaders headers, 
            @ApiParam(value = "container id that task instance belongs to", required = true, example = "evaluation_1.0.0-SNAPSHOT") @PathParam(CONTAINER_ID) String containerId,
            @ApiParam(value = "identifier of the task instance that comment belongs to", required = true, example = "123") @PathParam(TASK_INSTANCE_ID) Long taskId, 
            @ApiParam(value = "identifier of the comment to be deleted", required = true, example = "567") @PathParam("commentId") Long commentId) {

        Variant v = getVariant(headers);
        Header conversationIdHeader = buildConversationIdHeader(containerId, context, headers);
        try {

            userTaskServiceBase.deleteComment(containerId, taskId, commentId);
            // produce 204 NO_CONTENT response code
            return noContent(v, conversationIdHeader);
        } catch (TaskNotFoundException e){
            return notFound(errorMessage(e, MessageFormat.format(TASK_INSTANCE_NOT_FOUND, taskId)), v, conversationIdHeader);
        } catch (Exception e) {
            logger.error("Unexpected error during processing {}", e.getMessage(), e);
            return internalServerError(errorMessage(e), v, conversationIdHeader);
        }
    }

    @ApiOperation(value="Returns all comments in a specified task instance.",
            response=TaskCommentList.class, code=200)
    @ApiResponses(value = { @ApiResponse(code = 500, message = "Unexpected error"), @ApiResponse(code = 404, message = "Task with given id not found"), 
            @ApiResponse(code = 200, message = "Successfull response", examples=@Example(value= {
                    @ExampleProperty(mediaType=JSON, value=GET_TASK_COMMENTS_RESPONSE_JSON)})) })
    @GET
    @Path(TASK_INSTANCE_COMMENTS_GET_URI)
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getCommentsByTaskId(@Context HttpHeaders headers, 
            @ApiParam(value = "container id that task instance belongs to", required = true, example = "evaluation_1.0.0-SNAPSHOT") @PathParam(CONTAINER_ID) String containerId,
            @ApiParam(value = "identifier of the task instance that comments should be loaded for", required = true, example = "123") @PathParam(TASK_INSTANCE_ID) Long taskId) {
        Variant v = getVariant(headers);
        String type = getContentType(headers);
        Header conversationIdHeader = buildConversationIdHeader(containerId, context, headers);
        try {
            String response = userTaskServiceBase.getCommentsByTaskId(containerId, taskId, type);

            logger.debug("Returning OK response with content '{}'", response);
            return createResponse(response, v, Response.Status.OK, conversationIdHeader);

        } catch (TaskNotFoundException e){
            return notFound(errorMessage(e, MessageFormat.format(TASK_INSTANCE_NOT_FOUND, taskId)), v, conversationIdHeader);
        } catch (Exception e) {
            logger.error("Unexpected error during processing {}", e.getMessage(), e);
            return internalServerError(errorMessage(e), v, conversationIdHeader);
        }
    }

    @ApiOperation(value="Returns a specified comment from a specified task instance.",
            response=TaskComment.class, code=200)
    @ApiResponses(value = { @ApiResponse(code = 500, message = "Unexpected error"), @ApiResponse(code = 404, message = "Task with given id not found"), 
            @ApiResponse(code = 200, message = "Successfull response", examples=@Example(value= {
                    @ExampleProperty(mediaType=JSON, value=TASK_COMMENT_JSON)})) })
    @GET
    @Path(TASK_INSTANCE_COMMENT_GET_URI)
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getCommentById(@Context HttpHeaders headers, 
            @ApiParam(value = "container id that task instance belongs to", required = true, example = "evaluation_1.0.0-SNAPSHOT") @PathParam(CONTAINER_ID) String containerId,
            @ApiParam(value = "identifier of the task instance that comment belongs to", required = true, example = "123") @PathParam(TASK_INSTANCE_ID) Long taskId, 
            @ApiParam(value = "identifier of the comment to be loaded", required = true, example = "567") @PathParam("commentId") Long commentId) {
        Variant v = getVariant(headers);
        String type = getContentType(headers);
        Header conversationIdHeader = buildConversationIdHeader(containerId, context, headers);
        try {
            String response = userTaskServiceBase.getCommentById(containerId, taskId, commentId, type);

            logger.debug("Returning OK response with content '{}'", response);
            return createResponse(response, v, Response.Status.OK, conversationIdHeader);

        } catch (TaskNotFoundException e){
            return notFound(errorMessage(e, MessageFormat.format(TASK_INSTANCE_NOT_FOUND, taskId)), v, conversationIdHeader);
        } catch (Exception e) {
            logger.error("Unexpected error during processing {}", e.getMessage(), e);
            return internalServerError(errorMessage(e), v, conversationIdHeader);
        }
    }

    
    @ApiOperation(value="Adds an attachment to a specified task instance and returns the ID of the new attachment.",
            response=Long.class, code=201)
    @ApiResponses(value = { @ApiResponse(code = 500, message = "Unexpected error"), @ApiResponse(code = 404, message = "Task with given id not found"), 
            @ApiResponse(code = 201, message = "Successfull response", examples=@Example(value= {
                    @ExampleProperty(mediaType=JSON, value=INTEGER_JSON)})) })
    @POST
    @Path(TASK_INSTANCE_ATTACHMENT_ADD_POST_URI)
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response addAttachment(@Context HttpHeaders headers, 
            @ApiParam(value = "container id that task instance belongs to", required = true, example = "evaluation_1.0.0-SNAPSHOT") @PathParam(CONTAINER_ID) String containerId,
            @ApiParam(value = "identifier of the task instance that attachment should be added to", required = true, example = "123") @PathParam(TASK_INSTANCE_ID) Long taskId, 
            @ApiParam(value = "optional user id to be used instead of authenticated user - only when bypass authenticated user is enabled", required = false) @QueryParam("user") String userId, 
            @ApiParam(value = "name of the attachment to be added", required = true) @QueryParam("name") String name, 
            @ApiParam(value = "attachment content, any type can be provided", required = true, examples=@Example(value= {
                    @ExampleProperty(mediaType=JSON, value=TASK_ATTACHMENT_JSON),
                    @ExampleProperty(mediaType=XML, value=TASK_ATTACHMENT_XML)})) String attachmentPayload) {
        Variant v = getVariant(headers);
        String type = getContentType(headers);
        Header conversationIdHeader = buildConversationIdHeader(containerId, context, headers);
        try {

            String response = userTaskServiceBase.addAttachment(containerId, taskId, userId, name, attachmentPayload, type);

            logger.debug("Returning CREATED response with content '{}'", response);
            return createResponse(response, v, Response.Status.CREATED, conversationIdHeader);
        } catch (TaskNotFoundException e){
            return notFound(errorMessage(e, MessageFormat.format(TASK_INSTANCE_NOT_FOUND, taskId)), v, conversationIdHeader);
        } catch (Exception e) {
            logger.error("Unexpected error during processing {}", e.getMessage(), e);
            return internalServerError(errorMessage(e), v, conversationIdHeader);
        }
    }

    @ApiOperation(value="Deletes a specified attachment from a specified task instance.",
            response=Void.class, code=204)
    @ApiResponses(value = { @ApiResponse(code = 500, message = "Unexpected error"), @ApiResponse(code = 404, message = "Task with given id not found") })
    @DELETE
    @Path(TASK_INSTANCE_ATTACHMENT_DELETE_URI)
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response deleteAttachment(@Context HttpHeaders headers,
            @ApiParam(value = "container id that task instance belongs to", required = true, example = "evaluation_1.0.0-SNAPSHOT") @PathParam(CONTAINER_ID) String containerId,
            @ApiParam(value = "identifier of the task instance that attachment belongs to", required = true, example = "123") @PathParam(TASK_INSTANCE_ID) Long taskId, 
            @ApiParam(value = "identifier of the attachment to be deleted", required = true, example = "567") @PathParam("attachmentId") Long attachmentId) {

        Variant v = getVariant(headers);
        Header conversationIdHeader = buildConversationIdHeader(containerId, context, headers);
        try {

            userTaskServiceBase.deleteAttachment(containerId, taskId, attachmentId);
            // produce 204 NO_CONTENT response code
            return noContent(v, conversationIdHeader);
        } catch (TaskNotFoundException e){
            return notFound(errorMessage(e, MessageFormat.format(TASK_INSTANCE_NOT_FOUND, taskId)), v, conversationIdHeader);
        } catch (Exception e) {
            logger.error("Unexpected error during processing {}", e.getMessage(), e);
            return internalServerError(errorMessage(e), v, conversationIdHeader);
        }
    }

    @ApiOperation(value="Returns information about a specified attachment for a specified task instance.",
            response=TaskAttachment.class, code=200)
    @ApiResponses(value = { @ApiResponse(code = 500, message = "Unexpected error"), @ApiResponse(code = 404, message = "Task with given id not found"), 
            @ApiResponse(code = 200, message = "Successfull response", examples=@Example(value= {
                    @ExampleProperty(mediaType=JSON, value=TASK_ATTACHMENT_JSON)})) })
    @GET
    @Path(TASK_INSTANCE_ATTACHMENT_GET_URI)
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getAttachmentById(@Context HttpHeaders headers, 
            @ApiParam(value = "container id that task instance belongs to", required = true, example = "evaluation_1.0.0-SNAPSHOT") @PathParam(CONTAINER_ID) String containerId,
            @ApiParam(value = "identifier of the task instance that attachment belongs to", required = true, example = "123") @PathParam(TASK_INSTANCE_ID) Long taskId, 
            @ApiParam(value = "identifier of the attachment to be loaded", required = true, example = "567") @PathParam("attachmentId") Long attachmentId) {
        Variant v = getVariant(headers);
        String type = getContentType(headers);
        Header conversationIdHeader = buildConversationIdHeader(containerId, context, headers);
        try {
            String response = userTaskServiceBase.getAttachmentById(containerId, taskId, attachmentId, type);

            logger.debug("Returning OK response with content '{}'", response);
            return createResponse(response, v, Response.Status.OK, conversationIdHeader);

        } catch (TaskNotFoundException e){
            return notFound(errorMessage(e, MessageFormat.format(TASK_INSTANCE_NOT_FOUND, taskId)), v, conversationIdHeader);
        } catch (Exception e) {
            logger.error("Unexpected error during processing {}", e.getMessage(), e);
            return internalServerError(errorMessage(e), v, conversationIdHeader);
        }
    }

    @ApiOperation(value="Returns the content of a specified attachment for a specified task instance.",
            response=Object.class, code=200)
    @ApiResponses(value = { @ApiResponse(code = 500, message = "Unexpected error"), @ApiResponse(code = 404, message = "Task with given id not found"), 
            @ApiResponse(code = 200, message = "Successfull response", examples=@Example(value= {
                    @ExampleProperty(mediaType=JSON, value=VAR_JSON)})) })
    @GET
    @Path(TASK_INSTANCE_ATTACHMENT_CONTENT_GET_URI)
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getAttachmentContentById(@Context HttpHeaders headers, 
            @ApiParam(value = "container id that task instance belongs to", required = true, example = "evaluation_1.0.0-SNAPSHOT") @PathParam(CONTAINER_ID) String containerId,
            @ApiParam(value = "identifier of the task instance that attachment belongs to", required = true, example = "123") @PathParam(TASK_INSTANCE_ID) Long taskId, 
            @ApiParam(value = "identifier of the attachment that content should be loaded from", required = true, example = "567") @PathParam("attachmentId") Long attachmentId) {
        Variant v = getVariant(headers);
        String type = getContentType(headers);
        Header conversationIdHeader = buildConversationIdHeader(containerId, context, headers);
        try {
            String response = userTaskServiceBase.getAttachmentContentById(containerId, taskId, attachmentId, type);

            logger.debug("Returning OK response with content '{}'", response);
            return createResponse(response, v, Response.Status.OK, conversationIdHeader);

        } catch (TaskNotFoundException e){
            return notFound(errorMessage(e, MessageFormat.format(TASK_INSTANCE_NOT_FOUND, taskId)), v, conversationIdHeader);
        } catch (Exception e) {
            logger.error("Unexpected error during processing {}", e.getMessage(), e);
            return internalServerError(errorMessage(e), v, conversationIdHeader);
        }
    }
    
    @ApiOperation(value="Returns all attachments for a specified task instance.",
            response=TaskAttachmentList.class, code=200)
    @ApiResponses(value = { @ApiResponse(code = 500, message = "Unexpected error"), @ApiResponse(code = 404, message = "Task with given id not found"), 
            @ApiResponse(code = 200, message = "Successfull response", examples=@Example(value= {
                    @ExampleProperty(mediaType=JSON, value=GET_TASK_ATTACHMENTS_RESPONSE_JSON)})) })
    @GET
    @Path(TASK_INSTANCE_ATTACHMENTS_GET_URI)
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getAttachmentsByTaskId(@Context HttpHeaders headers, 
            @ApiParam(value = "container id that task instance belongs to", required = true, example = "evaluation_1.0.0-SNAPSHOT") @PathParam(CONTAINER_ID) String containerId,
            @ApiParam(value = "identifier of the task instance that attachments should be loaded for", required = true, example = "123") @PathParam(TASK_INSTANCE_ID) Long taskId) {
        Variant v = getVariant(headers);
        String type = getContentType(headers);
        Header conversationIdHeader = buildConversationIdHeader(containerId, context, headers);
        try {

            String response = userTaskServiceBase.getAttachmentsByTaskId(containerId, taskId, type);

            logger.debug("Returning OK response with content '{}'", response);
            return createResponse(response, v, Response.Status.OK, conversationIdHeader);

        } catch (TaskNotFoundException e){
            return notFound(errorMessage(e, MessageFormat.format(TASK_INSTANCE_NOT_FOUND, taskId)), v, conversationIdHeader);
        } catch (Exception e) {
            logger.error("Unexpected error during processing {}", e.getMessage(), e);
            return internalServerError(errorMessage(e), v, conversationIdHeader);
        }
    }

    @ApiOperation(value="Returns information about a specified task instance.",
            response=TaskInstance.class, code=200)
    @ApiResponses(value = { @ApiResponse(code = 500, message = "Unexpected error"), @ApiResponse(code = 404, message = "Task with given id not found"), 
            @ApiResponse(code = 200, message = "Successfull response", examples=@Example(value= {
                    @ExampleProperty(mediaType=JSON, value=GET_TASK_RESPONSE_JSON)})) })
    @GET
    @Path(TASK_INSTANCE_GET_URI)
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response  getTask(@Context HttpHeaders headers, 
            @ApiParam(value = "container id that task instance belongs to", required = true, example = "evaluation_1.0.0-SNAPSHOT") @PathParam(CONTAINER_ID) String containerId, 
            @ApiParam(value = "identifier of the task instance that should be loaded", required = true, example = "123") @PathParam(TASK_INSTANCE_ID) Long taskId,
            @ApiParam(value = "optionally loads task input data", required = false) @QueryParam("withInputData") boolean withInput, 
            @ApiParam(value = "optionally loads task output data", required = false) @QueryParam("withOutputData") boolean withOutput, 
            @ApiParam(value = "optionally loads task people assignments", required = false) @QueryParam("withAssignments") boolean withAssignments) {
        Variant v = getVariant(headers);
        String type = getContentType(headers);
        Header conversationIdHeader = buildConversationIdHeader(containerId, context, headers);
        try {

            String response = userTaskServiceBase.getTask(containerId, taskId, withInput, withOutput, withAssignments, type);

            logger.debug("Returning OK response with content '{}'", response);
            return createResponse(response, v, Response.Status.OK, conversationIdHeader);

        } catch (TaskNotFoundException e) {
            return notFound(errorMessage(e, MessageFormat.format(TASK_INSTANCE_NOT_FOUND, taskId)), v, conversationIdHeader);
        } catch (Exception e) {
            logger.error("Unexpected error during processing {}", e.getMessage(), e);
            return internalServerError(errorMessage(e), v, conversationIdHeader);
        }
    }

    @ApiOperation(value="Returns all events for a specified task instance.",
            response=TaskEventInstanceList.class, code=200)
    @ApiResponses(value = { @ApiResponse(code = 500, message = "Unexpected error"),
                            @ApiResponse(code = 404, message = "Task with given id not found"), 
                            @ApiResponse(code = 200, message = "Successfull response", examples=@Example(value= {
                                    @ExampleProperty(mediaType=JSON, value=GET_TASK_EVENTS_RESPONSE_JSON)}))})
    @GET
    @Path(TASK_INSTANCE_EVENTS_GET_URI)
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getTaskEvents(@Context HttpHeaders headers, 
            @ApiParam(value = "container id that task instance belongs to", required = true, example = "evaluation_1.0.0-SNAPSHOT") @PathParam(CONTAINER_ID) String containerId, 
            @ApiParam(value = "identifier of the task instance that events should be loaded for", required = true, example = "123") @PathParam(TASK_INSTANCE_ID) Long taskId,
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
            return notFound(errorMessage(e, MessageFormat.format(TASK_INSTANCE_NOT_FOUND, taskId)), v, conversationIdHeader);
        } catch (Exception e) {
            logger.error("Unexpected error during processing {}", e.getMessage(), e);
            return internalServerError(errorMessage(e), v, conversationIdHeader);
        }
    }
    
    @ApiOperation(value="Updates information in a specified task instance.",
            response=Void.class, code=201)
    @ApiResponses(value = { @ApiResponse(code = 500, message = "Unexpected error"), @ApiResponse(code = 404, message = "Task with given id not found") })
    @PUT
    @Path(TASK_INSTANCE_PUT_URI)
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response update(@Context HttpHeaders headers, 
            @ApiParam(value = "container id that task instance belongs to", required = true, example = "evaluation_1.0.0-SNAPSHOT") @PathParam(CONTAINER_ID) String containerId,
            @ApiParam(value = "identifier of the task instance that should be updated", required = true, example = "123") @PathParam(TASK_INSTANCE_ID) Long taskId, 
            @ApiParam(value = "optional user id to be used instead of authenticated user - only when bypass authenticated user is enabled", required = false) @QueryParam("user") String userId,             
            @ApiParam(value = "task instance with updates as TaskInstance type", required = true, examples=@Example(value= {
                    @ExampleProperty(mediaType=JSON, value=TASK_JSON),
                    @ExampleProperty(mediaType=XML, value=TASK_XML)})) String payload) {

        Variant v = getVariant(headers);
        String type = getContentType(headers);
        Header conversationIdHeader = buildConversationIdHeader(containerId, context, headers);
        try {
            
            userTaskServiceBase.update(containerId, taskId, userId, payload, type);
            
            return createResponse("", v, Response.Status.CREATED, conversationIdHeader);
        } catch (TaskNotFoundException e){
            return notFound(errorMessage(e, MessageFormat.format(TASK_INSTANCE_NOT_FOUND, taskId)), v, conversationIdHeader);
        } catch (Exception e) {
            logger.error("Unexpected error during processing {}", e.getMessage(), e);
            return internalServerError(errorMessage(e), v, conversationIdHeader);
        }
    }
}
