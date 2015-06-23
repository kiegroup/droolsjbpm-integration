/*
 * Copyright 2015 JBoss Inc
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.drools.core.time.SessionPseudoClock;
import org.jbpm.simulation.converter.SimulationFilterPathFormatConverter;
import org.jbpm.simulation.helper.HardCodedSimulationDataProvider;
import org.jbpm.simulation.helper.TestUtils;
import org.jbpm.simulation.impl.BPMN2SimulationDataProvider;
import org.jbpm.simulation.impl.SimulationPath;
import org.jbpm.simulation.impl.WorkingMemorySimulationRepository;
import org.junit.Test;
import org.kie.internal.runtime.StatefulKnowledgeSession;

public class WorkingMemorySimulationRepositoryTest {

    @Test
    public void testWorkingMemorySimulationRepositoryPrintoutRule() {
        
        PathFinder finder = PathFinderFactory.getInstance(this.getClass().getResourceAsStream("/BPMN2-UserTask.bpmn2"));
        
        List<SimulationPath> paths = finder.findPaths(new SimulationFilterPathFormatConverter());
        SimulationContext context = SimulationContextFactory.newContext(new HardCodedSimulationDataProvider()
        , new WorkingMemorySimulationRepository(true, "printOutRule.drl"));
        
        
        for (SimulationPath path : paths) {
            
            context.setCurrentPath(path);
            StatefulKnowledgeSession session = TestUtils.createSession("BPMN2-UserTask.bpmn2");
            
            context.setClock((SessionPseudoClock) session.getSessionClock());
            // set start date to current time
            context.getClock().advanceTime(System.currentTimeMillis(), TimeUnit.MILLISECONDS);
            
            session.startProcess("UserTask");
            System.out.println("#####################################");
        }
    }
    
    @Test
    public void testWorkingMemorySimulationRepositoryCEPRule() {
        
        PathFinder finder = PathFinderFactory.getInstance(this.getClass().getResourceAsStream("/BPMN2-TwoUserTasks.bpmn2"));
        
        List<SimulationPath> paths = finder.findPaths(new SimulationFilterPathFormatConverter());
        SimulationContext context = SimulationContextFactory.newContext(new BPMN2SimulationDataProvider(this.getClass().getResourceAsStream("/BPMN2-TwoUserTasks.bpmn2"))
        , new WorkingMemorySimulationRepository("default.simulation.rules.drl"));

        
        for (int i =0; i < 5; i++ ){

            for (SimulationPath path : paths) {
                
                context.setCurrentPath(path);
                StatefulKnowledgeSession session = TestUtils.createSession("BPMN2-TwoUserTasks.bpmn2");
                
                context.setClock((SessionPseudoClock) session.getSessionClock());
                // set start date to current time
                context.getClock().advanceTime(System.currentTimeMillis(), TimeUnit.MILLISECONDS);
                
                session.startProcess("BPMN2-TwoUserTasks");
                System.out.println("#####################################");
            }
        }
        ((WorkingMemorySimulationRepository) context.getRepository()).fireAllRules();
        List<AggregatedSimulationEvent> results = ((WorkingMemorySimulationRepository) context.getRepository()).getAggregatedEvents();
        
        assertNotNull(results);
        assertEquals(3, results.size());
    }
}
