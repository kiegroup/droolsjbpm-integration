package org.jbpm.simulation.impl.time;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.math3.random.RandomDataGenerator;
import org.jbpm.simulation.TimeGenerator;
import org.jbpm.simulation.util.SimulationConstants;
import org.jbpm.simulation.util.SimulationUtils;

public class NormalTimeGenerator implements TimeGenerator {

    private Map<String, Object> data;
    private static RandomDataGenerator generator = new RandomDataGenerator();
    private TimeUnit timeUnit = TimeUnit.MILLISECONDS;
    
    public NormalTimeGenerator(Map<String, Object> data) {
        this.data = data;
    }



    public long generateTime() {
        TimeUnit tu = SimulationUtils.getTimeUnit(data);
        long mean = (long)SimulationUtils.asDouble(data.get(SimulationConstants.MEAN));
        mean = timeUnit.convert(mean, tu);
        
        long sdv = (long)SimulationUtils.asDouble(data.get(SimulationConstants.STANDARD_DEVIATION));
        sdv = timeUnit.convert(sdv, tu);
        
        if (sdv > 0) {
        
            long value =  (long) generator.nextGaussian(mean, sdv);
            if (value <= 0) {
                value = mean;
            }
            return value;
        } else {
            return 0;
        }
    }


}
