/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
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

import static org.kie.server.api.rest.RestURI.ADMIN_CASE_ALL_INSTANCES_GET_URI;
import static org.kie.server.api.rest.RestURI.ADMIN_CASE_URI;
import static org.kie.server.api.rest.RestURI.CONTAINER_ID;
import static org.kie.server.api.rest.RestURI.MIGRATE_CASE_INST_PUT_URI;
import static org.kie.server.remote.rest.casemgmt.docs.ParameterSamples.CASE_INSTANCES_JSON;
import static org.kie.server.remote.rest.casemgmt.docs.ParameterSamples.CASE_MIGRATION_MAP_JSON;
import static org.kie.server.remote.rest.casemgmt.docs.ParameterSamples.CASE_MIGRATION_MAP_XML;
import static org.kie.server.remote.rest.casemgmt.docs.ParameterSamples.CASE_MIGRATION_REPORT_JSON;
import static org.kie.server.remote.rest.casemgmt.docs.ParameterSamples.JSON;
import static org.kie.server.remote.rest.casemgmt.docs.ParameterSamples.XML;
import static org.kie.server.remote.rest.common.util.RestUtils.createCorrectVariant;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Variant;

import org.kie.server.api.model.cases.CaseInstanceList;
import org.kie.server.api.model.cases.CaseMigrationReportInstance;
import org.kie.server.remote.rest.common.Header;
import org.kie.server.services.api.KieServerRegistry;
import org.kie.server.services.casemgmt.CaseAdminServiceBase;
import org.kie.server.services.casemgmt.CaseManagementRuntimeDataServiceBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Example;
import io.swagger.annotations.ExampleProperty;

@Api(value="Case instance administration")
@Path("server/" + ADMIN_CASE_URI)
public class CaseAdminResource extends AbstractCaseResource {

    private static final Logger logger = LoggerFactory.getLogger(CaseAdminResource.class);

    private CaseAdminServiceBase caseAdminServiceBase;
    
    public CaseAdminResource() {

    }

    public CaseAdminResource(
            final CaseManagementRuntimeDataServiceBase caseManagementRuntimeDataServiceBase,
            final CaseAdminServiceBase caseAdminServiceBase,
            final KieServerRegistry context) {
        super(caseManagementRuntimeDataServiceBase,
              context);
        this.caseAdminServiceBase = caseAdminServiceBase;
    }

    @ApiOperation(value="Returns case instances without authentication checks.",
            response=CaseInstanceList.class, code=200)
    @ApiResponses(value = { @ApiResponse(code = 500, message = "Unexpected error"), 
            @ApiResponse(code = 200, message = "Successfull response", examples=@Example(value= {
                    @ExampleProperty(mediaType=JSON, value=CASE_INSTANCES_JSON)}))})
    @GET
    @Path(ADMIN_CASE_ALL_INSTANCES_GET_URI)
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getCaseInstances(@javax.ws.rs.core.Context HttpHeaders headers,
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

                                       logger.debug("About to look for case instances with status {}",
                                                    status);
                                       responseObject = this.caseManagementRuntimeDataServiceBase.getCaseInstances(status,
                                                                                                                   page,
                                                                                                                   pageSize,
                                                                                                                   sort,
                                                                                                                   sortOrder);

                                       logger.debug("Returning OK response with content '{}'",
                                                    responseObject);
                                       return createCorrectVariant(responseObject,
                                                                   headers,
                                                                   Response.Status.OK,
                                                                   customHeaders);
                                   });
    }
    
    @ApiOperation(value="Migrates a specified case instance to another KIE container and case definition.",
            response=CaseMigrationReportInstance.class, code=201)
    @ApiResponses(value = { @ApiResponse(code = 500, message = "Unexpected error"),
            @ApiResponse(code = 404, message = "Case instance or Container Id not found"), 
            @ApiResponse(code = 201, message = "Successfull response", examples=@Example(value= {
                    @ExampleProperty(mediaType=JSON, value=CASE_MIGRATION_REPORT_JSON)})) })
    @PUT
    @Path(MIGRATE_CASE_INST_PUT_URI)
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response migrateCaseInstance(@javax.ws.rs.core.Context HttpHeaders headers, 
            @ApiParam(value = "container id that case instance belongs to", required = true, example = "evaluation_1.0.0-SNAPSHOT") @PathParam(CONTAINER_ID) String containerId, 
            @ApiParam(value = "identifier of case instance to be migrated", required = true, example = "CASE-000000001") @PathParam("caseId") String caseId,
            @ApiParam(value = "container id that new case definition should be migrated to to", required = true) @QueryParam("targetContainerId") String targetContainerId,             
            @ApiParam(value = "process and node mapping - unique ids of old definition to new definition given as Map of Maps - ProcessMapping should provide map of process definitions (mandatory), NodeMapping should provide map of node mappings (optional)",
            required = false, examples=@Example(value= {
                    @ExampleProperty(mediaType=JSON, value=CASE_MIGRATION_MAP_JSON),
                    @ExampleProperty(mediaType=XML, value=CASE_MIGRATION_MAP_XML)})) String payload) {
        
        return invokeCaseOperation(headers,
                                   "",
                                   null,
                                   (Variant v, String type, Header... customHeaders) -> {

                                       CaseMigrationReportInstance responseObject = caseAdminServiceBase.migrateCaseInstance(containerId, caseId, targetContainerId, payload, type);

                                       logger.debug("Returning CREATED response with content '{}'",
                                                    responseObject);
                                       return createCorrectVariant(responseObject,
                                                                   headers,
                                                                   Response.Status.CREATED,
                                                                   customHeaders);
                                   });
    }
}
