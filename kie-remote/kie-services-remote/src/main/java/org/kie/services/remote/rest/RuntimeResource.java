package org.kie.services.remote.rest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;

import org.drools.core.command.runtime.process.AbortProcessInstanceCommand;
import org.drools.core.command.runtime.process.AbortWorkItemCommand;
import org.drools.core.command.runtime.process.CompleteWorkItemCommand;
import org.drools.core.command.runtime.process.GetProcessInstanceCommand;
import org.drools.core.command.runtime.process.GetWorkItemCommand;
import org.drools.core.command.runtime.process.SignalEventCommand;
import org.drools.core.command.runtime.process.StartProcessCommand;
import org.drools.core.process.instance.WorkItem;
import org.jboss.resteasy.spi.BadRequestException;
import org.jbpm.process.audit.ProcessInstanceLog;
import org.jbpm.process.audit.VariableInstanceLog;
import org.jbpm.process.audit.command.ClearHistoryLogsCommand;
import org.jbpm.process.audit.command.FindNodeInstancesCommand;
import org.jbpm.process.audit.command.FindProcessInstanceCommand;
import org.jbpm.process.audit.command.FindProcessInstancesCommand;
import org.jbpm.process.audit.command.FindSubProcessInstancesCommand;
import org.jbpm.process.audit.command.FindVariableInstancesByNameCommand;
import org.jbpm.process.audit.command.FindVariableInstancesCommand;
import org.jbpm.process.audit.event.AuditEvent;
import org.kie.api.command.Command;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.services.client.serialization.jaxb.impl.JaxbCommandsRequest;
import org.kie.services.client.serialization.jaxb.impl.JaxbCommandsResponse;
import org.kie.services.client.serialization.jaxb.impl.JaxbVariablesResponse;
import org.kie.services.client.serialization.jaxb.impl.audit.JaxbHistoryLogList;
import org.kie.services.client.serialization.jaxb.impl.process.JaxbProcessInstanceListResponse;
import org.kie.services.client.serialization.jaxb.impl.process.JaxbProcessInstanceResponse;
import org.kie.services.client.serialization.jaxb.impl.process.JaxbProcessInstanceWithVariablesResponse;
import org.kie.services.client.serialization.jaxb.impl.process.JaxbWorkItem;
import org.kie.services.client.serialization.jaxb.rest.JaxbGenericResponse;
import org.kie.services.remote.exception.KieRemoteServicesPreConditionException;
import org.kie.services.remote.util.Paginator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/runtime/{id: [a-zA-Z0-9-:\\.]+}")
@RequestScoped
@SuppressWarnings("unchecked")
public class RuntimeResource extends ResourceBase {

    private static final Logger logger = LoggerFactory.getLogger(RuntimeResource.class);

    /* REST information */
    @Context
    private HttpHeaders headers;
    
    @Context
    private HttpServletRequest request;
    
    @Context
    private Request restRequest;

    /* KIE information and processing */
    @Inject
    private RestProcessRequestBean processRequestBean;

    @PathParam("id")
    private String deploymentId;
    
    // Rest methods --------------------------------------------------------------------------------------------------------------

    @POST
    @Consumes(MediaType.APPLICATION_XML)
    @Produces(MediaType.APPLICATION_XML)
    @Path("/execute")
    public JaxbCommandsResponse execute(JaxbCommandsRequest cmdsRequest) {
        return restProcessJaxbCommandsRequest(cmdsRequest, processRequestBean);
    } 

    @POST
    @Path("/process/{processDefId: [_a-zA-Z0-9-:\\.]+}/start")
    public Response startNewProcess(@PathParam("processDefId") String processId) {
        Map<String, List<String>> requestParams = getRequestParams(request);
        String oper = getRelativePath(request);
        
        Map<String, Object> params = extractMapFromParams(requestParams, oper);
        Command<?> cmd = new StartProcessCommand(processId, params);

        Object result = processRequestBean.doKieSessionOperation(
                cmd, 
                deploymentId, 
                null,
                "Unable to start process with process definition id '" + processId + "'");

        JaxbProcessInstanceResponse responseObj = new JaxbProcessInstanceResponse((ProcessInstance) result, request);
        return createCorrectVariant(responseObj, headers);
    }

