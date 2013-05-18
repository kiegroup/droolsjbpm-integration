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

import org.drools.core.command.runtime.process.AbortProcessInstanceCommand;
import org.drools.core.command.runtime.process.AbortWorkItemCommand;
import org.drools.core.command.runtime.process.CompleteWorkItemCommand;
import org.drools.core.command.runtime.process.GetProcessInstanceCommand;
import org.drools.core.command.runtime.process.SignalEventCommand;
import org.drools.core.command.runtime.process.StartProcessCommand;
import org.drools.core.command.runtime.process.StartProcessInstanceCommand;
import org.jboss.resteasy.spi.BadRequestException;
import org.jbpm.services.task.commands.TaskCommand;
import org.kie.api.command.Command;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.services.client.serialization.jaxb.JaxbCommandMessage;
import org.kie.services.client.serialization.jaxb.impl.JaxbProcessInstance;
import org.kie.services.remote.cdi.ProcessRequestBean;
import org.kie.services.remote.rest.jaxb.JaxbGenericResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/runtime/{id: [a-zA-Z0-9-:\\.]+}")
@RequestScoped
public class RuntimeResource extends ResourceBase {

    private Logger logger = LoggerFactory.getLogger(RuntimeResource.class);

    @Inject
    protected ProcessRequestBean processRequestBean;

    @PathParam("id")
    private String deploymentId;
    
    @Context
    private HttpServletRequest request;

    // Rest methods --------------------------------------------------------------------------------------------------------------

    @POST
    @Consumes(MediaType.APPLICATION_XML)
    @Path("/execute")
    public JaxbGenericResponse execute(JaxbCommandMessage cmdMsg) {
        // TODO: add "ExecuteResource" for general execute mehod that differentiates based on package name?
        List<Object> results = new ArrayList<Object>();
        for( Object cmdObj : cmdMsg.getCommands() ) {
            Command<?> cmd = (Command<?>) cmdObj;
            if( cmd instanceof TaskCommand<?> ) {
                throw new BadRequestException("Command " + cmd.getClass().getSimpleName() + " is not supported for the KieSession."); 
            }
            Object result = processRequestBean.doKieSessionOperation(cmd, deploymentId);
            results.add(result);
        }
        
        // TODO: jaxb object for results
        return null;
    }

    @POST
    @Produces(MediaType.APPLICATION_XML)
    @Path("/process/{processDefId}/start")
    public JaxbGenericResponse startNewProcess(@PathParam("processDefId") String processId) { 
        Map<String, List<String>> formParams = getRequestParams(request);
        Map<String, Object> params = extractMapFromParams(formParams, "process/" + processId + "/start");
        Command<?> cmd = new StartProcessCommand(processId, params);
        
        processRequestBean.doKieSessionOperation(cmd, deploymentId);
        return new JaxbGenericResponse(request);
    }

    @GET
    @Produces(MediaType.APPLICATION_XML)
    @Path("/process/instance/{procInstId: [0-9]+}")
    public JaxbProcessInstance getProcessInstanceDetails(@PathParam("procInstId") Long procInstId) {
        Command<?> cmd = new GetProcessInstanceCommand(procInstId);
        Object result = processRequestBean.doKieSessionOperation(cmd, deploymentId);
        return new JaxbProcessInstance((ProcessInstance) result);
    }

    @POST
    @Produces(MediaType.APPLICATION_XML)
    @Path("/process/instance/{procInstId: [0-9]+}/{oper: [a-zA-Z]+}")
    public JaxbGenericResponse doProcessInstanceOperation(@PathParam("procInstId") Long procInstId, @PathParam("oper") String operation) { 
        Map<String, List<String>> formParams = getRequestParams(request);
        Command<?> cmd = null;
        if ("start".equals(operation.toLowerCase().trim())) {
            cmd = new StartProcessInstanceCommand(procInstId);
        } else if ("signal".equals(operation.toLowerCase().trim())) {
            String eventType = getStringParam("eventType", true, formParams, operation);
            Object event = getObjectParam("event", false, formParams, operation);
            cmd = new SignalEventCommand(procInstId, eventType, event);
        } else if ("abort".equals(operation.toLowerCase().trim())) {
            cmd = new AbortProcessInstanceCommand();
            ((AbortProcessInstanceCommand) cmd).setProcessInstanceId(procInstId);
        } else {
            throw new BadRequestException("Unsupported operation: /process/instance/" + procInstId + "/" + operation);
        }
        processRequestBean.doKieSessionOperation(cmd, deploymentId);
        return new JaxbGenericResponse(request);
    }

    @POST
    @Produces(MediaType.APPLICATION_XML)
    @Path("/signal/{signal: [a-zA-Z0-9-]+}")
    public JaxbGenericResponse signalEvent(@PathParam("signal") String signal) { 
        Map<String, List<String>> formParams = getRequestParams(request);
        Object event = getObjectParam("event", false, formParams, "signal/" + signal);
        Command<?> cmd = new SignalEventCommand(signal, event);
        processRequestBean.doKieSessionOperation(cmd, deploymentId);
        return new JaxbGenericResponse(request);
    }

    @POST
    @Path("/workitem/{workItemId: [0-9-]+}/{oper: [a-zA-Z]+}")
    public JaxbGenericResponse doWorkItemOperation(@PathParam("workItemId") Long workItemId, @PathParam("oper") String operation) { 
        Map<String, List<String>> formParams = getRequestParams(request);
        Command<?> cmd = null;
        if ("complete".equalsIgnoreCase((operation.trim()))) {
            Map<String, Object> results = extractMapFromParams(formParams, operation);
            cmd = new CompleteWorkItemCommand(workItemId, results);
        } else if ("abort".equals(operation.toLowerCase())) {
            cmd = new AbortWorkItemCommand(workItemId);
        } else {
            throw new BadRequestException("Unsupported operation: /process/instance/" + workItemId + "/" + operation);
        }
        processRequestBean.doKieSessionOperation(cmd, deploymentId);
        return new JaxbGenericResponse(request);
    }

}
