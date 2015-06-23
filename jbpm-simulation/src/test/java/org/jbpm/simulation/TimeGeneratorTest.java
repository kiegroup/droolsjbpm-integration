/*
 * Copyright 2015 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package org.jbpm.simulation;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.math3.stat.descriptive.rank.Percentile;
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
        data.put(SimulationConstants.MIN, 500L);
        data.put(SimulationConstants.MAX, 40000L);
        
        TimeGenerator generator = TimeGeneratorFactory.newTimeGenerator(data);
        assertNotNull(generator);
        assertTrue(generator instanceof RandomTimeGenerator);
        
        System.out.println(generator.generateTime());
        System.out.println(generator.generateTime());
        System.out.println(generator.generateTime());
        System.out.println(generator.generateTime());
    }
}
