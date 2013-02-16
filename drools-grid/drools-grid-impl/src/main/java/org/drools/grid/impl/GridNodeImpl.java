/*
 *  Copyright 2010 salaboy.
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
 */
package org.drools.grid.impl;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.drools.agent.KnowledgeAgent;
import org.drools.grid.helper.GridHelper;
import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.grid.Grid;
import org.drools.grid.GridNode;
import org.drools.grid.GridServiceDescription;
import org.drools.grid.MessageReceiverHandlerFactoryService;
import org.drools.grid.io.MessageReceiverHandler;
import org.drools.grid.io.impl.NodeData;
import org.drools.grid.remote.StatefulKnowledgeSessionRemoteClient;
import org.drools.grid.service.directory.WhitePages;
import org.drools.impl.StatefulKnowledgeSessionImpl;
import org.drools.util.ServiceRegistry;
import org.drools.util.ServiceRegistryImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GridNodeImpl
        implements
        GridNode,
        MessageReceiverHandlerFactoryService {

    private String id;
    private final Map<String, Object> localContext = new ConcurrentHashMap<String, Object>();
    private final Map<String, String> sessionids = new ConcurrentHashMap<String, String>();
    private final Map<String, String> reversesessionids = new ConcurrentHashMap<String, String>();
    private final ServiceRegistry serviceRegistry = ServiceRegistryImpl.getInstance();
    private static Logger logger = LoggerFactory.getLogger(GridNodeImpl.class);

    private Grid grid;

    private boolean disposed = false;

    public GridNodeImpl( Grid grid ) {
        this.grid = grid;
        this.id = UUID.randomUUID().toString();
    }

    public GridNodeImpl( String id, Grid grid ) {
        this.grid = grid;
        this.id = id;
    }

    /*
     * (non-Javadoc) @see org.drools.grid.GridNode#get(java.lang.String,
     * java.lang.Class)
     */
    public <T> T get(String identifier,
            Class<T> cls) {
        T service = null;
        // @TODO: this is a hack we need to have a more flexible mechanisms to expose session identifiers
        if (cls.isAssignableFrom(String.class)) {
            if (logger.isDebugEnabled()) {
                logger.debug(" ### GridNodeImpl("+this+"): Resolving String.class with ID: " + identifier + ")");
                
            }
            if(logger.isTraceEnabled()){
                logger.trace(" ### GridNodeImpl: \t available sessionsid: " + sessionids + ")");
                logger.trace(" ### GridNodeImpl: \t available reverse sessionsid: " + reversesessionids + ")");
            }
            service = (T) sessionids.get(identifier);

            return service;
        }
        if (cls.isAssignableFrom(StatefulKnowledgeSession.class)) {
            if (logger.isDebugEnabled()) {
                logger.debug(" ### GridNodeImpl("+this+"): Resolving StatefulKnowledgeSession.class with ID: " + identifier + ")");
                
            }
            if( logger.isTraceEnabled()){
                logger.trace(" ### GridNodeImpl: \t localContext KeySet: " + localContext.keySet() + ")");
                logger.trace(" ### GridNodeImpl: \t sessions KeySet: " + sessionids.keySet() + ")");
                logger.trace(" ### GridNodeImpl: \t reversesessionids KeySet: " + reversesessionids.keySet() + ")");
                logger.trace(" ### GridNodeImpl: \t sessions values: " + sessionids.values() + ")");
                logger.trace(" ### GridNodeImpl: \t reverse sessions values: " + reversesessionids.values() + ")");
            }
            Object o = localContext.get(identifier);
            if (o != null) {
                service = (T) o;
            }

            if (service == null) {
                //Try with reverse
                String sessionId = reversesessionids.get(identifier);
                if (logger.isDebugEnabled()) {
                    logger.debug(" ### GridNodeImpl("+this+"): Resolving StatefulKnowledgeSession.class with reverse ID: " + identifier + " - Found: -> " + sessionId + ")");
                }
                if (sessionId != null) {
                    o = localContext.get(sessionId);
                    if (o != null) {
                        service = (T) o;
                    }
                }
            }
            if (logger.isDebugEnabled()) {
                logger.debug(" ### GridNodeImpl("+this+"): Resolving StatefulKnowledgeSession.class with ID: " + identifier + " - Instance Found: " + service + " )");
            }
            if (service != null) {
                return (T) service;
            }
        }
        service = (T) localContext.get(identifier);
        if (service == null) {
            service = this.serviceRegistry.get(cls);
        }

        return service;
    }

    /*
     * (non-Javadoc) @see org.drools.grid.GridNode#get(java.lang.Class)
     */
    public <T> T get(Class<T> serviceClass) {
        return get(serviceClass.getName(),
                serviceClass);
    }

    /*
     * (non-Javadoc) @see org.drools.grid.GridNode#set(java.lang.String,
     * java.lang.Object)
     */
    public void set(String identifier,
            Object object) {

        if (object instanceof StatefulKnowledgeSession) {
            String instanceId = "";
            if (logger.isDebugEnabled()) {
                logger.debug(" ### GridNodeImpl("+this+"): Registering session: " + object);
            }
            if(object instanceof StatefulKnowledgeSessionRemoteClient){
                throw new IllegalStateException("I'm registering a remote client!!!");
                //instanceId = ((StatefulKnowledgeSessionRemoteClient)object).getInstanceId();
            }
            if(object instanceof StatefulKnowledgeSessionImpl){
                instanceId = UUID.randomUUID().toString();
            }
            StatefulKnowledgeSession ksession = (StatefulKnowledgeSession) object;
            
            if (logger.isDebugEnabled()) {
                logger.debug(" ### GridNodeImpl("+this+"): Registering  (" + this.id + ") id: " + identifier + " (reverse - clientSessionId: " + instanceId + ") - SFKS: " + ksession);
            }
            this.sessionids.put(identifier, instanceId);
            this.reversesessionids.put(instanceId, identifier);
            this.localContext.put(identifier, ksession);
            return;
        }

        this.localContext.put(identifier,
                object);
    }

    /*
     * (non-Javadoc) @see org.drools.grid.GridNode#getId()
     */
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void dispose() {
        if ( ! disposed ) {
            for ( String sid : reversesessionids.keySet() ) {
                String sName = reversesessionids.get( sid );
                if ( sName != null && localContext.containsKey( sName ) ) {
                    StatefulKnowledgeSession ks = (StatefulKnowledgeSession) localContext.get( sName );
                    if ( ks != null ) {
                        ks.dispose();
                    } else {
                        throw new IllegalStateException( "Expected kSession in node " + sName );
                    }
                }
                String kName = sName + "_kAgent";
                if ( localContext.containsKey( kName ) ) {
                    KnowledgeAgent kAgent = (KnowledgeAgent) localContext.get( kName );
                    kAgent.dispose();
                } else {
//                throw new IllegalStateException( "Expected kAgent in node " + kName );
                }
            }
            localContext.clear();
            GridHelper.notifyDestruction( this );
            grid = null;
            disposed = true;
        }
    }

    public boolean isDisposed() {
        return disposed;
    }

    public void init(Object context) {
    }

    public MessageReceiverHandler getMessageReceiverHandler() {
        return new GridNodeServer(this,
                new NodeData());
    }

    public void registerSocketService(Grid grid,
            String id,
            String ip,
            int port) {
        WhitePages wp = grid.get(WhitePages.class);

        GridServiceDescription<GridNode> gsd = wp.lookup(id);
        if (gsd
                == null) {
            gsd = wp.create( id, grid.getId() );
        }

        gsd.setServiceInterface(GridNode.class);

        gsd.addAddress( "socket" ).setObject( new InetSocketAddress( ip, port ) );
    }

    public boolean isRemote() {
        return false;
    }

    public boolean isLocalProxy() {
        return false;
    }

    public Grid getGrid() {
        return grid;
    }

    public void setGrid(Grid grid) {
        this.grid = grid;
    }
}
