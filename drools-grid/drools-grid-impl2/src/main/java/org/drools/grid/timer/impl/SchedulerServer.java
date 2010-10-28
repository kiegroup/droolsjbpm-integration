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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.drools.grid.io.Conversation;
import org.drools.grid.io.Message;
import org.drools.grid.io.MessageReceiverHandler;
import org.drools.grid.io.impl.CommandImpl;
import org.drools.time.SchedulerService;


/**
 *
 * @author salaboy
 */
public class SchedulerServer implements
    MessageReceiverHandler{
    private SchedulerService scheduler = null; 

    public SchedulerServer(SchedulerService scheduler) {
        this.scheduler = scheduler;
    }
    
    public void messageReceived(Conversation conversation, Message msg) {
        final CommandImpl cmd = (CommandImpl) msg.getBody();
        this.execs.get( cmd.getName() ).execute( scheduler,
                                                 conversation,
                                                 msg,
                                                 cmd );
    }
    
    private Map<String, Exec> execs = new HashMap<String, Exec>() {
                                        {
                                            put( "Scheduler.scheduleJob",
                                                 new Exec() {
                                                     public void execute(Object object,
                                                                         Conversation con,
                                                                         Message msg,
                                                                         CommandImpl cmd) {
                                                         SchedulerService scheduler = (SchedulerService) object;
                                                         final List list = cmd.getArguments();
                                                         scheduler.scheduleJob(((ScheduledJob)list.get(0)).getJob(),((ScheduledJob)list.get(0)).getJobContext(), ((ScheduledJob)list.get(0)).getTrigger());
                                                         con.respond( ((ScheduledJob)list.get(0)).getJobHandle() );
                                                     }
                                                 } );
                                        }
                                    };

    public static interface Exec {
        void execute(Object object,
                     Conversation con,
                     Message msg,
                     CommandImpl cmd);
    }


}
