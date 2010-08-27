package org.drools.grid.internal;


public interface GenericMessageHandler {

    public abstract void messageReceived(GenericIoWriter session,
                                         Message msg) throws Exception;

}