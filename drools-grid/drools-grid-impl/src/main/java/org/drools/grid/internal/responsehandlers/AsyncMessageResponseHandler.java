package org.drools.grid.internal.responsehandlers;

import org.drools.grid.io.Conversation;
import org.drools.grid.io.Message;

public class AsyncMessageResponseHandler extends AbstractBaseResponseHandler {
    

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
