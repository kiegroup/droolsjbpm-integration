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

import java.io.IOException;
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

    public synchronized void start() throws IOException {
        acceptor.getFilterChain().addLast( "logger",
                                           new LoggingFilter() );
        acceptor.getFilterChain().addLast( "codec",
                                           new ProtocolCodecFilter( new ObjectSerializationCodecFactory() ) );
        acceptor.getSessionConfig().setReadBufferSize( 2048 );
        acceptor.getSessionConfig().setIdleTime( IdleStatus.BOTH_IDLE,
                                                 10 );
        acceptor.bind( address );
    }

    public synchronized void stop() {
        acceptor.dispose();
    }

    public synchronized IoAcceptor getIoAcceptor() {
        return acceptor;
    }

}