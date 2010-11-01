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

import java.io.Serializable;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.drools.grid.Grid;
import org.drools.grid.GridServiceDescription;
import org.drools.grid.MessageReceiverHandlerFactoryService;
import org.drools.grid.internal.responsehandlers.BlockingMessageResponseHandler;
import org.drools.grid.io.Conversation;
import org.drools.grid.io.ConversationManager;
import org.drools.grid.io.MessageReceiverHandler;
import org.drools.grid.io.impl.CommandImpl;
import org.drools.grid.service.directory.Address;
import org.drools.time.Job;
import org.drools.time.JobContext;
import org.drools.time.JobHandle;
import org.drools.time.SchedulerService;
import org.drools.time.Trigger;
import org.drools.time.impl.MultiJobHandle;

/**
 *
 * @author salaboy
 */
public class SchedulerClient implements SchedulerService,
    MessageReceiverHandlerFactoryService{

    private GridServiceDescription schedulerGsd;

    private ConversationManager    conversationManager;

    private Grid grid;
    public SchedulerClient(Grid grid, GridServiceDescription schedulerGsd, ConversationManager conversationManager) {
        this.grid = grid;
        this.schedulerGsd = schedulerGsd;
        this.conversationManager = conversationManager;
    }
    
    public static Object sendMessage(ConversationManager conversationManager,
                                     Serializable addr,
                                     String id,
                                     Object body) {
        
        InetSocketAddress[] sockets = null;
        if(addr instanceof InetSocketAddress[]){
            sockets = (InetSocketAddress[])addr;
        }else if (addr instanceof InetSocketAddress){
            sockets = new InetSocketAddress[1];
            sockets[0] = (InetSocketAddress)addr;
        }
        
        
        BlockingMessageResponseHandler handler = new BlockingMessageResponseHandler();
        Exception exception = null;
        for ( InetSocketAddress socket : sockets ) {
            try {
                Conversation conv = conversationManager.startConversation( socket,
                                                                           id );
                conv.sendMessage( body,
                                  handler );
                exception = null;
            } catch ( Exception e ) {
                exception = e;
                conversationManager.endConversation();
            }
            if ( exception == null ) {
                break;
            }
        }
        if ( exception != null ) {
            throw new RuntimeException( "Unable to send message",
                                        exception );
        }
        try {
            return handler.getMessage().getBody();
        } finally {
            conversationManager.endConversation();
        }
    }


    public MessageReceiverHandler getMessageReceiverHandler() {
        return new SchedulerServer( this );
    }


    public JobHandle scheduleJob(Job job, JobContext ctx, Trigger trigger) {
        List<JobHandle> jobHandles = new ArrayList<JobHandle>();
        UuidJobHandle jobhandle = new UuidJobHandle();
        // Get the Service Configuration from the Data field
        SchedulerServiceConfiguration conf = (SchedulerServiceConfiguration) schedulerGsd.getData();
        // If the GSD doesn't have conf and it doesn't have addresses, we can use the local SchedulerService
        if(conf == null && schedulerGsd.getAddresses().get("socket") == null){
            SchedulerService sched = null;
            try {
                // We use the ID that contains the type of the service that we are using -> refactor this and include serviceType in GSD
                sched = grid.get((Class<SchedulerService>)Class.forName(schedulerGsd.getId()));
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(SchedulerClient.class.getName()).log(Level.SEVERE, null, ex);
            }
            return sched.scheduleJob(job, ctx, trigger);
        }
        // If we have a service configuration
        int redundancy = 1;
        InetSocketAddress[] addresses = null;
        if(conf != null){
            redundancy = conf.getRedundancy();
            addresses = conf.getServices(grid);
        }
        // If we have an address use that address. 
        if(addresses == null){
            if(schedulerGsd.getAddresses() != null && schedulerGsd.getAddresses().get("socket") != null){
                addresses = (InetSocketAddress[])schedulerGsd.getAddresses().get("socket").getObject();
            }
        }
        //If not use the configuration and the bucket systems.
        for( int i = 0; i < redundancy; i ++){
            int bucket = (int)jobhandle.hashCode() % addresses.length;
            //InetSocketAddress[] sockets = (InetSocketAddress[]) ((Address) schedulerGsd.getAddresses().get( "socket" )).getObject();
            InetSocketAddress socket =  addresses[bucket];
            CommandImpl cmd = new CommandImpl( "Scheduler.scheduleJob",
                                           Arrays.asList( new Object[]{ new ScheduledJob(jobhandle, job, ctx, trigger, null) } ) ); 
            UuidJobHandle  handle = (UuidJobHandle) sendMessage( this.conversationManager,
                     socket,
                     this.schedulerGsd.getId(),
                     cmd ); 
          
            jobHandles.add(handle);
        }
        
        return new MultiJobHandle(jobHandles);
        
    }

    public boolean removeJob(JobHandle jobHandle) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
