package org.drools.fluent.compact;

import org.drools.fluent.FluentKnowledgeBase;
import org.drools.fluent.FluentTest;

public interface FluentCompactKnowledgeBase extends FluentKnowledgeBase<FluentCompactKnowledgeBase>, FluentTest<FluentCompactKnowledgeBase> {
    
    FluentCompactStatefulKnowledgeSession end();
    
}