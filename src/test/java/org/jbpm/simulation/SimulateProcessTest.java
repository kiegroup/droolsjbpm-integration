package org.jbpm.simulation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import org.drools.KnowledgeBase;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.ResourceType;
import org.drools.command.World;
import org.drools.fluent.simulation.SimulationFluent;
import org.drools.fluent.simulation.impl.DefaultSimulationFluent;
import org.drools.io.ResourceFactory;
import org.drools.runtime.StatefulKnowledgeSession;
import org.jbpm.simulation.converter.SimulationFilterPathFormatConverter;
import org.jbpm.simulation.impl.BPMN2SimulationDataProvider;
import org.jbpm.simulation.impl.SimulateProcessPathCommand;
import org.jbpm.simulation.impl.SimulationPath;
import org.jbpm.simulation.impl.WorkingMemorySimulationRepository;
import org.jbpm.simulation.impl.events.ActivitySimulationEvent;
import org.jbpm.simulation.impl.events.EndSimulationEvent;
import org.jbpm.simulation.impl.events.GenericSimulationEvent;
import org.junit.Test;

public class SimulateProcessTest {

    @Test
    public void testSimpleExclusiveGatewayTest() {
        
        PathFinder finder = PathFinderFactory.getInstance(this.getClass().getResourceAsStream("/BPMN-SimpleExclusiveGatewayProcess.bpmn2"));
        
        List<SimulationPath> paths = finder.findPaths(new SimulationFilterPathFormatConverter());
        assertEquals(2, paths.size());
        
        SimulationContext context = SimulationContextFactory.newContext(new BPMN2SimulationDataProvider(this.getClass().getResourceAsStream("/BPMN-SimpleExclusiveGatewayProcess.bpmn2")));
        
        
        SimulationDataProvider provider = context.getDataProvider();
        
        SimulationFluent f = new DefaultSimulationFluent();
        // @formatter:off
        // FIXME why building knowledge base on this level does not work??
        int numberOfAllInstances = 10;
        int counter = 0;
        // default interval 2 seconds, meaning each step in a path will be started after 2 seconds
        long interval = 2*1000*60;
        for (SimulationPath path : paths) {
            
            double probability = provider.calculatePathProbability(path);
            f.newPath("path" + counter);

            f.newKnowledgeBuilder().add( ResourceFactory.newClassPathResource("BPMN-SimpleExclusiveGatewayProcess.bpmn2"),
                    ResourceType.BPMN2 )
              .end(World.ROOT, KnowledgeBuilder.class.getName() )
            .newKnowledgeBase()
              .addKnowledgePackages()
              .end(World.ROOT, KnowledgeBase.class.getName() );
            
            // count how many instances/steps should current path have
            int instancesOfPath = (int) (numberOfAllInstances * probability);
            
            for (int i = 0; i < instancesOfPath; i++) {
                f.newStep( interval * i )
                    .newStatefulKnowledgeSession()
                        .end(World.ROOT, StatefulKnowledgeSession.class.getName())
                    .addCommand(new SimulateProcessPathCommand("defaultPackage.test", context, path));
            }
            
            counter++;
        }
        f.runSimulation();
        // @formatter:on
        
    }
    
    @Test
    public void testSimulationRunner() throws IOException {
        
        InputStreamReader in = new InputStreamReader(this.getClass().getResourceAsStream("/BPMN2-TwoUserTasks.bpmn"));
        
        String out = new String();
        BufferedReader br = new BufferedReader(in);
        for(String line = br.readLine(); line != null; line = br.readLine()) 
          out += line;


        
        SimulationRepository repo = SimulationRunner.runSimulation("BPMN2-TwoUserTasks", out, 10, 2000, "default.simulation.rules.drl");
        assertNotNull(repo);
        
        WorkingMemorySimulationRepository wmRepo = (WorkingMemorySimulationRepository) repo;
        wmRepo.fireAllRules();
        
        assertEquals(3, wmRepo.getAggregatedEvents().size());
        assertEquals(40, wmRepo.getEvents().size());
        
        AggregatedSimulationEvent event = wmRepo.getAggregatedEvents().get(0);
        assertNotNull(event.getProperty("minExecutionTime"));
        assertFalse(event.getProperty("activityId").equals(""));
        event = wmRepo.getAggregatedEvents().get(1);
        assertNotNull(event.getProperty("minExecutionTime"));
        event = wmRepo.getAggregatedEvents().get(2);
        assertNotNull(event.getProperty("minExecutionTime"));
        wmRepo.close();
        
    }
    
