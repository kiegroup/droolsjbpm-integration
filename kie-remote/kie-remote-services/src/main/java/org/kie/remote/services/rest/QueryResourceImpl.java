package org.kie.remote.services.rest;

import static org.kie.remote.services.rest.query.QueryResourceData.actionParamNameMap;
import static org.kie.remote.services.rest.query.QueryResourceData.getDates;
import static org.kie.remote.services.rest.query.QueryResourceData.getInts;
import static org.kie.remote.services.rest.query.QueryResourceData.getLongs;
import static org.kie.remote.services.rest.query.QueryResourceData.getTaskStatuses;
import static org.kie.remote.services.rest.query.QueryResourceData.metaRuntimeParams;
import static org.kie.remote.services.rest.query.QueryResourceData.metaRuntimeParamsShort;
import static org.kie.remote.services.rest.query.QueryResourceData.paramNameActionMap;

import java.util.ArrayDeque;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

import org.jbpm.kie.services.api.IdentityProvider;
import org.jbpm.process.audit.query.ProcInstLogQueryBuilderImpl;
import org.jbpm.services.task.commands.GetTasksByVariousFieldsCommand;
import org.jbpm.services.task.commands.TaskQueryDataCommand;
import org.kie.api.runtime.manager.audit.ProcessInstanceLog;
import org.kie.api.runtime.manager.audit.VariableInstanceLog;
import org.kie.api.task.model.Status;
import org.kie.api.task.model.TaskSummary;
import org.kie.internal.query.data.QueryData;
import org.kie.remote.services.jaxb.JaxbTaskSummaryListResponse;
import org.kie.remote.services.rest.exception.KieRemoteRestOperationException;
import org.kie.remote.services.rest.query.RemoteServicesQueryCommandBuilder;
import org.kie.services.client.serialization.jaxb.impl.process.JaxbProcessInstance;
import org.kie.services.client.serialization.jaxb.impl.query.JaxbQueryProcessInstanceInfo;
import org.kie.services.client.serialization.jaxb.impl.query.JaxbQueryProcessInstanceResult;
import org.kie.services.client.serialization.jaxb.impl.query.JaxbQueryTaskInfo;
import org.kie.services.client.serialization.jaxb.impl.query.JaxbQueryTaskResult;
import org.kie.services.client.serialization.jaxb.impl.query.JaxbVariableInfo;
import org.kie.services.client.serialization.jaxb.impl.task.JaxbTaskSummary;

/**
 * Not done.
 * 
 * Available starting with 6.2.0.Final
 */

@RequestScoped
@Path("/query/")
public class QueryResourceImpl extends ResourceBase {

    @Context
    private HttpHeaders headers;

    @Inject
    private IdentityProvider identityProvider;
    
    void setIdentityProvider( IdentityProvider identityProvider ) {
        this.identityProvider = identityProvider;
    }
    
    void setHeaders( HttpHeaders headers ) {
        this.headers = headers;
    }

    // REST operations -----------------------------------------------------------------------------------------------------------

    @GET
    @Path("/runtime/task")
    public Response queryTasks() {
        String oper = getRelativePath();
        Map<String, String[]> params = getRequestParams();
        int[] pageInfo = getPageNumAndPageSize(params, oper);
        int maxNumResults = getMaxNumResultsNeeded(pageInfo);

        JaxbQueryTaskResult result = queryTasksAndVariables(params, pageInfo, maxNumResults);

        return createCorrectVariant(result, headers);
    }

    @GET
    @Path("/runtime/process")
    public Response queryProcessInstances() { 
        String oper = getRelativePath();
        Map<String, String[]> params = getRequestParams();
        int[] pageInfo = getPageNumAndPageSize(params, oper);
        int maxNumResults = getMaxNumResultsNeeded(pageInfo);

        JaxbQueryProcessInstanceResult result = queryProcessInstancesAndVariables(params, pageInfo, maxNumResults);
        
        return createCorrectVariant(null, headers);
    }

    private class ActionData { 
      
