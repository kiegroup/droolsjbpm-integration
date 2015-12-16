/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
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

package org.jbpm.simulation.impl;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.jbpm.simulation.ActivitySimulator;
import org.jbpm.simulation.SimulationContext;
import org.jbpm.simulation.SimulationEvent;
import org.jbpm.workflow.instance.impl.NodeInstanceImpl;
import org.kie.api.definition.process.Connection;
import org.kie.api.definition.process.Node;
import org.kie.api.runtime.process.NodeInstance;

public class SimulationEventNodeInstance extends NodeInstanceImpl {

    private static final long serialVersionUID = -1965605499505300424L;

    @Override
    public void internalTrigger(NodeInstance from, String type) {
        
        SimulationContext context = SimulationContext.getContext();
       
        ActivitySimulator simulator = context.getRegistry().getSimulator(getNode());
        SimulationEvent event = simulator.simulate(this, context);
        
        context.getRepository().storeEvent(event);
        long thisNodeCurrentTime = context.getClock().getCurrentTime();
        
        List<Connection> outgoing = getNode().getOutgoingConnections().get(org.jbpm.workflow.core.Node.CONNECTION_DEFAULT_TYPE);
        for (Connection conn : outgoing) {
            if (context.getCurrentPath().getSequenceFlowsIds().contains(conn.getMetaData().get("UniqueId"))) {
                // handle loops
                if (context.isLoopLimitExceeded((String) conn.getMetaData().get("UniqueId"))) {
                    continue;
                }
                context.addExecutedNode((String) conn.getMetaData().get("UniqueId"));
                triggerConnection(conn);
                // reset clock to the value of this node
                context.getClock().advanceTime((thisNodeCurrentTime - context.getClock().getCurrentTime()), TimeUnit.MILLISECONDS);
            }
        }
        long currentNodeId = getNodeId();
        // process event definitions if any
        Map<String, String> throwEvents = context.getCurrentPath().getThrowEvents();
        String throwReference = throwEvents.get(getNode().getMetaData().get("UniqueId"));
        if (throwReference != null) {
            getProcessInstance().signalEvent(throwReference, null);
        }
        setNodeId(currentNodeId);
    }

}
