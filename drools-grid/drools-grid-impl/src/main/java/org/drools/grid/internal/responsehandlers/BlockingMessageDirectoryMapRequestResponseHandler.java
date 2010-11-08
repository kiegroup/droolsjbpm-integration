/**
 *
 */
package org.drools.grid.internal.responsehandlers;

import java.util.Map;

import org.drools.grid.io.Conversation;
import org.drools.grid.io.IoWriter;
import org.drools.grid.io.Message;
import org.drools.grid.io.MessageReceiverHandler;

public class BlockingMessageDirectoryMapRequestResponseHandler extends AbstractBlockingResponseHandler
    implements
    MessageReceiverHandler {
    private static final int    ATTACHMENT_ID_WAIT_TIME = 100000;
    private static final int    CONTENT_ID_WAIT_TIME    = 50000;
    private Map<String, String> directoryMap;
    private volatile Message    message;

    public synchronized void messageReceived(Conversation conversation,
                                             Message message) {
        this.message = message;

        setDone( true );
    }

    public Message getMessage() {
        boolean done = waitTillDone( CONTENT_ID_WAIT_TIME );

        if ( !done ) {
            throw new RuntimeException( "Timeout : unable to retrieve Object Id" );
        }

        return this.message;
    }

}