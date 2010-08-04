package org.drools.grid.services;

import java.util.List;
import org.drools.grid.services.strategies.ReturnFirstHumanTaskServiceSelectionStrategy;
import org.drools.grid.ConnectorException;
import org.drools.grid.GenericConnection;
import org.drools.grid.GenericHumanTaskConnector;
import org.drools.grid.strategies.HumanTaskSelectionStrategy;
import org.drools.grid.task.HumanTaskService;
/**
 * @author salaboy
 */
public class TaskServerInstance {

    private String name;
    private GenericHumanTaskConnector connector;

    public TaskServerInstance(String name, GenericHumanTaskConnector connector) {
        this.name = name;
        this.connector = connector;
    }

    public HumanTaskService getTaskClient() throws ConnectorException {
        GenericConnection connection = getConnector().getConnection();
        HumanTaskService htService = (HumanTaskService) connection.getHumanTaskNode(new ReturnFirstHumanTaskServiceSelectionStrategy(1, getConnector()));
        
        return htService;
    }

     public HumanTaskService getTaskClient(HumanTaskSelectionStrategy strategy) throws ConnectorException {
        GenericConnection connection = getConnector().getConnection();
        HumanTaskService htService = (HumanTaskService) connection.getHumanTaskNode(strategy);
        return htService;
    }

    public List<HumanTaskService> getTaskClients() {
        GenericConnection connection = getConnector().getConnection();
        return null;
    }

    public GenericHumanTaskConnector getConnector(){
        return this.connector;
    }


    public String getName() {
        return name;
    }

    
}
