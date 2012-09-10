package org.jbpm.simulation.impl.events;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jbpm.simulation.AggregatedSimulationEvent;

public class AggregatedProcessSimulationEvent implements AggregatedSimulationEvent {

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
}
