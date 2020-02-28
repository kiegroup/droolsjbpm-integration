/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.server.services.taskassigning.planning;

import org.kie.server.services.taskassigning.core.model.TaskAssigningSolution;
import org.kie.server.api.model.KieContainerStatus;
import org.kie.server.services.api.KieServerRegistry;
import org.kie.server.services.impl.KieContainerInstanceImpl;
import org.optaplanner.core.api.solver.Solver;
import org.optaplanner.core.api.solver.SolverFactory;

import static org.apache.commons.lang3.StringUtils.isEmpty;

public class SolverBuilder {

    private SolverDef solverDef;

    private KieServerRegistry registry;

    public static class SolverBuilderException extends RuntimeException {

        public SolverBuilderException(String message) {
            super(message);
        }
    }

    private SolverBuilder() {
    }

    public static SolverBuilder create() {
        return new SolverBuilder();
    }

    public SolverBuilder solverDef(SolverDef solverDef) {
        this.solverDef = solverDef;
        return this;
    }

    public SolverBuilder registry(KieServerRegistry registry) {
        this.registry = registry;
        return this;
    }

    public Solver<TaskAssigningSolution> build() {
        if (isEmpty(solverDef.getContainerId())) {
            return buildFromResource();
        } else {
            return buildFromContainer();
        }
    }

    private Solver<TaskAssigningSolution> buildFromResource() {
        SolverFactory<TaskAssigningSolution> solverFactory = SolverFactory.createFromXmlResource(solverDef.getSolverConfigResource());
        return solverFactory.buildSolver();
    }

    private Solver<TaskAssigningSolution> buildFromContainer() {
        final KieContainerInstanceImpl containerInstance = registry.getContainer(solverDef.getContainerId());
        if (containerInstance == null) {
            throw new SolverBuilderException("Container " + solverDef.getContainerId() + " was not found un current registry." +
                                                     " No solvers can be created for this container");
        }
        if (containerInstance.getStatus() != KieContainerStatus.STARTED) {
            throw new SolverBuilderException("Container " + solverDef.getContainerId() + " must be in " + KieContainerStatus.STARTED +
                                                     " status for creating solvers, but current status is: " + containerInstance.getStatus());
        }
        final SolverFactory<TaskAssigningSolution> solverFactory = SolverFactory.createFromKieContainerXmlResource(containerInstance.getKieContainer(),
                                                                                                                   solverDef.getSolverConfigResource());
        return solverFactory.buildSolver();
    }
}
