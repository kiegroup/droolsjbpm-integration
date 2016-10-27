/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
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
package org.kie.server.integrationtests.optaplanner.jms;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runners.Parameterized;
import org.kie.api.KieServices;
import org.kie.server.api.marshalling.Marshaller;
import org.kie.server.api.marshalling.MarshallerFactory;
import org.kie.server.api.marshalling.MarshallingFormat;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.api.model.instance.SolverInstance;
import org.kie.server.api.model.instance.SolverInstanceList;
import org.kie.server.client.KieServicesConfiguration;
import org.kie.server.client.jms.AsyncResponseHandler;
import org.kie.server.client.jms.BlockingResponseCallback;
import org.kie.server.client.jms.FireAndForgetResponseHandler;
import org.kie.server.client.jms.RequestReplyResponseHandler;
import org.kie.server.client.jms.ResponseCallback;
import org.kie.server.client.jms.ResponseHandler;
import org.kie.server.integrationtests.category.JMSOnly;
import org.kie.server.integrationtests.optaplanner.OptaplannerKieServerBaseIntegrationTest;
import org.kie.server.integrationtests.shared.KieServerDeployer;
import org.kie.server.integrationtests.shared.KieServerSynchronization;
import org.optaplanner.core.api.domain.solution.Solution;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

@Category({JMSOnly.class})
public class OptaPlannerJmsResponseHandlerIntegrationTest extends OptaplannerKieServerBaseIntegrationTest {

    private static final ReleaseId RELEASE_ID = new ReleaseId("org.kie.server.testing", "cloudbalance", "1.0.0.Final");

    private static final String CONTAINER_1_ID = "cloudbalance";
    private static final String SOLVER_1_ID = "cloudsolver";
    private static final String SOLVER_1_CONFIG = "META-INF/cloudbalance-solver.xml";

    private static final String CLASS_CLOUD_BALANCE = "org.kie.server.testing.CloudBalance";
    private static final String CLASS_CLOUD_COMPUTER = "org.kie.server.testing.CloudComputer";
    private static final String CLASS_CLOUD_PROCESS = "org.kie.server.testing.CloudProcess";
    private static final String CLASS_CLOUD_GENERATOR = "org.kie.server.testing.CloudBalancingGenerator";

    @Parameterized.Parameters(name = "{index}: {0}")
    public static Collection<Object[]> data() {
        KieServicesConfiguration jmsConfiguration = createKieServicesJmsConfiguration();

        return new ArrayList<Object[]>(Arrays.asList(new Object[][]{
                {MarshallingFormat.JAXB, jmsConfiguration},
                {MarshallingFormat.JSON, jmsConfiguration},
                {MarshallingFormat.XSTREAM, jmsConfiguration}
        }));
    }

    @BeforeClass
    public static void deployArtifacts() {
        KieServerDeployer.buildAndDeployCommonMavenParent();
        KieServerDeployer.buildAndDeployMavenProject(ClassLoader.class.getResource("/kjars-sources/cloudbalance").getFile());

        kieContainer = KieServices.Factory.get().newKieContainer(RELEASE_ID);

        createContainer(CONTAINER_1_ID, RELEASE_ID);
    }

    @Override
    protected void addExtraCustomClasses(Map<String, Class<?>> extraClasses) throws Exception {
        extraClasses.put(CLASS_CLOUD_BALANCE, Class.forName(CLASS_CLOUD_BALANCE, true, kieContainer.getClassLoader()));
        extraClasses.put(CLASS_CLOUD_COMPUTER, Class.forName(CLASS_CLOUD_COMPUTER, true, kieContainer.getClassLoader()));
        extraClasses.put(CLASS_CLOUD_PROCESS, Class.forName(CLASS_CLOUD_PROCESS, true, kieContainer.getClassLoader()));
    }

