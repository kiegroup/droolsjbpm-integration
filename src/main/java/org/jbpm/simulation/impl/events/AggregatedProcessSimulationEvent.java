package org.jbpm.simulation.impl.events;

public class AggregatedProcessSimulationEvent {

    protected double minExecutionTime;
    protected double avgExecutionTime;
    protected double maxExecutionTime;
    
    public AggregatedProcessSimulationEvent(double minExecutionTime,
            double avgExecutionTime, double maxExecutionTime) {
        super();
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
    
    
}
