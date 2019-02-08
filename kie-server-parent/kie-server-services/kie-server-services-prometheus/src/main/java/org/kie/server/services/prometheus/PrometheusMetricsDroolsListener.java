/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
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
import org.kie.api.event.rule.AfterMatchFiredEvent;
import org.kie.api.event.rule.AgendaEventListener;
import org.kie.api.event.rule.AgendaGroupPoppedEvent;
import org.kie.api.event.rule.AgendaGroupPushedEvent;
import org.kie.api.event.rule.BeforeMatchFiredEvent;
import org.kie.api.event.rule.MatchCancelledEvent;
import org.kie.api.event.rule.MatchCreatedEvent;
import org.kie.api.event.rule.RuleFlowGroupActivatedEvent;
import org.kie.api.event.rule.RuleFlowGroupDeactivatedEvent;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.services.api.KieContainerInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PrometheusMetricsDroolsListener implements AgendaEventListener {

    private static final Logger logger = LoggerFactory.getLogger(PrometheusMetricsDroolsListener.class);
    private final PrometheusMetrics metrics;
    private final String kieSessionId;
    private final KieContainerInstance containerInstance;

    public PrometheusMetricsDroolsListener(PrometheusMetrics metrics, String kieSessionId, KieContainerInstance containerInstance) {
        this.metrics = metrics;
        this.kieSessionId = kieSessionId;
        this.containerInstance = containerInstance;
    }

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
        ReleaseId releaseId = containerInstance.getResource().getReleaseId();
        metrics.getDroolsEvaluationTimeHistogram()
                .labels(containerInstance.getContainerId(), kieSessionId, releaseId.getGroupId(), releaseId.getArtifactId(), releaseId.getVersion(), ruleName)
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
