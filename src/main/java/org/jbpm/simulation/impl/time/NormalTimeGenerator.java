package org.jbpm.simulation.impl.time;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.math.random.RandomData;
import org.apache.commons.math.random.RandomDataImpl;
import org.jbpm.simulation.TimeGenerator;
import org.jbpm.simulation.util.SimulationConstants;
import org.jbpm.simulation.util.SimulationUtils;

public class NormalTimeGenerator implements TimeGenerator {

    private Map<String, Object> data;
    private static RandomData generator = new RandomDataImpl();
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
        
        return  (long) generator.nextGaussian(mean, sdv);
    }

}
