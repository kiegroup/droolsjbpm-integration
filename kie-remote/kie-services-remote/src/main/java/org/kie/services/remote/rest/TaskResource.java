package org.kie.services.remote.rest;

import static org.kie.services.remote.util.CommandsRequestUtil.restProcessJaxbCommandsRequest;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

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
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.jboss.resteasy.spi.BadRequestException;
import org.jboss.resteasy.spi.InternalServerErrorException;
import org.jboss.resteasy.spi.NotFoundException;
import org.jbpm.kie.services.api.IdentityProvider;
import org.jbpm.services.task.commands.*;
import org.jbpm.services.task.impl.model.TaskImpl;
import org.jbpm.services.task.impl.model.xml.JaxbContent;
import org.jbpm.services.task.impl.model.xml.JaxbTask;
import org.jbpm.services.task.query.TaskSummaryImpl;
import org.kie.api.command.Command;
import org.kie.api.task.model.Content;
import org.kie.api.task.model.OrganizationalEntity;
import org.kie.api.task.model.Status;
import org.kie.api.task.model.Task;
import org.kie.api.task.model.TaskSummary;
import org.kie.services.client.serialization.jaxb.impl.JaxbCommandsRequest;
import org.kie.services.client.serialization.jaxb.impl.JaxbCommandsResponse;
import org.kie.services.client.serialization.jaxb.impl.JaxbExceptionResponse;
import org.kie.services.client.serialization.jaxb.impl.task.JaxbTaskSummaryListResponse;
import org.kie.services.client.serialization.jaxb.rest.JaxbGenericResponse;
import org.kie.services.remote.cdi.ProcessRequestBean;
import org.kie.services.remote.util.Paginator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/task")
@RequestScoped
public class TaskResource extends ResourceBase {

    private static final Logger logger = LoggerFactory.getLogger(RuntimeResource.class);
    
    @Inject
    private ProcessRequestBean processRequestBean;

    @Context
    private HttpServletRequest request;
    
