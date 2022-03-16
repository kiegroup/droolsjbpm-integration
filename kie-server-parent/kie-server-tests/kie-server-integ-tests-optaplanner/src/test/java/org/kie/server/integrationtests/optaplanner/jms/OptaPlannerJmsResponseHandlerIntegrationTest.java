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
import java.util.List;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runners.Parameterized;
import org.kie.api.KieServices;
import org.kie.server.api.marshalling.Marshaller;
import org.kie.server.api.marshalling.MarshallerFactory;
import org.kie.server.api.marshalling.MarshallingFormat;
import org.kie.server.api.model.KieContainerResource;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.api.model.instance.SolverInstance;
import org.kie.server.api.model.instance.SolverInstanceList;
import org.kie.server.client.KieServicesClient;
import org.kie.server.client.KieServicesConfiguration;
import org.kie.server.client.jms.AsyncResponseHandler;
import org.kie.server.client.jms.BlockingResponseCallback;
import org.kie.server.client.jms.FireAndForgetResponseHandler;
import org.kie.server.client.jms.RequestReplyResponseHandler;
import org.kie.server.client.jms.ResponseCallback;
import org.kie.server.client.jms.ResponseHandler;
import org.kie.server.integrationtests.category.JMSOnly;
import org.kie.server.integrationtests.optaplanner.OptaplannerKieServerBaseIntegrationTest;
import org.kie.server.integrationtests.shared.KieServerAssert;
import org.kie.server.integrationtests.shared.KieServerDeployer;
import org.kie.server.integrationtests.shared.KieServerSynchronization;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

@Category({JMSOnly.class})
public class OptaPlannerJmsResponseHandlerIntegrationTest extends OptaplannerKieServerBaseIntegrationTest {

    private static final ReleaseId RELEASE_ID = new ReleaseId("org.kie.server.testing", "cloudbalance", "1.0.0.Final");

    private static final String CONTAINER_1_ID = "cloudbalance";
    private static final String SOLVER_1_ID = "cloudsolver";
    private static final String SOLVER_1_CONFIG = "cloudbalance-solver.xml";

    private static final String CLASS_CLOUD_BALANCE = "org.kie.server.testing.CloudBalance";
    private static final String CLASS_CLOUD_COMPUTER = "org.kie.server.testing.CloudComputer";
    private static final String CLASS_CLOUD_PROCESS = "org.kie.server.testing.CloudProcess";
    private static final String CLASS_CLOUD_GENERATOR = "org.kie.server.testing.CloudBalancingGenerator";
    private static final String CLASS_ADD_COMPUTER_PROBLEM_FACT_CHANGE = "org.kie.server.testing.AddComputerProblemFactChange";
    private static final String CLASS_DELETE_COMPUTER_PROBLEM_FACT_CHANGE = "org.kie.server.testing.DeleteComputerProblemFactChange";

    private static final long EXTENDED_TIMEOUT = 300_000L;

    @Parameterized.Parameters(name = "{0}")
    public static Collection<Object[]> data() {
        KieServicesConfiguration jmsConfiguration = createKieServicesJmsConfiguration();

        return new ArrayList<>(Arrays.asList(new Object[][]{
                {MarshallingFormat.JAXB, jmsConfiguration},
                {MarshallingFormat.JSON, jmsConfiguration},
                {MarshallingFormat.XSTREAM, jmsConfiguration}
        }));
    }

    @BeforeClass
    public static void deployArtifacts() {
        KieServerDeployer.buildAndDeployCommonMavenParent();
        KieServerDeployer.buildAndDeployMavenProjectFromResource("/kjars-sources/cloudbalance");

        kieContainer = KieServices.Factory.get().newKieContainer(RELEASE_ID);

        // Having timeout issues due to kjar dependencies -> raised timeout.
        KieServicesClient client = createDefaultStaticClient(EXTENDED_TIMEOUT);
        ServiceResponse<KieContainerResource> reply = client.createContainer(CONTAINER_1_ID, new KieContainerResource(CONTAINER_1_ID, RELEASE_ID));
        KieServerAssert.assertSuccess(reply);
    }

    @Override
    protected void addExtraCustomClasses(Map<String, Class<?>> extraClasses) throws Exception {
        extraClasses.put(CLASS_CLOUD_BALANCE, Class.forName(CLASS_CLOUD_BALANCE, true, kieContainer.getClassLoader()));
        extraClasses.put(CLASS_CLOUD_COMPUTER, Class.forName(CLASS_CLOUD_COMPUTER, true, kieContainer.getClassLoader()));
        extraClasses.put(CLASS_CLOUD_PROCESS, Class.forName(CLASS_CLOUD_PROCESS, true, kieContainer.getClassLoader()));
        extraClasses.put(CLASS_ADD_COMPUTER_PROBLEM_FACT_CHANGE, Class.forName(CLASS_ADD_COMPUTER_PROBLEM_FACT_CHANGE, true, kieContainer.getClassLoader()));
        extraClasses.put(CLASS_DELETE_COMPUTER_PROBLEM_FACT_CHANGE, Class.forName(CLASS_DELETE_COMPUTER_PROBLEM_FACT_CHANGE, true, kieContainer.getClassLoader()));
    }

