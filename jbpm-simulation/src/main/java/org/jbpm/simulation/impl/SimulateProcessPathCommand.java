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

import org.drools.core.command.impl.ExecutableCommand;
import org.drools.core.command.impl.RegistryContext;
import org.drools.core.event.DefaultProcessEventListener;
import org.drools.core.time.SessionPseudoClock;
import org.jbpm.simulation.SimulationContext;
import org.jbpm.simulation.SimulationInfo;
import org.jbpm.simulation.impl.events.ProcessInstanceEndSimulationEvent;
import org.kie.api.event.process.ProcessStartedEvent;
import org.kie.api.runtime.Context;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.process.ProcessInstance;

import java.util.ArrayList;
import java.util.List;

public class SimulateProcessPathCommand implements ExecutableCommand<KieSession> {

    private static final long serialVersionUID = 3485947845100224769L;

    private String processId;
    private SimulationContext simContext;
    private SimulationPath path;
    
    public SimulateProcessPathCommand(String processId, SimulationContext context, SimulationPath path) {
        this.processId = processId;
        this.simContext = context;
        this.path = path;
    }
    
    public KieSession execute(Context context ) {
        
        KieSession session = ((RegistryContext)context).lookup(KieSession.class);

        session.getEnvironment().set("NodeInstanceFactoryRegistry", SimulationNodeInstanceFactoryRegistry.getInstance());
        simContext.setClock((SessionPseudoClock) session.getSessionClock());
        simContext.setCurrentPath(path);
        SimulationInfo simInfo = simContext.getRepository().getSimulationInfo();
        if (simInfo != null) {
            simInfo.setProcessName(session.getKieBase().getProcess(processId).getName());
            simInfo.setProcessVersion(session.getKieBase().getProcess(processId).getVersion());
        }
        // reset max end time before starting new instance
        simContext.resetMaxEndTime();
        simContext.getExecutedNodes().clear();
        simContext.incrementProcessInstanceId();

        long instanceId = -1;
        ProcessInstance pi = null;
        if (path.getSignalName() != null) {
            final List<ProcessInstance> instances = new ArrayList<ProcessInstance>();
            session.addEventListener(new DefaultProcessEventListener() {
                @Override
                public void beforeProcessStarted(ProcessStartedEvent event) {
                    instances.add(event.getProcessInstance());
                }
            });
            session.signalEvent(path.getSignalName(), null);
            if (!instances.isEmpty()) {
                pi = instances.get(0);
                instanceId = session.getIdentifier()+pi.getId();
            }

        } else {
            pi = session.startProcess(processId);
            instanceId = session.getIdentifier()+pi.getId();
        }

        simContext.getRepository().storeEvent(new ProcessInstanceEndSimulationEvent(processId, instanceId,
                simContext.getStartTime(), simContext.getMaxEndTime(), path.getPathId(),
                pi.getProcessName(), pi.getProcess().getVersion()));

        return session;

    }

}
