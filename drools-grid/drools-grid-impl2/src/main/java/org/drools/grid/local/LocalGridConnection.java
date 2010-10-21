package org.drools.grid.local;

import org.drools.grid.GridNode;
import org.drools.grid.GridNodeConnection;
import org.drools.grid.impl.GridNodeImpl;

public class LocalGridConnection implements GridNodeConnection {
    private GridNode gridNode;
    
    public LocalGridConnection(GridNode gridNode) {
        this.gridNode = gridNode;
    }
    
    public LocalGridConnection(String id) {
        gridNode = new GridNodeImpl(id);
    }

    public void connect() {
        // do nothing as it's local
    }

    public void disconnect() {
        // do nothing as it's local
    }

    public GridNode getGridNode() {
        return this.gridNode;
    }

}
