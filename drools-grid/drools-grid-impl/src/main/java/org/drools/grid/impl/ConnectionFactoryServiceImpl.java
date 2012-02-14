package org.drools.grid.impl;

import org.drools.grid.ConnectionFactoryService;
import org.drools.grid.Grid;
import org.drools.grid.GridConnection;
import org.drools.grid.GridNode;
import org.drools.grid.GridServiceDescription;
import org.drools.grid.local.LocalGridNodeConnection;
import org.drools.grid.remote.RemoteGridNodeConnection;
import org.drools.grid.remote.mina.MinaConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConnectionFactoryServiceImpl
        implements
        ConnectionFactoryService {

    Grid grid;
    boolean localAllowed;
    private static Logger logger = LoggerFactory.getLogger(ConnectionFactoryServiceImpl.class);

    public ConnectionFactoryServiceImpl(Grid grid) {
        this.grid = grid;
        this.localAllowed = true;
    }

    public <T> GridConnection<T> createConnection(GridServiceDescription<T> gsd) {
        GridConnection<T> conn = null;
        if (logger.isTraceEnabled()) {
            logger.trace(" ### CONNECTION FACTORY: Creating a connection for: " + gsd.getId());
        }
        if (this.localAllowed) {
            // internal boolean to disallow local connections
            GridNode gnode = this.grid.getGridNode(gsd.getId());
            if (gnode != null) {
                if (logger.isTraceEnabled()) {
                    logger.trace(" ### CONNECTION FACTORY: Creating Local GridNodeConnection: " + gsd);
                }
                conn = new LocalGridNodeConnection(gnode);
            }
        }

        if (conn == null) {
            if (logger.isTraceEnabled()) {
                logger.trace(" ### CONNECTION FACTORY: Creating Remote GridNodeConnection: " + gsd);
            }
            conn = new RemoteGridNodeConnection(this.grid,
                    gsd);
        }
        if (logger.isTraceEnabled()) {
            logger.trace(" ### CONNECTION FACTORY: Connection created: " + conn);
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
