/*
 * Copyright 2015 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package org.kie.services.client.api;


import java.net.URL;

import org.jbpm.process.instance.impl.demo.DoNothingWorkItemHandler;
import org.junit.Assert;
import org.junit.Test;
import org.kie.api.runtime.process.WorkItemHandler;
import org.kie.remote.client.api.RemoteRestRuntimeEngineFactory;

public class RemoteApiTest extends Assert {
    
    @Test
    public void notAceptedMethodTest() throws Exception { 
        URL deploymentUrl = new URL( "http://localhost:8080/kie-wb/" );
        RemoteRestRuntimeEngineFactory restSessionFactory 
            = RemoteRuntimeEngineFactory.newRestBuilder()
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
            assertTrue("Incorrect error message: " + uoe.getMessage(), uoe.getMessage().contains("not supported on the Remote Client instance."));
        }
    }
    
}
