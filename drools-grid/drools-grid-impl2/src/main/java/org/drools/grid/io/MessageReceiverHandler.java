package org.drools.grid.io;



public interface MessageReceiverHandler {

    public void messageReceived(Conversation conversation,
                                Message msg);    

}