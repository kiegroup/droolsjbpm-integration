package org.drools.fluent.standard;

import org.drools.fluent.FluentStatefulKnowledgeSession;
import org.drools.fluent.FluentStep;
import org.drools.fluent.FluentTest;


public interface FluentStandardStatefulKnowledgeSession  extends FluentStatefulKnowledgeSession<FluentStandardStatefulKnowledgeSession>, FluentTest<FluentStandardStatefulKnowledgeSession>  {  
    
    FluentStandardStep end(String context, String name);
    FluentStandardStep end(String name);
    FluentStandardStep end();
    
}