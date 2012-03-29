package org.drools.grid.remote.mina;

import java.io.IOException;
import java.net.InetSocketAddress;

import org.apache.mina.core.service.IoAcceptor;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.serialization.ObjectSerializationCodecFactory;
import org.apache.mina.filter.logging.LoggingFilter;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;
import org.drools.SystemEventListener;
import org.drools.grid.DaemonService;
import org.drools.grid.io.Acceptor;
import org.drools.grid.io.IoWriter;
import org.drools.grid.io.MessageReceiverHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MinaAcceptor
    implements
    Acceptor {
    private static Logger logger = LoggerFactory.getLogger(MinaAcceptor.class);
    private IoAcceptor             acceptor;

    private MessageReceiverHandler handler;

    public MinaAcceptor() {
    }

    public synchronized void setAcceptor(IoAcceptor acceptor) {
        this.acceptor = acceptor;
    }

    public synchronized void open(InetSocketAddress address,
                                  MessageReceiverHandler handler,
                                  SystemEventListener systemEventListener) {
        if ( logger.isTraceEnabled() ){
            logger.trace("(" + Thread.currentThread().getId() + ")"+Thread.currentThread().getName() +" ### Binding a new SocketAcceptor to "+address.getHostName()+":"+address.getPort());
        }
        if ( this.acceptor == null ) {
            acceptor = new NioSocketAcceptor( 16 );

            acceptor.getFilterChain().addLast( "codec",
                                               new ProtocolCodecFilter( new ObjectSerializationCodecFactory() ) );
            acceptor.getSessionConfig().setReadBufferSize( 2048 );
            acceptor.getSessionConfig().setIdleTime( IdleStatus.BOTH_IDLE,
                                                     100 );
        }

        this.handler = handler;
        acceptor.setHandler( new MinaIoHandler( systemEventListener,
                                                handler ) );

        try {
            acceptor.bind( address );
        } catch ( IOException e ) {
            throw new RuntimeException( "(" + Thread.currentThread().getId() + ")"+Thread.currentThread().getName() +"Unable to bind Mina Acceptor",
                                        e );
        }
    }

    public synchronized void close() {
        acceptor.unbind();
        acceptor.dispose();
        this.acceptor = null;
    }

    public synchronized boolean isOpen() {
        if ( this.acceptor != null && this.acceptor.isActive() ) {
            return true;
        } else {
            return false;
        }
    }

    public synchronized IoAcceptor getIoAcceptor() {
        return this.acceptor;
    }

    public int getCurrentSessions() {
        return this.acceptor.getManagedSessionCount();
    }

    public MessageReceiverHandler getMessageReceiverHandler() {
        return this.handler;
    }

}
