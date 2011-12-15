package org.drools.fluent.standard;

import org.drools.fluent.FluentPath;

public interface FluentStandardPath extends FluentPath<FluentStandardPath, FluentStandardStep> {
    
    FluentStandardPath getPath(String name);
    
    FluentStandardPath newPath(String name);
    
    FluentStandardSimulation end();
}
