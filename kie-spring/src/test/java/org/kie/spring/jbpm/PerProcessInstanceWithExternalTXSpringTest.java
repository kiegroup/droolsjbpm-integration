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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.naming.InitialContext;
import javax.transaction.UserTransaction;

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
import org.kie.internal.runtime.manager.SessionNotFoundException;
import org.kie.internal.runtime.manager.context.ProcessInstanceIdContext;

@RunWith(Parameterized.class)
public class PerProcessInstanceWithExternalTXSpringTest extends AbstractJbpmSpringParameterizedTest {

    @Parameterized.Parameters(name = "{index}: {0}")
    public static Collection<Object[]> contextPath() {
        Object[][] data = new Object[][] {
                { JTA_EMF_PER_PROCESS_INSTANCE_PATH, ProcessInstanceIdContext.get() }
        };
        return Arrays.asList(data);
    };

    public PerProcessInstanceWithExternalTXSpringTest(String contextPath, Context<?> runtimeManagerContext) {
        super(contextPath, runtimeManagerContext);
    }

   
    @Test(timeout=100000000)
    public void testCompleteTasksWithSeparateThread() throws Exception {

        final RuntimeManager manager = getManager();
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
        
        manager.disposeRuntimeEngine(engine);

        final Long processInstanceId = processInstance.getId();
        final Long firstTaskId = tasks.get(0).getId();
        
        Thread t = new Thread(new Runnable() {
            
            
            @Override
            public void run() {
                UserTransaction ut = null;
                try {
                    ut = InitialContext.doLookup("java:comp/UserTransaction");
                
                    ut.begin();
                    RuntimeEngine engine = manager.getRuntimeEngine(ProcessInstanceIdContext.get(processInstanceId));
                    // call ksession so it will initiate first tx
                    engine.getKieSession();
                    TaskService taskService = engine.getTaskService();
    
                    taskService.start(firstTaskId, USER_JOHN);
                    taskService.complete(firstTaskId, USER_JOHN, null);
                    
                    System.out.println("Task for john completed");
                    
                    manager.disposeRuntimeEngine(engine);
                    
                    ut.commit();
                } catch (Exception e) {
                    e.printStackTrace();
                    try {
                        ut.rollback();
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                }
            }
        });        
        t.start();
        t.join();
        

        tasks = taskService.getTasksAssignedAsPotentialOwner(USER_MARY, "en-UK");
        System.out.println("Found " + tasks.size() + " task(s) for user '"+USER_MARY+"'");
        assertEquals(1, tasks.size());
        
        final Long secondTaskId = tasks.get(0).getId();
        
        t = new Thread(new Runnable() {
            
            
            @Override
            public void run() {
                UserTransaction ut = null;
                try {
                    ut = InitialContext.doLookup("java:comp/UserTransaction");
                    ut.begin();
                    RuntimeEngine engine = manager.getRuntimeEngine(ProcessInstanceIdContext.get(processInstanceId));
                    engine.getKieSession();
                    TaskService taskService = engine.getTaskService();
    
                    taskService.start(secondTaskId, USER_MARY);
                    taskService.complete(secondTaskId, USER_MARY, null);
                    
                    System.out.println("Task for mary completed");
                    
                    manager.disposeRuntimeEngine(engine);
                    ut.commit();
                } catch (Exception e) {
                    e.printStackTrace();
                    try {
                        ut.rollback();
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                }
            }
        });        
        t.start();
        t.join();

        try {
            engine = manager.getRuntimeEngine(ProcessInstanceIdContext.get(processInstance.getId()));
            ksession = engine.getKieSession();
            processInstance = ksession.getProcessInstance(processInstance.getId());
            assertNull(processInstance);
            System.out.println("Process instance completed");
        } catch (SessionNotFoundException e) {
            
        }
    }
}
