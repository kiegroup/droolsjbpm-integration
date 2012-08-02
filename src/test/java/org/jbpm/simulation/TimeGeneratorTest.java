package org.jbpm.simulation;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.math.stat.descriptive.rank.Percentile;
import org.jbpm.simulation.impl.time.RandomTimeGenerator;
import org.jbpm.simulation.util.SimulationConstants;
import org.junit.Test;

public class TimeGeneratorTest {

    @Test
    public void testRandomGenerator() {
        
        Percentile p = new Percentile(35);
        p.setData(new double[]{35});
        System.out.println(p.evaluate(5));
        
        Map<String, Object> data = new HashMap<String, Object>();
        data.put(SimulationConstants.DISTRIBUTION_TYPE, "random");
        data.put(SimulationConstants.RANGE, 500L);
        data.put(SimulationConstants.DURATION, 40000L);
        
        TimeGenerator generator = TimeGeneratorFactory.newTimeGenerator(data);
        assertNotNull(generator);
        assertTrue(generator instanceof RandomTimeGenerator);
        
        System.out.println(generator.generateTime());
        System.out.println(generator.generateTime());
        System.out.println(generator.generateTime());
        System.out.println(generator.generateTime());
    }
}
