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
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.rmi.RemoteException;
import org.apache.mina.transport.socket.SocketAcceptor;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;
import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseFactoryService;
import org.drools.SystemEventListenerFactory;
import org.drools.builder.DirectoryLookupFactoryService;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderFactoryService;
import org.drools.builder.ResourceType;
import org.drools.grid.ConnectorException;
import org.drools.grid.DirectoryNodeService;
import org.drools.grid.ExecutionNode;
import org.drools.grid.GenericNodeConnector;
import org.drools.grid.internal.GenericMessageHandlerImpl;
import org.drools.grid.internal.NodeData;
import org.drools.grid.remote.directory.DirectoryServerMessageHandlerImpl;
import org.drools.grid.remote.mina.MinaAcceptor;
import org.drools.grid.remote.mina.MinaIoHandler;
import org.drools.grid.services.DirectoryInstance;
import org.drools.grid.services.ExecutionEnvironment;
import org.drools.grid.services.GridTopology;
import org.drools.grid.services.configuration.GenericProvider;
import org.drools.grid.services.configuration.LocalProvider;
import org.drools.grid.services.configuration.MinaProvider;
import org.drools.grid.services.strategies.DirectoryInstanceByPrioritySelectionStrategy;
import org.drools.io.ResourceFactory;
import org.drools.runtime.StatefulKnowledgeSession;


import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author salaboy
 */
public class RegisterMinaDirectoryTest {
    private GridTopology grid;
    private MinaAcceptor serverDir;
    private MinaAcceptor serverNode;
 
    public RegisterMinaDirectoryTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() throws InterruptedException, IOException {

       System.out.println("Dir Server 1 Starting!");
         // Directory Server configuration
        SocketAddress dirAddress = new InetSocketAddress("127.0.0.1", 9123);
        SocketAcceptor dirAcceptor = new NioSocketAcceptor();

        dirAcceptor.setHandler(new MinaIoHandler(SystemEventListenerFactory.getSystemEventListener(),
                new DirectoryServerMessageHandlerImpl(
                SystemEventListenerFactory.getSystemEventListener())));
        this.serverDir = new MinaAcceptor(dirAcceptor, dirAddress);
        this.serverDir.start();
        System.out.println("Dir Server 1 Started! at = " + dirAddress.toString());
        Thread.sleep(5000);
        // End Execution Server

        //Execution Node related stuff

           System.out.println("Exec Server 1 Starting!");
        // the servers should be started in a different machine (jvm or physical) or in another thread
        SocketAddress address = new InetSocketAddress("127.0.0.1", 9124);
        NodeData nodeData = new NodeData();
        // setup Server
        SocketAcceptor acceptor = new NioSocketAcceptor();
        acceptor.setHandler(new MinaIoHandler(SystemEventListenerFactory.getSystemEventListener(),
                new GenericMessageHandlerImpl(nodeData,
                SystemEventListenerFactory.getSystemEventListener())));
        serverNode = new MinaAcceptor(acceptor, address);
        serverNode.start();
        System.out.println("Exec Server 1 Started! at = " + address.toString());

        Thread.sleep(5000);

    }

    @After
    public void tearDown() throws InterruptedException, ConnectorException, RemoteException {

        grid.dispose();

        Assert.assertEquals(0, serverDir.getCurrentSessions());
        serverDir.stop();
        System.out.println("Dir Server Stopped!");
        
        Assert.assertEquals(0, serverNode.getCurrentSessions());
        serverNode.stop();
        System.out.println("Execution Server Stopped!");


    }

