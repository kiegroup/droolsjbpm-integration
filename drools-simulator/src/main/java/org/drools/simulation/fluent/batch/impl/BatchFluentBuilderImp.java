package org.drools.simulation.fluent.batch.impl;

import org.drools.core.command.GetKieContainerCommand;
import org.drools.core.command.NewKieSessionCommand;
import org.drools.simulation.fluent.batch.*;
import org.kie.api.builder.ReleaseId;

public class BatchFluentBuilderImp extends BaseBatchFluent<BatchBuilderFluent> implements BatchBuilderFluent {



    public BatchFluentBuilderImp(FluentContext fluentCtx) {
        super(fluentCtx);
    }

    @Override
    public <K> K get(String name, Class<K> cls) {
        return null;
    }

//    @Override
//    public KieContainerBatchFluent getKieContainer(ReleaseId releaseId) {
//        addCommand( new GetKieContainerCommand(releaseId) );
//        KieContainerBatchFluentImpl fluent = new KieContainerBatchFluentImpl(fluentCtx);
//        return fluent;
//    }

    @Override
    public BatchBuilderFluent newContext(String name) {
        addCommand(new NewContextCommand(name));
        return this;
    }

    @Override
    public BatchBuilderFluent getContext(String name) {
        addCommand(new GetContextCommand(name));
        return this;
    }

    @Override
    public KieSessionBatchFluent getSession(ReleaseId releaseId) {
        return getSession(releaseId, null);
    }

    @Override
    public KieSessionBatchFluent getSession(ReleaseId releaseId, String sessionId) {
        NewKieSessionCommand cmd = new NewKieSessionCommand(releaseId, sessionId);
        fluentCtx.addCommand(cmd);
        return new KieSessionBatchFluentImpl(fluentCtx);
    }
}
