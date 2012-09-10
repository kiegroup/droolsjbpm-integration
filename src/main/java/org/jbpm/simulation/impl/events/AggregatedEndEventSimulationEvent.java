package org.jbpm.simulation.impl.events;

import org.jbpm.simulation.AggregatedSimulationEvent;

public class AggregatedEndEventSimulationEvent implements
        AggregatedSimulationEvent {
    
    protected String activityName;
    protected String activityId;
    
    protected double minProcessDuration;
    protected double avgProcessDuration;
    protected double maxProcessDuration;
    
    protected long numberOfInstances; 
    
    public AggregatedEndEventSimulationEvent(String activityName, String activityId, double minProcessDuration,
            double avgProcessDuration, double maxProcessDuration, long numberOfInstances) {
        super();        
        this.activityName = activityName;
        this.activityId = activityId;
        this.minProcessDuration = minProcessDuration;
        this.avgProcessDuration = avgProcessDuration;
        this.maxProcessDuration = maxProcessDuration;
        this.numberOfInstances = numberOfInstances;
    }

    

    public Object getProperty(String name) {
        if ("activityId".equalsIgnoreCase(name)) {
            
            return activityId;
        } else if ("activityName".equalsIgnoreCase(name)) {
            
            return activityName;
        } else if ("minProcessDuration".equalsIgnoreCase(name)) {
            
            return minProcessDuration;
        } else if ("avgProcessDuration".equalsIgnoreCase(name)) {
         
            return avgProcessDuration;
        } else if ("maxProcessDuration".equalsIgnoreCase(name)) {
         
            return maxProcessDuration;
        }
        return null;
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



    public double getMinProcessDuration() {
        return minProcessDuration;
    }



    public void setMinProcessDuration(double minProcessDuration) {
        this.minProcessDuration = minProcessDuration;
    }



    public double getAvgProcessDuration() {
        return avgProcessDuration;
    }



    public void setAvgProcessDuration(double avgProcessDuration) {
        this.avgProcessDuration = avgProcessDuration;
    }



    public double getMaxProcessDuration() {
        return maxProcessDuration;
    }



    public void setMaxProcessDuration(double maxProcessDuration) {
        this.maxProcessDuration = maxProcessDuration;
    }



    public long getNumberOfInstances() {
        return numberOfInstances;
    }



    public void setNumberOfInstances(long numberOfInstances) {
        this.numberOfInstances = numberOfInstances;
    }

}