    @Test
    public void testSolverWithAsyncResponseHandler() throws Exception {
        Marshaller marshaller = MarshallerFactory.getMarshaller(new HashSet<>(extraClasses.values()),
                configuration.getMarshallingFormat(), client.getClassLoader());
        ResponseCallback responseCallback = new BlockingResponseCallback(marshaller);
        ResponseHandler asyncResponseHandler = new AsyncResponseHandler(responseCallback);
        solverClient.setResponseHandler(asyncResponseHandler);

        SolverInstance response = solverClient.createSolver(CONTAINER_1_ID, SOLVER_1_ID, SOLVER_1_CONFIG);
        assertThat(response).isNull();
        SolverInstance solver = responseCallback.get(SolverInstance.class);
        assertThat(solver).isNotNull();
        assertThat(solver.getContainerId()).isEqualTo(CONTAINER_1_ID);
        assertThat(solver.getSolverId()).isEqualTo(SOLVER_1_ID);

        List<SolverInstance> solverInstanceList = solverClient.getSolvers(CONTAINER_1_ID);
        assertThat(solverInstanceList).isNull();
        SolverInstanceList solverList = responseCallback.get(SolverInstanceList.class);
        assertThat(solverList).isNotNull();
        assertThat(solverList.getContainers()).isNotNull().isNotEmpty().hasSize(1);
        solver = solverList.getContainers().get(0);
        assertThat(solver.getSolverId()).isEqualTo(SOLVER_1_ID);
        assertThat(solver.getStatus()).isEqualTo(SolverInstance.SolverStatus.NOT_SOLVING);

        solverClient.solvePlanningProblem(CONTAINER_1_ID, SOLVER_1_ID, loadPlanningProblem(5, 15));
        // Make sure the service call result is consumed
        responseCallback.get(Void.class);

        response = solverClient.getSolver(CONTAINER_1_ID, SOLVER_1_ID);
        assertThat(response).isNull();
        solver = responseCallback.get(SolverInstance.class);
        assertThat(solver.getSolverId()).isEqualTo(SOLVER_1_ID);
        assertThat(solver.getStatus()).isEqualTo(SolverInstance.SolverStatus.SOLVING);

        solverClient.terminateSolverEarly(CONTAINER_1_ID, SOLVER_1_ID);
        // Make sure the service call result is consumed
        responseCallback.get(Void.class);

        response = solverClient.getSolver(CONTAINER_1_ID, SOLVER_1_ID);
        assertThat(response).isNull();
        solver = responseCallback.get(SolverInstance.class);
        assertThat(solver.getSolverId()).isEqualTo(SOLVER_1_ID);
        assertThat(solver.getStatus()).isIn(SolverInstance.SolverStatus.TERMINATING_EARLY, SolverInstance.SolverStatus.NOT_SOLVING);

        solverClient.disposeSolver(CONTAINER_1_ID, SOLVER_1_ID);
        // Make sure the service call result is consumed
        responseCallback.get(Void.class);

        solverClient.getSolvers(CONTAINER_1_ID);
        assertThat(response).isNull();
        solverList = responseCallback.get(SolverInstanceList.class);
        assertThat(solverList).isNotNull();
        assertThat(solverList.getContainers()).isNullOrEmpty();
    }

    @Test
    public void testSolverWithFireAndForgetResponseHandler() throws Exception {
        solverClient.setResponseHandler(new FireAndForgetResponseHandler());
        SolverInstance solverInstance = solverClient.createSolver(CONTAINER_1_ID, SOLVER_1_ID, SOLVER_1_CONFIG);
        assertThat(solverInstance).isNull();

        solverClient.setResponseHandler(new RequestReplyResponseHandler());
        KieServerSynchronization.waitForSolver(solverClient, CONTAINER_1_ID, SOLVER_1_ID);

        solverClient.setResponseHandler(new FireAndForgetResponseHandler());
        solverClient.solvePlanningProblem(CONTAINER_1_ID, SOLVER_1_ID, loadPlanningProblem(5, 15));

        solverClient.setResponseHandler(new RequestReplyResponseHandler());
        KieServerSynchronization.waitForSolverStatus(solverClient, CONTAINER_1_ID, SOLVER_1_ID, SolverInstance.SolverStatus.SOLVING);

        solverClient.setResponseHandler(new FireAndForgetResponseHandler());
        solverClient.terminateSolverEarly(CONTAINER_1_ID, SOLVER_1_ID);

        solverClient.setResponseHandler(new RequestReplyResponseHandler());
        KieServerSynchronization.waitForSolverStatus(solverClient, CONTAINER_1_ID, SOLVER_1_ID,
                SolverInstance.SolverStatus.NOT_SOLVING);

        solverClient.setResponseHandler(new FireAndForgetResponseHandler());
        solverClient.disposeSolver(CONTAINER_1_ID, SOLVER_1_ID);

        solverClient.setResponseHandler(new RequestReplyResponseHandler());
        KieServerSynchronization.waitForSolverDispose(solverClient, CONTAINER_1_ID, SOLVER_1_ID);

        List<SolverInstance> solverInstanceList = solverClient.getSolvers(CONTAINER_1_ID);
        assertThat(solverInstanceList).isNullOrEmpty();
    }

    public Object loadPlanningProblem(int computerListSize, int processListSize) {
        Object problem = null;
        try {
            Class<?> cbgc = kieContainer.getClassLoader().loadClass(CLASS_CLOUD_GENERATOR);
            Object cbgi = cbgc.newInstance();

            Method method = cbgc.getMethod("createCloudBalance", int.class, int.class);
            problem = method.invoke(cbgi, computerListSize, processListSize);
        } catch (Exception e) {
            e.printStackTrace();
            fail("Exception trying to create cloud balance unsolved problem.");
        }
        return problem;
    }
}
