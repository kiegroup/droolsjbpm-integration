package org.jbpm.simulation.impl.simulators;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.jbpm.simulation.ActivitySimulator;
import org.jbpm.simulation.SimulationContext;
import org.jbpm.simulation.SimulationDataProvider;
import org.jbpm.simulation.SimulationEvent;
import org.jbpm.simulation.impl.events.GatewaySimulationEvent;
import org.kie.api.definition.process.Node;
import org.kie.api.runtime.process.NodeInstance;
import org.kie.api.runtime.process.ProcessInstance;


public class GatewaySimulator implements ActivitySimulator {

    public SimulationEvent simulate(Object activity, SimulationContext context) {
        NodeInstance gateway = (NodeInstance) activity;
        long startTime = context.getClock().getCurrentTime();
        Map<String, Object> metadata = gateway.getNode().getMetaData();
        
        ProcessInstance pi = gateway.getProcessInstance();
        Node node = gateway.getNode();
        String bpmn2NodeId = (String) metadata.get("UniqueId");
        // todo
        long duration = 0;
        
        long endTime = startTime + duration;

        context.getClock().advanceTime(duration, TimeUnit.MILLISECONDS);
        // set end time for processinstance end time
        context.setMaxEndTime(context.getClock().getCurrentTime());
        SimulationDataProvider provider = context.getDataProvider();
        String type = (String) provider.getProcessDataForNode(node).get("node.type");

        return new GatewaySimulationEvent(pi.getProcessId(), context.getProcessInstanceId(), startTime, endTime, bpmn2NodeId, node.getName(), type);
    }
    
}
