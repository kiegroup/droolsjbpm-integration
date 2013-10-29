package org.kie.services.client.api;

import static org.kie.services.client.SerializationTest.createKnowledgeSession;

import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.drools.core.command.runtime.process.GetProcessInstancesCommand;
import org.jbpm.bpmn2.objects.TestWorkItemHandler;
import org.jbpm.process.instance.impl.demo.DoNothingWorkItemHandler;
import org.junit.Assert;
import org.junit.Test;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.api.runtime.process.WorkItemHandler;

public class RemoteApiTest extends Assert {
    
    @Test
    public void notAceptedMethodTest() throws Exception { 
        URL deploymentUrl = new URL( "http://localhost:8080/kie-wb/" );
        RemoteRestSessionFactory restSessionFactory 
            = new RemoteRestSessionFactory("deployment", deploymentUrl, "mary", "pass");
        
        WorkItemHandler wih = new DoNothingWorkItemHandler();
        try { 
            restSessionFactory.newRuntimeEngine().getKieSession().getWorkItemManager().registerWorkItemHandler("test", wih);
            fail( "The above call should have failed.");
        } catch( UnsupportedOperationException uoe ) { 
            assertEquals("The .registerWorkItemHandler(..) method is not supported on the remote api.", uoe.getMessage());
        }
    }

}
