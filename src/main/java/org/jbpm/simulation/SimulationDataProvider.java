package org.jbpm.simulation;

import java.util.Map;

import org.jbpm.simulation.impl.SimulationPath;
import org.kie.definition.process.Node;

public interface SimulationDataProvider {

    Map<String, Object> getSimulationDataForNode(Node node);
    
    double calculatePathProbability(SimulationPath path);

    Map<String, Object> getProcessDataForNode(Node node);
}
