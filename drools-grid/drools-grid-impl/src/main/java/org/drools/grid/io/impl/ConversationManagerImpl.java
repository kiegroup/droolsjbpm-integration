package org.drools.grid.io.impl;

import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicLong;

import org.drools.grid.Grid;
import org.drools.grid.io.Connector;
import org.drools.grid.io.ConnectorFactoryService;
import org.drools.grid.io.Conversation;
import org.drools.grid.io.ConversationManager;
import org.drools.grid.io.IoWriter;
import org.kie.SystemEventListener;

public class ConversationManagerImpl
    implements
    ConversationManager {
    private SystemEventListener systemEventListener;

    private AtomicLong          conversationIdCounter;

    private Grid grid;
    
    public ConversationManagerImpl(Grid grid,
                                   SystemEventListener systemEventListener) {
        this.conversationIdCounter = new AtomicLong();
        this.systemEventListener = systemEventListener;
        this.grid = grid;
    }

    public Conversation startConversation(String senderId,
                                          InetSocketAddress address,
                                          String recipientId) {
        RequestResponseDispatchListener dispathListener = new RequestResponseDispatchListener();
        ConnectorFactoryService cfs = this.grid.get( ConnectorFactoryService.class );
        if ( cfs == null ) {
            throw new RuntimeException( "Unable to resolve ConnectorFactoryService" );
        }
        
        Connector conn = cfs.newConnector();
        IoWriter writer = conn.open( address,
                                     dispathListener,
                                     systemEventListener );
        return new ConversationImpl( conn,
                                     Long.toString( this.conversationIdCounter.incrementAndGet() ),
                                     senderId,
                                     recipientId,
                                     dispathListener,
                                     writer,
                                     this );
    }

}
