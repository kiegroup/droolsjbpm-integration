/**
 * 
 */
package org.drools.grid.remote.mina;

import java.util.concurrent.atomic.AtomicInteger;

import org.apache.mina.core.session.IoSession;
import org.drools.grid.ConnectorException;
import org.drools.grid.internal.responsehandlers.BlockingMessageResponseHandler;
import org.drools.grid.io.IoWriter;
import org.drools.grid.io.Message;
import org.drools.grid.io.MessageReceiverHandler;
import org.drools.grid.io.impl.MessageImpl;

public class MinaIoWriter
    implements
    IoWriter {
    //    protected final String         id;
    //    protected final MessageHandler handler;
    private IoSession session;

    //    private RequestResponseDispatchListener reqResDisListener;

    //    protected String                 conversationId;
    //    protected final AtomicInteger  requestIdCounter;

    public MinaIoWriter(
                        //                        String id,
                        //                        String conversationId,
                        IoSession session
    //                        MessageHandler handler
    ) {
        //        this.id = id;
        //        this.conversationId = conversationId;
        this.session = session;
        //        this.reqResDisListener = new RequestResponseDispatchListener(this);
        //        this.handler = handler;
        //        this.requestIdCounter = new AtomicInteger();
    }

    //    public String getId() {
    //        return this.id;
    //    }

    public IoSession getIoSession() {
        return this.session;
    }

    public void dispose() {
        this.session = null;
    }

    //    private void addResponseHandler(int id,
    //                                    MessageReceiverHandler responseHandler) {
    //        this.handler.addResponseHandler( id,
    //                                         responseHandler );
    //    }

    public void write(Message msg) {
        if ( this.session == null || !this.session.isConnected() ) {
            throw new IllegalStateException( "Cannot write message and socket is not open" );
        }

        //        int requestId = msg.getRequestId();
        //        if ( requestId != -1 ) {
        //            this.reqResDisListener.addMessageReceiverHandler( requestId, responseHandler );
        //        }
        //        int requestId = -1;
        //        if ( responseHandler != null ) {
        //            requestId = this.requestIdCounter.getAndIncrement();
        //            addResponseHandler( requestId,
        //                                responseHandler );            
        //        }
        //        Message msg = new MessageImpl( this.conversationId,
        //                                       this.id,
        //                                       
        //                                       requestId,
        //                                       null,
        //                                       body );

        this.session.write( msg );
    }

}