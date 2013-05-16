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
import javax.ws.rs.core.MultivaluedMap;

import org.drools.core.command.runtime.process.AbortProcessInstanceCommand;
import org.drools.core.command.runtime.process.AbortWorkItemCommand;
import org.drools.core.command.runtime.process.CompleteWorkItemCommand;
import org.drools.core.command.runtime.process.GetProcessInstanceCommand;
import org.drools.core.command.runtime.process.SignalEventCommand;
import org.drools.core.command.runtime.process.StartProcessCommand;
import org.drools.core.command.runtime.process.StartProcessInstanceCommand;
import org.kie.api.command.Command;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.services.client.api.command.serialization.jaxb.impl.JaxbCommandMessage;
import org.kie.services.remote.cdi.ProcessRequestBean;
import org.kie.services.remote.rest.exception.IncorrectRequestException;
import org.kie.services.remote.rest.jaxb.JaxbGenericResponse;
import org.kie.services.remote.rest.jaxb.JaxbProcessInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/runtime/{id: .+}")
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
    @Path("/process/{processDefId}/start")
    public void startNewProcess(@PathParam("processDefId") String processId) { 
        Map<String, List<String>> formParams = getRequestParams(request);
        Map<String, Object> params = extractMapFromParams(formParams, "process/" + processId + "/start");
        Command<?> cmd = new StartProcessCommand(processId, params);
        
        Object result = processRequestBean.doKieSessionOperation(cmd, deploymentId);
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
    @Path("/process/instance/{procInstId: [0-9]+}/{oper: [a-zA-Z]+}")
    public void doProcessInstanceOperation(@PathParam("procInstId") Long procInstId, @PathParam("oper") String operation) { 
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
            throw new IncorrectRequestException("Unsupported operation: /process/instance/" + procInstId + "/" + operation);
        }
        processRequestBean.doKieSessionOperation(cmd, deploymentId);
    }

    @POST
    @Path("/signal/{signal: [a-zA-Z0-9-]+}")
    public void signalEvent(@PathParam("signal") String signal) { 
        Map<String, List<String>> formParams = getRequestParams(request);
        String eventType = getStringParam("eventType", true, formParams, "signal/" + signal );
        Object event = getObjectParam("event", false, formParams, "signal/" + signal);
        Command<?> cmd = new SignalEventCommand(eventType, event);
        processRequestBean.doKieSessionOperation(cmd, deploymentId);
    }

    @POST
    @Path("/workitem/{workItemId: [0-9-]+}/{oper: [a-zA-Z]+}")
    public void doWorkItemOperation(@PathParam("workItemId") Long workItemId, @PathParam("oper") String operation) { 
        Map<String, List<String>> formParams = getRequestParams(request);
        Command<?> cmd = null;
        if ("complete".equalsIgnoreCase((operation.trim()))) {

            Map<String, Object> results = extractMapFromParams(formParams, operation);
            cmd = new CompleteWorkItemCommand(workItemId, results);
        } else if ("abort".equals(operation.toLowerCase())) {
            cmd = new AbortWorkItemCommand(workItemId);
        } else {
            throw new IncorrectRequestException("Unsupported operation: /process/instance/" + workItemId + "/" + operation);
        }
        processRequestBean.doKieSessionOperation(cmd, deploymentId);
    }

    @POST
    @Consumes(MediaType.APPLICATION_XML)
    @Path("/execute")
    public JaxbGenericResponse execute(JaxbCommandMessage cmdMsg) {
        List<Object> results = new ArrayList<Object>();
        for( Object cmd : cmdMsg.getCommands() ) {
            Object result = processRequestBean.doKieSessionOperation((Command) cmd, deploymentId);
            results.add(result);
        }
        if( null instanceof ProcessInstance ) { 
            return null;//JaxbProcessInstance((ProcessInstance) results.get(0));
        } else { 
            //???
        return null;
        }
    }

}
