/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
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

package org.kie.server.remote.rest.casemgmt;

import java.util.ArrayList;
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

import org.kie.api.runtime.process.ProcessInstance;
import org.kie.server.api.model.cases.CaseAdHocFragmentList;
import org.kie.server.api.model.cases.CaseCommentList;
import org.kie.server.api.model.cases.CaseDefinition;
import org.kie.server.api.model.cases.CaseDefinitionList;
import org.kie.server.api.model.cases.CaseInstanceList;
import org.kie.server.api.model.cases.CaseMilestoneList;
import org.kie.server.api.model.cases.CaseRoleAssignmentList;
import org.kie.server.api.model.cases.CaseStageList;
import org.kie.server.api.model.instance.NodeInstanceList;
import org.kie.server.api.model.instance.ProcessInstanceList;
import org.kie.server.remote.rest.common.Header;
import org.kie.server.services.api.KieServerRegistry;
import org.kie.server.services.casemgmt.CaseManagementRuntimeDataServiceBase;
import org.kie.server.services.casemgmt.CaseManagementServiceBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.kie.server.api.rest.RestURI.*;
import static org.kie.server.remote.rest.common.util.RestUtils.*;

@Path("server/" + CASE_URI)
public class CaseResource extends AbstractCaseResource {

    private static final Logger logger = LoggerFactory.getLogger(CaseResource.class);

    private CaseManagementServiceBase caseManagementServiceBase;

    public CaseResource() {

    }

    public CaseResource(final CaseManagementServiceBase caseManagementServiceBase,
                        final CaseManagementRuntimeDataServiceBase caseManagementRuntimeDataServiceBase,
                        final KieServerRegistry context) {
        super(caseManagementRuntimeDataServiceBase, context);
        this.caseManagementServiceBase = caseManagementServiceBase;
    }

