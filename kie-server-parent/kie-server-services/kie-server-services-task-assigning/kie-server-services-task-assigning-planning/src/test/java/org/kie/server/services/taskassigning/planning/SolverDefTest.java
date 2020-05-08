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

import org.junit.Before;
import org.junit.Test;
import org.kie.server.services.taskassigning.core.model.SolutionFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class SolverDefTest {

    private static final String CONTAINER_ID = "CONTAINER_ID";
    private static final String GROUP_ID = "GROUP_ID";
    private static final String ARTIFACT_ID = "ARTIFACT_ID";
    private static final String VERSION = "VERSION";

    private static final String SOLVER_CONFIG_RESOURCE = "SOLVER_CONFIG_RESOURCE";
    private static final String SOLUTION_FACTORY_NAME = "SOLUTION_FACTORY_NAME";
    private SolutionFactory solutionFactory;

    private SolverDef solverDef;

    @Before
    public void setUp() {
        solutionFactory = mock(SolutionFactory.class);
        solverDef = new SolverDef(CONTAINER_ID, GROUP_ID, ARTIFACT_ID, VERSION, SOLVER_CONFIG_RESOURCE, SOLUTION_FACTORY_NAME);
        solverDef.setSolutionFactory(solutionFactory);
    }

    @Test
    public void getContainerId() {
        assertThat(solverDef.getContainerId()).isEqualTo(CONTAINER_ID);
    }

    @Test
    public void getGroupId() {
        assertThat(solverDef.getGroupId()).isEqualTo(GROUP_ID);
    }

    @Test
    public void getArtifactId() {
        assertThat(solverDef.getArtifactId()).isEqualTo(ARTIFACT_ID);
    }

    @Test
    public void getVersion() {
        assertThat(solverDef.getVersion()).isEqualTo(VERSION);
    }

    @Test
    public void getSolverConfigResource() {
        assertThat(solverDef.getSolverConfigResource()).isEqualTo(SOLVER_CONFIG_RESOURCE);
    }

    @Test
    public void getSolverConfigResourceBySimpleConstructor() {
        assertThat(new SolverDef(SOLVER_CONFIG_RESOURCE).getSolverConfigResource()).isEqualTo(SOLVER_CONFIG_RESOURCE);
    }

    @Test
    public void getSolutionFactoryName() {
        assertThat(solverDef.getSolutionFactoryName()).isEqualTo(SOLUTION_FACTORY_NAME);
    }

    @Test
    public void getSolutionFactory() {
        assertThat(solverDef.getSolutionFactory()).isEqualTo(solutionFactory);
    }
}
