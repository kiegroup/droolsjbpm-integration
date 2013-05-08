package org.kie.services.client.api.same;

import java.util.List;

import org.kie.services.client.api.ApiRequestFactoryProvider;
import org.kie.services.client.api.MessageHolder;
import org.kie.services.client.api.same.SameApiRequestHandler;
import org.kie.services.client.message.OperationMessage;
import org.kie.services.client.message.ServiceMessage;
import org.kie.services.client.message.serialization.MessageSerializationProvider.Type;
import org.kie.services.client.message.serialization.impl.JaxbSerializationProvider;
import org.junit.Assert;
import org.junit.Test;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.task.TaskService;

public class SameApiRequestTest extends SameApiBaseTest {


    @Test
    public void createBasicRequest() {
        SameApiRequestHandler requestHandler = getSameApiRequestFactory();
        RuntimeEngine remoteRuntimeEngine = requestHandler.getRemoteRuntimeEngine("release");
        TaskService taskServiceRequest = remoteRuntimeEngine.getTaskService();

        long taskId = 3;
        String userId = "bob";
        taskServiceRequest.activate(taskId, userId);

        ServiceMessage request = ((MessageHolder) taskServiceRequest).getRequest();

        assertTrue(request != null);

        // test method name
        OperationMessage operRequest = request.getOperations().get(0);
        assertTrue("activate".equals(operRequest.getMethodName()));

        // test args
        Object[] args = operRequest.getArgs();
        assertTrue(args != null && args.length == 2);
        assertTrue((Long) args[0] == taskId);
        assertTrue(userId.equals(args[1]));
    }

    @Test
    public void createKieSessionRequest() {
        RuntimeEngine remoteRuntimeEngine = getSameApiRequestFactory().getRemoteRuntimeEngine("domain");
        KieSession kieSessionRequest = remoteRuntimeEngine.getKieSession();

        String processName = "example-process";
        kieSessionRequest.startProcess("example-process");

        ServiceMessage request = ((MessageHolder) kieSessionRequest).getRequest();

        assertTrue(request != null);

        // test method name
        OperationMessage operRequest = request.getOperations().get(0);
        assertEquals("startProcess", operRequest.getMethodName());

        // test args
        Object[] args = operRequest.getArgs();
        assertTrue(args != null && args.length == 1);
        assertTrue(processName.equals(args[0]));
    }

    @Test
    public void multipleOperationRequest() {
        String thirdOperSecondArg = "Clavier";
        ServiceMessage request = createMultipleOpRequest();
        List<OperationMessage> operations = request.getOperations();

        assertTrue("Expected 4 operations", operations.size() == 4);
        OperationMessage operRequest = operations.get(0);
        String firstMethod = operRequest.getMethodName();
        assertEquals("First method incorrect.", "startProcess", firstMethod);

        operRequest = operations.get(2);
        Object[] args = operRequest.getArgs();
        assertTrue("Expected 2 arguments for third operation, not " + args.length, args.length == 2);
        assertTrue("Expected '" + thirdOperSecondArg + "' as 2nd argument, not " + args[1], thirdOperSecondArg.equals(args[1]));
    }
    
    public static ServiceMessage createMultipleOpRequest() { 
        RuntimeEngine remoteRuntimeEngine = getSameApiRequestFactory().getRemoteRuntimeEngine("domain");
        KieSession kieSessionRequest = remoteRuntimeEngine.getKieSession();

        kieSessionRequest.startProcess("test");
        kieSessionRequest.signalEvent("party-event", null);

        TaskService taskServiceRequest = remoteRuntimeEngine.getTaskService();
        String thirdOperSecondArg = "Clavier";
        taskServiceRequest.activate(22, thirdOperSecondArg);
        taskServiceRequest.suspend(43l, "Renard");

        return ((MessageHolder) taskServiceRequest).getRequest();
    }
    
    @Test
    public void shouldBeAbleToHandleRequestAsMullitpleObjects() { 
        RuntimeEngine remoteRuntimeEngine = getSameApiRequestFactory().getRemoteRuntimeEngine("domain");
        KieSession kieSessionRequest = remoteRuntimeEngine.getKieSession();
        
        kieSessionRequest.startProcess("test");
        
        remoteRuntimeEngine.getTaskService().exit(23, "illuminati");
        kieSessionRequest.getWorkItemManager().abortWorkItem(42);
        
        int numOps = ((MessageHolder) kieSessionRequest).getRequest().getOperations().size();
        assertEquals(3, numOps);
    }
    
    @Test
    public void unwantedMethodsShouldNotSucceed() { 
        RuntimeEngine remoteRuntimeEngine = getSameApiRequestFactory().getRemoteRuntimeEngine("domain");
        KieSession kieSessionRequest = remoteRuntimeEngine.getKieSession();
        
        try {
            kieSessionRequest.dispose();
            fail();
        } catch(Throwable t) { }
        try {
            kieSessionRequest.getKieBase();
            fail();
        } catch(Throwable t) { }
        try {
            kieSessionRequest.getAgenda();
            fail();
        } catch(Throwable t) { }
        try {
            kieSessionRequest.execute(null);
            fail();
        } catch(Throwable t) { }
    }

}