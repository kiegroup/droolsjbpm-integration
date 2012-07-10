package org.jbpm.simulation;

import java.util.Map;

import org.jbpm.simulation.impl.time.ExactTimeGenerator;
import org.jbpm.simulation.impl.time.FixedTimeGenerator;
import org.jbpm.simulation.impl.time.NormalTimeGenerator;
import org.jbpm.simulation.impl.time.RandomTimeGenerator;
import org.jbpm.simulation.impl.time.UniformTimeGenerator;

public class TimeGeneratorFactory {

    
    public static TimeGenerator newTimeGenerator(Map<String, Object> data) {
        String distribution = (String) data.get("distributionType");
         if ("random".equalsIgnoreCase(distribution)) {
            return new RandomTimeGenerator(data);
        } else if ("fixed".equalsIgnoreCase(distribution)) {
            
            return new FixedTimeGenerator(data);
            
        } else if ("uniform".equalsIgnoreCase(distribution)) {
            return new UniformTimeGenerator(data);
        } else if ("normal".equalsIgnoreCase(distribution)) {
            return new NormalTimeGenerator(data);
        } else {
            return new ExactTimeGenerator(data);
        }
    }
}
