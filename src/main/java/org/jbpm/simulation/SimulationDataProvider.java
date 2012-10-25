package org.jbpm.simulation;

import java.util.Map;

import org.drools.definition.process.Node;
import org.jbpm.simulation.impl.SimulationPath;

public interface SimulationDataProvider {

    Map<String, Object> getSimulationDataForNode(Node node);
    
    double calculatePathProbability(SimulationPath path);

    Map<String, Object> getProcessDataForNode(Node node);
}
