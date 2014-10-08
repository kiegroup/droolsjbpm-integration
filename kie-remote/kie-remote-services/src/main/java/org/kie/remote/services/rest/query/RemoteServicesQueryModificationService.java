package org.kie.remote.services.rest.query;

import static org.kie.internal.query.QueryParameterIdentifiers.ACTUAL_OWNER_ID_LIST;
import static org.kie.internal.query.QueryParameterIdentifiers.BUSINESS_ADMIN_ID_LIST;
import static org.kie.internal.query.QueryParameterIdentifiers.CREATED_BY_LIST;
import static org.kie.internal.query.QueryParameterIdentifiers.END_DATE_LIST;
import static org.kie.internal.query.QueryParameterIdentifiers.EXTERNAL_ID_LIST;
import static org.kie.internal.query.QueryParameterIdentifiers.LAST_VARIABLE_LIST;
import static org.kie.internal.query.QueryParameterIdentifiers.OLD_VALUE_LIST;
import static org.kie.internal.query.QueryParameterIdentifiers.POTENTIAL_OWNER_ID_LIST;
import static org.kie.internal.query.QueryParameterIdentifiers.PROCESS_INSTANCE_STATUS_LIST;
import static org.kie.internal.query.QueryParameterIdentifiers.PROCESS_VERSION_LIST;
import static org.kie.internal.query.QueryParameterIdentifiers.STAKEHOLDER_ID_LIST;
import static org.kie.internal.query.QueryParameterIdentifiers.START_DATE_LIST;
import static org.kie.internal.query.QueryParameterIdentifiers.TASK_ID_LIST;
import static org.kie.internal.query.QueryParameterIdentifiers.TASK_STATUS_LIST;
import static org.kie.internal.query.QueryParameterIdentifiers.VALUE_LIST;
import static org.kie.internal.query.QueryParameterIdentifiers.VARIABLE_ID_LIST;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.jbpm.process.audit.JPAAuditLogService;
import org.jbpm.services.task.impl.TaskQueryServiceImpl;
import org.kie.internal.query.QueryAndParameterAppender;
import org.kie.internal.query.QueryModificationService;
import org.kie.internal.query.data.QueryData;

public class RemoteServicesQueryModificationService implements QueryModificationService {

    public RemoteServicesQueryModificationService() {
        // default constructor
    }

    private static Map<String, Class<?>> taskCriteriaFieldClasses = TaskQueryServiceImpl.criteriaFieldClasses;
    private static Map<String, String> taskCriteriaFields = TaskQueryServiceImpl.criteriaFields;
    private static Map<String, String> taskCriteriaFieldJoinClauses = TaskQueryServiceImpl.criteriaFieldJoinClauses;
   
    private static Map<String, Class<?>> auditCriteriaFieldClasses = JPAAuditLogService.criteriaFieldClasses;
    private static Map<String, String> auditCriteriaFields = JPAAuditLogService.criteriaFields;
  
    // add tables lookup table logic
    private static final String processInstanceLogTable = "ProcessInstanceLog p";
    private static final String variableInstanceLogTable = "VariableInstanceLog v";
    private static final String taskTable = "TaskImpl t";
        
    private static Set<String> procInstLogNeededCriterias = new CopyOnWriteArraySet<String>();
    private static Set<String> varInstLogNeededCriterias = new CopyOnWriteArraySet<String>();
    private static Set<String> procInstLogNeededWithVarInstLogCriterias = new CopyOnWriteArraySet<String>();
    private static Set<String> taskNeededCriterias = new CopyOnWriteArraySet<String>();
    
    static { 
      
        // when doing a task or proc inst log query, add var inst log if these criteria are used
        varInstLogNeededCriterias.add(VARIABLE_ID_LIST);
        varInstLogNeededCriterias.add(VALUE_LIST);
        varInstLogNeededCriterias.add(OLD_VALUE_LIST);
        varInstLogNeededCriterias.add(EXTERNAL_ID_LIST);
        
        // when doing a var inst log or proc inst log query, add task if these criteria are used
        taskNeededCriterias.add(TASK_ID_LIST);
        taskNeededCriterias.add(TASK_STATUS_LIST);
        taskNeededCriterias.add(CREATED_BY_LIST);
        taskNeededCriterias.add(STAKEHOLDER_ID_LIST);
        taskNeededCriterias.add(POTENTIAL_OWNER_ID_LIST);
        taskNeededCriterias.add(ACTUAL_OWNER_ID_LIST);
        taskNeededCriterias.add(BUSINESS_ADMIN_ID_LIST);
        
        // when doing a task or var inst log query, add task if these criteria are used
        procInstLogNeededCriterias.add(START_DATE_LIST);
        procInstLogNeededCriterias.add(END_DATE_LIST);
        procInstLogNeededCriterias.add(PROCESS_INSTANCE_STATUS_LIST);
        
        // when doing a task or var inst log query, add task if these criteria are used
        procInstLogNeededWithVarInstLogCriterias.add(START_DATE_LIST);
        procInstLogNeededWithVarInstLogCriterias.add(END_DATE_LIST);
        procInstLogNeededWithVarInstLogCriterias.add(PROCESS_INSTANCE_STATUS_LIST);
        procInstLogNeededWithVarInstLogCriterias.add(PROCESS_VERSION_LIST);
    }
    
