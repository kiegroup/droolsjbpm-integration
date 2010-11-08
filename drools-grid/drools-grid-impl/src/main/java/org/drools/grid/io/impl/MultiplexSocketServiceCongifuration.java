/**
 * 
 */
package org.drools.grid.io.impl;

import org.drools.grid.Grid;
import org.drools.grid.GridPeerServiceConfiguration;
import org.drools.grid.MultiplexSocketService;
import org.drools.grid.impl.GridImpl;

public class MultiplexSocketServiceCongifuration
    implements
    GridPeerServiceConfiguration {
    private MultiplexSocketService service;

    public MultiplexSocketServiceCongifuration(MultiplexSocketService service) {
        this.service = service;
    }

    public void configureService(Grid grid) {
        ((GridImpl) grid).addService( MultiplexSocketService.class,
                                      service );
    }

}