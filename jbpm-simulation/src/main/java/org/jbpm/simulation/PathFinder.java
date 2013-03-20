package org.jbpm.simulation;

import java.util.List;

public interface PathFinder {

    
    List<PathContext> findPaths();
    
    <E> E findPaths(PathFormatConverter<E> converter);
}
