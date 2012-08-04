package org.jbpm.simulation.helper;

import java.util.HashMap;
import java.util.Map;

import org.drools.definition.process.Node;
import org.jbpm.simulation.SimulationDataProvider;
import org.jbpm.simulation.impl.SimulationPath;
import org.jbpm.simulation.util.SimulationConstants;

public class HardCodedSimulationDataProvider implements SimulationDataProvider {

    public Map<String, Object> getSimulationDataForNode(Node node) {
        Map<String, Object> data = new HashMap<String, Object>();
        
        String nodeId = (String) node.getMetaData().get("UniqueId");
        
        if ("_2".equals(nodeId)) {
            data.put(SimulationConstants.DURATION, new Long(21));
            data.put(SimulationConstants.STAFF_AVAILABILITY, new Integer(3));
            data.put(SimulationConstants.WORKING_HOURS, new Integer(1));
            data.put(SimulationConstants.COST_PER_TIME_UNIT, new Double(45));
            data.put(SimulationConstants.TIMEUNIT, "minutes");
        } else if ("_3".equals(nodeId)) {
            data.put(SimulationConstants.DURATION, new Long(37000));
        } else if ("_4".equals(nodeId)) {
            data.put(SimulationConstants.DURATION, new Long(23000));
        } else {
            
            // default duration to 1 sec
            data.put(SimulationConstants.DURATION, new Long(1000));
        }
        return data;
    }

    public double calculatePathProbability(SimulationPath path) {
        // not supported
        return 100;
    }
    
    

}
