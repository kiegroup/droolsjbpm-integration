package org.drools.simulation.impl;

import java.util.HashMap;
import java.util.Map;

import org.drools.command.Context;
import org.drools.simulation.Path;
import org.drools.simulation.Simulation;

public class SimulationImpl implements Simulation {    
    private Map<String, Path> paths;
    
    public SimulationImpl() {
        this.paths = new HashMap<String, Path>();
    }
    
    public Map<String, Path> getPaths() {
        return this.paths;
    }
        
}
