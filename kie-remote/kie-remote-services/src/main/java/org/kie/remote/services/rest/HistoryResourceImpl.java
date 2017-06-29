/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.RequestScoped;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

import org.jbpm.process.audit.NodeInstanceLog;
import org.jbpm.process.audit.ProcessInstanceLog;
import org.jbpm.process.audit.VariableInstanceLog;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.remote.services.rest.exception.KieRemoteRestOperationException;
import org.kie.services.client.serialization.jaxb.impl.audit.JaxbHistoryLogList;
import org.kie.services.client.serialization.jaxb.impl.audit.JaxbProcessInstanceLog;
import org.kie.services.client.serialization.jaxb.rest.JaxbGenericResponse;

/**
 * This resource is responsible for direct, simple access to the history information. 
 * 
 * For complex queries, see the {@link QueryResourceImpl}
 */
@Path("/history")
@RequestScoped
@SuppressWarnings("unchecked")
public class HistoryResourceImpl extends ResourceBase {

    /* REST information */
    
    @Context
    private HttpHeaders headers;
   
    // Rest methods --------------------------------------------------------------------------------------------------------------

    @GET
    @Path("/instances")
    @RolesAllowed({REST_ROLE, REST_PROCESS_RO_ROLE, REST_PROCESS_ROLE})
    public Response getProcessInstanceLogs() {
        String oper = getRelativePath();
        Map<String, String []> params = getRequestParams();
        String activeProcessesStr = getStringParam("activeProcesses", false, params, oper);
        boolean activeProcesses = Boolean.parseBoolean(activeProcessesStr);
        
        List<ProcessInstanceLog> procInstLogResults;
        if( activeProcesses ) { 
            procInstLogResults = getAuditLogService().findActiveProcessInstances();
        } else { 
            procInstLogResults = getAuditLogService().findProcessInstances();
        }
        sortProcessInstanceLogs(procInstLogResults);
        
        List<Object> results = new ArrayList<Object>(procInstLogResults);
        JaxbHistoryLogList resultList =  paginateAndCreateResult(params, oper, results, new JaxbHistoryLogList());
        
        return createCorrectVariant(resultList, headers);
    }

    @GET
    @Path("/instance/{procInstId: [0-9]+}")
    @RolesAllowed({REST_ROLE, REST_PROCESS_RO_ROLE, REST_PROCESS_ROLE})
    public Response getProcessInstanceLog(@PathParam("procInstId") long procInstId ) {
        ProcessInstanceLog procInstLog = getAuditLogService().findProcessInstance(procInstId);
        JaxbProcessInstanceLog jaxbProcLog = new JaxbProcessInstanceLog(procInstLog);
        
        return createCorrectVariant(jaxbProcLog, headers);
    }

    @GET
    @Path("/instance/{procInstId: [0-9]+}/{type: [a-zA-Z]+}")
    @RolesAllowed({REST_ROLE, REST_PROCESS_RO_ROLE, REST_PROCESS_ROLE})
    public Response getInstanceLogsByProcInstId( @PathParam("procInstId") long instId, @PathParam("type") String logType)  {
        Map<String, String []> params = getRequestParams();
        String oper = getRelativePath();
        
        List<? extends Object> varInstLogList;
        if ("child".equalsIgnoreCase(logType)) {
            Object result = getAuditLogService().findSubProcessInstances(instId);
            varInstLogList = (List<Object>) result;
            sortProcessInstanceLogs((List<ProcessInstanceLog>) varInstLogList);
        } else if ("node".equalsIgnoreCase(logType)) {
            Object result = getAuditLogService().findNodeInstances(instId);
            varInstLogList = (List<Object>) result;
            sortNodeInstanceLogs((List<NodeInstanceLog>) varInstLogList);
        } else if ("variable".equalsIgnoreCase(logType)) {
            Object result = getAuditLogService().findVariableInstances(instId);
            varInstLogList = (List<Object>) result;
            sortVariableInstanceLogs((List<VariableInstanceLog>) varInstLogList);
        } else {
            throw KieRemoteRestOperationException.badRequest("Unsupported operation: " + oper );
        }

        JaxbHistoryLogList resultList =  paginateAndCreateResult(params, oper, (List<Object>) varInstLogList, new JaxbHistoryLogList());
        
        return createCorrectVariant(resultList, headers);
    }
    
