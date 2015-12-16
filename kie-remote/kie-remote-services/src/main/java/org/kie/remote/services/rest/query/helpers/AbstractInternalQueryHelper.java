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

package org.kie.remote.services.rest.query.helpers;

import static org.kie.remote.services.rest.ResourceBase.*;
import static org.kie.internal.query.QueryParameterIdentifiers.OLD_VALUE_LIST;
import static org.kie.internal.query.QueryParameterIdentifiers.VALUE_LIST;
import static org.kie.internal.query.QueryParameterIdentifiers.VARIABLE_ID_LIST;
import static org.kie.internal.query.QueryParameterIdentifiers.VARIABLE_INSTANCE_ID_LIST;
import static org.kie.internal.query.QueryParameterIdentifiers.VAR_VALUE_ID_LIST;
import static org.kie.remote.services.rest.query.data.QueryResourceData.actionParamNameMap;
import static org.kie.remote.services.rest.query.data.QueryResourceData.getDates;
import static org.kie.remote.services.rest.query.data.QueryResourceData.getInts;
import static org.kie.remote.services.rest.query.data.QueryResourceData.getLongs;
import static org.kie.remote.services.rest.query.data.QueryResourceData.getTaskStatuses;
import static org.kie.remote.services.rest.query.data.QueryResourceData.metaRuntimeParams;
import static org.kie.remote.services.rest.query.data.QueryResourceData.metaRuntimeParamsShort;
import static org.kie.remote.services.rest.query.data.QueryResourceData.paramNameActionMap;
import static org.kie.remote.services.rest.query.data.QueryResourceData.varInstQueryParams;
import static org.kie.remote.services.rest.query.data.QueryResourceData.varInstQueryParamsShort;

import java.util.ArrayDeque;
import java.util.Date;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Set;

import org.jbpm.query.jpa.data.QueryCriteria;
import org.jbpm.query.jpa.data.QueryWhere;
import org.kie.api.task.model.Status;
import org.kie.internal.query.data.QueryData;
import org.kie.remote.services.rest.ResourceBase;
import org.kie.remote.services.rest.exception.KieRemoteRestOperationException;
import org.kie.remote.services.rest.query.RemoteServicesQueryCommandBuilder;
import org.kie.remote.services.rest.query.data.QueryAction;
import org.kie.services.client.serialization.jaxb.impl.query.JaxbQueryProcessInstanceResult;
import org.kie.services.client.serialization.jaxb.impl.query.JaxbQueryTaskResult;

abstract class AbstractInternalQueryHelper<R> extends InternalQueryBuilderMethods {

    protected ResourceBase resourceBase;

    public AbstractInternalQueryHelper(ResourceBase resourceBase) {
       this.resourceBase = resourceBase;
    }

    public void dispose() {
        this.resourceBase = null;
    }

    /**
     * Create the {@link RemoteServicesQueryCommandBuilder} instances used by the particular Query Helper implementation.
     * @param identity The identity of the user doing the REST call, needed when querying Tasks
     */
    protected abstract void createAndSetQueryBuilders(String identity);

    /**
     * Calculates and returns the offset and max results information to be used in the query.
     * @param pageInfo Pagination information ([0] is page number, [1] is page size)
     * @return a int array with the calculated offset and maximum number of results
     */
    protected static int getOffset(int [] pageInfo) {
        int offset = 0;
        if( pageInfo[0] == 0 && pageInfo[1] > 0 ) {
            pageInfo[0] = 1;
        }
        if( pageInfo[0] > 1 && pageInfo[1] > 0 ) {
            offset = (pageInfo[0]-1)*pageInfo[1];
        }
        return offset;
    }

    /**
     * <ol>
     * <li>Use the created {@link RemoteServicesQueryCommandBuilder} instances to execute the query via the backend.</li>
     * <li>Create and fill a result instance (a {@link JaxbQueryProcessInstanceResult} or {@link JaxbQueryTaskResult}) with the
     * information from the results of the queries
     *
     * @param onlyRetrieveLastVarLogs Whether to
     * @param workFlowInstanceVariables (UNFINISHED FEATURE) whether to use the information from variable instance logs
     *        or from the process instance variables themselves.
     * @param pageInfo pagination information
     * @return
     */
    protected abstract R doQueryAndCreateResultObjects(boolean onlyRetrieveLastVarLogs, boolean workFlowInstanceVariables, int [] pageInfo);

