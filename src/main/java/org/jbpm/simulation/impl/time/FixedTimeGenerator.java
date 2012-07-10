package org.jbpm.simulation.impl.time;

import java.util.Map;

import org.jbpm.simulation.TimeGenerator;

public class FixedTimeGenerator implements TimeGenerator {

    private Map<String, Object> data;
    private long incrementBy;

    public FixedTimeGenerator(Map<String, Object> data) {
        this.data = data;
        this.incrementBy = (Long) data.get("fixed.increment");
    }
    
    public long generateTime() {
        // TODO Auto-generated method stub
        return 0;
    }

}
