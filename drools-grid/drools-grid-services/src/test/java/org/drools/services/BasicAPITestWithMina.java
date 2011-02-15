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
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderFactoryService;
import org.drools.builder.ResourceType;
import org.drools.grid.ConnectorException;
import org.drools.grid.ExecutionNode;
import org.drools.grid.internal.GenericMessageHandlerImpl;
import org.drools.grid.internal.NodeData;
import org.drools.grid.remote.mina.MinaAcceptor;
import org.drools.grid.remote.mina.MinaIoHandler;
import org.drools.grid.services.ExecutionEnvironment;
import org.drools.grid.services.GridTopology;
import org.drools.grid.services.configuration.ExecutionEnvironmentConfiguration;
import org.drools.grid.services.configuration.GridTopologyConfiguration;
import org.drools.grid.services.configuration.MinaProvider;
import org.drools.grid.services.factory.GridTopologyFactory;
import org.drools.grid.services.strategies.RandomEnvironmentSelectionStrategy;
import org.drools.io.ResourceFactory;
import org.drools.runtime.StatefulKnowledgeSession;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class BasicAPITestWithMina {

    private MinaAcceptor server1;
    private MinaAcceptor server2;
    private GridTopology grid;

    @Before
    public void setUp() throws IOException {

        System.out.println( "Server 1 Starting!" );
        // the servers should be started in a different machine (jvm or physical) or in another thread
        SocketAddress address = new InetSocketAddress( "127.0.0.1",
                                                       9123 );
        NodeData nodeData = new NodeData();
        // setup Server
        SocketAcceptor acceptor = new NioSocketAcceptor();
        acceptor.setHandler( new MinaIoHandler( SystemEventListenerFactory.getSystemEventListener(),
                                                new GenericMessageHandlerImpl( nodeData,
                                                                               SystemEventListenerFactory.getSystemEventListener() ) ) );
        this.server1 = new MinaAcceptor( acceptor,
                                         address );
        this.server1.start();
        System.out.println( "Server 1 Started! at = " + address.toString() );

        System.out.println( "Server 2 Starting!" );
        // the servers should be started in a different machine (jvm or physical) or in another thread
        address = new InetSocketAddress( "127.0.0.1",
                                         9124 );
        nodeData = new NodeData();
        // setup Server
        acceptor = new NioSocketAcceptor();
        acceptor.setHandler( new MinaIoHandler( SystemEventListenerFactory.getSystemEventListener(),
                                                new GenericMessageHandlerImpl( nodeData,
                                                                               SystemEventListenerFactory.getSystemEventListener() ) ) );
        this.server2 = new MinaAcceptor( acceptor,
                                         address );
        this.server2.start();
        System.out.println( "Server 2 Started! at = " + address.toString() );

    }

    @After
    public void stop() throws ConnectorException,
                      RemoteException {

        this.grid.dispose();
        Assert.assertEquals( 0,
                             this.server1.getCurrentSessions() );
        this.server1.stop();
        System.out.println( "Server 1 Stopped!" );
        Assert.assertEquals( 0,
                             this.server2.getCurrentSessions() );
        this.server2.stop();
        System.out.println( "Server 2 Stopped!" );

    }

    @Test
    public void singleMinaProvider() throws ConnectorException,
                                    RemoteException,
                                    RemoteException,
                                    RemoteException,
                                    RemoteException {

        GridTopologyConfiguration gridTopologyConfiguration = new GridTopologyConfiguration( "MyTopology" );
        gridTopologyConfiguration.addExecutionEnvironment(
                new ExecutionEnvironmentConfiguration( "MyMinaExecutionEnv1",
                                                       new MinaProvider( "127.0.0.1",
                                                                         9123 ) ) );

        this.grid = GridTopologyFactory.build( gridTopologyConfiguration );

        Assert.assertNotNull( this.grid );

        //Then we can get the registered Execution Environments by Name

        ExecutionEnvironment ee = this.grid.getExecutionEnvironment( "MyMinaExecutionEnv1" );
        Assert.assertNotNull( ee );

        // Give me an ExecutionNode in the selected environment
        // For the Mina we have just one Execution Node per server instance
        ExecutionNode node = ee.getExecutionNode();

        Assert.assertNotNull( node );

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
                node.get( KnowledgeBuilderFactoryService.class ).newKnowledgeBuilder();
        kbuilder.add( ResourceFactory.newByteArrayResource( str.getBytes() ),
                      ResourceType.DRL );

        if ( kbuilder.hasErrors() ) {
            System.out.println( "Errors: " + kbuilder.getErrors() );
        }

        KnowledgeBase kbase =
                node.get( KnowledgeBaseFactoryService.class ).newKnowledgeBase();
        Assert.assertNotNull( kbase );

        kbase.addKnowledgePackages( kbuilder.getKnowledgePackages() );

        StatefulKnowledgeSession ksession = kbase.newStatefulKnowledgeSession();
        Assert.assertNotNull( ksession );

        int fired = ksession.fireAllRules();
        Assert.assertEquals( 2,
                             fired );

    }

    @Test
    public void multiMinaProvider() throws ConnectorException {

        GridTopologyConfiguration gridTopologyConfiguration = new GridTopologyConfiguration( "MyTopology" );
        gridTopologyConfiguration.addExecutionEnvironment(
                new ExecutionEnvironmentConfiguration( "MyMinaExecutionEnv1",
                                                       new MinaProvider( "127.0.0.1",
                                                                         9123 ) ) );
        gridTopologyConfiguration.addExecutionEnvironment(
                new ExecutionEnvironmentConfiguration( "MyMinaExecutionEnv2",
                                                       new MinaProvider( "127.0.0.1",
                                                                         9124 ) ) );

        this.grid = GridTopologyFactory.build( gridTopologyConfiguration );

        Assert.assertNotNull( this.grid );

        //Then we can get the registered Execution Environments by Name
        ExecutionEnvironment ee = this.grid.getBestExecutionEnvironment( new RandomEnvironmentSelectionStrategy() );

        Assert.assertNotNull( ee );
        System.out.println( "Selected Environment = " + ee.getName() );

        // Give me an ExecutionNode in the selected environment
        // For the Mina we have just one Execution Node per server instance
        ExecutionNode node = ee.getExecutionNode();
        Assert.assertNotNull( node );

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
                node.get( KnowledgeBuilderFactoryService.class ).newKnowledgeBuilder();
        kbuilder.add( ResourceFactory.newByteArrayResource( str.getBytes() ),
                      ResourceType.DRL );

        if ( kbuilder.hasErrors() ) {
            System.out.println( "Errors: " + kbuilder.getErrors() );
        }

        KnowledgeBase kbase =
                node.get( KnowledgeBaseFactoryService.class ).newKnowledgeBase();
        Assert.assertNotNull( kbase );

        kbase.addKnowledgePackages( kbuilder.getKnowledgePackages() );

        StatefulKnowledgeSession ksession = kbase.newStatefulKnowledgeSession();
        Assert.assertNotNull( ksession );

        int fired = ksession.fireAllRules();
        Assert.assertEquals( 2,
                             fired );

    }

}
