package org.kie.server.services.prometheus;

import org.jbpm.executor.AsynchronousJobListener;
import org.jbpm.services.api.DeploymentEventListener;
import org.kie.api.event.rule.AgendaEventListener;
import org.kie.dmn.api.core.event.DMNRuntimeEventListener;
import org.kie.server.services.api.KieContainerInstance;
import org.optaplanner.core.impl.phase.event.PhaseLifecycleListener;

public interface PrometheusMetricsProvider {

    //Drools & DMN
    DMNRuntimeEventListener createDMNRuntimeEventListener(KieContainerInstance kContainer);

    AgendaEventListener createAgendaEventListener(String kieSessionId, KieContainerInstance kContainer);

    //Optaplanner
    PhaseLifecycleListener createPhaseLifecycleListener(String solverId);

    //jBPM
    AsynchronousJobListener createAsynchronousJobListener();

    DeploymentEventListener createDeploymentEventListener();
}
