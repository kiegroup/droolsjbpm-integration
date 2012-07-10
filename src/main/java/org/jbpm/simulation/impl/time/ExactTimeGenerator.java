package org.jbpm.simulation.impl.time;

import java.util.Map;

import org.jbpm.simulation.TimeGenerator;

public class ExactTimeGenerator implements TimeGenerator {

    private Map<String, Object> data;

    public ExactTimeGenerator(Map<String, Object> data) {
        this.data = data;
    }

    public long generateTime() {
        
        return (Long) data.get("duration");
    }

}
