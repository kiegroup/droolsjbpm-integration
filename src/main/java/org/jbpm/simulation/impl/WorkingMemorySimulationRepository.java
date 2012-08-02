package org.jbpm.simulation.impl;

import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.io.ResourceFactory;
import org.drools.runtime.StatefulKnowledgeSession;
import org.jbpm.simulation.SimulationEvent;
import org.jbpm.simulation.SimulationRepository;

public class WorkingMemorySimulationRepository implements SimulationRepository {

    private StatefulKnowledgeSession ksession;
    
    public WorkingMemorySimulationRepository(String... rules) {
        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
        
        for (String path : rules) {
        
            kbuilder.add(ResourceFactory.newClassPathResource(path), ResourceType.DRL);
        }
        if (kbuilder.hasErrors()) {
            throw new RuntimeException("Error while building knowledge base: " + kbuilder.getErrors());
        }
        
        this.ksession = kbuilder.newKnowledgeBase().newStatefulKnowledgeSession();
    }
    
    public void storeEvent(SimulationEvent event) {
        ksession.insert(event);
//        ksession.fireAllRules();
    }

    public void fireAllRules() {
        ksession.fireAllRules();
    }
}
