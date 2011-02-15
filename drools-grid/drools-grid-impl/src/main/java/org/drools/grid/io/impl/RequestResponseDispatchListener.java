package org.drools.grid.io.impl;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.drools.grid.io.Conversation;
import org.drools.grid.io.IoWriter;
import org.drools.grid.io.Message;
import org.drools.grid.io.MessageReceiverHandler;

public class RequestResponseDispatchListener
    implements
    MessageReceiverHandler {
    // Need time based eviction queue, to remove old unreturned requests
    private Map<Integer, MessageReceiverHandler> msgRecHandlers;

    public RequestResponseDispatchListener() {
        this.msgRecHandlers = new ConcurrentHashMap<Integer, MessageReceiverHandler>();
    }

    public void addMessageReceiverHandler(int requestId,
                                          MessageReceiverHandler msgRecHandler) {
        this.msgRecHandlers.put( requestId,
                                 msgRecHandler );
    }

    public void messageReceived(Conversation conversation,
                                Message msg) {
        MessageReceiverHandler msgRecHandler = this.msgRecHandlers.remove( msg.getResponseId() );
        if ( msgRecHandler != null ) {
            msgRecHandler.messageReceived( conversation,
                                           msg );
        }
    }

}