    private static final int TASK_SUMMARY_QUERY_TYPE;
    private static final int VARIABLE_INSTANCE_LOG_QUERY_TYPE;
    private static final int PROCESS_INSTANCE_LOG_QUERY_TYPE;
    private static final int OTHER_QUERY_TYPE;

    static { 
       int idGen = 0; 
       TASK_SUMMARY_QUERY_TYPE = idGen++;
       VARIABLE_INSTANCE_LOG_QUERY_TYPE = idGen++;
       PROCESS_INSTANCE_LOG_QUERY_TYPE = idGen++;
       OTHER_QUERY_TYPE = idGen++;
    }

    private int determineQueryType(StringBuilder queryBuilder) { 
        String taskSumQueryBegin = TaskQueryServiceImpl.TASKSUMMARY_SELECT;
        String varInstLogQueryBegin = JPAAuditLogService.VARIABLE_INSTANCE_LOG_QUERY;
        String procInstLogQueryBegin = JPAAuditLogService.PROCESS_INSTANCE_LOG_QUERY;
        
        int queryLength = queryBuilder.length();
        int taskQueryLength = taskSumQueryBegin.length() > queryLength ? queryLength : taskSumQueryBegin.length();
        int varLogLength = varInstLogQueryBegin.length() > queryLength ? queryLength : varInstLogQueryBegin.length();
        int procLogLength = procInstLogQueryBegin.length() > queryLength ? queryLength : procInstLogQueryBegin.length();
        
        if( queryBuilder.substring(0, taskQueryLength).equals(taskSumQueryBegin.substring(0, taskQueryLength)) ) { 
            return TASK_SUMMARY_QUERY_TYPE;
        } else if( queryBuilder.substring(0, varLogLength).equals(varInstLogQueryBegin.substring(0, varLogLength)) ) { 
            return VARIABLE_INSTANCE_LOG_QUERY_TYPE;
        } else if( queryBuilder.substring(0, procLogLength).equals(procInstLogQueryBegin.substring(0, procLogLength)) ) { 
            return PROCESS_INSTANCE_LOG_QUERY_TYPE;
        } else  {
            return OTHER_QUERY_TYPE;
        }
    }

    @Override
    public void addTablesToQuery( StringBuilder queryBuilder, QueryData queryData ) {
        int type = determineQueryType(queryBuilder);
        Set<String> additionalTables = new HashSet<String>();
       
        Set<String> parameterListIdsUsed = new HashSet<String>();
        if( ! queryData.intersectParametersAreEmpty() ) { 
           parameterListIdsUsed.addAll(queryData.getIntersectParameters().keySet());
        }
        if( ! queryData.intersectRangeParametersAreEmpty() ) { 
           parameterListIdsUsed.addAll(queryData.getIntersectRangeParameters().keySet()); 
        }
        if( ! queryData.intersectRegexParametersAreEmpty() ) { 
           parameterListIdsUsed.addAll(queryData.getIntersectRegexParameters().keySet()); 
        }
        
        for( String listId : parameterListIdsUsed ) { 
            if( type == TASK_SUMMARY_QUERY_TYPE ) { 
                if( procInstLogNeededCriterias.contains(listId) ) { 
                    additionalTables.add(processInstanceLogTable);
                } else if( varInstLogNeededCriterias.contains(listId) ) { 
                    additionalTables.add(variableInstanceLogTable);
                }
            } else if( type == VARIABLE_INSTANCE_LOG_QUERY_TYPE ) { 
                if( procInstLogNeededWithVarInstLogCriterias.contains(listId) ) { 
                    additionalTables.add(processInstanceLogTable);
                } else if( taskNeededCriterias.contains(listId) ) { 
                    additionalTables.add(taskTable);
                }
            } else if( type == PROCESS_INSTANCE_LOG_QUERY_TYPE ) { 
                if( varInstLogNeededCriterias.contains(listId) ) { 
                    additionalTables.add(variableInstanceLogTable);
                } else if( taskNeededCriterias.contains(listId) ) { 
                    additionalTables.add(taskTable);
                }
            }
            if( additionalTables.size() == 2 ) { 
                break;
            }
        }
        for( String table : additionalTables ) { 
           queryBuilder.append(", " + table + "\n");
        }
    }

