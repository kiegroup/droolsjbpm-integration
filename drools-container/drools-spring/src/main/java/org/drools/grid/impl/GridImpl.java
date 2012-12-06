package org.drools.grid.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.drools.grid.Grid;
import org.drools.grid.GridNode;

public class GridImpl
    implements
    Grid {
    private Map<String, Object>   services;

    private Map<String, GridNode> localNodes = new HashMap<String, GridNode>();
    
    //private Map<String, GridPeerServiceConfiguration> serviceConfigurators = new HashMap();
    
    private String id;

    public GridImpl() {
        this(null);
    }
    
    public GridImpl(Map<String, Object> services) {
        if ( services == null ) {
            this.services = new ConcurrentHashMap<String, Object>();
        } else {
            this.services = services;
        }
        
        this.id = UUID.randomUUID().toString();
        init();
    }
    
    private void init() {
//        // TODO hardcoding these for now, should probably be configured
//        SystemEventListener listener = SystemEventListenerFactory.getSystemEventListener();
//        this.services.put( SystemEventListener.class.getName(), listener );
//        this.services.put( AcceptorFactoryService.class.getName(), new MinaAcceptorFactoryService() );
//        this.services.put( ConnectorFactoryService.class.getName(), new MinaConnectorFactoryService() );
//        this.services.put( ConversationManager.class.getName(), new ConversationManagerImpl( this, listener ) );
//        
//        ConnectionFactoryService conn = new ConnectionFactoryServiceImpl(this);
//        this.services.put( ConnectionFactoryService.class.getName(), conn );
//        
//        this.serviceConfigurators.put( WhitePages.class.getName(), new WhitePagesRemoteConfiguration( ) );
    }

    public Object get(String str) {
        return this.services.get( str );
    }
    
    public <T> T get(Class<T> serviceClass) {
        throw new UnsupportedOperationException();
//        T service = (T) this.services.get( serviceClass.getName() );
//        
//        if ( service == null ) {
//            // If the service does not exist, it'll lazily create it
//            GridPeerServiceConfiguration configurator = this.serviceConfigurators.get( serviceClass.getName() );
//            if ( configurator != null ) {
//                configurator.configureService( this );
//                service = (T) this.services.get( serviceClass.getName() );
//            }
//        }
//        return service;
    }

    public void addService(Class cls,
                           Object service) {
        addService( cls.getName(),
                    service );
    }

    public void addService(String id,
                           Object service) {
        this.services.put( id,
                                service );
    }

    public GridNode createGridNode(String id) {
//        WhitePages wp = get( WhitePages.class );
//        GridServiceDescription gsd = wp.create( id );
//        gsd.setServiceInterface( GridNode.class );
        GridNode node = new GridNodeImpl( id );
        this.localNodes.put( id, node );
        return node;
    }

    public void removeGridNode(String id) {
//        WhitePages wp = get( WhitePages.class );
//        wp.remove( id );
        this.localNodes.remove( id );
    }

    public GridNode getGridNode(String id) {
        return this.localNodes.get( id );
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

//    public GridNodeConnection getGridNodeConnection(GridServiceDescription serviceDescription) {
//
//        if ( localNodes.containsKey( serviceDescription.getId() ) ) {
//            // see if the serviceDescription is local, if so use it
//            return new LocalGridNodeConnection( localNodes.get( serviceDescription.getId() ) );
//        } else {
//            // by default use socket
//
//        }
//
//        return null;
//    }
//
//    public GridNodeConnection getGridNodeConnection(Address address) {
//        boolean isLocal = false;
//        if ( "socket".equals( address.getTransport() ) ) {
//            InetSocketAddress isAddress = (InetSocketAddress) address.getObject();
//            try {
//                if ( InetAddress.getLocalHost().equals( isAddress.getAddress() ) ) {
//                    isLocal = true;
//                }
//            } catch ( UnknownHostException e ) {
//                throw new RuntimeException( "Unable to determine local ip address",
//                                            e );
//            }
//        }
//
//        if ( isLocal ) {
//            //new LocalGr
//        } else {
//
//        }
//
//        // TODO Auto-generated method stub
//        return null;
//    }
//
//    public GridNode createGridNode(String id) {
//        
//        WhitePages pages = get( WhitePages.class );
//        GridServiceDescription gsd = pages.create( id );
//        gsd.setServiceInterface( GridNode.class );
//        
//        SocketService ss = get( SocketService.class );
//        
//        gsd.addAddress( "socket" ).setObject( new         ss.getIp() )
//        
//        GridServiceDescription gsd = new GridServiceDescriptionImpl( id );
//        gsd.setServiceInterface( GridNode.class );
//        GridNode node = new GridNodeImpl( gsd.getId() );
//        
//        this.localNodes.put( gsd.getId(), node );
//        
//        GridNodeConnection connection = NodeConnectionFactory.newGridNodeConnection( gsd );
//        
//        GridNode gnode = connection.getGridNode();
//        if ( gnode instanceof GridNodeImpl ) {
//   
//        }
//
//
//
//        return gnode;
//
//    }
//    
//
//    public void removeGridNode(String id) {
//        // TODO Auto-generated method stub
//        
//    }
//
//    public GridNode createGridNode(GridServiceDescription gsd) {
//
//    }
//
//    public GridNode getGridNode(String id) {
////        GridNode node = this.localNodes.get( id );
////        GridNodeConnection conn;
////        if ( node != null ) {
////            conn = new LocalGridNodeConnection( node );
////        } else {
////            WhitePages pages = get( WhitePages.class );
////            GridServiceDescription gsd = pages.lookup( id );
////            conn = new RemoteGridNodeConnection( gsd );
////        }
//        
//        return conn;
//    }

    
}
