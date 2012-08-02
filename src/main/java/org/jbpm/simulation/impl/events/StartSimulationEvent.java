package org.jbpm.simulation.impl.events;

import java.util.Date;


public class StartSimulationEvent extends GenericSimulationEvent {

    
    public StartSimulationEvent(String processId, long processInstanceId, long startTime, long endTime) {
        super(processId, processInstanceId, startTime, endTime);

    }

    @Override
    public String toString() {
        
        return "StartSimulationEvent[process=" + processId + ", instance=" + processInstanceId + ", startTime=" + new Date(startTime)+"]";
    }

}