    @GET
    @Path("/process/instance/{procInstId: [0-9]+}")
    public Response getProcessInstanceDetails(@PathParam("procInstId") Long procInstId) {
        Command<?> cmd = new GetProcessInstanceCommand(procInstId);
        ((GetProcessInstanceCommand) cmd).setReadOnly(true);
        
        Object result = processRequestBean.doKieSessionOperation(
                cmd, 
                deploymentId, 
                procInstId, 
                "Unable to get process instance " + procInstId);
        
        Object responseObj = null;
        if (result != null) {
            responseObj = new JaxbProcessInstanceResponse((ProcessInstance) result);
            return createCorrectVariant(responseObj, headers);
        } else {
            throw new BadRequestException("Unable to retrieve process instance " + procInstId
                    + " which may have been completed. Please see the history operations.");
        }
    }

    @POST
    @Path("/process/instance/{procInstId: [0-9]+}/abort")
    public Response abortProcessInstance(@PathParam("procInstId") Long procInstId) {
        Command<?> cmd = new AbortProcessInstanceCommand();
        ((AbortProcessInstanceCommand) cmd).setProcessInstanceId(procInstId);
        
        processRequestBean.doKieSessionOperation(
                cmd, 
                deploymentId, 
                procInstId,
                "Unable to abort process instance " + procInstId);
                
        return createCorrectVariant(new JaxbGenericResponse(request), headers);
    }

    @POST
    @Path("/process/instance/{procInstId: [0-9]+}/signal")
    public Response signalProcessInstance(@PathParam("procInstId") Long procInstId) {
        String oper = getRelativePath(request);
        Map<String, List<String>> params = getRequestParams(request);
        String eventType = getStringParam("signal", true, params, oper);
        Object event = getObjectParam("event", false, params, oper);
        Command<?> cmd = new SignalEventCommand(procInstId, eventType, event);
        
        String errorMsg = "Unable to signal process instance";
        if( eventType == null ) { 
            errorMsg += " with empty signal";
        } else { 
            errorMsg += " with signal type '" + eventType + "'";
        }
        if( event != null ) { 
            errorMsg += " and event '" + event + "'";
        }

        processRequestBean.doKieSessionOperation(cmd, deploymentId, procInstId, errorMsg);
        return createCorrectVariant(new JaxbGenericResponse(request), headers);

    }

    @GET
    @Path("/process/instance/{procInstId: [0-9]+}/variables")
    public Response getProcessInstanceVariables(@PathParam("procInstId") Long procInstId) {
        String oper = getRelativePath(request);
        Map<String, String> vars = getVariables(procInstId);
        return createCorrectVariant(new JaxbVariablesResponse(vars, request), headers);
    }
    
    @POST
    @Path("/signal")
    public Response signalEvent() {
        String oper = getRelativePath(request);
        Map<String, List<String>> requestParams = getRequestParams(request);
        String eventType = getStringParam("signal", true, requestParams, oper);
        Object event = getObjectParam("event", false, requestParams, oper);
        String errorMsg = "Unable to send signal '" + eventType + "'";
        if( event != null ) { 
            errorMsg += " with event '" + event + "'";
        }

        processRequestBean.doKieSessionOperation(
                new SignalEventCommand(eventType, event),
                deploymentId, 
                (Long) getNumberParam(PROC_INST_ID_PARAM_NAME, false, requestParams, oper, true),
                errorMsg);
        return createCorrectVariant(new JaxbGenericResponse(request), headers);
    }

    @GET
    @Path("/workitem/{workItemId: [0-9-]+}")
    public Response getWorkItem(@PathParam("workItemId") Long workItemId) { 
        String oper = getRelativePath(request);
        WorkItem workItem = (WorkItem) processRequestBean.doKieSessionOperation(
                new GetWorkItemCommand(workItemId),
                deploymentId, 
                (Long) getNumberParam(PROC_INST_ID_PARAM_NAME, false, getRequestParams(request), oper, true),
                "Unable to get work item " +  workItemId);
        return createCorrectVariant(new JaxbWorkItem(workItem), headers);
    }
    
