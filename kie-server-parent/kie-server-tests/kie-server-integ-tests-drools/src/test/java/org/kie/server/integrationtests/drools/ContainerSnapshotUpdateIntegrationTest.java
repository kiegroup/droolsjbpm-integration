/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Test;
import org.kie.api.KieServices;
import org.kie.api.command.BatchExecutionCommand;
import org.kie.api.command.Command;
import org.kie.api.runtime.ExecutionResults;
import org.kie.api.runtime.KieContainer;
import org.kie.server.api.model.KieScannerResource;
import org.kie.server.api.model.KieScannerStatus;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.integrationtests.shared.KieServerAssert;
import org.kie.server.integrationtests.shared.KieServerDeployer;
import org.kie.server.integrationtests.shared.KieServerReflections;
import org.kie.server.integrationtests.shared.KieServerSynchronization;

import static org.assertj.core.api.Assertions.assertThat;

public class ContainerSnapshotUpdateIntegrationTest extends DroolsKieServerBaseIntegrationTest {
    private static final ReleaseId kjar1 = new ReleaseId("org.kie.server.testing", "container-isolation-kjar", "1.0.0-SNAPSHOT");
    private static final ReleaseId kjar2 = new ReleaseId("org.kie.server.testing", "container-isolation-kjar", "1.0.0-SNAPSHOT");

    private static final String CONTAINER_ID = "container-update";
    private static final String KIE_SESSION = "kjar1.session";
    private static final String PERSON_OUT_IDENTIFIER = "person";
    private static final String PERSON_CLASS_NAME = "org.kie.server.testing.Person";

    @After
    public void cleanContainers() throws IOException {
        disposeAllContainers();
        KieServerDeployer.cleanAllRepositories();
    }

    @Test
    public void testKieSessionWithUpdatedContainer() throws Exception {
        // Create container with first version
        KieServerDeployer.buildAndDeployCommonMavenParent();
        KieServerDeployer.buildAndDeployMavenProjectFromResource("/kjars-sources/container-isolation-kjar-SNAPSHOT-1");
        createContainer(CONTAINER_ID, kjar1);

        reinitClient(kjar1);

        assertThat(executeCommand()).isEqualTo("Person from kjar1");

        // now build and deploy second container with same snapshot version.
        KieServerDeployer.buildAndDeployMavenProjectFromResource("/kjars-sources/container-isolation-kjar-SNAPSHOT-2");
        KieServerAssert.assertSuccess(client.updateScanner(CONTAINER_ID, new KieScannerResource(KieScannerStatus.STARTED, 1_000L)));

        reinitClient(kjar2);

        KieServerSynchronization.waitForCondition(() -> {
            String personId = executeCommand();
            return personId != null && personId.equals("Person from kjar2");
        });
    }

    /**
     * Reinit client to contain correct extra class.
     *
     * @throws Exception
     */
    private void reinitClient(ReleaseId releaseId) throws Exception {
        configuration.clearExtraClasses();

        KieContainer kieContainer = KieServices.Factory.get().newKieContainer(releaseId);
        extraClasses.put(PERSON_CLASS_NAME, Class.forName(PERSON_CLASS_NAME, true, kieContainer.getClassLoader()));
        client = createDefaultClient();
    }

    /**
     * Execute command and return person id.
     *
     * @return Person id.
     */
    private String executeCommand() {
        Object person = createInstance(PERSON_CLASS_NAME);
        List<Command<?>> commands = new ArrayList<Command<?>>();
        BatchExecutionCommand batchExecution = commandsFactory.newBatchExecution(commands, KIE_SESSION);

        commands.add(commandsFactory.newInsert(person, PERSON_OUT_IDENTIFIER));
        commands.add(commandsFactory.newFireAllRules());

        ServiceResponse<ExecutionResults> response = ruleClient.executeCommandsWithResults(CONTAINER_ID, batchExecution);
        KieServerAssert.assertSuccess(response);
        ExecutionResults result = response.getResult();

        Object personOutcome = result.getValue(PERSON_OUT_IDENTIFIER);
        return (String) KieServerReflections.valueOf(personOutcome, "id");
    }
}
