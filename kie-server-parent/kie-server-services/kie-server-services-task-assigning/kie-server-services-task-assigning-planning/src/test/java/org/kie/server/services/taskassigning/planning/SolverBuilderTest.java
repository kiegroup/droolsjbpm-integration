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

import org.assertj.core.api.Assertions;
import org.drools.core.impl.InternalKieContainer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kie.api.KieServices;
import org.kie.server.api.model.KieContainerStatus;
import org.kie.server.services.api.KieServerRegistry;
import org.kie.server.services.impl.KieContainerInstanceImpl;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.optaplanner.core.api.solver.Solver;
import org.optaplanner.core.impl.solver.thread.DefaultSolverThreadFactory;

import static org.junit.Assert.assertNotNull;
import static org.kie.server.services.taskassigning.planning.SolverBuilder.CONFIGURED_THREAD_FACTORY_CLASS_MUST_IMPLEMENT_THREAD_FACTORY;
import static org.kie.server.services.taskassigning.planning.SolverBuilder.CONFIGURED_THREAD_FACTORY_CLASS_NOT_FOUND_ERROR;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SolverBuilderTest {

    private static final String SOLVER_SIMPLE_CONFIG_RESOURCE = "org/kie/server/services/taskassigning/planning/test/model/SolverBuilderTestSimpleConfig.xml";

    private static final String SOLVER_CONTAINER_CONFIG_RESOURCE = "org/kie/server/services/taskassigning/planning/test/model/SolverBuilderTestContainerConfig.xml";

    private static final String CONTAINER_ID = "CONTAINER_ID";

    private static final String NOT_EXISTING_SOLVER_CONFIG_RESOURCE = "NotExistingResource.xml";

    private static final String NOT_EXISTING_CLASS = "MyNonExistingClass";

    private static final String AUTO = "AUTO";

    @Mock
    private KieServerRegistry registry;

    @Mock
    private KieContainerInstanceImpl container;

    @Test
    public void buildFromResourceSuccessful() {
        SolverDef solverDef = new SolverDef(SOLVER_SIMPLE_CONFIG_RESOURCE);
        Solver solver = SolverBuilder.create()
                .solverDef(solverDef)
                .registry(registry)
                .build();
        assertNotNull(solver);
    }

    @Test
    public void buildFromResourceWithCustomThreadParamsSuccessful() {
        SolverDef solverDef = new SolverDef(SOLVER_SIMPLE_CONFIG_RESOURCE, AUTO, 10, DefaultSolverThreadFactory.class.getName());
        Solver solver = SolverBuilder.create()
                .solverDef(solverDef)
                .registry(registry)
                .build();
        assertNotNull(solver);
    }

    @Test
    public void buildFromResourceWithThreadFactoryNotFoundError() {
        SolverDef solverDef = new SolverDef(SOLVER_SIMPLE_CONFIG_RESOURCE, AUTO, 10, NOT_EXISTING_CLASS);
        Assertions.assertThatThrownBy(() ->
                                              SolverBuilder.create()
                                                      .solverDef(solverDef)
                                                      .registry(registry)
                                                      .build())
                .hasMessageContaining(String.format(CONFIGURED_THREAD_FACTORY_CLASS_NOT_FOUND_ERROR, NOT_EXISTING_CLASS));
    }

    @Test
    public void buildFromResourceWithThreadFactoryWrongError() {
        SolverDef solverDef = new SolverDef(SOLVER_SIMPLE_CONFIG_RESOURCE, AUTO, 10, String.class.getName());
        Assertions.assertThatThrownBy(() ->
                                              SolverBuilder.create()
                                                      .solverDef(solverDef)
                                                      .registry(registry)
                                                      .build())
                .hasMessageContaining(String.format(CONFIGURED_THREAD_FACTORY_CLASS_MUST_IMPLEMENT_THREAD_FACTORY, String.class.getName(), ThreadFactory.class.getName()));
    }

    @Test
    public void buildFromResourceWithError() {
        SolverDef solverDef = new SolverDef(NOT_EXISTING_SOLVER_CONFIG_RESOURCE);
        Assertions.assertThatThrownBy(() ->
                                              SolverBuilder.create()
                                                      .solverDef(solverDef)
                                                      .registry(registry)
                                                      .build())
                .hasMessageContaining("The solverConfigResource (" + NOT_EXISTING_SOLVER_CONFIG_RESOURCE + ") does not exist as a classpath resource in the classLoader");
    }

    @Test
    public void buildFromContainerSuccessful() {
        when(registry.getContainer(CONTAINER_ID)).thenReturn(container);
        InternalKieContainer kieContainer = (InternalKieContainer) KieServices.Factory.get().getKieClasspathContainer(getClass().getClassLoader());
        when(container.getKieContainer()).thenReturn(kieContainer);
        when(container.getStatus()).thenReturn(KieContainerStatus.STARTED);

        SolverDef solverDef = new SolverDef(CONTAINER_ID, null, null, null, SOLVER_CONTAINER_CONFIG_RESOURCE, null, -1, null);
        Solver solver = SolverBuilder.create()
                .solverDef(solverDef)
                .registry(registry)
                .build();
        assertNotNull(solver);
    }

    @Test
    public void buildFromContainerWithContainerNotFoundInRegistryError() {
        SolverDef solverDef = new SolverDef(CONTAINER_ID, null, null, null, SOLVER_CONTAINER_CONFIG_RESOURCE, null, -1, null);
        Assertions.assertThatThrownBy(() ->
                                              SolverBuilder.create()
                                                      .solverDef(solverDef)
                                                      .registry(registry)
                                                      .build()).hasMessageContaining("Container " + CONTAINER_ID + " was not found un current registry." +
                                                                                             " No solvers can be created for this container");
    }

    @Test
    public void buildFromContainerWithContainerNotStartedError() {
        when(registry.getContainer(CONTAINER_ID)).thenReturn(container);
        when(container.getStatus()).thenReturn(KieContainerStatus.STOPPED);
        SolverDef solverDef = new SolverDef(CONTAINER_ID, null, null, null, SOLVER_CONTAINER_CONFIG_RESOURCE, null, -1, null);
        Assertions.assertThatThrownBy(() ->
                                              SolverBuilder.create()
                                                      .solverDef(solverDef)
                                                      .registry(registry)
                                                      .build()).hasMessageContaining("Container " + CONTAINER_ID + " must be in " + KieContainerStatus.STARTED +
                                                                                             " status for creating solvers, but current status is: " + container.getStatus());
    }
}
