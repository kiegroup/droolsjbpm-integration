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

import org.kie.dmn.api.core.DMNDecisionResult;
import org.kie.dmn.api.core.ast.DecisionNode;
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
import org.kie.server.api.model.ReleaseId;
import org.kie.server.services.api.KieContainerInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PrometheusMetricsDMNListener implements DMNRuntimeEventListener {

    private static final Logger logger = LoggerFactory.getLogger(PrometheusMetricsDMNListener.class);

    private final PrometheusMetrics metrics;
    private final KieContainerInstance kieContainer;

    public PrometheusMetricsDMNListener(PrometheusMetrics metrics, KieContainerInstance kieContainer) {
        this.metrics = metrics;
        this.kieContainer = kieContainer;
    }

    @Override
    public void beforeEvaluateDecision(BeforeEvaluateDecisionEvent e) {
        long nanoTime = System.nanoTime();
        BeforeEvaluateDecisionEventImpl event = getBeforeImpl(e);
        event.setTimestamp(nanoTime);
    }

    @Override
    public void afterEvaluateDecision(AfterEvaluateDecisionEvent e) {
        AfterEvaluateDecisionEventImpl afterImpl = getAfterImpl(e);
        BeforeEvaluateDecisionEventImpl event = getBeforeImpl(afterImpl.getBeforeEvent());
        DecisionNode decisionNode = e.getDecision();
        long startTime = event.getTimestamp();
        long elapsed = System.nanoTime() - startTime;
        ReleaseId releaseId = kieContainer.getResource().getReleaseId();
        metrics.getEvaluationTimeHistogram()
                .labels(kieContainer.getContainerId(), releaseId.getGroupId(), releaseId.getArtifactId(), releaseId.getVersion(), decisionNode.getModelName(), decisionNode.getModelNamespace())
                .observe(elapsed);
        DMNDecisionResult decisionResultById = e.getResult().getDecisionResultById(decisionNode.getId());
        if(decisionResultById != null && decisionResultById.hasErrors()) {
            metrics.getDMNNumberOfEvaluationFailed()
                    .labels(kieContainer.getContainerId(), releaseId.getGroupId(), releaseId.getArtifactId(), releaseId.getVersion(), decisionNode.getModelName(), decisionNode.getModelNamespace())
                    .inc();
        }

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
}