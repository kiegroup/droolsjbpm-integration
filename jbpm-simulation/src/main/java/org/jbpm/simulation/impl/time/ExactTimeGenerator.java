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

package org.jbpm.simulation.impl.time;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.jbpm.simulation.TimeGenerator;
import org.jbpm.simulation.util.SimulationUtils;

public class ExactTimeGenerator implements TimeGenerator {

    private Map<String, Object> data;
    private TimeUnit timeUnit = TimeUnit.MILLISECONDS;
    
    public ExactTimeGenerator(Map<String, Object> data) {
        this.data = data;
    }

    public long generateTime() {
        TimeUnit tu = SimulationUtils.getTimeUnit(data);
        long duration = (long)SimulationUtils.asDouble(data.get("duration"));
        return timeUnit.convert(duration, tu);
    }

}
