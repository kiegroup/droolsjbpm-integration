package org.drools.grid.remote.mina;

import org.drools.grid.io.Connector;
import org.drools.grid.io.ConnectorFactoryService;

public class MinaConnectorFactoryService
    implements
    ConnectorFactoryService {

    public Connector newConnector() {
        return new MinaConnector();
    }

}
