package org.drools.simulation.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.drools.command.Context;
import org.drools.simulation.Path;
import org.drools.simulation.Simulation;
import org.drools.simulation.Step;

public class PathImpl
    implements
    Path {
    private SimulationImpl   simulation;

    private String           name;

    private Collection<Step> steps;

    public PathImpl(Simulation simulation,
                    String name) {
        this.name = name;
        this.simulation = (SimulationImpl) simulation;
    }

    public void setSteps(Collection<Step> steps) {
        this.steps = steps;
    }

    public String getName() {
        return this.name;
    }

    public Collection<Step> getSteps() {
        return this.steps;
    }

}
