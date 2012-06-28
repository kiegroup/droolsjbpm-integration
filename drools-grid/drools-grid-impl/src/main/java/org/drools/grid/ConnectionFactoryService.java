package org.drools.grid;


public interface ConnectionFactoryService {

    <T> GridConnection<T> createConnection( GridServiceDescription<T> gsd );

    public boolean isLocalAllowed();

    public void setLocalAllowed( boolean allow );
}
