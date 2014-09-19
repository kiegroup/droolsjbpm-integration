package org.kie.remote.services.rest;

import java.util.List;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

import org.jbpm.services.task.commands.GetTasksByVariousFieldsCommand;
import org.jbpm.services.task.commands.TaskCommand;
import org.kie.api.task.model.Status;
import org.kie.api.task.model.TaskSummary;
import org.kie.remote.services.jaxb.JaxbTaskSummaryListResponse;
import org.kie.remote.services.rest.exception.KieRemoteRestOperationException;

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
   
    private static String [] allowedQueryParams = {
        "workItemId",             // 0
        "taskId",                 // 1
        "businessAdministrator",  // 2
        "potentialOwner",         // 3
        "status",                 // 4
        "taskOwner",              // 5
        "processInstanceId",      // 6
        "language",               // 7
        "union"                   // 8
    };
    
    @GET
    @Path("/query")
    public Response taskSummaryQuery() {
        Map<String, String []> params = getRequestParams();
        String oper = getRelativePath();
        
        for( String queryParam : params.keySet() ) { 
            boolean allowed = false;
            for( String allowedParam : allowedQueryParams ) { 
                if( allowedParam.equalsIgnoreCase(queryParam) || paginationParams.contains(queryParam)) { 
                   allowed = true;
                   break;
                } 
            }
            if( ! allowed ) { 
                throw KieRemoteRestOperationException.badRequest(queryParam + " is an unknown and unsupported query param for the task query operation." );
            }
        }
        
        List<Long> workItemIds = getLongListParam(allowedQueryParams[0], false, params, "query", true);
        List<Long> taskIds = getLongListParam(allowedQueryParams[1], false, params, "query", true);
        List<Long> procInstIds = getLongListParam(allowedQueryParams[6], false, params, "query", true);
        List<String> busAdmins = getStringListParamAsList(allowedQueryParams[2], false, params, "query");
        List<String> potOwners = getStringListParamAsList(allowedQueryParams[3], false, params, "query");
        List<String> taskOwners = getStringListParamAsList(allowedQueryParams[5], false, params, "query");
        List<String> language = getStringListParamAsList(allowedQueryParams[7], false, params, "query");
        
        String unionStr = getStringParam(allowedQueryParams[8], false, params, "query");
        boolean union = Boolean.parseBoolean(unionStr); // null, etc == false
        
        List<String> statusStrList = getStringListParamAsList(allowedQueryParams[4], false, params, "query");
        List<Status> statuses = convertStringListToStatusList(statusStrList);
        
        int [] pageInfo = getPageNumAndPageSize(params, oper);
        int maxResults = getMaxNumResultsNeeded(pageInfo);
        TaskCommand<?> queryCmd 
            = new GetTasksByVariousFieldsCommand(workItemIds, taskIds, procInstIds, 
                    busAdmins, potOwners, taskOwners, 
                    statuses, language, union, maxResults);
        
        List<TaskSummary> results = (List<TaskSummary>) doRestTaskOperation(null, queryCmd);
        
        logger.debug("{} results found.", results.size());
        JaxbTaskSummaryListResponse resultList = paginateAndCreateResult(pageInfo, results, new JaxbTaskSummaryListResponse());
        logger.debug("Returning {} results after pagination.", resultList.getList().size());
        
        return createCorrectVariant(resultList, headers);
    }
}
