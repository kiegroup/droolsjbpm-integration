package org.kie.remote.services.rest.query;

import static org.kie.remote.services.rest.query.QueryResourceData.actionParamNameMap;
import static org.kie.remote.services.rest.query.QueryResourceData.getDates;
import static org.kie.remote.services.rest.query.QueryResourceData.getInts;
import static org.kie.remote.services.rest.query.QueryResourceData.getLongs;
import static org.kie.remote.services.rest.query.QueryResourceData.getTaskStatuses;

import java.util.ArrayDeque;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jbpm.services.task.commands.TaskQueryDataCommand;
import org.kie.api.runtime.manager.audit.VariableInstanceLog;
import org.kie.api.task.model.Status;
import org.kie.api.task.model.TaskSummary;
import org.kie.internal.query.data.QueryData;
import org.kie.remote.services.rest.ResourceBase;
import org.kie.remote.services.rest.exception.KieRemoteRestOperationException;
import org.kie.services.client.serialization.jaxb.impl.query.JaxbQueryTaskInfo;
import org.kie.services.client.serialization.jaxb.impl.query.JaxbQueryTaskResult;
import org.kie.services.client.serialization.jaxb.impl.query.JaxbVariableInfo;
import org.kie.services.client.serialization.jaxb.impl.task.JaxbTaskSummary;

/**
 * This class contains the core logic for processing the query parameters from a 
 * REST task query call to a {@link JaxbQueryTaskResult} instance.
 */
public class InternalTaskQueryHelper extends AbstractInternalQueryHelper {

    public InternalTaskQueryHelper(ResourceBase resourceBase) { 
       super(resourceBase);
    }
    
