package org.kie.services.client.api.same;

import static org.kie.services.client.api.AllMethodsTestUtil.testMethods;

import java.lang.reflect.Method;

import org.junit.Test;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.runtime.process.WorkItemManager;
import org.kie.api.task.TaskService;

public class SameApiAllMethodsTest extends SameApiBaseTest {

    @Test
    public void KieSessionMethodsShouldThrowUnsupportedOrSucceed() throws Exception {
        SameApiRequestHandler requestHandler = getSameApiRequestFactory();
        RuntimeEngine remoteRuntimeEngine = requestHandler.getRemoteRuntimeEngine("release");
        KieSession kieSessionRequest = remoteRuntimeEngine.getKieSession();

        Method[] methods = kieSessionRequest.getClass().getMethods();
        testMethods(kieSessionRequest, methods);
    }
    
    @Test
    public void WorkItemManagerMethodsShouldThrowUnsupportedOrSucceed() throws Exception {
        SameApiRequestHandler requestHandler = getSameApiRequestFactory();
        RuntimeEngine remoteRuntimeEngine = requestHandler.getRemoteRuntimeEngine("release");
        WorkItemManager workItemManagerRequest = remoteRuntimeEngine.getKieSession().getWorkItemManager();

        Method[] methods = workItemManagerRequest.getClass().getMethods();
        testMethods(workItemManagerRequest, methods);
    }

    @Test
    public void TaskServiceMethodsShouldThrowUnsupportedOrSucceed() throws Exception {
        SameApiRequestHandler requestHandler = getSameApiRequestFactory();
        RuntimeEngine remoteRuntimeEngine = requestHandler.getRemoteRuntimeEngine("release");
        TaskService taskServiceRequest = remoteRuntimeEngine.getTaskService();

        Method[] methods = taskServiceRequest.getClass().getMethods();
        testMethods(taskServiceRequest, methods);
    }
}
