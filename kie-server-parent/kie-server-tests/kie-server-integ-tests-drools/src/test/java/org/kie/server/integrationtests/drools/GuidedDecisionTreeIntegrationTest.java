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
import org.kie.server.api.model.KieContainerResource;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.ServiceResponse;

import static org.junit.Assert.*;

/**
 * Test used for verification of spreadsheet decision table processing.
 */
public class GuidedDecisionTreeIntegrationTest extends DroolsKieServerBaseIntegrationTest {

    private static ReleaseId releaseId = new ReleaseId("org.kie.server.testing", "tdrl-project", "1.0.0-SNAPSHOT");

    private static final String CONTAINER_ID = "tdrl";
    private static final String PERSON_CLASS_NAME = "org.kie.server.testing.Person";
    private static final String PERSON_OUT_IDENTIFIER = "person";
    private static final String PERSON_NAME = "Robert";
    private static final String PERSON_SURNAME_FIELD = "surname";

    private static ClassLoader kjarClassLoader;

    @BeforeClass
    public static void buildAndDeployArtifacts() {
        buildAndDeployCommonMavenParent();
        buildAndDeployMavenProject(ClassLoader.class.getResource("/kjars-sources/tdrl-project").getFile());

        kjarClassLoader = KieServices.Factory.get().newKieContainer(releaseId).getClassLoader();
    }

    @Override
    protected void addExtraCustomClasses(Map<String, Class<?>> extraClasses) throws Exception {
        extraClasses.put(PERSON_CLASS_NAME, Class.forName(PERSON_CLASS_NAME, true, kjarClassLoader));
    }

    @Test
    public void testExecuteGuidedDecisionTableRule() {
        assertSuccess(client.createContainer(CONTAINER_ID, new KieContainerResource(CONTAINER_ID, releaseId)));

        Object person = createInstance(PERSON_CLASS_NAME, PERSON_NAME);
        List<Command<?>> commands = new ArrayList<Command<?>>();
        BatchExecutionCommand batchExecution = commandsFactory.newBatchExecution(commands, "kbase1.stateless");

        commands.add(commandsFactory.newInsert(person, PERSON_OUT_IDENTIFIER));
        commands.add(commandsFactory.newFireAllRules());

        ServiceResponse<ExecutionResults> response = ruleClient.executeCommandsWithResults(CONTAINER_ID, batchExecution);
        assertSuccess(response);
        ExecutionResults results = response.getResult();
        Object value = results.getValue(PERSON_OUT_IDENTIFIER);
        assertEquals("Bob", valueOf(value, PERSON_SURNAME_FIELD));
    }
}
