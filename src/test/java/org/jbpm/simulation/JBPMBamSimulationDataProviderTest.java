package org.jbpm.simulation;

import static org.junit.Assert.assertNotNull;

import java.util.List;
import java.util.Map;

import org.drools.definition.process.Node;
import org.drools.runtime.StatefulKnowledgeSession;
import org.jbpm.simulation.converter.SimulationFilterPathFormatConverter;
import org.jbpm.simulation.helper.TestUtils;
import org.jbpm.simulation.impl.JBPMBAMSimulationDataProvider;
import org.jbpm.simulation.impl.SimulationPath;
import org.jbpm.workflow.core.impl.NodeImpl;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import bitronix.tm.resource.jdbc.PoolingDataSource;

@Ignore
public class JBPMBamSimulationDataProviderTest {

    @BeforeClass
    public static void setupPoolingDataSource2() {
        System.setProperty("java.naming.factory.initial", "bitronix.tm.jndi.BitronixInitialContextFactory");
        PoolingDataSource pds = new PoolingDataSource();
        pds.setUniqueName("jdbc/jbpm-ds");
        pds.setClassName("bitronix.tm.resource.jdbc.lrc.LrcXADataSource");
        pds.setMaxPoolSize(5);
        pds.setAllowLocalTransactions(true);
        pds.getDriverProperties().put("user", "jbpm");
        pds.getDriverProperties().put("password", "jbpm");
        pds.getDriverProperties().put("url", "jdbc:postgresql://localhost:5432/jbpm");
        pds.getDriverProperties().put("driverClassName", "org.postgresql.Driver");
        pds.init();

    }
    
    public static void tearDown() {
        System.clearProperty("java.naming.factory.initial");
    }
    
    @Test
    public void testLoadData() {
        SimulationDataProvider provider = new JBPMBAMSimulationDataProvider("jdbc/jbpm-ds");
        Node node = new NodeImpl() {

            @Override
            public long getId() {
                return 4;
            }
            
        };
        Map<String, Object> data = provider.getSimulationDataForNode("com.sample.test", node);
        assertNotNull(data);
    }
    
    @Test
    public void testExclusiveGatewayProcessSimulation() {
        PathFinder finder = PathFinderFactory.getInstance(this.getClass().getResourceAsStream("/BPMN2-ExclusiveSplit.bpmn2"));
        
        List<SimulationPath> paths = finder.findPaths(new SimulationFilterPathFormatConverter());
        JBPMBAMSimulationDataProvider provider = new JBPMBAMSimulationDataProvider("jdbc/jbpm-ds");
        SimulationContext context = SimulationContextFactory.newContext(provider);
        context.setStartTime(System.currentTimeMillis());
        
        for (SimulationPath path : paths) {
            
            context.setCurrentPath(path.getSequenceFlowsIds());
            StatefulKnowledgeSession session = TestUtils.createSession("BPMN2-ExclusiveSplit.bpmn2");
            
            session.startProcess("com.sample.test");
            double probability = provider.calculatePathProbability("com.sample.test", path.getActivityIds());
            System.out.println("Path probability is " + probability);
            System.out.println("#####################################");
        }
    }
}
