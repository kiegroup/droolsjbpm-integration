package org.jbpm.simulation.impl.time;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.math3.random.RandomDataGenerator;
import org.jbpm.simulation.TimeGenerator;
import org.jbpm.simulation.util.SimulationConstants;
import org.jbpm.simulation.util.SimulationUtils;

public class UniformTimeGenerator implements TimeGenerator {

    private Map<String, Object> data;
    private static RandomDataGenerator generator = new RandomDataGenerator();
    private TimeUnit timeUnit = TimeUnit.MILLISECONDS;
    
    public UniformTimeGenerator(Map<String, Object> data) {
        this.data = data;
    }
    
    public long generateTime() {

        TimeUnit tu = SimulationUtils.getTimeUnit(data);
        
        long min = (long)SimulationUtils.asDouble(data.get(SimulationConstants.MIN));
        min = timeUnit.convert(min, tu);
        
        long max = (long) SimulationUtils.asDouble(data.get(SimulationConstants.MAX));
        max = timeUnit.convert(max, tu);
        if (max > min) {
            return  (long) generator.nextUniform(min, max);
        } else {
            return min;
        }
    }

}
