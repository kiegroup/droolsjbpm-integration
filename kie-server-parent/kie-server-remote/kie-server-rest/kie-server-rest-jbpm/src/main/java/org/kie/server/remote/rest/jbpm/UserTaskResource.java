package org.kie.server.remote.rest.jbpm;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.ws.rs.DELETE;
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

import org.jbpm.services.api.TaskNotFoundException;
import org.jbpm.services.api.UserTaskService;
import org.kie.api.task.model.Attachment;
import org.kie.api.task.model.Comment;
import org.kie.api.task.model.OrganizationalEntity;
import org.kie.api.task.model.Task;
import org.kie.internal.identity.IdentityProvider;
import org.kie.internal.task.api.TaskModelProvider;
import org.kie.internal.task.api.model.InternalPeopleAssignments;
import org.kie.internal.task.api.model.InternalTask;
import org.kie.server.api.KieServerConstants;
import org.kie.server.api.marshalling.ModelWrapper;
import org.kie.server.api.model.instance.TaskAttachment;
import org.kie.server.api.model.instance.TaskAttachmentList;
import org.kie.server.api.model.instance.TaskComment;
import org.kie.server.api.model.instance.TaskCommentList;
import org.kie.server.api.model.instance.TaskInstance;
import org.kie.server.api.model.type.JaxbBoolean;
import org.kie.server.api.model.type.JaxbDate;
import org.kie.server.api.model.type.JaxbInteger;
import org.kie.server.api.model.type.JaxbMap;
import org.kie.server.api.model.type.JaxbString;
import org.kie.server.remote.rest.common.exception.ExecutionServerRestOperationException;
import org.kie.server.services.api.KieServerRegistry;
import org.kie.server.services.impl.marshal.MarshallerHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.kie.server.api.rest.RestURI.*;
import static org.kie.server.remote.rest.common.util.RestUtils.*;
import static org.kie.server.remote.rest.jbpm.resources.Messages.*;

@Path("/server")
public class UserTaskResource {

    public static final Logger logger = LoggerFactory.getLogger(UserTaskResource.class);
    private static final Boolean BYPASS_AUTH_USER = Boolean.parseBoolean(System.getProperty("org.kie.server.bypass.auth.user", "false"));

    private IdentityProvider identityProvider;
    private UserTaskService userTaskService;

    private MarshallerHelper marshallerHelper;

    public UserTaskResource(UserTaskService userTaskService, KieServerRegistry context) {
        this.userTaskService = userTaskService;
        this.identityProvider = context.getIdentityProvider();
        this.marshallerHelper = new MarshallerHelper(context);
    }

    protected String getUser(String queryParamUser) {
        if (BYPASS_AUTH_USER) {
            return queryParamUser;
        }

        return identityProvider.getName();
    }

