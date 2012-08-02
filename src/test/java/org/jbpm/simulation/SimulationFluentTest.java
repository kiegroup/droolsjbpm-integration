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
import org.jbpm.simulation.impl.BPMN2SimulationDataProvider;
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
    
    @Test
    public void testSimulationFluentWithUserTask() {
        
        PathFinder finder = PathFinderFactory.getInstance(this.getClass().getResourceAsStream("/BPMN2-UserTask.bpmn2"));
        
        List<SimulationPath> paths = finder.findPaths(new SimulationFilterPathFormatConverter());
        SimulationContext context = SimulationContextFactory.newContext(new HardCodedSimulationDataProvider());
        
        SimulationFluent f = new DefaultSimulationFluent();
        // @formatter:off
        f.newKnowledgeBuilder()
        .add( ResourceFactory.newClassPathResource("BPMN2-UserTask.bpmn2"),
                ResourceType.BPMN2 )
          .end(World.ROOT, KnowledgeBuilder.class.getName() )
        .newKnowledgeBase()
          .addKnowledgePackages()
          .end(World.ROOT, KnowledgeBase.class.getName() )
        .newPath("path1")
            .newStep( 0 )
                .newStatefulKnowledgeSession()
                    .end(World.ROOT, StatefulKnowledgeSession.class.getName())
                .addCommand(new SimulateProcessPathCommand("UserTask", context, paths.get(0)))
            .newStep( 5, TimeUnit.MINUTES )
                .newStatefulKnowledgeSession()
                    .end(World.ROOT, StatefulKnowledgeSession.class.getName())
                    .addCommand(new SimulateProcessPathCommand("UserTask", context, paths.get(0)))
            .newStep( 10, TimeUnit.MINUTES )
                .newStatefulKnowledgeSession()
                    .end(World.ROOT, StatefulKnowledgeSession.class.getName())
                    .addCommand(new SimulateProcessPathCommand("UserTask", context, paths.get(0)))
            .newStep( 15, TimeUnit.MINUTES )
                .newStatefulKnowledgeSession()
                    .end(World.ROOT, StatefulKnowledgeSession.class.getName())
                .addCommand(new SimulateProcessPathCommand("UserTask", context, paths.get(0)))
            .newStep( 20, TimeUnit.MINUTES )
                .newStatefulKnowledgeSession()
                    .end(World.ROOT, StatefulKnowledgeSession.class.getName())
                .addCommand(new SimulateProcessPathCommand("UserTask", context, paths.get(0)))
            .newStep( 25, TimeUnit.MINUTES )
                .newStatefulKnowledgeSession()
                    .end(World.ROOT, StatefulKnowledgeSession.class.getName())
                .addCommand(new SimulateProcessPathCommand("UserTask", context, paths.get(0)))
        .runSimulation();
        // @formatter:on
        
        System.out.println("Resource utilization for UserTask Hello: " + context.getStaffPoolManager().getActivityPool("Hello").getResourceUtilization());
    }
    
    @Test
    public void testSimulationFluentWithUserTaskBPMN2DataProvider() {
        
        PathFinder finder = PathFinderFactory.getInstance(this.getClass().getResourceAsStream("/BPMN2-UserTaskWithSimulationMetaData.bpmn2"));
        
        List<SimulationPath> paths = finder.findPaths(new SimulationFilterPathFormatConverter());
        SimulationContext context = SimulationContextFactory.newContext(new BPMN2SimulationDataProvider(this.getClass().getResourceAsStream("/BPMN2-UserTaskWithSimulationMetaData.bpmn2")));
        
        SimulationFluent f = new DefaultSimulationFluent();
        // @formatter:off
        f.newKnowledgeBuilder()
        .add( ResourceFactory.newClassPathResource("BPMN2-UserTaskWithSimulationMetaData.bpmn2"),
                ResourceType.BPMN2 )
          .end(World.ROOT, KnowledgeBuilder.class.getName() )
        .newKnowledgeBase()
          .addKnowledgePackages()
          .end(World.ROOT, KnowledgeBase.class.getName() )
        .newPath("path1")
            .newStep( 0 )
                .newStatefulKnowledgeSession()
                    .end(World.ROOT, StatefulKnowledgeSession.class.getName())
                .addCommand(new SimulateProcessPathCommand("UserTask", context, paths.get(0)))
            .newStep( 5, TimeUnit.MINUTES )
                .newStatefulKnowledgeSession()
                    .end(World.ROOT, StatefulKnowledgeSession.class.getName())
                    .addCommand(new SimulateProcessPathCommand("UserTask", context, paths.get(0)))
            .newStep( 10, TimeUnit.MINUTES )
                .newStatefulKnowledgeSession()
                    .end(World.ROOT, StatefulKnowledgeSession.class.getName())
                    .addCommand(new SimulateProcessPathCommand("UserTask", context, paths.get(0)))
            .newStep( 15, TimeUnit.MINUTES )
                .newStatefulKnowledgeSession()
                    .end(World.ROOT, StatefulKnowledgeSession.class.getName())
                .addCommand(new SimulateProcessPathCommand("UserTask", context, paths.get(0)))
            .newStep( 20, TimeUnit.MINUTES )
                .newStatefulKnowledgeSession()
                    .end(World.ROOT, StatefulKnowledgeSession.class.getName())
                .addCommand(new SimulateProcessPathCommand("UserTask", context, paths.get(0)))
            .newStep( 25, TimeUnit.MINUTES )
                .newStatefulKnowledgeSession()
                    .end(World.ROOT, StatefulKnowledgeSession.class.getName())
                .addCommand(new SimulateProcessPathCommand("UserTask", context, paths.get(0)))
        .runSimulation();
        // @formatter:on
        
        System.out.println("Resource utilization for UserTask Hello: " + context.getStaffPoolManager().getActivityPool("Hello").getResourceUtilization());
    }
    
//    @Test
//    public void testSimulationFluent() {
//        
//        PathFinder finder = PathFinderFactory.getInstance(this.getClass().getResourceAsStream("/BPMN2-TwoUserTasks.bpmn"));
//        
//        List<SimulationPath> paths = finder.findPaths(new SimulationFilterPathFormatConverter());
//        SimulationContext context = SimulationContextFactory.newContext(new HardCodedSimulationDataProvider());
//        
//        SimulationFluent f = new DefaultSimulationFluent();
//        // @formatter:off
//        f.newKnowledgeBuilder()
//        .add( ResourceFactory.newClassPathResource("BPMN2-TwoUserTasks.bpmn"),
//                ResourceType.BPMN2 )
//          .end(World.ROOT, KnowledgeBuilder.class.getName() )
//        .newKnowledgeBase()
//          .addKnowledgePackages()
//          .end(World.ROOT, KnowledgeBase.class.getName() )
//        .newPath("path1")
//            .newStep( 0 )
//                .newStatefulKnowledgeSession()
//                    .end(World.ROOT, StatefulKnowledgeSession.class.getName())
//                .addCommand(new SimulateProcessPathCommand("com.sample.test", context, paths.get(0)))
//        .newPath("path2")
//            .newStep( 10, TimeUnit.MINUTES )
//                .newStatefulKnowledgeSession()
//                    .end(World.ROOT, StatefulKnowledgeSession.class.getName())
//                .addCommand(new SimulateProcessPathCommand("com.sample.test", context, paths.get(1)))
//        .runSimulation();
//        // @formatter:on
//    }
}
