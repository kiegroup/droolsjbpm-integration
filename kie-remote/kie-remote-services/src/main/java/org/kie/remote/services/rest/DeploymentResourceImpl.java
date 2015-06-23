/*
 * Copyright 2015 JBoss Inc
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

import java.util.Map;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.jbpm.services.api.model.DeploymentUnit;
import org.kie.internal.runtime.conf.DeploymentDescriptor;
import org.kie.services.client.serialization.jaxb.impl.deploy.JaxbDeploymentDescriptor;
import org.kie.services.client.serialization.jaxb.impl.deploy.JaxbDeploymentJobResult;
import org.kie.services.client.serialization.jaxb.impl.deploy.JaxbDeploymentUnit;
import org.kie.services.client.serialization.jaxb.impl.process.JaxbProcessDefinitionList;
import org.kie.services.client.serialization.jaxb.rest.JaxbGenericResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This REST resource is responsible for retrieving information about and managing deployment units. 
 */
@RequestScoped
@Path("/deployment/{deploymentId: [\\w\\.-]+(:[\\w\\.-]+){2,2}(:[\\w\\.-]*){0,2}}")
public class DeploymentResourceImpl extends ResourceBase {

    private static final Logger logger = LoggerFactory.getLogger(DeploymentResourceImpl.class);
    
    /* REST information */
    
    @Context
    private HttpHeaders headers;
    
    @PathParam("deploymentId")
    private String deploymentId;
    
    /* Deployment operations */
   
    @Inject 
    private DeployResourceBase deployResourceBase;
   
    // REST operations -----------------------------------------------------------------------------------------------------------

    /**
     * Retrieve the status of the {@link DeploymentUnit} specified in the URL.
     * 
     * @return A {@link JaxbDeploymentUnit} instance
     */
    @GET
    public Response getConfig() { 
        JaxbDeploymentUnit jaxbDepUnit = deployResourceBase.determineStatus(deploymentId, true);
        logger.debug("Returning deployment unit information for " + deploymentId);
        return createCorrectVariant(jaxbDepUnit, headers);
    }

    /**
     * Queues a request to deploy the given deployment unit. If the deployment already exist, this
     * operation will fail.
     * 
     * @param deployDescriptor An optional {@link DeploymentDescriptor} instance specifying additional information about how
     * the deployment unit should be deployed.
     * @return A {@link JaxbDeploymentJobResult} instance with the initial status of the job
     */
    @POST
    @Path("/deploy")
    public Response deploy(JaxbDeploymentDescriptor deployDescriptor) {
        // parse request/options 
        Map<String, String []> params = getRequestParams();
        String oper = getRelativePath();
        String strategy = getStringParam("strategy", false, params, oper);
        String mergeMode = getStringParam("mergemode", false, params, oper);
        
        // schedule deployment
        JaxbDeploymentJobResult jobResult = deployResourceBase.submitDeployJob(deploymentId, strategy, mergeMode, deployDescriptor);
        return createCorrectVariant(jobResult, headers, Status.ACCEPTED);
    }
   
    /**
     * Queues a request to undeploy the deployment unit specified in the URL
     * 
     * @return A {@link JaxbDeploymentJobResult} instance with the initial status of the job
     */
    @POST
    @Path("/undeploy")
    public Response undeploy() { 
        JaxbDeploymentJobResult jobResult = deployResourceBase.submitUndeployJob(deploymentId);
        return createCorrectVariant(jobResult, headers, Status.ACCEPTED);
    }
   

    /**
     * Returns a list of process definitions for the specified deployment.
     * @return A {@link JaxbProcessDefinitionList} instance
     */
    @GET
    @Path("/processes")
    public Response listProcessDefinitions() { 
        String oper = getRelativePath();
        Map<String, String[]> params = getRequestParams();
        int [] pageInfo = getPageNumAndPageSize(params, oper);
        int maxNumResults = getMaxNumResultsNeeded(pageInfo); 
        
        JaxbProcessDefinitionList jaxbProcDefList  = new JaxbProcessDefinitionList();
        deployResourceBase.fillProcessDefinitionList(deploymentId, pageInfo, maxNumResults, jaxbProcDefList.getProcessDefinitionList());
        JaxbProcessDefinitionList resultList 
            = paginateAndCreateResult(pageInfo, jaxbProcDefList.getProcessDefinitionList(), new JaxbProcessDefinitionList());
        return createCorrectVariant(resultList, headers);
    }

    @POST
    @Path("/activate")
    public Response activate() {
        deployResourceBase.activate(deploymentId);
        return createCorrectVariant(new JaxbGenericResponse(getRequestUri()), headers);

    }

    @POST
    @Path("/deactivate")
    public Response deactivate() {
        deployResourceBase.deactivate(deploymentId);
        return createCorrectVariant(new JaxbGenericResponse(getRequestUri()), headers);

    }
}
