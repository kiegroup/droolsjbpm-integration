package org.kie.services.remote.rest;

import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

import org.jbpm.services.task.commands.ActivateTaskCommand;
import org.jbpm.services.task.commands.StartTaskCommand;
import org.kie.api.command.Command;
import org.kie.services.remote.ejb.ProcessRequestBean;
import org.kie.services.remote.rest.exception.IncorrectRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/rest/task/{id: [0-9-]+}")
@RequestScoped
public class TaskResource {

    private Logger logger = LoggerFactory.getLogger(TaskResource.class);

    @EJB
    protected ProcessRequestBean processRequestBean;
    
    @PathParam("id")
    private Long taskId;

    @POST
    @Path("/{oper: [a-zA-Z]+}")
    public void doTaskOperation(@PathParam("oper") String operation, @Context UriInfo uriInfo, @QueryParam("userId") String userId) { 
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
        } else {
            throw new IncorrectRequestException("Unsupported operation: /task/" + taskId + "/" + operation );
        }
        Object result = processRequestBean.doTaskOperation(cmd);
    }
}
