/*
 * Copyright 2015 JBoss Inc
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
import java.util.List;

import javax.naming.InitialContext;
import javax.transaction.UserTransaction;

import org.drools.persistence.jta.JtaTransactionManager;
import org.jbpm.process.audit.AuditLogService;
import org.jbpm.process.audit.ProcessInstanceLog;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.manager.Context;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.api.task.TaskService;
import org.kie.api.task.model.TaskSummary;

@RunWith(Parameterized.class)
public class UserManagedSharedTaskServiceSpringTest extends AbstractJbpmSpringParameterizedTest {

    @Parameterized.Parameters(name = "{index}: {0}")
    public static Collection<Object[]> contextPath() {
        Object[][] data = new Object[][] {
                { SHARED_TASKSERVICE_JTA_EM_SINGLETON_PATH, null },
                { SHARED_TASKSERVICE_JTA_EMF_SINGLETON_PATH, null }
        };
        return Arrays.asList(data);
    };

    public UserManagedSharedTaskServiceSpringTest(String contextPath, Context<?> runtimeManagerContext) {
        super(contextPath, runtimeManagerContext);
    }

	/* The purpose of the shared entity manager is to allow the application domain
	 * and JBPM domain to be persisted by a single entity manager and transaction. 
	 */
    @Test
    public void testSpringWithJTAAndSharedEMFAndUserManagedTx() throws Exception {

        UserTransaction ut = (UserTransaction) new InitialContext().lookup(JtaTransactionManager.DEFAULT_USER_TRANSACTION_NAME);
        ut.begin();

        KieSession ksession = getKieSession();
        TaskService taskService = getTaskService();
        ProcessInstance processInstance = ksession.startProcess(SAMPLE_HELLO_PROCESS_ID);

        System.out.println("Process started");

        AuditLogService logService = getLogService();
        ProcessInstanceLog log = logService.findProcessInstance(processInstance.getId());
        assertNotNull(log);

        List<TaskSummary> tasks = taskService.getTasksAssignedAsPotentialOwner(USER_JOHN, "en-UK");
        System.out.println("Found " + tasks.size() + " task(s) for user '"+ USER_JOHN + "'");
        assertEquals(1, tasks.size());

        long taskId = tasks.get(0).getId();
        taskService.start(taskId, USER_JOHN);
        taskService.complete(taskId, USER_JOHN, null);

        tasks = taskService.getTasksAssignedAsPotentialOwner(USER_MARY, "en-UK");
        System.out.println("Found " + tasks.size() + " task(s) for user '"+ USER_MARY + "'");
        assertEquals(1, tasks.size());

        taskId = tasks.get(0).getId();
        taskService.start(taskId, USER_MARY);
        taskService.complete(taskId, USER_MARY, null);

        processInstance = ksession.getProcessInstance(processInstance.getId());
        assertNull(processInstance);
        System.out.println("Process instance completed");

        ut.commit();
    }

}
