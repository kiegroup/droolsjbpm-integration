package org.jbpm.simulation.impl.time;

import java.util.Map;

import org.apache.commons.math.random.RandomData;
import org.apache.commons.math.random.RandomDataImpl;
import org.jbpm.simulation.TimeGenerator;
import org.jbpm.simulation.util.SimulationConstants;
import org.jbpm.simulation.util.SimulationUtils;

public class NormalTimeGenerator implements TimeGenerator {

    private Map<String, Object> data;
    private static RandomData generator = new RandomDataImpl();
    
    
    public NormalTimeGenerator(Map<String, Object> data) {
        this.data = data;
    }



    public long generateTime() {

        long mean = SimulationUtils.asLong(data.get(SimulationConstants.DURATION));
        long sdv = SimulationUtils.asLong(data.get(SimulationConstants.STANDARD_DEVIATION));
       
        return  (long) generator.nextGaussian(mean, sdv);
    }

}
