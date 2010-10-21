/**
 * 
 */
package org.drools.grid.io.impl;

import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicLong;

import org.drools.SystemEventListener;
import org.drools.grid.io.Connector;
import org.drools.grid.io.Conversation;
import org.drools.grid.io.ConversationManager;
import org.drools.grid.io.IoWriter;
import org.drools.grid.io.MessageReceiverHandler;

public class ConversationManagerImpl
    implements
    ConversationManager {

    private Connector              conn;
   
    private SystemEventListener    systemEventListener;

    private String                 senderId;

    private AtomicLong             conversationIdCounter;

    public ConversationManagerImpl(String senderId,
                                   Connector conn,
                                   SystemEventListener    systemEventListener) {
        this.conn = conn;
        this.senderId = senderId;
        this.conversationIdCounter = new AtomicLong();
        this.systemEventListener = systemEventListener;
    }

    public Conversation startConversation(InetSocketAddress address,
                                          String recipientId) {
        RequestResponseDispatchListener dispathListener = new RequestResponseDispatchListener();  
        IoWriter writer = this.conn.open( address,
                                          dispathListener,
                                          systemEventListener );
        return new ConversationImpl( Long.toString( this.conversationIdCounter.incrementAndGet() ),
                                     this.senderId,
                                     recipientId,
                                     dispathListener,
                                     writer,
                                     this );
    }
    
    public void endConversation() {
        this.conn.close();
    }

}