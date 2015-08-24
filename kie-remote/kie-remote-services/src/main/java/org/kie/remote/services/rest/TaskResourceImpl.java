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

import static org.kie.internal.remote.PermissionConstants.*;
import static org.kie.internal.remote.PermissionConstants.REST_ROLE;

import java.util.List;
import java.util.Map;

import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

import org.drools.core.util.StringUtils;
import org.jbpm.services.task.audit.commands.DeleteBAMTaskSummariesCommand;
import org.jbpm.services.task.commands.ActivateTaskCommand;
import org.jbpm.services.task.commands.ClaimNextAvailableTaskCommand;
import org.jbpm.services.task.commands.ClaimTaskCommand;
import org.jbpm.services.task.commands.CompleteTaskCommand;
import org.jbpm.services.task.commands.DelegateTaskCommand;
import org.jbpm.services.task.commands.ExitTaskCommand;
import org.jbpm.services.task.commands.FailTaskCommand;
import org.jbpm.services.task.commands.ForwardTaskCommand;
import org.jbpm.services.task.commands.GetContentByIdCommand;
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
import org.kie.internal.identity.IdentityProvider;
import org.kie.remote.services.rest.exception.KieRemoteRestOperationException;
import org.kie.remote.services.util.FormURLGenerator;
import org.kie.services.client.serialization.jaxb.impl.task.JaxbTaskFormResponse;
import org.kie.services.client.serialization.jaxb.rest.JaxbGenericResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This resource provides operations to manage (human) tasks.
 */
@Path("/task")
@RequestScoped
public class TaskResourceImpl extends ResourceBase {

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

    @GET
    @Path("/query")
    @RolesAllowed({REST_ROLE, REST_QUERY_ROLE})
    @Deprecated
    public Response query() {
        return queryResource.taskSummaryQuery();
    }
   
    @GET
    @Path("/{taskId: [0-9-]+}")
    @RolesAllowed({REST_ROLE, REST_TASK_RO_ROLE, REST_TASK_ROLE})
    public Response getTask(@PathParam("taskId") long taskId) { 
        TaskCommand<?> cmd = new GetTaskCommand(taskId);
        JaxbTask task = (JaxbTask) doRestTaskOperationWithTaskId(taskId, cmd);
        if( task == null ) { 
            throw KieRemoteRestOperationException.notFound("Task " + taskId + " could not be found.");
        }
        return createCorrectVariant(task, headers);
    }

    @POST
    @Path("/{taskId: [0-9-]+}/{oper: [a-zA-Z]+}")
    @RolesAllowed({REST_ROLE, REST_TASK_ROLE})
    public Response doTaskOperation(@PathParam("taskId") long taskId, @PathParam("oper") String operation) { 
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
   
    // TODO: in 7, make sure to use GetContentByIdForUserCommand
    @GET
    @Path("/{taskId: [0-9-]+}/content")
    @RolesAllowed({REST_ROLE, REST_TASK_RO_ROLE, REST_TASK_ROLE})
    public Response getTaskContentByTaskId(@PathParam("taskId") long taskId) { 
        TaskCommand<?> cmd = new GetTaskCommand(taskId);
        Object result = doRestTaskOperationWithTaskId(taskId, cmd);
        if( result == null ) {
            throw KieRemoteRestOperationException.notFound("Task " + taskId + " could not be found.");
        }
        Task task = ((Task) result);
        long contentId = task.getTaskData().getDocumentContentId();
        JaxbContent jaxbContent = null;
        if( contentId > -1 ) { 
            cmd = new GetContentByIdCommand(contentId);
            result = processRequestBean.doRestTaskOperation(taskId, task.getTaskData().getDeploymentId(), task.getTaskData().getProcessInstanceId(), task, cmd);
            jaxbContent = (JaxbContent) result;
        } else { 
            throw KieRemoteRestOperationException.notFound("Content for task " + taskId + " could not be found.");
        }
        return createCorrectVariant(jaxbContent, headers);
    }
    
    @GET
    @Path("/{taskId: [0-9-]+}/showTaskForm")
    @RolesAllowed({REST_ROLE, REST_TASK_RO_ROLE, REST_TASK_ROLE})
    public Response getTaskFormByTaskId(@PathParam("taskId") long taskId) {
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
    
    // TODO: in 7, make sure to use GetContentByIdForUserCommand
    @GET
    @Path("/content/{contentId: [0-9-]+}")
    @RolesAllowed({REST_ROLE, REST_TASK_RO_ROLE, REST_TASK_ROLE})
    public Response getTaskContentByContentId(@PathParam("contentId") long contentId) { 
        TaskCommand<?> cmd = new GetContentByIdCommand(contentId);
        cmd.setUserId(identityProvider.getName());
        JaxbContent jaxbContent = (JaxbContent) doRestTaskOperation(cmd);
        if( jaxbContent == null ) { 
            throw KieRemoteRestOperationException.notFound("Content " + contentId + " could not be found.");
        }
        return createCorrectVariant(jaxbContent, headers);
    }
    
    @POST
    @Path("/history/bam/clear")
    @RolesAllowed({REST_ROLE, REST_TASK_ROLE})
    public Response clearTaskBamHistory() { 
        doRestTaskOperation(new DeleteBAMTaskSummariesCommand());
        return createCorrectVariant(new JaxbGenericResponse(getRelativePath()), headers);
    }
 

}
