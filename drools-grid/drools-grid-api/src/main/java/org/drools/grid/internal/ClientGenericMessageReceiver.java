package org.drools.grid.internal;

import org.drools.grid.internal.GenericIoWriter;
import org.drools.grid.internal.GenericIoWriter;
import org.drools.grid.internal.MessageResponseHandler;
import org.drools.grid.internal.Message;
import org.drools.grid.internal.Message;
import org.drools.grid.internal.MessageResponseHandler;

public interface ClientGenericMessageReceiver {

    public abstract void addResponseHandler(int id,
                                            MessageResponseHandler responseHandler);

    public abstract void messageReceived(GenericIoWriter writer,
                                         Message msg) throws Exception;

}