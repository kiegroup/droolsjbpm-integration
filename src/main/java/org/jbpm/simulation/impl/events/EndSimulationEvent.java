package org.jbpm.simulation.impl.events;

import java.util.Date;

public class EndSimulationEvent extends GenericSimulationEvent {

    private long processDuration;
    private String activityName;
    private String activityId;
    
    public EndSimulationEvent(String processId, long processInstanceId, long startTime, long endTime, long proceesStartTime,
            String activityId, String activityName) {
        super(processId, processInstanceId, startTime, endTime);
        this.setProcessDuration(endTime - proceesStartTime);
        this.activityId = activityId;
        this.activityName = activityName;
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

    public long getProcessDuration() {
        return processDuration;
    }

    public void setProcessDuration(long processDuration) {
        this.processDuration = processDuration;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }
    
    @Override
    public String toString() {
        
        return "EndSimulationEvent[process=" + processId + ", instance=" + processInstanceId + ", activity=" + activityName + ", endTime=" + new Date(endTime) + ", processDuration=" + processDuration/1000+" seconds]";
    }
}
