package org.drools.grid.remote.mina;

import org.apache.mina.core.future.CloseFuture;
import org.apache.mina.core.future.IoFuture;
import org.apache.mina.core.future.IoFutureListener;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.drools.SystemEventListener;
import org.drools.grid.io.Conversation;
import org.drools.grid.io.Message;
import org.drools.grid.io.MessageReceiverHandler;
import org.drools.grid.io.impl.ConversationImpl;
import org.drools.grid.io.impl.MessageIoReceiverImpl;
import org.drools.grid.io.impl.RequestResponseDispatchListener;

public class MinaIoHandler extends IoHandlerAdapter {
    /**
     * Listener used for logging
     */
    private final SystemEventListener       systemEventListener;

    private MessageReceiverHandler          messageHandler;

    private String                          senderId;

    private RequestResponseDispatchListener dispathListener;

    public MinaIoHandler(SystemEventListener systemEventListener) {
        this( systemEventListener,
              null );

    }

    public MinaIoHandler(SystemEventListener systemEventListener,
                         MessageReceiverHandler messageHandler) {
        this.systemEventListener = systemEventListener;
        //        this.messageHandler = new MessageIoReceiverImpl( handler,
        //                                                         systemEventListener );
        this.messageHandler = messageHandler;

    }

    //    public void addResponseHandler(int id,
    //                                   MessageResponseHandler responseHandler) {
    //        this.messageHandler.addResponseHandler( id,
    //                                                responseHandler );
    //    }

    @Override
    public void exceptionCaught(IoSession session,
                                Throwable cause) throws Exception {
        this.systemEventListener.exception( "Uncaught exception on Server",
                                            cause );
    }

    @Override
    public void messageReceived(IoSession session,
                                Object object) throws Exception {
        Message msg = (Message) object;
        Conversation conversation = new ConversationImpl( null, //TODO this should not be null, but we currently have no concept of a ConversationManager on the Acceptor
                                                          msg.getConversationId(),
                                                          this.senderId,
                                                          msg.getSenderId(),
                                                          this.dispathListener,
                                                          msg,
                                                          new MinaIoWriter( session ),
                                                          null );
        this.messageHandler.messageReceived( conversation,
                                             msg );

       
    }

   

    @Override
    public void sessionIdle(IoSession session,
                            IdleStatus status) throws Exception {
        this.systemEventListener.debug( "Server IDLE " + session.getIdleCount( status ) );
    }

}
