package org.jbpm.simulation.impl.time;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.jbpm.simulation.TimeGenerator;
import org.jbpm.simulation.util.SimulationConstants;
import org.jbpm.simulation.util.SimulationUtils;

public class ExactTimeGenerator implements TimeGenerator {

    private Map<String, Object> data;
    private TimeUnit timeUnit = TimeUnit.MILLISECONDS;

    public ExactTimeGenerator(Map<String, Object> data) {
        this.data = data;
    }

    public long generateTime() {
        long duration = SimulationUtils.asLong(data.get(SimulationConstants.DURATION));
        duration = timeUnit.convert(duration, SimulationUtils.getTimeUnit(data));
        
        return duration;
    }

}
