package org.drools.simulation.fluent.batch;

import org.drools.simulation.fluent.batch.impl.TimeFluent;
import org.kie.internal.fluent.ContextFluent;
import org.kie.internal.fluent.EndContextFluent;
import org.kie.internal.fluent.runtime.KieSessionFluent;

public interface KieSessionBatchFluent extends
        TimeFluent<KieSessionBatchFluent>,
        KieSessionFluent<KieSessionBatchFluent, BatchBuilderFluent>,
        ContextFluent<KieSessionBatchFluent>,
        EndContextFluent<BatchBuilderFluent> {
}
