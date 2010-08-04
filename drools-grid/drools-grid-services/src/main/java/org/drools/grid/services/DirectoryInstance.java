package org.drools.grid.services;

import java.util.List;

import org.drools.grid.ConnectorException;
import org.drools.grid.DirectoryNode;
import org.drools.grid.GenericConnection;
import org.drools.grid.GenericNodeConnector;
import org.drools.grid.strategies.DirectorySelectionStrategy;

/**
 * @author salaboy
 */
public class DirectoryInstance {
    private String name;
    private GenericNodeConnector connector;

    public DirectoryInstance(String name, GenericNodeConnector connector) {
        this.name = name;
        this.connector = connector;
    }

    public DirectoryNode getDirectoryService() throws ConnectorException {
        GenericConnection connection = getConnector().getConnection();
        return connection.getDirectoryNode();
    }

    public DirectoryNode getDirectoryService(DirectorySelectionStrategy strategy) throws ConnectorException {
        GenericConnection connection = getConnector().getConnection();
        return connection.getDirectoryNode(strategy);
    }

    public List<DirectoryNode> getDirectoryServices() throws ConnectorException {
        GenericConnection connection = getConnector().getConnection();
        return connection.getDirectoryNodes();
    }

     public GenericNodeConnector getConnector(){
        return this.connector;
    }


    public String getName() {
        return name;
    }

    
}
