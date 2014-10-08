package org.kie.remote.services.rest.query;

import static org.kie.remote.services.rest.query.QueryResourceData.actionParamNameMap;
import static org.kie.remote.services.rest.query.QueryResourceData.getDates;
import static org.kie.remote.services.rest.query.QueryResourceData.getInts;
import static org.kie.remote.services.rest.query.QueryResourceData.getLongs;

import java.util.ArrayDeque;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jbpm.process.audit.AuditLogService;
import org.kie.api.runtime.manager.audit.ProcessInstanceLog;
import org.kie.api.runtime.manager.audit.VariableInstanceLog;
import org.kie.internal.query.data.QueryData;
import org.kie.remote.services.rest.ResourceBase;
import org.kie.remote.services.rest.exception.KieRemoteRestOperationException;
import org.kie.services.client.serialization.jaxb.impl.process.JaxbProcessInstance;
import org.kie.services.client.serialization.jaxb.impl.query.JaxbQueryProcessInstanceInfo;
import org.kie.services.client.serialization.jaxb.impl.query.JaxbQueryProcessInstanceResult;
import org.kie.services.client.serialization.jaxb.impl.query.JaxbVariableInfo;

public class InternalProcInstQueryHelper extends AbstractInternalQueryHelper {

    public InternalProcInstQueryHelper(ResourceBase resourceBase) { 
        super(resourceBase);
    }
   
    public JaxbQueryProcessInstanceResult queryProcessInstancesAndVariables( Map<String, String[]> queryParams, int[] pageInfo,
            int maxResults ) {

        Map<String, String> varValueMap = new HashMap<String, String>();
        Map<String, String> varRegexMap = new HashMap<String, String>();

        // meta (in-memory or history variable values?)
        Boolean workFlowInstanceVariables = determineWorkFlowInstanceVariableUse(queryParams);

        Map<String, String> procVarValues;
        if( workFlowInstanceVariables ) {
            procVarValues = new HashMap<String, String>();
        }
      
        boolean onlyRetrieveLastVarLogs = true;
        String [] paramVals = queryParams.remove("all");
        if( paramVals != null ) { 
           onlyRetrieveLastVarLogs = false;
        }
        
        ArrayDeque<ActionData> actionDataQueue = fillActionDataQueueFromQueryParams(queryParams, varValueMap, varRegexMap);
      
        // setup queries
        RemoteServicesQueryCommandBuilder instLogQueryBuilder = new RemoteServicesQueryCommandBuilder();

        if( onlyRetrieveLastVarLogs ) { 
            instLogQueryBuilder.last();
        }
        
        int[] intData;
        long[] longData;
        Date[] dateData;
        while( !actionDataQueue.isEmpty() ) {
            ActionData actionData = actionDataQueue.poll();
            String[] data = actionData.paramData;
            int action = actionData.action;
            switch ( action ) {
            // general
            case 0: // processinstanceid
                assert "processinstanceid".equals(actionParamNameMap.get(action)): action + " : processinstanceid";
                longData = getLongs(action, data);
                instLogQueryBuilder.processInstanceId(longData);
                break;
            case 1: // processid
                assert "processid".equals(actionParamNameMap.get(action)): action + " : processid";
                setRegexOnOff(actionData, true, instLogQueryBuilder);
                instLogQueryBuilder.processId(data);
                setRegexOnOff(actionData, false, instLogQueryBuilder);
                break;
            case 2: // workitemid
                assert "workitemid".equals(actionParamNameMap.get(action)): action + " : workitemid";
                longData = getLongs(action, data);
                instLogQueryBuilder.workItemId(longData);
                break;
            case 3: // deploymentid
                assert "deploymentid".equals(actionParamNameMap.get(action)): action + " : deploymentid";
                instLogQueryBuilder.deploymentId(data);
                break;

            // process instance
            case 11: // process instance status
                assert "processinstancestatus".equals(actionParamNameMap.get(action)): action + " : processinstancestatus";
                intData = getInts(action, data);
                instLogQueryBuilder.processInstanceStatus(intData);
                break;
            case 12: // process version
                assert "processversion".equals(actionParamNameMap.get(action)): action + " : processversion";
                setRegexOnOff(actionData, true, instLogQueryBuilder);
                instLogQueryBuilder.processVersion(data);
                setRegexOnOff(actionData, false, instLogQueryBuilder);
                break;
            case 13: // start date
                assert "startdate".equals(actionParamNameMap.get(action)): action + " : startdate";
                dateData = getDates(action, data);
                if( actionData.min || actionData.max ) {
                    if( dateData.length != 1 ) {
                        throw KieRemoteRestOperationException.notFound("Only 1 '" + actionData.paramName
                                + "' parameter is accepted");
                    }
                    if( actionData.min ) {
                        instLogQueryBuilder.startDateMin(dateData[0]);
                        actionData.min = false;
                    } else if( actionData.max ) {
                        instLogQueryBuilder.startDateMax(dateData[0]);
                        actionData.max = false;
                    }
                } else {
                    instLogQueryBuilder.startDate(dateData);
                }
                break;
            case 14: // end date
                assert "enddate".equals(actionParamNameMap.get(action)): action + " : enddate";
                dateData = getDates(action, data);
                if( actionData.min || actionData.max ) {
                    if( dateData.length > 1 ) {
                        throw KieRemoteRestOperationException.notFound("Only 1 '" + actionData.paramName
                                + "' parameter is accepted");
                    }
                    if( actionData.min ) {
                        instLogQueryBuilder.endDateMin(dateData[0]);
                        actionData.min = false;
                    } else if( actionData.max ) {
                        instLogQueryBuilder.endDateMax(dateData[0]);
                        actionData.max = false;
                    }
                } else {
                    instLogQueryBuilder.startDate(dateData);
                }
                break;

            // variable instance
            case 15: // var id
                assert "varid".equals(actionParamNameMap.get(action)): action + " : varid";
                if( actionData.regex && workFlowInstanceVariables ) {
                    String param = actionParamNameMap.get(action);
                    throw KieRemoteRestOperationException.badRequest("Regular expresssions are not supported on the '" + param
                            + "' parameter " + "when retrieving in-memory process variables");
                }
                setRegexOnOff(actionData, true, instLogQueryBuilder);
                instLogQueryBuilder.variableId(data);
                setRegexOnOff(actionData, false, instLogQueryBuilder);
                instLogQueryBuilder.last();
                break;
            case 16: // var value
                assert "varvalue".equals(actionParamNameMap.get(action)): action + " : varvalue";
                if( actionData.regex && workFlowInstanceVariables ) {
                    String param = actionParamNameMap.get(action);
                    throw KieRemoteRestOperationException.badRequest("Regular expresssions are not supported on the '" + param
                            + "' parameter when retrieving in-memory process variables");
                }
                setRegexOnOff(actionData, true, instLogQueryBuilder);
                instLogQueryBuilder.value(data);
                setRegexOnOff(actionData, false, instLogQueryBuilder);
                break;
            case 17: // var
                assert "var".equals(actionParamNameMap.get(action)): action + " : var";
                instLogQueryBuilder.variableValue(data[0], varValueMap.get(data[0]));
                break;
            case 18: // varregex
                assert "varregex".equals(actionParamNameMap.get(action)): action + " : varregex";
                setRegexOnOff(actionData, true, instLogQueryBuilder);
                instLogQueryBuilder.variableValue(data[0], varRegexMap.get(data[0]));
                setRegexOnOff(actionData, false, instLogQueryBuilder);
                break;
                
            default:
                throw KieRemoteRestOperationException
                        .internalServerError("Please contact the developers: this state should not be possible.");
            }
            if( actionData.min || actionData.max || actionData.regex ) {
                throw KieRemoteRestOperationException.notFound("Query parameter '" + actionData.paramName + "' is not supported.");
            }
        }

        QueryData queryData = instLogQueryBuilder.getQueryData();
        AuditLogService auditLogService = resourceBase.getAuditLogService();
        List<VariableInstanceLog> varLogs = auditLogService.queryVariableInstanceLogs(queryData);
        List<ProcessInstanceLog> procLogs = auditLogService.queryProcessInstanceLogs(queryData);
        List<JaxbVariableInfo> procVars = null;

        JaxbQueryProcessInstanceResult result = createProcessInstanceResult(procLogs, varLogs, procVars);
        return result;
    }

