/**
 * 
 */
package org.drools.grid.io;

import java.net.InetSocketAddress;


public interface ConversationManager {
    Conversation startConversation(InetSocketAddress address,
                                   String recipientId);
    
    void endConversation();

}