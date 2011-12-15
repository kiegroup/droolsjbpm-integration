package org.drools.fluent;

import org.drools.fluent.standard.FluentStandardSimulation;


public interface FluentPath<T, S> extends FluentBase, FluentTest<T> {
    S newStep(long distance);
    
//    FluentPath newPath(String name); // ends current path and creates a new one
    
//    BU newKnowledgeBuilder();
//    BA newKnowledgeBase();
//    S newStatefulKnowledgeSession();
//
//    BU getKnowledgeBuilder();
//    BA getKnowledgeBase();
//    S getStatefulKnowledgeSession();        
//    
//    BU getKnowledgeBuilder(String name);
//    BA getKnowledgeBase(String name);
//    S getStatefulKnowledgeSession(String name);  

}