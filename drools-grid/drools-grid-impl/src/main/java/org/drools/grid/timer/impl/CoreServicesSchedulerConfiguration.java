package org.drools.grid.timer.impl;

import org.drools.grid.CoreServicesLookup;
import org.drools.grid.Grid;
import org.drools.grid.conf.GridPeerServiceConfiguration;
import org.drools.grid.impl.GridImpl;
import org.drools.grid.service.directory.impl.GridServiceDescriptionImpl;
import org.drools.grid.timer.CoreServicesScheduler;

public class CoreServicesSchedulerConfiguration
    implements
    GridPeerServiceConfiguration {

    public CoreServicesSchedulerConfiguration() {

    }

    public void configureService(Grid grid) {
        CoreServicesLookup wp = grid.get( CoreServicesLookup.class );

        ((GridImpl) grid).addService( CoreServicesScheduler.class,
                                      new CoreServicesSchedulerImpl( new SchedulerImpl( "scheduler:core" ) ) );
        wp.getServices().put( CoreServicesScheduler.class.getName(),
                              new GridServiceDescriptionImpl( CoreServicesScheduler.class ) );
    }
}
