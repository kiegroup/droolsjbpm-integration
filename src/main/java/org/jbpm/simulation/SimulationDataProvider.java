package org.jbpm.simulation;

import java.util.Map;

public interface SimulationDataProvider {

    Map<String, Object> getSimulationDataForNode(String processId, String nodeId);
}
