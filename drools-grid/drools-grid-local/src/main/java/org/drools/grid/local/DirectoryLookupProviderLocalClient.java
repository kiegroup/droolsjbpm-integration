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
package org.drools.grid.local;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.drools.builder.DirectoryLookupFactoryService;
import org.drools.grid.ConnectorException;
import org.drools.grid.DirectoryNodeService;
import org.drools.grid.GenericConnection;
import org.drools.grid.GenericNodeConnector;
import org.drools.runtime.CommandExecutor;

/**
 *
 * @author salaboy
 */
public class DirectoryLookupProviderLocalClient implements DirectoryLookupFactoryService {

    private static Map<String, CommandExecutor> services = new HashMap<String, CommandExecutor>();
    private GenericConnection connection;
    private GenericNodeConnector client;

    public DirectoryLookupProviderLocalClient(GenericNodeConnector client, GenericConnection connection) {
        this.connection = connection;
        this.client = client;
    }

    public void register(String key, CommandExecutor executor) {
            try {
                DirectoryNodeService directoryNode = connection.getDirectoryNode().get(DirectoryNodeService.class);
                directoryNode.register(key, client.getId());
            } catch (ConnectorException ex) {
                Logger.getLogger(DirectoryLookupProviderLocalClient.class.getName()).log(Level.SEVERE, null, ex);
            } catch (RemoteException ex) {
                Logger.getLogger(DirectoryLookupProviderLocalClient.class.getName()).log(Level.SEVERE, null, ex);
            }
        services.put(key, executor);
    }

    public CommandExecutor lookup(String key) {
        return services.get(key);
    }

    public Map<String, String> getDirectoryMap() {
        Map<String, String> directory = new HashMap<String, String>();
        for (String key : services.keySet()) {
            directory.put(key, services.get(key).toString());

        }
        return directory;

    }

    public void unregister(String key) {
        try {
                DirectoryNodeService directoryNode = connection.getDirectoryNode().get(DirectoryNodeService.class);
                directoryNode.unregister(key);
            } catch (ConnectorException ex) {
                Logger.getLogger(DirectoryLookupProviderLocalClient.class.getName()).log(Level.SEVERE, null, ex);
            } catch (RemoteException ex) {
                Logger.getLogger(DirectoryLookupProviderLocalClient.class.getName()).log(Level.SEVERE, null, ex);
            }
        services.remove(key);
    }
}
