package org.drools.grid.impl;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.drools.grid.Grid;
import org.drools.grid.GridNode;
import org.drools.grid.GridNodeConnection;
import org.drools.grid.GridServiceDescription;
import org.drools.grid.MessageReceiverHandlerFactoryService;
import org.drools.grid.MultiplexSocketService;
import org.drools.grid.local.LocalGridConnection;
import org.drools.grid.service.directory.Address;
import org.drools.grid.service.directory.WhitePages;
import org.drools.grid.service.directory.impl.AddressImpl;
import org.drools.grid.service.directory.impl.WhitePagesImpl;

public class GridImpl implements Grid {
    private Map<String, Object> services;
    private Map<String, Object> localServices;
    
    private Map<String, GridNode> localNodes = new HashMap<String, GridNode>();    
    
    public GridImpl(Map<String, Object> services) {
        this.services = services;
        this.localServices = new ConcurrentHashMap<String, Object>();
    }
    
    public <T> T get(Class<T> serviceClass) {
        T service = (T) this.localServices.get( serviceClass.getName() );
        if ( service == null ) {
            service = (T) services.get( serviceClass.getName() );    
        }
        return service;
    }
    
    public void addService(Class cls, Object service) {
        addService( cls.getName(), service );
    }
    public void addService(String id, Object service) {
        this.localServices.put( id, service );        
    }
    
    
//    public void configureServiceForSocket(int port, Class cls) {
//        configureServiceForSocket( port, cls.getName() );
//    }
//    
//    public void configureServiceForSocket(int port, String id) {
//        Object service = this.localServices.get( id );
//        if ( service == null ) {
//            throw new IllegalArgumentException( "Service '" + id + "' could not be found" );
//        }
//        this.socketServer.addService( port, id, ((MessageReceiverHandlerFactoryService) service).getMessageReceiverHandler() );        
//    }

    public GridNodeConnection getGridNodeConnection(GridServiceDescription serviceDescription) {
        
        if ( localNodes.containsKey( serviceDescription.getId() ) ) {
            // see if the serviceDescription is local, if so use it
            return new LocalGridConnection( localNodes.get( serviceDescription.getId() ) );
        } else {
            // by default use socket
            
        }
            
        return null;
    }
    
    public GridNodeConnection getGridNodeConnection(Address address) {
        boolean isLocal = false;
        if ( "socket".equals( address.getTransport() ) ) {
            InetSocketAddress isAddress = ( InetSocketAddress ) address.getObject();
            try {
                if ( InetAddress.getLocalHost().equals( isAddress.getAddress() ) ) {
                    isLocal = true;
                }
            } catch ( UnknownHostException e ) {
                throw new RuntimeException( "Unable to determine local ip address", e );
            }
        }
        
        if ( isLocal ) {
            //new LocalGr
        } else {
            
        }
        
        // TODO Auto-generated method stub
        return null;
    }

    public GridNode createGridNode(String id) {
        GridNodeConnection connection = new LocalGridConnection( id );
        GridNode gnode = connection.getGridNode();
        localNodes.put( id, gnode );
                
        WhitePages pages = get( WhitePages.class );
        pages.create( id );
        
        return gnode;
    }

}