    @POST
    @Path("/workitem/{workItemId: [0-9-]+}/{oper: [a-zA-Z]+}")
    public Response doWorkItemOperation(@PathParam("workItemId") Long workItemId, @PathParam("oper") String operation) {
        String oper = getRelativePath(request);
        Map<String, List<String>> params = getRequestParams(request);
        Command<?> cmd = null;
        if ("complete".equalsIgnoreCase((operation.trim()))) {
            Map<String, Object> results = extractMapFromParams(params, operation);
            cmd = new CompleteWorkItemCommand(workItemId, results);
        } else if ("abort".equalsIgnoreCase(operation.toLowerCase())) {
            cmd = new AbortWorkItemCommand(workItemId);
        } else {
            throw new BadRequestException("Unsupported operation: " + oper);
        }
        
        processRequestBean.doKieSessionOperation(
                cmd, 
                deploymentId, 
                (Long) getNumberParam(PROC_INST_ID_PARAM_NAME, false, params, oper, true),
                "Unable to " + operation + " work item " +  workItemId);
        return createCorrectVariant(new JaxbGenericResponse(request), headers);
    }

    /**
     * History methods
     */
    
    @POST
    @Path("/history/clear")
    public Response clearProcessInstanceLogs() {
        String oper = getRelativePath(request);
        processRequestBean.doKieSessionOperation(
                new ClearHistoryLogsCommand(),
                deploymentId, 
                (Long) getNumberParam(PROC_INST_ID_PARAM_NAME, false, getRequestParams(request), oper, true),
                "Unable to clear process instance logs");
        return createCorrectVariant(new JaxbGenericResponse(request), headers);
    }

    @GET
    @Path("/history/instance")
    public Response getProcessInstanceLogs() {
        String oper = getRelativePath(request);
        Map<String, List<String>> params = getRequestParams(request);
        int [] pageInfo = getPageNumAndPageSize(params);
        
        Object result = processRequestBean.doKieSessionOperation(
                new FindProcessInstancesCommand(),
                deploymentId, 
                (Long) getNumberParam(PROC_INST_ID_PARAM_NAME, false, params, oper, true),
                "Unable to get process instance logs");
        List<ProcessInstanceLog> results = (List<ProcessInstanceLog>) result;
        
        results = (new Paginator<ProcessInstanceLog>()).paginate(pageInfo, results);
        return createCorrectVariant(new JaxbHistoryLogList(results), headers);
    }

    @GET
    @Path("/history/instance/{procInstId: [0-9]+}")
    public Response getSpecificProcessInstanceLogs(@PathParam("procInstId") long procInstId) {
        Map<String, List<String>> params = getRequestParams(request);
        int [] pageInfo = getPageNumAndPageSize(params);
        
        Object result = processRequestBean.doKieSessionOperation(
                new FindProcessInstanceCommand(procInstId),
                deploymentId, 
                procInstId, 
                "Unable to get process instance logs for process instance " + procInstId);
        ProcessInstanceLog procInstLog = (ProcessInstanceLog) result;
        
        List<ProcessInstanceLog> logList = new ArrayList<ProcessInstanceLog>();
        logList.add(procInstLog);
        
        logList = (new Paginator<ProcessInstanceLog>()).paginate(pageInfo, logList);
        return createCorrectVariant(new JaxbHistoryLogList(logList), headers);
    }

    @GET
    @Path("/history/instance/{procInstId: [0-9]+}/{oper: [a-zA-Z]+}")
    public Response getVariableOrNodeHistoryList(@PathParam("procInstId") Long procInstId, @PathParam("oper") String operation) {
        Map<String, List<String>> params = getRequestParams(request);
        int [] pageInfo = getPageNumAndPageSize(params);
        
        Command<?> cmd;
        String errorMsg;
        if ("child".equalsIgnoreCase(operation)) {
            cmd = new FindSubProcessInstancesCommand(procInstId);
            errorMsg = "Unable to get child process instance logs for process instance " + procInstId;
        } else if ("node".equalsIgnoreCase(operation)) {
            cmd = new FindNodeInstancesCommand(procInstId);
            errorMsg = "Unable to get node instance logs for process instance " + procInstId;
        } else if ("variable".equalsIgnoreCase(operation)) {
            cmd = new FindVariableInstancesCommand(procInstId);
            errorMsg = "Unable to get variable instance logs for process instance " + procInstId;
        } else {
            String oper = getRelativePath(request);
            throw new BadRequestException("Unsupported operation: " + oper );
        }

        Object result = processRequestBean.doKieSessionOperation(cmd, deploymentId, procInstId, errorMsg);
        List<AuditEvent> varInstLogList = (List<AuditEvent>) result;
        varInstLogList = (new Paginator<AuditEvent>()).paginate(pageInfo, varInstLogList);
        JaxbHistoryLogList resultList = new JaxbHistoryLogList(varInstLogList);
        return createCorrectVariant(resultList, headers);
    }

