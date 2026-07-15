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

import java.util.List;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Variant;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.kie.api.runtime.query.QueryContext;
import org.kie.server.api.model.cases.CaseDefinitionList;
import org.kie.server.api.model.cases.CaseFileDataItemList;
import org.kie.server.api.model.cases.CaseInstanceCustomVarsList;
import org.kie.server.api.model.cases.CaseInstanceList;
import org.kie.server.api.model.cases.CaseUserTaskWithVariablesList;
import org.kie.server.api.model.definition.ProcessDefinitionList;
import org.kie.server.api.model.instance.TaskSummaryList;
import org.kie.server.api.rest.RestURI;
import org.kie.server.remote.rest.common.Header;
import org.kie.server.remote.rest.common.marker.KieServerEndpoint;
import org.kie.server.remote.rest.common.marker.KieServerEndpoint.EndpointType;
import org.kie.server.services.api.KieServerRegistry;
import org.kie.server.services.casemgmt.CaseManagementRuntimeDataServiceBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.kie.server.api.rest.RestURI.CASE_ALL_INSTANCES_GET_URI;
import static org.kie.server.api.rest.RestURI.CASE_ALL_PROCESSES_INSTANCES_GET_URI;
import static org.kie.server.api.rest.RestURI.CASE_FILE_GET_URI;
import static org.kie.server.api.rest.RestURI.CASE_ID;
import static org.kie.server.api.rest.RestURI.CASE_INSTANCES_BY_ROLE_GET_URI;
import static org.kie.server.api.rest.RestURI.CASE_PROCESSES_BY_CONTAINER_INSTANCES_GET_URI;
import static org.kie.server.api.rest.RestURI.CASE_QUERY_URI;
import static org.kie.server.api.rest.RestURI.CASE_ROLE_NAME;
import static org.kie.server.api.rest.RestURI.CASE_TASKS_AS_ADMIN_GET_URI;
import static org.kie.server.api.rest.RestURI.CASE_TASKS_AS_POT_OWNER_GET_URI;
import static org.kie.server.api.rest.RestURI.CASE_TASKS_AS_STAKEHOLDER_GET_URI;
import static org.kie.server.api.rest.RestURI.CONTAINER_ID;
import static org.kie.server.remote.rest.casemgmt.docs.ParameterSamples.CASE_DEFINITIONS_JSON;
import static org.kie.server.remote.rest.casemgmt.docs.ParameterSamples.CASE_INSTANCES_JSON;
import static org.kie.server.remote.rest.casemgmt.docs.ParameterSamples.GET_CASE_FILE_DATA_RESPONSE_JSON;
import static org.kie.server.remote.rest.casemgmt.docs.ParameterSamples.GET_PROCESS_DEFS_RESPONSE_JSON;
import static org.kie.server.remote.rest.casemgmt.docs.ParameterSamples.GET_TASK_SUMMARY_RESPONSE_JSON;
import static org.kie.server.remote.rest.casemgmt.docs.ParameterSamples.JSON;
import static org.kie.server.remote.rest.common.util.RestUtils.buildConversationIdHeader;
import static org.kie.server.remote.rest.common.util.RestUtils.createCorrectVariant;
import static org.kie.server.remote.rest.common.util.RestUtils.errorMessage;
import static org.kie.server.remote.rest.common.util.RestUtils.getContentType;
import static org.kie.server.remote.rest.common.util.RestUtils.getVariant;
import static org.kie.server.remote.rest.common.util.RestUtils.internalServerError;

@Tag(name="Case queries")
@Path("server/" + CASE_QUERY_URI)
public class CaseQueryResource extends AbstractCaseResource {

    private static final Logger logger = LoggerFactory.getLogger(CaseQueryResource.class);

    public CaseQueryResource() {

    }

    public CaseQueryResource(
            final CaseManagementRuntimeDataServiceBase caseManagementRuntimeDataServiceBase,
            final KieServerRegistry context) {
        super(caseManagementRuntimeDataServiceBase, context);
    }

