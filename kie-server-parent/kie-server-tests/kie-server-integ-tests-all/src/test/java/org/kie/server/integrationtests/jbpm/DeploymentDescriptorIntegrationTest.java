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

package org.kie.server.integrationtests.jbpm;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;
import org.kie.api.KieServices;
import org.kie.api.command.BatchExecutionCommand;
import org.kie.api.command.Command;
import org.kie.api.runtime.ExecutionResults;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.integrationtests.shared.KieServerAssert;
import org.kie.server.integrationtests.shared.KieServerDeployer;


public class DeploymentDescriptorIntegrationTest extends JbpmKieServerBaseIntegrationTest {

    private static ReleaseId releaseId = new ReleaseId("org.kie.server.testing", "deployment-descriptor-project",
            "1.0.0.Final");

    private static final String CONTAINER_ID = "deployment-descriptor-project";
    private static final String PERSON_CLASS_NAME = "org.jbpm.data.Person";
    private static final String PERSON_NAME_FIELD = "name";
    private static final String GLOBAL_PERSON_IDENTIFIER = "person";
    private static final String GLOBAL_PERSON_NAME = "Bob";


    @BeforeClass
    public static void buildAndDeployArtifacts() {
        KieServerDeployer.buildAndDeployCommonMavenParent();
        KieServerDeployer.buildAndDeployMavenProject(ClassLoader.class.getResource("/kjars-sources/deployment-descriptor-project").getFile());

        kieContainer = KieServices.Factory.get().newKieContainer(releaseId);

        createContainer(CONTAINER_ID, releaseId);
    }

    @Override
    protected void addExtraCustomClasses(Map<String, Class<?>> extraClasses) throws Exception {
        extraClasses.put(PERSON_CLASS_NAME, Class.forName(PERSON_CLASS_NAME, true, kieContainer.getClassLoader()));
    }

    @Test
    public void testGlobalVariableFromDeploymentDescriptor() throws Exception {
        List<Command<?>> commands = new ArrayList<Command<?>>();
        BatchExecutionCommand executionCommand = commandsFactory.newBatchExecution(commands, CONTAINER_ID);

        // retrieve global variable set in deployment descriptor
        commands.add(commandsFactory.newGetGlobal(GLOBAL_PERSON_IDENTIFIER));

        ServiceResponse<ExecutionResults> reply = ruleClient.executeCommandsWithResults(CONTAINER_ID, executionCommand);
        assertEquals(ServiceResponse.ResponseType.SUCCESS, reply.getType());

        ExecutionResults actualData = reply.getResult();
        assertNotNull(actualData);
        Object personVar = actualData.getValue(GLOBAL_PERSON_IDENTIFIER);
        assertNotNull(personVar);
        assertEquals(GLOBAL_PERSON_NAME, valueOf(personVar, PERSON_NAME_FIELD));
    }

    @Test
    public void testPerRequestRuntimeStrategy() throws Exception {
        String personOutIdentifier = "personOut";
        String personName = USER_YODA;

        List<Command<?>> commands = new ArrayList<Command<?>>();
        BatchExecutionCommand executionCommand = commandsFactory.newBatchExecution(commands, CONTAINER_ID);

        // insert person object to working memory
        Object createPersonInstance = createPersonInstance(personName);
        commands.add(commandsFactory.newInsert(createPersonInstance, personOutIdentifier));
        commands.add(commandsFactory.newGetObjects(personOutIdentifier));
        ServiceResponse<ExecutionResults> reply = ruleClient.executeCommandsWithResults(CONTAINER_ID, executionCommand);
        assertEquals(ServiceResponse.ResponseType.SUCCESS, reply.getType());

        ExecutionResults actualData = reply.getResult();
        assertNotNull(actualData);
        ArrayList<Object> personVar = (ArrayList<Object>) actualData.getValue(personOutIdentifier);
        assertEquals(1, personVar.size());
        assertEquals(personName, valueOf(personVar.get(0), PERSON_NAME_FIELD));

        // try to retrieve person object by new request
        commands.clear();
        commands.add(commandsFactory.newGetObjects(personOutIdentifier));
        reply = ruleClient.executeCommandsWithResults(CONTAINER_ID, executionCommand);
        assertEquals(ServiceResponse.ResponseType.SUCCESS, reply.getType());

        actualData = reply.getResult();
        assertNotNull(actualData);
        personVar = (ArrayList<Object>) actualData.getValue(personOutIdentifier);
        KieServerAssert.assertNullOrEmpty("Person object was returned!", personVar);
    }
}
