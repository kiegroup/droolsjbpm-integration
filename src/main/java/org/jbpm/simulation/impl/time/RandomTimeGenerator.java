package org.jbpm.simulation.impl.time;

import java.util.Map;

import org.apache.commons.math.random.RandomData;
import org.apache.commons.math.random.RandomDataImpl;
import org.jbpm.simulation.TimeGenerator;
import org.jbpm.simulation.util.SimulationConstants;
import org.jbpm.simulation.util.SimulationUtils;

public class RandomTimeGenerator implements TimeGenerator {

    private Map<String, Object> data;
    private static RandomData generator = new RandomDataImpl();
    
    
    public RandomTimeGenerator(Map<String, Object> data) {
        this.data = data;
    }
    
    public long generateTime() {
        
        long value = SimulationUtils.asLong(data.get(SimulationConstants.DURATION));
        long range = SimulationUtils.asLong(data.get(SimulationConstants.RANGE));
       
        return  (long) generator.nextLong(value-range, value+range);
    }

}
