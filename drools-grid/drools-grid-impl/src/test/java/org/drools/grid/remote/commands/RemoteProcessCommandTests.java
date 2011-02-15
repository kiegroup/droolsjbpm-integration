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



import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.process.ProcessInstance;
import org.junit.Assert;
import org.junit.Test;

public class RemoteProcessCommandTests extends BaseRemoteTest{

    public RemoteProcessCommandTests() {
    }

   

    

     @Test
     public void startProcessTest() {   
        StatefulKnowledgeSession ksession = createProcessSession();
        
        ProcessInstance processInstance = ksession.startProcess("Minimal");
        
        Assert.assertNotNull(processInstance);
        
        Assert.assertEquals("Minimal", processInstance.getProcessId());
     }
     
     
     
     
      
      
      
    
     


}
