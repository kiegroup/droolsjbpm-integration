package org.drools.grid.local;

import org.drools.grid.Grid;
import org.drools.grid.GridConnection;
import org.drools.grid.GridNode;
import org.drools.grid.GridNodeConnection;
import org.drools.grid.impl.GridNodeImpl;

/**
 * Testing only
 * @param <T>
 */
public class LocalGridNodeConnection<T>
    implements
    GridConnection<GridNode> {
    private GridNode gridNode;

    public LocalGridNodeConnection(GridNode gridNode) {
        this.gridNode = gridNode;
    }

    public LocalGridNodeConnection( String id, Grid grid ) {
        gridNode = new GridNodeImpl( id, grid );
    }

    public GridNode connect() {
        return gridNode;
    }

    public void disconnect() {
    }


}
