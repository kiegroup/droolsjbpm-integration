package org.jbpm.simulation.impl;

import org.drools.core.command.impl.GenericCommand;
import org.drools.core.command.impl.KnowledgeCommandContext;
import org.drools.core.time.SessionPseudoClock;
import org.jbpm.simulation.SimulationContext;
import org.jbpm.simulation.SimulationInfo;
import org.jbpm.simulation.impl.events.ProcessInstanceEndSimulationEvent;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.internal.command.Context;

public class SimulateProcessPathCommand implements GenericCommand<Void> {

    private static final long serialVersionUID = 3485947845100224769L;

    private String processId;
    private SimulationContext simContext;
    private SimulationPath path;
    
    public SimulateProcessPathCommand(String processId, SimulationContext context, SimulationPath path) {
        this.processId = processId;
        this.simContext = context;
        this.path = path;
    }
    
    public Void execute(Context context) {              
        
        KieSession session = ((KnowledgeCommandContext)context).getKieSession();

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
        
        ProcessInstance pi = session.startProcess(processId);
        long instanceId = session.getId()+pi.getId();
        simContext.getRepository().storeEvent(new ProcessInstanceEndSimulationEvent(processId, instanceId,
                simContext.getStartTime(), simContext.getMaxEndTime(), path.getPathId(),
                pi.getProcessName(), pi.getProcess().getVersion()));
        
        return null;

    }

}
