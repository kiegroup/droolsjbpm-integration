package org.drools.simulation.fluent.batch.impl;

import org.drools.core.command.impl.GenericCommand;
import org.drools.simulation.fluent.batch.Batch;
import org.kie.internal.command.Context;

public class AfterBatchCommand extends BatchImpl implements GenericCommand<Void>, Batch {

    private long distance;

    public AfterBatchCommand(long distance) {
        this.distance = distance;
    }

    @Override
    public Void execute(Context context) {
        return null;
    }

    public long getDistance() {
        return distance;
    }

    public void setDistance(long distance) {
        this.distance = distance;
    }

}
