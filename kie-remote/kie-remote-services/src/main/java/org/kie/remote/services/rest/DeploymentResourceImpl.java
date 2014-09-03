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

import org.kie.services.client.serialization.jaxb.impl.deploy.JaxbDeploymentDescriptor;
import org.kie.services.client.serialization.jaxb.impl.deploy.JaxbDeploymentJobResult;
import org.kie.services.client.serialization.jaxb.impl.deploy.JaxbDeploymentUnit;
import org.kie.services.client.serialization.jaxb.impl.process.JaxbProcessDefinitionList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * If a method in this class is annotated by a @Path annotation, 
 * then the name of the method should match the URL specified in the @Path, 
 * where "_" characters should be used for all "/" characters in the path. 
 * <p>
 * For example: 
 * <pre>
 * @Path("/begin/{varOne: [_a-zA-Z0-9-:\\.]+}/midddle/{varTwo: [a-z]+}")
 * public void begin_varOne_middle_varTwo() { 
 * </pre>
 * 
 * If the method is annotated by the @Path anno, but is the "root", then
 * give it a name that explains it's funtion.
 */
@Path("/deployment/{deploymentId: [\\w\\.-]+(:[\\w\\.-]+){2,2}(:[\\w\\.-]*){0,2}}")
@RequestScoped
public class DeploymentResourceImpl extends ResourceBase {

    private static final Logger logger = LoggerFactory.getLogger(DeploymentResourceImpl.class);
    
    /* REST information */
    
    @Context
    private HttpHeaders headers;
    
    @PathParam("deploymentId")
    private String deploymentId;
    
    /* Deployment operations */
   
    @Inject 
    private DeployResourceBase deployBase;
   
    // REST operations -----------------------------------------------------------------------------------------------------------

    @GET
    public Response getConfig() { 
        JaxbDeploymentUnit jaxbDepUnit = deployBase.determineStatus(deploymentId, true);
        logger.debug("Returning deployment unit information for " + deploymentId);
        return createCorrectVariant(jaxbDepUnit, headers);
    }

    @POST
    @Path("/deploy")
    public Response deploy(JaxbDeploymentDescriptor deployDescriptor) {
        // parse request/options 
        Map<String, String []> params = getRequestParams();
        String oper = getRelativePath();
        String strategy = getStringParam("strategy", false, params, oper);
        String mergeMode = getStringParam("mergemode", false, params, oper);
        
        // schedule deployment
        JaxbDeploymentJobResult jobResult = deployBase.submitDeployJob(deploymentId, strategy, mergeMode, deployDescriptor);
        return createCorrectVariant(jobResult, headers, Status.ACCEPTED);
    }
   
    @POST
    @Path("/undeploy")
    public Response undeploy() { 
        JaxbDeploymentJobResult jobResult = deployBase.submitUndeployJob(deploymentId);
        return createCorrectVariant(jobResult, headers, Status.ACCEPTED);
    }
   
    @GET
    @Path("/processes")
    public Response listProcessDefinitions() { 
        String oper = getRelativePath();
        Map<String, String[]> params = getRequestParams();
        int [] pageInfo = getPageNumAndPageSize(params, oper);
        int maxNumResults = getMaxNumResultsNeeded(pageInfo); 
        
        JaxbProcessDefinitionList jaxbProcDefList  = new JaxbProcessDefinitionList();
        deployBase.fillProcessDefinitionList(deploymentId, pageInfo, maxNumResults, jaxbProcDefList.getProcessDefinitionList());
        JaxbProcessDefinitionList resultList 
            = paginateAndCreateResult(pageInfo, jaxbProcDefList.getProcessDefinitionList(), new JaxbProcessDefinitionList());
        return createCorrectVariant(resultList, headers);
    }
}