    @Operation(summary="Returns cases instances with authentication checks.")
    @ApiResponses(value = { @ApiResponse(responseCode = "500", description = "Unexpected error"), 
            @ApiResponse(responseCode = "200", description = "Successful response", content = {
                @Content(mediaType = JSON, examples = {@ExampleObject(value = CASE_INSTANCES_JSON)})
            })})
    @GET
    @Path(CASE_ALL_INSTANCES_GET_URI)
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getCaseInstances(@jakarta.ws.rs.core.Context HttpHeaders headers,
            @Parameter(description = "data item name that case instances will be filtered by", required = false) @QueryParam("dataItemName") String dataItemName, 
            @Parameter(description = "data item value that case instances will be filtered by", required = false) @QueryParam("dataItemValue") String dataItemValue,
            @Parameter(description = "case instance owner that case instances will be filtered by", required = false) @QueryParam("owner") String owner,
            @Parameter(description = "optional case instance status (open, closed, canceled) - defaults ot open (1) only", required = false, allowableValues="open,closed,cancelled") @QueryParam("status") List<String> status,
            @Parameter(description = "optional pagination - at which page to start, defaults to 0 (meaning first)", required = false) @QueryParam("page") @DefaultValue("0") Integer page, 
            @Parameter(description = "optional pagination - size of the result, defaults to 10", required = false) @QueryParam("pageSize") @DefaultValue("10") Integer pageSize,
            @Parameter(description = "optional sort column, no default", required = false) @QueryParam("sort") String sort, 
            @Parameter(description = "optional sort direction (asc, desc) - defaults to asc", required = false) @QueryParam("sortOrder") @DefaultValue("true") boolean sortOrder,
            @Parameter(description = "optional flag to load data when loading case instance", required = false) @QueryParam("withData") @DefaultValue("false") boolean withData) {

        return invokeCaseOperation(headers,
                "",
                null,
                (Variant v, String type, Header... customHeaders) -> {

                    CaseInstanceList responseObject = null;
                    if (dataItemName != null && !dataItemName.isEmpty() && dataItemValue != null && !dataItemValue.isEmpty()) {
                        logger.debug("About to look for case instances by data item name {} and value {} with status {}", dataItemName, dataItemValue, status);
                        responseObject = this.caseManagementRuntimeDataServiceBase.getCaseInstancesByCaseFileData(dataItemName, dataItemValue, status, page, pageSize, sort, sortOrder, withData);
                    } else if (dataItemName != null && !dataItemName.isEmpty()) {
                        logger.debug("About to look for case instances by data item name {} with status {}", dataItemName, status);
                        responseObject = this.caseManagementRuntimeDataServiceBase.getCaseInstancesByCaseFileData(dataItemName, null, status, page, pageSize, sort, sortOrder, withData);
                    } else if (owner != null && !owner.isEmpty()) {
                        logger.debug("About to look for case instances owned by {} with status {}", owner, status);
                        responseObject = this.caseManagementRuntimeDataServiceBase.getCaseInstancesOwnedBy(owner,
                                                                                                           status, page, pageSize, sort, sortOrder, withData);
                    } else {
                        logger.debug("About to look for case instances with status {}", status);
                        responseObject = this.caseManagementRuntimeDataServiceBase.getCaseInstancesAnyRole(status, page, pageSize, sort, sortOrder, withData);
                    }

                    logger.debug("Returning OK response with content '{}'", responseObject);
                    return createCorrectVariant(responseObject, headers, Response.Status.OK, customHeaders);
                });
    }

