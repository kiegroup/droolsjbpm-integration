package org.jbpm.simulation;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.drools.fluent.simulation.SimulationFluent;
import org.drools.fluent.simulation.impl.DefaultSimulationFluent;
import org.jbpm.simulation.converter.SimulationFilterPathFormatConverter;
import org.jbpm.simulation.helper.HardCodedSimulationDataProvider;
import org.jbpm.simulation.helper.TestUtils;
import org.jbpm.simulation.impl.BPMN2SimulationDataProvider;
import org.jbpm.simulation.impl.SimulateProcessPathCommand;
import org.jbpm.simulation.impl.SimulationPath;
import org.junit.Test;
import org.kie.KnowledgeBase;
import org.kie.builder.KnowledgeBuilder;
import org.kie.builder.ReleaseId;
import org.kie.command.World;
import org.kie.io.ResourceFactory;
import org.kie.io.ResourceType;
import org.kie.runtime.StatefulKnowledgeSession;

public class SimulationFluentTest {

    @Test
    public void testSimulationFluent() throws Exception {
        
        PathFinder finder = PathFinderFactory.getInstance(this.getClass().getResourceAsStream("/BPMN2-ExclusiveSplit.bpmn2"));
        
        List<SimulationPath> paths = finder.findPaths(new SimulationFilterPathFormatConverter());
        SimulationContext context = SimulationContextFactory.newContext(new HardCodedSimulationDataProvider());
        ReleaseId releaseId = TestUtils.createKJarWithMultipleResources("TestKbase",
                new String[]{"BPMN2-ExclusiveSplit.bpmn2"}, new ResourceType[]{ResourceType.BPMN2});
        
        SimulationFluent f = new DefaultSimulationFluent();
        // @formatter:off
        f.newPath("path1")
            .newStep( 0 )
                .newKieSession( releaseId, "TestKbase.KSession1" )
                    .end(World.ROOT, StatefulKnowledgeSession.class.getName())
                .addCommand(new SimulateProcessPathCommand("com.sample.test", context, paths.get(0)))
        .newPath("path2")
            .newStep( 10, TimeUnit.MINUTES )
                .newKieSession( releaseId, "TestKbase.KSession1" )
                    .end(World.ROOT, StatefulKnowledgeSession.class.getName())
                .addCommand(new SimulateProcessPathCommand("com.sample.test", context, paths.get(1)))
        .runSimulation();
        // @formatter:on
    }
    
    @Test
    public void testSimulationFluentWithUserTask() throws Exception {
        
        PathFinder finder = PathFinderFactory.getInstance(this.getClass().getResourceAsStream("/BPMN2-UserTask.bpmn2"));
        
        List<SimulationPath> paths = finder.findPaths(new SimulationFilterPathFormatConverter());
        SimulationContext context = SimulationContextFactory.newContext(new HardCodedSimulationDataProvider());
        ReleaseId releaseId = TestUtils.createKJarWithMultipleResources("TestKbase",
                new String[]{"BPMN2-UserTask.bpmn2"}, new ResourceType[]{ResourceType.BPMN2});
        
        SimulationFluent f = new DefaultSimulationFluent();
        // @formatter:off
        f.newPath("path1")
            .newStep( 0 )
                .newKieSession( releaseId, "TestKbase.KSession1" )
                    .end(World.ROOT, StatefulKnowledgeSession.class.getName())
                .addCommand(new SimulateProcessPathCommand("UserTask", context, paths.get(0)))
            .newStep( 5, TimeUnit.MINUTES )
                .newKieSession( releaseId, "TestKbase.KSession1" )
                    .end(World.ROOT, StatefulKnowledgeSession.class.getName())
                    .addCommand(new SimulateProcessPathCommand("UserTask", context, paths.get(0)))
            .newStep( 10, TimeUnit.MINUTES )
                .newKieSession( releaseId, "TestKbase.KSession1" )
                    .end(World.ROOT, StatefulKnowledgeSession.class.getName())
                    .addCommand(new SimulateProcessPathCommand("UserTask", context, paths.get(0)))
            .newStep( 15, TimeUnit.MINUTES )
                .newKieSession( releaseId, "TestKbase.KSession1" )
                    .end(World.ROOT, StatefulKnowledgeSession.class.getName())
                .addCommand(new SimulateProcessPathCommand("UserTask", context, paths.get(0)))
            .newStep( 20, TimeUnit.MINUTES )
                .newKieSession( releaseId, "TestKbase.KSession1" )
                    .end(World.ROOT, StatefulKnowledgeSession.class.getName())
                .addCommand(new SimulateProcessPathCommand("UserTask", context, paths.get(0)))
            .newStep( 25, TimeUnit.MINUTES )
                .newKieSession( releaseId, "TestKbase.KSession1" )
                    .end(World.ROOT, StatefulKnowledgeSession.class.getName())
                .addCommand(new SimulateProcessPathCommand("UserTask", context, paths.get(0)))
        .runSimulation();
        // @formatter:on
        
        System.out.println("Resource utilization for UserTask Hello: " + context.getStaffPoolManager().getActivityPool("Hello").getResourceUtilization());
    }
    
