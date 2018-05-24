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

import static org.kie.server.api.rest.RestURI.*;
import static org.kie.server.remote.rest.common.util.RestUtils.*;

import java.util.List;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Variant;

import org.kie.server.api.model.cases.CaseDefinitionList;
import org.kie.server.api.model.cases.CaseFileDataItemList;
import org.kie.server.api.model.cases.CaseInstanceList;
import org.kie.server.api.model.definition.ProcessDefinitionList;
import org.kie.server.api.model.instance.TaskSummaryList;
import org.kie.server.remote.rest.common.Header;
import org.kie.server.services.api.KieServerRegistry;
import org.kie.server.services.casemgmt.CaseManagementRuntimeDataServiceBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Api(value="Queries - case definitions and instances :: Case Management")
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

    @ApiOperation(value="Retrieves case instances with authentication checks and applies pagination, allows to filter by data (case file) name and value, owner and case instance status",
            response=CaseInstanceList.class, code=200)
    @ApiResponses(value = { @ApiResponse(code = 500, message = "Unexpected error")})
    @GET
    @Path(CASE_ALL_INSTANCES_GET_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getCaseInstances(@javax.ws.rs.core.Context HttpHeaders headers,
            @ApiParam(value = "data item name that case instances will be filtered by", required = false) @QueryParam("dataItemName") String dataItemName, 
            @ApiParam(value = "data item value that case instances will be filtered by", required = false) @QueryParam("dataItemValue") String dataItemValue,
            @ApiParam(value = "case instance owner that case instances will be filtered by", required = false) @QueryParam("owner") String owner,
            @ApiParam(value = "optional case instance status (open, closed, canceled) - defaults ot open (1) only", required = false, allowableValues="open,closed,cancelled") @QueryParam("status") List<String> status,
            @ApiParam(value = "optional pagination - at which page to start, defaults to 0 (meaning first)", required = false) @QueryParam("page") @DefaultValue("0") Integer page, 
            @ApiParam(value = "optional pagination - size of the result, defaults to 10", required = false) @QueryParam("pageSize") @DefaultValue("10") Integer pageSize,
            @ApiParam(value = "optional sort column, no default", required = false) @QueryParam("sort") String sort, 
            @ApiParam(value = "optional sort direction (asc, desc) - defaults to asc", required = false) @QueryParam("sortOrder") @DefaultValue("true") boolean sortOrder) {

        return invokeCaseOperation(headers,
                "",
                null,
                (Variant v, String type, Header... customHeaders) -> {

                    CaseInstanceList responseObject = null;
                    if (dataItemName != null && !dataItemName.isEmpty() && dataItemValue != null && !dataItemValue.isEmpty()) {
                        logger.debug("About to look for case instances by data item name {} and value {} with status {}", dataItemName, dataItemValue, status);
                        responseObject = this.caseManagementRuntimeDataServiceBase.getCaseInstancesByCaseFileData(dataItemName, dataItemValue, status, page, pageSize, sort, sortOrder);
                    } else if (dataItemName != null && !dataItemName.isEmpty()) {
                        logger.debug("About to look for case instances by data item name {} with status {}", dataItemName, dataItemValue, status);
                        responseObject = this.caseManagementRuntimeDataServiceBase.getCaseInstancesByCaseFileData(dataItemName, null, status, page, pageSize, sort, sortOrder);
                    } else if (owner != null && !owner.isEmpty()) {
                        logger.debug("About to look for case instances owned by {} with status {}", owner, status);
                        responseObject = this.caseManagementRuntimeDataServiceBase.getCaseInstancesOwnedBy(owner,
                                                                                                           status, page, pageSize, sort, sortOrder);
                    } else {
                        logger.debug("About to look for case instances with status {}", status);
                        responseObject = this.caseManagementRuntimeDataServiceBase.getCaseInstancesAnyRole(status, page, pageSize, sort, sortOrder);
                    }

                    logger.debug("Returning OK response with content '{}'", responseObject);
                    return createCorrectVariant(responseObject, headers, Response.Status.OK, customHeaders);
                });
    }

    @ApiOperation(value="Retrieves case instances where user is involved in given role and applies pagination, allows to filter by case instance status",
            response=CaseInstanceList.class, code=200)
    @ApiResponses(value = { @ApiResponse(code = 500, message = "Unexpected error")})
    @GET
    @Path(CASE_INSTANCES_BY_ROLE_GET_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getCaseInstancesByRole(@javax.ws.rs.core.Context HttpHeaders headers,
            @ApiParam(value = "case role that instances should be found for", required = true) @PathParam(CASE_ROLE_NAME) String roleName, 
            @ApiParam(value = "optional case instance status (open, closed, canceled) - defaults ot open (1) only", required = false, allowableValues="open,closed,cancelled") @QueryParam("status") List<String> status,
            @ApiParam(value = "optional pagination - at which page to start, defaults to 0 (meaning first)", required = false) @QueryParam("page") @DefaultValue("0") Integer page, 
            @ApiParam(value = "optional pagination - size of the result, defaults to 10", required = false) @QueryParam("pageSize") @DefaultValue("10") Integer pageSize,
            @ApiParam(value = "optional sort column, no default", required = false) @QueryParam("sort") String sort, 
            @ApiParam(value = "optional sort direction (asc, desc) - defaults to asc", required = false) @QueryParam("sortOrder") @DefaultValue("true") boolean sortOrder) {

        return invokeCaseOperation(headers,
                "",
                null,
                (Variant v, String type, Header... customHeaders) -> {

                    logger.debug("About to look for case instances with status {}", status);
                    CaseInstanceList responseObject = this.caseManagementRuntimeDataServiceBase.getCaseInstancesByRole(roleName, status, page, pageSize, sort, sortOrder);

                    logger.debug("Returning OK response with content '{}'", responseObject);
                    return createCorrectVariant(responseObject, headers, Response.Status.OK, customHeaders);
                });
    }

    /*
     * case definition methods
     */
    
    @ApiOperation(value="Retrieves case definitions with filtering by name or id of the case definition and applies pagination",
            response=CaseInstanceList.class, code=200)
    @ApiResponses(value = { @ApiResponse(code = 500, message = "Unexpected error")})
    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getCaseDefinitions(@javax.ws.rs.core.Context HttpHeaders headers,
            @ApiParam(value = "case definition id or name that case definitions will be filtered by", required = true) @QueryParam("filter") String filter,
            @ApiParam(value = "optional pagination - at which page to start, defaults to 0 (meaning first)", required = false) @QueryParam("page") @DefaultValue("0") Integer page, 
            @ApiParam(value = "optional pagination - size of the result, defaults to 10", required = false) @QueryParam("pageSize") @DefaultValue("10") Integer pageSize,
            @ApiParam(value = "optional sort column, no default", required = false) @QueryParam("sort") String sort, 
            @ApiParam(value = "optional sort direction (asc, desc) - defaults to asc", required = false) @QueryParam("sortOrder") @DefaultValue("true") boolean sortOrder) {

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
    @ApiOperation(value="Retrieves process definitions with filtering by name or id of the process definition and applies pagination",
            response=CaseInstanceList.class, code=200)
    @ApiResponses(value = { @ApiResponse(code = 500, message = "Unexpected error")})
    @GET
    @Path(CASE_ALL_PROCESSES_INSTANCES_GET_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getProcessDefinitions(@javax.ws.rs.core.Context HttpHeaders headers,
            @ApiParam(value = "process definition id or name that process definitions will be filtered by", required = true) @QueryParam("filter") String filter,
            @ApiParam(value = "optional pagination - at which page to start, defaults to 0 (meaning first)", required = false) @QueryParam("page") @DefaultValue("0") Integer page, 
            @ApiParam(value = "optional pagination - size of the result, defaults to 10", required = false) @QueryParam("pageSize") @DefaultValue("10") Integer pageSize,
            @ApiParam(value = "optional sort column, no default", required = false) @QueryParam("sort") String sort, 
            @ApiParam(value = "optional sort direction (asc, desc) - defaults to asc", required = false) @QueryParam("sortOrder") @DefaultValue("true") boolean sortOrder) {

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

    @ApiOperation(value="Retrieves process definitions that belong to given container and applies pagination",
            response=CaseInstanceList.class, code=200)
    @ApiResponses(value = { @ApiResponse(code = 500, message = "Unexpected error")})
    @GET
    @Path(CASE_PROCESSES_BY_CONTAINER_INSTANCES_GET_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getProcessDefinitionsByContainer(@javax.ws.rs.core.Context HttpHeaders headers, 
            @ApiParam(value = "container id that process definitions should be filtered by", required = true) @PathParam("id") String containerId,
            @ApiParam(value = "optional pagination - at which page to start, defaults to 0 (meaning first)", required = false) @QueryParam("page") @DefaultValue("0") Integer page, 
            @ApiParam(value = "optional pagination - size of the result, defaults to 10", required = false) @QueryParam("pageSize") @DefaultValue("10") Integer pageSize,
            @ApiParam(value = "optional sort column, no default", required = false) @QueryParam("sort") String sort, 
            @ApiParam(value = "optional sort direction (asc, desc) - defaults to asc", required = false) @QueryParam("sortOrder") @DefaultValue("true") boolean sortOrder) {

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

    @ApiOperation(value="Retrieves case instance tasks assigned as potential owner, allows to filter by task status and applies pagination",
            response=CaseInstanceList.class, code=200)
    @ApiResponses(value = { @ApiResponse(code = 500, message = "Unexpected error")})
    @GET
    @Path(CASE_TASKS_AS_POT_OWNER_GET_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getCaseInstanceTasksAsPotentialOwner(@javax.ws.rs.core.Context HttpHeaders headers,
            @ApiParam(value = "case instance identifier that tasks should belong to", required = true) @PathParam(CASE_ID) String caseId,
            @ApiParam(value = "optional user id to be used instead of authenticated user - only when bypass authenticated user is enabled", required = false) @QueryParam("user") String user, 
            @ApiParam(value = "optional task status (Created, Ready, Reserved, InProgress, Suspended, Completed, Failed, Error, Exited, Obsolete)", required = false, allowableValues="Created, Ready, Reserved,InProgress,Suspended,Completed,Failed,Error,Exited,Obsolete") @QueryParam("status") List<String> status,
            @ApiParam(value = "optional pagination - at which page to start, defaults to 0 (meaning first)", required = false) @QueryParam("page") @DefaultValue("0") Integer page, 
            @ApiParam(value = "optional pagination - size of the result, defaults to 10", required = false) @QueryParam("pageSize") @DefaultValue("10") Integer pageSize,
            @ApiParam(value = "optional sort column, no default", required = false) @QueryParam("sort") String sort, 
            @ApiParam(value = "optional sort direction (asc, desc) - defaults to asc", required = false) @QueryParam("sortOrder") @DefaultValue("true") boolean sortOrder) {

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

    @ApiOperation(value="Retrieves case instance tasks assigned as business admin, allows to filter by task status and applies pagination",
            response=CaseInstanceList.class, code=200)
    @ApiResponses(value = { @ApiResponse(code = 500, message = "Unexpected error")})
    @GET
    @Path(CASE_TASKS_AS_ADMIN_GET_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getCaseInstanceTasksAsAdmin(@javax.ws.rs.core.Context HttpHeaders headers,
            @ApiParam(value = "case instance identifier that tasks should belong to", required = true) @PathParam(CASE_ID) String caseId,
            @ApiParam(value = "optional user id to be used instead of authenticated user - only when bypass authenticated user is enabled", required = false) @QueryParam("user") String user, 
            @ApiParam(value = "optional task status (Created, Ready, Reserved, InProgress, Suspended, Completed, Failed, Error, Exited, Obsolete)", required = false, allowableValues="Created, Ready, Reserved,InProgress,Suspended,Completed,Failed,Error,Exited,Obsolete") @QueryParam("status") List<String> status,
            @ApiParam(value = "optional pagination - at which page to start, defaults to 0 (meaning first)", required = false) @QueryParam("page") @DefaultValue("0") Integer page, 
            @ApiParam(value = "optional pagination - size of the result, defaults to 10", required = false) @QueryParam("pageSize") @DefaultValue("10") Integer pageSize,
            @ApiParam(value = "optional sort column, no default", required = false) @QueryParam("sort") String sort, 
            @ApiParam(value = "optional sort direction (asc, desc) - defaults to asc", required = false) @QueryParam("sortOrder") @DefaultValue("true") boolean sortOrder) {

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

    @ApiOperation(value="Retrieves case instance tasks assigned as stakeholder, allows to filter by task status and applies pagination",
            response=CaseInstanceList.class, code=200)
    @ApiResponses(value = { @ApiResponse(code = 500, message = "Unexpected error")})
    @GET
    @Path(CASE_TASKS_AS_STAKEHOLDER_GET_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getCaseInstanceTasksAsStakeholder(@javax.ws.rs.core.Context HttpHeaders headers,
            @ApiParam(value = "case instance identifier that tasks should belong to", required = true) @PathParam(CASE_ID) String caseId,
            @ApiParam(value = "optional user id to be used instead of authenticated user - only when bypass authenticated user is enabled", required = false) @QueryParam("user") String user, 
            @ApiParam(value = "optional task status (Created, Ready, Reserved, InProgress, Suspended, Completed, Failed, Error, Exited, Obsolete)", required = false, allowableValues="Created, Ready, Reserved,InProgress,Suspended,Completed,Failed,Error,Exited,Obsolete") @QueryParam("status") List<String> status,
            @ApiParam(value = "optional pagination - at which page to start, defaults to 0 (meaning first)", required = false) @QueryParam("page") @DefaultValue("0") Integer page, 
            @ApiParam(value = "optional pagination - size of the result, defaults to 10", required = false) @QueryParam("pageSize") @DefaultValue("10") Integer pageSize,
            @ApiParam(value = "optional sort column, no default", required = false) @QueryParam("sort") String sort, 
            @ApiParam(value = "optional sort direction (asc, desc) - defaults to asc", required = false) @QueryParam("sortOrder") @DefaultValue("true") boolean sortOrder) {

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

    @ApiOperation(value="Retrieves case instance data items, allows to filter by name or type of data and applies pagination",
            response=CaseInstanceList.class, code=200)
    @ApiResponses(value = { @ApiResponse(code = 500, message = "Unexpected error")})
    @GET
    @Path(CASE_FILE_GET_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getCaseInstanceDataItems(@javax.ws.rs.core.Context HttpHeaders headers, 
            @ApiParam(value = "case instance identifier that data items should belong to", required = true) @PathParam(CASE_ID) String caseId,
            @ApiParam(value = "optionally filter by data item names", required = false) @QueryParam("name") List<String> names, 
            @ApiParam(value = "optionally filter by data item types", required = false) @QueryParam("type") List<String> types,
            @ApiParam(value = "optional pagination - at which page to start, defaults to 0 (meaning first)", required = false) @QueryParam("page") @DefaultValue("0") Integer page, 
            @ApiParam(value = "optional pagination - size of the result, defaults to 10", required = false) @QueryParam("pageSize") @DefaultValue("10") Integer pageSize) {
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
}
