/*
 * Copyright 2022 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kie.server.services.prometheus;

import org.drools.core.event.rule.impl.AfterActivationFiredEventImpl;
import org.drools.core.event.rule.impl.BeforeActivationFiredEventImpl;
import org.kie.api.builder.ReleaseId;
import org.kie.api.event.rule.AfterMatchFiredEvent;
import org.kie.api.event.rule.AgendaEventListener;
import org.kie.api.event.rule.AgendaGroupPoppedEvent;
import org.kie.api.event.rule.AgendaGroupPushedEvent;
import org.kie.api.event.rule.BeforeMatchFiredEvent;
import org.kie.api.event.rule.MatchCancelledEvent;
import org.kie.api.event.rule.MatchCreatedEvent;
import org.kie.api.event.rule.RuleFlowGroupActivatedEvent;
import org.kie.api.event.rule.RuleFlowGroupDeactivatedEvent;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.manager.RuntimeManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PrometheusMetricsRuleTaskListener implements AgendaEventListener {

    private static final Logger logger = LoggerFactory.getLogger(PrometheusMetricsRuleTaskListener.class);
    private final PrometheusMetrics metrics;
    private final RuntimeManager runtimeManager;
    private final KieContainer kieContainer;

    private static final String KIE_SESSION_ID_DEFAULT = "default"; // default ksession is used in process use cases

    public PrometheusMetricsRuleTaskListener(RuntimeManager runtimeManager, KieContainer kieContainer) {
        this.metrics = PrometheusKieServerExtension.getMetrics();
        this.runtimeManager = runtimeManager;
        this.kieContainer = kieContainer;
    }

    @Override
    public void matchCreated(MatchCreatedEvent event) {
        // Do nothing
    }

    @Override
    public void matchCancelled(MatchCancelledEvent event) {
        // Do nothing
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
        ReleaseId releaseId = kieContainer.getReleaseId();
        String containerId = runtimeManager.getIdentifier();
        metrics.getDroolsEvaluationTimeHistogram()
                .labels(containerId, KIE_SESSION_ID_DEFAULT, releaseId.getGroupId(), releaseId.getArtifactId(), releaseId.getVersion(), ruleName)
                .observe(elapsed);
        if (logger.isDebugEnabled()) {
            logger.debug("Elapsed time: {}", elapsed);
        }
    }

    @Override
    public void agendaGroupPopped(AgendaGroupPoppedEvent event) {
        // Do nothing
    }

    @Override
    public void agendaGroupPushed(AgendaGroupPushedEvent event) {
        // Do nothing
    }

    @Override
    public void beforeRuleFlowGroupActivated(RuleFlowGroupActivatedEvent event) {
        // Do nothing
    }

    @Override
    public void afterRuleFlowGroupActivated(RuleFlowGroupActivatedEvent event) {
        // Do nothing
    }

    @Override
    public void beforeRuleFlowGroupDeactivated(RuleFlowGroupDeactivatedEvent event) {
        // Do nothing
    }

    @Override
    public void afterRuleFlowGroupDeactivated(RuleFlowGroupDeactivatedEvent event) {
        // Do nothing
    }

    public BeforeActivationFiredEventImpl getBeforeImpl(BeforeMatchFiredEvent e) {
        return (BeforeActivationFiredEventImpl)e;
    }

    public AfterActivationFiredEventImpl getAfterImpl(AfterMatchFiredEvent e) {
        return (AfterActivationFiredEventImpl)e;
    }
}
