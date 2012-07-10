package org.jbpm.simulation;

import java.util.List;

import org.drools.KnowledgeBase;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.io.ResourceFactory;
import org.drools.runtime.StatefulKnowledgeSession;
import org.jbpm.simulation.converter.SimulationFilterPathFormatConverter;
import org.jbpm.simulation.helper.HardCodedSimulationDataProvider;
import org.jbpm.simulation.impl.SimulationEndNodeInstance;
import org.jbpm.simulation.impl.SimulationNodeInstance;
import org.jbpm.simulation.impl.SimulationStartNodeInstance;
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
import org.junit.Test;

//@Ignore
public class SimulationTest {
    
    @Test
    public void testParallelGatewayProcessSimulation() {
        PathFinder finder = PathFinderFactory.getInstance(this.getClass().getResourceAsStream("/BPMN2-ParallelSplit.bpmn2"));
        
        List<List<String>> paths = finder.findPaths(new SimulationFilterPathFormatConverter());
        
        SimulationContext context = SimulationContextFactory.newContext(new HardCodedSimulationDataProvider());
        context.setStartTime(System.currentTimeMillis());
        
        for (List<String> path : paths) {
            
            context.setCurrentPath(path);
            StatefulKnowledgeSession session = createSession("BPMN2-ParallelSplit.bpmn2");
            
            session.startProcess("com.sample.test");
            System.out.println("#####################################");
        }
    }
    
    @Test
    public void testExclusiveGatewayProcessSimulation() {
        PathFinder finder = PathFinderFactory.getInstance(this.getClass().getResourceAsStream("/BPMN2-ExclusiveSplit.bpmn2"));
        
        List<List<String>> paths = finder.findPaths(new SimulationFilterPathFormatConverter());
        SimulationContext context = SimulationContextFactory.newContext(new HardCodedSimulationDataProvider());
        context.setStartTime(System.currentTimeMillis());
        
        for (List<String> path : paths) {
            
            context.setCurrentPath(path);
            StatefulKnowledgeSession session = createSession("BPMN2-ExclusiveSplit.bpmn2");
            
            session.startProcess("com.sample.test");
            System.out.println("#####################################");
        }
    }
    
    public StatefulKnowledgeSession createSession(String process) {
        KnowledgeBuilder builder = KnowledgeBuilderFactory.newKnowledgeBuilder();
        
        builder.add(ResourceFactory.newClassPathResource(process), ResourceType.BPMN2);
        
        KnowledgeBase kbase = builder.newKnowledgeBase();
        
        StatefulKnowledgeSession session = kbase.newStatefulKnowledgeSession();
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
        return session;
    }
}
