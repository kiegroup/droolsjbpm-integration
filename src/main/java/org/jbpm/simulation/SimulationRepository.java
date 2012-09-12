package org.jbpm.simulation;

public interface SimulationRepository {

    void storeEvent(SimulationEvent event);
    
    SimulationInfo getSimulationInfo();
    
    void setSimulationInfo(SimulationInfo simInfo);
}
