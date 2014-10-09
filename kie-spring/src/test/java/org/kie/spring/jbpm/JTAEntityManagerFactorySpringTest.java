package org.kie.spring.jbpm;

import static org.kie.internal.query.QueryParameterIdentifiers.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.naming.InitialContext;
import javax.transaction.UserTransaction;

import bitronix.tm.resource.jdbc.PoolingDataSource;
import org.drools.core.event.DefaultProcessEventListener;
import org.jbpm.process.audit.AuditLogService;
import org.jbpm.process.audit.ProcessInstanceLog;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.kie.api.event.process.ProcessNodeTriggeredEvent;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.runtime.manager.RuntimeManager;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.api.task.TaskService;
import org.kie.api.task.model.Status;
import org.kie.api.task.model.TaskSummary;
import org.kie.internal.task.api.InternalTaskService;
import org.kie.internal.task.api.TaskQueryService;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.jta.JtaTransactionManager;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.notNullValue;

public class JTAEntityManagerFactorySpringTest extends AbstractJbpmSpringTest {

    @Test
    public void testSpringWithJTAAndEMF() throws Exception{

        context = new ClassPathXmlApplicationContext("jbpm/jta-emf/jta-emf-spring.xml");

        RuntimeManager manager = (RuntimeManager) context.getBean("runtimeManager");

        RuntimeEngine engine = manager.getRuntimeEngine(null);
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
    }

    @Test
    public void testSpringWithJTAAndEMFwithRollback() throws Exception{

        context = new ClassPathXmlApplicationContext("jbpm/jta-emf/jta-emf-spring.xml");

        RuntimeManager manager = (RuntimeManager) context.getBean("runtimeManager");

        RuntimeEngine engine = manager.getRuntimeEngine(null);
        KieSession ksession = engine.getKieSession();
        TaskService taskService = engine.getTaskService();


        AuditLogService logService = (AuditLogService) context.getBean("logService");

        UserTransaction ut = (UserTransaction) new InitialContext().lookup("java:comp/UserTransaction");
        ut.begin();
        ProcessInstance processInstance = ksession.startProcess("com.sample.bpmn.hello");
        long processInstanceId = processInstance.getId();
        ut.rollback();

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
    }

    @Test
    public void testSpringWithJTAAndEMFwithUserTransaction() throws Exception{

        context = new ClassPathXmlApplicationContext("jbpm/jta-emf/tx-jta-emf-spring.xml");

        RuntimeManager manager = (RuntimeManager) context.getBean("runtimeManager");

        RuntimeEngine engine = manager.getRuntimeEngine(null);
        KieSession ksession = engine.getKieSession();
        TaskService taskService = engine.getTaskService();

        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("owner", "john");
        JtaTransactionManager jtaTxm = (JtaTransactionManager) context.getBean("jbpmTxManager");
        TransactionStatus ut = null;

        try {
            ut = beginTransaction(jtaTxm);

            ProcessInstance processInstance = ksession.startProcess("expense", parameters);

            executeTasksByProcessByTaskName(processInstance.getId(), "create", taskService);

            jtaTxm.commit(ut);
            ut = beginTransaction(jtaTxm);

            executeTasksByProcessByTaskName(processInstance.getId(), "edit", taskService);

            jtaTxm.commit(ut);
            ut = beginTransaction(jtaTxm);

            //The problem happens here. There is no task "edit", but it should be.
            executeTasksByProcessByTaskName(processInstance.getId(), "edit", taskService);

            jtaTxm.commit(ut);
            ut = beginTransaction(jtaTxm);

            executeTasksByProcessByTaskName(processInstance.getId(), "edit", taskService);

            jtaTxm.commit(ut);
            ut = beginTransaction(jtaTxm);

            executeTasksByProcessByTaskName(processInstance.getId(), "delete", taskService);

            jtaTxm.commit(ut);
        } finally {
//            if (ut != null && javax.transaction.Status.STATUS_ACTIVE == ut.getStatus()) {
//                jtaTxm.rollback(ut);
//            }
        }
    }

    private TransactionStatus beginTransaction(JtaTransactionManager jtaTxm) throws Exception {
//        UserTransaction ut = (UserTransaction) new InitialContext().lookup("java:comp/UserTransaction");
//        ut.begin();
//
//        return ut;

        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        TransactionStatus status = jtaTxm.getTransaction(def);

        return status;
    }

    private void executeTasksByProcessByTaskName(long processId, String taskName, TaskService taskService) {
        Map<String, List<?>> parameters = new HashMap<String, List<?>>();
        parameters.put(PROCESS_INSTANCE_ID_LIST, Arrays.asList(processId));
        parameters.put(TASK_STATUS_LIST, Arrays.asList(Status.Ready, Status.Created, Status.Reserved));
        List<TaskSummary> tasks = ((InternalTaskService) taskService).getTasksByVariousFields("john", parameters, false);

        TaskSummary task = null;
        for (TaskSummary t : tasks) {
            if (t.getName().equalsIgnoreCase(taskName)) {
                task = t;
                break;
            }
        }

        assertThat(task, notNullValue(TaskSummary.class));

        taskService.start(task.getId(), "john");
        taskService.complete(task.getId(), "john", null);
    }
}
