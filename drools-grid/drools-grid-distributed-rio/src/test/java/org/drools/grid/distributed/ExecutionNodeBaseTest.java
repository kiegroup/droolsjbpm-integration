/*
 *  Copyright 2010 salaboy.
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
 */

package org.drools.grid.distributed;


import java.rmi.RemoteException;
import org.drools.grid.ConnectorException;
import org.drools.grid.ExecutionNode;
import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseFactoryService;
import org.drools.builder.DirectoryLookupFactoryService;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderFactoryService;
import org.drools.builder.ResourceType;
import org.drools.command.runtime.rule.FireAllRulesCommand;
import org.drools.grid.GridConnection;
import org.drools.io.ResourceFactory;
import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.grid.strategies.StaticIncrementalNodeSelectionStrategy;
import org.drools.runtime.ExecutionResults;
import org.junit.Assert;
import org.junit.Test;

public abstract class ExecutionNodeBaseTest {

    protected ExecutionNode node;
    protected GridConnection connection = new GridConnection();
    public ExecutionNodeBaseTest() {
        
    }


    @Test
    public void fireAllRules(){
        StaticIncrementalNodeSelectionStrategy.counter = 0;
        String str = "";
        str += "package org.drools \n";
        str += "global java.util.List list \n";
        str += "rule rule1 \n";
        str += "    dialect \"java\" \n";
        str += "when \n";
        str += "then \n";
        str += "    System.out.println( \"hello1!!!\" ); \n";
        str += "end \n";
        str += "rule rule2 \n";
        str += "    dialect \"java\" \n";
        str += "when \n";
        str += "then \n";
        str += "    System.out.println( \"hello2!!!\" ); \n";
        str += "end \n";

        Assert.assertNotNull(node);
         
        KnowledgeBuilder kbuilder = 
                node.get(KnowledgeBuilderFactoryService.class)
                 .newKnowledgeBuilder();
        Assert.assertNotNull(kbuilder);

        kbuilder.add( ResourceFactory
                       .newByteArrayResource( str.getBytes() ),
                      ResourceType.DRL );

        if ( kbuilder.hasErrors() ) {
            System.out.println( "Errors: " + kbuilder.getErrors() );
        }

        KnowledgeBase kbase = 
                node.get(KnowledgeBaseFactoryService.class)
                 .newKnowledgeBase();
        Assert.assertNotNull(kbase);
        
        kbase.addKnowledgePackages( kbuilder.getKnowledgePackages() );

        StatefulKnowledgeSession ksession =
                            kbase.newStatefulKnowledgeSession();
        Assert.assertNotNull(ksession);

        int fired = ksession.fireAllRules();
        Assert.assertEquals( 2, fired );


    }

