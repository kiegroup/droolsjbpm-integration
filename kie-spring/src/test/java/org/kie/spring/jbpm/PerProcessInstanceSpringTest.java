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
import javax.persistence.EntityManager;

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
        KieSession ksession = getKieSession();
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
}
