package org.drools.grid.generic;

import org.drools.grid.remote.mina.MinaIoWriter;

public interface ClientGenericMessageReceiver {

    public abstract void addResponseHandler(int id,
                                            MessageResponseHandler responseHandler);

    public abstract void messageReceived(GenericIoWriter writer,
                                         Message msg) throws Exception;

}