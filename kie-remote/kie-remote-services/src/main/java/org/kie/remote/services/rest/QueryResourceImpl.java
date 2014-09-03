package org.kie.remote.services.rest;

import java.util.Map;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

/**
 * Not done. 
 * 
 * Available starting with 6.2.0.Final
 */

//@RequestScoped
//@Path("/query/{deploymentId: [\\w\\.-]+(:[\\w\\.-]+){2,2}(:[\\w\\.-]*){0,2}}")
public class QueryResourceImpl extends ResourceBase {

    @Context
    private HttpHeaders headers;
   
    /* Deployment operations */
   
    // REST operations -----------------------------------------------------------------------------------------------------------

//    @GET
//    @Path("/task")
    // TODO: docs pagination
    public Response queryTasks() { 
        String oper = getRelativePath();
        Map<String, String[]> params = getRequestParams();
        int [] pageInfo = getPageNumAndPageSize(params, oper);
        int maxNumResults = getMaxNumResultsNeeded(pageInfo); 
       
        
       
        return createCorrectVariant(null, headers);
    }
   
//    @GET
//    @Path("/process")
    public Response queryProcessInstances() { 
        String oper = getRelativePath();
        Map<String, String[]> params = getRequestParams();
        int [] pageInfo = getPageNumAndPageSize(params, oper);
        int maxNumResults = getMaxNumResultsNeeded(pageInfo); 
        
        
        return createCorrectVariant(null, headers);
    }
}