    @GET
    @Path("/instance/{procInstId: [0-9]+}/{type: [a-zA-Z]+}/{logId: [a-zA-Z0-9-:\\._]+}")
    @RolesAllowed({REST_ROLE, REST_PROCESS_RO_ROLE, REST_PROCESS_ROLE})
    public Response getInstanceLogsByProcInstIdByLogId(@PathParam("procInstId") long procInstId, @PathParam("type") String operation, @PathParam("logId") String logId) {
        Map<String, String []> params = getRequestParams();
        String oper = getRelativePath();
        
        List<? extends Object> varInstLogList;
        if ("node".equalsIgnoreCase(operation)) {
            Object result = getAuditLogService().findNodeInstances(procInstId, logId);
            varInstLogList = (List<Object>) result;
            sortNodeInstanceLogs((List<NodeInstanceLog>) varInstLogList);
        } else if ("variable".equalsIgnoreCase(operation)) {
            Object result = getAuditLogService().findVariableInstances(procInstId, logId);
            varInstLogList = (List<Object>) result;
            sortVariableInstanceLogs((List<VariableInstanceLog>) varInstLogList);
        } else {
            throw KieRemoteRestOperationException.badRequest("Unsupported operation: " + oper );
        }
        
        JaxbHistoryLogList resultList = paginateAndCreateResult(params, oper, (List<Object>) varInstLogList, new JaxbHistoryLogList());
        
        return createCorrectVariant(resultList, headers);
    }

    @GET
    @Path("/process/{processDefId: [a-zA-Z0-9-:\\._]+}")
    @RolesAllowed({REST_ROLE, REST_PROCESS_RO_ROLE, REST_PROCESS_ROLE})
    public Response getProcessInstanceLogsByProcessId(@PathParam("processDefId") String processId) {
        Map<String, String []> params = getRequestParams();
        Number statusParam = getNumberParam("status", false, params, getRelativePath(), false);
        String oper = getRelativePath();
        int[] pageInfo = getPageNumAndPageSize(params, oper);

        Object result;
        if (statusParam != null) {
            if (statusParam.intValue() == ProcessInstance.STATE_ACTIVE) {
                result = getAuditLogService().findActiveProcessInstances(processId);
            } else {
                result = getAuditLogService().findProcessInstances(processId);
            }
        } else {
            result = getAuditLogService().findProcessInstances(processId);
        }
        
        List<ProcessInstanceLog> procInstLogList = (List<ProcessInstanceLog>) result;
        
        List<ProcessInstanceLog> filteredProcLogList = procInstLogList;
        if (statusParam != null && !statusParam.equals(ProcessInstance.STATE_ACTIVE)) {
            filteredProcLogList = new ArrayList<ProcessInstanceLog>();
            for (int i = 0; 
                    i < procInstLogList.size() && filteredProcLogList.size() < getMaxNumResultsNeeded(pageInfo);
                    ++i) {
                ProcessInstanceLog procLog = procInstLogList.get(i);
                if (procLog.getStatus().equals(statusParam.intValue())) {
                    filteredProcLogList.add(procLog);
                }
            }
        }
        
        sortProcessInstanceLogs(filteredProcLogList);
        List<Object> results = new ArrayList<Object>(filteredProcLogList);
        JaxbHistoryLogList resultList = paginateAndCreateResult(pageInfo, results, new JaxbHistoryLogList());
        return createCorrectVariant(resultList, headers);
    }

    @GET
    @Path("/variable/{varId: [a-zA-Z0-9-:\\._]+}")
    @RolesAllowed({REST_ROLE, REST_PROCESS_RO_ROLE, REST_PROCESS_ROLE})
    public Response getVariableInstanceLogsByVariableId(@PathParam("varId") String variableId) {
        Map<String, String []> params = getRequestParams();
        String oper = getRelativePath();
        
        List<VariableInstanceLog> varInstLogList = internalGetVariableInstancesByVarAndValue(variableId, null, params, oper);
        sortVariableInstanceLogs(varInstLogList);
        
        List<Object> results = new ArrayList<Object>(varInstLogList);
        JaxbHistoryLogList resultList = paginateAndCreateResult(params, oper, results, new JaxbHistoryLogList());
        
        return createCorrectVariant(resultList, headers);
    }
    
