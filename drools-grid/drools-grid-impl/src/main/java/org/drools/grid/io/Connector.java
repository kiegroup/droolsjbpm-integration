package org.drools.grid.io;

import java.net.InetSocketAddress;

import org.kie.SystemEventListener;

public interface Connector {
    public boolean isOpen();

    public IoWriter open(InetSocketAddress address,
                         MessageReceiverHandler handler,
                         SystemEventListener systemEventListener);

    public void close();
}
