package org.drools.grid;

import java.util.Set;

public interface SocketService {

    void addService( String id,
                     int port,
                     Object object );

    void removeService( int port,
                        String id );

    String getIp();

    Set<Integer> getPorts();

    void close();
}
