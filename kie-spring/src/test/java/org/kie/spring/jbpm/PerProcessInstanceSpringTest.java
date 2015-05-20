package org.kie.spring.jbpm;

import static org.junit.Assert.*;

import java.util.List;

import javax.persistence.EntityManager;

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

/**
 * Tests verifying per process instance configuration.
 */
public class PerProcessInstanceSpringTest extends AbstractJbpmSpringTest {

    @Test
    public void testNoSessionInDbAfterInit() throws Exception {

        context = new ClassPathXmlApplicationContext("jbpm/local-em/per-process-instance.xml");
        EntityManager em = (EntityManager) context.getBean("jbpmEM");
        // check that there is no sessions in db
        List<?> sessions = em.createQuery("from SessionInfo").getResultList();
        assertNotNull(sessions);
        assertEquals(0, sessions.size());

        RuntimeManager manager = (RuntimeManager) context.getBean("runtimeManager");

        // after creating per process instance manager init creates temp session that shall be directly destroyed
        sessions = em.createQuery("from SessionInfo").getResultList();
        assertNotNull(sessions);
        assertEquals(0, sessions.size());
    }
    
    /**
     * Test verifying ProcessInstanceIdContext functionality for per process instance Runtime manager.
     *
     * @throws Exception
     */
    @Test
    public void testRecoveringKieSessionByProcessInstanceIdContext() throws Exception {

        context = new ClassPathXmlApplicationContext("jbpm/local-emf/per-process-instance.xml");

        RuntimeManager manager = (RuntimeManager) context.getBean("runtimeManager");

        // Creating new runtime engine with new kie session
        RuntimeEngine engine = manager.getRuntimeEngine(ProcessInstanceIdContext.get());
        KieSession ksession = engine.getKieSession();
        long ksessionId = ksession.getIdentifier();

        ProcessInstance processInstance = ksession.startProcess("com.sample.bpmn.hello");

        System.out.println("Process started");

        manager.disposeRuntimeEngine(engine);

        // Creating new runtime engine, should return kie session defined previously as we pass its process instance id in context
        engine = manager.getRuntimeEngine(ProcessInstanceIdContext.get(processInstance.getId()));
        ksession = engine.getKieSession();
        TaskService taskService = engine.getTaskService();

        assertEquals(ksessionId, ksession.getIdentifier());

        // Process can continue with new task service
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
}