        public final String param;
       public final int action;
       public final String [] data;
       
       public boolean regex = false;
       public boolean min = false;
       public boolean max = false;
       
       public ActionData(String param, int action, String [] data) { 
           this.param = param;
           this.action = action;
           this.data = data;
       }
       
       public ActionData(String param, int action) { 
           this(param, action, null);
       }
    }
    
    // pkg scope for testing
    JaxbQueryTaskResult queryTasksAndVariables( Map<String, String[]> queryParams, int[] pageInfo, int maxResults ) {

        ArrayDeque<ActionData> actionDataQueue = new ArrayDeque<QueryResourceImpl.ActionData>();
//        Map<String, String> varValueMap = new HashMap<String, String>();
//        Map<String, String> varRegexMap = new HashMap<String, String>();
        
        for( Entry<String, String[]> entry : queryParams.entrySet() ) {
            String orig_param = entry.getKey();
            if( paginationParams.contains(orig_param) ) {
                continue;
            }
            String[] paramParts = orig_param.split("_");
            String param = paramParts[0];
            if( paramParts.length >= 2 ) {
                /**
                
                // TODO: finish "variableValue" functionality, in both audit query backend and here
                if( "var".equals(paramParts[0]) || "varre".equals(paramParts[0]) || "vr".equals(paramParts[0]) ) {
                    // setup var value
                    String [] values = queryParams.get(orig_param);
                    if( values.length > 1 ) { 
                        throw KieRemoteRestOperationException.badRequest("Only one value per variable parameter: '" + orig_param + "'"); 
                    }
                    String value = values[0];
                    
                    // setup var name
                    String name = null;
                    StringBuilder nameBuilder = new StringBuilder(paramParts[1]);
                    for( int i = 2; i < paramParts.length; ++i ) { 
                        nameBuilder.append("_").append(paramParts[i]);
                    }
                    name = nameBuilder.toString();

                    // add info to var maps
                    if( "var".equals(paramParts[0]) ) { 
                        varValueMap.put(name, value);
                    } else { 
                        varRegexMap.put(name, value);
                    }

                    // add info to action maps
                    Integer queryAction = paramNameActionMap.get("varvalue");
                    String [] varNames = actionDataMap.get(queryAction);
                    if( varNames == null ) { 
                       varNames = new String[1];
                       varNames[0] = name;
                    } else { 
                       String [] newVarNames = new String[varNames.length+1]; 
                       System.arraycopy(varNames, 0, newVarNames, 0, varNames.length);
                       newVarNames[varNames.length] = name;
                       varNames = newVarNames;
                    }
                    actionDataMap.put(queryAction, varNames );
                    continue;
                }
                */

                if( paramParts.length > 2 ) {
                    throw KieRemoteRestOperationException.badRequest("Query parameter '" + orig_param + "' is not supported.");
                }
                
                Integer action = paramNameActionMap.get(paramParts[0]);
                if( action == null ) { 
                    throw KieRemoteRestOperationException.badRequest("Query parameter '" + orig_param + "' is not supported.");
                }
                ActionData actionData = new ActionData(orig_param, action, entry.getValue());
                actionDataQueue.add(actionData);
                if( "min".equals(paramParts[1]) ) {
                    actionData.min = true;
                } else if( "max".equals(paramParts[1]) ) {
                    actionData.max = true;
                } else if( "re".equals(paramParts[1]) ) {
                    actionData.regex = true;
                } else {
                    throw KieRemoteRestOperationException.badRequest("Query parameter '" + orig_param + "' is not supported.");
                }
            } else { 
                Integer action = paramNameActionMap.get(param);
                if( action != null ) {
                    actionDataQueue.add(new ActionData(orig_param, action, entry.getValue()));
                } else {
                    throw KieRemoteRestOperationException.badRequest("Query parameter '" + orig_param + "' is not supported.");
                }
            }
        }

        // meta (in-memory or history variable values?)
        Boolean workFlowInstanceVariables = null;
        if( queryParams.containsKey(metaRuntimeParams[0]) || queryParams.containsKey(metaRuntimeParamsShort[0]) ) { 
           workFlowInstanceVariables = true; 
           queryParams.remove(metaRuntimeParams[0]); 
           queryParams.remove(metaRuntimeParamsShort[0]);
        }
        if( queryParams.containsKey(metaRuntimeParams[1]) || queryParams.containsKey(metaRuntimeParamsShort[1]) ) { 
            if( workFlowInstanceVariables ) { 
                throw KieRemoteRestOperationException.badRequest("Only one of the 'memory' and 'history' query parameters may be specified.");
            }
            workFlowInstanceVariables = false;
            queryParams.remove(metaRuntimeParams[1]); 
            queryParams.remove(metaRuntimeParamsShort[1]);
        }
        if( workFlowInstanceVariables == null ) { 
            workFlowInstanceVariables = false;
        }
        
        // setup queries
        RemoteServicesQueryCommandBuilder taskQueryBuilder = new RemoteServicesQueryCommandBuilder(identityProvider.getName());
        RemoteServicesQueryCommandBuilder varInstLogQueryBuilder = new RemoteServicesQueryCommandBuilder();
        
        Map<String, String> procVarValues;
        if( workFlowInstanceVariables ) { 
            procVarValues = new HashMap<String, String>();
        } 

        varInstLogQueryBuilder.last(); // TODO: only call this if there are other calls to the var log query builder!

        int[] intData;
        long[] longData;
        Date[] dateData;
        while( !actionDataQueue.isEmpty() ) {
            ActionData actionData = actionDataQueue.poll();
            String[] data = actionData.data;
            int action = actionData.action;
            switch ( action ) {
            // general
            case 0: // processinstanceid
                assert "processinstanceid".equals(actionParamNameMap.get(action)) : action +  " : processinstanceid";
                longData = getLongs(action, data);
                taskQueryBuilder.processInstanceId(longData);
                varInstLogQueryBuilder.processInstanceId(longData);
                break;
            case 1: // processid
                assert "processid".equals(actionParamNameMap.get(action)) : action + " : processid";
                setRegexOnOff(actionData, true, taskQueryBuilder, varInstLogQueryBuilder);
                taskQueryBuilder.processId(data);
                varInstLogQueryBuilder.processId(data);
                setRegexOnOff(actionData, false, taskQueryBuilder, varInstLogQueryBuilder);
                break;
            case 2: // workitemid
                assert "workitemid".equals(actionParamNameMap.get(action)) : action + " : workitemid";
                longData = getLongs(action, data);
                taskQueryBuilder.workItemId(longData);
                varInstLogQueryBuilder.workItemId(longData);
                break;
            case 3: // deploymentid
                assert "deploymentid".equals(actionParamNameMap.get(action)) : action + " : deploymentid";
                taskQueryBuilder.deploymentId(data);
                varInstLogQueryBuilder.deploymentId(data);
                break;

            // task
            case 4: // task id
                assert "taskid".equals(actionParamNameMap.get(action)) : action + " : taskid";
                longData = getLongs(action, data);
                taskQueryBuilder.taskId(longData);
                varInstLogQueryBuilder.taskId(longData);
                break;
            case 5: // initiator
                assert "initiator".equals(actionParamNameMap.get(action)) : action + " : initiator";
                setRegexOnOff(actionData, true, taskQueryBuilder, varInstLogQueryBuilder);
                taskQueryBuilder.initiator(data);
                varInstLogQueryBuilder.initiator(data);
                setRegexOnOff(actionData, false, taskQueryBuilder, varInstLogQueryBuilder);
                break;
            case 6: // stakeholder
                assert "stakeholder".equals(actionParamNameMap.get(action)) : action + " : stakeholder";
                setRegexOnOff(actionData, true, taskQueryBuilder, varInstLogQueryBuilder);
                taskQueryBuilder.stakeHolder(data);
                varInstLogQueryBuilder.stakeHolder(data);
                setRegexOnOff(actionData, false, taskQueryBuilder, varInstLogQueryBuilder);
                break;
            case 7: // potential owner
                assert "potentialowner".equals(actionParamNameMap.get(action)) : action + " : potentialowner";
                taskQueryBuilder.potentialOwner(data);
                varInstLogQueryBuilder.potentialOwner(data);
                break;
            case 8: // task owner
                assert "taskowner".equals(actionParamNameMap.get(action)) : action + " : taskowner";
                taskQueryBuilder.taskOwner(data);
                varInstLogQueryBuilder.taskOwner(data);
                break;
            case 9: // business admin
                assert "businessadmin".equals(actionParamNameMap.get(action)) : action + " : businessadmin";
                taskQueryBuilder.businessAdmin(data);
                varInstLogQueryBuilder.businessAdmin(data);
                break;
            case 10: // task status
                assert "taskstatus".equals(actionParamNameMap.get(action)) : action + " : taskstatus";
                Status[] statuses = getTaskStatuses(data);
                taskQueryBuilder.taskStatus(statuses);
                varInstLogQueryBuilder.taskStatus(statuses);
                break;

            // process instance
            case 11: // process instance status
                assert "processinstancestatus".equals(actionParamNameMap.get(action)) : action + " : processinstancestatus";
                intData = getInts(action, data);
                taskQueryBuilder.processInstanceStatus(intData);
                varInstLogQueryBuilder.processInstanceStatus(intData);
                break;
            case 12: // process version
                assert "processversion".equals(actionParamNameMap.get(action)) : action + " : processversion";
                setRegexOnOff(actionData, true, taskQueryBuilder, varInstLogQueryBuilder);
                taskQueryBuilder.processVersion(data);
                varInstLogQueryBuilder.processVersion(data);
                setRegexOnOff(actionData, false, taskQueryBuilder, varInstLogQueryBuilder);
                break;
            case 13: // start date
                assert "startdate".equals(actionParamNameMap.get(action)) : action + " : startdate";
                dateData = getDates(action, data);
                if( actionData.min || actionData.max ) {
                    if( dateData.length != 1 ) {
                        throw KieRemoteRestOperationException.notFound("Only 1 '" + actionData.param + "' parameter is accepted");
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
                assert "enddate".equals(actionParamNameMap.get(action)) : action + " : enddate";
                dateData = getDates(action, data);
                if( actionData.min || actionData.max ) {
                    if( dateData.length > 1 ) {
                        throw KieRemoteRestOperationException.notFound("Only 1 '" + actionData.param + "' parameter is accepted");
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
                assert "varid".equals(actionParamNameMap.get(action)) : action + " : varid";
                if( actionData.regex && workFlowInstanceVariables ) { 
                    String param = actionParamNameMap.get(action);
                    throw KieRemoteRestOperationException.badRequest("Regular expresssions are not supported on the '" + param + "' parameter "
                            + "when retrieving in-memory process variables");
                }
                setRegexOnOff(actionData, true, taskQueryBuilder, varInstLogQueryBuilder);
                taskQueryBuilder.variableId(data);
                varInstLogQueryBuilder.last().variableId(data);
                setRegexOnOff(actionData, false, taskQueryBuilder, varInstLogQueryBuilder);
                break;
            case 16: // var value
                assert "varvalue".equals(actionParamNameMap.get(action)) : action + " : varvalue";
                if( actionData.regex && workFlowInstanceVariables ) { 
                    String param = actionParamNameMap.get(action);
                    throw KieRemoteRestOperationException.badRequest("Regular expresssions are not supported on the '" + param + "' parameter "
                            + "when retrieving in-memory process variables");
                }
                setRegexOnOff(actionData, true, taskQueryBuilder, varInstLogQueryBuilder);
                taskQueryBuilder.value(data);
//                if( ! workFlowInstanceVariables ) { 
                    varInstLogQueryBuilder.value(data);
//                }
//                else { 
//                    varValues.put(name, value);
//                }
                setRegexOnOff(actionData, false, taskQueryBuilder, varInstLogQueryBuilder);
                break;

            default:
                throw KieRemoteRestOperationException
                        .internalServerError("Please contact the developers: this state should not be possible.");
            }
            if( actionData.min || actionData.max || actionData.regex ) {
                throw KieRemoteRestOperationException.notFound("Query parameter '" + actionData.param + "' is not supported.");
            }
        }

        TaskQueryDataCommand taskCmd = taskQueryBuilder.createTaskQueryDataCommand();
        
        List<TaskSummary> taskSummaries = doRestTaskOperation(taskCmd);
        QueryData varLogQueryData = varInstLogQueryBuilder.getQueryData();
        List<VariableInstanceLog> varLogs 
            = getAuditLogService().queryVariableInstanceLogs(varLogQueryData);
        List<JaxbVariableInfo> procVars = null;
        if( workFlowInstanceVariables ) { 
            for( VariableInstanceLog varLog : varLogs ) { 
                
            }
        }
        
        JaxbQueryTaskResult result = createQueryTaskResult(taskSummaries, varLogs, procVars);
        return result;
    }

    private static void setRegexOnOff( ActionData actionData, boolean on, RemoteServicesQueryCommandBuilder... queryCmdBuilders) {
        if( actionData.regex ) {
            if( on ) {
                for( RemoteServicesQueryCommandBuilder queryCmdBuilder : queryCmdBuilders ) { 
                    queryCmdBuilder.like();
                }
            } else {
                for( RemoteServicesQueryCommandBuilder queryCmdBuilder : queryCmdBuilders ) { 
                    queryCmdBuilder.equals();
                }
                actionData.regex = false;
            }
        }
    }
    
    private static JaxbQueryTaskResult createQueryTaskResult(List<TaskSummary> taskSummaries, List<VariableInstanceLog> varLogs, 
            List<JaxbVariableInfo> processVariables) { 
        JaxbQueryTaskResult result = new JaxbQueryTaskResult();
        
        Map<Long, JaxbQueryTaskInfo> procInstIdTaskInfoMap = new HashMap<Long, JaxbQueryTaskInfo>();
        for( TaskSummary taskSum : taskSummaries ) { 
            long procInstId = taskSum.getProcessInstanceId();
            JaxbQueryTaskInfo taskInfo = getQueryTaskInfo(procInstId, procInstIdTaskInfoMap);
            taskInfo.getTaskSummaries().add(new JaxbTaskSummary(taskSum));
        }
        for( VariableInstanceLog varLog : varLogs ) { 
            long procInstId = varLog.getProcessInstanceId();
            JaxbQueryTaskInfo taskInfo = getQueryTaskInfo(procInstId, procInstIdTaskInfoMap);
            taskInfo.getVariables().add(new JaxbVariableInfo(varLog));
        }
        
        result.getTaskInfoList().addAll(procInstIdTaskInfoMap.values());
        return result;
    }

    private static JaxbQueryProcessInstanceInfo getQueryProcessInstanceInfo(long procInstId, Map<Long, JaxbQueryProcessInstanceInfo> procInstIdProcInstInfoMap) { 
        JaxbQueryProcessInstanceInfo procInstInfo = procInstIdProcInstInfoMap.get(procInstId);
        if( procInstInfo == null ) { 
            procInstInfo = new JaxbQueryProcessInstanceInfo();
            procInstIdProcInstInfoMap.put(procInstId, procInstInfo);
        }
        return procInstInfo;
    }
    
    private static JaxbQueryTaskInfo getQueryTaskInfo(long procInstId, Map<Long, JaxbQueryTaskInfo> procInstIdTaskInfoMap) { 
        JaxbQueryTaskInfo taskInfo = procInstIdTaskInfoMap.get(procInstId);
        if( taskInfo == null ) { 
            taskInfo = new JaxbQueryTaskInfo(procInstId);
            procInstIdTaskInfoMap.put(procInstId, taskInfo);
        }
        return taskInfo;
    }
   
    // pkg scope for testing
    JaxbQueryProcessInstanceResult queryProcessInstancesAndVariables( Map<String, String[]> queryParams, int[] pageInfo, int maxResults ) {

        ArrayDeque<ActionData> actionDataQueue = new ArrayDeque<QueryResourceImpl.ActionData>();
//        Map<String, String> varValueMap = new HashMap<String, String>();
//        Map<String, String> varRegexMap = new HashMap<String, String>();
        
        for( Entry<String, String[]> entry : queryParams.entrySet() ) {
            String orig_param = entry.getKey();
            if( paginationParams.contains(orig_param) ) {
                continue;
            }
            String[] paramParts = orig_param.split("_");
            String param = paramParts[0];
            if( paramParts.length >= 2 ) {

                if( paramParts.length > 2 ) {
                    throw KieRemoteRestOperationException.badRequest("Query parameter '" + orig_param + "' is not supported.");
                }
                
                Integer action = paramNameActionMap.get(paramParts[0]);
                if( action == null ) { 
                    throw KieRemoteRestOperationException.badRequest("Query parameter '" + orig_param + "' is not supported.");
                }
                ActionData actionData = new ActionData(orig_param, action, entry.getValue());
                actionDataQueue.add(actionData);
                if( "min".equals(paramParts[1]) ) {
                    actionData.min = true;
                } else if( "max".equals(paramParts[1]) ) {
                    actionData.max = true;
                } else if( "re".equals(paramParts[1]) ) {
                    actionData.regex = true;
                } else {
                    throw KieRemoteRestOperationException.badRequest("Query parameter '" + orig_param + "' is not supported.");
                }
            } else { 
                Integer action = paramNameActionMap.get(param);
                if( action != null ) {
                    actionDataQueue.add(new ActionData(orig_param, action, entry.getValue()));
                } else {
                    throw KieRemoteRestOperationException.badRequest("Query parameter '" + orig_param + "' is not supported.");
                }
            }
        }

        // meta (in-memory or history variable values?)
        Boolean workFlowInstanceVariables = null;
        if( queryParams.containsKey(metaRuntimeParams[0]) || queryParams.containsKey(metaRuntimeParamsShort[0]) ) { 
           workFlowInstanceVariables = true; 
           queryParams.remove(metaRuntimeParams[0]); 
           queryParams.remove(metaRuntimeParamsShort[0]);
        }
        if( queryParams.containsKey(metaRuntimeParams[1]) || queryParams.containsKey(metaRuntimeParamsShort[1]) ) { 
            if( workFlowInstanceVariables ) { 
                throw KieRemoteRestOperationException.badRequest("Only one of the 'memory' and 'history' query parameters may be specified.");
            }
            workFlowInstanceVariables = false;
            queryParams.remove(metaRuntimeParams[1]); 
            queryParams.remove(metaRuntimeParamsShort[1]);
        }
        if( workFlowInstanceVariables == null ) { 
            workFlowInstanceVariables = false;
        }
        
        // setup queries
        RemoteServicesQueryCommandBuilder varInstLogQueryBuilder = new RemoteServicesQueryCommandBuilder();
        
        Map<String, String> procVarValues;
        if( workFlowInstanceVariables ) { 
            procVarValues = new HashMap<String, String>();
        } 

        varInstLogQueryBuilder.last(); // TODO: only call this if there are other calls to the var log query builder!

        int[] intData;
        long[] longData;
        Date[] dateData;
        while( !actionDataQueue.isEmpty() ) {
            ActionData actionData = actionDataQueue.poll();
            String[] data = actionData.data;
            int action = actionData.action;
            switch ( action ) {
            // general
            case 0: // processinstanceid
                assert "processinstanceid".equals(actionParamNameMap.get(action)) : action +  " : processinstanceid";
                longData = getLongs(action, data);
                varInstLogQueryBuilder.processInstanceId(longData);
                break;
            case 1: // processid
                assert "processid".equals(actionParamNameMap.get(action)) : action + " : processid";
                setRegexOnOff(actionData, true, varInstLogQueryBuilder);
                varInstLogQueryBuilder.processId(data);
                setRegexOnOff(actionData, false, varInstLogQueryBuilder);
                break;
            case 2: // workitemid
                assert "workitemid".equals(actionParamNameMap.get(action)) : action + " : workitemid";
                longData = getLongs(action, data);
                varInstLogQueryBuilder.workItemId(longData);
                break;
            case 3: // deploymentid
                assert "deploymentid".equals(actionParamNameMap.get(action)) : action + " : deploymentid";
                varInstLogQueryBuilder.deploymentId(data);
                break;

            // process instance
            case 11: // process instance status
                assert "processinstancestatus".equals(actionParamNameMap.get(action)) : action + " : processinstancestatus";
                intData = getInts(action, data);
                varInstLogQueryBuilder.processInstanceStatus(intData);
                break;
            case 12: // process version
                assert "processversion".equals(actionParamNameMap.get(action)) : action + " : processversion";
                setRegexOnOff(actionData, true, varInstLogQueryBuilder);
                varInstLogQueryBuilder.processVersion(data);
                setRegexOnOff(actionData, false, varInstLogQueryBuilder);
                break;
            case 13: // start date
                assert "startdate".equals(actionParamNameMap.get(action)) : action + " : startdate";
                dateData = getDates(action, data);
                if( actionData.min || actionData.max ) {
                    if( dateData.length != 1 ) {
                        throw KieRemoteRestOperationException.notFound("Only 1 '" + actionData.param + "' parameter is accepted");
                    }
                    if( actionData.min ) {
                        varInstLogQueryBuilder.startDateMin(dateData[0]);
                        actionData.min = false;
                    } else if( actionData.max ) {
                        varInstLogQueryBuilder.startDateMax(dateData[0]);
                        actionData.max = false;
                    }
                } else {
                    varInstLogQueryBuilder.startDate(dateData);
                }
                break;
            case 14: // end date
                assert "enddate".equals(actionParamNameMap.get(action)) : action + " : enddate";
                dateData = getDates(action, data);
                if( actionData.min || actionData.max ) {
                    if( dateData.length > 1 ) {
                        throw KieRemoteRestOperationException.notFound("Only 1 '" + actionData.param + "' parameter is accepted");
                    }
                    if( actionData.min ) {
                        varInstLogQueryBuilder.endDateMin(dateData[0]);
                        actionData.min = false;
                    } else if( actionData.max ) {
                        varInstLogQueryBuilder.endDateMax(dateData[0]);
                        actionData.max = false;
                    }
                } else {
                    varInstLogQueryBuilder.startDate(dateData);
                }
                break;

            // variable instance
            case 15: // var id
                assert "varid".equals(actionParamNameMap.get(action)) : action + " : varid";
                if( actionData.regex && workFlowInstanceVariables ) { 
                    String param = actionParamNameMap.get(action);
                    throw KieRemoteRestOperationException.badRequest("Regular expresssions are not supported on the '" + param + "' parameter "
                            + "when retrieving in-memory process variables");
                }
                setRegexOnOff(actionData, true, varInstLogQueryBuilder);
                varInstLogQueryBuilder.variableId(data);
                setRegexOnOff(actionData, false, varInstLogQueryBuilder);
                varInstLogQueryBuilder.last();
                break;
            case 16: // var value
                assert "varvalue".equals(actionParamNameMap.get(action)) : action + " : varvalue";
                if( actionData.regex && workFlowInstanceVariables ) { 
                    String param = actionParamNameMap.get(action);
                    throw KieRemoteRestOperationException.badRequest("Regular expresssions are not supported on the '" + param + "' parameter "
                            + "when retrieving in-memory process variables");
                }
                setRegexOnOff(actionData, true, varInstLogQueryBuilder);
//                if( ! workFlowInstanceVariables ) { 
                    varInstLogQueryBuilder.value(data);
//                }
//                else { 
//                    varValues.put(name, value);
//                }
                setRegexOnOff(actionData, false, varInstLogQueryBuilder);
                break;

            default:
                throw KieRemoteRestOperationException
                        .internalServerError("Please contact the developers: this state should not be possible.");
            }
            if( actionData.min || actionData.max || actionData.regex ) {
                throw KieRemoteRestOperationException.notFound("Query parameter '" + actionData.param + "' is not supported.");
            }
        }

        QueryData queryData = varInstLogQueryBuilder.getQueryData();
        List<VariableInstanceLog> varLogs = getAuditLogService().queryVariableInstanceLogs(queryData);
        List<ProcessInstanceLog> procLogs = getAuditLogService().queryProcessInstanceLogs(queryData);
        List<JaxbVariableInfo> procVars = null;
        
        JaxbQueryProcessInstanceResult result = createProcessInstanceResult(procLogs, varLogs, procVars);
        return result;
    }

    private static JaxbQueryProcessInstanceResult createProcessInstanceResult(List<ProcessInstanceLog> procLogs, List<VariableInstanceLog> varLogs, 
            List<JaxbVariableInfo> processVariables) { 
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
    
    private static final String[] allowedQueryParams = { "workItemId",             // 0
            "taskId",                 // 1
            "businessAdministrator",  // 2
            "potentialOwner",         // 3
            "status",                 // 4
            "taskOwner",              // 5
            "processInstanceId",      // 6
            "language",               // 7
            "union"                   // 8
    };


    @GET
    @Path("/task")
    public Response taskSummaryQuery() {
        Map<String, String[]> params = getRequestParams();
        String oper = getRelativePath();

        for( String queryParam : params.keySet() ) {
            boolean allowed = false;
            for( String allowedParam : allowedQueryParams ) {
                if( allowedParam.equalsIgnoreCase(queryParam) || paginationParams.contains(queryParam) ) {
                    allowed = true;
                    break;
                }
            }
            if( !allowed ) {
                throw KieRemoteRestOperationException.badRequest(queryParam
                        + " is an unknown and unsupported query param for the task query operation.");
            }
        }

        List<Long> workItemIds = getLongListParam(allowedQueryParams[0], false, params, "query", true);
        List<Long> taskIds = getLongListParam(allowedQueryParams[1], false, params, "query", true);
        List<Long> procInstIds = getLongListParam(allowedQueryParams[6], false, params, "query", true);
        List<String> busAdmins = getStringListParamAsList(allowedQueryParams[2], false, params, "query");
        List<String> potOwners = getStringListParamAsList(allowedQueryParams[3], false, params, "query");
        List<String> taskOwners = getStringListParamAsList(allowedQueryParams[5], false, params, "query");
        List<String> language = getStringListParamAsList(allowedQueryParams[7], false, params, "query");

        String unionStr = getStringParam(allowedQueryParams[8], false, params, "query");
        boolean union = Boolean.parseBoolean(unionStr); // null, etc == false

        List<String> statusStrList = getStringListParamAsList(allowedQueryParams[4], false, params, "query");
        List<Status> statuses = convertStringListToStatusList(statusStrList);

        int[] pageInfo = getPageNumAndPageSize(params, oper);
        int maxResults = getMaxNumResultsNeeded(pageInfo);
        GetTasksByVariousFieldsCommand queryCmd = new GetTasksByVariousFieldsCommand(workItemIds, taskIds, procInstIds, busAdmins, potOwners,
                taskOwners, statuses, language, union, maxResults);
        queryCmd.setUserId(identityProvider.getName());

        List<TaskSummary> results = doRestTaskOperationWithTaskId((Long) null, queryCmd);

        logger.debug("{} results found.", results.size());
        JaxbTaskSummaryListResponse resultList = paginateAndCreateResult(pageInfo, results, new JaxbTaskSummaryListResponse());
        logger.debug("Returning {} results after pagination.", resultList.getList().size());

        return createCorrectVariant(resultList, headers);
    }
}
