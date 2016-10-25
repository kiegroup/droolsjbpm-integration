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

import org.junit.BeforeClass;
import org.junit.Test;
import org.kie.api.KieServices;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.api.model.instance.ScoreWrapper;
import org.kie.server.api.model.instance.SolverInstance;
import org.kie.server.api.model.instance.SolverInstanceList;
import org.kie.server.client.KieServicesException;
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;

import java.lang.Thread;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;
import org.kie.server.integrationtests.shared.KieServerAssert;
import org.kie.server.integrationtests.shared.KieServerDeployer;

public class OptaplannerIntegrationTest
        extends OptaplannerKieServerBaseIntegrationTest {
    private static final ReleaseId kjar1 = new ReleaseId(
            "org.kie.server.testing", "cloudbalance",
            "1.0.0.Final" );

    private static final String NOT_EXISTING_CONTAINER_ID  = "no_container";
    private static final String CONTAINER_1_ID  = "cloudbalance";
    private static final String SOLVER_1_ID     = "cloudsolver";
    private static final String SOLVER_1_CONFIG = "META-INF/cloudbalance-solver.xml";

    private static final String CLASS_CLOUD_BALANCE  = "org.kie.server.testing.CloudBalance";
    private static final String CLASS_CLOUD_COMPUTER = "org.kie.server.testing.CloudComputer";
    private static final String CLASS_CLOUD_PROCESS  = "org.kie.server.testing.CloudProcess";
    private static final String CLASS_CLOUD_GENERATOR = "org.kie.server.testing.CloudBalancingGenerator";


    @BeforeClass
    public static void deployArtifacts() {
        KieServerDeployer.buildAndDeployCommonMavenParent();
        KieServerDeployer.buildAndDeployMavenProject( ClassLoader.class.getResource( "/kjars-sources/cloudbalance" ).getFile() );

        kieContainer = KieServices.Factory.get().newKieContainer(kjar1);

        createContainer(CONTAINER_1_ID, kjar1);
    }

    @Override
    protected void addExtraCustomClasses(Map<String, Class<?>> extraClasses)
            throws Exception {

        extraClasses.put( CLASS_CLOUD_BALANCE, Class.forName( CLASS_CLOUD_BALANCE, true, kieContainer.getClassLoader() ) );
        extraClasses.put( CLASS_CLOUD_COMPUTER, Class.forName( CLASS_CLOUD_COMPUTER, true, kieContainer.getClassLoader() ) );
        extraClasses.put( CLASS_CLOUD_PROCESS, Class.forName( CLASS_CLOUD_PROCESS, true, kieContainer.getClassLoader() ) );
    }

    @Test
    public void testCreateDisposeSolver()
            throws Exception {
        KieServerAssert.assertSuccess( solverClient.createSolver( CONTAINER_1_ID, SOLVER_1_ID, SOLVER_1_CONFIG ) );
        KieServerAssert.assertSuccess( solverClient.disposeSolver( CONTAINER_1_ID, SOLVER_1_ID ) );
    }

    @Test
    public void testCreateSolverFromNotExistingContainer()
            throws Exception {
        ServiceResponse<SolverInstance> createSolverResponse = solverClient.createSolver( NOT_EXISTING_CONTAINER_ID, SOLVER_1_ID, SOLVER_1_CONFIG );

        ServiceResponse.ResponseType type = createSolverResponse.getType();
        assertEquals( "Expected FAILURE response, but got " + type + "!", ServiceResponse.ResponseType.FAILURE, type );
        KieServerAssert.assertResultContainsString( createSolverResponse.getMsg(), "Failed to create solver. Container does not exist" );
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateSolverWithoutSolverInstance() throws Exception {
        solverClient.createSolver(CONTAINER_1_ID, SOLVER_1_ID, null);
    }

    @Test
    public void testCreateSolverWrongSolverInstanceConfigPath() throws Exception {
        ServiceResponse<SolverInstance> createSolverResponse = solverClient.createSolver(CONTAINER_1_ID, SOLVER_1_ID, "NonExistingPath");

        ServiceResponse.ResponseType type = createSolverResponse.getType();
        assertEquals( "Expected FAILURE response, but got " + type + "!", ServiceResponse.ResponseType.FAILURE, type );
        KieServerAssert.assertResultContainsStringRegex( createSolverResponse.getMsg(), ".*The solverConfigResource.*does not exist as a classpath resource in the classLoader.*" );
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateSolverNullContainer() throws Exception {
        solverClient.createSolver( null, SOLVER_1_ID, null );
    }

    @Test
    public void testCreateDuplicitSolver() throws Exception {
        ServiceResponse<SolverInstance> createSolverResponse = solverClient.createSolver( CONTAINER_1_ID, SOLVER_1_ID, SOLVER_1_CONFIG );
        KieServerAssert.assertSuccess(createSolverResponse);

        createSolverResponse = solverClient.createSolver( CONTAINER_1_ID, SOLVER_1_ID, SOLVER_1_CONFIG );
        ServiceResponse.ResponseType type = createSolverResponse.getType();
        assertEquals( "Expected FAILURE response, but got " + type + "!", ServiceResponse.ResponseType.FAILURE, type );
    }

    @Test
    public void testDisposeNotExistingSolver() throws Exception {
        try {
            solverClient.disposeSolver( CONTAINER_1_ID, SOLVER_1_ID );
            fail("A KieServicesException should have been thrown by now.");
        } catch (KieServicesException e) {
            KieServerAssert.assertResultContainsStringRegex(e.getMessage(), ".*Solver.*from container.*not found.*");
        }
    }

    @Test
    public void testGetSolverState() throws Exception {
        KieServerAssert.assertSuccess( solverClient.createSolver( CONTAINER_1_ID, SOLVER_1_ID, SOLVER_1_CONFIG ) );

        ServiceResponse<SolverInstance> solverState = solverClient.getSolverState(CONTAINER_1_ID, SOLVER_1_ID);
        KieServerAssert.assertSuccess(solverState);

        SolverInstance returnedInstance = solverState.getResult();
        assertEquals(CONTAINER_1_ID, returnedInstance.getContainerId());
        assertEquals( SOLVER_1_CONFIG, returnedInstance.getSolverConfigFile() );
        assertEquals( SOLVER_1_ID, returnedInstance.getSolverId() );
        assertEquals( SolverInstance.getSolverInstanceKey( CONTAINER_1_ID, SOLVER_1_ID ), returnedInstance.getSolverInstanceKey());
        assertEquals( SolverInstance.SolverStatus.NOT_SOLVING, returnedInstance.getStatus());
        assertNotNull( returnedInstance.getScoreWrapper() );
        assertNull( returnedInstance.getScoreWrapper().toScore() );
    }

    @Test
    public void testGetNotExistingSolverState() throws Exception {
        try {
            solverClient.getSolverState(CONTAINER_1_ID, SOLVER_1_ID);
            fail("A KieServicesException should have been thrown by now.");
        } catch (KieServicesException e) {
            KieServerAssert.assertResultContainsStringRegex(e.getMessage(), ".*Solver.*not found in container.*");
        }
    }

    @Test
    public void testGetSolvers() throws Exception {
        ServiceResponse<SolverInstanceList> solvers = solverClient.getSolvers(CONTAINER_1_ID);
        KieServerAssert.assertSuccess(solvers);
        assertEquals( 0, solvers.getResult().getContainers().size() );

        KieServerAssert.assertSuccess( solverClient.createSolver( CONTAINER_1_ID, SOLVER_1_ID, SOLVER_1_CONFIG ) );

        solvers = solverClient.getSolvers(CONTAINER_1_ID);
        KieServerAssert.assertSuccess(solvers);
        assertEquals( 1, solvers.getResult().getContainers().size() );

        SolverInstance returnedInstance = solvers.getResult().getContainers().get(0);
        assertEquals( CONTAINER_1_ID, returnedInstance.getContainerId() );
        assertEquals( SOLVER_1_CONFIG, returnedInstance.getSolverConfigFile() );
        assertEquals( SOLVER_1_ID, returnedInstance.getSolverId() );
        assertEquals( SolverInstance.getSolverInstanceKey( CONTAINER_1_ID, SOLVER_1_ID ), returnedInstance.getSolverInstanceKey());
        assertEquals( SolverInstance.SolverStatus.NOT_SOLVING, returnedInstance.getStatus());
        assertNotNull( returnedInstance.getScoreWrapper() );
        assertNull( returnedInstance.getScoreWrapper().toScore() );
    }

    @Test
    public void testExecuteSolver() throws Exception {
        ServiceResponse<SolverInstance> response = solverClient.createSolver( CONTAINER_1_ID, SOLVER_1_ID, SOLVER_1_CONFIG );
        KieServerAssert.assertSuccess( response );
        assertEquals( SolverInstance.SolverStatus.NOT_SOLVING, response.getResult().getStatus() );

        // the following status starts the solver
        SolverInstance instance = new SolverInstance();
        instance.setStatus( SolverInstance.SolverStatus.SOLVING );
        instance.setPlanningProblem( loadPlanningProblem( 5, 15 ) );
        response = solverClient.updateSolverState( CONTAINER_1_ID, SOLVER_1_ID, instance );
        KieServerAssert.assertSuccess( response );

        // solver should finish in 5 seconds, but we wait up to 15s before timing out
        for( int i = 0; i < 5 && response.getResult().getStatus() == SolverInstance.SolverStatus.SOLVING; i++ ) {
            Thread.sleep( 3000 );
            response = solverClient.getSolverState( CONTAINER_1_ID, SOLVER_1_ID );
            KieServerAssert.assertSuccess( response );
        }

        assertEquals( SolverInstance.SolverStatus.NOT_SOLVING, response.getResult().getStatus() );

        KieServerAssert.assertSuccess( solverClient.disposeSolver( CONTAINER_1_ID, SOLVER_1_ID ) );
    }

    @Test
    public void testExecuteRunningSolver() throws Exception {
        ServiceResponse<SolverInstance> response = solverClient.createSolver( CONTAINER_1_ID, SOLVER_1_ID, SOLVER_1_CONFIG );
        KieServerAssert.assertSuccess( response );
        assertEquals( SolverInstance.SolverStatus.NOT_SOLVING, response.getResult().getStatus() );

        // start solver
        SolverInstance instance = new SolverInstance();
        instance.setStatus( SolverInstance.SolverStatus.SOLVING );
        instance.setPlanningProblem( loadPlanningProblem( 50, 150 ) );
        response = solverClient.updateSolverState( CONTAINER_1_ID, SOLVER_1_ID, instance );
        KieServerAssert.assertSuccess( response );
        assertEquals( SolverInstance.SolverStatus.SOLVING, response.getResult().getStatus() );

        // start solver again
        response = solverClient.updateSolverState( CONTAINER_1_ID, SOLVER_1_ID, instance );
        KieServerAssert.assertSuccess( response );
        KieServerAssert.assertResultContainsStringRegex( response.getMsg(), "Solver.*on container.*is already executing." );

        KieServerAssert.assertSuccess( solverClient.disposeSolver( CONTAINER_1_ID, SOLVER_1_ID ) );
    }

    @Test(timeout = 60000)
    public void testGetBestSolution() throws Exception {
        KieServerAssert.assertSuccess( solverClient.createSolver( CONTAINER_1_ID, SOLVER_1_ID, SOLVER_1_CONFIG ) );

        // the following status starts the solver
        SolverInstance instance = new SolverInstance();
        instance.setStatus( SolverInstance.SolverStatus.SOLVING );
        instance.setPlanningProblem( loadPlanningProblem( 10, 30 ) );
        ServiceResponse<SolverInstance> response = solverClient.updateSolverState( CONTAINER_1_ID, SOLVER_1_ID, instance );
        KieServerAssert.assertSuccess( response );
        assertEquals( SolverInstance.SolverStatus.SOLVING, response.getResult().getStatus() );

        Object solution = null;
        HardSoftScore score = null;
        // It can take a while for the Construction Heuristic to initialize the solution
        // The test timeout will interrupt this thread if it takes too long
        while (!Thread.currentThread().isInterrupted()) {
            ServiceResponse<SolverInstance> solutionResponse = solverClient.getSolverBestSolution(CONTAINER_1_ID, SOLVER_1_ID);
            KieServerAssert.assertSuccess(solutionResponse);
            solution = solutionResponse.getResult().getBestSolution();

            ScoreWrapper scoreWrapper = solutionResponse.getResult().getScoreWrapper();
            assertNotNull( scoreWrapper );

            if ( scoreWrapper.toScore() != null ) {
                assertEquals( HardSoftScore.class, scoreWrapper.getScoreClass() );
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

        List<?> computerList = (List<?>) valueOf(solution, "computerList");
        assertEquals(10, computerList.size());
        List<?> processList = (List<?>) valueOf(solution, "processList");
        assertEquals(30, processList.size());
        for(Object process : processList) {
            Object computer = valueOf(process, "computer");
            assertNotNull(computer);
            // TODO: Change to identity comparation after @XmlID is implemented
            assertTrue(computerList.contains(computer));
        }

        KieServerAssert.assertSuccess( solverClient.disposeSolver( CONTAINER_1_ID, SOLVER_1_ID ) );
    }

    @Test
    public void testGetBestSolutionNotExistingSolver() throws Exception {
        try {
            solverClient.getSolverBestSolution(CONTAINER_1_ID, SOLVER_1_ID);
            fail("A KieServicesException should have been thrown by now.");
        } catch (KieServicesException e) {
            KieServerAssert.assertResultContainsStringRegex(e.getMessage(), ".*Solver.*not found in container.*");
        }
    }

    @Test
    public void testTerminateEarlyNotExistingSolver() throws Exception {
        SolverInstance instance = new SolverInstance();
        instance.setSolverConfigFile( SOLVER_1_CONFIG );
        instance.setStatus( SolverInstance.SolverStatus.NOT_SOLVING );
        try {
            solverClient.updateSolverState( CONTAINER_1_ID, SOLVER_1_ID, instance );
            fail("A KieServicesException should have been thrown by now.");
        } catch (KieServicesException e) {
            KieServerAssert.assertResultContainsStringRegex(e.getMessage(), ".*Solver.*not found in container.*");
        }
    }

    @Test
    public void testTerminateEarlyStoppedSolver() throws Exception {
        KieServerAssert.assertSuccess( solverClient.createSolver( CONTAINER_1_ID, SOLVER_1_ID, SOLVER_1_CONFIG ) );

        SolverInstance instance = new SolverInstance();
        instance.setStatus( SolverInstance.SolverStatus.NOT_SOLVING );
        ServiceResponse<SolverInstance> updateSolverState = solverClient.updateSolverState( CONTAINER_1_ID, SOLVER_1_ID, instance );
        KieServerAssert.assertSuccess( updateSolverState );
        KieServerAssert.assertResultContainsStringRegex(updateSolverState.getMsg(), "Solver.*on container.*already terminated.");
    }

    @Test
    public void testTerminateEarly() throws Exception {
        ServiceResponse<SolverInstance> response = solverClient.createSolver( CONTAINER_1_ID, SOLVER_1_ID, SOLVER_1_CONFIG );
        KieServerAssert.assertSuccess( response );
        assertEquals( SolverInstance.SolverStatus.NOT_SOLVING, response.getResult().getStatus() );

        // start solver
        SolverInstance instance = new SolverInstance();
        instance.setStatus( SolverInstance.SolverStatus.SOLVING );
        instance.setPlanningProblem( loadPlanningProblem( 50, 150 ) );
        response = solverClient.updateSolverState( CONTAINER_1_ID, SOLVER_1_ID, instance );
        KieServerAssert.assertSuccess( response );
        assertEquals( SolverInstance.SolverStatus.SOLVING, response.getResult().getStatus() );

        // and then terminate it
        instance.setStatus( SolverInstance.SolverStatus.NOT_SOLVING );
        instance.setPlanningProblem(null);
        response = solverClient.updateSolverState( CONTAINER_1_ID, SOLVER_1_ID, instance );
        KieServerAssert.assertSuccess( response );
        assertTrue(response.getResult().getStatus() == SolverInstance.SolverStatus.TERMINATING_EARLY
                || response.getResult().getStatus() == SolverInstance.SolverStatus.NOT_SOLVING);

        KieServerAssert.assertSuccess( solverClient.disposeSolver( CONTAINER_1_ID, SOLVER_1_ID ) );
    }

    public Object loadPlanningProblem( int computerListSize, int processListSize ) {
        Object problem = null;
        try {
            Class<?> cbgc = kieContainer.getClassLoader().loadClass( CLASS_CLOUD_GENERATOR );
            Object cbgi = cbgc.newInstance();

            Method method = cbgc.getMethod( "createCloudBalance", int.class, int.class );
            problem = method.invoke( cbgi, computerListSize, processListSize );
        } catch ( Exception e ) {
            e.printStackTrace();
            fail( "Exception trying to create cloud balance unsolved problem.");
        }
        return problem;
    }

}
