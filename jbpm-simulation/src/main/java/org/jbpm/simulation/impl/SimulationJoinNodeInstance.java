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

package org.jbpm.simulation.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.jbpm.simulation.ActivitySimulator;
import org.jbpm.simulation.SimulationContext;
import org.jbpm.simulation.SimulationEvent;
import org.jbpm.workflow.core.node.Join;
import org.jbpm.workflow.instance.impl.NodeInstanceImpl;
import org.kie.api.definition.process.Connection;
import org.kie.api.definition.process.Node;
import org.kie.api.runtime.process.NodeInstance;

public class SimulationJoinNodeInstance extends NodeInstanceImpl {

    private static final long serialVersionUID = -1965605499505300424L;

    private Map<Long, Integer> triggers = new HashMap<Long, Integer>();

    @Override
    public void internalTrigger(NodeInstance from, String type) {

        Join joinNode = (Join) getNode();

        SimulationContext context = SimulationContext.getContext();

        ActivitySimulator simulator = context.getRegistry().getSimulator(getNode());
        SimulationEvent event = simulator.simulate(this, context);

        context.getRepository().storeEvent(event);
        long thisNodeCurrentTime = context.getClock().getCurrentTime();
        boolean processOutgoing = true;
        if (joinNode.getType() == Join.TYPE_AND) {
            processOutgoing = false;
            Integer count = (Integer) this.triggers.get( from.getNodeId() );
            if ( count == null ) {
                this.triggers.put( from.getNodeId(),
                        1 );
            } else {
                this.triggers.put( from.getNodeId(), count.intValue() + 1 );
            }
            if (checkAllActivated(joinNode)) {
                processOutgoing = true;
            }
        }

        if (processOutgoing) {
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
            // handle boundary events
            for (String boundaryEvent : context.getCurrentPath().getBoundaryEventIds()) {

                Node boundaryEventNode = null;
                for (Node node : getNode().getNodeContainer().getNodes()) {

                    if (node.getMetaData().get("UniqueId").equals(boundaryEvent) &&
                            node.getMetaData().get("AttachedTo").equals(getNode().getMetaData().get("UniqueId"))) {

                        boundaryEventNode = node;
                        break;
                    }
                }
                if (boundaryEventNode != null) {
                    NodeInstance next = ((org.jbpm.workflow.instance.NodeInstanceContainer) getNodeInstanceContainer()).getNodeInstance(boundaryEventNode);
                    setNodeId(boundaryEventNode.getId());
                    this.trigger(next, org.jbpm.workflow.core.Node.CONNECTION_DEFAULT_TYPE);
                }
            }
            setNodeId(currentNodeId);
        }
        

    }

    private boolean checkAllActivated(Join join) {
        // test whether all parent nodes have been triggered
        for (final Connection connection: join.getDefaultIncomingConnections()) {
            if ( this.triggers.get( connection.getFrom().getId() ) == null ) {
                return false;
            }
        }
        return true;
    }

}
