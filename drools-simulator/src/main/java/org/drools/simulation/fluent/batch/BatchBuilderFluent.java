package org.drools.simulation.fluent.batch;

import org.drools.simulation.fluent.batch.impl.TimeFluent;
import org.kie.api.builder.ReleaseId;
import org.kie.internal.fluent.ContextFluent;
import org.kie.internal.fluent.EndContextFluent;

public interface BatchBuilderFluent extends TimeFluent<BatchBuilderFluent>, ContextFluent<BatchBuilderFluent> {
    KieContainerBatchFluent getKieContainer(ReleaseId releaseId);
}
