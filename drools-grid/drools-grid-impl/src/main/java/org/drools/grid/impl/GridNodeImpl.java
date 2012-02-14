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
import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.grid.Grid;
import org.drools.grid.GridNode;
import org.drools.grid.GridServiceDescription;
import org.drools.grid.MessageReceiverHandlerFactoryService;
import org.drools.grid.io.MessageReceiverHandler;
import org.drools.grid.io.impl.NodeData;
import org.drools.grid.service.directory.WhitePages;
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

    public GridNodeImpl() {
        this.id = UUID.randomUUID().toString();
    }

    public GridNodeImpl(String id) {
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
                logger.debug(" ### GridNodeImpl: Resolving String.class with ID: " + identifier + ")");
            }
            service = (T) sessionids.get(identifier);

            return service;
        }
        if (cls.isAssignableFrom(StatefulKnowledgeSession.class)) {
            if (logger.isDebugEnabled()) {
                logger.debug(" ### GridNodeImpl: Resolving StatefulKnowledgeSession.class with ID: " + identifier + ")");
            }
            Object o = localContext.get(identifier);
            if (o != null) {
                service = (T) o;
            }

            if (service == null) {
                //Try with reverse
                String sessionId = reversesessionids.get(identifier);
                if (logger.isDebugEnabled()) {
                    logger.debug(" ### GridNodeImpl: Resolving StatefulKnowledgeSession.class with reverse ID: " + identifier + " - Found: -> "+sessionId+")");
                }
                
                o = localContext.get(sessionId);
                if (o != null) {
                    service = (T) o;
                }
            }


            return (T) service;

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
            String randomId = UUID.randomUUID().toString();
            if (logger.isDebugEnabled()) {
                logger.debug(" ### GridNodeImpl: Registering  (" + this.id + ") id: " + identifier + " (reverse: " + randomId + ") - SFKS: " + object);
            }

            this.sessionids.put(identifier, randomId);
            this.reversesessionids.put(randomId, identifier);
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

        if (gsd == null) {
            gsd = wp.create(id);
        }

        gsd.setServiceInterface(GridNode.class);

        gsd.addAddress("socket").setObject(new InetSocketAddress(ip,
                port));
    }
}
