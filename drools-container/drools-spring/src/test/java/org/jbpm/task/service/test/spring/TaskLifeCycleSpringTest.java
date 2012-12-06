package org.jbpm.task.service.test.spring;

import static org.jbpm.task.service.base.async.TaskLifeCycleBaseAsyncTest.runTestLifeCycle;
import static org.jbpm.task.service.base.async.TaskLifeCycleBaseAsyncTest.runTestLifeCycleMultipleTasks;

import org.junit.Ignore;
import org.junit.Test;

public class TaskLifeCycleSpringTest extends BaseSpringTest {

    @Test
    @Ignore
    public void testLifeCycle() throws Exception {    
        runTestLifeCycle(client, users, groups);
    }

    @Test
    @Ignore
    public void testLifeCycleMultipleTasks() throws Exception { 
        runTestLifeCycleMultipleTasks(client, users, groups);
    }
}
