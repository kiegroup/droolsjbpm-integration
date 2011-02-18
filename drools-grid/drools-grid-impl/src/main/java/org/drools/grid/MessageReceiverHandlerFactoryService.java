package org.drools.grid;

import org.drools.grid.io.MessageReceiverHandler;

public interface MessageReceiverHandlerFactoryService {
    MessageReceiverHandler getMessageReceiverHandler();
    
    
    // @TODO This is just a temporary location, while we get things working (mdp)
    void registerSocketService(Grid grid, String id, String ip, int port);
}
