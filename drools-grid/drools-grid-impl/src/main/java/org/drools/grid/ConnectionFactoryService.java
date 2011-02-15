package org.drools.grid;


public interface ConnectionFactoryService {
    <T> GridConnection<T> createConnection(GridServiceDescription<T> gsd);
}
