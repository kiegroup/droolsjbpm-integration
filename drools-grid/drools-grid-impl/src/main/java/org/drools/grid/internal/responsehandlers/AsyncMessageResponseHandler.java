package org.drools.grid.internal.responsehandlers;

import org.drools.grid.io.Conversation;
import org.drools.grid.io.IoWriter;
import org.drools.grid.io.Message;
import org.drools.grid.io.MessageReceiverHandler;

public class AsyncMessageResponseHandler extends AbstractBlockingResponseHandler
    implements
    MessageReceiverHandler {
    

    private volatile Message message;

    public void messageReceived(Conversation conversation,
                                Message message) {
        this.message = message;
        setDone( true );
    }

    public Message getMessage() {
        return this.message;
    }

}
