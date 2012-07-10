package org.jbpm.simulation;

public interface SimulationEvent {

    String getProcessId();
    
    long getProcessInstanceId();
    
    String getMetric(String name);
}
