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

package org.drools.services;

import java.io.IOException;
import org.junit.Assert;
import org.drools.grid.services.configuration.GenericProvider;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseFactoryService;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderFactoryService;
import org.drools.builder.ResourceType;
import org.drools.grid.AcceptorService;
import org.drools.grid.ConnectorException;
import org.drools.grid.DirectoryNodeService;
import org.drools.grid.ExecutionNode;
import org.drools.grid.ExecutionNodeService;
import org.drools.grid.services.ExecutionEnvironment;
import org.drools.grid.services.GridTopology;
import org.drools.grid.services.configuration.LocalProvider;
import org.drools.grid.services.configuration.RioProvider;
import org.drools.grid.services.strategies.ExecutionEnvByPrioritySelectionStrategy;
import org.drools.io.ResourceFactory;
import org.drools.runtime.StatefulKnowledgeSession;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
//import net.jini.core.lookup.ServiceItem;
//import org.rioproject.gnostic.Gnostic;
//import org.rioproject.sla.RuleMap;
//import org.rioproject.test.RioTestRunner;
//import org.rioproject.test.SetTestManager;
//import org.rioproject.test.TestManager;

/**
 *
 * @author salaboy
 */
//    @RunWith(RioTestRunner.class)
public class ITGridExecutionTest {

//    AcceptorService server;
//    @SetTestManager
//    static TestManager testManager;
//    private GridTopology grid;
//    private List<ExecutionNodeService> executionNodes = new ArrayList<ExecutionNodeService>();
//    private List<DirectoryNodeService> directoryNodes = new ArrayList<DirectoryNodeService>();
    
    public ITGridExecutionTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() throws IOException {
//        Assert.assertNotNull(testManager);
//        //setup all the services using the Rio Test Framework
//        //In real scenarios all this code will happen inside the connection.connect() method
//
//        DirectoryNodeService directoryService = (DirectoryNodeService) testManager.waitForService(DirectoryNodeService.class);
//        Assert.assertNotNull(directoryService);
//        directoryNodes.add(directoryService);
//        Gnostic gnostic = (Gnostic)testManager.waitForService(Gnostic.class);
//
//        waitForRule(gnostic, "SLAKsessions");
//
//
//
//        ExecutionNodeService executionNode = (ExecutionNodeService) testManager.waitForService(ExecutionNodeService.class);
//        Assert.assertNotNull(executionNode);
//        ServiceItem[] nodeServiceItems = testManager.getServiceItems(ExecutionNodeService.class);
//        System.out.println("ExecutionNodes Items =" + nodeServiceItems.length);
//
//        for (int i = 0; i < nodeServiceItems.length; i++) {
//            if (nodeServiceItems[i].service instanceof ExecutionNodeService) {
//                executionNodes.add((ExecutionNodeService) nodeServiceItems[i].service);
//            }
//        }

    }

    @After
    public void tearDown() throws ConnectorException, RemoteException {
//        System.out.println("Disconecting all clients");
//        grid.dispose();
//
        
    }
    @Test
    public void doNothing(){}
    
     @Test
     public void rioProviderTest() throws ConnectorException, RemoteException {
//        grid = new GridTopology("MyBusinessUnit");
//
//
//        GenericProvider rioProvider = new RioProvider();
//        GenericProvider rioDirProvider = new RioProvider();
//
//
//        grid.registerExecutionEnvironment("MyRioEnv", rioProvider);
//        grid.registerDirectoryInstance("MyRioDir", rioDirProvider);
//
//
//
//        ExecutionEnvironment ee = grid.getBestExecutionEnvironment(new ExecutionEnvByPrioritySelectionStrategy());
//        Assert.assertNotNull(ee);
//        System.out.println("EE Name = "+ee.getName());
//        System.out.println("Connector Id = "+ee.getConnector().getId());
//
//        ExecutionNode node = ee.getExecutionNode();
//        Assert.assertNotNull(node);
//
//         // Do a basic Runtime Test that register a ksession and fire some rules.
//        String str = "";
//        str += "package org.drools \n";
//        str += "global java.util.List list \n";
//        str += "rule rule1 \n";
//        str += "    dialect \"java\" \n";
//        str += "when \n";
//        str += "then \n";
//        str += "    System.out.println( \"hello1!!!\" ); \n";
//        str += "end \n";
//        str += "rule rule2 \n";
//        str += "    dialect \"java\" \n";
//        str += "when \n";
//        str += "then \n";
//        str += "    System.out.println( \"hello2!!!\" ); \n";
//        str += "end \n";
//
//
//        KnowledgeBuilder kbuilder =
//                node.get(KnowledgeBuilderFactoryService.class).newKnowledgeBuilder();
//        kbuilder.add(ResourceFactory.newByteArrayResource(str.getBytes()),
//                ResourceType.DRL);
//
//        if (kbuilder.hasErrors()) {
//            System.out.println("Errors: " + kbuilder.getErrors());
//        }
//
//        KnowledgeBase kbase =
//                node.get(KnowledgeBaseFactoryService.class).newKnowledgeBase();
//        Assert.assertNotNull(kbase);
//
//        kbase.addKnowledgePackages(kbuilder.getKnowledgePackages());
//
//        StatefulKnowledgeSession ksession = kbase.newStatefulKnowledgeSession();
//        Assert.assertNotNull(ksession);
//
//        int fired = ksession.fireAllRules();
//        Assert.assertEquals(2, fired);
//




     }

//      private void waitForRule(Gnostic g, String rule) {
//        Throwable thrown = null;
//        long t0 = System.currentTimeMillis();
//        try {
//            while (!hasRule(g.get(), rule)) {
//                sleep(500);
//            }
//        } catch (RemoteException e) {
//            e.printStackTrace();
//            thrown = e;
//        }
//        Assert.assertNull(thrown);
//        System.out.println("Rule loaded in " + (System.currentTimeMillis() - t0) + " millis");
//    }
//
//     private boolean hasRule(List<RuleMap> ruleMaps, String rule) {
//        boolean hasRule = false;
//        for (RuleMap ruleMap : ruleMaps) {
//            System.out.println("===> rule: " + ruleMap.getRuleDefinition().getResource());
//            if (ruleMap.getRuleDefinition().getResource().indexOf(rule) != -1) {
//                hasRule = true;
//                break;
//            }
//        }
//        return hasRule;
//    }
//    public static void sleep(long l) {
//        try {
//            Thread.sleep(l);
//        } catch (InterruptedException e) {
//
//        }
//    }

}