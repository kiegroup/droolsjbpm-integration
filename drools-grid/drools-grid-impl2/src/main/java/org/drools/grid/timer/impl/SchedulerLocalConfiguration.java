/**
 * 
 */
package org.drools.grid.timer.impl;

import org.drools.grid.Grid;
import org.drools.grid.GridPeerServiceConfiguration;
import org.drools.grid.impl.GridImpl;
import org.drools.grid.timer.Scheduler;

public class SchedulerLocalConfiguration
    implements
    GridPeerServiceConfiguration {

    private Scheduler scheduler;

    public SchedulerLocalConfiguration() {

    }

    public void setScheduler(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    public void configureService(Grid grid) {
        Scheduler sched = (this.scheduler != null) ? this.scheduler : new SchedulerImpl(grid);
        ((GridImpl) grid).addService( Scheduler.class,
                                      sched );

    }

}