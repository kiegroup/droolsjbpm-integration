/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
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
package org.kie.server.integrationtests.optaplanner;

import java.io.BufferedReader;
import java.io.StringReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.concurrent.TimeUnit;

import org.jboss.resteasy.client.jaxrs.BasicAuthentication;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kie.api.KieServices;
import org.kie.server.api.exception.KieServicesException;
import org.kie.server.api.model.KieContainerResource;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.api.model.instance.ScoreWrapper;
import org.kie.server.api.model.instance.SolverInstance;
import org.kie.server.client.KieServicesClient;
import org.kie.server.integrationtests.config.TestConfig;
import org.kie.server.integrationtests.shared.KieServerAssert;
import org.kie.server.integrationtests.shared.KieServerDeployer;
import org.kie.server.integrationtests.shared.KieServerReflections;
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;
import org.optaplanner.core.impl.solver.ProblemFactChange;

import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static org.junit.Assert.*;
import org.assertj.core.api.Assertions;

public class OptaplannerIntegrationTest
        extends OptaplannerKieServerBaseIntegrationTest {

    private static final ReleaseId kjar1 = new ReleaseId(
            "org.kie.server.testing",
            "cloudbalance",
            "1.0.0.Final");

    private static final String NOT_EXISTING_CONTAINER_ID = "no_container";
    private static final String CONTAINER_1_ID = "cloudbalance";
    private static final String SOLVER_1_ID = "cloudsolver";
    private static final String SOLVER_PROMETHEUS_ID = "cloudsolver-prometheus";
    private static final String SOLVER_1_CONFIG = "cloudbalance-solver.xml";
    private static final String SOLVER_1_REALTIME_CONFIG = "cloudbalance-realtime-planning-solver.xml";
    private static final String SOLVER_1_REALTIME_DAEMON_CONFIG = "cloudbalance-realtime-planning-daemon-solver.xml";

    private static final String CLASS_CLOUD_BALANCE = "org.kie.server.testing.CloudBalance";
    private static final String CLASS_CLOUD_COMPUTER = "org.kie.server.testing.CloudComputer";
    private static final String CLASS_CLOUD_PROCESS = "org.kie.server.testing.CloudProcess";
    private static final String CLASS_ADD_COMPUTER_PROBLEM_FACT_CHANGE = "org.kie.server.testing.AddComputerProblemFactChange";
    private static final String CLASS_DELETE_COMPUTER_PROBLEM_FACT_CHANGE = "org.kie.server.testing.DeleteComputerProblemFactChange";
    private static final String CLASS_CLOUD_GENERATOR = "org.kie.server.testing.CloudBalancingGenerator";

    private static final long EXTENDED_TIMEOUT = 300_000L;

    @BeforeClass
    public static void deployArtifacts() {
        KieServerDeployer.buildAndDeployCommonMavenParent();
        KieServerDeployer.buildAndDeployMavenProjectFromResource("/kjars-sources/cloudbalance");

        kieContainer = KieServices.Factory.get().newKieContainer(kjar1);

        // Having timeout issues due to kjar dependencies -> raised timeout.
        KieServicesClient client = createDefaultStaticClient(EXTENDED_TIMEOUT);
        ServiceResponse<KieContainerResource> reply = client.createContainer(CONTAINER_1_ID, new KieContainerResource(CONTAINER_1_ID, kjar1));
        KieServerAssert.assertSuccess(reply);
    }

    @Override
    protected void addExtraCustomClasses(Map<String, Class<?>> extraClasses)
            throws ClassNotFoundException {

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
        extraClasses.put(CLASS_ADD_COMPUTER_PROBLEM_FACT_CHANGE,
                         Class.forName(CLASS_ADD_COMPUTER_PROBLEM_FACT_CHANGE,
                                       true,
                                       kieContainer.getClassLoader()));
        extraClasses.put(CLASS_DELETE_COMPUTER_PROBLEM_FACT_CHANGE,
                         Class.forName(CLASS_DELETE_COMPUTER_PROBLEM_FACT_CHANGE,
                                       true,
                                       kieContainer.getClassLoader()));
    }

    @Test
    public void testCreateDisposeSolver() {
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
                                                            ".*Container '" + NOT_EXISTING_CONTAINER_ID + "' is not instantiated or cannot find container for alias '" + NOT_EXISTING_CONTAINER_ID + "'.*");
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateSolverWithoutConfigPath() {
        solverClient.createSolver(CONTAINER_1_ID,
                                  SOLVER_1_ID,
                                  null);
    }

    @Test
    public void testCreateSolverWrongConfigPath() {
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
    public void testCreateSolverNullContainer() {
        solverClient.createSolver(null,
                                  SOLVER_1_ID,
                                  SOLVER_1_CONFIG);
    }

    @Test
    public void testCreateDuplicitSolver() {
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
    public void testDisposeNotExistingSolver() {
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
    public void testGetSolverState() {
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
    public void testGetNotExistingSolverState() {
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
    public void testGetSolvers() {
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

    @Test(timeout = 15_000)
    public void testExecuteSolver() throws Exception {
        testExecuteSolverHelper(SOLVER_1_ID);
    }

    @Test(timeout = 15_000)
    public void testPrometheusMetrics() throws Exception {
        testExecuteSolverHelper(SOLVER_PROMETHEUS_ID);
        checkPrometheusMetrics();
    }

    private void testExecuteSolverHelper(String solverID) throws Exception {
        SolverInstance solverInstance = solverClient.createSolver(CONTAINER_1_ID,
                                                                  solverID,
                                                                  SOLVER_1_CONFIG);
        assertNotNull(solverInstance);
        assertEquals(SolverInstance.SolverStatus.NOT_SOLVING,
                     solverInstance.getStatus());

        // the following status starts the solver
        Object planningProblem = loadPlanningProblem(5,
                                                     15);
        solverClient.solvePlanningProblem(CONTAINER_1_ID,
                                          solverID,
                                          planningProblem);

        solverInstance = solverClient.getSolver(CONTAINER_1_ID,
                                                solverID);

        // solver should finish in 5 seconds, but we wait up to 15s before timing out
        final long SOLVER_STATUS_CHECK_PERIOD = 1000L;
        while (solverInstance.getStatus() == SolverInstance.SolverStatus.SOLVING) {
            solverInstance = solverClient.getSolver(CONTAINER_1_ID,
                                                    solverID);
            assertNotNull(solverInstance);
            Thread.sleep(SOLVER_STATUS_CHECK_PERIOD);
        }

        assertEquals(SolverInstance.SolverStatus.NOT_SOLVING,
                     solverInstance.getStatus());

        solverClient.disposeSolver(CONTAINER_1_ID,
                                   solverID);
    }

    @Test(timeout = 60_000)
    public void testExecuteRealtimePlanningSolverSingleItemSubmit() throws Exception {
        testExecuteRealtimePlanningSolverSingleItemSubmit(false);
    }

    @Test(timeout = 60_000)
    public void testExecuteRealtimePlanningSolverSingleItemSubmitDaemonEnabled() throws Exception {
        testExecuteRealtimePlanningSolverSingleItemSubmit(true);
    }

    private void testExecuteRealtimePlanningSolverSingleItemSubmit(boolean daemon) throws Exception {
        SolverInstance solverInstance = solverClient.createSolver(CONTAINER_1_ID,
                                                                  SOLVER_1_ID,
                                                                  daemon ? SOLVER_1_REALTIME_DAEMON_CONFIG : SOLVER_1_REALTIME_CONFIG);
        assertNotNull(solverInstance);
        assertEquals(SolverInstance.SolverStatus.NOT_SOLVING,
                     solverInstance.getStatus());

        final int computerCount = 5;
        final int numberOfComputersToAdd = 5;
        final Object planningProblem = loadPlanningProblem(computerCount,
                                                           15);
        solverClient.solvePlanningProblem(CONTAINER_1_ID,
                                          SOLVER_1_ID,
                                          planningProblem);

        SolverInstance solver = solverClient.getSolver(CONTAINER_1_ID,
                                                       SOLVER_1_ID);
        assertEquals(SolverInstance.SolverStatus.SOLVING,
                     solver.getStatus());

        Thread.sleep(3000);

        List computerList = null;

        for (int i = 0; i < numberOfComputersToAdd; i++) {
            ProblemFactChange<?> problemFactChange = loadAddProblemFactChange(computerCount + i);
            solverClient.addProblemFactChange(CONTAINER_1_ID,
                                              SOLVER_1_ID,
                                              problemFactChange);

            do {
                final Object bestSolution = verifySolverAndGetBestSolution();
                computerList = getCloudBalanceComputerList(bestSolution);
            } while (computerCount + i + 1 != computerList.size());
        }

        assertTrue(solverClient.isEveryProblemFactChangeProcessed(CONTAINER_1_ID,
                                                                  SOLVER_1_ID));

        Thread.sleep(3000);

        assertNotNull(computerList);

        for (int i = 0; i < numberOfComputersToAdd; i++) {
            ProblemFactChange<?> problemFactChange = loadDeleteProblemFactChange(computerList.get(i));

            solverClient.addProblemFactChange(CONTAINER_1_ID,
                                              SOLVER_1_ID,
                                              problemFactChange);

            do {
                final Object bestSolution = verifySolverAndGetBestSolution();
                computerList = getCloudBalanceComputerList(bestSolution);
            } while (computerCount + numberOfComputersToAdd - i - 1 != computerList.size());
        }

        assertTrue(solverClient.isEveryProblemFactChangeProcessed(CONTAINER_1_ID,
                                                                  SOLVER_1_ID));

        solverClient.disposeSolver(CONTAINER_1_ID,
                                   SOLVER_1_ID);
    }

    @Test(timeout = 60_000)
    public void testExecuteRealtimePlanningSolverBulkItemSubmit() throws Exception {
        testExecuteRealtimePlanningSolverBulkItemSubmit(false);
    }

    @Test(timeout = 60_000)
    public void testExecuteRealtimePlanningSolverBulkItemSubmitDaemonEnabled() throws Exception {
        testExecuteRealtimePlanningSolverBulkItemSubmit(true);
    }

    private void testExecuteRealtimePlanningSolverBulkItemSubmit(boolean daemon) throws Exception {
        SolverInstance solverInstance = solverClient.createSolver(CONTAINER_1_ID,
                                                                  SOLVER_1_ID,
                                                                  daemon ? SOLVER_1_REALTIME_DAEMON_CONFIG : SOLVER_1_REALTIME_CONFIG);
        assertNotNull(solverInstance);
        assertEquals(SolverInstance.SolverStatus.NOT_SOLVING,
                     solverInstance.getStatus());

        final int initialComputerCount = 5;
        final int numberOfComputersToAdd = 5;
        final Object planningProblem = loadPlanningProblem(initialComputerCount,
                                                           15);
        solverClient.solvePlanningProblem(CONTAINER_1_ID,
                                          SOLVER_1_ID,
                                          planningProblem);

        SolverInstance solver = solverClient.getSolver(CONTAINER_1_ID,
                                                       SOLVER_1_ID);
        assertEquals(SolverInstance.SolverStatus.SOLVING,
                     solver.getStatus());

        Thread.sleep(3000);

        List<ProblemFactChange> problemFactChangeList = new ArrayList<>(numberOfComputersToAdd);
        for (int i = 0; i < numberOfComputersToAdd; i++) {
            problemFactChangeList.add(loadAddProblemFactChange(initialComputerCount + i));
        }
        solverClient.addProblemFactChanges(CONTAINER_1_ID,
                                           SOLVER_1_ID,
                                           problemFactChangeList);

        List computerList = null;
        do {
            final Object bestSolution = verifySolverAndGetBestSolution();
            computerList = getCloudBalanceComputerList(bestSolution);
        } while (initialComputerCount + numberOfComputersToAdd != computerList.size());

        assertNotNull(computerList);
        assertTrue(solverClient.isEveryProblemFactChangeProcessed(CONTAINER_1_ID,
                                                                  SOLVER_1_ID));

        Thread.sleep(3000);

        problemFactChangeList.clear();
        for (int i = 0; i < numberOfComputersToAdd; i++) {
            problemFactChangeList.add(loadDeleteProblemFactChange(computerList.get(i)));
        }

        solverClient.addProblemFactChanges(CONTAINER_1_ID,
                                           SOLVER_1_ID,
                                           problemFactChangeList);

        do {
            final Object bestSolution = verifySolverAndGetBestSolution();
            computerList = getCloudBalanceComputerList(bestSolution);
        } while (initialComputerCount != computerList.size());

        assertTrue(solverClient.isEveryProblemFactChangeProcessed(CONTAINER_1_ID,
                                                                  SOLVER_1_ID));

        solverClient.disposeSolver(CONTAINER_1_ID,
                                   SOLVER_1_ID);
    }

    private Object verifySolverAndGetBestSolution() {
        SolverInstance solver = solverClient.getSolverWithBestSolution(CONTAINER_1_ID,
                                                                       SOLVER_1_ID);
        assertEquals(SolverInstance.SolverStatus.SOLVING,
                     solver.getStatus());

        Object bestSolution = solver.getBestSolution();
        assertEquals(bestSolution.getClass().getName(),
                     CLASS_CLOUD_BALANCE);
        return bestSolution;
    }

    private List getCloudBalanceComputerList(final Object cloudBalance) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method computerListMethod = cloudBalance.getClass().getDeclaredMethod("getComputerList");
        return (List) computerListMethod.invoke(cloudBalance);
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
    public void testGetBestSolutionNotExistingSolver() {
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
    public void testTerminateEarlyNotExistingSolver() {
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
    public void testTerminateEarlyStoppedSolver() {
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

    protected WebTarget newRequest(String uriString) {

        ResteasyClient httpClient = new ResteasyClientBuilder()
                    .connectTimeout(10, TimeUnit.SECONDS)
                    .readTimeout(10, TimeUnit.SECONDS)
                    .build();

        WebTarget webTarget = httpClient.target(uriString);
        webTarget.register(new BasicAuthentication(TestConfig.getUsername(), TestConfig.getPassword()));
        return webTarget;
    }

    private void checkPrometheusMetrics() {
        Response response = null;
        try {
            String uriString = TestConfig.getKieServerHttpUrl().replaceAll("/server", "") + "/metrics";
            WebTarget clientRequest = newRequest(uriString);
            response = clientRequest.request(MediaType.TEXT_PLAIN_TYPE).get();

            Assert.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

            String res = response.readEntity(String.class);

            //These are Prometheus metrics attributes we defined in Optaplanner
            Assertions.assertThat(res).contains("solvers_running");
            Assertions.assertThat(res).contains("solver_duration_seconds_count{solver_id=\"cloudsolver-prometheus\"");
            Assertions.assertThat(res).contains("solver_duration_seconds_sum{solver_id=\"cloudsolver-prometheus\"");
            Assertions.assertThat(res).contains("solver_score_calculation_speed_count{solver_id=\"cloudsolver-prometheus\"");
            Assertions.assertThat(res).contains("solver_score_calculation_speed_sum{solver_id=\"cloudsolver-prometheus\"");
            //verify metric values
            try (BufferedReader br = new BufferedReader(new StringReader(res))) {
                String line = br.readLine();
                while (line != null) {
                    checkPrometheusMetricsLine(line);
                    line = br.readLine();
                }
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (response != null) {
                response.close();
            }
        }
    }

    private void checkPrometheusMetricsLine(String line) {
        if (line.startsWith("solver_duration_seconds_count")
                || line.startsWith("solver_duration_seconds_sum")
                || line.startsWith("solver_score_calculation_speed_count")
                || line.startsWith("solver_score_calculation_speed_sum")
        ) {
            // all metric values should be a positive double
            Assertions.assertThat(Double.parseDouble(getMetricToken(line))).isGreaterThan(0d);
        } else if (line.startsWith("solvers_running")) {
            //when test is done we should have zero solvers running
            Assertions.assertThat(Double.parseDouble(getMetricToken(line))).isEqualTo(0d);
        }
    }

    private String getMetricToken(String line) {
        StringTokenizer st = new StringTokenizer(line, " ");
        Assert.assertEquals(2, st.countTokens());
        st.nextToken();
        return st.nextToken();
    }

    private Object loadPlanningProblem(int computerListSize,
                                       int processListSize) throws NoSuchMethodException, ClassNotFoundException,
            IllegalAccessException, InstantiationException, InvocationTargetException {
        Class<?> cbgc = kieContainer.getClassLoader().loadClass(CLASS_CLOUD_GENERATOR);
        Object cbgi = cbgc.newInstance();

        Method method = cbgc.getMethod("createCloudBalance",
                                       int.class,
                                       int.class);
        return method.invoke(cbgi,
                             computerListSize,
                             processListSize);
    }

    private ProblemFactChange<?> loadAddProblemFactChange(final int currentNumberOfComputers) throws ClassNotFoundException,
            IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException {
        Class<?> cbgc = kieContainer.getClassLoader().loadClass(CLASS_CLOUD_GENERATOR);
        Object cbgi = cbgc.newInstance();

        Method method = cbgc.getMethod("createCloudComputer",
                                       int.class);
        Object cloudComputer = method.invoke(cbgi,
                                             currentNumberOfComputers);

        Class<?> cloudComputerClass = kieContainer.getClassLoader().loadClass(CLASS_CLOUD_COMPUTER);
        Class<?> problemFactChangeClass = kieContainer.getClassLoader().loadClass(CLASS_ADD_COMPUTER_PROBLEM_FACT_CHANGE);

        Constructor<?> constructor = problemFactChangeClass.getConstructor(cloudComputerClass);
        return (ProblemFactChange) constructor.newInstance(cloudComputer);
    }

    private ProblemFactChange<?> loadDeleteProblemFactChange(final Object cloudComputer) throws ClassNotFoundException,
            NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Class<?> cloudComputerClass = kieContainer.getClassLoader().loadClass(CLASS_CLOUD_COMPUTER);
        Class<?> problemFactChangeClass = kieContainer.getClassLoader().loadClass(CLASS_DELETE_COMPUTER_PROBLEM_FACT_CHANGE);

        Constructor<?> constructor = problemFactChangeClass.getConstructor(cloudComputerClass);
        return (ProblemFactChange) constructor.newInstance(cloudComputer);
    }
}