    @GET
    @Path("/history/instance/{procInstId: [0-9]+}/{oper: [a-zA-Z]+}/{logId: [a-zA-Z0-9-:\\.]+}")
    public Response getSpecificVariableOrNodeHistoryList(@PathParam("procInstId") Long procInstId,
            @PathParam("oper") String operation, @PathParam("logId") String logId) {
        Map<String, List<String>> params = getRequestParams(request);
        int [] pageInfo = getPageNumAndPageSize(params);
        
        Command<?> cmd;
        String errorMsg;
        if ("node".equalsIgnoreCase(operation)) {
            cmd = new FindNodeInstancesCommand(procInstId, logId);
            errorMsg ="Unable to get node instance logs for node '" + logId + "' in process instance " + procInstId;
        } else if ("variable".equalsIgnoreCase(operation)) {
            cmd = new FindVariableInstancesCommand(procInstId, logId);
            errorMsg = "Unable to get variable instance logs for variable '" + logId + "' in process instance " + procInstId;
        } else {
            String oper = getRelativePath(request);
            throw new BadRequestException("Unsupported operation: " + oper );
        }
        
        Object result = processRequestBean.doKieSessionOperation(cmd, deploymentId, procInstId, errorMsg);
        List<AuditEvent> varInstLogList = (List<AuditEvent>) result;
        varInstLogList = (new Paginator<AuditEvent>()).paginate(pageInfo, varInstLogList);
        JaxbHistoryLogList resultList = new JaxbHistoryLogList(varInstLogList);
        return createCorrectVariant(resultList, headers);
    }

    @GET
    @Path("/history/process/{procId: [a-zA-Z0-9-:\\.]+}")
    public Response getProcessInstanceLogs(@PathParam("procId") String processId) {
        Map<String, List<String>> params = getRequestParams(request);
        int [] pageInfo = getPageNumAndPageSize(params);
        
        Object result = processRequestBean.doKieSessionOperation(
                new FindProcessInstancesCommand(processId),
                deploymentId, 
                (Long) getNumberParam(PROC_INST_ID_PARAM_NAME, false, params, getRelativePath(request), true),
                "Unable to get process instance logs for process '" + processId + "'");
        List<ProcessInstanceLog> procInstLogList = (List<ProcessInstanceLog>) result;
        
        procInstLogList = (new Paginator<ProcessInstanceLog>()).paginate(pageInfo, procInstLogList);
        return createCorrectVariant(new JaxbHistoryLogList(procInstLogList), headers);
    }

    @GET
    @Path("/history/variable/{varId: [a-zA-Z0-9-:\\.]+}")
    public Response getVariableInstanceByVar(@PathParam("varId") String variableId) {
        Map<String, List<String>> params = getRequestParams(request);
        int [] pageInfo = getPageNumAndPageSize(params);
        List<VariableInstanceLog> varLogList 
            = internalGetVariableInstancesByVarAndValue(variableId, null, params, getRelativePath(request));
        varLogList = (new Paginator<VariableInstanceLog>()).paginate(pageInfo, varLogList);
        
        return createCorrectVariant(new JaxbHistoryLogList(varLogList), headers);
    }
    
    @GET
    @Path("/history/variable/{varId: [a-zA-Z0-9-:\\.]+}/value/{value: [a-zA-Z0-9-:\\.]+}")
    public Response getVariableInstanceByVarAndValue(@PathParam("varId") String variableId, @PathParam("value") String value) {
        Map<String, List<String>> params = getRequestParams(request);
        int [] pageInfo = getPageNumAndPageSize(params);
        List<VariableInstanceLog> varLogList 
            = internalGetVariableInstancesByVarAndValue(variableId, value, params, getRelativePath(request));
        varLogList = (new Paginator<VariableInstanceLog>()).paginate(pageInfo, varLogList);
        
        return createCorrectVariant(new JaxbHistoryLogList(varLogList), headers);
    } 
    
