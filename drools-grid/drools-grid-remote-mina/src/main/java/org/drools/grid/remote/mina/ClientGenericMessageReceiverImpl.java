/**
 * 
 */
package org.drools.grid.remote.mina;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.drools.SystemEventListener;
import org.drools.grid.internal.ClientGenericMessageReceiver;
import org.drools.grid.internal.GenericIoWriter;
import org.drools.grid.internal.GenericMessageHandler;
import org.drools.grid.internal.Message;
import org.drools.grid.internal.MessageResponseHandler;
import org.drools.grid.internal.commands.SimpleCommand;

public class ClientGenericMessageReceiverImpl
    implements
    ClientGenericMessageReceiver {
    protected Map<Integer, MessageResponseHandler> responseHandlers;

    private GenericMessageHandler                  handler;

    private final SystemEventListener              systemEventListener;

    public ClientGenericMessageReceiverImpl(GenericMessageHandler handler,
                                            SystemEventListener systemEventListener) {
        this.handler = handler;
        this.responseHandlers = new ConcurrentHashMap<Integer, MessageResponseHandler>();;
        this.systemEventListener = systemEventListener;
    }

    /* (non-Javadoc)
     * @see org.drools.vsm.mina.ClientGenericMessageReceiver#addResponseHandler(int, org.drools.vsm.MessageResponseHandler)
     */
    public void addResponseHandler(int id,
                                   MessageResponseHandler responseHandler) {
        this.responseHandlers.put( id,
                                   responseHandler );
    }

    /* (non-Javadoc)
     * @see org.drools.vsm.mina.ClientGenericMessageReceiver#messageReceived(org.drools.vsm.mina.MinaIoWriter, org.drools.vsm.Message)
     */
    public void messageReceived(GenericIoWriter writer,
                                Message msg) throws Exception {

        this.systemEventListener.debug( "Message receieved : " + msg );

        MessageResponseHandler responseHandler = this.responseHandlers.remove( msg.getResponseId() );

        if ( responseHandler != null ) {
            Object payload = msg.getPayload();
            if ( payload instanceof SimpleCommand && ((SimpleCommand) msg.getPayload()).getArguments().size() > 0 &&
                 ((SimpleCommand) msg.getPayload()).getArguments().get( 0 ) instanceof RuntimeException ) {
                payload = ((SimpleCommand) msg.getPayload()).getArguments().get( 0 );
            }
            if ( (payload != null && payload instanceof RuntimeException) ) {
                responseHandler.setError( (RuntimeException) payload );
            } else {
                responseHandler.receive( msg );
            }
        } else if ( this.handler != null ) {
            this.handler.messageReceived( writer,
                                          msg );
        } else {
            throw new RuntimeException( "Unable to process Message" );
        }
    }
}