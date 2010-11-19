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



import java.util.Collection;
import org.drools.common.DefaultFactHandle;
import org.drools.grid.NodeTests.MyObject;
import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.rule.FactHandle;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author salaboy
 */
public class RemoteSessionCommandTests extends BaseRemoteTest{

    public RemoteSessionCommandTests() {
    }

   

    

     @Test
     public void insertTest() {
        StatefulKnowledgeSession ksession = createSession();
        
        ksession.setGlobal("myGlobalObj", new MyObject("myglobalObj"));
        
        FactHandle handle = ksession.insert(new MyObject("obj1"));
        Assert.assertNotNull(handle);
        Assert.assertEquals(true, ((DefaultFactHandle)handle).isDisconnected());
        
        int fired = ksession.fireAllRules();
        
        Assert.assertEquals(fired, 1);
     
     }
     
     @Test
     public void getGlobalTest() {
        StatefulKnowledgeSession ksession = createSession();
        
        ksession.setGlobal("myGlobalObj", new MyObject("myglobalObj"));
        
        Assert.assertEquals("myglobalObj", ((MyObject)ksession.getGlobal("myGlobalObj")).getName());
     
     }
     
     @Test
     public void retractTest() {
        StatefulKnowledgeSession ksession = createSession();
        
        ksession.setGlobal("myGlobalObj", new MyObject("myglobalObj"));
        
        FactHandle handle = ksession.insert(new MyObject("obj1"));
        Assert.assertNotNull(handle);
        Assert.assertEquals(true, ((DefaultFactHandle)handle).isDisconnected());
        
        int fired = ksession.fireAllRules();
        Assert.assertEquals(fired, 1);
        
        Assert.assertEquals(1, ksession.getFactCount());
        
        ksession.retract(handle);
        
        Assert.assertEquals(0, ksession.getFactCount());
     
     }
     
     
     @Test
     public void updateTest() {
        StatefulKnowledgeSession ksession = createSession();
        
        ksession.setGlobal("myGlobalObj", new MyObject("myglobalObj"));
        
        FactHandle handle = ksession.insert(new MyObject("obj1"));
        Assert.assertNotNull(handle);
        Assert.assertEquals(true, ((DefaultFactHandle)handle).isDisconnected());
        
        int fired = ksession.fireAllRules();
        Assert.assertEquals(fired, 1);
        
        Assert.assertEquals(1, ksession.getFactCount());
        
        ksession.update(handle, new MyObject("obj2"));
        
        Assert.assertEquals(1, ksession.getFactCount());
        
        fired = ksession.fireAllRules();
        Assert.assertEquals(fired, 1);
     
     }
     
//     @Test
//     public void getFactHandleTest() {
//        StatefulKnowledgeSession ksession = createSession();
//        
//        ksession.setGlobal("myGlobalObj", new MyObject("myglobalObj"));
//        MyObject obj1 = new MyObject("obj1");
//        FactHandle handle = ksession.insert(obj1);
//        Assert.assertNotNull(handle);
//        Assert.assertEquals(true, ((DefaultFactHandle)handle).isDisconnected());
//        // The session assertMap doesn't find the factHandle for this object
//        FactHandle newHandle = ksession.getFactHandle(obj1);
//        
//        Assert.assertEquals( newHandle, handle );
//        
//     
//     }
     
     
//     @Test
//     public void getFactHandlesTest() {
//        StatefulKnowledgeSession ksession = createSession();
//        
//        ksession.setGlobal("myGlobalObj", new MyObject("myglobalObj"));
//        MyObject obj1 = new MyObject("obj1");
//        FactHandle handle = ksession.insert(obj1);
//        Assert.assertNotNull(handle);
//        Assert.assertEquals(true, ((DefaultFactHandle)handle).isDisconnected());
//        //I'm having problems with ObjectStoreWrapper that it's not serializable
//        Collection<FactHandle> factHandles = ksession.getFactHandles();
//        Assert.assertEquals(1, factHandles.size());
//        Assert.assertEquals(handle, factHandles.iterator().next() );
//        
//     
//     }
     
      @Test
     public void getObjectTest() {
        StatefulKnowledgeSession ksession = createSession();
        
        ksession.setGlobal("myGlobalObj", new MyObject("myglobalObj"));
        MyObject obj1 = new MyObject("obj1");
        FactHandle handle = ksession.insert(obj1);
        Assert.assertNotNull(handle);
        Assert.assertEquals(true, ((DefaultFactHandle)handle).isDisconnected());
        
        Object result = ksession.getObject(handle);
        
        Assert.assertEquals(obj1,  result);
        
     
     }
     
      
      
      
    
     


}