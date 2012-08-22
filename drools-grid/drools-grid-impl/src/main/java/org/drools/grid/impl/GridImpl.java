package org.drools.grid.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;


import org.drools.SystemEventListener;
import org.drools.SystemEventListenerFactory;
import org.drools.grid.*;
import org.drools.grid.conf.GridPeerServiceConfiguration;
import org.drools.grid.io.AcceptorFactoryService;
import org.drools.grid.io.ConnectorFactoryService;
import org.drools.grid.io.ConversationManager;
import org.drools.grid.io.impl.ConversationManagerImpl;
import org.drools.grid.remote.GridNodeRemoteClient;
import org.drools.grid.remote.mina.MinaAcceptorFactoryService;
import org.drools.grid.remote.mina.MinaConnectorFactoryService;
import org.drools.grid.service.directory.WhitePages;
import org.drools.grid.service.directory.impl.WhitePagesRemoteConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GridImpl implements Grid {

    private String id;
    private Map<String, Object> services;
    private Map<String, GridNode> localNodes = new HashMap<String, GridNode>();
    private Map<String, GridPeerServiceConfiguration> serviceConfigurators = new HashMap();
    private static Logger logger = LoggerFactory.getLogger( GridImpl.class );

    public GridImpl() {
        this( UUID.randomUUID().toString(), null );
    }

    public GridImpl( String id ) {
        this( id, null );
    }

    public GridImpl( Map<String, Object> services ) {
        this( UUID.randomUUID().toString(), services );
    }

    public GridImpl( String id, Map<String, Object> services ) {
        if ( services == null ) {
            this.services = new ConcurrentHashMap<String, Object>();
        } else {
            this.services = services;
        }

        this.id = id;
        init();
    }

    private void init() {
        // TODO hardcoding these for now, should probably be configured
        SystemEventListener listener = SystemEventListenerFactory.getSystemEventListener();

        addService( SystemEventListener.class, listener );
        addService( AcceptorFactoryService.class, new MinaAcceptorFactoryService() );
        addService( ConnectorFactoryService.class, new MinaConnectorFactoryService() );
        addService( ConversationManager.class, new ConversationManagerImpl( this, listener ) );
        addService( ConnectionFactoryService.class.getName(), new ConnectionFactoryServiceImpl( this ) );

        this.serviceConfigurators.put( WhitePages.class.getName(), new WhitePagesRemoteConfiguration() );
    }

    public void dispose() {

        if ( logger.isInfoEnabled() ) {
            logger.info( " Shutting down GRID! " + id );
        }

        try {
            WhitePages wp = get( WhitePages.class, false );
            if ( wp != null ) {
                for ( String nodeId : localNodes.keySet() ) {
                    GridNode node = localNodes.get( nodeId );
                    if( ! node.isLocalProxy() ) {
                        wp.remove( nodeId );
                    }
                    node.dispose();
                }
            }
        } catch ( Throwable t ) {
            logger.error( " Grid couldn't unregister all local nodes " + t.getMessage(), t );
        } finally {
            SocketService socketService = get( SocketService.class );
            socketService.close();
        }

//        SystemEventListener listener = get( SystemEventListener.class, false );
//
//        AcceptorFactoryService acceptor = get( AcceptorFactoryService.class, false );
//
//        ConnectorFactoryService connector = get( ConnectorFactoryService.class, false );
//
//        ConversationManager orator = get( ConversationManager.class, false );
//
//        ConnectionFactoryService connecter = get( ConnectionFactoryService.class, false );

        if ( logger.isInfoEnabled() ) {
            logger.info( " GRID shut down ! " + id );
        }

    }

    public Object get( String str ) {
        return this.services.get( str );
    }


    public <T> T get( Class<T> serviceClass ) {
        return get( serviceClass, false );
    }

    public <T> T get( Class<T> serviceClass, boolean lazyInit ) {
        T service = (T) this.services.get( serviceClass.getName() );

        if ( lazyInit && service == null ) {
            // If the service does not exist, it'll lazily create it
            GridPeerServiceConfiguration configurator = this.serviceConfigurators.get( serviceClass.getName() );
            if (configurator != null) {
                configurator.configureService( this );
                service = (T) this.services.get( serviceClass.getName() );
            }
        }
        return service;
    }

    public void addService( Class cls, Object service ) {
        addService( cls.getName(), service );
    }

    public void addService( String id, Object service ) {
        this.services.put( id, service );
    }

    public GridNode createGridNode( String id ) {
        if ( logger.isDebugEnabled() ) {
            logger.debug( " ### GridImpl: Registering in white pages (grid = " + getId() + ") new node = " + id );
        }
        WhitePages wp = get( WhitePages.class );
        GridServiceDescription gsd = wp.create( id, this.id );
        gsd.setServiceInterface( GridNode.class );
        GridNode node = new GridNodeImpl( id );
        this.localNodes.put( id , node );
        return node;
    }

    public GridNode claimGridNode( String id ) {
        if ( logger.isDebugEnabled() ) {
            logger.debug( " ### GridImpl: Claiming orphan node " + id + " found in white pages (grid = " + getId() + ") " );
        }
        WhitePages wp = get( WhitePages.class );
        wp.remove( id );
        GridServiceDescription gsd = wp.create( id, this.id );
        gsd.setServiceInterface( GridNode.class );
        GridNode node = new GridNodeImpl( id );
        this.localNodes.put( id , node );
        return node;
    }

    public void removeGridNode( String id ) {
        WhitePages wp = get( WhitePages.class );
        wp.remove( id );
        this.localNodes.remove( id );
    }

    public GridNode getGridNode( String id ) {
        return this.localNodes.get( id );
    }

    public GridNode asRemoteNode( GridNode node ) {
        if  ( node.isRemote() ) {
            return node;
        }
        GridServiceDescription<GridNode> nGsd = this.get( WhitePages.class ).lookup( node.getId() );
        ConnectionFactoryService cfs = get( ConnectionFactoryService.class );
        boolean allowsLocal = cfs.isLocalAllowed();
        cfs.setLocalAllowed( false );
        GridConnection<GridNode> conn = cfs.createConnection( nGsd );
        if ( logger.isDebugEnabled() ) {
            logger.debug( "  ### Session Manager: Opened connection to node: " + conn );
        }
        // forcing a remote connection to the node
        cfs.setLocalAllowed( allowsLocal );
        GridNodeRemoteClient rem = (GridNodeRemoteClient) conn.connect();
        rem.setLocalProxy( true );
        this.localNodes.put( node.getId() + "$$LocalProxy", rem );
        return rem;
    }

    public String getId() {
        return id;
    }
}
