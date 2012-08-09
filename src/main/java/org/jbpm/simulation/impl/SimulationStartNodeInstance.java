package org.jbpm.simulation.impl;

import java.util.List;

import org.drools.definition.process.Connection;
import org.drools.runtime.process.NodeInstance;
import org.jbpm.simulation.ActivitySimulator;
import org.jbpm.simulation.SimulationContext;
import org.jbpm.simulation.SimulationEvent;
import org.jbpm.workflow.core.Node;
import org.jbpm.workflow.instance.node.StartNodeInstance;

public class SimulationStartNodeInstance extends StartNodeInstance {

    private static final long serialVersionUID = -1554447958986697677L;

    @Override
    public void internalTrigger(NodeInstance from, String type) {
        
        SimulationContext context = SimulationContext.getContext();

        ActivitySimulator simulator = context.getRegistry().getSimulator(getNode());
        SimulationEvent event = simulator.simulate(this, context);
        
        context.getRepository().storeEvent(event);
        
        
        List<Connection> outgoing = getNode().getOutgoingConnections().get(Node.CONNECTION_DEFAULT_TYPE);
        for (Connection conn : outgoing) {
            if (context.getCurrentPath().contains(conn.getMetaData().get("UniqueId"))) {

                triggerConnection(conn);
            }
        }
    }
}
