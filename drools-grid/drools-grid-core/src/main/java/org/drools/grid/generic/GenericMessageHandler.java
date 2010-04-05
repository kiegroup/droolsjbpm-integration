package org.drools.grid.generic;

public interface GenericMessageHandler {

    public abstract void messageReceived(GenericIoWriter session,
                                         Message msg) throws Exception;

}