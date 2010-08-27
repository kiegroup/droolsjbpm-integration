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

package org.drools.grid;

import java.io.IOException;

import org.apache.mina.transport.socket.SocketAcceptor;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;
import org.drools.SystemEventListenerFactory;
import org.drools.grid.internal.GenericMessageHandlerImpl;
import org.drools.grid.internal.NodeData;
import org.drools.grid.local.LocalDirectoryConnector;
import org.drools.grid.remote.mina.MinaAcceptor;
import org.drools.grid.remote.mina.MinaIoHandler;
import org.drools.grid.remote.mina.RemoteMinaNodeConnector;
import org.drools.grid.strategies.StaticIncrementalNodeSelectionStrategy;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;

/**
 *
 * @author salaboy
 */
public class RemoteExecutionNodeTest extends ExecutionNodeBase {
    private MinaAcceptor      server;
    private GenericConnection connection;

    public RemoteExecutionNodeTest() {
    }

    @Before
    public void configureNode() throws IOException,
                               ConnectorException {

        //Starting the server

        StaticIncrementalNodeSelectionStrategy.counter = 0;

        String address = "127.0.0.1";
        int port = 9123;

        NodeData nodeData = new NodeData();
        // setup Server
        SocketAcceptor acceptor = new NioSocketAcceptor();
        acceptor.setHandler( new MinaIoHandler( SystemEventListenerFactory.getSystemEventListener(),
                                                new GenericMessageHandlerImpl( nodeData,
                                                                               SystemEventListenerFactory.getSystemEventListener() ) ) );
        this.server = new MinaAcceptor( acceptor,
                                        address,
                                        port );
        this.server.start();
        System.out.println( "Server Started!" );

        //Client configuration
        this.connection = new GridConnection();
        GenericNodeConnector localDirectory = new LocalDirectoryConnector();
        this.connection.addDirectoryNode( localDirectory );

        // setup Client

        GenericNodeConnector minaClient = new RemoteMinaNodeConnector( "client 1",
                                                                       address,
                                                                       port,
                                                                       SystemEventListenerFactory.getSystemEventListener() );
        this.connection.addExecutionNode( minaClient );

        this.node = this.connection.getExecutionNode();
        Assert.assertNotNull( this.node );

    }

    @After
    public void tearDown() throws ConnectorException {
        System.out.println( "Stopping the server and all the clients!" );
        this.connection.dispose();

        this.server.stop();

    }

}