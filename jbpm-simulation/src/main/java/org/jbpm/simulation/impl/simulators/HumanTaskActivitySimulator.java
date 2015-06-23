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
import org.jbpm.simulation.impl.events.HumanTaskActivitySimulationEvent;
import org.jbpm.simulation.impl.ht.StaffPool;
import org.jbpm.simulation.util.SimulationUtils;
import org.kie.api.definition.process.Node;
import org.kie.api.runtime.process.NodeInstance;
import org.kie.api.runtime.process.ProcessInstance;

public class HumanTaskActivitySimulator implements ActivitySimulator {

    public SimulationEvent simulate(Object activity, SimulationContext context) {
        long startTime = context.getClock().getCurrentTime();
        NodeInstance stateNode = (NodeInstance) activity;
        
        Map<String, Object> metadata = stateNode.getNode().getMetaData();
        
        ProcessInstance pi = stateNode.getProcessInstance();
        Node node = stateNode.getNode();
        String bpmn2NodeId = (String) metadata.get("UniqueId");
        SimulationDataProvider provider = context.getDataProvider();
        Map<String, Object> properties = provider.getSimulationDataForNode(node);
        
        TimeGenerator timeGenerator=TimeGeneratorFactory.newTimeGenerator(properties);
        long duration = timeGenerator.generateTime();
        
        context.getStaffPoolManager().registerPool(pi.getProcessId(), node, 1);
        StaffPool pool = context.getStaffPoolManager().getActivityPool(node.getName());
        
        long waitTime = pool.allocate(context.getClock().getCurrentTime());
        
        
        double resourceUtilization = pool.getResourceUtilization();
        // ensure that duration will include wait time
        duration += waitTime;
        
        TimeUnit timeUnit = SimulationUtils.getTimeUnit(properties);
        long durationInUnit = timeUnit.convert(duration, TimeUnit.MILLISECONDS);
        double resourceCost = pool.getResourceCost() * durationInUnit;
        
        context.getClock().advanceTime((duration), TimeUnit.MILLISECONDS);
        
        // set end time for processinstance end time
        context.setMaxEndTime(context.getClock().getCurrentTime());
        
        return new HumanTaskActivitySimulationEvent(pi.getProcessId(), context.getProcessInstanceId(), node.getName(),
                bpmn2NodeId, duration, waitTime, resourceCost, startTime, 
                context.getClock().getCurrentTime(), resourceUtilization);
    }

}
