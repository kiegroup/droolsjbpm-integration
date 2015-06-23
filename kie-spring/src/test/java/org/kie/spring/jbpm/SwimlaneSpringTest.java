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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jbpm.process.audit.AuditLogService;
import org.jbpm.process.audit.ProcessInstanceLog;
import org.junit.Test;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.runtime.manager.RuntimeManager;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.api.task.TaskService;
import org.kie.api.task.model.TaskSummary;
import org.kie.internal.runtime.manager.context.EmptyContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class SwimlaneSpringTest extends AbstractJbpmSpringTest {

    @Test
    public void testCompleteTaskInSwimlane() throws Exception{

        context = new ClassPathXmlApplicationContext("jbpm/usergroup-callback/local-emf-singleton.xml");

        RuntimeManager manager = (RuntimeManager) context.getBean("runtimeManager");
        RuntimeEngine engine = manager.getRuntimeEngine(EmptyContext.get());

        KieSession ksession = engine.getKieSession();
        TaskService taskService = engine.getTaskService();

        Map<String,Object> params = new HashMap<String,Object>();
        params.put("userid", "max");
        ProcessInstance processInstance = ksession.startProcess("agu.samples.sample1", params);

        AuditLogService logService = (AuditLogService) context.getBean("logService");
        ProcessInstanceLog log = logService.findProcessInstance(processInstance.getId());
        assertNotNull(log);

        List<TaskSummary> tasks = taskService.getTasksOwned("max", "en-UK");
        System.out.println("Found " + tasks.size() + " task(s) for user 'max'");

        long taskId = tasks.get(0).getId();
        taskService.start(taskId, "max");
        taskService.complete(taskId, "max", null);


        tasks = taskService.getTasksAssignedAsPotentialOwner("max", "en-UK");
        System.out.println("Found " + tasks.size() + " task(s) for user 'max'");

        taskId = tasks.get(0).getId();
        taskService.start(taskId, "max");
        taskService.complete(taskId, "max", null);


        processInstance = ksession.getProcessInstance(processInstance.getId());
        assertNull(processInstance);
        System.out.println("Process instance completed");
    }

}
