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

package org.kie.remote.services.rest;

import static org.kie.internal.remote.PermissionConstants.*;
import java.util.Map;

import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

import org.kie.services.client.serialization.jaxb.impl.deploy.JaxbDeploymentUnitList;
import org.kie.services.client.serialization.jaxb.impl.process.JaxbProcessDefinitionList;

/**
 * This resource is responsible for retrieving overview information about all deployment units.
 */
@Path("/deployment")
@RequestScoped
public class DeploymentsResourceImpl extends ResourceBase {

    @Context
    private HttpHeaders headers;
   
    /* Deployment operations */
   
    @Inject
    private DeployResourceBase deployResourceBase;
  
    // REST operations -----------------------------------------------------------------------------------------------------------

    /**
     * Return a (paginated) list of the available deployments, sorted alphabetically by deployment id. 
     * @return A {@link JaxbDeploymentUnitList} instance
     */
    @GET
    @RolesAllowed({REST_ROLE, REST_DEPLOYMENT_ROLE})
    public Response listDeployments() { 
        String oper = getRelativePath();
        Map<String, String[]> params = getRequestParams();
        int [] pageInfo = getPageNumAndPageSize(params, oper);
        int maxNumResults = getMaxNumResultsNeeded(pageInfo); 
        
       JaxbDeploymentUnitList resultList = deployResourceBase.getDeploymentList(pageInfo, maxNumResults);
       
        return createCorrectVariant(resultList, headers);
    }
   

    /**
     * Return a list of process definition information, sorted alphabetically by deployment id.
     * @return
     */
    @GET
    @Path("/processes")
    @RolesAllowed({REST_ROLE, REST_DEPLOYMENT_ROLE})
    public Response listProcessDefinitions() { 
        String oper = getRelativePath();
        Map<String, String[]> params = getRequestParams();
        int [] pageInfo = getPageNumAndPageSize(params, oper);
        int maxNumResults = getMaxNumResultsNeeded(pageInfo); 
        
        JaxbProcessDefinitionList resultList = deployResourceBase.getProcessDefinitionList(pageInfo, maxNumResults);
        
        return createCorrectVariant(resultList, headers);
    }
}