    @Operation(summary="Returns cases instances that involve the querying user in a specified role.")
    @ApiResponses(value = { @ApiResponse(responseCode = "500", description = "Unexpected error"), 
            @ApiResponse(responseCode = "200", description = "Successful response", content = {
                @Content(mediaType = JSON, examples = {@ExampleObject(value = CASE_INSTANCES_JSON)})
            })})
    @GET
    @Path(CASE_INSTANCES_BY_ROLE_GET_URI)
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getCaseInstancesByRole(@jakarta.ws.rs.core.Context HttpHeaders headers,
            @Parameter(description = "case role that instances should be found for", required = true, example = "owner") @PathParam(CASE_ROLE_NAME) String roleName, 
            @Parameter(description = "optional case instance status (open, closed, canceled) - defaults ot open (1) only", required = false, allowableValues="open,closed,cancelled") @QueryParam("status") List<String> status,
            @Parameter(description = "optional pagination - at which page to start, defaults to 0 (meaning first)", required = false) @QueryParam("page") @DefaultValue("0") Integer page, 
            @Parameter(description = "optional pagination - size of the result, defaults to 10", required = false) @QueryParam("pageSize") @DefaultValue("10") Integer pageSize,
            @Parameter(description = "optional sort column, no default", required = false) @QueryParam("sort") String sort, 
            @Parameter(description = "optional sort direction (asc, desc) - defaults to asc", required = false) @QueryParam("sortOrder") @DefaultValue("true") boolean sortOrder,
            @Parameter(description = "optional flag to load data when loading case instance", required = false) @QueryParam("withData") @DefaultValue("false") boolean withData) {

        return invokeCaseOperation(headers,
                "",
                null,
                (Variant v, String type, Header... customHeaders) -> {

                    logger.debug("About to look for case instances with status {}", status);
                    CaseInstanceList responseObject = this.caseManagementRuntimeDataServiceBase.getCaseInstancesByRole(roleName, status, page, pageSize, sort, sortOrder, withData);

                    logger.debug("Returning OK response with content '{}'", responseObject);
                    return createCorrectVariant(responseObject, headers, Response.Status.OK, customHeaders);
                });
    }

    /*
     * case definition methods
     */
    
    @Operation(summary="Returns a specified case definition from all KIE containers.")
    @ApiResponses(value = { @ApiResponse(responseCode = "500", description = "Unexpected error"), 
            @ApiResponse(responseCode = "200", description = "Successful response", content = {
                @Content(mediaType = JSON, examples = {@ExampleObject(value = CASE_DEFINITIONS_JSON)})
            })})
    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getCaseDefinitions(@jakarta.ws.rs.core.Context HttpHeaders headers,
            @Parameter(description = "case definition id or name that case definitions will be filtered by", required = true) @QueryParam("filter") String filter,
            @Parameter(description = "optional pagination - at which page to start, defaults to 0 (meaning first)", required = false) @QueryParam("page") @DefaultValue("0") Integer page, 
            @Parameter(description = "optional pagination - size of the result, defaults to 10", required = false) @QueryParam("pageSize") @DefaultValue("10") Integer pageSize,
            @Parameter(description = "optional sort column, no default", required = false) @QueryParam("sort") String sort, 
            @Parameter(description = "optional sort direction (asc, desc) - defaults to asc", required = false) @QueryParam("sortOrder") @DefaultValue("true") boolean sortOrder) {

        return invokeCaseOperation(headers,
                "",
                null,
                (Variant v, String type, Header... customHeaders) -> {

                    logger.debug("About to look for case definitions with filter {}", filter);
                    CaseDefinitionList responseObject = this.caseManagementRuntimeDataServiceBase.getCaseDefinitions(filter, page, pageSize, sort, sortOrder);

                    logger.debug("Returning OK response with content '{}'", responseObject);
                    return createCorrectVariant(responseObject, headers, Response.Status.OK, customHeaders);
                });
    }

