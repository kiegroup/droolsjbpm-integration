package org.drools.fluent;


public interface FluentStep<ST, BU, BA, S> extends FluentBase, FluentTest<ST> {
    BU newKnowledgeBuilder();
    BA newKnowledgeBase();
    S newStatefulKnowledgeSession();

    BU getKnowledgeBuilder();
    BA getKnowledgeBase();
    S getStatefulKnowledgeSession();        
    
    BU getKnowledgeBuilder(String name);
    BA getKnowledgeBase(String name);
    S getStatefulKnowledgeSession(String name);
}