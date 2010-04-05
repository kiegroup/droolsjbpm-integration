package org.drools.grid.remote.mina;


import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.drools.SystemEventListener;
import org.drools.grid.generic.ClientGenericMessageReceiver;
import org.drools.grid.generic.GenericMessageHandler;
import org.drools.grid.generic.Message;
import org.drools.grid.generic.MessageResponseHandler;

public class MinaIoHandler extends IoHandlerAdapter {
    /**
     * Listener used for logging
     */
    private final SystemEventListener systemEventListener;

    private ClientGenericMessageReceiver clientMessageReceiver;

    public MinaIoHandler(SystemEventListener systemEventListener) {
        this( systemEventListener,
              null );

    }

    public MinaIoHandler(SystemEventListener systemEventListener,
                         GenericMessageHandler handler) {
        this.systemEventListener = systemEventListener;
        this.clientMessageReceiver = new ClientGenericMessageReceiverImpl( handler,
                                                                           systemEventListener );

    }

    public void addResponseHandler(int id,
                                   MessageResponseHandler responseHandler) {
        this.clientMessageReceiver.addResponseHandler( id,
                                                       responseHandler );
    }

    public void exceptionCaught(IoSession session,
                                Throwable cause) throws Exception {
        systemEventListener.exception( "Uncaught exception on Server",
                                       cause );
    }

    public void messageReceived(IoSession session,
                                Object object) throws Exception {
        Message msg = (Message) object;
        clientMessageReceiver.messageReceived( new MinaIoWriter( session ),
                                               msg );
    }

    @Override
    public void sessionIdle(IoSession session,
                            IdleStatus status) throws Exception {
        this.systemEventListener.debug( "Server IDLE " + session.getIdleCount( status ) );
    }

}
