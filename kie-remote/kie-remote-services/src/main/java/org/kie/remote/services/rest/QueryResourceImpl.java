package org.kie.remote.services.rest;

import static org.kie.remote.services.rest.query.QueryResourceData.actionParamNameMap;
import static org.kie.remote.services.rest.query.QueryResourceData.getDates;
import static org.kie.remote.services.rest.query.QueryResourceData.getInts;
import static org.kie.remote.services.rest.query.QueryResourceData.getLongs;
import static org.kie.remote.services.rest.query.QueryResourceData.getTaskStatuses;
import static org.kie.remote.services.rest.query.QueryResourceData.metaRuntimeParams;
import static org.kie.remote.services.rest.query.QueryResourceData.metaRuntimeParamsShort;
import static org.kie.remote.services.rest.query.QueryResourceData.paramNameActionMap;

import java.util.ArrayDeque;
import java.util.Date;
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

import org.jbpm.kie.services.api.IdentityProvider;
import org.jbpm.process.audit.query.ProcInstLogQueryBuilderImpl;
import org.jbpm.services.task.commands.GetTasksByVariousFieldsCommand;
import org.jbpm.services.task.commands.TaskQueryDataCommand;
import org.kie.api.runtime.manager.audit.ProcessInstanceLog;
import org.kie.api.runtime.manager.audit.VariableInstanceLog;
import org.kie.api.task.model.Status;
import org.kie.api.task.model.TaskSummary;
import org.kie.internal.query.data.QueryData;
import org.kie.remote.services.jaxb.JaxbTaskSummaryListResponse;
import org.kie.remote.services.rest.exception.KieRemoteRestOperationException;
import org.kie.remote.services.rest.query.InternalProcInstQueryHelper;
import org.kie.remote.services.rest.query.InternalTaskQueryHelper;
import org.kie.remote.services.rest.query.RemoteServicesQueryCommandBuilder;
import org.kie.services.client.serialization.jaxb.impl.process.JaxbProcessInstance;
import org.kie.services.client.serialization.jaxb.impl.query.JaxbQueryProcessInstanceInfo;
import org.kie.services.client.serialization.jaxb.impl.query.JaxbQueryProcessInstanceResult;
import org.kie.services.client.serialization.jaxb.impl.query.JaxbQueryTaskInfo;
import org.kie.services.client.serialization.jaxb.impl.query.JaxbQueryTaskResult;
import org.kie.services.client.serialization.jaxb.impl.query.JaxbVariableInfo;
import org.kie.services.client.serialization.jaxb.impl.task.JaxbTaskSummary;

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
        Map<String, String[]> params = getRequestParams();
        int[] pageInfo = getPageNumAndPageSize(params, oper);
        int maxNumResults = getMaxNumResultsNeeded(pageInfo);

        InternalTaskQueryHelper queryHelper = new InternalTaskQueryHelper(this);
        JaxbQueryTaskResult result 
            = queryHelper.queryTasksAndVariables(identityProvider.getName(), params, pageInfo, maxNumResults);
        queryHelper.dispose();
        
        return createCorrectVariant(result, headers);
    }

    @GET
    @Path("/runtime/process")
    public Response queryProcessInstances() {
        String oper = getRelativePath();
        Map<String, String[]> params = getRequestParams();
        int[] pageInfo = getPageNumAndPageSize(params, oper);
        int maxNumResults = getMaxNumResultsNeeded(pageInfo);

        InternalProcInstQueryHelper queryHelper = new InternalProcInstQueryHelper(this);
        JaxbQueryProcessInstanceResult result 
            = queryHelper.queryProcessInstancesAndVariables(params, pageInfo, maxNumResults);
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

        for( String queryParam : params.keySet() ) {
            boolean allowed = false;
            for( String allowedParam : allowedQueryParams ) {
                if( allowedParam.equalsIgnoreCase(queryParam) || paginationParams.contains(queryParam) ) {
                    allowed = true;
                    break;
                }
            }
            if( !allowed ) {
                throw KieRemoteRestOperationException.badRequest(queryParam
                        + " is an unknown and unsupported query param for the task query operation.");
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
}
