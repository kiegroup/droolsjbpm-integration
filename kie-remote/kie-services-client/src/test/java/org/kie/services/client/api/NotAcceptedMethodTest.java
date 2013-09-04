package org.kie.services.client.api;

import java.net.URL;

import org.jbpm.process.instance.impl.demo.DoNothingWorkItemHandler;
import org.junit.Assert;
import org.junit.Test;
import org.kie.api.runtime.process.WorkItemHandler;

public class NotAcceptedMethodTest extends Assert {
    
    @Test
    public void remoteRestApi() throws Exception { 
        URL deploymentUrl = new URL( "http://localhost:8080/kie-wb/" );
        RemoteRestSessionFactory restSessionFactory 
            = new RemoteRestSessionFactory("deployment", deploymentUrl.toExternalForm(), "mary", "pass");
        
        WorkItemHandler wih = new DoNothingWorkItemHandler();
        try { 
            restSessionFactory.newRuntimeEngine().getKieSession().getWorkItemManager().registerWorkItemHandler("test", wih);
            fail( "The above call should have failed.");
        } catch( UnsupportedOperationException uoe ) { 
            assertEquals("The .registerWorkItemHandler(..) method is not supported on the remote api.", uoe.getMessage());
        }
    }
}
