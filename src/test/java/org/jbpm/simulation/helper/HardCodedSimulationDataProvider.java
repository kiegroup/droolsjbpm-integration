package org.jbpm.simulation.helper;

import java.util.HashMap;
import java.util.Map;

import org.jbpm.simulation.SimulationDataProvider;

public class HardCodedSimulationDataProvider implements SimulationDataProvider {

    public Map<String, Object> getSimulationDataForNode(String processId, String nodeId) {
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("distributionType", "normal");
        data.put("std.deviation", 12L);
        
        if ("_3".equals(nodeId)) {
            data.put("duration", new Long(37000));
        } else if ("_4".equals(nodeId)) {
            data.put("duration", new Long(23000));
        } else {
            
            // default duration to 1 sec
            data.put("duration", new Long(1000));
        }
        return data;
    }

}
