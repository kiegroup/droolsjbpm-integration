package org.jbpm.simulation.impl.events;

public class HumanTaskActivitySimulationEvent extends ActivitySimulationEvent {

    private double resourceCost;
    private long waitTime;
    
    public HumanTaskActivitySimulationEvent(String processId,
            long processInstanceId, String activityName, String activityId,
            long duration, long waitTime, double resourceCost) {
        
        super(processId, processInstanceId, activityName, activityId, duration);
        this.waitTime = waitTime;
        this.resourceCost = resourceCost;

    }

    public double getResourceCost() {
        return resourceCost;
    }

    public void setResourceCost(double resourceCost) {
        this.resourceCost = resourceCost;
    }

    public long getWaitTime() {
        return waitTime;
    }

    public void setWaitTime(long waitTime) {
        this.waitTime = waitTime;
    }

    @Override
    public String toString() {
        
        return "UserTaskActivitySimulationEvent[process=" + processId + ", instance=" 
        + processInstanceId + ", activity=" + activityName + ", duration=" + duration/1000+" seconds" +
        		", wait time=" + waitTime/1000 + " seconds , resource cost=" +resourceCost +"]";
    }
}
