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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.IOUtils;
import org.drools.agent.KnowledgeAgent;
import org.drools.builder.ResourceType;
import org.drools.command.*;
import org.drools.command.impl.GenericCommand;
import org.drools.command.runtime.BatchExecutionCommandImpl;
import org.drools.common.DefaultFactHandle;

import org.drools.grid.NodeTests.MyObject;
import org.drools.grid.helper.GridHelper;
import org.drools.grid.remote.InternalQueryResultsClient;
import org.drools.grid.remote.QueryResultsRemoteClient;
import org.drools.io.Resource;
import org.drools.io.impl.ByteArrayResource;
import org.drools.io.impl.ChangeSetImpl;
import org.drools.io.impl.ClassPathResource;
import org.drools.io.internal.InternalResource;
import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.rule.FactHandle;
import org.drools.runtime.rule.QueryResults;
import org.drools.runtime.rule.QueryResultsRow;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.drools.runtime.ExecutionResults;
import org.drools.command.CommandFactory;
import org.drools.grid.remote.command.AsyncBatchExecutionCommandImpl;

public class RemoteSessionCommandTest extends BaseRemoteTest {

    public RemoteSessionCommandTest() {
    }

    @Test
    public void insertTest() {
        StatefulKnowledgeSession ksession = createSession();

        ksession.setGlobal("myGlobalObj", new MyObject("myglobalObj"));

        FactHandle handle = ksession.insert(new MyObject("obj1"));
        Assert.assertNotNull(handle);
        Assert.assertEquals(true, ((DefaultFactHandle) handle).isDisconnected());

        int fired = ksession.fireAllRules();

        Assert.assertEquals(fired, 1);

    }

    @Test
    public void getGlobalTest() {
        StatefulKnowledgeSession ksession = createSession();

        ksession.setGlobal("myGlobalObj", new MyObject("myglobalObj"));

        Assert.assertEquals("myglobalObj", ((MyObject) ksession.getGlobal("myGlobalObj")).getName());

    }

    @Test
    public void retractTest() {
        StatefulKnowledgeSession ksession = createSession();

        ksession.setGlobal("myGlobalObj", new MyObject("myglobalObj"));

        FactHandle handle = ksession.insert(new MyObject("obj1"));
        Assert.assertNotNull(handle);
        Assert.assertEquals(true, ((DefaultFactHandle) handle).isDisconnected());

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
        Assert.assertEquals(true, ((DefaultFactHandle) handle).isDisconnected());

        int fired = ksession.fireAllRules();
        Assert.assertEquals(fired, 1);

        Assert.assertEquals(1, ksession.getFactCount());

        ksession.update(handle, new MyObject("obj2"));

        Assert.assertEquals(1, ksession.getFactCount());

        fired = ksession.fireAllRules();
        Assert.assertEquals(fired, 1);

    }

    @Ignore // FIX
    public void getFactHandleTest() {
        StatefulKnowledgeSession ksession = createSession();

        ksession.setGlobal("myGlobalObj", new MyObject("myglobalObj"));
        MyObject obj1 = new MyObject("obj1");
        FactHandle handle = ksession.insert(obj1);
        System.out.println("Handle Identity HashCode -> " + handle.toExternalForm());
        Assert.assertNotNull(handle);
        Assert.assertEquals(true, ((DefaultFactHandle) handle).isDisconnected());
        // The session assertMap doesn't find the factHandle for this object
        FactHandle newHandle = ksession.getFactHandle(obj1);
        System.out.println("Handle Identity HashCode -> " + newHandle.toExternalForm());

        Assert.assertEquals(newHandle, handle);


    }

    @Test
    public void getFactHandlesTest() {
        StatefulKnowledgeSession ksession = createSession();

        ksession.setGlobal("myGlobalObj", new MyObject("myglobalObj"));
        MyObject obj1 = new MyObject("obj1");
        FactHandle handle = ksession.insert(obj1);
        Assert.assertNotNull(handle);
        Assert.assertEquals(true, ((DefaultFactHandle) handle).isDisconnected());
        System.out.println("Handle Identity HashCode -> " + handle.toExternalForm());

        //I'm having problems with ObjectStoreWrapper that it's not serializable
        Collection<FactHandle> factHandles = ksession.getFactHandles();
        Assert.assertEquals(1, factHandles.size());
        FactHandle newHandle = factHandles.iterator().next();
        System.out.println("Handle Identity HashCode -> " + newHandle.toExternalForm());
        Assert.assertEquals(handle, newHandle);


    }

