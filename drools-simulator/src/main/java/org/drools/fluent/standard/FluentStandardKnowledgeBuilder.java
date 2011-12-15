package org.drools.fluent.standard;

import org.drools.fluent.FluentKnowledgeBuilder;
import org.drools.fluent.FluentStep;
import org.drools.fluent.FluentTest;


public interface FluentStandardKnowledgeBuilder extends FluentKnowledgeBuilder<FluentStandardKnowledgeBuilder>, FluentTest<FluentStandardKnowledgeBuilder> {
    
    FluentStandardStep end(String context, String name);
    FluentStandardStep end(String name);    
    FluentStandardStep end();

}