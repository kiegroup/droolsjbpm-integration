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

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;
import org.kie.api.KieServices;
import org.kie.api.command.BatchExecutionCommand;
import org.kie.api.command.Command;
import org.kie.api.runtime.KieContainer;
import org.kie.server.api.model.KieContainerResource;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.ServiceResponse;

public class ContainerIsolationIntegrationTest extends DroolsKieServerBaseIntegrationTest {
    private static final ReleaseId kjar1 = new ReleaseId("org.kie.server.testing", "container-isolation-kjar1",
            "1.0.0.Final");
    private static final ReleaseId kjar2 = new ReleaseId("org.kie.server.testing", "container-isolation-kjar2",
            "1.0.0.Final");

    private static final String CONTAINER_1_ID = "container-isolation-kjar1";
    private static final String CONTAINER_2_ID = "container-isolation-kjar2";
    private static final String KIE_SESSION_1 = "kjar1.session";
    private static final String KIE_SESSION_2 = "kjar2.session";
    private static final String PERSON_OUT_IDENTIFIER = "person";
    private static final String PERSON_CLASS_NAME = "org.kie.server.testing.Person";

    @BeforeClass
    public static void deployArtifacts() {
        buildAndDeployCommonMavenParent();
        buildAndDeployMavenProject(ClassLoader.class.getResource("/kjars-sources/container-isolation-kjar1").getFile());
        buildAndDeployMavenProject(ClassLoader.class.getResource("/kjars-sources/container-isolation-kjar2").getFile());
    }

    @Override
    protected void addExtraCustomClasses(Map<String, Class<?>> extraClasses) throws Exception {
        KieContainer kieContainer = KieServices.Factory.get().newKieContainer(kjar1);
        extraClasses.put(PERSON_CLASS_NAME, Class.forName(PERSON_CLASS_NAME, true, kieContainer.getClassLoader()));
    }

    @Test 
    public void testUseClassWithSameFQNInDifferentContainers() throws Exception {
        assertSuccess(client.createContainer(CONTAINER_1_ID, new KieContainerResource(CONTAINER_1_ID, kjar1)));

        Object person = createInstance(PERSON_CLASS_NAME);
        List<Command<?>> commands = new ArrayList<Command<?>>();
        BatchExecutionCommand batchExecution1 = commandsFactory.newBatchExecution(commands, KIE_SESSION_1);

        commands.add(commandsFactory.newInsert(person, PERSON_OUT_IDENTIFIER));
        commands.add(commandsFactory.newFireAllRules());

        ServiceResponse<String> response1 = ruleClient.executeCommands(CONTAINER_1_ID, batchExecution1);
        assertSuccess(response1);
        String result1 = response1.getResult();
        assertTrue("Person's id should be 'Person from kjar1'!. Got result: " + result1, result1.contains("Person from kjar1"));

        // now execute the same commands, but for the second container. The rule in there should set different id
        // (namely "Person from kjar2") for the inserted person
        assertSuccess(client.createContainer(CONTAINER_2_ID, new KieContainerResource(CONTAINER_2_ID, kjar2)));

        person = createInstance(PERSON_CLASS_NAME);
        commands = new ArrayList<Command<?>>();
        BatchExecutionCommand batchExecution2 = commandsFactory.newBatchExecution(commands, KIE_SESSION_2);

        commands.add(commandsFactory.newInsert(person, PERSON_OUT_IDENTIFIER));
        commands.add(commandsFactory.newFireAllRules());

        ServiceResponse<String> response2 = ruleClient.executeCommands(CONTAINER_2_ID, batchExecution2);
        assertSuccess(response2);
        String result2 = response2.getResult();
        assertTrue("Person's id should be 'Person from kjar2'!. Got result: " + result2, result2.contains("Person from kjar2"));
    }

}
