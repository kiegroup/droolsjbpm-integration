package org.drools.fluent.compact;

import org.drools.command.Command;
import org.drools.simulation.Simulation;

public interface InternalSimulation extends Simulation {
    void addCommand(Command cmd);

    void newStep(long distance);
}
