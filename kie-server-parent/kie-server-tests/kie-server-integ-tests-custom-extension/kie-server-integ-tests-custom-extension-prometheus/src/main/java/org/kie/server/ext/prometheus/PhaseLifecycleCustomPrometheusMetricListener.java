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

import io.prometheus.client.Gauge;
import org.optaplanner.core.impl.phase.event.PhaseLifecycleListenerAdapter;
import org.optaplanner.core.impl.solver.scope.DefaultSolverScope;

public class PhaseLifecycleCustomPrometheusMetricListener extends PhaseLifecycleListenerAdapter {

    private final String solverId;

    private static final Gauge randomGauge = Gauge.build()
                                                  .name("random_gauge_phase_lifecycle_best_solution_time_millis")
                                                  .help("Random gauge as an example of custom KIE Prometheus metric")
                                                  .labelNames("solver_id")
                                                  .register();

    public PhaseLifecycleCustomPrometheusMetricListener(String solverId) {
        this.solverId = solverId;
    }

    @Override
    public void solvingEnded(DefaultSolverScope solverScope) {
        Long bestSolutionTimeMillis = solverScope.getBestSolutionTimeMillis();
        randomGauge.labels(solverId).set(bestSolutionTimeMillis);
    }
}