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

import java.util.Arrays;
import java.util.Collection;
import static org.junit.Assert.*;

import java.util.List;
import javax.naming.InitialContext;
import javax.persistence.EntityManager;
import javax.transaction.UserTransaction;

import org.drools.persistence.jta.JtaTransactionManager;
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
import org.kie.internal.runtime.manager.context.ProcessInstanceIdContext;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionDefinition;

/**
 * Tests verifying per process instance configuration.
 */
@RunWith(Parameterized.class)
public class PerProcessInstanceSpringTest extends AbstractJbpmSpringParameterizedTest {

    @Parameterized.Parameters(name = "{index}: {0}")
    public static Collection<Object[]> contextPath() {
        Object[][] data = new Object[][] {
                { LOCAL_EM_PER_PROCESS_INSTANCE_PATH, ProcessInstanceIdContext.get() },
                { LOCAL_EMF_PER_PROCESS_INSTANCE_PATH, ProcessInstanceIdContext.get() },
                { JTA_EM_PER_PROCESS_INSTANCE_PATH, ProcessInstanceIdContext.get() },
                { JTA_EMF_PER_PROCESS_INSTANCE_PATH, ProcessInstanceIdContext.get() }
        };
        return Arrays.asList(data);
    };

    public PerProcessInstanceSpringTest(String contextPath, Context<?> runtimeManagerContext) {
        super(contextPath, runtimeManagerContext);
    }

    @Test
    public void testNoSessionInDbAfterInit() throws Exception {

        EntityManager entityManager = getEntityManager();
        // check that there is no sessions in db
        List<?> sessions = entityManager.createQuery("from SessionInfo").getResultList();
        assertNotNull(sessions);
        assertEquals(0, sessions.size());

        getManager();

        // after creating per process instance manager init creates temp session that shall be directly destroyed
        sessions = entityManager.createQuery("from SessionInfo").getResultList();
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

        RuntimeManager manager = getManager();
        RuntimeEngine engine = getEngine();
        KieSession ksession = engine.getKieSession();
        long ksessionId = ksession.getIdentifier();

        ProcessInstance processInstance = ksession.startProcess(SAMPLE_HELLO_PROCESS_ID);

        System.out.println("Process started");

        manager.disposeRuntimeEngine(engine);

        // Creating new runtime engine, should return kie session defined previously as we pass its process instance id in context
        engine = manager.getRuntimeEngine(ProcessInstanceIdContext.get(processInstance.getId()));
        ksession = engine.getKieSession();
        TaskService taskService = engine.getTaskService();

        assertEquals(ksessionId, ksession.getIdentifier());

        // Process can continue with new task service
        ProcessInstanceLog log = getLogService().findProcessInstance(processInstance.getId());
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
    public void testProcessWithTaskCompletionWithDispose() throws Exception{
        RuntimeManager manager = getManager();

        final AbstractPlatformTransactionManager transactionManager = getTransactionManager();
        final DefaultTransactionDefinition defTransDefinition = new DefaultTransactionDefinition();

        TransactionStatus status = transactionManager.getTransaction(defTransDefinition);

        // start process 1
        RuntimeEngine runtime = manager.getRuntimeEngine(ProcessInstanceIdContext.get());
        KieSession ksession = runtime.getKieSession();

        assertNotNull(ksession);
        ProcessInstance pi1 = ksession.startProcess(SAMPLE_HELLO_PROCESS_ID);
        assertEquals(ProcessInstance.STATE_ACTIVE, pi1.getState());

        // collect task for process instance 1
        List<Long> taskIds = runtime.getTaskService().getTasksByProcessInstanceId(pi1.getId());
        assertNotNull(taskIds);
        assertEquals(1, taskIds.size());

        Long taskId1 = taskIds.get(0);

        runtime.getTaskService().start(taskId1, USER_JOHN);

        transactionManager.commit(status);
        manager.disposeRuntimeEngine(runtime);

        status = transactionManager.getTransaction(defTransDefinition);
        // start process 2
        RuntimeEngine runtime2 = manager.getRuntimeEngine(ProcessInstanceIdContext.get());
        KieSession ksession2 = runtime2.getKieSession();
        assertNotNull(ksession2);

        ProcessInstance pi2 = ksession2.startProcess(SAMPLE_HELLO_PROCESS_ID);
        assertEquals(ProcessInstance.STATE_ACTIVE, pi2.getState());

        // collect task for process instance 2
        List<Long> taskIds2 = runtime2.getTaskService().getTasksByProcessInstanceId(pi2.getId());
        assertNotNull(taskIds2);
        assertEquals(1, taskIds2.size());

        Long taskId2 = taskIds2.get(0);
        runtime2.getTaskService().start(taskId2, USER_JOHN);
        transactionManager.commit(status);

        manager.disposeRuntimeEngine(runtime2);

        status = transactionManager.getTransaction(defTransDefinition);
        // start and complete first task in process instance 1
        runtime = manager.getRuntimeEngine(ProcessInstanceIdContext.get(pi1.getId()));

        runtime.getTaskService().complete(taskId1, USER_JOHN, null);

        transactionManager.commit(status);
        manager.disposeRuntimeEngine(runtime);

        status = transactionManager.getTransaction(defTransDefinition);
        // start and complete first task in process instance 2
        runtime2 = manager.getRuntimeEngine(ProcessInstanceIdContext.get(pi2.getId()));

        runtime2.getTaskService().complete(taskId2, USER_JOHN, null);

        transactionManager.commit(status);
        manager.disposeRuntimeEngine(runtime);

        status = transactionManager.getTransaction(defTransDefinition);
        runtime = manager.getRuntimeEngine(ProcessInstanceIdContext.get(pi1.getId()));
        taskIds = runtime.getTaskService().getTasksByProcessInstanceId(pi1.getId());
        assertNotNull(taskIds);
        assertEquals(2, taskIds.size());

        taskId1 = taskIds.get(1);
        // start and complete second task in process instance 1

        runtime.getTaskService().start(taskId1, USER_MARY);
        runtime.getTaskService().complete(taskId1, USER_MARY, null);

        transactionManager.commit(status);
        manager.disposeRuntimeEngine(runtime);

        // since process is completed now session should not be there any more
        try {
            manager.getRuntimeEngine(ProcessInstanceIdContext.get(pi1.getId())).getKieSession();
            fail("Session for this (" + pi1.getId() + ") process instance is no more accessible");
        } catch (RuntimeException e) {

        }
        status = transactionManager.getTransaction(defTransDefinition);
        runtime2 = manager.getRuntimeEngine(ProcessInstanceIdContext.get(pi2.getId()));
        taskIds2 = runtime2.getTaskService().getTasksByProcessInstanceId(pi2.getId());
        assertNotNull(taskIds2);
        assertEquals(2, taskIds2.size());

        taskId2 = taskIds2.get(1);

        // start and complete second task in process instance 2
        runtime2.getTaskService().start(taskId2, USER_MARY);
        runtime2.getTaskService().complete(taskId2, USER_MARY, null);

        transactionManager.commit(status);
        manager.disposeRuntimeEngine(runtime2);

        // since process is completed now session should not be there any more
        try {
            manager.getRuntimeEngine(ProcessInstanceIdContext.get(pi2.getId())).getKieSession();
            fail("Session for this (" + pi2.getId() + ") process instance is no more accessible");
        } catch (RuntimeException e) {

        }

    }
}
