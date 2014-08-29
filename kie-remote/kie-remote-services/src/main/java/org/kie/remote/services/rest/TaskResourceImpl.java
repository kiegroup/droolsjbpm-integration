package org.kie.remote.services.rest;

import java.util.List;
import java.util.Map;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.drools.core.util.StringUtils;
import org.jbpm.kie.services.api.IdentityProvider;
import org.jbpm.services.task.audit.commands.DeleteBAMTaskSummariesCommand;
import org.jbpm.services.task.commands.ActivateTaskCommand;
import org.jbpm.services.task.commands.ClaimNextAvailableTaskCommand;
import org.jbpm.services.task.commands.ClaimTaskCommand;
import org.jbpm.services.task.commands.CompleteTaskCommand;
import org.jbpm.services.task.commands.DelegateTaskCommand;
import org.jbpm.services.task.commands.ExitTaskCommand;
import org.jbpm.services.task.commands.FailTaskCommand;
import org.jbpm.services.task.commands.ForwardTaskCommand;
import org.jbpm.services.task.commands.GetContentCommand;
import org.jbpm.services.task.commands.GetTaskCommand;
import org.jbpm.services.task.commands.GetTasksByVariousFieldsCommand;
import org.jbpm.services.task.commands.NominateTaskCommand;
import org.jbpm.services.task.commands.ReleaseTaskCommand;
import org.jbpm.services.task.commands.ResumeTaskCommand;
import org.jbpm.services.task.commands.SkipTaskCommand;
import org.jbpm.services.task.commands.StartTaskCommand;
import org.jbpm.services.task.commands.StopTaskCommand;
import org.jbpm.services.task.commands.SuspendTaskCommand;
import org.jbpm.services.task.commands.TaskCommand;
import org.jbpm.services.task.impl.model.xml.JaxbContent;
import org.jbpm.services.task.impl.model.xml.JaxbTask;
import org.kie.api.task.model.OrganizationalEntity;
import org.kie.api.task.model.Status;
import org.kie.api.task.model.Task;
import org.kie.api.task.model.TaskSummary;
import org.kie.remote.common.exception.RestOperationException;
import org.kie.remote.services.jaxb.JaxbCommandsRequest;
import org.kie.remote.services.jaxb.JaxbCommandsResponse;
import org.kie.remote.services.jaxb.JaxbTaskSummaryListResponse;
import org.kie.remote.services.rest.api.TaskResource;
import org.kie.remote.services.util.FormURLGenerator;
import org.kie.services.client.serialization.jaxb.impl.task.JaxbTaskFormResponse;
import org.kie.services.client.serialization.jaxb.rest.JaxbGenericResponse;
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
 */
@Path("/task")
@RequestScoped
public class TaskResourceImpl extends ResourceBase implements TaskResource {

    private static final Logger logger = LoggerFactory.getLogger(RuntimeResourceImpl.class);
    
    /* REST information */
    @Context
    protected HttpHeaders headers;
    
    /* KIE information and processing */

    @Inject
    private FormURLGenerator formURLGenerator;

    @Inject
    protected IdentityProvider identityProvider;
   
    private static String[] allowedOperations = { 
        "activate", 
        "claim", 
        "claimnextavailable", 
        "complete", 
        "delegate", 
        "exit",
        "fail", 
        "forward", 
        "release", 
        "resume", 
        "skip", 
        "start", 
        "stop", 
        "suspend", 
        "nominate", 
        "content"};

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
    
    // Rest methods --------------------------------------------------------------------------------------------------------------

    @POST
    @Consumes(MediaType.APPLICATION_XML)
    @Produces(MediaType.APPLICATION_XML)
    @Path("/execute")
    public JaxbCommandsResponse execute(JaxbCommandsRequest cmdsRequest) {
        return restProcessJaxbCommandsRequest(cmdsRequest);
    }

