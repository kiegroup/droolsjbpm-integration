package org.jbpm.simulation.impl.events;

public class HTAggregatedSimulationEvent extends AggregatedActivitySimulationEvent {

    public HTAggregatedSimulationEvent(String activityName, String activityId,
            double minExecutionTime, double avgExecutionTime,
            double maxExecutionTime, double minWaitTime, double avgWaitTime,
            double maxWaitTime, double resourceUtilization) {
        
        super(activityName, activityId, minExecutionTime, avgExecutionTime,
                maxExecutionTime);
        this.minWaitTime = minWaitTime;
        this.avgWaitTime = avgWaitTime;
        this.maxWaitTime = maxWaitTime;
        this.resourceUtilization = resourceUtilization;
    }


    private double minWaitTime;
    private double avgWaitTime;
    private double maxWaitTime;
    
    
    
    private double resourceUtilization;

    public double getMinWaitTime() {
        return minWaitTime;
    }

    public void setMinWaitTime(double minWaitTime) {
        this.minWaitTime = minWaitTime;
    }

    public double getAvgWaitTime() {
        return avgWaitTime;
    }

    public void setAvgWaitTime(double avgWaitTime) {
        this.avgWaitTime = avgWaitTime;
    }

    public double getMaxWaitTime() {
        return maxWaitTime;
    }

    public void setMaxWaitTime(double maxWaitTime) {
        this.maxWaitTime = maxWaitTime;
    }

    public double getResourceUtilization() {
        return resourceUtilization;
    }

    public void setResourceUtilization(double resourceUtilization) {
        this.resourceUtilization = resourceUtilization;
    }
    
    
}
