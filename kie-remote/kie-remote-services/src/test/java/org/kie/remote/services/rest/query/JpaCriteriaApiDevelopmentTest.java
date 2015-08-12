package org.kie.remote.services.rest.query;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaBuilder.In;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import org.jbpm.process.audit.ProcessInstanceLog;
import org.jbpm.process.audit.ProcessInstanceLog_;
import org.jbpm.process.audit.VariableInstanceLog;
import org.jbpm.process.audit.VariableInstanceLog_;
import org.jbpm.services.task.impl.model.TaskDataImpl_;
import org.jbpm.services.task.impl.model.TaskImpl;
import org.jbpm.services.task.impl.model.TaskImpl_;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

@Ignore
public class JpaCriteriaApiDevelopmentTest extends AbstractQueryResourceTest {

    @Before
    public void init() {
        runtimeManager = createRuntimeManager(PROCESS_STRING_VAR_FILE);
        engine = getRuntimeEngine();
        ksession = engine.getKieSession();
        taskService = engine.getTaskService();
        
        addObjectProcessInstances = false;
        setupTestData();
    }
    
    @After
    public void cleanup() {
        if( runtimeManager != null ) { 
            runtimeManager.disposeRuntimeEngine(engine);
            runtimeManager.close();
        }
    }

    @Test
    public void multipleTasksPerProcessInstanceJoinTest() throws Exception {
       
        EntityManager em = getEmf().createEntityManager();
        CriteriaBuilder builder = em.getCriteriaBuilder();
       
        boolean basis = false;
        // VAR

        Query query;
        { 
            CriteriaQuery varLogQuery = builder.createQuery(VariableInstanceLog.class);
            varLogQuery.from(VariableInstanceLog.class);
            query = em.createQuery(varLogQuery);
        }
        List<VariableInstanceLog> varLogs = query.getResultList(); 
        long procInstId = varLogs.get(0).getProcessInstanceId();

        if( basis ) { 
            int numVarLogs = varLogs.size();
            logger.debug( "Var logs: " + numVarLogs );
            Set<Long> uniqueIds = new HashSet<Long>(numVarLogs);
            for( VariableInstanceLog varLog : varLogs ) { 
                assertTrue( "Unique var logs", uniqueIds.add(varLog.getId()) );
            }

            // PROC
            { 
                CriteriaQuery procLogQuery = builder.createQuery(ProcessInstanceLog.class);
                procLogQuery.from(ProcessInstanceLog.class);
                query = em.createQuery(procLogQuery);
            }
            List<ProcessInstanceLog> procLogs = query.getResultList(); 

            int numProcLogs = procLogs.size();
            logger.debug( "Proc logs: " + numProcLogs );

            // TASK
            {
                CriteriaQuery taskQuery = builder.createQuery(TaskImpl.class);
                taskQuery.from(TaskImpl.class);
                query = em.createQuery(taskQuery);
            }
            List<TaskImpl> tasks = query.getResultList(); 

            int numTasks = tasks.size();
            logger.debug( "Tasks: " + numTasks );
        } 
        
        // TEST
        {
            CriteriaQuery varLogQuery = builder.createQuery(VariableInstanceLog.class);
            Root<VariableInstanceLog> varLogRoot = varLogQuery.from(VariableInstanceLog.class);
            Root<ProcessInstanceLog> procLogRoot = varLogQuery.from(ProcessInstanceLog.class);
            
            varLogQuery.select(varLogRoot);

            Predicate varProcJoinPred = builder.equal( 
                    varLogRoot.get(VariableInstanceLog_.processInstanceId), 
                    procLogRoot.get(ProcessInstanceLog_.processInstanceId));
           
            Subquery<Long> taskQuery = varLogQuery.subquery(Long.class);
            Root<TaskImpl> taskQueryRoot = taskQuery.from(TaskImpl.class);
            taskQuery.select(taskQueryRoot.get(TaskImpl_.taskData).get(TaskDataImpl_.processInstanceId));
            Predicate taskQueryPred = builder.equal(
                    varLogRoot.get(VariableInstanceLog_.processInstanceId),
                    taskQueryRoot.get(TaskImpl_.taskData).get(TaskDataImpl_.processInstanceId));
            taskQuery.where(taskQueryPred);
            
            Predicate taskSubqueryPred = builder.in(varLogRoot.get(VariableInstanceLog_.processInstanceId)).value(taskQuery);
                    
            Predicate pred = builder.and(
                    varProcJoinPred,
                    taskSubqueryPred,
                    builder.equal(varLogRoot.get(VariableInstanceLog_.processInstanceId), procInstId) );
           
            query = em.createQuery(varLogQuery);
        }
        
        varLogs = query.getResultList(); 

        int numVarLogs = varLogs.size();
        Set<Long> uniqueIds = new HashSet<Long>(numVarLogs);
        for( VariableInstanceLog varLog : varLogs ) { 
            assertTrue( "Unique var logs", uniqueIds.add(varLog.getId()) );
        }
        
        Assert.assertEquals( 7, numVarLogs );
    }
}
