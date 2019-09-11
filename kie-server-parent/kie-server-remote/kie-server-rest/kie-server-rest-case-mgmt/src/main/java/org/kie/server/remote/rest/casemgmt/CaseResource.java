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

import static org.kie.server.api.rest.RestURI.CASE_AD_HOC_FRAGMENTS_GET_URI;
import static org.kie.server.api.rest.RestURI.CASE_ALL_DEFINITIONS_GET_URI;
import static org.kie.server.api.rest.RestURI.CASE_COMMENTS_DELETE_URI;
import static org.kie.server.api.rest.RestURI.CASE_COMMENTS_GET_URI;
import static org.kie.server.api.rest.RestURI.CASE_COMMENTS_POST_URI;
import static org.kie.server.api.rest.RestURI.CASE_COMMENTS_PUT_URI;
import static org.kie.server.api.rest.RestURI.CASE_COMMENT_ID;
import static org.kie.server.api.rest.RestURI.CASE_DEFINITIONS_BY_ID_GET_URI;
import static org.kie.server.api.rest.RestURI.CASE_DEF_ID;
import static org.kie.server.api.rest.RestURI.CASE_DYNAMIC_PROCESS_IN_STAGE_POST_URI;
import static org.kie.server.api.rest.RestURI.CASE_DYNAMIC_PROCESS_POST_URI;
import static org.kie.server.api.rest.RestURI.CASE_DYNAMIC_TASK_IN_STAGE_POST_URI;
import static org.kie.server.api.rest.RestURI.CASE_DYNAMIC_TASK_IN_STAGE_PUT_URI;
import static org.kie.server.api.rest.RestURI.CASE_DYNAMIC_TASK_POST_URI;
import static org.kie.server.api.rest.RestURI.CASE_DYNAMIC_TASK_PUT_URI;
import static org.kie.server.api.rest.RestURI.CASE_FILE_BY_NAME_GET_URI;
import static org.kie.server.api.rest.RestURI.CASE_FILE_BY_NAME_POST_URI;
import static org.kie.server.api.rest.RestURI.CASE_FILE_DELETE_URI;
import static org.kie.server.api.rest.RestURI.CASE_FILE_GET_URI;
import static org.kie.server.api.rest.RestURI.CASE_FILE_ITEM;
import static org.kie.server.api.rest.RestURI.CASE_FILE_POST_URI;
import static org.kie.server.api.rest.RestURI.CASE_ID;
import static org.kie.server.api.rest.RestURI.CASE_INSTANCES_BY_DEF_GET_URI;
import static org.kie.server.api.rest.RestURI.CASE_INSTANCES_GET_URI;
import static org.kie.server.api.rest.RestURI.CASE_INSTANCE_DELETE_URI;
import static org.kie.server.api.rest.RestURI.CASE_INSTANCE_GET_URI;
import static org.kie.server.api.rest.RestURI.CASE_INSTANCE_POST_URI;
import static org.kie.server.api.rest.RestURI.CASE_MILESTONES_GET_URI;
import static org.kie.server.api.rest.RestURI.CASE_NODE_INSTANCES_GET_URI;
import static org.kie.server.api.rest.RestURI.CASE_NODE_NAME;
import static org.kie.server.api.rest.RestURI.CASE_PROCESS_INSTANCES_GET_URI;
import static org.kie.server.api.rest.RestURI.CASE_ROLES_DELETE_URI;
import static org.kie.server.api.rest.RestURI.CASE_ROLES_GET_URI;
import static org.kie.server.api.rest.RestURI.CASE_ROLES_PUT_URI;
import static org.kie.server.api.rest.RestURI.CASE_ROLE_NAME;
import static org.kie.server.api.rest.RestURI.CASE_STAGES_GET_URI;
import static org.kie.server.api.rest.RestURI.CASE_STAGE_ID;
import static org.kie.server.api.rest.RestURI.CASE_URI;
import static org.kie.server.api.rest.RestURI.CONTAINER_ID;
import static org.kie.server.api.rest.RestURI.PROCESS_ID;
import static org.kie.server.api.rest.RestURI.REOPEN_CASE_PUT_URI;
import static org.kie.server.api.rest.RestURI.START_CASE_POST_URI;
import static org.kie.server.remote.rest.casemgmt.Messages.CASE_COMMENT_NOT_FOUND;
import static org.kie.server.remote.rest.casemgmt.Messages.CASE_DEFINITION_NOT_FOUND;
import static org.kie.server.remote.rest.casemgmt.docs.ParameterSamples.CASE_ADHOC_FRAGMENTS_JSON;
import static org.kie.server.remote.rest.casemgmt.docs.ParameterSamples.CASE_COMMENTS_JSON;
import static org.kie.server.remote.rest.casemgmt.docs.ParameterSamples.CASE_DEFINITIONS_JSON;
import static org.kie.server.remote.rest.casemgmt.docs.ParameterSamples.CASE_DEFINITION_JSON;
import static org.kie.server.remote.rest.casemgmt.docs.ParameterSamples.CASE_FILE_JSON;
import static org.kie.server.remote.rest.casemgmt.docs.ParameterSamples.CASE_FILE_XML;
import static org.kie.server.remote.rest.casemgmt.docs.ParameterSamples.CASE_ID_JSON;
import static org.kie.server.remote.rest.casemgmt.docs.ParameterSamples.CASE_INSTANCES_JSON;
import static org.kie.server.remote.rest.casemgmt.docs.ParameterSamples.CASE_INSTANCE_JSON;
import static org.kie.server.remote.rest.casemgmt.docs.ParameterSamples.CASE_MILESTONES_JSON;
import static org.kie.server.remote.rest.casemgmt.docs.ParameterSamples.CASE_ROLES_ASSIGNMENTS_JSON;
import static org.kie.server.remote.rest.casemgmt.docs.ParameterSamples.CASE_STAGES_JSON;
import static org.kie.server.remote.rest.casemgmt.docs.ParameterSamples.JSON;
import static org.kie.server.remote.rest.casemgmt.docs.ParameterSamples.NODE_INSTANCES_JSON;
import static org.kie.server.remote.rest.casemgmt.docs.ParameterSamples.PROCESS_INSTANCES_JSON;
import static org.kie.server.remote.rest.casemgmt.docs.ParameterSamples.VAR_JSON;
import static org.kie.server.remote.rest.casemgmt.docs.ParameterSamples.VAR_MAP_JSON;
import static org.kie.server.remote.rest.casemgmt.docs.ParameterSamples.VAR_MAP_XML;
import static org.kie.server.remote.rest.casemgmt.docs.ParameterSamples.VAR_XML;
import static org.kie.server.remote.rest.casemgmt.docs.ParameterSamples.XML;
import static org.kie.server.remote.rest.common.util.RestUtils.createCorrectVariant;
import static org.kie.server.remote.rest.common.util.RestUtils.createResponse;
import static org.kie.server.remote.rest.common.util.RestUtils.noContent;
import static org.kie.server.remote.rest.common.util.RestUtils.notFound;

import java.text.MessageFormat;
import java.util.ArrayList;
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
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Variant;

