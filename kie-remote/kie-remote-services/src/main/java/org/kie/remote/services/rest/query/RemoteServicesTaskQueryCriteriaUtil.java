package org.kie.remote.services.rest.query;

import static org.jbpm.query.jpa.data.QueryParameterIdentifiersUtil.getQueryParameterIdNameMap;
import static org.kie.remote.services.rest.query.RemoteServicesCriteriaUtil.createJoinedAuditPredicateFromSingleCriteria;
import static org.kie.remote.services.rest.query.RemoteServicesCriteriaUtil.createJoinedTaskPredicateFromSingleCriteria;
import static org.kie.remote.services.rest.query.RemoteServicesCriteriaUtil.getJoinRootClassAndAddNeededJoin;
import static org.kie.remote.services.rest.query.RemoteServicesCriteriaUtil.sharedCreateQueryAndCallApplyMetaCriteriaAndGetResult;
import static org.kie.remote.services.rest.query.RemoteServicesQueryData.checkAndInitializeCriteriaAttributes;

import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.metamodel.Attribute;

import org.jbpm.process.audit.ProcessInstanceLog;
import org.jbpm.process.audit.VariableInstanceLog;
import org.jbpm.query.jpa.data.QueryCriteria;
import org.jbpm.query.jpa.data.QueryWhere;
import org.jbpm.services.task.impl.model.TaskImpl;
import org.jbpm.services.task.persistence.TaskSummaryQueryCriteriaUtil;

public class RemoteServicesTaskQueryCriteriaUtil extends TaskSummaryQueryCriteriaUtil {

    // Query Field Info -----------------------------------------------------------------------------------------------------------

    public static final Map<Class, Map<String, Attribute>> criteriaAttributes
        = RemoteServicesQueryData.criteriaAttributes;

    @Override
    protected synchronized boolean initializeCriteriaAttributes() {
        return RemoteServicesQueryData.initializeCriteriaAttributes();
    }

    // Implementation specific logic ----------------------------------------------------------------------------------------------

    private final RemoteServicesQueryJPAService jpaService;

    public RemoteServicesTaskQueryCriteriaUtil(RemoteServicesQueryJPAService service) {
        initialize(criteriaAttributes);
        this.jpaService = service;
        checkAndInitializeCriteriaAttributes();
    }

    protected EntityManager getEntityManager() {
        return this.jpaService.getEntityManager();
    }

    protected Object joinTransaction(EntityManager em) {
        return this.jpaService.joinTransaction(em);
    }

    protected void closeEntityManager(EntityManager em, Object transaction) {
        this.jpaService.closeEntityManager(em, transaction);
    }

    // Implementation specific methods --------------------------------------------------------------------------------------------

    protected CriteriaBuilder getCriteriaBuilder() {
        return getEntityManager().getCriteriaBuilder();
    }

    // Implementation specific methods --------------------------------------------------------------------------------------------

    /*
     * (non-Javadoc)
     * @see org.jbpm.services.task.persistence.TaskQueryCriteriaUtil#implSpecificCreatePredicateFromSingleCriteria(javax.persistence.criteria.CriteriaQuery, javax.persistence.criteria.CriteriaBuilder, javax.persistence.criteria.Root, org.jbpm.query.jpa.data.QueryCriteria, org.jbpm.query.jpa.data.QueryWhere)
     */
    @Override
    protected <R,T> Predicate implSpecificCreatePredicateFromSingleCriteria(
            CriteriaQuery<R> query,
            CriteriaBuilder builder,
            Class queryType,
            QueryCriteria criteria,
            QueryWhere queryWhere) {

        Class newJoinRootClass = getJoinRootClassAndAddNeededJoin(query, builder, queryType, criteria, queryWhere);

        Attribute attr = getCriteriaAttributes().get(newJoinRootClass).get(criteria.getListId());

        if( TaskImpl.class.equals(newJoinRootClass) ) {
            return createJoinedTaskPredicateFromSingleCriteria(query, builder, attr, criteria, queryWhere);
        } else if( ProcessInstanceLog.class.equals(newJoinRootClass)
                || VariableInstanceLog.class.equals(newJoinRootClass) ) {
            return createJoinedAuditPredicateFromSingleCriteria(query, builder, attr, criteria, queryWhere);
        } else {
            throw new IllegalStateException("Unexpected query type " + queryType.getSimpleName() + " when processing criteria "
                    + getQueryParameterIdNameMap().get(Integer.parseInt(criteria.getListId())) + " (" + criteria.toString() + ")");
        }
    }

    @Override
    protected <T> List<T> createQueryAndCallApplyMetaCriteriaAndGetResult(QueryWhere queryWhere, CriteriaQuery<T> criteriaQuery, CriteriaBuilder builder) {
        return sharedCreateQueryAndCallApplyMetaCriteriaAndGetResult(queryWhere, criteriaQuery, builder, jpaService);
    }
}
