package org.drools.grid.remote.mina;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.serialization.ObjectSerializationCodecFactory;
import org.apache.mina.transport.socket.SocketConnector;
import org.apache.mina.transport.socket.nio.NioSocketConnector;
import org.drools.SystemEventListener;
import org.drools.grid.io.Connector;
import org.drools.grid.io.IoWriter;
import org.drools.grid.io.Message;
import org.drools.grid.io.MessageReceiverHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MinaConnector
    implements
    Connector {

    private static Logger logger = LoggerFactory.getLogger(MinaConnector.class);
    
    protected MinaIoWriter    writer;

    protected SocketConnector connector;

    public MinaConnector() {
    }

    /**
     * Allow users to cast to impl, so they can set their own Connector.
     * @param connector
     */
    public synchronized void setConnector(SocketConnector connector) {
        this.connector = connector;
    }

    public synchronized IoWriter open(InetSocketAddress address,
                                      MessageReceiverHandler handler,
                                      SystemEventListener systemEventListener) {
        if ( this.writer != null && this.writer.getIoSession().isConnected() ) {
            throw new IllegalStateException( "Already connected. Disconnect first." );
        } else {
            this.writer = null;
        }

        if ( address == null ) {
            throw new IllegalArgumentException( "Address cannot be null" );
        }
         
        if ( this.connector == null ) {
            // Allow users to pass their own configured SocketConnector
            this.connector = new NioSocketConnector();
            this.connector.getFilterChain().addLast( "codec",
                                                     new ProtocolCodecFilter(new ObjectSerializationCodecFactory() ) );
        }

        //        this.handler = new MessageHandler() {
        //            private Map<Integer, MessageResponseHandler> map = new HashMap<Integer, MessageResponseHandler>();
        //            
        //            public void messageReceived(IoWriter session,
        //                                        Message msg) throws Exception {
        //                MessageResponseHandler responseHandler = map.remove( msg.getRequestId() );
        //                if ( responseHandler != null ) {
        //                    responseHandler.receive( msg );
        //                }
        //            }
        //            
        //            public void addResponseHandler(int id,
        //                                           MessageResponseHandler responseHandler) {
        //                map.put( id, responseHandler );
        //            }
        //        };

        this.connector.setHandler( new MinaIoHandler( systemEventListener,
                                                      handler ) );

        if(logger.isTraceEnabled()){
            logger.trace(" ### Connecting with "+address.getHostName()+":"+address.getPort());
        }                                              
        ConnectFuture future1 = this.connector.connect( address );
        future1.join();
        if ( !future1.isConnected() ) {
            throw new IllegalStateException( "Unnable to connect to " + address );
        }
        IoSession session = future1.getSession();

        this.writer = new MinaIoWriter( session );

        return this.writer;
    }

    public synchronized void close() {
        if ( this.writer != null ) {
            IoSession session = this.writer.getIoSession();
            if ( session != null && session.isConnected() ) {
                session.close();
                session.getCloseFuture().join();
            }
            this.writer.dispose();
            this.writer = null;
            this.connector = null;
        }
    }

    public synchronized boolean isOpen() {
        if ( this.writer != null ) {
            IoSession session = this.writer.getIoSession();
            if ( session != null && session.isConnected() ) {
                return true;
            }
        }
        return false;
    }

}
