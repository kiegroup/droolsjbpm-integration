package org.jbpm.simulation.impl.time;

import java.util.Map;

import org.apache.commons.math.random.RandomData;
import org.apache.commons.math.random.RandomDataImpl;
import org.jbpm.simulation.TimeGenerator;

public class NormalTimeGenerator implements TimeGenerator {

    private Map<String, Object> data;
    private static RandomData generator = new RandomDataImpl();
    
    
    public NormalTimeGenerator(Map<String, Object> data) {
        this.data = data;
    }



    public long generateTime() {

        long mean = (Long) data.get("duration");
        long sdv = (Long) data.get("std.deviation");
       
        return  (long) generator.nextGaussian(mean, sdv);
    }

}
