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

import static org.junit.Assert.assertNotNull;

import java.util.List;
import java.util.Map;

import org.jbpm.simulation.converter.SimulationFilterPathFormatConverter;
import org.jbpm.simulation.helper.TestUtils;
import org.jbpm.simulation.impl.JBPMBAMSimulationDataProvider;
import org.jbpm.simulation.impl.SimulationPath;
import org.jbpm.workflow.core.impl.NodeImpl;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.kie.api.definition.process.Node;
import org.kie.api.runtime.KieSession;

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
        SimulationDataProvider provider = new JBPMBAMSimulationDataProvider("jdbc/jbpm-ds", "com.sample.test");
        Node node = new NodeImpl() {

            @Override
            public long getId() {
                return 4;
            }
            
        };
        Map<String, Object> data = provider.getSimulationDataForNode(node);
        assertNotNull(data);
    }
    
    @Test
    public void testExclusiveGatewayProcessSimulation() {
        PathFinder finder = PathFinderFactory.getInstance(this.getClass().getResourceAsStream("/BPMN2-ExclusiveSplit.bpmn2"));
        
        List<SimulationPath> paths = finder.findPaths(new SimulationFilterPathFormatConverter());
        JBPMBAMSimulationDataProvider provider = new JBPMBAMSimulationDataProvider("jdbc/jbpm-ds", "com.sample.test");
        SimulationContext context = SimulationContextFactory.newContext(provider);
        context.setStartTime(System.currentTimeMillis());
        
        for (SimulationPath path : paths) {
            
            context.setCurrentPath(path);
            KieSession session = TestUtils.createSession("BPMN2-ExclusiveSplit.bpmn2");
            
            session.startProcess("com.sample.test");
            double probability = provider.calculatePathProbability(path);
            System.out.println("Path probability is " + probability);
            System.out.println("#####################################");
        }
    }
}
