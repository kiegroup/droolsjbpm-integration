package org.jbpm.simulation.impl.simulators;

import org.jbpm.simulation.ActivitySimulator;
import org.jbpm.simulation.SimulationContext;
import org.jbpm.simulation.SimulationEvent;
import org.jbpm.simulation.impl.events.StartSimulationEvent;
import org.kie.api.definition.process.Node;
import org.kie.api.runtime.process.NodeInstance;
import org.kie.api.runtime.process.ProcessInstance;

public class StartEventSimulator implements ActivitySimulator {

    public SimulationEvent simulate(Object activity, SimulationContext context) {
        NodeInstance nodeInstance = (NodeInstance) activity;
        
        ProcessInstance pi = nodeInstance.getProcessInstance();

        context.setStartTime(context.getClock().getCurrentTime());
        Node node = nodeInstance.getNode();
        String bpmn2NodeId = (String) node.getMetaData().get("UniqueId");

        // set end time for processinstance end time
        context.setMaxEndTime(context.getClock().getCurrentTime());
        
        return new StartSimulationEvent(pi.getProcessId(), context.getProcessInstanceId(), context.getStartTime(), context.getClock().getCurrentTime(), bpmn2NodeId, node.getName());
    }

}
