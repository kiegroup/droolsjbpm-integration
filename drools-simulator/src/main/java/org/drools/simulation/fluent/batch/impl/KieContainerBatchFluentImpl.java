package org.drools.simulation.fluent.batch.impl;

import org.drools.core.command.NewKieSessionCommand;
import org.drools.simulation.fluent.batch.Batch;
import org.drools.simulation.fluent.batch.BatchBuilderFluent;
import org.drools.simulation.fluent.batch.KieContainerBatchFluent;
import org.drools.simulation.fluent.batch.KieSessionBatchFluent;
import org.kie.api.builder.ReleaseId;

public class KieContainerBatchFluentImpl extends BaseBatchFluent<BatchBuilderFluent> implements KieContainerBatchFluent {

    private FluentContext ctx;
    private ReleaseId     releaseId;

    public KieContainerBatchFluentImpl(FluentContext ctx, ReleaseId releaseId) {
        super(ctx);
        this.ctx = ctx;
    }
    @Override
    public KieSessionBatchFluent newSession() {
        return newSession(null);
    }

    @Override
    public KieSessionBatchFluent newSession(String id) {
        NewKieSessionCommand cmd = new NewKieSessionCommand(releaseId, null);
        ctx.addCommand(cmd);
        return new KieSessionBatchFluentImpl(ctx);
    }


}
