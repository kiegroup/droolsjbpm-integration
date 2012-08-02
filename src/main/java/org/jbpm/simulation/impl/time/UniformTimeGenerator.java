package org.jbpm.simulation.impl.time;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.math.random.RandomData;
import org.apache.commons.math.random.RandomDataImpl;
import org.jbpm.simulation.TimeGenerator;
import org.jbpm.simulation.util.SimulationConstants;
import org.jbpm.simulation.util.SimulationUtils;

public class UniformTimeGenerator implements TimeGenerator {

    private Map<String, Object> data;
    private static RandomData generator = new RandomDataImpl();
    private TimeUnit timeUnit = TimeUnit.MILLISECONDS;
    
    public UniformTimeGenerator(Map<String, Object> data) {
        this.data = data;
    }
    
    public long generateTime() {

        TimeUnit tu = SimulationUtils.getTimeUnit(data);
        
        long value = SimulationUtils.asLong(data.get(SimulationConstants.DURATION));
        value = timeUnit.convert(value, tu);
        
        long range = SimulationUtils.asLong(data.get(SimulationConstants.RANGE));
        range = timeUnit.convert(range, tu);
       
        return  (long) generator.nextUniform(value-range, value+range);
    }

}