    @Test
    public void testSimulationFluentWithUserTaskBPMN2DataProvider() throws Exception {
        
        PathFinder finder = PathFinderFactory.getInstance(this.getClass().getResourceAsStream("/BPMN2-UserTaskWithSimulationMetaData.bpmn2"));
        
        List<SimulationPath> paths = finder.findPaths(new SimulationFilterPathFormatConverter());
        SimulationContext context = SimulationContextFactory.newContext(new BPMN2SimulationDataProvider(this.getClass().getResourceAsStream("/BPMN2-UserTaskWithSimulationMetaData.bpmn2")));
        
        ReleaseId releaseId = TestUtils.createKJarWithMultipleResources("TestKbase",
                new String[]{"BPMN2-UserTaskWithSimulationMetaData.bpmn2"}, new ResourceType[]{ResourceType.BPMN2});
        
        SimulationFluent f = new DefaultSimulationFluent();
        // @formatter:off        
        f.newPath("path1")
            .newStep( 0 )
                .newKieSession( releaseId, "TestKbase.KSession1" )
                    .end(World.ROOT, StatefulKnowledgeSession.class.getName())
                .addCommand(new SimulateProcessPathCommand("UserTask", context, paths.get(0)))
            .newStep( 5, TimeUnit.MINUTES )
                .newKieSession( releaseId, "TestKbase.KSession1" )
                    .end(World.ROOT, StatefulKnowledgeSession.class.getName())
                    .addCommand(new SimulateProcessPathCommand("UserTask", context, paths.get(0)))
            .newStep( 10, TimeUnit.MINUTES )
                .newKieSession( releaseId, "TestKbase.KSession1" )
                    .end(World.ROOT, StatefulKnowledgeSession.class.getName())
                    .addCommand(new SimulateProcessPathCommand("UserTask", context, paths.get(0)))
            .newStep( 15, TimeUnit.MINUTES )
                .newKieSession( releaseId, "TestKbase.KSession1" )
                    .end(World.ROOT, StatefulKnowledgeSession.class.getName())
                .addCommand(new SimulateProcessPathCommand("UserTask", context, paths.get(0)))
            .newStep( 20, TimeUnit.MINUTES )
                .newKieSession( releaseId, "TestKbase.KSession1" )
                    .end(World.ROOT, StatefulKnowledgeSession.class.getName())
                .addCommand(new SimulateProcessPathCommand("UserTask", context, paths.get(0)))
            .newStep( 25, TimeUnit.MINUTES )
                .newKieSession( releaseId, "TestKbase.KSession1" )
                    .end(World.ROOT, StatefulKnowledgeSession.class.getName())
                .addCommand(new SimulateProcessPathCommand("UserTask", context, paths.get(0)))
        .runSimulation();
        // @formatter:on
        
        System.out.println("Resource utilization for UserTask Hello: " + context.getStaffPoolManager().getActivityPool("Hello").getResourceUtilization());
    }
    
//    @Test
//    public void testSimulationFluent() {
//        
//        PathFinder finder = PathFinderFactory.getInstance(this.getClass().getResourceAsStream("/BPMN2-TwoUserTasks.bpmn2"));
//        
//        List<SimulationPath> paths = finder.findPaths(new SimulationFilterPathFormatConverter());
//        SimulationContext context = SimulationContextFactory.newContext(new HardCodedSimulationDataProvider());
//        
//        SimulationFluent f = new DefaultSimulationFluent();
//        // @formatter:off
//        f.newKnowledgeBuilder()
//        .add( ResourceFactory.newClassPathResource("BPMN2-TwoUserTasks.bpmn2"),
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
