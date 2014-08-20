package org.kie.services.client.api;


import java.net.URL;

import org.jbpm.process.instance.impl.demo.DoNothingWorkItemHandler;
import org.junit.Assert;
import org.junit.Test;
import org.kie.api.runtime.process.WorkItemHandler;

public class RemoteApiTest extends Assert {
    
    @Test
    public void notAceptedMethodTest() throws Exception { 
        URL deploymentUrl = new URL( "http://localhost:8080/kie-wb/" );
        RemoteRestRuntimeEngineFactory restSessionFactory 
            = RemoteRestRuntimeEngineFactory.newBuilder()
                .addDeploymentId("deployment")
                .addUrl(deploymentUrl)
                .addUserName("mary")
                .addPassword("pass")
                .buildFactory();
        
        WorkItemHandler wih = new DoNothingWorkItemHandler();
        try { 
            restSessionFactory.newRuntimeEngine().getKieSession().getWorkItemManager().registerWorkItemHandler("test", wih);
            fail( "The above call should have failed.");
        } catch( UnsupportedOperationException uoe ) { 
            assertEquals("The WorkItemManager.registerWorkItemHandler(..) method is not supported on the Remote Client instance.", uoe.getMessage());
        }
    }
    
}