import org.jbpm.casemgmt.api.CaseCommentNotFoundException;
import org.jbpm.casemgmt.api.CaseDefinitionNotFoundException;
import org.jbpm.casemgmt.api.model.CaseStatus;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.server.api.model.cases.CaseAdHocFragmentList;
import org.kie.server.api.model.cases.CaseCommentList;
import org.kie.server.api.model.cases.CaseDefinition;
import org.kie.server.api.model.cases.CaseDefinitionList;
import org.kie.server.api.model.cases.CaseInstance;
import org.kie.server.api.model.cases.CaseInstanceList;
import org.kie.server.api.model.cases.CaseMilestoneList;
import org.kie.server.api.model.cases.CaseRoleAssignmentList;
import org.kie.server.api.model.cases.CaseStageList;
import org.kie.server.api.model.instance.NodeInstanceList;
import org.kie.server.api.model.instance.ProcessInstanceList;
import org.kie.server.remote.rest.common.Header;
import org.kie.server.remote.rest.common.util.RestUtils;
import org.kie.server.services.api.KieServerRegistry;
import org.kie.server.services.casemgmt.CaseManagementRuntimeDataServiceBase;
import org.kie.server.services.casemgmt.CaseManagementServiceBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Example;
import io.swagger.annotations.ExampleProperty;