    /*
     * process definition methods
     */
    @Operation(summary="Returns a specified process associated with case definitions from all KIE containers.")
    @ApiResponses(value = { @ApiResponse(responseCode = "500", description = "Unexpected error"), 
            @ApiResponse(responseCode = "200", description = "Successful response", content = {
                @Content(mediaType = JSON, examples = {@ExampleObject(value = GET_PROCESS_DEFS_RESPONSE_JSON)})
            })})
    @GET
    @Path(CASE_ALL_PROCESSES_INSTANCES_GET_URI)
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getProcessDefinitions(@jakarta.ws.rs.core.Context HttpHeaders headers,
            @Parameter(description = "process definition id or name that process definitions will be filtered by", required = true) @QueryParam("filter") String filter,
            @Parameter(description = "optional pagination - at which page to start, defaults to 0 (meaning first)", required = false) @QueryParam("page") @DefaultValue("0") Integer page, 
            @Parameter(description = "optional pagination - size of the result, defaults to 10", required = false) @QueryParam("pageSize") @DefaultValue("10") Integer pageSize,
            @Parameter(description = "optional sort column, no default", required = false) @QueryParam("sort") String sort, 
            @Parameter(description = "optional sort direction (asc, desc) - defaults to asc", required = false) @QueryParam("sortOrder") @DefaultValue("true") boolean sortOrder) {

        return invokeCaseOperation(headers,
                "",
                null,
                (Variant v, String type, Header... customHeaders) -> {

                    logger.debug("About to look for process definitions with filter {}", filter);
                    ProcessDefinitionList responseObject = this.caseManagementRuntimeDataServiceBase.getProcessDefinitions(filter, null, page, pageSize, sort, sortOrder);

                    logger.debug("Returning OK response with content '{}'", responseObject);
                    return createCorrectVariant(responseObject, headers, Response.Status.OK, customHeaders);
                });
    }

    @Operation(summary="Returns processes associated with case definitions in a specified KIE container.")
    @ApiResponses(value = { @ApiResponse(responseCode = "500", description = "Unexpected error"), 
            @ApiResponse(responseCode = "200", description = "Successful response", content = {
                @Content(mediaType = JSON, examples = {@ExampleObject(value = GET_PROCESS_DEFS_RESPONSE_JSON)})
            })})
    @GET
    @Path(CASE_PROCESSES_BY_CONTAINER_INSTANCES_GET_URI)
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getProcessDefinitionsByContainer(@jakarta.ws.rs.core.Context HttpHeaders headers, 
            @Parameter(description = "container id that process definitions should be filtered by", required = true, example = "evaluation_1.0.0-SNAPSHOT") @PathParam(CONTAINER_ID) String containerId,
            @Parameter(description = "optional pagination - at which page to start, defaults to 0 (meaning first)", required = false) @QueryParam("page") @DefaultValue("0") Integer page, 
            @Parameter(description = "optional pagination - size of the result, defaults to 10", required = false) @QueryParam("pageSize") @DefaultValue("10") Integer pageSize,
            @Parameter(description = "optional sort column, no default", required = false) @QueryParam("sort") String sort, 
            @Parameter(description = "optional sort direction (asc, desc) - defaults to asc", required = false) @QueryParam("sortOrder") @DefaultValue("true") boolean sortOrder) {

        return invokeCaseOperation(headers,
                "",
                null,
                (Variant v, String type, Header... customHeaders) -> {

                    logger.debug("About to look for process definitions with container id {}", containerId);
                    ProcessDefinitionList responseObject = this.caseManagementRuntimeDataServiceBase.getProcessDefinitions(null, containerId, page, pageSize, sort, sortOrder);

                    logger.debug("Returning OK response with content '{}'", responseObject);
                    return createCorrectVariant(responseObject, headers, Response.Status.OK, customHeaders);
                });
    }

    /*
     * Case tasks
     */

