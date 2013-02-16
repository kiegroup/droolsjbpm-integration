package org.drools.grid;

public interface GridNode
    extends
    GridService {

    public <T> T get( String identifier,
                      Class<T> cls );

    public <T> T get( Class<T> serviceClass );

    public void set( String identifier,
                     Object object );

    public boolean isRemote();

    public boolean isLocalProxy();

    public Grid getGrid();

}