    private List<VariableInstanceLog> internalGetVariableInstancesByVarAndValue(String varId, String value, 
            Map<String, List<String>> params, String operation) { 
        // active processes parameter
        String activeProcsParam = getStringParam("activeProcesses", false, params, operation);
        boolean activeProcesses = true;
        if( activeProcsParam != null ) { 
            activeProcesses = Boolean.parseBoolean(activeProcsParam);
        }
        
        Command<?> findVarCmd; 
        String errMsg;
        if( value == null ) { 
            findVarCmd = new FindVariableInstancesByNameCommand(varId, activeProcesses);
            errMsg = "Unable to get variable instance logs for variable id '" + varId + "'";
        } else { 
            findVarCmd = new FindVariableInstancesByNameCommand(varId, value, activeProcesses);
            errMsg = "Unable to get variable instance logs for variable id '" + varId + "' and value '" + value + "'";
        }
        Object result = processRequestBean.doKieSessionOperation(
                findVarCmd,
                deploymentId, 
                null,
                errMsg);
        
        return (List<VariableInstanceLog>) result;
    }
    
    @GET
    @Path("/history/variable/{varId: [a-zA-Z0-9-:\\.]+}/instances")
    public Response getProcessInstanceByVar(@PathParam("varId") String variableId) {
        Map<String, List<String>> params = getRequestParams(request);
        int [] pageInfo = getPageNumAndPageSize(params);
        String oper = getRelativePath(request);

        // get variables
        List<VariableInstanceLog> varLogList = internalGetVariableInstancesByVarAndValue(variableId, null, params, oper);
        
        // get process instances
        JaxbProcessInstanceListResponse response = getProcessInstanceListResponse(varLogList, pageInfo);
        return createCorrectVariant(response, headers);
    }
    
    @GET
    @Path("/history/variable/{varId: [a-zA-Z0-9-:\\.]+}/value/{value: [a-zA-Z0-9-:\\.]+}/instances")
    public Response getProcessInstanceByVarAndValue(@PathParam("procId") String variableId, @PathParam("value") String value) {
        Map<String, List<String>> params = getRequestParams(request);
        int [] pageInfo = getPageNumAndPageSize(params);
        String oper = getRelativePath(request);

        // get variables
        List<VariableInstanceLog> varLogList = internalGetVariableInstancesByVarAndValue(variableId, value, params, oper);
        
        // get process instances
        JaxbProcessInstanceListResponse response = getProcessInstanceListResponse(varLogList, pageInfo);
        return createCorrectVariant(response, headers);
    }

    private JaxbProcessInstanceListResponse getProcessInstanceListResponse(List<VariableInstanceLog> varLogList, int [] pageInfo) { 
        JaxbProcessInstanceListResponse response = new JaxbProcessInstanceListResponse();
        response.setCommandName(FindProcessInstanceCommand.class.getSimpleName());
        
        int numVarLogs = varLogList.size();
        int numResults = pageInfo[PAGE_NUM]*pageInfo[PAGE_SIZE];
        int numProcInsts = 0;
        
        for( int i = 0; i < numVarLogs && numProcInsts < numResults; ++i ) { 
            long procInstId = varLogList.get(i).getProcessInstanceId();
            Command<?> findProcInstCmd = new GetProcessInstanceCommand(procInstId);
            Object procInstResult = processRequestBean.doKieSessionOperation(
                    findProcInstCmd,
                    deploymentId, 
                    null,
                    "Unable to get process instance with id id '" + procInstId + "'");
            if( procInstResult != null ) { 
                response.getResult().add(new JaxbProcessInstanceResponse((ProcessInstance) procInstResult));
                ++numProcInsts;
            }
        }
        return response;
    }
    
    /**
     * WithVars methods
     */
    
    @POST
    @Path("/withvars/process/{processDefId: [_a-zA-Z0-9-:\\.]+}/start")
    public Response startNewProcessWithVars(@PathParam("processDefId") String processId) {
        Map<String, List<String>> requestParams = getRequestParams(request);
        String oper = getRelativePath(request);
        Map<String, Object> params = extractMapFromParams(requestParams, oper );

        Object result = processRequestBean.doKieSessionOperation(
                new StartProcessCommand(processId, params),
                deploymentId, 
                (Long) getNumberParam(PROC_INST_ID_PARAM_NAME, false, requestParams, oper, true),
                "Unable to get process instance logs for process '" + processId + "'");
        
        ProcessInstance procInst = (ProcessInstance) result;
        
        Map<String, String> vars = getVariables(procInst.getId());
        JaxbProcessInstanceWithVariablesResponse resp = new JaxbProcessInstanceWithVariablesResponse(procInst, vars, request);
        
        return createCorrectVariant(resp, headers);
    }

