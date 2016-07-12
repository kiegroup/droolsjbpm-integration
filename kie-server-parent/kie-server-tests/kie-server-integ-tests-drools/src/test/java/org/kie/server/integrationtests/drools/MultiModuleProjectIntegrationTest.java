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

package org.kie.server.integrationtests.drools;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;
import org.kie.api.KieServices;
import org.kie.api.command.BatchExecutionCommand;
import org.kie.api.command.Command;
import org.kie.api.runtime.ExecutionResults;
import org.kie.api.runtime.KieContainer;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.ServiceResponse;

import static org.junit.Assert.assertEquals;
import org.kie.server.integrationtests.shared.KieServerAssert;
import org.kie.server.integrationtests.shared.KieServerDeployer;

public class MultiModuleProjectIntegrationTest extends DroolsKieServerBaseIntegrationTest {

    private static ReleaseId releaseIdRules1 = new ReleaseId("org.kie.server.testing", "multimodule-project-rules1",
            "2.0.0.Final");
    private static ReleaseId releaseIdRules2 = new ReleaseId("org.kie.server.testing", "multimodule-project-rules2",
            "2.0.0.Final");

    private static final String CONTAINER_1_ID = "multimodule-rules1";
    private static final String CONTAINER_2_ID = "multimodule-rules2";
    private static final String KIE_SESSION = "kbase.session";
    private static final String CAR_CLASS_NAME = "org.kie.server.testing.multimodule.domain.Car";
    private static final String CAR_OUT_IDENTIFIER = "car";
    private static final String BUS_CLASS_NAME = "org.kie.server.testing.multimodule.domain.Bus";
    private static final String BUS_OUT_IDENTIFIER = "bus";

    @BeforeClass
    public static void buildAndDeployArtifacts() {
        KieServerDeployer.buildAndDeployCommonMavenParent();
        // the parent will build and deploy also all of its modules, so no need to deploy them individually
        KieServerDeployer.buildAndDeployMavenProject(ClassLoader.class.getResource("/kjars-sources/multimodule-project").getFile());

        disposeAllContainers();
        createContainer(CONTAINER_1_ID, releaseIdRules1);
        createContainer(CONTAINER_2_ID, releaseIdRules2);
    }

    @Override
    protected void addExtraCustomClasses(Map<String, Class<?>> extraClasses) throws Exception {
        KieContainer kieContainer = KieServices.Factory.get().newKieContainer(releaseIdRules1);
        extraClasses.put(CAR_CLASS_NAME, Class.forName(CAR_CLASS_NAME, true, kieContainer.getClassLoader()));
        kieContainer = KieServices.Factory.get().newKieContainer(releaseIdRules2);
        extraClasses.put(BUS_CLASS_NAME, Class.forName(BUS_CLASS_NAME, true, kieContainer.getClassLoader()));
    }

    @Test
    public void testCreateMultipleContainersAndExecuteRules() {
        Object car = createInstance(CAR_CLASS_NAME);
        List<Command<?>> commands = new ArrayList<Command<?>>();
        BatchExecutionCommand batchExecution1 = commandsFactory.newBatchExecution(commands, KIE_SESSION);

        commands.add(commandsFactory.newInsert(car, CAR_OUT_IDENTIFIER));
        commands.add(commandsFactory.newFireAllRules());

        ServiceResponse<ExecutionResults> response1 = ruleClient.executeCommandsWithResults(CONTAINER_1_ID, batchExecution1);
        KieServerAssert.assertSuccess(response1);
        ExecutionResults result = response1.getResult();

        Object outcome = result.getValue(CAR_OUT_IDENTIFIER);
        assertEquals("Driving car!", valueOf(outcome, "message"));

        Object bus = createInstance(BUS_CLASS_NAME);
        commands = new ArrayList<Command<?>>();
        BatchExecutionCommand batchExecution2 = commandsFactory.newBatchExecution(commands, KIE_SESSION);

        commands.add(commandsFactory.newInsert(bus, BUS_OUT_IDENTIFIER));
        commands.add(commandsFactory.newFireAllRules());

        ServiceResponse<ExecutionResults> response2 = ruleClient.executeCommandsWithResults(CONTAINER_2_ID, batchExecution2);

        KieServerAssert.assertSuccess(response2);
        ExecutionResults result2 = response2.getResult();

        Object outcome2 = result2.getValue(BUS_OUT_IDENTIFIER);
        assertEquals("Driving bus!", valueOf(outcome2, "message"));
    }

}
