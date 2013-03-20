package org.jbpm.simulation;

public interface ActivitySimulator {

    public SimulationEvent simulate(Object activity, SimulationContext context);
}
