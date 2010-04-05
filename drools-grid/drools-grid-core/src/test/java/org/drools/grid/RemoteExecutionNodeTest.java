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

import java.rmi.RemoteException;
import java.util.Iterator;
import org.drools.grid.remote.mina.MinaNodeConnector;
import org.drools.grid.generic.GenericNodeConnector;
import org.apache.mina.transport.socket.nio.NioSocketConnector;
import java.io.IOException;
import org.drools.grid.remote.mina.MinaAcceptor;
import org.drools.grid.generic.GenericMessageHandlerImpl;
import org.drools.grid.remote.mina.MinaIoHandler;
import org.drools.SystemEventListenerFactory;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;
import org.apache.mina.transport.socket.SocketAcceptor;
import org.drools.grid.generic.NodeData;
import java.net.SocketAddress;
import java.net.InetSocketAddress;
import org.drools.grid.DirectoryNodeService;
import org.drools.grid.RemoteConnection;
import org.drools.grid.local.DirectoryNodeLocalImpl;
import org.drools.grid.strategies.StaticIncrementalSelectionStrategy;
import org.junit.After;
import org.junit.Before;

/**
 *
 * @author salaboy
 */
public class RemoteExecutionNodeTest extends ExecutionNodeBaseTest{
    private MinaAcceptor server;
    
    public RemoteExecutionNodeTest() {
    }

   

    @Before
    public void configureNode() throws IOException {
        connection = new RemoteConnection();

        StaticIncrementalSelectionStrategy.counter = 0;
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

        connection.addDirectoryNode(new DirectoryNodeLocalImpl());

        // setup Client
        NioSocketConnector clientConnector = new NioSocketConnector();
        clientConnector.setHandler( new MinaIoHandler( SystemEventListenerFactory.getSystemEventListener() ) );
        GenericNodeConnector minaClient = new MinaNodeConnector( "client 1",
                                                         clientConnector,
                                                         address,
                                                         SystemEventListenerFactory.getSystemEventListener() );
        connection.addNodeConnector(minaClient);
        node = connection.getExecutionNode(null); 
        
    }

    @After
    public void tearDown() throws RemoteException {
        
        for(Iterator<GenericNodeConnector> iterator = connection.getNodeConnectors().iterator(); iterator.hasNext();){
            iterator.next().disconnect();
        }
       
        this.server.stop();

    }

    

}