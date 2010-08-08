package org.drools.grid.services;

import java.rmi.RemoteException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
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
 *
 * This class will represent a Grid Topology where we can execute and manager our
 * knowledge sessions.
 * A Grid Topology can be conformed using different types of nodes/resources depending on
 * your business requirements and architecture.
 * The grid topology can contains a set of the following type of nodes:
 *   - Execution Environments: we will use them to host knowledge sessions (runtime)
 *   - Directory Instances: we will use them to keep track of the current nodes in our topology
 *   - Task Servers Instance: we will use them to execute and manage Human Tasks for business processes
 *
 * From the user perspective the GridTopology will be normally constructed with a HelperClass called: GridTopologyFactory
 * This helper class will be in charge of reading the GridTopology configuration and based on that
 * create the GridTopology object with all the nodes registered.
 *
 * It's important to understand that the GridTopologyConfiguration will represent a static description/configuration of a
 * runtime environment. Based on this static description/configuration the GridTopology object will represent a
 * live status of this runtime environment.
 *
 *
 *
 */
public class GridTopology {

    private String topologyName;
    private Map<String, ExecutionEnvironment> executionEnvironments = new ConcurrentHashMap<String, ExecutionEnvironment>();
    private Map<String, String> executionEnvironmentsByConnectorId = new ConcurrentHashMap<String, String>();
    private Map<String, DirectoryInstance> directoryInstances = new ConcurrentHashMap<String, DirectoryInstance>();
    private Map<String, TaskServerInstance> taskServerInstances = new ConcurrentHashMap<String, TaskServerInstance>();
    private final ExecutionEnvironmentSelectionStrategy DEFAULT_EXECTUTION_STRATEGY = new ExecutionEnvByPrioritySelectionStrategy();
    private final DirectoryInstanceSelectionStrategy DEFAULT_DIRECTORY_STRATEGY = new DirectoryInstanceByPrioritySelectionStrategy();
    private final TaskServerInstanceSelectionStrategy DEFAULT_TASK_STRATEGY = new TaskServerInstanceByPrioritySelectionStrategy();


    /*
     * Create a new Grid Topology
     * @param topologyName
     */
    public GridTopology(String topologyName) {
        this.topologyName = topologyName;

    }

    /*
     * Get the GridTopology name
     */
    public String getTopologyName() {
        return topologyName;
    }

    /*
     * This method will register a new Execution Environment based on the configured Provider.
     * The provider will contain all the information to be able to establish a connection with it.
     * The following steps are executed inside this method:
     *  1) Create the new ExecutionEnvironment object that will represent a remote host for our knowledge sessions.
     *  2) Each ExecutionEnvironment will have an underlaying connection to support remote/distribtued interactions
     *  3) for each Execution Environment registered in this topology
     *     3.1) We need to inject the reference from the newly created Execution Enviroment to the existing ones
     *     3.2) We need to inject the reference from all the existing Execution Environments to the newly created
     *  4) for each Directory Instance registered in this topology
     *     4.1) We need to inject the reference from the newly created Execution Environment in each exisiting Directory
     *     4.2) We need to inject a reference from each existing directory to the newly created Execution Environment
     *  5) Add the newly created Execution Environment to the topology maps. We keep to maps for Execution Environments
     *     to be able to look based on the underlaying connector and based on the defined Execution Environment name.
     *  6) Register the Execution Environment inside all the currently available Directory Instances
     */
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
        for (DirectoryInstance d : directoryInstances.values()) {
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
        registerResourceInDirectories(name, provider.getId());

    }

    /*
     * This method unregister the Execution Environment from this running instance of the grid topology
     * based on the name. The following steps are executed in order to unregister
     * an ExecutionEnvironment from the GridTopology:
     *  1) Get the ExecutionEnvironment from the executionEnvironmentMap
     *  2) Get the ExecutionEnvironment connector
     *     2.1) Remove the ExecutionEnvironment from the executionEnvironmentByConnectorId map
     *     2.2) Disconnect the connector
     *  3) Remove the executionEnvironment from the executionEnvironmentMap
     *  4) Unregister the ExecutionEnvironment from the running Directory Instances
     *
     */
    public void unregisterExecutionEnvironment(String name) {
        ExecutionEnvironment ee = executionEnvironments.get(name);
        try {

            GenericNodeConnector connector = ee.getConnector();
            connector.getConnection().dispose();
            executionEnvironmentsByConnectorId.remove(connector.getId());
            connector.disconnect();
        } catch (ConnectorException ex) {
            Logger.getLogger(GridTopology.class.getName()).log(Level.SEVERE, null, ex);
        } catch (RemoteException ex) {
            Logger.getLogger(GridTopology.class.getName()).log(Level.SEVERE, null, ex);
        }


        //Remove Execution Environment
        executionEnvironments.remove(name);
        //UnRegister EE from current Directories
        unregisterResourceFromDirectories(name);

    }
    /*
     * Get Execution Environment by Name
     * @param name: String Execution Environment Name
     */
    public ExecutionEnvironment getExecutionEnvironment(String name) {
        return executionEnvironments.get(name);
    }

