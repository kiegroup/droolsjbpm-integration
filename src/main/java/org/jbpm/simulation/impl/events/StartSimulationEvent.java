package org.jbpm.simulation.impl.events;

import java.util.Date;


public class StartSimulationEvent extends GenericSimulationEvent {

    private long startTime;
    
    public StartSimulationEvent(String processId, long processInstanceId, long startTime) {
        super(processId, processInstanceId);
        this.setStartTime(startTime);
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    @Override
    public String toString() {
        
        return "StartSimulationEvent[process=" + processId + ", instance=" + processInstanceId + ", startTime=" + new Date(startTime)+"]";
    }

}
