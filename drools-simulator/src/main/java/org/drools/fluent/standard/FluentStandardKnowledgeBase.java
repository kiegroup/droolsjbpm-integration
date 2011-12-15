package org.drools.fluent.standard;

import org.drools.fluent.FluentKnowledgeBase;
import org.drools.fluent.FluentStep;
import org.drools.fluent.FluentTest;


public interface FluentStandardKnowledgeBase extends FluentKnowledgeBase<FluentStandardKnowledgeBase>, FluentTest<FluentStandardKnowledgeBase> {  
    
    FluentStandardStatefulKnowledgeSession newStatefulKnowledgeSession();
    
    FluentStandardStep end(String context, String name);
    
    FluentStandardStep end(String name);
    
    FluentStandardStep end();
        
}