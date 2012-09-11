package org.jbpm.task.service.test.spring;

import static org.jbpm.task.service.base.async.TaskLifeCycleBaseAsyncTest.*;

import org.junit.Ignore;
import org.junit.Test;

public class TaskLifeCycleSpringTest extends BaseSpringTest {

    @Test
    public void testLifeCycle() throws Exception {    
        runTestLifeCycle(client, users, groups);
    }

    @Test
    public void testLifeCycleMultipleTasks() throws Exception { 
        runTestLifeCycleMultipleTasks(client, users, groups);
    }
}
