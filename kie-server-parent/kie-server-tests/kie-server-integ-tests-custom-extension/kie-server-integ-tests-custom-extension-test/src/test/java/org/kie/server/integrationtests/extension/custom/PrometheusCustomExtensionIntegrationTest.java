/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
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

package org.kie.server.integrationtests.extension.custom;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.net.URI;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.assertj.core.api.Assertions;
import org.jboss.resteasy.client.jaxrs.BasicAuthentication;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kie.api.KieServices;
import org.kie.api.command.BatchExecutionCommand;
import org.kie.api.command.Command;
import org.kie.api.runtime.ExecutionResults;
import org.kie.dmn.api.core.DMNContext;
import org.kie.dmn.api.core.DMNResult;
import org.kie.server.api.model.KieContainerResource;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.api.model.instance.JobRequestInstance;
import org.kie.server.api.model.instance.SolverInstance;
import org.kie.server.client.DMNServicesClient;
import org.kie.server.client.JobServicesClient;
import org.kie.server.client.KieServicesClient;
import org.kie.server.client.RuleServicesClient;
import org.kie.server.client.SolverServicesClient;
import org.kie.server.integrationtests.config.TestConfig;
import org.kie.server.integrationtests.shared.KieServerAssert;
import org.kie.server.integrationtests.shared.KieServerDeployer;
import org.kie.server.integrationtests.shared.basetests.KieServerBaseIntegrationTest;
import org.kie.server.integrationtests.shared.basetests.RestJmsSharedBaseIntegrationTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class PrometheusCustomExtensionIntegrationTest extends RestJmsSharedBaseIntegrationTest {

    private static ReleaseId releaseId = new ReleaseId("org.kie.server.testing", "function-definition", "1.0.0.Final");
    private static ReleaseId releaseIdRuleflowGroup = new ReleaseId("org.kie.server.testing", "ruleflow-group", "1.0.0.Final");
    private static ReleaseId releaseIdCloudBalance = new ReleaseId("org.kie.server.testing", "cloudbalance", "1.0.0.Final");

    private static final String CONTAINER_ID  = "function-definition";
    private static final String CONTAINER_ID_RULE_FLOW  = "ruleflow-group";
    private static final String CONTAINER_ID_CLOUD_BALANCE = "cloudbalance";

    private static final String SOLVER_CONFIG = "cloudbalance-solver.xml";

    private static final String CLASS_CLOUD_BALANCE = "org.kie.server.testing.CloudBalance";
    private static final String CLASS_CLOUD_COMPUTER = "org.kie.server.testing.CloudComputer";
    private static final String CLASS_CLOUD_PROCESS = "org.kie.server.testing.CloudProcess";
    private static final String CLASS_ADD_COMPUTER_PROBLEM_FACT_CHANGE = "org.kie.server.testing.AddComputerProblemFactChange";
    private static final String CLASS_DELETE_COMPUTER_PROBLEM_FACT_CHANGE = "org.kie.server.testing.DeleteComputerProblemFactChange";
    private static final String CLASS_CLOUD_GENERATOR = "org.kie.server.testing.CloudBalancingGenerator";

    protected static final String PRINT_OUT_COMMAND = "org.jbpm.executor.commands.PrintOutCommand";

    private static Client httpClient;

    private DMNServicesClient dmnClient;
    private RuleServicesClient ruleClient;
    private SolverServicesClient solverClient;
    private JobServicesClient jobServicesClient;

    private static final long EXTENDED_TIMEOUT = 300_000L;

    @BeforeClass
    public static void deployArtifacts() {
        KieServerDeployer.buildAndDeployCommonMavenParent();
        KieServerDeployer.buildAndDeployMavenProjectFromResource("/kjars-sources/function-definition");
        KieServerDeployer.buildAndDeployMavenProjectFromResource("/kjars-sources/ruleflow-group");
        KieServerDeployer.buildAndDeployMavenProjectFromResource("/kjars-sources/cloudbalance");

        commandsFactory = KieServices.Factory.get().getCommands();
        kieContainer = KieServices.Factory.get().newKieContainer(releaseIdCloudBalance);

        KieServerBaseIntegrationTest.createContainer(CONTAINER_ID, releaseId);
        KieServerBaseIntegrationTest.createContainer(CONTAINER_ID_RULE_FLOW, releaseIdRuleflowGroup);

        // Having timeout issues due to kjar dependencies -> raised timeout.
        KieServicesClient client = createDefaultStaticClient(EXTENDED_TIMEOUT);
        ServiceResponse<KieContainerResource> reply = client.createContainer(CONTAINER_ID_CLOUD_BALANCE, new KieContainerResource(CONTAINER_ID_CLOUD_BALANCE, releaseIdCloudBalance));
        KieServerAssert.assertSuccess(reply);
    }

    @AfterClass
    public static void disposeContainers() {
        disposeAllContainers();
    }

    @AfterClass
    public static void closeHttpClient() {
        if (httpClient != null) {
            httpClient.close();
            httpClient = null;
        }
    }

    @Override
    protected void setupClients(KieServicesClient client) {
        dmnClient = client.getServicesClient( DMNServicesClient.class );
        ruleClient = client.getServicesClient(RuleServicesClient.class);
        solverClient = client.getServicesClient(SolverServicesClient.class);
        jobServicesClient = client.getServicesClient(JobServicesClient.class);
    }

    @Override
    protected void addExtraCustomClasses(Map<String, Class<?>> extraClasses) throws ClassNotFoundException {
        extraClasses.put(CLASS_CLOUD_BALANCE, Class.forName(CLASS_CLOUD_BALANCE, true, kieContainer.getClassLoader()));
        extraClasses.put(CLASS_CLOUD_COMPUTER, Class.forName(CLASS_CLOUD_COMPUTER, true, kieContainer.getClassLoader()));
        extraClasses.put(CLASS_CLOUD_PROCESS, Class.forName(CLASS_CLOUD_PROCESS, true, kieContainer.getClassLoader()));
        extraClasses.put(CLASS_ADD_COMPUTER_PROBLEM_FACT_CHANGE, Class.forName(CLASS_ADD_COMPUTER_PROBLEM_FACT_CHANGE, true, kieContainer.getClassLoader()));
        extraClasses.put(CLASS_DELETE_COMPUTER_PROBLEM_FACT_CHANGE, Class.forName(CLASS_DELETE_COMPUTER_PROBLEM_FACT_CHANGE, true, kieContainer.getClassLoader()));
    }

    @Test
    public void test_evaluateAll() {
        DMNContext dmnContext = dmnClient.newContext();
        dmnContext.set( "a", 10 );
        dmnContext.set( "b", 5 );
        ServiceResponse<DMNResult> evaluateAll = dmnClient.evaluateAll(CONTAINER_ID, dmnContext);
        KieServerAssert.assertSuccess(evaluateAll);

        DMNResult dmnResult = evaluateAll.getResult();

        Map<String, Object> mathInCtx = (Map<String, Object>) dmnResult.getContext().get( "Math" );
        Assertions.assertThat(mathInCtx).containsEntry("Sum", BigDecimal.valueOf( 15 ));

        Map<String, Object> dr0 = (Map<String, Object>) dmnResult.getDecisionResultByName("Math").getResult();
        Assertions.assertThat(dr0).containsEntry("Sum", BigDecimal.valueOf( 15 ));

        assertThat(getMetrics()).contains("random_gauge_nanosecond");
    }

    @Test
    public void testExecuteSimpleRuleFlowProcess() {
        String kieSession = "defaultKieSession";
        String processId = "simple-ruleflow";
        String listName = "list";
        String listOutputName = "output-list";

        List<Command<?>> commands = new ArrayList<Command<?>>();
        BatchExecutionCommand batchExecution = commandsFactory.newBatchExecution(commands, kieSession);

        commands.add(commandsFactory.newSetGlobal(listName, new ArrayList<String>(), listOutputName));
        commands.add(commandsFactory.newStartProcess(processId));
        commands.add(commandsFactory.newFireAllRules());
        commands.add(commandsFactory.newGetGlobal(listName, listOutputName));

        ServiceResponse<ExecutionResults> response = ruleClient.executeCommandsWithResults(CONTAINER_ID_RULE_FLOW, batchExecution);
        KieServerAssert.assertSuccess(response);

        assertThat(getMetrics()).contains("random_gauge_ruleflow_group_nanosecond", "ruleflow_group_name=\"ruleflow-group1\"", "ruleflow_group_name=\"ruleflow-group2\"");
    }

    @Test(timeout = 15_000)
    public void testExecuteSolver() throws Exception {
        String solverId = "my-solver";
        SolverInstance solverInstance = solverClient.createSolver(CONTAINER_ID_CLOUD_BALANCE, solverId, SOLVER_CONFIG);
        assertNotNull(solverInstance);
        assertEquals(SolverInstance.SolverStatus.NOT_SOLVING,
                     solverInstance.getStatus());

        // the following status starts the solver
        Object planningProblem = loadPlanningProblem(5, 15);
        solverClient.solvePlanningProblem(CONTAINER_ID_CLOUD_BALANCE, solverId, planningProblem);

        solverInstance = solverClient.getSolver(CONTAINER_ID_CLOUD_BALANCE, solverId);

        // solver should finish in 5 seconds, but we wait up to 15s before timing out
        final long SOLVER_STATUS_CHECK_PERIOD = 1000L;
        while (solverInstance.getStatus() == SolverInstance.SolverStatus.SOLVING) {
            solverInstance = solverClient.getSolver(CONTAINER_ID_CLOUD_BALANCE, solverId);
            assertNotNull(solverInstance);
            Thread.sleep(SOLVER_STATUS_CHECK_PERIOD);
        }

        assertEquals(SolverInstance.SolverStatus.NOT_SOLVING,
                     solverInstance.getStatus());

        solverClient.disposeSolver(CONTAINER_ID_CLOUD_BALANCE, solverId);

        assertThat(getMetrics()).contains("random_gauge_phase_lifecycle_best_solution_time_millis", "solver_id=\"my-solver\"");
    }

    @Test
    public void testDeploymentEvents() {
        // Deployment was created in @BeforeClass phase, the metric is already available for Prometheus
        assertThat(getMetrics()).contains("random_gauge_deployment_count");
    }

    @Test
    public void testScheduleAndCancelJob() {
        Calendar tomorrow = Calendar.getInstance();
        tomorrow.add(Calendar.DATE, 1);

        JobRequestInstance jobRequestInstance = createJobRequestInstance();
        jobRequestInstance.setScheduledDate(tomorrow.getTime());

        Long jobId = jobServicesClient.scheduleRequest(jobRequestInstance);
        jobServicesClient.cancelRequest(jobId);

        assertThat(getMetrics()).contains("random_gauge_jobs_scheduled_count");
    }

    private String getMetrics() {
        if (httpClient == null) {
            httpClient = new ResteasyClientBuilder().readTimeout(10, TimeUnit.SECONDS).build();
        }

        WebTarget webTarget = httpClient.target(URI.create(TestConfig.getKieServerHttpUrl()).resolve("../rest/metrics"));
        webTarget.register(new BasicAuthentication(TestConfig.getUsername(), TestConfig.getPassword()));

        try (Response response = webTarget.request(MediaType.TEXT_PLAIN).get()) {
            assertEquals(200, response.getStatus());
            return response.readEntity(String.class);
        }
    }

    private Object loadPlanningProblem(int computerListSize,
                                       int processListSize) throws NoSuchMethodException, ClassNotFoundException,
            IllegalAccessException, InstantiationException, InvocationTargetException {
        Class<?> cbgc = kieContainer.getClassLoader().loadClass(CLASS_CLOUD_GENERATOR);
        Object cbgi = cbgc.newInstance();

        Method method = cbgc.getMethod("createCloudBalance", int.class, int.class);
        return method.invoke(cbgi, computerListSize, processListSize);
    }

    private JobRequestInstance createJobRequestInstance() {
        JobRequestInstance jobRequestInstance = new JobRequestInstance();
        jobRequestInstance.setCommand(PRINT_OUT_COMMAND);
        jobRequestInstance.setData(new HashMap<>());
        return jobRequestInstance;
    }
}
