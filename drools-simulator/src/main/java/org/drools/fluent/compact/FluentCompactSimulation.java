package org.drools.fluent.compact;

import org.drools.fluent.FluentBase;
import org.drools.fluent.FluentRoot;
import org.drools.fluent.FluentTest;

public interface FluentCompactSimulation extends FluentBase, FluentRoot, FluentTest<FluentCompactSimulation> {
    FluentCompactStatefulKnowledgeSession newStatefulKnowledgeSession();  
}