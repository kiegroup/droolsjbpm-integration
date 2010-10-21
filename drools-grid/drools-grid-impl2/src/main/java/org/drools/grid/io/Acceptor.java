package org.drools.grid.io;

import java.net.InetSocketAddress;

import org.drools.SystemEventListener;

public interface Acceptor {
    boolean isOpen();
    void open(InetSocketAddress address, MessageReceiverHandler handler, SystemEventListener systemEventListener);
    void close();
    MessageReceiverHandler getMessageReceiverHandler();
}
