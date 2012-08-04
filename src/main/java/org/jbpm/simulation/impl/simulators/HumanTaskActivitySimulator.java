package org.jbpm.simulation.impl.simulators;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.drools.definition.process.Node;
import org.drools.runtime.process.NodeInstance;
import org.drools.runtime.process.ProcessInstance;
import org.jbpm.simulation.ActivitySimulator;
import org.jbpm.simulation.SimulationContext;
import org.jbpm.simulation.SimulationDataProvider;
import org.jbpm.simulation.SimulationEvent;
import org.jbpm.simulation.TimeGenerator;
import org.jbpm.simulation.TimeGeneratorFactory;
import org.jbpm.simulation.impl.events.HumanTaskActivitySimulationEvent;
import org.jbpm.simulation.impl.ht.StaffPool;

public class HumanTaskActivitySimulator implements ActivitySimulator {

    public SimulationEvent simulate(Object activity, SimulationContext context) {
        long startTime = context.getClock().getCurrentTime();
        NodeInstance stateNode = (NodeInstance) activity;
        
        Map<String, Object> metadata = stateNode.getNode().getMetaData();
        
        ProcessInstance pi = stateNode.getProcessInstance();
        Node node = stateNode.getNode();
        String bpmn2NodeId = (String) metadata.get("UniqueId");
        SimulationDataProvider provider = context.getDataProvider();
        
        TimeGenerator timeGenerator=TimeGeneratorFactory.newTimeGenerator(provider.getSimulationDataForNode(node));
        long duration = timeGenerator.generateTime();
        
        context.getStaffPoolManager().registerPool(pi.getProcessId(), node, 1);
        StaffPool pool = context.getStaffPoolManager().getActivityPool(node.getName());
        
        long waitTime = pool.allocate(context.getClock().getCurrentTime());
        double resourceCost = 2.5;
        
        context.getClock().advanceTime((duration + waitTime), TimeUnit.MILLISECONDS);
        
        return new HumanTaskActivitySimulationEvent(pi.getProcessId(), pi.getId(), node.getName(),
                bpmn2NodeId, duration, waitTime, resourceCost, startTime, context.getClock().getCurrentTime());
    }

}
