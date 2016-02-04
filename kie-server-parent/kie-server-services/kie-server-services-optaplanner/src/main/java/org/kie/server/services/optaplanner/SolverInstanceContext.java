/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
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
package org.kie.server.services.optaplanner;

import org.kie.server.api.model.instance.SolverInstance;
import org.optaplanner.core.api.solver.Solver;

/**
 * Aggregates solver instance context information
 */
public class SolverInstanceContext {

    private SolverInstance instance;
    private Solver solver;

    public SolverInstanceContext() {
    }

    public SolverInstanceContext(SolverInstance instance) {
        this.instance = instance;
    }

    public SolverInstance getInstance() {
        return instance;
    }

    public void setInstance(SolverInstance instance) {
        this.instance = instance;
    }

    public Solver getSolver() {
        return solver;
    }

    public void setSolver(Solver solver) {
        this.solver = solver;
    }

}
