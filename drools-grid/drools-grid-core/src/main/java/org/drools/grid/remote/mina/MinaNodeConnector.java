/**
 * Copyright 2010 JBoss Inc
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
 */

package org.drools.grid.remote.mina;

import java.net.SocketAddress;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.serialization.ObjectSerializationCodecFactory;
import org.apache.mina.transport.socket.SocketConnector;
import org.drools.SystemEventListener;
import org.drools.grid.generic.GenericNodeConnector;
import org.drools.grid.generic.GenericIoWriter;
import org.drools.grid.generic.Message;
import org.drools.grid.generic.MessageResponseHandler;
import org.drools.grid.responsehandlers.BlockingMessageResponseHandler;

public class MinaNodeConnector
    implements
    GenericNodeConnector,
    GenericIoWriter {


    protected IoSession           session;

    protected final String        name;
    protected AtomicInteger       counter;
    protected SocketConnector     connector;
    protected SocketAddress       address;
    protected SystemEventListener eventListener;

    public MinaNodeConnector(String name,
                         SocketConnector connector,
                         SocketAddress address,
                         SystemEventListener eventListener) {
        if ( name == null ) {
            throw new IllegalArgumentException( "Name can not be null" );
        }
        this.name = name;
        this.counter = new AtomicInteger();
        this.address = address;
        this.connector = connector;
        this.eventListener = eventListener;
    }

    /* (non-Javadoc)
     * @see org.drools.vsm.mina.Messenger#connect()
     */
    public boolean connect() {
        if ( session != null && session.isConnected() ) {
            throw new IllegalStateException( "Already connected. Disconnect first." );
        }

        try {
            this.connector.getFilterChain().addLast( "codec",
                                                     new ProtocolCodecFilter( new ObjectSerializationCodecFactory() ) );

            ConnectFuture future1 = this.connector.connect( this.address );
            future1.await( 2000 );
            if ( !future1.isConnected() ) {
                eventListener.info( "unable to connect : " + address + " : " + future1.getException() );
                return false;
            }
            eventListener.info( "connected : " + address );
            this.session = future1.getSession();
            return true;
        } catch ( Exception e ) {
            e.printStackTrace();
            return false;
        }
    }

    /* (non-Javadoc)
     * @see org.drools.vsm.mina.Messenger#disconnect()
     */
    public void disconnect() {
        if ( session != null && session.isConnected() ) {
            session.close();
            session.getCloseFuture().join();
        }
    }

    private void addResponseHandler(int id,
                                    MessageResponseHandler responseHandler) {
        ((MinaIoHandler) this.connector.getHandler()).addResponseHandler( id,
                                                                          responseHandler );
    }

    public void write(Message msg,
                      MessageResponseHandler responseHandler) {
        if ( responseHandler != null ) {
            addResponseHandler( msg.getResponseId(),
                                responseHandler );
        }
        this.session.write( msg );
    }

    public Message write(Message msg) {
        BlockingMessageResponseHandler responseHandler = new BlockingMessageResponseHandler();

        if ( responseHandler != null ) {
            addResponseHandler( msg.getResponseId(),
                                responseHandler );
        }
        this.session.write( msg );

        Message returnMessage = responseHandler.getMessage();
        if ( responseHandler.getError() != null ) {
            throw responseHandler.getError();
        }

        return returnMessage;
    }

    public String getId() {
        return String.valueOf(session.getId());
    }

    public void setSession(Object object) {
        this.session = (IoSession) object;
    }
}
