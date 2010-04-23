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
package org.drools.grid.distributed;

import java.io.IOException;
import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;
import java.security.Permission;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.drools.grid.DirectoryNodeService;
import org.drools.builder.DirectoryLookupFactoryService;
import java.util.ArrayList;
import java.util.List;
import net.jini.discovery.DiscoveryGroupManagement;
import org.drools.KnowledgeBaseFactoryService;
import org.drools.builder.KnowledgeBuilderFactoryService;
import org.drools.grid.ExecutionNode;
import org.drools.grid.ExecutionNodeService;
import org.drools.grid.NodeSelectionStrategy;
import org.drools.grid.generic.GenericConnection;
import org.drools.grid.generic.GenericNodeConnector;
import org.drools.grid.strategies.DirectoryServiceSelectionStrategy;
import org.drools.grid.strategies.StaticIncrementalSelectionStrategy;
import org.rioproject.associations.AssociationDescriptor;
import org.rioproject.associations.AssociationMgmt;

/**
 *
 * @author salaboy
 */
public class GridConnection implements GenericConnection {

    static {
        System.setSecurityManager(new RMISecurityManager() {

            @Override
            public void checkPermission(Permission perm) {
            }
        });

    }
    //Cached Services
    private List<GenericNodeConnector> nodeConnectors;
    //Cached Directories
    private List<DirectoryNodeService> directories;

    public GridConnection() {
        this.nodeConnectors = new ArrayList<GenericNodeConnector>();
        this.directories = new ArrayList<DirectoryNodeService>();
    }

    public void addNodeConnector(GenericNodeConnector service) {

        this.nodeConnectors.add(service);

    }

    public void addDirectoryNode(DirectoryNodeService directory) {
        this.directories.add(directory);
    }

    // In real scenarios this method will be in charge of populating
    // all the ExecutionNodeService and DirectoryServices
    public boolean connect() throws IOException, RemoteException, InterruptedException {
        Logger.getLogger("org.rioproject.associations").setLevel(Level.FINEST);
        AssociationDescriptor descriptorExecutionNode = AssociationDescriptor.create("ExecutionNodeService",
                "nodeConnectorsInj",
                org.drools.grid.ExecutionNodeService.class,
                DiscoveryGroupManagement.ALL_GROUPS);
        AssociationDescriptor descriptorDirectoryNode = AssociationDescriptor.create("DirectoryNodeService",
                "directoriesInj",
                org.drools.grid.DirectoryNodeService.class,
                DiscoveryGroupManagement.ALL_GROUPS);
        /*
         * Create and configure association management. Make sure to set
         * the backend, this is the object that has the setter method provided
         * above. For our case its "this".
         */
        AssociationMgmt aMgr = new AssociationMgmt();
        aMgr.setBackend(this);
        aMgr.addAssociationDescriptors(descriptorExecutionNode, descriptorDirectoryNode);

        

        Iterable<org.drools.grid.ExecutionNodeService> executionNodes = aMgr.getAssociations("ExecutionNodeService", null)[0];
        for (ExecutionNodeService executionNodeService : executionNodes) {
            addNodeConnector(executionNodeService);
        }

        Iterable<org.drools.grid.DirectoryNodeService> directoryNodes = aMgr.getAssociations("DirectoryNodeService", null)[0];
        for (DirectoryNodeService directoryNodeService : directoryNodes) {
            addDirectoryNode(directoryNodeService);
        }

        long waited = 0;
        while (nodeConnectors.size() == 0 && waited < 30000) {
            Thread.sleep(500);
            waited += 500;
        }

        if (nodeConnectors.size() > 0 && directories.size() > 0) {
            return true;
        }



        return false;
    }

    @Override
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
                node.set(KnowledgeBuilderFactoryService.class,
                        new KnowledgeBuilderProviderGridClient(currentNode, this));
                node.set(KnowledgeBaseFactoryService.class, new KnowledgeBaseProviderGridClient(currentNode, this));
                node.set(DirectoryLookupFactoryService.class, new DirectoryLookupProviderGridClient(currentNode, this));
            }

        } catch (RemoteException ex) {
            Logger.getLogger(GridConnection.class.getName()).log(Level.SEVERE, null, ex);
        }
        return node;
    }

    public GenericNodeConnector getBestNode(NodeSelectionStrategy nodeSelectionStrategy) {
        return nodeSelectionStrategy.getBestNode();


    }

    public void setNodeConnectorsInj(Iterable<ExecutionNodeService> nodes) {
        for (ExecutionNodeService node : nodes) {
            addNodeConnector(node);
        }


    }

    public void setDirectoriesInj(Iterable<DirectoryNodeService> nodes) {
        for (DirectoryNodeService node : nodes) {
            addDirectoryNode(node);
        }


    }

    public List<DirectoryNodeService> getDirectories() {
        return (List<DirectoryNodeService>) directories;


    }

    public void setDirectories(List<DirectoryNodeService> directories) {
        this.directories = directories;


    }

    public List<GenericNodeConnector> getNodeConnectors() {
        return nodeConnectors;


    }

    public void setNodeConnectors(List<GenericNodeConnector> services) {
        this.nodeConnectors = services;


    }

    @Override
    public DirectoryNodeService getDirectoryNode(DirectoryServiceSelectionStrategy directorySelectionStrategy) {
        return ((List<DirectoryNodeService>) directories).get(0);


    }

    @Override
    public ExecutionNode getExecutionNode() {
        return getExecutionNode(null);

    }
}
