package org.drools.grid.services.factory;

import org.drools.grid.services.GridTopology;
import org.drools.grid.services.configuration.DirectoryInstanceConfiguration;
import org.drools.grid.services.configuration.ExecutionEnvironmentConfiguration;
import org.drools.grid.services.configuration.GridTopologyConfiguration;
import org.drools.grid.services.configuration.TaskServerInstanceConfiguration;

public class GridTopologyFactory {

    public static GridTopology build(GridTopologyConfiguration gridConfiguration) {
        GridTopology topology = new GridTopology( gridConfiguration.getName() );
        for ( DirectoryInstanceConfiguration directoryInstanceConfiguration : gridConfiguration.getDirectoryInstances() ) {
            topology.registerDirectoryInstance( directoryInstanceConfiguration.getName(),
                                                directoryInstanceConfiguration.getProvider() );
        }
        for ( ExecutionEnvironmentConfiguration executionEnvironmentConfiguration : gridConfiguration.getExecutionEnvironments() ) {
            topology.registerExecutionEnvironment( executionEnvironmentConfiguration.getName(),
                                                   executionEnvironmentConfiguration.getProvider() );
        }
        for ( TaskServerInstanceConfiguration taskServerInstanceView : gridConfiguration.getTaskServers() ) {
            topology.registerTaskServerInstance( taskServerInstanceView.getName(),
                                                 taskServerInstanceView.getProvider() );
        }

        return topology;
    }

}
