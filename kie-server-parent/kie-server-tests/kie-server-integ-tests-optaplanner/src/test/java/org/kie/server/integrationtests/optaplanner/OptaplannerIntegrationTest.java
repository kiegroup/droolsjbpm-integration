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
import org.junit.Ignore;
import org.junit.Test;
import org.kie.api.KieServices;
import org.kie.server.api.model.KieContainerResource;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.api.model.instance.SolverInstance;
import org.kie.server.api.model.instance.SolverInstanceList;
import org.kie.server.client.KieServicesException;
import org.optaplanner.core.api.domain.solution.Solution;

import java.lang.Thread;
import java.lang.reflect.Method;
import java.util.Map;

import static org.junit.Assert.*;

public class OptaplannerIntegrationTest
        extends OptaplannerKieServerBaseIntegrationTest {
    private static final ReleaseId kjar1 = new ReleaseId(
            "org.kie.server.testing", "cloudbalance",
            "1.0.0.Final" );

    private static final String CONTAINER_1_ID  = "cloudbalance";
    private static final String SOLVER_1_ID     = "cloudsolver";
    private static final String SOLVER_1_CONFIG = "META-INF/cloudbalance-solver.xml";

    private static final String CLASS_CLOUD_BALANCE  = "org.kie.server.testing.CloudBalance";
    private static final String CLASS_CLOUD_COMPUTER = "org.kie.server.testing.CloudComputer";
    private static final String CLASS_CLOUD_PROCESS  = "org.kie.server.testing.CloudProcess";
    private static final String CLASS_CLOUD_GENERATOR = "org.kie.server.testing.CloudBalancingGenerator";


    @BeforeClass
    public static void deployArtifacts() {
        buildAndDeployCommonMavenParent();
        buildAndDeployMavenProject( ClassLoader.class.getResource( "/kjars-sources/cloudbalance" ).getFile() );

        kieContainer = KieServices.Factory.get().newKieContainer(kjar1);
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
        SolverInstance instance = new SolverInstance();
        instance.setSolverConfigFile( SOLVER_1_CONFIG );
        assertSuccess( client.createContainer( CONTAINER_1_ID, new KieContainerResource( CONTAINER_1_ID, kjar1 ) ) );
        assertSuccess( solverClient.createSolver( CONTAINER_1_ID, SOLVER_1_ID, instance ) );
        assertSuccess( solverClient.disposeSolver( CONTAINER_1_ID, SOLVER_1_ID ) );
    }

    @Test
    public void testCreateSolverFromNotExistingContainer()
            throws Exception {
        SolverInstance instance = new SolverInstance();
        instance.setSolverConfigFile( SOLVER_1_CONFIG );
        ServiceResponse<SolverInstance> createSolverResponse = solverClient.createSolver( CONTAINER_1_ID, SOLVER_1_ID, instance );

        ServiceResponse.ResponseType type = createSolverResponse.getType();
        assertEquals( "Expected FAILURE response, but got " + type + "!", ServiceResponse.ResponseType.FAILURE, type );
        assertResultContainsString( createSolverResponse.getMsg(), "Failed to create solver. Container does not exist" );
    }

    @Test
    public void testCreateSolverWithoutSolverInstance() throws Exception {
        assertSuccess( client.createContainer( CONTAINER_1_ID, new KieContainerResource( CONTAINER_1_ID, kjar1 ) ) );
        ServiceResponse<SolverInstance> createSolverResponse = solverClient.createSolver(CONTAINER_1_ID, SOLVER_1_ID, null);

        ServiceResponse.ResponseType type = createSolverResponse.getType();
        assertEquals("Expected FAILURE response, but got " + type + "!", ServiceResponse.ResponseType.FAILURE, type);
        assertResultContainsStringRegex( createSolverResponse.getMsg(), "Failed to create solver for container.*Solver configuration file is null.*" );
    }

    @Test
    public void testCreateSolverWrongSolverInstanceConfigPath() throws Exception {
        SolverInstance instance = new SolverInstance();
        instance.setSolverConfigFile("NonExistingPath");
        assertSuccess( client.createContainer( CONTAINER_1_ID, new KieContainerResource( CONTAINER_1_ID, kjar1 ) ) );
        ServiceResponse<SolverInstance> createSolverResponse = solverClient.createSolver(CONTAINER_1_ID, SOLVER_1_ID, instance);

        ServiceResponse.ResponseType type = createSolverResponse.getType();
        assertEquals( "Expected FAILURE response, but got " + type + "!", ServiceResponse.ResponseType.FAILURE, type );
        assertResultContainsStringRegex(createSolverResponse.getMsg(), ".*The solverConfigResource.*does not exist as a classpath resource in the classLoader.*");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateSolverNullContainer() throws Exception {
        assertSuccess( client.createContainer( CONTAINER_1_ID, new KieContainerResource( CONTAINER_1_ID, kjar1 ) ) );
        solverClient.createSolver( null, SOLVER_1_ID, null );
    }

    @Test
    public void testCreateDuplicitSolver() throws Exception {
        SolverInstance instance = new SolverInstance();
        instance.setSolverConfigFile( SOLVER_1_CONFIG );
        assertSuccess( client.createContainer( CONTAINER_1_ID, new KieContainerResource( CONTAINER_1_ID, kjar1 ) ) );

        ServiceResponse<SolverInstance> createSolverResponse = solverClient.createSolver( CONTAINER_1_ID, SOLVER_1_ID, instance );
        assertSuccess(createSolverResponse);

        createSolverResponse = solverClient.createSolver( CONTAINER_1_ID, SOLVER_1_ID, instance );
        ServiceResponse.ResponseType type = createSolverResponse.getType();
        assertEquals( "Expected FAILURE response, but got " + type + "!", ServiceResponse.ResponseType.FAILURE, type );
    }

    @Ignore @Test(expected = KieServicesException.class)
    public void testDisposeNotExistingSolver() throws Exception {
        solverClient.disposeSolver( CONTAINER_1_ID, SOLVER_1_ID );
    }

    @Test
    public void testGetSolverState() throws Exception {
        SolverInstance instance = new SolverInstance();
        instance.setSolverConfigFile( SOLVER_1_CONFIG );
        assertSuccess( client.createContainer( CONTAINER_1_ID, new KieContainerResource( CONTAINER_1_ID, kjar1 ) ) );
        assertSuccess( solverClient.createSolver( CONTAINER_1_ID, SOLVER_1_ID, instance ) );

        ServiceResponse<SolverInstance> solverState = solverClient.getSolverState(CONTAINER_1_ID, SOLVER_1_ID);
        assertSuccess(solverState);

        SolverInstance returnedInstance = solverState.getResult();
        assertEquals(CONTAINER_1_ID, returnedInstance.getContainerId());
        assertEquals( SOLVER_1_CONFIG, returnedInstance.getSolverConfigFile() );
        assertEquals( SOLVER_1_ID, returnedInstance.getSolverId() );
        assertEquals( SolverInstance.getSolverInstanceKey( CONTAINER_1_ID, SOLVER_1_ID ), returnedInstance.getSolverInstanceKey());
        assertEquals( SolverInstance.SolverStatus.NOT_SOLVING, returnedInstance.getStatus());
        assertNull( returnedInstance.getScore() );
    }

    @Test
    public void testGetNotExistingSolverState() throws Exception {
        assertSuccess( client.createContainer( CONTAINER_1_ID, new KieContainerResource( CONTAINER_1_ID, kjar1 ) ) );

        try {
            solverClient.getSolverState(CONTAINER_1_ID, SOLVER_1_ID);
            fail("A KieServicesException should have been thrown by now.");
        } catch (KieServicesException e) {
            assertResultContainsStringRegex(e.getMessage(), ".*Solver.*not found in container.*");
        }
    }

    @Test
    public void testGetSolvers() throws Exception {
        ServiceResponse<SolverInstanceList> solvers = solverClient.getSolvers(CONTAINER_1_ID);
        assertSuccess(solvers);
        assertEquals(0, solvers.getResult().getContainers().size());

        SolverInstance instance = new SolverInstance();
        instance.setSolverConfigFile( SOLVER_1_CONFIG );
        assertSuccess( client.createContainer( CONTAINER_1_ID, new KieContainerResource( CONTAINER_1_ID, kjar1 ) ) );
        assertSuccess( solverClient.createSolver( CONTAINER_1_ID, SOLVER_1_ID, instance ) );

        solvers = solverClient.getSolvers(CONTAINER_1_ID);
        assertSuccess(solvers);
        assertEquals(1, solvers.getResult().getContainers().size());

        SolverInstance returnedInstance = solvers.getResult().getContainers().get(0);
        assertEquals(CONTAINER_1_ID, returnedInstance.getContainerId());
        assertEquals( SOLVER_1_CONFIG, returnedInstance.getSolverConfigFile() );
        assertEquals( SOLVER_1_ID, returnedInstance.getSolverId() );
        assertEquals( SolverInstance.getSolverInstanceKey( CONTAINER_1_ID, SOLVER_1_ID ), returnedInstance.getSolverInstanceKey());
        assertEquals( SolverInstance.SolverStatus.NOT_SOLVING, returnedInstance.getStatus());
        assertNull( returnedInstance.getScore() );
    }

    @Test
    public void testExecuteSolver() throws Exception {
        SolverInstance instance = new SolverInstance();
        instance.setSolverConfigFile( SOLVER_1_CONFIG );
        assertSuccess( client.createContainer( CONTAINER_1_ID, new KieContainerResource( CONTAINER_1_ID, kjar1 ) ) );

        ServiceResponse<SolverInstance> response = solverClient.createSolver( CONTAINER_1_ID, SOLVER_1_ID, instance );
        assertSuccess( response );
        assertEquals( SolverInstance.SolverStatus.NOT_SOLVING, response.getResult().getStatus() );

        // the following status starts the solver
        instance.setStatus( SolverInstance.SolverStatus.SOLVING );
        instance.setPlanningProblem( loadPlanningProblem( 5, 10 ) );
        response = solverClient.updateSolverState( CONTAINER_1_ID, SOLVER_1_ID, instance );
        assertSuccess( response );

        // solver should finish in 5 seconds, but we wait up to 15s before timing out
        for( int i = 0; i < 5 && response.getResult().getStatus() == SolverInstance.SolverStatus.SOLVING; i++ ) {
            Thread.sleep( 3000 );
            response = solverClient.getSolverState( CONTAINER_1_ID, SOLVER_1_ID );
            assertSuccess( response );
        }

        assertEquals( SolverInstance.SolverStatus.NOT_SOLVING, response.getResult().getStatus() );

        assertSuccess( solverClient.disposeSolver( CONTAINER_1_ID, SOLVER_1_ID ) );
    }

    @Test
    public void testExecuteRunningSolver() throws Exception {
        SolverInstance instance = new SolverInstance();
        instance.setSolverConfigFile( SOLVER_1_CONFIG );
        assertSuccess( client.createContainer( CONTAINER_1_ID, new KieContainerResource( CONTAINER_1_ID, kjar1 ) ) );

        ServiceResponse<SolverInstance> response = solverClient.createSolver( CONTAINER_1_ID, SOLVER_1_ID, instance );
        assertSuccess( response );
        assertEquals( SolverInstance.SolverStatus.NOT_SOLVING, response.getResult().getStatus() );

        // start solver
        instance.setStatus( SolverInstance.SolverStatus.SOLVING );
        instance.setPlanningProblem( loadPlanningProblem( 50, 300 ) );
        response = solverClient.updateSolverState( CONTAINER_1_ID, SOLVER_1_ID, instance );
        assertSuccess( response );
        assertEquals( SolverInstance.SolverStatus.SOLVING, response.getResult().getStatus() );

        // start solver again
        response = solverClient.updateSolverState( CONTAINER_1_ID, SOLVER_1_ID, instance );
        assertSuccess( response );
        assertResultContainsStringRegex( response.getMsg(), "Solver.*on container.*is already executing." );

        assertSuccess( solverClient.disposeSolver( CONTAINER_1_ID, SOLVER_1_ID ) );
    }

    @Test
    public void testGetBestSolution() throws Exception {
        SolverInstance instance = new SolverInstance();
        instance.setSolverConfigFile( SOLVER_1_CONFIG );
        assertSuccess( client.createContainer( CONTAINER_1_ID, new KieContainerResource( CONTAINER_1_ID, kjar1 ) ) );

        assertSuccess( solverClient.createSolver( CONTAINER_1_ID, SOLVER_1_ID, instance ) );

        // the following status starts the solver
        instance.setStatus( SolverInstance.SolverStatus.SOLVING );
        instance.setPlanningProblem( loadPlanningProblem( 10, 100 ) );
        ServiceResponse<SolverInstance> response = solverClient.updateSolverState( CONTAINER_1_ID, SOLVER_1_ID, instance );
        assertSuccess( response );
        assertEquals( SolverInstance.SolverStatus.SOLVING, response.getResult().getStatus() );

        Solution solution = null;
        // It can take a while for the Construction Heuristic to initialize the solution
        for (int i = 0; i < 15; i++) {
            ServiceResponse<SolverInstance> solutionResponse = solverClient.getSolverBestSolution(CONTAINER_1_ID, SOLVER_1_ID);
            assertSuccess(solutionResponse);
            solution = solutionResponse.getResult().getBestSolution();
            // Only once the solution's score is null, the solution is fully uninitialized
            // TODO add "|| solution.getScore().isInitialized()" once PLANNER-405 is fixed
            if (solution != null && solution.getScore() != null) {
                break;
            }
            Thread.sleep(1000);
        }
        // TODO add "|| solution.getScore().isInitialized()" once PLANNER-405 is fixed
        assertTrue(solution.getScore() != null);

        assertSuccess( solverClient.disposeSolver( CONTAINER_1_ID, SOLVER_1_ID ) );
    }

    @Test
    public void testGetBestSolutionNotExistingSolver() throws Exception {
        assertSuccess( client.createContainer( CONTAINER_1_ID, new KieContainerResource( CONTAINER_1_ID, kjar1 ) ) );

        try {
            solverClient.getSolverBestSolution(CONTAINER_1_ID, SOLVER_1_ID);
            fail("A KieServicesException should have been thrown by now.");
        } catch (KieServicesException e) {
            assertResultContainsStringRegex(e.getMessage(), ".*Solver.*not found in container.*");
        }
    }

    @Test
    public void testTerminateEarlyNotExistingSolver() throws Exception {
        assertSuccess( client.createContainer( CONTAINER_1_ID, new KieContainerResource( CONTAINER_1_ID, kjar1 ) ) );

        SolverInstance instance = new SolverInstance();
        instance.setSolverConfigFile( SOLVER_1_CONFIG );
        instance.setStatus( SolverInstance.SolverStatus.NOT_SOLVING );
        instance.setPlanningProblem( loadPlanningProblem( 5, 10 ) );
        try {
            solverClient.updateSolverState( CONTAINER_1_ID, SOLVER_1_ID, instance );
            fail("A KieServicesException should have been thrown by now.");
        } catch (KieServicesException e) {
            assertResultContainsStringRegex(e.getMessage(), ".*Solver.*not found in container.*");
        }
    }

    @Test
    public void testTerminateEarlyStoppedSolver() throws Exception {
        SolverInstance instance = new SolverInstance();
        instance.setSolverConfigFile( SOLVER_1_CONFIG );
        assertSuccess( client.createContainer( CONTAINER_1_ID, new KieContainerResource( CONTAINER_1_ID, kjar1 ) ) );
        assertSuccess( solverClient.createSolver( CONTAINER_1_ID, SOLVER_1_ID, instance ) );

        instance.setStatus( SolverInstance.SolverStatus.NOT_SOLVING );
        instance.setPlanningProblem( loadPlanningProblem( 50, 300 ) );
        ServiceResponse<SolverInstance> updateSolverState = solverClient.updateSolverState( CONTAINER_1_ID, SOLVER_1_ID, instance );
        assertSuccess( updateSolverState );
        assertResultContainsStringRegex(updateSolverState.getMsg(), "Solver.*on container.*already terminated.");
    }

    @Test
    public void testTerminateEarly() throws Exception {
        assertSuccess( client.createContainer( CONTAINER_1_ID, new KieContainerResource( CONTAINER_1_ID, kjar1 ) ) );

        SolverInstance instance = new SolverInstance();
        instance.setSolverConfigFile( SOLVER_1_CONFIG );
        ServiceResponse<SolverInstance> response = solverClient.createSolver( CONTAINER_1_ID, SOLVER_1_ID, instance );
        assertSuccess( response );
        assertEquals( SolverInstance.SolverStatus.NOT_SOLVING, response.getResult().getStatus() );

        // start solver
        instance.setStatus( SolverInstance.SolverStatus.SOLVING );
        instance.setPlanningProblem( loadPlanningProblem( 50, 300 ) );
        response = solverClient.updateSolverState( CONTAINER_1_ID, SOLVER_1_ID, instance );
        assertSuccess( response );
        assertEquals( SolverInstance.SolverStatus.SOLVING, response.getResult().getStatus() );

        // and then terminate it
        instance.setStatus( SolverInstance.SolverStatus.NOT_SOLVING );
        response = solverClient.updateSolverState( CONTAINER_1_ID, SOLVER_1_ID, instance );
        assertSuccess( response );
        assertTrue(response.getResult().getStatus() == SolverInstance.SolverStatus.TERMINATING_EARLY
                || response.getResult().getStatus() == SolverInstance.SolverStatus.NOT_SOLVING);

        assertSuccess( solverClient.disposeSolver( CONTAINER_1_ID, SOLVER_1_ID ) );
    }

    public Solution loadPlanningProblem( int computerListSize, int processListSize ) {
        Solution problem = null;
        try {
            Class<?> cbgc = kieContainer.getClassLoader().loadClass( CLASS_CLOUD_GENERATOR );
            Object cbgi = cbgc.newInstance();

            Method method = cbgc.getMethod( "createCloudBalance", int.class, int.class );
            problem = (Solution) method.invoke( cbgi, computerListSize, processListSize );
        } catch ( Exception e ) {
            e.printStackTrace();
            fail( "Exception trying to create cloud balance unsolved problem.");
        }
        return problem;
    }

}
