package org.drools.grid.io;

import java.net.InetSocketAddress;

public interface ConversationManager {
    Conversation startConversation(String senderId,
                                   InetSocketAddress address,
                                   String recipientId);
}
