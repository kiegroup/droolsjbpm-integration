package org.drools.fluent.compact;

import org.drools.fluent.FluentStatefulKnowledgeSession;
import org.drools.fluent.FluentTest;


public interface FluentCompactStatefulKnowledgeSession  extends FluentStatefulKnowledgeSession<FluentCompactStatefulKnowledgeSession>, FluentTest<FluentCompactStatefulKnowledgeSession> { 
    FluentCompactStatefulKnowledgeSession newStep(long distance);
    
    /**
     * The knowledge base is already created and attached to the KnowledgeBuilder, so all kbuilder changes are automatically reflected in kbase.
     * @return
     */
    FluentCompactKnowledgeBase getKnowledgeBase();     
    
    /**
     * The last executed command, if it returns a value, is set to a name in this executings context
     * @param name
     * @return
     */
    FluentCompactStatefulKnowledgeSession set(String name);
           
    /**
     * The contexts for tests will still refer to the path created by this ksession, until a new ksession is created
     * @return
     */
    FluentCompactSimulation end();
}