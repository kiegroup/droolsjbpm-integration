package org.drools.grid.service.directory.impl;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.drools.grid.CoreServicesLookup;
import org.drools.grid.Grid;
import org.drools.grid.GridServiceDescription;
import org.drools.grid.MessageReceiverHandlerFactoryService;
import org.drools.grid.SocketService;
import org.drools.grid.io.MessageReceiverHandler;
import org.drools.grid.service.directory.Address;
import org.drools.grid.service.directory.WhitePages;
import org.drools.grid.timer.impl.ServiceConfiguration;

public class WhitePagesImpl
    implements
    WhitePages,
    MessageReceiverHandlerFactoryService {
    private Map<String, GridServiceDescription> directory = new ConcurrentHashMap<String, GridServiceDescription>();

    public GridServiceDescription create( String serviceDescriptionId, String ownerGridId ) {
        GridServiceDescription gsd = new GridServiceDescriptionImpl( serviceDescriptionId, ownerGridId );
        this.directory.put( gsd.getId(),
                            gsd );
        return gsd;
    }

    public GridServiceDescription lookup(String serviceDescriptionId) {
        return this.directory.get( serviceDescriptionId );
    }

    public void remove(String serviceDescriptionId) {
        this.directory.remove( serviceDescriptionId );
    }

    public MessageReceiverHandler getMessageReceiverHandler() {
        return new WhitePagesServer( this );
    }
   
    public void registerSocketService(Grid grid, String id, String ip, int port) {
        doRegisterSocketService(grid, id, ip, port);
    }
    
    public static void doRegisterSocketService( Grid grid, String id, String ip, int port ) {
        CoreServicesLookupImpl coreServicesWP = (CoreServicesLookupImpl) grid.get( CoreServicesLookup.class );

        GridServiceDescriptionImpl gsd = (GridServiceDescriptionImpl) coreServicesWP.lookup( WhitePages.class );
        if ( gsd == null ) {
            gsd = new GridServiceDescriptionImpl( WhitePages.class, grid.getId() );
        }

        GridServiceDescription<WhitePages> service = coreServicesWP.getServices().get( WhitePages.class.getName() );
        if ( service == null ) {
            coreServicesWP.getServices().put( WhitePages.class.getName(),
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
            ServiceConfiguration conf = new WhitePagesServiceConfiguration( newAddresses );
            service.setData( conf );
        } else {
            InetSocketAddress[] newAddress = new InetSocketAddress[ 1 ];
            newAddress[0] = new InetSocketAddress( ip,
                                                   port );
            address.setObject( newAddress );
            ServiceConfiguration conf = new WhitePagesServiceConfiguration( newAddress );
            service.setData( conf );
        }
    }
}
