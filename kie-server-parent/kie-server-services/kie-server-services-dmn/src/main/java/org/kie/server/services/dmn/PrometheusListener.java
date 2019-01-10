package org.kie.server.services.dmn;

import io.prometheus.client.Histogram;
import org.kie.dmn.api.core.event.AfterEvaluateBKMEvent;
import org.kie.dmn.api.core.event.AfterEvaluateContextEntryEvent;
import org.kie.dmn.api.core.event.AfterEvaluateDecisionEvent;
import org.kie.dmn.api.core.event.AfterEvaluateDecisionServiceEvent;
import org.kie.dmn.api.core.event.AfterEvaluateDecisionTableEvent;
import org.kie.dmn.api.core.event.BeforeEvaluateBKMEvent;
import org.kie.dmn.api.core.event.BeforeEvaluateContextEntryEvent;
import org.kie.dmn.api.core.event.BeforeEvaluateDecisionEvent;
import org.kie.dmn.api.core.event.BeforeEvaluateDecisionServiceEvent;
import org.kie.dmn.api.core.event.BeforeEvaluateDecisionTableEvent;
import org.kie.dmn.api.core.event.DMNRuntimeEventListener;
import org.kie.dmn.core.impl.AfterEvaluateDecisionEventImpl;
import org.kie.dmn.core.impl.BeforeEvaluateDecisionEventImpl;
import org.kie.dmn.model.api.Decision;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PrometheusListener implements DMNRuntimeEventListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(PrometheusListener.class);

    /**
     * Number of nanoseconds in a second.
     */
    private static final long NANOSECONDS_PER_SECOND = 1_000_000_000;
    private static final long HALF_SECOND_NANO = 500_000_000;

    private static long toNano(long second) {
        return second * NANOSECONDS_PER_SECOND;
    }

    private static final Histogram histogram = Histogram.build().name("dmn_evaluate_decision_nanosecond")
            .help("DMN Evaluation Time")
            .labelNames("decision_name")
            .buckets(HALF_SECOND_NANO, toNano(1), toNano(2), toNano(3), toNano(4))
            .register();

    @Override
    public void beforeEvaluateDecision(BeforeEvaluateDecisionEvent e) {
        long nanoTime = System.nanoTime();
        BeforeEvaluateDecisionEventImpl event = getBeforeImpl(e);
        event.setTimestamp(nanoTime);
    }

    @Override
    public void afterEvaluateDecision(AfterEvaluateDecisionEvent e) {
        BeforeEvaluateDecisionEventImpl event = getBeforeImpl(getAfterImpl(e).getBeforeEvent());
        String decisionName = getDecisionName(e.getDecision().getDecision());
        long startTime = event.getTimestamp();
        long elapsed = System.nanoTime() - startTime;
        histogram.labels(decisionName)
                .observe(elapsed);
    }

    private AfterEvaluateDecisionEventImpl getAfterImpl(AfterEvaluateDecisionEvent e) {
        return (AfterEvaluateDecisionEventImpl) e;
    }

    @Override
    public void beforeEvaluateBKM(BeforeEvaluateBKMEvent event) {
    }

    @Override
    public void afterEvaluateBKM(AfterEvaluateBKMEvent event) {
    }

    @Override
    public void beforeEvaluateContextEntry(BeforeEvaluateContextEntryEvent event) {
    }

    @Override
    public void afterEvaluateContextEntry(AfterEvaluateContextEntryEvent event) {
    }

    @Override
    public void beforeEvaluateDecisionTable(BeforeEvaluateDecisionTableEvent event) {
    }

    @Override
    public void afterEvaluateDecisionTable(AfterEvaluateDecisionTableEvent event) {
    }

    @Override
    public void beforeEvaluateDecisionService(BeforeEvaluateDecisionServiceEvent event) {
    }

    @Override
    public void afterEvaluateDecisionService(AfterEvaluateDecisionServiceEvent event) {
    }

    private BeforeEvaluateDecisionEventImpl getBeforeImpl(BeforeEvaluateDecisionEvent e) {
        return (BeforeEvaluateDecisionEventImpl) e;
    }

    private String getDecisionName(Decision decision) {
        return decision.getName();
    }
}