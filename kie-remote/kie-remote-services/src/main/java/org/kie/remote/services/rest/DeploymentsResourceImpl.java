package org.kie.remote.services.rest;

import java.util.Map;

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
    public Response listProcessDefinitions() { 
        String oper = getRelativePath();
        Map<String, String[]> params = getRequestParams();
        int [] pageInfo = getPageNumAndPageSize(params, oper);
        int maxNumResults = getMaxNumResultsNeeded(pageInfo); 
        
        JaxbProcessDefinitionList resultList = deployResourceBase.getProcessDefinitionList(pageInfo, maxNumResults);
        
        return createCorrectVariant(resultList, headers);
    }
}
