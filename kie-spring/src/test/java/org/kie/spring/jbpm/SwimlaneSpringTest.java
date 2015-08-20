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

import java.util.Arrays;
import java.util.Collection;
import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jbpm.process.audit.ProcessInstanceLog;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.manager.Context;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.api.task.TaskService;
import org.kie.api.task.model.TaskSummary;
import org.kie.internal.runtime.manager.context.EmptyContext;

@RunWith(Parameterized.class)
public class SwimlaneSpringTest extends AbstractJbpmSpringParameterizedTest {

    @Parameterized.Parameters(name = "{index}: {0}")
    public static Collection<Object[]> contextPath() {
        Object[][] data = new Object[][] {
                { USERGROUP_CALLBACK_LOCAL_EMF_SINGLETON_PATH, EmptyContext.get() }
             };
        return Arrays.asList(data);
    };

    public SwimlaneSpringTest(String contextPath, Context<?> runtimeManagerContext) {
        super(contextPath, runtimeManagerContext);
    }

    @Test
    public void testCompleteTaskInSwimlane() throws Exception{

        KieSession ksession = getKieSession();
        TaskService taskService = getTaskService();
        Map<String,Object> params = new HashMap<String,Object>();
        params.put("userid", USER_MAX);
        ProcessInstance processInstance = ksession.startProcess(AGU_SAMPLE_PROCESS_ID, params);

        ProcessInstanceLog log = getLogService().findProcessInstance(processInstance.getId());
        assertNotNull(log);

        List<TaskSummary> tasks = taskService.getTasksOwned(USER_MAX, "en-UK");
        System.out.println("Found " + tasks.size() + " task(s) for user '"+USER_MAX+"'");

        long taskId = tasks.get(0).getId();
        taskService.start(taskId, USER_MAX);
        taskService.complete(taskId, USER_MAX, null);


        tasks = taskService.getTasksAssignedAsPotentialOwner(USER_MAX, "en-UK");
        System.out.println("Found " + tasks.size() + " task(s) for user '"+USER_MAX+"'");

        taskId = tasks.get(0).getId();
        taskService.start(taskId, USER_MAX);
        taskService.complete(taskId, USER_MAX, null);


        processInstance = ksession.getProcessInstance(processInstance.getId());
        assertNull(processInstance);
        System.out.println("Process instance completed");
    }

}
