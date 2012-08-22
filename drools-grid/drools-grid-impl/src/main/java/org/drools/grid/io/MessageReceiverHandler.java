package org.drools.grid.io;

import org.drools.grid.io.impl.ExceptionMessage;

public interface MessageReceiverHandler {

    public void messageReceived( Conversation conversation,
                                 Message msg );

    public void exceptionReceived( Conversation conversation,
                                 ExceptionMessage msg );
}
