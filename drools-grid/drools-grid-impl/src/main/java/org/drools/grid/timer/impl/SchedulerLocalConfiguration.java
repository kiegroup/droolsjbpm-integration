/**
 * 
 */
package org.drools.grid.timer.impl;

import org.drools.grid.Grid;
import org.drools.grid.conf.GridPeerServiceConfiguration;
import org.drools.grid.impl.GridImpl;
import org.drools.grid.service.directory.WhitePages;
import org.drools.time.SchedulerService;

public class SchedulerLocalConfiguration
    implements
    GridPeerServiceConfiguration {

    private SchedulerService scheduler;
    private String           id;

    public SchedulerLocalConfiguration(String id) {
        this.id = id;
    }

    public void setScheduler(SchedulerService scheduler) {
        this.scheduler = scheduler;
    }

    public void configureService(Grid grid) {
        WhitePages wp = grid.get( WhitePages.class );
        
        ((GridImpl) grid).addService( SchedulerService.class,
                                      getSchedulerService() );
        
        wp.create( "scheduler:" + this.id + SchedulerService.class.getName() );

    }
    
    public SchedulerService getSchedulerService() {
        if ( this.scheduler == null ) {
            this.scheduler = new SchedulerImpl( this.id ); 
        }
        
        return this.scheduler;
    }

}