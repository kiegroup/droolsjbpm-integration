package org.jbpm.simulation;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.drools.KnowledgeBase;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.ResourceType;
import org.drools.command.World;
import org.drools.fluent.simulation.SimulationFluent;
import org.drools.fluent.simulation.impl.DefaultSimulationFluent;
import org.drools.io.ResourceFactory;
import org.drools.runtime.StatefulKnowledgeSession;
import org.jbpm.simulation.converter.SimulationFilterPathFormatConverter;
import org.jbpm.simulation.helper.HardCodedSimulationDataProvider;
import org.jbpm.simulation.impl.SimulateProcessPathCommand;
import org.jbpm.simulation.impl.SimulationPath;
import org.junit.Test;

public class SimulationFluentTest {

    @Test
    public void testSimulationFluent() {
        
        PathFinder finder = PathFinderFactory.getInstance(this.getClass().getResourceAsStream("/BPMN2-ExclusiveSplit.bpmn2"));
        
        List<SimulationPath> paths = finder.findPaths(new SimulationFilterPathFormatConverter());
        SimulationContext context = SimulationContextFactory.newContext(new HardCodedSimulationDataProvider());
        
        SimulationFluent f = new DefaultSimulationFluent();
        // @formatter:off
        f.newKnowledgeBuilder()
        .add( ResourceFactory.newClassPathResource("BPMN2-ExclusiveSplit.bpmn2"),
                ResourceType.BPMN2 )
          .end(World.ROOT, KnowledgeBuilder.class.getName() )
        .newKnowledgeBase()
          .addKnowledgePackages()
          .end(World.ROOT, KnowledgeBase.class.getName() )
        .newPath("path1")
            .newStep( 0 )
                .newStatefulKnowledgeSession()
                    .end(World.ROOT, StatefulKnowledgeSession.class.getName())
                .addCommand(new SimulateProcessPathCommand("com.sample.test", context, paths.get(0)))
        .newPath("path2")
            .newStep( 10, TimeUnit.MINUTES )
                .newStatefulKnowledgeSession()
                    .end(World.ROOT, StatefulKnowledgeSession.class.getName())
                .addCommand(new SimulateProcessPathCommand("com.sample.test", context, paths.get(1)))
        .runSimulation();
        // @formatter:on
    }
}