    @Operation(summary="Returns tasks for potential owners in a specified case instance.")
    @ApiResponses(value = { @ApiResponse(responseCode = "500", description = "Unexpected error"), 
            @ApiResponse(responseCode = "200", description = "Successful response", content = {
                @Content(mediaType = JSON, examples = {@ExampleObject(value = GET_TASK_SUMMARY_RESPONSE_JSON)})
            })})
    @GET
    @Path(CASE_TASKS_AS_POT_OWNER_GET_URI)
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getCaseInstanceTasksAsPotentialOwner(@jakarta.ws.rs.core.Context HttpHeaders headers,
            @Parameter(description = "case instance identifier that tasks should belong to", required = true, example = "CASE-00000000011") @PathParam(CASE_ID) String caseId,
            @Parameter(description = "optional user id to be used instead of authenticated user - only when bypass authenticated user is enabled", required = false) @QueryParam("user") String user, 
            @Parameter(description = "optional task status (Created, Ready, Reserved, InProgress, Suspended, Completed, Failed, Error, Exited, Obsolete)", required = false, allowableValues="Created, Ready, Reserved,InProgress,Suspended,Completed,Failed,Error,Exited,Obsolete") @QueryParam("status") List<String> status,
            @Parameter(description = "optional pagination - at which page to start, defaults to 0 (meaning first)", required = false) @QueryParam("page") @DefaultValue("0") Integer page, 
            @Parameter(description = "optional pagination - size of the result, defaults to 10", required = false) @QueryParam("pageSize") @DefaultValue("10") Integer pageSize,
            @Parameter(description = "optional sort column, no default", required = false) @QueryParam("sort") String sort, 
            @Parameter(description = "optional sort direction (asc, desc) - defaults to asc", required = false) @QueryParam("sortOrder") @DefaultValue("true") boolean sortOrder) {

        return invokeCaseOperation(headers,
                "",
                null,
                (Variant v, String type, Header... customHeaders) -> {
                    logger.debug("About to look for case instance {} tasks with status {} assigned to potential owner {}", caseId, status, user);
                    TaskSummaryList responseObject = this.caseManagementRuntimeDataServiceBase.getCaseTasks(caseId, user, status, page, pageSize, sort, sortOrder);

                    logger.debug("Returning OK response with content '{}'", responseObject);
                    return createCorrectVariant(responseObject, headers, Response.Status.OK, customHeaders);
                });
    }

    @Operation(summary="Returns tasks for business administrators in a specified case instance.")
    @ApiResponses(value = { @ApiResponse(responseCode = "500", description = "Unexpected error"), 
            @ApiResponse(responseCode = "200", description = "Successful response", content = {
                @Content(mediaType = JSON, examples = {@ExampleObject(value = GET_TASK_SUMMARY_RESPONSE_JSON)})
            })})
    @GET
    @Path(CASE_TASKS_AS_ADMIN_GET_URI)
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getCaseInstanceTasksAsAdmin(@jakarta.ws.rs.core.Context HttpHeaders headers,
            @Parameter(description = "case instance identifier that tasks should belong to", required = true, example = "CASE-00000000001") @PathParam(CASE_ID) String caseId,
            @Parameter(description = "optional user id to be used instead of authenticated user - only when bypass authenticated user is enabled", required = false) @QueryParam("user") String user, 
            @Parameter(description = "optional task status (Created, Ready, Reserved, InProgress, Suspended, Completed, Failed, Error, Exited, Obsolete)", required = false, allowableValues="Created, Ready, Reserved,InProgress,Suspended,Completed,Failed,Error,Exited,Obsolete") @QueryParam("status") List<String> status,
            @Parameter(description = "optional pagination - at which page to start, defaults to 0 (meaning first)", required = false) @QueryParam("page") @DefaultValue("0") Integer page, 
            @Parameter(description = "optional pagination - size of the result, defaults to 10", required = false) @QueryParam("pageSize") @DefaultValue("10") Integer pageSize,
            @Parameter(description = "optional sort column, no default", required = false) @QueryParam("sort") String sort, 
            @Parameter(description = "optional sort direction (asc, desc) - defaults to asc", required = false) @QueryParam("sortOrder") @DefaultValue("true") boolean sortOrder) {

        return invokeCaseOperation(headers,
                "",
                null,
                (Variant v, String type, Header... customHeaders) -> {
                    logger.debug("About to look for case instance {} tasks with status {} assigned to business admin {}", caseId, status, user);
                    TaskSummaryList responseObject = this.caseManagementRuntimeDataServiceBase.getCaseTasksAsBusinessAdmin(caseId, user, status, page, pageSize, sort, sortOrder);

                    logger.debug("Returning OK response with content '{}'", responseObject);
                    return createCorrectVariant(responseObject, headers, Response.Status.OK, customHeaders);
                });
    }

