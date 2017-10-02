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

import static org.kie.server.api.rest.RestURI.ADMIN_CASE_URI;
import static org.kie.server.api.rest.RestURI.CASE_ALL_INSTANCES_GET_URI;
import static org.kie.server.remote.rest.common.util.RestUtils.createCorrectVariant;

import java.util.List;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Variant;

import org.kie.server.api.model.cases.CaseInstanceList;
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

@Api(value="Administration of cases :: Case Management")
@Path("server/" + ADMIN_CASE_URI)
public class CaseAdminResource extends AbstractCaseResource {

    private static final Logger logger = LoggerFactory.getLogger(CaseAdminResource.class);

    public CaseAdminResource() {

    }

    public CaseAdminResource(
            final CaseManagementRuntimeDataServiceBase caseManagementRuntimeDataServiceBase,
            final KieServerRegistry context) {
        super(caseManagementRuntimeDataServiceBase,
              context);
    }

    @ApiOperation(value="Retrieves case instances without authntication checks and applies pagination",
            response=CaseInstanceList.class, code=200)
    @ApiResponses(value = { @ApiResponse(code = 500, message = "Unexpected error")})
    @GET
    @Path(CASE_ALL_INSTANCES_GET_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
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
}