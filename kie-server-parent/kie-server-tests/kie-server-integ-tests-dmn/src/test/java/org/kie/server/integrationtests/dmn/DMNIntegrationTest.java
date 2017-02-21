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

package org.kie.server.integrationtests.dmn;

import org.junit.BeforeClass;
import org.junit.Test;
import org.kie.api.KieServices;
import org.kie.dmn.api.core.DMNContext;
import org.kie.dmn.api.core.DMNResult;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.api.model.dmn.DMNResultKS;
import org.kie.server.api.model.instance.ScoreWrapper;
import org.kie.server.api.model.instance.SolverInstance;
import org.kie.server.api.model.instance.SolverInstanceList;
import org.kie.server.client.KieServicesException;

import java.lang.Thread;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;
import org.kie.server.integrationtests.shared.KieServerAssert;
import org.kie.server.integrationtests.shared.KieServerDeployer;

public class DMNIntegrationTest
        extends DMNKieServerBaseIntegrationTest {
    private static final ReleaseId kjar1 = new ReleaseId(
            "org.kie.server.testing", "function-definition",
            "1.0.0.Final" );

    private static final String CONTAINER_1_ID  = "function-definition";

    @BeforeClass
    public static void deployArtifacts() {
        KieServerDeployer.buildAndDeployCommonMavenParent();
        KieServerDeployer.buildAndDeployMavenProject( ClassLoader.class.getResource( "/kjars-sources/function-definition" ).getFile() );

        kieContainer = KieServices.Factory.get().newKieContainer(kjar1);
        createContainer(CONTAINER_1_ID, kjar1);
    }

    @Override
    protected void addExtraCustomClasses(Map<String, Class<?>> extraClasses)
            throws Exception {

        // no extra classes.
    }

    @Test
    public void testHelloWorld() {
        System.err.println("1+1:");
        DMNContext dmnContext = dmnClient.newContext();
        dmnContext.set( "a", 10 );
        dmnContext.set( "b", 5 );
        ServiceResponse<DMNResult> evaluateAllDecisions = dmnClient.evaluateAllDecisions(CONTAINER_1_ID, dmnContext);
        
        System.out.println("FROM THE TEST:"+evaluateAllDecisions);
//        DO NOT CALL: System.out.println(evaluateAllDecisions.getResult().getContext());
        System.out.println(evaluateAllDecisions.getResult().getMessages());
        System.out.println(evaluateAllDecisions.getResult().getDecisionResults());
    }
    
    /*
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
*/

}
