package org.kie.services.remote.rest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;

import org.jboss.resteasy.spi.BadRequestException;
import org.jbpm.process.audit.AuditLogService;
import org.jbpm.process.audit.JPAAuditLogService;
import org.jbpm.process.audit.ProcessInstanceLog;
import org.jbpm.process.audit.VariableInstanceLog;
import org.jbpm.process.audit.event.AuditEvent;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.services.client.serialization.jaxb.impl.audit.JaxbHistoryLogList;
import org.kie.services.client.serialization.jaxb.rest.JaxbGenericResponse;

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
 * 
 * If the method is annotated by the @Path anno, but is the "root", then
 * give it a name that explains it's funtion.
 */
@Path("/history")
@RequestScoped
@SuppressWarnings("unchecked")
public class HistoryResource extends ResourceBase {

    /* REST information */
    @Context
    private HttpHeaders headers;
    
    @Context
    private HttpServletRequest request;
    
    @Context
    private Request restRequest;

    @PersistenceUnit(unitName = "org.jbpm.domain")
    private EntityManagerFactory emf;
   
    private AuditLogService auditLogService;
    
    // Rest methods --------------------------------------------------------------------------------------------------------------

    @PostConstruct
    public void setAuditLogService() { 
        auditLogService = new JPAAuditLogService(emf);
    }
    
    public AuditLogService getAuditLogService() { 
        return auditLogService;
    }
    
    /**
     * History methods
     */
    
    @POST
    @Path("/clear")
    public Response clear() {
        auditLogService.clear();
        return createCorrectVariant(new JaxbGenericResponse(request), headers);
    }

    @GET
    @Path("/instances")
    public Response instances() {
        String oper = getRelativePath(request);
        Map<String, List<String>> params = getRequestParams(request);
        
        List<ProcessInstanceLog> results = auditLogService.findProcessInstances();
        
        results = paginate(getPageNumAndPageSize(params, oper), results);
        return createCorrectVariant(new JaxbHistoryLogList(results), headers);
    }

    @GET
    @Path("/instance/{procInstId: [0-9]+}")
    public Response instance_procInstId(@PathParam("procInstId") long procInstId) {
        Map<String, List<String>> params = getRequestParams(request);
        String oper = getRelativePath(request);
        
        ProcessInstanceLog procInstLog = auditLogService.findProcessInstance(procInstId);
        
        List<ProcessInstanceLog> logList = new ArrayList<ProcessInstanceLog>();
        logList.add(procInstLog);
        
        logList = paginate(getPageNumAndPageSize(params, oper), logList);
        return createCorrectVariant(new JaxbHistoryLogList(logList), headers);
    }

    @GET
    @Path("/instance/{procInstId: [0-9]+}/{oper: [a-zA-Z]+}")
    public Response instance_procInstid_oper(@PathParam("procInstId") Long procInstId, @PathParam("oper") String operation) {
        Map<String, List<String>> params = getRequestParams(request);
        String oper = getRelativePath(request);
        
        Object result = null;
        if ("child".equalsIgnoreCase(operation)) {
            auditLogService.findSubProcessInstances(procInstId);
        } else if ("node".equalsIgnoreCase(operation)) {
            auditLogService.findNodeInstances(procInstId);
        } else if ("variable".equalsIgnoreCase(operation)) {
            auditLogService.findVariableInstances(procInstId);
        } else {
            throw new BadRequestException("Unsupported operation: " + oper );
        }

        List<AuditEvent> varInstLogList = (List<AuditEvent>) result;
        varInstLogList = paginate(getPageNumAndPageSize(params, oper), varInstLogList);
        JaxbHistoryLogList resultList = new JaxbHistoryLogList(varInstLogList);
        
        return createCorrectVariant(resultList, headers);
    }

    @GET
    @Path("/instance/{procInstId: [0-9]+}/{oper: [a-zA-Z]+}/{logId: [a-zA-Z0-9-:\\.]+}")
    public Response instance_procInstId_oper_logId(@PathParam("procInstId") Long procInstId,
            @PathParam("oper") String operation, @PathParam("logId") String logId) {
        Map<String, List<String>> params = getRequestParams(request);
        String oper = getRelativePath(request);
        
        Object result = null;
        if ("node".equalsIgnoreCase(operation)) {
            result = auditLogService.findNodeInstances(procInstId, logId);
        } else if ("variable".equalsIgnoreCase(operation)) {
            result = auditLogService.findVariableInstances(procInstId, logId);
        } else {
            throw new BadRequestException("Unsupported operation: " + oper );
        }
        
        List<AuditEvent> varInstLogList = (List<AuditEvent>) result;
        varInstLogList = paginate(getPageNumAndPageSize(params, oper), varInstLogList);
        JaxbHistoryLogList resultList = new JaxbHistoryLogList(varInstLogList);
        
        return createCorrectVariant(resultList, headers);
    }

