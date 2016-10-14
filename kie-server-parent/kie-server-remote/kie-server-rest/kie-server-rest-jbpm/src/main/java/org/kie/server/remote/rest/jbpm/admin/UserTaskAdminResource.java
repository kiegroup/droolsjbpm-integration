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
import org.jbpm.services.api.ProcessInstanceNotFoundException;
import org.jbpm.services.api.TaskNotFoundException;
import org.kie.server.api.model.admin.TaskNotificationList;
import org.kie.server.api.model.admin.TaskReassignmentList;
import org.kie.server.api.model.admin.TimerInstanceList;
import org.kie.server.remote.rest.common.Header;
import org.kie.server.services.api.KieServerRegistry;
import org.kie.server.services.jbpm.admin.UserTaskAdminServiceBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.kie.server.api.rest.RestURI.*;
import static org.kie.server.remote.rest.common.util.RestUtils.*;
import static org.kie.server.remote.rest.jbpm.resources.Messages.*;

@Path("server/" + ADMIN_TASK_URI)
public class UserTaskAdminResource {

    private static final Logger logger = LoggerFactory.getLogger(ProcessAdminResource.class);

    private UserTaskAdminServiceBase userTaskAdminServiceBase;
    private KieServerRegistry context;


    private static final int POT_OWNER = 1;
    private static final int EXL_OWNER = 2;
    private static final int ADMIN = 3;

    public UserTaskAdminResource() {

    }

    public UserTaskAdminResource(UserTaskAdminServiceBase userTaskAdminServiceBase, KieServerRegistry context) {
        this.userTaskAdminServiceBase = userTaskAdminServiceBase;
        this.context = context;
    }

