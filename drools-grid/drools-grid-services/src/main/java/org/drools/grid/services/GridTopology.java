package org.drools.grid.services;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.drools.grid.ConnectorException;
import org.drools.grid.DirectoryNodeService;
import org.drools.grid.GenericConnection;
import org.drools.grid.GenericHumanTaskConnector;
import org.drools.grid.GenericNodeConnector;
import org.drools.grid.services.configuration.GenericProvider;
import org.drools.grid.services.factory.DirectoryInstanceFactory;
import org.drools.grid.services.factory.ExecutionEnvironmentFactory;
import org.drools.grid.services.factory.TaskServerInstanceFactory;
import org.drools.grid.services.strategies.DirectoryInstanceByPrioritySelectionStrategy;
import org.drools.grid.services.strategies.DirectoryInstanceSelectionStrategy;
import org.drools.grid.services.strategies.ExecutionEnvByPrioritySelectionStrategy;
import org.drools.grid.services.strategies.ExecutionEnvironmentSelectionStrategy;
import org.drools.grid.services.strategies.TaskServerInstanceByPrioritySelectionStrategy;
import org.drools.grid.services.strategies.TaskServerInstanceSelectionStrategy;

/**
 * @author salaboy  
 */
public class GridTopology {

    private String topologyName;
    private Map<String, ExecutionEnvironment> executionEnvironments = new HashMap<String, ExecutionEnvironment>();
    private Map<String, String> executionEnvironmentsByConnectorId = new HashMap<String, String>();
    private Map<String, DirectoryInstance> directories = new HashMap<String, DirectoryInstance>();
    private Map<String, TaskServerInstance> taskServerInstance = new HashMap<String, TaskServerInstance>();

    private final ExecutionEnvironmentSelectionStrategy DEFAULT_EXECTUTION_STRATEGY = new ExecutionEnvByPrioritySelectionStrategy();
    private final DirectoryInstanceSelectionStrategy DEFAULT_DIRECTORY_STRATEGY = new DirectoryInstanceByPrioritySelectionStrategy();
    private final TaskServerInstanceSelectionStrategy DEFAULT_TASK_STRATEGY = new TaskServerInstanceByPrioritySelectionStrategy();
   

    public GridTopology(String topologyName) {
        this.topologyName = topologyName;

    }

    public String getTopologyName() {
        return topologyName;
    }

    //Execution Environments Methods
    public void registerExecutionEnvironment(String name, GenericProvider provider) {
        //Create the executionEnvironment using the provider

        ExecutionEnvironment environment = ExecutionEnvironmentFactory.newExecutionEnvironment(name, provider);
        //Get the connector
        GenericNodeConnector connector = environment.getConnector();
        //Get the connection
        GenericConnection connection = connector.getConnection();
        //Adding the connector to the conection collection of connectors
        connection.addExecutionNode(connector);
        //We need to add all the other exec envs connectors inside this connection
        for (ExecutionEnvironment e : executionEnvironments.values()) {
            connection.addExecutionNode(e.getConnector());
            //I need to add this execution node to all the other EE
            e.getConnector().getConnection().addExecutionNode(connector);
        }
        //We need to add all the other directory connectors inside this connection
        for (DirectoryInstance d : directories.values()) {
            connection.addDirectoryNode(d.getConnector());
            //I need to add this execution node to all the other DI
            d.getConnector().getConnection().addExecutionNode(connector);
        }
        //Adding the env to the local cache
        executionEnvironments.put(name, environment);
        try {
            executionEnvironmentsByConnectorId.put(connector.getId(), environment.getName());
        } catch (ConnectorException ex) {
            Logger.getLogger(GridTopology.class.getName()).log(Level.SEVERE, null, ex);
        } catch (RemoteException ex) {
            Logger.getLogger(GridTopology.class.getName()).log(Level.SEVERE, null, ex);
        }
        //Register all the Execution Environments into the current directories
        registerResourceInCurrentDirectories(name, provider.getId());

    }

    public void unregisterExecutionEnvironment(String name) {
        //Remove Execution Environment
        executionEnvironments.remove(name);
        //UnRegister EE from current Directories
        unregisterResourceInCurrentDirectories(name);


    }

    public ExecutionEnvironment getExecutionEnvironment(String name) {
        return executionEnvironments.get(name);
    }

    public ExecutionEnvironment getExecutionEnvironment(GenericNodeConnector connector){
        ExecutionEnvironment ee = null;
        try {
             String eeName = executionEnvironmentsByConnectorId.get(connector.getId());
             ee = executionEnvironments.get(eeName);
        } catch (ConnectorException ex) {
            Logger.getLogger(GridTopology.class.getName()).log(Level.SEVERE, null, ex);
        } catch (RemoteException ex) {
            Logger.getLogger(GridTopology.class.getName()).log(Level.SEVERE, null, ex);
        }
        return ee;
    }

    public ExecutionEnvironment getBestExecutionEnvironment(ExecutionEnvironmentSelectionStrategy strategy) {
        return strategy.getBestExecutionEnvironment(executionEnvironments);
    }

