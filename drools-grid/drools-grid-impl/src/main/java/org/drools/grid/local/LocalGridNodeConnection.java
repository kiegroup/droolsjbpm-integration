package org.drools.grid.local;

import org.drools.grid.GridConnection;
import org.drools.grid.GridNode;
import org.drools.grid.impl.GridNodeImpl;

public class LocalGridNodeConnection<T>
    implements
    GridConnection<GridNode> {
    private GridNode gridNode;

    public LocalGridNodeConnection(GridNode gridNode) {
        this.gridNode = gridNode;
    }

    public LocalGridNodeConnection(String id) {
        gridNode = new GridNodeImpl( id );
    }

    public GridNode connect() {
        return gridNode;
    }

    public void disconnect() {
    }


}
