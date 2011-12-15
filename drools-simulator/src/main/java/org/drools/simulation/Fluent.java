package org.drools.simulation;

import org.drools.KnowledgeBase;
import org.drools.builder.KnowledgeBuilderErrors;
import org.drools.builder.ResourceConfiguration;
import org.drools.builder.ResourceType;
import org.drools.io.Resource;
import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.rule.FactHandle;

public interface Fluent {       
        Object getValue(); // returns the last commands returned value
        Fluent set(String name);   // assigns the last commands return vlaue to a variable
        
    
        Fluent newPath(String name);
        Fluent createStep(long distance);

        Fluent createKnowledgeBuilder();

        Fluent hasErrors();        
        Fluent getErrors();

        Fluent add(Resource resource,
                   ResourceType type);

        Fluent add(Resource resource,
                                   ResourceType type,
                                   ResourceConfiguration configuration); 
        
        Fluent newKnowledgeBase();        
        
        Fluent fireAllRules();
        
        Fluent insert(Object object);
    
}
    