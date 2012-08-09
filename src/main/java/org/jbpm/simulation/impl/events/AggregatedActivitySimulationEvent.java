package org.jbpm.simulation.impl.events;


public class AggregatedActivitySimulationEvent {

    protected String activityName;
    protected String activityId;
    
    protected double minExecutionTime;
    protected double avgExecutionTime;
    protected double maxExecutionTime;
    
    
    
    public AggregatedActivitySimulationEvent(String activityName, String activityId,
            double minExecutionTime, double avgExecutionTime,
            double maxExecutionTime) {
        super();
        this.activityName = activityName;
        this.activityId = activityId;
        this.minExecutionTime = minExecutionTime;
        this.avgExecutionTime = avgExecutionTime;
        this.maxExecutionTime = maxExecutionTime;
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
    
    
}
