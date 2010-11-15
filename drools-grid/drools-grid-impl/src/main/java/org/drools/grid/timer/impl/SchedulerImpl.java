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

import java.net.InetSocketAddress;

import org.drools.grid.CoreServicesLookup;
import org.drools.grid.Grid;
import org.drools.grid.GridServiceDescription;
import org.drools.grid.MessageReceiverHandlerFactoryService;
import org.drools.grid.io.MessageReceiverHandler;
import org.drools.grid.service.directory.Address;
import org.drools.grid.service.directory.WhitePages;
import org.drools.grid.service.directory.impl.CoreServicesLookupImpl;
import org.drools.grid.service.directory.impl.GridServiceDescriptionImpl;
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

    public SchedulerImpl(String id) {
        this.id = id;
    }

    public String getId() {
        return this.id;
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

    public MessageReceiverHandler getMessageReceiverHandler() {
        return new SchedulerServer( this );
    }

    public void registerSocketService(Grid grid,
                                      String id,
                                      String ip,
                                      int port) {
        CoreServicesLookupImpl coreServicesWP = (CoreServicesLookupImpl) grid.get( CoreServicesLookup.class );

        GridServiceDescriptionImpl gsd = (GridServiceDescriptionImpl) coreServicesWP.lookup( SchedulerService.class );
        if ( gsd == null ) {
            gsd = new GridServiceDescriptionImpl( WhitePages.class );
        }

        GridServiceDescription<WhitePages> service = coreServicesWP.getServices().get( SchedulerService.class.getName() );
        if ( service == null ) {
            coreServicesWP.getServices().put( SchedulerService.class.getName(),
                                              gsd );
            service = gsd;
        }

        Address address = null;
        if ( service.getAddresses().get( "socket" ) != null ) {
            address = service.getAddresses().get( "socket" );
        } else {
            address = service.addAddress( "socket" );
        }
        InetSocketAddress[] addresses = (InetSocketAddress[]) address.getObject();
        if ( addresses != null && addresses.length >= 1 ) {
            InetSocketAddress[] newAddresses = new InetSocketAddress[ addresses.length + 1 ];
            if ( addresses != null ) {
                System.arraycopy( addresses,
                                  0,
                                  newAddresses,
                                  0,
                                  addresses.length );
            }
            newAddresses[addresses.length] = new InetSocketAddress( ip,
                                                                    port );
            ServiceConfiguration conf = new SchedulerServiceConfiguration( newAddresses );
            service.setData( conf );
        } else {
            InetSocketAddress[] newAddress = new InetSocketAddress[ 1 ];
            newAddress[0] = new InetSocketAddress( ip,
                                                   port );
            address.setObject( newAddress );
            ServiceConfiguration conf = new SchedulerServiceConfiguration( newAddress );
            service.setData( conf );
        }

    }

}
