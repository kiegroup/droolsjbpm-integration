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

package org.jbpm.simulation.impl.events;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.jbpm.simulation.AggregatedSimulationEvent;
import org.jbpm.simulation.SimulationEvent;

public class GenericSimulationEvent implements SimulationEvent {
    
    protected String processId;
    protected long processInstanceId;
    protected Map<String, Object> customMetrics = new ConcurrentHashMap<String, Object>();
    protected long startTime;
    protected long endTime;
    protected UUID uuid;

    protected String type;
    
    protected AggregatedSimulationEvent aggregatedEvent;
    protected boolean used; 
    
    
    public boolean isUsed() {
        return used;
    }

    public void setUsed(boolean used) {
        this.used = used;
    }

    public AggregatedSimulationEvent getAggregatedEvent() {
        return aggregatedEvent;
    }

    public void setAggregatedEvent(AggregatedSimulationEvent aggregatedEvent) {
        this.aggregatedEvent = aggregatedEvent;

    }

    public long getStartTime() {
        return startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public GenericSimulationEvent(String processId, long processInstanceId, long startTime, long endTime, String type) {
        super();
        this.processId = processId;
        this.processInstanceId = processInstanceId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.uuid = UUID.randomUUID();
        this.type = type;
    }

    

    public String getProcessId() {
        
        return this.processId;
    }

    public long getProcessInstanceId() {
        return processInstanceId;
    }

    public UUID getUUID() {
    	return this.uuid;
    }

    public String getType() {
        return this.type;
    }


    public String getMetric(String name) {
        Object metric = this.customMetrics.get(name);
        if (metric != null) {
            return metric.toString();
        }
        return null;
    }
    
    public void addCustomMetric(String name, Object value) {
        this.customMetrics.put(name, value);
    }

    @Override
    public String toString() {
        return "GenericSimulationEvent[process=" + processId + ", instance=" + processInstanceId + ", uuid=" + uuid + "]";
    }
    
    

}
