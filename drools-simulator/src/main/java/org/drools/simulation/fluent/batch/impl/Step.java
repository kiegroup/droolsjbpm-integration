package org.drools.simulation.fluent.batch.impl;

import org.kie.api.command.Command;
import org.kie.internal.command.Context;
import org.kie.internal.simulation.SimulationPath;

import java.util.ArrayList;
import java.util.List;

public class Step {

    private Context ctx;
    private long distance;
    private List<Command> commands = new ArrayList<Command>();

    public Step(Context ctx, long distance) {
        this.ctx = ctx;
        this.distance = distance;
    }

    public Context getContext() {
        return this.ctx;
    }

    public long getDistance() {
        return distance;
    }

    public List<Command> getCommands() {
        return commands;
    }

    @Override
    public String toString() {
        return "Step [context=" + ctx + ", distance=" + distance
                + ", commands=" + commands + "]";
    }
}
