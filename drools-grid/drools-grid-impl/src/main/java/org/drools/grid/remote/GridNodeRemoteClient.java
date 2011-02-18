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

import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.drools.KnowledgeBaseFactoryService;
import org.drools.builder.KnowledgeBuilderFactoryService;
import org.drools.grid.Grid;
import org.drools.grid.GridNode;
import org.drools.grid.GridServiceDescription;
import org.drools.grid.io.ConversationManager;
import org.drools.grid.io.impl.CommandImpl;
import org.drools.grid.remote.mina.MinaConnector;
import org.drools.grid.service.directory.Address;
import org.drools.util.ServiceRegistry;
import org.drools.util.ServiceRegistryImpl;

public class GridNodeRemoteClient<T>
    implements
    GridNode {

    private GridServiceDescription    gsd;
    private Grid                      grid;
    private final Map<String, Object> localContext    = new ConcurrentHashMap<String, Object>();
    private final ServiceRegistry     serviceRegistry = ServiceRegistryImpl.getInstance();
    private MinaConnector             connector       = new MinaConnector();

    public GridNodeRemoteClient(Grid grid,
                                GridServiceDescription gsd) {
        this.gsd = gsd;
        this.grid = grid;
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
       //We need a way to do it more generic, so we can set whatever we want.
        
       if(object instanceof StatefulKnowledgeSessionRemoteClient){
            String localId = UUID.randomUUID().toString();

            CommandImpl cmd = new CommandImpl( "registerKsession",
                                               Arrays.asList( new Object[]{identifier, ((StatefulKnowledgeSessionRemoteClient)object).getInstanceId()} ) );

            ConversationManager connm = this.grid.get( ConversationManager.class );
            ConversationUtil.sendMessage( connm,
                                          (InetSocketAddress) ((Map<String, Address>)this.gsd.getAddresses()).get( "socket" ).getObject(),
                                          this.gsd.getId(),
                                          cmd );
       } else{
            
           throw new UnsupportedOperationException( "Not supported yet." );
       }

        
    }

    public String getId() {
        return gsd.getId();
    }

    public void init(Object context) {

        this.localContext.put( KnowledgeBuilderFactoryService.class.getName(),
                               new KnowledgeBuilderProviderRemoteClient( this.grid,
                                                                         gsd ) );
        this.localContext.put( KnowledgeBaseFactoryService.class.getName(),
                               new KnowledgeBaseProviderRemoteClient( this.grid,
                                                                      gsd ) );

    }

    public void dispose() {
        connector.close();
    }

}
