package org.kie.spring.jbpm;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.jbpm.process.audit.AuditLogService;
import org.jbpm.process.audit.ProcessInstanceLog;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.manager.Context;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.runtime.manager.RuntimeManager;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.api.task.TaskService;
import org.kie.api.task.model.TaskSummary;
import org.kie.internal.runtime.manager.context.EmptyContext;
import org.kie.internal.runtime.manager.context.ProcessInstanceIdContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionDefinition;

/**
 * Test used to verify correct initialization of runtime manager and transaction functionality in different configuration.
 */
@RunWith(Parameterized.class)
public class RuntimeManagerInitSpringTest extends AbstractJbpmSpringTest {

    @Parameterized.Parameters(name = "{index}: {0}")
    public static Collection<Object[]> contextPath() {
        Object[][] data = new Object[][] {
                { "jbpm/jta-emf/singleton.xml", EmptyContext.get() },
                { "jbpm/jta-em/singleton.xml", EmptyContext.get() },
                { "jbpm/local-emf/singleton.xml", EmptyContext.get() },
                { "jbpm/local-em/singleton.xml", EmptyContext.get() },
                { "jbpm/jta-emf/per-process-instance.xml", ProcessInstanceIdContext.get() },
                { "jbpm/jta-em/per-process-instance.xml", ProcessInstanceIdContext.get() },
                { "jbpm/local-emf/per-process-instance.xml", ProcessInstanceIdContext.get() },
                { "jbpm/local-em/per-process-instance.xml", ProcessInstanceIdContext.get() },
                { "jbpm/jta-emf/per-request.xml", EmptyContext.get() },
                { "jbpm/jta-em/per-request.xml", EmptyContext.get() },
                { "jbpm/local-emf/per-request.xml", EmptyContext.get() },
                { "jbpm/local-em/per-request.xml", EmptyContext.get() }
        };
        return Arrays.asList(data);
    };

    @Parameterized.Parameter(0)
    public String contextPath;

    @Parameterized.Parameter(1)
    public Context<?> runtimeManagerContext;

    @Test
    public void testSimpleTaskInvocation() throws Exception{

        context = new ClassPathXmlApplicationContext(contextPath);

        RuntimeManager manager = (RuntimeManager) context.getBean("runtimeManager");

        RuntimeEngine engine = manager.getRuntimeEngine(runtimeManagerContext);
        KieSession ksession = engine.getKieSession();
        TaskService taskService = engine.getTaskService();

        ProcessInstance processInstance = ksession.startProcess("com.sample.bpmn.hello");

        System.out.println("Process started");

        AuditLogService logService = (AuditLogService) context.getBean("logService");
        ProcessInstanceLog log = logService.findProcessInstance(processInstance.getId());
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

        manager.disposeRuntimeEngine(engine);
    }

    @Test
    public void testSimpleTaskInvocationWithRollback() throws Exception{

        context = new ClassPathXmlApplicationContext(contextPath);

        AbstractPlatformTransactionManager aptm = (AbstractPlatformTransactionManager) context.getBean( "jbpmTxManager" );
        RuntimeManager manager = (RuntimeManager) context.getBean("runtimeManager");

        RuntimeEngine engine = manager.getRuntimeEngine(runtimeManagerContext);
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

        ProcessInstanceLog log = logService.findProcessInstance(processInstanceId);
        assertNull(log);

        manager.disposeRuntimeEngine(engine);
    }
}
