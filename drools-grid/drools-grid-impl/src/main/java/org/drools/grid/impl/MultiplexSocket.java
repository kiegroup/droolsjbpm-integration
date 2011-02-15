package org.drools.grid.impl;

import java.util.Map;

import org.apache.mina.util.CopyOnWriteMap;
import org.drools.grid.io.Conversation;
import org.drools.grid.io.Message;
import org.drools.grid.io.MessageReceiverHandler;

public class MultiplexSocket
    implements
    MessageReceiverHandler {
    private Map<String, MessageReceiverHandler> handlers;

    public MultiplexSocket() {
        this.handlers = new CopyOnWriteMap<String, MessageReceiverHandler>();
    }

    public Map<String, MessageReceiverHandler> getHandlers() {
        return this.handlers;
    }

    public void messageReceived(Conversation conversation,
                                Message msg) {
        this.handlers.get( msg.getRecipientId() ).messageReceived( conversation,
                                                                   msg );
    }

}
