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

package org.jbpm.simulation.impl;

import java.util.ArrayList;
import java.util.List;

import org.jbpm.simulation.AggregatedSimulationEvent;
import org.jbpm.simulation.SimulationEvent;
import org.jbpm.simulation.impl.events.AggregatedActivitySimulationEvent;
import org.jbpm.simulation.impl.events.AggregatedProcessSimulationEvent;
import org.kie.api.io.Resource;
import org.kie.api.io.ResourceType;
import org.kie.api.runtime.KieSession;
import org.kie.internal.builder.KnowledgeBuilder;
import org.kie.internal.builder.KnowledgeBuilderFactory;
import org.kie.internal.io.ResourceFactory;

public class WorkingMemorySimulationRepository extends InMemorySimulationRepository {

    private KieSession ksession;
    private boolean fireRulesOnStore = false;
    
    public WorkingMemorySimulationRepository() {
        
    }
    
    public WorkingMemorySimulationRepository(String... rules) {
        this(false, rules);
    }
    
    public WorkingMemorySimulationRepository(Resource... rules) {
        this(false, rules);
    }
    
    public WorkingMemorySimulationRepository(boolean fireRulesOnStore, Resource... rules) {
        this.fireRulesOnStore = fireRulesOnStore;
        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
        
        for (Resource path : rules) {
        
            kbuilder.add(path, ResourceType.DRL);
        }
        if (kbuilder.hasErrors()) {
            throw new RuntimeException("Error while building knowledge base: " + kbuilder.getErrors());
        }
        
        this.ksession = kbuilder.newKnowledgeBase().newKieSession();
        try {
            // register global for aggregated events
            ksession.setGlobal("logger", new SystemOutLogger());
            ksession.setGlobal("simulation", new ArrayList<AggregatedActivitySimulationEvent>());
            ksession.setGlobal("summary", new ArrayList<AggregatedActivitySimulationEvent>());            
            AggregatedProcessSimulationEvent init = new AggregatedProcessSimulationEvent("", 0, 0, 0);
            List processOnlyList = new ArrayList<AggregatedSimulationEvent>();
            processOnlyList.add(init);
            ksession.setGlobal("processEventsOnly", processOnlyList);
        } catch (Exception e) {
            // catch it as there could be no simulation global declared
        }
    }
    
    public WorkingMemorySimulationRepository(boolean fireRulesOnStore, String... rules) {
        this.fireRulesOnStore = fireRulesOnStore;
        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
        
        for (String path : rules) {
        
            kbuilder.add(ResourceFactory.newClassPathResource(path), ResourceType.DRL);
        }
        if (kbuilder.hasErrors()) {
            throw new RuntimeException("Error while building knowledge base: " + kbuilder.getErrors());
        }
        
        this.ksession = kbuilder.newKnowledgeBase().newKieSession();
        try {
            ksession.setGlobal("logger", new SystemOutLogger());
            // register global for aggregated events
            ksession.setGlobal("simulation", new ArrayList<AggregatedActivitySimulationEvent>());
        } catch (Exception e) {
            // catch it as there could be no simulation global declared
        }
    }
    
    public void storeEvent(SimulationEvent event) {
        super.storeEvent(event);
        ksession.insert(event);
        if (fireRulesOnStore) {
            ksession.fireAllRules();
        }
    }

    public void fireAllRules() {
        ksession.fireAllRules();
    }
    
    public KieSession getSession() {
        return this.ksession;
    }
    
    public List<AggregatedSimulationEvent> getAggregatedEvents() {
        return (List<AggregatedSimulationEvent>) this.ksession.getGlobal("simulation");
    }
    
    public Object getGlobal(String globalName) {
        return  this.ksession.getGlobal(globalName);
    }

    @Override
    public void close() {
        super.close();
        this.ksession.dispose();
    }
    
    
}