    @Test
    public void testSimulationRunnerWithGateway() throws IOException {
        
        InputStreamReader in = new InputStreamReader(this.getClass().getResourceAsStream("/BPMN-SimpleExclusiveGatewayProcess.bpmn2"));
        
        String out = new String();
        BufferedReader br = new BufferedReader(in);
        for(String line = br.readLine(); line != null; line = br.readLine()) 
          out += line;


        
        SimulationRepository repo = SimulationRunner.runSimulation("defaultPackage.test", out, 10, 2000, "default.simulation.rules.drl");
        assertNotNull(repo);
        
        WorkingMemorySimulationRepository wmRepo = (WorkingMemorySimulationRepository) repo;
        wmRepo.fireAllRules();
        assertEquals(4, wmRepo.getAggregatedEvents().size());
        assertEquals(60, wmRepo.getEvents().size());
        wmRepo.close();
    }

    @Test
    public void testSimulationRunnerWithGatewaySingleInstance() throws IOException {
        
        InputStreamReader in = new InputStreamReader(this.getClass().getResourceAsStream("/BPMN-SimpleExclusiveGatewayProcess.bpmn2"));
        
        String out = new String();
        BufferedReader br = new BufferedReader(in);
        for(String line = br.readLine(); line != null; line = br.readLine()) 
          out += line;


        
        SimulationRepository repo = SimulationRunner.runSimulation("defaultPackage.test", out, 1, 2000, "default.simulation.rules.drl");
        assertNotNull(repo);
        
        WorkingMemorySimulationRepository wmRepo = (WorkingMemorySimulationRepository) repo;
        wmRepo.fireAllRules();
        assertEquals(3, wmRepo.getAggregatedEvents().size());
        assertEquals(6, wmRepo.getEvents().size());
        wmRepo.close();
    }
    
    @Test
    public void testSimulationRunnerWithGatewayTwoInstances() throws IOException {
        
        InputStreamReader in = new InputStreamReader(this.getClass().getResourceAsStream("/BPMN-SimpleExclusiveGatewayProcess.bpmn2"));
        
        String out = new String();
        BufferedReader br = new BufferedReader(in);
        for(String line = br.readLine(); line != null; line = br.readLine()) 
          out += line;


        
        SimulationRepository repo = SimulationRunner.runSimulation("defaultPackage.test", out, 2, 2000, "default.simulation.rules.drl");
        assertNotNull(repo);
        
        WorkingMemorySimulationRepository wmRepo = (WorkingMemorySimulationRepository) repo;
        wmRepo.fireAllRules();
        assertEquals(4, wmRepo.getAggregatedEvents().size());
        assertEquals(12, wmRepo.getEvents().size());
        wmRepo.close();
    }
    
    @Test
    public void testSimulationRunnerWithGatewaySingleInstanceWithRunRulesOnEveryEvent() throws IOException {
        
        InputStreamReader in = new InputStreamReader(this.getClass().getResourceAsStream("/BPMN-SimpleExclusiveGatewayProcess.bpmn2"));
        
        String out = new String();
        BufferedReader br = new BufferedReader(in);
        for(String line = br.readLine(); line != null; line = br.readLine()) 
          out += line;


        
        SimulationRepository repo = SimulationRunner.runSimulation("defaultPackage.test", out, 5, 2000, true, "default.simulation.rules.drl");
        assertNotNull(repo);
        
        WorkingMemorySimulationRepository wmRepo = (WorkingMemorySimulationRepository) repo;

        assertEquals(15, wmRepo.getAggregatedEvents().size());
        assertEquals(30, wmRepo.getEvents().size());
        wmRepo.close();
    }
    
    @Test
    public void testSimulationRunnerWithRunRulesOnEveryEvent() throws IOException {
        
        InputStreamReader in = new InputStreamReader(this.getClass().getResourceAsStream("/BPMN-SimpleExclusiveGatewayProcess.bpmn2"));
        
        String out = new String();
        BufferedReader br = new BufferedReader(in);
        for(String line = br.readLine(); line != null; line = br.readLine()) 
          out += line;


        
        SimulationRepository repo = SimulationRunner.runSimulation("defaultPackage.test", out, 5, 2000, true, "onevent.simulation.rules.drl");
        assertNotNull(repo);
        
        WorkingMemorySimulationRepository wmRepo = (WorkingMemorySimulationRepository) repo;

        assertEquals(15, wmRepo.getAggregatedEvents().size());
        assertEquals(30, wmRepo.getEvents().size());
        
        for (SimulationEvent event : wmRepo.getEvents()) {
            if ((event instanceof EndSimulationEvent) || (event instanceof ActivitySimulationEvent)) {
                assertNotNull(((GenericSimulationEvent) event).getAggregatedEvent());
            }
        }
        wmRepo.close();
    }
}
