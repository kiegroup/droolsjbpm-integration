package org.drools.fluent.standard;

import org.drools.fluent.FluentBase;
import org.drools.fluent.FluentPath;
import org.drools.fluent.FluentRoot;
import org.drools.fluent.FluentStep;
import org.drools.fluent.FluentTest;

public interface FluentStandardSimulation extends FluentBase, FluentRoot, FluentTest<FluentStandardSimulation> {
    
    FluentStandardPath newPath(String name);
    
    FluentStandardPath getPath(String name);   
    
}