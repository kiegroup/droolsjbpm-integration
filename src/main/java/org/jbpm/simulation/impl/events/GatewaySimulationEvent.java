package org.jbpm.simulation.impl.events;

import java.util.Date;

public class GatewaySimulationEvent extends GenericSimulationEvent {

    private String activityName;
    private String activityId;
    
    public GatewaySimulationEvent(String processId, long processInstanceId,
            long startTime, long endTime, 
            String activityId, String activityName) {
        super(processId, processInstanceId, startTime, endTime);
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

    @Override
    public String toString() {
        
        return "StartSimulationEvent[process=" + processId + ", instance=" + processInstanceId + ", activity=" + activityName + ", startTime=" + new Date(startTime)+"]";
    }
}
