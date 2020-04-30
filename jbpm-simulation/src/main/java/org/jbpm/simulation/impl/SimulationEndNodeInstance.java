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

import java.util.Map;

import org.jbpm.simulation.ActivitySimulator;
import org.jbpm.simulation.SimulationContext;
import org.jbpm.simulation.SimulationEvent;
import org.jbpm.workflow.instance.NodeInstanceContainer;
import org.jbpm.workflow.instance.node.EndNodeInstance;
import org.kie.api.runtime.process.NodeInstance;

public class SimulationEndNodeInstance extends EndNodeInstance {

    private static final long serialVersionUID = 4148987012107271001L;

    @Override
    public void internalTrigger(NodeInstance from, String type) {
        SimulationContext context = SimulationContext.getContext();
        
        ActivitySimulator simulator = context.getRegistry().getSimulator(getNode());
        SimulationEvent event = simulator.simulate(this, context);
        
        context.getRepository().storeEvent(event);

        // process event definitions if any
        Map<String, String> throwEvents = context.getCurrentPath().getThrowEvents();
        String throwReference = throwEvents.get(getNode().getMetaData().get("UniqueId"));
        if (throwReference != null) {
            getProcessInstance().signalEvent(throwReference, null);
        }

        ((NodeInstanceContainer) getNodeInstanceContainer()).nodeInstanceCompleted(this, null);
    }

    
}