    /*
     * Get ExecutionEnvironment by connector
     */
    public ExecutionEnvironment getExecutionEnvironment(GenericNodeConnector connector) {
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

    /*
     * Get the Best ExecutionEnvironment available based on a ExecutionEnvironmentSelectionStrategy
     * @param strategy: it's an implementation of the ExecutionEnvironmentSelectionStrategy interface
     */
    public ExecutionEnvironment getBestExecutionEnvironment(ExecutionEnvironmentSelectionStrategy strategy) {
        return strategy.getBestExecutionEnvironment(executionEnvironments);
    }

    /*
     * Get the Best ExecutionEnvironment available based on the default ExecutionEnvironmentSelectionStrategy
     */
    public ExecutionEnvironment getExecutionEnvironment() {
        return DEFAULT_EXECTUTION_STRATEGY.getBestExecutionEnvironment(executionEnvironments);
    }

    /*
     * This method register a new Directory instance based on the information provided by the GenericProvider
     * The provider will contain all the information to be able to establish a connection with it.
     * The following steps are executed inside this method:
     *  1) Create the new DirectoryInstance object that will represent map that will store information about
     *     the resources that are currently running in our topology.
     *  2) Each DirectoryInstance will have an underlaying connection to support remote/distribtued interactions
     *  3) for each Execution Environment registered in this topology
     *     3.1) We need to inject the reference from the newly created Directory to the existing ExecutionEnvironments
     *     3.2) We need to inject the reference from all the existing Execution Environments to the newly created Directory Instance
     *  4) for each Directory Instance registered in this topology
     *     4.1) We need to inject the reference from the newly created Directory Instance in each exisiting Directory
     *     4.2) We need to inject a reference from each existing directory to the newly created DirectoryInstance
     *  5) Add the newly created Directory Instance to the topology map. We keep a map for Directory Instances
     *     to be able to lookup based on the Directory Instance name.
     *  6) Register the Directory Instance inside all the currently available Directory Instances
     */
    public void registerDirectoryInstance(String name, GenericProvider provider) {

        DirectoryInstance directory = DirectoryInstanceFactory.newDirectoryInstance(name, provider);
        GenericNodeConnector connector = directory.getConnector();
        GenericConnection connection = connector.getConnection();
        connection.addDirectoryNode(connector);

        for (ExecutionEnvironment e : executionEnvironments.values()) {
            e.getConnector().getConnection().addDirectoryNode(connector);
            connection.addExecutionNode(e.getConnector());
        }
        for (DirectoryInstance d : directoryInstances.values()) {
            connection.addDirectoryNode(d.getConnector());
            d.getConnector().getConnection().addDirectoryNode(connector);
        }


        

        directoryInstances.put(name, directory);

        registerResourceInDirectories(name, provider.getId());
        
    }
    /*
     * Get the Best DirectoryInstance based on a DirectoryInstanceSelectionStrategy
     * @param strategy it's the strategy used to choose the best DirectoryInstance available
     */
    public DirectoryInstance getBestDirectoryInstance(DirectoryInstanceSelectionStrategy strategy) {
        return (DirectoryInstance) strategy.getBestDirectoryInstance(directoryInstances);
    }

    /*
     * Get the Directory Instance based on a default strategy
     */
    public DirectoryInstance getDirectoryInstance() {
        return DEFAULT_DIRECTORY_STRATEGY.getBestDirectoryInstance(directoryInstances);
    }

    /*
     * Get a Directory Instance by Name
     */
    public DirectoryInstance getDirectoryInstance(String name) {
        return directoryInstances.get(name);
    }

    /*
     * Unregister a Directroy Instance from this running GridTopology
     * This method unregister the Directory Instance from this running instance of the grid topology
     * based on the name. The following steps are executed in order to unregister
     * a DirectoryInstance from the GridTopology:
     *  1) Get the Directory Instance from the directoryInstances Map
     *  2) Get the DirectoryInstance connector
     *     2.1) Disconnect the connector
     *  3) Unregister the DirectoryInstance from the running Directory Instances
     *  4) Remove the DirectoryInstance from the directoryInstances Map
     */
    public void unregisterDirectoryInstance(String name) {
        DirectoryInstance dir = directoryInstances.get(name);
        GenericNodeConnector connector = dir.getConnector();
        
        try {
            connector.getConnection().dispose();
            connector.disconnect();
        } catch (ConnectorException ex) {
            Logger.getLogger(GridTopology.class.getName()).log(Level.SEVERE, null, ex);
        } catch (RemoteException ex) {
            Logger.getLogger(GridTopology.class.getName()).log(Level.SEVERE, null, ex);
        }

        unregisterResourceFromDirectories(name);

        directoryInstances.remove(name);

    }

     /*
     * This method will register a new Task Server  based on the configured Provider.
     * The provider will contain all the information to be able to establish a connection with the Task Server.
     * The following steps are executed inside this method:
     *  1) Create the new ExecutionEnvironment object that will represent a remote host for our knowledge sessions.
     *  2) Each ExecutionEnvironment will have an underlaying connection to support remote/distribtued interactions
     *  3) for each Execution Environment registered in this topology
     *     3.1) We need to inject the reference from the newly created Execution Enviroment to the existing ones
     *     3.2) We need to inject the reference from all the existing Execution Environments to the newly created
     *  4) for each Directory Instance registered in this topology
     *     4.1) We need to inject the reference from the newly created Execution Environment in each exisiting Directory
     *     4.2) We need to inject a reference from each existing directory to the newly created Execution Environment
     *  5) Add the newly created Execution Environment to the topology maps. We keep to maps for Execution Environments
     *     to be able to look based on the underlaying connector and based on the defined Execution Environment name.
     *  6) Register the Execution Environment inside all the currently available Directory Instances
     */
    public void registerTaskServerInstance(String name, GenericProvider provider) {

        TaskServerInstance taskServer = TaskServerInstanceFactory.newTaskServerInstance(name, provider);
        GenericHumanTaskConnector connector = taskServer.getConnector();
        GenericConnection connection = connector.getConnection();
        connection.addHumanTaskNode(connector);

        taskServerInstances.put(name, taskServer);
        registerResourceInDirectories(name, provider.getId());

    }

    public TaskServerInstance getTaskServerInstance(String name) {
        return taskServerInstances.get(name);
    }

    public TaskServerInstance getBestTaskServerInstance(TaskServerInstanceSelectionStrategy strategy) {
        return (TaskServerInstance) strategy.getBestTaskServerInstance(taskServerInstances);
    }

    public TaskServerInstance getBestTaskServerInstance() {
        return DEFAULT_TASK_STRATEGY.getBestTaskServerInstance(taskServerInstances);
    }

    public void unregisterTaskServerInstance(String name) {
        //Remove Task Server Instance
        taskServerInstances.remove(name);
        //UnRegister task Server instance from current Directories
        unregisterResourceFromDirectories(name);


    }

    public void disconnect() throws ConnectorException, RemoteException {

        for (String key : executionEnvironments.keySet()) {
            ExecutionEnvironment ee = executionEnvironments.get(key);
            GenericNodeConnector connector = ee.getConnector();
            connector.getConnection().dispose();
            connector.disconnect();
        }
        for (String key : directoryInstances.keySet()) {
            DirectoryInstance dir = directoryInstances.get(key);
            GenericNodeConnector connector = dir.getConnector();
            connector.getConnection().dispose();
            connector.disconnect();
        }
        for (String key : taskServerInstances.keySet()) {
            TaskServerInstance taskServer = taskServerInstances.get(key);
            GenericHumanTaskConnector connector = taskServer.getConnector();
            connector.getConnection().dispose();
            connector.disconnect();
        }
    }


    public void dispose() throws ConnectorException, RemoteException {

        for (String key : executionEnvironments.keySet()) {
            unregisterExecutionEnvironment(key);
        }
        for (String key : directoryInstances.keySet()) {
            unregisterDirectoryInstance(key);
        }
        for (String key : taskServerInstances.keySet()) {
            unregisterTaskServerInstance(key);
        }
    }

    private void registerResourceInDirectories(String name, String resourceId) {
        for (DirectoryInstance directory : directoryInstances.values()) {

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

    private void unregisterResourceFromDirectories(String name) {
        for (DirectoryInstance directory : directoryInstances.values()) {

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
