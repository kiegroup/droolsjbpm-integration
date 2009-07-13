package org.drools.simulation;

import java.util.Collection;

public interface Path {
    String getName();
    
    Collection<Step> getSteps();
}
