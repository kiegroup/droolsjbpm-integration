package org.kie.remote.services.rest.query;


import static org.jbpm.process.audit.AuditQueryCriteriaUtil.variableInstanceLogSpecificCreatePredicateFromSingleCriteria;
import static org.jbpm.query.jpa.data.QueryParameterIdentifiersUtil.getQueryParameterIdNameMap;
import static org.jbpm.query.jpa.impl.QueryCriteriaUtil.applyMetaCriteriaToQuery;
import static org.jbpm.query.jpa.impl.QueryCriteriaUtil.basicCreatePredicateFromSingleCriteria;
import static org.jbpm.query.jpa.impl.QueryCriteriaUtil.defaultGetEntityField;
import static org.jbpm.query.jpa.impl.QueryCriteriaUtil.getRoot;
import static org.jbpm.services.task.persistence.TaskSummaryQueryCriteriaUtil.taskImplSpecificGetEntityField;
import static org.jbpm.services.task.persistence.TaskSummaryQueryCriteriaUtil.taskSpecificCreatePredicateFromSingleCriteria;
import static org.kie.remote.services.rest.query.RemoteServicesQueryData.procInstLogNeededCriterias;
import static org.kie.remote.services.rest.query.RemoteServicesQueryData.taskNeededCriterias;
import static org.kie.remote.services.rest.query.RemoteServicesQueryData.taskSpecificCriterias;
import static org.kie.remote.services.rest.query.RemoteServicesQueryData.varInstLogNeededCriterias;
import static org.kie.remote.services.rest.query.RemoteServicesQueryData.varInstLogSpecificCriterias;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.SingularAttribute;

import org.jbpm.process.audit.ProcessInstanceLog;
import org.jbpm.process.audit.ProcessInstanceLog_;
import org.jbpm.process.audit.VariableInstanceLog;
import org.jbpm.process.audit.VariableInstanceLog_;
import org.jbpm.query.jpa.data.QueryCriteria;
import org.jbpm.query.jpa.data.QueryWhere;
import org.jbpm.services.task.audit.impl.model.TaskVariableImpl;
import org.jbpm.services.task.impl.model.PeopleAssignmentsImpl;
import org.jbpm.services.task.impl.model.TaskDataImpl;
import org.jbpm.services.task.impl.model.TaskDataImpl_;
import org.jbpm.services.task.impl.model.TaskImpl;
import org.jbpm.services.task.impl.model.TaskImpl_;

public class RemoteServicesCriteriaUtil {

