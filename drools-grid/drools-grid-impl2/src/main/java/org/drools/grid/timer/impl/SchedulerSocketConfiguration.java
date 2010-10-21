/**
 * 
 */
package org.drools.grid.timer.impl;

import org.drools.grid.service.directory.impl.*;
import java.net.InetSocketAddress;
import org.drools.grid.CoreServicesWhitePages;

import org.drools.grid.Grid;
import org.drools.grid.GridPeerServiceConfiguration;
import org.drools.grid.MessageReceiverHandlerFactoryService;
import org.drools.grid.MultiplexSocketService;
import org.drools.grid.service.directory.Address;
import org.drools.grid.service.directory.WhitePages;
import org.drools.grid.timer.CoreServicesScheduler;
import org.drools.grid.timer.Scheduler;

public class SchedulerSocketConfiguration
implements
GridPeerServiceConfiguration {
    private int port = -1;
    
    public SchedulerSocketConfiguration(int port) {
        this.port = port;
    }

    public int getPort() {
        return port;
    }
    
    public void configureService(Grid grid) {
        Scheduler sched = grid.get( Scheduler.class );

        if ( port != -1 ) {
            CoreServicesWhitePagesImpl coreServicesWP = (CoreServicesWhitePagesImpl) grid.get( CoreServicesWhitePages.class );

            GridServiceDescriptionImpl gsd = (GridServiceDescriptionImpl) coreServicesWP.lookup(Scheduler.class);
            if ( gsd == null ) {
                gsd = new GridServiceDescriptionImpl( Scheduler.class );
            }

            MultiplexSocketService mss = grid.get( MultiplexSocketService.class );

            Address address = gsd.addAddress( "socket" );
            address.setObject(  new InetSocketAddress[]{ new InetSocketAddress( mss.getIp(),
                                                         this.port ) } );

            coreServicesWP.getServices().put( Scheduler.class.getName(),
                                            gsd );

            mss.addService( this.port,
                            Scheduler.class.getName(),
                            ((MessageReceiverHandlerFactoryService) sched   ).getMessageReceiverHandler() );
        }
    }
}