    @Operation(summary="Returns tasks for stakeholders in a specified case instance.")
    @ApiResponses(value = { @ApiResponse(responseCode = "500", description = "Unexpected error"), 
            @ApiResponse(responseCode = "200", description = "Successful response", content = {
                @Content(mediaType = JSON, examples = {@ExampleObject(value = GET_TASK_SUMMARY_RESPONSE_JSON)})
            })})
    @GET
    @Path(CASE_TASKS_AS_STAKEHOLDER_GET_URI)
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getCaseInstanceTasksAsStakeholder(@jakarta.ws.rs.core.Context HttpHeaders headers,
            @Parameter(description = "case instance identifier that tasks should belong to", required = true, example = "CASE-0000000001") @PathParam(CASE_ID) String caseId,
            @Parameter(description = "optional user id to be used instead of authenticated user - only when bypass authenticated user is enabled", required = false) @QueryParam("user") String user, 
            @Parameter(description = "optional task status (Created, Ready, Reserved, InProgress, Suspended, Completed, Failed, Error, Exited, Obsolete)", required = false, allowableValues="Created, Ready, Reserved,InProgress,Suspended,Completed,Failed,Error,Exited,Obsolete") @QueryParam("status") List<String> status,
            @Parameter(description = "optional pagination - at which page to start, defaults to 0 (meaning first)", required = false) @QueryParam("page") @DefaultValue("0") Integer page, 
            @Parameter(description = "optional pagination - size of the result, defaults to 10", required = false) @QueryParam("pageSize") @DefaultValue("10") Integer pageSize,
            @Parameter(description = "optional sort column, no default", required = false) @QueryParam("sort") String sort, 
            @Parameter(description = "optional sort direction (asc, desc) - defaults to asc", required = false) @QueryParam("sortOrder") @DefaultValue("true") boolean sortOrder) {

        return invokeCaseOperation(headers,
                "",
                null,
                (Variant v, String type, Header... customHeaders) -> {
                    logger.debug("About to look for case instance {} tasks with status {} assigned to stakeholder {}", caseId, status, user);
                    TaskSummaryList responseObject = this.caseManagementRuntimeDataServiceBase.getCaseTasksAsStakeholder(caseId, user, status, page, pageSize, sort, sortOrder);

                    logger.debug("Returning OK response with content '{}'", responseObject);
                    return createCorrectVariant(responseObject, headers, Response.Status.OK, customHeaders);
                });
    }

