package org.drools.simulation;

import java.util.Collection;

import org.drools.command.Command;

public interface Step {
    long getTemporalDistance();
    Collection<Command> getCommands();
}
