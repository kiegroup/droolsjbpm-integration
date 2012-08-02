package org.jbpm.simulation.impl.events;

import java.util.Date;

public class EndSimulationEvent extends GenericSimulationEvent {

    private long processDuration;
    
    public EndSimulationEvent(String processId, long processInstanceId, long startTime, long endTime, long proceesStartTime) {
        super(processId, processInstanceId, startTime, endTime);
        this.setProcessDuration(endTime - proceesStartTime);
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
