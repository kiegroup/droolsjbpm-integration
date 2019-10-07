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
package org.kie.server.ext.prometheus;

import java.util.Random;

import io.prometheus.client.Gauge;
import org.drools.core.event.DefaultAgendaEventListener;
import org.kie.api.event.rule.RuleFlowGroupActivatedEvent;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.services.api.KieContainerInstance;

public class AgendaEventCustomPrometheusMetricListener extends DefaultAgendaEventListener {

    private final KieContainerInstance kieContainer;

    private static final Gauge randomGauge = Gauge.build()
                                                  .name("random_gauge_ruleflow_group_nanosecond")
                                                  .help("Random gauge as an example of custom KIE Prometheus metric")
                                                  .labelNames("container_id", "group_id", "artifact_id", "version", "ruleflow_group_name")
                                                  .register();

    public AgendaEventCustomPrometheusMetricListener(KieContainerInstance containerInstance) {
        kieContainer = containerInstance;
    }

    @Override
    public void afterRuleFlowGroupActivated(RuleFlowGroupActivatedEvent event) {
        String ruleFlowGroupName = event.getRuleFlowGroup().getName();
        ReleaseId releaseId = kieContainer.getResource().getReleaseId();
        randomGauge.labels(kieContainer.getContainerId(), releaseId.getGroupId(),
                           releaseId.getArtifactId(), releaseId.getVersion(),
                           ruleFlowGroupName)
                   .set((int) (new Random().nextInt(100)));
    }
}