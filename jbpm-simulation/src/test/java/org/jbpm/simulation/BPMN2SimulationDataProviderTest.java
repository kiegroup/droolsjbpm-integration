/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package org.jbpm.simulation;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jbpm.simulation.converter.SimulationFilterPathFormatConverter;
import org.jbpm.simulation.impl.BPMN2SimulationDataProvider;
import org.jbpm.simulation.impl.SimulationPath;
import org.jbpm.simulation.util.SimulationConstants;
import org.jbpm.workflow.core.NodeContainer;
import org.junit.Test;
import org.kie.api.definition.process.Connection;
import org.kie.api.definition.process.Node;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class BPMN2SimulationDataProviderTest {

    @Test
    public void testReadAllSimulationProperties() {
        SimulationDataProvider provider = new BPMN2SimulationDataProvider(this.getClass().getResourceAsStream("/BPMN2-UserTaskWithSimulationMetaData.bpmn2"));
        
        Map<String, Object> data = provider.getSimulationDataForNode(new Node() {
            
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
                metaData.put("UniqueId", "_2B5B707D-3458-475C-943D-74F20B13AF20");
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

            @Override
            public String getNodeUniqueId() {
                return null;
            }
        });
        
        assertNotNull(data);
        assertEquals(9, data.size());
        assertTrue(data.containsKey(SimulationConstants.COST_PER_TIME_UNIT));
        assertTrue(data.containsKey(SimulationConstants.DISTRIBUTION_TYPE));
        assertTrue(data.containsKey(SimulationConstants.STANDARD_DEVIATION));
        assertTrue(data.containsKey(SimulationConstants.MEAN));
        assertTrue(data.containsKey(SimulationConstants.STAFF_AVAILABILITY));
        assertTrue(data.containsKey(SimulationConstants.STANDARD_DEVIATION));
        assertTrue(data.containsKey(SimulationConstants.TIMEUNIT));
        assertTrue(data.containsKey(SimulationConstants.WORKING_HOURS));
        assertTrue(data.containsKey(SimulationConstants.PROBABILITY));
    }
    
    @Test
    public void testReadProbabilitySimulationProperties() {
        SimulationDataProvider provider = new BPMN2SimulationDataProvider(this.getClass().getResourceAsStream("/BPMN2-ExclusiveSplitWithSimulationProps.bpmn2"));
        
        Map<String, Object> data = provider.getSimulationDataForNode(new Node() {
            
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
                metaData.put("UniqueId", "_575A78C8-C34A-445E-8B2F-BB990B513A03");
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

            @Override
            public String getNodeUniqueId() {
                // TODO Auto-generated method stub
                return null;
            }
        });
        
        assertNotNull(data);
        assertEquals(1, data.size());
        assertTrue(data.containsKey(SimulationConstants.PROBABILITY));
    }
    
    
    @Test
    public void testExclusiveSplit() throws IOException {
        PathFinder finder = PathFinderFactory.getInstance(this.getClass().getResourceAsStream("/BPMN2-ExclusiveSplitWithSimulationProps.bpmn2"));
        
        List<SimulationPath> paths = finder.findPaths(new SimulationFilterPathFormatConverter());
        
        assertNotNull(paths);
        assertEquals(2, paths.size());

        BPMN2SimulationDataProvider provider = new BPMN2SimulationDataProvider(this.getClass().getResourceAsStream("/BPMN2-ExclusiveSplitWithSimulationProps.bpmn2"));
        
        double probabilityOfPathOne = provider.calculatePathProbability(paths.get(0));
        double probabilityOfPathTwo = provider.calculatePathProbability(paths.get(1));
        
        assertTrue(0.7 == probabilityOfPathOne);
        assertTrue(0.3 == probabilityOfPathTwo);
        System.out.println("Probability 1: " + probabilityOfPathOne + " probabilit 2: " + probabilityOfPathTwo);
    }
}
