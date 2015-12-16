/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package org.kie.spring.jbpm;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.manager.Context;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.api.task.TaskService;
import org.kie.api.task.model.Status;
import org.kie.api.task.model.TaskSummary;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.jta.JtaTransactionManager;
import org.springframework.transaction.support.DefaultTransactionDefinition;

@RunWith(Parameterized.class)
public class ProcessFlowSpringTest extends AbstractJbpmSpringParameterizedTest {

    @Parameterized.Parameters(name = "{index}: {0}")
    public static Collection<Object[]> contextPath() {
        Object[][] data = new Object[][] {
                { JTA_EMF_SINGLETON_PATH, null},
                { JTA_EM_SINGLETON_PATH, null}
        };
        return Arrays.asList(data);
    };

    public ProcessFlowSpringTest(String contextPath, Context<?> runtimeManagerContext) {
        super(contextPath, runtimeManagerContext);
    }

    @Test
    public void testEventBasedGatewayWithUserTransaction() throws Exception {

        KieSession ksession = getKieSession();
        TaskService taskService = getTaskService();
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("owner", USER_JOHN);
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
        List<TaskSummary> tasks = taskService.getTasksAssignedAsPotentialOwnerByStatus(USER_JOHN, status, "en-UK");

        TaskSummary task = null;
        for (TaskSummary t : tasks) {
            if (t.getName().equalsIgnoreCase(taskName)) {
                task = t;
                break;
            }
        }

        assertNotNull(task);

        taskService.start(task.getId(), USER_JOHN);
        taskService.complete(task.getId(), USER_JOHN, null);
    }
}
