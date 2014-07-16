package org.kie.services.client.api;


import java.net.URL;

import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientRequestFactory;
import org.jboss.resteasy.client.ClientResponse;
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
            assertEquals("The .registerWorkItemHandler(..) method is not supported on the remote api.", uoe.getMessage());
        }
    }
    
    @Test
    public void reRequestTest() throws Exception { 
        String urlString = "http://localhost:8080/appBase/";
        URL appBaseUrl = new URL(urlString);
        ClientRequestFactory factory = new ClientRequestFactory(appBaseUrl.toURI());
       
        String pathAdd = "/j_security_check";
        ClientRequest formRequest = factory.createRelativeRequest(pathAdd);
        formRequest.formParameter("test", "test");
       
        assertEquals(urlString + pathAdd , formRequest.getUri());
    }

}
