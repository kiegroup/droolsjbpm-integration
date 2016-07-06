package org.drools.simulation.fluent.batch.impl;

import org.drools.simulation.fluent.batch.Batch;
import org.drools.simulation.fluent.batch.BatchBuilderFluent;
import org.kie.api.command.Command;

import java.util.ArrayList;
import java.util.List;

public class FluentContext {
    private BatchBuilderFluent batchBuilderFluent;

    private Batch batch;

    private List<Batch> batches;

    public FluentContext() {
        batch = new BatchImpl();
        batches = new ArrayList<Batch>();
        batches.add(batch);
    }

    public BatchBuilderFluent getBatchBuilderFluent() {
        return batchBuilderFluent;
    }

    public void setBatchBuilderFluent(BatchBuilderFluent batchBuilderFluent) {
        this.batchBuilderFluent = batchBuilderFluent;
    }

    public void addCommand(Command cmd) {
        if( cmd instanceof Batch) {
            batches.add((Batch)cmd);
            batch = (Batch) cmd;
        }

        batch.addCommand(cmd);
    }

    public Batch getBatch() {
        return batch;
    }

    public List<Batch> getBatches() {
        return batches;
    }
}