    @Test
     public void directoryRemoteTest() throws ConnectorException, RemoteException {
        grid = new GridTopology("MyBusinessUnit");

        GenericProvider remoteDirProvider = new MinaProvider("127.0.0.1", 9123);
        GenericProvider localEnvProvider = new LocalProvider();

        MinaProvider remoteEnvProvider = new MinaProvider("127.0.0.1", 9124);

        grid.registerDirectoryInstance("MyMinaDir", remoteDirProvider);
        grid.registerExecutionEnvironment("MyLocalEnv", localEnvProvider);
        grid.registerExecutionEnvironment("MyRemoteEnv", remoteEnvProvider);


        DirectoryInstance directory = grid.getBestDirectoryInstance(new DirectoryInstanceByPrioritySelectionStrategy());
        Assert.assertNotNull(directory);

        DirectoryNodeService dir = directory.getDirectoryService().get(DirectoryNodeService.class);
        Assert.assertNotNull(dir);

        Assert.assertNotNull("Dir Null", dir.getExecutorsMap());

        Assert.assertEquals(3, dir.getExecutorsMap().size());

        System.out.println("dir.getDirectoryMap() = "+dir.getExecutorsMap());

        Assert.assertEquals(3, dir.getExecutorsMap().size());

        directory.getConnector().disconnect();



                //Then we can get the registered Execution Environments by Name

        ExecutionEnvironment ee = grid.getExecutionEnvironment("MyRemoteEnv");
        Assert.assertNotNull(ee);

        // Give me an ExecutionNode in the selected environment
        // For the Mina we have just one Execution Node per server instance
        ExecutionNode node = ee.getExecutionNode();

        Assert.assertNotNull(node);


        // Do a basic Runtime Test that register a ksession and fire some rules.
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


        KnowledgeBuilder kbuilder =
                node.get(KnowledgeBuilderFactoryService.class).newKnowledgeBuilder();
        kbuilder.add(ResourceFactory.newByteArrayResource(str.getBytes()),
                ResourceType.DRL);

        if (kbuilder.hasErrors()) {
            System.out.println("Errors: " + kbuilder.getErrors());
        }

        KnowledgeBase kbase =
                node.get(KnowledgeBaseFactoryService.class).newKnowledgeBase();
        Assert.assertNotNull(kbase);

        kbase.addKnowledgePackages(kbuilder.getKnowledgePackages());

        StatefulKnowledgeSession ksession = kbase.newStatefulKnowledgeSession();
        Assert.assertNotNull(ksession);

        node.get(DirectoryLookupFactoryService.class).register("ksession1", ksession);

        ksession = (StatefulKnowledgeSession) node.get(DirectoryLookupFactoryService.class).lookup("ksession1");

        int fired = ksession.fireAllRules();
        Assert.assertEquals(2, fired);





     }

     @Test
    public void directoryInstanceRetriveKSessionFromEE() throws ConnectorException, RemoteException {

        //This APIs are used to create the Execution Environment Topology that will define which logical set of nodes
        //will be used for a specific situation/use case.

        //The Execution Environment Topology will contain the Runtime state, persistent in time to be able to restore the
        //topology in case of failure or restarting
        grid = new GridTopology("MyCompanyTopology");


        //Create the provider
        MinaProvider provider = new MinaProvider("127.0.0.1", 9124);
        GenericProvider remoteDirProvider = new MinaProvider("127.0.0.1", 9123);
        //Register the provider into the topology
        
        grid.registerDirectoryInstance("MyMinaDir", remoteDirProvider);
        grid.registerExecutionEnvironment("MyMinaExecutionEnv1", provider);

        //Then we can get the registered Execution Environments by Name

        ExecutionEnvironment ee = grid.getExecutionEnvironment("MyMinaExecutionEnv1");
        Assert.assertNotNull(ee);

        // Give me an ExecutionNode in the selected environment
        // For the Mina we have just one Execution Node per server instance
        ExecutionNode node = ee.getExecutionNode();

        Assert.assertNotNull(node);


        // Do a basic Runtime Test that register a ksession and fire some rules.
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


        KnowledgeBuilder kbuilder =
                node.get(KnowledgeBuilderFactoryService.class).newKnowledgeBuilder();
        kbuilder.add(ResourceFactory.newByteArrayResource(str.getBytes()),
                ResourceType.DRL);

        if (kbuilder.hasErrors()) {
            System.out.println("Errors: " + kbuilder.getErrors());
        }

        KnowledgeBase kbase =
                node.get(KnowledgeBaseFactoryService.class).newKnowledgeBase();
        Assert.assertNotNull(kbase);

        kbase.addKnowledgePackages(kbuilder.getKnowledgePackages());

        StatefulKnowledgeSession ksession = kbase.newStatefulKnowledgeSession();
        Assert.assertNotNull(ksession);

        node.get(DirectoryLookupFactoryService.class).register("sessionName", ksession);
        DirectoryInstance directoryInstance = grid.getDirectoryInstance();
        DirectoryNodeService directory = directoryInstance.getDirectoryService().get(DirectoryNodeService.class);
        GenericNodeConnector connector = directory.lookup("sessionName");
        
        directoryInstance.getConnector().disconnect();

        grid.dispose();
        //System.out.println("Connector -->"+connector.getId());

       node = grid.getExecutionEnvironment(connector).getExecutionNode();
//
       ksession = (StatefulKnowledgeSession) node.get(DirectoryLookupFactoryService.class).lookup("sessionName");
       Assert.assertNotNull(ksession);

    }


   
}
