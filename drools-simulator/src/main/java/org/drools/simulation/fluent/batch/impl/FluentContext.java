package org.drools.simulation.fluent.batch.impl;

import org.drools.simulation.fluent.batch.Batch;
import org.drools.simulation.fluent.batch.BatchBuilderFluent;
import org.kie.api.command.Command;

public class FluentContext {
    private BatchBuilderFluent batchBuilderFluent;

    private Batch batch;

    private Batch focus;

    public FluentContext() {
        batch = new BatchImpl();
        focus = batch;
    }

    public BatchBuilderFluent getBatchBuilderFluent() {
        return batchBuilderFluent;
    }

    public void setBatchBuilderFluent(BatchBuilderFluent batchBuilderFluent) {
        this.batchBuilderFluent = batchBuilderFluent;
    }

    public void addCommand(Command cmd) {
        if( cmd instanceof Batch) {
            batch.addCommand(cmd);
            this.focus = (Batch) cmd;
        } else {
            this.focus.addCommand(cmd);
        }
    }

    public Batch getBatch() {
        return batch;
    }
}
