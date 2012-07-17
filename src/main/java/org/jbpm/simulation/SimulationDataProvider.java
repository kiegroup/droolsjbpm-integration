package org.jbpm.simulation;

import java.util.Map;

import org.drools.definition.process.Node;

public interface SimulationDataProvider {

    Map<String, Object> getSimulationDataForNode(String processId, Node node);
}