    /* (non-Javadoc)
     * @see org.kie.remote.services.rest.TaskResource#query()
     */
    @Override
    @GET
    @Path("/query")
    public Response query() {
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
                throw RestOperationException.badRequest(queryParam + " is an unknown and unsupported query param for the task query operation." );
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

    /* (non-Javadoc)
     * @see org.kie.remote.services.rest.TaskResource#taskId(long)
     */
    @Override
    @GET
    @Path("/{taskId: [0-9-]+}")
    public Response taskId(@PathParam("taskId") long taskId) { 
        TaskCommand<?> cmd = new GetTaskCommand(taskId);
        JaxbTask task = (JaxbTask) doRestTaskOperation(taskId, cmd);
        if( task == null ) { 
            throw RestOperationException.notFound("Task " + taskId + " could not be found.");
        }
        return createCorrectVariant(task, headers);
    }

    /* (non-Javadoc)
     * @see org.kie.remote.services.rest.TaskResource#taskId_oper(long, java.lang.String)
     */
    @Override
    @POST
    @Path("/{taskId: [0-9-]+}/{oper: [a-zA-Z]+}")
    public Response taskId_oper(@PathParam("taskId") long taskId, @PathParam("oper") String operation) { 
        Map<String, String[]> params = getRequestParams();
        operation = checkThatOperationExists(operation, allowedOperations);
        String oper = getRelativePath();
        String userId = identityProvider.getName();
        logger.debug("Executing " + operation + " on task " + taskId + " by user " + userId );
       
        TaskCommand<?> cmd = null;
        
        if ("activate".equalsIgnoreCase(operation)) {
            cmd = new ActivateTaskCommand(taskId, userId);
        } else if ("claim".equalsIgnoreCase(operation)) {
            cmd = new ClaimTaskCommand(taskId, userId);
        } else if ("claimnextavailable".equalsIgnoreCase(operation)) {
            cmd = new ClaimNextAvailableTaskCommand(userId);
        } else if ("complete".equalsIgnoreCase(operation)) {
            Map<String, Object> data = extractMapFromParams(params, operation);
            cmd = new CompleteTaskCommand(taskId, userId, data);
        } else if ("delegate".equalsIgnoreCase(operation)) {
            String targetEntityId = getStringParam("targetEntityId", true, params, oper);
            cmd = new DelegateTaskCommand(taskId, userId, targetEntityId);
        } else if ("exit".equalsIgnoreCase(operation)) {
            cmd = new ExitTaskCommand(taskId, userId);
        } else if ("fail".equalsIgnoreCase(operation)) {
            Map<String, Object> data = extractMapFromParams(params, oper);
            cmd = new FailTaskCommand(taskId, userId, data);
        } else if ("forward".equalsIgnoreCase(operation)) {
            String targetEntityId = getStringParam("targetEntityId", true, params, oper);
            cmd = new ForwardTaskCommand(taskId, userId, targetEntityId);
        } else if ("release".equalsIgnoreCase(operation)) {
            cmd = new ReleaseTaskCommand(taskId, userId);
        } else if ("resume".equalsIgnoreCase(operation)) {
            cmd = new ResumeTaskCommand(taskId, userId);
        } else if ("skip".equalsIgnoreCase(operation)) {
            cmd = new SkipTaskCommand(taskId, userId);
        } else if ("start".equalsIgnoreCase(operation)) {
            cmd = new StartTaskCommand(taskId, userId);
        } else if ("stop".equalsIgnoreCase(operation)) {
            cmd = new StopTaskCommand(taskId, userId);
        } else if ("suspend".equalsIgnoreCase(operation)) {
            cmd = new SuspendTaskCommand(taskId, userId);
        } else if ("nominate".equalsIgnoreCase(operation)) {
            List<OrganizationalEntity> potentialOwners = getOrganizationalEntityListFromParams(params, true, oper);
            cmd = new NominateTaskCommand(taskId, userId, potentialOwners);
        } else {
            throw RestOperationException.badRequest("Unsupported operation: " + oper);
        }
        
        doRestTaskOperation(taskId, cmd);
        return createCorrectVariant(new JaxbGenericResponse(getRequestUri()), headers);
    }

    private static String checkThatOperationExists(String operation, String[] possibleOperations) {
        for (String oper : possibleOperations) {
            if (oper.equals(operation.trim().toLowerCase())) {
                return oper;
            }
        }
        throw RestOperationException.badRequest("Operation '" + operation + "' is not supported on tasks.");
    }
    
    /* (non-Javadoc)
     * @see org.kie.remote.services.rest.TaskResource#taskId_content(long)
     */
    @Override
    @GET
    @Path("/{taskId: [0-9-]+}/content")
    public Response taskId_content(@PathParam("taskId") long taskId) { 
        TaskCommand<?> cmd = new GetTaskCommand(taskId);
        Object result = doRestTaskOperation(taskId, cmd);
        if( result == null ) {
            throw RestOperationException.notFound("Task " + taskId + " could not be found.");
        }
        Task task = ((Task) result);
        long contentId = task.getTaskData().getDocumentContentId();
        JaxbContent content = null;
        if( contentId > -1 ) { 
            cmd = new GetContentCommand(contentId);
            result = processRequestBean.doRestTaskOperation(taskId, task.getTaskData().getDeploymentId(), task.getTaskData().getProcessInstanceId(), task, cmd);
            content = (JaxbContent) result;
        } else { 
            throw RestOperationException.notFound("Content for task " + taskId + " could not be found.");
        }
        return createCorrectVariant(content, headers);
    }

    /* (non-Javadoc)
     * @see org.kie.remote.services.rest.TaskResource#taskId_form(long)
     */
    @Override
    @GET
    @Path("/{taskId: [0-9-]+}/showTaskForm")
    public Response taskId_form(@PathParam("taskId") long taskId) {
        TaskCommand<?> cmd = new GetTaskCommand(taskId);
        Object result = doRestTaskOperation(taskId, cmd);

        if (result != null) {
            Map<String, String[]> requestParams = getRequestParams();

            String opener = "";

            List<String> openers = headers.getRequestHeader("host");
            if (openers.size() == 1) {
                opener = openers.get(0);
            }
                String formUrl = formURLGenerator.generateFormTaskURL(getBaseUri(), taskId, opener);
            if (!StringUtils.isEmpty(formUrl)) {
                JaxbTaskFormResponse response = new JaxbTaskFormResponse(formUrl, getRequestUri());
                return createCorrectVariant(response, headers);
            }
        }
        throw RestOperationException.notFound("Task " + taskId + " could not be found.");
    }
    
    /* (non-Javadoc)
     * @see org.kie.remote.services.rest.TaskResource#content_contentId(long)
     */
    @Override
    @GET
    @Path("/content/{contentId: [0-9-]+}")
    public Response content_contentId(@PathParam("contentId") long contentId) { 
        TaskCommand<?> cmd = new GetContentCommand(contentId);
        JaxbContent content = (JaxbContent) doRestTaskOperation(null, cmd);
        if( content == null ) { 
            throw RestOperationException.notFound("Content " + contentId + " could not be found.");
        }
        return createCorrectVariant(new JaxbContent(content), headers);
    }
    
    /* (non-Javadoc)
     * @see org.kie.remote.services.rest.TaskResource#bam_clear()
     */
    @Override
    @POST
    @Path("/history/bam/clear")
    public Response bam_clear() { 
        doRestTaskOperation(null, new DeleteBAMTaskSummariesCommand());
        return createCorrectVariant(new JaxbGenericResponse(getRelativePath()), headers);
    }
 
    private Object doRestTaskOperation(Long taskId, TaskCommand<?> cmd) { 
        return processRequestBean.doRestTaskOperation(taskId, null, null, null, cmd);
    }
}
