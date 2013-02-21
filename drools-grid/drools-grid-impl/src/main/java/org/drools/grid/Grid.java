package org.drools.grid;

import org.drools.grid.remote.GridNodeRemoteClient;

public interface Grid {

    public <T> T get(Class<T> serviceClass);

    public GridNode createGridNode( String id );
    
    public GridNode claimGridNode( String id );
    
    public void removeGridNode( String id );

    public GridNode getGridNode( String id );

    public GridNode asRemoteNode( GridNode node );
    
    public String getId();

    public void dispose();

    boolean addGridNode(GridNode node);
}
