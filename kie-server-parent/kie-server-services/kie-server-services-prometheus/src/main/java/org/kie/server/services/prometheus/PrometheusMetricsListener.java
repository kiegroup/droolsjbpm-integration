package org.kie.server.services.prometheus;

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

public class PrometheusMetricsListener implements DMNRuntimeEventListener {

    private static final Logger logger = LoggerFactory.getLogger(PrometheusMetricsListener.class);

    private static final double[] DECISION_TIME_BUCKETS = new double[]{1_000_000, 2_000_000, 3_000_000, 100_000_000, 200_000_000, 300_000_000, 400_000_000, 500_000_000};

    public static final long NANOSECONDS_PER_SECOND = 1_000_000_000;
    public static final long HALF_SECOND_NANO = 500_000_000;

    public static long toNano(long second) {
        return second * NANOSECONDS_PER_SECOND;
    }

    private static final Histogram histogram = Histogram.build().name("dmn_evaluate_decision_nanosecond")
            .help("DMN Evaluation Time")
            .labelNames("decision_name")
            .buckets(DECISION_TIME_BUCKETS)
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
        if (logger.isDebugEnabled()) {
            logger.debug("Elapsed time: " + elapsed);
        }
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