package org.drools.simulation.fluent.batch.impl;

import org.drools.core.command.NewKieSessionCommand;
import org.drools.simulation.fluent.batch.Batch;
import org.drools.simulation.fluent.batch.BatchBuilderFluent;
import org.drools.simulation.fluent.batch.KieContainerBatchFluent;
import org.drools.simulation.fluent.batch.KieSessionBatchFluent;
import org.kie.api.builder.ReleaseId;

public class KieContainerBatchFluentImpl extends BaseBatchFluent<BatchBuilderFluent> implements KieContainerBatchFluent {

    private FluentContext ctx;

    public KieContainerBatchFluentImpl(FluentContext ctx) {
        super(ctx);
        this.ctx = ctx;
    }
    @Override
    public KieSessionBatchFluent newSession() {
        return newSession(null);
    }

    @Override
    public KieSessionBatchFluent newSession(String sessionId) {
        NewKieSessionCommand cmd = new NewKieSessionCommand(sessionId);
        ctx.addCommand(cmd);
        return new KieSessionBatchFluentImpl(ctx);
    }


}
