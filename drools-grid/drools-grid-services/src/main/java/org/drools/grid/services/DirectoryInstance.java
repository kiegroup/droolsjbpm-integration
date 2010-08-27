package org.drools.grid.services;

import java.util.List;

import org.drools.grid.ConnectorException;
import org.drools.grid.DirectoryNode;
import org.drools.grid.GenericConnection;
import org.drools.grid.GenericNodeConnector;
import org.drools.grid.strategies.NodeSelectionStrategy;
import org.drools.grid.strategies.ReturnAlwaysTheFirstSelectionStrategy;

/**
 * @author salaboy
 *
 * The DirectoryInstance class represent a remote/distributed Directory Service.
 * Depending on the underlaying implementation each DirectoryInstance can encapsulate
 * one or a set of Directory Nodes.
 *
 */
public class DirectoryInstance {

    private String                name;
    private GenericNodeConnector  connector;
    private NodeSelectionStrategy defaultStrategy = new ReturnAlwaysTheFirstSelectionStrategy();

    /*
     * Creates a new DirectoryInstance using a name associated with it and a
     * GenericNodeConnector that will be used to establish the remote/distribtued
     * communication.
     * @param name
     * @param connector
     */
    public DirectoryInstance(String name,
                             GenericNodeConnector connector) {
        this.name = name;
        this.connector = connector;
    }

    /*
     * Get a DirectoryNode based on the default NodeSelectionStrategy
     */
    public DirectoryNode getDirectoryNode() throws ConnectorException {
        return getDirectoryNode( this.defaultStrategy );
    }

    /*
     * Get a DirectoryNode based on the provided NodeSelectionStrategy
     */
    public DirectoryNode getDirectoryNode(NodeSelectionStrategy strategy) throws ConnectorException {
        GenericConnection connection = getConnector().getConnection();
        return connection.getDirectoryNode( strategy );
    }

    /*
     * Get all the DirectoryNodes available from the DirectoryInstance. This can be
     * expensive because it needs to be able to connect to all the services.
     */

    public List<DirectoryNode> getDirectoryNodes() throws ConnectorException {
        GenericConnection connection = getConnector().getConnection();
        return connection.getDirectoryNodes();
    }

    /*
     * Get the DirectoryInstance connector
     */
    public GenericNodeConnector getConnector() {
        return this.connector;
    }

    /*
     * Get the DirectoryInstance name
     */
    public String getName() {
        return this.name;
    }
}
