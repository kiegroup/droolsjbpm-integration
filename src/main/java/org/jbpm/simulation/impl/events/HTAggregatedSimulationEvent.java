package org.jbpm.simulation.impl.events;

public class HTAggregatedSimulationEvent extends AggregatedActivitySimulationEvent {

    public HTAggregatedSimulationEvent(String activityName, String activityId,
            double minExecutionTime, double avgExecutionTime,
            double maxExecutionTime, double minWaitTime, double avgWaitTime,
            double maxWaitTime, double minResourceUtilization, double avgResourceUtilization, double maxResourceUtilization) {
        
        super(activityName, activityId, minExecutionTime, avgExecutionTime,
                maxExecutionTime);
        this.minWaitTime = minWaitTime;
        this.avgWaitTime = avgWaitTime;
        this.maxWaitTime = maxWaitTime;
        
        this.minResourceUtilization = minResourceUtilization;
        this.avgResourceUtilization = avgResourceUtilization;
        this.maxResourceUtilization = maxResourceUtilization;
    }


    private double minWaitTime;
    private double avgWaitTime;
    private double maxWaitTime;
    
    private double minResourceUtilization;
    private double avgResourceUtilization;
    private double maxResourceUtilization;
    
    

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

    public double getMinResourceUtilization() {
        return minResourceUtilization;
    }

    public void setMinResourceUtilization(double minResourceUtilization) {
        this.minResourceUtilization = minResourceUtilization;
    }

    public double getAvgResourceUtilization() {
        return avgResourceUtilization;
    }

    public void setAvgResourceUtilization(double avgResourceUtilization) {
        this.avgResourceUtilization = avgResourceUtilization;
    }

    public double getMaxResourceUtilization() {
        return maxResourceUtilization;
    }

    public void setMaxResourceUtilization(double maxResourceUtilization) {
        this.maxResourceUtilization = maxResourceUtilization;
    }

    
}
