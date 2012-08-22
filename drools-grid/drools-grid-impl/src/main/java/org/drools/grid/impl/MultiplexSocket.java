package org.drools.grid.impl;

import java.util.Map;

import org.apache.mina.util.CopyOnWriteMap;
import org.drools.grid.io.Conversation;
import org.drools.grid.io.Message;
import org.drools.grid.io.MessageReceiverHandler;
import org.drools.grid.io.impl.ExceptionMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MultiplexSocket
    implements
    MessageReceiverHandler {
    private static Logger logger = LoggerFactory.getLogger( MultiplexSocket.class );
    
    private Map<String, MessageReceiverHandler> handlers;

    public MultiplexSocket() {
        this.handlers = new CopyOnWriteMap<String, MessageReceiverHandler>();
    }

    public Map<String, MessageReceiverHandler> getHandlers() {
        return this.handlers;
    }

    public void messageReceived( Conversation conversation,
                                 Message msg ) {
        if( logger.isTraceEnabled() ) {
            logger.trace( " --- MSG Received: " + msg );
            logger.trace( " \t --- Available Handlers: " + handlers.keySet() );
        }
        if ( this.handlers.containsKey( msg.getRecipientId() ) ) {
            this.handlers.get( msg.getRecipientId() ).messageReceived( conversation, msg );
        } else {
            logger.error( " \t --- No handler available for message recipient: " + msg.getRecipientId() );
            conversation.respond( null );
        }

    }

    public void exceptionReceived(Conversation conversation, ExceptionMessage msg) {
        throw new AssertionError();
    }

}
