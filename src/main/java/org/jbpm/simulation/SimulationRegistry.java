package org.jbpm.simulation;

import java.util.HashMap;
import java.util.Map;

import org.drools.definition.process.Node;
import org.jbpm.simulation.impl.simulators.EndEventSimulator;
import org.jbpm.simulation.impl.simulators.StartEventSimulator;
import org.jbpm.simulation.impl.simulators.StateBasedActivitySimulator;
import org.jbpm.workflow.core.node.EndNode;
import org.jbpm.workflow.core.node.StartNode;



public class SimulationRegistry {
    
    protected static SimulationRegistry instance;
    
    private Map<Class<? extends Node>, ActivitySimulator> simulators = new HashMap<Class<? extends Node>, ActivitySimulator>();
    
    protected SimulationRegistry() {
        simulators.put(StartNode.class, new StartEventSimulator());
        simulators.put(EndNode.class, new EndEventSimulator());
    }
    
    public static SimulationRegistry getInstance() {
        if (instance == null) {
            instance = new SimulationRegistry();
        }
        
        return instance;
    }

    public ActivitySimulator getSimulator(Node node) {
        if (this.simulators.containsKey(node.getClass())) {
            return this.simulators.get(node.getClass());
        }
        return new StateBasedActivitySimulator();
    }
    
    public void registerSimulator(Node node, ActivitySimulator simulator) {
        
    }
}