    @Override
    public void addCriteriaToQuery( StringBuilder queryBuilder, QueryData queryData, 
            QueryAndParameterAppender queryAppender ) {
        
       int type = determineQueryType(queryBuilder);
    
       boolean addLastVariableQueryClause = false;
       String varInstLogTableId = "l";
       if( type != VARIABLE_INSTANCE_LOG_QUERY_TYPE ) { 
           Set<String> queryDataParms = new HashSet<String>();
           if( ! queryData.intersectParametersAreEmpty() ) { 
               queryDataParms.addAll(queryData.getIntersectParameters().keySet());
           }
           if( ! queryData.intersectRangeParametersAreEmpty() ) { 
               queryDataParms.addAll(queryData.getIntersectRangeParameters().keySet());
           }
           if( ! queryData.intersectRegexParametersAreEmpty() ) { 
               queryDataParms.addAll(queryData.getIntersectRegexParameters().keySet());
           }
           for( String listId : varInstLogNeededCriterias ) { 
               if( queryDataParms.contains(listId) ) { 
                   addLastVariableQueryClause = true;
                   break;
               }
           }
       }
       
       Map<String, String> emptyJoinClauses = Collections.emptyMap();
       // task queries
       if( type == TASK_SUMMARY_QUERY_TYPE ) { 
           varInstLogTableId = "v";
           internalAddCriteriaToQuery(
                   queryBuilder, queryData, queryAppender, 
                   
                   procInstLogNeededCriterias, "p", 
                   auditCriteriaFieldClasses, auditCriteriaFields, 
                   ".processInstanceId = t.taskData.processInstanceId",
                   
                   varInstLogNeededCriterias, varInstLogTableId,
                   auditCriteriaFieldClasses, auditCriteriaFields, emptyJoinClauses, 
                   ".processInstanceId = t.taskData.processInstanceId");
       } 
      
       // variable log queries
       else if( type == VARIABLE_INSTANCE_LOG_QUERY_TYPE ) { 
           internalAddCriteriaToQuery(
                   queryBuilder, queryData, queryAppender, 
                   
                   procInstLogNeededWithVarInstLogCriterias, "p", 
                   auditCriteriaFieldClasses, auditCriteriaFields, 
                   ".processInstanceId = l.processInstanceId",
                   
                   taskNeededCriterias, "t",
                   taskCriteriaFieldClasses, taskCriteriaFields, taskCriteriaFieldJoinClauses,
                   ".taskData.processInstanceId = l.processInstanceId");
       }
       
       // process log queries
       else if( type == PROCESS_INSTANCE_LOG_QUERY_TYPE ) { 
           varInstLogTableId = "v";
           internalAddCriteriaToQuery(
                   queryBuilder, queryData, queryAppender, 
                   
                   varInstLogNeededCriterias, varInstLogTableId,
                   auditCriteriaFieldClasses, auditCriteriaFields, 
                   ".processInstanceId = l.processInstanceId",
                   
                   taskNeededCriterias, "t",
                   taskCriteriaFieldClasses, taskCriteriaFields, taskCriteriaFieldJoinClauses,
                   ".taskData.processInstanceId = l.processInstanceId");
       }
       
       if( addLastVariableQueryClause ) { 
           queryData.getIntersectParameters().remove(LAST_VARIABLE_LIST);
           boolean whereAnd = queryAppender.getFirstUse() && type != TASK_SUMMARY_QUERY_TYPE;
           queryBuilder.append("\n").append( (whereAnd ? "WHERE" : "AND" ) )
           .append(" (").append(varInstLogTableId).append(".id IN (SELECT MAX(ll.id) FROM VariableInstanceLog ll ")
           .append("GROUP BY ll.variableId, ll.processInstanceId))"); 
       }
    } 

