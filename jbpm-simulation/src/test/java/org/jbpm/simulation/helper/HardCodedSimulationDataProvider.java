package org.jbpm.simulation.helper;

import java.util.HashMap;
import java.util.Map;

import org.jbpm.simulation.SimulationDataProvider;
import org.jbpm.simulation.impl.SimulationPath;
import org.jbpm.simulation.util.SimulationConstants;
import org.kie.api.definition.process.Node;

public class HardCodedSimulationDataProvider implements SimulationDataProvider {

    public Map<String, Object> getSimulationDataForNode(Node node) {
        Map<String, Object> data = new HashMap<String, Object>();
        
        String nodeId = (String) node.getMetaData().get("UniqueId");
        data.put(SimulationConstants.DISTRIBUTION_TYPE, "random");
        
        if ("_2".equals(nodeId)) {
            data.put(SimulationConstants.MIN, new Long(15));
            data.put(SimulationConstants.MAX, new Long(25));
            data.put(SimulationConstants.STAFF_AVAILABILITY, new Integer(3));
            data.put(SimulationConstants.WORKING_HOURS, new Integer(1));
            data.put(SimulationConstants.COST_PER_TIME_UNIT, new Double(45));
            data.put(SimulationConstants.TIMEUNIT, "minutes");
        } else if ("_3".equals(nodeId)) {
            data.put(SimulationConstants.MIN, new Long(32000));
            data.put(SimulationConstants.MAX, new Long(41000));
        } else if ("_4".equals(nodeId)) {
            data.put(SimulationConstants.MIN, new Long(20000));
            data.put(SimulationConstants.MAX, new Long(26000));
        } else {
            
            // default duration to 1 sec
            data.put(SimulationConstants.MIN, new Long(1000));
            data.put(SimulationConstants.MAX, new Long(2000));
        }
        return data;
    }

    public double calculatePathProbability(SimulationPath path) {
        // not supported
        return 100;
    }

    public Map<String, Object> getProcessDataForNode(Node node) {
        return new HashMap<String, Object>();
    }


}
