package org.kie.spring.jbpm;

import java.util.List;

import javax.persistence.EntityManager;

import bitronix.tm.resource.jdbc.PoolingDataSource;
import org.jbpm.process.audit.AuditLogService;
import org.jbpm.process.audit.ProcessInstanceLog;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.runtime.manager.RuntimeManager;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.api.task.TaskService;
import org.kie.api.task.model.TaskSummary;
import org.kie.internal.runtime.manager.context.ProcessInstanceIdContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import static junit.framework.Assert.*;

public class LocalEntityManagerSpringTest extends AbstractJbpmSpringTest {

    @Test
    public void testSpringWithJTAAndEM() throws Exception{


        context = new ClassPathXmlApplicationContext("jbpm/local-em/per-process-instance.xml");
        EntityManager em = (EntityManager) context.getBean("jbpmEM");
        // check that there is no sessions in db
        List<?> sessions = em.createQuery("from SessionInfo").getResultList();
        assertNotNull(sessions);
        assertEquals(0, sessions.size());

        AbstractPlatformTransactionManager aptm = (AbstractPlatformTransactionManager) context.getBean( "jbpmTxManager" );
        RuntimeManager manager = (RuntimeManager) context.getBean("runtimeManager");

        // after creating per process instance manager init creates temp session that shall be directly destroyed
        sessions = em.createQuery("from SessionInfo").getResultList();
        assertNotNull(sessions);
        assertEquals(0, sessions.size());

        RuntimeEngine engine = manager.getRuntimeEngine(ProcessInstanceIdContext.get());
        KieSession ksession = engine.getKieSession();
        TaskService taskService = engine.getTaskService();

        ProcessInstance processInstance = ksession.startProcess("com.sample.bpmn.hello");

        System.out.println("Process started");

        AuditLogService logService = (AuditLogService) context.getBean("logService");
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        TransactionStatus status = aptm.getTransaction(def);
        ProcessInstanceLog log = logService.findProcessInstance(processInstance.getId());
        aptm.commit(status);
        assertNotNull(log);

        List<TaskSummary> tasks = taskService.getTasksAssignedAsPotentialOwner("john", "en-UK");
        System.out.println("Found " + tasks.size() + " task(s) for user 'john'");
        assertEquals(1, tasks.size());

        long taskId = tasks.get(0).getId();
        taskService.start(taskId, "john");
        taskService.complete(taskId, "john", null);

        tasks = taskService.getTasksAssignedAsPotentialOwner("mary", "en-UK");
        System.out.println("Found " + tasks.size() + " task(s) for user 'mary'");
        assertEquals(1, tasks.size());

        taskId = tasks.get(0).getId();
        taskService.start(taskId, "mary");
        taskService.complete(taskId, "mary", null);

        processInstance = ksession.getProcessInstance(processInstance.getId());
        assertNull(processInstance);
        System.out.println("Process instance completed");
    }

    @Test
    public void testSpringWithJTAAndEMwithRollback() throws Exception{


        context = new ClassPathXmlApplicationContext("jbpm/local-em/per-process-instance.xml");

        AbstractPlatformTransactionManager aptm = (AbstractPlatformTransactionManager) context.getBean( "jbpmTxManager" );
        RuntimeManager manager = (RuntimeManager) context.getBean("runtimeManager");

        RuntimeEngine engine = manager.getRuntimeEngine(ProcessInstanceIdContext.get());
        KieSession ksession = engine.getKieSession();
        TaskService taskService = engine.getTaskService();


        AuditLogService logService = (AuditLogService) context.getBean("logService");

        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        TransactionStatus status = aptm.getTransaction(def);
        ProcessInstance processInstance = ksession.startProcess("com.sample.bpmn.hello");
        long processInstanceId = processInstance.getId();
        aptm.rollback(status);

        processInstance = ksession.getProcessInstance(processInstanceId);

        if (processInstance == null) {
            System.out.println("Process instance rolled back");
        } else {
            throw new IllegalArgumentException("Process instance not rolled back");
        }

        List<TaskSummary> tasks = taskService.getTasksAssignedAsPotentialOwner("john", "en-UK");
        System.out.println("Found " + tasks.size() + " task(s) for user 'john'");
        assertEquals(0, tasks.size());

        def = new DefaultTransactionDefinition();
        status = aptm.getTransaction(def);
        ProcessInstanceLog log = logService.findProcessInstance(processInstanceId);
        aptm.commit(status);
        assertNull(log);
    }
}
