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

import java.util.concurrent.ThreadFactory;

import org.kie.server.api.model.KieContainerStatus;
import org.kie.server.services.api.KieServerRegistry;
import org.kie.server.services.impl.KieContainerInstanceImpl;
import org.kie.server.services.taskassigning.core.model.TaskAssigningSolution;
import org.optaplanner.core.api.solver.Solver;
import org.optaplanner.core.api.solver.SolverFactory;
import org.optaplanner.core.config.solver.SolverConfig;

import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

public class SolverBuilder {

    static final String CONFIGURED_THREAD_FACTORY_CLASS_NOT_FOUND_ERROR = "An error was produced during threadFactoryClass initialization, class: %s was not found.";
    static final String CONFIGURED_THREAD_FACTORY_CLASS_MUST_IMPLEMENT_THREAD_FACTORY = "An error was produced during threadFactoryClass initialization, class: %s must implement: %s.";

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

    @SuppressWarnings("unchecked")
    private Solver<TaskAssigningSolution> buildFromResource() {
        final SolverConfig solverConfig = SolverConfig.createFromXmlResource(solverDef.getSolverConfigResource());
        if (isNotEmpty(solverDef.getMoveThreadCount())) {
            solverConfig.setMoveThreadCount(solverDef.getMoveThreadCount());
            if (solverDef.getMoveThreadBufferSize() >= 1) {
                solverConfig.setMoveThreadBufferSize(solverConfig.getMoveThreadBufferSize());
            }
        }
        if (isNotEmpty(solverDef.getThreadFactoryClass())) {
            Class<?> threadFactoryClass;
            try {
                threadFactoryClass = Class.forName(solverDef.getThreadFactoryClass());
            } catch (ClassNotFoundException e) {
                throw new SolverBuilderException(String.format(CONFIGURED_THREAD_FACTORY_CLASS_NOT_FOUND_ERROR, solverDef.getThreadFactoryClass()));
            }
            if (!ThreadFactory.class.isAssignableFrom(threadFactoryClass)) {
                throw new SolverBuilderException(String.format(CONFIGURED_THREAD_FACTORY_CLASS_MUST_IMPLEMENT_THREAD_FACTORY, solverDef.getThreadFactoryClass(), ThreadFactory.class.getName()));
            }
            solverConfig.setThreadFactoryClass((Class<? extends ThreadFactory>) threadFactoryClass);
        }
        final SolverFactory<TaskAssigningSolution> solverFactory = SolverFactory.create(solverConfig);
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
