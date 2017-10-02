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

import static org.kie.server.api.rest.RestURI.ACK_ERRORS_PUT_URI;
import static org.kie.server.api.rest.RestURI.ACK_ERROR_PUT_URI;
import static org.kie.server.api.rest.RestURI.ADMIN_TASK_URI;
import static org.kie.server.api.rest.RestURI.ERRORS_BY_TASK_ID_GET_URI;
import static org.kie.server.api.rest.RestURI.ERRORS_GET_URI;
import static org.kie.server.api.rest.RestURI.ERROR_GET_URI;
import static org.kie.server.api.rest.RestURI.TASK_INSTANCE_ADMINS_GROUPS_DELETE_URI;
import static org.kie.server.api.rest.RestURI.TASK_INSTANCE_ADMINS_USERS_DELETE_URI;
import static org.kie.server.api.rest.RestURI.TASK_INSTANCE_ADMINS_USERS_URI;
import static org.kie.server.api.rest.RestURI.TASK_INSTANCE_EXL_OWNERS_GROUPS_DELETE_URI;
import static org.kie.server.api.rest.RestURI.TASK_INSTANCE_EXL_OWNERS_USERS_DELETE_URI;
import static org.kie.server.api.rest.RestURI.TASK_INSTANCE_EXL_OWNERS_USERS_URI;
import static org.kie.server.api.rest.RestURI.TASK_INSTANCE_INPUTS_URI;
import static org.kie.server.api.rest.RestURI.TASK_INSTANCE_NOTIFICATIONS_URI;
import static org.kie.server.api.rest.RestURI.TASK_INSTANCE_NOTIFICATION_DELETE_URI;
import static org.kie.server.api.rest.RestURI.TASK_INSTANCE_OUTPUTS_URI;
import static org.kie.server.api.rest.RestURI.TASK_INSTANCE_POT_OWNERS_GROUPS_DELETE_URI;
import static org.kie.server.api.rest.RestURI.TASK_INSTANCE_POT_OWNERS_USERS_DELETE_URI;
import static org.kie.server.api.rest.RestURI.TASK_INSTANCE_POT_OWNERS_USERS_URI;
import static org.kie.server.api.rest.RestURI.TASK_INSTANCE_REASSIGNMENTS_URI;
import static org.kie.server.api.rest.RestURI.TASK_INSTANCE_REASSIGNMENT_DELETE_URI;
import static org.kie.server.remote.rest.common.util.RestUtils.badRequest;
import static org.kie.server.remote.rest.common.util.RestUtils.buildConversationIdHeader;
import static org.kie.server.remote.rest.common.util.RestUtils.createCorrectVariant;
import static org.kie.server.remote.rest.common.util.RestUtils.createResponse;
import static org.kie.server.remote.rest.common.util.RestUtils.getContentType;
import static org.kie.server.remote.rest.common.util.RestUtils.getVariant;
import static org.kie.server.remote.rest.common.util.RestUtils.internalServerError;
import static org.kie.server.remote.rest.common.util.RestUtils.noContent;
import static org.kie.server.remote.rest.common.util.RestUtils.notFound;
import static org.kie.server.remote.rest.jbpm.resources.Messages.CONTAINER_NOT_FOUND;
import static org.kie.server.remote.rest.jbpm.resources.Messages.TASK_INSTANCE_NOT_FOUND;
import static org.kie.server.remote.rest.jbpm.resources.Messages.UNEXPECTED_ERROR;

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
import org.jbpm.services.api.admin.ExecutionErrorNotFoundException;
import org.kie.server.api.model.admin.ExecutionErrorInstance;
import org.kie.server.api.model.admin.ExecutionErrorInstanceList;
import org.kie.server.api.model.admin.TaskNotificationList;
import org.kie.server.api.model.admin.TaskReassignmentList;
import org.kie.server.remote.rest.common.Header;
import org.kie.server.services.api.KieServerRegistry;
import org.kie.server.services.jbpm.admin.UserTaskAdminServiceBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Api(value="User tasks administration :: BPM")
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

    @ApiOperation(value="Adds potential owners to given task instance, optionally removing existing ones",
            response=Void.class, code=201)
    @ApiResponses(value = { @ApiResponse(code = 500, message = "Unexpected error"),
            @ApiResponse(code = 404, message = "Task instance or Container Id not found") })
    @PUT
    @Path(TASK_INSTANCE_POT_OWNERS_USERS_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response addPotentialOwners(@javax.ws.rs.core.Context HttpHeaders headers, 
            @ApiParam(value = "container id that task instance belongs to", required = true) @PathParam("id") String containerId, 
            @ApiParam(value = "identifier of task instance to be updated", required = true) @PathParam("tInstanceId") Long tInstanceId, 
            @ApiParam(value = "optional flag that indicates if existing potential owners should be removed, defaults to false", required = false) @QueryParam("remove") @DefaultValue("false") boolean removeExisting, 
            @ApiParam(value = "list of users/groups to be added as potential owners, as OrgEntities type", required = true) String payload) {

        return addToTask(headers, containerId, tInstanceId, removeExisting, payload, POT_OWNER);
    }

    @ApiOperation(value="Adds excluded owners to given task instance, optionally removing existing ones",
            response=Void.class, code=201)
    @ApiResponses(value = { @ApiResponse(code = 500, message = "Unexpected error"),
            @ApiResponse(code = 404, message = "Task instance or Container Id not found") })
    @PUT
    @Path(TASK_INSTANCE_EXL_OWNERS_USERS_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response addExcludedOwners(@javax.ws.rs.core.Context HttpHeaders headers, 
            @ApiParam(value = "container id that task instance belongs to", required = true) @PathParam("id") String containerId, 
            @ApiParam(value = "identifier of task instance to be updated", required = true) @PathParam("tInstanceId") Long tInstanceId, 
            @ApiParam(value = "optional flag that indicates if existing excluded owners should be removed, defaults to false", required = false) @QueryParam("remove") @DefaultValue("false") boolean removeExisting, 
            @ApiParam(value = "list of users/groups to be added as excluded owners, as OrgEntities type", required = true) String payload) {

        return addToTask(headers, containerId, tInstanceId, removeExisting, payload, EXL_OWNER);
    }

    @ApiOperation(value="Adds business admins to given task instance, optionally removing existing ones",
            response=Void.class, code=201)
    @ApiResponses(value = { @ApiResponse(code = 500, message = "Unexpected error"),
            @ApiResponse(code = 404, message = "Task instance or Container Id not found") })
    @PUT
    @Path(TASK_INSTANCE_ADMINS_USERS_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response addAdmins(@javax.ws.rs.core.Context HttpHeaders headers, 
            @ApiParam(value = "container id that task instance belongs to", required = true) @PathParam("id") String containerId, 
            @ApiParam(value = "identifier of task instance to be updated", required = true) @PathParam("tInstanceId") Long tInstanceId, 
            @ApiParam(value = "optional flag that indicates if existing business admins should be removed, defaults to false", required = false) @QueryParam("remove") @DefaultValue("false") boolean removeExisting, 
            @ApiParam(value = "list of users/groups to be added as business admins, as OrgEntities type", required = true) String payload) {

        return addToTask(headers, containerId, tInstanceId, removeExisting, payload, ADMIN);
    }

    
    @ApiOperation(value="Removes potential owners from given task instance",
            response=Void.class, code=204)
    @ApiResponses(value = { @ApiResponse(code = 500, message = "Unexpected error"),
            @ApiResponse(code = 404, message = "Task instance or Container Id not found") })
    @DELETE
    @Path(TASK_INSTANCE_POT_OWNERS_USERS_DELETE_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response removePotentialOwnersUsers(@javax.ws.rs.core.Context HttpHeaders headers, 
            @ApiParam(value = "container id that task instance belongs to", required = true) @PathParam("id") String containerId, 
            @ApiParam(value = "identifier of task instance to be updated", required = true) @PathParam("tInstanceId") Long tInstanceId, 
            @ApiParam(value = "list of users to be removed from potantial owners list", required = true) @PathParam("entityId") String users) {

        return removeFromTask(headers, containerId, tInstanceId, true, users, POT_OWNER);
    }

    @ApiOperation(value="Removes excluded owners from given task instance",
            response=Void.class, code=204)
    @ApiResponses(value = { @ApiResponse(code = 500, message = "Unexpected error"),
            @ApiResponse(code = 404, message = "Task instance or Container Id not found") })
    @DELETE
    @Path(TASK_INSTANCE_EXL_OWNERS_USERS_DELETE_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response removeExcludedOwnersUsers(@javax.ws.rs.core.Context HttpHeaders headers, 
            @ApiParam(value = "container id that task instance belongs to", required = true) @PathParam("id") String containerId, 
            @ApiParam(value = "identifier of task instance to be updated", required = true) @PathParam("tInstanceId") Long tInstanceId, 
            @ApiParam(value = "list of users to be removed from excluded owners list", required = true) @PathParam("entityId") String users) {

        return removeFromTask(headers, containerId, tInstanceId, true, users, EXL_OWNER);
    }

    @ApiOperation(value="Removes business admins from given task instance",
            response=Void.class, code=204)
    @ApiResponses(value = { @ApiResponse(code = 500, message = "Unexpected error"),
            @ApiResponse(code = 404, message = "Task instance or Container Id not found") })
    @DELETE
    @Path(TASK_INSTANCE_ADMINS_USERS_DELETE_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response removeAdminsUsers(@javax.ws.rs.core.Context HttpHeaders headers, 
            @ApiParam(value = "container id that task instance belongs to", required = true) @PathParam("id") String containerId, 
            @ApiParam(value = "identifier of task instance to be updated", required = true) @PathParam("tInstanceId") Long tInstanceId, 
            @ApiParam(value = "list of users to be removed from business admin list", required = true) @PathParam("entityId") String users) {

        return removeFromTask(headers, containerId, tInstanceId, true, users, ADMIN);
    }

    @ApiOperation(value="Removes potential owner groups from given task instance",
            response=Void.class, code=204)
    @ApiResponses(value = { @ApiResponse(code = 500, message = "Unexpected error"),
            @ApiResponse(code = 404, message = "Task instance or Container Id not found") })
    @DELETE
    @Path(TASK_INSTANCE_POT_OWNERS_GROUPS_DELETE_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response removePotentialOwnersGroups(@javax.ws.rs.core.Context HttpHeaders headers, 
            @ApiParam(value = "container id that task instance belongs to", required = true) @PathParam("id") String containerId, 
            @ApiParam(value = "identifier of task instance to be updated", required = true) @PathParam("tInstanceId") Long tInstanceId, 
            @ApiParam(value = "list of groups to be removed from potantial owners list", required = true) @PathParam("entityId") String groups) {

        return removeFromTask(headers, containerId, tInstanceId, false, groups, POT_OWNER);
    }

    @ApiOperation(value="Removes excluded owners groups from given task instance",
            response=Void.class, code=204)
    @ApiResponses(value = { @ApiResponse(code = 500, message = "Unexpected error"),
            @ApiResponse(code = 404, message = "Task instance or Container Id not found") })
    @DELETE
    @Path(TASK_INSTANCE_EXL_OWNERS_GROUPS_DELETE_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response removeExcludedOwnersGroups(@javax.ws.rs.core.Context HttpHeaders headers, 
            @ApiParam(value = "container id that task instance belongs to", required = true) @PathParam("id") String containerId, 
            @ApiParam(value = "identifier of task instance to be updated", required = true) @PathParam("tInstanceId") Long tInstanceId, 
            @ApiParam(value = "list of groups to be removed from excluded owners list", required = true) @PathParam("entityId") String groups) {

        return removeFromTask(headers, containerId, tInstanceId, false, groups, EXL_OWNER);
    }

    @ApiOperation(value="Removes business admin groups from given task instance",
            response=Void.class, code=204)
    @ApiResponses(value = { @ApiResponse(code = 500, message = "Unexpected error"),
            @ApiResponse(code = 404, message = "Task instance or Container Id not found") })
    @DELETE
    @Path(TASK_INSTANCE_ADMINS_GROUPS_DELETE_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response removeAdminsGroups(@javax.ws.rs.core.Context HttpHeaders headers, 
            @ApiParam(value = "container id that task instance belongs to", required = true) @PathParam("id") String containerId, 
            @ApiParam(value = "identifier of task instance to be updated", required = true) @PathParam("tInstanceId") Long tInstanceId, 
            @ApiParam(value = "list of groups to be removed from business admin list", required = true) @PathParam("entityId") String groups) {

        return removeFromTask(headers, containerId, tInstanceId, false, groups, ADMIN);
    }

    @ApiOperation(value="Adds task inputs to given task instance",
            response=Void.class, code=201)
    @ApiResponses(value = { @ApiResponse(code = 500, message = "Unexpected error"),
            @ApiResponse(code = 404, message = "Task instance or Container Id not found") })
    @PUT
    @Path(TASK_INSTANCE_INPUTS_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response addTaskInputs(@javax.ws.rs.core.Context HttpHeaders headers, 
            @ApiParam(value = "container id that task instance belongs to", required = true) @PathParam("id") String containerId, 
            @ApiParam(value = "identifier of task instance to be updated", required = true) @PathParam("tInstanceId") Long tInstanceId, 
            @ApiParam(value = "map of data to be set as task inputs, as Map", required = true) String payload) {

        Variant v = getVariant(headers);
        String type = getContentType(headers);
        Header conversationIdHeader = buildConversationIdHeader(containerId, context, headers);
        try {
            userTaskAdminServiceBase.addTaskInputs(containerId, tInstanceId, payload, type);
            return createResponse("", v, Response.Status.CREATED, conversationIdHeader);
        } catch (TaskNotFoundException e) {
            return notFound(MessageFormat.format(TASK_INSTANCE_NOT_FOUND, tInstanceId), v, conversationIdHeader);
        } catch (DeploymentNotFoundException e) {
            return notFound(MessageFormat.format(CONTAINER_NOT_FOUND, containerId), v, conversationIdHeader);
        } catch (Exception e) {
            logger.error("Unexpected error during processing {}", e.getMessage(), e);
            return internalServerError(MessageFormat.format(UNEXPECTED_ERROR, e.getMessage()), v, conversationIdHeader);
        }
    }

    @ApiOperation(value="Removes task inputs referenced by names from given task instance",
            response=Void.class, code=204)
    @ApiResponses(value = { @ApiResponse(code = 500, message = "Unexpected error"),
            @ApiResponse(code = 404, message = "Task instance or Container Id not found") })
    @DELETE
    @Path(TASK_INSTANCE_INPUTS_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response removeTaskInputs(@javax.ws.rs.core.Context HttpHeaders headers, 
            @ApiParam(value = "container id that task instance belongs to", required = true) @PathParam("id") String containerId, 
            @ApiParam(value = "identifier of task instance to be updated", required = true) @PathParam("tInstanceId") Long tInstanceId, 
            @ApiParam(value = "one or more names of task inputs to be removed", required = true) @QueryParam("name") List<String> inputNames) {
        Variant v = getVariant(headers);

        Header conversationIdHeader = buildConversationIdHeader(containerId, context, headers);
        try {
            userTaskAdminServiceBase.removeTaskInputs(containerId, tInstanceId, inputNames);
            return noContent(v, conversationIdHeader);
        } catch (TaskNotFoundException e) {
            return notFound(MessageFormat.format(TASK_INSTANCE_NOT_FOUND, tInstanceId), v, conversationIdHeader);
        } catch (DeploymentNotFoundException e) {
            return notFound(MessageFormat.format(CONTAINER_NOT_FOUND, containerId), v, conversationIdHeader);
        } catch (Exception e) {
            logger.error("Unexpected error during processing {}", e.getMessage(), e);
            return internalServerError(MessageFormat.format(UNEXPECTED_ERROR, e.getMessage()), v, conversationIdHeader);
        }
    }

    @ApiOperation(value="Removes task outputs referenced by names from given task instance",
            response=Void.class, code=204)
    @ApiResponses(value = { @ApiResponse(code = 500, message = "Unexpected error"),
            @ApiResponse(code = 404, message = "Task instance or Container Id not found") })
    @DELETE
    @Path(TASK_INSTANCE_OUTPUTS_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response removeTaskOutputs(@javax.ws.rs.core.Context HttpHeaders headers, 
            @ApiParam(value = "container id that task instance belongs to", required = true) @PathParam("id") String containerId, 
            @ApiParam(value = "identifier of task instance to be updated", required = true) @PathParam("tInstanceId") Long tInstanceId, 
            @ApiParam(value = "one or more names of task outputs to be removed", required = true) @QueryParam("name") List<String> outputNames) {
        Variant v = getVariant(headers);

        Header conversationIdHeader = buildConversationIdHeader(containerId, context, headers);
        try {
            userTaskAdminServiceBase.removeTaskOutputs(containerId, tInstanceId, outputNames);
            return noContent(v, conversationIdHeader);
        } catch (TaskNotFoundException e) {
            return notFound(MessageFormat.format(TASK_INSTANCE_NOT_FOUND, tInstanceId), v, conversationIdHeader);
        } catch (DeploymentNotFoundException e) {
            return notFound(MessageFormat.format(CONTAINER_NOT_FOUND, containerId), v, conversationIdHeader);
        } catch (Exception e) {
            logger.error("Unexpected error during processing {}", e.getMessage(), e);
            return internalServerError(MessageFormat.format(UNEXPECTED_ERROR, e.getMessage()), v, conversationIdHeader);
        }
    }

    @ApiOperation(value="Schedules new reassign of given task instance",
            response=Long.class, code=201)
    @ApiResponses(value = { @ApiResponse(code = 500, message = "Unexpected error"),
            @ApiResponse(code = 404, message = "Task instance or Container Id not found") })
    @POST
    @Path(TASK_INSTANCE_REASSIGNMENTS_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response reassign(@javax.ws.rs.core.Context HttpHeaders headers, 
            @ApiParam(value = "container id that task instance belongs to", required = true) @PathParam("id") String containerId, 
            @ApiParam(value = "identifier of task instance to be updated", required = true) @PathParam("tInstanceId") Long tInstanceId, 
            @ApiParam(value = "time expression for reassignmnet", required = true) @QueryParam("expiresAt") String expiresAt, 
            @ApiParam(value = "optional flag that indicates the type of reassignment, either whenNotStarted or whenNotCompleted must be set", required = false) @QueryParam("whenNotStarted") @DefaultValue("false") boolean whenNotStarted, 
            @ApiParam(value = "optional flag that indicates the type of reassignment, either whenNotStarted or whenNotCompleted must be set", required = false) @QueryParam("whenNotCompleted") @DefaultValue("false") boolean whenNotCompleted, 
            @ApiParam(value = "list of users/groups that task should be reassined to, as OrgEntities type", required = true) String payload) {
        Variant v = getVariant(headers);
        String type = getContentType(headers);
        Header conversationIdHeader = buildConversationIdHeader(containerId, context, headers);

        if (expiresAt == null) {
            return badRequest("'expiresAt' query parameter is mandatory", v, conversationIdHeader);
        }

        try {
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
            return notFound(MessageFormat.format(TASK_INSTANCE_NOT_FOUND, tInstanceId), v, conversationIdHeader);
        } catch (DeploymentNotFoundException e) {
            return notFound(MessageFormat.format(CONTAINER_NOT_FOUND, containerId), v, conversationIdHeader);
        } catch (RuntimeException e) {
            return badRequest(e.getMessage(), v, conversationIdHeader);
        } catch (Exception e) {
            logger.error("Unexpected error during processing {}", e.getMessage(), e);
            return internalServerError(MessageFormat.format(UNEXPECTED_ERROR, e.getMessage()), v, conversationIdHeader);
        }
    }

    @ApiOperation(value="Schedules new notification for given task instance",
            response=Long.class, code=201)
    @ApiResponses(value = { @ApiResponse(code = 500, message = "Unexpected error"),
            @ApiResponse(code = 404, message = "Task instance or Container Id not found") })
    @POST
    @Path(TASK_INSTANCE_NOTIFICATIONS_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response notify(@javax.ws.rs.core.Context HttpHeaders headers, 
            @ApiParam(value = "container id that task instance belongs to", required = true) @PathParam("id") String containerId, 
            @ApiParam(value = "identifier of task instance to be updated", required = true) @PathParam("tInstanceId") Long tInstanceId, 
            @ApiParam(value = "time expression for notification", required = true) @QueryParam("expiresAt") String expiresAt, 
            @ApiParam(value = "optional flag that indicates the type of notification, either whenNotStarted or whenNotCompleted must be set", required = false) @QueryParam("whenNotStarted") @DefaultValue("false") boolean whenNotStarted, 
            @ApiParam(value = "optional flag that indicates the type of notification, either whenNotStarted or whenNotCompleted must be set", required = false) @QueryParam("whenNotCompleted") @DefaultValue("false") boolean whenNotCompleted, 
            @ApiParam(value = "email notification details, as EmailNotification type", required = true) String payload) {
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
            return notFound(MessageFormat.format(TASK_INSTANCE_NOT_FOUND, tInstanceId), v, conversationIdHeader);
        } catch (DeploymentNotFoundException e) {
            return notFound(MessageFormat.format(CONTAINER_NOT_FOUND, containerId), v, conversationIdHeader);
        } catch (Exception e) {
            logger.error("Unexpected error during processing {}", e.getMessage(), e);
            return internalServerError(MessageFormat.format(UNEXPECTED_ERROR, e.getMessage()), v, conversationIdHeader);
        }
    }

    @ApiOperation(value="Cancels reassignment for given task instance",
            response=Void.class, code=204)
    @ApiResponses(value = { @ApiResponse(code = 500, message = "Unexpected error"),
            @ApiResponse(code = 404, message = "Task instance or Container Id not found") })
    @DELETE
    @Path(TASK_INSTANCE_REASSIGNMENT_DELETE_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response cancelReassignment(@javax.ws.rs.core.Context HttpHeaders headers, 
            @ApiParam(value = "container id that task instance belongs to", required = true) @PathParam("id") String containerId, 
            @ApiParam(value = "identifier of task instance to be updated", required = true) @PathParam("tInstanceId") Long tInstanceId, 
            @ApiParam(value = "identifier of reassignment to be canceled", required = true) @PathParam("reassignmentId") Long reassignmentId) {
        Variant v = getVariant(headers);

        Header conversationIdHeader = buildConversationIdHeader(containerId, context, headers);
        try {
            userTaskAdminServiceBase.cancelReassignment(containerId, tInstanceId, reassignmentId);
            return noContent(v, conversationIdHeader);
        } catch (TaskNotFoundException e) {
            return notFound(MessageFormat.format(TASK_INSTANCE_NOT_FOUND, tInstanceId), v, conversationIdHeader);
        } catch (DeploymentNotFoundException e) {
            return notFound(MessageFormat.format(CONTAINER_NOT_FOUND, containerId), v, conversationIdHeader);
        } catch (Exception e) {
            logger.error("Unexpected error during processing {}", e.getMessage(), e);
            return internalServerError(MessageFormat.format(UNEXPECTED_ERROR, e.getMessage()), v, conversationIdHeader);
        }
    }

    @ApiOperation(value="Cancels notification for given task instance",
            response=Void.class, code=204)
    @ApiResponses(value = { @ApiResponse(code = 500, message = "Unexpected error"),
            @ApiResponse(code = 404, message = "Task instance or Container Id not found") })
    @DELETE
    @Path(TASK_INSTANCE_NOTIFICATION_DELETE_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response cancelNotification(@javax.ws.rs.core.Context HttpHeaders headers, 
            @ApiParam(value = "container id that task instance belongs to", required = true) @PathParam("id") String containerId, 
            @ApiParam(value = "identifier of task instance to be updated", required = true) @PathParam("tInstanceId") Long tInstanceId, 
            @ApiParam(value = "identifier of notification to be canceled", required = true) @PathParam("notificationId") Long notificationId) {
        Variant v = getVariant(headers);

        Header conversationIdHeader = buildConversationIdHeader(containerId, context, headers);
        try {
            userTaskAdminServiceBase.cancelNotification(containerId, tInstanceId, notificationId);
            return noContent(v, conversationIdHeader);
        } catch (TaskNotFoundException e) {
            return notFound(MessageFormat.format(TASK_INSTANCE_NOT_FOUND, tInstanceId), v, conversationIdHeader);
        } catch (DeploymentNotFoundException e) {
            return notFound(MessageFormat.format(CONTAINER_NOT_FOUND, containerId), v, conversationIdHeader);
        } catch (Exception e) {
            logger.error("Unexpected error during processing {}", e.getMessage(), e);
            return internalServerError(MessageFormat.format(UNEXPECTED_ERROR, e.getMessage()), v, conversationIdHeader);
        }
    }

    @ApiOperation(value="Retrieves reassignments for given task",
            response=TaskReassignmentList.class, code=200)
    @ApiResponses(value = { @ApiResponse(code = 500, message = "Unexpected error"),
            @ApiResponse(code = 404, message = "Task instance or Container Id not found") })
    @GET
    @Path(TASK_INSTANCE_REASSIGNMENTS_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getTaskReassignments(@javax.ws.rs.core.Context HttpHeaders headers, 
            @ApiParam(value = "container id that task instance belongs to", required = true) @PathParam("id") String containerId, 
            @ApiParam(value = "identifier of task instance to be updated", required = true) @PathParam("tInstanceId") Long tInstanceId, 
            @ApiParam(value = "optional flag that indicates if active only reassignmnets should be collected, defaults to true", required = false) @QueryParam("activeOnly") @DefaultValue("true") boolean activeOnly) {
        Variant v = getVariant(headers);
        Header conversationIdHeader = buildConversationIdHeader(containerId, context, headers);
        try {
            TaskReassignmentList taskReassignmentList = userTaskAdminServiceBase.getTaskReassignments(containerId, tInstanceId, activeOnly);

            return createCorrectVariant(taskReassignmentList, headers, Response.Status.OK, conversationIdHeader);
        } catch (ProcessInstanceNotFoundException e) {
            return notFound(MessageFormat.format(TASK_INSTANCE_NOT_FOUND, tInstanceId), v, conversationIdHeader);
        } catch (DeploymentNotFoundException e) {
            return notFound(MessageFormat.format(CONTAINER_NOT_FOUND, containerId), v, conversationIdHeader);
        } catch (Exception e) {
            logger.error("Unexpected error during processing {}", e.getMessage(), e);
            return internalServerError(MessageFormat.format(UNEXPECTED_ERROR, e.getMessage()), v, conversationIdHeader);
        }
    }

    @ApiOperation(value="Retrieves notifications for given task",
            response=TaskNotificationList.class, code=200)
    @ApiResponses(value = { @ApiResponse(code = 500, message = "Unexpected error"),
            @ApiResponse(code = 404, message = "Task instance or Container Id not found") })
    @GET
    @Path(TASK_INSTANCE_NOTIFICATIONS_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getTaskNotifications(@javax.ws.rs.core.Context HttpHeaders headers, 
            @ApiParam(value = "container id that task instance belongs to", required = true) @PathParam("id") String containerId, 
            @ApiParam(value = "identifier of task instance to be updated", required = true) @PathParam("tInstanceId") Long tInstanceId, 
            @ApiParam(value = "optional flag that indicates if active only notifications should be collected, defaults to true", required = false) @QueryParam("activeOnly") @DefaultValue("true") boolean activeOnly) {
        Variant v = getVariant(headers);
        Header conversationIdHeader = buildConversationIdHeader(containerId, context, headers);
        try {
            TaskNotificationList taskNotificationList = userTaskAdminServiceBase.getTaskNotifications(containerId, tInstanceId, activeOnly);

            return createCorrectVariant(taskNotificationList, headers, Response.Status.OK, conversationIdHeader);
        } catch (ProcessInstanceNotFoundException e) {
            return notFound(MessageFormat.format(TASK_INSTANCE_NOT_FOUND, tInstanceId), v, conversationIdHeader);
        } catch (DeploymentNotFoundException e) {
            return notFound(MessageFormat.format(CONTAINER_NOT_FOUND, containerId), v, conversationIdHeader);
        } catch (Exception e) {
            logger.error("Unexpected error during processing {}", e.getMessage(), e);
            return internalServerError(MessageFormat.format(UNEXPECTED_ERROR, e.getMessage()), v, conversationIdHeader);
        }
    }

    @ApiOperation(value="Acknowledges given execution error",
            response=Void.class, code=201)
    @ApiResponses(value = { @ApiResponse(code = 500, message = "Unexpected error"),
            @ApiResponse(code = 404, message = "Task instance or Container Id not found") })
    @PUT
    @Path(ACK_ERROR_PUT_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response acknowledgeError(@javax.ws.rs.core.Context HttpHeaders headers, 
            @ApiParam(value = "container id that error belongs to", required = true) @PathParam("id") String containerId, 
            @ApiParam(value = "identifier of the execution error to be acknowledged", required = true) @PathParam("errorId") String errorId) {
        Variant v = getVariant(headers);
        Header conversationIdHeader = buildConversationIdHeader(containerId, context, headers);
        try {

            userTaskAdminServiceBase.acknowledgeError(Arrays.asList(errorId));
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

    @ApiOperation(value="Acknowledges given execution errors",
            response=Void.class, code=201)
    @ApiResponses(value = { @ApiResponse(code = 500, message = "Unexpected error"),
            @ApiResponse(code = 404, message = "Task instance or Container Id not found") })
    @PUT
    @Path(ACK_ERRORS_PUT_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response acknowledgeErrors(@javax.ws.rs.core.Context HttpHeaders headers, 
            @ApiParam(value = "container id that errors belong to", required = true) @PathParam("id") String containerId, 
            @ApiParam(value = "list of identifiers of execution errors to be acknowledged", required = true) @QueryParam("errorId") List<String> errorIds) {
        Variant v = getVariant(headers);
        Header conversationIdHeader = buildConversationIdHeader(containerId, context, headers);
        try {

            userTaskAdminServiceBase.acknowledgeError(errorIds);
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

    @ApiOperation(value="Retrieve execution error by its identifier",
            response=ExecutionErrorInstance.class, code=200)
    @ApiResponses(value = { @ApiResponse(code = 500, message = "Unexpected error"),
            @ApiResponse(code = 404, message = "Task instance or Container Id not found") })
    @GET
    @Path(ERROR_GET_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getExecutionErrorById(@javax.ws.rs.core.Context HttpHeaders headers, 
            @ApiParam(value = "container id that error belongs to", required = true) @PathParam("id") String containerId, 
            @ApiParam(value = "identifier of the execution error to load", required = true) @PathParam("errorId") String errorId) {
        Variant v = getVariant(headers);
        Header conversationIdHeader = buildConversationIdHeader(containerId, context, headers);
        try {
            ExecutionErrorInstance executionErrorInstance = userTaskAdminServiceBase.getError(errorId);

            return createCorrectVariant(executionErrorInstance, headers, Response.Status.OK, conversationIdHeader);
        } catch (ExecutionErrorNotFoundException e) {
            return notFound(e.getMessage(), v, conversationIdHeader);
        } catch (DeploymentNotFoundException e) {
            return notFound(MessageFormat.format(CONTAINER_NOT_FOUND, containerId), v, conversationIdHeader);
        } catch (Exception e) {
            logger.error("Unexpected error during processing {}", e.getMessage(), e);
            return internalServerError(MessageFormat.format(UNEXPECTED_ERROR, e.getMessage()), v, conversationIdHeader);
        }
    }

    @ApiOperation(value="Retrieves execution errors for task instance and container, applies pagination",
            response=ExecutionErrorInstanceList.class, code=200)
    @ApiResponses(value = { @ApiResponse(code = 500, message = "Unexpected error"),
            @ApiResponse(code = 404, message = "Container Id not found") })
    @GET
    @Path(ERRORS_BY_TASK_ID_GET_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getExecutionErrorsByTask(@javax.ws.rs.core.Context HttpHeaders headers, 
            @ApiParam(value = "container id that task instance belongs to", required = true) @PathParam("id") String containerId, 
            @ApiParam(value = "identifier of the task instance that errors should be collected for", required = true) @PathParam("tInstanceId") Long taskId, 
            @ApiParam(value = "optional flag that indicates if acknowledged errors should also be collected, defaults to false", required = false) @QueryParam("includeAck") @DefaultValue("false") boolean includeAcknowledged, 
            @ApiParam(value = "optional pagination - at which page to start, defaults to 0 (meaning first)", required = false) @QueryParam("page") @DefaultValue("0") Integer page, 
            @ApiParam(value = "optional pagination - size of the result, defaults to 10", required = false) @QueryParam("pageSize") @DefaultValue("10") Integer pageSize, 
            @ApiParam(value = "optional sort column, no default", required = false) @QueryParam("sort") String sort, 
            @ApiParam(value = "optional sort direction (asc, desc) - defaults to asc", required = false) @QueryParam("sortOrder") @DefaultValue("true") boolean sortOrder) {
        Variant v = getVariant(headers);
        Header conversationIdHeader = buildConversationIdHeader(containerId, context, headers);
        try {
            ExecutionErrorInstanceList executionErrorInstanceList = userTaskAdminServiceBase.getExecutionErrorsByTaskId(containerId, taskId, includeAcknowledged, page, pageSize, sort, sortOrder);

            return createCorrectVariant(executionErrorInstanceList, headers, Response.Status.OK, conversationIdHeader);
        } catch (ExecutionErrorNotFoundException e) {
            return notFound(e.getMessage(), v, conversationIdHeader);
        } catch (DeploymentNotFoundException e) {
            return notFound(MessageFormat.format(CONTAINER_NOT_FOUND, containerId), v, conversationIdHeader);
        } catch (Exception e) {
            logger.error("Unexpected error during processing {}", e.getMessage(), e);
            return internalServerError(MessageFormat.format(UNEXPECTED_ERROR, e.getMessage()), v, conversationIdHeader);
        }
    }

    @ApiOperation(value="Retrieves execution errors for container, allows to filter by task name and/or process id, applies pagination",
            response=ExecutionErrorInstanceList.class, code=200)
    @ApiResponses(value = { @ApiResponse(code = 500, message = "Unexpected error"),
            @ApiResponse(code = 404, message = "Container Id not found") })
    @GET
    @Path(ERRORS_GET_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getExecutionErrors(@javax.ws.rs.core.Context HttpHeaders headers, 
            @ApiParam(value = "container id that task instance belongs to", required = true) @PathParam("id") String containerId, 
            @ApiParam(value = "optional flag that indicates if acknowledged errors should also be collected, defaults to false", required = false) @QueryParam("includeAck") @DefaultValue("false") boolean includeAcknowledged, 
            @ApiParam(value = "optional name of the task to filter by", required = false) @QueryParam("name") String taskName, 
            @ApiParam(value = "optional process id that the task belongs to to filter by", required = false) @QueryParam("process") String processId, 
            @ApiParam(value = "optional pagination - at which page to start, defaults to 0 (meaning first)", required = false) @QueryParam("page") @DefaultValue("0") Integer page, 
            @ApiParam(value = "optional pagination - size of the result, defaults to 10", required = false) @QueryParam("pageSize") @DefaultValue("10") Integer pageSize, 
            @ApiParam(value = "optional sort column, no default", required = false) @QueryParam("sort") String sort, 
            @ApiParam(value = "optional sort direction (asc, desc) - defaults to asc", required = false) @QueryParam("sortOrder") @DefaultValue("true") boolean sortOrder) {
        Variant v = getVariant(headers);
        Header conversationIdHeader = buildConversationIdHeader(containerId, context, headers);
        try {
            ExecutionErrorInstanceList executionErrorInstanceList = userTaskAdminServiceBase.getExecutionErrorsByTaskName(containerId, processId, taskName, includeAcknowledged, page, pageSize, sort, sortOrder);

            return createCorrectVariant(executionErrorInstanceList, headers, Response.Status.OK, conversationIdHeader);
        } catch (ExecutionErrorNotFoundException e) {
            return notFound(e.getMessage(), v, conversationIdHeader);
        } catch (DeploymentNotFoundException e) {
            return notFound(MessageFormat.format(CONTAINER_NOT_FOUND, containerId), v, conversationIdHeader);
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
                case POT_OWNER:
                    userTaskAdminServiceBase.addPotentialOwners(containerId, tInstanceId, removeExisting, payload, type);
                    break;

                case EXL_OWNER:
                    userTaskAdminServiceBase.addExcludedOwners(containerId, tInstanceId, removeExisting, payload, type);
                    break;

                case ADMIN:
                    userTaskAdminServiceBase.addBusinessAdmins(containerId, tInstanceId, removeExisting, payload, type);
                    break;
            }

            return createResponse("", v, Response.Status.CREATED, conversationIdHeader);
        } catch (TaskNotFoundException e) {
            return notFound(MessageFormat.format(TASK_INSTANCE_NOT_FOUND, tInstanceId), v, conversationIdHeader);
        } catch (DeploymentNotFoundException e) {
            return notFound(MessageFormat.format(CONTAINER_NOT_FOUND, containerId), v, conversationIdHeader);
        } catch (Exception e) {
            logger.error("Unexpected error during processing {}", e.getMessage(), e);
            return internalServerError(MessageFormat.format(UNEXPECTED_ERROR, e.getMessage()), v, conversationIdHeader);
        }
    }

    protected Response removeFromTask(HttpHeaders headers, String containerId, Long tInstanceId, boolean isUser, String entities, int operation) {
        Variant v = getVariant(headers);
        Header conversationIdHeader = buildConversationIdHeader(containerId, context, headers);
        try {

            String[] multipleEntities = entities.split(",");
            List<String> listOfEntities = Arrays.asList(multipleEntities);

            switch (operation) {
                case POT_OWNER:
                    userTaskAdminServiceBase.removePotentialOwners(containerId, tInstanceId, listOfEntities, isUser);
                    break;

                case EXL_OWNER:
                    userTaskAdminServiceBase.removeExcludedOwners(containerId, tInstanceId, listOfEntities, isUser);
                    break;

                case ADMIN:
                    userTaskAdminServiceBase.removeBusinessAdmins(containerId, tInstanceId, listOfEntities, isUser);
                    break;
            }

            return noContent(v, conversationIdHeader);
        } catch (TaskNotFoundException e) {
            return notFound(MessageFormat.format(TASK_INSTANCE_NOT_FOUND, tInstanceId), v, conversationIdHeader);
        } catch (DeploymentNotFoundException e) {
            return notFound(MessageFormat.format(CONTAINER_NOT_FOUND, containerId), v, conversationIdHeader);
        } catch (Exception e) {
            logger.error("Unexpected error during processing {}", e.getMessage(), e);
            return internalServerError(MessageFormat.format(UNEXPECTED_ERROR, e.getMessage()), v, conversationIdHeader);
        }
    }
}