    @Context
    private Request restRequest;
    
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
        "nominate" };

    private static String [] allowedQueryParams = {
        "workItemId", 
        "taskId", 
        "businessAdministrator", 
        "potentialOwner",
        "status",
        "taskOwner",
        "processInstanceId",
        "language"
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
        
        List<Long> workItemIdList = getLongListParam(allowedQueryParams[0], false, params, "query", true);
        List<Long> taskIdList = getLongListParam(allowedQueryParams[1], false, params, "query", true);
        List<String> busAdminList = getStringListParam(allowedQueryParams[2], false, params, "query");
        List<String> potOwnList = getStringListParam(allowedQueryParams[3], false, params, "query");
        List<String> statusStrList = getStringListParam(allowedQueryParams[4], false, params, "query");
        List<String> taskOwnList = getStringListParam(allowedQueryParams[5], false, params, "query");
        List<Long> procInstIdList = getLongListParam(allowedQueryParams[6], false, params, "query", true);
        String language = getStringParam(allowedQueryParams[7], false, params, "query");
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
        
        int [] pageInfo = getPageNumAndPageSize(params);
        Paginator<TaskSummaryImpl> paginator = new Paginator<TaskSummaryImpl>();
        
        /**
         * TODO: talk with Mauricio about new query command that accepts all of the above params? 
        String andStr = getStringParam("and", false, params, "query");
        boolean and = false;
        if( andStr != null && Boolean.parseBoolean(andStr) ) {
            and = true;
        }
        */

        // clean up params
        if (language == null) {
            language = "en-UK";
        }
        List<Status> statusList = convertStringListToStatusList(statusStrList);

        // process params/cmds
        Queue<Command<?>> cmds = new LinkedList<Command<?>>();
        if (!workItemIdList.isEmpty()) {
            for (Long workItemId : workItemIdList) {
                cmds.add(new GetTaskByWorkItemIdCommand(workItemId));
            }
        }
        if (!taskIdList.isEmpty()) {
            for (Long taskId : taskIdList) {
                cmds.add(new GetTaskCommand(taskId));
            }
        }

        Set<TaskSummaryImpl> alreadyRetrievedSet = new HashSet<TaskSummaryImpl>();
        List<TaskSummaryImpl> results = new ArrayList<TaskSummaryImpl>();
        
        Command<?> cmd = null;
        while (!cmds.isEmpty()) {
            cmd = cmds.poll();
            logger.debug( "query: " + cmd.getClass().getSimpleName());
            TaskImpl task = (TaskImpl) internalDoTaskOperation(cmd, "Unable to execute " + cmd.getClass().getSimpleName());
            if (task != null) {
                TaskSummaryImpl taskSum = convertTaskToTaskSummary(task);
                if( alreadyRetrievedSet.add(taskSum) ) { 
                    results.add(taskSum);
                }
            }
        }

        if( results.size() >= pageInfo[2] ) { 
            results = paginator.paginate(pageInfo, results);
            responseObj = new JaxbTaskSummaryListResponse(results);
            return createCorrectVariant(responseObj, restRequest);
        }
        
        int assignments = 0;
        assignments += potOwnList.isEmpty() ? 0 : 1;
        assignments += busAdminList.isEmpty() ? 0 : 1;
        assignments += taskOwnList.isEmpty() ? 0 : 1;

        if (assignments == 0) {
            if (!procInstIdList.isEmpty()) {
                if (!statusList.isEmpty()) {
                    for (Long procInstId : procInstIdList) {
                        cmds.add(new GetTasksByStatusByProcessInstanceIdCommand(procInstId.longValue(), language, statusList));
                    }
                } else {
                    for (Long procInstId : procInstIdList) {
                        cmd = new GetTasksByProcessInstanceIdCommand(procInstId);
                        logger.debug( "query: " + cmd.getClass().getSimpleName());
                        @SuppressWarnings("unchecked")
                        List<Long> procInstTaskIdList = (List<Long>) internalDoTaskOperation(cmd, "Unable to execute " + cmd.getClass().getSimpleName());
                        for (Long taskId : procInstTaskIdList) {
                            cmd = new GetTaskCommand(taskId);
                            TaskImpl task = (TaskImpl) internalDoTaskOperation(cmd, "Unable to execute " + cmd.getClass().getSimpleName());
                            if (task != null) {
                                TaskSummaryImpl taskSum = convertTaskToTaskSummary(task);
                                if( alreadyRetrievedSet.add(taskSum) ) { 
                                    results.add(taskSum);
                                }
                            }
                        }
                        if( results.size() >= pageInfo[2] ) { 
                            results = paginator.paginate(pageInfo, results);
                            responseObj = new JaxbTaskSummaryListResponse(results);
                            return createCorrectVariant(responseObj, restRequest);
                        }
                    }
                }
            }
        } else {
            if (!busAdminList.isEmpty()) {
                for (String userId : busAdminList) {
                    cmds.add(new GetTaskAssignedAsBusinessAdminCommand(userId, language));
                }
            }
            if (!potOwnList.isEmpty()) {
                if (statusList.isEmpty()) {
                    for (String userId : potOwnList) {
                        cmds.add(new GetTaskAssignedAsPotentialOwnerCommand(userId, language));
                    }
                } else {
                    for (String userId : potOwnList) {
                        cmds.add(new GetTaskAssignedAsPotentialOwnerCommand(userId, language, statusList));
                    }

                }
            }
            if (!taskOwnList.isEmpty()) {
                if (statusList.isEmpty()) {
                    for (String userId : taskOwnList) {
                        cmds.add(new GetTasksOwnedCommand(userId, language));
                    }
                } else {
                    for (String userId : taskOwnList) {
                        cmds.add(new GetTasksOwnedCommand(userId, language, statusList));
                    }
                }
            }
        }

        while (!cmds.isEmpty()) {
            cmd = cmds.poll();
            logger.debug( "query: " + cmd.getClass().getSimpleName());
            @SuppressWarnings("unchecked")
            List<TaskSummary> taskSummaryList = (List<TaskSummary>) internalDoTaskOperation(cmd, "Unable to execute " + cmd.getClass().getSimpleName());
            if (taskSummaryList != null && !taskSummaryList.isEmpty()) {
                for (TaskSummary taskSummary : taskSummaryList) {
                    TaskSummaryImpl taskSum = (TaskSummaryImpl) taskSummary;
                    if( alreadyRetrievedSet.add(taskSum) ) { 
                        results.add(taskSum);
                    }
                }
            }
            if( results.size() >= pageInfo[2] ) { 
                results = paginator.paginate(pageInfo, results);
                responseObj = new JaxbTaskSummaryListResponse(results);
                return createCorrectVariant(responseObj, restRequest);
            }
        }
        
        results = paginator.paginate(pageInfo, results);
        responseObj= new JaxbTaskSummaryListResponse(results);
        return createCorrectVariant(responseObj, restRequest);
    }

    @GET
    @Path("/{taskId: [0-9-]+}")
    public Response getTaskInstanceInfo(@PathParam("taskId") long taskId) { 
        Command<?> cmd = new GetTaskCommand(taskId);
        Task task = (Task) internalDoTaskOperation(cmd, "Unable to get task " + taskId);
        if( task == null ) { 
            throw new NotFoundException("Task " + taskId + " could not be found.");
        }
        return createCorrectVariant(new JaxbTask(task), restRequest);
    }

    @POST
    @Produces(MediaType.APPLICATION_XML)
    @Path("/{taskId: [0-9-]+}/{oper: [a-zA-Z]+}")
    public Response doTaskOperation(@PathParam("taskId") long taskId, @PathParam("oper") String operation) { 
        Map<String, List<String>> params = getRequestParams(request);
        operation = checkThatOperationExists(operation, allowedOperations);        
        String userId = identityProvider.getName();
        logger.debug("Executing " + operation + " on task " + taskId + " by user " + userId );
       
        Command<?> cmd = null;
        cmd = new GetTaskCommand(taskId);
        if( internalDoTaskOperation(cmd, "Unable to check if task " + taskId + " exists") == null ) { 
            throw new NotFoundException("Task " + taskId + " could not be found.");
        }
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
        } else {
            throw new BadRequestException("Unsupported operation: /task/" + taskId + "/" + operation);
        }
        internalDoTaskOperation(cmd, "Unable to " + operation + " task " + taskId);
        return createCorrectVariant(new JaxbGenericResponse(request), restRequest);
    }

    @GET
    @Path("/{taskId: [0-9-]+}/content")
    public Response getTaskContent(@PathParam("taskId") long taskId) { 
        Command<?> cmd = new GetTaskCommand(taskId);
        Object result = internalDoTaskOperation(cmd, "Unable to get task " + taskId);
        if( result == null ) { 
            throw new NotFoundException("Task " + taskId + " could not be found.");
        }
        long contentId = ((Task) result).getTaskData().getDocumentContentId();
        Content content = null;
        if( contentId > -1 ) { 
            cmd = new GetContentCommand(contentId);
            result = internalDoTaskOperation(cmd, "Unable get content " + contentId + " (from task " + taskId + ")");
            content = (Content) result;
        }
        return createCorrectVariant(new JaxbContent(content), restRequest);
    }
    
    @GET
    @Path("/content/{contentId: [0-9-]+}")
    public Response getContent(@PathParam("contentId") long contentId) { 
        Command<?> cmd = new GetContentCommand(contentId);
        Content content = (Content) internalDoTaskOperation(cmd, "Unable to get task content " + contentId);
        if( content == null ) { 
            throw new NotFoundException("Content " + contentId + " could not be found.");
        }
        return createCorrectVariant(new JaxbContent(content), restRequest);
    }
    
    // Helper methods --------------------------------------------------------------------------------------------------------------

    private Object internalDoTaskOperation(Command<?> cmd, String errorMsg) { 
        Object result = processRequestBean.doTaskOperation(cmd);
        if( result instanceof JaxbExceptionResponse ) { 
           Exception cause = ((JaxbExceptionResponse) result).getCause();
           if( cause instanceof RuntimeException ) { 
               throw (RuntimeException) cause;
           } else { 
               throw new InternalServerErrorException(errorMsg, cause);
           }
        }
        return result;
    }
    
}
