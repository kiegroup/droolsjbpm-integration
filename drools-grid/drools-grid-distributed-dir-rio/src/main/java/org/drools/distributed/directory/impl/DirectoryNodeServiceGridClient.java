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

import org.drools.grid.distributed.connectors.DistributedRioDirectoryConnector;
import java.rmi.RemoteException;
import java.util.Map;
import org.drools.KnowledgeBase;
import org.drools.grid.ConnectorException;
import org.drools.grid.DirectoryNodeService;
import org.drools.grid.GenericConnection;
import org.drools.grid.GenericConnectorFactory;
import org.drools.grid.GenericNodeConnector;

public class DirectoryNodeServiceGridClient implements DirectoryNodeService {
    
    private GenericNodeConnector connector;
    
    public DirectoryNodeServiceGridClient(GenericNodeConnector connector, GenericConnection connection) {
        

        this.connector = connector;
    
    }



    public void register(String executorId, String resourceId) throws ConnectorException, RemoteException {
        connector.connect();
        DirectoryNodeService client = ((DistributedRioDirectoryConnector) connector).getDirectoryNodeService();
        client.register(executorId, resourceId);
        connector.disconnect();
    }

    public void register(String executorId, GenericNodeConnector resourceConnector) throws ConnectorException, RemoteException {
        connector.connect();
        DirectoryNodeService client = ((DistributedRioDirectoryConnector) connector).getDirectoryNodeService();
        client.register(executorId, resourceConnector);
        connector.disconnect();
    }

    public void unregister(String executorId) throws ConnectorException, RemoteException {
        connector.connect();
        DirectoryNodeService client = ((DistributedRioDirectoryConnector) connector).getDirectoryNodeService();
        client.unregister(executorId);
        connector.disconnect();
    }

    public GenericNodeConnector lookup(String resourceId) throws ConnectorException, RemoteException {
        connector.connect();
        DirectoryNodeService client = ((DistributedRioDirectoryConnector) connector).getDirectoryNodeService();
        GenericNodeConnector result = GenericConnectorFactory.newConnector(client.lookupId(resourceId));
        connector.disconnect();
        return result;
    }

    public String lookupId(String resourceId) throws ConnectorException, RemoteException {
        connector.connect();
        DirectoryNodeService client = ((DistributedRioDirectoryConnector) connector).getDirectoryNodeService();
        String result = client.lookupId(resourceId);
        connector.disconnect();
        return result;
    }

    public void registerKBase(String kbaseId, String resourceId) throws ConnectorException, RemoteException {
        connector.connect();
        DirectoryNodeService client = ((DistributedRioDirectoryConnector) connector).getDirectoryNodeService();
        client.registerKBase(kbaseId, resourceId);
        connector.disconnect();
    }

    public void registerKBase(String kbaseId, KnowledgeBase kbase) throws ConnectorException, RemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void unregisterKBase(String kbaseId) throws ConnectorException, RemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public KnowledgeBase lookupKBase(String kbaseId) throws ConnectorException, RemoteException {
        connector.connect();
        DirectoryNodeService client = ((DistributedRioDirectoryConnector) connector).getDirectoryNodeService();
        KnowledgeBase result = client.lookupKBase(kbaseId);
        connector.disconnect();
        return result;
    }

    public Map<String, String> getExecutorsMap() throws ConnectorException, RemoteException {
        connector.connect();
        DirectoryNodeService client = ((DistributedRioDirectoryConnector) connector).getDirectoryNodeService();
        Map<String, String>  result = client.getExecutorsMap();
        connector.disconnect();
        return result;
    }

    public Map<String, String> getKBasesMap() throws ConnectorException, RemoteException {
        connector.connect();
        DirectoryNodeService client = ((DistributedRioDirectoryConnector) connector).getDirectoryNodeService();
        Map<String, String>  result = client.getKBasesMap();
        connector.disconnect();
        return result;
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

    public String lookupKBaseLocationId(String kbaseId) throws ConnectorException, RemoteException {
        connector.connect();
        DirectoryNodeService client = ((DistributedRioDirectoryConnector) connector).getDirectoryNodeService();
        String result = client.lookupKBaseLocationId(kbaseId);
        connector.disconnect();
        return result;
    }

    
   
  

}
