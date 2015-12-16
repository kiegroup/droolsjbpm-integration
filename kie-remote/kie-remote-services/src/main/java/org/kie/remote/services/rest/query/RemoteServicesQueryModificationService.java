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

package org.kie.remote.services.rest.query;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;

import org.jbpm.query.jpa.data.QueryCriteria;
import org.jbpm.query.jpa.data.QueryWhere;
import org.jbpm.query.jpa.service.QueryModificationService;

/**
 * This is the {@link QueryModificationService} implementation for the REST remote services, 
 * which allows us to do complicated queries that join the following:<ul>
 * <li>The process instance tables to the variable instance log tables</li>
 * <li>Or the task tables to the variable instance log tables</li>
 * </ul>
 */
public class RemoteServicesQueryModificationService implements QueryModificationService {

    public RemoteServicesQueryModificationService() {
        // default constructor
    }

    @Override
    public boolean accepts(String listId) {
        return false;
    }

    @Override
    public void optimizeCriteria(QueryWhere queryWhere) {

    }

    @Override
    public <R> Predicate createPredicate(QueryCriteria criteria, CriteriaQuery<R> query, CriteriaBuilder builder) {
        return null;
    }

//    private static Map<String, Class<?>> taskCriteriaFieldClasses = TaskQueryServiceImpl.criteriaFieldClasses;
//    private static Map<String, String> taskCriteriaFields = TaskQueryServiceImpl.criteriaFields;
//    private static Map<String, String> taskCriteriaFieldJoinClauses = TaskQueryServiceImpl.criteriaFieldJoinClauses;
//   
//    private static Map<String, Class<?>> auditCriteriaFieldClasses = JPAAuditLogService.criteriaFieldClasses;
//    private static Map<String, String> auditCriteriaFields = JPAAuditLogService.criteriaFields;
//  
//    // add tables lookup table logic
//    private enum JbpmEntity { 
//        processInstanceLog,
//        variableInstanceLog,
//        task,
//        stakeHolders,
//        potentialOwners,
//        businessAdministrators,
//    }
//        
//    private static Set<String> procInstLogNeededCriterias = new CopyOnWriteArraySet<String>();
//    private static Set<String> varInstLogNeededCriterias = new CopyOnWriteArraySet<String>();
//    private static Set<String> procInstLogNeededWithVarInstLogCriterias = new CopyOnWriteArraySet<String>();
//    private static Set<String> taskNeededCriterias = new CopyOnWriteArraySet<String>();
//    private static Set<String> organizationalEntityNeededCriterias = new CopyOnWriteArraySet<String>();
//    
//    static { 
//      
//        // when doing a task or proc inst log query, add var inst log if these criteria are used
//        varInstLogNeededCriterias.add(VARIABLE_ID_LIST);
//        varInstLogNeededCriterias.add(VALUE_LIST);
//        varInstLogNeededCriterias.add(OLD_VALUE_LIST);
//        varInstLogNeededCriterias.add(EXTERNAL_ID_LIST);
//        varInstLogNeededCriterias.add(VARIABLE_INSTANCE_ID_LIST);
//        varInstLogNeededCriterias.add(VAR_VALUE_ID_LIST);
//        
//        // when doing a var inst log or proc inst log query, add task if these criteria are used
//        taskNeededCriterias.add(TASK_ID_LIST);
//        taskNeededCriterias.add(TASK_STATUS_LIST);
//        taskNeededCriterias.add(CREATED_BY_LIST);
//        taskNeededCriterias.add(STAKEHOLDER_ID_LIST);
//        taskNeededCriterias.add(POTENTIAL_OWNER_ID_LIST);
//        taskNeededCriterias.add(ACTUAL_OWNER_ID_LIST);
//        taskNeededCriterias.add(BUSINESS_ADMIN_ID_LIST);
//        
//        // when doing a var inst log or proc inst log query, add orgEnt if these criteria are used
//        organizationalEntityNeededCriterias.add(STAKEHOLDER_ID_LIST);
//        organizationalEntityNeededCriterias.add(POTENTIAL_OWNER_ID_LIST);
//        organizationalEntityNeededCriterias.add(BUSINESS_ADMIN_ID_LIST);
//        
//        // when doing a task or var inst log query, add task if these criteria are used
//        procInstLogNeededCriterias.add(START_DATE_LIST);
//        procInstLogNeededCriterias.add(END_DATE_LIST);
//        procInstLogNeededCriterias.add(PROCESS_INSTANCE_STATUS_LIST);
//        
//        // when doing a task or var inst log query, add task if these criteria are used
//        procInstLogNeededWithVarInstLogCriterias.add(START_DATE_LIST);
//        procInstLogNeededWithVarInstLogCriterias.add(END_DATE_LIST);
//        procInstLogNeededWithVarInstLogCriterias.add(PROCESS_INSTANCE_STATUS_LIST);
//        procInstLogNeededWithVarInstLogCriterias.add(PROCESS_VERSION_LIST);
//    }
//   
//    // query types
//    private static final int TASK_SUMMARY_QUERY_TYPE;
//    private static final int VARIABLE_INSTANCE_LOG_QUERY_TYPE;
//    private static final int PROCESS_INSTANCE_LOG_QUERY_TYPE;
//    private static final int OTHER_QUERY_TYPE;
//
//    static { 
//       int idGen = 0; 
//       TASK_SUMMARY_QUERY_TYPE = idGen++;
//       VARIABLE_INSTANCE_LOG_QUERY_TYPE = idGen++;
//       PROCESS_INSTANCE_LOG_QUERY_TYPE = idGen++;https://play.spotify.com/user/spotify/playlist/0IhwR4MDWOsZ6ZHo8CRTob
//       OTHER_QUERY_TYPE = idGen++;
//    }
//
//    /**
//     * This looks at the query string built and determines what type of query it is. 
//     * @param queryBuilder The query string(Builder)
//     * @return an int showing which type it is (see "query types" above)
//     */
//    private int determineQueryType(Class querytype) { 
//        if( TaskSummaryImpl.class.equals(querytype) ) { 
//            return TASK_SUMMARY_QUERY_TYPE;
//        } else if( VariableInstanceLog.class.equals(querytype) ) { 
//            return VARIABLE_INSTANCE_LOG_QUERY_TYPE;
//        } else if( ProcessInstanceLog.class.equals(querytype) ) { 
//            return PROCESS_INSTANCE_LOG_QUERY_TYPE;
//        } else  {
//            throw new IllegalArgumentException("Unexpected type for query: " + querytype.getName() );
//        }
//    }
//
//    @Override
//    public <T> void addTablesToQuery( QueryWhere queryWhere, CriteriaQuery<T> query, Class<T> queryType ) {
////    public void addTablesToQuery( StringBuilder queryBuilder, QueryData queryData ) {
//        int type = determineQueryType(queryType);
//      
//        // make a list with all of the parameter list ids
//        Set<String> parameterListIdsUsed  = new HashSet<String>();
//        parameterListIdsUsed.addAll(getParameterListIdsUsed(queryWhere.getCriteria()));
//
//        // go through parameter list ids to see which tables need to be added
//        Set<JbpmEntity> additionalTables = new HashSet<JbpmEntity>();
//        for( String listId : parameterListIdsUsed ) { 
//            switch( type ) { 
//            case TASK_SUMMARY_QUERY_TYPE:
//                if( procInstLogNeededCriterias.contains(listId) ) { 
//                    additionalTables.add(JbpmEntity.processInstanceLog);
//                } else if( varInstLogNeededCriterias.contains(listId) ) { 
//                    additionalTables.add(JbpmEntity.variableInstanceLog);
//                }
//                break;
//            case VARIABLE_INSTANCE_LOG_QUERY_TYPE:
//                if( procInstLogNeededWithVarInstLogCriterias.contains(listId) ) { 
//                    additionalTables.add(JbpmEntity.processInstanceLog);
//                } 
//                if( taskNeededCriterias.contains(listId) ) { 
//                    additionalTables.add(JbpmEntity.task);
//                }
//                if( organizationalEntityNeededCriterias.contains(listId) ) { 
//                    if( listId.equals(STAKEHOLDER_ID_LIST) ) { 
//                        additionalTables.add(JbpmEntity.stakeHolders);
//                    } else if( listId.equals(POTENTIAL_OWNER_ID_LIST) ) { 
//                        additionalTables.add(JbpmEntity.potentialOwners);
//                    } else if( listId.equals(BUSINESS_ADMIN_ID_LIST) ) { 
//                        additionalTables.add(JbpmEntity.businessAdministrators);
//                    }
//                }
//                break;
//            case PROCESS_INSTANCE_LOG_QUERY_TYPE:
//                if( varInstLogNeededCriterias.contains(listId) ) { 
//                    additionalTables.add(JbpmEntity.variableInstanceLog);
//                } else if( taskNeededCriterias.contains(listId) ) { 
//                    additionalTables.add(JbpmEntity.task);
//                }
//                break;
//            default:
//                throw new IllegalStateException("Unknown query type: " + type);
//            } 
//        }
//       
//        // Add the extra tables
//        for( JbpmEntity table : additionalTables ) { 
//            // TODO?
//            switch( table ) { 
//            case processInstanceLog:
//            case variableInstanceLog:
//            case task:
//            case businessAdministrators:
//            case potentialOwners:
//            case stakeHolders:
//            default:
//                throw new IllegalStateException("Unexpected table: " + table.toString());
//            }
//        }
//    }
//
//    private List<String> getParameterListIdsUsed(List<QueryCriteria> criteriaList) { 
//        List<String> parameterListIdsUsed = new LinkedList<String>();
//        for( QueryCriteria criteria : criteriaList ) { 
//           if( criteria.isGroupCriteria() ) { 
//               parameterListIdsUsed.addAll(getParameterListIdsUsed(criteriaList));
//           } else { 
//               parameterListIdsUsed.add(criteria.getListId());
//           }
//        }
//        return parameterListIdsUsed;
//    }
//   
//    private enum JbpmEntityJoin { 
//        auditLogToTask, // log.processInstanceId = task.taskData.processInstanceId
//        auditLogToAuditLog, // log.processInstanceId = otherLog.processInstanceId
//        taskToAuditLog // task.taskData.processInstanceId = log.processInstanceId
//    }
//    
//    @Override
//    public <R, T> void addCriteriaToQuery( QueryWhere queryWhere, CriteriaQuery<R> query, CriteriaBuilder criteriaBuilder, Class<T> queryType ) {
//        
//       int type = determineQueryType(queryType);
//    
//       boolean addLastVariableQueryClause = false;
//       boolean addVariableValueQueryClause = false;
//      
//       Set<String> parameterListIdsUsed  = new HashSet<String>();
//       parameterListIdsUsed.addAll(getParameterListIdsUsed(queryWhere.getCriteria()));
//       
//       if( type != VARIABLE_INSTANCE_LOG_QUERY_TYPE ) { 
//           Set<String> queryDataParms = new HashSet<String>(getParameterListIdsUsed(queryWhere.getCriteria()));
//           Iterator<String> iter = queryDataParms.iterator();
//           int lastAndVarValFound = 0;
//           while( iter.hasNext() && lastAndVarValFound < 3 ) {
//               String listId = iter.next();
//               if( lastAndVarValFound % 2 == 0 )  {
//                   if( varInstLogNeededCriterias.contains(listId) ) { 
//                       addLastVariableQueryClause = true;
//                       ++lastAndVarValFound;
//                       continue;
//                   }
//               } else if( VAR_VALUE_ID_LIST.equals(listId) )  { 
//                  addVariableValueQueryClause = true;
//                  lastAndVarValFound += 2;
//                  continue;
//               }
//           }
//       }
//       
//       // task queries
//       if( type == TASK_SUMMARY_QUERY_TYPE ) { 
//           internalAddCriteriaToQuery(
//                   queryWhere, query, criteriaBuilder,
//                   
//                   procInstLogNeededCriterias, JbpmEntity.processInstanceLog,
//                   JbpmEntityJoin.auditLogToTask,
//                   
//                   varInstLogNeededCriterias, JbpmEntity.variableInstanceLog,
//                   JbpmEntityJoin.auditLogToTask);
//       } 
//      
//       // variable log queries
//       else if( type == VARIABLE_INSTANCE_LOG_QUERY_TYPE ) { 
//           internalAddCriteriaToQuery(
//                   queryWhere, query, criteriaBuilder,
//                   
//                   procInstLogNeededWithVarInstLogCriterias, JbpmEntity.processInstanceLog,
//                  JbpmEntityJoin.auditLogToAuditLog,
//                   
//                   taskNeededCriterias, JbpmEntity.task,
//                   JbpmEntityJoin.taskToAuditLog);
//       }
//       
//       // process log queries
//       else if( type == PROCESS_INSTANCE_LOG_QUERY_TYPE ) { 
//           internalAddCriteriaToQuery(
//                   queryWhere, query, criteriaBuilder,
//                   
//                   varInstLogNeededCriterias, JbpmEntity.variableInstanceLog,
//                  JbpmEntityJoin.auditLogToAuditLog,
//                   
//                   taskNeededCriterias, JbpmEntity.task,
//                   JbpmEntityJoin.taskToAuditLog);
//       }
//       
//       if( type != VARIABLE_INSTANCE_LOG_QUERY_TYPE ) { 
//           if( addLastVariableQueryClause ) { 
//               queryData.getIntersectParameters().remove(LAST_VARIABLE_LIST);
//               // @formatter:off
//               StringBuilder queryPhraseBuilder = new StringBuilder(" (")
//                   .append(varInstLogTableId).append(".id IN (SELECT MAX(ll.id) FROM VariableInstanceLog ll ")
//                   .append("GROUP BY ll.variableId, ll.processInstanceId))"); 
//               // @formatter:on
//               queryAppender.addToQueryBuilder(queryPhraseBuilder.toString(), false);
//           }
//           if( addVariableValueQueryClause ) { 
//               if( ! queryData.intersectParametersAreEmpty() ) { 
//                   List<String> varValParameters = (List<String>) queryData.getIntersectParameters().remove(VAR_VALUE_ID_LIST);
//                   if( varValParameters != null && ! varValParameters.isEmpty() ) { 
//                      List<Object[]> varValCriteria = new ArrayList<Object[]>();
//                      checkVarValCriteria(varValParameters, false, false, varValCriteria);
//                      addVarValCriteria(! queryAppender.hasBeenUsed(), queryAppender, varInstLogTableId, varValCriteria);
//                      queryAppender.markAsUsed();
//                      queryAppender.queryBuilderModificationCleanup();
//                   }
//               } 
//               if( ! queryData.intersectRegexParametersAreEmpty() ) { 
//                   List<String> varValRegexParameters = queryData.getIntersectRegexParameters().remove(VAR_VALUE_ID_LIST);
//                   if( varValRegexParameters != null && ! varValRegexParameters.isEmpty() ) { 
//                      List<Object[]> varValCriteria = new ArrayList<Object[]>();
//                      checkVarValCriteria(varValRegexParameters, false, true, varValCriteria);
//                      addVarValCriteria(! queryAppender.hasBeenUsed(), queryAppender, varInstLogTableId, varValCriteria);
//                      queryAppender.markAsUsed();
//                      queryAppender.queryBuilderModificationCleanup();
//                   }
//               }
//           }
//       }
//       
//       int end = "SELECT".length();
//       if( type == VARIABLE_INSTANCE_LOG_QUERY_TYPE || type == PROCESS_INSTANCE_LOG_QUERY_TYPE ) { 
//           queryAppender.getQueryBuilder().replace(0, end, "SELECT DISTINCT");
//       }
//    } 
//
//    private static <T> void internalAddCriteriaToQuery(QueryWhere queryWhere, CriteriaQuery<T> query, CriteriaBuilder builder,
//           Set<String> firstNeededCriterias, JbpmEntity firstTable, 
//           JbpmEntityJoin firstTableIdJoinClause,
//           
//           Set<String> otherNeededCriterias, JbpmEntity otherTable, 
//           JbpmEntityJoin otherTableJoinClause ) { 
//        
//        boolean addFirstTableJoinClause = false;
//        boolean addOtherTableJoinClause = false;
//
//        Set<String> processedListIds = new HashSet<String>();
//
//        Iterator<QueryCriteria> iter = queryWhere.getCriteria().iterator();
//        while( iter.hasNext() ) { 
//            QueryCriteria criteria = iter.next();
//            String listId = criteria.getListId();
//            if( VAR_VALUE_ID_LIST.equals(listId) ) { 
//                if( firstNeededCriterias.contains(listId) ) {
//                    addFirstTableJoinClause = true;
//                } else if ( otherNeededCriterias.contains(listId) ) { 
//                    addOtherTableJoinClause = true;
//                }
//                continue;
//            }
//            if( firstNeededCriterias.contains(listId) )   { 
//                iter.remove(); // to prevent double processing of this criteria
//                addCriteriaToQuery(criteria, firstTable, firstTableIdJoinClause, query, builder);
//            } else if( otherNeededCriterias.contains(listId) ) { 
//                iter.remove(); // to prevent double processing of this criteria
//                addCriteriaToQuery(criteria, otherTable, otherTableJoinClause, query, builder);
//            }
//        }
//
//        for( String processedListId : processedListIds ) { 
//            // remove processListId from criteria, so it's not processed twice!!
//        }
//        processedListIds.clear();
//
//        if( addFirstTableJoinClause ) { 
//            // add join clause to first table id
//        }
//        if( addOtherTableJoinClause ) { 
//            // add join clause to other table id
//        }
//    }
//
//    private static <T> void addCriteriaToQuery( QueryCriteria criteria, JbpmEntity table, JbpmEntityJoin joinType, CriteriaQuery<T> query, CriteriaBuilder builder ) {
//       
//        Set<Root<?>> queryRoots = query.getRoots();
//        assert ! queryRoots.isEmpty() : "This query does not have any root tables!";
//       
//        if( queryRoots.size() > 1 ) { 
//            StringBuffer msg = new StringBuffer("This should not be possible! There are multiple query roots: ");
//            Iterator<Root<?>> iter = queryRoots.iterator();
//            msg.append(iter.next().getJavaType().getSimpleName());
//            while( iter.hasNext() ) { 
//                msg.append(", ").append(iter.next().getJavaType().getSimpleName());
//            }
//            throw new IllegalStateException(msg.toString());
//        }
//     
//        // TODO: add criteria
//    }


}
