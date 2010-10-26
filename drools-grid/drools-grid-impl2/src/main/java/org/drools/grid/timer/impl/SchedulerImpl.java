/*
 * Copyright 2010 salaboy.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * under the License.
 */

package org.drools.grid.timer.impl;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.drools.grid.ServiceFactory;
import org.drools.grid.Grid;
import org.drools.grid.GridServiceDescription;
import org.drools.grid.MessageReceiverHandlerFactoryService;
import org.drools.grid.io.MessageReceiverHandler;
import org.drools.grid.service.directory.WhitePages;
import org.drools.grid.timer.Scheduler;
import org.drools.time.TimerService;
import org.drools.time.impl.JDKTimerService;

/**
 *
 * @author salaboy
 */
public class SchedulerImpl implements Scheduler, MessageReceiverHandlerFactoryService{
    private TimerService timer =  new JDKTimerService();
    private String id;
    private Grid grid; 
    
    public SchedulerImpl(String id, Grid grid){
        this.id = id;
        this.grid = grid;
    }
    
    public void scheduleJob(ScheduledJob job) {
        if(job.getConfiguration().getRedundancy() == 1){
            timer.scheduleJob(job.getJob(), job.getJobContext(), job.getTrigger());
        
        }else if(job.getConfiguration().getRedundancy() > 1){
           WhitePages wp = grid.get(WhitePages.class);
           GridServiceDescription schedservice = wp.lookup(Scheduler.class.getName()); 
           
           Scheduler sched = ServiceFactory.newServiceInstance(Scheduler.class, schedservice);
           sched.scheduleJob(job);
           
           
        }
        
    }

    public void removeJob(String jobId) {
        
    }

    public MessageReceiverHandler getMessageReceiverHandler() {
        return new SchedulerServer(this);
    }

    public String getId() {
        return this.id;
    }

    public Grid getGrid() {
        return grid;
    }
    
    
   
    
    
    
    
}
