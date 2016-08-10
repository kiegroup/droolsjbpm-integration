package org.drools.simulation.fluent.batch;

import org.drools.core.command.impl.GenericCommand;
import org.kie.api.command.Command;

import java.util.List;

public interface Batch {

    public long getDistance();

    public void addCommand(Command cmd);

    List<Command> getCommands();
}