    /**
     * This method is the internal logic that
     * <ol>
     * <li>preprocesses the list of query parameters passed to the <code>../rest/query/runtime/{task,process}</code> operation</li>
     * <li>builds and executes a query to retrieved the requested information</li>
     * <li>converts the results to a {@link JaxbQueryTaskResult} or {@link JaxbQueryProcessInstanceResult} instance</li>
     * </ol>
     *
     * There are some design patterns here that future developers should be aware of: <ol>
     * <li>The meta-message format: We translate the REST query parameters to a list of {@link QueryAction} instances
     * to the actual {@link QueryData}, instead of directly translating REST query parameters to {@link QueryData} information. This
     * effectively decouples the REST query parameters from the query builders, giving us more flexibility/maintainability in the code.</li>
     * </ol>
     *
     * @param identity The identity of the caller, needed when doing task queries
     * @param queryParams The query parameters map returned by the JAX-RS logic
     * @param pageInfo The pagination information
     * @param maxResults The maximum number of results to be returned, determined by the pagination information
     * @return A {@link JaxbQueryTaskResult} or {@link JaxbQueryProcessInstanceResult} containing a list of entities containing task
     *         and process variable information
     */
    public R queryTaskOrProcInstAndAssociatedVariables( String identity, Map<String, String[]> queryParams, int[] pageInfo) {

        // UNFINISHED FEATURE: use in-memory/process instance variables
        // 1. meta (in-memory or history variable values?)
        boolean workFlowInstanceVariables = determineWorkFlowInstanceVariableUse(queryParams);

        // UNFINISHED FEATURE: use in-memory/process instance variables
        Map<String, String> procVarValues;
        if( workFlowInstanceVariables ) {
            procVarValues = new HashMap<String, String>();
        }

        // 0. Retrieve *all* variable log values, or just the most recent?
        boolean onlyRetrieveLastVarLogs = true;
        String [] paramVals = queryParams.remove("all");
        if( paramVals != null ) {
           onlyRetrieveLastVarLogs = false;
        }

        // Hold the information for parameters like "var_myVar=myVal" or "varregex_myVar=my*"
        Map<String, String> varValueMap = new HashMap<String, String>();
        Map<String, String> varRegexMap = new HashMap<String, String>();

        // 1. Create the query action queue, that is then processed to fill the query
        Queue<QueryAction> queryActionQueue = fillQueryActionQueueFromQueryParams(queryParams, varValueMap, varRegexMap);

        // 2. Setup query builders
        createAndSetQueryBuilders(identity);

        // 3. process the query action queue, thus creating the {@link QueryData} instance for the query builders
        processQueryActionQueue(queryActionQueue, varValueMap, varRegexMap, workFlowInstanceVariables);

        // 4. execute the query via the backend, which uses the creatd {@link QueryData} instance
        return doQueryAndCreateResultObjects(onlyRetrieveLastVarLogs, workFlowInstanceVariables, pageInfo);
    }

    public R queryTasksOrProcInstsAndVariables( Map<String, String[]> queryParams, int[] pageInfo) {
       return queryTaskOrProcInstAndAssociatedVariables(null, queryParams, pageInfo);
    }

