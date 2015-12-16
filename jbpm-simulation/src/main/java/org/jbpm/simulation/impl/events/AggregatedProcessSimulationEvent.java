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

package org.jbpm.simulation.impl.events;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.jbpm.simulation.AggregatedSimulationEvent;

public class AggregatedProcessSimulationEvent implements AggregatedSimulationEvent {

    protected String type;

    protected String processId;
    protected String processName;
    protected String processVersion;
    
    protected double minExecutionTime;
    protected double avgExecutionTime;
    protected double maxExecutionTime;
    
    protected Map<String, Integer> pathInstances = new HashMap<String, Integer>();
    
    public AggregatedProcessSimulationEvent(Object processInfo,
            double minExecutionTime, double avgExecutionTime, double maxExecutionTime) {
        super();
        setProcessInfoValues(processInfo);
        
        this.minExecutionTime = minExecutionTime;
        this.avgExecutionTime = avgExecutionTime;
        this.maxExecutionTime = maxExecutionTime;
        this.type = "process";
    }
    
    protected void setProcessInfoValues(Object processInfo) {
        if (processInfo instanceof Set && !((Set) processInfo).isEmpty()) {
            
            String values = (String) ((Set) processInfo).toArray()[0];
            String[] splitValues = values.split("@");
            if (splitValues.length > 0) {
                this.processId = splitValues[0];
            }
            
            if (splitValues.length > 1) {
                this.processName = splitValues[1];
            }
            
            if (splitValues.length > 2) {
                this.processVersion = splitValues[2];
            }
        }
    }

    public double getMinExecutionTime() {
        return minExecutionTime;
    }
    public void setMinExecutionTime(double minExecutionTime) {
        this.minExecutionTime = minExecutionTime;
    }
    public double getAvgExecutionTime() {
        return avgExecutionTime;
    }
    public void setAvgExecutionTime(double avgExecutionTime) {
        this.avgExecutionTime = avgExecutionTime;
    }
    public double getMaxExecutionTime() {
        return maxExecutionTime;
    }
    public void setMaxExecutionTime(double maxExecutionTime) {
        this.maxExecutionTime = maxExecutionTime;
    }

    public Object getProperty(String name) {
        if ("minExecutionTime".equalsIgnoreCase(name)) {
            
            return minExecutionTime;
        } else if ("avgExecutionTime".equalsIgnoreCase(name)) {
            
            return avgExecutionTime;
        } else if ("maxExecutionTime".equalsIgnoreCase(name)) {
            
            return maxExecutionTime;
        }
        return null;
    }

    public String getType() {
        return this.type;
    }

    public String getProcessId() {
        return processId;
    }

    public void setProcessId(String processId) {
        this.processId = processId;
    }

    public String getProcessName() {
        return processName;
    }

    public void setProcessName(String processName) {
        this.processName = processName;
    }

    public String getProcessVersion() {
        return processVersion;
    }

    public void setProcessVersion(String processVersion) {
        this.processVersion = processVersion;
    }

    public void calculatePaths(List<String> pathIds) {
        for (String pathId : pathIds) {
            Integer current = pathInstances.get(pathId);
            if (current == null) {
                current = 1;
            } else {
                current++;
            }
            pathInstances.put(pathId, current);
        }
    }
    
    public void calculateAggregatedPaths(List<String> pathIds) {
        for (String pathInfo : pathIds) {
            String[] entires = pathInfo.split(";");
            
            for (String entry : entires) {
                String[] keyValue = entry.split("=");
                int current = 0;
                int nextValue = Integer.valueOf(keyValue[1]);
                if (pathInstances.containsKey(keyValue[0])) {
                    current = pathInstances.get(keyValue[0]);
                }
                if (current < nextValue) {
                    pathInstances.put(keyValue[0], nextValue);
                }
            }
        }
    }
    
    public String getPathInfo() {
        StringBuffer info = new StringBuffer();
        for(Entry<String, Integer> entries : pathInstances.entrySet()) {
            info.append(entries.getKey()+"="+entries.getValue()+";");
        }
        info.deleteCharAt(info.length()-1);
        return info.toString();
    }
    
    public Map<String, Integer> getPathNumberOfInstances() {
        return this.pathInstances;
    }
    
    public Integer getNumberOfInstancesPerPath(String pathId) {
        return this.pathInstances.get(pathId);
    }
}