    @PUT
    @Path(TASK_INSTANCE_POT_OWNERS_USERS_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response addPotentialOwners(@javax.ws.rs.core.Context HttpHeaders headers, @PathParam("id") String containerId, @PathParam("tInstanceId") Long tInstanceId,
            @QueryParam("remove") @DefaultValue("false") boolean removeExisting, String payload) {

        return addToTask(headers, containerId, tInstanceId, removeExisting, payload, POT_OWNER);
    }

    @PUT
    @Path(TASK_INSTANCE_EXL_OWNERS_USERS_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response addExcludedOwners(@javax.ws.rs.core.Context HttpHeaders headers, @PathParam("id") String containerId, @PathParam("tInstanceId") Long tInstanceId,
            @QueryParam("remove") @DefaultValue("false") boolean removeExisting, String payload) {

        return addToTask(headers, containerId, tInstanceId, removeExisting, payload, EXL_OWNER);
    }

    @PUT
    @Path(TASK_INSTANCE_ADMINS_USERS_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response addAdmins(@javax.ws.rs.core.Context HttpHeaders headers, @PathParam("id") String containerId, @PathParam("tInstanceId") Long tInstanceId,
            @QueryParam("remove") @DefaultValue("false") boolean removeExisting, String payload) {

        return addToTask(headers, containerId, tInstanceId, removeExisting, payload, ADMIN);
    }

    @DELETE
    @Path(TASK_INSTANCE_POT_OWNERS_USERS_DELETE_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response removePotentialOwnersUsers(@javax.ws.rs.core.Context HttpHeaders headers, @PathParam("id") String containerId, @PathParam("tInstanceId") Long tInstanceId,
            @PathParam("entityId") String users) {

        return removeFromTask(headers, containerId, tInstanceId, true, users, POT_OWNER);
    }

    @DELETE
    @Path(TASK_INSTANCE_EXL_OWNERS_USERS_DELETE_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response removeExcludedOwnersUsers(@javax.ws.rs.core.Context HttpHeaders headers, @PathParam("id") String containerId, @PathParam("tInstanceId") Long tInstanceId,
            @PathParam("entityId") String users) {

        return removeFromTask(headers, containerId, tInstanceId, true, users, EXL_OWNER);
    }

    @DELETE
    @Path(TASK_INSTANCE_ADMINS_USERS_DELETE_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response removeAdminsUsers(@javax.ws.rs.core.Context HttpHeaders headers, @PathParam("id") String containerId, @PathParam("tInstanceId") Long tInstanceId,
            @PathParam("entityId") String users) {

        return removeFromTask(headers, containerId, tInstanceId, true, users, ADMIN);
    }

    @DELETE
    @Path(TASK_INSTANCE_POT_OWNERS_GROUPS_DELETE_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response removePotentialOwnersGroups(@javax.ws.rs.core.Context HttpHeaders headers, @PathParam("id") String containerId, @PathParam("tInstanceId") Long tInstanceId,
            @PathParam("entityId") String groups) {

        return removeFromTask(headers, containerId, tInstanceId, false, groups, POT_OWNER);
    }

    @DELETE
    @Path(TASK_INSTANCE_EXL_OWNERS_GROUPS_DELETE_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response removeExcludedOwnersGroups(@javax.ws.rs.core.Context HttpHeaders headers, @PathParam("id") String containerId, @PathParam("tInstanceId") Long tInstanceId,
            @PathParam("entityId") String groups) {

        return removeFromTask(headers, containerId, tInstanceId, false, groups, EXL_OWNER);
    }

    @DELETE
    @Path(TASK_INSTANCE_ADMINS_GROUPS_DELETE_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response removeAdminsGroups(@javax.ws.rs.core.Context HttpHeaders headers, @PathParam("id") String containerId, @PathParam("tInstanceId") Long tInstanceId,
            @PathParam("entityId") String groups) {

        return removeFromTask(headers, containerId, tInstanceId, false, groups, ADMIN);
    }

    @PUT
    @Path(TASK_INSTANCE_INPUTS_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response addTaskInputs(@javax.ws.rs.core.Context HttpHeaders headers, @PathParam("id") String containerId, @PathParam("tInstanceId") Long tInstanceId, String payload) {

        Variant v = getVariant(headers);
        String type = getContentType(headers);
        Header conversationIdHeader = buildConversationIdHeader(containerId, context, headers);
        try {
            userTaskAdminServiceBase.addTaskInputs(containerId, tInstanceId, payload, type);
            return createResponse("", v, Response.Status.CREATED, conversationIdHeader);
        } catch (TaskNotFoundException e) {
            return notFound(
                    MessageFormat.format(TASK_INSTANCE_NOT_FOUND, tInstanceId), v, conversationIdHeader);
        } catch (DeploymentNotFoundException e) {
            return notFound(
                    MessageFormat.format(CONTAINER_NOT_FOUND, containerId), v, conversationIdHeader);
        } catch (Exception e) {
            logger.error("Unexpected error during processing {}", e.getMessage(), e);
            return internalServerError(MessageFormat.format(UNEXPECTED_ERROR, e.getMessage()), v, conversationIdHeader);
        }
    }

    @DELETE
    @Path(TASK_INSTANCE_INPUTS_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response removeTaskInputs(@javax.ws.rs.core.Context HttpHeaders headers, @PathParam("id") String containerId, @PathParam("tInstanceId") Long tInstanceId,
            @QueryParam("name")List<String> inputNames) {
        Variant v = getVariant(headers);

        Header conversationIdHeader = buildConversationIdHeader(containerId, context, headers);
        try {
            userTaskAdminServiceBase.removeTaskInputs(containerId, tInstanceId, inputNames);
            return noContent(v, conversationIdHeader);
        } catch (TaskNotFoundException e) {
            return notFound(
                    MessageFormat.format(TASK_INSTANCE_NOT_FOUND, tInstanceId), v, conversationIdHeader);
        } catch (DeploymentNotFoundException e) {
            return notFound(
                    MessageFormat.format(CONTAINER_NOT_FOUND, containerId), v, conversationIdHeader);
        } catch (Exception e) {
            logger.error("Unexpected error during processing {}", e.getMessage(), e);
            return internalServerError(MessageFormat.format(UNEXPECTED_ERROR, e.getMessage()), v, conversationIdHeader);
        }
    }

    @DELETE
    @Path(TASK_INSTANCE_OUTPUTS_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response removeTaskOutputs(@javax.ws.rs.core.Context HttpHeaders headers, @PathParam("id") String containerId, @PathParam("tInstanceId") Long tInstanceId,
            @QueryParam("name")List<String> inputNames) {
        Variant v = getVariant(headers);

        Header conversationIdHeader = buildConversationIdHeader(containerId, context, headers);
        try {
            userTaskAdminServiceBase.removeTaskOutputs(containerId, tInstanceId, inputNames);
            return noContent(v, conversationIdHeader);
        } catch (TaskNotFoundException e) {
            return notFound(
                    MessageFormat.format(TASK_INSTANCE_NOT_FOUND, tInstanceId), v, conversationIdHeader);
        } catch (DeploymentNotFoundException e) {
            return notFound(
                    MessageFormat.format(CONTAINER_NOT_FOUND, containerId), v, conversationIdHeader);
        } catch (Exception e) {
            logger.error("Unexpected error during processing {}", e.getMessage(), e);
            return internalServerError(MessageFormat.format(UNEXPECTED_ERROR, e.getMessage()), v, conversationIdHeader);
        }
    }

    @POST
    @Path(TASK_INSTANCE_REASSIGNMENTS_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response reassign(@javax.ws.rs.core.Context HttpHeaders headers, @PathParam("id") String containerId, @PathParam("tInstanceId") Long tInstanceId,
            @QueryParam("expiresAt") String expiresAt,
            @QueryParam("whenNotStarted") @DefaultValue("false") boolean whenNotStarted,
            @QueryParam("whenNotCompleted") @DefaultValue("false") boolean whenNotCompleted, String payload) {
        Variant v = getVariant(headers);
        String type = getContentType(headers);
        Header conversationIdHeader = buildConversationIdHeader(containerId, context, headers);
        try {
            if (expiresAt == null) {
                return badRequest("'expiresAt' query parameter is mandatory", v, conversationIdHeader);
            }

            if (!whenNotCompleted && !whenNotStarted) {
                return badRequest("At least one query parameters must be set to true - 'whenNotStarted' or 'whenNotCompleted'", v, conversationIdHeader);
            }
            String id = null;
            if (whenNotStarted) {
                id = userTaskAdminServiceBase.reassignWhenNotStarted(containerId, tInstanceId, expiresAt, payload, type);
            }
            if (whenNotCompleted) {
                id = userTaskAdminServiceBase.reassignWhenNotCompleted(containerId, tInstanceId, expiresAt, payload, type);
            }
            return createResponse(id, v, Response.Status.CREATED, conversationIdHeader);
        } catch (TaskNotFoundException e) {
            return notFound(
                    MessageFormat.format(TASK_INSTANCE_NOT_FOUND, tInstanceId), v, conversationIdHeader);
        } catch (DeploymentNotFoundException e) {
            return notFound(
                    MessageFormat.format(CONTAINER_NOT_FOUND, containerId), v, conversationIdHeader);
        } catch (Exception e) {
            logger.error("Unexpected error during processing {}", e.getMessage(), e);
            return internalServerError(MessageFormat.format(UNEXPECTED_ERROR, e.getMessage()), v, conversationIdHeader);
        }
    }

    @POST
    @Path(TASK_INSTANCE_NOTIFICATIONS_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response notify(@javax.ws.rs.core.Context HttpHeaders headers, @PathParam("id") String containerId, @PathParam("tInstanceId") Long tInstanceId,
            @QueryParam("expiresAt") String expiresAt,
            @QueryParam("whenNotStarted") @DefaultValue("false") boolean whenNotStarted,
            @QueryParam("whenNotCompleted") @DefaultValue("false") boolean whenNotCompleted, String payload) {
        Variant v = getVariant(headers);
        String type = getContentType(headers);
        Header conversationIdHeader = buildConversationIdHeader(containerId, context, headers);
        try {
            if (expiresAt == null) {
                return badRequest("'expiresAt' query parameter is mandatory", v, conversationIdHeader);
            }

            if (!whenNotCompleted && !whenNotStarted) {
                return badRequest("At least one query parameters must be set to true - 'whenNotStarted' or 'whenNotCompleted'", v, conversationIdHeader);
            }
            String id = null;
            if (whenNotStarted) {
                id = userTaskAdminServiceBase.notifyWhenNotStarted(containerId, tInstanceId, expiresAt, payload, type);
            }
            if (whenNotCompleted) {
                id = userTaskAdminServiceBase.notifyWhenNotCompleted(containerId, tInstanceId, expiresAt, payload, type);
            }
            return createResponse(id, v, Response.Status.CREATED, conversationIdHeader);
        } catch (TaskNotFoundException e) {
            return notFound(
                    MessageFormat.format(TASK_INSTANCE_NOT_FOUND, tInstanceId), v, conversationIdHeader);
        } catch (DeploymentNotFoundException e) {
            return notFound(
                    MessageFormat.format(CONTAINER_NOT_FOUND, containerId), v, conversationIdHeader);
        } catch (Exception e) {
            logger.error("Unexpected error during processing {}", e.getMessage(), e);
            return internalServerError(MessageFormat.format(UNEXPECTED_ERROR, e.getMessage()), v, conversationIdHeader);
        }
    }

    @DELETE
    @Path(TASK_INSTANCE_REASSIGNMENT_DELETE_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response cancelReassignment(@javax.ws.rs.core.Context HttpHeaders headers, @PathParam("id") String containerId, @PathParam("tInstanceId") Long tInstanceId,
            @PathParam("reassignmentId") Long reassignmentId) {
        Variant v = getVariant(headers);

        Header conversationIdHeader = buildConversationIdHeader(containerId, context, headers);
        try {
            userTaskAdminServiceBase.cancelReassignment(containerId, tInstanceId, reassignmentId);
            return noContent(v, conversationIdHeader);
        } catch (TaskNotFoundException e) {
            return notFound(
                    MessageFormat.format(TASK_INSTANCE_NOT_FOUND, tInstanceId), v, conversationIdHeader);
        } catch (DeploymentNotFoundException e) {
            return notFound(
                    MessageFormat.format(CONTAINER_NOT_FOUND, containerId), v, conversationIdHeader);
        } catch (Exception e) {
            logger.error("Unexpected error during processing {}", e.getMessage(), e);
            return internalServerError(MessageFormat.format(UNEXPECTED_ERROR, e.getMessage()), v, conversationIdHeader);
        }
    }

    @DELETE
    @Path(TASK_INSTANCE_NOTIFICATION_DELETE_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response cancelNotification(@javax.ws.rs.core.Context HttpHeaders headers, @PathParam("id") String containerId, @PathParam("tInstanceId") Long tInstanceId,
            @PathParam("notificationId") Long notificationId) {
        Variant v = getVariant(headers);

        Header conversationIdHeader = buildConversationIdHeader(containerId, context, headers);
        try {
            userTaskAdminServiceBase.cancelNotification(containerId, tInstanceId, notificationId);
            return noContent(v, conversationIdHeader);
        } catch (TaskNotFoundException e) {
            return notFound(
                    MessageFormat.format(TASK_INSTANCE_NOT_FOUND, tInstanceId), v, conversationIdHeader);
        } catch (DeploymentNotFoundException e) {
            return notFound(
                    MessageFormat.format(CONTAINER_NOT_FOUND, containerId), v, conversationIdHeader);
        } catch (Exception e) {
            logger.error("Unexpected error during processing {}", e.getMessage(), e);
            return internalServerError(MessageFormat.format(UNEXPECTED_ERROR, e.getMessage()), v, conversationIdHeader);
        }
    }

    @GET
    @Path(TASK_INSTANCE_REASSIGNMENTS_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getTaskReassignments(@javax.ws.rs.core.Context HttpHeaders headers, @PathParam("id") String containerId, @PathParam("tInstanceId") Long tInstanceId,
            @QueryParam("activeOnly") @DefaultValue("true") boolean activeOnly) {
        Variant v = getVariant(headers);
        Header conversationIdHeader = buildConversationIdHeader(containerId, context, headers);
        try {
            TaskReassignmentList taskReassignmentList = userTaskAdminServiceBase.getTaskReassignments(containerId, tInstanceId, activeOnly);

            return createCorrectVariant(taskReassignmentList, headers, Response.Status.OK, conversationIdHeader);
        } catch (ProcessInstanceNotFoundException e) {
            return notFound(
                    MessageFormat.format(TASK_INSTANCE_NOT_FOUND, tInstanceId), v, conversationIdHeader);
        } catch (DeploymentNotFoundException e) {
            return notFound(
                    MessageFormat.format(CONTAINER_NOT_FOUND, containerId), v, conversationIdHeader);
        } catch (Exception e) {
            logger.error("Unexpected error during processing {}", e.getMessage(), e);
            return internalServerError(MessageFormat.format(UNEXPECTED_ERROR, e.getMessage()), v, conversationIdHeader);
        }
    }

    @GET
    @Path(TASK_INSTANCE_NOTIFICATIONS_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getTaskNotifications(@javax.ws.rs.core.Context HttpHeaders headers, @PathParam("id") String containerId, @PathParam("tInstanceId") Long tInstanceId,
            @QueryParam("activeOnly") @DefaultValue("true") boolean activeOnly) {
        Variant v = getVariant(headers);
        Header conversationIdHeader = buildConversationIdHeader(containerId, context, headers);
        try {
            TaskNotificationList taskNotificationList = userTaskAdminServiceBase.getTaskNotifications(containerId, tInstanceId, activeOnly);

            return createCorrectVariant(taskNotificationList, headers, Response.Status.OK, conversationIdHeader);
        } catch (ProcessInstanceNotFoundException e) {
            return notFound(
                    MessageFormat.format(TASK_INSTANCE_NOT_FOUND, tInstanceId), v, conversationIdHeader);
        } catch (DeploymentNotFoundException e) {
            return notFound(
                    MessageFormat.format(CONTAINER_NOT_FOUND, containerId), v, conversationIdHeader);
        } catch (Exception e) {
            logger.error("Unexpected error during processing {}", e.getMessage(), e);
            return internalServerError(MessageFormat.format(UNEXPECTED_ERROR, e.getMessage()), v, conversationIdHeader);
        }
    }

    /*
     * internal methods
     */

    protected Response addToTask(HttpHeaders headers, String containerId, Long tInstanceId, boolean removeExisting, String payload, int operation) {
        Variant v = getVariant(headers);
        String type = getContentType(headers);
        Header conversationIdHeader = buildConversationIdHeader(containerId, context, headers);
        try {
            switch (operation) {
                case POT_OWNER :
                    userTaskAdminServiceBase.addPotentialOwners(containerId, tInstanceId, removeExisting, payload, type);
                    break;

                case EXL_OWNER :
                    userTaskAdminServiceBase.addExcludedOwners(containerId, tInstanceId, removeExisting, payload, type);
                    break;

                case ADMIN :
                    userTaskAdminServiceBase.addBusinessAdmins(containerId, tInstanceId, removeExisting, payload, type);
                    break;
            }


            return createResponse("", v, Response.Status.CREATED, conversationIdHeader);
        } catch (TaskNotFoundException e) {
            return notFound(
                    MessageFormat.format(TASK_INSTANCE_NOT_FOUND, tInstanceId), v, conversationIdHeader);
        } catch (DeploymentNotFoundException e) {
            return notFound(
                    MessageFormat.format(CONTAINER_NOT_FOUND, containerId), v, conversationIdHeader);
        } catch (Exception e) {
            logger.error("Unexpected error during processing {}", e.getMessage(), e);
            return internalServerError(MessageFormat.format(UNEXPECTED_ERROR, e.getMessage()), v, conversationIdHeader);
        }
    }

    protected Response removeFromTask(HttpHeaders headers, String containerId, Long tInstanceId, boolean isUser, String entities, int operation) {
        Variant v = getVariant(headers);
        String type = getContentType(headers);
        Header conversationIdHeader = buildConversationIdHeader(containerId, context, headers);
        try {

            String[] multipleEntities = entities.split(",");
            List<String> listOfEntities = Arrays.asList(multipleEntities);

            switch (operation) {
                case POT_OWNER :
                    userTaskAdminServiceBase.removePotentialOwners(containerId, tInstanceId, listOfEntities, isUser);
                    break;

                case EXL_OWNER :
                    userTaskAdminServiceBase.removeExcludedOwners(containerId, tInstanceId, listOfEntities, isUser);
                    break;

                case ADMIN :
                    userTaskAdminServiceBase.removeBusinessAdmins(containerId, tInstanceId, listOfEntities, isUser);
                    break;
            }

            return noContent(v, conversationIdHeader);
        } catch (TaskNotFoundException e) {
            return notFound(
                    MessageFormat.format(TASK_INSTANCE_NOT_FOUND, tInstanceId), v, conversationIdHeader);
        } catch (DeploymentNotFoundException e) {
            return notFound(
                    MessageFormat.format(CONTAINER_NOT_FOUND, containerId), v, conversationIdHeader);
        } catch (Exception e) {
            logger.error("Unexpected error during processing {}", e.getMessage(), e);
            return internalServerError(MessageFormat.format(UNEXPECTED_ERROR, e.getMessage()), v, conversationIdHeader);
        }
    }
}
