/**
 * 
 */
package org.drools.grid.io.impl;

import java.util.concurrent.atomic.AtomicInteger;

import org.drools.grid.io.Conversation;
import org.drools.grid.io.ConversationManager;
import org.drools.grid.io.IoWriter;
import org.drools.grid.io.Message;
import org.drools.grid.io.MessageReceiverHandler;

public class ConversationImpl
    implements
    Conversation {

    private IoWriter                        writer;
    private String                          conversationId;
    private String                          senderId;
    private String                        recipientId;
    private AtomicInteger                   requestId;
    private RequestResponseDispatchListener dispathListener;
    private ConversationManager             conversationManager;

    private Message                         receivedMessage;

    public ConversationImpl(String conversationId,
                            String senderId,
                            String recipientId,
                            RequestResponseDispatchListener dispathListener,
                            IoWriter writer,
                            ConversationManager conversationManager) {
        this( conversationId,
              senderId,
              recipientId,
              dispathListener,
              null,
              writer,
              conversationManager );
    }

    public ConversationImpl(String conversationId,
                            String senderId,
                            String recipientId,
                            RequestResponseDispatchListener dispathListener,
                            Message receivedMessage,
                            IoWriter writer,
                            ConversationManager conversationManager) {

        this.conversationId = conversationId;
        this.senderId = senderId;
        this.recipientId = recipientId;
        this.dispathListener = dispathListener;
        this.writer = writer;
        this.requestId = new AtomicInteger();
        this.receivedMessage = receivedMessage;
        this.conversationManager = conversationManager;
    }

    public void respond(Object body) {
        Message msg = new MessageImpl( this.conversationId,
                                       this.senderId,
                                       recipientId,
                                       -1,
                                       receivedMessage.getRequestId(),
                                       body );

        writer.write( msg );
    }

    public void sendMessage(Object body,
                                MessageReceiverHandler handler) {
        int requestId = -1;
        if ( handler != null ) {
            requestId = this.requestId.getAndIncrement();
        }
        Message msg = new MessageImpl( this.conversationId,
                                       this.senderId,
                                       recipientId,
                                       requestId,
                                       -1,
                                       body );

        this.dispathListener.addMessageReceiverHandler( requestId,
                                                        handler );
        writer.write( msg );
    }

}