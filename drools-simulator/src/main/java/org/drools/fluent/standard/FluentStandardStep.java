package org.drools.fluent.standard;

import org.drools.fluent.FluentStep;

public interface FluentStandardStep extends FluentStep<FluentStandardStep, FluentStandardKnowledgeBuilder, FluentStandardKnowledgeBase, FluentStandardStatefulKnowledgeSession> {
    FluentStandardStep newStep(long distance);
    
    FluentStandardPath end();
}
