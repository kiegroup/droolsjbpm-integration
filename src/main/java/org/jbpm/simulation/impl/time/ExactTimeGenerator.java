package org.jbpm.simulation.impl.time;

import java.util.Map;

import org.jbpm.simulation.TimeGenerator;
import org.jbpm.simulation.util.SimulationConstants;
import org.jbpm.simulation.util.SimulationUtils;

public class ExactTimeGenerator implements TimeGenerator {

    private Map<String, Object> data;

    public ExactTimeGenerator(Map<String, Object> data) {
        this.data = data;
    }

    public long generateTime() {
        
        return SimulationUtils.asLong(data.get(SimulationConstants.DURATION));
    }

}