    @GET
    @Path("/process/{processDefId: [a-zA-Z0-9-:\\.]+}")
    public Response process_procDefId(@PathParam("processDefId") String processId) {
        Map<String, List<String>> params = getRequestParams(request);
        Number statusParam = getNumberParam("status", false, params, getRelativePath(request), false);
        String oper = getRelativePath(request);
        int[] pageInfo = getPageNumAndPageSize(params, oper);

        Object result;
        if (statusParam != null) {
            if (statusParam.intValue() == ProcessInstance.STATE_ACTIVE) {
                result = auditLogService.findActiveProcessInstances(processId);
            } else {
                result = auditLogService.findProcessInstances(processId);
            }
        } else {
            result = auditLogService.findProcessInstances(processId);
        }
        
        List<ProcessInstanceLog> procInstLogList = (List<ProcessInstanceLog>) result;
        
        if (statusParam != null && !statusParam.equals(ProcessInstance.STATE_ACTIVE)) {
            List<ProcessInstanceLog> filteredProcLogList = new ArrayList<ProcessInstanceLog>();
            for (int i = 0; 
                    i < procInstLogList.size() && filteredProcLogList.size() < getMaxNumResultsNeeded(pageInfo);
                    ++i) {
                ProcessInstanceLog procLog = procInstLogList.get(i);
                if (procLog.getStatus().equals(statusParam.intValue())) {
                    filteredProcLogList.add(procLog);
                }
            }
        }
        procInstLogList = paginate(pageInfo, procInstLogList);
        return createCorrectVariant(new JaxbHistoryLogList(procInstLogList), headers);
    }

    @GET
    @Path("/variable/{varId: [a-zA-Z0-9-:\\.]+}")
    public Response variable_varId(@PathParam("varId") String variableId) {
        Map<String, List<String>> params = getRequestParams(request);
        String oper = getRelativePath(request);
        List<VariableInstanceLog> varLogList = internalGetVariableInstancesByVarAndValue(variableId, null, params, oper);
        varLogList = paginate(getPageNumAndPageSize(params, oper), varLogList);
        
        return createCorrectVariant(new JaxbHistoryLogList(varLogList), headers);
    }
    
    @GET
    @Path("/variable/{varId: [a-zA-Z0-9-:\\.]+}/value/{value: [a-zA-Z0-9-:\\.]+}")
    public Response variable_varId_value_valueVal(@PathParam("varId") String variableId, @PathParam("value") String value) {
        Map<String, List<String>> params = getRequestParams(request);
        String oper = getRelativePath(request);
        List<VariableInstanceLog> varLogList 
            = internalGetVariableInstancesByVarAndValue(variableId, value, params, oper);
        varLogList = paginate(getPageNumAndPageSize(params, oper), varLogList);
        
        return createCorrectVariant(new JaxbHistoryLogList(varLogList), headers);
    } 
   
    @GET
    @Path("/history/variable/{varId: [a-zA-Z0-9-:\\.]+}/instances")
    public Response variable_varId_instances(@PathParam("varId") String variableId) {
        Map<String, List<String>> params = getRequestParams(request);
        String oper = getRelativePath(request);

        // get variables
        List<VariableInstanceLog> varLogList = internalGetVariableInstancesByVarAndValue(variableId, null, params, oper);
        
        // get process instance logs
        int maxNumResults = getMaxNumResultsNeeded(getPageNumAndPageSize(params, oper));
        List<ProcessInstanceLog> procInstLogList = getProcessInstanceLogsByVariable(varLogList, maxNumResults);
        return createCorrectVariant(new JaxbHistoryLogList(procInstLogList), headers);
    }
    
    @GET
    @Path("/history/variable/{varId: [a-zA-Z0-9-:\\.]+}/value/{value: [a-zA-Z0-9-:\\.]+}/instances")
    public Response variable_varId_value_valueVal_instances(@PathParam("procId") String variableId, @PathParam("value") String value) {
        Map<String, List<String>> params = getRequestParams(request);
        String oper = getRelativePath(request);

        // get variables
        List<VariableInstanceLog> varLogList = internalGetVariableInstancesByVarAndValue(variableId, value, params, oper);
        
        // get process instance logs
        int maxNumResults = getMaxNumResultsNeeded(getPageNumAndPageSize(params, oper));
        List<ProcessInstanceLog> procInstLogList = getProcessInstanceLogsByVariable(varLogList, maxNumResults);
        return createCorrectVariant(new JaxbHistoryLogList(procInstLogList), headers);
    }
    
    

    // Helper methods --------------------------------------------------------------------------------------------------------------

    private List<VariableInstanceLog> internalGetVariableInstancesByVarAndValue(String varId, String value, 
            Map<String, List<String>> params, String oper) { 
        // active processes parameter
        String activeProcsParam = getStringParam("activeProcesses", false, params, oper); 
        boolean onlyActiveProcesses = false;
        if( activeProcsParam != null ) { 
            onlyActiveProcesses = Boolean.parseBoolean(activeProcsParam);
        }
       
        Object result;
        if( value == null ) { 
            result = auditLogService.findVariableInstancesByName(varId, onlyActiveProcesses);
        } else { 
            result = auditLogService.findVariableInstancesByNameAndValue(varId, value, onlyActiveProcesses);
        }
        
        return (List<VariableInstanceLog>) result;
    }

    private List<ProcessInstanceLog> getProcessInstanceLogsByVariable(List<VariableInstanceLog> varLogList, int maxNumResults) {
        int numVarLogs = varLogList.size();
        int numProcInsts = 0;
       
        List<ProcessInstanceLog> resultList = new ArrayList<ProcessInstanceLog>();
        for( int i = 0; i < numVarLogs && numProcInsts < maxNumResults; ++i ) { 
            long procInstId = varLogList.get(i).getProcessInstanceId();
            ProcessInstanceLog procInstlog = auditLogService.findProcessInstance(procInstId);
            if( procInstlog != null ) { 
                resultList.add(procInstlog);
                ++numProcInsts;
            }
        }
        return resultList;
    }
}
