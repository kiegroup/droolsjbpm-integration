/*
 * Copyright 2010 salaboy.
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
 * under the License.
 */

package org.drools.grid.remote;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.drools.KnowledgeBaseFactoryService;
import org.drools.SystemEventListenerFactory;
import org.drools.builder.KnowledgeBuilderFactoryService;
import org.drools.grid.GridNode;
import org.drools.grid.GridServiceDescription;
import org.drools.grid.io.ConversationManager;
import org.drools.grid.io.impl.ConversationManagerImpl;
import org.drools.grid.remote.mina.MinaConnector;
import org.drools.util.ServiceRegistry;
import org.drools.util.ServiceRegistryImpl;

/**
 *
 * @author salaboy
 */
public class GridNodeRemoteClient<T>
    implements
    GridNode {

    private GridServiceDescription    gsd;
    private final Map<String, Object> localContext    = new ConcurrentHashMap<String, Object>();
    private final ServiceRegistry     serviceRegistry = ServiceRegistryImpl.getInstance();
    private MinaConnector connector = new MinaConnector();
    
    public GridNodeRemoteClient(GridServiceDescription gsd) {
        this.gsd = gsd;
        init( this.localContext );
    }

    public <T> T get(String identifier,
                     Class<T> cls) {
        T service = (T) localContext.get( identifier );
        if ( service == null ) {
            service = this.serviceRegistry.get( cls );
        }

        return service;
    }

    public <T> T get(Class<T> serviceClass) {
        return get( serviceClass.getName(),
                    serviceClass );
    }

    public void set(String identifier,
                    Object object) {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    public String getId() {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    public void init(Object context) {

        
        ConversationManager cm = new ConversationManagerImpl( this.gsd.getId(),
                                                              connector,
                                                              SystemEventListenerFactory.getSystemEventListener() );
        this.localContext.put( KnowledgeBuilderFactoryService.class.getCanonicalName(),
                               new KnowledgeBuilderProviderRemoteClient( cm,
                                                                         gsd ) );
        this.localContext.put( KnowledgeBaseFactoryService.class.getCanonicalName(),
                               new KnowledgeBaseProviderRemoteClient( cm,
                                                                      gsd ) );

    }

    public void dispose() {
         connector.close();
    }

}
