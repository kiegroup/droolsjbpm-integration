package org.jbpm.simulation.impl.simulators;

import org.drools.runtime.process.NodeInstance;
import org.drools.runtime.process.ProcessInstance;
import org.jbpm.simulation.ActivitySimulator;
import org.jbpm.simulation.SimulationContext;
import org.jbpm.simulation.SimulationEvent;
import org.jbpm.simulation.impl.events.EndSimulationEvent;

public class EndEventSimulator implements ActivitySimulator {

    public SimulationEvent simulate(Object activity, SimulationContext context) {
        NodeInstance nodeInstance = (NodeInstance) activity;
        
        ProcessInstance pi = nodeInstance.getProcessInstance();
        
        return new EndSimulationEvent(pi.getProcessId(), pi.getId(), context.getStartTime(), context.getClock().getCurrentTime());
    }

}
