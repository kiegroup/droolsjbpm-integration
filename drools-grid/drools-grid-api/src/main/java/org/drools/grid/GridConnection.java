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

import org.drools.grid.strategies.NodeSelectionStrategy;
import org.drools.grid.strategies.ReturnAlwaysTheFirstSelectionStrategy;

/**
 *
 * @author salaboy
 */
public class GridConnection
    implements
    GenericConnection {

    private List<GenericNodeConnector> executionNodeConnectors;
    private List<GenericNodeConnector> directoryNodeConnectors;
    private List<GenericNodeConnector> humanTaskNodeConnectors;
    private NodeSelectionStrategy      defaultStrategy = new ReturnAlwaysTheFirstSelectionStrategy();

    public GridConnection() {
        this.executionNodeConnectors = new ArrayList<GenericNodeConnector>();
        this.directoryNodeConnectors = new ArrayList<GenericNodeConnector>();
        this.humanTaskNodeConnectors = new ArrayList<GenericNodeConnector>();
    }

    public void addExecutionNode(GenericNodeConnector execNodeConnector) {
        this.executionNodeConnectors.add( execNodeConnector );
    }

    public void addDirectoryNode(GenericNodeConnector directoryNodeConnector) {
        this.directoryNodeConnectors.add( directoryNodeConnector );

    }

    public void addHumanTaskNode(GenericNodeConnector humanTaskNodeConnector) {
        this.humanTaskNodeConnectors.add( humanTaskNodeConnector );
    }

    /**
     * @throws ConnectorException 
     * @throws IllegalStateException if unable to connect to node 
     */
    public ExecutionNode getExecutionNode(NodeSelectionStrategy strategy) throws ConnectorException {
        ExecutionNode node = null;
        GenericNodeConnector connector = null;

        connector = getBestNode( strategy );

        NodeConnectionType type;
        try {
            type = connector.getNodeConnectionType();

            type.setConnector( connector );
            type.setConnection( this );

            node = NodeFactory.newExecutionNode( type );
        } catch ( RemoteException ex ) {
            Logger.getLogger( GridConnection.class.getName() ).log( Level.SEVERE,
                                                                    null,
                                                                    ex );
        }

        return node;
    }

    public ExecutionNode getExecutionNode() throws ConnectorException {
        return getExecutionNode( this.defaultStrategy );
    }

    public DirectoryNode getDirectoryNode(NodeSelectionStrategy strategy) throws ConnectorException {

        GenericNodeConnector connector = null;

        connector = getBestDirectory( strategy );

        NodeConnectionType type;
        DirectoryNode directoryNode = null;
        try {
            type = connector.getNodeConnectionType();

            type.setConnector( connector );
            type.setConnection( this );

            directoryNode = NodeFactory.newDirectoryNode( type );
        } catch ( RemoteException ex ) {
            Logger.getLogger( GridConnection.class.getName() ).log( Level.SEVERE,
                                                                    null,
                                                                    ex );
        }

        return directoryNode;
    }

    public DirectoryNode getDirectoryNode() throws ConnectorException {
        return getDirectoryNode( this.defaultStrategy );
    }

    public HumanTaskNode getHumanTaskNode(NodeSelectionStrategy strategy) throws ConnectorException {

        GenericNodeConnector connector = null;

        connector = getBestHumanTask( strategy );

        NodeConnectionType type;
        HumanTaskNode humanTaskNode = null;
        try {
            type = connector.getNodeConnectionType();
            connector.connect();
            type.setConnector( connector );
            type.setConnection( this );

            humanTaskNode = NodeFactory.newHumanTaskNode( type );
        } catch ( RemoteException ex ) {
            Logger.getLogger( GridConnection.class.getName() ).log( Level.SEVERE,
                                                                    null,
                                                                    ex );
        }

        return humanTaskNode;

    }

    public HumanTaskNode getHumanTaskNode() throws ConnectorException {
        return getHumanTaskNode( this.defaultStrategy );
    }

    public List<ExecutionNode> getExecutionNodes() throws ConnectorException {
        List<ExecutionNode> executionNodes = new ArrayList<ExecutionNode>();
        for ( GenericNodeConnector connector : this.executionNodeConnectors ) {
            NodeConnectionType type;
            try {
                type = connector.getNodeConnectionType();

                type.setConnector( connector );
                type.setConnection( this );
                executionNodes.add( NodeFactory.newExecutionNode( type ) );
            } catch ( RemoteException ex ) {
                Logger.getLogger( GridConnection.class.getName() ).log( Level.SEVERE,
                                                                        null,
                                                                        ex );
            }
        }
        return executionNodes;
    }

    public List<DirectoryNode> getDirectoryNodes() throws ConnectorException {
        List<DirectoryNode> directoryNodes = new ArrayList<DirectoryNode>();
        for ( GenericNodeConnector connector : this.directoryNodeConnectors ) {
            NodeConnectionType type;
            try {
                type = connector.getNodeConnectionType();

                type.setConnector( connector );
                type.setConnection( this );
                directoryNodes.add( NodeFactory.newDirectoryNode( type ) );
            } catch ( RemoteException ex ) {
                Logger.getLogger( GridConnection.class.getName() ).log( Level.SEVERE,
                                                                        null,
                                                                        ex );
            }
        }
        return directoryNodes;

    }

    public List<HumanTaskNode> getHumanTaskNodes() throws ConnectorException {
        List<HumanTaskNode> humanTaskNodes = new ArrayList<HumanTaskNode>();
        for ( GenericNodeConnector connector : this.humanTaskNodeConnectors ) {
            NodeConnectionType type;
            try {
                type = connector.getNodeConnectionType();

                type.setConnector( connector );
                type.setConnection( this );
                humanTaskNodes.add( NodeFactory.newHumanTaskNode( type ) );
            } catch ( RemoteException ex ) {
                Logger.getLogger( GridConnection.class.getName() ).log( Level.SEVERE,
                                                                        null,
                                                                        ex );
            }
        }
        return humanTaskNodes;
    }

    public void dispose() throws ConnectorException {
        for ( GenericNodeConnector connector : this.executionNodeConnectors ) {
            try {
                connector.disconnect();

            } catch ( RemoteException ex ) {
                Logger.getLogger( GridConnection.class.getName() ).log( Level.SEVERE,
                                                                        null,
                                                                        ex );
            }
        }
        for ( GenericNodeConnector connector : this.directoryNodeConnectors ) {
            try {
                connector.disconnect();

            } catch ( RemoteException ex ) {
                Logger.getLogger( GridConnection.class.getName() ).log( Level.SEVERE,
                                                                        null,
                                                                        ex );
            }
        }
        for ( GenericNodeConnector connector : this.humanTaskNodeConnectors ) {
            try {
                connector.disconnect();

            } catch ( RemoteException ex ) {
                Logger.getLogger( GridConnection.class.getName() ).log( Level.SEVERE,
                                                                        null,
                                                                        ex );
            }
        }

    }

    private GenericNodeConnector getBestNode(NodeSelectionStrategy nodeSelectionStrategy) {
        return nodeSelectionStrategy.getBestNode( this.executionNodeConnectors );
    }

    private GenericNodeConnector getBestDirectory(NodeSelectionStrategy directorySelectionStrategy) {
        return directorySelectionStrategy.getBestNode( this.directoryNodeConnectors );
    }

    private GenericNodeConnector getBestHumanTask(NodeSelectionStrategy humanTaskSelectionStrategy) {
        return humanTaskSelectionStrategy.getBestNode( this.humanTaskNodeConnectors );
    }
}
