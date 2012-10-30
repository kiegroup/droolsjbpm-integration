package org.jbpm.simulation.impl.time;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.jbpm.simulation.TimeGenerator;
import org.jbpm.simulation.util.SimulationUtils;

public class ExactTimeGenerator implements TimeGenerator {

    private Map<String, Object> data;
    private TimeUnit timeUnit = TimeUnit.MILLISECONDS;
    
    public ExactTimeGenerator(Map<String, Object> data) {
        this.data = data;
    }

    public long generateTime() {
        TimeUnit tu = SimulationUtils.getTimeUnit(data);
        long duration = (long)SimulationUtils.asDouble(data.get("duration"));
        return timeUnit.convert(duration, tu);
    }

}
