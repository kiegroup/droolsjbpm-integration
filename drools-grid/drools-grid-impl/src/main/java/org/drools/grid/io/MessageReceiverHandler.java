package org.drools.grid.io;

import org.drools.grid.Grid;

public interface MessageReceiverHandler {

    public void messageReceived( Conversation conversation,
                                 Message msg );

}
