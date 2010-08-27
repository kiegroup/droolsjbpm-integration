package org.drools.grid.remote.mina;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

import org.apache.mina.core.service.IoAcceptor;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.serialization.ObjectSerializationCodecFactory;
import org.apache.mina.filter.logging.LoggingFilter;
import org.apache.mina.transport.socket.SocketAcceptor;
import org.drools.grid.AcceptorService;

public class MinaAcceptor
    implements
    AcceptorService {

    protected SocketAcceptor acceptor;
    protected SocketAddress  address;

    public MinaAcceptor(SocketAcceptor acceptor,
                        SocketAddress address) {
        this.acceptor = acceptor;
        this.address = address;
    }

    public MinaAcceptor(SocketAcceptor acceptor,
                        String address,
                        int port) {
        this.acceptor = acceptor;
        this.address = new InetSocketAddress( address,
                                              port );

    }

    public synchronized void start() throws IOException {
        this.acceptor.getFilterChain().addLast( "logger",
                                                new LoggingFilter() );
        this.acceptor.getFilterChain().addLast( "codec",
                                                new ProtocolCodecFilter( new ObjectSerializationCodecFactory() ) );
        this.acceptor.getSessionConfig().setReadBufferSize( 2048 );
        this.acceptor.getSessionConfig().setIdleTime( IdleStatus.BOTH_IDLE,
                                                      10 );
        this.acceptor.bind( this.address );
    }

    public synchronized void stop() {

        this.acceptor.dispose();
    }

    public synchronized IoAcceptor getIoAcceptor() {
        return this.acceptor;
    }

    public int getCurrentSessions() {
        return this.acceptor.getManagedSessionCount();
    }
}
