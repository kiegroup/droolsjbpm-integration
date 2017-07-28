/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
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

package org.kie.server.integrationtests.optaplanner;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;
import org.kie.api.KieServices;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.instance.ScoreWrapper;
import org.kie.server.api.model.instance.SolverInstance;
import org.kie.server.integrationtests.shared.KieServerAssert;
import org.kie.server.integrationtests.shared.KieServerDeployer;
import org.kie.server.integrationtests.shared.KieServerReflections;
import org.kie.server.api.exception.KieServicesException;
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;

import static org.junit.Assert.*;

public class OptaplannerIntegrationTest
        extends OptaplannerKieServerBaseIntegrationTest {

    private static final ReleaseId kjar1 = new ReleaseId(
            "org.kie.server.testing",
            "cloudbalance",
            "1.0.0.Final");

    private static final String NOT_EXISTING_CONTAINER_ID = "no_container";
    private static final String CONTAINER_1_ID = "cloudbalance";
    private static final String SOLVER_1_ID = "cloudsolver";
    private static final String SOLVER_1_CONFIG = "META-INF/cloudbalance-solver.xml";

    private static final String CLASS_CLOUD_BALANCE = "org.kie.server.testing.CloudBalance";
    private static final String CLASS_CLOUD_COMPUTER = "org.kie.server.testing.CloudComputer";
    private static final String CLASS_CLOUD_PROCESS = "org.kie.server.testing.CloudProcess";
    private static final String CLASS_CLOUD_GENERATOR = "org.kie.server.testing.CloudBalancingGenerator";

    @BeforeClass
    public static void deployArtifacts() {
        KieServerDeployer.buildAndDeployCommonMavenParent();
        KieServerDeployer.buildAndDeployMavenProject(ClassLoader.class.getResource("/kjars-sources/cloudbalance").getFile());

        kieContainer = KieServices.Factory.get().newKieContainer(kjar1);

        createContainer(CONTAINER_1_ID,
                        kjar1);
    }

    @Override
    protected void addExtraCustomClasses(Map<String, Class<?>> extraClasses)
            throws Exception {

        extraClasses.put(CLASS_CLOUD_BALANCE,
                         Class.forName(CLASS_CLOUD_BALANCE,
                                       true,
                                       kieContainer.getClassLoader()));
        extraClasses.put(CLASS_CLOUD_COMPUTER,
                         Class.forName(CLASS_CLOUD_COMPUTER,
                                       true,
                                       kieContainer.getClassLoader()));
        extraClasses.put(CLASS_CLOUD_PROCESS,
                         Class.forName(CLASS_CLOUD_PROCESS,
                                       true,
                                       kieContainer.getClassLoader()));
    }

    @Test
    public void testCreateDisposeSolver()
            throws Exception {
        solverClient.createSolver(CONTAINER_1_ID,
                                  SOLVER_1_ID,
                                  SOLVER_1_CONFIG);
        solverClient.disposeSolver(CONTAINER_1_ID,
                                   SOLVER_1_ID);
    }

    @Test
    public void testCreateSolverFromNotExistingContainer() {
        try {
            solverClient.createSolver(NOT_EXISTING_CONTAINER_ID,
                                      SOLVER_1_ID,
                                      SOLVER_1_CONFIG);
            fail("A KieServicesException should have been thrown by now.");
        } catch (KieServicesException e) {
            KieServerAssert.assertResultContainsStringRegex(e.getMessage(),
                                                            ".*Failed to create solver. Container does not exist: .*");
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateSolverWithoutSolverInstance() throws Exception {
        solverClient.createSolver(CONTAINER_1_ID,
                                  SOLVER_1_ID,
                                  null);
    }

    @Test
    public void testCreateSolverWrongSolverInstanceConfigPath() throws Exception {
        try {
            solverClient.createSolver(CONTAINER_1_ID,
                                      SOLVER_1_ID,
                                      "NonExistingPath");
            fail("A KieServicesException should have been thrown by now.");
        } catch (KieServicesException e) {
            KieServerAssert.assertResultContainsStringRegex(e.getMessage(),
                                                            ".*The solverConfigResource \\(.*\\) does not exist as a classpath resource in the classLoader \\(.*\\)*");
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateSolverNullContainer() throws Exception {
        solverClient.createSolver(null,
                                  SOLVER_1_ID,
                                  null);
    }

    @Test
    public void testCreateDuplicitSolver() throws Exception {
        SolverInstance solverInstance = solverClient.createSolver(CONTAINER_1_ID,
                                                                  SOLVER_1_ID,
                                                                  SOLVER_1_CONFIG);
        assertNotNull(solverInstance);

        try {
            solverClient.createSolver(CONTAINER_1_ID,
                                      SOLVER_1_ID,
                                      SOLVER_1_CONFIG);
            fail("A KieServicesException should have been thrown by now.");
        } catch (KieServicesException e) {
            KieServerAssert.assertResultContainsStringRegex(e.getMessage(),
                                                            ".*Failed to create solver. Solver .* already exists for container .*");
        }
    }

    @Test
    public void testDisposeNotExistingSolver() throws Exception {
        try {
            solverClient.disposeSolver(CONTAINER_1_ID,
                                       SOLVER_1_ID);
            fail("A KieServicesException should have been thrown by now.");
        } catch (KieServicesException e) {
            KieServerAssert.assertResultContainsStringRegex(e.getMessage(),
                                                            ".*Solver.*from container.*not found.*");
        }
    }

    @Test
    public void testGetSolverState() throws Exception {
        SolverInstance solverInstance = solverClient.createSolver(CONTAINER_1_ID,
                                                                  SOLVER_1_ID,
                                                                  SOLVER_1_CONFIG);
        assertNotNull(solverInstance);

        solverInstance = solverClient.getSolver(CONTAINER_1_ID,
                                                SOLVER_1_ID);
        assertNotNull(solverInstance);
        assertEquals(CONTAINER_1_ID,
                     solverInstance.getContainerId());
        assertEquals(SOLVER_1_CONFIG,
                     solverInstance.getSolverConfigFile());
        assertEquals(SOLVER_1_ID,
                     solverInstance.getSolverId());
        assertEquals(SolverInstance.getSolverInstanceKey(CONTAINER_1_ID,
                                                         SOLVER_1_ID),
                     solverInstance.getSolverInstanceKey());
        assertEquals(SolverInstance.SolverStatus.NOT_SOLVING,
                     solverInstance.getStatus());
        assertNotNull(solverInstance.getScoreWrapper());
        assertNull(solverInstance.getScoreWrapper().toScore());
    }

    @Test
    public void testGetNotExistingSolverState() throws Exception {
        try {
            solverClient.getSolver(CONTAINER_1_ID,
                                   SOLVER_1_ID);
            fail("A KieServicesException should have been thrown by now.");
        } catch (KieServicesException e) {
            KieServerAssert.assertResultContainsStringRegex(e.getMessage(),
                                                            ".*Solver.*not found in container.*");
        }
    }

    @Test
    public void testGetSolvers() throws Exception {
        List<SolverInstance> solverInstanceList = solverClient.getSolvers(CONTAINER_1_ID);
        assertNotNull(solverInstanceList);
        assertEquals(0,
                     solverInstanceList.size());

        solverClient.createSolver(CONTAINER_1_ID,
                                  SOLVER_1_ID,
                                  SOLVER_1_CONFIG);

        solverInstanceList = solverClient.getSolvers(CONTAINER_1_ID);
        assertNotNull(solverInstanceList);
        assertEquals(1,
                     solverInstanceList.size());

        SolverInstance returnedInstance = solverInstanceList.get(0);
        assertEquals(CONTAINER_1_ID,
                     returnedInstance.getContainerId());
        assertEquals(SOLVER_1_CONFIG,
                     returnedInstance.getSolverConfigFile());
        assertEquals(SOLVER_1_ID,
                     returnedInstance.getSolverId());
        assertEquals(SolverInstance.getSolverInstanceKey(CONTAINER_1_ID,
                                                         SOLVER_1_ID),
                     returnedInstance.getSolverInstanceKey());
        assertEquals(SolverInstance.SolverStatus.NOT_SOLVING,
                     returnedInstance.getStatus());
        assertNotNull(returnedInstance.getScoreWrapper());
        assertNull(returnedInstance.getScoreWrapper().toScore());
    }

    @Test
    public void testExecuteSolver() throws Exception {
        SolverInstance solverInstance = solverClient.createSolver(CONTAINER_1_ID,
                                                                  SOLVER_1_ID,
                                                                  SOLVER_1_CONFIG);
        assertNotNull(solverInstance);
        assertEquals(SolverInstance.SolverStatus.NOT_SOLVING,
                     solverInstance.getStatus());

        // the following status starts the solver
        Object planningProblem = loadPlanningProblem(5,
                                                     15);
        solverClient.solvePlanningProblem(CONTAINER_1_ID,
                                          SOLVER_1_ID,
                                          planningProblem);

        solverInstance = solverClient.getSolver(CONTAINER_1_ID,
                                                SOLVER_1_ID);

        // solver should finish in 5 seconds, but we wait up to 15s before timing out
        for (int i = 0; i < 5 && solverInstance.getStatus() == SolverInstance.SolverStatus.SOLVING; i++) {
            Thread.sleep(3000);
            solverInstance = solverClient.getSolver(CONTAINER_1_ID,
                                                    SOLVER_1_ID);
            assertNotNull(solverInstance);
        }

        assertEquals(SolverInstance.SolverStatus.NOT_SOLVING,
                     solverInstance.getStatus());

        solverClient.disposeSolver(CONTAINER_1_ID,
                                   SOLVER_1_ID);
    }

    @Test
    public void testExecuteRunningSolver() throws Exception {
        SolverInstance solverInstance = solverClient.createSolver(CONTAINER_1_ID,
                                                                  SOLVER_1_ID,
                                                                  SOLVER_1_CONFIG);
        assertNotNull(solverInstance);
        assertEquals(SolverInstance.SolverStatus.NOT_SOLVING,
                     solverInstance.getStatus());

        // start solver
        Object planningProblem = loadPlanningProblem(5,
                                                     15);
        solverClient.solvePlanningProblem(CONTAINER_1_ID,
                                          SOLVER_1_ID,
                                          planningProblem);

        // start solver again
        try {
            solverClient.solvePlanningProblem(CONTAINER_1_ID,
                                              SOLVER_1_ID,
                                              planningProblem);
            fail("A KieServicesException should have been thrown by now.");
        } catch (KieServicesException e) {
            KieServerAssert.assertResultContainsStringRegex(e.getMessage(),
                                                            ".*Solver .* on container .* is already executing.*");
        }

        solverClient.disposeSolver(CONTAINER_1_ID,
                                   SOLVER_1_ID);
    }

    @Test(timeout = 60000)
    public void testGetBestSolution() throws Exception {
        SolverInstance solverInstance = solverClient.createSolver(CONTAINER_1_ID,
                                                                  SOLVER_1_ID,
                                                                  SOLVER_1_CONFIG);

        // Start the solver
        Object planningProblem = loadPlanningProblem(10,
                                                     30);
        solverClient.solvePlanningProblem(CONTAINER_1_ID,
                                          SOLVER_1_ID,
                                          planningProblem);

        Object solution = null;
        HardSoftScore score = null;
        // It can take a while for the Construction Heuristic to initialize the solution
        // The test timeout will interrupt this thread if it takes too long
        while (!Thread.currentThread().isInterrupted()) {
            solverInstance = solverClient.getSolverWithBestSolution(CONTAINER_1_ID,
                                                                    SOLVER_1_ID);
            assertNotNull(solverInstance);
            solution = solverInstance.getBestSolution();

            ScoreWrapper scoreWrapper = solverInstance.getScoreWrapper();
            assertNotNull(scoreWrapper);

            if (scoreWrapper.toScore() != null) {
                assertEquals(HardSoftScore.class,
                             scoreWrapper.getScoreClass());
                score = (HardSoftScore) scoreWrapper.toScore();
            }

            // Wait until the solver finished initializing the solution
            if (solution != null && score != null && score.isSolutionInitialized()) {
                break;
            }
            Thread.sleep(1000);
        }
        assertNotNull(score);
        assertTrue(score.isSolutionInitialized());
        assertTrue(score.getHardScore() <= 0);
        // A soft score of 0 is impossible because we'll always need at least 1 computer
        assertTrue(score.getSoftScore() < 0);

        List<?> computerList = (List<?>) KieServerReflections.valueOf(solution,
                                                                      "computerList");
        assertEquals(10,
                     computerList.size());
        List<?> processList = (List<?>) KieServerReflections.valueOf(solution,
                                                                     "processList");
        assertEquals(30,
                     processList.size());
        for (Object process : processList) {
            Object computer = KieServerReflections.valueOf(process,
                                                           "computer");
            assertNotNull(computer);
            // TODO: Change to identity comparation after @XmlID is implemented
            assertTrue(computerList.contains(computer));
        }

        solverClient.disposeSolver(CONTAINER_1_ID,
                                   SOLVER_1_ID);
    }

    @Test
    public void testGetBestSolutionNotExistingSolver() throws Exception {
        try {
            solverClient.getSolverWithBestSolution(CONTAINER_1_ID,
                                                   SOLVER_1_ID);
            fail("A KieServicesException should have been thrown by now.");
        } catch (KieServicesException e) {
            KieServerAssert.assertResultContainsStringRegex(e.getMessage(),
                                                            ".*Solver.*not found in container.*");
        }
    }

    @Test
    public void testTerminateEarlyNotExistingSolver() throws Exception {
        try {
            solverClient.terminateSolverEarly(CONTAINER_1_ID,
                                              SOLVER_1_ID);
            fail("A KieServicesException should have been thrown by now.");
        } catch (KieServicesException e) {
            KieServerAssert.assertResultContainsStringRegex(e.getMessage(),
                                                            ".*Solver.*not found in container.*");
        }
    }

    @Test
    public void testTerminateEarlyStoppedSolver() throws Exception {
        solverClient.createSolver(CONTAINER_1_ID,
                                  SOLVER_1_ID,
                                  SOLVER_1_CONFIG);

        try {
            solverClient.terminateSolverEarly(CONTAINER_1_ID,
                                              SOLVER_1_ID);
            fail("A KieServicesException should have been thrown by now.");
        } catch (KieServicesException e) {
            KieServerAssert.assertResultContainsStringRegex(e.getMessage(),
                                                            ".*Solver.*from container.*is not executing.*");
        }
    }

    @Test
    public void testTerminateEarly() throws Exception {
        solverClient.createSolver(CONTAINER_1_ID,
                                  SOLVER_1_ID,
                                  SOLVER_1_CONFIG);

        // start solver
        solverClient.solvePlanningProblem(CONTAINER_1_ID,
                                          SOLVER_1_ID,
                                          loadPlanningProblem(50,
                                                              150));

        SolverInstance instance = solverClient.getSolver(CONTAINER_1_ID,
                                                         SOLVER_1_ID);
        assertEquals(SolverInstance.SolverStatus.SOLVING,
                     instance.getStatus());

        // and then terminate it
        solverClient.terminateSolverEarly(CONTAINER_1_ID,
                                          SOLVER_1_ID);

        instance = solverClient.getSolver(CONTAINER_1_ID,
                                          SOLVER_1_ID);
        assertTrue(instance.getStatus() == SolverInstance.SolverStatus.TERMINATING_EARLY
                           || instance.getStatus() == SolverInstance.SolverStatus.NOT_SOLVING);

        solverClient.disposeSolver(CONTAINER_1_ID,
                                   SOLVER_1_ID);
    }

    public Object loadPlanningProblem(int computerListSize,
                                      int processListSize) {
        Object problem = null;
        try {
            Class<?> cbgc = kieContainer.getClassLoader().loadClass(CLASS_CLOUD_GENERATOR);
            Object cbgi = cbgc.newInstance();

            Method method = cbgc.getMethod("createCloudBalance",
                                           int.class,
                                           int.class);
            problem = method.invoke(cbgi,
                                    computerListSize,
                                    processListSize);
        } catch (Exception e) {
            e.printStackTrace();
            fail("Exception trying to create cloud balance unsolved problem.");
        }
        return problem;
    }
}
