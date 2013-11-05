package org.kie.services.remote.rest;

import static org.kie.services.client.api.command.AcceptedCommands.TASK_COMMANDS_THAT_INFLUENCE_KIESESSION;

import java.util.List;
import java.util.Map;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.jboss.resteasy.spi.BadRequestException;
import org.jboss.resteasy.spi.NotFoundException;
import org.jbpm.kie.services.api.IdentityProvider;
import org.jbpm.services.task.commands.*;
import org.jbpm.services.task.impl.model.xml.JaxbContent;
import org.jbpm.services.task.impl.model.xml.JaxbTask;
import org.jbpm.services.task.query.TaskSummaryImpl;
import org.kie.api.task.model.OrganizationalEntity;
import org.kie.api.task.model.Status;
import org.kie.api.task.model.Task;
import org.kie.services.client.serialization.jaxb.impl.JaxbCommandsRequest;
import org.kie.services.client.serialization.jaxb.impl.JaxbCommandsResponse;
import org.kie.services.client.serialization.jaxb.impl.task.JaxbTaskSummaryListResponse;
import org.kie.services.client.serialization.jaxb.rest.JaxbGenericResponse;
import org.kie.services.remote.util.Paginator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/task")
@RequestScoped
public class TaskResource extends ResourceBase {

    private static final Logger logger = LoggerFactory.getLogger(RuntimeResource.class);
    
    /* REST information */
    @Context
    private HttpHeaders headers;
    
    @Context
    private HttpServletRequest request;
    
    @Context
    private Request restRequest;

    /* KIE information and processing */
    @Inject
    private RestProcessRequestBean processRequestBean;