    @GET
    @Path("/variable/{varId: [a-zA-Z0-9-:\\._]+}/value/{value: .+}")
    @RolesAllowed({REST_ROLE, REST_PROCESS_RO_ROLE, REST_PROCESS_ROLE})
    public Response getVariableInstanceLogsByVariableIdByVariableValue(@PathParam("varId") String variableId, @PathParam("value") String value) {
        Map<String, String []> params = getRequestParams();
        String oper = getRelativePath();
        List<VariableInstanceLog> varInstLogList = internalGetVariableInstancesByVarAndValue(variableId, value, params, oper);
        sortVariableInstanceLogs(varInstLogList);
        
        List<Object> results = new ArrayList<Object>(varInstLogList);
        JaxbHistoryLogList resultList = paginateAndCreateResult(params, oper, results, new JaxbHistoryLogList());
        
        return createCorrectVariant(resultList, headers);
    } 
   
    @GET
    @Path("/variable/{varId: [a-zA-Z0-9-:\\._]+}/instances")
    @RolesAllowed({REST_ROLE, REST_PROCESS_RO_ROLE, REST_PROCESS_ROLE})
    public Response getProcessInstanceLogsByVariableId(@PathParam("varId") String variableId) {
        Map<String, String[]> params = getRequestParams();
        String oper = getRelativePath();

        // get variables
        List<VariableInstanceLog> varLogList = internalGetVariableInstancesByVarAndValue(variableId, null, params, oper);
        
        // get process instance logs
        int [] pageInfo = getPageNumAndPageSize(params, oper);
        int maxNumResults = getMaxNumResultsNeeded(pageInfo);
        List<ProcessInstanceLog> procInstLogList = getProcessInstanceLogsByVariable(varLogList, maxNumResults);
       
        // paginate
        List<Object> results = new ArrayList<Object>(procInstLogList);
        JaxbHistoryLogList resultList = paginateAndCreateResult(pageInfo, results, new JaxbHistoryLogList());
        
        return createCorrectVariant(resultList, headers);
    }
    
    @GET
    @Path("/variable/{varId: [a-zA-Z0-9-:\\._]+}/value/{value: .+}/instances")
    @RolesAllowed({REST_ROLE, REST_PROCESS_RO_ROLE, REST_PROCESS_ROLE})
    public Response getProcessInstanceLogsByVariableIdByVariableValue(@PathParam("varId") String variableId, @PathParam("value") String value) {
        Map<String, String[]> params = getRequestParams();
        String oper = getRelativePath();

        // get variables
        List<VariableInstanceLog> varLogList = internalGetVariableInstancesByVarAndValue(variableId, value, params, oper);
        
        // get process instance logs
        int [] pageInfo = getPageNumAndPageSize(params, oper);
        int maxNumResults = getMaxNumResultsNeeded(pageInfo);
        List<ProcessInstanceLog> procInstLogList = getProcessInstanceLogsByVariable(varLogList, maxNumResults);
        
        List<Object> results = new ArrayList<Object>(procInstLogList);
        JaxbHistoryLogList resultList = paginateAndCreateResult(pageInfo, results, new JaxbHistoryLogList());
        return createCorrectVariant(resultList, headers);
    }
   
    @POST
    @Path("/clear")
    @RolesAllowed({REST_ROLE, REST_PROCESS_ROLE})
    public Response clear() {
        getAuditLogService().clear();
        return createCorrectVariant(new JaxbGenericResponse(getRequestUri()), headers);
    }
    
    // Helper methods --------------------------------------------------------------------------------------------------------------

