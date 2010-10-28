/**
 * 
 */
package org.drools.grid.timer.impl;


import org.drools.grid.CoreServicesWhitePages;
import org.drools.grid.Grid;
import org.drools.grid.GridPeerServiceConfiguration;
import org.drools.grid.impl.GridImpl;
import org.drools.grid.service.directory.impl.GridServiceDescriptionImpl;
import org.drools.grid.timer.CoreServicesScheduler;

public class CoreServicesSchedulerConfiguration
    implements
    GridPeerServiceConfiguration {

    public CoreServicesSchedulerConfiguration() { 
        
    }

    public void configureService(Grid grid) {
        CoreServicesWhitePages wp = grid.get(CoreServicesWhitePages.class);
        
        ((GridImpl) grid).addService( CoreServicesScheduler.class,
                                      new CoreServicesSchedulerImpl( new SchedulerImpl("scheduler:core",grid) ) );
        wp.getServices().put(CoreServicesScheduler.class.getName(), new GridServiceDescriptionImpl(CoreServicesScheduler.class));
    }
}