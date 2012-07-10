package org.jbpm.simulation.impl.events;

import java.util.Date;

public class EndSimulationEvent extends GenericSimulationEvent {

    private long processDuration;
    private long endTime;
    
    public EndSimulationEvent(String processId, long processInstanceId, long startTime, long endTime) {
        super(processId, processInstanceId);
        this.setEndTime(endTime);
        this.setProcessDuration(endTime - startTime);
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
        
        return "EndSimulationEvent[process=" + processId + ", instance=" + processInstanceId + ", endTime=" + new Date(endTime) + ", processDuration=" + processDuration/1000+" seconds]";
    }
}
