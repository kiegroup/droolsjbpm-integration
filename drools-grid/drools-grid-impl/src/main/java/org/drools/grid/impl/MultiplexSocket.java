package org.drools.grid.impl;

import java.util.Map;

import org.apache.mina.util.CopyOnWriteMap;
import org.drools.grid.io.Conversation;
import org.drools.grid.io.Message;
import org.drools.grid.io.MessageReceiverHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MultiplexSocket
    implements
    MessageReceiverHandler {
    private static Logger logger = LoggerFactory.getLogger(MultiplexSocket.class);
    
    private Map<String, MessageReceiverHandler> handlers;

    public MultiplexSocket() {
        this.handlers = new CopyOnWriteMap<String, MessageReceiverHandler>();
    }

    public Map<String, MessageReceiverHandler> getHandlers() {
        return this.handlers;
    }

    public void messageReceived(Conversation conversation,
                                Message msg) {
        if(logger.isTraceEnabled()){
            logger.trace(" --- MSG Recieved: "+msg);
            logger.trace(" \t --- Available Handlers: "+handlers.keySet());
        }
        this.handlers.get( msg.getRecipientId() ).messageReceived( conversation,
                                                                   msg );
    }

}
