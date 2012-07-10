package org.jbpm.simulation.impl.time;

import java.util.Map;

import org.apache.commons.math.random.RandomData;
import org.apache.commons.math.random.RandomDataImpl;
import org.jbpm.simulation.TimeGenerator;

public class UniformTimeGenerator implements TimeGenerator {

    private Map<String, Object> data;
    private static RandomData generator = new RandomDataImpl();
    
    
    public UniformTimeGenerator(Map<String, Object> data) {
        this.data = data;
    }
    
    public long generateTime() {

        long value = (Long) data.get("duration");
        long range = (Long) data.get("range");
       
        return  (long) generator.nextUniform(value-range, value+range);
    }

}
