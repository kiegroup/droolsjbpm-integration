package org.jbpm.simulation.impl.events;

import org.jbpm.simulation.AggregatedSimulationEvent;


public class AggregatedActivitySimulationEvent implements AggregatedSimulationEvent {

    protected String activityName;
    protected String activityId;
    
    protected double minExecutionTime;
    protected double avgExecutionTime;
    protected double maxExecutionTime;
    
    protected long numberOfInstances; 
    
    
    public AggregatedActivitySimulationEvent(String activityName, String activityId,
            double minExecutionTime, double avgExecutionTime,
            double maxExecutionTime, long numberOfInstances) {
        super();
        this.activityName = activityName;
        this.activityId = activityId;
        this.minExecutionTime = minExecutionTime;
        this.avgExecutionTime = avgExecutionTime;
        this.maxExecutionTime = maxExecutionTime;
        this.numberOfInstances = numberOfInstances;
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
    public String getActivityName() {
        return activityName;
    }
    public void setActivityName(String activityName) {
        this.activityName = activityName;
    }
    public String getActivityId() {
        return activityId;
    }
    public void setActivityId(String activityId) {
        this.activityId = activityId;
    }
    public Object getProperty(String name) {
        if ("activityId".equalsIgnoreCase(name)) {
         
            return activityId;
        } else if ("activityName".equalsIgnoreCase(name)) {
            
            return activityName;
        } else if ("minExecutionTime".equalsIgnoreCase(name)) {
            
            return minExecutionTime;
        } else if ("avgExecutionTime".equalsIgnoreCase(name)) {
         
            return avgExecutionTime;
        } else if ("maxExecutionTime".equalsIgnoreCase(name)) {
         
            return maxExecutionTime;
        }
        
        return null;
    }
    public long getNumberOfInstances() {
        return numberOfInstances;
    }
    public void setNumberOfInstances(long numberOfInstances) {
        this.numberOfInstances = numberOfInstances;
    }
    
    
}
