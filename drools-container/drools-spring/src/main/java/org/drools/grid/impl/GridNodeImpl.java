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
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.drools.grid.Grid;
import org.drools.grid.GridNode;
/*
import org.kie.grid.GridServiceDescription;
import org.kie.grid.MessageReceiverHandlerFactoryService;
import org.kie.grid.SocketService;
import org.kie.grid.io.MessageReceiverHandler;
import org.kie.grid.io.impl.NodeData;
import org.kie.grid.service.directory.WhitePages;
*/
import org.kie.util.ServiceRegistry;
import org.kie.util.ServiceRegistryImpl;

public class GridNodeImpl
    implements
    GridNode
//    MessageReceiverHandlerFactoryService 
    {

    private String                    id;
    private final Map<String, Object> localContext    = new ConcurrentHashMap<String, Object>();
    private final ServiceRegistry     serviceRegistry = ServiceRegistryImpl.getInstance();

    public GridNodeImpl() {
        this.id = UUID.randomUUID().toString();
    }

    public GridNodeImpl(String id) {
        this.id = id;
    }

    /* (non-Javadoc)
     * @see org.kie.grid.GridNode#get(java.lang.String, java.lang.Class)
     */
    public <T> T get(String identifier,
                     Class<T> cls) {
        T service = (T) localContext.get( identifier );
        if ( service == null ) {
            service = this.serviceRegistry.get( cls );
        }

        return service;
    }

    /* (non-Javadoc)
     * @see org.kie.grid.GridNode#get(java.lang.Class)
     */
    public <T> T get(Class<T> serviceClass) {
        return get( serviceClass.getName(),
                    serviceClass );
    }

    /* (non-Javadoc)
     * @see org.kie.grid.GridNode#set(java.lang.String, java.lang.Object)
     */
    public void set(String identifier,
                    Object object) {
        this.localContext.put( identifier,
                               object );
    }

    /* (non-Javadoc)
     * @see org.kie.grid.GridNode#getId()
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

    /*
    public MessageReceiverHandler getMessageReceiverHandler() {
        return new GridNodeServer( this,
                                   new NodeData() );
    }

    public void registerSocketService(Grid grid,
                                      String id,
                                      String ip,
                                      int port) {
      WhitePages wp = grid.get( WhitePages.class );
      
      GridServiceDescription<GridNode> gsd = wp.lookup( id );
      
      if ( gsd == null ) {
          gsd = wp.create( id );
      }
      
      gsd.setServiceInterface( GridNode.class );
      
      gsd.addAddress( "socket" ).setObject( new InetSocketAddress( ip,
                                                                   port ) );
    }
    */

}
