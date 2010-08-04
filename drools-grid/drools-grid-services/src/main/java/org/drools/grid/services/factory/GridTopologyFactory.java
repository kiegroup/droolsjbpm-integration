package org.drools.grid.services.factory;

import org.drools.grid.services.GridTopology;
import org.drools.grid.services.configuration.DirectoryInstanceView;
import org.drools.grid.services.configuration.ExecutionEnvironmentView;
import org.drools.grid.services.configuration.GridTopologyView;
import org.drools.grid.services.configuration.TaskServerInstanceView;

public class GridTopologyFactory {
	
	public static GridTopology build(GridTopologyView gridConfiguration){
		GridTopology topology = new GridTopology(gridConfiguration.getName());
		for (DirectoryInstanceView directoryInstanceView : gridConfiguration.getDirectoryInstances()) {
			topology.registerDirectoryInstance(directoryInstanceView.getName(), directoryInstanceView.getProvider());
		}
		for (ExecutionEnvironmentView executionEnvironmentView : gridConfiguration.getExecutionEnvironments()) {
			topology.registerExecutionEnvironment(executionEnvironmentView.getName(), executionEnvironmentView.getProvider());
		}
		for (TaskServerInstanceView taskServerInstanceView : gridConfiguration.getTaskServers()) {
			topology.registerTaskServerInstance(taskServerInstanceView.getName(), taskServerInstanceView.getProvider());
		}

		return topology;
	}

}
