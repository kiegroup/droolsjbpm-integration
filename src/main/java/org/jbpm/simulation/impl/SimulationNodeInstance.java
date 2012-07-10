package org.jbpm.simulation.impl;

import java.util.List;

import org.drools.definition.process.Connection;
import org.drools.runtime.process.NodeInstance;
import org.jbpm.simulation.ActivitySimulator;
import org.jbpm.simulation.SimulationContext;
import org.jbpm.simulation.SimulationEvent;
import org.jbpm.workflow.core.Node;
import org.jbpm.workflow.instance.impl.NodeInstanceImpl;

public class SimulationNodeInstance extends NodeInstanceImpl {

    private static final long serialVersionUID = -1965605499505300424L;

    @Override
    public void internalTrigger(NodeInstance from, String type) {
        System.out.println("Triggered " + getNode().getName() + " id " + getNode().getMetaData().get("UniqueId") + this.toString());
        
        SimulationContext context = SimulationContext.getContext();
       
        ActivitySimulator simulator = context.getRegistry().getSimulator(getNode());
        SimulationEvent event = simulator.simulate(this, context);
        
        context.getRepository().storeEvent(event);
        long thisNodeCurrentTime = context.getCurrentTime();
        
        List<Connection> outgoing = getNode().getOutgoingConnections().get(Node.CONNECTION_DEFAULT_TYPE);
        for (Connection conn : outgoing) {
            if (context.getCurrentPath().contains(conn.getMetaData().get("UniqueId"))) {
                
                triggerConnection(conn);
                context.setCurrentTime(thisNodeCurrentTime);
            }
        }
    }

}
