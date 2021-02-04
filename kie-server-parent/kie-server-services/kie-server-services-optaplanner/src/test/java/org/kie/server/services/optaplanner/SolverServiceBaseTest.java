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

package org.kie.server.services.optaplanner;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;

import org.drools.core.impl.InternalKieContainer;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.api.model.instance.SolverInstance;
import org.kie.server.services.api.KieServerRegistry;
import org.kie.server.services.impl.KieContainerInstanceImpl;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.optaplanner.core.api.solver.Solver;
import org.optaplanner.core.api.solver.SolverFactory;
import org.optaplanner.core.config.domain.ScanAnnotatedClassesConfig;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SolverServiceBaseTest {

    private static final String CONTAINER_ID = "CONTAINER_ID";
    private static final String SOLVER_ID = "SOLVER_ID";
    private static final String SOLVER_CONFIG = "org/kie/server/services/optaplanner/SolverConfig.xml";
    private static final String SOLVER_CONFIG_WRONG = "org/kie/server/services/optaplanner/SolverConfigWrong.xml";
    private static final String SOLVER_CONFIG_GLOBAL_SCAN_SET = "org/kie/server/services/optaplanner/SolverConfigGlobalScanSet.xml";
    private static final String SOLVER_CONFIG_FILTERED_SCAN_SET = "org/kie/server/services/optaplanner/SolverConfigFilteredScanSet.xml";

    private static final String PRE_CONFIGURED_PACKAGE_INCLUDE = "pre.configured.package.include";
    private static final String PRE_CONFIGURED_PACKAGE_EXCLUDE = "pre.configured.package.exclude";

    private static final String CREATE_SOLVER_FOR_CONTAINER_ERROR = "Failed to create solver for container %s";
    private static final String CREATE_SOLVER_CONTAINER_NOT_EXISTS_ERROR = "Failed to create solver. Container does not exist: %s";
    private static final String CREATING_SOLVER_FACTORY_ERROR = "Error creating solver factory for solver: %s";
    private static final String SOLVER_ALREADY_EXISTS_ERROR = "Failed to create solver. Solver '%s' already exists for container '%s'.";
    private static final String SOLVER_CREATED_SUCCESSFULLY_MESSAGE = "Solver '%s' successfully created in container '%s'";

    @Mock
    private KieServerRegistry context;

    @Mock
    private ExecutorService executorService;

    @Mock
    private SolverInstance solverInstance;

    @Mock
    private KieContainerInstanceImpl containerInstance;

    @Mock
    private InternalKieContainer internalKieContainer;

    @Captor
    private ArgumentCaptor<SolverFactory<Object>> solverFactory;

    @Mock
    private Solver<Object> solver;

    private SolverServiceBase serviceBase;

    @Before
    public void setUp() {
        solverInstance = new SolverInstance();
        solverInstance.setContainerId(CONTAINER_ID);
        solverInstance.setSolverId(SOLVER_ID);
        solverInstance.setSolverConfigFile(SOLVER_CONFIG);

        doReturn(internalKieContainer).when(containerInstance).getKieContainer();
        doReturn(getClass().getClassLoader()).when(internalKieContainer).getClassLoader();
        doReturn(containerInstance).when(context).getContainer(CONTAINER_ID);

        serviceBase = spy(new SolverServiceBase(context, executorService));
    }

    @Test
    public void createSolverWithInstanceNullFailure() {
        ServiceResponse<SolverInstance> response = serviceBase.createSolver(CONTAINER_ID, SOLVER_ID, null);
        assertFailure(response, String.format(CREATE_SOLVER_FOR_CONTAINER_ERROR, CONTAINER_ID));
    }

    @Test
    public void createSolverWithConfigFileNullFailure() {
        solverInstance.setSolverConfigFile(null);
        ServiceResponse<SolverInstance> response = serviceBase.createSolver(CONTAINER_ID, SOLVER_ID, solverInstance);
        assertFailure(response, String.format(CREATE_SOLVER_FOR_CONTAINER_ERROR, CONTAINER_ID));
    }

    @Test
    public void createSolverWithContainerNotExistsFailure() {
        when(context.getContainer(CONTAINER_ID)).thenReturn(null);
        ServiceResponse<SolverInstance> response = serviceBase.createSolver(CONTAINER_ID, SOLVER_ID, solverInstance);
        assertFailure(response, String.format(CREATE_SOLVER_CONTAINER_NOT_EXISTS_ERROR, CONTAINER_ID));
    }

    @Test
    public void createSolverWithSolverConfigurationFileWrongFailure() {
        solverInstance.setSolverConfigFile(SOLVER_CONFIG_WRONG);
        ServiceResponse<SolverInstance> response = serviceBase.createSolver(CONTAINER_ID, SOLVER_ID, solverInstance);
        assertFailure(response, String.format(CREATING_SOLVER_FACTORY_ERROR, ""));
    }

    @Test
    public void createSolverWithErrorCreatingSolverFailure() {
        String internalError = "An error was produced";
        doThrow(new RuntimeException(internalError))
                .when(serviceBase)
                .newSolver(any(SolverFactory.class));

        ServiceResponse<SolverInstance> response = serviceBase.createSolver(CONTAINER_ID, SOLVER_ID, solverInstance);
        assertBuildFactoryWasInvoked(null);
        assertFailure(response, String.format(CREATING_SOLVER_FACTORY_ERROR, internalError));
    }

    @Test
    public void createSolverWithSolverAlreadyExistsFailure() {
        doReturn(solver).when(serviceBase).newSolver(any(SolverFactory.class));

        serviceBase.createSolver(CONTAINER_ID, SOLVER_ID, solverInstance);
        assertBuildFactoryWasInvoked(null);

        ServiceResponse<SolverInstance> response = serviceBase.createSolver(CONTAINER_ID, SOLVER_ID, solverInstance);
        assertFailure(response, String.format(SOLVER_ALREADY_EXISTS_ERROR, SOLVER_ID, CONTAINER_ID));
    }

    @Test
    public void createSolverWithNoScanSetSuccessful() {
        doReturn(solver).when(serviceBase).newSolver(any(SolverFactory.class));

        ServiceResponse<SolverInstance> response = serviceBase.createSolver(CONTAINER_ID, SOLVER_ID, solverInstance);
        assertBuildFactoryWasInvoked(null);
        assertSuccess(response, String.format(SOLVER_CREATED_SUCCESSFULLY_MESSAGE, SOLVER_ID, CONTAINER_ID));
    }

    @Test
    public void createSolverWithGlobalScanSetSuccessful() {
        solverInstance.setSolverConfigFile(SOLVER_CONFIG_GLOBAL_SCAN_SET);
        doReturn(solver).when(serviceBase).newSolver(any(SolverFactory.class));

        ServiceResponse<SolverInstance> response = serviceBase.createSolver(CONTAINER_ID, SOLVER_ID, solverInstance);
        assertBuildFactoryWasInvoked(Arrays.asList(TestConfigProvider1.PACKAGE1,
                                                   TestConfigProvider1.PACKAGE2,
                                                   TestConfigProvider1.PACKAGE3,
                                                   TestConfigProvider2.PACKAGE1));
        assertSuccess(response, String.format(SOLVER_CREATED_SUCCESSFULLY_MESSAGE, SOLVER_ID, CONTAINER_ID));
    }

    @Test
    public void createSolverWithFilteredScanSetSuccessful() {
        solverInstance.setSolverConfigFile(SOLVER_CONFIG_FILTERED_SCAN_SET);
        doReturn(solver).when(serviceBase).newSolver(any(SolverFactory.class));

        ServiceResponse<SolverInstance> response = serviceBase.createSolver(CONTAINER_ID, SOLVER_ID, solverInstance);
        assertBuildFactoryWasInvoked(Collections.singletonList(PRE_CONFIGURED_PACKAGE_INCLUDE),
                                     Arrays.asList(TestConfigProvider1.PACKAGE1,
                                                   TestConfigProvider1.PACKAGE2,
                                                   TestConfigProvider1.PACKAGE3,
                                                   TestConfigProvider2.PACKAGE1,
                                                   PRE_CONFIGURED_PACKAGE_EXCLUDE));
        assertSuccess(response, String.format(SOLVER_CREATED_SUCCESSFULLY_MESSAGE, SOLVER_ID, CONTAINER_ID));
    }

    private void assertFailure(ServiceResponse<SolverInstance> response, String messagePrefix) {
        assertThat(response.getType()).isEqualTo(ServiceResponse.ResponseType.FAILURE);
        assertThat(response.getMsg()).startsWith(messagePrefix);
    }

    private void assertSuccess(ServiceResponse<SolverInstance> response, String messagePrefix) {
        assertThat(response.getType()).isEqualTo(ServiceResponse.ResponseType.SUCCESS);
        assertThat(response.getMsg()).startsWith(messagePrefix);
    }

    private void assertBuildFactoryWasInvoked(List<String> expectedExcludedPackages) {
        assertBuildFactoryWasInvoked(null, expectedExcludedPackages);
    }

    private void assertBuildFactoryWasInvoked(List<String> expectedIncludedPackages, List<String> expectedExcludedPackages) {
        verify(serviceBase).newSolver(solverFactory.capture());
        ScanAnnotatedClassesConfig config = solverFactory.getValue().getSolverConfig().getScanAnnotatedClassesConfig();
        if (expectedExcludedPackages != null) {
            assertThat(config).isNotNull();
            assertThat(config.getPackageExcludeList())
                    .containsExactlyInAnyOrder(expectedExcludedPackages.toArray(new String[0]));
        } else {
            assertThat(config).isNull();
        }
        if (expectedIncludedPackages != null) {
            assertThat(config).isNotNull();
            assertThat(config.getPackageIncludeList())
                    .containsExactlyInAnyOrder(expectedIncludedPackages.toArray(new String[0]));
        }
    }
}