    @Test
    public void getObjectTest() {
        StatefulKnowledgeSession ksession = createSession();

        ksession.setGlobal("myGlobalObj", new MyObject("myglobalObj"));
        MyObject obj1 = new MyObject("obj1");
        FactHandle handle = ksession.insert(obj1);
        Assert.assertNotNull(handle);
        Assert.assertEquals(true, ((DefaultFactHandle) handle).isDisconnected());

        Object result = ksession.getObject(handle);

        Assert.assertEquals(obj1, result);


    }

    @Test
    public void queryTest() {
        StatefulKnowledgeSession ksession = createSession();

        ksession.setGlobal("myGlobalObj", new MyObject("myglobalObj"));
        MyObject obj1 = new MyObject("obj1");
        FactHandle handle = ksession.insert(obj1);
        Assert.assertNotNull(handle);
        Assert.assertEquals(true, ((DefaultFactHandle) handle).isDisconnected());

        Object result = ksession.getObject(handle);

        Assert.assertEquals(obj1, result);

        QueryResults queryResults = ksession.getQueryResults("getMyObjects", new Object[]{"obj1"});
        //Do black magic stuff with remoting :)
        InternalQueryResultsClient results = ((QueryResultsRemoteClient) queryResults).getResults();
        String[] parameters = results.getParameters();
        Assert.assertEquals(1, parameters.length);
        Assert.assertEquals("n", parameters[0]);

        String[] identifiers = queryResults.getIdentifiers();

        Assert.assertEquals(2, identifiers.length);
        Assert.assertEquals("n", identifiers[0]);
        Assert.assertEquals("$mo", identifiers[1]);
        Assert.assertEquals(1, queryResults.size());

        Assert.assertNotNull(results.getObject(identifiers[0]));
        Assert.assertEquals("obj1", results.getObject(identifiers[0]));
        Assert.assertEquals(obj1, results.getObject(identifiers[1]));

        for (QueryResultsRow row : queryResults) {
            Object o = row.get(identifiers[0]);
            System.out.println("Object from the query = " + o);
            Assert.assertNotNull(o);
            handle = row.getFactHandle(identifiers[0]);
            Assert.assertNotNull(handle);
            System.out.println("FactHandle from the query = " + handle);
        }



    }

    @Test
    @Ignore // FIX!!
    public void remoteKAgentProcessTest() {
        StatefulKnowledgeSession ksession = createSession();
        ksession.setGlobal("myGlobalObj", new MyObject("myglobalObj"));
        try {
            ksession.startProcess("Definition");
        } catch (Exception e) {
            System.out.println("Exception: " + e);
        }

        //ChangeSetImpl changeSet = new ChangeSetImpl();
//        String process = "<definitions id=\"Definition\" "
//                + "targetNamespace=\"http://www.example.org/MinimalExample\" "
//                + "typeLanguage=\"http://www.java.com/javaTypes\" "
//                + "expressionLanguage=\"http://www.mvel.org/2.0\" "
//                + "xmlns=\"http://www.omg.org/spec/BPMN/20100524/MODEL\" "
//                + "xmlns:xs=\"http://www.w3.org/2001/XMLSchema-instance\" "
//                + "xs:schemaLocation=\"http://www.omg.org/spec/BPMN/20100524/MODEL BPMN20.xsd\" "
//                + "xmlns:tns=\"http://www.jboss.org/drools\">"
//                + "<process id=\"Minimal\" name=\"Minimal Process\" tns:packageName=\"com.sample\">"
//                + "<startEvent id=\"_1\" name=\"StartProcess\"/>"
//                + "<sequenceFlow sourceRef=\"_1\" targetRef=\"_2\"/>"
//                + "<scriptTask id=\"_2\" name=\"Hello\">"
//                + "<script>System.out.println(\"Hello World\");</script>"
//                + "</scriptTask>"
//                + "<sequenceFlow sourceRef=\"_2\" targetRef=\"_3\"/>"
//                + "<endEvent id=\"_3\" name=\"EndProcess\">"
//                + "<terminateEventDefinition/>"
//                + "</endEvent>"
//                + "</process>"
//                + "</definitions>";
        Resource res = new ClassPathResource("test-process.bpmn");
        ((InternalResource) res).setResourceType(ResourceType.BPMN2);
        //changeSet.setResourcesAdded(Arrays.asList(res));
        //@TODO: for some reason the Classpath resource when is sent to the grid node is loosing the
        // ResourceType, so I need to fix that. 
        KnowledgeAgent kAgent = GridHelper.getInstance().getKnowledgeAgentRemoteClient( grid2, remoteN1.getId(), "ksession-rules" );
        kAgent.applyChangeSet(res);


        try {
            ksession.startProcess("Definition");
            System.out.println("Executed! :)");
        } catch (Exception e) {
            System.out.println("Exception: " + e);
        }

    }

