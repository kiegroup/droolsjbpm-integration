package org.jbpm.simulation.impl.events;


public class ActivitySimulationEvent extends GenericSimulationEvent {
    
    protected String activityName;
    protected String activityId;
    protected long duration;
    
    
    
    public ActivitySimulationEvent(String processId, long processInstanceId,
            String activityName, String activityId, long duration) {
        super(processId, processInstanceId);
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
        
        return "ActivitySimulationEvent[process=" + processId + ", instance=" 
        + processInstanceId + ", activity=" + activityName + ", duration=" + duration/1000+" seconds]";
    }
    
    

}
