package org.kie.remote.services.rest.query;

import static org.kie.remote.services.rest.query.QueryResourceData.metaRuntimeParams;
import static org.kie.remote.services.rest.query.QueryResourceData.metaRuntimeParamsShort;
import static org.kie.remote.services.rest.query.QueryResourceData.paramNameActionMap;

import java.util.ArrayDeque;
import java.util.Map;
import java.util.Map.Entry;

import org.kie.remote.services.rest.ResourceBase;
import org.kie.remote.services.rest.exception.KieRemoteRestOperationException;

public abstract class AbstractInternalQueryHelper {

    protected ResourceBase resourceBase;
    
    public AbstractInternalQueryHelper(ResourceBase resourceBase) { 
       this.resourceBase = resourceBase;
    }
   
    public void dispose() { 
        this.resourceBase = null;
    }
    
    protected static void setRegexOnOff( ActionData actionData, boolean on, RemoteServicesQueryCommandBuilder... queryCmdBuilders ) {
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
    
    protected ArrayDeque<ActionData> fillActionDataQueueFromQueryParams( Map<String, String[]> queryParams,
            Map<String, String> varValueMap, Map<String, String> varRegexMap) { 
        
        ArrayDeque<ActionData> actionDataQueue = new ArrayDeque<ActionData>();
        
        // Go through all parameters and build a queue of actions (ActionData instances) to be executed in the following loop
        for( Entry<String, String[]> entry : queryParams.entrySet() ) {
            String orig_param = entry.getKey();
            if( ResourceBase.paginationParams.contains(orig_param) ) {
                continue;
            }
            String[] paramParts = orig_param.split("_");
            String param = paramParts[0];
            if( paramParts.length >= 2 ) {
               
                if( "var".equals(paramParts[0]) || "varregex".equals(paramParts[0]) || "vr".equals(paramParts[0]) ) {
                    // check that variable parameter has only been submitted once as query parameter
                    String[] values = queryParams.get(orig_param);
                    if( values.length > 1 ) {
                        throw KieRemoteRestOperationException.badRequest("Only one value per variable parameter: '" + orig_param + "'");
                    }
                    String value = values[0];

                    // The variable may contain a "_": compensate for that by readding the rest of the split parts
                    String varName = null;
                    StringBuilder nameBuilder = new StringBuilder(paramParts[1]);
                    for( int i = 2; i < paramParts.length; ++i ) {
                        nameBuilder.append("_").append(paramParts[i]);
                    }
                    varName = nameBuilder.toString();

                    // add action data to queue
                    Integer queryAction = paramNameActionMap.get(paramParts[0]);
                    String [] data = { varName };
                    ActionData actionData = new ActionData(orig_param, queryAction, data);
                    actionDataQueue.add(actionData);
                    
                    // IF: 
                    // - the query parameter starts with 'var', then it's an 'equals' operation ("var_partid=23a")
                    // - the query parameter starts with 'varre' or 'vr', then it's an regex operation ( "vr_partid=23*" )
                    if( "var".equals(paramParts[0]) ) {
                        varValueMap.put(varName, value);
                    } else {
                        varRegexMap.put(varName, value);
                        actionData.regex = true;
                    }
                    continue;
                }

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

        return actionDataQueue;
    }
    
    protected Boolean determineWorkFlowInstanceVariableUse(Map<String, String[]> queryParams) { 
        Boolean workFlowInstanceVariables = null;
        if( queryParams.containsKey(metaRuntimeParams[0]) || queryParams.containsKey(metaRuntimeParamsShort[0]) ) {
            workFlowInstanceVariables = true;
            queryParams.remove(metaRuntimeParams[0]);
            queryParams.remove(metaRuntimeParamsShort[0]);
        }
        if( queryParams.containsKey(metaRuntimeParams[1]) || queryParams.containsKey(metaRuntimeParamsShort[1]) ) {
            if( workFlowInstanceVariables != null ) {
                throw KieRemoteRestOperationException
                        .badRequest("Only one of the 'memory' and 'history' query parameters may be specified.");
            }
            workFlowInstanceVariables = false;
            queryParams.remove(metaRuntimeParams[1]);
            queryParams.remove(metaRuntimeParamsShort[1]);
        }
        if( workFlowInstanceVariables == null ) {
            workFlowInstanceVariables = false;
        }
        return workFlowInstanceVariables;
    }
}
