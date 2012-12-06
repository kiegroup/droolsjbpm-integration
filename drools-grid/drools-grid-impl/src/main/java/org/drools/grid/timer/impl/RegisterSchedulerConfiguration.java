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
import org.drools.grid.SocketService;
import org.drools.grid.conf.GridPeerServiceConfiguration;
import org.drools.grid.service.directory.Address;
import org.drools.grid.service.directory.impl.CoreServicesLookupImpl;
import org.drools.grid.service.directory.impl.GridServiceDescriptionImpl;
import org.drools.time.SchedulerService;

public class RegisterSchedulerConfiguration
    implements
    GridPeerServiceConfiguration {

    public RegisterSchedulerConfiguration() {
    }

    public void configureService(Grid grid) {
        CoreServicesLookupImpl coreServicesWP = (CoreServicesLookupImpl) grid.get( CoreServicesLookup.class );

        GridServiceDescriptionImpl gsd = (GridServiceDescriptionImpl) coreServicesWP.lookup( SchedulerService.class );
        if ( gsd == null ) {
            gsd = new GridServiceDescriptionImpl( SchedulerService.class, grid.getId() );
        }

        SocketService mss = grid.get( SocketService.class );
        int port = mss.getPorts().iterator().next();
        GridServiceDescription<SchedulerService> service = coreServicesWP.getServices().get( SchedulerService.class.getName() );
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

            newAddresses[addresses.length] = new InetSocketAddress( mss.getIp(),
                                                                    port );
            ServiceConfiguration conf = new SchedulerServiceConfiguration( newAddresses );
            service.setData( conf );
        } else {
            InetSocketAddress[] newAddress = new InetSocketAddress[ 1 ];
            newAddress[0] = new InetSocketAddress( mss.getIp(),
                                                   port );
            address.setObject( newAddress );
            ServiceConfiguration conf = new SchedulerServiceConfiguration( newAddress );
            service.setData( conf );

        }
    }
}
