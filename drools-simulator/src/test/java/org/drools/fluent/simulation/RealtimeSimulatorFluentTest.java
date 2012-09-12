/*
 * Copyright 2012 JBoss by Red Hat.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.drools.fluent.simulation;

import java.util.ArrayList;
import java.util.List;
import org.drools.KnowledgeBase;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.ResourceType;
import org.drools.command.World;
import org.drools.fluent.simulation.impl.DefaultSimulationFluent;
import org.drools.io.ResourceFactory;
import org.drools.simulation.impl.RealtimeStepExecutionHandler;
import org.drools.simulation.impl.Simulator;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 */
public class RealtimeSimulatorFluentTest {
    
    private long[] timeGaps = new long[]{0, 400, 200};
    
    //in ms
    private final int tolerance = 100;
    
    @Test
    public void testRealtimeSimulator(){
        
        List pseudoList = new ArrayList();
        List realList = new ArrayList();
        
        this.createSimulation(pseudoList, realList, new RealtimeStepExecutionHandler()).runSimulation();
        validateResults(pseudoList, realList, 1);
    }
    
    @Test
    public void testRealtimeSimulatorWithFactorOf2(){
        float factor = 2;
        this.testRealtimeSimulatorWithFactor(factor);
    }
    
    @Test
    public void testRealtimeSimulatorWithFactorOf05(){
        float factor = 0.5f;
        this.testRealtimeSimulatorWithFactor(factor);
    }
    
    public void testRealtimeSimulatorWithFactor(float factor){
        List pseudoList = new ArrayList();
        List realList = new ArrayList();
        
        this.createSimulation(pseudoList, realList, new RealtimeStepExecutionHandler(factor)).runSimulation();
        
        validateResults(pseudoList, realList, factor);
    }
    
    private void validateResults(List pseudoList, List realList, float factor) {
        //pseudo clock must always respect defined times
        int i =0;
        long last = (Long)pseudoList.get(0);
        for (Object object : pseudoList) {
            long value = (Long) object;
            Assert.assertEquals(timeGaps[i++], value - last);
            last = value;
        }
 
        //real clock depends on factor. If factor is 1 these times should be 
        //very close to the pseudo-clock times.
        i=0;
        last = (Long)realList.get(0);
        for (Object object : realList) {
            long value = (Long) object;
            long diff = (long) (value - last - timeGaps[i++] / factor);
            Assert.assertTrue( diff >= 0);
            Assert.assertTrue( diff <= tolerance);
            last = value;
        }
    }
    
    private SimulationFluent createSimulation(List pseudoList, List realList, Simulator.StepExecutionHandler stepExecutionHandler){
        SimulationFluent f = new DefaultSimulationFluent();
        

        String str = "package org.drools.simulation.test\n" +
                     "global java.util.List pseudoList\n" +
                     "global java.util.List realList\n" +
                     "rule setTime when String() then pseudoList.add( kcontext.getKnowledgeRuntime().getSessionClock().getCurrentTime() );\n realList.add( System.currentTimeMillis() );\n end\n ";
        
        return f.newPath("init")
        .newStep( timeGaps[0] )
            .newKnowledgeBuilder()
                .add( ResourceFactory.newByteArrayResource( str.getBytes() ),
                      ResourceType.DRL )
                .end(World.ROOT, KnowledgeBuilder.class.getName() )
            .newKnowledgeBase()
                .addKnowledgePackages()
                .end(World.ROOT, KnowledgeBase.class.getName() )
            .newStatefulKnowledgeSession()
                .setGlobal( "pseudoList", pseudoList ).set( "pseudoList" )
                .setGlobal( "realList", realList ).set( "realList" )
                .insert("")
                .fireAllRules()
                .end()
                
        .newStep(timeGaps[0]+timeGaps[1])
            .getStatefulKnowledgeSession()
                .insert("a")
                .fireAllRules()
                .end()
                
        .newStep(timeGaps[1]+timeGaps[2])
            .getStatefulKnowledgeSession()
                .insert("b")
                .fireAllRules()
                .end()
        .setSimulatorStepExecutionHandler(stepExecutionHandler);
    }

}
