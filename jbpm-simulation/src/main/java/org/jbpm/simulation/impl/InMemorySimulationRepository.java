package org.jbpm.simulation.impl;

import java.util.ArrayList;
import java.util.List;

import org.jbpm.simulation.SimulationEvent;
import org.jbpm.simulation.SimulationInfo;
import org.jbpm.simulation.SimulationRepository;

public class InMemorySimulationRepository implements SimulationRepository {

    protected List<SimulationEvent> events = new ArrayList<SimulationEvent>();
    
    protected SimulationInfo simulationInfo;
    
    public void storeEvent(SimulationEvent event) {
        this.events.add(event);
    }

    public List<SimulationEvent> getEvents() {
        return this.events;
    }
    
    public void close() {
    	this.events.clear();
    }

    public SimulationInfo getSimulationInfo() {
        return this.simulationInfo;
    }

    public void setSimulationInfo(SimulationInfo simInfo) {
        this.simulationInfo = simInfo;
        
    }
}
