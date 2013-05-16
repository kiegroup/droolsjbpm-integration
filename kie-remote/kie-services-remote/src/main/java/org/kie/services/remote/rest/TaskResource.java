package org.kie.services.remote.rest;

import java.util.ArrayList;
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
import javax.ws.rs.core.MediaType;

import org.jbpm.services.task.commands.ActivateTaskCommand;
import org.jbpm.services.task.commands.ClaimNextAvailableTaskCommand;
import org.jbpm.services.task.commands.ClaimTaskCommand;
import org.jbpm.services.task.commands.CompleteTaskCommand;
import org.jbpm.services.task.commands.DelegateTaskCommand;
import org.jbpm.services.task.commands.ExitTaskCommand;
import org.jbpm.services.task.commands.FailTaskCommand;
import org.jbpm.services.task.commands.ForwardTaskCommand;
import org.jbpm.services.task.commands.NominateTaskCommand;
import org.jbpm.services.task.commands.ReleaseTaskCommand;
import org.jbpm.services.task.commands.ResumeTaskCommand;
import org.jbpm.services.task.commands.SkipTaskCommand;
import org.jbpm.services.task.commands.StartTaskCommand;
import org.jbpm.services.task.commands.StopTaskCommand;
import org.kie.api.command.Command;
import org.kie.api.task.model.OrganizationalEntity;
import org.kie.services.client.api.command.serialization.jaxb.impl.JaxbCommandMessage;
import org.kie.services.remote.cdi.ProcessRequestBean;
import org.kie.services.remote.rest.exception.IncorrectRequestException;
import org.kie.services.remote.rest.jaxb.JaxbGenericResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/task")
@RequestScoped
public class TaskResource extends ResourceBase {

    private Logger logger = LoggerFactory.getLogger(TaskResource.class);

    @Inject
    private ProcessRequestBean processRequestBean;
    
    @Context 
    private HttpServletRequest request;
    
    private static String[] allowedOperations = { "activate", "claim", "claimnextavailable", "complete", "delegate", "exit",
            "fail", "forward", "release", "resume", "skip", "start", "stop", "suspend", "nominate" };


    @GET
    @Produces(MediaType.APPLICATION_XML)
    @Path("/")
    public JaxbGenericResponse getTaskInstanceInfo(@PathParam("oper") String operation) { 
        return null;
    }

    @POST
    @Consumes(MediaType.APPLICATION_XML)
    @Produces(MediaType.APPLICATION_XML)
    @Path("/execute")
    public JaxbGenericResponse execute(JaxbCommandMessage<?> cmdMsg) {
        List<Object> results = new ArrayList<Object>();
        for (Object cmd : cmdMsg.getCommands()) {
            Object result = processRequestBean.doTaskOperation((Command<?>) cmd);
            results.add(result);
        }
        return null;
    }

    @GET
    @Produces(MediaType.APPLICATION_XML)
    @Path("/query")
    public JaxbGenericResponse query(JaxbCommandMessage<?> cmdMsg) {
        List<Object> results = new ArrayList<Object>();
        for (Object cmd : cmdMsg.getCommands()) {
            Object result = processRequestBean.doTaskOperation((Command<?>) cmd);
            results.add(result);
        }
        return null;
    }

    @POST
    @Path("/{id: [0-9-]+}/{oper: [a-zA-Z]+}")
    public void doTaskOperation(@PathParam("id") long taskId, @PathParam("oper") String operation) { 
        Map<String, List<String>> formParams = getRequestParams(request);
        operation = checkThatOperationExists(operation, allowedOperations);        
        String userId = getStringParam("userId", true, formParams, operation);
        Command<?> cmd = null;
        if ("activate".equals(operation)) {
            cmd = new ActivateTaskCommand(taskId, userId);
        } else if ("claim".equals(operation)) {
            cmd = new ClaimTaskCommand(taskId, userId);
        } else if ("claimnextavailable".equals(operation)) {
            String language = getStringParam("language", false, formParams, operation);
            if (language == null) {
                language = "en-UK";
            }
            cmd = new ClaimNextAvailableTaskCommand(userId, language);
        } else if ("complete".equals(operation)) {
            Map<String, Object> data = extractMapFromParams(formParams, operation);
            cmd = new CompleteTaskCommand(taskId, userId, data);
        } else if ("delegate".equals(operation)) {
            String targetEntityId = getStringParam("targetEntityId", true, formParams, operation);
           cmd = new DelegateTaskCommand(taskId, userId, targetEntityId);
        } else if ("exit".equals(operation)) {
            cmd = new ExitTaskCommand(taskId, userId);
        } else if ("fail".equals(operation)) {
            Map<String, Object> data = extractMapFromParams(formParams, operation);
            cmd = new FailTaskCommand(taskId, userId, data);
        } else if ("forward".equals(operation)) {
            String targetEntityId = getStringParam("targetEntityId", true, formParams, operation);
            cmd = new ForwardTaskCommand(taskId, userId, targetEntityId);
        } else if ("release".equals(operation)) {
            cmd = new ReleaseTaskCommand(taskId, userId);
        } else if ("resume".equals(operation)) {
            cmd = new ResumeTaskCommand(taskId, userId);
        } else if ("skip".equals(operation)) {
            cmd = new SkipTaskCommand(taskId, userId);
        } else if ("start".equals(operation)) {
            cmd = new StartTaskCommand(taskId, userId);
        } else if ("stop".equals(operation)) {
            cmd = new StopTaskCommand(taskId, userId);
        } else if ("nominate".equals(operation)) {
            List<OrganizationalEntity> potentialOwners = getOrganizationalEntityListFromParams(formParams);
            cmd = new NominateTaskCommand(taskId, userId, potentialOwners);
        } else {
            throw new IncorrectRequestException("Unsupported operation: /task/" + taskId + "/" + operation);
        }
        processRequestBean.doTaskOperation(cmd);
    }

}
