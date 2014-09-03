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

@Path("/deployment")
@RequestScoped
public class DeploymentsResourceImpl extends ResourceBase {

    @Context
    private HttpHeaders headers;
   
    /* Deployment operations */
   
    @Inject
    private DeployResourceBase deployResourceBase;
  
    // REST operations -----------------------------------------------------------------------------------------------------------

    @GET
    // TODO: docs pagination
    public Response listDeployments() { 
        String oper = getRelativePath();
        Map<String, String[]> params = getRequestParams();
        int [] pageInfo = getPageNumAndPageSize(params, oper);
        int maxNumResults = getMaxNumResultsNeeded(pageInfo); 
        
       JaxbDeploymentUnitList resultList = deployResourceBase.getDeploymentList(pageInfo, maxNumResults);
       
        return createCorrectVariant(resultList, headers);
    }
   
    @GET
    @Path("/processes")
    // DOCS: (+ pagination)
    public Response listProcessDefinitions() { 
        String oper = getRelativePath();
        Map<String, String[]> params = getRequestParams();
        int [] pageInfo = getPageNumAndPageSize(params, oper);
        int maxNumResults = getMaxNumResultsNeeded(pageInfo); 
        
        JaxbProcessDefinitionList resultList = deployResourceBase.getProcessDefinitionList(pageInfo, maxNumResults);
        
        return createCorrectVariant(resultList, headers);
    }
}
