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
import static org.junit.Assert.assertTrue;

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
import org.kie.server.integrationtests.shared.KieServerAssert;
import org.kie.server.integrationtests.shared.KieServerDeployer;


public class PerProcessInstanceIntegrationTest extends JbpmKieServerBaseIntegrationTest {

    private static ReleaseId releaseId = new ReleaseId("org.kie.server.testing", "per-process-instance-project",
            "1.0.0.Final");

    private static final String CONTAINER_ID = "per-process-instance-project";
    private static final String PROCESS_ID = "per-process-instance-project.usertask";

    private static final String PERSON_CLASS_NAME = "org.jbpm.data.Person";
    private static final String PERSON_NAME_FIELD = "name";

    private static final String PERSON_JOHN = "john";
    private static final String PERSON_MARY = "mary";


    @BeforeClass
    public static void buildAndDeployArtifacts() {
        KieServerDeployer.buildAndDeployCommonMavenParent();
        KieServerDeployer.buildAndDeployMavenProject(ClassLoader.class.getResource("/kjars-sources/per-process-instance-project").getFile());

        kieContainer = KieServices.Factory.get().newKieContainer(releaseId);
    }

    @Override
    protected void addExtraCustomClasses(Map<String, Class<?>> extraClasses) throws Exception {
        extraClasses.put(PERSON_CLASS_NAME, Class.forName(PERSON_CLASS_NAME, true, kieContainer.getClassLoader()));
    }

    @Test
    public void testProcessWithBusinessRuleTask() throws Exception {
        KieServerAssert.assertSuccess(client.createContainer(CONTAINER_ID, new KieContainerResource(CONTAINER_ID, releaseId)));

        Long processInstanceId1 = processClient.startProcess(CONTAINER_ID, PROCESS_ID);
        assertNotNull(processInstanceId1);
        assertTrue(processInstanceId1.longValue() > 0);

        Long processInstanceId2 = processClient.startProcess(CONTAINER_ID, PROCESS_ID);
        assertNotNull(processInstanceId2);
        assertTrue(processInstanceId2.longValue() > 0);

        try {
            // use container id as ksession id to use ksession from jBPM extension
            String ksessionId1 = CONTAINER_ID + "#" + processInstanceId1;
            String ksessionId2 = CONTAINER_ID + "#" + processInstanceId2;

            // Both process should have persisted facts separated from each other
            Object john = createPersonInstance(PERSON_JOHN);
            insertPersonFact(john, ksessionId1);
            checkSinglePersonFact(john, ksessionId1);

            Object mary = createPersonInstance(PERSON_MARY);
            insertPersonFact(mary, ksessionId2);
            checkSinglePersonFact(mary, ksessionId2);
        } finally {
            processClient.abortProcessInstance(CONTAINER_ID, processInstanceId1);
            processClient.abortProcessInstance(CONTAINER_ID, processInstanceId2);
        }
    }

    private void insertPersonFact(Object person, String kieSession) {
        // insert person as fact into working memory
        List<Command<?>> commands = new ArrayList<Command<?>>();
        BatchExecutionCommand executionCommand = commandsFactory.newBatchExecution(commands, kieSession);

        commands.add(commandsFactory.newInsert(person));

        KieServerAssert.assertSuccess(ruleClient.executeCommandsWithResults(CONTAINER_ID, executionCommand));
    }

    private void checkSinglePersonFact(Object person, String kieSession) {
        String personListOutputId = "people-out";

        // check that working memory contains just single person fact
        List<Command<?>> commands = new ArrayList<Command<?>>();
        BatchExecutionCommand executionCommand = commandsFactory.newBatchExecution(commands, kieSession);

        commands.add(commandsFactory.newGetObjects(personListOutputId));

        ServiceResponse<ExecutionResults> reply = ruleClient.executeCommandsWithResults(CONTAINER_ID, executionCommand);
        assertEquals(ServiceResponse.ResponseType.SUCCESS, reply.getType());

        ExecutionResults actualData = reply.getResult();
        assertNotNull(actualData);
        ArrayList<Object> personList = (ArrayList<Object>) actualData.getValue(personListOutputId);
        assertEquals(1, personList.size());
        assertEquals(valueOf(person, PERSON_NAME_FIELD), valueOf(personList.get(0), PERSON_NAME_FIELD));
    }
}
