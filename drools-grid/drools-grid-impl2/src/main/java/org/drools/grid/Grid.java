package org.drools.grid;

import org.drools.grid.service.directory.Address;


public interface Grid {
    public <T> T get(Class<T> serviceClass);

    public GridNode createGridNode(String id);
    
    public GridNodeConnection getGridNodeConnection(GridServiceDescription serviceDescription);        
}