    /**
     * This method creates a {@link Predicate} based for a field of a joined {@link Root}.
     *
     * @param query The {@link CriteriaQuery} that we are building
     * @param builder A {@link CriteriaBuilder} to help create the {@link Predicate}
     * @param queryType
     * @param queryWhere The {@link QueryWhere} instance that we're processing to create this {@link CriteriaQuery}
     * @param criteria The specific {@link QueryCriteria} that is being used to create the {@link Predicate}
     * @return The created {@link Predicate}
     */
    @SuppressWarnings("unchecked")
    public static <Q> Class getJoinRootClassAndAddNeededJoin(
            CriteriaQuery<Q> query,
            CriteriaBuilder builder,
            Class queryType,
            QueryCriteria criteria,
            QueryWhere queryWhere) {

        String listId = criteria.getListId();
        Class newJoinRootClass = null;

        if( ProcessInstanceLog.class.equals(queryType) ) {
            Root<ProcessInstanceLog> procInstLogRoot = getRoot(query, queryType);
            if( varInstLogNeededCriterias.contains(listId)
                || varInstLogSpecificCriterias.contains(listId) ) {
                newJoinRootClass = VariableInstanceLog.class;
                addAuditRootAndJoinClauseIfNeeded(query, builder, queryWhere, criteria,
                        (Class<VariableInstanceLog>) newJoinRootClass,
                        procInstLogRoot.get(ProcessInstanceLog_.processInstanceId),
                        VariableInstanceLog_.processInstanceId);
            } else if( taskNeededCriterias.contains(listId)
                || taskSpecificCriterias.contains(listId) ) {
                newJoinRootClass = TaskImpl.class;
                addTaskRootAndJoinClauseIfNeeded(query, builder, queryWhere, criteria,
                        procInstLogRoot.get(ProcessInstanceLog_.processInstanceId));
            }
        } else if( VariableInstanceLog.class.equals(queryType) ) {
            if( varInstLogSpecificCriterias.contains(listId) ) {
                return VariableInstanceLog.class;
            }
            Root<VariableInstanceLog> varInstLogRoot = getRoot(query, queryType);
            if( taskNeededCriterias.contains(listId)
                || taskSpecificCriterias.contains(listId) ) {
                newJoinRootClass = TaskImpl.class;
                addTaskRootAndJoinClauseIfNeeded(query, builder, queryWhere, criteria,
                        varInstLogRoot.get(VariableInstanceLog_.processInstanceId));

            } else if( procInstLogNeededCriterias.contains(listId) ) {
                newJoinRootClass = ProcessInstanceLog.class;
                addAuditRootAndJoinClauseIfNeeded(query, builder, queryWhere, criteria,
                        (Class<ProcessInstanceLog>) newJoinRootClass,
                        varInstLogRoot.get(VariableInstanceLog_.processInstanceId),
                        ProcessInstanceLog_.processInstanceId);
            }
        } else if( TaskImpl.class.equals(queryType) ) {
            if( taskSpecificCriterias.contains(listId) ) {
                return TaskImpl.class;
            }
            Root<TaskImpl> taskRoot = getRoot(query, queryType);
            if( varInstLogNeededCriterias.contains(listId)
                || varInstLogSpecificCriterias.contains(listId) ) {
                newJoinRootClass = VariableInstanceLog.class;
                addAuditRootAndJoinClauseIfNeeded(query, builder, queryWhere, criteria,
                        (Class<VariableInstanceLog>) newJoinRootClass,
                        taskRoot.get(TaskImpl_.taskData).get(TaskDataImpl_.processInstanceId),
                        VariableInstanceLog_.processInstanceId);
            } else if( procInstLogNeededCriterias.contains(listId) ) {
                newJoinRootClass = ProcessInstanceLog.class;
                addAuditRootAndJoinClauseIfNeeded(query, builder, queryWhere, criteria,
                        (Class<ProcessInstanceLog>) newJoinRootClass,
                        taskRoot.get(TaskImpl_.taskData).get(TaskDataImpl_.processInstanceId),
                        ProcessInstanceLog_.processInstanceId);
            }
        } else {
            throw new IllegalStateException("Unexpected query type " + queryType.getSimpleName() + " when processing criteria "
                    + getQueryParameterIdNameMap().get(Integer.parseInt(listId)) + " (" + criteria.toString() + ")");
        }

        if( newJoinRootClass == null ) {
            throw new IllegalStateException("Unexpected criteria [" + getQueryParameterIdNameMap().get(Integer.parseInt(listId)) + "] when joining to " + queryType.getSimpleName() + " query!");
        }
        return newJoinRootClass;
    }

    public class TASK_SPECIFIC_CRITERIA {
       // class constant to indicate the criteria encountered is a TaskQueryCriteriaUtil implementation specific criteria
    }

    private static <Q,J> void addAuditRootAndJoinClauseIfNeeded(
            CriteriaQuery<Q> query, CriteriaBuilder builder, QueryWhere queryWhere, QueryCriteria criteria,
            Class<J> newJoinRootClass,
            Path origRootJoinFieldPath,
            SingularAttribute... newJoinAttr) {

        Root<J> newJoinRoot = null;
        for( Root<?> root : query.getRoots() ) {
            Class rootType = root.getJavaType();
            if( rootType.equals(newJoinRootClass) )  {
                newJoinRoot = (Root<J>) root;
                break;
            }
        }

        if( newJoinRoot == null ) {
            newJoinRoot = query.from(newJoinRootClass);
            Path newJoinRootJoinFieldPath = newJoinRoot.get(newJoinAttr[0]);
            for( int i = 1; i < newJoinAttr.length; ++i ) {
               newJoinRootJoinFieldPath = newJoinRootJoinFieldPath.get(newJoinAttr[i]);
            }
            Predicate joinPredicate = builder.equal(
                    newJoinRootJoinFieldPath,
                    origRootJoinFieldPath);
            joinPredicate = queryWhere.getJoinPredicates().put(newJoinRootClass.getSimpleName(), joinPredicate);
            assert joinPredicate == null : "There is already an existing join to " + newJoinRootClass.getSimpleName();
        }
    }

