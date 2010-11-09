package org.drools.grid.impl;

import org.drools.grid.ConnectionFactoryService;
import org.drools.grid.Grid;
import org.drools.grid.GridConnection;
import org.drools.grid.GridNode;
import org.drools.grid.GridServiceDescription;
import org.drools.grid.local.LocalGridNodeConnection;
import org.drools.grid.remote.RemoteGridNodeConnection;

public class ConnectionFactoryServiceImpl
    implements
    ConnectionFactoryService {

    Grid    grid;

    boolean localAllowed;

    public ConnectionFactoryServiceImpl(Grid grid) {
        this.grid = grid;
        this.localAllowed = true;
    }

    public <T> GridConnection<T> createConnection(GridServiceDescription<T> gsd) {
        GridConnection<T> conn = null;

        if ( this.localAllowed ) {
            // internal boolean to disallow local connections
            GridNode gnode = this.grid.getGridNode( gsd.getId() );
            if ( gnode != null ) {
                conn = new LocalGridNodeConnection( gnode );
            }
        }

        if ( conn == null ) {
            conn = new RemoteGridNodeConnection( gsd );
        }

        return conn;
    }

    public boolean isLocalAllowed() {
        return localAllowed;
    }

    public void setLocalAllowed(boolean localAllowed) {
        this.localAllowed = localAllowed;
    }

}
