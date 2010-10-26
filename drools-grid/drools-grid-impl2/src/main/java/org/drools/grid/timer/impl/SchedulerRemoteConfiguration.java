/**
 * 
 */
package org.drools.grid.timer.impl;

import org.drools.grid.CoreServicesWhitePages;
import org.drools.grid.service.directory.impl.*;
import org.drools.grid.Grid;
import org.drools.grid.GridPeerServiceConfiguration;
import org.drools.grid.impl.GridImpl;
import org.drools.grid.io.ConversationManager;
import org.drools.grid.timer.Scheduler;

public class SchedulerRemoteConfiguration
    implements
    GridPeerServiceConfiguration {
    private ConversationManager cm;
    

    public SchedulerRemoteConfiguration( ConversationManager cm) {
        
        this.cm = cm;
    }

    public void configureService(Grid grid) {
        CoreServicesWhitePagesImpl coreServices = (CoreServicesWhitePagesImpl) grid.get( CoreServicesWhitePages.class );

        GridServiceDescriptionImpl gsd = (GridServiceDescriptionImpl) coreServices.lookup( Scheduler.class );

        Scheduler scheduler = new SchedulerClient(gsd.getId(), gsd, 
                                              cm );
        ((GridImpl) grid).addService( Scheduler.class,
                                      scheduler );
        
    }
}