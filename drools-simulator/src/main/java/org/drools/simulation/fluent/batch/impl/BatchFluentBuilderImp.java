package org.drools.simulation.fluent.batch.impl;

import org.drools.core.command.GetKieContainerCommand;
import org.drools.core.command.NewKieSessionCommand;
import org.drools.core.command.impl.ContextImpl;
import org.drools.simulation.fluent.batch.*;
import org.kie.api.builder.ReleaseId;
import org.kie.api.runtime.KieSession;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

public class BatchFluentBuilderImp extends BaseBatchFluent<BatchBuilderFluent> implements BatchBuilderFluent {


    public BatchFluentBuilderImp() {
        super(new FluentContext());
        getFluentContext().setBatchBuilderFluent(this);

    }

    public BatchFluentBuilderImp(FluentContext fluentCtx) {
        super(fluentCtx);
        getFluentContext().setBatchBuilderFluent(this);
    }

    @Override
    public KieContainerBatchFluent getKieContainer(ReleaseId releaseId) {
        addCommand( new GetKieContainerCommand(releaseId) );
        KieContainerBatchFluentImpl fluent = new KieContainerBatchFluentImpl(fluentCtx);
        return fluent;
    }

}
