package org.kie.server.services.prometheus;

import org.drools.core.event.rule.impl.AfterActivationFiredEventImpl;
import org.drools.core.event.rule.impl.BeforeActivationFiredEventImpl;
import org.kie.api.event.rule.AfterMatchFiredEvent;
import org.kie.api.event.rule.AgendaEventListener;
import org.kie.api.event.rule.AgendaGroupPoppedEvent;
import org.kie.api.event.rule.AgendaGroupPushedEvent;
import org.kie.api.event.rule.BeforeMatchFiredEvent;
import org.kie.api.event.rule.MatchCancelledEvent;
import org.kie.api.event.rule.MatchCreatedEvent;
import org.kie.api.event.rule.RuleFlowGroupActivatedEvent;
import org.kie.api.event.rule.RuleFlowGroupDeactivatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.prometheus.client.Histogram;

public class PrometheusMetricsDroolsListener implements AgendaEventListener {

    private static final Logger logger = LoggerFactory.getLogger(PrometheusMetricsDroolsListener.class);


    private static final double[] RULE_TIME_BUCKETS = new double[]{1_000_000, 2_000_000, 3_000_000, 100_000_000, 200_000_000, 300_000_000, 400_000_000, 500_000_000};

    private static final Histogram evaluationTimeHistogram = Histogram.build()
            .name("drl_match_fired_nanosecond")
            .help("Drools Firing Time")
            .labelNames("Rule Name")
            .buckets(RULE_TIME_BUCKETS)
            .register();

    @Override
    public void matchCreated(MatchCreatedEvent event) {

    }

    @Override
    public void matchCancelled(MatchCancelledEvent event) {

    }

    @Override
    public void beforeMatchFired(BeforeMatchFiredEvent event) {
        long nanoTime = System.nanoTime();
        BeforeActivationFiredEventImpl impl = getBeforeImpl(event);
        impl.setTimestamp(nanoTime);
    }

    @Override
    public void afterMatchFired(AfterMatchFiredEvent event) {
        AfterActivationFiredEventImpl afterImpl = getAfterImpl(event);
        BeforeActivationFiredEventImpl beforeImpl = getBeforeImpl(afterImpl.getBeforeMatchFiredEvent());
        long startTime = beforeImpl.getTimestamp();
        long elapsed = System.nanoTime() - startTime;
        String ruleName = event.getMatch().getRule().getName();
        evaluationTimeHistogram.labels(ruleName)
                .observe(elapsed);
        if (logger.isDebugEnabled()) {
            logger.debug("Elapsed time: " + elapsed);
        }
    }

    @Override
    public void agendaGroupPopped(AgendaGroupPoppedEvent event) {

    }

    @Override
    public void agendaGroupPushed(AgendaGroupPushedEvent event) {

    }

    @Override
    public void beforeRuleFlowGroupActivated(RuleFlowGroupActivatedEvent event) {

    }

    @Override
    public void afterRuleFlowGroupActivated(RuleFlowGroupActivatedEvent event) {

    }

    @Override
    public void beforeRuleFlowGroupDeactivated(RuleFlowGroupDeactivatedEvent event) {

    }

    @Override
    public void afterRuleFlowGroupDeactivated(RuleFlowGroupDeactivatedEvent event) {

    }

    public BeforeActivationFiredEventImpl getBeforeImpl(BeforeMatchFiredEvent e) {
        return (BeforeActivationFiredEventImpl)e;
    }

    public AfterActivationFiredEventImpl getAfterImpl(AfterMatchFiredEvent e) {
        return (AfterActivationFiredEventImpl)e;
    }
}
