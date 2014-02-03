package org.jbpm.simulation.impl.time;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.math3.random.RandomData;
import org.apache.commons.math3.random.RandomDataImpl;
import org.jbpm.simulation.TimeGenerator;
import org.jbpm.simulation.util.SimulationConstants;
import org.jbpm.simulation.util.SimulationUtils;

public class RandomTimeGenerator implements TimeGenerator {

    private Map<String, Object> data;
    private static RandomData generator = new RandomDataImpl();
    private TimeUnit timeUnit = TimeUnit.MILLISECONDS;
    
    public RandomTimeGenerator(Map<String, Object> data) {
        this.data = data;
    }
    
    public long generateTime() {
        
        TimeUnit tu = SimulationUtils.getTimeUnit(data);
        
        long min = (long)SimulationUtils.asDouble(data.get(SimulationConstants.MIN));
        min = timeUnit.convert(min, tu);
        
        long max = (long)SimulationUtils.asDouble(data.get(SimulationConstants.MAX));
        max = timeUnit.convert(max, tu);
        if (max > min) {
            return  (long) generator.nextLong(min, max);
        } else {
            return min;
        }
    }

}
