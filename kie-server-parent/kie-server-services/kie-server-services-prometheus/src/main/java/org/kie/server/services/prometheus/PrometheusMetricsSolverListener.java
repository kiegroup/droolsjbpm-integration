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

import io.prometheus.client.Summary;
import org.optaplanner.core.impl.phase.event.PhaseLifecycleListenerAdapter;
import org.optaplanner.core.impl.solver.scope.DefaultSolverScope;

public class PrometheusMetricsSolverListener extends PhaseLifecycleListenerAdapter<Object> {

    private final PrometheusMetrics metrics;
    private final String solverId;
    private Summary.Timer timer;

    public PrometheusMetricsSolverListener(String solverId) {
        this(solverId, PrometheusKieServerExtension.getMetrics());
    }

    public PrometheusMetricsSolverListener(String solverId, PrometheusMetrics metrics) {
        this.solverId = solverId;
        this.metrics = metrics;
    }

    @Override
    public void solvingStarted(DefaultSolverScope solverScope) {
        metrics.getOptaPlannerSolverCount().inc();
        timer = metrics.getOptaPlannerSolverDuration().labels(solverId).startTimer();
    }

    @Override
    public void solvingEnded(DefaultSolverScope solverScope) {
        metrics.getOptaPlannerSolverCount().dec();
        metrics.getOptaPlannerSolverScoreCalculationSpeed().labels(solverId).observe(getScoreCalculationSpeed(solverScope));
        timer.observeDuration();
    }

    private double getScoreCalculationSpeed(DefaultSolverScope solverScope) {
        long timeMillisSpent = solverScope.calculateTimeMillisSpentUpToNow();
        // Avoid divide by zero exception on a fast CPU
        return solverScope.getScoreCalculationCount() * 1000L / (timeMillisSpent == 0L ? 1L : timeMillisSpent);
    }
}
