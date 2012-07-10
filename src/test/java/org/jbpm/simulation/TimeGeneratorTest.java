package org.jbpm.simulation;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.jbpm.simulation.impl.time.RandomTimeGenerator;
import org.junit.Test;

public class TimeGeneratorTest {

    @Test
    public void testRandomGenerator() {
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("distributionType", "random");
        data.put("range", 5L);
        data.put("duration", 40000L);
        
        TimeGenerator generator = TimeGeneratorFactory.newTimeGenerator(data);
        assertNotNull(generator);
        assertTrue(generator instanceof RandomTimeGenerator);
        
        System.out.println(generator.generateTime());
        System.out.println(generator.generateTime());
        System.out.println(generator.generateTime());
        System.out.println(generator.generateTime());
    }
}
