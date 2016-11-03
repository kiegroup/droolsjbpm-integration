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
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Variant;

import org.kie.server.api.model.cases.CaseDefinitionList;
import org.kie.server.api.model.cases.CaseInstanceList;
import org.kie.server.remote.rest.common.Header;
import org.kie.server.services.api.KieServerRegistry;
import org.kie.server.services.casemgmt.CaseManagementRuntimeDataServiceBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.kie.server.api.rest.RestURI.*;
import static org.kie.server.remote.rest.common.util.RestUtils.*;

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

    @GET
    @Path(CASE_ALL_INSTANCES_GET_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getCaseInstances(@javax.ws.rs.core.Context HttpHeaders headers,
            @QueryParam("owner") String owner, @QueryParam("status") List<Integer> status,
            @QueryParam("page") @DefaultValue("0") Integer page, @QueryParam("pageSize") @DefaultValue("10") Integer pageSize,
            @QueryParam("sort") String sort, @QueryParam("sortOrder") @DefaultValue("true") boolean sortOrder) {

        return invokeCaseOperation(headers,
                "",
                null,
                (Variant v, String type, Header... customHeaders) -> {

                    CaseInstanceList responseObject = null;
                    if (owner != null && !owner.isEmpty()) {
                        logger.debug("About to look for case instances owned by {} with status {}", owner, status);
                        responseObject = this.caseManagementRuntimeDataServiceBase.getCaseInstancesOwnedBy(owner, status, page, pageSize, sort, sortOrder);
                    } else {
                        logger.debug("About to look for case instances with status {}", status);
                        responseObject = this.caseManagementRuntimeDataServiceBase.getCaseInstances(status, page, pageSize, sort, sortOrder);
                    }

                    logger.debug("Returning OK response with content '{}'", responseObject);
                    return createCorrectVariant(responseObject, headers, Response.Status.OK, customHeaders);
                });
    }

    /*
     * case definition methods
     */
    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getCaseDefinitions(@javax.ws.rs.core.Context HttpHeaders headers,
            @QueryParam("filter") String filter,
            @QueryParam("page") @DefaultValue("0") Integer page, @QueryParam("pageSize") @DefaultValue("10") Integer pageSize,
            @QueryParam("sort") String sort, @QueryParam("sortOrder") @DefaultValue("true") boolean sortOrder) {

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

}