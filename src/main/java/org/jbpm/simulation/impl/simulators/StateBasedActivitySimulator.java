package org.jbpm.simulation.impl.simulators;

import java.util.Map;

import org.drools.definition.process.Node;
import org.drools.runtime.process.NodeInstance;
import org.drools.runtime.process.ProcessInstance;
import org.jbpm.simulation.ActivitySimulator;
import org.jbpm.simulation.SimulationContext;
import org.jbpm.simulation.SimulationDataProvider;
import org.jbpm.simulation.SimulationEvent;
import org.jbpm.simulation.TimeGenerator;
import org.jbpm.simulation.TimeGeneratorFactory;
import org.jbpm.simulation.impl.events.ActivitySimulationEvent;

public class StateBasedActivitySimulator implements ActivitySimulator {

    public SimulationEvent simulate(Object activity, SimulationContext context) {
       NodeInstance stateNode = (NodeInstance) activity;
       
       Map<String, Object> metadata = stateNode.getNode().getMetaData();
       
       ProcessInstance pi = stateNode.getProcessInstance();
       Node node = stateNode.getNode();
       String bpmn2NodeId = (String) metadata.get("UniqueId");
       SimulationDataProvider provider = context.getDataProvider();
       
       TimeGenerator timeGenerator=TimeGeneratorFactory.newTimeGenerator(provider.getSimulationDataForNode(pi.getProcessId(), bpmn2NodeId));
       long duration = timeGenerator.generateTime();
       
       // TODO calculate duration based on various strategies
       
       context.setCurrentTime(context.getCurrentTime() + duration);
       
       return new ActivitySimulationEvent(pi.getProcessId(), pi.getId(), node.getName(), bpmn2NodeId, duration);
    }

}