@Api(value="Case instances")
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

    @ApiOperation(value="Starts a new case instance for a specified case definition.",
            response=String.class, code=201)
    @ApiResponses(value = { @ApiResponse(code = 500, message = "Unexpected error"),
            @ApiResponse(code = 404, message = "Case definition or Container Id not found"), 
            @ApiResponse(code = 201, message = "Successfull response", examples=@Example(value= {
                    @ExampleProperty(mediaType=JSON, value=CASE_ID_JSON)})) })
    @POST
    @Path(START_CASE_POST_URI)
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response startCase(@javax.ws.rs.core.Context HttpHeaders headers,
            @ApiParam(value = "container id where the case definition resides", required = true, example = "evaluation_1.0.0-SNAPSHOT") @PathParam(CONTAINER_ID) String containerId, 
            @ApiParam(value = "case definition id that new instance should be created from", required = true, example = "CASE-0000000001") @PathParam(CASE_DEF_ID) String caseDefId, 
            @ApiParam(value = "optional CaseFile with variables and/or case role assignments", required = false, examples=@Example(value= {
                    @ExampleProperty(mediaType=JSON, value=CASE_FILE_JSON),
                    @ExampleProperty(mediaType=XML, value=CASE_FILE_XML)})) String payload) {

        return invokeCaseOperation(headers,
                containerId,
                null,
                (Variant v, String type, Header... customHeaders) -> {
                    try {
                        String response = caseManagementServiceBase.startCase(containerId, caseDefId, payload, type);
                        logger.debug("Returning CREATED response for start case with content '{}'", response);

                        return createResponse(response, v, Response.Status.CREATED, customHeaders);
                    } catch (CaseDefinitionNotFoundException e) {
                        return notFound(
                                MessageFormat.format(CASE_DEFINITION_NOT_FOUND, caseDefId, containerId), v, customHeaders);
                    }
                });
    }

    @ApiOperation(value="Returns information about a specified case instance.",
            response=CaseInstance.class, code=200)
    @ApiResponses(value = { @ApiResponse(code = 500, message = "Unexpected error"),
            @ApiResponse(code = 404, message = "Case instance not found"), 
            @ApiResponse(code = 200, message = "Successfull response", examples=@Example(value= {
                    @ExampleProperty(mediaType=JSON, value=CASE_INSTANCE_JSON)})) })
    @GET
    @Path(CASE_INSTANCE_GET_URI)
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getCaseInstance(@javax.ws.rs.core.Context HttpHeaders headers,
            @ApiParam(value = "container id that case instance belongs to", required = true, example = "evaluation_1.0.0-SNAPSHOT") @PathParam(CONTAINER_ID) String containerId, 
            @ApiParam(value = "identifier of the case instance", required = true, example = "CASE-00000000001") @PathParam(CASE_ID) String caseId,
            @ApiParam(value = "optional flag to load data when loading case instance", required = false) @QueryParam("withData") @DefaultValue("false") boolean withData,
            @ApiParam(value = "optional flag to load roles when loading case instance", required = false) @QueryParam("withRoles") @DefaultValue("false") boolean withRoles,
            @ApiParam(value = "optional flag to load milestones when loading case instance", required = false) @QueryParam("withMilestones") @DefaultValue("false") boolean withMilestones,
            @ApiParam(value = "optional flag to load stages when loading case instance", required = false) @QueryParam("withStages") @DefaultValue("false") boolean withStages) {

        return invokeCaseOperation(headers,
                containerId,
                caseId,
                (Variant v, String type, Header... customHeaders) -> {
                    String response = caseManagementServiceBase.getCaseInstance(containerId, caseId, withData, withRoles, withMilestones, withStages, type);
                    logger.debug("Returning OK response for get case instance with content '{}'", response);

                    return createResponse(response, v, Response.Status.OK, customHeaders);
                });
    }

    @ApiOperation(value="Aborts or permanently destroys a specified case instance.",
            response=Void.class, code=204)
    @ApiResponses(value = { @ApiResponse(code = 500, message = "Unexpected error"),
            @ApiResponse(code = 404, message = "Case instance not found") })
    @DELETE
    @Path(CASE_INSTANCE_DELETE_URI)
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response cancelCaseInstance(@javax.ws.rs.core.Context HttpHeaders headers,
            @ApiParam(value = "container id that case instance belongs to", required = true, example = "evaluation_1.0.0-SNAPSHOT") @PathParam(CONTAINER_ID) String containerId, 
            @ApiParam(value = "identifier of the case instance", required = true, example = "CASE-00000000001") @PathParam(CASE_ID) String caseId,
            @ApiParam(value = "allows to destroy (permanently) case instance as part of the cancel operation, defaults to false", required = false) @QueryParam("destroy") @DefaultValue("false") boolean destroy) {
        return invokeCaseOperation(headers,
                containerId,
                caseId,
                (Variant v, String type, Header... customHeaders) -> {
                    caseManagementServiceBase.cancelCaseInstance(containerId, caseId, destroy);
                    logger.debug("Returning NO CONTENT response after cancelling a case with id {}", caseId);

                    return noContent(v, customHeaders);
                });
    }
    
    @ApiOperation(value="Closes a specified case instance.",
            response=Void.class, code=201)
    @ApiResponses(value = { @ApiResponse(code = 500, message = "Unexpected error"),
            @ApiResponse(code = 404, message = "Case instance not found") })
    @POST
    @Path(CASE_INSTANCE_POST_URI)
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response closeCaseInstance(@javax.ws.rs.core.Context HttpHeaders headers,
            @ApiParam(value = "container id that case instance belongs to", required = true, example = "evaluation_1.0.0-SNAPSHOT") @PathParam(CONTAINER_ID) String containerId, 
            @ApiParam(value = "identifier of the case instance", required = true, example = "CASE-00000000001") @PathParam(CASE_ID) String caseId,
            @ApiParam(value = "optional comment when closing a case instance as String", required = false) String payload) {
        return invokeCaseOperation(headers,
                containerId,
                caseId,
                (Variant v, String type, Header... customHeaders) -> {
                    caseManagementServiceBase.closeCaseInstance(containerId, caseId, payload, type);
                    logger.debug("Returning NO CONTENT response after closing a case with id {}", caseId);

                    return createResponse("", v, Response.Status.CREATED, customHeaders);
                });
    }

    @ApiOperation(value="Reopens a closed case instance for a specified case definition.",
            response=Void.class, code=201)
    @ApiResponses(value = { @ApiResponse(code = 500, message = "Unexpected error"),
            @ApiResponse(code = 404, message = "Case instance not found") })
    @PUT
    @Path(REOPEN_CASE_PUT_URI)
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response reopenCase(@javax.ws.rs.core.Context HttpHeaders headers,
            @ApiParam(value = "container id where the case definition resides", required = true, example = "evaluation_1.0.0-SNAPSHOT") @PathParam(CONTAINER_ID) String containerId, 
            @ApiParam(value = "case definition id that new instance should be created from", required = true, example = "evaluation") @PathParam(CASE_DEF_ID) String caseDefId, 
            @ApiParam(value = "identifier of the case instance", required = true) @PathParam(CASE_ID) String caseId, 
            @ApiParam(value = "optional CaseFile with variables and/or case role assignments", required = false, examples=@Example(value= {
                    @ExampleProperty(mediaType=JSON, value=CASE_FILE_JSON),
                    @ExampleProperty(mediaType=XML, value=CASE_FILE_XML)})) String payload) {

        return invokeCaseOperation(headers,
                containerId,
                null,
                (Variant v, String type, Header... customHeaders) -> {
                    caseManagementServiceBase.reopenCase(caseId, containerId, caseDefId, payload, type);
                    logger.debug("Returning CREATED response for reopen case {}", caseId);

                    return createResponse("", v, Response.Status.CREATED, customHeaders);
                });
    }

    @ApiOperation(value="Returns data from the case files of a specified case instance.",
            response=Map.class, code=200)
    @ApiResponses(value = { @ApiResponse(code = 500, message = "Unexpected error"),
            @ApiResponse(code = 404, message = "Case instance not found"), 
            @ApiResponse(code = 200, message = "Successfull response", examples=@Example(value= {
                    @ExampleProperty(mediaType=JSON, value=VAR_MAP_JSON)})) })
    @GET
    @Path(CASE_FILE_GET_URI)
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getCaseInstanceData(@javax.ws.rs.core.Context HttpHeaders headers,
            @ApiParam(value = "container id that case instance belongs to", required = true, example = "evaluation_1.0.0-SNAPSHOT") @PathParam(CONTAINER_ID) String containerId, 
            @ApiParam(value = "identifier of the case instance", required = true, example = "CASE-00000000001") @PathParam(CASE_ID) String caseId,
            @ApiParam(value = "optional name(s) of the data items to retrieve", required = false) @QueryParam("name") List<String> names) {
        return invokeCaseOperation(headers,
                containerId,
                caseId,
                (Variant v, String type, Header... customHeaders) -> {
                    logger.debug("About to load case file data of case {}", caseId);
                    String response = this.caseManagementServiceBase.getCaseFileData(containerId, caseId, names, type);

                    logger.debug("Returning OK response with content '{}'", response);
                    return createResponse(response, v, Response.Status.OK, customHeaders);
                });
    }

    @ApiOperation(value="Returns the value of a specified case file data item in a specified case instance.",
            response=Object.class, code=200)
    @ApiResponses(value = { @ApiResponse(code = 500, message = "Unexpected error"),
            @ApiResponse(code = 404, message = "Case instance not found"), 
            @ApiResponse(code = 200, message = "Successfull response", examples=@Example(value= {
                    @ExampleProperty(mediaType=JSON, value=VAR_JSON)})) })
    @GET
    @Path(CASE_FILE_BY_NAME_GET_URI)
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getCaseInstanceDataByName(@javax.ws.rs.core.Context HttpHeaders headers,
            @ApiParam(value = "container id that case instance belongs to", required = true, example = "evaluation_1.0.0-SNAPSHOT") @PathParam(CONTAINER_ID) String containerId, 
            @ApiParam(value = "identifier of the case instance", required = true, example = "CASE-00000000001") @PathParam(CASE_ID) String caseId, 
            @ApiParam(value = "name of the data item within case file to retrieve", required = true) @PathParam(CASE_FILE_ITEM) String caseDataName) {
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

    @ApiOperation(value="Adds data as case files in a specified case instance.",
            response=Void.class, code=201)
    @ApiResponses(value = { @ApiResponse(code = 500, message = "Unexpected error"),
            @ApiResponse(code = 404, message = "Case instance not found") })
    @POST
    @Path(CASE_FILE_POST_URI)
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response putCaseInstanceData(@javax.ws.rs.core.Context HttpHeaders headers,
            @ApiParam(value = "container id that case instance belongs to", required = true, example = "evaluation_1.0.0-SNAPSHOT") @PathParam(CONTAINER_ID) String containerId, 
            @ApiParam(value = "identifier of the case instance", required = true, example = "CASE-00000000001") @PathParam(CASE_ID) String caseId,
            @ApiParam(value = "optional role name(s) that given data should be restricted to", required = false) @QueryParam("restrictedTo") List<String> restrictions,
            @ApiParam(value = "map of data to be placed in case file as Map", required = true, examples=@Example(value= {
                    @ExampleProperty(mediaType=JSON, value=VAR_MAP_JSON),
                    @ExampleProperty(mediaType=XML, value=VAR_MAP_XML)})) String payload) {

        return invokeCaseOperation(headers,
                containerId,
                caseId,
                (Variant v, String type, Header... customHeaders) -> {
                    logger.debug("About to put case file data of case {}", caseId);
                    this.caseManagementServiceBase.putCaseFileData(containerId, caseId, restrictions, payload, type);

                    logger.debug("Returning CREATED response");
                    return createResponse("", v, Response.Status.CREATED, customHeaders);
                });

    }

    @ApiOperation(value="Adds a single data item as a case file in a specified case instance.",
            response=Void.class, code=201)
    @ApiResponses(value = { @ApiResponse(code = 500, message = "Unexpected error"),
            @ApiResponse(code = 404, message = "Case instance not found") })
    @POST
    @Path(CASE_FILE_BY_NAME_POST_URI)
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response putCaseInstanceDataByName(@javax.ws.rs.core.Context HttpHeaders headers,
            @ApiParam(value = "container id that case instance belongs to", required = true, example = "evaluation_1.0.0-SNAPSHOT") @PathParam(CONTAINER_ID) String containerId, 
            @ApiParam(value = "identifier of the case instance", required = true, example = "CASE-00000000001") @PathParam(CASE_ID) String caseId, 
            @ApiParam(value = "name of the data item to be added to case file", required = true) @PathParam(CASE_FILE_ITEM) String caseDataName,
            @ApiParam(value = "optional role name(s) that given data should be restricted to", required = false) @QueryParam("restrictedTo") List<String> restrictions,
            @ApiParam(value = "data to be placed in case file, any type can be provided", required = true, examples=@Example(value= {
                    @ExampleProperty(mediaType=JSON, value=VAR_JSON),
                    @ExampleProperty(mediaType=XML, value=VAR_XML)})) String payload) {

        return invokeCaseOperation(headers,
                containerId,
                caseId,
                (Variant v, String type, Header... customHeaders) -> {
                    logger.debug("About to put case file data of case {}", caseId);
                    this.caseManagementServiceBase.putCaseFileDataByName(containerId, caseId, caseDataName, restrictions, payload, type);

                    logger.debug("Returning CREATED response");
                    return createResponse("", v, Response.Status.CREATED, customHeaders);
                });
    }

    @ApiOperation(value="Deletes one or more specified case file data items from a specified case instance.",
            response=Void.class, code=204)
    @ApiResponses(value = { @ApiResponse(code = 500, message = "Unexpected error"),
            @ApiResponse(code = 404, message = "Case instance not found") })
    @DELETE
    @Path(CASE_FILE_DELETE_URI)
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response deleteCaseInstanceData(@javax.ws.rs.core.Context HttpHeaders headers,
            @ApiParam(value = "container id that case instance belongs to", required = true, example = "evaluation_1.0.0-SNAPSHOT") @PathParam(CONTAINER_ID) String containerId, 
            @ApiParam(value = "identifier of the case instance", required = true, example = "CASE-00000000001") @PathParam(CASE_ID) String caseId, 
            @ApiParam(value = "one or more names of the data items to be removed from case file", required = true) @QueryParam(CASE_FILE_ITEM) List<String> variableNames) {

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

    @ApiOperation(value="Adds a dynamic user task or service task to the specified case instance.",
            response=Void.class, code=201)
    @ApiResponses(value = { @ApiResponse(code = 500, message = "Unexpected error"),
            @ApiResponse(code = 404, message = "Case instance not found") })
    @POST
    @Path(CASE_DYNAMIC_TASK_POST_URI)
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response addDynamicTaskToCase(@javax.ws.rs.core.Context HttpHeaders headers,
            @ApiParam(value = "container id that case instance belongs to", required = true, example = "evaluation_1.0.0-SNAPSHOT") @PathParam(CONTAINER_ID) String containerId, 
            @ApiParam(value = "identifier of the case instance", required = true, example = "CASE-00000000001") @PathParam(CASE_ID) String caseId,
            @ApiParam(value = "data for dynamic task (it represents task specification that drives the selection of the type of task)", required = true, examples=@Example(value= {
                    @ExampleProperty(mediaType=JSON, value=VAR_MAP_JSON),
                    @ExampleProperty(mediaType=XML, value=VAR_MAP_XML)})) String payload) {
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

    @ApiOperation(value="Adds a dynamic user task or service task to the specified stage in a specified case instance.",
            response=Void.class, code=201)
    @ApiResponses(value = { @ApiResponse(code = 500, message = "Unexpected error"),
            @ApiResponse(code = 404, message = "Case instance not found") })
    @POST
    @Path(CASE_DYNAMIC_TASK_IN_STAGE_POST_URI)
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response addDynamicTaskToCase(@javax.ws.rs.core.Context HttpHeaders headers,
            @ApiParam(value = "container id that case instance belongs to", required = true, example = "evaluation_1.0.0-SNAPSHOT") @PathParam(CONTAINER_ID) String containerId, 
            @ApiParam(value = "identifier of the case instance", required = true, example = "CASE-00000000001") @PathParam(CASE_ID) String caseId, 
            @ApiParam(value = "identifier of the stage within case instance where dynamic task should be added", required = true, example = "stageOne") @PathParam(CASE_STAGE_ID) String stageId,
            @ApiParam(value = "data for dynamic task (it represents task specification that drives the selection of the type of task)", required = true, examples=@Example(value= {
                    @ExampleProperty(mediaType=JSON, value=VAR_MAP_JSON),
                    @ExampleProperty(mediaType=XML, value=VAR_MAP_XML)})) String payload) {
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

    @ApiOperation(value="Adds a dynamic subprocess to a specified case instance.",
            response=Void.class, code=201)
    @ApiResponses(value = { @ApiResponse(code = 500, message = "Unexpected error"),
            @ApiResponse(code = 404, message = "Case instance not found") })
    @POST
    @Path(CASE_DYNAMIC_PROCESS_POST_URI)
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response addDynamicProcessToCase(@javax.ws.rs.core.Context HttpHeaders headers,
            @ApiParam(value = "container id that case instance belongs to", required = true, example = "evaluation_1.0.0-SNAPSHOT") @PathParam(CONTAINER_ID) String containerId, 
            @ApiParam(value = "identifier of the case instance", required = true, example = "CASE-00000000001") @PathParam(CASE_ID) String caseId,
            @ApiParam(value = "process id of the subprocess to be added", required = true, example = "placeOrder") @PathParam(PROCESS_ID) String processId,
            @ApiParam(value = "data for dynamic subprocess", required = true, examples=@Example(value= {
                    @ExampleProperty(mediaType=JSON, value=VAR_MAP_JSON),
                    @ExampleProperty(mediaType=XML, value=VAR_MAP_XML)})) String payload) {
        return invokeCaseOperation(headers,
                containerId,
                caseId,
                processId,
                (Variant v, String type, Header... customHeaders) -> {
                    logger.debug("About to add dynamic subprocess {} in case {}", processId, caseId);
                    this.caseManagementServiceBase.addDynamicSubprocess(containerId, caseId, null, processId, payload, type);

                    logger.debug("Returning CREATED response");
                    return createResponse("", v, Response.Status.CREATED, customHeaders);
                });
    }

    @ApiOperation(value="Adds dynamic subprocess identified by process id to stage within case instance",
            response=Void.class, code=201)
    @ApiResponses(value = { @ApiResponse(code = 500, message = "Unexpected error"),
            @ApiResponse(code = 404, message = "Case instance not found") })
    @POST
    @Path(CASE_DYNAMIC_PROCESS_IN_STAGE_POST_URI)
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response addDynamicProcessToCase(@javax.ws.rs.core.Context HttpHeaders headers,
            @ApiParam(value = "container id that case instance belongs to", required = true, example = "evaluation_1.0.0-SNAPSHOT") @PathParam(CONTAINER_ID) String containerId, 
            @ApiParam(value = "identifier of the case instance", required = true, example = "CASE-00000000001") @PathParam(CASE_ID) String caseId,
            @ApiParam(value = "identifier of the stage within case instance where dynamic subprocess should be added", required = true, example = "stageOne") @PathParam(CASE_STAGE_ID) String stageId, 
            @ApiParam(value = "process id of the subprocess to be added", required = true) @PathParam(PROCESS_ID) String processId,
            @ApiParam(value = "data for dynamic subprocess", required = true, examples=@Example(value= {
                    @ExampleProperty(mediaType=JSON, value=VAR_MAP_JSON),
                    @ExampleProperty(mediaType=XML, value=VAR_MAP_XML)})) String payload) {
        return invokeCaseOperation(headers,
                containerId,
                caseId,
                processId,
                (Variant v, String type, Header... customHeaders) -> {
                    logger.debug("About to add dynamic subprocess stage {} in case {}", stageId, caseId);
                    this.caseManagementServiceBase.addDynamicSubprocess(containerId, caseId, stageId, processId, payload, type);

                    logger.debug("Returning CREATED response");
                    return createResponse("", v, Response.Status.CREATED, customHeaders);
                });
    }

    @ApiOperation(value="Triggers a specified ad hoc fragment in the specified stage of a specified case instance.",
            response=Void.class, code=201)
    @ApiResponses(value = { @ApiResponse(code = 500, message = "Unexpected error"),
            @ApiResponse(code = 404, message = "Case instance not found") })
    @PUT
    @Path(CASE_DYNAMIC_TASK_IN_STAGE_PUT_URI)
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response triggerAdHocNodeInStage(@javax.ws.rs.core.Context HttpHeaders headers,
            @ApiParam(value = "container id that case instance belongs to", required = true, example = "evaluation_1.0.0-SNAPSHOT") @PathParam(CONTAINER_ID) String containerId, 
            @ApiParam(value = "identifier of the case instance", required = true, example = "CASE-00000000001") @PathParam(CASE_ID) String caseId,
            @ApiParam(value = "identifier of the stage within case instance where adhoc fragment should be triggered", required = true, example = "stageOne") @PathParam(CASE_STAGE_ID) String stageId, 
            @ApiParam(value = "name of the adhoc fragment to be triggered", required = true) @PathParam(CASE_NODE_NAME) String adHocName,
            @ApiParam(value = "optional data to be given when triggering adhoc fragment", required = false, examples=@Example(value= {
                    @ExampleProperty(mediaType=JSON, value=VAR_MAP_JSON),
                    @ExampleProperty(mediaType=XML, value=VAR_MAP_XML)})) String payload) {
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

    @ApiOperation(value="Triggers a specified ad hoc fragment in a specified case instance.",
            response=Void.class, code=201)
    @ApiResponses(value = { @ApiResponse(code = 500, message = "Unexpected error"),
            @ApiResponse(code = 404, message = "Case instance not found") })
    @PUT
    @Path(CASE_DYNAMIC_TASK_PUT_URI)
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response triggerAdHocNode(@javax.ws.rs.core.Context HttpHeaders headers,
            @ApiParam(value = "container id that case instance belongs to", required = true, example = "evaluation_1.0.0-SNAPSHOT") @PathParam(CONTAINER_ID) String containerId, 
            @ApiParam(value = "identifier of the case instance", required = true, example = "CASE-00000000001") @PathParam(CASE_ID) String caseId,
            @ApiParam(value = "name of the adhoc fragment to be triggered", required = true, example = "sampleTask") @PathParam(CASE_NODE_NAME) String adHocName,
            @ApiParam(value = "optional data to be given when triggering adhoc fragment", required = false, examples=@Example(value= {
                    @ExampleProperty(mediaType=JSON, value=VAR_MAP_JSON),
                    @ExampleProperty(mediaType=XML, value=VAR_MAP_XML)})) String payload) {
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

    @ApiOperation(value="Returns milestones for a specified case instance.",
            response=CaseMilestoneList.class, code=200)
    @ApiResponses(value = { @ApiResponse(code = 500, message = "Unexpected error"),
            @ApiResponse(code = 404, message = "Case instance not found"), 
            @ApiResponse(code = 200, message = "Successfull response", examples=@Example(value= {
                    @ExampleProperty(mediaType=JSON, value=CASE_MILESTONES_JSON)})) })
    @GET
    @Path(CASE_MILESTONES_GET_URI)
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getCaseInstanceMilestones(@javax.ws.rs.core.Context HttpHeaders headers,
            @ApiParam(value = "container id that case instance belongs to", required = true, example = "evaluation_1.0.0-SNAPSHOT") @PathParam(CONTAINER_ID) String containerId, 
            @ApiParam(value = "identifier of the case instance", required = true, example = "CASE-00000000001") @PathParam(CASE_ID) String caseId,
            @ApiParam(value = "optional flag that allows to control which milestones to load - achieved only or actives ones too, defaults to true", required = false) @QueryParam("achievedOnly") @DefaultValue("true") boolean achievedOnly,
            @ApiParam(value = "optional pagination - at which page to start, defaults to 0 (meaning first)", required = false) @QueryParam("page") @DefaultValue("0") Integer page,
            @ApiParam(value = "optional pagination - size of the result, defaults to 10", required = false) @QueryParam("pageSize") @DefaultValue("10") Integer pageSize) {

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

    @ApiOperation(value="Returns stages for a specified case instance.",
            response=CaseStageList.class, code=200)
    @ApiResponses(value = { @ApiResponse(code = 500, message = "Unexpected error"),
            @ApiResponse(code = 404, message = "Case instance not found"), 
            @ApiResponse(code = 200, message = "Successfull response", examples=@Example(value= {
                    @ExampleProperty(mediaType=JSON, value=CASE_STAGES_JSON)})) })
    @GET
    @Path(CASE_STAGES_GET_URI)
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getCaseInstanceStages(@javax.ws.rs.core.Context HttpHeaders headers,
            @ApiParam(value = "container id that case instance belongs to", required = true, example = "evaluation_1.0.0-SNAPSHOT") @PathParam(CONTAINER_ID) String containerId, 
            @ApiParam(value = "identifier of the case instance", required = true, example = "CASE-00000000001") @PathParam(CASE_ID) String caseId,
            @ApiParam(value = "optional flag that allows to control which stages to load - active only or completed ones too, defaults to true", required = false) @QueryParam("activeOnly") @DefaultValue("true") boolean activeOnly,
            @ApiParam(value = "optional pagination - at which page to start, defaults to 0 (meaning first)", required = false) @QueryParam("page") @DefaultValue("0") Integer page, 
            @ApiParam(value = "optional pagination - size of the result, defaults to 10", required = false) @QueryParam("pageSize") @DefaultValue("10") Integer pageSize) {

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

    @ApiOperation(value="Returns ad hoc fragments for a specified case instance.",
            response=CaseAdHocFragmentList.class, code=200)
    @ApiResponses(value = { @ApiResponse(code = 500, message = "Unexpected error"),
            @ApiResponse(code = 404, message = "Case instance not found"), 
            @ApiResponse(code = 200, message = "Successfull response", examples=@Example(value= {
                    @ExampleProperty(mediaType=JSON, value=CASE_ADHOC_FRAGMENTS_JSON)})) })
    @GET
    @Path(CASE_AD_HOC_FRAGMENTS_GET_URI)
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getCaseInstanceAdHocFragments(@javax.ws.rs.core.Context HttpHeaders headers,
            @ApiParam(value = "container id that case instance belongs to", required = true, example = "evaluation_1.0.0-SNAPSHOT") @PathParam(CONTAINER_ID) String containerId, 
            @ApiParam(value = "identifier of the case instance", required = true, example = "CASE-00000000001") @PathParam(CASE_ID) String caseId) {

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

    @ApiOperation(value="Returns process instances in a specified case instance.",
            response=ProcessInstanceList.class, code=200)
    @ApiResponses(value = { @ApiResponse(code = 500, message = "Unexpected error"),
            @ApiResponse(code = 404, message = "Case instance not found"), 
            @ApiResponse(code = 200, message = "Successfull response", examples=@Example(value= {
                    @ExampleProperty(mediaType=JSON, value=PROCESS_INSTANCES_JSON)})) })
    @GET
    @Path(CASE_PROCESS_INSTANCES_GET_URI)
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getCaseInstanceProcessInstance(@javax.ws.rs.core.Context HttpHeaders headers,
            @ApiParam(value = "container id that case instance belongs to", required = true, example = "evaluation_1.0.0-SNAPSHOT") @PathParam(CONTAINER_ID) String containerId, 
            @ApiParam(value = "identifier of the case instance", required = true, example = "CASE-00000000001") @PathParam(CASE_ID) String caseId,
            @ApiParam(value = "optional process instance status (active, completed, aborted) - defaults ot active (1) only", required = false, allowableValues="1,2,3") @QueryParam("status") List<Integer> status,
            @ApiParam(value = "optional pagination - at which page to start, defaults to 0 (meaning first)", required = false) @QueryParam("page") @DefaultValue("0") Integer page, 
            @ApiParam(value = "optional pagination - size of the result, defaults to 10", required = false) @QueryParam("pageSize") @DefaultValue("10") Integer pageSize,
            @ApiParam(value = "optional sort column, no default", required = false) @QueryParam("sort") String sort, 
            @ApiParam(value = "optional sort direction (asc, desc) - defaults to asc", required = false) @QueryParam("sortOrder") @DefaultValue("true") boolean sortOrder) {

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
                    ProcessInstanceList responseObject = this.caseManagementRuntimeDataServiceBase.getProcessInstancesForCase(containerId, caseId, actualStatus, page, pageSize, sort, sortOrder);

                    logger.debug("Returning OK response with content '{}'", responseObject);
                    return createCorrectVariant(responseObject, headers, Response.Status.OK, customHeaders);
                });
    }

    @ApiOperation(value="Returns node instances for a specified case instance.",
            response=NodeInstanceList.class, code=200)
    @ApiResponses(value = { @ApiResponse(code = 500, message = "Unexpected error"),
            @ApiResponse(code = 404, message = "Case instance not found"), 
            @ApiResponse(code = 200, message = "Successfull response", examples=@Example(value= {
                    @ExampleProperty(mediaType=JSON, value=NODE_INSTANCES_JSON)})) })
    @GET
    @Path(CASE_NODE_INSTANCES_GET_URI)
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getCaseInstanceActiveNodes(@javax.ws.rs.core.Context HttpHeaders headers,
            @ApiParam(value = "container id that case instance belongs to", required = true, example = "evaluation_1.0.0-SNAPSHOT") @PathParam(CONTAINER_ID) String containerId, 
            @ApiParam(value = "identifier of the case instance", required = true, example = "CASE-00000000001") @PathParam(CASE_ID) String caseId, 
            @ApiParam(value = "optional flag that allows to control which node instances to load - active or completed, defaults to false loading only active ones", required = false) @QueryParam("completed") @DefaultValue("false") Boolean completed,
            @ApiParam(value = "optional pagination - at which page to start, defaults to 0 (meaning first)", required = false) @QueryParam("page") @DefaultValue("0") Integer page, 
            @ApiParam(value = "optional pagination - size of the result, defaults to 10", required = false) @QueryParam("pageSize") @DefaultValue("10") Integer pageSize) {

        return invokeCaseOperation(headers,
                containerId,
                caseId,
                (Variant v, String type, Header... customHeaders) -> {
                    logger.debug("About to look for active nodes in case {}", caseId);
                    NodeInstanceList responseObject = null;

                    if (completed) {
                        responseObject = this.caseManagementRuntimeDataServiceBase.getCompletedNodes(containerId, caseId, page, pageSize);
                    } else {
                        responseObject = this.caseManagementRuntimeDataServiceBase.getActiveNodes(containerId, caseId, page, pageSize);
                    }
                    logger.debug("Returning OK response with content '{}'", responseObject);
                    return createCorrectVariant(responseObject, headers, Response.Status.OK, customHeaders);
                });
    }

    @ApiOperation(value="Returns role assignments for a specified case instance.",
            response=CaseRoleAssignmentList.class, code=200)
    @ApiResponses(value = { @ApiResponse(code = 500, message = "Unexpected error"),
            @ApiResponse(code = 404, message = "Case instance not found"), 
            @ApiResponse(code = 200, message = "Successfull response", examples=@Example(value= {
                    @ExampleProperty(mediaType=JSON, value=CASE_ROLES_ASSIGNMENTS_JSON)})) })
    @GET
    @Path(CASE_ROLES_GET_URI)
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getCaseInstanceRoleAssignments(@javax.ws.rs.core.Context HttpHeaders headers,
            @ApiParam(value = "container id that case instance belongs to", required = true, example = "evaluation_1.0.0-SNAPSHOT") @PathParam(CONTAINER_ID) String containerId, 
            @ApiParam(value = "identifier of the case instance", required = true, example = "CASE-00000000001") @PathParam(CASE_ID) String caseId) {

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

    @ApiOperation(value="Assigns a specified case role to a specified user and group for a specified case instance.",
            response=Void.class, code=201)
    @ApiResponses(value = { @ApiResponse(code = 500, message = "Unexpected error"),
            @ApiResponse(code = 404, message = "Case instance not found") })
    @PUT
    @Path(CASE_ROLES_PUT_URI)
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response addRoleAssignment(@javax.ws.rs.core.Context HttpHeaders headers,
            @ApiParam(value = "container id that case instance belongs to", required = true, example = "evaluation_1.0.0-SNAPSHOT") @PathParam(CONTAINER_ID) String containerId, 
            @ApiParam(value = "identifier of the case instance", required = true, example = "CASE-00000000001") @PathParam(CASE_ID) String caseId,
            @ApiParam(value = "name of the case role the assignment should be set", required = true, example = "owner") @PathParam(CASE_ROLE_NAME) String roleName,
            @ApiParam(value = "user to be aded to case role for given case instance", required = true) @QueryParam("user") String user, 
            @ApiParam(value = "group to be aded to case role for given case instance", required = true) @QueryParam("group") String group) {
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

    @ApiOperation(value="Removes an assigned case role from a specified user and group for a specified case instance.",
            response=Void.class, code=204)
    @ApiResponses(value = { @ApiResponse(code = 500, message = "Unexpected error"),
            @ApiResponse(code = 404, message = "Case instance not found") })
    @DELETE
    @Path(CASE_ROLES_DELETE_URI)
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response removeRoleAssignment(@javax.ws.rs.core.Context HttpHeaders headers,
            @ApiParam(value = "container id that case instance belongs to", required = true, example = "evaluation_1.0.0-SNAPSHOT") @PathParam(CONTAINER_ID) String containerId, 
            @ApiParam(value = "identifier of the case instance", required = true, example = "CASE-00000000001") @PathParam(CASE_ID) String caseId,
            @ApiParam(value = "name of the case role the assignment should be removed", required = true, example = "owner") @PathParam(CASE_ROLE_NAME) String roleName,
            @ApiParam(value = "user to be removed from case role for given case instance", required = true) @QueryParam("user") String user, 
            @ApiParam(value = "group to be removed from case role for given case instance", required = true) @QueryParam("group") String group) {
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

    @ApiOperation(value="Returns comments from a specified case instance.",
            response=CaseCommentList.class, code=200)
    @ApiResponses(value = { @ApiResponse(code = 500, message = "Unexpected error"),
            @ApiResponse(code = 404, message = "Case instance not found"), 
            @ApiResponse(code = 200, message = "Successfull response", examples=@Example(value= {
                    @ExampleProperty(mediaType=JSON, value=CASE_COMMENTS_JSON)})) })
    @GET
    @Path(CASE_COMMENTS_GET_URI)
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getCaseInstanceComments(@javax.ws.rs.core.Context HttpHeaders headers,
            @ApiParam(value = "container id that case instance belongs to", required = true, example = "evaluation_1.0.0-SNAPSHOT") @PathParam(CONTAINER_ID) String containerId, 
            @ApiParam(value = "identifier of the case instance", required = true, example = "CASE-00000000001") @PathParam(CASE_ID) String caseId,
            @ApiParam(value = "optional sort column, no default", required = false) @QueryParam("sort") String sort,
            @ApiParam(value = "optional pagination - at which page to start, defaults to 0 (meaning first)", required = false) @QueryParam("page") @DefaultValue("0") Integer page, 
            @ApiParam(value = "optional pagination - size of the result, defaults to 10", required = false) @QueryParam("pageSize") @DefaultValue("10") Integer pageSize) {

        return invokeCaseOperation(headers,
                containerId,
                caseId,
                (Variant v, String type, Header... customHeaders) -> {
                    logger.debug("About to look for comments in case {}", caseId);
                    CaseCommentList responseObject = this.caseManagementServiceBase.getComments(containerId, caseId, sort, page, pageSize);

                    logger.debug("Returning OK response with content '{}'", responseObject);
                    return createCorrectVariant(responseObject, headers, Response.Status.OK, customHeaders);
                });
    }

    @ApiOperation(value="Adds a comment to a specified case instance.",
            response=Void.class, code=201)
    @ApiResponses(value = { @ApiResponse(code = 500, message = "Unexpected error"),
            @ApiResponse(code = 404, message = "Case instance not found") })
    @POST
    @Path(CASE_COMMENTS_POST_URI)
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response addComment(@javax.ws.rs.core.Context HttpHeaders headers,
            @ApiParam(value = "container id that case instance belongs to", required = true, example = "evaluation_1.0.0-SNAPSHOT") @PathParam(CONTAINER_ID) String containerId, 
            @ApiParam(value = "identifier of the case instance", required = true, example = "CASE-00000000001") @PathParam(CASE_ID) String caseId,
            @ApiParam(value = "optional user id to be used instead of authenticated user - only when bypass authenticated user is enabled", required = false) @QueryParam("author") String author, 
            @ApiParam(value = "optional role name(s) that given comment should be restricted to", required = false) @QueryParam("restrictedTo") List<String> restrictions,
            @ApiParam(value = "actual content of the comment to be added as String", required = true) String payload) {
        return invokeCaseOperation(headers,
                containerId,
                caseId,
                (Variant v, String type, Header... customHeaders) -> {
                    logger.debug("About to add comment to case {}", caseId);
                    String commentId = this.caseManagementServiceBase.addCommentToCase(containerId, caseId, author, restrictions, payload, type);

                    logger.debug("Returning CREATED response");
                    return createResponse(RestUtils.toIdentifier(commentId), v, Response.Status.CREATED, customHeaders);
                });
    }

    @ApiOperation(value="Updates a specified comment in a specified case instance.",
            response=Void.class, code=201)
    @ApiResponses(value = { @ApiResponse(code = 500, message = "Unexpected error"),
            @ApiResponse(code = 404, message = "Case instance not found") })
    @PUT
    @Path(CASE_COMMENTS_PUT_URI)
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response updateComment(@javax.ws.rs.core.Context HttpHeaders headers,
            @ApiParam(value = "container id that case instance belongs to", required = true, example = "evaluation_1.0.0-SNAPSHOT") @PathParam(CONTAINER_ID) String containerId, 
            @ApiParam(value = "identifier of the case instance", required = true, example = "CASE-00000000001") @PathParam(CASE_ID) String caseId,
            @ApiParam(value = "identifier of the comment to be updated", required = true, example = "xxx-yyy-zzz") @PathParam(CASE_COMMENT_ID) String commentId, 
            @ApiParam(value = "optional user id to be used instead of authenticated user - only when bypass authenticated user is enabled", required = false) @QueryParam("author") String author, 
            @ApiParam(value = "optional role name(s) that given comment should be restricted to", required = false) @QueryParam("restrictedTo") List<String> restrictions,
            @ApiParam(value = "actual content of the comment to be updated to as String", required = true) String payload) {
        return invokeCaseOperation(headers,
                containerId,
                caseId,
                (Variant v, String type, Header... customHeaders) -> {
                    logger.debug("About to update comment {} in case {}", commentId, caseId);

                    try {
                        this.caseManagementServiceBase.updateCommentInCase(containerId, caseId, commentId, author, restrictions, payload, type);

                        logger.debug("Returning CREATED response");
                        return createResponse("", v, Response.Status.CREATED, customHeaders);
                    } catch(CaseCommentNotFoundException e) {
                        return notFound(
                                MessageFormat.format(CASE_COMMENT_NOT_FOUND, commentId, caseId), v, customHeaders);
                    }
                });
    }

    @ApiOperation(value="Deletes a specified comment in a specified case instance.",
            response=Void.class, code=204)
    @ApiResponses(value = { @ApiResponse(code = 500, message = "Unexpected error"),
            @ApiResponse(code = 404, message = "Case instance not found") })
    @DELETE
    @Path(CASE_COMMENTS_DELETE_URI)
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response removeComment(@javax.ws.rs.core.Context HttpHeaders headers,
            @ApiParam(value = "container id that case instance belongs to", required = true, example = "evaluation_1.0.0-SNAPSHOT") @PathParam(CONTAINER_ID) String containerId, 
            @ApiParam(value = "identifier of the case instance", required = true, example = "CASE-00000000001") @PathParam(CASE_ID) String caseId,
            @ApiParam(value = "identifier of the comment to be removed", required = true, example = "xxx-yyy-zzz") @PathParam(CASE_COMMENT_ID) String commentId) {
        return invokeCaseOperation(headers,
                containerId,
                caseId,
                (Variant v, String type, Header... customHeaders) -> {
                    logger.debug("About to remove comment {} from case {}", commentId, caseId);

                    try {
                        this.caseManagementServiceBase.removeCommentFromCase(containerId, caseId, commentId);

                        logger.debug("Returning NO_CONTENT response");
                        return noContent(v, customHeaders);
                    } catch(CaseCommentNotFoundException e) {
                        return notFound(
                                MessageFormat.format(CASE_COMMENT_NOT_FOUND, commentId, caseId), v, customHeaders);
                    }
                });
    }

    /*
     * basic queries through container
     */

    @ApiOperation(value="Returns case instances in a specified KIE container.",
            response=CaseInstanceList.class, code=200)
    @ApiResponses(value = { @ApiResponse(code = 500, message = "Unexpected error"), 
            @ApiResponse(code = 200, message = "Successfull response", examples=@Example(value= {
                    @ExampleProperty(mediaType=JSON, value=CASE_INSTANCES_JSON)}))})
    @GET
    @Path(CASE_INSTANCES_GET_URI)
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getCaseInstancesByContainer(@javax.ws.rs.core.Context HttpHeaders headers,
            @ApiParam(value = "container id that should be used to filter case instances", required = true, example = "evaluation_1.0.0-SNAPSHOT") @PathParam(CONTAINER_ID) String containerId,
            @ApiParam(value = "optional case instance status (open, closed, canceled) - defaults ot open (1) only", required = false, allowableValues="open,closed,cancelled") @QueryParam("status") List<String> status,
            @ApiParam(value = "optional pagination - at which page to start, defaults to 0 (meaning first)", required = false) @QueryParam("page") @DefaultValue("0") Integer page, 
            @ApiParam(value = "optional pagination - size of the result, defaults to 10", required = false) @QueryParam("pageSize") @DefaultValue("10") Integer pageSize,
            @ApiParam(value = "optional sort column, no default", required = false) @QueryParam("sort") String sort, 
            @ApiParam(value = "optional sort direction (asc, desc) - defaults to asc", required = false) @QueryParam("sortOrder") @DefaultValue("true") boolean sortOrder) {

        return invokeCaseOperation(headers,
                containerId,
                null,
                (Variant v, String type, Header... customHeaders) -> {
                    List<String> actualStatus = status;
                    if (status == null || status.isEmpty()) {
                        actualStatus = new ArrayList<String>();
                        actualStatus.add(CaseStatus.OPEN.getName());
                    }
                    logger.debug("About to look for case instances in container {} with status {}", containerId, actualStatus);
                    CaseInstanceList responseObject = this.caseManagementRuntimeDataServiceBase.getCaseInstancesByContainer(containerId, actualStatus, page, pageSize, sort, sortOrder);

                    logger.debug("Returning OK response with content '{}'", responseObject);
                    return createCorrectVariant(responseObject, headers, Response.Status.OK, customHeaders);
                });
    }

    @ApiOperation(value="Returns case instances for a specified case definition.",
            response=CaseInstanceList.class, code=200)
    @ApiResponses(value = { @ApiResponse(code = 500, message = "Unexpected error"), 
            @ApiResponse(code = 200, message = "Successfull response", examples=@Example(value= {
                    @ExampleProperty(mediaType=JSON, value=CASE_INSTANCES_JSON)}))})
    @GET
    @Path(CASE_INSTANCES_BY_DEF_GET_URI)
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getCaseInstancesByDefinition(@javax.ws.rs.core.Context HttpHeaders headers,
            @ApiParam(value = "container id that should be used to filter case instances", required = true, example = "evaluation_1.0.0-SNAPSHOT") @PathParam(CONTAINER_ID) String containerId, 
            @ApiParam(value = "case definition id that should be used to filter case instances", required = true, example = "CASE-00000000001") @PathParam(CASE_DEF_ID) String caseDefId,
            @ApiParam(value = "optional case instance status (open, closed, canceled) - defaults ot open (1) only", required = false, allowableValues="open,closed,cancelled") @QueryParam("status") List<String> status,
            @ApiParam(value = "optional pagination - at which page to start, defaults to 0 (meaning first)", required = false) @QueryParam("page") @DefaultValue("0") Integer page, 
            @ApiParam(value = "optional pagination - size of the result, defaults to 10", required = false) @QueryParam("pageSize") @DefaultValue("10") Integer pageSize,
            @ApiParam(value = "optional sort column, no default", required = false) @QueryParam("sort") String sort, 
            @ApiParam(value = "optional sort direction (asc, desc) - defaults to asc", required = false) @QueryParam("sortOrder") @DefaultValue("true") boolean sortOrder) {

        return invokeCaseOperation(headers,
                containerId,
                null,
                (Variant v, String type, Header... customHeaders) -> {
                    List<String> actualStatus = status;
                    if (status == null || status.isEmpty()) {
                        actualStatus = new ArrayList<String>();
                        actualStatus.add(CaseStatus.OPEN.getName());
                    }
                    logger.debug("About to look for case instances with case definition id {} with status {}", caseDefId, actualStatus);
                    CaseInstanceList responseObject = this.caseManagementRuntimeDataServiceBase.getCaseInstancesByDefinition(containerId, caseDefId, actualStatus, page, pageSize, sort, sortOrder);

                    logger.debug("Returning OK response with content '{}'", responseObject);
                    return createCorrectVariant(responseObject, headers, Response.Status.OK, customHeaders);
                });
    }

    @ApiOperation(value="Returns case definitions in a specified KIE container.",
            response=CaseDefinitionList.class, code=200)
    @ApiResponses(value = { @ApiResponse(code = 500, message = "Unexpected error"), 
            @ApiResponse(code = 200, message = "Successfull response", examples=@Example(value= {
                    @ExampleProperty(mediaType=JSON, value=CASE_DEFINITIONS_JSON)}))})
    @GET
    @Path(CASE_ALL_DEFINITIONS_GET_URI)
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getCaseDefinitionsByContainer(@javax.ws.rs.core.Context HttpHeaders headers,
            @ApiParam(value = "container id that should be used to filter case definitions", required = true, example = "evaluation_1.0.0-SNAPSHOT") @PathParam(CONTAINER_ID) String containerId,
            @ApiParam(value = "optional pagination - at which page to start, defaults to 0 (meaning first)", required = false) @QueryParam("page") @DefaultValue("0") Integer page, 
            @ApiParam(value = "optional pagination - size of the result, defaults to 10", required = false) @QueryParam("pageSize") @DefaultValue("10") Integer pageSize,
            @ApiParam(value = "optional sort column, no default", required = false) @QueryParam("sort") String sort, 
            @ApiParam(value = "optional sort direction (asc, desc) - defaults to asc", required = false) @QueryParam("sortOrder") @DefaultValue("true") boolean sortOrder) {

        return invokeCaseOperation(headers,
                containerId,
                null,
                (Variant v, String type, Header... customHeaders) -> {

                    logger.debug("About to look for case definitions in container {}", containerId);
                    CaseDefinitionList responseObject = this.caseManagementRuntimeDataServiceBase.getCaseDefinitionsByContainer(containerId, page, pageSize, sort, sortOrder);

                    logger.debug("Returning OK response with content '{}'", responseObject);
                    return createCorrectVariant(responseObject, headers, Response.Status.OK, customHeaders);
                });
    }

    @ApiOperation(value="Returns information about a specified case definition.",
            response=CaseDefinition.class, code=200)
    @ApiResponses(value = { @ApiResponse(code = 500, message = "Unexpected error"), 
            @ApiResponse(code = 200, message = "Successfull response", examples=@Example(value= {
                    @ExampleProperty(mediaType=JSON, value=CASE_DEFINITION_JSON)}))})
    @GET
    @Path(CASE_DEFINITIONS_BY_ID_GET_URI)
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getCaseDefinitionsByDefinition(@javax.ws.rs.core.Context HttpHeaders headers,
            @ApiParam(value = "container id that should be used to filter case definitions", required = true, example = "evaluation_1.0.0-SNAPSHOT") @PathParam(CONTAINER_ID) String containerId, 
            @ApiParam(value = "case definition id that should be loaded", required = true, example = "CASE-00000000001") @PathParam(CASE_DEF_ID) String caseDefId) {

        return invokeCaseOperation(headers,
                containerId,
                null,
                (Variant v, String type, Header... customHeaders) -> {

                    logger.debug("About to look for case definition with id {} in container {}", caseDefId, containerId);

                    try {
                        CaseDefinition responseObject = this.caseManagementRuntimeDataServiceBase.getCaseDefinition(containerId, caseDefId);

                        logger.debug("Returning OK response with content '{}'", responseObject);
                        return createCorrectVariant(responseObject, headers, Response.Status.OK, customHeaders);
                    } catch (IllegalStateException e) {
                        return notFound(
                                MessageFormat.format(CASE_DEFINITION_NOT_FOUND, caseDefId, containerId), v, customHeaders);
                    }
                });
    }

}
