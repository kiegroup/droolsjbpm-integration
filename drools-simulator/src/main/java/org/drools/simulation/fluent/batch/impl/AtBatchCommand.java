package org.drools.simulation.fluent.batch.impl;

import org.drools.core.command.impl.GenericCommand;
import org.kie.internal.command.Context;

public class AtBatchCommand implements GenericCommand<Void> {

    private long time;

    @Override
    public Void execute(Context context) {
        return null;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }
}
