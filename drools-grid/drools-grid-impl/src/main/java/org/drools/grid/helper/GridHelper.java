/*
 * Copyright 2012 JBoss by Red Hat.
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
 */
package org.drools.grid.helper;

import java.util.HashMap;
import java.util.Map;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import org.drools.grid.*;
import org.drools.grid.conf.impl.GridPeerConfiguration;
import org.drools.grid.impl.GridImpl;
import org.drools.grid.io.ConversationManager;
import org.drools.grid.remote.KnowledgeAgentRemoteClient;
import org.drools.grid.remote.QueryResultsRemoteClient;
import org.drools.grid.remote.StatefulKnowledgeSessionRemoteClient;
import org.drools.grid.service.directory.WhitePages;
import org.drools.grid.service.directory.impl.JpaWhitePages;
import org.drools.grid.service.directory.impl.WhitePagesLocalConfiguration;
import org.kie.agent.KnowledgeAgent;
import org.kie.runtime.StatefulKnowledgeSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author salaboy
 */
public class GridHelper {

    public static Logger logger = LoggerFactory.getLogger(GridHelper.class);

    private static EntityManagerFactory emf = Persistence.createEntityManagerFactory( "org.drools.grid" );
    private static JpaWhitePages whitePages = new JpaWhitePages( emf );

    public static void reset() {
        if ( emf != null && emf.isOpen() ) {
            emf.close();
        }
        emf = Persistence.createEntityManagerFactory( "org.drools.grid" );
        whitePages = new JpaWhitePages( emf );
    }

    public static Grid createGrid() {

        Grid gridHelper = new GridImpl( new HashMap<String, Object>() );
        //Local Grid Configuration, for our client
        GridPeerConfiguration conf = new GridPeerConfiguration();


        //Configuring the a local WhitePages service that is being shared with all the grid peers
        WhitePagesLocalConfiguration wplConf = new WhitePagesLocalConfiguration();

        wplConf.setWhitePages( whitePages );
        conf.addConfiguration( wplConf );

        conf.configure( gridHelper );

        return gridHelper;

    }
    
    
    private static Map<String, GridNode> nodeCache = new HashMap<String, GridNode>();

    public static synchronized GridNode getGridNode( String name, Grid grid, boolean forceRemote ) {

        if ( logger.isDebugEnabled() ) {
            logger.debug(" ### Grid Helper trying to locate GridNode: " + name);
        }

        if ( nodeCache.containsKey( name ) ) {
            logger.debug(" ### Grid Helper found node " + name + " in cache" );
            return nodeCache.get( name );
        }

        GridServiceDescription<GridNode> nGsd = grid.get( WhitePages.class ).lookup( name );

        if ( nGsd == null ) {
            if ( logger.isDebugEnabled() ) {
                logger.error( "(" + Thread.currentThread().getId() + ")"+Thread.currentThread().getName() +" ### Grid Helper DOES NOT Found a Node Descriptor for: " + name );
            }
            return null;
        }
        if ( logger.isDebugEnabled() ) {

            logger.debug( "(" + Thread.currentThread().getId() + ")"+Thread.currentThread().getName() +" ### Grid Helper Found Node Descriptor: " + nGsd );
            logger.debug( "(" + Thread.currentThread().getId() + ")"+Thread.currentThread().getName() +" ### \t id: " + nGsd.getId() );
            logger.debug( "(" + Thread.currentThread().getId() + ")"+Thread.currentThread().getName() +" ### \t Address size: " + nGsd.getAddresses().size() );
            logger.debug( "(" + Thread.currentThread().getId() + ")"+Thread.currentThread().getName() +" ### \t Addresses: " + nGsd.getAddresses() );
            for ( String key : nGsd.getAddresses().keySet() ) {
                logger.debug( "(" + Thread.currentThread().getId() + ")"+Thread.currentThread().getName() +" \t ### Address: " + nGsd.getAddresses().get(key) );
            }

            logger.debug( "(" + Thread.currentThread().getId() + ")"+Thread.currentThread().getName() +" ### \t Interface: " + nGsd.getServiceInterface() );
            logger.debug( "(" + Thread.currentThread().getId() + ")"+Thread.currentThread().getName() +" ### \t DATA: " + nGsd.getData() );
        }

        ConnectionFactoryService csf = grid.get( ConnectionFactoryService.class );
        boolean allowsLocal = csf.isLocalAllowed();
        csf.setLocalAllowed( ! forceRemote );
        GridConnection<GridNode> conn = csf.createConnection( nGsd );
        csf.setLocalAllowed( allowsLocal );

        if ( logger.isDebugEnabled() ) {
            logger.debug( "(" + Thread.currentThread().getId() + ")"+Thread.currentThread().getName() +" ### Grid Helper Create a Conection: " + name );
        }
        GridNode node = conn.connect();

        if ( logger.isDebugEnabled() ) {
            logger.debug( "(" + Thread.currentThread().getId() + ")"+Thread.currentThread().getName() +" ### Grid Helper found GridNode: (" + name + ") -> " + node );
        }

        nodeCache.put( name, node );

        return node;
    }

