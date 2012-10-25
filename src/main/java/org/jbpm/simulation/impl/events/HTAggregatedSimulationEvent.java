package org.jbpm.simulation.impl.events;

public class HTAggregatedSimulationEvent extends AggregatedActivitySimulationEvent {

    public HTAggregatedSimulationEvent(String activityName, String activityId,
            double minExecutionTime, double avgExecutionTime,
            double maxExecutionTime, double minWaitTime, double avgWaitTime,
            double maxWaitTime, double minResourceUtilization, double avgResourceUtilization, 
            double maxResourceUtilization, long numberOfInstances, double avgResourceCost,
            double minResourceCost, double maxResourceCost, String type) {
        
        super(activityName, activityId, minExecutionTime, avgExecutionTime,
                maxExecutionTime, numberOfInstances, type);
        this.minWaitTime = minWaitTime;
        this.avgWaitTime = avgWaitTime;
        this.maxWaitTime = maxWaitTime;
        
        this.minResourceUtilization = minResourceUtilization;
        this.avgResourceUtilization = avgResourceUtilization;
        this.maxResourceUtilization = maxResourceUtilization;
        
        this.minResourceCost = minResourceCost;
        this.avgResourceCost = avgResourceCost;
        this.maxResourceCost = maxResourceCost;
    }


    private double minWaitTime;
    private double avgWaitTime;
    private double maxWaitTime;
    
    private double minResourceUtilization;
    private double avgResourceUtilization;
    private double maxResourceUtilization;
    
    private double minResourceCost;
    private double avgResourceCost;
    private double maxResourceCost;

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

    @Override
    public Object getProperty(String name) {
        
        Object value = super.getProperty(name);
        
        if (value != null) {
            return value;
        }
        
        if ("minWaitTime".equalsIgnoreCase(name)) {
         
            return minWaitTime;
        } else if ("avgWaitTime".equalsIgnoreCase(name)) {
            
            return avgWaitTime;
        } else if ("maxWaitTime".equalsIgnoreCase(name)) {
            
            return maxWaitTime;
        } else if ("minResourceUtilization".equalsIgnoreCase(name)) {
         
            return minResourceUtilization;
        } else if ("avgResourceUtilization".equalsIgnoreCase(name)) {
            
            return avgResourceUtilization;
        } else if ("maxResourceUtilization".equalsIgnoreCase(name)) {
            
            return maxResourceUtilization;
        }
        return null;
    }

    public double getAvgResourceCost() {
        return avgResourceCost;
    }

    public void setAvgResourceCost(double avgResourceCost) {
        this.avgResourceCost = avgResourceCost;
    }

    public double getMinResourceCost() {
        return minResourceCost;
    }

    public void setMinResourceCost(double minResourceCost) {
        this.minResourceCost = minResourceCost;
    }

    public double getMaxResourceCost() {
        return maxResourceCost;
    }

    public void setMaxResourceCost(double maxResourceCost) {
        this.maxResourceCost = maxResourceCost;
    }

    
}
