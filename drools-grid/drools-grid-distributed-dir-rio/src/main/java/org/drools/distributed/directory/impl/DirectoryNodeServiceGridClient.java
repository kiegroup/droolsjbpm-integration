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

package org.drools.distributed.directory.impl;

import java.rmi.RemoteException;
import java.util.Map;
import org.drools.KnowledgeBase;
import org.drools.grid.ConnectorException;
import org.drools.grid.DirectoryNodeService;
import org.drools.grid.GenericConnection;
import org.drools.grid.GenericNodeConnector;

/**
 *
 * @author salaboy
 */
public class DirectoryNodeServiceGridClient implements DirectoryNodeService {
    private DirectoryNodeService client;
    
    public DirectoryNodeServiceGridClient(GenericNodeConnector connector, GenericConnection connection) {
        


        this.client = ((DistributedRioDirectoryConnector) connector).getDirectoryNodeService();
    }



    public void register(String executorId, String resourceId) throws ConnectorException, RemoteException {
        
        client.register(executorId, resourceId);
    }

    public void register(String executorId, GenericNodeConnector resourceConnector) throws ConnectorException, RemoteException {
        
        client.register(executorId, resourceConnector);
    }

    public void unregister(String executorId) throws ConnectorException, RemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public GenericNodeConnector lookup(String resourceId) throws ConnectorException, RemoteException {
        return client.lookup(resourceId);
    }

    public String lookupId(String resourceId) throws ConnectorException, RemoteException {
        return client.lookupId(resourceId);
    }

    public void registerKBase(String kbaseId, String resourceId) throws ConnectorException, RemoteException {
        client.registerKBase(kbaseId, resourceId);
    }

    public void registerKBase(String kbaseId, KnowledgeBase kbase) throws ConnectorException, RemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void unregisterKBase(String kbaseId) throws ConnectorException, RemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public KnowledgeBase lookupKBase(String kbaseId) throws ConnectorException, RemoteException {
        return client.lookupKBase(kbaseId);
    }

    public Map<String, String> getExecutorsMap() throws ConnectorException, RemoteException {
        return client.getExecutorsMap();
    }

    public Map<String, String> getKBasesMap() throws ConnectorException, RemoteException {
        return client.getKBasesMap();
    }

    public void dispose() throws ConnectorException, RemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

   

    public String getId() throws ConnectorException, RemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public ServiceType getServiceType() throws ConnectorException, RemoteException {
        return ServiceType.DISTRIBUTED;
    }

    
   
  

}
