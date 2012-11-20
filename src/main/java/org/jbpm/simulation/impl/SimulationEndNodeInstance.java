package org.jbpm.simulation.impl;

import org.jbpm.simulation.ActivitySimulator;
import org.jbpm.simulation.SimulationContext;
import org.jbpm.simulation.SimulationEvent;
import org.jbpm.workflow.instance.NodeInstanceContainer;
import org.jbpm.workflow.instance.node.EndNodeInstance;
import org.kie.runtime.process.NodeInstance;

public class SimulationEndNodeInstance extends EndNodeInstance {

    private static final long serialVersionUID = 4148987012107271001L;

    @Override
    public void internalTrigger(NodeInstance from, String type) {
        SimulationContext context = SimulationContext.getContext();
        
        ActivitySimulator simulator = context.getRegistry().getSimulator(getNode());
        SimulationEvent event = simulator.simulate(this, context);
        
        context.getRepository().storeEvent(event);
        ((NodeInstanceContainer) getNodeInstanceContainer()).nodeInstanceCompleted(this, null);
    }

    
}
