package org.jbpm.simulation;

import java.io.StringReader;
import java.util.List;

import org.drools.KnowledgeBase;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.ResourceType;
import org.drools.command.World;
import org.drools.command.runtime.DisposeCommand;
import org.drools.fluent.simulation.SimulationFluent;
import org.drools.fluent.simulation.impl.DefaultSimulationFluent;
import org.drools.io.Resource;
import org.drools.io.ResourceFactory;
import org.drools.runtime.StatefulKnowledgeSession;
import org.jbpm.simulation.converter.SimulationFilterPathFormatConverter;
import org.jbpm.simulation.impl.BPMN2SimulationDataProvider;
import org.jbpm.simulation.impl.SimulateProcessPathCommand;
import org.jbpm.simulation.impl.SimulationPath;
import org.jbpm.simulation.impl.WorkingMemorySimulationRepository;

public class SimulationRunner {

    public static SimulationRepository runSimulation(String processId, String bpmn2Container, int numberOfAllInstances, long interval, String... rules) {
        
        Resource[] resources = new Resource[rules.length];
        for (int i = 0; i < rules.length; i++) {
            resources[i] = ResourceFactory.newClassPathResource(rules[i]);
        }
        
        return runSimulation(processId, bpmn2Container, numberOfAllInstances, interval, resources);
    }
    
    public static SimulationRepository runSimulation(String processId, String bpmn2Container, int numberOfAllInstances, long interval, Resource... rules) {
        
        SimulationContext context = SimulationContextFactory.newContext(new BPMN2SimulationDataProvider(bpmn2Container), new WorkingMemorySimulationRepository(rules));
        SimulationDataProvider provider = context.getDataProvider();
        
        PathFinder finder = PathFinderFactory.getInstance(bpmn2Container);
        
        List<SimulationPath> paths = finder.findPaths(new SimulationFilterPathFormatConverter(provider));
        
        
        SimulationFluent f = new DefaultSimulationFluent();
        // @formatter:off        
        int counter = 0;
        
        for (SimulationPath path : paths) {
            
            double probability = provider.calculatePathProbability(path);
            f.newPath("path" + counter)
                .newKnowledgeBuilder()
                    .add( ResourceFactory.newReaderResource(new StringReader(bpmn2Container)), ResourceType.BPMN2 )
                .end(World.ROOT, KnowledgeBuilder.class.getName() )
            .newKnowledgeBase()
              .addKnowledgePackages()
              .end(World.ROOT, KnowledgeBase.class.getName() );
            int instancesOfPath = 1;
            // count how many instances/steps should current path have
            if (numberOfAllInstances > 1) {
                instancesOfPath = (int) Math.round((numberOfAllInstances * probability));
            
                for (int i = 0; i < instancesOfPath; i++) {
                    f.newStep( interval * i )
                        .newStatefulKnowledgeSession()
                            .end(World.ROOT, StatefulKnowledgeSession.class.getName())
                        .addCommand(new SimulateProcessPathCommand(processId, context, path));
                }
            } else {
                f.newStep( interval )
                .newStatefulKnowledgeSession()
                    .end(World.ROOT, StatefulKnowledgeSession.class.getName())
                .addCommand(new SimulateProcessPathCommand(processId, context, path));
                break;
            }
            
            counter++;
        }
        f.runSimulation();
        // @formatter:on
        
        return context.getRepository();
    }
}
