package org.drools.services;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.rmi.RemoteException;

import junit.framework.Assert;

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
import org.drools.grid.ExecutionNode;
import org.drools.grid.GenericNodeConnector;
import org.drools.grid.internal.GenericMessageHandlerImpl;
import org.drools.grid.internal.NodeData;
import org.drools.grid.remote.mina.MinaAcceptor;
import org.drools.grid.remote.mina.MinaIoHandler;
import org.drools.grid.services.ExecutionEnvironment;
import org.drools.grid.services.GridTopology;
import org.drools.grid.services.configuration.MinaProvider;
import org.drools.grid.services.strategies.RandomEnvironmentSelectionStrategy;
import org.drools.io.ResourceFactory;
import org.drools.runtime.StatefulKnowledgeSession;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author: salaboy
 */
public class BasicAPITestWithMina {

    private MinaAcceptor server1;
    private MinaAcceptor server2;
    private GridTopology grid;

    @Before
    public void setUp() throws IOException {

        System.out.println("Server 1 Starting!");
        // the servers should be started in a different machine (jvm or physical) or in another thread
        SocketAddress address = new InetSocketAddress("127.0.0.1", 9123);
        NodeData nodeData = new NodeData();
        // setup Server
        SocketAcceptor acceptor = new NioSocketAcceptor();
        acceptor.setHandler(new MinaIoHandler(SystemEventListenerFactory.getSystemEventListener(),
                new GenericMessageHandlerImpl(nodeData,
                SystemEventListenerFactory.getSystemEventListener())));
        server1 = new MinaAcceptor(acceptor, address);
        server1.start();
        System.out.println("Server 1 Started! at = " + address.toString());


        System.out.println("Server 2 Starting!");
        // the servers should be started in a different machine (jvm or physical) or in another thread
        address = new InetSocketAddress("127.0.0.1", 9124);
        nodeData = new NodeData();
        // setup Server
        acceptor = new NioSocketAcceptor();
        acceptor.setHandler(new MinaIoHandler(SystemEventListenerFactory.getSystemEventListener(),
                new GenericMessageHandlerImpl(nodeData,
                SystemEventListenerFactory.getSystemEventListener())));
        server2 = new MinaAcceptor(acceptor, address);
        server2.start();
        System.out.println("Server 2 Started! at = " + address.toString());


    }

    @After
    public void stop() throws ConnectorException, RemoteException {

        grid.dispose();
        Assert.assertEquals(0, server1.getCurrentSessions());
        server1.stop();
        System.out.println("Server 1 Stopped!");
        Assert.assertEquals(0, server2.getCurrentSessions());
        server2.stop();
        System.out.println("Server 2 Stopped!");




    }

    @Test
    public void singleMinaProvider() throws ConnectorException, RemoteException, RemoteException, RemoteException, RemoteException {

        //This APIs are used to create the Execution Environment Topology that will define which logical set of nodes
        //will be used for a specific situation/use case.

        //The Execution Environment Topology will contain the Runtime state, persistent in time to be able to restore the
        //topology in case of failure or restarting
        grid = new GridTopology("MyCompanyTopology");

      
        //Create the provider
        MinaProvider provider = new MinaProvider("127.0.0.1", 9123);
        //Register the provider into the topology
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

        int fired = ksession.fireAllRules();
        Assert.assertEquals(2, fired);



    }


   

    @Test
    public void multiMinaProvider() throws ConnectorException {

        //This APIs are used to create the Execution Environment Topology that will define which logical set of nodes
        //will be used for a specific situation/use case.

        //The Execution Environment Topology will contain the Runtime state, persistent in time to be able to restore the 
        //topology in case of failure or restarting

        
        grid = new GridTopology("MyCompanyTopology");

       
        //Create the provider
        MinaProvider provider1 = new MinaProvider("127.0.0.1", 9123);
        //Register the provider into the topology
        grid.registerExecutionEnvironment("MyMinaExecutionEnv1", provider1);


        //Create the provider
         MinaProvider provider2 = new MinaProvider("127.0.0.1", 9124);
        //Register the provider into the topology
        grid.registerExecutionEnvironment("MyMinaExecutionEnv2", provider2);

        //Then we can get the registered Execution Environments by Name
        ExecutionEnvironment ee = grid.getBestExecutionEnvironment(new RandomEnvironmentSelectionStrategy());

        Assert.assertNotNull(ee);
        System.out.println("Selected Environment = " + ee.getName());

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

        int fired = ksession.fireAllRules();
        Assert.assertEquals(2, fired);

    }

}
