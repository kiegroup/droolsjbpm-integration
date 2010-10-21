/**
 * 
 */
package org.drools.grid.service.directory.impl;

import java.net.InetSocketAddress;

import org.drools.grid.CoreServicesWhitePages;
import org.drools.grid.Grid;
import org.drools.grid.GridPeerServiceConfiguration;
import org.drools.grid.MessageReceiverHandlerFactoryService;
import org.drools.grid.MultiplexSocketService;
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
            CoreServicesWhitePagesImpl coreServices = (CoreServicesWhitePagesImpl) grid.get( CoreServicesWhitePages.class );

            GridServiceDescriptionImpl gsd = (GridServiceDescriptionImpl) coreServices.lookup( WhitePages.class );
            if ( gsd == null ) {
                gsd = new GridServiceDescriptionImpl( WhitePages.class );
            }

            MultiplexSocketService mss = grid.get( MultiplexSocketService.class );

            Address address = gsd.addAddress( "socket" );
            address.setObject(  new InetSocketAddress[]{ new InetSocketAddress( mss.getIp(),
                                                         this.port ) } );

            coreServices.getServices().put( WhitePages.class.getName(),
                                            gsd );

            mss.addService( this.port,
                            WhitePages.class.getName(),
                            ((MessageReceiverHandlerFactoryService) wp).getMessageReceiverHandler() );
        }
    }
}