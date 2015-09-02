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
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionDefinition;

/**
 * Test used to verify correct initialization of runtime manager and transaction functionality in different configuration.
 */
@RunWith(Parameterized.class)
public class RuntimeManagerInitSpringTest extends AbstractJbpmSpringParameterizedTest {

    @Parameterized.Parameters(name = "{index}: {0}")
    public static Collection<Object[]> contextPath() {
        Object[][] data = new Object[][] {
                { JTA_EMF_SINGLETON_PATH, EmptyContext.get() },
                { JTA_EM_SINGLETON_PATH, EmptyContext.get() },
                { LOCAL_EMF_SINGLETON_PATH, EmptyContext.get() },
                { LOCAL_EM_SINGLETON_PATH, EmptyContext.get() },
                { JTA_EMF_PER_PROCESS_INSTANCE_PATH, ProcessInstanceIdContext.get() },
                { JTA_EM_PER_PROCESS_INSTANCE_PATH, ProcessInstanceIdContext.get() },
                { LOCAL_EMF_PER_PROCESS_INSTANCE_PATH, ProcessInstanceIdContext.get() },
                { LOCAL_EM_PER_PROCESS_INSTANCE_PATH, ProcessInstanceIdContext.get() },
                { JTA_EMF_PER_REQUEST_PATH, EmptyContext.get() },
                { JTA_EM_PER_REQUEST_PATH, EmptyContext.get() },
                { LOCAL_EMF_PER_REQUEST_PATH, EmptyContext.get() },
                { LOCAL_EM_PER_REQUEST_PATH, EmptyContext.get() }
        };
        return Arrays.asList(data);
    };

    public RuntimeManagerInitSpringTest(String contextPath, Context<?> runtimeManagerContext) {
        super(contextPath, runtimeManagerContext);
    }

    @Test
    public void testSimpleTaskInvocation() throws Exception{
        RuntimeManager manager = getManager();
        RuntimeEngine engine = getEngine();
        KieSession ksession = getKieSession();
        TaskService taskService = getTaskService();

        ProcessInstance processInstance = ksession.startProcess(SAMPLE_HELLO_PROCESS_ID);

        System.out.println("Process started");
        AuditLogService logService = getLogService();
        ProcessInstanceLog log = logService.findProcessInstance(processInstance.getId());
        assertNotNull(log);

        List<TaskSummary> tasks = taskService.getTasksAssignedAsPotentialOwner(USER_JOHN, "en-UK");
        System.out.println("Found " + tasks.size() + " task(s) for user '"+USER_JOHN+"'");
        assertEquals(1, tasks.size());

        long taskId = tasks.get(0).getId();
        taskService.start(taskId, USER_JOHN);
        taskService.complete(taskId, USER_JOHN, null);

        tasks = taskService.getTasksAssignedAsPotentialOwner(USER_MARY, "en-UK");
        System.out.println("Found " + tasks.size() + " task(s) for user '"+USER_MARY+"'");
        assertEquals(1, tasks.size());

        taskId = tasks.get(0).getId();
        taskService.start(taskId, USER_MARY);
        taskService.complete(taskId, USER_MARY, null);

        processInstance = ksession.getProcessInstance(processInstance.getId());
        assertNull(processInstance);
        System.out.println("Process instance completed");

        manager.disposeRuntimeEngine(engine);
    }

    @Test
    public void testSimpleTaskInvocationWithRollback() throws Exception{
        AbstractPlatformTransactionManager transactionManager = getTransactionManager();
        RuntimeManager manager = getManager();

        RuntimeEngine engine = getEngine();
        KieSession ksession = getKieSession();

        AuditLogService logService = getLogService();
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        TransactionStatus status = transactionManager.getTransaction(def);
        ProcessInstance processInstance = ksession.startProcess(SAMPLE_HELLO_PROCESS_ID);
        long processInstanceId = processInstance.getId();
        transactionManager.rollback(status);

        processInstance = ksession.getProcessInstance(processInstanceId);

        assertNull("Process instance not rolled back", processInstance);
        System.out.println("Process instance rolled back");

        List<TaskSummary> tasks = getTaskService().getTasksAssignedAsPotentialOwner(USER_JOHN, "en-UK");
        System.out.println("Found " + tasks.size() + " task(s) for user '"+USER_JOHN+"'");
        assertEquals(0, tasks.size());

        ProcessInstanceLog log = logService.findProcessInstance(processInstanceId);
        assertNull(log);

        manager.disposeRuntimeEngine(engine);
    }
}