    public static QueryResultsRemoteClient getQueryResultRemoteClient( Grid grid, String nodeId, String sessionId, String queryName, String remoteResultsId ) {

        GridServiceDescription<GridNode> gsd = getGridServiceDescriptor( grid, nodeId );
        GridNode node = getGridNode( nodeId, grid, false );
        String reverseId = node.get( sessionId, String.class );
        return new QueryResultsRemoteClient( queryName, reverseId, remoteResultsId, gsd, grid.get( ConversationManager.class ) ) ;
    }
    
    
    public static KnowledgeAgent getKnowledgeAgentRemoteClient( Grid grid, String nodeId, String sessionId ) {
        GridServiceDescription<GridNode> gsd = getGridServiceDescriptor( grid, nodeId );
        GridNode node = getGridNode( nodeId, grid, false );
        String reverseId = node.get( sessionId, String.class );
        if ( logger.isDebugEnabled() ) {
            logger.debug(" ### Grid Helper: Creating KnowledgeAgent Client for: reverseId: " + reverseId +" - session-id: "+sessionId );
        }
        return new KnowledgeAgentRemoteClient( reverseId, gsd, grid.get( ConversationManager.class ) );
    }

    private static GridServiceDescription<GridNode> getGridServiceDescriptor( Grid grid, String name ) {
        if ( logger.isDebugEnabled() ) {
            logger.debug( "(" + Thread.currentThread().getId() + ")"+Thread.currentThread().getName() +" ### Grid Helper trying to locate GridNode: " + name );
        }


        if ( logger.isDebugEnabled() ) {
            logger.debug( "(" + Thread.currentThread().getId() + ")"+Thread.currentThread().getName() +" ### Grid Helper Looking up: " + name );
        }
        GridServiceDescription<GridNode> nGsd = grid.get( WhitePages.class ).lookup( name );
        return nGsd;
    }
    
    public static StatefulKnowledgeSession getStatefulKnowledgeSession( Grid grid, String nodeId, String sessionId, boolean forceRemote ) {
        GridNode node = GridHelper.getGridNode( nodeId, grid, forceRemote );
        logger.error( "(" + Thread.currentThread().getId() + ")"+Thread.currentThread().getName() +" ### SESSION 2 : Looking session = "+sessionId + " in node = "+nodeId + " - " + node );
        if ( logger.isDebugEnabled() ) {
            logger.debug( "(" + Thread.currentThread().getId() + ")"+Thread.currentThread().getName() +" ### SESSION 2 : Looking session = "+sessionId + " in node = "+nodeId + " - " + node );
        }
        StatefulKnowledgeSession kSession = node.get( sessionId, StatefulKnowledgeSession.class );
        return kSession;
    }
}
