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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.drools.core.command.runtime.rule.InsertElementsCommand;
import org.jbpm.simulation.impl.WorkingMemorySimulationRepository;
import org.jbpm.simulation.impl.events.ActivitySimulationEvent;
import org.jbpm.simulation.impl.events.AggregatedEndEventSimulationEvent;
import org.jbpm.simulation.impl.events.AggregatedProcessSimulationEvent;
import org.jbpm.simulation.impl.events.EndSimulationEvent;
import org.jbpm.simulation.impl.events.GenericSimulationEvent;
import org.jbpm.simulation.impl.events.HTAggregatedSimulationEvent;
import org.jbpm.simulation.impl.events.HumanTaskActivitySimulationEvent;
import org.jbpm.simulation.impl.events.ProcessInstanceEndSimulationEvent;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class SimulateProcessTest {

    @Before
    public void configure() {
        // enable logging
        //System.setProperty("jbpm.simulation.log.enabled", "true");
    }
    
    @Test
    public void testSimulationRunner() throws IOException {
        
        InputStreamReader in = new InputStreamReader(this.getClass().getResourceAsStream("/BPMN2-TwoUserTasks.bpmn2"));
        
        String out = new String();
        BufferedReader br = new BufferedReader(in);
        for(String line = br.readLine(); line != null; line = br.readLine()) 
          out += line;


        
        SimulationRepository repo = SimulationRunner.runSimulation("BPMN2-TwoUserTasks", out, 10, 2000, "default.simulation.rules.drl");
        assertNotNull(repo);
        
        WorkingMemorySimulationRepository wmRepo = (WorkingMemorySimulationRepository) repo;
        wmRepo.fireAllRules();
        
        assertEquals(4, wmRepo.getAggregatedEvents().size());
        assertEquals(50, wmRepo.getEvents().size());
        
        AggregatedSimulationEvent event = wmRepo.getAggregatedEvents().get(0);
        if (event instanceof AggregatedEndEventSimulationEvent) {
            assertNotNull(event.getProperty("minProcessDuration"));
            assertFalse(event.getProperty("activityId").equals(""));
        } 
        
        event = wmRepo.getAggregatedEvents().get(1);
        assertFalse(event.getProperty("activityId").equals(""));
        assertNotNull(event.getProperty("minExecutionTime"));
        event = wmRepo.getAggregatedEvents().get(2);
        assertFalse(event.getProperty("activityId").equals(""));
        assertNotNull(event.getProperty("minExecutionTime"));
        
        event = wmRepo.getAggregatedEvents().get(3);
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
        assertEquals(5, wmRepo.getAggregatedEvents().size());
        assertEquals(70, wmRepo.getEvents().size());
        
        List<AggregatedSimulationEvent> aggEvents = wmRepo.getAggregatedEvents();
        for (AggregatedSimulationEvent event : aggEvents) {
            if (event instanceof AggregatedProcessSimulationEvent) {
                Map<String, Integer> numberOfInstancePerPath = ((AggregatedProcessSimulationEvent) event).getPathNumberOfInstances();
                assertNotNull(numberOfInstancePerPath);
                assertTrue(3 == numberOfInstancePerPath.get("Path800898475-0"));
                assertTrue(7 == numberOfInstancePerPath.get("Path-960633761-1"));
            }
        }
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
        assertEquals(4, wmRepo.getAggregatedEvents().size());
        assertEquals(7, wmRepo.getEvents().size());
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
        assertEquals(5, wmRepo.getAggregatedEvents().size());
        assertEquals(14, wmRepo.getEvents().size());
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

        assertEquals(20, wmRepo.getAggregatedEvents().size());
        assertEquals(35, wmRepo.getEvents().size());
        wmRepo.close();
    }
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
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

        assertEquals(20, wmRepo.getAggregatedEvents().size());
        assertEquals(35, wmRepo.getEvents().size());

        for (SimulationEvent event : wmRepo.getEvents()) {
            if ((event instanceof EndSimulationEvent) || (event instanceof ActivitySimulationEvent)|| (event instanceof HumanTaskActivitySimulationEvent)) {
                assertNotNull(((GenericSimulationEvent) event).getAggregatedEvent());
                assertTrue(((GenericSimulationEvent) event).getAggregatedEvent() instanceof AggregatedProcessSimulationEvent);
            } else if (event instanceof ProcessInstanceEndSimulationEvent) {
                assertNull(((GenericSimulationEvent) event).getAggregatedEvent());
            }
        }
        wmRepo.getSession().execute(new InsertElementsCommand((Collection)wmRepo.getAggregatedEvents()));
        wmRepo.fireAllRules();
        List<AggregatedSimulationEvent> summary = (List<AggregatedSimulationEvent>) wmRepo.getGlobal("summary");
        assertNotNull(summary);
        assertEquals(5, summary.size());
        for (AggregatedSimulationEvent event : summary) {
            if (event instanceof AggregatedProcessSimulationEvent) {
                Map<String, Integer> numberOfInstancePerPath = ((AggregatedProcessSimulationEvent) event).getPathNumberOfInstances();
                assertNotNull(numberOfInstancePerPath);
                assertEquals(1, (int)numberOfInstancePerPath.get("Path800898475-0"));
                assertEquals(4, (int)numberOfInstancePerPath.get("Path-960633761-1"));
            }
        }
        
        SimulationInfo info = wmRepo.getSimulationInfo();
        
        assertNotNull(info);
        assertEquals("defaultPackage.test", info.getProcessId());
        assertEquals("test", info.getProcessName());
        assertEquals(5, info.getNumberOfExecutions());
        assertEquals(2000, info.getInterval());
        
        System.out.println("Start date is " + new Date(info.getStartTime()) + " end date is " + new Date(info.getEndTime()));
        wmRepo.close();
    }
    
    @Test
    public void testSimulationRunnerWithSinglePath() throws IOException {
        
        InputStreamReader in = new InputStreamReader(this.getClass().getResourceAsStream("/BPMN2-UserTaskWithSimulationMetaData.bpmn2"));
        
        String out = new String();
        BufferedReader br = new BufferedReader(in);
        for(String line = br.readLine(); line != null; line = br.readLine()) 
          out += line;


        
        SimulationRepository repo = SimulationRunner.runSimulation("UserTask", out, 5, 2000, true, "onevent.simulation.rules.drl");
        assertNotNull(repo);
        
        WorkingMemorySimulationRepository wmRepo = (WorkingMemorySimulationRepository) repo;

        assertEquals(15, wmRepo.getAggregatedEvents().size());
        assertEquals(20, wmRepo.getEvents().size());
        wmRepo.close();
    }
    
    @Test
    public void testSimulationRunnerWithSinglePathAndCatchingEvent() throws IOException {
        
        InputStreamReader in = new InputStreamReader(this.getClass().getResourceAsStream("/BPMN2-SinglePathWithCatchingEvent.bpmn2"));
        
        String out = new String();
        BufferedReader br = new BufferedReader(in);
        for(String line = br.readLine(); line != null; line = br.readLine()) 
          out += line;


        
        SimulationRepository repo = SimulationRunner.runSimulation("defaultPackage.test", out, 5, 2000, true, "onevent.simulation.rules.drl");
        assertNotNull(repo);
        
        WorkingMemorySimulationRepository wmRepo = (WorkingMemorySimulationRepository) repo;

        assertEquals(25, wmRepo.getAggregatedEvents().size());
        assertEquals(30, wmRepo.getEvents().size());
        wmRepo.close();
    }
    
    @Test
    public void testSimulationRunnerWithSinglePathAndThrowingEvent() throws IOException {
        
        InputStreamReader in = new InputStreamReader(this.getClass().getResourceAsStream("/BPMN2-SinglePathWithThrowingEvent.bpmn2"));
        
        String out = new String();
        BufferedReader br = new BufferedReader(in);
        for(String line = br.readLine(); line != null; line = br.readLine()) 
          out += line;


        
        SimulationRepository repo = SimulationRunner.runSimulation("defaultPackage.test", out, 5, 2000, true, "onevent.simulation.rules.drl");
        assertNotNull(repo);
        
        WorkingMemorySimulationRepository wmRepo = (WorkingMemorySimulationRepository) repo;

        assertEquals(25, wmRepo.getAggregatedEvents().size());
        assertEquals(30, wmRepo.getEvents().size());
        wmRepo.close();
    }
    
    @Test
    public void testSimulationRunnerWithBoundaryEvent() throws IOException {
        
        InputStreamReader in = new InputStreamReader(this.getClass().getResourceAsStream("/BPMN2-SimpleWithBoundaryEvent.bpmn2"));
        
        String out = new String();
        BufferedReader br = new BufferedReader(in);
        for(String line = br.readLine(); line != null; line = br.readLine()) 
          out += line;


        
        SimulationRepository repo = SimulationRunner.runSimulation("defaultPackage.test", out, 5, 2000, true, "onevent.simulation.rules.drl");
        assertNotNull(repo);
        
        WorkingMemorySimulationRepository wmRepo = (WorkingMemorySimulationRepository) repo;

        assertEquals(25, wmRepo.getAggregatedEvents().size());
        assertEquals(30, wmRepo.getEvents().size());
        wmRepo.close();
    }
    
    @Test
    public void testSimulationRunnerWithScriptRuleXor() throws IOException {
        
        InputStreamReader in = new InputStreamReader(this.getClass().getResourceAsStream("/BPMN2-ScriptRuleXor.bpmn2"));
        
        String out = new String();
        BufferedReader br = new BufferedReader(in);
        for(String line = br.readLine(); line != null; line = br.readLine()) 
          out += line;


        
        SimulationRepository repo = SimulationRunner.runSimulation("defaultPackage.demo", out, 5, 2000, true, "onevent.simulation.rules.drl");
        assertNotNull(repo);
        
        WorkingMemorySimulationRepository wmRepo = (WorkingMemorySimulationRepository) repo;

        assertEquals(30, wmRepo.getAggregatedEvents().size());
        assertEquals(45, wmRepo.getEvents().size());
        
        wmRepo.getSession().execute(new InsertElementsCommand((Collection)wmRepo.getAggregatedEvents()));
        wmRepo.fireAllRules();
        
        List<AggregatedSimulationEvent> summary = (List<AggregatedSimulationEvent>) wmRepo.getGlobal("summary");
        assertNotNull(summary);
        assertEquals(7, summary.size());
        wmRepo.close();
    }
    
    @Test
    public void testSimulationRunnerWithLoop() throws IOException {
        
        InputStreamReader in = new InputStreamReader(this.getClass().getResourceAsStream("/BPMN2-loop-sim.bpmn2"));
        
        String out = new String();
        BufferedReader br = new BufferedReader(in);
        for(String line = br.readLine(); line != null; line = br.readLine()) 
          out += line;


        
        SimulationRepository repo = SimulationRunner.runSimulation("defaultPackage.loop-sim", out, 5, 2000, true, "onevent.simulation.rules.drl");
        assertNotNull(repo);
        
        WorkingMemorySimulationRepository wmRepo = (WorkingMemorySimulationRepository) repo;

        assertEquals(19, wmRepo.getAggregatedEvents().size());
        assertEquals(37, wmRepo.getEvents().size());
        wmRepo.close();
    }

    @Test
    public void testSimulationRunnerEmbeddedSubprocessWithActivites() throws IOException {

        InputStreamReader in = new InputStreamReader(this.getClass().getResourceAsStream("/BPMN2-EmbeddedSubprocessWithActivites.bpmn2"));

        String out = new String();
        BufferedReader br = new BufferedReader(in);
        for(String line = br.readLine(); line != null; line = br.readLine())
            out += line;



        SimulationRepository repo = SimulationRunner.runSimulation("project.simulation", out, 10, 120000, true, "onevent.simulation.rules.drl");
        assertNotNull(repo);

        WorkingMemorySimulationRepository wmRepo = (WorkingMemorySimulationRepository) repo;

        assertEquals(50, wmRepo.getAggregatedEvents().size());
        assertEquals(80, wmRepo.getEvents().size());
        wmRepo.close();
    }

    @Test
    public void testSimulationRunnerWithNestedFork() throws IOException {

        InputStreamReader in = new InputStreamReader(this.getClass().getResourceAsStream("/fork-process.bpmn2"));

        String out = new String();
        BufferedReader br = new BufferedReader(in);
        for(String line = br.readLine(); line != null; line = br.readLine())
            out += line;

        Integer intervalInt = 8*1000*60*60;

        SimulationRepository repo = SimulationRunner.runSimulation("simulation.fork-process", out, 40, intervalInt, true, "onevent.simulation.rules.drl");
        assertNotNull(repo);

        WorkingMemorySimulationRepository wmRepo = (WorkingMemorySimulationRepository) repo;
        wmRepo.getSession().execute(new InsertElementsCommand((Collection)wmRepo.getAggregatedEvents()));
        wmRepo.fireAllRules();

        List<AggregatedSimulationEvent> aggEvents = (List<AggregatedSimulationEvent>) wmRepo.getGlobal("summary");

        for (AggregatedSimulationEvent event : aggEvents) {
            if (event instanceof HTAggregatedSimulationEvent) {
                assertEquals(0.0, ((HTAggregatedSimulationEvent) event).getAvgWaitTime(), 0);
            }
        }
        wmRepo.close();

    }
}