    public ExecutionEnvironment getExecutionEnvironment() {
        return DEFAULT_EXECTUTION_STRATEGY.getBestExecutionEnvironment(executionEnvironments);
    }

    // DirectoryInstances Methods
    public void registerDirectoryInstance(String name, GenericProvider provider) {

        DirectoryInstance directory = DirectoryInstanceFactory.newDirectoryInstance(name, provider);

        GenericNodeConnector connector = directory.getConnector();
        GenericConnection connection = connector.getConnection();


        connection.addDirectoryNode(connector);

        for (ExecutionEnvironment e : executionEnvironments.values()) {
            connection.addExecutionNode(e.getConnector());
            //I need to add this directory instance to all the other EE
            e.getConnector().getConnection().addDirectoryNode(connector);
        }
        for (DirectoryInstance d : directories.values()) {
            connection.addDirectoryNode(d.getConnector());
            //I need to add this directory Instance to all the other DI
            d.getConnector().getConnection().addDirectoryNode(connector);
        }

        
        directories.put(name, directory);
        registerResourceInCurrentDirectories(name, provider.getId());

    }

    public DirectoryInstance getBestDirectoryInstance(DirectoryInstanceSelectionStrategy strategy) {
        return (DirectoryInstance) strategy.getBestDirectoryInstance(directories);
    }

    public DirectoryInstance getDirectoryInstance() {
        return DEFAULT_DIRECTORY_STRATEGY.getBestDirectoryInstance(directories);
    }

    public DirectoryInstance getDirectoryInstance(String name) {
        return directories.get(name);
    }

     public void unregisterDirectoryInstance(String name) {
        //Remove Directory Instance
        directories.remove(name);
        //UnRegister Directory from current Directories
        unregisterResourceInCurrentDirectories(name);


    }


    // Task Server Instance Methods

    public void registerTaskServerInstance(String name, GenericProvider provider) {

        TaskServerInstance taskServer = TaskServerInstanceFactory.newTaskServerInstance(name, provider);
        GenericHumanTaskConnector connector = taskServer.getConnector();
        GenericConnection connection = connector.getConnection();
        connection.addHumanTaskNode(connector);
        
        taskServerInstance.put(name, taskServer);
        registerResourceInCurrentDirectories(name, provider.getId());

    }

    
   

    public TaskServerInstance getTaskServerInstance(String name) {
        return taskServerInstance.get(name);
    }

    public TaskServerInstance getBestTaskServerInstance(TaskServerInstanceSelectionStrategy strategy) {
        return (TaskServerInstance) strategy.getBestTaskServerInstance(taskServerInstance);
    }

    public TaskServerInstance getBestTaskServerInstance() {
        return DEFAULT_TASK_STRATEGY.getBestTaskServerInstance(taskServerInstance);
    }

    public void unregisterTaskServerInstance(String name) {
        //Remove Task Server Instance
        taskServerInstance.remove(name);
        //UnRegister task Server instance from current Directories
        unregisterResourceInCurrentDirectories(name);


    }

    

    public void dispose() throws ConnectorException, RemoteException {
        
        for (String key : executionEnvironments.keySet()) {
            executionEnvironments.get(key).getConnector().disconnect();
            executionEnvironments.get(key).getConnector().getConnection().dispose();
        }
        for (String key : directories.keySet()) {
            directories.get(key).getConnector().disconnect();
            directories.get(key).getConnector().getConnection().dispose();
        }
        for (String key : taskServerInstance.keySet()) {
            taskServerInstance.get(key).getConnector().disconnect();
            taskServerInstance.get(key).getConnector().getConnection().dispose();
        }
    }

    private void registerResourceInCurrentDirectories(String name, String resourceId) {
        for (DirectoryInstance directory : directories.values()) {

            try {
                DirectoryNodeService directoryNode = directory.getDirectoryService().get(DirectoryNodeService.class);
                if (directoryNode != null) {
                    try {
                        directoryNode.register(name, resourceId);
                    } catch (RemoteException ex) {
                        Logger.getLogger(GridTopology.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                try {
                    directory.getConnector().disconnect();
                } catch (RemoteException ex) {
                    Logger.getLogger(GridTopology.class.getName()).log(Level.SEVERE, null, ex);
                }
            } catch (ConnectorException e) {
                Logger.getLogger(GridTopology.class.getName()).log(Level.SEVERE, null, e);
            }
        }
    }

    private void unregisterResourceInCurrentDirectories(String name) {
        for (DirectoryInstance directory : directories.values()) {

            try {
                DirectoryNodeService directoryNode = directory.getDirectoryService().get(DirectoryNodeService.class);
                if (directoryNode != null) {
                    try {
                        directoryNode.unregister(name);
                    } catch (RemoteException ex) {
                        Logger.getLogger(GridTopology.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                try {
                    directory.getConnector().disconnect();
                } catch (RemoteException ex) {
                    Logger.getLogger(GridTopology.class.getName()).log(Level.SEVERE, null, ex);
                }
            } catch (ConnectorException e) {
                Logger.getLogger(GridTopology.class.getName()).log(Level.SEVERE, null, e);
            }
        }
    }
}
