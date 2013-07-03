package org.kie.services.remote.rest;

import static org.kie.services.remote.util.CommandsRequestUtil.processJaxbCommandsRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

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

import org.drools.compiler.lang.dsl.DSLMapParser.variable_definition_return;
import org.drools.core.command.runtime.process.AbortProcessInstanceCommand;
import org.drools.core.command.runtime.process.AbortWorkItemCommand;
import org.drools.core.command.runtime.process.CompleteWorkItemCommand;
import org.drools.core.command.runtime.process.GetProcessInstanceCommand;
import org.drools.core.command.runtime.process.SignalEventCommand;
import org.drools.core.command.runtime.process.StartProcessCommand;
import org.drools.core.command.runtime.process.StartProcessInstanceCommand;
import org.jboss.resteasy.spi.BadRequestException;
import org.jbpm.process.audit.NodeInstanceLog;
import org.jbpm.process.audit.ProcessInstanceLog;
import org.jbpm.process.audit.VariableInstanceLog;
import org.jbpm.process.audit.command.ClearHistoryLogsCommand;
import org.jbpm.process.audit.command.FindNodeInstancesCommand;
import org.jbpm.process.audit.command.FindProcessInstanceCommand;
import org.jbpm.process.audit.command.FindProcessInstancesCommand;
import org.jbpm.process.audit.command.FindSubProcessInstancesCommand;
import org.jbpm.process.audit.command.FindVariableInstancesCommand;
import org.kie.api.command.Command;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.services.client.serialization.jaxb.JaxbCommandsRequest;
import org.kie.services.client.serialization.jaxb.JaxbCommandsResponse;
import org.kie.services.client.serialization.jaxb.impl.JaxbHistoryLogList;
import org.kie.services.client.serialization.jaxb.impl.JaxbProcessInstanceResponse;
import org.kie.services.client.serialization.jaxb.rest.JaxbGenericResponse;
import org.kie.services.remote.cdi.ProcessRequestBean;

@Path("/runtime/{id: [a-zA-Z0-9-:\\.]+}")
@RequestScoped
public class RuntimeResource extends ResourceBase {

    @Inject
    private ProcessRequestBean processRequestBean;

