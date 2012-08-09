package org.jbpm.simulation.impl.events;

public class HumanTaskActivitySimulationEvent extends GenericSimulationEvent {

    private double resourceCost;
    private long waitTime;
    protected String activityName;
    protected String activityId;
    protected long duration;
    
    public HumanTaskActivitySimulationEvent(String processId,
            long processInstanceId, String activityName, String activityId,
            long duration, long waitTime, double resourceCost, long startTime, long endTime) {
        
        super(processId, processInstanceId, startTime, endTime);
        this.duration = duration;
        this.activityId = activityId;
        this.activityName = activityName;
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

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    @Override
    public String toString() {
        
        return "UserTaskActivitySimulationEvent[process=" + processId + ", instance=" 
        + processInstanceId + ", activity=" + activityName + ", duration=" + duration/1000+" seconds" +
        		", wait time=" + waitTime/1000 + " seconds , resource cost=" +resourceCost +"]";
    }
}
