package org.drools.grid;

import org.drools.grid.service.directory.Address;

public interface ConnectionFactoryService {
    GridNodeConnection createConnection(Address address);
}
