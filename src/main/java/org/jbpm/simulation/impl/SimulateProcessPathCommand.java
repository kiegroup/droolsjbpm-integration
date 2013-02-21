package org.jbpm.simulation.impl;

import org.drools.command.impl.GenericCommand;
import org.drools.command.impl.KnowledgeCommandContext;
import org.drools.time.SessionPseudoClock;
import org.jbpm.simulation.SimulationContext;
import org.jbpm.simulation.SimulationInfo;
import org.jbpm.simulation.impl.events.ProcessInstanceEndSimulationEvent;
import org.jbpm.workflow.core.node.ActionNode;
import org.jbpm.workflow.core.node.CatchLinkNode;
import org.jbpm.workflow.core.node.CompositeContextNode;
import org.jbpm.workflow.core.node.CompositeNode;
import org.jbpm.workflow.core.node.DynamicNode;
import org.jbpm.workflow.core.node.EndNode;
import org.jbpm.workflow.core.node.EventNode;
import org.jbpm.workflow.core.node.FaultNode;
import org.jbpm.workflow.core.node.ForEachNode;
import org.jbpm.workflow.core.node.HumanTaskNode;
import org.jbpm.workflow.core.node.Join;
import org.jbpm.workflow.core.node.MilestoneNode;
import org.jbpm.workflow.core.node.RuleSetNode;
import org.jbpm.workflow.core.node.Split;
import org.jbpm.workflow.core.node.StartNode;
import org.jbpm.workflow.core.node.StateNode;
import org.jbpm.workflow.core.node.SubProcessNode;
import org.jbpm.workflow.core.node.ThrowLinkNode;
import org.jbpm.workflow.core.node.TimerNode;
import org.jbpm.workflow.core.node.WorkItemNode;
import org.jbpm.workflow.instance.impl.NodeInstanceFactoryRegistry;
import org.jbpm.workflow.instance.impl.factory.CreateNewNodeFactory;
import org.jbpm.workflow.instance.impl.factory.ReuseNodeFactory;
import org.kie.command.Context;
import org.kie.runtime.KieSession;
import org.kie.runtime.StatefulKnowledgeSession;
import org.kie.runtime.process.ProcessInstance;

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
        NodeInstanceFactoryRegistry n = NodeInstanceFactoryRegistry.INSTANCE;
        
        n.register( RuleSetNode.class,
                  new CreateNewNodeFactory( SimulationNodeInstance.class ) );
        n.register( Split.class,
                  new CreateNewNodeFactory( SimulationNodeInstance.class ) );
        n.register( Join.class,
                  new ReuseNodeFactory( SimulationNodeInstance.class ) );
        n.register( StartNode.class,
                  new CreateNewNodeFactory( SimulationStartNodeInstance.class ) );
        n.register( EndNode.class,
                  new CreateNewNodeFactory( SimulationEndNodeInstance.class ) );
        n.register( MilestoneNode.class,
                  new CreateNewNodeFactory( SimulationNodeInstance.class ) );
        n.register( SubProcessNode.class,
                  new CreateNewNodeFactory( SimulationNodeInstance.class ) );
        n.register( ActionNode.class,
                  new CreateNewNodeFactory( SimulationNodeInstance.class ) );
        n.register( WorkItemNode.class,
                  new CreateNewNodeFactory( SimulationNodeInstance.class ) );
        n.register( TimerNode.class,
                  new CreateNewNodeFactory( SimulationNodeInstance.class ) );
        n.register( FaultNode.class,
                  new CreateNewNodeFactory( SimulationNodeInstance.class ) );
        n.register( CompositeNode.class,
                  new CreateNewNodeFactory( SimulationNodeInstance.class ) );
        n.register( CompositeContextNode.class,
                  new CreateNewNodeFactory( SimulationNodeInstance.class ) );
        n.register( HumanTaskNode.class,
                  new CreateNewNodeFactory( SimulationNodeInstance.class ) );
        n.register( ForEachNode.class,
                  new CreateNewNodeFactory( SimulationNodeInstance.class ) );
        n.register( EventNode.class,
                  new CreateNewNodeFactory( SimulationNodeInstance.class ) );
        n.register( StateNode.class,
                  new CreateNewNodeFactory( SimulationNodeInstance.class ) );
        n.register( DynamicNode.class,
                  new CreateNewNodeFactory( SimulationNodeInstance.class ) );
        
        n.register(CatchLinkNode.class, new CreateNewNodeFactory(
                SimulationNodeInstance.class));
        n.register(ThrowLinkNode.class, new CreateNewNodeFactory(
                SimulationNodeInstance.class));
        
        KieSession session = ((KnowledgeCommandContext)context).getKieSession();
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