    private static JaxbQueryProcessInstanceResult createProcessInstanceResult( List<ProcessInstanceLog> procLogs,
            List<VariableInstanceLog> varLogs, List<JaxbVariableInfo> processVariables ) {
        JaxbQueryProcessInstanceResult result = new JaxbQueryProcessInstanceResult();

        Map<Long, JaxbQueryProcessInstanceInfo> procInstIdProcInstInfoMap = new HashMap<Long, JaxbQueryProcessInstanceInfo>();
        for( ProcessInstanceLog procLog : procLogs ) {
            long procInstId = procLog.getProcessInstanceId();
            JaxbQueryProcessInstanceInfo taskInfo = getQueryProcessInstanceInfo(procInstId, procInstIdProcInstInfoMap);
            taskInfo.setProcessInstance(new JaxbProcessInstance(procLog));
        }
        for( VariableInstanceLog varLog : varLogs ) {
            long procInstId = varLog.getProcessInstanceId();
            JaxbQueryProcessInstanceInfo taskInfo = getQueryProcessInstanceInfo(procInstId, procInstIdProcInstInfoMap);
            taskInfo.getVariables().add(new JaxbVariableInfo(varLog));
        }

        result.getProcessInstanceInfoList().addAll(procInstIdProcInstInfoMap.values());
        return result;
    }

    private static JaxbQueryProcessInstanceInfo getQueryProcessInstanceInfo( long procInstId,
            Map<Long, JaxbQueryProcessInstanceInfo> procInstIdProcInstInfoMap ) {
        JaxbQueryProcessInstanceInfo procInstInfo = procInstIdProcInstInfoMap.get(procInstId);
        if( procInstInfo == null ) {
            procInstInfo = new JaxbQueryProcessInstanceInfo();
            procInstIdProcInstInfoMap.put(procInstId, procInstInfo);
        }
        return procInstInfo;
    }

}
