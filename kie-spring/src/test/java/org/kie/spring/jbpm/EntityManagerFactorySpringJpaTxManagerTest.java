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
import org.springframework.context.support.ClassPathXmlApplicationContext;

import javax.naming.InitialContext;
import javax.transaction.UserTransaction;
import java.util.List;

import static junit.framework.Assert.*;

public class EntityManagerFactorySpringJpaTxManagerTest extends AbstractJbpmSpringTest {

    @Test
    public void testSpringWithJpa() throws Exception{

        context = new ClassPathXmlApplicationContext("jbpm/jpa/jpa-spring.xml");

        RuntimeManager manager = (RuntimeManager) context.getBean("runtimeManager");

        RuntimeEngine engine = manager.getRuntimeEngine(null);
        KieSession ksession = engine.getKieSession();
        TaskService taskService = engine.getTaskService();

        ProcessInstance processInstance = ksession.startProcess("com.sample.bpmn.hello");

        System.out.println("Process started");

/*      TODO : ERROR TO BE FIXED AS UNTIL NOW No Tx is created for Audit with a Spring JPATxManager

        java.lang.IllegalStateException: Unable to join EntityManager to transaction: No local transaction to join
	at org.springframework.orm.jpa.ExtendedEntityManagerCreator$ExtendedEntityManagerInvocationHandler.doJoinTransaction(ExtendedEntityManagerCreator.java:407)
	at org.springframework.orm.jpa.ExtendedEntityManagerCreator$ExtendedEntityManagerInvocationHandler.invoke(ExtendedEntityManagerCreator.java:350)
	at com.sun.proxy.$Proxy40.joinTransaction(Unknown Source)
	at org.jbpm.process.audit.strategy.StandaloneJtaStrategy.joinTransaction(StandaloneJtaStrategy.java:54)
	at org.jbpm.process.audit.JPAAuditLogService.joinTransaction(JPAAuditLogService.java:331)

        AuditLogService logService = (AuditLogService) context.getBean("logService");
        ProcessInstanceLog log = logService.findProcessInstance(processInstance.getId());
        assertNotNull(log);*/

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

}
