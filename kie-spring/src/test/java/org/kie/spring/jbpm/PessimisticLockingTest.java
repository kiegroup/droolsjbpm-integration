package org.kie.spring.jbpm;

import java.lang.reflect.Field;
import java.util.List;

import org.drools.core.command.impl.CommandBasedStatefulKnowledgeSession;
import org.drools.persistence.SingleSessionCommandService;
import org.jbpm.process.audit.AuditLogService;
import org.jbpm.process.audit.ProcessInstanceLog;
import org.junit.Test;
import org.kie.api.runtime.Environment;
import org.kie.api.runtime.EnvironmentName;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.runtime.manager.RuntimeManager;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.api.task.TaskService;
import org.kie.api.task.model.TaskSummary;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import static junit.framework.Assert.*;

public class PessimisticLockingTest extends AbstractJbpmSpringTest {

    @Test
    public void testSpringWithJpaLocking() throws Exception {

        context = new ClassPathXmlApplicationContext("jbpm/jpa/jpa-locking-spring.xml");

        RuntimeManager manager = (RuntimeManager) context.getBean("runtimeManager");

        RuntimeEngine engine = manager.getRuntimeEngine(null);
        KieSession ksession = engine.getKieSession();
        TaskService taskService = engine.getTaskService();

        ProcessInstance processInstance = ksession.startProcess("com.sample.bpmn.hello");

        validatePessimisticLockingUse(ksession);
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
    }

    private void validatePessimisticLockingUse(KieSession ksession) throws Exception { 
        // do an ugly hack to get the info
       CommandBasedStatefulKnowledgeSession cmdBasedKsession = (CommandBasedStatefulKnowledgeSession) ksession;
       SingleSessionCommandService sscs = (SingleSessionCommandService) cmdBasedKsession.getCommandService();
       // yes, this will break some day, and when it does, just delete this entire test, which isn't that good anyways.. :/ 
       Field envField = SingleSessionCommandService.class.getDeclaredField("env");
       envField.setAccessible(true);
       Environment env = (Environment) envField.get(sscs);
       
       // verify  pessimistic lockings
       Boolean pessLocking = (Boolean) env.get(EnvironmentName.USE_PESSIMISTIC_LOCKING);
       assertNotNull( "Pessimistic locking not set in the environment: null object", pessLocking );
       assertTrue( "Pessimistic locking not set in the environment: null object", pessLocking );
    }
}