    @POST
    @Path(START_CASE_POST_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response startCase(@javax.ws.rs.core.Context HttpHeaders headers,
            @PathParam(CONTAINER_ID) String containerId, @PathParam(CASE_DEF_ID) String caseDefId, String payload) {

        return invokeCaseOperation(headers,
                containerId,
                null,
                (Variant v, String type, Header... customHeaders) -> {
                    String response = caseManagementServiceBase.startCase(containerId, caseDefId, payload, type);
                    logger.debug("Returning CREATED response for start case with content '{}'", response);

                    return createResponse(response, v, Response.Status.CREATED, customHeaders);
                });
    }

    @GET
    @Path(CASE_INSTANCE_GET_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getCaseInstance(@javax.ws.rs.core.Context HttpHeaders headers,
            @PathParam(CONTAINER_ID) String containerId, @PathParam(CASE_ID) String caseId,
            @QueryParam("withData") @DefaultValue("false") boolean withData,
            @QueryParam("withRoles") @DefaultValue("false") boolean withRoles,
            @QueryParam("withMilestones") @DefaultValue("false") boolean withMilestones,
            @QueryParam("withStages") @DefaultValue("false") boolean withStages) {

        return invokeCaseOperation(headers,
                containerId,
                caseId,
                (Variant v, String type, Header... customHeaders) -> {
                    String response = caseManagementServiceBase.getCaseInstance(containerId, caseId, withData, withRoles, withMilestones, withStages, type);
                    logger.debug("Returning OK response for get case instance with content '{}'", response);

                    return createResponse(response, v, Response.Status.OK, customHeaders);
                });
    }

    @DELETE
    @Path(CASE_INSTANCE_DELETE_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response cancelCaseInstance(@javax.ws.rs.core.Context HttpHeaders headers,
            @PathParam(CONTAINER_ID) String containerId, @PathParam(CASE_ID) String caseId,
            @QueryParam("destroy") @DefaultValue("false") boolean destroy) {
        return invokeCaseOperation(headers,
                containerId,
                caseId,
                (Variant v, String type, Header... customHeaders) -> {
                    caseManagementServiceBase.cancelCaseInstance(containerId, caseId, destroy);
                    logger.debug("Returning NO CONTENT response after cancelling a case with id {}", caseId);

                    return noContent(v, customHeaders);
                });
    }

    @PUT
    @Path(REOPEN_CASE_PUT_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response reopenCase(@javax.ws.rs.core.Context HttpHeaders headers,
            @PathParam(CONTAINER_ID) String containerId, @PathParam(CASE_DEF_ID) String caseDefId, @PathParam(CASE_ID) String caseId, String payload) {

        return invokeCaseOperation(headers,
                containerId,
                null,
                (Variant v, String type, Header... customHeaders) -> {
                    caseManagementServiceBase.reopenCase(caseId, containerId, caseDefId, payload, type);
                    logger.debug("Returning CREATED response for reopen case {}", caseId);

                    return createResponse("", v, Response.Status.CREATED, customHeaders);
                });
    }

    @GET
    @Path(CASE_FILE_GET_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getCaseInstanceData(@javax.ws.rs.core.Context HttpHeaders headers,
            @PathParam(CONTAINER_ID) String containerId, @PathParam(CASE_ID) String caseId) {
        return invokeCaseOperation(headers,
                containerId,
                caseId,
                (Variant v, String type, Header... customHeaders) -> {
                    logger.debug("About to load case file data of case {}", caseId);
                    String response = this.caseManagementServiceBase.getCaseFileData(containerId, caseId, type);

                    logger.debug("Returning OK response with content '{}'", response);
                    return createResponse(response, v, Response.Status.OK, customHeaders);
                });
    }

    @GET
    @Path(CASE_FILE_BY_NAME_GET_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getCaseInstanceDataByName(@javax.ws.rs.core.Context HttpHeaders headers,
            @PathParam(CONTAINER_ID) String containerId, @PathParam(CASE_ID) String caseId, @PathParam(CASE_FILE_ITEM) String caseDataName) {
        return invokeCaseOperation(headers,
                containerId,
                caseId,
                (Variant v, String type, Header... customHeaders) -> {
                    logger.debug("About to load case file data of case {}", caseId);
                    String response = this.caseManagementServiceBase.getCaseFileDataByName(containerId, caseId, caseDataName, type);

                    logger.debug("Returning OK response with content '{}'", response);
                    return createResponse(response, v, Response.Status.OK, customHeaders);
                });
    }

    @POST
    @Path(CASE_FILE_POST_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response putCaseInstanceData(@javax.ws.rs.core.Context HttpHeaders headers,
            @PathParam(CONTAINER_ID) String containerId, @PathParam(CASE_ID) String caseId,
            String payload) {

        return invokeCaseOperation(headers,
                containerId,
                caseId,
                (Variant v, String type, Header... customHeaders) -> {
                    logger.debug("About to put case file data of case {}", caseId);
                    this.caseManagementServiceBase.putCaseFileData(containerId, caseId, payload, type);

                    logger.debug("Returning OK response");
                    return createResponse("", v, Response.Status.OK, customHeaders);
                });

    }

    @POST
    @Path(CASE_FILE_BY_NAME_POST_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response putCaseInstanceDataByName(@javax.ws.rs.core.Context HttpHeaders headers,
            @PathParam(CONTAINER_ID) String containerId, @PathParam(CASE_ID) String caseId, @PathParam(CASE_FILE_ITEM) String caseDataName,
            String payload) {

        return invokeCaseOperation(headers,
                containerId,
                caseId,
                (Variant v, String type, Header... customHeaders) -> {
                    logger.debug("About to put case file data of case {}", caseId);
                    this.caseManagementServiceBase.putCaseFileDataByName(containerId, caseId, caseDataName, payload, type);

                    logger.debug("Returning OK response");
                    return createResponse("", v, Response.Status.OK, customHeaders);
                });
    }

    @DELETE
    @Path(CASE_FILE_DELETE_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response deleteCaseInstanceData(@javax.ws.rs.core.Context HttpHeaders headers,
            @PathParam(CONTAINER_ID) String containerId, @PathParam(CASE_ID) String caseId, @QueryParam(CASE_FILE_ITEM) List<String> variableNames) {

        return invokeCaseOperation(headers,
                containerId,
                caseId,
                (Variant v, String type, Header... customHeaders) -> {
                    if (variableNames == null || variableNames.isEmpty()) {
                        return notFound("Variable names to remove from case file not provided", v, customHeaders);
                    }

                    logger.debug("About to remove case file data of case {}", caseId);
                    this.caseManagementServiceBase.removeCaseFileDataByName(containerId, caseId, variableNames);

                    logger.debug("Returning NO_CONTENT response");
                    return noContent(v, customHeaders);
                });
    }

    @POST
    @Path(CASE_DYNAMIC_TASK_POST_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response addDynamicTaskToCase(@javax.ws.rs.core.Context HttpHeaders headers,
            @PathParam(CONTAINER_ID) String containerId, @PathParam(CASE_ID) String caseId,
            String payload) {
        return invokeCaseOperation(headers,
                containerId,
                caseId,
                (Variant v, String type, Header... customHeaders) -> {
            logger.debug("About to add dynamic task to case {}", caseId);
            this.caseManagementServiceBase.addDynamicTask(containerId, caseId, null, payload, type);

            logger.debug("Returning CREATED response");
            return createResponse("", v, Response.Status.CREATED, customHeaders);
        });
    }

    @POST
    @Path(CASE_DYNAMIC_TASK_IN_STAGE_POST_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response addDynamicTaskToCase(@javax.ws.rs.core.Context HttpHeaders headers,
            @PathParam(CONTAINER_ID) String containerId, @PathParam(CASE_ID) String caseId, @PathParam(CASE_STAGE_ID) String stageId,
            String payload) {
        return invokeCaseOperation(headers,
                containerId,
                caseId,
                (Variant v, String type, Header... customHeaders) -> {
                    logger.debug("About to add dynamic task stage {} in case {}", stageId, caseId);
                    this.caseManagementServiceBase.addDynamicTask(containerId, caseId, stageId, payload, type);

                    logger.debug("Returning CREATED response");
                    return createResponse("", v, Response.Status.CREATED, customHeaders);
                });
    }

    @POST
    @Path(CASE_DYNAMIC_PROCESS_POST_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response addDynamicProcessToCase(@javax.ws.rs.core.Context HttpHeaders headers,
            @PathParam(CONTAINER_ID) String containerId, @PathParam(CASE_ID) String caseId,
            @PathParam(PROCESS_ID) String processId,
            String payload) {
        return invokeCaseOperation(headers,
                containerId,
                caseId,
                (Variant v, String type, Header... customHeaders) -> {
                    logger.debug("About to add dynamic subprocess {} in case {}", processId, caseId);
                    this.caseManagementServiceBase.addDynamicSubprocess(containerId, caseId, null, processId, payload, type);

                    logger.debug("Returning CREATED response");
                    return createResponse("", v, Response.Status.CREATED, customHeaders);
                });
    }

    @POST
    @Path(CASE_DYNAMIC_PROCESS_IN_STAGE_POST_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response addDynamicProcessToCase(@javax.ws.rs.core.Context HttpHeaders headers,
            @PathParam(CONTAINER_ID) String containerId, @PathParam(CASE_ID) String caseId,
            @PathParam(CASE_STAGE_ID) String stageId, @PathParam(PROCESS_ID) String processId,
            String payload) {
        return invokeCaseOperation(headers,
                containerId,
                caseId,
                (Variant v, String type, Header... customHeaders) -> {
                    logger.debug("About to add dynamic subprocess stage {} in case {}", stageId, caseId);
                    this.caseManagementServiceBase.addDynamicSubprocess(containerId, caseId, stageId, processId, payload, type);

                    logger.debug("Returning CREATED response");
                    return createResponse("", v, Response.Status.CREATED, customHeaders);
                });
    }

    @PUT
    @Path(CASE_DYNAMIC_TASK_IN_STAGE_PUT_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response triggerAdHocNodeInStage(@javax.ws.rs.core.Context HttpHeaders headers,
            @PathParam(CONTAINER_ID) String containerId, @PathParam(CASE_ID) String caseId,
            @PathParam(CASE_STAGE_ID) String stageId, @PathParam(CASE_NODE_NAME) String adHocName,
            String payload) {
        return invokeCaseOperation(headers,
                containerId,
                caseId,
                (Variant v, String type, Header... customHeaders) -> {
                    logger.debug("About to trigger ad hoc task {} in stage {} and in case {}", adHocName, stageId, caseId);
                    this.caseManagementServiceBase.triggerAdHocNode(containerId, caseId, stageId, adHocName, payload, type);

                    logger.debug("Returning CREATED response");
                    return createResponse("", v, Response.Status.CREATED, customHeaders);
                });
    }

    @PUT
    @Path(CASE_DYNAMIC_TASK_PUT_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response triggerAdHocNode(@javax.ws.rs.core.Context HttpHeaders headers,
            @PathParam(CONTAINER_ID) String containerId, @PathParam(CASE_ID) String caseId,
            @PathParam(CASE_NODE_NAME) String adHocName,
            String payload) {
        return invokeCaseOperation(headers,
                containerId,
                caseId,
                (Variant v, String type, Header... customHeaders) -> {
                    logger.debug("About to trigger ad hoc task {} in case {}", adHocName, caseId);
                    this.caseManagementServiceBase.triggerAdHocNode(containerId, caseId, null, adHocName, payload, type);

                    logger.debug("Returning CREATED response");
                    return createResponse("", v, Response.Status.CREATED, customHeaders);
                });
    }

    @GET
    @Path(CASE_MILESTONES_GET_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getCaseInstanceMilestones(@javax.ws.rs.core.Context HttpHeaders headers,
            @PathParam(CONTAINER_ID) String containerId, @PathParam(CASE_ID) String caseId,
            @QueryParam("achievedOnly") @DefaultValue("true") boolean achievedOnly,
            @QueryParam("page") @DefaultValue("0") Integer page, @QueryParam("pageSize") @DefaultValue("10") Integer pageSize) {

        return invokeCaseOperation(headers,
                containerId,
                caseId,
                (Variant v, String type, Header... customHeaders) -> {
                    logger.debug("About to look for milestones in case {} achieved only = {}", caseId, achievedOnly);
                    CaseMilestoneList responseObject = this.caseManagementRuntimeDataServiceBase.getMilestones(containerId, caseId, achievedOnly, page, pageSize);

                    logger.debug("Returning OK response with content '{}'", responseObject);
                    return createCorrectVariant(responseObject, headers, Response.Status.OK, customHeaders);
                });
    }

    @GET
    @Path(CASE_STAGES_GET_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getCaseInstanceStages(@javax.ws.rs.core.Context HttpHeaders headers,
            @PathParam(CONTAINER_ID) String containerId, @PathParam(CASE_ID) String caseId,
            @QueryParam("activeOnly") @DefaultValue("true") boolean activeOnly,
            @QueryParam("page") @DefaultValue("0") Integer page, @QueryParam("pageSize") @DefaultValue("10") Integer pageSize) {

        return invokeCaseOperation(headers,
                containerId,
                caseId,
                (Variant v, String type, Header... customHeaders) -> {
                    logger.debug("About to look for stages in case {} active only = {}", caseId, activeOnly);
                    CaseStageList responseObject = this.caseManagementRuntimeDataServiceBase.getStages(containerId, caseId, activeOnly, page, pageSize);

                    logger.debug("Returning OK response with content '{}'", responseObject);
                    return createCorrectVariant(responseObject, headers, Response.Status.OK, customHeaders);
                });
    }

    @GET
    @Path(CASE_AD_HOC_FRAGMENTS_GET_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getCaseInstanceAdHocFragments(@javax.ws.rs.core.Context HttpHeaders headers,
            @PathParam(CONTAINER_ID) String containerId, @PathParam(CASE_ID) String caseId) {

        return invokeCaseOperation(headers,
                containerId,
                caseId,
                (Variant v, String type, Header... customHeaders) -> {
                    logger.debug("About to look for adhoc fragments in case {}", caseId);
                    CaseAdHocFragmentList responseObject = this.caseManagementRuntimeDataServiceBase.getAdHocFragments(containerId, caseId);

                    logger.debug("Returning OK response with content '{}'", responseObject);
                    return createCorrectVariant(responseObject, headers, Response.Status.OK, customHeaders);
                });
    }

    @GET
    @Path(CASE_PROCESS_INSTANCES_GET_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getCaseInstanceProcessInstance(@javax.ws.rs.core.Context HttpHeaders headers,
            @PathParam(CONTAINER_ID) String containerId, @PathParam(CASE_ID) String caseId,
            @QueryParam("status") List<Integer> status,
            @QueryParam("page") @DefaultValue("0") Integer page, @QueryParam("pageSize") @DefaultValue("10") Integer pageSize) {

        return invokeCaseOperation(headers,
                containerId,
                caseId,
                (Variant v, String type, Header... customHeaders) -> {
                    List<Integer> actualStatus = status;
                    if (status == null || status.isEmpty()) {
                        actualStatus = new ArrayList<Integer>();
                        actualStatus.add(ProcessInstance.STATE_ACTIVE);
                    }
                    logger.debug("About to look for process instances in case {} with status {}", caseId, actualStatus);
                    ProcessInstanceList responseObject = this.caseManagementRuntimeDataServiceBase.getProcessInstancesForCase(containerId, caseId, actualStatus, page, pageSize);

                    logger.debug("Returning OK response with content '{}'", responseObject);
                    return createCorrectVariant(responseObject, headers, Response.Status.OK, customHeaders);
                });
    }

    @GET
    @Path(CASE_NODE_INSTANCES_GET_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getCaseInstanceActiveNodes(@javax.ws.rs.core.Context HttpHeaders headers,
            @PathParam(CONTAINER_ID) String containerId, @PathParam(CASE_ID) String caseId,
            @QueryParam("page") @DefaultValue("0") Integer page, @QueryParam("pageSize") @DefaultValue("10") Integer pageSize) {

        return invokeCaseOperation(headers,
                containerId,
                caseId,
                (Variant v, String type, Header... customHeaders) -> {
                    logger.debug("About to look for active nodes in case {}", caseId);
                    NodeInstanceList responseObject = this.caseManagementRuntimeDataServiceBase.getActiveNodes(containerId, caseId, page, pageSize);

                    logger.debug("Returning OK response with content '{}'", responseObject);
                    return createCorrectVariant(responseObject, headers, Response.Status.OK, customHeaders);
                });
    }

    @GET
    @Path(CASE_ROLES_GET_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getCaseInstanceRoleAssignments(@javax.ws.rs.core.Context HttpHeaders headers,
            @PathParam(CONTAINER_ID) String containerId, @PathParam(CASE_ID) String caseId) {

        return invokeCaseOperation(headers,
                containerId,
                caseId,
                (Variant v, String type, Header... customHeaders) -> {
                    logger.debug("About to look for role assignments in case {}", caseId);
                    CaseRoleAssignmentList responseObject = this.caseManagementServiceBase.getRoleAssignment(containerId, caseId);

                    logger.debug("Returning OK response with content '{}'", responseObject);
                    return createCorrectVariant(responseObject, headers, Response.Status.OK, customHeaders);
                });
    }

    @PUT
    @Path(CASE_ROLES_PUT_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response addRoleAssignment(@javax.ws.rs.core.Context HttpHeaders headers,
            @PathParam(CONTAINER_ID) String containerId, @PathParam(CASE_ID) String caseId,
            @PathParam(CASE_ROLE_NAME) String roleName,
            @QueryParam("user") String user, @QueryParam("group") String group) {
        return invokeCaseOperation(headers,
                containerId,
                caseId,
                (Variant v, String type, Header... customHeaders) -> {
                    logger.debug("About to assign  user {}, group {} for role {} in case {}", user, group, roleName, caseId);
                    this.caseManagementServiceBase.assignToRole(containerId, caseId, roleName, user, group);

                    logger.debug("Returning CREATED response");
                    return createResponse("", v, Response.Status.CREATED, customHeaders);
                });
    }

    @DELETE
    @Path(CASE_ROLES_DELETE_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response removeRoleAssignment(@javax.ws.rs.core.Context HttpHeaders headers,
            @PathParam(CONTAINER_ID) String containerId, @PathParam(CASE_ID) String caseId,
            @PathParam(CASE_ROLE_NAME) String roleName,
            @QueryParam("user") String user, @QueryParam("group") String group) {
        return invokeCaseOperation(headers,
                containerId,
                caseId,
                (Variant v, String type, Header... customHeaders) -> {
                    logger.debug("About to remove user {}, group {} from role {} in case {}", user, group, roleName, caseId);
                    this.caseManagementServiceBase.removeFromRole(containerId, caseId, roleName, user, group);

                    logger.debug("Returning NO_CONTENT response");
                    return noContent(v, customHeaders);
                });
    }


    @GET
    @Path(CASE_COMMENTS_GET_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getCaseInstanceComments(@javax.ws.rs.core.Context HttpHeaders headers,
            @PathParam(CONTAINER_ID) String containerId, @PathParam(CASE_ID) String caseId,
            @QueryParam("page") @DefaultValue("0") Integer page, @QueryParam("pageSize") @DefaultValue("10") Integer pageSize) {

        return invokeCaseOperation(headers,
                containerId,
                caseId,
                (Variant v, String type, Header... customHeaders) -> {
                    logger.debug("About to look for comments in case {}", caseId);
                    CaseCommentList responseObject = this.caseManagementServiceBase.getComments(containerId, caseId, page, pageSize);

                    logger.debug("Returning OK response with content '{}'", responseObject);
                    return createCorrectVariant(responseObject, headers, Response.Status.OK, customHeaders);
                });
    }

    @POST
    @Path(CASE_COMMENTS_POST_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response addComment(@javax.ws.rs.core.Context HttpHeaders headers,
            @PathParam(CONTAINER_ID) String containerId, @PathParam(CASE_ID) String caseId,
            @QueryParam("author") String author, String payload) {
        return invokeCaseOperation(headers,
                containerId,
                caseId,
                (Variant v, String type, Header... customHeaders) -> {
                    logger.debug("About to add comment to case {}", caseId);
                    this.caseManagementServiceBase.addCommentToCase(containerId, caseId, author, payload, type);

                    logger.debug("Returning CREATED response");
                    return createResponse("", v, Response.Status.CREATED, customHeaders);
                });
    }

    @PUT
    @Path(CASE_COMMENTS_PUT_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response updateComment(@javax.ws.rs.core.Context HttpHeaders headers,
            @PathParam(CONTAINER_ID) String containerId, @PathParam(CASE_ID) String caseId,
            @PathParam(CASE_COMMENT_ID) String commentId, @QueryParam("author") String author, String payload) {
        return invokeCaseOperation(headers,
                containerId,
                caseId,
                (Variant v, String type, Header... customHeaders) -> {
                    logger.debug("About to update comment {} in case {}", commentId, caseId);
                    this.caseManagementServiceBase.updateCommentInCase(containerId, caseId, commentId, author, payload, type);

                    logger.debug("Returning CREATED response");
                    return createResponse("", v, Response.Status.CREATED, customHeaders);
                });
    }

    @DELETE
    @Path(CASE_COMMENTS_DELETE_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response removeComment(@javax.ws.rs.core.Context HttpHeaders headers,
            @PathParam(CONTAINER_ID) String containerId, @PathParam(CASE_ID) String caseId,
            @PathParam(CASE_COMMENT_ID) String commentId) {
        return invokeCaseOperation(headers,
                containerId,
                caseId,
                (Variant v, String type, Header... customHeaders) -> {
                    logger.debug("About to remove comment {} from case {}", commentId, caseId);
                    this.caseManagementServiceBase.removeCommentFromCase(containerId, caseId, commentId);

                    logger.debug("Returning NO_CONTENT response");
                    return noContent(v, customHeaders);
                });
    }

    /*
     * basic queries through container
     */

    @GET
    @Path(CASE_INSTANCES_GET_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getCaseInstancesByContainer(@javax.ws.rs.core.Context HttpHeaders headers,
            @PathParam(CONTAINER_ID) String containerId,
            @QueryParam("status") List<Integer> status,
            @QueryParam("page") @DefaultValue("0") Integer page, @QueryParam("pageSize") @DefaultValue("10") Integer pageSize) {

        return invokeCaseOperation(headers,
                containerId,
                null,
                (Variant v, String type, Header... customHeaders) -> {
                    List<Integer> actualStatus = status;
                    if (status == null || status.isEmpty()) {
                        actualStatus = new ArrayList<Integer>();
                        actualStatus.add(ProcessInstance.STATE_ACTIVE);
                    }
                    logger.debug("About to look for case instances in container {} with status {}", containerId, actualStatus);
                    CaseInstanceList responseObject = this.caseManagementRuntimeDataServiceBase.getCaseInstancesByContainer(containerId, actualStatus, page, pageSize);

                    logger.debug("Returning OK response with content '{}'", responseObject);
                    return createCorrectVariant(responseObject, headers, Response.Status.OK, customHeaders);
                });
    }

    @GET
    @Path(CASE_INSTANCES_BY_DEF_GET_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getCaseInstancesByDefinition(@javax.ws.rs.core.Context HttpHeaders headers,
            @PathParam(CONTAINER_ID) String containerId, @PathParam(CASE_DEF_ID) String caseDefId,
            @QueryParam("status") List<Integer> status,
            @QueryParam("page") @DefaultValue("0") Integer page, @QueryParam("pageSize") @DefaultValue("10") Integer pageSize) {

        return invokeCaseOperation(headers,
                containerId,
                null,
                (Variant v, String type, Header... customHeaders) -> {
                    List<Integer> actualStatus = status;
                    if (status == null || status.isEmpty()) {
                        actualStatus = new ArrayList<Integer>();
                        actualStatus.add(ProcessInstance.STATE_ACTIVE);
                    }
                    logger.debug("About to look for case instances with case definition id {} with status {}", caseDefId, actualStatus);
                    CaseInstanceList responseObject = this.caseManagementRuntimeDataServiceBase.getCaseInstancesByDefinition(containerId, caseDefId, actualStatus, page, pageSize);

                    logger.debug("Returning OK response with content '{}'", responseObject);
                    return createCorrectVariant(responseObject, headers, Response.Status.OK, customHeaders);
                });
    }

    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getCaseDefinitionsByContainer(@javax.ws.rs.core.Context HttpHeaders headers,
            @PathParam(CONTAINER_ID) String containerId,
            @QueryParam("page") @DefaultValue("0") Integer page, @QueryParam("pageSize") @DefaultValue("10") Integer pageSize) {

        return invokeCaseOperation(headers,
                containerId,
                null,
                (Variant v, String type, Header... customHeaders) -> {

                    logger.debug("About to look for case definitions in container {}", containerId);
                    CaseDefinitionList responseObject = this.caseManagementRuntimeDataServiceBase.getCaseDefinitionsByContainer(containerId, page, pageSize);

                    logger.debug("Returning OK response with content '{}'", responseObject);
                    return createCorrectVariant(responseObject, headers, Response.Status.OK, customHeaders);
                });
    }

    @GET
    @Path(CASE_GET_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getCaseDefinitionsByDefinition(@javax.ws.rs.core.Context HttpHeaders headers,
            @PathParam(CONTAINER_ID) String containerId, @PathParam(CASE_DEF_ID) String caseDefId) {

        return invokeCaseOperation(headers,
                containerId,
                null,
                (Variant v, String type, Header... customHeaders) -> {

                    logger.debug("About to look for case definition with id {} in container {}", caseDefId, containerId);
                    CaseDefinition responseObject = this.caseManagementRuntimeDataServiceBase.getCaseDefinition(containerId, caseDefId);

                    logger.debug("Returning OK response with content '{}'", responseObject);
                    return createCorrectVariant(responseObject, headers, Response.Status.OK, customHeaders);
                });
    }

}