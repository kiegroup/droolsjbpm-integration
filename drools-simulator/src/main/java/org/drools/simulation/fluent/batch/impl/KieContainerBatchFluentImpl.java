package org.drools.simulation.fluent.batch.impl;

import org.drools.core.command.NewKieSessionCommand;
import org.drools.simulation.fluent.batch.Batch;
import org.drools.simulation.fluent.batch.KieContainerBatchFluent;
import org.drools.simulation.fluent.batch.KieSessionBatchFluent;

public class KieContainerBatchFluentImpl implements KieContainerBatchFluent {

    private FluentContext ctx;

    public KieContainerBatchFluentImpl(FluentContext ctx) {
        this.ctx = ctx;
    }

    @Override
    public KieSessionBatchFluent newSession() {
        //new NewKieSessionCommand();
        return new KieSessionBatchFluentImpl(ctx);
    }

    @Override
    public KieSessionBatchFluent newSession(String id) {
        return null;
    }



}
