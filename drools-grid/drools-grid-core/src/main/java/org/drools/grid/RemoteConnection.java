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

package org.drools.grid;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.drools.KnowledgeBaseFactoryService;
import org.drools.builder.DirectoryLookupFactoryService;
import org.drools.builder.KnowledgeBuilderFactoryService;
import org.drools.grid.generic.GenericConnection;
import org.drools.grid.generic.GenericNodeConnector;
import org.drools.grid.remote.DirectoryLookupProviderRemoteClient;
import org.drools.grid.remote.KnowledgeBaseProviderRemoteClient;
import org.drools.grid.remote.KnowledgeBuilderProviderRemoteClient;
import org.drools.grid.strategies.DirectoryServiceSelectionStrategy;

import org.drools.grid.strategies.StaticIncrementalSelectionStrategy;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

/**
 *
 * @author salaboy
 */
public class RemoteConnection implements GenericConnection {
    //Cached Services
    private List<GenericNodeConnector> nodeConnectors;
    //Cached Directories
    private List<DirectoryNodeService> directories;

    public RemoteConnection() {
        this.nodeConnectors = new ArrayList<GenericNodeConnector>();
        this.directories = new ArrayList<DirectoryNodeService>();
    }

    public void addNodeConnector(GenericNodeConnector service) {
        //register the service to all the DirectoryServices
        for(DirectoryNodeService directory : directories){
            directory.addService(service);
        }
        this.nodeConnectors.add(service);

    }
    public void addDirectoryNode(DirectoryNodeService directory) {
        this.directories.add(directory);
    }

    // In real scenarios this method will be in charge of populating
    // all the ExecutionNodeService and DirectoryServices
    public void connect() {
        throw new NotImplementedException();
    }


    public ExecutionNode getExecutionNode(NodeSelectionStrategy strategy) {
        ExecutionNode node = null;
        GenericNodeConnector currentNode = null;
        try {

            //if the strategy is null use the default one
            if (strategy == null) {
                currentNode = getBestNode(new StaticIncrementalSelectionStrategy(this));
            } else {
                strategy.setConnection(this);
                currentNode = getBestNode(strategy);
            }
            if (currentNode.connect()) {
                node = new ExecutionNode();
                node.set(KnowledgeBuilderFactoryService.class, new KnowledgeBuilderProviderRemoteClient(currentNode));
                node.set(KnowledgeBaseFactoryService.class, new KnowledgeBaseProviderRemoteClient(currentNode));
                node.set(DirectoryLookupFactoryService.class, new DirectoryLookupProviderRemoteClient(currentNode, this));

            }
            
        } catch (RemoteException ex) {
            Logger.getLogger(RemoteConnection.class.getName()).log(Level.SEVERE, null, ex);
        }
        return node;
    }
    public GenericNodeConnector getBestNode(NodeSelectionStrategy nodeSelectionStrategy) {
        return nodeSelectionStrategy.getBestNode();
    }

    public List<DirectoryNodeService> getDirectories() {
        return directories;
    }

    public void setDirectories(List<DirectoryNodeService> directories) {
        this.directories = directories;
    }

    public List<GenericNodeConnector> getNodeConnectors() {
        return nodeConnectors;
    }

    public void setServices(List<GenericNodeConnector> services) {
        this.nodeConnectors = services;
    }

    public DirectoryNodeService getDirectoryNode(DirectoryServiceSelectionStrategy directorySelectionStrategy) {
        return directories.get(0);
    }

    public ExecutionNode getExecutionNode() {
        return getExecutionNode(null);
    }

   


}