    @Test
    
    public void remoteKAgentRuleTest() throws IOException, IOException, IOException, IOException, InterruptedException {
        StatefulKnowledgeSession ksession = createSession();
        ksession.setGlobal( "myGlobalObj", new MyObject( "myglobalObj" ) );
        
        MyObject obj1 = new MyObject( "obj1" );
        ksession.insert( obj1 );

        int fired = ksession.fireAllRules();
        Assert.assertEquals( 1, fired );

        String changeSetString = "<change-set xmlns='http://drools.org/drools-5.0/change-set'>"
                + "<add>"
                + "<resource type=\"DRL\" source=\"classpath:simple.drl\" />"
                + "</add>"
                + "</change-set>"
                + "";
        Resource changeSetRes = new ByteArrayResource( changeSetString.getBytes() );
        ( (InternalResource) changeSetRes ).setResourceType( ResourceType.CHANGE_SET );
        
        
        
        KnowledgeAgent kAgent = GridHelper.getInstance().getKnowledgeAgentRemoteClient( grid2, remoteN1.getId(), "ksession-rules" );
        kAgent.applyChangeSet( changeSetRes );

        Thread.sleep(5000);

        MyObject obj2 = new MyObject("obj2");
        ksession.insert(obj2);
        fired = ksession.fireAllRules();
        
        Assert.assertEquals(2, fired);


    }
    
    @Test
    public void executeBatchTest() {
        StatefulKnowledgeSession ksession = createSession();

        List<GenericCommand<?>> cmds = new ArrayList<GenericCommand<?>>();
        cmds.add((GenericCommand<?>)CommandFactory.newSetGlobal("myGlobalObj", new MyObject("myglobalObj")));
        cmds.add((GenericCommand<?>)CommandFactory.newInsert(new MyObject("obj1"), "myObject1"));
        cmds.add((GenericCommand<?>)CommandFactory.newFireAllRules());
        BatchExecutionCommandImpl batch = new BatchExecutionCommandImpl(cmds, "out");
        
        ExecutionResults results = ksession.execute(batch);
        
       
        System.out.println("Results = "+results);
        Assert.assertNotNull(results);
        Assert.assertEquals(1, results.getIdentifiers().size());
        Assert.assertEquals("myObject1", results.getIdentifiers().iterator().next());


    }
    
    @Test
    public void executeAsyncBatchTest() throws InterruptedException {
        StatefulKnowledgeSession ksession = createSession();

        List<Command> cmds = new ArrayList<Command>();
        cmds.add(CommandFactory.newSetGlobal("myGlobalObj", new MyObject("myglobalObj")));

        MyObject myObject = new MyObject("obj1");
        cmds.add(CommandFactory.newInsert(myObject));
        cmds.add(CommandFactory.newFireAllRules());
        AsyncBatchExecutionCommandImpl batch = new AsyncBatchExecutionCommandImpl(cmds);
        
        ExecutionResults results = ksession.execute(batch);
        
       
        
        Assert.assertNull(results);
        
        Thread.sleep(2000);
        
        FactHandle handle = ksession.getFactHandle(myObject);
        Assert.assertNotNull(handle);
        
        

    }

}