    private List<VariableInstanceLog> internalGetVariableInstancesByVarAndValue(String varId, String value, 
            Map<String, String[]> params, String oper) { 
        // active processes parameter
        String activeProcsParam = getStringParam("activeProcesses", false, params, oper); 
        boolean onlyActiveProcesses = false;
        if( activeProcsParam != null ) { 
            onlyActiveProcesses = Boolean.parseBoolean(activeProcsParam);
        }
       
        Object result;
        if( value == null ) { 
            result = getAuditLogService().findVariableInstancesByName(varId, onlyActiveProcesses);
        } else { 
            result = getAuditLogService().findVariableInstancesByNameAndValue(varId, value, onlyActiveProcesses);
        }
        
        return (List<VariableInstanceLog>) result;
    }

    private List<ProcessInstanceLog> getProcessInstanceLogsByVariable(List<VariableInstanceLog> varLogList, int maxNumResults) {
        int numVarLogs = varLogList.size();
        int numProcInsts = 0;
       
        List<ProcessInstanceLog> resultList = new ArrayList<ProcessInstanceLog>();
        for( int i = 0; i < numVarLogs && numProcInsts < maxNumResults; ++i ) { 
            long procInstId = varLogList.get(i).getProcessInstanceId();
            ProcessInstanceLog procInstlog = getAuditLogService().findProcessInstance(procInstId);
            if( procInstlog != null ) { 
                resultList.add(procInstlog);
                ++numProcInsts;
            }
        }
        return resultList;
    }

    private void sortProcessInstanceLogs(List<ProcessInstanceLog> procInstLogList) { 
        Collections.sort(procInstLogList, new Comparator<ProcessInstanceLog>() {
    
            @Override
            public int compare( ProcessInstanceLog o1, ProcessInstanceLog o2 ) {
                if( ! o1.getExternalId().equals(o2.getExternalId()) ) { 
                   return o1.getExternalId().compareTo(o2.getExternalId());
                }
                if( ! o1.getProcessId().equals(o2.getProcessId()) ) { 
                   return o1.getProcessId().compareTo(o2.getProcessId());
                }
                return o1.getProcessInstanceId().compareTo(o2.getProcessInstanceId());
            }
        });
    }
    

    private void sortNodeInstanceLogs(List<NodeInstanceLog> procInstLogList) { 
        Collections.sort(procInstLogList, new Comparator<NodeInstanceLog>() {
    
            @Override
            public int compare( NodeInstanceLog o1, NodeInstanceLog o2 ) {
                if( ! o1.getExternalId().equals(o2.getExternalId()) ) { 
                   return o1.getExternalId().compareTo(o2.getExternalId());
                }
                if( ! o1.getProcessId().equals(o2.getProcessId()) ) { 
                   return o1.getProcessId().compareTo(o2.getProcessId());
                }
                if( ! o1.getProcessInstanceId().equals(o2.getProcessInstanceId()) ) { 
                   return o1.getProcessInstanceId().compareTo(o2.getProcessInstanceId());
                }
                if( ! o1.getNodeId().equals(o2.getNodeId()) ) { 
                   return o1.getNodeId().compareTo(o2.getNodeId());
                }
                return o1.getNodeInstanceId().compareTo(o2.getNodeInstanceId());
            }
        });
    }

    private void sortVariableInstanceLogs(List<VariableInstanceLog> varInstLogList ) { 
        Collections.sort(varInstLogList, new Comparator<VariableInstanceLog>() {

            @Override
            public int compare( VariableInstanceLog o1, VariableInstanceLog o2 ) {
                if( ! o1.getExternalId().equals(o2.getExternalId()) ) { 
                    return o1.getExternalId().compareTo(o2.getExternalId());
                }
                if( ! o1.getProcessId().equals(o2.getProcessId()) ) { 
                    return o1.getProcessId().compareTo(o2.getProcessId());
                }
                if( ! o1.getProcessInstanceId().equals(o2.getProcessInstanceId()) ) { 
                   return o1.getProcessInstanceId().compareTo(o2.getProcessInstanceId());
                }
                if( ! o1.getVariableId().equals(o2.getVariableId()) ) { 
                   return o1.getVariableId().compareTo(o2.getVariableId());
                }
                return o1.getVariableInstanceId().compareTo(o2.getVariableInstanceId());
            }
        });
    }
}
    
