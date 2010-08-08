package org.drools.grid.services;

import java.util.List;
import org.drools.grid.ConnectorException;
import org.drools.grid.GenericConnection;
import org.drools.grid.GenericNodeConnector;
import org.drools.grid.HumanTaskNode;
import org.drools.grid.strategies.NodeSelectionStrategy;
import org.drools.grid.strategies.ReturnAlwaysTheFirstSelectionStrategy;

/**
 * @author salaboy
 *
 * This class represent a remote/distributed Task Server service that will let us
 * execute and manage Human Tasks associated with business processes.
 * As ExecutionEnvironment and DirectoryInstance this class can encapsulate one
 * or a set of HumanTaskNodes based on the underlaying implementation.
 *
 */
public class TaskServerInstance {

    private String name;
    private GenericNodeConnector connector;
    private NodeSelectionStrategy defaultStrategy = new ReturnAlwaysTheFirstSelectionStrategy();

    /*
     * Creates a new TaskServer Instance that will be associated to a name using the
     * GenericNodeConnector provided.
     */
    public TaskServerInstance(String name, GenericNodeConnector connector) {
        this.name = name;
        this.connector = connector;
    }

    public HumanTaskNode getHumanTaskNode() throws ConnectorException {
        return getHumanTaskNode(defaultStrategy);
    }

    public HumanTaskNode getHumanTaskNode(NodeSelectionStrategy strategy) throws ConnectorException{
        GenericConnection connection = getConnector().getConnection();
        return connection.getHumanTaskNode(strategy);
    }

    public List<HumanTaskNode> getHumanTaskNodes() throws ConnectorException{
        GenericConnection connection = getConnector().getConnection();
        return connection.getHumanTaskNodes();
    }


    public GenericNodeConnector getConnector(){
        return this.connector;
    }

    public String getName() {
        return name;
    }

    
}
