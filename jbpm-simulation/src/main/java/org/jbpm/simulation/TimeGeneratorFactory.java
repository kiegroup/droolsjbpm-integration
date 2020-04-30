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

package org.jbpm.simulation;

import java.util.Map;

import org.jbpm.simulation.impl.time.ExactTimeGenerator;
import org.jbpm.simulation.impl.time.NormalTimeGenerator;
import org.jbpm.simulation.impl.time.PoissonTimeGenerator;
import org.jbpm.simulation.impl.time.RandomTimeGenerator;
import org.jbpm.simulation.impl.time.UniformTimeGenerator;
import org.jbpm.simulation.util.SimulationConstants;

public class TimeGeneratorFactory {

    
    public static TimeGenerator newTimeGenerator(Map<String, Object> data) {
        String distribution = (String) data.get(SimulationConstants.DISTRIBUTION_TYPE);
         if ("random".equalsIgnoreCase(distribution)) {
            return new RandomTimeGenerator(data);
        } else if ("uniform".equalsIgnoreCase(distribution)) {
            return new UniformTimeGenerator(data);
        } else if ("normal".equalsIgnoreCase(distribution)) {
            return new NormalTimeGenerator(data);
        } else if ("poisson".equalsIgnoreCase(distribution)) {
            return new PoissonTimeGenerator(data);
        } else if ("exact".equalsIgnoreCase(distribution)) {
            return new ExactTimeGenerator(data);
        } else {
            throw new RuntimeException("Unsupported distribution type " + distribution);
        }
    }
}
