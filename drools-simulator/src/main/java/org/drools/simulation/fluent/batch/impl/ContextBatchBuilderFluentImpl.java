package org.drools.simulation.fluent.batch.impl;

import org.drools.simulation.fluent.batch.Batch;
import org.drools.simulation.fluent.batch.BatchBuilderFluent;
import org.drools.simulation.fluent.batch.ContextBatchBuilderFluent;

import java.util.ArrayList;
import java.util.List;

public class ContextBatchBuilderFluentImpl implements ContextBatchBuilderFluent {

    List<Batch> batches = new ArrayList<Batch>();



    @Override
    public BatchBuilderFluent newBatch() {

        return null;
    }
}
