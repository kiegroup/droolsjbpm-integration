package org.drools.grid.services.factory;

import org.drools.grid.services.GridTopology;
import org.drools.grid.services.configuration.DirectoryInstanceConfiguration;
import org.drools.grid.services.configuration.ExecutionEnvironmentConfiguration;
import org.drools.grid.services.configuration.GridTopologyConfiguration;
import org.drools.grid.services.configuration.TaskServerInstanceConfiguration;

public class GridTopologyFactory {
	
	public static GridTopology build(GridTopologyConfiguration gridConfiguration){
		GridTopology topology = new GridTopology(gridConfiguration.getName());
		for (DirectoryInstanceConfiguration directoryInstanceView : gridConfiguration.getDirectoryInstances()) {
			topology.registerDirectoryInstance(directoryInstanceView.getName(), directoryInstanceView.getProvider());
		}
		for (ExecutionEnvironmentConfiguration executionEnvironmentView : gridConfiguration.getExecutionEnvironments()) {
			topology.registerExecutionEnvironment(executionEnvironmentView.getName(), executionEnvironmentView.getProvider());
		}
		for (TaskServerInstanceConfiguration taskServerInstanceView : gridConfiguration.getTaskServers()) {
			topology.registerTaskServerInstance(taskServerInstanceView.getName(), taskServerInstanceView.getProvider());
		}

		return topology;
	}

}