    @GET
    @Path("/withvars/process/instance/{procInstId: [0-9]+}")
    public Response getProcessInstanceWithVars(@PathParam("procInstId") Long procInstId) {
        Command<?> cmd = new GetProcessInstanceCommand(procInstId);
        ((GetProcessInstanceCommand) cmd).setReadOnly(true);
        
        Object result = processRequestBean.doKieSessionOperation(
                cmd, 
                deploymentId, 
                procInstId,
                "Unable to get process instance " + procInstId);
        
        JaxbProcessInstanceWithVariablesResponse responseObj = null;
        if (result != null) {
            ProcessInstance procInst = (ProcessInstance) result;
            Map<String, String> vars = getVariables(procInstId);
            responseObj = new JaxbProcessInstanceWithVariablesResponse(procInst, vars, request);
        } else {
            throw new BadRequestException("Unable to retrieve process instance " + procInstId
                    + " since it has been completed. Please see the history operations.");
        }
        return createCorrectVariant(responseObj, headers);
    }

    @POST
    @Path("/withvars/process/instance/{procInstId: [0-9]+}/signal")
    public Response signalProcessInstanceWithVars(@PathParam("procInstId") Long procInstId) {
        Map<String, List<String>> params = getRequestParams(request);
        String eventType = getStringParam("eventType", true, params, "signal");
        Object event = getObjectParam("event", false, params, "signal");
        String errorMsg = "Unable to signal process instance " + procInstId;
        if( eventType == null ) { 
            errorMsg += " with empty signal";
        } else { 
            errorMsg += " with signal type '" + eventType + "'";
        }
        if( event != null ) { 
            errorMsg += " and event '" + event + "'";
        }
        
        processRequestBean.doKieSessionOperation(
                new SignalEventCommand(procInstId, eventType, event),
                deploymentId, 
                procInstId,
                errorMsg);
        
        Command<?> cmd = new GetProcessInstanceCommand(procInstId);
        ((GetProcessInstanceCommand) cmd).setReadOnly(true);
        Object result = processRequestBean.doKieSessionOperation(
                cmd,
                deploymentId, 
                procInstId,
                "Unable to get process instance " + procInstId);
        ProcessInstance processInstance = (ProcessInstance) result;
        if( processInstance == null ) { 
            throw new KieRemoteServicesPreConditionException("This method can only be used on processes that will not complete after a signal.");
        }
        Map<String, String> vars = getVariables(processInstance.getId());
        
        return createCorrectVariant(new JaxbProcessInstanceWithVariablesResponse(processInstance, vars), headers);
    }

    // Helper methods --------------------------------------------------------------------------------------------------------------

    private Map<String, String> getVariables(long processInstanceId) {
        Object result = processRequestBean.doKieSessionOperation(
                new FindVariableInstancesCommand(processInstanceId),
                deploymentId, 
                processInstanceId,
                "Unable to retrieve process variables from process instance " + processInstanceId);
        List<VariableInstanceLog> varInstLogList = (List<VariableInstanceLog>) result;
        
        Map<String, String> vars = new HashMap<String, String>();
        if( varInstLogList.isEmpty() ) { 
            return vars;
        }
        
        Map<String, VariableInstanceLog> varLogMap = new HashMap<String, VariableInstanceLog>();
        for( VariableInstanceLog varLog: varInstLogList ) {
            String varId = varLog.getVariableId();
            VariableInstanceLog prevVarLog = varLogMap.put(varId, varLog);
            if( prevVarLog != null ) { 
                if( prevVarLog.getDate().after(varLog.getDate()) ) { 
                  varLogMap.put(varId, prevVarLog);
                } 
            }
        }
        
        for( Entry<String, VariableInstanceLog> varEntry : varLogMap.entrySet() ) { 
            vars.put(varEntry.getKey(), varEntry.getValue().getValue());
        }
            
        return vars;
    }

}
