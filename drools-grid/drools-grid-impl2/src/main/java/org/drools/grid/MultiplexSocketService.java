package org.drools.grid;

import org.drools.grid.io.MessageReceiverHandler;

public interface MultiplexSocketService {

    void addService(int port,
                    String id,
                    MessageReceiverHandler receiver);

    void removeService(int port,
                       String id);

    String getIp();
    
    void close();
}