package org.drools.grid.io.impl;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.drools.SystemEventListener;
import org.drools.grid.internal.commands.SimpleCommand;
import org.drools.grid.io.Conversation;
import org.drools.grid.io.IoWriter;
import org.drools.grid.io.Message;
import org.drools.grid.io.MessageReceiverHandler;

public class MessageIoReceiverImpl
    implements
    MessageReceiverHandler {

    //    protected Map<Integer, RequestResponseListener> responseHandlers;

    private MessageReceiverHandler    handler;

    private final SystemEventListener systemEventListener;

    public MessageIoReceiverImpl(MessageReceiverHandler handler,
                                 SystemEventListener systemEventListener) {
        this.handler = handler;
        //        this.responseHandlers = new ConcurrentHashMap<Integer, RequestResponseListener>();;
        this.systemEventListener = systemEventListener;
    }

    //    /* (non-Javadoc)
    //     * @see org.drools.vsm.mina.ClientGenericMessageReceiver#addResponseHandler(int, org.drools.vsm.MessageResponseHandler)
    //     */
    //    public void addResponseHandler(int id,
    //                                   RequestResponseListener responseHandler) {
    //        this.responseHandlers.put( id,
    //                                   responseHandler );
    //    }

    public void messageReceived(Conversation conversation,
                                Message msg) {
        // TODO Auto-generated method stub

    }

    //    /* (non-Javadoc)
    //     * @see org.drools.vsm.mina.ClientGenericMessageReceiver#messageReceived(org.drools.vsm.mina.MinaIoWriter, org.drools.vsm.Message)
    //     */
    //    public void messageReceived(IoWriter writer,
    //                                Message msg) throws Exception {
    //
    //        this.systemEventListener.debug( "Message receieved : " + msg );
    //
    //        RequestResponseListener responseHandler = this.responseHandlers.remove( msg.getRequestId() );
    //
    //        if ( responseHandler != null ) {
    //            Object payload = msg.getPayload();
    //            if ( payload instanceof SimpleCommand && ((SimpleCommand) msg.getPayload()).getArguments().size() > 0 &&
    //                 ((SimpleCommand) msg.getPayload()).getArguments().get( 0 ) instanceof RuntimeException ) {
    //                payload = ((SimpleCommand) msg.getPayload()).getArguments().get( 0 );
    //            }
    //            if ( (payload != null && payload instanceof RuntimeException) ) {
    //                responseHandler.setError( (RuntimeException) payload );
    //            } else {
    //                responseHandler.receive( msg );
    //            }
    //        } else if ( this.handler != null ) {
    //            this.handler.messageReceived( writer,
    //                                          msg );
    //        } else {
    //            throw new RuntimeException( "Unable to process Message" );
    //        }
    //    }
}
