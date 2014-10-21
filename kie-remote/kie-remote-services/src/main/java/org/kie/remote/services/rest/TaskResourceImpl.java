package org.kie.remote.services.rest;

import java.util.List;
import java.util.Map;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
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
import org.kie.api.task.model.Task;
import org.kie.remote.services.rest.api.TaskResource;
import org.kie.remote.services.rest.exception.KieRemoteRestOperationException;
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
   
    @Inject
    protected QueryResourceImpl queryResource;
   
    private static final String[] allowedOperations = { 
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
    
    // Rest methods --------------------------------------------------------------------------------------------------------------

    @Override
    @Deprecated
    public Response query() {
        return queryResource.taskSummaryQuery();
    }
    
    @Override
    public Response getTask(long taskId) { 
        TaskCommand<?> cmd = new GetTaskCommand(taskId);
        JaxbTask task = (JaxbTask) doRestTaskOperationWithTaskId(taskId, cmd);
        if( task == null ) { 
            throw KieRemoteRestOperationException.notFound("Task " + taskId + " could not be found.");
        }
        return createCorrectVariant(task, headers);
    }

    @Override
    public Response doTaskOperation(long taskId, String operation) { 
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
            throw KieRemoteRestOperationException.badRequest("Unsupported operation: " + oper);
        }
        
        doRestTaskOperationWithTaskId(taskId, cmd);
        return createCorrectVariant(new JaxbGenericResponse(getRequestUri()), headers);
    }

    private static String checkThatOperationExists(String operation, String[] possibleOperations) {
        for (String oper : possibleOperations) {
            if (oper.equals(operation.trim().toLowerCase())) {
                return oper;
            }
        }
        throw KieRemoteRestOperationException.badRequest("Operation '" + operation + "' is not supported on tasks.");
    }
    
    @Override
    public Response getTaskContentByTaskId(long taskId) { 
        TaskCommand<?> cmd = new GetTaskCommand(taskId);
        Object result = doRestTaskOperationWithTaskId(taskId, cmd);
        if( result == null ) {
            throw KieRemoteRestOperationException.notFound("Task " + taskId + " could not be found.");
        }
        Task task = ((Task) result);
        long contentId = task.getTaskData().getDocumentContentId();
        JaxbContent content = null;
        if( contentId > -1 ) { 
            cmd = new GetContentCommand(contentId);
            result = processRequestBean.doRestTaskOperation(taskId, task.getTaskData().getDeploymentId(), task.getTaskData().getProcessInstanceId(), task, cmd);
            content = (JaxbContent) result;
        } else { 
            throw KieRemoteRestOperationException.notFound("Content for task " + taskId + " could not be found.");
        }
        return createCorrectVariant(content, headers);
    }

    @Override
    public Response getTaskFormByTaskId(long taskId) {
        TaskCommand<?> cmd = new GetTaskCommand(taskId);
        Object result = doRestTaskOperationWithTaskId(taskId, cmd);

        if (result != null) {
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
        throw KieRemoteRestOperationException.notFound("Task " + taskId + " could not be found.");
    }
    
    @Override
    public Response getTaskContentByContentId(long contentId) { 
        TaskCommand<?> cmd = new GetContentCommand(contentId);
        cmd.setUserId(identityProvider.getName());
        JaxbContent content = (JaxbContent) doRestTaskOperation(cmd);
        if( content == null ) { 
            throw KieRemoteRestOperationException.notFound("Content " + contentId + " could not be found.");
        }
        return createCorrectVariant(new JaxbContent(content), headers);
    }
    
    @Override
    public Response clearTaskBamHistory() { 
        doRestTaskOperation(new DeleteBAMTaskSummariesCommand());
        return createCorrectVariant(new JaxbGenericResponse(getRelativePath()), headers);
    }
 

}
