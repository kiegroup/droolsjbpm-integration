/**
 * 
 */
package org.drools.grid.service.directory.impl;

import java.io.Serializable;
import java.net.InetSocketAddress;

import org.drools.grid.CoreServicesLookup;
import org.drools.grid.Grid;
import org.drools.grid.GridServiceDescription;
import org.drools.grid.MessageReceiverHandlerFactoryService;
import org.drools.grid.SocketService;
import org.drools.grid.conf.GridPeerServiceConfiguration;
import org.drools.grid.service.directory.Address;
import org.drools.grid.service.directory.WhitePages;

public class WhitePagesSocketConfiguration
    implements
    GridPeerServiceConfiguration {
    private int port = -1;

    public WhitePagesSocketConfiguration(int port) {
        this.port = port;
    }

    public int getPort() {
        return port;
    }

    public void configureService(Grid grid) {
        WhitePages wp = grid.get( WhitePages.class );

        if ( port != -1 ) {
            CoreServicesWhitePagesImpl coreServicesWP = (CoreServicesWhitePagesImpl) grid.get( CoreServicesLookup.class );

            GridServiceDescriptionImpl gsd = (GridServiceDescriptionImpl) coreServicesWP.lookup( WhitePages.class );
            if ( gsd == null ) {
                gsd = new GridServiceDescriptionImpl( WhitePages.class );
            }

            SocketService mss = grid.get( SocketService.class );

            //            GridServiceDescription service = coreServicesWP.getServices().get( WhitePages.class.getName() );
            //            if( service == null){
            //                coreServicesWP.getServices().put(WhitePages.class.getName(), gsd);
            //                service = gsd;
            //            }
            //            
            //            Address address = null;
            //            if(service.getAddresses().get("socket") != null){
            //                address = service.getAddresses().get("socket");
            //            } else{
            //                address = service.addAddress( "socket" );
            //            }
            //            InetSocketAddress[] addresses = (InetSocketAddress[])address.getObject();
            //            int newAddressesLenght = 1;
            //            if(addresses != null){
            //                newAddressesLenght = addresses.length + 1;
            //            }
            //            InetSocketAddress[] newAddresses = new InetSocketAddress[newAddressesLenght];
            //            if(addresses !=null){
            //                System.arraycopy(addresses, 0, newAddresses, 0, addresses.length);
            //            }
            //            newAddresses[newAddressesLenght-1]= new InetSocketAddress( mss.getIp(),
            //                                                         this.port);
            //            address.setObject(  newAddresses );

            mss.addService( WhitePages.class.getName(),
                            this.port,
                            (MessageReceiverHandlerFactoryService) wp );
        }
    }
}