    @Inject
    private Logger logger;
    
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
        if( result != null ) { 
            return new JaxbProcessInstanceResponse((ProcessInstance) result);
        } else { 
            throw new BadRequestException("Unable to retrieve process instance " + procInstId + " since it has been completed. Please see the history operations." );
        }
    }

    @POST
    @Produces(MediaType.APPLICATION_XML)
    @Path("/process/instance/{procInstId: [0-9]+}/{oper: [a-zA-Z]+}")
    public JaxbGenericResponse doProcessInstanceOperation(@PathParam("procInstId") Long procInstId, @PathParam("oper") String operation) { 
        Map<String, List<String>> params = getRequestParams(request);
        Command<?> cmd = null;
        if ("start".equalsIgnoreCase(operation.toLowerCase().trim())) {
            cmd = new StartProcessInstanceCommand(procInstId);
        } else if ("signal".equalsIgnoreCase(operation.toLowerCase().trim())) {
            String eventType = getStringParam("eventType", true, params, operation);
            Object event = getObjectParam("event", false, params, operation);
            cmd = new SignalEventCommand(procInstId, eventType, event);
        } else if ("abort".equalsIgnoreCase(operation.toLowerCase().trim())) {
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
        Map<String, List<String>> params = getRequestParams(request);
        Command<?> cmd = null;
        if ("complete".equalsIgnoreCase((operation.trim()))) {
            Map<String, Object> results = extractMapFromParams(params, operation);
            cmd = new CompleteWorkItemCommand(workItemId, results);
        } else if ("abort".equalsIgnoreCase(operation.toLowerCase())) {
            cmd = new AbortWorkItemCommand(workItemId);
        } else {
            throw new BadRequestException("Unsupported operation: /process/instance/" + workItemId + "/" + operation);
        }
        processRequestBean.doKieSessionOperation(cmd, deploymentId);
        return new JaxbGenericResponse(request);
    }

    @POST
    @Produces(MediaType.APPLICATION_XML)
    @Path("/history/clear")
    public JaxbGenericResponse clearProcessInstanceLogs() {
       Command<?> cmd = new ClearHistoryLogsCommand();
       processRequestBean.doKieSessionOperation(cmd, deploymentId);
       return new JaxbGenericResponse(request);
    }
    
    @GET
    @Produces(MediaType.APPLICATION_XML)
    @Path("/history/instance")
    public JaxbHistoryLogList getProcessInstanceLogs() {
       Command<?> cmd = new FindProcessInstancesCommand();
       List<ProcessInstanceLog> procInstLogList = (List<ProcessInstanceLog>) processRequestBean.doKieSessionOperation(cmd, deploymentId);
       return new JaxbHistoryLogList(procInstLogList);
    }
    
    @GET
    @Produces(MediaType.APPLICATION_XML)
    @Path("/history/instance/{procInstId: [0-9]+}")
    public JaxbHistoryLogList getSpecificProcessInstanceLogs(@PathParam("procInstId") long procInstId) {
       Command<?> cmd = new FindProcessInstanceCommand(procInstId);
       ProcessInstanceLog procInstLog= (ProcessInstanceLog) processRequestBean.doKieSessionOperation(cmd, deploymentId);
       List<ProcessInstanceLog> logList = new ArrayList<ProcessInstanceLog>();
       logList.add(procInstLog);
       return new JaxbHistoryLogList(logList);
    }
    
    @GET
    @Produces(MediaType.APPLICATION_XML)
    @Path("/history/instance/{procInstId: [0-9]+}/{oper: [a-zA-Z]+}")
    public JaxbHistoryLogList getVariableOrNodeHistoryList(@PathParam("procInstId") Long processInstanceId, @PathParam("oper") String operation ) {
        JaxbHistoryLogList resultList;
        Command<?> cmd;
       if( "child".equalsIgnoreCase(operation) ) { 
           cmd = new FindSubProcessInstancesCommand(processInstanceId);
           List<ProcessInstanceLog> procInstLogList = (List<ProcessInstanceLog>) processRequestBean.doKieSessionOperation(cmd, deploymentId);
           resultList = new JaxbHistoryLogList(procInstLogList);
       } else if( "node".equalsIgnoreCase(operation) ) {
           cmd = new FindNodeInstancesCommand(processInstanceId);
           List<NodeInstanceLog> nodeInstLogList = (List<NodeInstanceLog>) processRequestBean.doKieSessionOperation(cmd, deploymentId);
           resultList = new JaxbHistoryLogList(nodeInstLogList);
       } else if( "variable".equalsIgnoreCase(operation) ) {
           cmd = new FindVariableInstancesCommand(processInstanceId);
           List<VariableInstanceLog> varInstLogList = (List<VariableInstanceLog>) processRequestBean.doKieSessionOperation(cmd, deploymentId);
           resultList = new JaxbHistoryLogList(varInstLogList);
       } else { 
           throw new BadRequestException("Unsupported operation: /history/instance/" + processInstanceId + "/" + operation);
       }
       return resultList;
    }
    
    @GET
    @Produces(MediaType.APPLICATION_XML)
    @Path("/history/instance/{procInstId: [0-9]+}/{oper: [a-zA-Z]+}/{id: [a-zA-Z0-9-:\\.]+}")
    public JaxbHistoryLogList getSpecificVariableOrNodeHistoryList(@PathParam("procInstId") Long processInstanceId, @PathParam("oper") String operation, @PathParam("id") String id ) {
        JaxbHistoryLogList resultList;
        Command<?> cmd;
       if( "node".equalsIgnoreCase(operation) ) {
           cmd = new FindNodeInstancesCommand(processInstanceId, id);
           List<NodeInstanceLog> nodeInstLogList = (List<NodeInstanceLog>) processRequestBean.doKieSessionOperation(cmd, deploymentId);
           resultList = new JaxbHistoryLogList(nodeInstLogList);
       } else if( "variable".equalsIgnoreCase(operation) ) {
           cmd = new FindVariableInstancesCommand(processInstanceId, id);
           List<VariableInstanceLog> varInstLogList = (List<VariableInstanceLog>) processRequestBean.doKieSessionOperation(cmd, deploymentId);
           resultList = new JaxbHistoryLogList(varInstLogList);
       } else { 
           throw new BadRequestException("Unsupported operation: /history/instance/" + processInstanceId + "/" + operation + "/" + id);
       }
       return resultList;
    }
    
    @GET
    @Produces(MediaType.APPLICATION_XML)
    @Path("/history/process/{procId: [a-zA-Z0-9-:\\.]+}")
    public JaxbHistoryLogList getProcessInstanceLogs(@PathParam("procInstId") String processId) { 
        JaxbHistoryLogList resultList;
        Command<?> cmd = new FindProcessInstancesCommand(processId);
        List<ProcessInstanceLog> nodeInstLogList = (List<ProcessInstanceLog>) processRequestBean.doKieSessionOperation(cmd, deploymentId);
        return new JaxbHistoryLogList(nodeInstLogList);
    }
    
}
