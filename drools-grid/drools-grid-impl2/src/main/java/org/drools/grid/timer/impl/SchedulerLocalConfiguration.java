/**
 * 
 */
package org.drools.grid.timer.impl;

import org.drools.grid.Grid;
import org.drools.grid.GridPeerServiceConfiguration;
import org.drools.grid.impl.GridImpl;
import org.drools.grid.service.directory.WhitePages;
import org.drools.grid.timer.Scheduler;

public class SchedulerLocalConfiguration
    implements
    GridPeerServiceConfiguration {

    private Scheduler scheduler;
    private String id;

    public SchedulerLocalConfiguration(String id) {
        this.id =  id;
    }

    public void setScheduler(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    public void configureService(Grid grid) {
        WhitePages wp = grid.get(WhitePages.class);
        Scheduler sched = (this.scheduler != null) ? this.scheduler : new SchedulerImpl(this.id, grid);
        ((GridImpl) grid).addService( Scheduler.class,
                                      sched );
        wp.create("scheduler:"+sched.getId()+"@local/local");

    }

}