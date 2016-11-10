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

package org.kie.server.integrationtests.policy;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kie.api.KieServices;
import org.kie.api.command.BatchExecutionCommand;
import org.kie.api.command.Command;
import org.kie.api.runtime.ExecutionResults;
import org.kie.api.runtime.KieContainer;
import org.kie.server.api.model.KieContainerResource;
import org.kie.server.api.model.KieContainerResourceList;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.integrationtests.shared.KieServerAssert;
import org.kie.server.integrationtests.shared.KieServerDeployer;
import org.kie.server.integrationtests.shared.KieServerSynchronization;

import static org.junit.Assert.*;

public class KieServerPolicyDroolsIntegrationTest extends KieServerPolicyBaseIntegrationTest {
    private static final ReleaseId kjar1 = new ReleaseId("org.kie.server.testing", "container-isolation-kjar1",
            "1.0.0.Final");
    private static final ReleaseId kjar101 = new ReleaseId("org.kie.server.testing", "container-isolation-kjar1",
            "1.0.1.Final");

    private static final String CONTAINER_ALIAS = "container-isolation";
    private static final String CONTAINER_ID = "container-isolation-kjar1";
    private static final String CONTAINER_ID_101 = "container-isolation-kjar101";
    private static final String KIE_SESSION_1 = "kjar1.session";
    private static final String PERSON_OUT_IDENTIFIER = "person";
    private static final String PERSON_CLASS_NAME = "org.kie.server.testing.Person";

    @BeforeClass
    public static void deployArtifacts() {
        KieServerDeployer.buildAndDeployCommonMavenParent();
        KieServerDeployer.buildAndDeployMavenProject(ClassLoader.class.getResource("/kjars-sources/container-isolation-kjar1").getFile());
        KieServerDeployer.buildAndDeployMavenProject(ClassLoader.class.getResource("/kjars-sources/container-isolation-kjar101").getFile());
    }


    protected void createExtraContainer() {
        KieContainerResource containerResource = new KieContainerResource(CONTAINER_ID_101, kjar101);
        containerResource.setContainerAlias(CONTAINER_ALIAS);
        client.createContainer(CONTAINER_ID_101, containerResource);
    }

    @Before
    public void cleanContainers() {
        disposeAllContainers();
        createContainer(CONTAINER_ID, kjar1, CONTAINER_ALIAS);
    }

    @After
    public void removeExtraContainer() {
        if (client != null) {
            client.disposeContainer(CONTAINER_ID_101);
        }
    }

    @Override
    protected void addExtraCustomClasses(Map<String, Class<?>> extraClasses) throws Exception {
        KieContainer kieContainer = KieServices.Factory.get().newKieContainer(kjar1);
        extraClasses.put(PERSON_CLASS_NAME, Class.forName(PERSON_CLASS_NAME, true, kieContainer.getClassLoader()));
    }

    @Test
    public void testKeepLatestContainerOnlyPolicy() throws Exception {
        Object person = createInstance(PERSON_CLASS_NAME);
        List<Command<?>> commands = new ArrayList<Command<?>>();
        BatchExecutionCommand batchExecution1 = commandsFactory.newBatchExecution(commands, KIE_SESSION_1);

        commands.add(commandsFactory.newInsert(person, PERSON_OUT_IDENTIFIER));
        commands.add(commandsFactory.newFireAllRules());

        ServiceResponse<ExecutionResults> response1 = ruleClient.executeCommandsWithResults(CONTAINER_ALIAS, batchExecution1);
        KieServerAssert.assertSuccess(response1);
        ExecutionResults result1 = response1.getResult();

        Object outcome = result1.getValue(PERSON_OUT_IDENTIFIER);
        assertEquals("Person's id should be 'Person from kjar1'!", "Person from kjar1", valueOf(outcome, "id"));

        ServiceResponse<KieContainerResourceList> containersResponse = client.listContainers();
        KieServerAssert.assertSuccess(containersResponse);

        List<KieContainerResource> containerResources = containersResponse.getResult().getContainers();
        assertEquals(1, containerResources.size());

        createExtraContainer();

        person = createInstance(PERSON_CLASS_NAME);
        commands = new ArrayList<Command<?>>();
        BatchExecutionCommand batchExecution2 = commandsFactory.newBatchExecution(commands, KIE_SESSION_1);

        commands.add(commandsFactory.newInsert(person, PERSON_OUT_IDENTIFIER));
        commands.add(commandsFactory.newFireAllRules());

        ServiceResponse<ExecutionResults> response2 = ruleClient.executeCommandsWithResults(CONTAINER_ALIAS, batchExecution2);
        KieServerAssert.assertSuccess(response2);
        ExecutionResults result2 = response2.getResult();

        Object outcome2 = result2.getValue(PERSON_OUT_IDENTIFIER);
        assertEquals("Person's id should be 'Person from kjar101'!", "Person from kjar101", valueOf(outcome2, "id"));

        // wait for policy to be activated
        KieServerSynchronization.waitForKieServerSynchronization(client, 1);

        containersResponse = client.listContainers();
        KieServerAssert.assertSuccess(containersResponse);

        containerResources = containersResponse.getResult().getContainers();
        assertEquals(1, containerResources.size());

        ReleaseId latestContainerReleaseId = containerResources.get(0).getReleaseId();
        assertEquals(kjar101, latestContainerReleaseId);
    }
}
