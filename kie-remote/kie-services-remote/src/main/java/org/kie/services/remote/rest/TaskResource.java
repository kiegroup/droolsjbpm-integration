package org.kie.services.remote.rest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import org.jbpm.services.task.commands.ActivateTaskCommand;
import org.jbpm.services.task.commands.CompleteTaskCommand;
import org.jbpm.services.task.commands.StartTaskCommand;
import org.kie.api.command.Command;
import org.kie.services.client.api.command.serialization.jaxb.impl.JaxbCommandMessage;
import org.kie.services.remote.cdi.ProcessRequestBean;
import org.kie.services.remote.rest.exception.IncorrectRequestException;
import org.kie.services.remote.rest.jaxb.JaxbGenericResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/task")
@RequestScoped
public class TaskResource {

    private Logger logger = LoggerFactory.getLogger(TaskResource.class);

    @Inject
    protected ProcessRequestBean processRequestBean;
    
    @POST
    @Path("/{id: [0-9-]+}/{oper: [a-zA-Z]+}")
    public void doTaskOperation(@PathParam("id") long taskId, @PathParam("oper") String operation, @Context UriInfo uriInfo, @QueryParam("userId") String userId) { 
        Command cmd; 
        if (userId == null) {
        	// TODO: error handling
        	throw new RuntimeException("start task: userId null");
        }
        if ("activate".equals(operation.toLowerCase().trim())) {
            uriInfo.getQueryParameters();
            // TODO: extract params
            cmd = new ActivateTaskCommand(taskId, userId);
        } else if ("start".equals(operation.toLowerCase().trim())) {
            uriInfo.getQueryParameters();
            // TODO: extract params
            System.out.println("StartTaskCommand " + taskId + " " + userId);
            cmd = new StartTaskCommand(taskId, userId);
        } else if ("complete".equals(operation.toLowerCase().trim())) {
            uriInfo.getQueryParameters();
            // TODO: extract params
            System.out.println("CompleteTaskCommand " + taskId + " " + userId);
            Map<String, Object> result = new HashMap<String, Object>();
            cmd = new CompleteTaskCommand(taskId, userId, result);
        } else {
            throw new IncorrectRequestException("Unsupported operation: /task/" + taskId + "/" + operation );
        }
        Object result = processRequestBean.doTaskOperation(cmd);
    }

    @POST
    @Consumes(MediaType.APPLICATION_XML)
    @Path("/execute")
    public JaxbGenericResponse execute(JaxbCommandMessage cmdMsg) {
        List<Object> results = new ArrayList<Object>();
        for( Object cmd : cmdMsg.getCommands() ) {
            Object result = processRequestBean.doTaskOperation((Command) cmd);
            results.add(result);
        }
        return null;
    }
}
