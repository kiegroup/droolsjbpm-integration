package org.kie.spring.jbpm;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.runtime.manager.RuntimeManager;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.api.task.TaskService;
import org.kie.api.task.model.Status;
import org.kie.api.task.model.TaskSummary;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.jta.JtaTransactionManager;
import org.springframework.transaction.support.DefaultTransactionDefinition;

public class ProcessFlowSpringTest extends AbstractJbpmSpringTest {

    @Test
    public void testEventBasedGatewayWithUserTransaction() throws Exception{

        context = new ClassPathXmlApplicationContext("jbpm/jta-emf/singleton.xml");

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
        List<Status> status = Arrays.asList(Status.Ready, Status.Created, Status.Reserved);
        List<TaskSummary> tasks = taskService.getTasksAssignedAsPotentialOwnerByStatus("john", status, "en-UK");

        TaskSummary task = null;
        for (TaskSummary t : tasks) {
            if (t.getName().equalsIgnoreCase(taskName)) {
                task = t;
                break;
            }
        }

        assertNotNull(task);

        taskService.start(task.getId(), "john");
        taskService.complete(task.getId(), "john", null);
    }
}
