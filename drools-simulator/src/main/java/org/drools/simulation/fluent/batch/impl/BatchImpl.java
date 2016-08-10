package org.drools.simulation.fluent.batch.impl;

import org.drools.core.command.impl.GenericCommand;
import org.drools.simulation.fluent.batch.Batch;
import org.kie.api.command.Command;

import java.util.ArrayList;
import java.util.List;

public class BatchImpl implements Batch {
    private long distance;

    private List<Command> commands = new ArrayList<Command>();

    public BatchImpl() {
    }

    public BatchImpl(long distance) {
        this.distance = distance;
    }

    public long getDistance() {
        return distance;
    }

    public void setDistance(int distance) {
        this.distance = distance;
    }

    public void addCommand(Command cmd) {
        this.commands.add(cmd);
    }

    @Override
    public List<Command> getCommands() {
        return commands;
    }
}
