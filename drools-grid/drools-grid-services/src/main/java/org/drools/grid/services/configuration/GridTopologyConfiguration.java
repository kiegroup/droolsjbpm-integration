package org.drools.grid.services.configuration;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class GridTopologyConfiguration
    implements
    Serializable {

    private String                                         name;
    private Map<String, ExecutionEnvironmentConfiguration> executionEnvironments = new HashMap<String, ExecutionEnvironmentConfiguration>();
    private Map<String, DirectoryInstanceConfiguration>    directoryInstances    = new HashMap<String, DirectoryInstanceConfiguration>();
    private Map<String, TaskServerInstanceConfiguration>   taskServerInstances   = new HashMap<String, TaskServerInstanceConfiguration>();

    public GridTopologyConfiguration() {
    }

    public GridTopologyConfiguration(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Collection<ExecutionEnvironmentConfiguration> getExecutionEnvironments() {
        return Collections.unmodifiableCollection( this.executionEnvironments.values() );
    }

    public Collection<DirectoryInstanceConfiguration> getDirectoryInstances() {
        return Collections.unmodifiableCollection( this.directoryInstances.values() );
    }

    public Collection<TaskServerInstanceConfiguration> getTaskServers() {
        return Collections.unmodifiableCollection( this.taskServerInstances.values() );
    }

    public void addExecutionEnvironment(ExecutionEnvironmentConfiguration newExecutionEnvironment) {
        String newGridResourceName = newExecutionEnvironment.getName();
        checkUniqueName( newGridResourceName,
                         this.directoryInstances,
                         this.taskServerInstances );
        this.executionEnvironments.put( newGridResourceName,
                                        newExecutionEnvironment );
    }

    public void addDirectoryInstance(DirectoryInstanceConfiguration newDirectoryInstance) {
        String newGridResourceName = newDirectoryInstance.getName();
        checkUniqueName( newGridResourceName,
                         this.executionEnvironments,
                         this.taskServerInstances );
        this.directoryInstances.put( newGridResourceName,
                                     newDirectoryInstance );
    }

    public void addTaskServerInstance(TaskServerInstanceConfiguration newTaskServerInstance) {
        String newGridResourceName = newTaskServerInstance.getName();
        checkUniqueName( newGridResourceName,
                         this.executionEnvironments,
                         this.directoryInstances );
        this.taskServerInstances.put( newGridResourceName,
                                      newTaskServerInstance );
    }

    public ExecutionEnvironmentConfiguration getExecutionEnvironment(String name) {
        return this.executionEnvironments.get( name );
    }

    public DirectoryInstanceConfiguration getDirectoryInstance(String name) {
        return this.directoryInstances.get( name );
    }

    public TaskServerInstanceConfiguration getTaskServerInstance(String name) {
        return this.taskServerInstances.get( name );
    }

    public void removeResource(String name) {
        boolean removed = this.executionEnvironments.remove( name ) != null;
        removed |= this.directoryInstances.remove( name ) != null;
        removed |= this.taskServerInstances.remove( name ) != null;
    }

    private void checkUniqueName(String newGridResourceName,
                                 Map<String, ? extends GridResourceConfiguration> firstResources,
                                 Map<String, ? extends GridResourceConfiguration> secondResources) {
        if ( null != firstResources.get( newGridResourceName ) || null != secondResources.get( newGridResourceName ) ) {
            throw new IllegalArgumentException( "Existing resource with name: " + newGridResourceName );
        }
    }
}