    private static <Q,A> void addTaskRootAndJoinClauseIfNeeded(
            CriteriaQuery<Q> query, CriteriaBuilder builder, QueryWhere queryWhere, QueryCriteria criteria,
            Path origRootJoinFieldPath) {

        Root<TaskImpl> taskRoot = null;
        for( Root<?> root : query.getRoots() ) {
            Class rootType = root.getJavaType();
            if( rootType.equals(TaskImpl.class) )  {
                taskRoot = (Root<TaskImpl>) root;
                break;
            }
        }

        if( taskRoot == null ) {
            taskRoot = query.from(TaskImpl.class);
            Path newJoinRootJoinFieldPath = taskRoot.get(TaskImpl_.taskData).get(TaskDataImpl_.processInstanceId);
            Predicate joinPredicate = builder.equal(
                    newJoinRootJoinFieldPath,
                    origRootJoinFieldPath);

            queryWhere.getJoinPredicates().put(TaskImpl.class.getSimpleName(), joinPredicate);
            assert joinPredicate != null : "There is already an existing join to " + TaskImpl.class.getSimpleName();
        }
    }

    public static <Q> Predicate createJoinedAuditPredicateFromSingleCriteria(CriteriaQuery<Q> query, CriteriaBuilder builder,
            Attribute attr,
            QueryCriteria criteria, QueryWhere queryWhere) {
        String listId = criteria.getListId();
        if( varInstLogSpecificCriterias.contains(listId) ) {
            Root<VariableInstanceLog> table = getRoot(query, VariableInstanceLog.class);
            return variableInstanceLogSpecificCreatePredicateFromSingleCriteria(query, builder, criteria, table);
        } else {
            // how to get the entity field for all classes, except for TaskImpl:
            Expression entityField = defaultGetEntityField(query, listId, attr);
            return basicCreatePredicateFromSingleCriteria(builder, entityField, criteria);
        }
    }

    public static <Q> Predicate createJoinedTaskPredicateFromSingleCriteria(CriteriaQuery<Q> query, CriteriaBuilder builder,
            Attribute attr,
            QueryCriteria criteria, QueryWhere queryWhere) {
        String listId = criteria.getListId();
        if( taskSpecificCriterias.contains(listId) ) {
            return taskSpecificCreatePredicateFromSingleCriteria(query, builder, criteria, queryWhere);
        } else {
            // Will be replaced with subquery code once this issue is fixed:
            // https://issues.jboss.org/browse/JBPM-4715
            Root<TaskImpl> taskRoot = null;
            Join<TaskImpl, TaskDataImpl> taskDataJoin = null;
            Join<TaskImpl, PeopleAssignmentsImpl> peopleAssignJoin = null;
            for( Root root : query.getRoots() ) {
                if( TaskImpl.class.equals(root.getJavaType()) ) {
                   taskRoot = (Root<TaskImpl>) root;
                   for( Join<TaskImpl, ?> join : taskRoot.getJoins() ) {
                      if( TaskDataImpl.class.equals(join.getJavaType()) ) {
                          taskDataJoin = (Join<TaskImpl, TaskDataImpl>) join;
                      } else if( PeopleAssignmentsImpl.class.equals(join.getJavaType()) )  {
                          peopleAssignJoin = (Join<TaskImpl, PeopleAssignmentsImpl>) join;
                      }
                   }
                }
            }
            // how to get the entity field for the TaskImpl class:
            Expression entityField = taskImplSpecificGetEntityField(query,
                    taskRoot, taskDataJoin, peopleAssignJoin,
                    listId, attr);
            return basicCreatePredicateFromSingleCriteria(builder, entityField, criteria);
        }
    }

    protected static <T> List<T> sharedCreateQueryAndCallApplyMetaCriteriaAndGetResult(QueryWhere queryWhere, CriteriaQuery<T> criteriaQuery, CriteriaBuilder builder, RemoteServicesQueryJPAService jpaService) {

        // apply joined Predicates
        if( ! queryWhere.getJoinPredicates().isEmpty() ) {
            Predicate originalPredicate = criteriaQuery.getRestriction();
            List<Predicate> allPredicates = new ArrayList<Predicate>(queryWhere.getJoinPredicates().values());
            allPredicates.add(originalPredicate);
            Predicate newQueryPredicate = builder.and(allPredicates.toArray(new Predicate[allPredicates.size()]));

            criteriaQuery.where(newQueryPredicate);
        }

        // TODO: this should not be neccessary!
        criteriaQuery.distinct(true);

        EntityManager em = jpaService.getEntityManager();
        Object newTx = jpaService.joinTransaction(em);
        Query query = em.createQuery(criteriaQuery);

        applyMetaCriteriaToQuery(query, queryWhere);

        // execute query
        List<T> result = query.getResultList();

        jpaService.closeEntityManager(em, newTx);

        return result;
    }
}
