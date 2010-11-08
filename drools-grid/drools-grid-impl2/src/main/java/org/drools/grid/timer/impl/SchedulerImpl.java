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

import org.drools.grid.Grid;
import org.drools.grid.MessageReceiverHandlerFactoryService;
import org.drools.grid.io.MessageReceiverHandler;
import org.drools.time.Job;
import org.drools.time.JobContext;
import org.drools.time.JobHandle;
import org.drools.time.SchedulerService;

import org.drools.time.TimerService;
import org.drools.time.Trigger;
import org.drools.time.impl.JDKTimerService;

/**
 *
 * @author salaboy
 */
public class SchedulerImpl
    implements
    SchedulerService,
    MessageReceiverHandlerFactoryService {
    private TimerService timer = new JDKTimerService();
    private String       id;
    private Grid         grid;

    public SchedulerImpl(String id,
                         Grid grid) {
        this.id = id;
        this.grid = grid;
    }

    public MessageReceiverHandler getMessageReceiverHandler() {
        return new SchedulerServer( this );
    }

    public String getId() {
        return this.id;
    }

    public Grid getGrid() {
        return grid;
    }

    public JobHandle scheduleJob(Job job,
                                 JobContext ctx,
                                 Trigger trigger) {
        return timer.scheduleJob( job,
                                  ctx,
                                  trigger );
    }

    public boolean removeJob(JobHandle jobHandle) {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

}
