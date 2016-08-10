package org.drools.simulation.fluent.batch.impl;

import org.drools.simulation.fluent.batch.Batch;
import org.drools.simulation.fluent.batch.BatchBuilderFluent;
import org.kie.api.command.Command;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FluentContext {
    private BatchFluentComponentFactory factory;
    private BatchBuilderFluent batchBuilderFluent;

    private Batch batch;

    private List<Batch> batches;

    public FluentContext() {
        batches = new ArrayList<Batch>();
    }

    public BatchBuilderFluent getBatchBuilderFluent() {
        return batchBuilderFluent;
    }

    public void setBatchBuilderFluent(BatchBuilderFluent batchBuilderFluent) {
        this.batchBuilderFluent = batchBuilderFluent;
    }

    public BatchFluentComponentFactory getFactory() {
        if ( factory == null ) {
            factory = new BatchFluentComponentFactory();
        }
        return factory;
    }

    public void setFactory(BatchFluentComponentFactory factory) {
        this.factory = factory;
    }

    public void addCommand(Command cmd) {
        if ( batch == null ) {
            batch = new BatchImpl();
            addBatch(batch);
        }
        batch.addCommand(cmd);
    }

    public void addBatch(Batch batch) {
        batches.add(batch);
        this.batch = batch;
    }

    public Batch getBatch() {
        return batch;
    }

    public List<Batch> getBatches() {
        return batches;
    }
}
