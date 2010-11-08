/**
 *
 */
package org.drools.grid.internal.responsehandlers;

import org.drools.grid.io.Conversation;
import org.drools.grid.io.IoWriter;
import org.drools.grid.io.Message;
import org.drools.grid.io.MessageReceiverHandler;

public class BlockingMessageResponseHandler extends AbstractBlockingResponseHandler
    implements
    MessageReceiverHandler {
    private static final int WAIT_TIME = 60000;

    private volatile Message message;

    public void messageReceived(Conversation conversation,
                                Message message) {
        this.message = message;
        setDone( true );
    }

    public Message getMessage() {
        return getMessage( WAIT_TIME );
    }

    public Message getMessage(long waitTime) {
        boolean done = waitTillDone( waitTime );

        if ( !done ) {
            throw new RuntimeException( "Timeout : unable to retrieve Object Id" );
        }

        return this.message;
    }

}