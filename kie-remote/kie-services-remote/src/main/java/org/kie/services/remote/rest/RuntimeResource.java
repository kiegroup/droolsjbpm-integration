package org.kie.services.remote.rest;

import static org.kie.services.remote.util.CommandsRequestUtil.processJaxbCommandsRequest;

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
import org.kie.api.command.Command;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.services.client.serialization.jaxb.JaxbCommandsRequest;
import org.kie.services.client.serialization.jaxb.JaxbCommandsResponse;
import org.kie.services.client.serialization.jaxb.impl.JaxbProcessInstanceResponse;
import org.kie.services.client.serialization.jaxb.rest.JaxbGenericResponse;
import org.kie.services.remote.cdi.ProcessRequestBean;

@Path("/runtime/{id: [a-zA-Z0-9-:\\.]+}")
@RequestScoped
public class RuntimeResource extends ResourceBase {

    @Inject
    private ProcessRequestBean processRequestBean;

    @PathParam("id")
    private String deploymentId;
    
    @Context
    private HttpServletRequest request;

    // Rest methods --------------------------------------------------------------------------------------------------------------

    @POST
    @Consumes(MediaType.APPLICATION_XML)
    @Produces(MediaType.APPLICATION_XML)
    @Path("/execute")
    public JaxbCommandsResponse execute(JaxbCommandsRequest cmdsRequest) {
        return processJaxbCommandsRequest(cmdsRequest, processRequestBean);
    }

    @POST
    @Produces(MediaType.APPLICATION_XML)
    @Path("/process/{processDefId: [a-zA-Z0-9-:\\.]+}/start")
    public JaxbProcessInstanceResponse startNewProcess(@PathParam("processDefId") String processId) { 
        Map<String, List<String>> formParams = getRequestParams(request);
        Map<String, Object> params = extractMapFromParams(formParams, "process/" + processId + "/start");
        Command<?> cmd = new StartProcessCommand(processId, params);
        
        ProcessInstance procInst = (ProcessInstance) processRequestBean.doKieSessionOperation(cmd, deploymentId);
        JaxbProcessInstanceResponse resp = new JaxbProcessInstanceResponse(procInst, request);
        return resp;
    }

    @GET
    @Produces(MediaType.APPLICATION_XML)
    @Path("/process/instance/{procInstId: [0-9]+}")
    public JaxbProcessInstanceResponse getProcessInstanceDetails(@PathParam("procInstId") Long procInstId) {
        Command<?> cmd = new GetProcessInstanceCommand(procInstId);
        Object result = processRequestBean.doKieSessionOperation(cmd, deploymentId);
        return new JaxbProcessInstanceResponse((ProcessInstance) result);
    }

    @POST
    @Produces(MediaType.APPLICATION_XML)
    @Path("/process/instance/{procInstId: [0-9]+}/{oper: [a-zA-Z]+}")
    public JaxbGenericResponse doProcessInstanceOperation(@PathParam("procInstId") Long procInstId, @PathParam("oper") String operation) { 
        Map<String, List<String>> params = getRequestParams(request);
        Command<?> cmd = null;
        if ("start".equals(operation.toLowerCase().trim())) {
            cmd = new StartProcessInstanceCommand(procInstId);
        } else if ("signal".equals(operation.toLowerCase().trim())) {
            String eventType = getStringParam("eventType", true, params, operation);
            Object event = getObjectParam("event", false, params, operation);
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
