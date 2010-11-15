/**
 * 
 */
package org.drools.grid.timer.impl;

import org.drools.grid.CoreServicesLookup;
import org.drools.grid.service.directory.impl.*;
import org.drools.grid.Grid;
import org.drools.grid.conf.GridPeerServiceConfiguration;
import org.drools.grid.impl.GridImpl;
import org.drools.grid.io.ConversationManager;
import org.drools.time.SchedulerService;

public class SchedulerRemoteConfiguration
    implements
    GridPeerServiceConfiguration {

    public SchedulerRemoteConfiguration() {
    }

    public void configureService(Grid grid) {
        CoreServicesLookupImpl coreServices = (CoreServicesLookupImpl) grid.get( CoreServicesLookup.class );

        GridServiceDescriptionImpl gsd = (GridServiceDescriptionImpl) coreServices.lookup( SchedulerService.class );

        SchedulerService scheduler = new SchedulerClient( grid,
                                                          gsd);
        ((GridImpl) grid).addService( SchedulerService.class,
                                      scheduler );

    }
}