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

import org.jbpm.simulation.ActivitySimulator;
import org.jbpm.simulation.SimulationContext;
import org.jbpm.simulation.SimulationEvent;
import org.jbpm.simulation.impl.events.EndSimulationEvent;
import org.kie.api.definition.process.Node;
import org.kie.api.runtime.process.NodeInstance;
import org.kie.api.runtime.process.ProcessInstance;

public class EndEventSimulator implements ActivitySimulator {

    public SimulationEvent simulate(Object activity, SimulationContext context) {
        NodeInstance nodeInstance = (NodeInstance) activity;
        long startTime = context.getClock().getCurrentTime();
        ProcessInstance pi = nodeInstance.getProcessInstance();
        
        Node node = nodeInstance.getNode();
        String bpmn2NodeId = (String) node.getMetaData().get("UniqueId");
        
        String processName = pi.getProcessName();
        String processVer = pi.getProcess().getVersion();
        if (processVer == null) {
            processVer = "";
        }
        // set end time for processinstance end time
        context.setMaxEndTime(context.getClock().getCurrentTime());
        return new EndSimulationEvent(pi.getProcessId(), context.getProcessInstanceId(), startTime, context.getClock().getCurrentTime(), context.getStartTime(), bpmn2NodeId, node.getName(), processName, processVer);
    }

}
