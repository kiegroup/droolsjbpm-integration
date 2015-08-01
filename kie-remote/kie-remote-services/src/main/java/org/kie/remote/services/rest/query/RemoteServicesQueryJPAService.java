package org.kie.remote.services.rest.query;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.jbpm.process.audit.JPAService;
import org.jbpm.process.audit.NodeInstanceLog;
import org.jbpm.process.audit.ProcessInstanceLog;
import org.jbpm.process.audit.VariableInstanceLog;
import org.jbpm.query.jpa.data.QueryWhere;
import org.jbpm.query.jpa.impl.QueryCriteriaUtil;
import org.jbpm.services.task.query.TaskSummaryImpl;
import org.kie.api.task.UserGroupCallback;
import org.kie.api.task.model.TaskSummary;

public class RemoteServicesQueryJPAService extends JPAService {

    public RemoteServicesQueryJPAService(EntityManagerFactory emf) {
        super(emf);
    }
    
    protected EntityManager getEntityManager() {
        return persistenceStrategy.getEntityManager();
    }

    protected Object joinTransaction(EntityManager em) {
        return persistenceStrategy.joinTransaction(em);
    }

    protected void closeEntityManager(EntityManager em, Object transaction) {
       persistenceStrategy.leaveTransaction(em, transaction);
    }
   
    // Query specific logic -------------------------------------------------------------------------------------------------------
    
    private final RemoteServicesAuditQueryCriteriaUtil auditQueryUtil = new RemoteServicesAuditQueryCriteriaUtil(this);
    private final RemoteServicesTaskQueryCriteriaUtil taskQueryUtil = new RemoteServicesTaskQueryCriteriaUtil(this);
    
    protected QueryCriteriaUtil getQueryCriteriaUtil(Class queryType) { 
        if( queryType.equals(TaskSummaryImpl.class) ) { 
            return taskQueryUtil;
        } else if( queryType.equals(ProcessInstanceLog.class) 
                || queryType.equals(VariableInstanceLog.class) 
                || queryType.equals(NodeInstanceLog.class) ) {
            return auditQueryUtil;
        } else { 
            throw new IllegalArgumentException("Unsupported query type: " + queryType.getName());
        }
    }
    
    /**
     *
     * @param queryWhere
     * @param queryType
     * @return The result of the query, a list of type T
     */
    public <T> List<T> doQuery(QueryWhere queryWhere, Class<T> queryType) { 
       return auditQueryUtil.doCriteriaQuery(queryWhere, queryType);
    }
   
    public List<TaskSummaryImpl> doTaskSummaryQuery(String userId, UserGroupCallback userGroupCallback, QueryWhere queryWhere) { 
        assert userGroupCallback != null : "The " + UserGroupCallback.class.getSimpleName() + " instance is null!";
        return taskQueryUtil.doCriteriaQuery(userId, userGroupCallback, queryWhere);
    }
   
}
