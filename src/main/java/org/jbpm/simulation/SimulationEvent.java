package org.jbpm.simulation;

import java.util.UUID;

public interface SimulationEvent {

    String getProcessId();
    
    long getProcessInstanceId();
    
    String getMetric(String name);
    
    long getStartTime();

    long getEndTime();
    
    UUID getUUID();
}
