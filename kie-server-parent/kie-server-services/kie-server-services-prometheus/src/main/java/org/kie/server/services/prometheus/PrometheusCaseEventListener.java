/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.server.services.prometheus;

import java.util.Date;

import io.prometheus.client.Counter;
import io.prometheus.client.Gauge;
import io.prometheus.client.Summary;
import org.jbpm.casemgmt.api.event.CaseCancelEvent;
import org.jbpm.casemgmt.api.event.CaseCloseEvent;
import org.jbpm.casemgmt.api.event.CaseDestroyEvent;
import org.jbpm.casemgmt.api.event.CaseEvent;
import org.jbpm.casemgmt.api.event.CaseEventListener;
import org.jbpm.casemgmt.api.event.CaseStartEvent;
import org.jbpm.casemgmt.api.model.instance.CaseFileInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.kie.server.services.prometheus.PrometheusMetrics.millisToSeconds;

public class PrometheusCaseEventListener implements CaseEventListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(PrometheusCaseEventListener.class);

    protected static final Counter numberOfCasesStarted = Counter.build()
            .name("kie_server_case_started_total")
            .help("Kie Server Started Cases")
            .labelNames("case_definition_id")
            .register();

    protected static final Gauge runningCases = Gauge.build()
            .name("kie_server_case_running_total")
            .help("Kie Server Running Cases")
            .labelNames("case_definition_id")
            .register();

    protected static final Summary caseDuration = Summary.build()
            .name("kie_server_case_duration_seconds")
            .help("Kie Server Case Duration")
            .labelNames("case_definition_id")
            .register();

    protected static void recordRunningCaseInstance(String caseDefinitionId) {
        runningCases.labels(caseDefinitionId).inc();
    }

    @Override
    public void afterCaseStarted(CaseStartEvent event) {
        LOGGER.debug("After case started: {}", event);
        numberOfCasesStarted.labels(event.getCaseDefinitionId()).inc();
        recordRunningCaseInstance(event.getCaseDefinitionId());
    }

    @Override
    public void afterCaseClosed(CaseCloseEvent event) {
        endCase(event);
    }

    @Override
    public void afterCaseDestroyed(CaseDestroyEvent event) {
        endCase(event);
    }

    @Override
    public void afterCaseCancelled(CaseCancelEvent event) {
        endCase(event);
    }

    protected void endCase(CaseEvent event) {
        LOGGER.debug("Close Case with Id: {}", event.getCaseId());
        final CaseFileInstance caseFile = event.getCaseFile();
        if (caseFile != null) {
            runningCases.labels(caseFile.getDefinitionId()).dec();
            final Date caseReopenDate = caseFile.getCaseReopenDate();
            final Date start = caseReopenDate == null ? caseFile.getCaseStartDate() : caseReopenDate;
            final double duration = millisToSeconds(System.currentTimeMillis() - start.getTime());
            caseDuration.labels(caseFile.getDefinitionId()).observe(duration);
            LOGGER.debug("Case duration: {}s", duration);
        }
    }
}