    private static void internalAddCriteriaToQuery(StringBuilder queryBuilder, QueryData queryData, QueryAndParameterAppender queryAppender,
           Set<String> firstNeededCriterias, String firstTableId, 
           Map<String, Class<?>> firstCriteriaFieldClasses, Map<String, String> firstCriteriaFields, 
           String firstTableIdJoinClause,
           Set<String> otherNeededCriterias, String otherTableId, 
           Map<String, Class<?>> otherCriteriaFieldClasses, Map<String, String> otherCriteriaFields,
           Map<String, String> otherCriteriaFieldJoinClauses, 
           String otherTableJoinClause ) { 
        
        boolean addFirstTableJoinClause = false;
        boolean addOtherTableJoinClause = false;
        
        Set<String> processedListIds = new HashSet<String>();
        // regular parameters
        if( ! queryData.intersectParametersAreEmpty() ) { 
            for( Entry<String, List<? extends Object>> entry : queryData.getIntersectParameters().entrySet() ) { 
                String listId = entry.getKey();
                if( firstNeededCriterias.contains(listId) )   { 
                    addFirstTableJoinClause = true;
                    processedListIds.add(listId);
                    String fieldName = firstTableId + firstCriteriaFields.get(listId).substring(1);
                    queryAppender.addQueryParameters(
                            entry.getValue(), listId, 
                            firstCriteriaFieldClasses.get(listId), fieldName, 
                            false);
                } else if( otherNeededCriterias.contains(listId) ) { 
                    addOtherTableJoinClause = true;
                    processedListIds.add(listId);
                    String fieldName = otherTableId + otherCriteriaFields.get(listId).substring(1);
                    queryAppender.addQueryParameters(
                            entry.getValue(), listId, 
                            otherCriteriaFieldClasses.get(listId), fieldName,
                            otherCriteriaFieldJoinClauses.get(listId),
                            false);
                }
            }
            for( String processedListId : processedListIds ) { 
                queryData.getIntersectParameters().remove(processedListId);
            }
            processedListIds.clear();
        }
        
        // range parameters
        if( ! queryData.intersectRangeParametersAreEmpty() ) { 
            for( Entry<String, List<? extends Object>> entry : queryData.getIntersectRangeParameters().entrySet() ) { 
                String listId = entry.getKey();
                if( firstNeededCriterias.contains(listId) )   { 
                    addFirstTableJoinClause = true;
                    processedListIds.add(listId);
                    String fieldName = firstTableId + firstCriteriaFields.get(listId).substring(1);
                    queryAppender.addRangeQueryParameters(
                            entry.getValue(), listId, 
                            firstCriteriaFieldClasses.get(listId), fieldName, 
                            false);
                } else if( otherNeededCriterias.contains(listId) ) { 
                    addOtherTableJoinClause = true;
                    processedListIds.add(listId);
                    String fieldName = otherTableId + otherCriteriaFields.get(listId).substring(1);
                    queryAppender.addRangeQueryParameters(
                            entry.getValue(), listId, 
                            otherCriteriaFieldClasses.get(listId), fieldName,
                            otherCriteriaFieldJoinClauses.get(listId),
                            false);
                }
            }
            for( String processedListId : processedListIds ) { 
                queryData.getIntersectRangeParameters().remove(processedListId);
            }
            processedListIds.clear();
        }
        
        // regex parameters
        if( ! queryData.intersectRegexParametersAreEmpty() ) { 
            for( Entry<String, List<String>> entry : queryData.getIntersectRegexParameters().entrySet() ) { 
                String listId = entry.getKey();
                if( firstNeededCriterias.contains(listId) )   { 
                    addFirstTableJoinClause = true;
                    processedListIds.add(listId);
                    String fieldName = firstTableId + firstCriteriaFields.get(listId).substring(1);
                    queryAppender.addRegexQueryParameters(
                            entry.getValue(), listId, 
                            fieldName, 
                            false);
                } else if( otherNeededCriterias.contains(listId) ) { 
                    addOtherTableJoinClause = true;
                    processedListIds.add(listId);
                    String fieldName = otherTableId + otherCriteriaFields.get(listId).substring(1);
                    queryAppender.addRegexQueryParameters(
                            entry.getValue(), listId, 
                            fieldName,
                            otherCriteriaFieldJoinClauses.get(listId),
                            false);
                }
            }
            for( String processedListId : processedListIds ) { 
                queryData.getIntersectRegexParameters().remove(processedListId);
            }
        }
        
        if( addFirstTableJoinClause ) { 
           queryBuilder.append("\nAND " + firstTableId + firstTableIdJoinClause );
        }
        if( addOtherTableJoinClause ) { 
           queryBuilder.append("\nAND " + otherTableId + otherTableJoinClause );
        }
    }
    
}
