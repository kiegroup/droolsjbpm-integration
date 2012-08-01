package org.jbpm.simulation;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseFactory;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.impl.EnvironmentFactory;
import org.drools.io.ResourceFactory;
import org.drools.runtime.KnowledgeSessionConfiguration;
import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.conf.ClockTypeOption;
import org.drools.time.SessionPseudoClock;
import org.jbpm.simulation.converter.SimulationFilterPathFormatConverter;
import org.jbpm.simulation.helper.HardCodedSimulationDataProvider;
import org.jbpm.simulation.impl.SimulateProcessPathCommand;
import org.jbpm.simulation.impl.SimulationPath;
import org.junit.Test;

public class SimulationCommandTest {

    @Test
    public void testSimulationCommand() {
        
        PathFinder finder = PathFinderFactory.getInstance(this.getClass().getResourceAsStream("/BPMN2-ExclusiveSplit.bpmn2"));
        
        List<SimulationPath> paths = finder.findPaths(new SimulationFilterPathFormatConverter());
        SimulationContext context = SimulationContextFactory.newContext(new HardCodedSimulationDataProvider());
        
        KnowledgeBuilder builder = KnowledgeBuilderFactory.newKnowledgeBuilder();
        
        builder.add(ResourceFactory.newClassPathResource("BPMN2-ExclusiveSplit.bpmn2"), ResourceType.BPMN2);
        
        KnowledgeBase kbase = builder.newKnowledgeBase();
        KnowledgeSessionConfiguration config = KnowledgeBaseFactory.newKnowledgeSessionConfiguration();
        config.setOption(ClockTypeOption.get("pseudo") );
        
        for (SimulationPath sp : paths) {
        
            StatefulKnowledgeSession session = kbase.newStatefulKnowledgeSession(config, EnvironmentFactory.newEnvironment());
            ((SessionPseudoClock) session.getSessionClock()).advanceTime(System.currentTimeMillis(), TimeUnit.MILLISECONDS);
            
            session.execute(new SimulateProcessPathCommand("com.sample.test", context, sp));
        }
    }
}
