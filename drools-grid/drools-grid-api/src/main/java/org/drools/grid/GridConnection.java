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

import org.drools.grid.strategies.DirectorySelectionStrategy;
import org.drools.grid.strategies.HumanTaskSelectionStrategy;
import org.drools.grid.strategies.NodeSelectionStrategy;
import org.drools.grid.strategies.StaticIncrementalDirectorySelectionStrategy;
import org.drools.grid.strategies.StaticIncrementalNodeSelectionStrategy;

/**
 *
 * @author salaboy
 */
public class GridConnection implements GenericConnection {

    private List<GenericNodeConnector> executionNodeConnectors;
    private List<GenericNodeConnector> directoryNodeConnectors;
    private List<GenericHumanTaskConnector> humanTaskNodeConnectors;

    public GridConnection() {
        this.executionNodeConnectors = new ArrayList<GenericNodeConnector>();
        this.directoryNodeConnectors = new ArrayList<GenericNodeConnector>();
        this.humanTaskNodeConnectors = new ArrayList<GenericHumanTaskConnector>();
    }

    public void addExecutionNode(GenericNodeConnector execNodeConnector) {
        this.executionNodeConnectors.add(execNodeConnector);
    }

    public void addDirectoryNode(GenericNodeConnector directoryNodeConnector) {
        this.directoryNodeConnectors.add(directoryNodeConnector);

    }

    public void addHumanTaskNode(GenericHumanTaskConnector humanTaskNodeConnector) {
        this.humanTaskNodeConnectors.add(humanTaskNodeConnector);
    }

    /**
     * @throws ConnectorException 
     * @throws IllegalStateException if unable to connect to node 
     */
    public ExecutionNode getExecutionNode(NodeSelectionStrategy strategy) throws ConnectorException {
        ExecutionNode node = null;
        GenericNodeConnector connector = null;
        //if the strategy is null use the default one
        if (strategy == null) {
            connector = getBestNode(new StaticIncrementalNodeSelectionStrategy());
        } else {
            connector = getBestNode(strategy);
        }

        NodeConnectionType type;
        try {
            type = connector.getNodeConnectionType();
            connector.connect();

            type.setConnector(connector);
            type.setConnection(this);

            node = NodeFactory.newExecutionNode(type);
        } catch (RemoteException ex) {
            Logger.getLogger(GridConnection.class.getName()).log(Level.SEVERE, null, ex);
        }



        return node;
    }

    public DirectoryNode getDirectoryNode(DirectorySelectionStrategy strategy) throws ConnectorException {


        GenericNodeConnector connector = null;
        //if the strategy is null use the default one


        if (strategy == null) {
            connector = getBestDirectory(new StaticIncrementalDirectorySelectionStrategy());
        } else {
            connector = getBestDirectory(strategy);
        }


        NodeConnectionType type;
        DirectoryNode directoryNode = null;
        try {
            type = connector.getNodeConnectionType();

            connector.connect();

            type.setConnector(connector);
            type.setConnection(this);

            directoryNode = NodeFactory.newDirectoryNode(type);
        } catch (RemoteException ex) {
            Logger.getLogger(GridConnection.class.getName()).log(Level.SEVERE, null, ex);
        }


        return directoryNode;
    }

    public DirectoryNode getDirectoryNode() throws ConnectorException {
        return getDirectoryNode(null);
    }

    public ExecutionNode getExecutionNode() throws ConnectorException {
        return getExecutionNode(null);
    }

    public HumanTaskNodeService getHumanTaskNode(HumanTaskSelectionStrategy humanTaskSelectionStrategy) throws ConnectorException {
        if (humanTaskNodeConnectors.isEmpty()) {
            return null;
        }
        // humanTaskSelectionStrategy.getBestHumanTask(humanTaskNodeConnectors);
        GenericHumanTaskConnector connector = humanTaskNodeConnectors.get(0);

        connector.connect();

        HumanTaskNodeService humanTaskNode = connector.getHumanTaskNodeService();

        return humanTaskNode;

    }

    public HumanTaskNodeService getHumanTaskNode() throws ConnectorException {
        return getHumanTaskNode(null);
    }

    public List<ExecutionNode> getExecutionNodes() throws ConnectorException {
        List<ExecutionNode> executionNodes = new ArrayList<ExecutionNode>();
        for (GenericNodeConnector connector : executionNodeConnectors) {
            NodeConnectionType type;
            try {
                type = connector.getNodeConnectionType();

                connector.connect();

                type.setConnector(connector);
                type.setConnection(this);
                executionNodes.add(NodeFactory.newExecutionNode(type));
            } catch (RemoteException ex) {
                Logger.getLogger(GridConnection.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return executionNodes;
    }

    public List<DirectoryNode> getDirectoryNodes() throws ConnectorException {
        List<DirectoryNode> directoryNodes = new ArrayList<DirectoryNode>();
        for (GenericNodeConnector connector : directoryNodeConnectors) {
            NodeConnectionType type;
            try {
                type = connector.getNodeConnectionType();

                connector.connect();
                type.setConnector(connector);
                type.setConnection(this);
                directoryNodes.add(NodeFactory.newDirectoryNode(type));
            } catch (RemoteException ex) {
                Logger.getLogger(GridConnection.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return directoryNodes;

    }

    public List<HumanTaskNodeService> getHumanTaskNodes() {
        throw new UnsupportedOperationException("not Implemented yet!");
    }

    public void dispose() throws ConnectorException {
        for (GenericNodeConnector connector : executionNodeConnectors) {
            try {
                connector.disconnect();
            } catch (RemoteException ex) {
                Logger.getLogger(GridConnection.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        for (GenericNodeConnector connector : directoryNodeConnectors) {
            try {
                connector.disconnect();
            } catch (RemoteException ex) {
                Logger.getLogger(GridConnection.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        for (GenericHumanTaskConnector connector : humanTaskNodeConnectors) {
            connector.disconnect();
        }

    }

    private GenericNodeConnector getBestNode(NodeSelectionStrategy nodeSelectionStrategy) {
        return nodeSelectionStrategy.getBestNode(this.executionNodeConnectors);
    }

    private GenericNodeConnector getBestDirectory(DirectorySelectionStrategy directorySelectionStrategy) {
        return directorySelectionStrategy.getBestDirectory(this.directoryNodeConnectors);
    }

    private GenericHumanTaskConnector getBestHumanTask(HumanTaskSelectionStrategy humanTaskSelectionStrategy) {
        return humanTaskSelectionStrategy.getBestHumanTask(this.humanTaskNodeConnectors);
    }
}
