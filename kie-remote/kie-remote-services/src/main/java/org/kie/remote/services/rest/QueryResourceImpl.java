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

import static org.kie.remote.services.rest.query.data.QueryResourceData.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

import org.jbpm.services.task.commands.GetTasksByVariousFieldsCommand;
import org.kie.api.task.model.Status;
import org.kie.api.task.model.TaskSummary;
import org.kie.internal.identity.IdentityProvider;
import org.kie.remote.services.jaxb.JaxbTaskSummaryListResponse;
import org.kie.remote.services.rest.exception.KieRemoteRestOperationException;
import org.kie.remote.services.rest.query.data.QueryResourceData;
import org.kie.remote.services.rest.query.helpers.InternalProcInstQueryHelper;
import org.kie.remote.services.rest.query.helpers.InternalTaskQueryHelper;
import org.kie.services.client.serialization.jaxb.impl.query.JaxbQueryProcessInstanceResult;
import org.kie.services.client.serialization.jaxb.impl.query.JaxbQueryTaskResult;

/**
 * Resource that does various query operations
 */

@RequestScoped
@Path("/query/")
public class QueryResourceImpl extends ResourceBase {

    @Context
    private HttpHeaders headers;

    @Inject
    private IdentityProvider identityProvider;

    public void setIdentityProvider( IdentityProvider identityProvider ) {
        this.identityProvider = identityProvider;
    }

    public void setHeaders( HttpHeaders headers ) {
        this.headers = headers;
    }

    // REST operations -----------------------------------------------------------------------------------------------------------

    @GET
    @Path("/runtime/task")
    public Response queryTasks() {
        String oper = getRelativePath();
        Map<String, String[]> params = makeQueryParametersLowerCase(getRequestParams());
        checkIfParametersAreAllowed(params, QueryResourceData.getQueryParameters(), true, oper);
        
        int[] pageInfo = getPageNumAndPageSize(params, oper);

        InternalTaskQueryHelper queryHelper = new InternalTaskQueryHelper(this);
        JaxbQueryTaskResult result 
            = queryHelper.queryTaskOrProcInstAndAssociatedVariables(identityProvider.getName(), params, pageInfo);
        queryHelper.dispose();
        
        return createCorrectVariant(result, headers);
    }

    @GET
    @Path("/runtime/process")
    public Response queryProcessInstances() {
        String oper = getRelativePath();
        Map<String, String[]> params = makeQueryParametersLowerCase(getRequestParams());
        checkIfParametersAreAllowed(params, QueryResourceData.getQueryParameters(), true, oper);
        
        int[] pageInfo = getPageNumAndPageSize(params, oper);

        String identityNotNeeded = null;
        InternalProcInstQueryHelper queryHelper = new InternalProcInstQueryHelper(this);
        JaxbQueryProcessInstanceResult result 
            = queryHelper.queryTaskOrProcInstAndAssociatedVariables(identityNotNeeded, params, pageInfo);
        queryHelper.dispose();

        return createCorrectVariant(result, headers);
    }
  
    private static final String[] allowedQueryParams = { 
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
    @Path("/task")
    public Response taskSummaryQuery() {
        Map<String, String[]> params = getRequestParams();
        String oper = getRelativePath();

        checkIfParametersAreAllowed(params, Arrays.asList(allowedQueryParams), "task query");

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

        int[] pageInfo = getPageNumAndPageSize(params, oper);
        int maxResults = getMaxNumResultsNeeded(pageInfo);
        GetTasksByVariousFieldsCommand queryCmd = new GetTasksByVariousFieldsCommand(workItemIds, taskIds, procInstIds, busAdmins,
                potOwners, taskOwners, statuses, language, union, maxResults);
        queryCmd.setUserId(identityProvider.getName());

        List<TaskSummary> results = doRestTaskOperationWithTaskId((Long) null, queryCmd);

        logger.debug("{} results found.", results.size());
        JaxbTaskSummaryListResponse resultList = paginateAndCreateResult(pageInfo, results, new JaxbTaskSummaryListResponse());
        logger.debug("Returning {} results after pagination.", resultList.getList().size());

        return createCorrectVariant(resultList, headers);
    }
   
    // helper methods -------------------------------------------------------------------------------------------------------------
   
    static Map<String, String[]> makeQueryParametersLowerCase(Map<String, String[]> params) { 
        if( params == null || params.isEmpty() )  { 
            return params;
        }
        Map<String, String[]> lowerCaseParams = new HashMap<String, String[]>(params.size());
        for( Entry<String, String[]> entry : params.entrySet() ) { 
            String varName = entry.getKey();
            if( isNameValueParam(varName) ) { 
                int _index = varName.indexOf('_');
                varName = varName.substring(0, _index).toLowerCase() + varName.substring(_index);
            } else { 
                varName = varName.toLowerCase();
            }
            lowerCaseParams.put(varName, entry.getValue()) ;
        }
        return lowerCaseParams;
    }
    
    public static void checkIfParametersAreAllowed(Map<String, String[]> params, Collection<String> allowedParams, String oper) { 
        checkIfParametersAreAllowed(params, allowedParams, false, oper);
    }
    
    public static void checkIfParametersAreAllowed(Map<String, String[]> params, Collection<String> allowedParams, boolean checkSpecial, String oper ) { 
        if( params == null || params.isEmpty() )  { 
            return;
        }
       
        EACHPARAM: for( String queryParam : params.keySet() ) {
            for( String allowedParam : allowedParams ) {
                if( allowedParam.equalsIgnoreCase(queryParam) || paginationParams.contains(queryParam) ) {
                    continue EACHPARAM;
                }
                if( checkSpecial && isSpecialParameter(queryParam) ) { 
                    continue EACHPARAM;
                }
            }
            throw KieRemoteRestOperationException.badRequest(queryParam
                        + " is an unknown and unsupported query param for the " + oper + " operation.");
        }
    }
    

}