    @PUT
    @Path(TASK_INSTANCE_ACTIVATE_PUT_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response activate(@Context HttpHeaders headers, @PathParam("id") String containerId,
            @PathParam("tInstanceId") Long taskId, @QueryParam("user") String userId) {

        Variant v = getVariant(headers);
        try {
            userId = getUser(userId);
            logger.debug("About to activate task with id '{}' as user '{}'", taskId, userId);
            userTaskService.activate(taskId, userId);

            return createResponse("", v, Response.Status.CREATED);
        } catch (TaskNotFoundException e){
            throw ExecutionServerRestOperationException.notFound(MessageFormat.format(TASK_INSTANCE_NOT_FOUND, taskId), v);
        } catch (Exception e) {
            logger.error("Unexpected error during processing {}", e.getMessage(), e);
            throw ExecutionServerRestOperationException.internalServerError(MessageFormat.format(UNEXPECTED_ERROR, e.getMessage()), v);
        }

    }

    @PUT
    @Path(TASK_INSTANCE_CLAIM_PUT_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response claim(@Context HttpHeaders headers, @PathParam("id") String containerId,
            @PathParam("tInstanceId") Long taskId, @QueryParam("user")  String userId) {
        Variant v = getVariant(headers);
        try {
            userId = getUser(userId);
            logger.debug("About to claim task with id '{}' as user '{}'", taskId, userId);
            userTaskService.claim(taskId, userId);

            return createResponse("", v, Response.Status.CREATED);
        } catch (TaskNotFoundException e){
            throw ExecutionServerRestOperationException.notFound(MessageFormat.format(TASK_INSTANCE_NOT_FOUND, taskId), v);
        } catch (Exception e) {
            logger.error("Unexpected error during processing {}", e.getMessage(), e);
            throw ExecutionServerRestOperationException.internalServerError(MessageFormat.format(UNEXPECTED_ERROR, e.getMessage()), v);
        }
    }

    @PUT
    @Path(TASK_INSTANCE_COMPLETE_PUT_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response complete(@Context HttpHeaders headers, @PathParam("id") String containerId,
            @PathParam("tInstanceId") Long taskId, @QueryParam("user") String userId, String payload) {

        Variant v = getVariant(headers);
        String type = v.getMediaType().getSubtype();
        try {
            userId = getUser(userId);
            logger.debug("About to unmarshal task outcome parameters from payload: '{}'", payload);
            Map<String, Object> parameters = marshallerHelper.unmarshal(containerId, payload, type, JaxbMap.class, Map.class);

            logger.debug("About to complete task with id '{}' as user '{}' with data {}", taskId, userId, parameters);
            userTaskService.complete(taskId, userId, parameters);

            return createResponse("", v, Response.Status.CREATED);
        } catch (TaskNotFoundException e){
            throw ExecutionServerRestOperationException.notFound(MessageFormat.format(TASK_INSTANCE_NOT_FOUND, taskId), v);
        } catch (Exception e) {
            logger.error("Unexpected error during processing {}", e.getMessage(), e);
            throw ExecutionServerRestOperationException.internalServerError(MessageFormat.format(UNEXPECTED_ERROR, e.getMessage()), v);
        }
    }

    @PUT
    @Path(TASK_INSTANCE_DELEGATE_PUT_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response delegate(@Context HttpHeaders headers, @PathParam("id") String containerId,
            @PathParam("tInstanceId") Long taskId, @QueryParam("user") String userId, @QueryParam("targetUser") String targetUserId) {

        Variant v = getVariant(headers);
        try {
            userId = getUser(userId);
            logger.debug("About to delegate task with id '{}' as user '{}' to user '{}'", taskId, userId, targetUserId);
            userTaskService.delegate(taskId, userId, targetUserId);

            return createResponse("", v, Response.Status.CREATED);
        } catch (TaskNotFoundException e){
            throw ExecutionServerRestOperationException.notFound(MessageFormat.format(TASK_INSTANCE_NOT_FOUND, taskId), v);
        } catch (Exception e) {
            logger.error("Unexpected error during processing {}", e.getMessage(), e);
            throw ExecutionServerRestOperationException.internalServerError(MessageFormat.format(UNEXPECTED_ERROR, e.getMessage()), v);
        }

    }

    @PUT
    @Path(TASK_INSTANCE_EXIT_PUT_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response exit(@Context HttpHeaders headers, @PathParam("id") String containerId,
            @PathParam("tInstanceId") Long taskId, @QueryParam("user") String userId) {

        Variant v = getVariant(headers);
        try {
            userId = getUser(userId);
            logger.debug("About to exit task with id '{}' as user '{}'", taskId, userId);
            userTaskService.exit(taskId, userId);

            return createResponse("", v, Response.Status.CREATED);
        } catch (TaskNotFoundException e){
            throw ExecutionServerRestOperationException.notFound(MessageFormat.format(TASK_INSTANCE_NOT_FOUND, taskId), v);
        } catch (Exception e) {
            logger.error("Unexpected error during processing {}", e.getMessage(), e);
            throw ExecutionServerRestOperationException.internalServerError(MessageFormat.format(UNEXPECTED_ERROR, e.getMessage()), v);
        }
    }

    @PUT
    @Path(TASK_INSTANCE_FAIL_PUT_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response fail(@Context HttpHeaders headers, @PathParam("id") String containerId,
            @PathParam("tInstanceId") Long taskId, @QueryParam("user") String userId, String payload) {

        Variant v = getVariant(headers);
        String type = v.getMediaType().getSubtype();
        try {
            userId = getUser(userId);
            logger.debug("About to unmarshal task failure data from payload: '{}'", payload);
            Map<String, Object> parameters = marshallerHelper.unmarshal(containerId, payload, type, JaxbMap.class, Map.class);

            logger.debug("About to fail task with id '{}' as user '{}' with data {}", taskId, userId, parameters);
            userTaskService.fail(taskId, userId, parameters);

            return createResponse("", v, Response.Status.CREATED);
        } catch (TaskNotFoundException e){
            throw ExecutionServerRestOperationException.notFound(MessageFormat.format(TASK_INSTANCE_NOT_FOUND, taskId), v);
        } catch (Exception e) {
            logger.error("Unexpected error during processing {}", e.getMessage(), e);
            throw ExecutionServerRestOperationException.internalServerError(MessageFormat.format(UNEXPECTED_ERROR, e.getMessage()), v);
        }
    }

    @PUT
    @Path(TASK_INSTANCE_FORWARD_PUT_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response forward(@Context HttpHeaders headers, @PathParam("id") String containerId,
            @PathParam("tInstanceId") Long taskId, @QueryParam("user") String userId, @QueryParam("targetUser") String targetUserId) {

        Variant v = getVariant(headers);
        try {
            userId = getUser(userId);
            logger.debug("About to forward task with id '{}' as user '{}' to user '{}'", taskId, userId, targetUserId);
            userTaskService.forward(taskId, userId, targetUserId);

            return createResponse("", v, Response.Status.CREATED);
        } catch (TaskNotFoundException e){
            throw ExecutionServerRestOperationException.notFound(MessageFormat.format(TASK_INSTANCE_NOT_FOUND, taskId), v);
        } catch (Exception e) {
            logger.error("Unexpected error during processing {}", e.getMessage(), e);
            throw ExecutionServerRestOperationException.internalServerError(MessageFormat.format(UNEXPECTED_ERROR, e.getMessage()), v);
        }
    }

    @PUT
    @Path(TASK_INSTANCE_RELEASE_PUT_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response release(@Context HttpHeaders headers, @PathParam("id") String containerId,
            @PathParam("tInstanceId") Long taskId, @QueryParam("user") String userId) {
        Variant v = getVariant(headers);
        try {
            userId = getUser(userId);
            logger.debug("About to release task with id '{}' as user '{}'", taskId, userId);
            userTaskService.release(taskId, userId);

            return createResponse("", v, Response.Status.CREATED);
        } catch (TaskNotFoundException e){
            throw ExecutionServerRestOperationException.notFound(MessageFormat.format(TASK_INSTANCE_NOT_FOUND, taskId), v);
        } catch (Exception e) {
            logger.error("Unexpected error during processing {}", e.getMessage(), e);
            throw ExecutionServerRestOperationException.internalServerError(MessageFormat.format(UNEXPECTED_ERROR, e.getMessage()), v);
        }
    }

    @PUT
    @Path(TASK_INSTANCE_RESUME_PUT_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response resume(@Context HttpHeaders headers, @PathParam("id") String containerId,
            @PathParam("tInstanceId") Long taskId, @QueryParam("user") String userId) {
        Variant v = getVariant(headers);
        try {
            userId = getUser(userId);
            logger.debug("About to resume task with id '{}' as user '{}'", taskId, userId);
            userTaskService.resume(taskId, userId);

            return createResponse("", v, Response.Status.CREATED);
        } catch (TaskNotFoundException e){
            throw ExecutionServerRestOperationException.notFound(MessageFormat.format(TASK_INSTANCE_NOT_FOUND, taskId), v);
        } catch (Exception e) {
            logger.error("Unexpected error during processing {}", e.getMessage(), e);
            throw ExecutionServerRestOperationException.internalServerError(MessageFormat.format(UNEXPECTED_ERROR, e.getMessage()), v);
        }
    }

    @PUT
    @Path(TASK_INSTANCE_SKIP_PUT_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response skip(@Context HttpHeaders headers, @PathParam("id") String containerId,
            @PathParam("tInstanceId") Long taskId, @QueryParam("user") String userId) {
        Variant v = getVariant(headers);
        try {
            userId = getUser(userId);
            logger.debug("About to skip task with id '{}' as user '{}'", taskId, userId);
            userTaskService.skip(taskId, userId);

            return createResponse("", v, Response.Status.CREATED);
        } catch (TaskNotFoundException e){
            throw ExecutionServerRestOperationException.notFound(MessageFormat.format(TASK_INSTANCE_NOT_FOUND, taskId), v);
        } catch (Exception e) {
            logger.error("Unexpected error during processing {}", e.getMessage(), e);
            throw ExecutionServerRestOperationException.internalServerError(MessageFormat.format(UNEXPECTED_ERROR, e.getMessage()), v);
        }
    }
    @PUT
    @Path(TASK_INSTANCE_START_PUT_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response start(@Context HttpHeaders headers, @PathParam("id") String containerId,
            @PathParam("tInstanceId") Long taskId, @QueryParam("user") String userId) {

        Variant v = getVariant(headers);
        try {
            userId = getUser(userId);
            logger.debug("About to start task with id '{}' as user '{}'", taskId, userId);
            userTaskService.start(taskId, userId);

            return createResponse("", v, Response.Status.CREATED);
        } catch (TaskNotFoundException e){
            throw ExecutionServerRestOperationException.notFound(MessageFormat.format(TASK_INSTANCE_NOT_FOUND, taskId), v);
        } catch (Exception e) {
            logger.error("Unexpected error during processing {}", e.getMessage(), e);
            throw ExecutionServerRestOperationException.internalServerError(MessageFormat.format(UNEXPECTED_ERROR, e.getMessage()), v);
        }
    }

    @PUT
    @Path(TASK_INSTANCE_STOP_PUT_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response stop(@Context HttpHeaders headers, @PathParam("id") String containerId,
            @PathParam("tInstanceId") Long taskId, @QueryParam("user") String userId) {

        Variant v = getVariant(headers);
        try {
            userId = getUser(userId);
            logger.debug("About to stop task with id '{}' as user '{}'", taskId, userId);
            userTaskService.stop(taskId, userId);

            return createResponse("", v, Response.Status.CREATED);
        } catch (TaskNotFoundException e){
            throw ExecutionServerRestOperationException.notFound(MessageFormat.format(TASK_INSTANCE_NOT_FOUND, taskId), v);
        } catch (Exception e) {
            logger.error("Unexpected error during processing {}", e.getMessage(), e);
            throw ExecutionServerRestOperationException.internalServerError(MessageFormat.format(UNEXPECTED_ERROR, e.getMessage()), v);
        }
    }

    @PUT
    @Path(TASK_INSTANCE_SUSPEND_PUT_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response suspend(@Context HttpHeaders headers, @PathParam("id") String containerId,
            @PathParam("tInstanceId") Long taskId, @QueryParam("user") String userId) {
        Variant v = getVariant(headers);
        try {
            userId = getUser(userId);
            logger.debug("About to suspend task with id '{}' as user '{}'", taskId, userId);
            userTaskService.suspend(taskId, userId);

            return createResponse("", v, Response.Status.CREATED);
        } catch (TaskNotFoundException e){
            throw ExecutionServerRestOperationException.notFound(MessageFormat.format(TASK_INSTANCE_NOT_FOUND, taskId), v);
        } catch (Exception e) {
            logger.error("Unexpected error during processing {}", e.getMessage(), e);
            throw ExecutionServerRestOperationException.internalServerError(MessageFormat.format(UNEXPECTED_ERROR, e.getMessage()), v);
        }
    }

    @PUT
    @Path(TASK_INSTANCE_NOMINATE_PUT_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response nominate(@Context HttpHeaders headers, @PathParam("id") String containerId,
            @PathParam("tInstanceId") Long taskId, @QueryParam("user") String userId, @QueryParam("potOwner") List<String> potentialOwners) {

        Variant v = getVariant(headers);
        try {
            userId = getUser(userId);
            logger.debug("About to nominate task with id '{}' as user '{}' to potential owners", taskId, userId, potentialOwners);
            List<OrganizationalEntity> potOwnerEntities = new ArrayList<OrganizationalEntity>();
            for (String potOwnerId : potentialOwners) {
                potOwnerEntities.add(TaskModelProvider.getFactory().newUser(potOwnerId));
            }
            userTaskService.nominate(taskId, userId, potOwnerEntities);

            return createResponse("", v, Response.Status.CREATED);
        } catch (TaskNotFoundException e){
            throw ExecutionServerRestOperationException.notFound(MessageFormat.format(TASK_INSTANCE_NOT_FOUND, taskId), v);
        } catch (Exception e) {
            logger.error("Unexpected error during processing {}", e.getMessage(), e);
            throw ExecutionServerRestOperationException.internalServerError(MessageFormat.format(UNEXPECTED_ERROR, e.getMessage()), v);
        }
    }

    @PUT
    @Path(TASK_INSTANCE_PRIORITY_PUT_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response setPriority(@Context HttpHeaders headers, @PathParam("id") String containerId,
            @PathParam("tInstanceId") Long taskId, String priorityPayload) {

        Variant v = getVariant(headers);
        String type = v.getMediaType().getSubtype();
        try {

            logger.debug("About to unmarshal task priority from payload: '{}'", priorityPayload);
            Integer priority = marshallerHelper.unmarshal(containerId, priorityPayload, type, JaxbInteger.class, Integer.class);

            logger.debug("About to set priority for a task with id '{}' with value '{}'", taskId, priority);
            userTaskService.setPriority(taskId, priority);

            return createResponse("", v, Response.Status.CREATED);
        } catch (TaskNotFoundException e){
            throw ExecutionServerRestOperationException.notFound(MessageFormat.format(TASK_INSTANCE_NOT_FOUND, taskId), v);
        } catch (Exception e) {
            logger.error("Unexpected error during processing {}", e.getMessage(), e);
            throw ExecutionServerRestOperationException.internalServerError(MessageFormat.format(UNEXPECTED_ERROR, e.getMessage()), v);
        }
    }

    @PUT
    @Path(TASK_INSTANCE_EXPIRATION_DATE_PUT_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response setExpirationDate(@Context HttpHeaders headers, @PathParam("id") String containerId,
            @PathParam("tInstanceId") Long taskId, String datePayload) {

        Variant v = getVariant(headers);
        String type = v.getMediaType().getSubtype();
        try {

            logger.debug("About to unmarshal task priority from payload: '{}'", datePayload);
            Date expirationDate = marshallerHelper.unmarshal(containerId, datePayload, type, JaxbDate.class, Date.class);

            logger.debug("About to set expiration date for a task with id '{}' with value '{}'", taskId, expirationDate);
            userTaskService.setExpirationDate(taskId, expirationDate);

            return createResponse("", v, Response.Status.CREATED);
        } catch (TaskNotFoundException e){
            throw ExecutionServerRestOperationException.notFound(MessageFormat.format(TASK_INSTANCE_NOT_FOUND, taskId), v);
        } catch (Exception e) {
            logger.error("Unexpected error during processing {}", e.getMessage(), e);
            throw ExecutionServerRestOperationException.internalServerError(MessageFormat.format(UNEXPECTED_ERROR, e.getMessage()), v);
        }
    }

    @PUT
    @Path(TASK_INSTANCE_SKIPABLE_PUT_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response setSkipable(@Context HttpHeaders headers, @PathParam("id") String containerId,
            @PathParam("tInstanceId") Long taskId, String skipablePayload) {

        Variant v = getVariant(headers);
        String type = v.getMediaType().getSubtype();
        try {

            logger.debug("About to unmarshal task skipable from payload: '{}'", skipablePayload);
            Boolean skipable = marshallerHelper.unmarshal(containerId, skipablePayload, type, JaxbBoolean.class, Boolean.class);

            logger.debug("About to set skipable attribute for a task with id '{}' with value '{}'", taskId, skipable);
            userTaskService.setSkipable(taskId, skipable);

            return createResponse("", v, Response.Status.CREATED);
        } catch (TaskNotFoundException e){
            throw ExecutionServerRestOperationException.notFound(MessageFormat.format(TASK_INSTANCE_NOT_FOUND, taskId), v);
        } catch (Exception e) {
            logger.error("Unexpected error during processing {}", e.getMessage(), e);
            throw ExecutionServerRestOperationException.internalServerError(MessageFormat.format(UNEXPECTED_ERROR, e.getMessage()), v);
        }
    }

    @PUT
    @Path(TASK_INSTANCE_NAME_PUT_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response setName(@Context HttpHeaders headers, @PathParam("id") String containerId,
            @PathParam("tInstanceId") Long taskId, String namePayload) {
        Variant v = getVariant(headers);
        String type = v.getMediaType().getSubtype();
        try {

            logger.debug("About to unmarshal task name from payload: '{}'", namePayload);
            String name = marshallerHelper.unmarshal(containerId, namePayload, type, JaxbString.class, String.class);

            logger.debug("About to set name for a task with id '{}' with value '{}'", taskId, name);
            userTaskService.setName(taskId, name);

            return createResponse("", v, Response.Status.CREATED);
        } catch (TaskNotFoundException e){
            throw ExecutionServerRestOperationException.notFound(MessageFormat.format(TASK_INSTANCE_NOT_FOUND, taskId), v);
        } catch (Exception e) {
            logger.error("Unexpected error during processing {}", e.getMessage(), e);
            throw ExecutionServerRestOperationException.internalServerError(MessageFormat.format(UNEXPECTED_ERROR, e.getMessage()), v);
        }
    }

    @PUT
    @Path(TASK_INSTANCE_DESCRIPTION_PUT_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response setDescription(@Context HttpHeaders headers, @PathParam("id") String containerId,
            @PathParam("tInstanceId") Long taskId, String descriptionPayload) {
        Variant v = getVariant(headers);
        String type = v.getMediaType().getSubtype();
        try {

            logger.debug("About to unmarshal task description from payload: '{}'", descriptionPayload);
            String description = marshallerHelper.unmarshal(containerId, descriptionPayload, type, JaxbString.class, String.class);

            logger.debug("About to set name for a task with id '{}' with value '{}'", taskId, description);
            userTaskService.setDescription(taskId, description);

            return createResponse("", v, Response.Status.CREATED);
        } catch (TaskNotFoundException e){
            throw ExecutionServerRestOperationException.notFound(MessageFormat.format(TASK_INSTANCE_NOT_FOUND, taskId), v);
        } catch (Exception e) {
            logger.error("Unexpected error during processing {}", e.getMessage(), e);
            throw ExecutionServerRestOperationException.internalServerError(MessageFormat.format(UNEXPECTED_ERROR, e.getMessage()), v);
        }
    }

    @PUT
    @Path(TASK_INSTANCE_OUTPUT_DATA_PUT_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response saveContent(@Context HttpHeaders headers, @PathParam("id") String containerId,
            @PathParam("tInstanceId") Long taskId, String payload) {
        Variant v = getVariant(headers);
        String type = v.getMediaType().getSubtype();
        try {

            logger.debug("About to unmarshal task content parameters from payload: '{}'", payload);
            Map<String, Object> parameters = marshallerHelper.unmarshal(containerId, payload, type, JaxbMap.class, Map.class);

            logger.debug("About to set content of a task with id '{}' with data {}", taskId, parameters);
            Long contentId = userTaskService.saveContent(taskId, parameters);

            String response = marshallerHelper.marshal(containerId, type, ModelWrapper.wrap(contentId));

            logger.debug("Returning CREATED response with content '{}'", response);
            return createResponse(response, v, Response.Status.CREATED);
        } catch (TaskNotFoundException e){
            throw ExecutionServerRestOperationException.notFound(MessageFormat.format(TASK_INSTANCE_NOT_FOUND, taskId), v);
        } catch (Exception e) {
            logger.error("Unexpected error during processing {}", e.getMessage(), e);
            throw ExecutionServerRestOperationException.internalServerError(MessageFormat.format(UNEXPECTED_ERROR, e.getMessage()), v);
        }
    }

    @GET
    @Path(TASK_INSTANCE_OUTPUT_DATA_GET_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getTaskOutputContentByTaskId(@Context HttpHeaders headers, @PathParam("id") String containerId,
            @PathParam("tInstanceId") Long taskId) {
        Variant v = getVariant(headers);
        String type = v.getMediaType().getSubtype();
        try {
            Map<String, Object> variables = userTaskService.getTaskOutputContentByTaskId(taskId);

            logger.debug("About to marshal task '{}' output variables {}", taskId, variables);
            String response = marshallerHelper.marshal(containerId, type, ModelWrapper.wrap(variables));

            logger.debug("Returning OK response with content '{}'", response);
            return createResponse(response, v, Response.Status.OK);

        } catch (TaskNotFoundException e){
            throw ExecutionServerRestOperationException.notFound(MessageFormat.format(TASK_INSTANCE_NOT_FOUND, taskId), v);
        } catch (Exception e) {
            logger.error("Unexpected error during processing {}", e.getMessage(), e);
            throw ExecutionServerRestOperationException.internalServerError(MessageFormat.format(UNEXPECTED_ERROR, e.getMessage()), v);
        }
    }

    @GET
    @Path(TASK_INSTANCE_INPUT_DATA_GET_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getTaskInputContentByTaskId(@Context HttpHeaders headers, @PathParam("id") String containerId,
            @PathParam("tInstanceId") Long taskId) {
        Variant v = getVariant(headers);
        String type = v.getMediaType().getSubtype();
        try {
            Map<String, Object> variables = userTaskService.getTaskInputContentByTaskId(taskId);

            logger.debug("About to marshal task '{}' input variables {}", taskId, variables);
            String response = marshallerHelper.marshal(containerId, type, ModelWrapper.wrap(variables));

            logger.debug("Returning OK response with content '{}'", response);
            return createResponse(response, v, Response.Status.OK);

        } catch (TaskNotFoundException e){
            throw ExecutionServerRestOperationException.notFound(MessageFormat.format(TASK_INSTANCE_NOT_FOUND, taskId), v);
        } catch (Exception e) {
            logger.error("Unexpected error during processing {}", e.getMessage(), e);
            throw ExecutionServerRestOperationException.internalServerError(MessageFormat.format(UNEXPECTED_ERROR, e.getMessage()), v);
        }
    }

    @DELETE
    @Path(TASK_INSTANCE_CONTENT_DATA_DELETE_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response deleteContent(@Context HttpHeaders headers, @PathParam("id") String containerId,
            @PathParam("tInstanceId") Long taskId, @PathParam("contentId") Long contentId) {

        Variant v = getVariant(headers);
        try {

            userTaskService.deleteContent(taskId, contentId);
            // return null to produce 204 NO_CONTENT response code
            return null;
        } catch (TaskNotFoundException e){
            throw ExecutionServerRestOperationException.notFound(MessageFormat.format(TASK_INSTANCE_NOT_FOUND, taskId), v);
        } catch (Exception e) {
            logger.error("Unexpected error during processing {}", e.getMessage(), e);
            throw ExecutionServerRestOperationException.internalServerError(MessageFormat.format(UNEXPECTED_ERROR, e.getMessage()), v);
        }
    }

    @POST
    @Path(TASK_INSTANCE_COMMENT_ADD_POST_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response addComment(@Context HttpHeaders headers, @PathParam("id") String containerId,
            @PathParam("tInstanceId") Long taskId, String payload) {
        Variant v = getVariant(headers);
        String type = v.getMediaType().getSubtype();
        try {

            logger.debug("About to unmarshal task comment from payload: '{}'", payload);
            TaskComment comment = marshallerHelper.unmarshal(containerId, payload, type, TaskComment.class, TaskComment.class);

            logger.debug("About to set comment on a task with id '{}' with data {}", taskId, comment);
            Long commentId = userTaskService.addComment(taskId, comment.getText(), comment.getAddedBy(), comment.getAddedAt());

            String response = marshallerHelper.marshal(containerId, type, ModelWrapper.wrap(commentId));

            logger.debug("Returning CREATED response with content '{}'", response);
            return createResponse(response, v, Response.Status.CREATED);
        } catch (TaskNotFoundException e){
            throw ExecutionServerRestOperationException.notFound(MessageFormat.format(TASK_INSTANCE_NOT_FOUND, taskId), v);
        } catch (Exception e) {
            logger.error("Unexpected error during processing {}", e.getMessage(), e);
            throw ExecutionServerRestOperationException.internalServerError(MessageFormat.format(UNEXPECTED_ERROR, e.getMessage()), v);
        }
    }

    @DELETE
    @Path(TASK_INSTANCE_COMMENT_DELETE_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response deleteComment(@Context HttpHeaders headers, @PathParam("id") String containerId,
            @PathParam("tInstanceId") Long taskId, @PathParam("commentId") Long commentId) {

        Variant v = getVariant(headers);
        try {

            userTaskService.deleteComment(taskId, commentId);
            // return null to produce 204 NO_CONTENT response code
            return null;
        } catch (TaskNotFoundException e){
            throw ExecutionServerRestOperationException.notFound(MessageFormat.format(TASK_INSTANCE_NOT_FOUND, taskId), v);
        } catch (Exception e) {
            logger.error("Unexpected error during processing {}", e.getMessage(), e);
            throw ExecutionServerRestOperationException.internalServerError(MessageFormat.format(UNEXPECTED_ERROR, e.getMessage()), v);
        }
    }

    @GET
    @Path(TASK_INSTANCE_COMMENTS_GET_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getCommentsByTaskId(@Context HttpHeaders headers, @PathParam("id") String containerId,
            @PathParam("tInstanceId") Long taskId) {
        Variant v = getVariant(headers);
        String type = v.getMediaType().getSubtype();
        try {
            List<Comment> comments = userTaskService.getCommentsByTaskId(taskId);

            TaskComment[] taskComments = new TaskComment[comments.size()];
            int counter = 0;
            for (Comment comment : comments) {

                TaskComment taskComment = TaskComment.builder()
                        .id(comment.getId())
                        .text(comment.getText())
                        .addedBy(comment.getAddedBy().getId())
                        .addedAt(comment.getAddedAt())
                        .build();

                taskComments[counter] = taskComment;
                counter++;
            }
            TaskCommentList result = new TaskCommentList(taskComments);

            logger.debug("About to marshal task '{}' comments {}", taskId, result);
            String response = marshallerHelper.marshal(containerId, type, result);

            logger.debug("Returning OK response with content '{}'", response);
            return createResponse(response, v, Response.Status.OK);

        } catch (TaskNotFoundException e){
            throw ExecutionServerRestOperationException.notFound(MessageFormat.format(TASK_INSTANCE_NOT_FOUND, taskId), v);
        } catch (Exception e) {
            logger.error("Unexpected error during processing {}", e.getMessage(), e);
            throw ExecutionServerRestOperationException.internalServerError(MessageFormat.format(UNEXPECTED_ERROR, e.getMessage()), v);
        }
    }

    @GET
    @Path(TASK_INSTANCE_COMMENT_GET_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getCommentById(@Context HttpHeaders headers, @PathParam("id") String containerId,
            @PathParam("tInstanceId") Long taskId, @PathParam("commentId") Long commentId) {
        Variant v = getVariant(headers);
        String type = v.getMediaType().getSubtype();
        try {
            Comment comment = userTaskService.getCommentById(taskId, commentId);

            if (comment == null) {
                throw ExecutionServerRestOperationException.notFound(
                        MessageFormat.format(TASK_COMMENT_NOT_FOUND, commentId, taskId), v);
            }

            TaskComment taskComment = TaskComment.builder()
                    .id(comment.getId())
                    .text(comment.getText())
                    .addedBy(comment.getAddedBy().getId())
                    .addedAt(comment.getAddedAt())
                    .build();

            logger.debug("About to marshal task '{}' comment {}", taskId, taskComment);
            String response = marshallerHelper.marshal(containerId, type, taskComment);

            logger.debug("Returning OK response with content '{}'", response);
            return createResponse(response, v, Response.Status.OK);

        } catch (TaskNotFoundException e){
            throw ExecutionServerRestOperationException.notFound(MessageFormat.format(TASK_INSTANCE_NOT_FOUND, taskId), v);
        } catch (Exception e) {
            logger.error("Unexpected error during processing {}", e.getMessage(), e);
            throw ExecutionServerRestOperationException.internalServerError(MessageFormat.format(UNEXPECTED_ERROR, e.getMessage()), v);
        }
    }

    @POST
    @Path(TASK_INSTANCE_ATTACHMENT_ADD_POST_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response addAttachment(@Context HttpHeaders headers, @PathParam("id") String containerId,
            @PathParam("tInstanceId") Long taskId, @QueryParam("user") String userId, String attachmentPayload) {
        Variant v = getVariant(headers);
        String type = v.getMediaType().getSubtype();
        try {
            String classType = getClassType(headers);
            logger.debug("About to unmarshal task attachment from payload: '{}'", attachmentPayload);
            Object attachment = marshallerHelper.unmarshal(containerId, attachmentPayload, type, classType, Object.class);

            logger.debug("About to add attachment on a task with id '{}' with data {}", taskId, attachment);
            Long attachmentId = userTaskService.addAttachment(taskId, getUser(userId), attachment);

            String response = marshallerHelper.marshal(containerId, type, ModelWrapper.wrap(attachmentId));

            logger.debug("Returning CREATED response with content '{}'", response);
            return createResponse(response, v, Response.Status.CREATED);
        } catch (TaskNotFoundException e){
            throw ExecutionServerRestOperationException.notFound(MessageFormat.format(TASK_INSTANCE_NOT_FOUND, taskId), v);
        } catch (Exception e) {
            logger.error("Unexpected error during processing {}", e.getMessage(), e);
            throw ExecutionServerRestOperationException.internalServerError(MessageFormat.format(UNEXPECTED_ERROR, e.getMessage()), v);
        }
    }

    @DELETE
    @Path(TASK_INSTANCE_ATTACHMENT_DELETE_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response deleteAttachment(@Context HttpHeaders headers, @PathParam("id") String containerId,
            @PathParam("tInstanceId") Long taskId, @PathParam("attachmentId") Long attachmentId) {

        Variant v = getVariant(headers);
        try {

            userTaskService.deleteAttachment(taskId, attachmentId);
            // return null to produce 204 NO_CONTENT response code
            return null;
        } catch (TaskNotFoundException e){
            throw ExecutionServerRestOperationException.notFound(MessageFormat.format(TASK_INSTANCE_NOT_FOUND, taskId), v);
        } catch (Exception e) {
            logger.error("Unexpected error during processing {}", e.getMessage(), e);
            throw ExecutionServerRestOperationException.internalServerError(MessageFormat.format(UNEXPECTED_ERROR, e.getMessage()), v);
        }
    }

    @GET
    @Path(TASK_INSTANCE_ATTACHMENT_GET_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getAttachmentById(@Context HttpHeaders headers, @PathParam("id") String containerId,
            @PathParam("tInstanceId") Long taskId, @PathParam("attachmentId") Long attachmentId) {
        Variant v = getVariant(headers);
        String type = v.getMediaType().getSubtype();
        try {
            Attachment attachment = userTaskService.getAttachmentById(taskId, attachmentId);

            TaskAttachment taskAttachment = TaskAttachment.builder()
                    .id(attachment.getId())
                    .name(attachment.getName())
                    .addedBy(attachment.getAttachedBy().getId())
                    .addedAt(attachment.getAttachedAt())
                    .attachmentContentId(attachment.getAttachmentContentId())
                    .contentType(attachment.getContentType())
                    .size(attachment.getSize())
                    .build();

            logger.debug("About to marshal task '{}' attachment {} with content {}", taskId, attachmentId, taskAttachment);
            String response = marshallerHelper.marshal(containerId, type, taskAttachment);

            logger.debug("Returning OK response with content '{}'", response);
            return createResponse(response, v, Response.Status.OK);

        } catch (TaskNotFoundException e){
            throw ExecutionServerRestOperationException.notFound(MessageFormat.format(TASK_INSTANCE_NOT_FOUND, taskId), v);
        } catch (Exception e) {
            logger.error("Unexpected error during processing {}", e.getMessage(), e);
            throw ExecutionServerRestOperationException.internalServerError(MessageFormat.format(UNEXPECTED_ERROR, e.getMessage()), v);
        }
    }

    @GET
    @Path(TASK_INSTANCE_ATTACHMENT_CONTENT_GET_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getAttachmentContentById(@Context HttpHeaders headers, @PathParam("id") String containerId,
            @PathParam("tInstanceId") Long taskId, @PathParam("attachmentId") Long attachmentId) {
        Variant v = getVariant(headers);
        String type = v.getMediaType().getSubtype();
        try {

            Object attachment = userTaskService.getAttachmentContentById(taskId, attachmentId);

            if (attachment == null) {
                throw ExecutionServerRestOperationException.notFound(
                        MessageFormat.format(TASK_ATTACHMENT_NOT_FOUND, attachmentId, taskId), v);
            }

            logger.debug("About to marshal task attachment with id '{}' {}", attachmentId, attachment);
            Object wrappedObject = ModelWrapper.wrap(attachment);
            String response = marshallerHelper.marshal(containerId, type, wrappedObject);

            logger.debug("Returning OK response with content '{}'", response);
            return createResponse(response, Collections.singletonMap(KieServerConstants.CLASS_TYPE_HEADER, wrappedObject.getClass().getName()), v, Response.Status.OK);

        } catch (TaskNotFoundException e){
            throw ExecutionServerRestOperationException.notFound(MessageFormat.format(TASK_INSTANCE_NOT_FOUND, taskId), v);
        } catch (Exception e) {
            logger.error("Unexpected error during processing {}", e.getMessage(), e);
            throw ExecutionServerRestOperationException.internalServerError(MessageFormat.format(UNEXPECTED_ERROR, e.getMessage()), v);
        }
    }
    @GET
    @Path(TASK_INSTANCE_ATTACHMENTS_GET_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getAttachmentsByTaskId(@Context HttpHeaders headers, @PathParam("id") String containerId,
            @PathParam("tInstanceId") Long taskId) {
        Variant v = getVariant(headers);
        String type = v.getMediaType().getSubtype();
        try {
            List<Attachment> attachments = userTaskService.getAttachmentsByTaskId(taskId);

            TaskAttachment[] taskComments = new TaskAttachment[attachments.size()];
            int counter = 0;
            for (Attachment attachment : attachments) {

                TaskAttachment taskComment = TaskAttachment.builder()
                        .id(attachment.getId())
                        .name(attachment.getName())
                        .addedBy(attachment.getAttachedBy().getId())
                        .addedAt(attachment.getAttachedAt())
                        .contentType(attachment.getContentType())
                        .attachmentContentId(attachment.getAttachmentContentId())
                        .size(attachment.getSize())
                        .build();

                taskComments[counter] = taskComment;
                counter++;
            }
            TaskAttachmentList result = new TaskAttachmentList(taskComments);

            logger.debug("About to marshal task '{}' attachments {}", taskId, result);
            String response = marshallerHelper.marshal(containerId, type, result);

            logger.debug("Returning OK response with content '{}'", response);
            return createResponse(response, v, Response.Status.OK);

        } catch (TaskNotFoundException e){
            throw ExecutionServerRestOperationException.notFound(MessageFormat.format(TASK_INSTANCE_NOT_FOUND, taskId), v);
        } catch (Exception e) {
            logger.error("Unexpected error during processing {}", e.getMessage(), e);
            throw ExecutionServerRestOperationException.internalServerError(MessageFormat.format(UNEXPECTED_ERROR, e.getMessage()), v);
        }
    }

    @GET
    @Path(TASK_INSTANCE_GET_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response  getTask(@Context HttpHeaders headers, @PathParam("id") String containerId, @PathParam("tInstanceId") Long taskId,
            @QueryParam("withInputData") boolean withInput, @QueryParam("withOutputData") boolean withOutput, @QueryParam("withAssignments") boolean withAssignments) {
        Variant v = getVariant(headers);
        String type = v.getMediaType().getSubtype();
        try {

            Task task = userTaskService.getTask(taskId);
            TaskInstance.Builder builder = TaskInstance.builder();
            builder
                    .id(task.getId())
                    .name(task.getName())
                    .subject(task.getSubject())
                    .description(task.getDescription())
                    .priority(task.getPriority())
                    .taskType(task.getTaskType())
                    .formName(((InternalTask) task).getFormName())
                    .status(task.getTaskData().getStatus().name())
                    .actualOwner(getOrgEntityIfNotNull(task.getTaskData().getActualOwner()))
                    .createdBy(getOrgEntityIfNotNull(task.getTaskData().getCreatedBy()))
                    .createdOn(task.getTaskData().getCreatedOn())
                    .activationTime(task.getTaskData().getActivationTime())
                    .expirationTime(task.getTaskData().getExpirationTime())
                    .skippable(task.getTaskData().isSkipable())
                    .workItemId(task.getTaskData().getWorkItemId())
                    .processInstanceId(task.getTaskData().getProcessInstanceId())
                    .parentId(task.getTaskData().getParentId())
                    .processId(task.getTaskData().getProcessId())
                    .containerId(task.getTaskData().getDeploymentId());

            if (Boolean.TRUE.equals(withInput)) {
                Map<String, Object> variables = userTaskService.getTaskInputContentByTaskId(taskId);
                builder.inputData(variables);
            }

            if (Boolean.TRUE.equals(withOutput)) {
                Map<String, Object> variables = userTaskService.getTaskOutputContentByTaskId(taskId);
                builder.outputData(variables);
            }

            if (Boolean.TRUE.equals(withAssignments)) {
                builder.potentialOwners(orgEntityAsList(task.getPeopleAssignments().getPotentialOwners()));

                builder.excludedOwners(orgEntityAsList(((InternalPeopleAssignments) task.getPeopleAssignments()).getExcludedOwners()));

                builder.businessAdmins(orgEntityAsList(task.getPeopleAssignments().getBusinessAdministrators()));
            }

            TaskInstance taskInstance = builder.build();


            logger.debug("About to marshal task '{}' representation {}", taskId, taskInstance);
            String response = marshallerHelper.marshal(containerId, type, taskInstance);

            logger.debug("Returning OK response with content '{}'", response);
            return createResponse(response, v, Response.Status.OK);

        } catch (TaskNotFoundException e){
            throw ExecutionServerRestOperationException.notFound(MessageFormat.format(TASK_INSTANCE_NOT_FOUND, taskId), v);
        } catch (Exception e) {
            logger.error("Unexpected error during processing {}", e.getMessage(), e);
            throw ExecutionServerRestOperationException.internalServerError(MessageFormat.format(UNEXPECTED_ERROR, e.getMessage()), v);
        }
    }

    private String getOrgEntityIfNotNull(OrganizationalEntity organizationalEntity) {
        if (organizationalEntity == null) {
            return "";
        }

        return organizationalEntity.getId();
    }

    private List<String> orgEntityAsList(List<OrganizationalEntity> organizationalEntities) {
        ArrayList<String> entities = new ArrayList<String>();
        if (organizationalEntities == null) {
            return entities;
        }

        for (OrganizationalEntity entity : organizationalEntities) {
            entities.add(entity.getId());
        }

        return entities;
    }
}
