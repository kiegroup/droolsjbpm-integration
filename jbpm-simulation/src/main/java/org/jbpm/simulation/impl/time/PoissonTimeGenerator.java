package org.jbpm.simulation.impl.time;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.math.random.RandomData;
import org.apache.commons.math.random.RandomDataImpl;
import org.jbpm.simulation.TimeGenerator;
import org.jbpm.simulation.util.SimulationConstants;
import org.jbpm.simulation.util.SimulationUtils;

public class PoissonTimeGenerator implements TimeGenerator {

    private Map<String, Object> data;
    private static RandomData generator = new RandomDataImpl();
    private TimeUnit timeUnit = TimeUnit.MILLISECONDS;
    
    public PoissonTimeGenerator(Map<String, Object> data) {
        this.data = data;
    }



    public long generateTime() {
        TimeUnit tu = SimulationUtils.getTimeUnit(data);
        long mean = (long)SimulationUtils.asDouble(data.get(SimulationConstants.MEAN));
        mean = timeUnit.convert(mean, tu);
        if(mean > 0) {    
            return  (long) generator.nextPoisson(mean);
        } else {
            return 0;
        }
    }

}
