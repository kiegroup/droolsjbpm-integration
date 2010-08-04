package org.drools.grid.internal;

import org.drools.grid.internal.GenericIoWriter;
import org.drools.grid.internal.Message;

public interface GenericMessageHandler {

    public abstract void messageReceived(GenericIoWriter session,
                                         Message msg) throws Exception;

}