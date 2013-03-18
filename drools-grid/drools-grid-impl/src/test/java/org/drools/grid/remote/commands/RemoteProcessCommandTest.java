/*
 * Copyright 2010 salaboy.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * under the License.
 */

package org.drools.grid.remote.commands;



import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.kie.api.runtime.StatefulKnowledgeSession;
import org.kie.api.runtime.process.ProcessInstance;

public class RemoteProcessCommandTest extends BaseRemoteTest{

    public RemoteProcessCommandTest() {
    }

   

    

     @Test
     public void startProcessTest() {
        StatefulKnowledgeSession ksession = createProcessSession();
        
        ProcessInstance processInstance = ksession.startProcess("Minimal");
        
        Assert.assertNotNull(processInstance);
        
        Assert.assertEquals("Minimal", processInstance.getProcessId());
     }
     
     @Test
     public void startUnexistingProcessTest() {
        StatefulKnowledgeSession ksession = createProcessSession();
        
        try{
            ProcessInstance processInstance = ksession.startProcess("Minimal NO EXISTS");
            Assert.fail("Exception expected");
        } catch (Exception e){
            e.printStackTrace();
        }
        
     }
     
     @Test
     public void startProcessWithParametersTest() {
        StatefulKnowledgeSession ksession = createProcessSession();
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("long", 1L);
        ProcessInstance processInstance = ksession.startProcess("Minimal", parameters);
        
        Assert.assertNotNull(processInstance);
        
        Assert.assertEquals("Minimal", processInstance.getProcessId());
     }
     
     
     
     
      
      
      
    
     


}
