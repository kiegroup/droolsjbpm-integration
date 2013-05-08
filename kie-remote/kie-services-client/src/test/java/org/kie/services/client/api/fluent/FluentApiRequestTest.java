package org.kie.services.client.api.fluent;

import java.util.HashMap;

import org.kie.services.client.api.MessageHolder;
import org.kie.services.client.api.fluent.api.RemoteRuntimeEngineFluent;
import org.kie.services.client.api.fluent.api.RemoteKieSessionFluent;
import org.kie.services.client.api.fluent.api.RemoteTaskFluent;
import org.kie.services.client.message.OperationMessage;
import org.kie.services.client.message.ServiceMessage;
import org.junit.Ignore;
import org.junit.Test;

@Ignore // until fluent has been fully implemented
public class FluentApiRequestTest extends FluentApiBaseTest { 

    @Test
    public void basicRequestShouldBeCreatedAndContainInfo() { 
       RemoteRuntimeEngineFluent runtimeEngine = getFluentApiRequestFactory().getRemoteRuntimeEngine("domain");
       
       long taskId = 3;
       String userId = "bob";
       RemoteTaskFluent taskServiceRequest 
           = runtimeEngine.getTaskService().activate(taskId, userId);
       
       ServiceMessage request = ((MessageHolder) taskServiceRequest).getRequest();
       
       assertTrue(request != null); 
      
       // test method name
       OperationMessage operRequest = request.getOperations().get(0);
       assertEquals("activate", operRequest.getMethodName());
       assertEquals(ServiceMessage.TASK_SERVICE_REQUEST, operRequest.getServiceType());
       
       // test args
       Object [] args = operRequest.getArgs();
       assertTrue(args != null && args.length == 2 );
       assertTrue((Long) args[0] == taskId);
       assertTrue(userId.equals(args[1]));
    }
    
    @Test
    public void shouldBeAbleToCreateKieSessionRequestAndAddInfo() { 
        RemoteRuntimeEngineFluent runtimeEngine = getFluentApiRequestFactory().getRemoteRuntimeEngine("domain");

       String processName = "example-process";
       RemoteKieSessionFluent kieSessionRequest
           = runtimeEngine.getKieSession().startProcess(processName);
       ServiceMessage request = ((MessageHolder) kieSessionRequest).getRequest();
       
       assertTrue(request != null); 
      
       // test method name
       OperationMessage operRequest = request.getOperations().get(0);
       assertEquals("startProcess".toLowerCase(), operRequest.getMethodName());
       
       // test args
       Object [] args = operRequest.getArgs();
       assertTrue(args != null && args.length == 1 );
       assertTrue(processName.equals(args[0]));
    }
    
    @Test
    public void shouldBeAbleToAddMultipleOperations() { 
        RemoteRuntimeEngineFluent runtimeEngine = getFluentApiRequestFactory().getRemoteRuntimeEngine("domain");

       String processName = "example-process";
       RemoteKieSessionFluent kieSessionRequest
           = runtimeEngine.getKieSession()
               .startProcess(processName)
               .signalEvent("party-event", null);
        
        kieSessionRequest.startProcess("test").signalEvent("party-event", null);
    }
    
    @Test
    public void shouldBeAbleToCallMethodWithParameters() { 
        RemoteRuntimeEngineFluent runtimeEngine = getFluentApiRequestFactory().getRemoteRuntimeEngine("domain");

        HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("user-id", "Lin Dze");
       RemoteKieSessionFluent kieSessionRequest
        = runtimeEngine.getKieSession()
            .startProcess("og", params);
       
//        String msgXmlString = ((MessageHolder) kieSessionRequest).getMessageJaxbXml();
    }
}
