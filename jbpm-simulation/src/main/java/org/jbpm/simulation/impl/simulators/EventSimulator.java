/*
 * Copyright 2015 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package org.jbpm.simulation.impl.simulators;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.jbpm.simulation.ActivitySimulator;
import org.jbpm.simulation.SimulationContext;
import org.jbpm.simulation.SimulationDataProvider;
import org.jbpm.simulation.SimulationEvent;
import org.jbpm.simulation.TimeGenerator;
import org.jbpm.simulation.TimeGeneratorFactory;
import org.jbpm.simulation.impl.events.ActivitySimulationEvent;
import org.kie.api.definition.process.Node;
import org.kie.api.runtime.process.NodeInstance;
import org.kie.api.runtime.process.ProcessInstance;

public class EventSimulator implements ActivitySimulator  {

    public SimulationEvent simulate(Object activity, SimulationContext context) {
        NodeInstance eventNodeInstance = (NodeInstance) activity;
        long startTime = context.getClock().getCurrentTime();
        Map<String, Object> metadata = eventNodeInstance.getNode().getMetaData();
        
        ProcessInstance pi = eventNodeInstance.getProcessInstance();
        Node node = eventNodeInstance.getNode();
        String bpmn2NodeId = (String) metadata.get("UniqueId");
        SimulationDataProvider provider = context.getDataProvider();
        
        TimeGenerator timeGenerator=TimeGeneratorFactory.newTimeGenerator(provider.getSimulationDataForNode(node));
        long duration = timeGenerator.generateTime();
        
        context.getClock().advanceTime(duration, TimeUnit.MILLISECONDS);
        // set end time for processinstance end time
        context.setMaxEndTime(context.getClock().getCurrentTime());

        String type = (String) provider.getProcessDataForNode(node).get("node.type");

        return new ActivitySimulationEvent(pi.getProcessId(), context.getProcessInstanceId(), node.getName(), bpmn2NodeId, duration,
                startTime, context.getClock().getCurrentTime(), type);
    }

}
