package org.kie.server.remote.rest.jbpm;

import java.text.MessageFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
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
import org.kie.server.api.model.type.JaxbMap;
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

    void delegate(Long taskId, String userId, String targetUserId) {

    }

    void exit(Long taskId, String userId) {

    }

    void fail(Long taskId, String userId, Map<String, Object> faultData) {

    }

    void forward(Long taskId, String userId, String targetEntityId) {

    }

    void release(Long taskId, String userId) {

    }

    void resume(Long taskId, String userId) {

    }

    void skip(Long taskId, String userId) {

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

    void suspend(Long taskId, String userId) {

    }

    void nominate(Long taskId, String userId, List<OrganizationalEntity> potentialOwners) {

    }

    void setPriority(Long taskId, int priority) {

    }

    void setExpirationDate(Long taskId, Date date) {

    }

    void setSkipable(Long taskId, boolean skipable) {

    }

    void setName(Long taskId, String name) {

    }

    void setDescription(Long taskId, String description) {

    }

    Long saveContent(Long taskId, Map<String, Object> values) {
        return null;
    }

    Map<String, Object> getTaskOutputContentByTaskId(Long taskId) {
        return null;
    }

    Map<String, Object> getTaskInputContentByTaskId(Long taskId) {
        return null;
    }

    void deleteContent(Long taskId, Long contentId) {

    }

    Long addComment(Long taskId, String text, String addedBy, Date addedOn) {
        return null;
    }

    void deleteComment(Long taskId, Long commentId) {

    }

    List<Comment> getCommentsByTaskId(Long taskId) {
        return null;
    }

    Comment getCommentById(Long taskId, Long commentId) {
        return null;
    }

    Long addAttachment(Long taskId, String userId, Object attachment) {
        return null;
    }

    void deleteAttachment(Long taskId, Long attachmentId) {

    }

    Attachment getAttachmentById(Long taskId, Long attachmentId) {
        return null;
    }

    Object getAttachmentContentById(Long taskId, Long attachmentId) {
        return null;
    }

    List<Attachment> getAttachmentsByTaskId(Long taskId) {
        return null;
    }

    Task getTask(Long taskId) {
        return null;
    }
}