    @Test
    public void testSolverWithAsyncResponseHandler() throws Exception {
        Marshaller marshaller = MarshallerFactory.getMarshaller(new HashSet<Class<?>>(extraClasses.values()),
                configuration.getMarshallingFormat(), client.getClassLoader());
        ResponseCallback responseCallback = new BlockingResponseCallback(marshaller);
        ResponseHandler asyncResponseHandler = new AsyncResponseHandler(responseCallback);
        solverClient.setResponseHandler(asyncResponseHandler);

        ServiceResponse<?> response = solverClient.createSolver(CONTAINER_1_ID, SOLVER_1_ID, SOLVER_1_CONFIG);
        assertThat(response);
        SolverInstance solver = responseCallback.get(SolverInstance.class);
        assertThat(solver).isNotNull();
        assertThat(solver.getContainerId()).isEqualTo(CONTAINER_1_ID);
        assertThat(solver.getSolverId()).isEqualTo(SOLVER_1_ID);

        response = solverClient.getSolvers(CONTAINER_1_ID);
        assertThat(response);
        SolverInstanceList solverList = responseCallback.get(SolverInstanceList.class);
        assertThat(solverList).isNotNull();
        assertThat(solverList.getContainers()).isNotNull().isNotEmpty().hasSize(1);
        solver = solverList.getContainers().get(0);
        assertThat(solver.getSolverId()).isEqualTo(SOLVER_1_ID);
        assertThat(solver.getStatus()).isEqualTo(SolverInstance.SolverStatus.NOT_SOLVING);

        solver = new SolverInstance();
        solver.setStatus(SolverInstance.SolverStatus.SOLVING);
        solver.setPlanningProblem(loadPlanningProblem(5, 15));
        response = solverClient.updateSolverState(CONTAINER_1_ID, SOLVER_1_ID, solver);
        assertThat(response);
        solver = responseCallback.get(SolverInstance.class);
        assertThat(solver.getSolverId()).isEqualTo(SOLVER_1_ID);
        assertThat(solver.getStatus()).isEqualTo(SolverInstance.SolverStatus.SOLVING);

        response = solverClient.getSolverState(CONTAINER_1_ID, SOLVER_1_ID);
        assertThat(response);
        solver = responseCallback.get(SolverInstance.class);
        assertThat(solver.getSolverId()).isEqualTo(SOLVER_1_ID);
        assertThat(solver.getStatus()).isEqualTo(SolverInstance.SolverStatus.SOLVING);

        response = solverClient.disposeSolver(CONTAINER_1_ID, SOLVER_1_ID);
        assertThat(response);
        responseCallback.get(SolverInstance.class);

        response = solverClient.getSolvers(CONTAINER_1_ID);
        assertThat(response);
        solverList = responseCallback.get(SolverInstanceList.class);
        assertThat(solverList).isNotNull();
        assertThat(solverList.getContainers()).isNullOrEmpty();
    }

    @Test
    public void testSolverWithFireAndForgetResponseHandler() throws Exception {
        solverClient.setResponseHandler(new FireAndForgetResponseHandler());
        ServiceResponse<?> response = solverClient.createSolver(CONTAINER_1_ID, SOLVER_1_ID, SOLVER_1_CONFIG);
        assertThat(response);

        solverClient.setResponseHandler(new RequestReplyResponseHandler());
        KieServerSynchronization.waitForSolver(solverClient, CONTAINER_1_ID, SOLVER_1_ID);

        solverClient.setResponseHandler(new FireAndForgetResponseHandler());
        SolverInstance solver = new SolverInstance();
        solver.setStatus(SolverInstance.SolverStatus.SOLVING);
        solver.setPlanningProblem(loadPlanningProblem(5, 15));
        response = solverClient.updateSolverState(CONTAINER_1_ID, SOLVER_1_ID, solver);
        assertThat(response);

        solverClient.setResponseHandler(new RequestReplyResponseHandler());
        KieServerSynchronization.waitForSolverStatus(solverClient, CONTAINER_1_ID, SOLVER_1_ID, SolverInstance.SolverStatus.SOLVING);

        solverClient.setResponseHandler(new FireAndForgetResponseHandler());
        response = solverClient.disposeSolver(CONTAINER_1_ID, SOLVER_1_ID);
        assertThat(response);

        solverClient.setResponseHandler(new RequestReplyResponseHandler());
        KieServerSynchronization.waitForSolverDispose(solverClient, CONTAINER_1_ID, SOLVER_1_ID);

        ServiceResponse<SolverInstanceList> solverListResponse = solverClient.getSolvers(CONTAINER_1_ID);
        assertThat(solverListResponse).isNotNull();
        SolverInstanceList solverList = solverListResponse.getResult();
        assertThat(solverList).isNotNull();
        assertThat(solverList.getContainers()).isNullOrEmpty();
    }

    public Solution loadPlanningProblem(int computerListSize, int processListSize) {
        Solution problem = null;
        try {
            Class<?> cbgc = kieContainer.getClassLoader().loadClass(CLASS_CLOUD_GENERATOR);
            Object cbgi = cbgc.newInstance();

            Method method = cbgc.getMethod("createCloudBalance", int.class, int.class);
            problem = (Solution) method.invoke(cbgi, computerListSize, processListSize);
        } catch (Exception e) {
            e.printStackTrace();
            fail("Exception trying to create cloud balance unsolved problem.");
        }
        return problem;
    }

}
