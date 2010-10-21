/**
 * 
 */
package org.drools.grid.timer.impl;


import java.util.Map;
import org.drools.grid.Grid;
import org.drools.grid.GridPeerServiceConfiguration;
import org.drools.grid.GridServiceDescription;
import org.drools.grid.impl.GridImpl;
import org.drools.grid.timer.CoreServicesScheduler;

public class CoreServicesSchedulerConfiguration
    implements
    GridPeerServiceConfiguration {
    private Map<String, GridServiceDescription> services;

    public CoreServicesSchedulerConfiguration(Map<String, GridServiceDescription> services) {
        this.services = services;
    }

    public void configureService(Grid grid) {
        ((GridImpl) grid).addService( CoreServicesScheduler.class,
                                      new CoreServicesSchedulerImpl( new SchedulerImpl(grid) ) );
    }
}