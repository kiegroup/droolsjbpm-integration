/**
 * 
 */
package org.drools.grid.timer.impl;

import org.drools.grid.service.directory.impl.*;
import java.net.InetSocketAddress;
import org.drools.grid.CoreServicesWhitePages;

import org.drools.grid.Grid;
import org.drools.grid.GridPeerServiceConfiguration;
import org.drools.grid.GridServiceDescription;
import org.drools.grid.MessageReceiverHandlerFactoryService;
import org.drools.grid.MultiplexSocketService;
import org.drools.grid.service.directory.Address;
import org.drools.grid.service.directory.WhitePages;
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

            
            GridServiceDescription service = coreServicesWP.getServices().get( Scheduler.class.getName() );
            if( service == null){
                coreServicesWP.getServices().put(Scheduler.class.getName(), gsd);
                service = gsd;
            }
            Address address = null;
            if(service.getAddresses().get("socket") != null){
                address = service.getAddresses().get("socket");
            } else{
                address = service.addAddress( "socket" );
            }
            InetSocketAddress[] addresses = (InetSocketAddress[])address.getObject();
            int newAddressesLenght = 1;
            if(addresses != null){
                newAddressesLenght = addresses.length + 1;
            }
            InetSocketAddress[] newAddresses = new InetSocketAddress[newAddressesLenght];
            if(addresses !=null){
                System.arraycopy(addresses, 0, newAddresses, 0, addresses.length);
            }
            newAddresses[newAddressesLenght-1]= new InetSocketAddress( mss.getIp(),
                                                         this.port);
            address.setObject(  newAddresses );
           
            
            mss.addService( this.port,
                            Scheduler.class.getName(),
                            ((MessageReceiverHandlerFactoryService) sched   ).getMessageReceiverHandler() );
        }
    }
}