    @Inject
    private IdentityProvider identityProvider;
   
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
        "workItemId", 
        "taskId", 
        "businessAdministrator", 
        "potentialOwner",
        "status",
        "taskOwner",
        "processInstanceId",
        "union"
    };
    
    // Rest methods --------------------------------------------------------------------------------------------------------------

    @POST
    @Consumes(MediaType.APPLICATION_XML)
    @Produces(MediaType.APPLICATION_XML)
    @Path("/execute")
    public JaxbCommandsResponse execute(JaxbCommandsRequest cmdsRequest) {
        return restProcessJaxbCommandsRequest(cmdsRequest, processRequestBean);
    }

    @GET
    @Path("/query")
    public Response query(@Context UriInfo uriInfo) {
        JaxbTaskSummaryListResponse responseObj = null;
        Map<String, List<String>> params = getRequestParams(request);
        
        int [] pageInfo = getPageNumAndPageSize(params);
        Paginator<TaskSummaryImpl> paginator = new Paginator<TaskSummaryImpl>();
        
        for( String queryParam : params.keySet() ) { 
            boolean allowed = false;
            for( String allowedParam : allowedQueryParams ) { 
                if( allowedParam.equalsIgnoreCase(queryParam) ) { 
                   allowed = true;
                   break;
                }
            }
            if( ! allowed ) { 
                throw new BadRequestException(queryParam + " is an unknown and unsupported query param for the task query operation." );
            }
        }
        
        List<Long> workItemIds = getLongListParam(allowedQueryParams[0], false, params, "query", true);
        List<Long> taskIds = getLongListParam(allowedQueryParams[1], false, params, "query", true);
        List<Long> procInstIds = getLongListParam(allowedQueryParams[6], false, params, "query", true);
        List<String> busAdmins = getStringListParam(allowedQueryParams[2], false, params, "query");
        List<String> potOwners = getStringListParam(allowedQueryParams[3], false, params, "query");
        List<String> taskOwners = getStringListParam(allowedQueryParams[5], false, params, "query");
        String unionStr = getStringParam(allowedQueryParams[7], false, params, "query");
        boolean union = Boolean.parseBoolean(unionStr); // null, etc == false
        
        List<String> statusStrList = getStringListParam(allowedQueryParams[4], false, params, "query");
        List<Status> statuses = convertStringListToStatusList(statusStrList);
        
        TaskCommand<?> queryCmd = new GetTasksByVariousFieldsCommand(workItemIds, taskIds, procInstIds, busAdmins, potOwners, taskOwners, statuses, union);
        
        List<TaskSummaryImpl> results = (List<TaskSummaryImpl>) processRequestBean.doTaskOperation(
                queryCmd, 
                "Unable to execute " + queryCmd.getClass().getSimpleName());

        results = paginator.paginate(pageInfo, results);
        responseObj = new JaxbTaskSummaryListResponse(results);
        return createCorrectVariant(responseObj, headers);
    }

    @GET
    @Path("/{taskId: [0-9-]+}")
    public Response getTaskInstanceInfo(@PathParam("taskId") long taskId) { 
        TaskCommand<?> cmd = new GetTaskCommand(taskId);
        Task task = (Task) processRequestBean.doTaskOperation(
                cmd, 
                "Unable to get task " + taskId);
        if( task == null ) { 
            throw new NotFoundException("Task " + taskId + " could not be found.");
        }
        return createCorrectVariant(new JaxbTask(task), headers);
    }

    @POST
    @Path("/{taskId: [0-9-]+}/{oper: [a-zA-Z]+}")
    public Response doTaskOperation(@PathParam("taskId") long taskId, @PathParam("oper") String operation) { 
        Map<String, List<String>> params = getRequestParams(request);
        operation = checkThatOperationExists(operation, allowedOperations);        
        String userId = identityProvider.getName();
        logger.debug("Executing " + operation + " on task " + taskId + " by user " + userId );
       
        TaskCommand<?> cmd = null;
        
        if ("activate".equalsIgnoreCase(operation)) {
            cmd = new ActivateTaskCommand(taskId, userId);
        } else if ("claim".equalsIgnoreCase(operation)) {
            cmd = new ClaimTaskCommand(taskId, userId);
        } else if ("claimnextavailable".equalsIgnoreCase(operation)) {
            String language = getStringParam("language", false, params, operation);
            if (language == null) {
                language = "en-UK";
            }
            cmd = new ClaimNextAvailableTaskCommand(userId, language);
        } else if ("complete".equalsIgnoreCase(operation)) {
            Map<String, Object> data = extractMapFromParams(params, operation);
            cmd = new CompleteTaskCommand(taskId, userId, data);
        } else if ("delegate".equalsIgnoreCase(operation)) {
            String targetEntityId = getStringParam("targetEntityId", true, params, operation);
            cmd = new DelegateTaskCommand(taskId, userId, targetEntityId);
        } else if ("exit".equalsIgnoreCase(operation)) {
            cmd = new ExitTaskCommand(taskId, userId);
        } else if ("fail".equalsIgnoreCase(operation)) {
            Map<String, Object> data = extractMapFromParams(params, operation);
            cmd = new FailTaskCommand(taskId, userId, data);
        } else if ("forward".equalsIgnoreCase(operation)) {
            String targetEntityId = getStringParam("targetEntityId", true, params, operation);
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
            List<OrganizationalEntity> potentialOwners = getOrganizationalEntityListFromParams(params);
            cmd = new NominateTaskCommand(taskId, userId, potentialOwners);
        } else if("content".equalsIgnoreCase(operation)) { 
            cmd = new GetTaskCommand(taskId);
            Object result = processRequestBean.doTaskOperationAndSerializeResult(
                    cmd,
                    "Unable to get task " + taskId);
            if( result == null ) {
                throw new NotFoundException("Task " + taskId + " could not be found.");
            }
            long contentId = ((Task) result).getTaskData().getDocumentContentId();
            JaxbContent content = null;
            if( contentId > -1 ) { 
                cmd = new GetContentCommand(contentId);
                result = processRequestBean.doTaskOperationAndSerializeResult(
                        cmd, 
                        "Unable get content " + contentId + " (from task " + taskId + ")");
                content = (JaxbContent) content;
            } 
            return createCorrectVariant(content, headers);
        } else {
            throw new BadRequestException("Unsupported operation: /task/" + taskId + "/" + operation);
        }
        
        String errorMsg = "Unable to " + operation + " task " + taskId;
        if( TASK_COMMANDS_THAT_INFLUENCE_KIESESSION.contains(cmd.getClass()) ) { 
            Task task = (Task) processRequestBean.doTaskOperation(
                    new GetTaskCommand(taskId),
                    "Task " + taskId + " does not exist or unable to check if it exists");
            if( task == null ) {
                throw new NotFoundException("Task " + taskId + " could not be found.");
            }
            processRequestBean.doTaskOperationOnDeployment(
                    cmd, 
                    task.getTaskData().getDeploymentId(),
                    task.getTaskData().getProcessInstanceId(), 
                    errorMsg);
        } else { 
            processRequestBean.doTaskOperation(cmd, errorMsg);
        }
        
        return createCorrectVariant(new JaxbGenericResponse(request), headers);
    }

    private static String checkThatOperationExists(String operation, String[] possibleOperations) {
        for (String oper : possibleOperations) {
            if (oper.equals(operation.trim().toLowerCase())) {
                return oper;
            }
        }
        throw new BadRequestException("Operation '" + operation + "' is not supported on tasks.");
    }
    
    @GET
    @Path("/content/{contentId: [0-9-]+}")
    public Response getContent(@PathParam("contentId") long contentId) { 
        TaskCommand<?> cmd = new GetContentCommand(contentId);
        JaxbContent content = (JaxbContent) processRequestBean.doTaskOperationAndSerializeResult(cmd, "Unable to get task content " + contentId);
        if( content == null ) { 
            throw new NotFoundException("Content " + contentId + " could not be found.");
        }
        return createCorrectVariant(new JaxbContent(content), headers);
    }
    
}