     @Test
    public void testExecute() throws Exception {
         StaticIncrementalNodeSelectionStrategy.counter = 0;
        String str = "";
        str += "package org.drools \n";
        str += "global java.util.List list \n";
        str += "rule rule1 \n";
        str += "    dialect \"java\" \n";
        str += "when \n";
        str += "then \n";
        str += "    System.out.println( \"hello1!!!\" ); \n";
        str += "end \n";
        str += "rule rule2 \n";
        str += "    dialect \"java\" \n";
        str += "when \n";
        str += "then \n";
        str += "    System.out.println( \"hello2!!!\" ); \n";
        str += "end \n";

        Assert.assertNotNull(node);

        KnowledgeBuilder kbuilder = node.get(KnowledgeBuilderFactoryService.class).newKnowledgeBuilder();
        Assert.assertNotNull(kbuilder);
        kbuilder.add( ResourceFactory.newByteArrayResource( str.getBytes() ),
                      ResourceType.DRL );

        if ( kbuilder.hasErrors() ) {
            System.out.println( "Errors: " + kbuilder.getErrors() );
        }

        KnowledgeBase kbase = node.get(KnowledgeBaseFactoryService.class).newKnowledgeBase();
        Assert.assertNotNull(kbase);

        kbase.addKnowledgePackages( kbuilder.getKnowledgePackages() );

        StatefulKnowledgeSession ksession = kbase.newStatefulKnowledgeSession();

        int fired = ksession.fireAllRules();

        Assert.assertEquals( 2, fired );
    }
//
    @Test
    public void testVsmPipeline() throws Exception {
        StaticIncrementalNodeSelectionStrategy.counter = 0;
        String str = "";
        str += "package org.drools \n";
        str += "global java.util.List list \n";
        str += "rule rule1 \n";
        str += "    dialect \"java\" \n";
        str += "when \n";
        str += "then \n";
        str += "    System.out.println( \"hello3!!!\" ); \n";
        str += "end \n";
        str += "rule rule2 \n";
        str += "    dialect \"java\" \n";
        str += "when \n";
        str += "then \n";
        str += "    System.out.println( \"hello4!!!\" ); \n";
        str += "end \n";

        Assert.assertNotNull(node);

        KnowledgeBuilder kbuilder = node.get(KnowledgeBuilderFactoryService.class).newKnowledgeBuilder();
        Assert.assertNotNull(kbuilder);

        kbuilder.add( ResourceFactory.newByteArrayResource( str.getBytes() ),
                      ResourceType.DRL );

        if ( kbuilder.hasErrors() ) {
            System.out.println( "Errors: " + kbuilder.getErrors() );
        }

        KnowledgeBase kbase = node.get(KnowledgeBaseFactoryService.class).newKnowledgeBase();
        Assert.assertNotNull(kbase);

        kbase.addKnowledgePackages( kbuilder.getKnowledgePackages() );

        StatefulKnowledgeSession ksession = kbase.newStatefulKnowledgeSession();

        node.get(DirectoryLookupFactoryService.class).register( "ksession1", ksession );

        int fired = ((StatefulKnowledgeSession)node.get(DirectoryLookupFactoryService.class).lookup( "ksession1" ) ).fireAllRules();

        Assert.assertEquals( 2, fired );
    }
//
    @Test
    public void testNamedService() throws Exception {
        StaticIncrementalNodeSelectionStrategy.counter = 1;
        
        String str = "";
        str += "package org.drools \n";
        str += "global java.util.List list \n";
        str += "rule rule1 \n";
        str += "    dialect \"java\" \n";
        str += "when \n";
        str += "then \n";
        str += "    System.out.println( \"hello1!!!\" ); \n";
        str += "end \n";
        str += "rule rule2 \n";
        str += "    dialect \"java\" \n";
        str += "when \n";
        str += "then \n";
        str += "    System.out.println( \"hello2!!!\" ); \n";
        str += "end \n";

        KnowledgeBuilder kbuilder = node.get(KnowledgeBuilderFactoryService.class).newKnowledgeBuilder();
        Assert.assertNotNull(kbuilder);

        kbuilder.add( ResourceFactory.newByteArrayResource( str.getBytes() ),
                      ResourceType.DRL );

        if ( kbuilder.hasErrors() ) {
            System.out.println( "Errors: " + kbuilder.getErrors() );
        }

        KnowledgeBase kbase = node.get(KnowledgeBaseFactoryService.class).newKnowledgeBase();
        Assert.assertNotNull(kbase);

        kbase.addKnowledgePackages( kbuilder.getKnowledgePackages() );

        StatefulKnowledgeSession ksession = kbase.newStatefulKnowledgeSession();
        System.out.println("registering a session!!!!!!!");
        node.get(DirectoryLookupFactoryService.class).register( "ksession1",
                              ksession );
        node.get(DirectoryLookupFactoryService.class).register( "ksession2",
                              ksession );
        node.get(DirectoryLookupFactoryService.class).register( "ksession3",
                              ksession );
        node.get(DirectoryLookupFactoryService.class).register( "ksession4",
                              ksession );
        
        int fired = ((StatefulKnowledgeSession)node.get(DirectoryLookupFactoryService.class).lookup( "ksession1" ) ).fireAllRules();

        Assert.assertEquals( 2,
                     fired );
        
    }
   @Test
    public void twoSessionsIntoDifferentSessionServices() throws RemoteException, ConnectorException, InterruptedException{
        StaticIncrementalNodeSelectionStrategy.counter = 0;
        String str = "";
        str += "package org.drools \n";
        str += "global java.util.List list \n";
        str += "rule rule1 \n";
        str += "    dialect \"java\" \n";
        str += "when \n";
        str += "then \n";
        str += "    System.out.println( \"hello1!!!\" ); \n";
        str += "end \n";
        str += "rule rule2 \n";
        str += "    dialect \"java\" \n";
        str += "when \n";
        str += "then \n";
        str += "    System.out.println( \"hello2!!!\" ); \n";
        str += "end \n";

        KnowledgeBuilder kbuilder = node.get(KnowledgeBuilderFactoryService.class).newKnowledgeBuilder();
        Assert.assertNotNull(kbuilder);

        kbuilder.add( ResourceFactory.newByteArrayResource( str.getBytes() ),
                      ResourceType.DRL );

        if ( kbuilder.hasErrors() ) {
            System.out.println( "Errors: " + kbuilder.getErrors() );
        }

        KnowledgeBase kbase = node.get(KnowledgeBaseFactoryService.class).newKnowledgeBase();
        Assert.assertNotNull(kbase);

        kbase.addKnowledgePackages( kbuilder.getKnowledgePackages() );

        StatefulKnowledgeSession ksession1 = kbase.newStatefulKnowledgeSession();
        System.out.println("registering ksession1!");
        node.get(DirectoryLookupFactoryService.class).register( "ksession1", ksession1 );

        System.out.println("Let's go for the Second ksession");
        //Switching to another node
        node = connection.getExecutionNode();

        KnowledgeBuilder kbuilder2 = node.get(KnowledgeBuilderFactoryService.class).newKnowledgeBuilder();
        Assert.assertNotNull(kbuilder2);

        kbuilder2.add( ResourceFactory.newByteArrayResource( str.getBytes() ),
                      ResourceType.DRL );

        if ( kbuilder2.hasErrors() ) {
            System.out.println( "Errors: " + kbuilder2.getErrors() );
        }


        KnowledgeBase kbase2 = node.get(KnowledgeBaseFactoryService.class).newKnowledgeBase();
        Assert.assertNotNull(kbase2);


        kbase2.addKnowledgePackages( kbuilder2.getKnowledgePackages() );

        StatefulKnowledgeSession ksession2 = kbase2.newStatefulKnowledgeSession();
        System.out.println("registering ksession2!");
        node.get(DirectoryLookupFactoryService.class).register( "ksession2", ksession2 );

        System.out.println("Lookuping up ksession1 !");
        int fired = ((StatefulKnowledgeSession)node.get(DirectoryLookupFactoryService.class).lookup( "ksession1" ) ).fireAllRules();

        Assert.assertEquals( 2,
                     fired );

        System.out.println("Lookuping up ksession2 !");
        int fired2 = ((StatefulKnowledgeSession)node.get(DirectoryLookupFactoryService.class).lookup( "ksession2" ) ).fireAllRules();

        Assert.assertEquals( 2, fired2 );
    }

    @Test
    public void justwait() throws RemoteException, ConnectorException, InterruptedException{
        Thread.sleep(10000);
    }

//   @Test
//   public void scalingUp(){
//       for(int i=1; i<5; i++) {
//
//       List<GenericNodeConnector> services = connection.getNodeConnectors();
//         for(GenericNodeConnector serviceConnector : services) {
//             if(serviceConnector instanceof ExecutionNodeService){
//                    Throwable thrown = null;
//                    try {
//                        double d = ((ExecutionNodeService)serviceConnector).getLoad();
//                        if(d<80)
//                            ((ExecutionNodeService)serviceConnector).setLoad(d+10);
//                        else
//                            ((ExecutionNodeService)serviceConnector).setLoad(d);
//                        Thread.sleep(5000);
//                    } catch (Exception e) {
//                        thrown = e;
//                    }
//                    Assert.assertNull(thrown);
//                }
//         }
//       }
//
//   }



}