    @Operation(summary="Returns case file data items for a specified case instance.")
    @ApiResponses(value = { @ApiResponse(responseCode = "500", description = "Unexpected error"), 
            @ApiResponse(responseCode = "200", description = "Successful response", content = {
                @Content(mediaType = JSON, examples = {@ExampleObject(value = GET_CASE_FILE_DATA_RESPONSE_JSON)})
            })})
    @GET
    @Path(CASE_FILE_GET_URI)
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getCaseInstanceDataItems(@jakarta.ws.rs.core.Context HttpHeaders headers, 
            @Parameter(description = "case instance identifier that data items should belong to", required = true, example = "CASE-0000000001") @PathParam(CASE_ID) String caseId,
            @Parameter(description = "optionally filter by data item names", required = false) @QueryParam("name") List<String> names, 
            @Parameter(description = "optionally filter by data item types", required = false) @QueryParam("type") List<String> types,
            @Parameter(description = "optional pagination - at which page to start, defaults to 0 (meaning first)", required = false) @QueryParam("page") @DefaultValue("0") Integer page, 
            @Parameter(description = "optional pagination - size of the result, defaults to 10", required = false) @QueryParam("pageSize") @DefaultValue("10") Integer pageSize) {
        return invokeCaseOperation(headers,
                "",
                caseId,
                (Variant v, String type, Header... customHeaders) -> {
                    logger.debug("About to load case file data items of case {}", caseId);
                    CaseFileDataItemList response = this.caseManagementRuntimeDataServiceBase.getCaseInstanceDataItems(caseId, names, types, page, pageSize);

                    logger.debug("Returning OK response with content '{}'", response);
                    return createCorrectVariant(response, headers, Response.Status.OK, customHeaders);
                });
    }

    @Operation(summary = "Queries cases by variables and tasks")
    @POST
    @Path(RestURI.VARIABLES_CASES_URI)
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @KieServerEndpoint(categories = {EndpointType.DEFAULT, EndpointType.HISTORY})
    public Response queryCaseByVariables(@Context HttpHeaders headers, String payload,
                                         @Parameter(description = "optional pagination - at which page to start, defaults to 0 (meaning first)", required = false) @QueryParam("page") @DefaultValue("0") Integer page, 
                                         @Parameter(description = "optional pagination - size of the result, defaults to 10", required = false) @QueryParam("pageSize") @DefaultValue("10") Integer pageSize) {

        Header conversationIdHeader = buildConversationIdHeader("", context, headers);
        Variant v = getVariant(headers);
        try {
            String type = getContentType(headers);
            CaseInstanceCustomVarsList processVariableSummaryList = caseManagementRuntimeDataServiceBase.queryCasesByVariables(payload, type, new QueryContext(page * pageSize, pageSize));
            logger.debug("Returning result of case instance search: {}", processVariableSummaryList);

            return createCorrectVariant(processVariableSummaryList, headers, Response.Status.OK, conversationIdHeader);

        } catch (Exception e) {
            logger.error("Unexpected error during processing {}", e.getMessage(), e);
            return internalServerError(errorMessage(e), v, conversationIdHeader);
        }

    }

    @Operation(summary = "Queries cases tasks by variables")
    @POST
    @Path(RestURI.VARIABLES_TASKS_CASES_URI)
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @KieServerEndpoint(categories = {EndpointType.DEFAULT, EndpointType.HISTORY})
    public Response queryCaseUserTasksByVariables(@Context HttpHeaders headers,
                                                  String payload,
                                                  @Parameter(description = "optional pagination - at which page to start, defaults to 0 (meaning first)", required = false) @QueryParam("page") @DefaultValue("0") Integer page, 
                                                  @Parameter(description = "optional pagination - size of the result, defaults to 10", required = false) @QueryParam("pageSize") @DefaultValue("10") Integer pageSize) {

        Header conversationIdHeader = buildConversationIdHeader("", context, headers);
        Variant v = getVariant(headers);
        try {
            String type = getContentType(headers);
            CaseUserTaskWithVariablesList taskVariableSummaryList = caseManagementRuntimeDataServiceBase.queryUserTasksByVariables(payload, type, new QueryContext((page * pageSize), pageSize));
            logger.debug("Returning result of case instance user task search: {}", taskVariableSummaryList);

            return createCorrectVariant(taskVariableSummaryList, headers, Response.Status.OK, conversationIdHeader);

        } catch (Exception e) {
            logger.error("Unexpected error during processing {}", e.getMessage(), e);
            return internalServerError(errorMessage(e), v, conversationIdHeader);
        }
    }
}
