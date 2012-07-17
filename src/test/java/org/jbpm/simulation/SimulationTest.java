package org.jbpm.simulation;

import java.util.List;

import org.drools.runtime.StatefulKnowledgeSession;
import org.jbpm.simulation.converter.SimulationFilterPathFormatConverter;
import org.jbpm.simulation.helper.HardCodedSimulationDataProvider;
import org.jbpm.simulation.helper.TestUtils;
import org.jbpm.simulation.impl.SimulationPath;
import org.junit.Test;

//@Ignore
public class SimulationTest {
    
    @Test
    public void testParallelGatewayProcessSimulation() {
        PathFinder finder = PathFinderFactory.getInstance(this.getClass().getResourceAsStream("/BPMN2-ParallelSplit.bpmn2"));
        
        List<SimulationPath> paths = finder.findPaths(new SimulationFilterPathFormatConverter());
        
        SimulationContext context = SimulationContextFactory.newContext(new HardCodedSimulationDataProvider());
        context.setStartTime(System.currentTimeMillis());
        
        for (SimulationPath path : paths) {
            
            context.setCurrentPath(path.getSequenceFlowsIds());
            StatefulKnowledgeSession session = TestUtils.createSession("BPMN2-ParallelSplit.bpmn2");
            
            session.startProcess("com.sample.test");
            System.out.println("#####################################");
        }
    }
    
    @Test
    public void testExclusiveGatewayProcessSimulation() {
        PathFinder finder = PathFinderFactory.getInstance(this.getClass().getResourceAsStream("/BPMN2-ExclusiveSplit.bpmn2"));
        
        List<SimulationPath> paths = finder.findPaths(new SimulationFilterPathFormatConverter());
        SimulationContext context = SimulationContextFactory.newContext(new HardCodedSimulationDataProvider());
        context.setStartTime(System.currentTimeMillis());
        
        for (SimulationPath path : paths) {
            
            context.setCurrentPath(path.getSequenceFlowsIds());
            StatefulKnowledgeSession session = TestUtils.createSession("BPMN2-ExclusiveSplit.bpmn2");
            
            session.startProcess("com.sample.test");
            System.out.println("#####################################");
        }
    }
    
    
}