    /**
     * This method is the internal logic that
     * <ol>
     * <li>preprocesses the list of query parameters passed to the <code>../rest/query/runtime/task</code> operation</li>
     * <li>builds and executes a query to retrieved the requested information
     * <li>
     * <li>converts the results to a {@link JaxbQueryTaskResult} instance</li>
     * 
     * @param queryParams The query parameters map returned by the JAX-RS logic
     * @param pageInfo The pagination information
     * @param maxResults The maximum number of results to be returned, determined by the pagination information
     * @return A {@link JaxbQueryTaskResult} containing a list of entities containing task and process variable information
     */
    public JaxbQueryTaskResult queryTasksAndVariables( String identity, Map<String, String[]> queryParams, int[] pageInfo, int maxResults ) {

        Map<String, String> varValueMap = new HashMap<String, String>();
        Map<String, String> varRegexMap = new HashMap<String, String>();

        // meta (in-memory or history variable values?)
        Boolean workFlowInstanceVariables = determineWorkFlowInstanceVariableUse(queryParams);

        Map<String, String> procVarValues;
        if( workFlowInstanceVariables ) {
            procVarValues = new HashMap<String, String>();
        }
       
        boolean retrieveAllVarLogs = true;
        String [] paramVals = queryParams.remove("all");
        if( paramVals != null ) { 
           retrieveAllVarLogs = false;
        }
        
        ArrayDeque<ActionData> actionDataQueue = fillActionDataQueueFromQueryParams(queryParams, varValueMap, varRegexMap);
      
        // setup queries
        RemoteServicesQueryCommandBuilder taskQueryBuilder = new RemoteServicesQueryCommandBuilder(identity);
        RemoteServicesQueryCommandBuilder varInstLogQueryBuilder = new RemoteServicesQueryCommandBuilder();

        if( retrieveAllVarLogs ) { 
            varInstLogQueryBuilder.last(); 
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
                taskQueryBuilder.processInstanceId(longData);
                varInstLogQueryBuilder.processInstanceId(longData);
                if( actionData.min || actionData.max ) {
                    if( longData.length > 1 ) {
                        throw KieRemoteRestOperationException.notFound("Only 1 '" + actionData.paramName
                                + "' parameter is accepted");
                    }
                    if( actionData.min ) {
                        varInstLogQueryBuilder.processInstanceIdMin(longData[0]);
                        taskQueryBuilder.processInstanceIdMin(longData[0]);
                        actionData.min = false;
                    } else if( actionData.max ) {
                        varInstLogQueryBuilder.processInstanceIdMax(longData[0]);
                        taskQueryBuilder.processInstanceIdMax(longData[0]);
                        actionData.max = false;
                    }
                } else {
                    varInstLogQueryBuilder.processInstanceId(longData);
                    taskQueryBuilder.processInstanceId(longData);
                }
                break;
            case 1: // processid
                assert "processid".equals(actionParamNameMap.get(action)): action + " : processid";
                setRegexOnOff(actionData, true, taskQueryBuilder, varInstLogQueryBuilder);
                taskQueryBuilder.processId(data);
                varInstLogQueryBuilder.processId(data);
                setRegexOnOff(actionData, false, taskQueryBuilder, varInstLogQueryBuilder);
                break;
            case 2: // workitemid
                assert "workitemid".equals(actionParamNameMap.get(action)): action + " : workitemid";
                longData = getLongs(action, data);
                taskQueryBuilder.workItemId(longData);
                varInstLogQueryBuilder.workItemId(longData);
                break;
            case 3: // deploymentid
                assert "deploymentid".equals(actionParamNameMap.get(action)): action + " : deploymentid";
                taskQueryBuilder.deploymentId(data);
                varInstLogQueryBuilder.deploymentId(data);
                break;

            // task
            case 4: // task id
                assert "taskid".equals(actionParamNameMap.get(action)): action + " : taskid";
                longData = getLongs(action, data);
                taskQueryBuilder.taskId(longData);
                varInstLogQueryBuilder.taskId(longData);
                break;
            case 5: // initiator
                assert "initiator".equals(actionParamNameMap.get(action)): action + " : initiator";
                setRegexOnOff(actionData, true, taskQueryBuilder, varInstLogQueryBuilder);
                taskQueryBuilder.initiator(data);
                varInstLogQueryBuilder.initiator(data);
                setRegexOnOff(actionData, false, taskQueryBuilder, varInstLogQueryBuilder);
                break;
            case 6: // stakeholder
                assert "stakeholder".equals(actionParamNameMap.get(action)): action + " : stakeholder";
                setRegexOnOff(actionData, true, taskQueryBuilder, varInstLogQueryBuilder);
                taskQueryBuilder.stakeHolder(data);
                varInstLogQueryBuilder.stakeHolder(data);
                setRegexOnOff(actionData, false, taskQueryBuilder, varInstLogQueryBuilder);
                break;
            case 7: // potential owner
                assert "potentialowner".equals(actionParamNameMap.get(action)): action + " : potentialowner";
                setRegexOnOff(actionData, true, taskQueryBuilder, varInstLogQueryBuilder);
                taskQueryBuilder.potentialOwner(data);
                varInstLogQueryBuilder.potentialOwner(data);
                setRegexOnOff(actionData, false, taskQueryBuilder, varInstLogQueryBuilder);
                break;
            case 8: // task owner
                assert "taskowner".equals(actionParamNameMap.get(action)): action + " : taskowner";
                setRegexOnOff(actionData, true, taskQueryBuilder, varInstLogQueryBuilder);
                taskQueryBuilder.taskOwner(data);
                varInstLogQueryBuilder.taskOwner(data);
                setRegexOnOff(actionData, false, taskQueryBuilder, varInstLogQueryBuilder);
                break;
            case 9: // business admin
                assert "businessadmin".equals(actionParamNameMap.get(action)): action + " : businessadmin";
                setRegexOnOff(actionData, true, taskQueryBuilder, varInstLogQueryBuilder);
                taskQueryBuilder.businessAdmin(data);
                varInstLogQueryBuilder.businessAdmin(data);
                setRegexOnOff(actionData, false, taskQueryBuilder, varInstLogQueryBuilder);
                break;
            case 10: // task status
                assert "taskstatus".equals(actionParamNameMap.get(action)): action + " : taskstatus";
                Status[] statuses = getTaskStatuses(data);
                taskQueryBuilder.taskStatus(statuses);
                varInstLogQueryBuilder.taskStatus(statuses);
                break;

            // process instance
            case 11: // process instance status
                assert "processinstancestatus".equals(actionParamNameMap.get(action)): action + " : processinstancestatus";
                intData = getInts(action, data);
                taskQueryBuilder.processInstanceStatus(intData);
                varInstLogQueryBuilder.processInstanceStatus(intData);
                break;
            case 12: // process version
                assert "processversion".equals(actionParamNameMap.get(action)): action + " : processversion";
                setRegexOnOff(actionData, true, taskQueryBuilder, varInstLogQueryBuilder);
                taskQueryBuilder.processVersion(data);
                varInstLogQueryBuilder.processVersion(data);
                setRegexOnOff(actionData, false, taskQueryBuilder, varInstLogQueryBuilder);
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
                        taskQueryBuilder.startDateMin(dateData[0]);
                        varInstLogQueryBuilder.startDateMin(dateData[0]);
                        actionData.min = false;
                    } else if( actionData.max ) {
                        taskQueryBuilder.startDateMax(dateData[0]);
                        varInstLogQueryBuilder.startDateMax(dateData[0]);
                        actionData.max = false;
                    }
                } else {
                    taskQueryBuilder.startDate(dateData);
                    varInstLogQueryBuilder.startDate(dateData);
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
                        taskQueryBuilder.endDateMin(dateData[0]);
                        varInstLogQueryBuilder.endDateMin(dateData[0]);
                        actionData.min = false;
                    } else if( actionData.max ) {
                        taskQueryBuilder.endDateMax(dateData[0]);
                        varInstLogQueryBuilder.endDateMax(dateData[0]);
                        actionData.max = false;
                    }
                } else {
                    taskQueryBuilder.startDate(dateData);
                    varInstLogQueryBuilder.startDate(dateData);
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
                setRegexOnOff(actionData, true, taskQueryBuilder, varInstLogQueryBuilder);
                taskQueryBuilder.variableId(data);
                varInstLogQueryBuilder.last().variableId(data);
                setRegexOnOff(actionData, false, taskQueryBuilder, varInstLogQueryBuilder);
                break;
            case 16: // var value
                assert "varvalue".equals(actionParamNameMap.get(action)): action + " : varvalue";
                if( actionData.regex && workFlowInstanceVariables ) {
                    String param = actionParamNameMap.get(action);
                    throw KieRemoteRestOperationException.badRequest("Regular expresssions are not supported on the '" + param
                            + "' parameter " + "when retrieving in-memory process variables");
                }
                setRegexOnOff(actionData, true, taskQueryBuilder, varInstLogQueryBuilder);
                taskQueryBuilder.value(data);
                varInstLogQueryBuilder.value(data);
                setRegexOnOff(actionData, false, taskQueryBuilder, varInstLogQueryBuilder);
                break;
            case 17: // var
                assert "var".equals(actionParamNameMap.get(action)): action + " : var";
                taskQueryBuilder.variableValue(data[0], varValueMap.get(data[0]));
                varInstLogQueryBuilder.variableValue(data[0], varValueMap.get(data[0]));
                break;
            case 18: // varregex
                assert "varregex".equals(actionParamNameMap.get(action)): action + " : varregex";
                setRegexOnOff(actionData, true, taskQueryBuilder, varInstLogQueryBuilder);
                taskQueryBuilder.variableValue(data[0], varRegexMap.get(data[0]));
                varInstLogQueryBuilder.variableValue(data[0], varRegexMap.get(data[0]));
                setRegexOnOff(actionData, false, taskQueryBuilder, varInstLogQueryBuilder);
                break;

            default:
                throw KieRemoteRestOperationException.internalServerError("Please contact the developers: state [" + action + "] should not be possible.");
            }
            if( actionData.min || actionData.max || actionData.regex ) {
                throw KieRemoteRestOperationException.notFound("Query parameter '" + actionData.paramName + "' is not supported.");
            }
        }

        TaskQueryDataCommand taskCmd = taskQueryBuilder.createTaskQueryDataCommand();
        List<TaskSummary> taskSummaries = resourceBase.doRestTaskOperation(taskCmd);
        QueryData varLogQueryData = varInstLogQueryBuilder.getQueryData();
        List<VariableInstanceLog> varLogs = resourceBase.getAuditLogService().queryVariableInstanceLogs(varLogQueryData);
        List<JaxbVariableInfo> procVars = null;
        
        if( workFlowInstanceVariables ) {
            for( VariableInstanceLog varLog : varLogs ) {
                // TODO: retrieve process instance variables instead of log string values
            }
        }

        JaxbQueryTaskResult result = createQueryTaskResult(taskSummaries, varLogs, procVars);
        return result;
    }

    /**
     * Create a {@link JaxbQueryTaskResult} instance from the given information.
     * @param taskSummaries A list of {@link TaskSummary} instances
     * @param varLogs A list of {@link VariableInstanceLog} instances
     * @param processVariables A list of {@link JaxbVariableInfo} instances
     * @return A {@link JaxbQueryTaskResult}
     */
    private static JaxbQueryTaskResult createQueryTaskResult( List<TaskSummary> taskSummaries, List<VariableInstanceLog> varLogs,
            List<JaxbVariableInfo> processVariables ) {
        JaxbQueryTaskResult result = new JaxbQueryTaskResult();

        Map<Long, JaxbQueryTaskInfo> procInstIdTaskInfoMap = new HashMap<Long, JaxbQueryTaskInfo>();
        for( TaskSummary taskSum : taskSummaries ) {
            long procInstId = taskSum.getProcessInstanceId();
            JaxbQueryTaskInfo taskInfo = createJaxbQueryTaskInfo(procInstId, procInstIdTaskInfoMap);
            taskInfo.getTaskSummaries().add(new JaxbTaskSummary(taskSum));
        }
        for( VariableInstanceLog varLog : varLogs ) {
            long procInstId = varLog.getProcessInstanceId();
            JaxbQueryTaskInfo taskInfo = createJaxbQueryTaskInfo(procInstId, procInstIdTaskInfoMap);
            taskInfo.getVariables().add(new JaxbVariableInfo(varLog));
        }

        result.getTaskInfoList().addAll(procInstIdTaskInfoMap.values());
        return result;

    }

    private static JaxbQueryTaskInfo createJaxbQueryTaskInfo( long procInstId, Map<Long, JaxbQueryTaskInfo> procInstIdTaskInfoMap ) {
        JaxbQueryTaskInfo taskInfo = procInstIdTaskInfoMap.get(procInstId);
        if( taskInfo == null ) {
            taskInfo = new JaxbQueryTaskInfo(procInstId);
            procInstIdTaskInfoMap.put(procInstId, taskInfo);
        }
        return taskInfo;
    }

}
