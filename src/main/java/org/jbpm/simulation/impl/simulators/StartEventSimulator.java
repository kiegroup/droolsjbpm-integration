package org.jbpm.simulation.impl.simulators;

import org.drools.runtime.process.NodeInstance;
import org.drools.runtime.process.ProcessInstance;
import org.jbpm.simulation.ActivitySimulator;
import org.jbpm.simulation.SimulationContext;
import org.jbpm.simulation.SimulationEvent;
import org.jbpm.simulation.impl.events.StartSimulationEvent;

public class StartEventSimulator implements ActivitySimulator {

    public SimulationEvent simulate(Object activity, SimulationContext context) {
        NodeInstance nodeInstance = (NodeInstance) activity;
        
        ProcessInstance pi = nodeInstance.getProcessInstance();

        context.setStartTime(context.getClock().getCurrentTime());
        
        return new StartSimulationEvent(pi.getProcessId(), pi.getId(), context.getStartTime());
    }

}
