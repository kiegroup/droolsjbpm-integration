package org.kie.services.client.api.fluent;

import static org.kie.services.client.api.AllMethodsTestUtil.testMethods;

import java.lang.reflect.Method;

import org.kie.services.client.api.fluent.api.RemoteKieSessionFluent;
import org.kie.services.client.api.fluent.api.RemoteTaskFluent;
import org.junit.Ignore;
import org.junit.Test;
import org.kie.internal.fluent.runtime.WorkItemManagerFluent;

@SuppressWarnings("rawtypes")
@Ignore // until fluent has been fully implemented
public class FluentApiAllMethodsTest extends FluentApiBaseTest {

    @Test
    public void KieSessionMethodsShouldThrowUnsupportedOrSucceed() throws Exception {
        FluentApiRequestHandler requestHandler = getFluentApiRequestFactory();
        RemoteKieSessionFluent kieSessionRequest 
            = requestHandler.getRemoteRuntimeEngine("domain").getKieSession();

        Method[] methods = kieSessionRequest.getClass().getMethods();
        testMethods(kieSessionRequest, methods);
    }

    @Test
    public void WorkItemManagerMethodsShouldThrowUnsupportedOrSucceed() throws Exception {
        FluentApiRequestHandler requestHandler = getFluentApiRequestFactory();
        WorkItemManagerFluent workItemManagerRequest 
            = requestHandler.getRemoteRuntimeEngine("domain").getKieSession().getWorkItemManager();

        Method[] methods = workItemManagerRequest.getClass().getMethods();
        testMethods(workItemManagerRequest, methods);
    }

    @Test
    public void TaskServiceMethodsShouldThrowUnsupportedOrSucceed() throws Exception {
        FluentApiRequestHandler requestHandler = getFluentApiRequestFactory();
        RemoteTaskFluent taskServiceRequest 
            = requestHandler.getRemoteRuntimeEngine("domain").getTaskService();

        Method[] methods = taskServiceRequest.getClass().getMethods();
        testMethods(taskServiceRequest, methods);
    }
}
