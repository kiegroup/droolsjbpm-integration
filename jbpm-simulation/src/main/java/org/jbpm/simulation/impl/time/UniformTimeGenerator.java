/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
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

package org.jbpm.simulation.impl.time;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.math3.random.RandomDataGenerator;
import org.jbpm.simulation.TimeGenerator;
import org.jbpm.simulation.util.SimulationConstants;
import org.jbpm.simulation.util.SimulationUtils;

public class UniformTimeGenerator implements TimeGenerator {

    private Map<String, Object> data;
    private static RandomDataGenerator generator = new RandomDataGenerator();
    private TimeUnit timeUnit = TimeUnit.MILLISECONDS;
    
    public UniformTimeGenerator(Map<String, Object> data) {
        this.data = data;
    }
    
    public long generateTime() {

        TimeUnit tu = SimulationUtils.getTimeUnit(data);
        
        long min = (long)SimulationUtils.asDouble(data.get(SimulationConstants.MIN));
        min = timeUnit.convert(min, tu);
        
        long max = (long) SimulationUtils.asDouble(data.get(SimulationConstants.MAX));
        max = timeUnit.convert(max, tu);
        if (max > min) {
            return  (long) generator.nextUniform(min, max);
        } else {
            return min;
        }
    }

}
