package org.jbpm.simulation.impl.events;


public class ActivitySimulationEvent extends GenericSimulationEvent {
    
    protected String activityName;
    protected String activityId;
    protected long duration;

    protected String type;
    
    public ActivitySimulationEvent(String processId, long processInstanceId,
            String activityName, String activityId, long duration, long startTime, long endTime, String type) {
        super(processId, processInstanceId, startTime, endTime, type);
        this.activityName = activityName;
        this.activityId = activityId;
        this.duration = duration;
    }
    

    public String getActivityName() {
        return this.activityName;
    }

    public String getActivityId() {
        return this.activityId;
    }

    public long getDuration() {
        return this.duration;
    }


    @Override
    public String toString() {
        
        return "ActivitySimulationEvent[process=" + processId + ", type = " + type + " instance="
        + processInstanceId + ", activity=" + activityName + ", duration=" + duration/1000+" seconds]";
    }


    public String getType() {
        return this.type;
    }
}
