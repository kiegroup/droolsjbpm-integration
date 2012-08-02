package org.jbpm.simulation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.drools.definition.process.Connection;
import org.drools.definition.process.Node;
import org.drools.definition.process.NodeContainer;
import org.jbpm.simulation.impl.BPMN2SimulationDataProvider;
import org.jbpm.simulation.util.SimulationConstants;
import org.junit.Test;

public class BPMN2SimulationDataProviderTest {

    @Test
    public void testReadAllSimulationProperties() {
        SimulationDataProvider provider = new BPMN2SimulationDataProvider(this.getClass().getResourceAsStream("/BPMN2-UserTaskWithSimulationMetaData.bpmn2"));
        
        Map<String, Object> data = provider.getSimulationDataForNode("UserTask", new Node() {
            
            public List<Connection> getOutgoingConnections(String type) {
                return null;
            }
            
            public Map<String, List<Connection>> getOutgoingConnections() {
                return null;
            }
            
            public NodeContainer getNodeContainer() {
                return null;
            }
            
            public String getName() {
                return "Hello";
            }
            
            public Object getMetaData(String name) {
                return null;
            }
            
            public Map<String, Object> getMetaData() {
                Map<String, Object> metaData = new HashMap<String, Object>();
                metaData.put("UniqueId", "_2");
                return metaData;
            }
            
            public List<Connection> getIncomingConnections(String type) {
                return null;
            }
            
            public Map<String, List<Connection>> getIncomingConnections() {
                return null;
            }
            
            public long getId() {
                return 2;
            }
        });
        
        assertNotNull(data);
        assertEquals(8, data.size());
        assertTrue(data.containsKey(SimulationConstants.COST_PER_TIME_UNIT));
        assertTrue(data.containsKey(SimulationConstants.DISTRIBUTION_TYPE));
        assertTrue(data.containsKey(SimulationConstants.DURATION));
        assertTrue(data.containsKey(SimulationConstants.RANGE));
        assertTrue(data.containsKey(SimulationConstants.STAFF_AVAILABILITY));
        assertTrue(data.containsKey(SimulationConstants.STANDARD_DEVIATION));
        assertTrue(data.containsKey(SimulationConstants.TIMEUNIT));
        assertTrue(data.containsKey(SimulationConstants.WORKING_HOURS));
    }
}
