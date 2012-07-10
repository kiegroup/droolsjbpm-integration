package org.jbpm.simulation.impl;

import java.util.ArrayList;
import java.util.List;

import org.jbpm.simulation.SimulationEvent;
import org.jbpm.simulation.SimulationRepository;

public class InMemorySimulationRepository implements SimulationRepository {

    public List<SimulationEvent> events = new ArrayList<SimulationEvent>();
    
    public void storeEvent(SimulationEvent event) {
        this.events.add(event);
        System.out.println("Event added: " + event);

    }

}
