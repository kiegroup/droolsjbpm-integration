package org.kie.spring.jbpm;

import org.jbpm.process.audit.AuditLogService;
import org.jbpm.process.audit.ProcessInstanceLog;
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

import java.util.List;

import static junit.framework.Assert.*;

public class LocalEntityManagerFactorySpringTest extends AbstractJbpmSpringTest {

    @Test
    public void testSpringWithJTAAndEMF() throws Exception{


        context = new ClassPathXmlApplicationContext("jbpm/local-emf/local-emf-spring.xml");

        AbstractPlatformTransactionManager aptm = (AbstractPlatformTransactionManager) context.getBean( "jbpmTxManager" );
        RuntimeManager manager = (RuntimeManager) context.getBean("runtimeManager");

        RuntimeEngine engine = manager.getRuntimeEngine(ProcessInstanceIdContext.get());
        KieSession ksession = engine.getKieSession();
        TaskService taskService = engine.getTaskService();

        long ksessionId = ksession.getIdentifier();

        ProcessInstance processInstance = ksession.startProcess("com.sample.bpmn.hello");

        System.out.println("Process started");

        manager.disposeRuntimeEngine(engine);

        engine = manager.getRuntimeEngine(ProcessInstanceIdContext.get(processInstance.getId()));
        ksession = engine.getKieSession();
        taskService = engine.getTaskService();

        assertEquals(ksessionId, ksession.getIdentifier());

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
    public void testSpringWithJTAAndEMFwithRollback() throws Exception{


        context = new ClassPathXmlApplicationContext("jbpm/local-emf/local-emf-spring.xml");

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
