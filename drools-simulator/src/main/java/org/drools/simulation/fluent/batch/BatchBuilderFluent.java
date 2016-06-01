package org.drools.simulation.fluent.batch;

import org.drools.simulation.fluent.batch.impl.TimeFluent;
import org.kie.api.builder.ReleaseId;
import org.kie.internal.fluent.ContextFluent;
import org.kie.internal.fluent.EndContextFluent;

public interface BatchBuilderFluent extends TimeFluent<BatchBuilderFluent> {

    <K>  K get(String name, Class<K> cls);

    //KieSessionBatchFluent<BatchFluentBuilder> newSession(ReleaseId releaseId);
    //KieContainerBatchFluent getKieContainer(ReleaseId releaseId);

//    KieSessionBatchFluent<BatchFluentBuilder> getKieSession();

    KieSessionBatchFluent getSession(ReleaseId releaseId);

    KieSessionBatchFluent getSession(ReleaseId releaseId, String sessionId);

    BatchBuilderFluent newContext(String name);

    BatchBuilderFluent getContext(String name);

}
