/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.camel.container.integration.tests.remote;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.assertj.core.api.Assertions;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.kie.camel.container.api.ExecutionServerCommand;
import org.kie.camel.container.api.model.cloudbalance.CloudBalance;
import org.kie.server.api.model.instance.SolverInstance;

public class SolverClientIntegrationTest extends AbstractRemoteIntegrationTest {

    private static final String CLOUD_BALANCE_SOLVER_ID = "cloudsolver";

    @Test
    public void testGetSolver() {
        final Map<String, Object> parameters = new HashMap<>();
        parameters.put("containerId", CONTAINER_ID);
        parameters.put("solverId", CLOUD_BALANCE_SOLVER_ID);
        final ExecutionServerCommand executionServerCommand = new ExecutionServerCommand();
        executionServerCommand.setClient("solver");
        executionServerCommand.setOperation("getSolver");
        executionServerCommand.setParameters(parameters);
        final Object response = runOnExecutionServer(executionServerCommand);
        Assertions.assertThat(response).isNotNull();
        Assertions.assertThat(response).isInstanceOf(SolverInstance.class);

        final SolverInstance solverInstance = (SolverInstance) response;
        Assertions.assertThat(solverInstance.getContainerId()).isEqualTo(CONTAINER_ID);
        Assertions.assertThat(solverInstance.getSolverId()).isEqualTo(CLOUD_BALANCE_SOLVER_ID);
    }

    @Test
    public void testSolveCloudBalanceProblem() throws InterruptedException {
        final CloudBalancingGenerator cloudBalancingGenerator = new CloudBalancingGenerator();
        final CloudBalance cloudBalanceProblem = cloudBalancingGenerator
                .createCloudBalance(5, 15);

        final Map<String, Object> parameters = new HashMap<>();
        parameters.put("containerId", CONTAINER_ID);
        parameters.put("solverId", CLOUD_BALANCE_SOLVER_ID);
        parameters.put("planningProblem", cloudBalanceProblem);
        final ExecutionServerCommand executionServerCommand = new ExecutionServerCommand();
        executionServerCommand.setClient("solver");
        executionServerCommand.setOperation("solvePlanningProblem");
        executionServerCommand.setParameters(parameters);
        runOnExecutionServer(executionServerCommand);

        SolverInstance.SolverStatus solverStatus = getSolverStatus();
        Assertions.assertThat(solverStatus).isEqualTo(SolverInstance.SolverStatus.SOLVING);

        Thread.sleep(5000);

        solverStatus = getSolverStatus();
        Assertions.assertThat(solverStatus).isEqualTo(SolverInstance.SolverStatus.NOT_SOLVING);
    }

    @Before
    public void createTestSolver() {
        final Map<String, Object> parameters = new HashMap<>();
        parameters.put("containerId", CONTAINER_ID);
        parameters.put("solverId", CLOUD_BALANCE_SOLVER_ID);
        parameters.put("configFile", "org/test/" + CLOUD_BALANCE_SOLVER_CONFIG);
        final ExecutionServerCommand executionServerCommand = new ExecutionServerCommand();
        executionServerCommand.setClient("solver");
        executionServerCommand.setOperation("createSolver");
        executionServerCommand.setParameters(parameters);
        final Object response = runOnExecutionServer(executionServerCommand);
        Assertions.assertThat(response).isNotNull();
        Assertions.assertThat(response).isInstanceOf(SolverInstance.class);
    }

    @After
    public void cleanUpSolvers() {
        final Map<String, Object> parameters = new HashMap<>();
        parameters.put("containerId", CONTAINER_ID);
        final ExecutionServerCommand executionServerCommand = new ExecutionServerCommand();
        executionServerCommand.setClient("solver");
        executionServerCommand.setOperation("getSolvers");
        executionServerCommand.setParameters(parameters);
        final Object response = runOnExecutionServer(executionServerCommand);

        final List<SolverInstance> solverInstances = (List<SolverInstance>) response;
        for (SolverInstance solverInstance : solverInstances) {
            disposeSolver(solverInstance.getSolverId());
        }
    }

    private SolverInstance.SolverStatus getSolverStatus() {
        final Map<String, Object> parameters = new HashMap<>();
        parameters.put("containerId", CONTAINER_ID);
        parameters.put("solverId", CLOUD_BALANCE_SOLVER_ID);
        final ExecutionServerCommand executionServerCommand = new ExecutionServerCommand();
        executionServerCommand.setClient("solver");
        executionServerCommand.setOperation("getSolver");
        executionServerCommand.setParameters(parameters);
        final Object response = runOnExecutionServer(executionServerCommand);
        Assertions.assertThat(response).isNotNull();
        Assertions.assertThat(response).isInstanceOf(SolverInstance.class);

        final SolverInstance solverInstance = (SolverInstance) response;
        return solverInstance.getStatus();
    }

    private void disposeSolver(final String solverId) {
        final Map<String, Object> parameters = new HashMap<>();
        parameters.put("containerId", CONTAINER_ID);
        parameters.put("solverId", solverId);
        final ExecutionServerCommand executionServerCommand = new ExecutionServerCommand();
        executionServerCommand.setClient("solver");
        executionServerCommand.setOperation("disposeSolver");
        executionServerCommand.setParameters(parameters);
        runOnExecutionServer(executionServerCommand);
    }
}