    /**
     * Figure out whether or not we're getting audit variable logs or workflow instance variables.
     *
     * @param queryParams The map of (REST operation) query parameters
     * @return Whether or not to use workflow instance variables
     */
    protected boolean determineWorkFlowInstanceVariableUse(Map<String, String[]> queryParams) {
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

    /**
     * Parse the given query parameters to create an {@link ArrayDeque} of {@link QueryAction}.
     * </p>
     * The {@link Queue} of {@link QueryAction} is then acted upon by the {{@link #processQueryActionQueue(ArrayDeque, Map, Map, boolean)}
     * method, which applies the different query actions to build the query requested via the query builder instances.
     *
     * @param queryParams a {@link Map} of the received query parameters
     * @param varValueMap a {@link Map} that maps query variable names to the passed values
     * @param varRegexMap a {@link Map} that maps query variable names to the passed regexs for the specified variable
     * @return A {@link Queue} of {@link QueryAction} instances
     */
    protected Queue<QueryAction> fillQueryActionQueueFromQueryParams(
            Map<String, String[]> queryParams,
            Map<String, String> varValueMap,
            Map<String, String> varRegexMap) {

        ArrayDeque<QueryAction> queryActionQueue = new ArrayDeque<QueryAction>();

        // Go through all parameters and build a queue of {@link QueryAction) instances to be executed in the following loop
        for( Entry<String, String[]> entry : queryParams.entrySet() ) {
            String orig_param = entry.getKey();
            if( ResourceBase.paginationParams.contains(orig_param) ) {
                continue;
            }
            String[] paramParts = orig_param.split("_");
            String param = paramParts[0];
            if( paramParts.length >= 2 ) {

                if( varInstQueryParams[2].equals(paramParts[0])
                    || varInstQueryParams[3].equals(paramParts[0])
                    || varInstQueryParamsShort[3].equals(paramParts[0]) ) {
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
                    Integer queryActionInt = paramNameActionMap.get(paramParts[0]);
                    String [] data = { varName };
                    QueryAction queryAction = new QueryAction(orig_param, queryActionInt, data);
                    queryActionQueue.add(queryAction);

                    // IF:
                    // - the query parameter starts with 'var', then it's an 'equals' operation ("var_partid=23a")
                    // - the query parameter starts with 'varregex' or 'vr', then it's an regex operation ( "vr_partid=23*" )
                    if( varInstQueryParams[2].equals(paramParts[0]) ) {
                        varValueMap.put(varName, value);
                    } else {
                        varRegexMap.put(varName, value);
                        queryAction.regex = true;
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
                QueryAction queryAction = new QueryAction(orig_param, action, entry.getValue());
                queryActionQueue.add(queryAction);
                if( "min".equals(paramParts[1]) ) {
                    queryAction.min = true;
                } else if( "max".equals(paramParts[1]) ) {
                    queryAction.max = true;
                } else if( "re".equals(paramParts[1]) ) {
                    queryAction.regex = true;
                } else {
                    throw KieRemoteRestOperationException.badRequest("Query parameter '" + orig_param + "' is not supported.");
                }
            } else {
                Integer action = paramNameActionMap.get(param);
                if( action != null ) {
                    queryActionQueue.add(new QueryAction(orig_param, action, entry.getValue()));
                } else {
                    throw KieRemoteRestOperationException.badRequest("Query parameter '" + orig_param + "' is not supported.");
                }
            }
        }

        return queryActionQueue;
    }

    /**
     * If the {@link QueryAction} instance is a regex instance, this prepares the query builder instance
     * to process the next parameter is as a regular expression, a.k.a. regex. The method must be called
     * a second time to "de-activate" the regex interpretation by the query builder instance.
     *
     * @param queryAction The {@link QueryAction} instance with the actual parameter
     * @param on Whether regular expressions should be turned on or off for the query builders
     */
    private void setRegexOnOff( QueryAction queryAction, boolean on ) {
        if( queryAction.regex ) {
            if( on ) {
                for( RemoteServicesQueryCommandBuilder queryCmdBuilder : getQueryBuilders() ) {
                    queryCmdBuilder.like();
                }
            } else {
                for( RemoteServicesQueryCommandBuilder queryCmdBuilder : getQueryBuilders() ) {
                    queryCmdBuilder.equals();
                }
                queryAction.regex = false;
            }
        }
    }

    private static Set<String> VAR_QUERY_SPECIFIC_PARAMETER_LIST_IDS = new HashSet<String>();
    static {
       VAR_QUERY_SPECIFIC_PARAMETER_LIST_IDS.add(VARIABLE_ID_LIST);
       VAR_QUERY_SPECIFIC_PARAMETER_LIST_IDS.add(VARIABLE_INSTANCE_ID_LIST);
       VAR_QUERY_SPECIFIC_PARAMETER_LIST_IDS.add(VALUE_LIST);
       VAR_QUERY_SPECIFIC_PARAMETER_LIST_IDS.add(OLD_VALUE_LIST);
       VAR_QUERY_SPECIFIC_PARAMETER_LIST_IDS.add(VAR_VALUE_ID_LIST);
    }

    protected boolean variableCriteriaInQuery(List<QueryCriteria> criteriaList) {
        for( QueryCriteria criteria : criteriaList ) {
            if( criteria.isUnion() ) {
                return false;
            }
            if( criteria.isGroupCriteria() ) {
               if( variableCriteriaInQuery(criteria.getCriteria()) )  {
                   return true;
               }
               continue;
            }
            if( VAR_QUERY_SPECIFIC_PARAMETER_LIST_IDS.contains(criteria.getListId()) ) {
                 return true;
           }
        }
        return false;
     }

    /**
     * This is the main core of logic for the query helper classes.
     * </p>
     * Initially, in methods called before this method, we converted the (REST operation) query methods to a {@link Deque}
     * of {@link QueryAction} instances.
     * </p>
     * We're using a {@link Queue} instead of a {@link List} because I may eventually want to add new {@link QueryAction} elements
     * to the queue during processing: once I'm sure that I won't be doing that, I'll move this back to a {@link List} which is
     * more efficient and performant.
     * </p>
     * In this method, we go through each element (in the order that it was added) and translate the REST query parameter to a
     * action on the query builder(s) so that the requested query can be built an executed.
     *
     * @param queryActionQueue An ordered collection of {@link QueryAction} instances
     * @param varValueMap A {@link Map} mapping the variable name to the variable's value
     * @param varRegexMap A {@link Map} mapping the variable name to a regex for the variable's value
     * @param workFlowInstanceVariables Whether or not variable values should be retrieved from the audit logs or the process instance
     */
    protected void processQueryActionQueue(
            Queue<QueryAction> queryActionQueue,
            Map<String, String> varValueMap,
            Map<String, String> varRegexMap,
            boolean workFlowInstanceVariables
            ) {
        int[] intData;
        long[] longData;
        Date[] dateData;

        List<QueryAction> varValueQueryActions = new java.util.LinkedList<QueryAction>();

        while( !queryActionQueue.isEmpty() ) {
            QueryAction queryAction = queryActionQueue.poll();
            String[] data = queryAction.paramData;
            int action = queryAction.action;
            switch ( action ) {
            // general
            case 0: // processinstanceid
                assert "processinstanceid".equals(actionParamNameMap.get(action)): action + " : processinstanceid";
                longData = getLongs(action, data);
                if( queryAction.min || queryAction.max ) {
                    if( longData.length > 1 ) {
                        throw KieRemoteRestOperationException.notFound("Only 1 '" + queryAction.paramName
                                + "' parameter is accepted");
                    }
                    if( queryAction.min ) {
                        processInstanceIdMin(longData);
                        queryAction.min = false;
                    } else if( queryAction.max ) {
                        processInstanceIdMax(longData);
                        queryAction.max = false;
                    }
                } else {
                    processInstanceId(longData);
                }
                break;
            case 1: // processid
                assert "processid".equals(actionParamNameMap.get(action)): action + " : processid";
                setRegexOnOff(queryAction, true);
                processId(data);
                setRegexOnOff(queryAction, false);
                break;
            case 2: // deploymentid
                assert "deploymentid".equals(actionParamNameMap.get(action)): action + " : deploymentid";
                setRegexOnOff(queryAction, true);
                deploymentId(data);
                setRegexOnOff(queryAction, false);
                break;

            // task
            case 3: // task id
                assert "taskid".equals(actionParamNameMap.get(action)): action + " : taskid";
                longData = getLongs(action, data);
                if( queryAction.min || queryAction.max ) {
                    if( longData.length > 1 ) {
                        throw KieRemoteRestOperationException.notFound("Only 1 '" + queryAction.paramName
                                + "' parameter is accepted");
                    }
                    if( queryAction.min ) {
                        taskIdMin(longData[0]);
                        queryAction.min = false;
                    } else if( queryAction.max ) {
                        taskIdMax(longData[0]);
                        queryAction.max = false;
                    }
                } else {
                    taskId(longData);
                }
                break;
            case 4: // initiator
                assert "initiator".equals(actionParamNameMap.get(action)): action + " : initiator";
                setRegexOnOff(queryAction, true);
                initiator(data);
                setRegexOnOff(queryAction, false);
                break;
            case 5: // stakeholder
                assert "stakeholder".equals(actionParamNameMap.get(action)): action + " : stakeholder";
                setRegexOnOff(queryAction, true);
                stakeHolder(data);
                setRegexOnOff(queryAction, false);
                break;
            case 6: // potential owner
                assert "potentialowner".equals(actionParamNameMap.get(action)): action + " : potentialowner";
                setRegexOnOff(queryAction, true);
                potentialOwner(data);
                setRegexOnOff(queryAction, false);
                break;
            case 7: // task owner
                assert "taskowner".equals(actionParamNameMap.get(action)): action + " : taskowner";
                setRegexOnOff(queryAction, true);
                taskOwner(data);
                setRegexOnOff(queryAction, false);
                break;
            case 8: // business admin
                assert "businessadmin".equals(actionParamNameMap.get(action)): action + " : businessadmin";
                setRegexOnOff(queryAction, true);
                businessAdmin(data);
                setRegexOnOff(queryAction, false);
                break;
            case 9: // task status
                assert "taskstatus".equals(actionParamNameMap.get(action)): action + " : taskstatus";
                Status[] statuses = getTaskStatuses(data);
                taskStatus(statuses);
                break;

            // process instance
            case 10: // process instance status
                assert "processinstancestatus".equals(actionParamNameMap.get(action)): action + " : processinstancestatus";
                intData = getInts(action, data);
                processInstanceStatus(intData);
                break;
            case 11: // process version
                assert "processversion".equals(actionParamNameMap.get(action)): action + " : processversion";
                setRegexOnOff(queryAction, true);
                processVersion(data);
                setRegexOnOff(queryAction, false);
                break;
            case 12: // start date
                assert "startdate".equals(actionParamNameMap.get(action)): action + " : startdate";
                dateData = getDates(action, data);
                if( queryAction.min || queryAction.max ) {
                    if( dateData.length != 1 ) {
                        throw KieRemoteRestOperationException.notFound("Only 1 '" + queryAction.paramName
                                + "' parameter is accepted");
                    }
                    if( queryAction.min ) {
                        startDateMin(dateData[0]);
                        queryAction.min = false;
                    } else if( queryAction.max ) {
                        startDateMax(dateData[0]);
                        queryAction.max = false;
                    }
                } else {
                    startDate(dateData);
                }
                break;
            case 13: // end date
                assert "enddate".equals(actionParamNameMap.get(action)): action + " : enddate";
                dateData = getDates(action, data);
                if( queryAction.min || queryAction.max ) {
                    if( dateData.length > 1 ) {
                        throw KieRemoteRestOperationException.notFound("Only 1 '" + queryAction.paramName
                                + "' parameter is accepted");
                    }
                    if( queryAction.min ) {
                        endDateMin(dateData[0]);
                        queryAction.min = false;
                    } else if( queryAction.max ) {
                        endDateMax(dateData[0]);
                        queryAction.max = false;
                    }
                } else {
                    endDate(dateData);
                }
                break;

            // variable instance
            case 14: // var id
                assert "varid".equals(actionParamNameMap.get(action)): action + " : varid";
                if( queryAction.regex && workFlowInstanceVariables ) {
                    String param = actionParamNameMap.get(action);
                    throw KieRemoteRestOperationException.badRequest("Regular expresssions are not supported on the '" + param
                            + "' parameter " + "when retrieving in-memory process variables");
                }
                setRegexOnOff(queryAction, true);
                variableId(data);
                setRegexOnOff(queryAction, false);
                break;
            case 15: // var value
                assert "varvalue".equals(actionParamNameMap.get(action)): action + " : varvalue";
                if( queryAction.regex && workFlowInstanceVariables ) {
                    String param = actionParamNameMap.get(action);
                    throw KieRemoteRestOperationException.badRequest("Regular expresssions are not supported on the '" + param
                            + "' parameter " + "when retrieving in-memory process variables");
                }
                setRegexOnOff(queryAction, true);
                value(data);
                setRegexOnOff(queryAction, false);
                break;
            case 16: // var
                varValueQueryActions.add(queryAction);
                break;
            case 17: // varregex
                varValueQueryActions.add(queryAction);
                break;

            default:
                throw KieRemoteRestOperationException.internalServerError("Please contact the developers: state [" + action + "] should not be possible.");
            }
            if( (queryAction.min || queryAction.max || queryAction.regex) && action < 16 ) {
                throw KieRemoteRestOperationException.notFound("Query parameter '" + queryAction.paramName + "' is not supported.");
            }
        }

        if( ! varValueQueryActions.isEmpty() ) {

            // start a new criteria group, and make it disjunctive
            newGroup();
            or();

            for( QueryAction varQueryAction : varValueQueryActions ) {
                String[] data = varQueryAction.paramData;
                int action = varQueryAction.action;
                switch ( action ) {
                case 16: // var
                    assert "var".equals(actionParamNameMap.get(action)): action + " : var";
                    variableValue(data[0], varValueMap.get(data[0]));
                    break;
                case 17: // varregex
                    assert "varregex".equals(actionParamNameMap.get(action)): action + " : varregex";
                    setRegexOnOff(varQueryAction, true);
                    variableValue(data[0], varRegexMap.get(data[0]));
                    setRegexOnOff(varQueryAction, false);
                    break;

                default:
                    throw KieRemoteRestOperationException.internalServerError("Please contact the developers: state [" + action + "] should not be possible.");
                }
            }

            // close the criteria group, and reset it to an intersection
            endGroup();
            and();
        }
    }

}
