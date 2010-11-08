/**
 * 
 */
package org.drools.grid.timer.impl;

import org.drools.grid.Grid;
import org.drools.grid.GridPeerServiceConfiguration;
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
        SchedulerService sched = (this.scheduler != null) ? this.scheduler : new SchedulerImpl( this.id,
                                                                                                grid );
        ((GridImpl) grid).addService( SchedulerService.class,
                                      sched );
        wp.create( "scheduler:" + this.id + SchedulerService.class.getName() );

    }

}