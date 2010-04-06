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

import java.util.ArrayList;
import java.util.List;
import org.drools.KnowledgeBaseFactoryService;
import org.drools.builder.DirectoryLookupFactoryService;
import org.drools.builder.KnowledgeBuilderFactoryService;
import org.drools.grid.DirectoryNodeService;
import org.drools.grid.ExecutionNode;
import org.drools.grid.NodeSelectionStrategy;
import org.drools.grid.generic.GenericConnection;
import org.drools.grid.generic.GenericNodeConnector;
import org.drools.grid.strategies.DirectoryServiceSelectionStrategy;

/**
 *
 * @author salaboy
 */
public class LocalConnection implements GenericConnection {

     //Cached NodeConnectors
    private List<GenericNodeConnector> nodeConnectors;


    public LocalConnection() {
        this.nodeConnectors = new ArrayList<GenericNodeConnector>();

    }


    public void addNodeConnector(GenericNodeConnector nodeConnector) {
        this.nodeConnectors.add(nodeConnector);
    }

    public void addDirectoryNode(DirectoryNodeService directory) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public List<GenericNodeConnector> getNodeConnectors() {
        return this.nodeConnectors;
    }

    public List<DirectoryNodeService> getDirectories() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public GenericNodeConnector getBestNode(NodeSelectionStrategy nodeSelectionStrategy) {
        //Analyze if we will need more than one node connector for local usage in the same JVM
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public DirectoryNodeService getDirectoryNode(DirectoryServiceSelectionStrategy directorySelectionStrategy) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public ExecutionNode getExecutionNode(NodeSelectionStrategy strategy) {

        ExecutionNode node = new ExecutionNode();
        node.set(KnowledgeBuilderFactoryService.class, new KnowledgeBuilderProviderLocalClient() );
        node.set(KnowledgeBaseFactoryService.class, new KnowledgeBaseProviderLocalClient() );
        node.set(DirectoryLookupFactoryService.class, new DirectoryLookupProviderLocalClient());
        return node;
    }

    public ExecutionNode getExecutionNode() {
        return getExecutionNode(null);
    }

}
