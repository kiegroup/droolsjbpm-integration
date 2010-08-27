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
package org.drools.grid.remote.directory;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.rmi.RemoteException;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.mina.core.future.CloseFuture;
import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.future.IoFuture;
import org.apache.mina.core.future.IoFutureListener;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.serialization.ObjectSerializationCodecFactory;
import org.apache.mina.transport.socket.SocketConnector;
import org.apache.mina.transport.socket.nio.NioSocketConnector;
import org.drools.SystemEventListener;
import org.drools.SystemEventListenerFactory;
import org.drools.grid.ConnectorException;
import org.drools.grid.ConnectorType;
import org.drools.grid.GenericConnection;
import org.drools.grid.GenericNodeConnector;
import org.drools.grid.GridConnection;
import org.drools.grid.NodeConnectionType;
import org.drools.grid.internal.Message;
import org.drools.grid.internal.MessageResponseHandler;
import org.drools.grid.remote.mina.MinaIoHandler;

/**
 *
 * @author salaboy
 */
public class RemoteMinaDirectoryConnector
    implements
    GenericNodeConnector {

    private GridConnection      connection;
    private String              providerName;
    private SystemEventListener eventListener;
    protected IoSession         session;
    private AtomicInteger       counter;
    private SocketConnector     connector;
    private SocketAddress       address;

    public RemoteMinaDirectoryConnector(String providerName,
                                        String providerAddress,
                                        Integer providerPort,
                                        SystemEventListener systemEventListener) {

        if ( providerName == null ) {
            throw new IllegalArgumentException( "Name can not be null" );
        }
        this.counter = new AtomicInteger();
        this.providerName = providerName;
        this.eventListener = systemEventListener;
        this.address = new InetSocketAddress( providerAddress,
                                              providerPort );
        this.connection = new GridConnection();

    }

    public void connect() throws ConnectorException {
        if ( this.session != null && this.session.isConnected() ) {
            throw new IllegalStateException( "Already connected. Disconnect first." );
        }

        try {
            this.connector = new NioSocketConnector();
            this.connector.setHandler( new MinaIoHandler( SystemEventListenerFactory.getSystemEventListener() ) );
            this.connector.getFilterChain().addLast( "codec" + UUID.randomUUID().toString(),
                                                     new ProtocolCodecFilter( new ObjectSerializationCodecFactory() ) );

            ConnectFuture future1 = this.connector.connect( this.address );
            future1.await( 2000 );
            if ( !future1.isConnected() ) {
                this.eventListener.info( "unable to connect : " + this.address + " : " + future1.getException() );
                Logger.getLogger( RemoteMinaDirectoryConnector.class.getName() ).log( Level.SEVERE,
                                                                                      null,
                                                                                      "The Directory Connection Failed!" );
                throw new ConnectorException( "unable to connect : " + this.address + " : " + future1.getException() );
            }
            this.eventListener.info( "connected : " + this.address );
            this.session = future1.getSession();

        } catch ( Exception e ) {
            throw new ConnectorException( e );
        }
    }

    public void disconnect() throws ConnectorException {

        if ( this.session != null && this.session.isConnected() ) {

            CloseFuture future = this.session.getCloseFuture();
            future.addListener( new IoFutureListener<IoFuture>() {

                public void operationComplete(IoFuture future) {
                    System.out.println( "The remote directory session is now closed" );
                }
            } );
            this.session.close( false );

            future.awaitUninterruptibly();

            this.connector.dispose();
        }

    }

    public String getId() {
        String hostName = ((InetSocketAddress) this.address).getHostName();
        int hostPort = ((InetSocketAddress) this.address).getPort();
        return "Remote:Mina:Directory:" + hostName + ":" + hostPort;
    }

    public int getSessionId() {
        return (int) this.session.getId();
    }

    public IoSession getSession() {
        return this.session;
    }

    public GenericConnection getConnection() {
        return this.connection;
    }

    public ConnectorType getConnectorType() {
        return ConnectorType.REMOTE;
    }

    public Message write(Message msg) throws ConnectorException,
                                     RemoteException {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    public NodeConnectionType getNodeConnectionType() throws ConnectorException,
                                                     RemoteException {
        return new RemoteMinaConnectionDirectory();

    }

    public void write(Message msg,
                      MessageResponseHandler responseHandler) {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    public AtomicInteger getCounter() {
        return this.counter;
    }
}
