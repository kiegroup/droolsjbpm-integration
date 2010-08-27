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
import org.drools.grid.services.configuration.LocalProvider;
import org.drools.grid.services.configuration.MinaProvider;
import org.drools.grid.services.factory.GridTopologyFactory;
import org.drools.grid.services.strategies.ExecutionEnvByPrioritySelectionStrategy;
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
public class ServiceAPITest {

    private MinaAcceptor server;
    private GridTopology grid;

    public ServiceAPITest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

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
        this.server = new MinaAcceptor( acceptor,
                                        address );
        this.server.start();
        System.out.println( "Server 1 Started! at = " + address.toString() );

    }

    @After
    public void tearDown() throws ConnectorException,
                          RemoteException {

        this.grid.dispose();
        System.out.println( "Stoping Server 1!" );
        Assert.assertEquals( 0,
                             this.server.getCurrentSessions() );
        this.server.stop();
    }

    @Test
    public void mixedTopologyMinaAndLocal() throws ConnectorException,
                                           RemoteException {

        GridTopologyConfiguration gridTopologyConfiguration = new GridTopologyConfiguration( "MyTopology" );
        gridTopologyConfiguration
                .addExecutionEnvironment( new ExecutionEnvironmentConfiguration( "MyMinaEnv",
                                                                                 new MinaProvider( "127.0.0.1",
                                                                                                   9123 ) ) );
        gridTopologyConfiguration
                .addExecutionEnvironment( new ExecutionEnvironmentConfiguration( "MyLocalEnv",
                                                                                 new LocalProvider() ) );

        this.grid = GridTopologyFactory.build( gridTopologyConfiguration );
        Assert.assertNotNull( this.grid );

        ExecutionEnvironment ee = this.grid.getBestExecutionEnvironment( new ExecutionEnvByPrioritySelectionStrategy() );
        Assert.assertNotNull( ee );

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