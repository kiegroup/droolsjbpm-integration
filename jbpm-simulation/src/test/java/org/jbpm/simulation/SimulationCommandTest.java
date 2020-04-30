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

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.drools.core.impl.KnowledgeBaseFactory;
import org.drools.core.time.SessionPseudoClock;
import org.jbpm.simulation.converter.SimulationFilterPathFormatConverter;
import org.jbpm.simulation.helper.HardCodedSimulationDataProvider;
import org.jbpm.simulation.impl.SimulateProcessPathCommand;
import org.jbpm.simulation.impl.SimulationPath;
import org.junit.Test;
import org.kie.api.KieBase;
import org.kie.api.KieServices;
import org.kie.api.io.ResourceType;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.KieSessionConfiguration;
import org.kie.api.runtime.conf.ClockTypeOption;
import org.kie.internal.builder.KnowledgeBuilder;
import org.kie.internal.builder.KnowledgeBuilderFactory;
import org.kie.internal.io.ResourceFactory;

public class SimulationCommandTest {

    @Test
    public void testSimulationCommand() {
        
        PathFinder finder = PathFinderFactory.getInstance(this.getClass().getResourceAsStream("/BPMN2-ExclusiveSplit.bpmn2"));
        
        List<SimulationPath> paths = finder.findPaths(new SimulationFilterPathFormatConverter());
        SimulationContext context = SimulationContextFactory.newContext(new HardCodedSimulationDataProvider());
        
        KnowledgeBuilder builder = KnowledgeBuilderFactory.newKnowledgeBuilder();
        
        builder.add(ResourceFactory.newClassPathResource("BPMN2-ExclusiveSplit.bpmn2"), ResourceType.BPMN2);
        
        KieBase kbase = builder.newKieBase();
        KieSessionConfiguration config = KnowledgeBaseFactory.newKnowledgeSessionConfiguration();
        config.setOption(ClockTypeOption.get("pseudo") );
        
        for (SimulationPath sp : paths) {
        
            KieSession session = kbase.newKieSession(config, KieServices.get().newEnvironment());
            ((SessionPseudoClock) session.getSessionClock()).advanceTime(System.currentTimeMillis(), TimeUnit.MILLISECONDS);
            
            session.execute(new SimulateProcessPathCommand("com.sample.test", context, sp));
        }
    }
}
