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

package org.kie.server.integrationtests.scenariosimulation;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;

import org.junit.BeforeClass;
import org.junit.Test;
import org.kie.api.KieServices;
import org.kie.server.api.model.KieServiceResponse.ResponseType;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.api.model.scenariosimulation.ScenarioSimulationResult;
import org.kie.server.client.KieServicesClient;
import org.kie.server.client.ScenarioSimulationServicesClient;
import org.kie.server.integrationtests.shared.KieServerDeployer;
import org.kie.server.integrationtests.shared.basetests.RestJmsSharedBaseIntegrationTest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class ScenarioSimulationIntegrationTest
        extends RestJmsSharedBaseIntegrationTest {

    private static final ReleaseId kjar1 = new ReleaseId(
            "org.kie.server.testing", "input-data-string",
            "1.0.0.Final");

    private static final String CONTAINER_1_ID = "input-data-string";
    private static final String DMN_SCESIM_SUCCESS_PATH = "/input-data-string-success.scesim";
    private static final String DMN_SCESIM_FAIL_PATH = "/input-data-string-fail.scesim";
    private static final String RULE_SCESIM_SUCCESS_PATH = "/rule-success.scesim";
    private static final String RULE_SCESIM_FAIL_PATH = "/rule-fail.scesim";
    private ScenarioSimulationServicesClient scenarioSimulationServicesClient;

    @Override
    protected void setupClients(KieServicesClient kieServicesClient) {
        this.scenarioSimulationServicesClient = kieServicesClient.getServicesClient(ScenarioSimulationServicesClient.class);
    }

    @BeforeClass
    public static void deployArtifacts() {
        KieServerDeployer.buildAndDeployCommonMavenParent();
        KieServerDeployer.buildAndDeployMavenProjectFromResource("/kjars-sources/input-data-string");

        kieContainer = KieServices.Factory.get().newKieContainer(kjar1);
        createContainer(CONTAINER_1_ID, kjar1);
    }

    @Test
    public void executeScenarioByPathTest() throws Exception {
        commonExecuteScenario((containerId, localPath) ->
                                      scenarioSimulationServicesClient.executeScenarioByPath(containerId, convertToAbsolutePath(localPath)));
    }

    @Test
    public void executeScenarioTest() throws Exception {
        commonExecuteScenario((containerId, content) ->
                                      scenarioSimulationServicesClient.executeScenario(containerId, loadResource(content)));
    }

    private void commonExecuteScenario(CheckedExceptionBiFunction<String, String, ServiceResponse<ScenarioSimulationResult>> methodToTest) throws Exception {

        ServiceResponse<ScenarioSimulationResult> dmnResponseSuccess = methodToTest.apply(CONTAINER_1_ID, DMN_SCESIM_SUCCESS_PATH);

        assertEquals(ResponseType.SUCCESS, dmnResponseSuccess.getType());
        assertEquals(1, dmnResponseSuccess.getResult().getRunCount());
        assertEquals("Test Scenario successfully executed", dmnResponseSuccess.getMsg());

        ServiceResponse<ScenarioSimulationResult> dmnResponseFail = methodToTest.apply(CONTAINER_1_ID, DMN_SCESIM_FAIL_PATH);

        assertEquals(ResponseType.FAILURE, dmnResponseFail.getType());
        assertEquals(1, dmnResponseFail.getResult().getRunCount());
        assertEquals("Test Scenario execution failed", dmnResponseFail.getMsg());
        assertFalse(dmnResponseFail.getResult().getFailures().isEmpty());
        assertEquals("#1: Scenario 'KO scenario' failed", dmnResponseFail.getResult().getFailures().get(0).getErrorMessage());

        ServiceResponse<ScenarioSimulationResult> ruleResponseSuccess = methodToTest.apply(CONTAINER_1_ID, RULE_SCESIM_SUCCESS_PATH);

        assertEquals(ResponseType.SUCCESS, ruleResponseSuccess.getType());
        assertEquals(1, ruleResponseSuccess.getResult().getRunCount());
        assertEquals("Test Scenario successfully executed", ruleResponseSuccess.getMsg());

        ServiceResponse<ScenarioSimulationResult> ruleResponseFail = methodToTest.apply(CONTAINER_1_ID, RULE_SCESIM_FAIL_PATH);

        assertEquals(ResponseType.FAILURE, ruleResponseFail.getType());
        assertEquals(1, ruleResponseFail.getResult().getRunCount());
        assertEquals("Test Scenario execution failed", ruleResponseFail.getMsg());
        assertFalse(ruleResponseFail.getResult().getFailures().isEmpty());
        assertEquals("#1: Scenario 'KO scenario' failed", ruleResponseFail.getResult().getFailures().get(0).getErrorMessage());
    }

    private static String loadResource(String path) throws IOException {
        return Files.lines(
                loadPathFromResource(path),
                StandardCharsets.UTF_8)
                .collect(Collectors.joining("\n"));
    }

    private static String convertToAbsolutePath(String path) {
        return loadPathFromResource(path).toFile().getAbsolutePath();
    }

    private static Path loadPathFromResource(String path) {
        return Paths.get(
                ScenarioSimulationIntegrationTest.class
                        .getResource(path)
                        .getFile());
    }

    @FunctionalInterface
    private interface CheckedExceptionBiFunction<T, U, R> {

        R apply(T t, U u) throws Exception;
    }
}